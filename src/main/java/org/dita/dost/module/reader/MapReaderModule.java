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
import org.dita.dost.writer.DebugFilter;
import org.dita.dost.writer.NormalizeFilter;
import org.dita.dost.writer.ProfilingFilter;
import org.dita.dost.writer.ValidationFilter;
import org.xml.sax.XMLFilter;

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
        addToWaitList(new Reference(rootFile));
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

        if (INDEX_TYPE_ECLIPSEHELP.equals(transtype)) {
            exportAnchorsFilter.setCurrentFile(fileToParse);
            exportAnchorsFilter.setErrorHandler(new DITAOTXMLErrorHandler(fileToParse.toString(), logger));
            pipe.add(exportAnchorsFilter);
        }

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