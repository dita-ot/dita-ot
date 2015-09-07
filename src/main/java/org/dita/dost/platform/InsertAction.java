/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2005, 2006 All Rights Reserved.
 */
package org.dita.dost.platform;

import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.util.XMLUtils;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * InsertAction implements IAction and insert the resource
 * provided by plug-ins into the xsl files, ant scripts and xml catalog.
 * @author Zhang, Yuan Peng
 */
class InsertAction extends XMLFilterImpl implements IAction {

    private final XMLReader reader;
    private DITAOTLogger logger;
    private final Set<String> fileNameSet;
    final Hashtable<String,String> paramTable;
    private int elemLevel = 0;
    /** Current processing file. */
    String currentFile;
    /**
     * Default Constructor.
     */
    public InsertAction() {
        fileNameSet = new LinkedHashSet<>(16);
        logger = new DITAOTJavaLogger();
        paramTable = new Hashtable<>();
        try {
            reader = XMLUtils.getXMLReader();
            reader.setContentHandler(this);
        } catch (final Exception e) {
            throw new RuntimeException("Failed to initialize parser: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void setInput(final List<String> input) {
        fileNameSet.addAll(input);
    }

    @Override
    public void addParam(final String name, final String value) {
        paramTable.put(name, value);
    }

    @Override
    public String getResult() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void getResult(final ContentHandler retBuf) throws SAXException {
        setContentHandler(retBuf);
        try{
            for (final String fileName: fileNameSet) {
                currentFile = fileName;
                reader.parse(currentFile);
            }
        } catch (final Exception e) {
            logger.error(e.getMessage(), e) ;
        }
    }

    @Override
    public void setFeatures(final Map<String, Features> h) {
    }

    @Override
    public void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
    }

    // XMLFilter methods
    
    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
        if (elemLevel != 0){
            getContentHandler().startElement(uri, localName, qName, attributes);
        }
        elemLevel ++;
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        elemLevel --;
        if (elemLevel != 0) {
            getContentHandler().endElement(uri, localName, qName);
        }
    }

    @Override
    public void startDocument() throws SAXException {
        elemLevel = 0;
        // suppress
    }
    
    @Override
    public void endDocument() throws SAXException {
        // suppress
    }

}
