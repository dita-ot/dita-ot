/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2005 All Rights Reserved.
 */
package org.dita.dost.reader;

import java.io.IOException;
import java.util.HashMap;

import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.module.Content;
import org.dita.dost.util.CatalogUtils;
import org.dita.dost.util.Constants;
import org.dita.dost.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;

/**
 * This class extends AbstractReader, implement SAX's ContentHandler, 
 * LexicalHandler, and EntityResolver.
 * 
 * @version 1.0 2005-06-24
 * 
 * @author Wu, Zhi Qiang
 */
public abstract class AbstractXMLReader implements AbstractReader,
        ContentHandler, LexicalHandler, EntityResolver {
	
    /** XMLReader instance for parsing dita file */
	protected static XMLReader reader = null;
	/** Map of XML catalog info */
	protected static HashMap<String, String> catalogMap = null;

	/**
	 * @param validate
	 * @param transtype 
	 * @param rootFile 
	 * @param grammarPool
	 * @throws SAXException 
	 */
	public static XMLReader initXMLReaderBase(String ditaDir, boolean validate,			
		XMLGrammarPool inGrammarPool) throws SAXException {		
		// FIXME: WEK: This is my attempt to factor out common reader initialization
		//             code for the GenListModuleReader and the Debug and filter reader.
		
		XMLGrammarPool grammarPool = null;
		
		if (inGrammarPool == null) {
			grammarPool = GrammarPoolManager.getGrammarPool();
		} else {
			grammarPool = inGrammarPool;
		}
	
		DITAOTJavaLogger javaLogger=new DITAOTJavaLogger();
		XMLReader reader = StringUtils.getXMLReader();
		reader.setFeature(Constants.FEATURE_NAMESPACE_PREFIX, true);
		if(validate==true){
			reader.setFeature(Constants.FEATURE_VALIDATION, true);
			reader.setFeature(Constants.FEATURE_VALIDATION_SCHEMA, true);
		}else{
			String msg=MessageUtils.getMessage("DOTJ037W").toString();
			javaLogger.logWarn(msg);
		}
		setGrammarPool(reader, grammarPool);
	
		CatalogUtils.setDitaDir(ditaDir);
		catalogMap = CatalogUtils.getCatalog(ditaDir);
		return reader;
	}

	/**
	 * Sets the grammar pool on the parser. Note that this is a Xerces-specific
	 * feature.
	 * @param reader
	 * @param grammarPool
	 */
	public static void setGrammarPool(XMLReader reader, XMLGrammarPool grammarPool) {
		
		DITAOTJavaLogger logger = new DITAOTJavaLogger();
		if (grammarPool == null) {
			grammarPool = GrammarPoolManager.getGrammarPool();
		}
		if (grammarPool != null) {
			try {
				reader.setProperty(
								"http://apache.org/xml/properties/internal/grammar-pool",
								grammarPool);
				
				String msg = "Using Xerces grammar pool for DTD and schema caching.";
				logger.logInfo(msg);
				
			} catch (Exception e) {
				String msg = "Failed to setXerces grammar pool for parser: "
					+ e.getMessage();
				logger.logInfo(msg);
			}
		} else {
			String msg = "grammar pool is null";
			logger.logInfo(msg);
		}
	}
	
	protected DITAOTJavaLogger javaLogger = null;

	/**
     * (non-Javadoc).
     * 
     * @see org.dita.dost.reader.AbstractReader#read(java.lang.String)
     */
    public void read(String filename) {
    }

    /**
     * (non-Javadoc).
     * 
     * @see org.dita.dost.reader.AbstractReader#getContent()
     */
    public Content getContent() {
        return null;
    }

    /**
     * (non-Javadoc).
     * 
     * @see org.xml.sax.ContentHandler#setDocumentLocator(org.xml.sax.Locator)
     */
    public void setDocumentLocator(Locator locator) {
    }

    /**
     * (non-Javadoc).
     * 
     * @see org.xml.sax.ContentHandler#startDocument()
     */
    public void startDocument() throws SAXException {
    }

    /**
     * (non-Javadoc).
     * 
     * @see org.xml.sax.ContentHandler#endDocument()
     */
    public void endDocument() throws SAXException {
    }

    /**
     * (non-Javadoc).
     * 
     * @see org.xml.sax.ContentHandler#startPrefixMapping(java.lang.String,
     *      java.lang.String)
     */
    public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
    }

    /**
     * (non-Javadoc).
     * 
     * @see org.xml.sax.ContentHandler#endPrefixMapping(java.lang.String)
     */
    public void endPrefixMapping(String prefix) throws SAXException {
    }

    /**
     * (non-Javadoc).
     * 
     * @see org.xml.sax.ContentHandler#startElement(java.lang.String,
     *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException {
    }

    /**
     * (non-Javadoc).
     * 
     * @see org.xml.sax.ContentHandler#endElement(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
    }

    /**
     * (non-Javadoc).
     * 
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] ch, int start, int length)
            throws SAXException {
    }

    /**
     * (non-Javadoc).
     * 
     * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
     */
    public void ignorableWhitespace(char[] ch, int start, int length)
            throws SAXException {
    }

    /**
     * (non-Javadoc).
     * 
     * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String,
     *      java.lang.String)
     */
    public void processingInstruction(String target, String data)
            throws SAXException {
    }

    /**
     * (non-Javadoc).
     * 
     * @see org.xml.sax.ContentHandler#skippedEntity(java.lang.String)
     */
    public void skippedEntity(String name) throws SAXException {
    }

    /**
     * (non-Javadoc).
     * 
     * @see org.xml.sax.ext.LexicalHandler#startDTD(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public void startDTD(String name, String publicId, String systemId)
            throws SAXException {
    }

    /**
     * (non-Javadoc).
     * 
     * @see org.xml.sax.ext.LexicalHandler#endDTD()
     */
    public void endDTD() throws SAXException {
    }

    /**
     * (non-Javadoc).
     * 
     * @see org.xml.sax.ext.LexicalHandler#startEntity(java.lang.String)
     */
    public void startEntity(String name) throws SAXException {
    }

    /**
     * (non-Javadoc).
     * 
     * @see org.xml.sax.ext.LexicalHandler#endEntity(java.lang.String)
     */
    public void endEntity(String name) throws SAXException {
    }

    /**
     * (non-Javadoc).
     * 
     * @see org.xml.sax.ext.LexicalHandler#startCDATA()
     */
    public void startCDATA() throws SAXException {
    }

    /**
     * (non-Javadoc).
     * 
     * @see org.xml.sax.ext.LexicalHandler#endCDATA()
     */
    public void endCDATA() throws SAXException {
    }

    /**
     * (non-Javadoc).
     * 
     * @see org.xml.sax.ext.LexicalHandler#comment(char[], int, int)
     */
    public void comment(char[] ch, int start, int length) throws SAXException {
    }

    /**
     * (non-Javadoc).
     * 
     * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String,
     *      java.lang.String)
     */
    public InputSource resolveEntity(String publicId, String systemId)
            throws SAXException, IOException {
        return null;
    }

}
