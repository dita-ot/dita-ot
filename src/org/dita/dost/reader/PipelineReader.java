/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.reader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.dita.dost.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;


/**
 * Parse the pipeline input.
 * 
 * @author Wu, Zhi Qiang
 */
public class PipelineReader implements ContentHandler {
    private List result = null;
    private boolean hasConRef = false;
    private boolean hasHref = false;
    private XMLReader reader = null;
    private String filePath = null;

    /**
     * Constructor
     */
    public PipelineReader() {
        result = new ArrayList();
    }

    /**
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public void initXMLReader() throws SAXException,
            ParserConfigurationException {
        if (System.getProperty("org.xml.sax.driver") == null) {
            // The default sax driver is set to xerces's sax driver
            System.setProperty("org.xml.sax.driver",
                    "org.apache.xerces.parsers.SAXParser");
        }
        reader = XMLReaderFactory.createXMLReader();
        reader.setContentHandler(this);
    }

    /**
     * @param file
     * @throws IOException
     * @throws SAXException
     */
    public void parse(String file) throws IOException, SAXException {
        int index = file.indexOf('|');
        File xmlRelativeFile;

        if (index != -1) { // if there is parent directory's absolute path
            xmlRelativeFile = new File(file.substring(index + 1));
        } else {
            xmlRelativeFile = new File(file);
        }

        filePath = xmlRelativeFile.getParent();

        hasConRef = false;
        hasHref = false;
        if (index != -1) {
            reader
                    .parse(new InputSource(file
                            .replace('|', File.separatorChar)));
        } else {
            reader.parse(file);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String,
     *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException {
        parseAttribute(atts, "conref");
        parseAttribute(atts, "href");
        parseAttribute(atts, "copy-to");
        parseAttribute(atts, "img");
    }

    private void parseAttribute(Attributes atts, String attrName) {
        String attrValue = atts.getValue(attrName);
        if (attrValue != null && attrValue.indexOf("://") == -1) {
            if ("conref".equals(attrName)) {
                hasConRef = true;
            } else if ("href".equals(attrName)) {
                hasHref = true;
            }

            if (attrValue.startsWith("#")) {
                return;
            }

            String filename = StringUtils.resolveDirectory(filePath, attrValue);
            String lcasefn = filename.toLowerCase();
            if (lcasefn.endsWith(".dita") || lcasefn.endsWith(".ditamap")
                    || lcasefn.endsWith(".xml") || lcasefn.endsWith(".jpg")
                    || lcasefn.endsWith(".gif") || lcasefn.endsWith(".eps")) {              
                result.add(filename);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#endDocument()
     */
    public void endDocument() throws SAXException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#startDocument()
     */
    public void startDocument() throws SAXException {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] ch, int start, int length)
            throws SAXException {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
     */
    public void ignorableWhitespace(char[] ch, int start, int length)
            throws SAXException {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
     */
    public void endPrefixMapping(String prefix) throws SAXException {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
     */
    public void skippedEntity(String name) throws SAXException {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
     */
    public void setDocumentLocator(Locator locator) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String,
     *      java.lang.String)
     */
    public void processingInstruction(String target, String data)
            throws SAXException {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String,
     *      java.lang.String)
     */
    public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public void endElement(String namespaceURI, String localName, String qName)
            throws SAXException {
    }

    /**
     * @return
     */
    public boolean hasConRef() {
        return hasConRef;
    }

    /**
     * @return
     */
    public boolean hasHref() {
        return hasHref;
    }

    /**
     * @return Returns the result.
     */
    public List getResult() {
        return result;
    }
}