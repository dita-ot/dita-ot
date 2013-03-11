/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2005, 2006 All Rights Reserved.
 */
package org.dita.dost.platform;

import static org.dita.dost.util.Constants.*;

import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DefaultHandler2;

/**
 * InsertAction implements IAction and insert the resource
 * provided by plug-ins into the xsl files, ant scripts and xml catalog.
 * @author Zhang, Yuan Peng
 */
class InsertAction extends DefaultHandler2 implements IAction {

    protected final XMLReader reader;
    protected DITAOTLogger logger;
    protected final Set<String> fileNameSet;
    protected final StringBuffer retBuf;
    protected final Hashtable<String,String> paramTable;
    protected int elemLevel = 0;
    protected boolean inCdataSection = false;
    /** Current processing file. */
    protected String currentFile;
    /**
     * Default Constructor.
     */
    public InsertAction() {
        fileNameSet = new LinkedHashSet<String>(INT_16);
        logger = new DITAOTJavaLogger();
        retBuf = new StringBuffer(INT_4096);
        paramTable = new Hashtable<String,String>();
        try {
            reader = StringUtils.getXMLReader();
            reader.setContentHandler(this);
            reader.setFeature(FEATURE_NAMESPACE_PREFIX, true);
            reader.setProperty(LEXICAL_HANDLER_PROPERTY, this);

            reader.setFeature("http://apache.org/xml/features/scanner/notify-char-refs", true);
            reader.setFeature("http://apache.org/xml/features/scanner/notify-builtin-refs", true);
        } catch (final Exception e) {
            throw new RuntimeException("Failed to initialize parser: " + e.getMessage(), e);
        }
    }

    @Override
    public void setInput(final String input) {
        final StringTokenizer inputTokenizer = new StringTokenizer(input, Integrator.FEAT_VALUE_SEPARATOR);
        while(inputTokenizer.hasMoreElements()){
            fileNameSet.add(inputTokenizer.nextToken());
        }
    }

    @Override
    public void addParam(final String name, final String value) {
        paramTable.put(name, value);
    }

    @Override
    public String getResult() {
        try{
            for (final String fileName: fileNameSet) {
                currentFile = fileName;
                reader.parse(currentFile);
            }
        } catch (final Exception e) {
            logger.logError(e.getMessage(), e) ;
        }
        return retBuf.toString();
    }

    @Override
    public void setFeatures(final Map<String, Features> h) {
    }

    @Override
    public void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
        if(elemLevel != 0){
            final int attLen = attributes.getLength();
            retBuf.append(LINE_SEPARATOR);
            retBuf.append("<").append(qName);
            for (int i = 0; i < attLen; i++){
                retBuf.append(" ").append(attributes.getQName(i)).append("=\"");
                retBuf.append(StringUtils.escapeXML(attributes.getValue(i))).append("\"");
            }
            if(("public".equals(localName) ||
                    "system".equals(localName) ||
                    "uri".equals(localName))){
                retBuf.append("/>");
            }
            else{
                retBuf.append(">");
            }
        }
        elemLevel ++;
    }

    @Override
    public void characters(final char[] ch, final int start, final int length) throws SAXException {
        if (inCdataSection) {
            retBuf.append(ch, start, length);
        } else {
            final char[] esc = StringUtils.escapeXML(ch, start, length).toCharArray();
            retBuf.append(esc, 0, esc.length);
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        elemLevel --;
        if(elemLevel != 0 &&
                (!"public".equals(localName) &&
                        !"system".equals(localName) &&
                        !"uri".equals(localName))
                ){
            //retBuf.append(LINE_SEPARATOR);
            retBuf.append("</").append(qName).append(">");
        }
    }

    @Override
    public void ignorableWhitespace(final char[] ch, final int start, final int length) throws SAXException {
        retBuf.append(ch, start, length);
    }

    @Override
    public void startDocument() throws SAXException {
        elemLevel = 0;
    }

    @Override
    public void comment(final char[] ch, final int start, final int length) throws SAXException {
        retBuf.append("<!--").append(ch, start, length).append("-->");
    }

    @Override
    public void startCDATA() throws SAXException {
        inCdataSection = true;
        retBuf.append(CDATA_HEAD);

    }

    @Override
    public void endCDATA() throws SAXException {
        retBuf.append(CDATA_END);
        inCdataSection = false;
    }

}
