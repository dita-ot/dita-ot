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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.dita.dost.exception.DITAOTXMLErrorHandler;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;


/**
 * MapLinksReader reads and parse the index information. It is also used to parse
 * map link information in "maplinks.unordered" file.
 * 
 * NOTE: Result of this reader is a map organized as Map<String, Map<String, String> >.
 * Logically it is organized as:
 * 
 * 		topic-file-path --+-- topic-id-1 --+-- index-entry-1-1
 * 		                  |                +-- index-entry-1-2
 *                        |                +-- ...
 *                        |                +-- index-entry-1-n
 *                        |
 *                        +-- topic-id-1 --+-- ...
 *                        +-- ...
 *                        +-- topic-id-m --+-- ...
 * 
 * IF the input URL DOES NOT specify a topic ID explicitly, we use the special character '#' to
 * stand for the first topic ID this parser encounters.
 * 
 * @author Zhang, Yuan Peng
 */
public final class MapLinksReader extends AbstractXMLReader {
    private static final String INTERNET_LINK_MARK = COLON_DOUBLE_SLASH;

    /**
     * Check whether the index entries we got is meaningful and valid
     */
    private static boolean verifyIndexEntries(final StringBuffer str) {
        if (str.length() == 0) {
            return false;
        }
        final int start = str.indexOf(GREATER_THAN); // start from first tag's end
        final int end = str.lastIndexOf(LESS_THAN); // end at last tag's start
        final String temp = str.substring(start + 1, end);
        return temp.trim().length() != 0;
    }
    private final List<String> ancestorList;
    private String filePath = null;
    private String firstMatchElement;
    private StringBuffer indexEntries;
    private File inputFile;
    private final Set<String> lastMatchElement;
    private int level;
    private final Map<File, Map<String,String> > map;
    private boolean match;

    /** Meta shows whether the event is in metadata when using sax to parse ditamap file. */
    private final List<String> matchList;
    private XMLReader reader;
    private String topicPath;
    /** Whether the current href target is internal dita topic file. */
    private boolean validHref;


    /**
     * Default constructor of MapLinksReader class.
     */
    public MapLinksReader() {
        super();
        map = new HashMap<File, Map<String,String> >();
        ancestorList = new ArrayList<String>(16);
        matchList = new ArrayList<String>(16);
        indexEntries = new StringBuffer(1024);
        firstMatchElement = null;
        lastMatchElement = new HashSet<String>();
        level = 0;
        match = false;
        validHref = true;
        topicPath = null;
        inputFile = null;

        try {
            reader = StringUtils.getXMLReader();
            reader.setContentHandler(this);
            reader.setProperty(LEXICAL_HANDLER_PROPERTY,this);
            reader.setFeature("http://xml.org/sax/features/namespaces", false);
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
     */
    private boolean checkMatch() {
        final int matchSize = matchList.size();
        final int ancestorSize = ancestorList.size();
        final ListIterator<String> matchIterator = matchList.listIterator();
        final ListIterator<String> ancestorIterator = ancestorList.listIterator(ancestorSize
                - matchSize);
        String currentMatchString;
        String ancestor;
        while (matchIterator.hasNext()) {
            currentMatchString = matchIterator.next();
            ancestor = ancestorIterator.next();
            if (!currentMatchString.contains(ancestor)) {
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

        if (lastMatchElement.contains(qName) && level == 0) {
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
            final File t = new File(FileUtils.stripFragment(topicPath));
            final String frag = FileUtils.getFragment(topicPath, SHARP);
            Map<String, String> m = map.get(t);
            if (m != null) {
                final String orig = m.get(frag);
                m.put(frag, StringUtils.setOrAppend(orig, indexEntries.toString(), false));
            } else {
                m = new HashMap<String, String>(16);
                m.put(frag, indexEntries.toString());
                map.put(t, m);
            }
            indexEntries = new StringBuffer(1024);
        }
    }

    /**
     * Get links for topics
     * 
     * @return map of links by topic path
     */
    public Map<File, Map<String, String>> getMapping() {
    	return Collections.unmodifiableMap(map);
    }
    
    @Override
    public void ignorableWhitespace(final char[] ch, final int start, final int length)
            throws SAXException {

        if (match && validHref) {
            indexEntries.append(ch, start, length);
        }
    }

    @Override
    public void read(final File filename) {
        if (matchList.isEmpty()) {
            throw new IllegalStateException("matchList not initialized");
        }

        match = false;
        inputFile = filename;
        filePath = inputFile.getParent();
        inputFile.getPath();
        if(indexEntries.length() != 0){
            //delete all the content in indexEntries
            indexEntries = new StringBuffer(1024);
        }

        try {
            reader.setErrorHandler(new DITAOTXMLErrorHandler(filename.getPath(), logger));
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
    public void setMatch(final String matchPattern) {
        int index = 0;
        firstMatchElement = (matchPattern.contains(SLASH)) ? matchPattern.substring(0, matchPattern.indexOf(SLASH)) : matchPattern;

        while (index != -1) {
            final int start = matchPattern.indexOf(SLASH, index);
            final int end = matchPattern.indexOf(SLASH,start + 1);
            if(start != -1 && end != -1){
                lastMatchElement.add(matchPattern.substring(start+1, end));
                index = end;
            } else if(start != -1 && end == -1){
                lastMatchElement.add(matchPattern.substring(start + 1));
                index = -1;
            }
        }
        matchList.add(firstMatchElement);
        final Iterator<String> it = lastMatchElement.iterator();
        final StringBuilder sb = new StringBuilder();
        while(it.hasNext()){
            sb.append(it.next()).append(STRING_BLANK);
        }
        matchList.add(sb.toString());
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
                final File t = new File(FileUtils.stripFragment(topicPath));
                final String frag = FileUtils.getFragment(topicPath, SHARP);
                Map<String, String> m = map.get(t);
                if (m != null) {
                    final String orig = m.get(frag);
                    m.put(frag, StringUtils.setOrAppend(orig, indexEntries.toString(), false));
                } else {
                    m = new HashMap<String, String>(16);
                    m.put(frag, indexEntries.toString());
                    map.put(t, m);
                }
                indexEntries = new StringBuffer(1024);
            }
            topicPath = null;
            if (hrefValue != null && !hrefValue.contains(INTERNET_LINK_MARK)
                    && (attrScope == null || ATTR_SCOPE_VALUE_LOCAL.equals(attrScope))
                    && (attrFormat == null || ATTR_FORMAT_VALUE_DITA.equals(attrFormat))) {
                // If the href is internal dita topic file
                topicPath = FileUtils.resolveTopic(new File(filePath), hrefValue);
                validHref = true;
            }else{
                //set up the boolean to prevent the invalid href's metadata inserted into indexEntries.
                topicPath = null;
                validHref = false;
            }
        }
        if (!match) {
            ancestorList.add(qName);
            if (lastMatchElement.contains(qName) && checkMatch()) {

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

    @Override
    public void processingInstruction(final String target, final String data)
            throws SAXException {
        if (match && validHref) {
            indexEntries.append(LESS_THAN)
                         .append(QUESTION)
                         .append(target);
            if (data != null) {
                indexEntries.append(STRING_BLANK)
                            .append(StringUtils.escapeXML(data));
            }
            indexEntries.append(QUESTION)
                         .append(GREATER_THAN);
        }

    }
}
