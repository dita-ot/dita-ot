/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.module;

import static org.dita.dost.util.Constants.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;

/**
 * This class replace all non-ASCII characters to their RTF Unicode-escaped forms.
 */
final class EscapeUnicodeModule extends AbstractPipelineModuleImpl {

    /**
     * Entry point of EscapeUnicodeModule.
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
        final String inputFile = input.getAttribute(ANT_INVOKER_EXT_PARAM_INPUT);
        final String outputFile = input.getAttribute(ANT_INVOKER_EXT_PARAM_OUTPUT);

        BufferedReader br = null;
        Writer fw = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(new File(inputFile)), "UTF-8"));
            fw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(outputFile))));
            int codePoint = br.read();
            while (codePoint != -1) {
                if (codePoint < 128) {
                    fw.append((char) codePoint);
                } else {
                    fw.append("\\uc0");
                    fw.append("\\u").append(Integer.toString(codePoint)).append(' ');
                }
                codePoint = br.read();
            }
            fw.flush();
        } catch (final IOException e) {
            throw new DITAOTException("Failed to escape non-ACSII characters: " + e.getMessage(), e);
        } finally {
            if (fw != null) {
                try {
                    fw.close();
                } catch (final Exception e) {
                    logger.error(e.getMessage(), e) ;
                }
            }
            if (br != null) {
                try {
                    br.close();
                } catch (final Exception e) {
                    logger.error(e.getMessage(), e) ;
                }
            }
        }

        return null;
    }

}
