/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2010 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.writer;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.exception.DITAOTXMLErrorHandler;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.util.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.*;
import org.xml.sax.helpers.XMLFilterImpl;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import java.io.*;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.dita.dost.util.CatalogUtils.getCatalogResolver;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.*;
import static org.dita.dost.util.XMLUtils.getDocumentBuilder;

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
 * <p>Optional id range is defined using:</p>
 *
 * <pre>uri ("#token=" start? ("," end)? )?</pre>
 *
 * <p>Lines idenfified using start and end IDs are exclusive. If the start range
 * is omitted, range starts from the first line; if end range
 * is omitted, range ends in last line.</p>
 */
public final class CoderefResolver extends AbstractXMLFilter {

    // Constants ---------------------------------------------------------------

    private static final char[] XML_NEWLINE = {'\n'};

    // Variables ---------------------------------------------------------------

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
        assert filename.isAbsolute();
        setCurrentFile(filename.toURI());
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

        if (PR_D_CODEREF.matches(atts) || TOPIC_INCLUDE.matches(atts)) {
            ignoreDepth++;
            try {
                final URI hrefValue = toURI(atts.getValue(ATTRIBUTE_NAME_HREF));
                if (hrefValue != null) {
                    logger.debug("Resolve " + localName + " " + currentFile.resolve(hrefValue));
                    final String parse = getParse(atts.getValue(ATTRIBUTE_NAME_PARSE));
                    switch (parse) {
                        case "text":
                            includeText(atts);
                            break;
                        case "xml":
                            includeXml(atts);
                            break;
                        default:
                            logger.error("Unsupported include parse " + parse);
                    }
                }
            } catch (final RuntimeException e) {
                throw e;
            } catch (final Exception e) {
                logger.error(e.getMessage(), e);
            }
        } else {
            super.startElement(uri, localName, name, atts);
        }
    }

    private void includeXml(final Attributes atts) throws SAXException, IOException, TransformerException {
        final URI hrefValue = toURI(atts.getValue(ATTRIBUTE_NAME_HREF));
        final Job.FileInfo fileInfo = job.getFileInfo(stripFragment(currentFile.resolve(hrefValue)));
        final DocumentBuilder builder = getDocumentBuilder();
        builder.setEntityResolver(getCatalogResolver());
        builder.setErrorHandler(new DITAOTXMLErrorHandler(fileInfo.src.toString(), logger));

        final Document doc = builder.parse(fileInfo.src.toString());
        Node src = null;
        if (hrefValue.getFragment() != null) {
            src = doc.getElementById(hrefValue.getFragment());
        }
        if (src == null) {
            src = doc;
        }
        final Transformer serializer = TransformerFactory.newInstance().newTransformer();
        final DOMSource source = new DOMSource(src);
        final SAXResult result = new SAXResult(new IncludeFilter(getContentHandler()));
        serializer.transform(source, result);
    }

    private void includeText(final Attributes atts) {
        final URI hrefValue = toURI(atts.getValue(ATTRIBUTE_NAME_HREF));
        final Charset charset = getCharset(atts.getValue(ATTRIBUTE_NAME_FORMAT), atts.getValue(ATTRIBUTE_NAME_ENCODING));
        final Range range = getRange(hrefValue);
        final File codeFile = getFile(hrefValue);
        try (BufferedReader codeReader = new BufferedReader(
                new InputStreamReader(new FileInputStream(codeFile), charset))) {
            range.copyLines(codeReader);
        } catch (final Exception e) {
            logger.error("Failed to process include " + codeFile, e);
        }
    }

    private String getParse(final String value) {
        if (value == null) {
            return "text";
        }
        return value;
    }

    private File getFile(URI hrefValue) {
        final File tempFile = toFile(stripFragment(currentFile.resolve(hrefValue))).getAbsoluteFile();
        final URI rel = job.tempDirURI.relativize(tempFile.toURI());
        final Job.FileInfo fi = job.getFileInfo(rel);

//        if (tempFile.exists() && fi != null && PR_D_CODEREF.localName.equals(fi.format)) {
//            return tempFile;
//        }
        if (fi != null && "file".equals(fi.src.getScheme())) {
            return new File(fi.src);
        }
        return null;
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
     * Factory method for Range implementation
     */
    private Range getRange(final URI uri) {
        int start = 0;
        int end = Integer.MAX_VALUE;
        String startId = null;
        String endId = null;

        final String fragment = uri.getFragment();
        if (fragment != null) {
            // RFC 5147
            final Matcher m = Pattern.compile("^line=(?:(\\d+)|(\\d+)?,(\\d+)?)$").matcher(fragment);
            if (m.matches()) {
                if (m.group(1) != null) {
                    start = Integer.parseInt(m.group(1));
                    end = start;
                } else {
                    if (m.group(2) != null) {
                        start = Integer.parseInt(m.group(2));
                    }
                    if (m.group(3) != null) {
                        end = Integer.parseInt(m.group(3)) - 1;
                    }
                }
                return new LineNumberRange(start, end).handler(this);
            } else {
                final Matcher mc = Pattern.compile("^line-range\\((\\d+)(?:,\\s*(\\d+))?\\)$").matcher(fragment);
                if (mc.matches()) {
                    start = Integer.parseInt(mc.group(1)) - 1;
                    if (mc.group(2) != null) {
                        end = Integer.parseInt(mc.group(2)) - 1;
                    }
                    return new LineNumberRange(start, end).handler(this);
                } else {
                    final Matcher mi = Pattern.compile("^token=([^,\\s)]*)(?:,\\s*([^,\\s)]+))?$").matcher(fragment);
                    if (mi.matches()) {
                        if (mi.group(1) != null && mi.group(1).length() != 0) {
                            startId = mi.group(1);
                        }
                        if (mi.group(2) != null) {
                            endId = mi.group(2);
                        }
                        return new AnchorRange(startId, endId).handler(this);
                    }
                }
            }
        }

        return new AllRange().handler(this);
    }

    private interface Range {
        /**
         * Copy lines from reader to target handler
         *
         * @param codeReader line reader
         */
        void copyLines(final BufferedReader codeReader) throws IOException, SAXException;

        /**
         * Set target handler
         */
        Range handler(final ContentHandler contentHandler);
    }

    private static class LineNumberRange extends AllRange implements Range {

        private final int start;
        private final int end;

        LineNumberRange(final int start, final int end) {
            this.start = start;
            this.end = end;
        }

        @Override
        public void copyLines(final BufferedReader codeReader) throws IOException, SAXException {
            boolean first = true;
            String line = codeReader.readLine();
            for (int i = 0; line != null; i++) {
                if (i >= start && i <= end) {
                    if (first) {
                        first = false;
                    } else {
                        handler.characters(XML_NEWLINE, 0, XML_NEWLINE.length);
                    }
                    final char[] ch = line.toCharArray();
                    handler.characters(ch, 0, ch.length);
                }
                line = codeReader.readLine();
            }
        }
    }

    private static class AnchorRange extends AllRange implements Range {

        private final String start;
        private final String end;
        private int include;

        AnchorRange(final String start, final String end) {
            this.start = start;
            this.end = end;
            include = start != null ? -1 : 1;
        }

        @Override
        public void copyLines(final BufferedReader codeReader) throws IOException, SAXException {
            boolean first = true;
            String line;
            while ((line = codeReader.readLine()) != null) {
                if (include == -1 && start != null) {
                    include = line.contains(start) ? 0 : -1;
                } else if (include > -1 && end != null) {
                    include = line.contains(end) ? -1 : include;
                }
                if (include > 0) {
                    if (first) {
                        first = false;
                    } else {
                        handler.characters(XML_NEWLINE, 0, XML_NEWLINE.length);
                    }
                    final char[] ch = line.toCharArray();
                    handler.characters(ch, 0, ch.length);
                }
                if (include >= 0) {
                    include++;
                }
            }
        }
    }

    private static class AllRange implements Range {

        ContentHandler handler;

        @Override
        public Range handler(final ContentHandler handler) {
            this.handler = handler;
            return this;
        }

        @Override
        public void copyLines(BufferedReader codeReader) throws IOException, SAXException {
            boolean first = true;
            String line;
            while ((line = codeReader.readLine()) != null) {
                if (first) {
                    first = false;
                } else {
                    handler.characters(XML_NEWLINE, 0, XML_NEWLINE.length);
                }
                final char[] ch = line.toCharArray();
                handler.characters(ch, 0, ch.length);
            }
        }
    }

    /**
     * Get code file charset.
     *
     * @param format   format attribute value, may be {@code null}
     * @param encoding encoding attribute balue, may be {@code null}
     * @return charset if set, otherwise default charset
     */
    private Charset getCharset(final String format, final String encoding) {
        Charset c = null;
        try {
            if (encoding != null) {
                c = Charset.forName(encoding);
            } else if (format != null) {
                final String[] tokens = format.trim().split("[;=]");
                if (tokens.length >= 3 && tokens[1].trim().equals(ATTRIBUTE_NAME_CHARSET)) {
                    c = Charset.forName(tokens[2].trim());
                }
            }
        } catch (final RuntimeException e) {
            logger.error(MessageUtils.getMessage("DOTJ052E", encoding).toString());
        }
        if (c == null) {
            final String defaultCharset = Configuration.configuration.get("default.coderef-charset");
            if (defaultCharset != null) {
                c = Charset.forName(defaultCharset);
            } else {
                c = Charset.defaultCharset();
            }
        }
        return c;
    }

    private static class IncludeFilter extends XMLFilterImpl {
        public IncludeFilter(final ContentHandler handler) {
            super();
            setContentHandler(handler);
        }

        public void startDocument() throws SAXException {
            // Ignore
        }

        public void endDocument() throws SAXException {
            // Ignore
        }
    }
}
