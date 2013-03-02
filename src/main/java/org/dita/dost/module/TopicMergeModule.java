/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.module;

import static org.dita.dost.util.Constants.*;

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
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.reader.MergeMapParser;

/**
 * The module handles topic merge in issues as PDF.
 */
final class TopicMergeModule implements AbstractPipelineModule {

    private DITAOTLogger logger;

    /**
     * Default Constructor.
     *
     */
    public TopicMergeModule() {
        super();
    }

    @Override
    public void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
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
        final String ditaInput = input
                .getAttribute(ANT_INVOKER_PARAM_INPUTMAP);
        final String style = input
                .getAttribute(ANT_INVOKER_EXT_PARAM_STYLE);
        final String out = input
                .getAttribute(ANT_INVOKER_EXT_PARAM_OUTPUT);
        final String tempdir = input
                .getAttribute(ANT_INVOKER_PARAM_TEMPDIR);
        final MergeMapParser mapParser = new MergeMapParser();
        mapParser.setLogger(logger);

        if (ditaInput == null || !new File(ditaInput).exists()){
            logger.logError(MessageUtils.getInstance().getMessage("DOTJ025E").toString());
            return null;
        }

        if ( out == null ){
            logger.logError(MessageUtils.getInstance().getMessage("DOTJ026E").toString());
            return null;
        }

        ByteArrayOutputStream midBuffer = null;
        try {
            midBuffer = new ByteArrayOutputStream();
            midBuffer.write(XML_HEAD.getBytes(UTF8));
            midBuffer.write("<dita-merge xmlns:ditaarch=\"http://dita.oasis-open.org/architecture/2005/\">".getBytes(UTF8));
            mapParser.setOutputStream(midBuffer);
            mapParser.read(ditaInput, tempdir);
            midBuffer.write("</dita-merge>".getBytes(UTF8));
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (final IOException e) {
            throw new DITAOTException("Failed to merge topics: " + e.getMessage(), e);
        } finally {
            if (midBuffer != null) {
                try {
                    midBuffer.close();
                } catch (final IOException e) {
                    logger.logError("Failed to close output buffer: " + e.getMessage(), e);
                }
            }
        }

        OutputStream output = null;
        try{
            final File outputDir = new File(out).getParentFile();
            if (!outputDir.exists()){
                outputDir.mkdirs();
            }
            output = new BufferedOutputStream(new FileOutputStream(out));
            if (style != null){
                final TransformerFactory factory = TransformerFactory.newInstance();
                final File styleFile = new File(style);
                final Transformer transformer = factory.newTransformer(new StreamSource(styleFile.toURI().toString()));
                transformer.transform(new StreamSource(new ByteArrayInputStream(midBuffer.toByteArray())),
                                      new StreamResult(output));
            }else{
                output.write(midBuffer.toByteArray());
                output.flush();
            }
        }catch (final Exception e){
            throw new DITAOTException("Failed to process merged topics: " + e.getMessage(), e);
        }finally{
            try{
                if (output !=null){
                    output.close();
                }
            }catch (final Exception e){
                logger.logError("Failed to close output buffer: " + e.getMessage(), e);
            }
        }

        return null;
    }

}
