/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.reader;

import java.util.HashMap;

import org.dita.dost.module.Content;
import org.dita.dost.module.ContentImpl;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;


/**
 * @author Zhang, Yuan Peng
 */
public class DitaValReader extends AbstractReader implements ContentHandler {

    private HashMap filterMap;
    private ContentImpl content;
    private XMLReader reader;

    /**
     * 
     */
    public DitaValReader() {
        super();
        filterMap = new HashMap();
        
        try {
            if (System.getProperty("org.xml.sax.driver") == null){
                //The default sax driver is set to xerces's sax driver
                System.setProperty("org.xml.sax.driver","org.apache.xerces.parsers.SAXParser");
            }
            reader = XMLReaderFactory.createXMLReader();
            reader.setContentHandler(this);
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }

    }

    /**
     * 
     */
    public void read(String filename) {
        try {
            reader.parse(filename);
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    public Content getContent() {
        content = new ContentImpl();
        content.setCollection(filterMap.entrySet());
        return content;
    }

    public void characters(char[] ch, int start, int length)
            throws SAXException {

    }

    public void endDocument() throws SAXException {

    }

    public void endElement(String uri, String localName, String qName)
            throws SAXException {

    }

    public void endPrefixMapping(String prefix) throws SAXException {

    }

    public void ignorableWhitespace(char[] ch, int start, int length)
            throws SAXException {

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
        if (qName.equals("prop")) {
            String action = atts.getValue("action");
            String key = atts.getValue("att") + "=" + atts.getValue("val");
            if (action != null) {
                if(filterMap.get(key) == null){
                    filterMap.put(key, action);
                }else{
                    System.out.println("duplicate condition in filter file");
                }
            }
        }
    }

    public void startPrefixMapping(String prefix, String uri)
            throws SAXException {

    }
}
