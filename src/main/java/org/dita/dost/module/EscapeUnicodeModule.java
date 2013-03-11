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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;

/**
 * This class replace all non-ASCII characters to their RTF Unicode-escaped forms.
 */
final class EscapeUnicodeModule implements AbstractPipelineModule {

    private DITAOTLogger logger;

    @Override
    public void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
    }

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

        //Transliterator transliterator = Transliterator.getInstance("Any-Hex/C");
        //initTransliterator(transliterator);

        final File file = new File(outputFile);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }

        FileInputStream fi = null;
        InputStreamReader is = null;
        BufferedReader br = null;
        FileWriter fw = null;
        try {
            fi = new FileInputStream(new File(inputFile));
            is = new InputStreamReader(fi, "UTF-8");
            br = new BufferedReader(is);
            fw = new FileWriter(file);

            String data = null;
            int codePoint = 0;
            while ((data = br.readLine()) != null) {
                for (int i = 0; i < data.length(); i++) {
                    codePoint = data.codePointAt(i);
                    if (codePoint < 128) {
                        fw.append(data.charAt(i));
                    } else {
                        fw.append("\\u").append("" + codePoint).append(" ?");
                    }
                }
                //fw.append(transliterator.transliterate(data));
            }
            fw.flush();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (fw != null) {
                try {
                    fw.close();
                } catch (final Exception e) {
                    logger.logError(e.getMessage(), e) ;
                }
            }
            if (br != null) {
                try {
                    br.close();
                } catch (final Exception e) {
                    logger.logError(e.getMessage(), e) ;
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (final Exception e) {
                    logger.logError(e.getMessage(), e) ;
                }
            }
            if (fi != null) {
                try {
                    fi.close();
                } catch (final Exception e) {
                    logger.logError(e.getMessage(), e) ;
                }
            }
        }

        return null;
    }

    //	private void initTransliterator(Transliterator t) {
    //		UnicodeSet u = new UnicodeSet();
    //		u.applyPattern("[\\u0080-\\U0010FFFF]"); // escape all non-ASCII characters
    //		t.setFilter(u);
    //		Field[] fields = t.getClass().getDeclaredFields();
    //		try {
    //			for (Field f : fields) {
    //				if (f.getName().equals("prefix")) {
    //					f.setAccessible(true);
    //					f.set(t, "\\u");
    //				} else if (f.getName().equals("suffix")) {
    //					f.setAccessible(true);
    //					f.set(t, " ?");
    //				} else if (f.getName().equals("radix")) {
    //					f.setAccessible(true);
    //					f.set(t, 10);
    //				} else if (f.getName().equals("minDigits")) {
    //					f.setAccessible(true);
    //					f.set(t, 4);
    //				}
    //			}
    //
    //		} catch (IllegalArgumentException e) {
    //			throw new RuntimeException(e);
    //		} catch (IllegalAccessException e) {
    //			throw new RuntimeException(e);
    //		}
    //	}
}
