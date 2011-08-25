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

import org.dita.dost.exception.DITAOTXMLErrorHandler;
import org.dita.dost.module.Content;
import org.dita.dost.module.ContentImpl;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.MergeUtils;
import org.dita.dost.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
/**
 * MergeTopicParser reads topic file and transform the references to other dita
 * files into internal references. The parse result of MergeTopicParser will be
 * returned to MergeMapParser and serves as part of intermediate merged result.
 * 
 * @author Zhang, Yuan Peng
 */
public final class MergeTopicParser extends AbstractXMLReader {
    private StringBuffer topicInfo = null;
    private ContentImpl content;
    private String dirPath = null;
    private String filePath = null;
    private boolean isFirstTopicId = false;

    private XMLReader reader = null;
    private String retId = null;
    private MergeUtils util;

    /**
     * Default Constructor.
     * 
     * @param util merge utility
     */
    public MergeTopicParser(final MergeUtils util) {
        try{
            if(reader == null){
                reader = StringUtils.getXMLReader();
                reader.setContentHandler(this);
                reader.setFeature(FEATURE_NAMESPACE_PREFIX, true);
            }
            synchronized(this) {
                if(topicInfo == null){
                    topicInfo = new StringBuffer(INT_1024);
                }
            }

            content = new ContentImpl();
            this.util = util;
        }catch (final Exception e){
            throw new RuntimeException("Failed to initialize merge topic parse: " + e.getMessage(), e);
        }
    }
    /**
     * reset.
     */
    public void reset() {
        topicInfo.delete(0, topicInfo.length());
    }

    @Override
    public void characters(final char[] ch, final int start, final int length) throws SAXException {
        topicInfo.append(StringUtils.escapeXML(ch, start, length));
    }

    @Override
    public void endDocument() throws SAXException {
        // NOOP
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        // Skip redundant <dita> tags.
        if (ELEMENT_NAME_DITA.equalsIgnoreCase(qName)) {
            return;
        }
        topicInfo.append(LESS_THAN)
        .append(SLASH)
        .append(qName)
        .append(GREATER_THAN);
    }

    @Override
    public Content getContent() {
        content.setValue(topicInfo);
        return content;
    }

    /**
     * @param classValue
     * @param attValue
     */
    private String handleID(final String classValue, final String attValue) {
        String retAttValue = attValue;
        if(classValue != null
                && TOPIC_TOPIC.matches(classValue)){
            // Process the topic element id
            final String value = filePath+SHARP+attValue;
            if(util.findId(value)){
                topicInfo.append(STRING_BLANK)
                .append("oid").append(EQUAL).append(QUOTATION)
                .append(StringUtils.escapeXML(attValue)).append(QUOTATION);
                retAttValue = util.getIdValue(value);
            }else{
                topicInfo.append(STRING_BLANK)
                .append("oid").append(EQUAL).append(QUOTATION)
                .append(StringUtils.escapeXML(attValue)).append(QUOTATION);
                retAttValue = util.addId(value);
            }
            if(isFirstTopicId){
                isFirstTopicId = false;
                retId = retAttValue;
                util.addId(filePath,retAttValue);
            }
        }
        return retAttValue;
    }


    /**
     * @param sharpIndex
     * @param attValue
     */
    private String handleLocalDita(final int sharpIndex, final String attValue) {
        String fileId;
        String topicId;
        String pathFromMap;
        int slashIndex;
        int index;
        String retAttValue = attValue;
        if (sharpIndex != -1){ // href value refer to an id in a topic
            if(sharpIndex == 0){
                pathFromMap = filePath.replace(WINDOWS_SEPARATOR, UNIX_SEPARATOR);
            }else{
                pathFromMap = FileUtils.resolveTopic(new File(filePath).getParent(),attValue.substring(0,sharpIndex)).replace(WINDOWS_SEPARATOR, UNIX_SEPARATOR);
            }

            topicInfo.append(STRING_BLANK)
            .append("ohref").append(EQUAL).append(QUOTATION)
            .append(pathFromMap)
            .append(attValue.substring(sharpIndex))
            .append(QUOTATION);

            topicId = attValue.substring(sharpIndex);
            slashIndex = topicId.indexOf(SLASH);
            index = attValue.indexOf(SLASH, sharpIndex);
            topicId = (slashIndex != -1)
                    ? pathFromMap + topicId.substring(0, slashIndex)
                            : pathFromMap + topicId;


                    if(util.findId(topicId)){// topicId found
                        final String prefix = SHARP + util.getIdValue(topicId);
                        retAttValue = (index!=-1)? prefix + attValue.substring(index) : prefix;
                    }else{//topicId not found
                        final String prefix = SHARP + util.addId(topicId);
                        retAttValue = (index!=-1)? prefix + attValue.substring(index) : prefix;
                    }

        }else{ // href value refer to a topic
            pathFromMap = FileUtils.resolveTopic(new File(filePath).getParent(),attValue);

            topicInfo.append(STRING_BLANK)
            .append("ohref").append(EQUAL).append(QUOTATION)
            .append(pathFromMap)
            .append(QUOTATION);

            if(util.findId(pathFromMap)){
                retAttValue = SHARP + util.getIdValue(pathFromMap);
            }else{
                fileId = util.getFirstTopicId(pathFromMap, dirPath , false);
                if (util.findId(pathFromMap + SHARP + fileId)){
                    util.addId(pathFromMap,util.getIdValue(pathFromMap + SHARP + fileId));
                    retAttValue = SHARP + util.getIdValue(pathFromMap + SHARP + fileId);
                }else{
                    retAttValue = SHARP + util.addId(pathFromMap);
                    util.addId(pathFromMap + SHARP + fileId, util.getIdValue(pathFromMap));
                }

            }
        }
        return retAttValue;
    }

    /**
     * Parse the file to update id.
     * @param filename filename
     * @param dir file dir
     * @return updated id
     */
    public String parse(final String filename,final String dir){
        final int index = filename.indexOf(SHARP);
        dirPath = dir;
        try{
            filePath = (index != -1) ? filename.substring(0,index):filename;
            reader.setErrorHandler(new DITAOTXMLErrorHandler(dir + File.separator + filePath));
            reader.parse(dir + File.separator + filePath);
            return retId;
        }catch (final Exception e){
            logger.logException(e);
            return null;
        }
    }

    @Override
    public void startDocument() throws SAXException {
        isFirstTopicId = true;
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes atts) throws SAXException {
        //write the start element of topic parsing logic;
        String classValue = null;
        String scopeValue = null;
        String formatValue = null;
        final int attsLen = atts.getLength();
        int sharpIndex;

        // Skip redundant <dita> tags.
        if (ELEMENT_NAME_DITA.equalsIgnoreCase(qName)) {
            return;
        }

        topicInfo.append(LESS_THAN).append(qName);
        classValue = atts.getValue(ATTRIBUTE_NAME_CLASS);

        for (int i = 0; i < attsLen; i++) {
            final String attQName = atts.getQName(i);
            String attValue = atts.getValue(i);

            if(ATTRIBUTE_NAME_ID.equals(attQName)){
                attValue = handleID(classValue, attValue);
            }

            if(classValue != null
                    && ATTRIBUTE_NAME_HREF.equals(attQName)
                    && attValue != null
                    && attValue.length() != 0){
                //If the element has valid @class attribute and current attribute
                //is valid @href

                scopeValue = atts.getValue(ATTRIBUTE_NAME_SCOPE);
                formatValue = atts.getValue(ATTRIBUTE_NAME_FORMAT);
                sharpIndex = attValue.indexOf(SHARP);

                //        		if (attValue.indexOf(SHARP) != -1){
                //        			attValue = attValue.substring(0, attValue.indexOf(SHARP));
                //        		}

                if((scopeValue == null
                        || ATTR_SCOPE_VALUE_LOCAL.equalsIgnoreCase(scopeValue))
                        && attValue.indexOf(COLON_DOUBLE_SLASH) == -1) {
                    //The scope for @href is local

                    if((TOPIC_XREF.matches(classValue)
                            || TOPIC_LINK.matches(classValue))
                            && (formatValue == null
                            || ATTR_FORMAT_VALUE_DITA.equalsIgnoreCase(formatValue))){
                        //local xref or link that refers to dita file

                        attValue = handleLocalDita(sharpIndex, attValue);
                    } else {
                        //local @href other than local xref and link that refers to dita file

                        attValue = handleLocalHref(attValue);
                    }
                }

            }

            //output all attributes
            topicInfo.append(STRING_BLANK)
            .append(attQName).append(EQUAL).append(QUOTATION)
            .append(StringUtils.escapeXML(attValue)).append(QUOTATION);
        }
        topicInfo.append(GREATER_THAN);
    }

    private String handleLocalHref(final String attValue) {
        String pathFromMap;
        pathFromMap = FileUtils.resolveTopic(new File(filePath).getParent(),attValue);
        return pathFromMap;
    }

    @Override
    public void processingInstruction(final String target, final String data)
            throws SAXException {
        final String pi = (data != null) ? target + STRING_BLANK + data : target;
        topicInfo.append(LESS_THAN + QUESTION
                + pi + QUESTION + GREATER_THAN);
    }


}