/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2016 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.reader;

import org.dita.dost.log.MessageUtils;
import org.dita.dost.writer.AbstractXMLFilter;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.net.URI;
import java.util.*;

import static org.dita.dost.reader.ChunkMapReader.CHUNK_TO_CONTENT;
import static org.dita.dost.util.Configuration.ditaFormat;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.*;

/**
 * Collect copy-to information from a map.
 * 
 * <p>
 * <strong>Not thread-safe</strong>. Instances can be reused by calling
 * {@link #reset()} between calls to parse.
 * </p>
 */
public final class CopyToReader extends AbstractXMLFilter {

    /** Map of copy-to target to souce */
    private final Map<URI, URI> copyToMap = new HashMap<>(16);
    /** foreign/unknown nesting level */
    private int foreignLevel = 0;
    /** chunk nesting level */
    private int chunkLevel = 0;
    /** Stack for @processing-role value */
    private final Stack<String> processRoleStack = new Stack<>();

    /**
     * Get the copy-to map.
     * 
     * @return copy-to map
     */
    public Map<URI, URI> getCopyToMap() {
        return copyToMap;
    }

    /**
     * Set current file absolute path
     * 
     * @param currentFile absolute path to current file
     */
    public void setCurrentFile(final URI currentFile) {
        assert currentFile.isAbsolute();
        super.setCurrentFile(currentFile);
    }

    /**
     * 
     * Reset the internal variables.
     */
    public void reset() {
        foreignLevel = 0;
        chunkLevel = 0;
        copyToMap.clear();
        processRoleStack.clear();
    }

    @Override
    public void startDocument() throws SAXException {
        processRoleStack.push(ATTR_PROCESSING_ROLE_VALUE_NORMAL);

        getContentHandler().startDocument();
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes atts)
            throws SAXException {
        String processingRole = atts.getValue(ATTRIBUTE_NAME_PROCESSING_ROLE);
        if (processingRole == null) {
            processingRole = processRoleStack.peek();
        }
        processRoleStack.push(processingRole);

        final String classValue = atts.getValue(ATTRIBUTE_NAME_CLASS);

        if (foreignLevel > 0) {
            foreignLevel++;
        } else if (TOPIC_FOREIGN.matches(classValue) || TOPIC_UNKNOWN.matches(classValue)) {
            foreignLevel++;
        }

        if (chunkLevel > 0) {
            chunkLevel++;
        } else if (atts.getValue(ATTRIBUTE_NAME_CHUNK) != null) {
            chunkLevel++;
        }

        if (MAP_TOPICREF.matches(classValue)) {
            parseAttribute(atts);
        }

        getContentHandler().startElement(uri, localName, qName, atts);
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        processRoleStack.pop();

        if (foreignLevel > 0) {
            foreignLevel--;
        }
        if (chunkLevel > 0) {
            chunkLevel--;
        }

        getContentHandler().endElement(uri, localName, qName);
    }

    /**
     * Clean up.
     */
    @Override
    public void endDocument() throws SAXException {
        processRoleStack.pop();

        getContentHandler().endDocument();
    }
    
    /**
     * Parse the input attributes for needed information.
     * 
     * @param atts all attributes
     */
    private void parseAttribute(final Attributes atts) {
        URI attrValue = toURI(atts.getValue(ATTRIBUTE_NAME_COPY_TO));
        if (attrValue == null) {
            return;
        }
        final String attrScope = atts.getValue(ATTRIBUTE_NAME_SCOPE);

        // external resource is filtered here.
        if (ATTR_SCOPE_VALUE_EXTERNAL.equals(attrScope) || ATTR_SCOPE_VALUE_PEER.equals(attrScope)
                // FIXME: testing for :// here is incorrect, rely on source scope instead
                || attrValue.toString().contains(COLON_DOUBLE_SLASH) || attrValue.toString().startsWith(SHARP)) {
            return;
        }

        final URI target = stripFragment(attrValue.isAbsolute() ? attrValue : currentFile.resolve(attrValue));
        assert target.isAbsolute();

        final String attrFormat = getFormat(atts);

        if (isFormatDita(attrFormat)) {
            final URI source = toURI(atts.getValue(ATTRIBUTE_NAME_HREF));
            if (source != null) {
                if (source.toString().isEmpty()) {
                    logger.warn("Copy-to task [href=\"\" copy-to=\"" + target + "\"] was ignored.");
                } else {
                    final URI value = stripFragment(currentFile.resolve(source));
                    if (copyToMap.get(target) != null) {
                        if (!value.equals(copyToMap.get(target))) {
                            logger.warn(MessageUtils.getInstance().getMessage("DOTX065W", source.toString(), target.toString()).toString());
                        }
                    } else if (!(atts.getValue(ATTRIBUTE_NAME_CHUNK) != null && atts.getValue(ATTRIBUTE_NAME_CHUNK).contains(
                            CHUNK_TO_CONTENT))) {
                        copyToMap.put(target, value);
                    }
                }
            }
        }
    }

    private String getFormat(Attributes atts) {
        final String attrClass = atts.getValue(ATTRIBUTE_NAME_CLASS);
        if (TOPIC_IMAGE.matches(attrClass)) {
            return ATTR_FORMAT_VALUE_IMAGE;
        } else if (TOPIC_OBJECT.matches(attrClass)) {
            throw new IllegalArgumentException();
            //return ATTR_FORMAT_VALUE_HTML;
        } else {
            return atts.getValue(ATTRIBUTE_NAME_FORMAT);
        }
    }

    /**
     * Check if format is DITA topic.
     *
     * @param attrFormat format attribute value, may be {@code null}
     * @return {@code true} if DITA topic, otherwise {@code false}
     */
    public static boolean isFormatDita(final String attrFormat) {
        if (attrFormat == null || attrFormat.equals(ATTR_FORMAT_VALUE_DITA)) {
            return true;
        }
        for (final String f: ditaFormat) {
            if (f.equals(attrFormat)) {
                return true;
            }
        }
        return false;
    }

}
