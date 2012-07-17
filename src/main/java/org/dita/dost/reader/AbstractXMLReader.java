/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2005 All Rights Reserved.
 */
package org.dita.dost.reader;

import static org.dita.dost.util.Constants.*;

import java.io.File;
import java.io.IOException;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.module.Content;
import org.dita.dost.util.CatalogUtils;
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

    /**
     * Initialize XML reader.
     * 
     * @param ditaDir absolute path to DITA-OT base directory
     * @param validate
     * @param inGrammarPool
     * @throws SAXException if initializing reader failed
     */
    @Deprecated
    public XMLReader initXMLReaderBase(final File ditaDir, final boolean validate,
            final XMLGrammarPool inGrammarPool) throws SAXException {
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
        reader.setFeature(FEATURE_NAMESPACE_PREFIX, true);
        if(validate==true){
            reader.setFeature(FEATURE_VALIDATION, true);
            reader.setFeature(FEATURE_VALIDATION_SCHEMA, true);
        }else{
            final String msg=MessageUtils.getMessage("DOTJ037W").toString();
            javaLogger.logWarn(msg);
        }
        setGrammarPool(reader, grammarPool);

        CatalogUtils.setDitaDir(ditaDir);
        return reader;
    }

    /**
     * Sets the grammar pool on the parser. Note that this is a Xerces-specific
     * feature.
     * @param reader
     * @param grammarPool
     */
    public void setGrammarPool(final XMLReader reader, final XMLGrammarPool grammarPool) {
        try {
            reader.setProperty("http://apache.org/xml/properties/internal/grammar-pool", grammarPool);
            logger.logInfo("Using Xerces grammar pool for DTD and schema caching.");
        } catch (final Exception e) {
            logger.logWarn("Failed to set Xerces grammar pool for parser: " + e.getMessage());
        }
    }

    protected DITAOTLogger logger;

    public void read(final String filename) {
        // NOOP
    }

    public Content getContent() {
        return null;
    }

    public final void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
    }

    public void setDocumentLocator(final Locator locator) {
        // NOOP
    }

    public void startDocument() throws SAXException {
        // NOOP
    }

    public void endDocument() throws SAXException {
        // NOOP
    }

    public void startPrefixMapping(final String prefix, final String uri)
            throws SAXException {
        // NOOP
    }

    public void endPrefixMapping(final String prefix) throws SAXException {
        // NOOP
    }

    public void startElement(final String uri, final String localName, final String qName,
            final Attributes atts) throws SAXException {
        // NOOP
    }

    public void endElement(final String uri, final String localName, final String qName)
            throws SAXException {
        // NOOP
    }

    public void characters(final char[] ch, final int start, final int length)
            throws SAXException {
        // NOOP
    }

    public void ignorableWhitespace(final char[] ch, final int start, final int length)
            throws SAXException {
        // NOOP
    }

    public void processingInstruction(final String target, final String data)
            throws SAXException {
        // NOOP
    }

    public void skippedEntity(final String name) throws SAXException {
        // NOOP
    }

    public void startDTD(final String name, final String publicId, final String systemId)
            throws SAXException {
        // NOOP
    }

    public void endDTD() throws SAXException {
        // NOOP
    }

    public void startEntity(final String name) throws SAXException {
        // NOOP
    }

    public void endEntity(final String name) throws SAXException {
        // NOOP
    }

    public void startCDATA() throws SAXException {
        // NOOP
    }

    public void endCDATA() throws SAXException {
        // NOOP
    }

    public void comment(final char[] ch, final int start, final int length) throws SAXException {
        // NOOP
    }

    public InputSource resolveEntity(final String publicId, final String systemId)
            throws SAXException, IOException {
        return null;
    }

}
