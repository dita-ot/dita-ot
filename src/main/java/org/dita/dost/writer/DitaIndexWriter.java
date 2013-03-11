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
import java.util.List;
import java.util.Properties;

import org.dita.dost.exception.DITAOTXMLErrorHandler;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.module.Content;
import org.dita.dost.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;


/*
 * Created on 2004-12-17
 */

/**
 * DitaIndexWriter reads dita topic file and insert the index information into it.
 * 
 * @author Zhang, Yuan Peng
 */
public final class DitaIndexWriter extends AbstractXMLWriter {
    private String firstMatchTopic;
    /** whether we have met <metadata> in <prolog> element */
    private boolean hasMetadataTillNow;
    /** whether we have met <prolog> in this topic we want */
    private boolean hasPrologTillNow;

    private String indexEntries;
    private String lastMatchTopic;
    /** topic path that topicIdList need to match */
    private List<String> matchList;
    private boolean needResolveEntity;
    private OutputStreamWriter output;
    private final XMLReader reader;
    /** whether to insert links at this topic */
    private boolean startTopic;
    /** array list that is used to keep the hierarchy of topic id */
    private final List<String> topicIdList;
    private boolean insideCDATA;
    private boolean hasWritten;
    private final ArrayList<String> topicSpecList = new ArrayList<String>();
    private int topicLevel = -1;


    /**
     * Default constructor of DitaIndexWriter class.
     */
    public DitaIndexWriter() {
        super();
        topicIdList = new ArrayList<String>(INT_16);
        firstMatchTopic = null;
        hasMetadataTillNow = false;
        hasPrologTillNow = false;
        indexEntries = null;
        lastMatchTopic = null;
        matchList = null;
        needResolveEntity = false;
        output = null;
        startTopic = false;
        insideCDATA = false;
        hasWritten = false;

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

    /**
     * check whether the hierarchy of current node match the matchList
     */
    private boolean checkMatch() {
        if (matchList == null){
            return true;
        }
        final int matchSize = matchList.size();
        final int ancestorSize = topicIdList.size();
        final List<String> tail = topicIdList.subList(ancestorSize - matchSize, ancestorSize);
        return matchList.equals(tail);
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
        if (!startTopic){
            topicIdList.remove(topicIdList.size() - 1);
        }
        try {
            if (topicSpecList.contains(localName)){
                topicLevel--;
            }

            if (!hasMetadataTillNow && TOPIC_PROLOG.localName.equals(qName) && startTopic && !hasWritten) {
                output.write(META_HEAD);
                output.write(indexEntries);
                output.write(META_END);
                hasMetadataTillNow = true;
                hasWritten = true;
            }
            output.write(LESS_THAN + SLASH + qName
                    + GREATER_THAN);
            if (!hasPrologTillNow && startTopic && !hasWritten && !topicSpecList.contains(localName)) {
                // if <prolog> don't exist
                output.write(LINE_SEPARATOR);
                output.write(PROLOG_HEAD + META_HEAD);
                output.write(indexEntries);
                output.write(META_END + PROLOG_END);
                hasPrologTillNow = true;
                hasWritten = true;
            }

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

    private boolean hasMetadata(final String qName){
        //check whether there is <metadata> in <prolog> element
        //if there is <prolog> element and there is no <metadata> element before
        //and current element is <resourceid>, then there is no <metadata> in current
        //<prolog> element. return false.
        if(hasPrologTillNow && !hasMetadataTillNow && TOPIC_RESOURCEID.localName.equals(qName)){
            return false;
        }
        return true;
    }

    private boolean hasProlog(final Attributes atts){
        //check whether there is <prolog> in the current topic
        //if current element is <body> and there is no <prolog> before
        //then this topic has no <prolog> and return false

        if (atts.getValue(ATTRIBUTE_NAME_CLASS) != null){
            if (!hasPrologTillNow){
                if (atts.getValue(ATTRIBUTE_NAME_CLASS).indexOf(TOPIC_BODY.matcher) != -1){
                    return false;
                }
                else if (atts.getValue(ATTRIBUTE_NAME_CLASS).indexOf(TOPIC_RELATED_LINKS.matcher) != -1){
                    return false;
                }
                else if (atts.getValue(ATTRIBUTE_NAME_CLASS).indexOf(TOPIC_TOPIC.matcher) != -1){

                    if (topicLevel > 0){
                        topicLevel++;
                    }else if (topicLevel == -1){
                        topicLevel = 1;
                    }else {
                        return false;
                    }
                    return false;  //Eric

                }
            }
        }
        return true;
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
     * @param content value {@code String}
     */
    @Override
    public void setContent(final Content content) {
        indexEntries = (String) content.getValue();
        if (indexEntries == null) {
            throw new IllegalArgumentException("Content value must be non-null String");
        }
    }
    
    private void setMatch(final String match) {
        int index = 0;
        matchList = new ArrayList<String>(INT_16);

        firstMatchTopic = (match.indexOf(SLASH) != -1) ? match.substring(0, match.indexOf('/')) : match;

        while (index != -1) {
            final int end = match.indexOf(SLASH, index);
            if (end == -1) {
                matchList.add(match.substring(index));
                lastMatchTopic = match.substring(index);
                index = end;
            } else {
                matchList.add(match.substring(index, end));
                index = end + 1;
            }
        }
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
    public void startElement(final String uri, final String localName, final String qName,
            final Attributes atts) throws SAXException {
        final int attsLen = atts.getLength();

        try {
            if (topicLevel != -1){
                if (!hasProlog(atts) && startTopic && !hasWritten) {
                    // if <prolog> don't exist
                    output.write(LINE_SEPARATOR);
                    output.write(PROLOG_HEAD + META_HEAD);
                    output.write(indexEntries);
                    output.write(META_END + PROLOG_END);
                    hasPrologTillNow = true;
                    hasWritten = true;
                }
            }
            if ( !startTopic && !ELEMENT_NAME_DITA.equalsIgnoreCase(qName)){
                if (atts.getValue(ATTRIBUTE_NAME_ID) != null){
                    topicIdList.add(atts.getValue(ATTRIBUTE_NAME_ID));
                }else{
                    topicIdList.add("null");
                }
                if (topicIdList.size() >= matchList.size()){
                    //To access topic by id globally
                    startTopic = checkMatch();
                }
            }

            if (!hasMetadata(qName) && startTopic && !hasWritten) {
                output.write(META_HEAD);
                output.write(indexEntries);
                output.write(META_END);
                hasMetadataTillNow = true;
                hasWritten = true;
            }

            output.write(LESS_THAN + qName);
            for (int i = 0; i < attsLen; i++) {
                final String attQName = atts.getQName(i);
                String attValue;
                attValue = atts.getValue(i);

                // replace '&' with '&amp;'
                if (attValue.indexOf('&') > 0) {
                    attValue = StringUtils.replaceAll(attValue, "&", "&amp;");
                }

                output.write(new StringBuffer().append(STRING_BLANK)
                        .append(attQName).append(EQUAL).append(QUOTATION)
                        .append(attValue).append(QUOTATION).toString());
            }
            output.write(GREATER_THAN);
            if (atts.getValue(ATTRIBUTE_NAME_CLASS) != null){

                if (atts.getValue(ATTRIBUTE_NAME_CLASS)
                        .indexOf(TOPIC_METADATA.matcher) != -1 && startTopic && !hasWritten) {
                    hasMetadataTillNow = true;
                    output.write(indexEntries);
                    hasWritten = true;
                }
                if (atts.getValue(ATTRIBUTE_NAME_CLASS)
                        .indexOf(TOPIC_PROLOG.matcher) != -1) {
                    hasPrologTillNow = true;
                }
            }
        } catch (final Exception e) {
            logger.logError(e.getMessage(), e) ;
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
    public void write(final String outputFilename) {
        String filename = outputFilename;
        String file = null;
        String topic = null;
        File inputFile = null;
        File outputFile = null;

        try {
            if(filename.endsWith(SHARP)){
                filename = filename.substring(0, filename.length()-1);
            }

            if(filename.lastIndexOf(SHARP)!=-1){
                file = filename.substring(0,filename.lastIndexOf(SHARP));
                topic = filename.substring(filename.lastIndexOf(SHARP)+1);
                setMatch(topic);
                startTopic = false;
            }else{
                file = filename;
                matchList = null;
                startTopic = true;
            }
            needResolveEntity = true;
            hasPrologTillNow = false;
            hasMetadataTillNow = false;
            hasWritten = false;
            inputFile = new File(file);
            outputFile = new File(file + FILE_EXTENSION_TEMP);
            output = new OutputStreamWriter(new FileOutputStream(outputFile), UTF8);

            topicIdList.clear();
            reader.setErrorHandler(new DITAOTXMLErrorHandler(file, logger));
            reader.parse(file);
        } catch (final Exception e) {
            logger.logError(e.getMessage(), e) ;
        }finally {
            if (output != null) {
                try{
                    output.close();
                } catch (final Exception e) {
                    logger.logError(e.getMessage(), e) ;
                }
            }
        }
        try {
            if(!inputFile.delete()){
                logger.logError(MessageUtils.getInstance().getMessage("DOTJ009E", inputFile.getPath(), outputFile.getPath()).toString());
            }
            if(!outputFile.renameTo(inputFile)){
                logger.logError(MessageUtils.getInstance().getMessage("DOTJ009E", inputFile.getPath(), outputFile.getPath()).toString());
            }
        } catch (final Exception e) {
            logger.logError(e.getMessage(), e) ;
        }
    }
}
