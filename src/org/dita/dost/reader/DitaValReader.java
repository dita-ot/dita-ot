/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.reader;

import java.util.HashMap;

import org.dita.dost.module.Content;
import org.dita.dost.module.ContentImpl;
import org.dita.dost.util.Constants;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * DitaValReader reads and parses the information from ditaval file which contains 
 * the information of filtering and flagging.
 * 
 * @author Zhang, Yuan Peng
 */
public class DitaValReader extends AbstractXMLReader {

    private HashMap filterMap;
    private ContentImpl content;
    private XMLReader reader;


    /**
     * Default constructor of DitaValReader class.
     */
    public DitaValReader() {
        super();
        filterMap = new HashMap();
        content = null;
        
        try {
            if (System.getProperty(Constants.SAX_DRIVER_PROPERTY) == null){
                //The default sax driver is set to xerces's sax driver
                System.setProperty(Constants.SAX_DRIVER_PROPERTY,Constants.SAX_DRIVER_DEFAULT_CLASS);
            }
            reader = XMLReaderFactory.createXMLReader();
            reader.setContentHandler(this);
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }

    }


    /**
     * @see org.dita.dost.reader.AbstractReader#read(java.lang.String)
     * 
     */
    public void read(String filename) {
        try {
            reader.parse(filename);
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    /**
     * @see org.dita.dost.reader.AbstractReader#getContent()
     */
    public Content getContent() {
        content = new ContentImpl();
        content.setCollection(filterMap.entrySet());
        return content;
    }
   
    /**
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     * 
     */
    public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException {
        if (Constants.ELEMENT_NAME_PROP.equals(qName)) {
            String action = atts.getValue(Constants.ELEMENT_NAME_ACTION);
            String key = atts.getValue(Constants.ATTRIBUTE_NAME_ATT) 
            + Constants.EQUAL + atts.getValue(Constants.ATTRIBUTE_NAME_VAL);
            if (action != null) {
                if(filterMap.get(key) == null){
                    filterMap.put(key, action);
                }else{
                    System.out.println("duplicate condition in filter file");
                }
            }
        }
    }
}
