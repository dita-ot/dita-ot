/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.reader;

import static org.dita.dost.util.Constants.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Stack;
import java.util.StringTokenizer;

import org.dita.dost.exception.DITAOTXMLErrorHandler;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.module.Content;
import org.dita.dost.module.ContentImpl;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.MergeUtils;
import org.dita.dost.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * MergeMapParser reads the ditamap file after preprocessing and merges
 * different files into one intermediate result. It calls MergeTopicParser
 * to process the topic file. Instances are reusable but not thread-safe.
 * 
 * @author Zhang, Yuan Peng
 */
public final class MergeMapParser extends AbstractXMLReader {
    
    private final XMLReader reader;
    private final StringBuffer mapInfo;
    private final MergeTopicParser topicParser;
    private final MergeUtils util;
    private final ContentImpl content;
    private String dirPath = null;
    private String tempdir = null;

    private final Stack<String> processStack;
    private int processLevel;

    /**
     * Default Constructor.
     */
    public MergeMapParser() {
        mapInfo = new StringBuffer(INT_1024);
        processStack = new Stack<String>();
        processLevel = 0;
        util = new MergeUtils();
        topicParser = new MergeTopicParser(util);
        topicParser.setLogger(logger);
        content = new ContentImpl();
        try{
            reader = StringUtils.getXMLReader();
            reader.setContentHandler(this);
            reader.setFeature(FEATURE_NAMESPACE_PREFIX, true);
        }catch (final Exception e){
            throw new RuntimeException("Failed to initialize XML parser: " + e.getMessage(), e);
        }
    }

    /**
     * @return content value {@code StringBuffer}
     */
    @Override
    public Content getContent() {
        content.setValue(mapInfo.append((StringBuffer)topicParser.getContent().getValue()));
        return content;
    }

    /**
     * Read map.
     * 
     * @param ditaInput input file path + pipe character + temporary directory path, or only input file path
     * @deprecated use {@link #read(String, String)} instead
     */
    @Deprecated
    @Override
    public void read(final String ditaInput) {
        String filename;
        if(ditaInput.contains(STICK)){
            filename = ditaInput.substring(0, ditaInput.indexOf(STICK));
            tempdir = ditaInput.substring(ditaInput.indexOf(STICK)+1);
        }else{
            filename = ditaInput;
            tempdir = new File(filename).getParent();
        }
        read(filename, tempdir);
    }

    /**
     * Read map.
     * 
     * @param input map file path
     * @param tmpdir temporary directory path, may be {@code null}
     */
    public void read(final String filename, final String tmpDir) {
        tempdir = tmpDir != null ? tmpDir : new File(filename).getParent();
        try{
            final File input = new File(filename);
            dirPath = input.getParent();
            reader.setErrorHandler(new DITAOTXMLErrorHandler(input.getAbsolutePath()));
            reader.parse(input.toURI().toString());
        }catch(final Exception e){
            logger.logException(e);
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        if (processLevel > 0) {
            String value = processStack.peek();
            if (processLevel == processStack.size()) {
                value = processStack.pop();
            }
            processLevel--;

            if (ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY.equalsIgnoreCase(value)) {
                return;
            }
        }
        mapInfo.append(LESS_THAN)
        .append(SLASH)
        .append(qName)
        .append(GREATER_THAN);
    }

    @Override
    public void characters(final char[] ch, final int start, final int length) throws SAXException {
        if (processStack.empty() || !ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY.equalsIgnoreCase(processStack.peek())){
            mapInfo.append(StringUtils.escapeXML(ch, start, length));
        }
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes atts) throws SAXException {
        final String attrValue = atts.getValue(ATTRIBUTE_NAME_PROCESSING_ROLE);
        if (attrValue != null) {
            processStack.push(attrValue);
            processLevel++;
            if (ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY.equalsIgnoreCase(attrValue)) {
                // @processing-role='resource-only'
                return;
            }
        } else if (processLevel > 0) {
            processLevel++;
            if (ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY.equalsIgnoreCase(processStack.peek())) {
                // Child of @processing-role='resource-only'
                return;
            }
        }

        mapInfo.append(LESS_THAN).append(qName);
        final String classValue = atts.getValue(ATTRIBUTE_NAME_CLASS);

        final int attsLen = atts.getLength();
        for (int i = 0; i < attsLen; i++) {
            final String attQName = atts.getQName(i);
            String attValue = atts.getValue(i);
            if(ATTRIBUTE_NAME_HREF.equals(attQName)
                    && !StringUtils.isEmptyString(attValue)
                    && classValue != null
                    && MAP_TOPICREF.matches(classValue)){
                final String scopeValue = atts.getValue(ATTRIBUTE_NAME_SCOPE);
                final String formatValue = atts.getValue(ATTRIBUTE_NAME_FORMAT);

                if((scopeValue == null
                        || ATTR_SCOPE_VALUE_LOCAL.equalsIgnoreCase(scopeValue))
                        && (formatValue == null
                        || ATTR_FORMAT_VALUE_DITA.equalsIgnoreCase(formatValue))){
                    final String ohref = attValue;
                    final String copyToValue = atts.getValue(ATTRIBUTE_NAME_COPY_TO);
                    if (!StringUtils.isEmptyString(copyToValue)) {
                        attValue = copyToValue;
                    }
                    if (util.isVisited(attValue)){
                        mapInfo.append(STRING_BLANK)
                        .append("ohref").append(EQUAL).append(QUOTATION)
                        .append(StringUtils.escapeXML(ohref)).append(QUOTATION);

                        attValue = new StringBuffer(SHARP).append(util.getIdValue(attValue)).toString();
                    }else{
                        mapInfo.append(STRING_BLANK)
                        .append("ohref").append(EQUAL).append(QUOTATION)
                        .append(StringUtils.escapeXML(ohref)).append(QUOTATION);

                        //parse the topic
                        util.visit(attValue);
                        if (new File(dirPath, attValue.indexOf(SHARP) != -1 ? attValue.substring(0, attValue.indexOf(SHARP)) : attValue).exists()) {
                            final String fileId = topicParser.parse(attValue,dirPath);
                            attValue = new StringBuffer(SHARP).append(fileId).toString();
                        } else {
                            final String fileName = new File(dirPath, attValue).getAbsolutePath();
                            final Properties prop = new Properties();
                            prop.put("%1", fileName);
                            logger.logError(MessageUtils.getMessage("DOTX008E", prop).toString());
                        }
                    }
                }

            }

            //output all attributes
            mapInfo.append(STRING_BLANK)
            .append(attQName).append(EQUAL).append(QUOTATION)
            .append(StringUtils.escapeXML(attValue)).append(QUOTATION);
        }
        mapInfo.append(GREATER_THAN);

    }

    @Override
    public void processingInstruction(final String target, final String data)
            throws SAXException {
        final String pi = (data != null) ? target + STRING_BLANK + data : target;
        mapInfo.append(LESS_THAN + QUESTION
                + pi + QUESTION + GREATER_THAN);
    }

    @Override
    public void endDocument() throws SAXException {
        // read href dita topic list
        // compare visitedSet with the list
        // if list item not in visitedSet then call MergeTopicParser to parse it
        final Properties property = new Properties();
        final File ditalist = new File(tempdir, FILE_NAME_DITA_LIST);
        final File xmlDitalist = new File(tempdir, FILE_NAME_DITA_LIST_XML);
        InputStream in = null;
        try{
            if(xmlDitalist.exists()) {
                in = new FileInputStream(xmlDitalist);
                property.loadFromXML(in);
            } else {
                in = new FileInputStream(ditalist);
                property.loadFromXML(in);
            }
            String resourceOnlySet = property.getProperty(RESOURCE_ONLY_LIST);
            resourceOnlySet = (resourceOnlySet == null ? "" : resourceOnlySet);
            String skipTopicSet = property.getProperty(CHUNK_TOPIC_LIST);
            skipTopicSet = (skipTopicSet == null ? "" : skipTopicSet);
            String chunkedTopicSet = property.getProperty(CHUNKED_TOPIC_LIST);
            chunkedTopicSet = (chunkedTopicSet == null ? "" : chunkedTopicSet);
            final String hrefTargetList = property.getProperty(HREF_TARGET_LIST);
            final StringTokenizer tokenizer = new StringTokenizer(hrefTargetList,COMMA);
            while(tokenizer.hasMoreElements())
            {
                String element = (String)tokenizer.nextElement();
                if (!new File(dirPath).equals(new File(tempdir))) {
                    element = FileUtils.getRelativePathFromMap(new File(dirPath,"a.ditamap").getAbsolutePath(), new File(tempdir,element).getAbsolutePath());
                }
                if(!util.isVisited(element)){
                    util.visit(element);
                    if (!resourceOnlySet.contains(element) && (chunkedTopicSet.contains(element)
                            || !skipTopicSet.contains(element))){
                        //ensure the file exists
                        if(new File(dirPath, element).exists()){
                            topicParser.parse(element, dirPath);
                        }else{
                            final String fileName = new File(dirPath, element).getAbsolutePath();
                            final Properties prop = new Properties();
                            prop.put("%1", fileName);
                            logger.logError(MessageUtils.getMessage("DOTX008E", prop).toString());
                        }
                    }

                }
            }
        }catch (final Exception e){
            logger.logException(e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (final IOException e) {
                    logger.logException(e);
                }
            }
        }
    }

}
