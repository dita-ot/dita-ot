/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2004, 2005 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.reader;

import static javax.xml.XMLConstants.*;
import static org.dita.dost.reader.MergeMapParser.*;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.FileUtils.*;
import static org.dita.dost.util.URLUtils.*;

import java.io.File;
import java.net.URI;

import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.util.Job;
import org.dita.dost.util.MergeUtils;
import org.dita.dost.util.XMLUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
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
    private String rootLang = null;

    /** ID of the first topic */
    private String firstTopicId = null;
    private final MergeUtils util;
    private Job job;
    private DITAOTLogger logger;
    private File output;
    private final String GENERATED_DITA_ELEMENT_ID = "GENERATED-DITA-ID";

    /**
     * Default Constructor.
     *
     * @param util merge utility
     */
    public MergeTopicParser(final MergeUtils util) {
        this.util = util;
    }

    public final void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
    }

    public final void setJob(final Job job) { this.job = job; }

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
        getContentHandler().endElement(uri, localName, qName);
    }

    /**
     * Get new value for topic id attribute.
     *
     */
    private void handleID(final AttributesImpl atts) {
        String idValue = atts.getValue(ATTRIBUTE_NAME_ID);
        if (idValue != null) {
            XMLUtils.addOrSetAttribute(atts, ATTRIBUTE_NAME_OID, idValue);
            final URI value = setFragment(dirPath.toURI().resolve(toURI(filePath)), idValue);
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
        final URI attValue = href;
        final int sharpIndex = attValue.toString().indexOf(SHARP);
        URI pathFromMap;
        URI retAttValue;
        if (sharpIndex != -1) { // href value refer to an id in a topic
            if (sharpIndex == 0) {
                pathFromMap = toURI(filePath);
            } else {
                pathFromMap = toURI(filePath).resolve(attValue.toString().substring(0, sharpIndex));
            }
            XMLUtils.addOrSetAttribute(atts, ATTRIBUTE_NAME_OHREF, toURI(pathFromMap + attValue.toString().substring(sharpIndex)).toString());
            final String topicID = getTopicID(attValue.getFragment());
            final int index = attValue.toString().indexOf(SLASH, sharpIndex);
            final String elementId = index != -1 ? attValue.toString().substring(index) : "";
            final URI pathWithTopicID = setFragment(dirPath.toURI().resolve(pathFromMap), topicID);
            if (util.findId(pathWithTopicID)) {// topicId found
                retAttValue = toURI(SHARP + util.getIdValue(pathWithTopicID) + elementId);
            } else {// topicId not found
                retAttValue = toURI(SHARP + util.addId(pathWithTopicID) + elementId);
            }
        } else { // href value refer to a topic
            pathFromMap = toURI(filePath).resolve(attValue.toString());
            URI absolutePath = dirPath.toURI().resolve(pathFromMap);
            XMLUtils.addOrSetAttribute(atts, ATTRIBUTE_NAME_OHREF, pathFromMap.toString());
            if (util.findId(absolutePath)) {
                retAttValue = toURI(SHARP + util.getIdValue(absolutePath));
            } else {
                final String fileId = util.getFirstTopicId(absolutePath, false);
                final URI key = setFragment(absolutePath, fileId);
                if (util.findId(key)) {
                    util.addId(absolutePath, util.getIdValue(key));
                    retAttValue = toURI(SHARP + util.getIdValue(key));
                } else {
                    retAttValue = toURI(SHARP + util.addId(absolutePath));
                    util.addId(key, util.getIdValue(absolutePath));
                }

            }
        }
        return retAttValue;
    }

    private String getTopicID(final String fragment) {
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
            final URI f = new File(dir, filePath).getAbsoluteFile().toURI();
            logger.info("Processing " + f);
            job.getStore().transform(f, this);
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw new RuntimeException("Failed to parse " + filename + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void startDocument() throws SAXException {
        firstTopicId = null;
        rootLang = null;
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes)
            throws SAXException {
        final AttributesImpl atts = new AttributesImpl(attributes);
        final String classValue = atts.getValue(ATTRIBUTE_NAME_CLASS);

        if (ELEMENT_NAME_DITA.equals(qName)) {
            rootLang = attributes.getValue(XML_NS_URI, "lang");
            if (atts.getValue(ATTRIBUTE_NAME_ID) == null) {
                XMLUtils.addOrSetAttribute(atts, ATTRIBUTE_NAME_ID, GENERATED_DITA_ELEMENT_ID);
            }
        }
        if (TOPIC_TOPIC.matches(classValue) || ELEMENT_NAME_DITA.equals(qName)) {
            handleID(atts);
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
                // term, keyword, cite, ph, and dt are resolved as keyref can make them links
                        || TOPIC_TERM.matches(classValue) || TOPIC_KEYWORD.matches(classValue)
                        || TOPIC_CITE.matches(classValue) || TOPIC_PH.matches(classValue)
                        || TOPIC_DT.matches(classValue))
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