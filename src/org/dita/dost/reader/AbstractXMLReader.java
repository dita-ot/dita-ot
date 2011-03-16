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
import org.dita.dost.log.DITAOTLogger;
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
	
		final DITAOTLogger javaLogger=new DITAOTJavaLogger();
		final XMLReader reader = StringUtils.getXMLReader();
		reader.setFeature(Constants.FEATURE_NAMESPACE_PREFIX, true);
		if(validate==true){
			reader.setFeature(Constants.FEATURE_VALIDATION, true);
			reader.setFeature(Constants.FEATURE_VALIDATION_SCHEMA, true);
		}else{
			final String msg=MessageUtils.getMessage("DOTJ037W").toString();
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
		
		final DITAOTLogger logger = new DITAOTJavaLogger();
		if (grammarPool == null) {
			grammarPool = GrammarPoolManager.getGrammarPool();
		}
		if (grammarPool != null) {
			try {
				reader.setProperty(
								"http://apache.org/xml/properties/internal/grammar-pool",
								grammarPool);
				
				final String msg = "Using Xerces grammar pool for DTD and schema caching.";
				logger.logInfo(msg);
				
			} catch (final Exception e) {
				final String msg = "Failed to setXerces grammar pool for parser: "
					+ e.getMessage();
				logger.logInfo(msg);
			}
		} else {
			final String msg = "grammar pool is null";
			logger.logInfo(msg);
		}
	}
	
	protected DITAOTLogger logger;

    public void read(String filename) {
        // NOOP
    }

    public Content getContent() {
        return null;
    }
    
    public void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
    }

    public void setDocumentLocator(Locator locator) {
        // NOOP
    }

    public void startDocument() throws SAXException {
        // NOOP
    }

    public void endDocument() throws SAXException {
        // NOOP
    }

    public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
        // NOOP
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        // NOOP
    }

    public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException {
        // NOOP
    }

    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        // NOOP
    }

    public void characters(char[] ch, int start, int length)
            throws SAXException {
        // NOOP
    }

    public void ignorableWhitespace(char[] ch, int start, int length)
            throws SAXException {
        // NOOP
    }

    public void processingInstruction(String target, String data)
            throws SAXException {
        // NOOP
    }

    public void skippedEntity(String name) throws SAXException {
        // NOOP
    }

    public void startDTD(String name, String publicId, String systemId)
            throws SAXException {
        // NOOP
    }

    public void endDTD() throws SAXException {
        // NOOP
    }

    public void startEntity(String name) throws SAXException {
        // NOOP
    }

    public void endEntity(String name) throws SAXException {
        // NOOP
    }

    public void startCDATA() throws SAXException {
        // NOOP
    }

    public void endCDATA() throws SAXException {
        // NOOP
    }

    public void comment(char[] ch, int start, int length) throws SAXException {
        // NOOP
    }

    public InputSource resolveEntity(String publicId, String systemId)
            throws SAXException, IOException {
        return null;
    }

}
