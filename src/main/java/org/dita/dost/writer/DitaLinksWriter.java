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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;

import org.dita.dost.exception.DITAOTXMLErrorHandler;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.module.Content;
import org.dita.dost.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;


/**
 * DitaLinksWriter reads dita topic file and insert map links information into it.
 * 
 * @author Zhang, Yuan Peng
 * 
 */
public final class DitaLinksWriter extends AbstractXMLWriter {
    private String curMatchTopic;
    private boolean firstTopic; //Eric

    private Map<String, String> indexEntries;
    private Set<String> topicSet;
    private boolean needResolveEntity;
    private OutputStreamWriter output;
    private final XMLReader reader;
    private Stack<String> topicIdStack; // array list that is used to keep the hierarchy of topic id
    private boolean insideCDATA;
    private final ArrayList<String> topicSpecList;  //Eric


    /**
     * Default constructor of DitaLinksWriter class.
     */
    public DitaLinksWriter() {
        super();
        topicIdStack = null;
        curMatchTopic = null;
        firstTopic = true;
        indexEntries = null;
        topicSet = null;
        needResolveEntity = false;
        output = null;
        insideCDATA = false;
        topicSpecList = new ArrayList<String>(); //Eric

        try {
            reader = StringUtils.getXMLReader();
            reader.setContentHandler(this);
            reader.setProperty(LEXICAL_HANDLER_PROPERTY,this);
            reader.setFeature(FEATURE_NAMESPACE_PREFIX, true);
            reader.setFeature("http://apache.org/xml/features/scanner/notify-char-refs", true);
            reader.setFeature("http://apache.org/xml/features/scanner/notify-builtin-refs", true);
        } catch (final Exception e) {
            throw new RuntimeException("Failed to initialize XML parser: " + e.getMessage(), e);
        }

    }

    @Override
    public void characters(final char[] ch, final int start, final int length)
            throws SAXException {
        if(needResolveEntity){
            try {
                if(insideCDATA) {
                    output.write(ch, start, length);
                } else {
                    output.write(StringUtils.escapeXML(ch, start, length));
                }
            } catch (final Exception e) {
                logger.logError(e.getMessage(), e) ;
            }
        }
    }

    @Override
    public void endCDATA() throws SAXException {
        insideCDATA = false;
        try{
            output.write(CDATA_END);
        }catch(final Exception e){
            logger.logError(e.getMessage(), e) ;
        }
    }

    @Override
    public void endDocument() throws SAXException {
        try {
            output.flush();
        } catch (final Exception e) {
            logger.logError(e.getMessage(), e) ;
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName)
            throws SAXException {
        if (topicSpecList.contains(localName)){//Eric
            // Remove the last topic id.
            if (!topicIdStack.empty()) {
                topicIdStack.pop();
            }
            if (firstTopic) {
                firstTopic = false;
            }
        }
        try {
            //Using the same type of logic that's used in DITAIndexWriter.
            if (curMatchTopic != null && topicSpecList.contains(localName)) {
                // if <prolog> don't exist
                output.write(RELATED_LINKS_HEAD);
                output.write(indexEntries.get(curMatchTopic));
                output.write(RELATED_LINKS_END);
                output.write(LINE_SEPARATOR);
                curMatchTopic = null;
            }
            output.write(LESS_THAN + SLASH + qName
                    + GREATER_THAN);
        } catch (final Exception e) {
            logger.logError(e.getMessage(), e) ;
        }
    }

    @Override
    public void endEntity(final String name) throws SAXException {
        if(!needResolveEntity){
            needResolveEntity = true;
        }
    }


    @Override
    public void ignorableWhitespace(final char[] ch, final int start, final int length)
            throws SAXException {
        try {
            output.write(ch, start, length);
        } catch (final Exception e) {
            logger.logError(e.getMessage(), e) ;
        }
    }


    @Override
    public void processingInstruction(final String target, final String data)
            throws SAXException {
        String pi;
        try {
            pi = (data != null) ? target + STRING_BLANK + data : target;
            output.write(LESS_THAN + QUESTION
                    + pi + QUESTION + GREATER_THAN);
        } catch (final Exception e) {
            logger.logError(e.getMessage(), e) ;
        }
    }

    /**
     * @param content value {@code HashMap<String, String>}
     */
    @SuppressWarnings("unchecked")
    @Override
    public void setContent(final Content content) {
        indexEntries = (HashMap<String, String>)content.getValue();
        if (indexEntries == null) {
            throw new IllegalArgumentException("Content value must be non-null HashMap<String, String>");
        }
        topicSet = indexEntries.keySet();
    }

    @Override
    public void skippedEntity(final String name) throws SAXException {
        try {
            output.write(StringUtils.getEntity(name));
        } catch (final Exception e) {
            logger.logError(e.getMessage(), e) ;
        }
    }

    @Override
    public void startCDATA() throws SAXException {
        insideCDATA = true;
        try{
            output.write(CDATA_HEAD);
        }catch(final Exception e){
            logger.logError(e.getMessage(), e) ;
        }
    }

    @Override
    public void startDocument() throws SAXException {
        topicIdStack.clear();
        firstTopic = true;
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName,
            final Attributes atts) throws SAXException {
        final int attsLen = atts.getLength();

        //only care about adding related links to topics.
        if (atts.getValue(ATTRIBUTE_NAME_CLASS) != null) {// Eric

            if (atts.getValue(ATTRIBUTE_NAME_CLASS).contains(TOPIC_TOPIC.matcher)) {

                if (!topicSpecList.contains(localName)) {
                    topicSpecList.add(localName);
                }

                if (!ELEMENT_NAME_DITA.equalsIgnoreCase(qName)) {
                    if (atts.getValue(ATTRIBUTE_NAME_ID) != null) {
                        topicIdStack.push(atts.getValue(ATTRIBUTE_NAME_ID));
                    }
                }

                if (curMatchTopic != null && !firstTopic) {

                    try {
                        output.write(RELATED_LINKS_HEAD);
                        output.write(indexEntries.get(curMatchTopic));
                        output.write(RELATED_LINKS_END);
                        output.write(LINE_SEPARATOR);
                        curMatchTopic = null;
                    } catch (final Exception e) {
                        if (atts.getValue(ATTRIBUTE_NAME_CLASS) != null) {
                            logger.logError(e.getMessage(), e) ;
                        }
                    }
                }
                final String t = StringUtils.assembleString(topicIdStack, SLASH);
                if (topicSet.contains(t)) {
                    curMatchTopic = t;
                } else if (topicSet.contains(topicIdStack.peek())) {
                    curMatchTopic = topicIdStack.peek();
                }
                if (firstTopic) {
                    firstTopic = false;
                }
            }
        }
        try {  //Eric

            output.write(LESS_THAN + qName);
            for (int i = 0; i < attsLen; i++) {
                final String attQName = atts.getQName(i);
                String attValue;
                attValue = atts.getValue(i);

                // replace '&' with '&amp;'
                // if (attValue.indexOf('&') > 0) {
                // attValue = StringUtils.replaceAll(attValue, "&", "&amp;");
                // }
                attValue = StringUtils.escapeXML(attValue);

                output.write(new StringBuffer().append(STRING_BLANK)
                        .append(attQName).append(EQUAL).append(
                                QUOTATION).append(attValue).append(
                                        QUOTATION).toString());  //Eric
            }
            output.write(GREATER_THAN);
            if (atts.getValue(ATTRIBUTE_NAME_CLASS)!=null
                    && atts.getValue(ATTRIBUTE_NAME_CLASS).indexOf(TOPIC_RELATED_LINKS.matcher) != -1
                    && curMatchTopic != null) {
                output.write(indexEntries.get(curMatchTopic));
                curMatchTopic = null;
            }

        } catch (final Exception e) {
            if (atts.getValue(ATTRIBUTE_NAME_CLASS) != null) {
                logger.logError(e.getMessage(), e) ;
            }// prevent printing stack trace when meeting <dita> which has no
            // class attribute
        }
    }

    @Override
    public void startEntity(final String name) throws SAXException {
        try {
            needResolveEntity = StringUtils.checkEntity(name);
            if(!needResolveEntity){
                output.write(StringUtils.getEntity(name));
            }
        } catch (final Exception e) {
            logger.logError(e.getMessage(), e) ;
        }

    }

    @Override
    public void write(final String filename) {
        String file = null;
        File inputFile = null;
        File outputFile = null;
        FileOutputStream fileOutput = null;

        try {

            file = filename;
            curMatchTopic = topicSet.contains(SHARP) ? SHARP : null;

            // ignore in-exists file
            if (file == null || !new File(file).exists()) {
                return;
            }

            needResolveEntity = true;
            topicIdStack = new Stack<String>();
            inputFile = new File(file);
            outputFile = new File(file + FILE_EXTENSION_TEMP);
            fileOutput = new FileOutputStream(outputFile);
            output = new OutputStreamWriter(fileOutput, UTF8);
            reader.setErrorHandler(new DITAOTXMLErrorHandler(file, logger));
            reader.parse(inputFile.toURI().toString());
            output.close();

            if(!inputFile.delete()){
                logger.logError(MessageUtils.getInstance().getMessage("DOTJ009E", inputFile.getPath(), outputFile.getPath()).toString());

            }
            if(!outputFile.renameTo(inputFile)){
                logger.logError(MessageUtils.getInstance().getMessage("DOTJ009E", inputFile.getPath(), outputFile.getPath()).toString());
            }
        } catch (final Exception e) {
            logger.logError(e.getMessage(), e) ;
        }finally {
            try {
                if (fileOutput != null) {
                    fileOutput.close();
                }
            }catch (final Exception e) {
                logger.logError(e.getMessage(), e) ;
            }
        }
    }
}
