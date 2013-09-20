/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.writer;

import static org.dita.dost.util.Constants.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Map;
import java.util.Set;

import org.dita.dost.exception.DITAOTXMLErrorHandler;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.module.Content;
import org.dita.dost.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;


/**
 * Read DITA topic file and insert map links information into it.
 */
public final class DitaLinksWriter extends AbstractXMLWriter {
    
    private String curMatchTopic;
    private boolean firstTopic;

    private Map<String, String> indexEntries;
    private Set<String> topicSet;
    private OutputStreamWriter output;
    private final XMLReader reader;
    /** Stack of topic IDs. */
    private Deque<String> topicIdStack;
    private final ArrayList<String> topicSpecList;

    /**
     * Default constructor of DitaLinksWriter class.
     */
    public DitaLinksWriter() {
        super();
        topicSpecList = new ArrayList<String>();
        try {
            reader = StringUtils.getXMLReader();
            reader.setContentHandler(this);
            reader.setProperty(LEXICAL_HANDLER_PROPERTY, this);
            reader.setFeature(FEATURE_NAMESPACE_PREFIX, true);
        } catch (final Exception e) {
            throw new RuntimeException("Failed to initialize XML parser: " + e.getMessage(), e);
        }
    }

    @Override
    public void setContent(final Content content) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Set relates links
     * 
     * @param indexEntries map of related links. Keys are topic IDs and
     * {@link SHARP} is used to denote root element; values are XML strings
     */
    public void setLinks(final Map<String, String> indexEntries) {
        this.indexEntries = indexEntries;
        topicSet = indexEntries.keySet();
    }

    @Override
    public void write(final File filename) {
        if (filename == null || !filename.exists()) {
            return;
        }
        curMatchTopic = topicSet.contains(SHARP) ? SHARP : null;
        topicIdStack = new ArrayDeque<String>();
        final File inputFile = filename;
        final File outputFile = new File(filename.getPath() + FILE_EXTENSION_TEMP);
        FileOutputStream fileOutput = null;
        try {
            fileOutput = new FileOutputStream(outputFile);
            output = new OutputStreamWriter(fileOutput, UTF8);
            reader.setErrorHandler(new DITAOTXMLErrorHandler(filename.getPath(), logger));
            reader.parse(inputFile.toURI().toString());
        } catch (final Exception e) {
            logger.logError(e.getMessage(), e);
        } finally {
            try {
                if (fileOutput != null) {
                    fileOutput.close();
                }
            } catch (final Exception e) {
                logger.logError(e.getMessage(), e);
            }
            try {
                if (output != null) {
                    output.close();
                }
            } catch (final Exception e) {
                logger.logError(e.getMessage(), e);
            }
        }
        if (!inputFile.delete()) {
            logger.logError(MessageUtils.getInstance().getMessage("DOTJ009E", inputFile.getPath(), outputFile.getPath()).toString());
        }
        if (!outputFile.renameTo(inputFile)) {
            logger.logError(MessageUtils.getInstance().getMessage("DOTJ009E", inputFile.getPath(), outputFile.getPath()).toString());
        }
    }

    // SAX methods

    @Override
    public void characters(final char[] ch, final int start, final int length) throws SAXException {
        try {
            output.append(StringUtils.escapeXML(ch, start, length));
        } catch (final Exception e) {
            logger.logError(e.getMessage(), e);
        }
    }

    @Override
    public void endDocument() throws SAXException {
        try {
            output.flush();
        } catch (final Exception e) {
            logger.logError(e.getMessage(), e);
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
        try {
            // Using the same type of logic that's used in DITAIndexWriter.
            if (curMatchTopic != null && topicSpecList.contains(localName)) {
                // if <prolog> don't exist
                output.append(RELATED_LINKS_HEAD)
                      .append(indexEntries.get(curMatchTopic))
                      .append(RELATED_LINKS_END);
                curMatchTopic = null;
            }
            output.append(LESS_THAN)
                  .append(SLASH)
                  .append(qName)
                  .append(GREATER_THAN);
        } catch (final Exception e) {
            logger.logError(e.getMessage(), e);
        }
    }

    @Override
    public void ignorableWhitespace(final char[] ch, final int start, final int length) throws SAXException {
        try {
            output.write(ch, start, length);
        } catch (final Exception e) {
            logger.logError(e.getMessage(), e);
        }
    }

    @Override
    public void processingInstruction(final String target, final String data) throws SAXException {
        String pi;
        try {
            pi = (data != null) ? target + STRING_BLANK + data : target;
            output.append(LESS_THAN)
                  .append(QUESTION)
                  .append(pi)
                  .append(QUESTION)
                  .append(GREATER_THAN);
        } catch (final Exception e) {
            logger.logError(e.getMessage(), e);
        }
    }

    @Override
    public void startDocument() throws SAXException {
        topicIdStack.clear();
        firstTopic = true;
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
                    output.append(RELATED_LINKS_HEAD)
                          .append(indexEntries.get(curMatchTopic))
                          .append(RELATED_LINKS_END);
                    curMatchTopic = null;
                } catch (final Exception e) {
                    logger.logError(e.getMessage(), e);
                }
            }
            final String t = StringUtils.assembleString(topicIdStack, SLASH);
            if (topicSet.contains(t)) {
                curMatchTopic = t;
            } else if (topicSet.contains(topicIdStack.peekFirst())) {
                curMatchTopic = topicIdStack.peekFirst();
            }
            if (firstTopic) {
                firstTopic = false;
            }
        }
        try {
            output.append(LESS_THAN);
            output.append(qName);
            for (int i = 0; i < atts.getLength(); i++) {
                output.append(STRING_BLANK)
                      .append(atts.getQName(i))
                      .append(EQUAL)
                      .append(QUOTATION)
                      .append(StringUtils.escapeXML(atts.getValue(i)))
                      .append(QUOTATION);
            }
            output.append(GREATER_THAN);
            if (TOPIC_RELATED_LINKS.matches(atts) && curMatchTopic != null) {
                output.append(indexEntries.get(curMatchTopic));
                curMatchTopic = null;
            }
        } catch (final Exception e) {
            logger.logError(e.getMessage(), e);
        }
    }

}
