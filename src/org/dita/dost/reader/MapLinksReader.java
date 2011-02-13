/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.reader;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.dita.dost.exception.DITAOTXMLErrorHandler;
import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.module.Content;
import org.dita.dost.module.ContentImpl;
import org.dita.dost.resolver.DitaURIResolverFactory;
import org.dita.dost.resolver.URIResolverAdapter;
import org.dita.dost.util.Constants;
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
 * NOTE: Result of this reader is a map organized as HashMap<String, HashMap<String, String> >.
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
public class MapLinksReader extends AbstractXMLReader {
    private static final String INTERNET_LINK_MARK = "://";

    // check whether the index entries we got is meaningfull and valid
    private static boolean verifyIndexEntries(StringBuffer str) {
    	int start;
    	int end;
    	String temp;
        if (str.length() == 0) {
            return false;
        }
        start = str.indexOf(Constants.GREATER_THAN); // start from first tag's end
        end = str.lastIndexOf(Constants.LESS_THAN); // end at last tag's start

        /*
         * original code check whether there is text between different tags
         * modified code check whether there is any content between first and
         * last tags
         */
                
        temp = str.substring(start + 1, end);
        if (temp.trim().length() != 0) {
            return true;
        }
        return false;
    }
    private List<String> ancestorList;
    private String filePath = null;
    private String firstMatchElement;
    private StringBuffer indexEntries;
    private File inputFile;
    private HashSet<String> lastMatchElement;
    private int level;
    private DITAOTJavaLogger logger;
    private HashMap<String, HashMap<String,String> > map;
    private boolean match;

    /*
     * meta shows whether the event is in metadata when using sax to parse
     * ditmap file.
     */
    private List<String> matchList;
    private boolean needResolveEntity;
    private XMLReader reader;
    private String topicPath;
    private boolean validHref;// whether the current href target is internal dita topic file
    

    /**
     * Default constructor of MapLinksReader class.
     */
    public MapLinksReader() {
        super();
        map = new HashMap<String, HashMap<String,String> >();
        ancestorList = new ArrayList<String>(Constants.INT_16);
        matchList = new ArrayList<String>(Constants.INT_16);
        indexEntries = new StringBuffer(Constants.INT_1024);
        firstMatchElement = null;
        lastMatchElement = new HashSet<String>();
        level = 0;
        match = false;
        validHref = true;
        needResolveEntity = false;
        topicPath = null;
        inputFile = null; 
        logger = new DITAOTJavaLogger();
        
        
        try {
            reader = StringUtils.getXMLReader();
            reader.setContentHandler(this);
            reader.setProperty(Constants.LEXICAL_HANDLER_PROPERTY,this);
            //Added by william on 2009-11-8 for ampbug:2893664 start
			reader.setFeature("http://apache.org/xml/features/scanner/notify-char-refs", true);
			reader.setFeature("http://apache.org/xml/features/scanner/notify-builtin-refs", true);
			//Added by william on 2009-11-8 for ampbug:2893664 end
        } catch (Exception e) {
        	logger.logException(e);
        }

    }

    /**
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     * 
     */
    public void characters(char[] ch, int start, int length)
            throws SAXException {

        if (match && needResolveEntity && validHref) {
            String temp = new String(ch, start, length);
            indexEntries.append(StringUtils.escapeXML(temp));
            
        }
    }

    // check whether the hierarchy of current node match the matchList
    private boolean checkMatch() {
        int matchSize = matchList.size();
        int ancestorSize = ancestorList.size();
        ListIterator<String> matchIterator = matchList.listIterator();
        ListIterator<String> ancestorIterator = ancestorList.listIterator(ancestorSize
                - matchSize);
        String currentMatchString;
        String ancestor;
        while (matchIterator.hasNext()) {
            currentMatchString = (String) matchIterator.next();
            ancestor = (String) ancestorIterator.next();
            if (!currentMatchString.contains(ancestor)) {
                return false;
            }
        }
        return true;
    }

	/**
     * @see org.xml.sax.ext.LexicalHandler#endCDATA()
     * 
     */
    public void endCDATA() throws SAXException {
    	if (match && validHref){
    		indexEntries.append(Constants.CDATA_END);
    	}
	    
	}

    /**
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     * 
     */
    public void endElement(String uri, String localName, String qName)
            throws SAXException {

        if (match) {
        	if (validHref){
        		indexEntries.append(Constants.LESS_THAN);
        		indexEntries.append(Constants.SLASH);
        		indexEntries.append(qName);
        		indexEntries.append(Constants.GREATER_THAN);
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
        	/*
                String origin = (String) map.get(topicPath);
                if (origin != null) {
                    map.put(topicPath, origin + indexEntries.toString());
                } else {
                    map.put(topicPath, indexEntries.toString());
                }
                indexEntries = new StringBuffer(Constants.INT_1024);
                */
        	String t = topicPath;
        	String frag = Constants.SHARP;
        	//Get topic id
        	if (t.contains(Constants.SHARP)) {
        		frag = t.indexOf(Constants.SHARP) + 1 >= t.length() ?
        				Constants.SHARP : t.substring(t.indexOf(Constants.SHARP) + 1);
        		//remove the "#" in topic file path
        		t = t.substring(0, t.indexOf(Constants.SHARP));
        	}
        	HashMap<String, String> m = (HashMap<String, String>)map.get(t);
        	if (m != null) {
        		String orig = m.get(frag);
        		m.put(frag, StringUtils.setOrAppend(orig, indexEntries.toString(), false));
        	} else {
        		m = new HashMap<String, String>(Constants.INT_16);
        		m.put(frag, indexEntries.toString());
        		map.put(t, m);
        	}
        	//TODO Added by William on 2009-06-16 for bug:2791696 reltable bug start
        	indexEntries = new StringBuffer(Constants.INT_1024);
        	//TODO Added by William on 2009-06-16 for bug:2791696 reltable bug end
            
        }
    }

	/**
     * @see org.xml.sax.ext.LexicalHandler#endEntity(java.lang.String)
     * 
     */
    public void endEntity(String name) throws SAXException {
		if(!needResolveEntity){
			needResolveEntity = true;
		}
	}

    /**
     * @see org.dita.dost.reader.AbstractReader#getContent()
     * 
     */
    public Content getContent() {

    	ContentImpl result = new ContentImpl();
        result.setCollection( map.entrySet());
        return result;
    }

    /**
     * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
     * 
     */
    public void ignorableWhitespace(char[] ch, int start, int length)
            throws SAXException {

        if (match && validHref) {
            String temp = new String(ch, start, length);
            indexEntries.append(temp);
           
        }
    }


    /**
     * @see org.dita.dost.reader.AbstractReader#read(java.lang.String)
     * 
     */
    public void read(String filename) {

        if (matchList.isEmpty()) {
        	logger.logError(MessageUtils.getMessage("DOTJ008E").toString());
        } else {
            match = false;
            needResolveEntity = true;
            inputFile = new File(filename);
            filePath = inputFile.getParent();
            inputFile.getPath();
            if(indexEntries.length() != 0){
				//delete all the content in indexEntries
				indexEntries = new StringBuffer(Constants.INT_1024);
            }
                         
            try {
            	reader.setErrorHandler(new DITAOTXMLErrorHandler(filename));
            	InputSource source=URIResolverAdapter.convertToInputSource(DitaURIResolverFactory.getURIResolver().resolve(filename, null));
                reader.parse(source);
            } catch (Exception e) {
            	logger.logException(e);
            }
        }
    }

    /**
     * Set the match pattern in the reader. The match pattern is used to see whether
     * current element can be include in the result of parsing.
     * 
     * @param matchPattern the match pattern
     */
    public void setMatch(String matchPattern) {
        int index = 0;
        firstMatchElement = (matchPattern.indexOf(Constants.SLASH) != -1) ? matchPattern.substring(0, matchPattern.indexOf(Constants.SLASH)) : matchPattern;
        
        while (index != -1) {
            int start = matchPattern.indexOf(Constants.SLASH, index);
            int end = matchPattern.indexOf(Constants.SLASH,start + 1);
            if(start != -1 && end != -1){
            	lastMatchElement.add(matchPattern.substring(start+1, end));
            	index = end;
            } else if(start != -1 && end == -1){
            	lastMatchElement.add(matchPattern.substring(start + 1));
            	index = -1;
            }
        }
        matchList.add(firstMatchElement);
        Iterator<String> it = lastMatchElement.iterator();
        StringBuffer sb = new StringBuffer();
        while(it.hasNext()){
        	sb.append(it.next() + Constants.STRING_BLANK);
        }
        matchList.add(sb.toString());
    }

	/**
     * @see org.xml.sax.ext.LexicalHandler#startCDATA()
     * 
     */
    public void startCDATA() throws SAXException {
    	if (match && validHref){
    		indexEntries.append(Constants.CDATA_HEAD);
    	}
	    
	}

	/**
     * @see org.xml.sax.ext.LexicalHandler#startDTD(java.lang.String, java.lang.String, java.lang.String)
     * 
     */
    public void startDTD(String name, String publicId, String systemId)
			throws SAXException {
	}

    /**
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     * 
     */
    public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException {
    	int attsLen = atts.getLength();
    	String attrScope = atts.getValue(Constants.ATTRIBUTE_NAME_SCOPE);
    	String attrFormat = atts.getValue(Constants.ATTRIBUTE_NAME_FORMAT);
    	
        if (qName.equals(firstMatchElement)) {
            String hrefValue = atts.getValue(Constants.ATTRIBUTE_NAME_HREF);
            if (verifyIndexEntries(indexEntries) && topicPath != null) {
            	/*
                String origin = (String) map.get(topicPath);
                map.put(topicPath, StringUtils.setOrAppend(origin, indexEntries.toString(), false));
                */
            	String t = topicPath;
            	String frag = Constants.SHARP;
            	if (t.contains(Constants.SHARP)) {
            		frag = t.indexOf(Constants.SHARP) + 1 >= t.length() ?
            				Constants.SHARP : t.substring(t.indexOf(Constants.SHARP) + 1);
            		t = t.substring(0, t.indexOf(Constants.SHARP));
            	}
            	HashMap<String, String> m = (HashMap<String, String>)map.get(t);
            	if (m != null) {
            		String orig = m.get(frag);
            		m.put(frag, StringUtils.setOrAppend(orig, indexEntries.toString(), false));
            	} else {
            		m = new HashMap<String, String>(Constants.INT_16);
            		m.put(frag, indexEntries.toString());
            		map.put(t, m);
            	}
                indexEntries = new StringBuffer(Constants.INT_1024);
            }
            topicPath = null;
            if (hrefValue != null && hrefValue.indexOf(INTERNET_LINK_MARK) == -1
            		&& (attrScope == null || Constants.ATTR_SCOPE_VALUE_LOCAL.equalsIgnoreCase(attrScope))
            		&& (attrFormat == null || Constants.ATTR_FORMAT_VALUE_DITA.equalsIgnoreCase(attrFormat))) {
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
            if (lastMatchElement.contains(qName) && checkMatch()) {
                
                    match = true;
                    level = 0;
            }
        }

        if (match) {
        	if (validHref){
	        	indexEntries.append(Constants.LESS_THAN + qName + Constants.STRING_BLANK);
	            
	            for (int i = 0; i < attsLen; i++) {
	            	indexEntries.append(atts.getQName(i));
	            	indexEntries.append(Constants.EQUAL);
					indexEntries.append(Constants.QUOTATION);
	            	indexEntries.append(StringUtils.escapeXML(atts.getValue(i)));
	            	indexEntries.append(Constants.QUOTATION);
	            	indexEntries.append(Constants.STRING_BLANK);
	            	
	            }
	            
	            indexEntries.append(Constants.GREATER_THAN);
        	}
            level++;
        }
    }

	/**
     * @see org.xml.sax.ext.LexicalHandler#startEntity(java.lang.String)
     * 
     */
    public void startEntity(String name) throws SAXException {
		needResolveEntity = StringUtils.checkEntity(name);
		if (match && !needResolveEntity && validHref) {
            indexEntries.append(StringUtils.getEntity(name));
            
        }
	}

	@Override
	public void processingInstruction(String target, String data)
			throws SAXException {		
		
		String pi = (data != null) ? target + Constants.STRING_BLANK + data : target;
		
        if (match && needResolveEntity && validHref) {
            String temp = Constants.LESS_THAN + Constants.QUESTION 
            + StringUtils.escapeXML(pi) + Constants.QUESTION + Constants.GREATER_THAN;
            indexEntries.append(temp);            
        }
        
	}
}
