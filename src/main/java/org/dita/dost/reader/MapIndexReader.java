/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.reader;

import static org.dita.dost.util.Constants.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dita.dost.exception.DITAOTXMLErrorHandler;
import org.dita.dost.resolver.DitaURIResolverFactory;
import org.dita.dost.resolver.URIResolverAdapter;
import org.dita.dost.util.FileUtils;
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

    /**
     * Check whether the index entries we got is meaningfull and valid.
     */
    private static boolean verifyIndexEntries(final StringBuffer str) {
        if (str.length() == 0) {
            return false;
        }
        final int start = str.indexOf(GREATER_THAN); // start from first tag's end
        final int end = str.lastIndexOf(LESS_THAN); // end at last tag's start
        final String temp = str.substring(start + 1, end);
        if (temp.trim().length() != 0) {
            return true;
        }
        return false;
    }
    private final List<String> ancestorList;
    private String filePath = null;
    private String firstMatchElement;
    private StringBuffer indexEntries;
    private File inputFile;
    private String lastMatchElement;
    private int level;
    private final Map<String, String> map;
    private boolean match;

    /** Meta shows whether the event is in metadata when using sax to parse ditmap file. */
    private final List<String> matchList;
    private boolean needResolveEntity;
    private XMLReader reader;
    private String topicPath;
    /** Whether the current href target is internal dita topic file. */
    private boolean validHref;


    /**
     * Default constructor of MapIndexReader class.
     */
    public MapIndexReader() {
        super();
        map = new HashMap<String, String>();
        ancestorList = new ArrayList<String>(INT_16);
        matchList = new ArrayList<String>(INT_16);
        indexEntries = new StringBuffer(INT_1024);
        firstMatchElement = null;
        lastMatchElement = null;
        level = 0;
        match = false;
        validHref = true;
        needResolveEntity = false;
        topicPath = null;
        inputFile = null;

        try {
            reader = StringUtils.getXMLReader();
            reader.setContentHandler(this);
            reader.setProperty(LEXICAL_HANDLER_PROPERTY,this);
            reader.setFeature("http://apache.org/xml/features/scanner/notify-char-refs", true);
            reader.setFeature("http://apache.org/xml/features/scanner/notify-builtin-refs", true);
        } catch (final Exception e) {
            logger.logError(e.getMessage(), e) ;
        }

    }

    @Override
    public void characters(final char[] ch, final int start, final int length)
            throws SAXException {

        if (match && needResolveEntity && validHref) {
            final String temp = new String(ch, start, length);
            indexEntries.append(StringUtils.escapeXML(temp));

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
        final List<String> tail = ancestorList.subList(ancestorSize - matchSize, ancestorSize);
        return matchList.equals(tail);
    }

    @Override
    public void endCDATA() throws SAXException {
        if (match && validHref){
            indexEntries.append(CDATA_END);
        }

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

        if (qName.equals(lastMatchElement) && level == 0) {
            if (match) {
                match = false;
            }
        }
        if (!match) {
            ancestorList.remove(ancestorList.size() - 1);
        }

        if (qName.equals(firstMatchElement) && verifyIndexEntries(indexEntries) && topicPath != null) {
            // if the href is not valid, topicPath will be null. We don't need to set the condition
            // to check validHref at here.
            final String origin = map.get(topicPath);
            if (origin != null) {
                map.put(topicPath, origin + indexEntries.toString());
            } else {
                map.put(topicPath, indexEntries.toString());
            }
            indexEntries = new StringBuffer(INT_1024);

        }
    }

    @Override
    public void endEntity(final String name) throws SAXException {
        if(!needResolveEntity){
            needResolveEntity = true;
        }
    }

    /**
     * Get index entries for topics
     * 
     * @return map of index entries by topic path
     */
    public Map<String, String> getMapping() {
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
    public void read(final String filename) {
        if (matchList.isEmpty()) {
            throw new IllegalStateException("matchList not initialized");
        }
        
        match = false;
        needResolveEntity = true;
        inputFile = new File(filename);
        filePath = inputFile.getParent();
        if(indexEntries.length() != 0){
            //delete all the content in indexEntries
            indexEntries = new StringBuffer(INT_1024);
        }

        try {
            reader.setErrorHandler(new DITAOTXMLErrorHandler(filename, logger));
            final InputSource source=URIResolverAdapter.convertToInputSource(DitaURIResolverFactory.getURIResolver().resolve(filename, null));
            reader.parse(source);
        } catch (final Exception e) {
            logger.logError(e.getMessage(), e) ;
        }
    }

    /**
     * Set the match pattern in the reader. The match pattern is used to see whether
     * current element can be include in the result of parsing.
     * 
     * @param matchPattern the match pattern
     */
    public void setMatch(final String matchPattern) {
        int index = 0;
        firstMatchElement = (matchPattern.indexOf(SLASH) != -1) ? matchPattern.substring(0, matchPattern.indexOf(SLASH)) : matchPattern;

        while (index != -1) {
            final int end = matchPattern.indexOf(SLASH, index);
            if (end == -1) {
                matchList.add(matchPattern.substring(index));
                lastMatchElement = matchPattern.substring(index);
                index = end;
            } else {
                matchList.add(matchPattern.substring(index, end));
                index = end + 1;
            }
        }

    }

    @Override
    public void startCDATA() throws SAXException {
        if (match && validHref){
            indexEntries.append(CDATA_HEAD);
        }

    }

    @Override
    public void startDTD(final String name, final String publicId, final String systemId)
            throws SAXException {
        // NOOP
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName,
            final Attributes atts) throws SAXException {
        final int attsLen = atts.getLength();
        final String attrScope = atts.getValue(ATTRIBUTE_NAME_SCOPE);
        final String attrFormat = atts.getValue(ATTRIBUTE_NAME_FORMAT);

        if (qName.equals(firstMatchElement)) {
            final String hrefValue = atts.getValue(ATTRIBUTE_NAME_HREF);
            if (verifyIndexEntries(indexEntries) && topicPath != null) {
                final String origin = map.get(topicPath);
                map.put(topicPath, StringUtils.setOrAppend(origin, indexEntries.toString(), false));
                indexEntries = new StringBuffer(INT_1024);
            }
            topicPath = null;
            if (hrefValue != null && hrefValue.indexOf(INTERNET_LINK_MARK) == -1
                    && (attrScope == null || ATTR_SCOPE_VALUE_LOCAL.equalsIgnoreCase(attrScope))
                    && (attrFormat == null || ATTR_FORMAT_VALUE_DITA.equalsIgnoreCase(attrFormat))) {
                // If the href is internal dita topic file
                topicPath = FileUtils.resolveTopic(filePath, hrefValue);
                validHref = true;
            }else{
                //set up the boolean to prevent the invalid href's metadata inserted into indexEntries.
                topicPath = null;
                validHref = false;
            }
        }
        if (!match) {
            ancestorList.add(qName);
            if (qName.equals(lastMatchElement) && checkMatch()) {

                match = true;
                level = 0;
            }
        }

        if (match) {
            if (validHref){
                indexEntries.append(LESS_THAN + qName + STRING_BLANK);

                for (int i = 0; i < attsLen; i++) {
                    indexEntries.append(atts.getQName(i));
                    indexEntries.append(EQUAL);
                    indexEntries.append(QUOTATION);
                    indexEntries.append(StringUtils.escapeXML(atts.getValue(i)));
                    indexEntries.append(QUOTATION);
                    indexEntries.append(STRING_BLANK);

                }

                indexEntries.append(GREATER_THAN);
            }
            level++;
        }
    }

    @Override
    public void startEntity(final String name) throws SAXException {
        needResolveEntity = StringUtils.checkEntity(name);
        if (match && !needResolveEntity && validHref) {
            indexEntries.append(StringUtils.getEntity(name));

        }
    }
}
