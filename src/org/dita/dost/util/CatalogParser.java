/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2005, 2006 All Rights Reserved.
 */
package org.dita.dost.util;

import java.io.File;
import java.util.HashMap;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;


/**
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments.
 *
 * @author Zhang, Yuan Peng
 */

public class CatalogParser implements ContentHandler{
    private String catalogDir;


    private String dtdBase;
    private String schemaBase; 
    private HashMap<String, String> map;
    
    /**
     * Automatically generated constructor: CatalogParser.
     */
    public CatalogParser() {
    	this(null, null);
    }
           

    /**
     * Default constructor of CatalogParser class.
     * 
     * @param catalogMap catalogMap
     * @param ditaDir ditaDir
     */
    public CatalogParser(HashMap<String, String> catalogMap, String ditaDir) {
        map = catalogMap;
        catalogDir = ditaDir;
        dtdBase = null;
    }
    
    /** 
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] ch, int start, int length)
            throws SAXException {
    }
    
    /** 
     * @see org.xml.sax.ContentHandler#endDocument()
     */
    public void endDocument() throws SAXException {
    }
    
    /** 
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if ("group".equals(qName)){
            dtdBase = null;
        }
    }
    
    /**
     * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
     */
    public void endPrefixMapping(String prefix) throws SAXException {
    }
    
    /**
     * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
     */
    public void ignorableWhitespace(char[] ch, int start, int length)
            throws SAXException {
    }
    
    /**
     * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String, java.lang.String)
     */
    public void processingInstruction(String target, String data)
            throws SAXException {
    }
    
    /**
     * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
     */
    public void setDocumentLocator(Locator locator) {
    }
    /**
     * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
     */
    public void skippedEntity(String name) throws SAXException {
    }
    /**
     * @see org.xml.sax.ContentHandler#startDocument()
     */
    public void startDocument() throws SAXException {
    }
    /**
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException {
        /*if ("group".equals(qName)){
        	String xmlBase = atts.getValue("xml:base");
        	if(xmlBase!=null){
        		if (xmlBase.indexOf("dtd")!=-1){
        			dtdBase = atts.getValue("xml:base");
        		}else if (xmlBase.indexOf("schema")!=-1){
        			schemaBase = atts.getValue("xml:base");
        		}
        	}
        }*/
        
        if ("public".equals(qName)){
        	
        	String xmlBase = atts.getValue("xml:base");
        	if(xmlBase!=null){
        		if (xmlBase.indexOf("dtd")!=-1){
        			dtdBase = atts.getValue("xml:base");
        		}
        	}
            String localURI;
            String absoluteLocalURI;
            localURI = (dtdBase != null) ? dtdBase+File.separatorChar+atts.getValue("uri") : atts.getValue("uri");
            absoluteLocalURI = (catalogDir != null) ? catalogDir + File.separatorChar + localURI : localURI;
            map.put(atts.getValue("publicId"), absoluteLocalURI);
        }else if("system".equals(qName)){
        	
        	String xmlBase = atts.getValue("xml:base");
        	if(xmlBase!=null){
        		if (xmlBase.indexOf("schema")!=-1){
        			schemaBase = atts.getValue("xml:base");
        		}
        	}
        	
        	String localURI;
            String absoluteLocalURI;
            localURI = (schemaBase != null) ? schemaBase+File.separatorChar+atts.getValue("uri") : atts.getValue("uri");
            absoluteLocalURI = (catalogDir != null) ? catalogDir + File.separatorChar + localURI : localURI;
            map.put(atts.getValue("systemId"), absoluteLocalURI);
        }
    }
    /**
     * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String, java.lang.String)
     */
    public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
    }
}