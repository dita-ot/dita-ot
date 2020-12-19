/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2016 Jarno Elovirta
 *
 *  See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.module.reader;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.exception.DITAOTXMLErrorHandler;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.module.filter.SubjectScheme;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.reader.GenListModuleReader.Reference;
import org.dita.dost.reader.SubjectSchemeReader;
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.writer.DebugFilter;
import org.dita.dost.writer.NormalizeFilter;
import org.dita.dost.writer.ProfilingFilter;
import org.dita.dost.writer.ValidationFilter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static net.sf.saxon.s9api.streams.Steps.descendant;
import static org.dita.dost.reader.GenListModuleReader.isFormatDita;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.*;
import static org.dita.dost.util.XMLUtils.ancestors;
import static org.dita.dost.util.XMLUtils.toList;
import static org.dita.dost.writer.DitaWriterFilter.ATTRIBUTE_NAME_ORIG_FORMAT;

/**
 * ModuleElem for reading and serializing topics into temporary directory.
 *
 * @since 2.5
 */
public final class TopicReaderModule extends AbstractReaderModule {

    static final QName QNAME_HREF = new QName(ATTRIBUTE_NAME_HREF);
    static final QName QNAME_SCOPE = new QName(ATTRIBUTE_NAME_SCOPE);
    static final QName QNAME_FORMAT = new QName(ATTRIBUTE_NAME_FORMAT);
    static final QName QNAME_CLASS = new QName(ATTRIBUTE_NAME_CLASS);
    static final QName QNAME_ORIG_FORMAT = new QName(DITA_OT_NS, ATTRIBUTE_NAME_ORIG_FORMAT);

    public TopicReaderModule() {
        super();
        formatFilter = v -> !(Objects.equals(v, ATTR_FORMAT_VALUE_DITAMAP) || Objects.equals(v, ATTR_FORMAT_VALUE_DITAVAL));
    }

    @Override
    public AbstractPipelineOutput execute(final AbstractPipelineInput input) throws DITAOTException {
        try {
            parseInputParameters(input);
            init();

            readResourceFiles();
            readStartFile();
            processWaitList();

            handleConref();
            outputResult();

            job.write();
        } catch (final RuntimeException | DITAOTException e) {
            throw e;
        } catch (final Exception e) {
            throw new DITAOTException(e.getMessage(), e);
        }

        return null;
    }

    @Override
    void init() throws SAXException {
        super.init();

        if (filterUtils != null) {
            final Document doc = getMapDocument();
            if (doc != null) {
                final SubjectSchemeReader subjectSchemeReader = new SubjectSchemeReader();
                subjectSchemeReader.setLogger(logger);
                subjectSchemeReader.setJob(job);
                logger.debug("Loading subject schemes");
                final List<Element> subjectSchemes = toList(doc.getDocumentElement().getElementsByTagName("*"));
                subjectSchemes.stream()
                        .filter(SUBJECTSCHEME_ENUMERATIONDEF::matches)
                        .forEach(enumerationDef -> {
                            final Element schemeRoot = ancestors(enumerationDef)
                                    .filter(SUBMAP::matches)
                                    .findFirst()
                                    .orElse(doc.getDocumentElement());
                            subjectSchemeReader.processEnumerationDef(schemeRoot, enumerationDef);
                        });
                final SubjectScheme subjectScheme = subjectSchemeReader.getSubjectSchemeMap();
                filterUtils = filterUtils.refine(subjectScheme);
            }
        }
    }

    @Override
    void readResourceFiles() throws DITAOTException {
        if (!resources.isEmpty()) {
            for (URI resource : resources) {
                additionalResourcesSet.add(resource);
                final FileInfo fi = job.getFileInfo(resource);
                if (fi == null) {
                    addToWaitList(new Reference(resource));
                } else {
                    if (ATTR_FORMAT_VALUE_DITAMAP.equals(fi.format)) {
                        getStartDocuments(fi).forEach(this::addToWaitList);
                    } else {
                        if (fi.format == null) {
                            fi.format = ATTR_FORMAT_VALUE_DITA;
                            job.add(fi);
                        }
                        addToWaitList(new Reference(resource, fi.format));
                    }
                }
            }
            processWaitList();

            additionalResourcesSet.addAll(hrefTargetSet);
            additionalResourcesSet.addAll(conrefTargetSet);
            additionalResourcesSet.addAll(nonConrefCopytoTargetSet);
            additionalResourcesSet.addAll(outDitaFilesSet);
            additionalResourcesSet.addAll(conrefpushSet);
            additionalResourcesSet.addAll(keyrefSet);
            additionalResourcesSet.addAll(resourceOnlySet);
            additionalResourcesSet.addAll(fullTopicSet);
            additionalResourcesSet.addAll(fullMapSet);
            additionalResourcesSet.addAll(conrefSet);

            resourceOnlySet.clear();
        }
    }

    private Document getMapDocument() throws SAXException {
        final FileInfo fi = job.getFileInfo(f -> f.isInput).iterator().next();
        if (fi == null) {
            return null;
        }
        final URI currentFile = job.tempDirURI.resolve(fi.uri);
        try {
            logger.debug("Reading " + currentFile);
            return job.getStore().getDocument(currentFile);
        } catch (final IOException e) {
            throw new SAXException("Failed to parse " + currentFile, e);
        }
    }

    @Override
    public void readStartFile() throws DITAOTException {
        final FileInfo fi = job.getFileInfo(f -> f.isInput).iterator().next();
        if (fi == null) {
            addToWaitList(new Reference(job.getInputFile()));
        } else {
            if (ATTR_FORMAT_VALUE_DITAMAP.equals(fi.format)) {
                getStartDocuments(fi).forEach(this::addToWaitList);
            } else {
                if (fi.format == null) {
                    fi.format = ATTR_FORMAT_VALUE_DITA;
                    job.add(fi);
                }
                addToWaitList(new Reference(job.getInputFile(), fi.format));
            }
        }
    }

    private List<Reference> getStartDocuments(final FileInfo startFileInfo) throws DITAOTException {
        final List<Reference> res = new ArrayList<>();
        assert startFileInfo.src != null;
        final URI tmp = job.tempDirURI.resolve(startFileInfo.uri);
        try {
            final XdmNode source = job.getStore().getImmutableNode(tmp);
            logger.info("Reading " + tmp);
            final Predicate<? super XdmNode> isTopicref = xdmItem -> MAP_TOPICREF.matches(xdmItem.getAttributeValue(QNAME_CLASS));
            source.select(descendant(isTopicref)).forEach(xdmItem -> {
                final URI href = getHref(xdmItem);
                if (href != null) {
                    FileInfo fi = job.getFileInfo(startFileInfo.src.resolve(href));
                    if (fi == null) {
                        fi = job.getFileInfo(tmp.resolve(href));
                    }
                    assert fi != null;
                    assert fi.src != null;
                    String format = xdmItem.getAttributeValue(QNAME_ORIG_FORMAT);
                    if (format == null) {
                        format = xdmItem.getAttributeValue(QNAME_FORMAT);
                    }
                    res.add(new Reference(fi.src, format));
                    nonConrefCopytoTargetSet.add(fi.src);
                }
            });
        } catch (final IOException e) {
            throw new DITAOTException(e);
        }
        return res;
    }

    private URI getHref(final XdmNode in) {
        final URI href = toURI(in.getAttributeValue(QNAME_HREF));
        if (href == null) {
            return null;
        }
        final String scope = in.getAttributeValue(QNAME_SCOPE);
        if (!(scope == null || scope.equals(ATTR_SCOPE_VALUE_LOCAL))) {
            return null;
        }
        final String format = in.getAttributeValue(QNAME_FORMAT);
        if (!(format == null || ATTR_FORMAT_VALUE_DITA.equals(format))) {
            return null;
        }
        return stripFragment(href);
    }

    @Override
    List<XMLFilter> getProcessingPipe(final URI fileToParse) {
        assert fileToParse.isAbsolute();
        final List<XMLFilter> pipe = new ArrayList<>();

        if (genDebugInfo) {
            final DebugFilter debugFilter = new DebugFilter();
            debugFilter.setLogger(logger);
            debugFilter.setCurrentFile(currentFile);
            pipe.add(debugFilter);
        }

        if (filterUtils != null) {
            final ProfilingFilter profilingFilter = new ProfilingFilter();
            profilingFilter.setLogger(logger);
            profilingFilter.setJob(job);
            profilingFilter.setFilterUtils(filterUtils);
            profilingFilter.setCurrentFile(fileToParse);
            pipe.add(profilingFilter);
        }

        final ValidationFilter validationFilter = new ValidationFilter();
        validationFilter.setLogger(logger);
        validationFilter.setValidateMap(validateMap);
        validationFilter.setCurrentFile(fileToParse);
        validationFilter.setJob(job);
        validationFilter.setProcessingMode(processingMode);
        pipe.add(validationFilter);

        final NormalizeFilter normalizeFilter = new NormalizeFilter();
        normalizeFilter.setLogger(logger);
        pipe.add(normalizeFilter);

        pipe.add(topicFragmentFilter);

        if (INDEX_TYPE_ECLIPSEHELP.equals(transtype)) {
            exportAnchorsFilter.setCurrentFile(fileToParse);
            exportAnchorsFilter.setErrorHandler(new DITAOTXMLErrorHandler(fileToParse.toString(), logger));
            pipe.add(exportAnchorsFilter);
        }

        listFilter.setCurrentFile(fileToParse);
        listFilter.setErrorHandler(new DITAOTXMLErrorHandler(fileToParse.toString(), logger));
        pipe.add(listFilter);

        ditaWriterFilter.setDefaultValueMap(defaultValueMap);
        ditaWriterFilter.setCurrentFile(currentFile);
        ditaWriterFilter.setOutputFile(outputFile);
        pipe.add(ditaWriterFilter);

        return pipe;
    }

    @Override
    void categorizeReferenceFile(final Reference file) {
        // avoid files referred by coderef being added into wait list
        if (listFilter.getCoderefTargets().contains(file.filename)) {
            return;
        }
        if (formatFilter.test(file.format)) {
            if (isFormatDita(file.format) && !job.crawlTopics() &&
                    !listFilter.getConrefTargets().contains(file.filename)) {
                return;  // Do not process topics linked from within topics
            } else if (isFormatDita(file.format) && (!job.getOnlyTopicInMap() || listFilter.getConrefTargets().contains(file.filename))) {
                addToWaitList(file);
            } else if (ATTR_FORMAT_VALUE_IMAGE.equals(file.format)) {
                formatSet.add(file);
                if (!exists(file.filename)) {
                    logger.warn(MessageUtils.getMessage("DOTX008E", file.filename.toString()).toString());
                }
            } else {
                htmlSet.put(file.format, file.filename);
            }
        }
    }
}