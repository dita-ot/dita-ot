/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2018 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.module;

import com.google.common.io.Files;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.util.CatalogUtils;
import org.dita.dost.util.Job.FileInfo;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.Collection;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.XMLUtils.withLogger;

/**
 * Recursively inline map references in maps.
 *
 * @since 3.1
 */
final class MaprefModule extends AbstractPipelineModuleImpl {

    private Transformer transformer;

    private void init(final AbstractPipelineInput input) {
        try {
            final File styleFile = new File(input.getAttribute(ANT_INVOKER_EXT_PARAM_STYLE));
            final TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setURIResolver(CatalogUtils.getCatalogResolver());
            transformer = withLogger(transformerFactory.newTransformer(new StreamSource(styleFile)), logger);
            transformer.setURIResolver(CatalogUtils.getCatalogResolver());
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(e);
        }

//        if (input.getAttribute("include.rellinks") != null) {
//            transformer.setParameter("include.rellinks", input.getAttribute("include.rellinks"));
//        }
//        transformer.setParameter("INPUTMAP", job.getInputMap());

        if (fileInfoFilter == null) {
            fileInfoFilter = fileInfo -> fileInfo.format != null && fileInfo.format.equals(ATTR_FORMAT_VALUE_DITAMAP);
        }
    }

    /**
     * @param input input parameters and resources
     * @return always {@code null}
     * @throws DITAOTException if process fails
     */
    @Override
    public AbstractPipelineOutput execute(final AbstractPipelineInput input) throws DITAOTException {
        init(input);

        final Collection<FileInfo> fileInfos = job.getFileInfo(fileInfoFilter);
        for (FileInfo fileInfo : fileInfos) {
            processMap(fileInfo);
        }
        for (FileInfo fileInfo : fileInfos) {
            replace(fileInfo);
        }

        return null;
    }

    private void processMap(final FileInfo input) throws DITAOTException {
        final File inputFile = new File(job.tempDir, input.file.getPath());
        final File outputFile = new File(inputFile.getAbsolutePath() + FILE_EXTENSION_TEMP);

        logger.info("Processing " + inputFile.toURI());
        try (InputStream in = new BufferedInputStream(new FileInputStream(inputFile));
             OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile))) {
            final Source source = new StreamSource(in, inputFile.toURI().toString());
            final Result result = new StreamResult(out);
            transformer.transform(source, result);
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw new DITAOTException("Failed to merge map " + inputFile + ": " + e.getMessage(), e);
        }
    }

    private void replace(final FileInfo input) throws DITAOTException {
        final File inputFile = new File(job.tempDir, input.file.getPath() + FILE_EXTENSION_TEMP);
        final File outputFile = new File(job.tempDir, input.file.getPath());
        try {
            Files.move(inputFile, outputFile);
        } catch (final IOException e) {
            throw new DITAOTException("Failed to replace temporary file " + inputFile + ": " + e.getMessage(), e);
        }
    }

}
