/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2020 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.writer.include;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.writer.AbstractXMLFilter;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.io.File;
import java.net.URI;
import java.util.ArrayDeque;
import java.util.Deque;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.toURI;

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
                                include = new IncludeText(job, currentFile, getContentHandler(), logger).include(atts);
                                break;
                            case "xml":
                                include = new IncludeXml(job, currentFile, getContentHandler(), logger).include(atts);
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




    private String getParse(final String value) {
        if (value == null) {
            return "text";
        }
        return value;
    }
}
