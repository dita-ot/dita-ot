/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2020 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.writer.include;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.exception.DITAOTXMLErrorHandler;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.util.Configuration;
import org.dita.dost.util.Job;
import org.dita.dost.writer.AbstractXMLFilter;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.dita.dost.util.CatalogUtils.getCatalogResolver;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.*;
import static org.dita.dost.util.XMLUtils.getDocumentBuilder;

/**
 * Include element resolver filter.
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
 *
 * @since 3.5
 */
public class IncludeResolver extends AbstractXMLFilter {

    // Constants ---------------------------------------------------------------

    public static final char[] XML_NEWLINE = {'\n'};

    // Variables ---------------------------------------------------------------

    private Deque<Boolean> ignoreDepth = new ArrayDeque<>();
    private Deque<Deque<StackItem>> includeStack = new ArrayDeque<>();

    private static class StackItem {
        public final String cls;
        public final boolean include;

        private StackItem(final String cls, final boolean include) {
            this.cls = cls;
            this.include = include;
        }
    }

    // Constructors ------------------------------------------------------------

    /**
     * Constructor.
     */
    public IncludeResolver() {
    }

    // AbstractWriter methods --------------------------------------------------

    @Override
    public void write(final File filename) throws DITAOTException {
        assert filename.isAbsolute();
        setCurrentFile(filename.toURI());
        super.write(filename);
    }

    // XMLFilter methods -------------------------------------------------------

    public void startDocument()
            throws SAXException {
        final ArrayDeque<StackItem> elementStack = new ArrayDeque<>();
        elementStack.push(new StackItem(null, true));
        includeStack.push(elementStack);
        super.startDocument();
    }

    public void endDocument()
            throws SAXException {
        super.endDocument();
        final Deque<StackItem> elementStack = includeStack.pop();
        assert elementStack.size() == 1;
        assert includeStack.isEmpty();
    }

//    public void startPrefixMapping(String prefix, String uri)
//            throws SAXException {
//        if (contentHandler != null) {
//            contentHandler.startPrefixMapping(prefix, uri);
//        }
//    }
//
//    public void endPrefixMapping(String prefix)
//            throws SAXException {
//        if (contentHandler != null) {
//            contentHandler.endPrefixMapping(prefix);
//        }
//    }

    @Override
    public void startElement(final String uri, final String localName, final String name,
                             final Attributes atts) throws SAXException {
        final Deque<StackItem> elementStack = includeStack.peek();
        final StackItem stackItem = elementStack.peek();
        final String cls = atts.getValue(ATTRIBUTE_NAME_CLASS);
        if (TOPIC_INCLUDE.matches(cls) || PR_D_CODEREF.matches(cls)) {
            elementStack.push(new StackItem(cls, stackItem.include));
            boolean include = false;
            if (stackItem.include) {
                try {
                    final URI hrefValue = toURI(atts.getValue(ATTRIBUTE_NAME_HREF));
                    if (hrefValue != null) {
                        logger.debug("Resolve " + localName + " " + currentFile.resolve(hrefValue));
                        final String parse = getParse(atts.getValue(ATTRIBUTE_NAME_PARSE));
                        switch (parse) {
                            case "text":
                                include = includeText(atts);
                                break;
                            case "xml":
                                include = includeXml(atts);
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
            }
            final Deque<StackItem> childStack = new ArrayDeque<>();
            childStack.push(new StackItem(cls, include));
            includeStack.push(childStack);
        } else if (TOPIC_FALLBACK.matches(cls)) {
            assert elementStack.size() == 1;
            elementStack.push(new StackItem(cls, stackItem.include));
            // ignore
        } else {
            elementStack.push(new StackItem(cls, stackItem.include));
            if (stackItem.include) {
                super.startElement(uri, localName, name, atts);
            }
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String name) throws SAXException {
        final Deque<StackItem> elementStack = includeStack.peek();
        final StackItem stackItem = elementStack.pop();

        if (TOPIC_INCLUDE.matches(stackItem.cls) || PR_D_CODEREF.matches(stackItem.cls)) {
            final Deque<StackItem> pop = includeStack.pop();
            assert pop.size() == 0;
            final Deque<StackItem> parentStack = includeStack.peek();
            parentStack.pop();
            // ignore
        } else if (TOPIC_FALLBACK.matches(stackItem.cls)) {
            // ignore
        } else {
            if (stackItem.include) {
                super.endElement(uri, localName, name);
            }
        }
    }

    @Override
    public void characters(char ch[], int start, int length) throws SAXException {
        final Deque<StackItem> elementStack = includeStack.peek();
        if (!elementStack.peek().include) {
            return;
        }
        super.characters(ch, start, length);
    }

    @Override
    public void ignorableWhitespace(char ch[], int start, int length) throws SAXException {
        final Deque<StackItem> elementStack = includeStack.peek();
        if (!elementStack.peek().include) {
            return;
        }
        super.ignorableWhitespace(ch, start, length);
    }

    @Override
    public void processingInstruction(String target, String data) throws SAXException {
        final Deque<StackItem> elementStack = includeStack.peek();
        if (!elementStack.peek().include) {
            return;
        }
        super.processingInstruction(target, data);
    }

    @Override
    public void skippedEntity(String name) throws SAXException {
        final Deque<StackItem> elementStack = includeStack.peek();
        if (!elementStack.peek().include) {
            return;
        }
        super.skippedEntity(name);
    }

    // Private methods ---------------------------------------------------------

    private boolean includeXml(final Attributes atts) {
        final URI hrefValue = toURI(atts.getValue(ATTRIBUTE_NAME_HREF));
        final Job.FileInfo fileInfo = job.getFileInfo(stripFragment(currentFile.resolve(hrefValue)));
        final DocumentBuilder builder = getDocumentBuilder();
        builder.setEntityResolver(getCatalogResolver());
        builder.setErrorHandler(new DITAOTXMLErrorHandler(fileInfo.src.toString(), logger));
        try {
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
        } catch (SAXException | IOException | TransformerException e) {
            logger.error("Failed to process include {}", fileInfo.src, e);
            return false;
        }
        return true;
    }

    private boolean includeText(final Attributes atts) {
        final URI hrefValue = toURI(atts.getValue(ATTRIBUTE_NAME_HREF));
        final Charset charset = getCharset(atts.getValue(ATTRIBUTE_NAME_FORMAT), atts.getValue(ATTRIBUTE_NAME_ENCODING));
        final Range range = getRange(hrefValue);
        final File codeFile = getFile(hrefValue);
        if (codeFile != null) {
            try (BufferedReader codeReader = Files.newBufferedReader(codeFile.toPath(), charset)) {
                range.copyLines(codeReader);
            } catch (final Exception e) {
                logger.error("Failed to process include {}", codeFile, e);
                return false;
            }
        }
        return true;
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

}
