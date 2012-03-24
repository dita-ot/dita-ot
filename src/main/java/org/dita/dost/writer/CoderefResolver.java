/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.writer;

import static org.dita.dost.util.Constants.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Properties;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.module.Content;
import org.dita.dost.util.FileUtils;

/**
 * Coderef element resolver filter.
 * 
 * <p>The format attribute is assumed to follow the syntax:</p>
 * 
 * <pre>format (";" space* "charset=" charset)?</pre>
 * 
 * <p>If no charset if defined or the charset name is not recognized,
 * {@link ava.nio.charset.Charset#defaultCharset() default charset} is used in
 * reading the code file.</p>
 */
public final class CoderefResolver extends AbstractXMLFilter {

    // Constants ---------------------------------------------------------------

    private static final char[] XML_NEWLINE = { '\n' };

    // Variables ---------------------------------------------------------------

    private File currentFile = null;
    private int ignoreDepth = 0;

    // Constructors ------------------------------------------------------------

    /**
     * Constructor.
     */
    public CoderefResolver() {
    }

    // AbstractWriter methods --------------------------------------------------

    @Override
    public void setContent(final Content content) {
        // NOOP
    }

    @Override
    public void write(final String filename) throws DITAOTException {
        // ignore in-exists file
        if (filename == null || !new File(filename).exists()) {
            return;
        }
        currentFile = new File(filename);
        super.write(filename);
    }

    // XMLFilter methods -------------------------------------------------------

    @Override
    public void startElement(final String uri, final String localName, final String name,
            final Attributes atts) throws SAXException {
        if (ignoreDepth > 0) {
            ignoreDepth++;
            return;
        }

        if (PR_D_CODEREF.matches(atts)) {
            ignoreDepth++;
            try{
                final String hrefValue = atts.getValue(ATTRIBUTE_NAME_HREF);
                if (hrefValue != null){
                    final String codeFile = FileUtils.normalizeDirectory(currentFile.getParentFile().getAbsolutePath(), hrefValue);
                    if (new File(codeFile).exists()){
                        final Charset charset = getCharset(atts.getValue(ATTRIBUTE_NAME_FORMAT));
                        BufferedReader codeReader = null;
                        try {
                            codeReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(codeFile)), charset));
                            String line = codeReader.readLine();
                            while (line != null) {
                                final char[] ch = line.toCharArray();
                                super.characters(ch, 0, ch.length);
                                line = codeReader.readLine();
                                if (line != null) {
                                    super.characters(XML_NEWLINE, 0, XML_NEWLINE.length);
                                }
                            }
                        } catch (final Exception e) {
                            logger.logException(new Exception("Failed to process code reference " + codeFile));
                        } finally {
                            if (codeReader != null) {
                                try {
                                    codeReader.close();
                                } catch (final IOException e) {
                                    logger.logException(e);
                                }
                            }
                        }
                    } else {
                        final Properties prop = new Properties();
                        prop.put("%1", hrefValue);
                        prop.put("%2", atts.getValue(ATTRIBUTE_NAME_XTRF));
                        prop.put("%3", atts.getValue(ATTRIBUTE_NAME_XTRC));
                        logger.logWarn(MessageUtils.getMessage("DOTJ051E",prop).toString());
                    }
                } else {
                    //logger.logDebug("Code reference target not defined");
                }
            } catch (final Exception e) {
                logger.logException(e);
            }
        } else {
            super.startElement(uri, localName, name, atts);
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String name)
            throws SAXException {
        if (ignoreDepth > 0) {
            ignoreDepth--;
            return;
        }

        super.endElement(uri, localName, name);
    }

    // Private methods ---------------------------------------------------------

    /**
     * Get code file charset.
     * 
     * @param value format attribute value, may be {@code null}
     * @return charset if set, otherwise default charset
     */
    private Charset getCharset(final String value) {
        Charset c = null;
        if (value != null) {
            final String[] tokens = value.trim().split("[;=]");
            if (tokens.length >= 3 && tokens[1].trim().equals("charset")) {
                try {
                    c = Charset.forName(tokens[2].trim());
                } catch (final RuntimeException e) {
                    final Properties prop = new Properties();
                    prop.put("%1", tokens[2].trim());
                    logger.logError(MessageUtils.getMessage("DOTJ052E",prop).toString());
                }
            }
        }
        if (c == null) {
            c = Charset.defaultCharset();
        }
        return c;
    }

}
