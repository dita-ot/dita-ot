/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.writer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

import org.dita.dost.module.Content;
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
 * 
 */
public class DitaLinksWriter extends AbstractWriter implements ContentHandler,LexicalHandler {

    private String indexEntries;
    private XMLReader reader;
    private OutputStreamWriter output;
    private boolean hasRelatedlinks;// whether there is <related-links> in thisfile
    private boolean needResolveEntity;
    private int level; // level of the element


    public void setContent(Content content) {
        indexEntries = (String) content.getObject();
        //System.out.println(indexEntries);
    }

    /**
     * 
     */
    public DitaLinksWriter() {
        super();
        try {
            //SAXParserFactory spFactory = SAXParserFactory.newInstance();
            //spFactory.setValidating(true);
            //spFactory.setFeature("http://xml.org/sax/features/validation", true);
            //SAXParser parser = spFactory.newSAXParser();
            //reader = parser.getXMLReader();
            if (System.getProperty("org.xml.sax.driver") == null){
                //The default sax driver is set to xerces's sax driver
                System.setProperty("org.xml.sax.driver","org.apache.xerces.parsers.SAXParser");
            }
            reader = XMLReaderFactory.createXMLReader();
            reader.setContentHandler(this);
            reader.setProperty("http://xml.org/sax/properties/lexical-handler",this);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void write(String filename) {

        try {
        	needResolveEntity = true;
            hasRelatedlinks = false;
            File inputFile = new File(filename);
            File outputFile = new File(filename + ".temp");
            FileOutputStream fileOutput = new FileOutputStream(outputFile);
            output = new OutputStreamWriter(fileOutput, "UTF-8");

            reader.parse(filename);
            output.close();
            
            if(!inputFile.delete()){
            	System.out.println("File not deleted. " + inputFile.getPath());
            }
            if(!outputFile.renameTo(inputFile)){
            	System.out.println("File not renamed. " + outputFile.getPath());
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    public void characters(char[] ch, int start, int length)
            throws SAXException {
    	if(needResolveEntity){
    		try {
    			output.write(ch, start, length);
    		} catch (Exception e) {
    			e.printStackTrace(System.out);
    		}
    	}
    }

    public void endDocument() throws SAXException {

        try {
            output.flush();
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        level--;
        try {
            if (!hasRelatedlinks && level == 0) {
                output.write("<related-links class=\"- topic/related-links \">");
                output.write(indexEntries);
                output.write("</related-links>");
                hasRelatedlinks = true;
            }
            output.write("</" + qName + ">");
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    public void endPrefixMapping(String prefix) throws SAXException {

    }

    public void ignorableWhitespace(char[] ch, int start, int length)
            throws SAXException {
        try {
            output.write(ch, start, length);
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    /**
     * 
     */
    public void processingInstruction(String target, String data)
            throws SAXException {
        String pi;
        try {
            if (data != null) {
                pi = target + " " + data;
            } else {
                pi = target;
            }
            output.write("<?" + pi + "?>");
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    public void setDocumentLocator(Locator locator) {

    }

    public void skippedEntity(String name) throws SAXException {
        try {
            output.write(name);
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    public void startDocument() throws SAXException {
        level = 0;
    }

    public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException {
    	
        level++;
        try {
            if (!hasRelatedlinks && level > 1
                    && atts.getValue("class").indexOf(" topic/topic ") != -1) {
                // if <related-links> don't exist
                output.write("<related-links class=\"- topic/related-links \">");
                output.write(indexEntries);
                output.write("</related-links>");
                hasRelatedlinks = true;
            }

            output.write("<" + qName);
            for (int i = 0; i < atts.getLength(); i++) {
                String attQName = atts.getQName(i);
                String attValue;
                attValue = atts.getValue(i);
                output.write(" " + attQName + "=\"" + attValue + "\"");
            }
            output.write(">");
            if (atts.getValue("class").indexOf(" topic/related-links ") != -1) {
                hasRelatedlinks = true;
                output.write(indexEntries);
            }

        } catch (Exception e) {
            e.printStackTrace(System.out);
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
	    try{
	        output.write("]]>");
	    }catch(Exception e){
	        e.printStackTrace(System.out);
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
	    try{
	        output.write("<![CDATA[");
	    }catch(Exception e){
	        e.printStackTrace(System.out);
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
		try {
           	needResolveEntity = StringUtils.checkEntity(name);
           	if(!needResolveEntity){
           		output.write(StringUtils.getEntity(name));
           	}
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
        
	}
}
