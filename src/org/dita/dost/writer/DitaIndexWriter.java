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


/*
 * Created on 2004-12-17
 */

/**
 * @author Zhang, Yuan Peng
 */
public class DitaIndexWriter extends AbstractWriter implements ContentHandler,LexicalHandler {

    private String indexEntries;
    private XMLReader reader;
    private OutputStreamWriter output;
    private boolean hasProlog;// whether there is <prolog> in this file
    private boolean hasMetadata;// whether there is <metadata> in this file
    private boolean needResolveEntity;

    public void setContent(Content content) {
        indexEntries = (String) content.getObject();
    }

    /**
     * 
     */
    public DitaIndexWriter() {
        super();
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
            reader.setProperty("http://xml.org/sax/properties/lexical-handler",this);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 
     */
    public void write(String filename) {

        try {
        	needResolveEntity = true;
            hasProlog = false;
            hasMetadata = false;
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

    /**
     * 
     */
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
        try {
            if (!hasMetadata && qName.equals("prolog")) {
                output.write("<metadata class=\"- topic/metadata \">");
                output.write(indexEntries);
                output.write("</metadata>");
                hasMetadata = true;
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

    }
    
    /**
     * 
     */
    public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException {
    	
        try {
            if (!hasProlog
                    && atts.getValue("class").indexOf("topic/body") != -1) {
                // if <prolog> don't exist
                output
                        .write("<prolog class=\"- topic/prolog \"><metadata class=\"- topic/metadata \">");
                output.write(indexEntries);
                output.write("</metadata></prolog>");
            }

            if (hasProlog && !hasMetadata && qName.equals("resourceid")) {
                output.write("<metadata class=\"- topic/metadata \">");
                output.write(indexEntries);
                output.write("</metadata>");
                hasMetadata = true;
            }

            output.write("<" + qName);
            for (int i = 0; i < atts.getLength(); i++) {
                String attQName = atts.getQName(i);
                String attValue;
                attValue = atts.getValue(i);
                output.write(" " + attQName + "=\"" + attValue + "\"");
            }
            output.write(">");
            if (atts.getValue("class").indexOf(" topic/metadata ") != -1) {
                hasMetadata = true;
                output.write(indexEntries);
            }
            if (atts.getValue("class").indexOf(" topic/prolog ") != -1) {
                hasProlog = true;
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
