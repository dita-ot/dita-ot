/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.writer;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.MessageUtils;
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
    public void write(final File filename) throws DITAOTException {
        // ignore in-exists file
        if (filename == null || !filename.exists()) {
            return;
        }
        currentFile = filename;
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
                final URI hrefValue = toURI(atts.getValue(ATTRIBUTE_NAME_HREF));
                if (hrefValue != null){
                    final File codeFile = FileUtils.resolve(currentFile.getParentFile().getAbsoluteFile(), toFile(hrefValue));
                    if (codeFile.exists()){
                        final Charset charset = getCharset(atts.getValue(ATTRIBUTE_NAME_FORMAT));
                        BufferedReader codeReader = null;
                        try {
                            codeReader = new BufferedReader(new InputStreamReader(new FileInputStream(codeFile), charset));
                            copyLines(codeReader, new Range(hrefValue));
                        } catch (final Exception e) {
                            logger.error("Failed to process code reference " + codeFile, e);
                        } finally {
                            if (codeReader != null) {
                                try {
                                    codeReader.close();
                                } catch (final IOException e) {
                                    logger.error(e.getMessage(), e) ;
                                }
                            }
                        }
                    } else {
                        logger.warn(MessageUtils.getInstance().getMessage("DOTJ051E", hrefValue.toString()).setLocation(atts).toString());
                    }
                } else {
                    //logger.logDebug("Code reference target not defined");
                }
            } catch (final Exception e) {
                logger.error(e.getMessage(), e) ;
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
        for (int i = 0; line != null; i++) {
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
        Range(final URI uri) {
            final String fragment = uri.getFragment();
            if (fragment == null) {
                this.start = 0;
                this.end = Integer.MAX_VALUE;
            } else {
                // RFC 5147
                final Matcher m = Pattern.compile("^line=(?:(\\d+)|(\\d+)?,(\\d+)?)$").matcher(fragment);
                if (m.matches()) {
                    if (m.group(1) != null) {
                        this.start = Integer.parseInt(m.group(1));
                        this.end = this.start;
                    } else {
                        if (m.group(2) != null) {
                            this.start = Integer.parseInt(m.group(2));
                        } else {
                            this.start = 0;
                        }
                        if (m.group(3) != null) {
                            this.end = Integer.parseInt(m.group(3)) - 1;
                        } else {
                            this.end = Integer.MAX_VALUE;
                        }
                    }
                } else {
                    final Matcher mc = Pattern.compile("^line-range\\((\\d+)(?:,\\s*(\\d+))?\\)$").matcher(fragment);
                    if (mc.matches()) {
                        this.start = Integer.parseInt(mc.group(1)) - 1;
                        if (mc.group(2) != null) {
                            this.end = Integer.parseInt(mc.group(2)) - 1;
                        } else {
                            this.end = Integer.MAX_VALUE;
                        }
                    } else {
                        this.start = 0;
                        this.end = Integer.MAX_VALUE;
                    }
                }
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
                    logger.error(MessageUtils.getInstance().getMessage("DOTJ052E", tokens[2].trim()).toString());
                }
            }
        }
        if (c == null) {
            c = Charset.defaultCharset();
        }
        return c;
    }

}
