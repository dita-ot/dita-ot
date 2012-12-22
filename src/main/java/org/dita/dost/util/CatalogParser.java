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
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;


/**
 * @author Zhang, Yuan Peng
 * @deprecated use Apache Commons Catalog Resolver instead
 */
@Deprecated
public final class CatalogParser implements ContentHandler{
    private final String catalogDir;


    private String dtdBase;
    private String schemaBase;
    private final Map<String, String> map;

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
    public CatalogParser(final Map<String, String> catalogMap, final String ditaDir) {
        map = catalogMap;
        catalogDir = ditaDir;
        dtdBase = null;
    }

    public void characters(final char[] ch, final int start, final int length)
            throws SAXException {
    }

    public void endDocument() throws SAXException {
    }

    public void endElement(final String uri, final String localName, final String qName)
            throws SAXException {
        if ("group".equals(qName)){
            dtdBase = null;
        }
    }

    public void endPrefixMapping(final String prefix) throws SAXException {
    }

    public void ignorableWhitespace(final char[] ch, final int start, final int length)
            throws SAXException {
    }

    public void processingInstruction(final String target, final String data)
            throws SAXException {
    }

    public void setDocumentLocator(final Locator locator) {
    }

    public void skippedEntity(final String name) throws SAXException {
    }

    public void startDocument() throws SAXException {
    }

    public void startElement(final String uri, final String localName, final String qName,
            final Attributes atts) throws SAXException {
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

            final String xmlBase = atts.getValue("xml:base");
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

            final String xmlBase = atts.getValue("xml:base");
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

    public void startPrefixMapping(final String prefix, final String uri)
            throws SAXException {
    }

}