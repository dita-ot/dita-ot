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
import org.dita.dost.util.CatalogUtils;
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.writer.DebugFilter;
import org.dita.dost.writer.ProfilingFilter;
import org.dita.dost.writer.ValidationFilter;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.Collections.emptyMap;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import static org.dita.dost.reader.GenListModuleReader.isFormatDita;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.stripFragment;
import static org.dita.dost.util.URLUtils.toURI;
import static org.dita.dost.util.XMLUtils.getXMLReader;

/**
 * Module for reading and serializing topics into temporary directory.
 *
 * @since 2.4
 */
public final class TopicReaderModule extends AbstractReaderModule {

    public TopicReaderModule() {
        super();
        formatFilter = v -> !(Objects.equals(v, ATTR_FORMAT_VALUE_DITAMAP) || Objects.equals(v, ATTR_FORMAT_VALUE_DITAVAL));
    }

    @Override
    public AbstractPipelineOutput execute(final AbstractPipelineInput input) throws DITAOTException {
        try {
            parseInputParameters(input);
            init();

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
    public void readStartFile() throws DITAOTException {
        getStartDocuments().stream().forEach(this::addToWaitList);
    }

    private List<Reference> getStartDocuments() throws DITAOTException {
        final List<Reference> res = new ArrayList<>();
        final FileInfo startFileInfo = job.getFileInfo(job.getInputFile());
        final URI tmp = job.tempDirURI.resolve(startFileInfo.uri);
        final Source source = new StreamSource(tmp.toString());
        logger.info("Reading " + tmp);
        try {
            final XMLStreamReader in = XMLInputFactory.newFactory().createXMLStreamReader(source);
            while (in.hasNext()) {
                int eventType = in.next();
                switch (eventType) {
                    case START_ELEMENT:
                        final URI href = getHref(in);
                        if (href != null) {
                            final URI targetTmp = tmp.resolve(href);
                            final FileInfo fi = job.getFileInfo(targetTmp);
                            assert fi != null;
                            assert fi.src != null;
                            final String format = in.getAttributeValue(null, ATTRIBUTE_NAME_FORMAT);
                            res.add(new Reference(fi.src, format));
                        }
                        break;
                    default:
                        break;
                }
            }
        } catch (final XMLStreamException e) {
            throw new DITAOTException(e);
        }
        return res;
    }

    private URI getHref(final XMLStreamReader in) {
        final URI href = toURI(in.getAttributeValue(null, ATTRIBUTE_NAME_HREF));
        if (href == null) {
            return null;
        }
        final String scope = in.getAttributeValue(null, ATTRIBUTE_NAME_SCOPE);
        if (ATTR_SCOPE_VALUE_EXTERNAL.equals(scope)) {
            return null;
        }
        final String format = in.getAttributeValue(null, ATTRIBUTE_NAME_FORMAT);
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
            pipe.add(profilingFilter);
        }

        final ValidationFilter validationFilter = new ValidationFilter();
        validationFilter.setLogger(logger);
        validationFilter.setValidateMap(validateMap);
        validationFilter.setCurrentFile(fileToParse);
        validationFilter.setJob(job);
        validationFilter.setProcessingMode(processingMode);
        pipe.add(validationFilter);

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

}