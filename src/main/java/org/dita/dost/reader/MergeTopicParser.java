/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.reader;

import static org.dita.dost.util.Constants.*;
import static javax.xml.XMLConstants.*;
import static org.dita.dost.reader.MergeMapParser.*;

import java.io.File;
import java.net.URI;

import org.dita.dost.exception.DITAOTXMLErrorHandler;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.util.Configuration;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.MergeUtils;
import org.dita.dost.util.StringUtils;
import org.dita.dost.util.URLUtils;
import org.dita.dost.util.XMLUtils;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * MergeTopicParser reads topic file and transform the references to other dita
 * files into internal references. The parse result of MergeTopicParser will be
 * returned to MergeMapParser and serves as part of intermediate merged result.
 * Instances are reusable but not thread-safe.
 */
public final class MergeTopicParser extends XMLFilterImpl {
        
    private String dirPath = null;
    private String filePath = null;
    private boolean isFirstTopic = false;
    private String rootLang = null;

    private final XMLReader reader;
    /** ID of the first topic */
    private String firstTopicId = null;
    private final MergeUtils util;
    private DITAOTLogger logger;

    /**
     * Default Constructor.
     * 
     * @param util merge utility
     */
    public MergeTopicParser(final MergeUtils util) {
        this.util = util;
        try{
            reader = StringUtils.getXMLReader();
            reader.setContentHandler(this);
            reader.setFeature(FEATURE_NAMESPACE_PREFIX, true);
        }catch (final Exception e){
            throw new RuntimeException("Failed to initialize XML parser: " + e.getMessage(), e);
        }
    }
    
    public final void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
    }

    /**
     * Get ID of the first topic
     * 
     * @return id attribute value of the first topic
     */
    public String getFirstTopicId() {
        return firstTopicId;
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
        getContentHandler().endElement(uri, localName, qName);
    }
    
    /**
     * Get new value for topic id attribute.
     * 
     * @param classValue class value
     */
    private void handleID(final String classValue, final AttributesImpl atts) {
        String idValue = atts.getValue(ATTRIBUTE_NAME_ID);
        if (idValue != null) {
            final String value = filePath + SHARP + idValue;
            if (util.findId(value)) {
                XMLUtils.addOrSetAttribute(atts, ATTRIBUTE_NAME_OID, idValue);
                idValue = util.getIdValue(value);
            } else {
                XMLUtils.addOrSetAttribute(atts, ATTRIBUTE_NAME_OID, idValue);
                idValue = util.addId(value);
            }
            XMLUtils.addOrSetAttribute(atts, ATTRIBUTE_NAME_ID, idValue);
        }
    }

    /**
     * Rewrite local DITA href value.
     *
     * <p>TODO: return type should be {@link java.net.URI}.</p>
     *
     * @param sharpIndex hash char index
     * @param attValue href attribute value
     * @return rewritten href value
     */
    private String handleLocalDita(final int sharpIndex, final String attValue, final AttributesImpl atts) {
        String pathFromMap;
        String retAttValue = attValue;
        if (sharpIndex != -1) { // href value refer to an id in a topic
            if (sharpIndex == 0) {
                pathFromMap = FileUtils.separatorsToUnix(filePath);
            } else {
                pathFromMap = FileUtils.separatorsToUnix(FileUtils.resolveTopic(new File(filePath).getParent(),attValue.substring(0,sharpIndex)));
            }
            pathFromMap = URLUtils.decode(pathFromMap);
            XMLUtils.addOrSetAttribute(atts, ATTRIBUTE_NAME_OHREF, URLUtils.clean(pathFromMap + attValue.substring(sharpIndex), false));
            String topicId = attValue.substring(sharpIndex);
            final int slashIndex = topicId.indexOf(SLASH);
            final int index = attValue.indexOf(SLASH, sharpIndex);
            topicId = slashIndex != -1 ? pathFromMap + topicId.substring(0, slashIndex) : pathFromMap + topicId;
            if (util.findId(topicId)) {// topicId found
                final String prefix = SHARP + util.getIdValue(topicId);
                retAttValue = index != -1 ? prefix + attValue.substring(index) : prefix;
            } else {//topicId not found
                final String prefix = SHARP + util.addId(topicId);
                retAttValue = index != -1 ? prefix + attValue.substring(index) : prefix;
            }
        } else { // href value refer to a topic
            pathFromMap = FileUtils.resolveTopic(new File(filePath).getParent(),attValue);
            pathFromMap = URLUtils.decode(pathFromMap);
            XMLUtils.addOrSetAttribute(atts, ATTRIBUTE_NAME_OHREF, URLUtils.clean(pathFromMap, false));
            if (util.findId(pathFromMap)) {
                retAttValue = SHARP + util.getIdValue(pathFromMap);
            } else {
                final String fileId = MergeUtils.getFirstTopicId(pathFromMap, dirPath , false);
                final String key = pathFromMap + SHARP + fileId;
                if (util.findId(key)) {
                    util.addId(pathFromMap,util.getIdValue(key));
                    retAttValue = SHARP + util.getIdValue(key);
                } else {
                    retAttValue = SHARP + util.addId(pathFromMap);
                    util.addId(key, util.getIdValue(pathFromMap));
                }

            }
        }
        return URLUtils.clean(retAttValue, false);
    }

    /**
     * Parse the file to update id.
     * 
     * @param filename relative topic system path, may contain a fragment part
     * @param dir topic directory system path
     */
    public void parse(final String filename,final String dir){
        final int index = filename.indexOf(SHARP);
        filePath = index != -1 ? filename.substring(0, index) : filename;
        dirPath = dir;
        try{
            final File f = new File(dir + File.separator + filePath);
            reader.setErrorHandler(new DITAOTXMLErrorHandler(f.getAbsolutePath(), logger));
            reader.parse(f.toURI().toString());
        } catch (final Exception e){
            throw new RuntimeException("Failed to parse " + filename + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void startDocument() throws SAXException {
        firstTopicId = null;
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
        final AttributesImpl atts = new AttributesImpl(attributes);
        final String classValue = atts.getValue(ATTRIBUTE_NAME_CLASS);

        // Add default language
        if (TOPIC_TOPIC.matches(classValue)) {
            if (isFirstTopic) {
                if (atts.getIndex(XML_NS_URI, "lang") == -1) {
                    atts.addAttribute(XML_NS_URI, "lang", XML_NS_PREFIX + ":lang", "CDATA",
                                      rootLang != null ? rootLang : Configuration.configuration.get("default.language"));
                    rootLang = null;
                }
                isFirstTopic = false;
            }
            handleID(classValue, atts);
            if (firstTopicId == null) {
                firstTopicId = atts.getValue(ATTRIBUTE_NAME_ID);
            }
        }
        handleHref(classValue, atts);
        
        getContentHandler().startElement(uri, localName, qName, atts);
    }

    /**
     * Rewrite href attribute.
     * 
     * @param classValue element class value
     * @param atts attributes
     */
    private void handleHref(final String classValue, final AttributesImpl atts) {
        final String attValue = atts.getValue(ATTRIBUTE_NAME_HREF);
        if (attValue != null) {
            final String scopeValue = atts.getValue(ATTRIBUTE_NAME_SCOPE);
            final int sharpIndex = attValue.indexOf(SHARP);
            if ((scopeValue == null || ATTR_SCOPE_VALUE_LOCAL.equalsIgnoreCase(scopeValue))
                    && attValue.indexOf(COLON_DOUBLE_SLASH) == -1) {
                final String formatValue = atts.getValue(ATTRIBUTE_NAME_FORMAT);
                //The scope for @href is local
                if ((TOPIC_XREF.matches(classValue) || TOPIC_LINK.matches(classValue)
                			// term and keyword are resolved as keyref can make them links
                			|| TOPIC_TERM.matches(classValue) || TOPIC_KEYWORD.matches(classValue))
                        && (formatValue == null || ATTR_FORMAT_VALUE_DITA.equalsIgnoreCase(formatValue))) {
                    //local xref or link that refers to dita file
                    XMLUtils.addOrSetAttribute(atts, ATTRIBUTE_NAME_HREF, handleLocalDita(sharpIndex, attValue, atts));
                } else {
                    //local @href other than local xref and link that refers to dita file
                    XMLUtils.addOrSetAttribute(atts, ATTRIBUTE_NAME_HREF, handleLocalHref(attValue));
                }
            }
        }
    }

    /**
     * Rewrite local non-DITA href value.
     * 
     * <p>TODO: return type should be {@link java.net.URI}.</p>
     * 
     * @param attValue href attribute value
     * @return rewritten href value
     */
    private String handleLocalHref(final String attValue) {
        final File parentFile = new File(filePath).getParentFile();
        if (parentFile != null) {
            final URI d = new File(dirPath).toURI();
            final URI p = new File(dirPath, filePath).getParentFile().toURI();
            final String b = d.relativize(p).toASCIIString();
            final StringBuilder ret = new StringBuilder(b);
            if (!b.endsWith(URI_SEPARATOR)) {
                ret.append(URI_SEPARATOR);
            }
            ret.append(attValue);
            return FileUtils.normalize(ret.toString(), URI_SEPARATOR);
        } else {
            return attValue;
        }
    }

}