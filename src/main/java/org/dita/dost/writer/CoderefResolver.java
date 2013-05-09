/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 * {@link java.nio.charset.Charset#defaultCharset() default charset} is used in
 * reading the code file.</p>
 * 
 * <p>The href attribute can contain an optional line range:</p>
 * 
 * <pre>uri ("#line-range(" start ("," end)? ")" )?</pre>
 * 
 * <p>Start and end line numbers start from 1 and are inclusive. If end range
 * is omitted, range ends in last line.</p>
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
                            copyLines(codeReader, new Range(hrefValue));
                        } catch (final Exception e) {
                            logger.logError("Failed to process code reference " + codeFile, e);
                        } finally {
                            if (codeReader != null) {
                                try {
                                    codeReader.close();
                                } catch (final IOException e) {
                                    logger.logError(e.getMessage(), e) ;
                                }
                            }
                        }
                    } else {
                        logger.logWarn(MessageUtils.getInstance().getMessage("DOTJ051E", hrefValue).setLocation(atts).toString());
                    }
                } else {
                    //logger.logDebug("Code reference target not defined");
                }
            } catch (final Exception e) {
                logger.logError(e.getMessage(), e) ;
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
     * Copy lines from reader to output
     * 
     * @param codeReader line reader
     * @param range range of lines to copy
     */
    private void copyLines(final BufferedReader codeReader, final Range range) throws IOException, SAXException {
        boolean first = true;
        String line = codeReader.readLine();
        for (int i = 1; line != null; i++) {
            if (i >= range.start && i <= range.end) {
                if (first) {
                    first = false;
                } else {
                    super.characters(XML_NEWLINE, 0, XML_NEWLINE.length);
                }
                final char[] ch = line.toCharArray();
                super.characters(ch, 0, ch.length);
            }
            line = codeReader.readLine();
        }
    }
    
    /**
     * Line range tuple
     */
    private static class Range {
        final int start;
        final int end;
        Range(final String uri) {
            final Pattern p = Pattern.compile(".+#line-range\\((\\d+)(?:,\\s*(\\d+))?\\)");
            final Matcher m = p.matcher(uri);
            if (m.matches()) {
                this.start = Integer.parseInt(m.group(1));
                if (m.group(2) != null) {
                    this.end = Integer.parseInt(m.group(2));
                } else {
                    this.end = Integer.MAX_VALUE;
                }
            } else {
                this.start = 0;
                this.end = Integer.MAX_VALUE;
            }
        }
    }
    
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
                    logger.logError(MessageUtils.getInstance().getMessage("DOTJ052E", tokens[2].trim()).toString());
                }
            }
        }
        if (c == null) {
            c = Charset.defaultCharset();
        }
        return c;
    }

}
