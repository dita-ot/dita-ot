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
import org.dita.dost.util.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.Collection;
import java.util.List;

import static org.dita.dost.reader.GenListModuleReader.KEYREF_ATTRS;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.XMLUtils.withLogger;

/**
 * Recursively inline map references in maps.
 *
 * @since 3.1
 */
final class MaprefModule extends AbstractPipelineModuleImpl {

    private final SAXTransformerFactory transformerFactory;

    private Templates templates;
    private Transformer serializer;

    public MaprefModule() {
        transformerFactory = (SAXTransformerFactory) TransformerFactory.newInstance();
        transformerFactory.setURIResolver(CatalogUtils.getCatalogResolver());
    }

    private void init(final AbstractPipelineInput input) {
        try {
            final File styleFile = new File(input.getAttribute(ANT_INVOKER_EXT_PARAM_STYLE));
            templates = transformerFactory.newTemplates(new StreamSource(styleFile));
            serializer = transformerFactory.newTransformer();
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException(e);
        }

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

        try {
            job.write();
        } catch (IOException e) {
            throw new DITAOTException("Failed to serialize job configuration: " + e.getMessage(), e);
        }

        return null;
    }

    private void processMap(final FileInfo input) throws DITAOTException {
        final File inputFile = new File(job.tempDir, input.file.getPath());
        final File outputFile = new File(inputFile.getAbsolutePath() + FILE_EXTENSION_TEMP);

        logger.info("Processing " + inputFile.toURI());
        Document doc;
        try (InputStream in = new BufferedInputStream(new FileInputStream(inputFile))) {
            doc = XMLUtils.getDocumentBuilder().newDocument();
            final Source source = new StreamSource(in, inputFile.toURI().toString());
            final Result result = new DOMResult(doc);
            final Transformer transformer = withLogger(templates.newTransformer(), logger);
            transformer.setURIResolver(CatalogUtils.getCatalogResolver());
            transformer.transform(source, result);
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw new DITAOTException("Failed to merge map " + inputFile + ": " + e.getMessage(), e);
        }

        final FileInfo updated = collectJobInfo(input, doc);
        job.add(updated);

        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile))) {
            final Source source = new DOMSource(doc);
            final Result result = new StreamResult(out);
            serializer.transform(source, result);
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw new DITAOTException("Failed to serialize map " + inputFile + ": " + e.getMessage(), e);
        }
    }

    private FileInfo collectJobInfo(FileInfo fileInfo, Document doc) {
        final FileInfo.Builder builder = new FileInfo.Builder(fileInfo);
        final List<Element> elements = XMLUtils.toList(doc.getElementsByTagName("*"));
        if (!fileInfo.hasConref) {
            builder.hasConref(elements.stream()
                    .anyMatch(e -> e.hasAttribute(ATTRIBUTE_NAME_CONREF) || e.hasAttribute(ATTRIBUTE_NAME_CONKEYREF)));
        }
        if (!fileInfo.hasKeyref) {
            builder.hasKeyref(elements.stream()
                    .anyMatch(e -> {
                        if (SUBJECTSCHEME_SUBJECTDEF.matches(e)) {
                            return false;
                        }
                        for (final String attr : KEYREF_ATTRS) {
                            if (e.hasAttribute(attr)) {
                                return true;
                            }
                        }
                        return false;
                    }));
        }

        return builder.build();
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
