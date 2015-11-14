/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.writer;

import static org.apache.commons.io.FileUtils.*;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.XMLUtils.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import org.dita.dost.exception.DITAOTXMLErrorHandler;
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

    /** whether we have met <metadata> in <prolog> element */
    private boolean hasMetadataTillNow;
    /** whether we have met <prolog> in this topic we want */
    private boolean hasPrologTillNow;

    private String indexEntries;
    /** topic path that topicIdList need to match */
    private List<String> matchList;
    private OutputStreamWriter output;
    private final XMLReader reader;
    /** whether to insert links at this topic */
    private boolean startTopic;
    /** array list that is used to keep the hierarchy of topic id */
    private final List<String> topicIdList;
    private boolean hasWritten;
    private int topicLevel = -1;


    /**
     * Default constructor of DitaIndexWriter class.
     */
    public DitaIndexWriter() {
        super();
        topicIdList = new ArrayList<>(16);
        hasMetadataTillNow = false;
        hasPrologTillNow = false;
        indexEntries = null;
        matchList = null;
        output = null;
        startTopic = false;
        hasWritten = false;

        try {
            reader = getXMLReader();
            reader.setContentHandler(this);
            reader.setFeature(FEATURE_NAMESPACE_PREFIX, true);
        } catch (final Exception e) {
            throw new RuntimeException("Failed to initialize XML parser: " + e.getMessage(), e);
        }

    }


    @Override
    public void characters(final char[] ch, final int start, final int length)
            throws SAXException {
        try {
            writeCharacters(ch, start, length);
        } catch (final IOException e) {
            logger.error(e.getMessage(), e) ;
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
    public void endDocument() throws SAXException {

        try {
            output.flush();
        } catch (final Exception e) {
            logger.error(e.getMessage(), e) ;
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName)
            throws SAXException {
        if (!startTopic){
            topicIdList.remove(topicIdList.size() - 1);
        }
        try {
            if (!hasMetadataTillNow && TOPIC_PROLOG.localName.equals(qName) && startTopic && !hasWritten) {
                
                writeStartElement(TOPIC_METADATA.localName, new AttributesBuilder().add(ATTRIBUTE_NAME_CLASS, TOPIC_METADATA.toString()).build());
                output.write(indexEntries);
                writeEndElement(TOPIC_METADATA.localName);
                hasMetadataTillNow = true;
                hasWritten = true;
            }
            writeEndElement(qName);
            if (!hasPrologTillNow && startTopic && !hasWritten) {
                // if <prolog> don't exist
                writeStartElement(TOPIC_PROLOG.localName, new AttributesBuilder().add(ATTRIBUTE_NAME_CLASS, TOPIC_PROLOG.toString()).build());
                writeStartElement(TOPIC_METADATA.localName, new AttributesBuilder().add(ATTRIBUTE_NAME_CLASS, TOPIC_METADATA.toString()).build());
                output.write(indexEntries);
                writeEndElement(TOPIC_METADATA.localName);
                writeEndElement(TOPIC_PROLOG.localName);
                hasPrologTillNow = true;
                hasWritten = true;
            }

        } catch (final Exception e) {
            logger.error(e.getMessage(), e) ;
        }
    }

    private boolean hasMetadata(final String qName){
        //check whether there is <metadata> in <prolog> element
        //if there is <prolog> element and there is no <metadata> element before
        //and current element is <resourceid>, then there is no <metadata> in current
        //<prolog> element. return false.
        return !(hasPrologTillNow && !hasMetadataTillNow && TOPIC_RESOURCEID.localName.equals(qName));
    }

    private boolean hasProlog(final Attributes atts){
        //check whether there is <prolog> in the current topic
        //if current element is <body> and there is no <prolog> before
        //then this topic has no <prolog> and return false

        if (atts.getValue(ATTRIBUTE_NAME_CLASS) != null){
            if (!hasPrologTillNow){
                if (atts.getValue(ATTRIBUTE_NAME_CLASS).contains(TOPIC_BODY.matcher)){
                    return false;
                }
                else if (atts.getValue(ATTRIBUTE_NAME_CLASS).contains(TOPIC_RELATED_LINKS.matcher)){
                    return false;
                }
                else if (atts.getValue(ATTRIBUTE_NAME_CLASS).contains(TOPIC_TOPIC.matcher)){

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
            writeCharacters(ch, start, length);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e) ;
        }
    }

    @Override
    public void processingInstruction(final String target, final String data)
            throws SAXException {
        try {
            writeProcessingInstruction(target, data);
        } catch (final IOException e) {
            logger.error(e.getMessage(), e) ;
        }
    }
    
    public void setIndexEntries(final String indexEntries) {
        this.indexEntries = indexEntries;
    }
    
    private void setMatch(final String match) {
        int index = 0;
        matchList = new ArrayList<>(16);

        while (index != -1) {
            final int end = match.indexOf(SLASH, index);
            if (end == -1) {
                matchList.add(match.substring(index));
                index = end;
            } else {
                matchList.add(match.substring(index, end));
                index = end + 1;
            }
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
                    writeStartElement(TOPIC_PROLOG.localName, new AttributesBuilder().add(ATTRIBUTE_NAME_CLASS, TOPIC_PROLOG.toString()).build());
                    writeStartElement(TOPIC_METADATA.localName, new AttributesBuilder().add(ATTRIBUTE_NAME_CLASS, TOPIC_METADATA.toString()).build());
                    output.write(indexEntries);
                    writeEndElement(TOPIC_METADATA.localName);
                    writeEndElement(TOPIC_PROLOG.localName);
                    hasPrologTillNow = true;
                    hasWritten = true;
                }
            }
            if ( !startTopic && !ELEMENT_NAME_DITA.equals(qName)){
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
                writeStartElement(TOPIC_METADATA.localName, new AttributesBuilder().add(ATTRIBUTE_NAME_CLASS, TOPIC_METADATA.toString()).build());
                output.write(indexEntries);
                writeEndElement(TOPIC_METADATA.localName);                
                hasMetadataTillNow = true;
                hasWritten = true;
            }

            writeStartElement(qName, atts);
            
            if (atts.getValue(ATTRIBUTE_NAME_CLASS) != null){

                if (atts.getValue(ATTRIBUTE_NAME_CLASS).contains(TOPIC_METADATA.matcher) && startTopic && !hasWritten) {
                    hasMetadataTillNow = true;
                    output.write(indexEntries);
                    hasWritten = true;
                }
                if (atts.getValue(ATTRIBUTE_NAME_CLASS).contains(TOPIC_PROLOG.matcher)) {
                    hasPrologTillNow = true;
                }
            }
        } catch (final Exception e) {
            logger.error(e.getMessage(), e) ;
        }
    }

    @Override
    public void write(final File outputFilename) {
        String filename = outputFilename.getAbsolutePath();
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
            logger.error(e.getMessage(), e) ;
        }finally {
            if (output != null) {
                try{
                    output.close();
                } catch (final Exception e) {
                    logger.error(e.getMessage(), e) ;
                }
            }
        }
        try {
            deleteQuietly(inputFile);
            moveFile(outputFile, inputFile);
        } catch (final Exception e) {
            logger.error("Failed to replace " + inputFile + ": " + e.getMessage());
        }
    }
    
    // SAX serializer methods
    
    private void writeStartElement(final String qName, final Attributes atts) throws IOException {
        final int attsLen = atts.getLength();
        output.write(LESS_THAN + qName);
        for (int i = 0; i < attsLen; i++) {
            final String attQName = atts.getQName(i);
            final String attValue = escapeXML(atts.getValue(i));
            output.write(STRING_BLANK + attQName + EQUAL + QUOTATION + attValue + QUOTATION);
        }
        output.write(GREATER_THAN);
    }
    
    private void writeEndElement(final String qName) throws IOException {
        output.write(LESS_THAN + SLASH + qName + GREATER_THAN);
    }
    
    private void writeCharacters(final char[] ch, final int start, final int length) throws IOException {
        output.write(escapeXML(ch, start, length));
    }

    private void writeProcessingInstruction(final String target, final String data) throws IOException {
        final String pi = data != null ? target + STRING_BLANK + data : target;
        output.write(LESS_THAN + QUESTION + pi + QUESTION + GREATER_THAN);
    }
    
}
