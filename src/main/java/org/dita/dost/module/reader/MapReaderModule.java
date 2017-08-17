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

import static org.dita.dost.reader.GenListModuleReader.isFormatDita;
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
//        // avoid files referred by coderef being added into wait list
//        if (listFilter.getCoderefTargets().contains(file.filename)) {
//            return;
//        }
        if (formatFilter.test(file.format)) {
//            if (isFormatDita(file.format)) {
//                addToWaitList(file);
//            } else
            if (ATTR_FORMAT_VALUE_DITAMAP.equals(file.format)) {
                addToWaitList(file);
            } else if (ATTR_FORMAT_VALUE_IMAGE.equals(file.format)) {
                formatSet.add(file);
                if (!exists(file.filename)) {
                    logger.warn(MessageUtils.getMessage("DOTX008W", file.filename.toString()).toString());
                }
            } else if (ATTR_FORMAT_VALUE_DITAVAL.equals(file.format)) {
                formatSet.add(file);
            } else {
                htmlSet.add(file.filename);
            }
        }
    }

}