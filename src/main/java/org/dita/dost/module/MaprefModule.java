/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2018 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.module;

import net.sf.saxon.s9api.*;
import net.sf.saxon.trans.UncheckedXPathException;
import net.sf.saxon.trans.XPathException;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.util.CatalogUtils;
import org.dita.dost.util.DelegatingURIResolver;
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.util.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static org.dita.dost.reader.GenListModuleReader.KEYREF_ATTRS;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.XMLUtils.toErrorListener;

/**
 * Recursively inline map references in maps.
 *
 * @since 3.1
 */
final class MaprefModule extends AbstractPipelineModuleImpl {

    private Processor processor;
    private XsltExecutable templates;

    private void init(final AbstractPipelineInput input) {
        processor = xmlUtils.getProcessor();
        final XsltCompiler xsltCompiler = processor.newXsltCompiler();
        xsltCompiler.setErrorListener(toErrorListener(logger));
        final File style = new File(input.getAttribute(ANT_INVOKER_EXT_PARAM_STYLE));
        try {
            templates = xsltCompiler.compile(new StreamSource(style));
        } catch (SaxonApiException e) {
            throw new RuntimeException("Failed to compile stylesheet '" + style.getAbsolutePath() + "': " + e.getMessage(), e);
        }

    }

    /**
     * @param input input parameters and resources
     * @return always {@code null}
     * @throws DITAOTException if process fails
     */
    @Override
    public AbstractPipelineOutput execute(final AbstractPipelineInput input) throws DITAOTException {
        if (fileInfoFilter == null) {
            fileInfoFilter = fileInfo -> fileInfo.format != null && fileInfo.format.equals(ATTR_FORMAT_VALUE_DITAMAP);
        }
        final Collection<FileInfo> fileInfos = job.getFileInfo(fileInfoFilter);
        if (fileInfos.isEmpty()) {
            return null;
        }

        init(input);
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
        try {
            doc = XMLUtils.getDocumentBuilder().newDocument();
            final XsltTransformer transformer = templates.load();
            transformer.setErrorListener(toErrorListener(logger));
            transformer.setURIResolver(new DelegatingURIResolver(CatalogUtils.getCatalogResolver(), job.getStore()));
            transformer.setParameter(new QName("file-being-processed"), XdmItem.makeValue(inputFile.getName()));

            final Source source = job.getStore().getSource(inputFile.toURI());
            transformer.setSource(source);
            final Destination serializer = new DOMDestination(doc);
            serializer.setDestinationBaseURI(inputFile.toURI());
            transformer.setDestination(serializer);
            transformer.transform();
        } catch (final UncheckedXPathException e) {
            throw new DITAOTException("Failed to merge map " + inputFile, e);
        } catch (final RuntimeException e) {
            throw e;
        } catch (final SaxonApiException e) {
            try {
                throw e.getCause();
            } catch (final XPathException cause) {
                throw new DITAOTException("Failed to merge map " + inputFile + ": " + cause.getMessageAndLocation(), e);
            } catch (Throwable throwable) {
                throw new DITAOTException("Failed to merge map " + inputFile + ": " + e.getMessage(), e);
            }
        } catch (final Exception e) {
            throw new DITAOTException("Failed to merge map " + inputFile + ": " + e.getMessage(), e);
        }

        final FileInfo updated = collectJobInfo(input, doc);
        job.add(updated);

        try {
            doc.setDocumentURI(outputFile.toURI().toString());
            job.getStore().writeDocument(doc, outputFile.toURI());
        } catch (final IOException e) {
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
            job.getStore().move(inputFile.toURI(), outputFile.toURI());
        } catch (final IOException e) {
            throw new DITAOTException("Failed to replace temporary file " + inputFile + ": " + e.getMessage(), e);
        }
    }

}
