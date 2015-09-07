/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.writer;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.util.StringUtils;
import org.w3c.dom.*;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import java.io.File;
import java.util.*;

import static javax.xml.XMLConstants.NULL_NS_URI;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.XMLUtils.AttributesBuilder;

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
    private final Transformer saxToDomTransformer;
    private static final Attributes relatedLinksAtts = new AttributesBuilder()
            .add(ATTRIBUTE_NAME_CLASS, TOPIC_RELATED_LINKS.toString())
            .build();

    /**
     * Default constructor of DitaLinksWriter class.
     */
    public DitaLinksWriter() {
        super();
        topicSpecList = new ArrayList<>();
        try {
            saxToDomTransformer = TransformerFactory.newInstance().newTransformer();
        } catch (final TransformerConfigurationException e) {
            throw new RuntimeException("Failed to configure DOM to SAX transformer: " + e.getMessage(), e);
        }
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
        if (filename == null || !filename.exists()) {
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
            final Result result = new SAXResult(new FilterHandler(getContentHandler()));
            final NodeList children = root.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                final Source source = new DOMSource(children.item(i));
                saxToDomTransformer.transform(source, result);
            }
        } catch (TransformerException e) {
            throw new SAXException("Failed to serialize DOM node to SAX: " + e.getMessage(), e);
        }
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
