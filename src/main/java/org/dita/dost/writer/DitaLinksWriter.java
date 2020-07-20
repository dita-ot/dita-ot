/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2004, 2005 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.writer;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.util.Job;
import org.dita.dost.util.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Map;

import static javax.xml.XMLConstants.NULL_NS_URI;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.getRelativePath;
import static org.dita.dost.util.XMLUtils.AttributesBuilder;
import static org.dita.dost.util.XMLUtils.getChildElements;

/**
 * Read DITA topic file and insert map links information into it.
 */
public final class DitaLinksWriter extends AbstractXMLFilter {

    private String curMatchTopic;
    private boolean firstTopic;

    private Map<String, Element> indexEntries;
    /** Stack of topic IDs. */
    private Deque<String> topicIdStack;
    private final ArrayList<String> topicSpecList;
    private static final Attributes relatedLinksAtts = new AttributesBuilder()
            .add(ATTRIBUTE_NAME_CLASS, TOPIC_RELATED_LINKS.toString())
            .build();
    private URI baseURI;

    /**
     * Default constructor of DitaLinksWriter class.
     */
    public DitaLinksWriter() {
        super();
        topicSpecList = new ArrayList<>();
    }

    @Override
    public void setJob(final Job job) {
        super.setJob(job);
        final Job.FileInfo in = job.getFileInfo(fi -> fi.isInput).iterator().next();
        baseURI = job.tempDir.toURI().resolve(in.uri);
    }

    /**
     * Set relates links
     *
     * @param indexEntries map of related links. Keys are topic IDs and
     * {@link org.dita.dost.util.Constants#SHARP #} is used to denote root element
     */
    public void setLinks(final Map<String, Element> indexEntries) {
        this.indexEntries = indexEntries;
    }

    @Override
    public void write(final File filename) throws DITAOTException {
        if (filename == null || !job.getStore().exists(filename.toURI())) {
            return;
        }
        curMatchTopic = indexEntries.containsKey(SHARP) ? SHARP : null;
        topicIdStack = new ArrayDeque<>();
        super.write(filename);
    }

    // SAX methods

    @Override
    public void startDocument() throws SAXException {
        topicIdStack.clear();
        firstTopic = true;
        getContentHandler().startDocument();
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes atts)
            throws SAXException {
        // only care about adding related links to topics.
        if (TOPIC_TOPIC.matches(atts)) {
            if (!topicSpecList.contains(localName)) {
                topicSpecList.add(localName);
            }
            topicIdStack.addFirst(atts.getValue(ATTRIBUTE_NAME_ID));
            if (curMatchTopic != null && !firstTopic) {
                try {
                    getContentHandler().startElement(NULL_NS_URI, TOPIC_RELATED_LINKS.localName, TOPIC_RELATED_LINKS.localName, relatedLinksAtts);
                    domToSax(indexEntries.get(curMatchTopic));
                    getContentHandler().endElement(NULL_NS_URI, TOPIC_RELATED_LINKS.localName, TOPIC_RELATED_LINKS.localName);
                    curMatchTopic = null;
                } catch (final RuntimeException e) {
                    throw e;
                } catch (final Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
            final String t = StringUtils.join(topicIdStack, SLASH);
            if (indexEntries.containsKey(t)) {
                curMatchTopic = t;
            } else if (indexEntries.containsKey(topicIdStack.peekFirst())) {
                curMatchTopic = topicIdStack.peekFirst();
            }
            if (firstTopic) {
                firstTopic = false;
            }
        }
        getContentHandler().startElement(uri, localName, qName, atts);
        if (TOPIC_RELATED_LINKS.matches(atts) && curMatchTopic != null) {
            domToSax(indexEntries.get(curMatchTopic));
            curMatchTopic = null;
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        if (topicSpecList.contains(localName)) {
            // Remove the last topic id.
            if (!topicIdStack.isEmpty()) {
                topicIdStack.removeFirst();
            }
            if (firstTopic) {
                firstTopic = false;
            }
        }
        if (curMatchTopic != null && topicSpecList.contains(localName)) {
            // if <TOPIC_RELATED_LINKS> doesn't exist
            getContentHandler().startElement(NULL_NS_URI, TOPIC_RELATED_LINKS.localName, TOPIC_RELATED_LINKS.localName, relatedLinksAtts);
            domToSax(indexEntries.get(curMatchTopic));
            getContentHandler().endElement(NULL_NS_URI, TOPIC_RELATED_LINKS.localName, TOPIC_RELATED_LINKS.localName);
            curMatchTopic = null;
        }
        getContentHandler().endElement(uri, localName, qName);
    }

    // DOM to SAX conversion methods

    private void domToSax(final Node root) throws SAXException {
        try {
            final FilterHandler handler = new FilterHandler(getContentHandler());
            final NodeList children = root.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                final Node links = rewriteLinks((Element) children.item(i));
                job.getStore().writeDocument(links, handler);
            }
        } catch (IOException e) {
            throw new SAXException("Failed to serialize DOM node to SAX: " + e.getMessage(), e);
        }
    }

    /** Relativize links */
    private Element rewriteLinks(final Element src) {
        final Element dst = (Element) src.cloneNode(true);
        for (final Element desc: getChildElements(dst, TOPIC_DESC, true)) {
            for (final Element elem: getChildElements(desc, true)) {
                final Attr href = elem.getAttributeNode(ATTRIBUTE_NAME_HREF);
                final String scope = elem.getAttribute(ATTRIBUTE_NAME_SCOPE);
                if (href != null && !scope.equals(ATTR_SCOPE_VALUE_EXTERNAL)) {
                    final URI abs = baseURI.resolve(href.getValue());
                    final URI rel = getRelativePath(currentFile, abs);
                    href.setValue(rel.toString());
                }
            }
        }
        return dst;
    }

    private static class FilterHandler extends XMLFilterImpl {

        public FilterHandler(final ContentHandler handler) {
            super();
            setContentHandler(handler);
        }

        @Override
        public void startDocument() throws SAXException {
            // ignore
        }

        @Override
        public void endDocument() throws SAXException {
            // ignore
        }

    }

}
