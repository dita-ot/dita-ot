/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.reader;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.ListIterator;

import org.dita.dost.module.Content;
import org.dita.dost.module.ContentImpl;
import org.dita.dost.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.XMLReaderFactory;


/**
 * @author Zhang, Yuan Peng
 */
public class MapIndexReader extends AbstractReader implements ContentHandler,LexicalHandler {

    /*
     * meta shows whether the event is in metadata when using sax to parse
     * ditmap file.
     */
    private ArrayList matchList;
    private ArrayList ancestorList;
    private String last;
    private String first;
    private int level;
    private boolean match;
    private boolean needResolveEntity;
    private String topicFileName;
    private XMLReader reader;
    private String indexEntries;
    private HashMap map;
    private File inputFile;
    private String filePath = null;
    private String filePathName = null;

    /**
     * 
     */
    public MapIndexReader() {
        super();
        map = new HashMap();
        ancestorList = new ArrayList();
        matchList = new ArrayList();
        
        try {
            //SAXParserFactory spFactory = SAXParserFactory.newInstance();
            //spFactory.setFeature("http://xml.org/sax/features/validation", true);
            //spFactory.setValidating(true);
            //SAXParser parser = spFactory.newSAXParser();
            //reader = parser.getXMLReader();
            if (System.getProperty("org.xml.sax.driver") == null){
                //The default sax driver is set to xerces's sax driver
                System.setProperty("org.xml.sax.driver","org.apache.xerces.parsers.SAXParser");
            }
            reader = XMLReaderFactory.createXMLReader();
            reader.setContentHandler(this);
            //reader.setFeature("http://xml.org/sax/features/lexical-handler",true);
            //reader.setFeature("http://xml.org/sax/features/external-general-entities",true);
            //reader.setFeature("http://xml.org/sax/features/external-parameter-entities",true);
            //reader.setFeature("http://xml.org/sax/features/lexical-handler/parameter-entities",true);
            reader.setProperty("http://xml.org/sax/properties/lexical-handler",this);
            
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }

    }

    public void setMatch(String match) {
        int index = 0;
        if (match.indexOf('/') != -1) {
            first = match.substring(0, match.indexOf('/'));
        } else {
            first = match;
        }

        while (index != -1) {
            int end = match.indexOf('/', index);
            if (end == -1) {
                matchList.add(match.substring(index));
                last = match.substring(index);
                index = end;
            } else {
                matchList.add(match.substring(index, end));
                index = end + 1;
            }
        }

    }

    // check whether the hierarchy of current node match the matchList
    private boolean checkMatch() {
        int matchSize = matchList.size();
        int ancestorSize = ancestorList.size();
        ListIterator matchIterator = matchList.listIterator();
        ListIterator ancestorIterator = ancestorList.listIterator(ancestorSize
                - matchSize);
        String match;
        String ancestor;
        while (matchIterator.hasNext()) {
            match = (String) matchIterator.next();
            ancestor = (String) ancestorIterator.next();
            if (!match.equals(ancestor)) {
                return false;
            }
        }
        return true;
    }

    // check whether the index entries we got is meaningfull and valid
    private boolean check(String str) {
        if (str == null) {
            return false;
        }
        int start = str.indexOf('>'); // start from first tag's end
        int end = str.lastIndexOf('<'); // end at last tag's start

        // original code check whether there is text between different tags
        // modified code check whether there is any content between first and
        // last tags

        /*
         * if(start == -1){ return false; } int end = str.indexOf(' <',start);
         * for(;start != -1 && end != -1;){ String temp = str.substring(start+1,
         * end); if(temp.length()!=0 && temp.trim().length()!=0){ return true; }
         * start = str.indexOf('>',end); end = str.indexOf(' <', start); }
         * return false;
         */
        String temp = str.substring(start + 1, end);
        if (temp.trim().length() != 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 
     */
    public void read(String filename) {

        if (!matchList.isEmpty()) {
            match = false;
            needResolveEntity = true;
            inputFile = new File(filename);
            filePath = inputFile.getParent();
            filePathName = inputFile.getPath();
            indexEntries = null;
            try {
                reader.parse(filename);
            } catch (Exception e) {
                e.printStackTrace(System.out);
            }
        } else {
            System.out.println("Set the match list with match string first.");
        }
    }

    public Content getContent() {

        ContentImpl result = new ContentImpl();
        result.setCollection((Collection) map.entrySet());
        return result;
    }

    public void characters(char[] ch, int start, int length)
            throws SAXException {

        if (match && needResolveEntity) {
            String temp = new String(ch, start, length);
            if (indexEntries != null) {
                indexEntries += temp;
            } else {
                indexEntries = temp;
            }
        }
    }

    public void endDocument() throws SAXException {

    }

    public void endElement(String uri, String localName, String qName)
            throws SAXException {

        if (match) {
            indexEntries += "</";
            indexEntries += qName;
            indexEntries += ">";
            level--;
        }

        if (qName.equals(last) && level == 0) {
            if (match) {
                match = false;
            }
        }
        if (!match) {
            ancestorList.remove(ancestorList.size() - 1);
        }

        if (qName.equals(first)) {
            if (check(indexEntries) && topicFileName != null) {
                String origin = (String) map.get(topicFileName);
                if (origin != null) {
                    map.put(topicFileName, origin + indexEntries);
                } else {
                    map.put(topicFileName, indexEntries);
                }
                indexEntries = null;
            }
        }
    }

    public void endPrefixMapping(String prefix) throws SAXException {

    }

    public void ignorableWhitespace(char[] ch, int start, int length)
            throws SAXException {

        if (match) {
            String temp = new String(ch, start, length);
            if (indexEntries != null) {
                indexEntries += temp;
            } else {
                indexEntries = temp;
            }
        }
    }

    public void processingInstruction(String target, String data)
            throws SAXException {

    }

    public void setDocumentLocator(Locator locator) {

    }

    public void skippedEntity(String name) throws SAXException {

    }

    public void startDocument() throws SAXException {

    }

    public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException {
    	
        if (qName.equals(first)) {
            String hrefValue = atts.getValue("href");
            if(hrefValue != null && hrefValue.indexOf("://") == -1){
                topicFileName = StringUtils.resolveDirectory(filePath, atts
                        .getValue("href"));
            }else{
                topicFileName = null;
            }
        }
        if (!match) {
            ancestorList.add(qName);
            if (qName.equals(last)) {
                if (checkMatch()) {
                    match = true;
                    level = 0;
                }
            }
        }

        if (match) {
            if (indexEntries != null) {
                indexEntries += "<";
            } else {
                indexEntries = "<";
            }
            indexEntries += qName;
            indexEntries += " ";

            for (int i = 0; i < atts.getLength(); i++) {
                indexEntries += atts.getQName(i);
                indexEntries += "=\"";
                indexEntries += atts.getValue(i);
                indexEntries += "\" ";
            }
            indexEntries += ">";
            level++;
        }
    }

    public void startPrefixMapping(String prefix, String uri)
            throws SAXException {

    }
    
	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#comment(char[], int, int)
	 */
	public void comment(char[] ch, int start, int length) throws SAXException {
	}
	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#endCDATA()
	 */
	public void endCDATA() throws SAXException {
	    if (indexEntries != null) {
            indexEntries += "]]>";
        } else {
            indexEntries = "]]>";
        }
	}
	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#endDTD()
	 */
	public void endDTD() throws SAXException {
	}
	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#endEntity(java.lang.String)
	 */
	public void endEntity(String name) throws SAXException {
		if(!needResolveEntity){
			needResolveEntity = true;
		}
	}
	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#startCDATA()
	 */
	public void startCDATA() throws SAXException {
	    if (indexEntries != null) {
            indexEntries += "<![CDATA[";
        } else {
            indexEntries = "<![CDATA[";
        }
	}
	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#startDTD(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void startDTD(String name, String publicId, String systemId)
			throws SAXException {
	}
	/* (non-Javadoc)
	 * @see org.xml.sax.ext.LexicalHandler#startEntity(java.lang.String)
	 */
	public void startEntity(String name) throws SAXException {
		needResolveEntity = StringUtils.checkEntity(name);
		//System.out.println("meet entity:"+name);
		if (match && !needResolveEntity) {
            if (indexEntries != null) {
                indexEntries += StringUtils.getEntity(name);
                //System.out.println(StringUtils.getEntity(name));
            } else {
                indexEntries = StringUtils.getEntity(name);
            }
        }
	}
}
