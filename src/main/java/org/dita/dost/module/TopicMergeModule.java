/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2004, 2005 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.module;

import net.sf.saxon.s9api.*;
import net.sf.saxon.trans.UncheckedXPathException;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.reader.MergeMapParser;
import org.dita.dost.util.CatalogUtils;
import org.dita.dost.util.DelegatingURIResolver;
import org.dita.dost.util.Job.FileInfo;

import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.nio.charset.StandardCharsets;

import static javax.xml.XMLConstants.XMLNS_ATTRIBUTE;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.XMLUtils.toErrorListener;

/**
 * The module handles topic merge in issues as PDF.
 */
final class TopicMergeModule extends AbstractPipelineModuleImpl {

    /**
     * Default Constructor.
     *
     */
    public TopicMergeModule() {
        super();
    }

    /**
     * Entry point of TopicMergeModule.
     *
     * @param input Input parameters and resources.
     * @return null
     * @throws DITAOTException exception
     */
    @Override
    public AbstractPipelineOutput execute(final AbstractPipelineInput input)
            throws DITAOTException {
        if (logger == null) {
            throw new IllegalStateException("Logger not set");
        }
        final FileInfo in = job.getFileInfo(fi -> fi.isInput).iterator().next();
        final File ditaInput = new File(job.tempDirURI.resolve(in.uri));
        if (!job.getStore().exists(ditaInput.toURI())) {
            logger.error(MessageUtils.getMessage("DOTJ025E").toString());
            return null;
        }
        final File style = input.getAttribute(ANT_INVOKER_EXT_PARAM_STYLE) != null
                ? new File(input.getAttribute(ANT_INVOKER_EXT_PARAM_STYLE))
                : null;
        final File out = new File(input.getAttribute(ANT_INVOKER_EXT_PARAM_OUTPUT)).getAbsoluteFile();

        final MergeMapParser mapParser = new MergeMapParser();
        mapParser.setLogger(logger);
        mapParser.setJob(job);
        mapParser.setOutput(out);

        final ByteArrayOutputStream midBuffer = new ByteArrayOutputStream();
        try {
            midBuffer.write(XML_HEAD.getBytes(StandardCharsets.UTF_8));
            midBuffer.write(("<dita-merge " + ATTRIBUTE_NAMESPACE_PREFIX_DITAARCHVERSION + "='" + DITA_NAMESPACE + "' "
                    + XMLNS_ATTRIBUTE + ":" + DITA_OT_NS_PREFIX + "='" + DITA_OT_NS + "'>").getBytes(StandardCharsets.UTF_8));
            mapParser.setOutputStream(midBuffer);
            mapParser.read(ditaInput, job.tempDir);
            midBuffer.write("</dita-merge>".getBytes(StandardCharsets.UTF_8));
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (final IOException e) {
            throw new DITAOTException("Failed to merge topics: " + e.getMessage(), e);
        }

        final File outputDir = out.getParentFile();
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            logger.error("Failed to create directory " + outputDir.getAbsolutePath());
        }
        try (final OutputStream output = new BufferedOutputStream(new FileOutputStream(out))) {
            if (style != null) {
                final Processor processor = xmlUtils.getProcessor();
                final XsltCompiler xsltCompiler = processor.newXsltCompiler();
                final XsltTransformer transformer = xsltCompiler.compile(new StreamSource(style)).load();
                transformer.setErrorListener(toErrorListener(logger));
                transformer.setURIResolver(new DelegatingURIResolver(CatalogUtils.getCatalogResolver(), job.getStore()));
                final StreamSource source = new StreamSource(new ByteArrayInputStream(midBuffer.toByteArray()));
                final Destination result = processor.newSerializer(output);
                transformer.setSource(source);
                transformer.setDestination(result);
                transformer.transform();
            } else {
                output.write(midBuffer.toByteArray());
                output.flush();
            }
        } catch (final UncheckedXPathException e) {
            throw new DITAOTException("Failed to process merged topics", e);
        } catch (final RuntimeException e) {
            throw e;
        } catch (final IOException | SaxonApiException e) {
            throw new DITAOTException("Failed to process merged topics: " + e.getMessage(), e);
        }

        return null;
    }

}
