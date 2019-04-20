/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2016 Jarno Elovirta
 *
 *  See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.module.reader;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.exception.DITAOTXMLErrorHandler;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.reader.GenListModuleReader.Reference;
import org.dita.dost.util.Job;
import org.dita.dost.writer.DebugFilter;
import org.dita.dost.writer.NormalizeFilter;
import org.dita.dost.writer.ProfilingFilter;
import org.dita.dost.writer.ValidationFilter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.XMLFilter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.exists;

/**
 * ModuleElem for reading and serializing topics into temporary directory.
 *
 * @since 2.5
 */
public final class MapReaderModule extends AbstractReaderModule {

    public MapReaderModule() {
        super();
        formatFilter = v -> Objects.equals(v, ATTR_FORMAT_VALUE_DITAMAP) || Objects.equals(v, ATTR_FORMAT_VALUE_DITAVAL);
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

            combine();

            job.write();
        } catch (final RuntimeException | DITAOTException e) {
            throw e;
        } catch (final Exception e) {
            throw new DITAOTException(e.getMessage(), e);
        }

        return null;
    }

    /**
     * Combines multiple inputs into a single root map.
     *
     * @throws DITAOTException if writing output fails
     */
    private void combine() throws DITAOTException {
        final URI rootTemp = tempFileNameScheme.generateTempFileName(rootFile);
        if (rootFiles.size() > 1) {
            final URI rootTempAbs = job.tempDirURI.resolve(rootTemp);
            logger.info("Writing " + rootTempAbs);
            try {
                final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                dbf.setNamespaceAware(true);
                final Document doc = dbf.newDocumentBuilder().newDocument();

                doc.appendChild(doc.createProcessingInstruction(PI_WORKDIR_TARGET_URI, job.tempDirURI.toString()));
                doc.appendChild(doc.createProcessingInstruction(PI_PATH2PROJ_TARGET_URI, "./"));
                doc.appendChild(doc.createProcessingInstruction(PI_PATH2ROOTMAP_TARGET_URI, "./"));

                final Element root = doc.createElement(MAP_MAP.localName);
                root.setAttribute(ATTRIBUTE_NAME_CLASS, MAP_MAP.toString());
                root.setAttribute(ATTRIBUTE_NAME_DOMAINS, "(map mapgroup-d)");
                root.setAttributeNS(DITA_NAMESPACE, ATTRIBUTE_PREFIX_DITAARCHVERSION + COLON + ATTRIBUTE_NAME_DITAARCHVERSION, "1.3");
                for (final URI file : rootFiles) {
                    final Job.FileInfo fi = job.getFileInfo(file);
                    final URI hrefTempAbs = job.tempDirURI.resolve(fi.uri);
                    final URI href = rootTempAbs.resolve(".").relativize(hrefTempAbs);

                    final Element ref = doc.createElement(MAP_TOPICREF.localName);
                    ref.setAttribute(ATTRIBUTE_NAME_CLASS, MAP_TOPICREF.toString());
                    ref.setAttribute(ATTRIBUTE_NAME_FORMAT, fi.format);
                    ref.setAttribute(ATTRIBUTE_NAME_HREF, href.toString());
                    root.appendChild(ref);
                }
                doc.appendChild(root);

                final Transformer serializer = TransformerFactory.newInstance().newTransformer();
                serializer.transform(new DOMSource(doc), new StreamResult(rootTempAbs.toString()));
            } catch (ParserConfigurationException | TransformerConfigurationException e) {
                throw new RuntimeException(e);
            } catch (TransformerException e) {
                throw new DITAOTException("Failed to serialize root file: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public void readStartFile() throws DITAOTException {
        for (final URI root : rootFiles) {
            addToWaitList(new Reference(root));
        }
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

        keydefFilter.setCurrentDir(fileToParse.resolve("."));
        keydefFilter.setErrorHandler(new DITAOTXMLErrorHandler(fileToParse.toString(), logger));
        pipe.add(keydefFilter);

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
        if (file.format == null || ATTR_FORMAT_VALUE_DITA.equals(file.format)) {
            return;
        }
        // Ignore topics
//        if (formatFilter.test(file.format)) {
        switch (file.format) {
            case ATTR_FORMAT_VALUE_DITAMAP:
                addToWaitList(file);
                break;
            case ATTR_FORMAT_VALUE_IMAGE:
                formatSet.add(file);
                if (!exists(file.filename)) {
                    logger.warn(MessageUtils.getMessage("DOTX008E", file.filename.toString()).toString());
                }
                break;
            case ATTR_FORMAT_VALUE_DITAVAL:
                formatSet.add(file);
                break;
            default:
                htmlSet.put(file.format, file.filename);
                break;
        }
//        }
    }

}