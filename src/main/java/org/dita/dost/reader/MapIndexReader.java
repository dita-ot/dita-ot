/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.reader;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.*;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dita.dost.exception.DITAOTXMLErrorHandler;
import org.dita.dost.util.DitaClass;
import org.dita.dost.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;


/**
 * MapIndexReader reads and parse the index information. It is also used to parse
 * map link information in "maplinks.unordered" file.
 * 
 * @author Zhang, Yuan Peng
 */
public final class MapIndexReader extends AbstractXMLReader {

    private static final String INTERNET_LINK_MARK = COLON_DOUBLE_SLASH;

    private final List<DitaClass> ancestorList;
    private URI filePath = null;
    private DitaClass firstMatchElement;
    private StringBuffer indexEntries;
    private DitaClass lastMatchElement;
    private int level;
    private final Map<URI, String> map;
    private boolean match;

    /** Meta shows whether the event is in metadata when using sax to parse ditmap file. */
    private List<DitaClass> matchList;
    private XMLReader reader;
    private URI topicPath;
    /** Whether the current href target is internal dita topic file. */
    private boolean validHref;


    /**
     * Default constructor of MapIndexReader class.
     */
    public MapIndexReader() {
        super();
        map = new HashMap<URI, String>();
        ancestorList = new ArrayList<DitaClass>(16);
        indexEntries = new StringBuffer(1024);
        level = 0;
        match = false;
        validHref = true;
        topicPath = null;

        try {
            reader = StringUtils.getXMLReader();
            reader.setContentHandler(this);
            reader.setProperty(LEXICAL_HANDLER_PROPERTY,this);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e) ;
        }

    }

    @Override
    public void characters(final char[] ch, final int start, final int length)
            throws SAXException {

        if (match && validHref) {
            indexEntries.append(StringUtils.escapeXML(new String(ch, start, length)));
        }
    }

    /**
     * Check whether the hierarchy of current node match the matchList.
     * 
     * @return {@code true} is match, otherwise {@code false}
     */
    private boolean checkMatch() {
        final int matchSize = matchList.size();
        final int ancestorSize = ancestorList.size();
        if (ancestorSize < matchSize) {
            return false;
        }
        final List<DitaClass> tail = ancestorList.subList(ancestorSize - matchSize, ancestorSize);
        for (int i = 0; i < matchSize; i++) {
            if (!matchList.get(i).matches(tail.get(i))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName)
            throws SAXException {

        if (match) {
            if (validHref){
                indexEntries.append(LESS_THAN);
                indexEntries.append(SLASH);
                indexEntries.append(qName);
                indexEntries.append(GREATER_THAN);
            }

            level--;
        }

        if (qName.equals(lastMatchElement.localName) && level == 0) {
            if (match) {
                match = false;
            }
        }
        if (!match) {
            ancestorList.remove(ancestorList.size() - 1);
        }

        if (qName.equals(firstMatchElement.localName) && verifyIndexEntries(indexEntries) && topicPath != null) {
            // if the href is not valid, topicPath will be null. We don't need to set the condition
            // to check validHref at here.
            final String origin = map.get(topicPath);
            if (origin != null) {
                map.put(topicPath, origin + indexEntries.toString());
            } else {
                map.put(topicPath, indexEntries.toString());
            }
            indexEntries = new StringBuffer(1024);

        }
    }

    /**
     * Get index entries for topics
     * 
     * @return map of index entries by topic path
     */
    public Map<URI, String> getMapping() {
    	return Collections.unmodifiableMap(map);
    }
    
    @Override
    public void ignorableWhitespace(final char[] ch, final int start, final int length)
            throws SAXException {

        if (match && validHref) {
            final String temp = new String(ch, start, length);
            indexEntries.append(temp);

        }
    }

    @Override
    public void read(final File filename) {
        if (matchList.isEmpty()) {
            throw new IllegalStateException("matchList not initialized");
        }
        
        match = false;
        filePath = toURI(filename.getParentFile());
        if(indexEntries.length() != 0){
            //delete all the content in indexEntries
            indexEntries = new StringBuffer(1024);
        }

        reader.setErrorHandler(new DITAOTXMLErrorHandler(filename.getPath(), logger));
        try {
            reader.parse(new InputSource(filename.toURI().toString()));
        } catch (final Exception e) {
            logger.error(e.getMessage(), e) ;
        }
    }

    /**
     * Set the match pattern in the reader. The match pattern is used to see whether
     * current element can be include in the result of parsing.
     * 
     * @param matchPattern the match pattern
     */    
    public void setMatch(final List<DitaClass> matchPattern) {
        matchList = Collections.unmodifiableList(new ArrayList<DitaClass>(matchPattern));
        firstMatchElement = matchPattern.get(0);
        lastMatchElement = matchPattern.get(matchPattern.size() - 1);
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName,
            final Attributes atts) throws SAXException {
        final int attsLen = atts.getLength();
        final String attrScope = atts.getValue(ATTRIBUTE_NAME_SCOPE);
        final String attrFormat = atts.getValue(ATTRIBUTE_NAME_FORMAT);

        if (qName.equals(firstMatchElement.localName)) {
            final URI hrefValue = toURI(atts.getValue(ATTRIBUTE_NAME_HREF));
            if (verifyIndexEntries(indexEntries) && topicPath != null) {
                final String origin = map.get(topicPath);
                map.put(topicPath, StringUtils.setOrAppend(origin, indexEntries.toString(), false));
                indexEntries = new StringBuffer(1024);
            }
            topicPath = null;
            if (hrefValue != null && !hrefValue.toString().contains(INTERNET_LINK_MARK)
                    && (attrScope == null || ATTR_SCOPE_VALUE_LOCAL.equals(attrScope))
                    && (attrFormat == null || ATTR_FORMAT_VALUE_DITA.equals(attrFormat))) {
                // If the href is internal dita topic file
                topicPath = filePath.resolve(hrefValue);
                validHref = true;
            }else{
                //set up the boolean to prevent the invalid href's metadata inserted into indexEntries.
                topicPath = null;
                validHref = false;
            }
        }
        if (!match) {
            ancestorList.add(DitaClass.getInstance(atts));
            if (qName.equals(lastMatchElement.localName) && checkMatch()) {

                match = true;
                level = 0;
            }
        }

        if (match) {
            if (validHref){
                indexEntries.append(LESS_THAN).append(qName);

                for (int i = 0; i < attsLen; i++) {
                    indexEntries.append(STRING_BLANK);
                    indexEntries.append(atts.getQName(i));
                    indexEntries.append(EQUAL);
                    indexEntries.append(QUOTATION);
                    indexEntries.append(StringUtils.escapeXML(atts.getValue(i)));
                    indexEntries.append(QUOTATION);
                }

                indexEntries.append(GREATER_THAN);
            }
            level++;
        }
    }
    
    /**
     * Check whether the index entries we got is meaningfull and valid.
     */
    private boolean verifyIndexEntries(final StringBuffer str) {
        if (str.length() == 0) {
            return false;
        }
        final int start = str.indexOf(GREATER_THAN); // start from first tag's end
        final int end = str.lastIndexOf(LESS_THAN); // end at last tag's start
        final String temp = str.substring(start + 1, end);
        return temp.trim().length() != 0;
    }

}
