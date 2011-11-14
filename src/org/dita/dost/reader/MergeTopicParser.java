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
import static javax.xml.XMLConstants.*;

import java.io.File;

import org.dita.dost.exception.DITAOTXMLErrorHandler;
import org.dita.dost.module.Content;
import org.dita.dost.module.ContentImpl;
import org.dita.dost.util.Configuration;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.MergeUtils;
import org.dita.dost.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
/**
 * MergeTopicParser reads topic file and transform the references to other dita
 * files into internal references. The parse result of MergeTopicParser will be
 * returned to MergeMapParser and serves as part of intermediate merged result.
 * Instances are reusable but not thread-safe.
 * 
 * @author Zhang, Yuan Peng
 */
public final class MergeTopicParser extends AbstractXMLReader {
    
    private static final String ATTRIBUTE_NAME_OHREF = "ohref";
    private static final String ATTRIBUTE_NAME_OID = "oid";
    
    private final StringBuffer topicInfo;
    private final ContentImpl content;
    private String dirPath = null;
    private String filePath = null;
    private boolean isFirstTopicId = false;
    private boolean isFirstTopic = false;
    private String rootLang = null;

    private final XMLReader reader;
    private String retId = null;
    private final MergeUtils util;

    /**
     * Default Constructor.
     * 
     * @param util merge utility
     */
    public MergeTopicParser(final MergeUtils util) {
        this.util = util;
        content = new ContentImpl();
        topicInfo = new StringBuffer(INT_1024);
        try{
            reader = StringUtils.getXMLReader();
            reader.setContentHandler(this);
            reader.setFeature(FEATURE_NAMESPACE_PREFIX, true);
        }catch (final Exception e){
            throw new RuntimeException("Failed to initialize XML parser: " + e.getMessage(), e);
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

    /**
     * @return content value {@code StringBuffer}
     */
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
                .append(ATTRIBUTE_NAME_OID).append(EQUAL).append(QUOTATION)
                .append(StringUtils.escapeXML(attValue)).append(QUOTATION);
                retAttValue = util.getIdValue(value);
            }else{
                topicInfo.append(STRING_BLANK)
                .append(ATTRIBUTE_NAME_OID).append(EQUAL).append(QUOTATION)
                .append(StringUtils.escapeXML(attValue)).append(QUOTATION);
                retAttValue = util.addId(value);
            }
            if(isFirstTopicId){
                isFirstTopicId = false;
                //retId = retAttValue;
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
        String pathFromMap;
        String retAttValue = attValue;
        if (sharpIndex != -1){ // href value refer to an id in a topic
            if(sharpIndex == 0){
                pathFromMap = filePath.replace(WINDOWS_SEPARATOR, UNIX_SEPARATOR);
            }else{
                pathFromMap = FileUtils.resolveTopic(new File(filePath).getParent(),attValue.substring(0,sharpIndex)).replace(WINDOWS_SEPARATOR, UNIX_SEPARATOR);
            }

            topicInfo.append(STRING_BLANK)
            .append(ATTRIBUTE_NAME_OHREF).append(EQUAL).append(QUOTATION)
            .append(pathFromMap)
            .append(attValue.substring(sharpIndex))
            .append(QUOTATION);

            String topicId = attValue.substring(sharpIndex);
            final int slashIndex = topicId.indexOf(SLASH);
            final int index = attValue.indexOf(SLASH, sharpIndex);
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
            .append(ATTRIBUTE_NAME_OHREF).append(EQUAL).append(QUOTATION)
            .append(pathFromMap)
            .append(QUOTATION);

            if(util.findId(pathFromMap)){
                retAttValue = SHARP + util.getIdValue(pathFromMap);
            }else{
                final String fileId = MergeUtils.getFirstTopicId(pathFromMap, dirPath , false);
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
        filePath = (index != -1) ? filename.substring(0,index):filename;
        dirPath = dir;
        try{
            reader.setErrorHandler(new DITAOTXMLErrorHandler(dir + File.separator + filePath));
            reader.parse(dir + File.separator + filePath);
            return retId;
        } catch (final Exception e){
            throw new RuntimeException("Failed to parse " + filename + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void startDocument() throws SAXException {
        isFirstTopicId = true;
        isFirstTopic = true;
        rootLang = null;
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
        // Skip redundant <dita> tags.
        if (ELEMENT_NAME_DITA.equalsIgnoreCase(qName)) {
            rootLang = attributes.getValue(XML_NS_URI, "lang");
            return;
        }

        topicInfo.append(LESS_THAN).append(qName);
        final AttributesImpl atts = new AttributesImpl(attributes);
        final String classValue = atts.getValue(ATTRIBUTE_NAME_CLASS);

        // Add default language
        if (TOPIC_TOPIC.matches(classValue) && isFirstTopic) {
            if (atts.getIndex(XML_NS_URI, "lang") == -1) {
                atts.addAttribute(XML_NS_URI, "lang", XML_NS_PREFIX + ":lang", "CDATA",
                                  rootLang != null ? rootLang : Configuration.configuration.get("default.language"));
                rootLang = null;
            }
            isFirstTopic = false;
        }
        
        final int attsLen = atts.getLength();
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
                final String scopeValue = atts.getValue(ATTRIBUTE_NAME_SCOPE);
                final String formatValue = atts.getValue(ATTRIBUTE_NAME_FORMAT);
                final int sharpIndex = attValue.indexOf(SHARP);

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