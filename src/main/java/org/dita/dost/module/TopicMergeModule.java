/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2004, 2005 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.module;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.XMLUtils.withLogger;
import static javax.xml.XMLConstants.*;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.reader.MergeMapParser;
import org.dita.dost.util.CatalogUtils;

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
        final File ditaInput = new File(job.tempDirURI.resolve(job.getInputMap()));
        if (!ditaInput.exists()){
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
            midBuffer.write(XML_HEAD.getBytes(UTF8));
            midBuffer.write(("<dita-merge " + ATTRIBUTE_NAMESPACE_PREFIX_DITAARCHVERSION + "='" + DITA_NAMESPACE + "' "
                    + XMLNS_ATTRIBUTE + ":" + DITA_OT_NS_PREFIX + "='" + DITA_OT_NS + "'>").getBytes(UTF8));
            mapParser.setOutputStream(midBuffer);
            mapParser.read(ditaInput, job.tempDir);
            midBuffer.write("</dita-merge>".getBytes(UTF8));
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
                final TransformerFactory factory = TransformerFactory.newInstance();
                factory.setURIResolver(CatalogUtils.getCatalogResolver());
                final StreamSource styleSource = new StreamSource(style);
                final Transformer transformer = withLogger(factory.newTransformer(styleSource), logger);
                final StreamSource source = new StreamSource(new ByteArrayInputStream(midBuffer.toByteArray()));
                final StreamResult result = new StreamResult(output);
                transformer.transform(source, result);
            } else {
                output.write(midBuffer.toByteArray());
                output.flush();
            }
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw new DITAOTException("Failed to process merged topics: " + e.getMessage(), e);
        }

        return null;
    }

}
