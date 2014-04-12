/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.reader;

import static javax.xml.XMLConstants.*;
import static org.dita.dost.reader.MergeMapParser.*;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.FileUtils.*;
import static org.dita.dost.util.URLUtils.*;

import java.io.File;
import java.net.URI;

import org.dita.dost.exception.DITAOTXMLErrorHandler;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.util.Configuration;
import org.dita.dost.util.MergeUtils;
import org.dita.dost.util.StringUtils;
import org.dita.dost.util.URLUtils;
import org.dita.dost.util.XMLUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * MergeTopicParser reads topic file and transform the references to other dita
 * files into internal references. The parse result of MergeTopicParser will be
 * returned to MergeMapParser and serves as part of intermediate merged result.
 * Instances are reusable but not thread-safe.
 */
public final class MergeTopicParser extends XMLFilterImpl {

    private File dirPath = null;
    private String filePath = null;
    private boolean isFirstTopic = false;
    private String rootLang = null;

    private final XMLReader reader;
    /** ID of the first topic */
    private String firstTopicId = null;
    private final MergeUtils util;
    private DITAOTLogger logger;
    private File output;
    
    /**
     * Default Constructor.
     * 
     * @param util merge utility
     */
    public MergeTopicParser(final MergeUtils util) {
        this.util = util;
        try {
            reader = StringUtils.getXMLReader();
            reader.setContentHandler(this);
            reader.setFeature(FEATURE_NAMESPACE_PREFIX, true);
        } catch (final Exception e) {
            throw new RuntimeException("Failed to initialize XML parser: " + e.getMessage(), e);
        }
    }

    public final void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
    }

    /**
     * Set merge output file
     * 
     * @param output merge output file
     */
    public void setOutput(File output) {
        this.output = output;
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
        if (ELEMENT_NAME_DITA.equals(qName)) {
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
            XMLUtils.addOrSetAttribute(atts, ATTRIBUTE_NAME_OID, idValue);
            final URI value = setFragment(toURI(filePath), idValue);
            if (util.findId(value)) {
                idValue = util.getIdValue(value);
            } else {
                idValue = util.addId(value);
            }
            XMLUtils.addOrSetAttribute(atts, ATTRIBUTE_NAME_ID, idValue);
        }
    }

    /**
     * Rewrite local DITA href value.
     * 
     * @param href href attribute value
     * @return rewritten href value
     */
    private URI handleLocalDita(final URI href, final AttributesImpl atts) {
        final String attValue = href.toString();
        final int sharpIndex = attValue.indexOf(SHARP);
        URI pathFromMap;
        String retAttValue = attValue;
        if (sharpIndex != -1) { // href value refer to an id in a topic
            if (sharpIndex == 0) {
                pathFromMap = toURI(filePath);
            } else {
                pathFromMap = toURI(resolveTopic(new File(filePath).getParentFile(),
                                                 attValue.substring(0, sharpIndex)));
            }
            XMLUtils.addOrSetAttribute(atts, ATTRIBUTE_NAME_OHREF,
                    URLUtils.clean(pathFromMap + attValue.substring(sharpIndex), false));
            final URI topicId = toURI(pathFromMap + getElementID(SHARP + getFragment(attValue)));
            final int index = attValue.indexOf(SLASH, sharpIndex);
            final String elementId = index != -1 ? attValue.substring(index) : "";
            if (util.findId(topicId)) {// topicId found
                retAttValue = SHARP + util.getIdValue(topicId) + elementId;
            } else {// topicId not found
                retAttValue = SHARP + util.addId(topicId) + elementId;
            }
        } else { // href value refer to a topic
            pathFromMap = toURI(resolveTopic(new File(filePath).getParent(), attValue));
            XMLUtils.addOrSetAttribute(atts, ATTRIBUTE_NAME_OHREF, pathFromMap.toString());
            if (util.findId(pathFromMap)) {
                retAttValue = SHARP + util.getIdValue(pathFromMap);
            } else {
                final String fileId = MergeUtils.getFirstTopicId(pathFromMap, dirPath, false);
                final URI key = setFragment(pathFromMap, fileId);
                if (util.findId(key)) {
                    util.addId(pathFromMap, util.getIdValue(key));
                    retAttValue = SHARP + util.getIdValue(key);
                } else {
                    retAttValue = SHARP + util.addId(pathFromMap);
                    util.addId(key, util.getIdValue(pathFromMap));
                }

            }
        }
        return toURI(retAttValue);
    }

    private String getElementID(final String fragment) {
        final int slashIndex = fragment.indexOf(SLASH);
        return slashIndex != -1 ? fragment.substring(0, slashIndex) : fragment;
    }

    /**
     * Parse the file to update id.
     * 
     * @param filename relative topic system path, may contain a fragment part
     * @param dir topic directory system path
     */
    public void parse(final String filename, final File dir) {
        filePath = stripFragment(filename);
        dirPath = dir;
        try {
            final File f = new File(dir, filePath);
            reader.setErrorHandler(new DITAOTXMLErrorHandler(f.getAbsolutePath(), logger));
            logger.info("Processing " + f.getAbsolutePath());
            reader.parse(f.toURI().toString());
        } catch (final Exception e) {
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
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes)
            throws SAXException {
        // Skip redundant <dita> tags.
        if (ELEMENT_NAME_DITA.equals(qName)) {
            rootLang = attributes.getValue(XML_NS_URI, "lang");
            return;
        }
        final AttributesImpl atts = new AttributesImpl(attributes);
        final String classValue = atts.getValue(ATTRIBUTE_NAME_CLASS);

        // Add default language
        if (TOPIC_TOPIC.matches(classValue)) {
            if (isFirstTopic) {
                if (atts.getIndex(XML_NS_URI, "lang") == -1) {
                    atts.addAttribute(XML_NS_URI, "lang", XML_NS_PREFIX + ":lang", "CDATA", rootLang != null ? rootLang
                            : Configuration.configuration.get("default.language"));
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
        final URI attValue = toURI(atts.getValue(ATTRIBUTE_NAME_HREF));
        if (attValue != null) {
            final String scopeValue = atts.getValue(ATTRIBUTE_NAME_SCOPE);
            if ((scopeValue == null || ATTR_SCOPE_VALUE_LOCAL.equals(scopeValue))
                    && attValue.getScheme() == null) {
                final String formatValue = atts.getValue(ATTRIBUTE_NAME_FORMAT);
                // The scope for @href is local
                if ((TOPIC_XREF.matches(classValue) || TOPIC_LINK.matches(classValue) || TOPIC_LQ.matches(classValue)
                // term and keyword are resolved as keyref can make them links
                        || TOPIC_TERM.matches(classValue) || TOPIC_KEYWORD.matches(classValue))
                        && (formatValue == null || ATTR_FORMAT_VALUE_DITA.equals(formatValue))) {
                    // local xref or link that refers to dita file
                    XMLUtils.addOrSetAttribute(atts, ATTRIBUTE_NAME_HREF, handleLocalDita(attValue, atts).toString());
                } else {
                    // local @href other than local xref and link that refers to
                    // dita file
                    XMLUtils.addOrSetAttribute(atts, ATTRIBUTE_NAME_HREF, handleLocalHref(attValue).toString());
                }
            }
        }
    }

    /**
     * Rewrite local non-DITA href value.
     * 
     * @param attValue href attribute value
     * @return rewritten href value
     */
    private URI handleLocalHref(final URI attValue) {
        final URI current = new File(dirPath, filePath).toURI().normalize();
        final URI reference = current.resolve(attValue);
        final URI merge = output.toURI();
        return getRelativePath(merge, reference);
    }

}