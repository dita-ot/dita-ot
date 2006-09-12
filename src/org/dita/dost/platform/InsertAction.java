/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2005, 2006 All Rights Reserved.
 */
package org.dita.dost.platform;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.util.Constants;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * InsertAction implements IAction and insert the resource 
 * provided by plug-ins into the xsl files, ant scripts and xml catalog.
 * @author Zhang, Yuan Peng
 */
public class InsertAction extends DefaultHandler implements IAction {

	private XMLReader reader;
	private DITAOTJavaLogger logger;
	private Set fileNameSet = null;
	private StringBuffer retBuf;
	private Hashtable paramTable = null;
	private int elemLevel = 0;
	
	/**
	 * Default Constructor
	 */
	public InsertAction() {
		fileNameSet = new LinkedHashSet(Constants.INT_16);
		logger = new DITAOTJavaLogger();
		retBuf = new StringBuffer(Constants.INT_4096);
		paramTable = new Hashtable();
		try {
            if (System.getProperty(Constants.SAX_DRIVER_PROPERTY) == null){
                //The default sax driver is set to xerces's sax driver
                System.setProperty(Constants.SAX_DRIVER_PROPERTY, Constants.SAX_DRIVER_DEFAULT_CLASS);
            }
            reader = XMLReaderFactory.createXMLReader();
            reader.setContentHandler(this);
            reader.setFeature(Constants.FEATURE_NAMESPACE_PREFIX, true);

        } catch (Exception e) {
        	logger.logException(e);
        }
	}

	/**
	 * @see org.dita.dost.platform.IAction#setInput(java.lang.String)
	 */
	public void setInput(String input) {
		StringTokenizer inputTokenizer = new StringTokenizer(input,",");
		while(inputTokenizer.hasMoreElements()){
			fileNameSet.add(inputTokenizer.nextElement());
		}
	}

	/**
	 * @see org.dita.dost.platform.IAction#setParam(java.lang.String)
	 */
	public void setParam(String param) {
		StringTokenizer paramTokenizer = new StringTokenizer(param,";");
		String paramExpression = null;
		int index;
		while(paramTokenizer.hasMoreElements()){
			paramExpression = (String) paramTokenizer.nextElement();
			index = paramExpression.indexOf("=");
			if(index > 0){
				paramTable.put(paramExpression.substring(0,index),
						paramExpression.substring(index+1));
			}
		}		
	}

	/**
	 * @see org.dita.dost.platform.IAction#getResult()
	 */
	public String getResult() {
		Iterator iter;
		iter = fileNameSet.iterator();
		try{
			while(iter.hasNext()){
				reader.parse((String)iter.next());
			}
		} catch (Exception e) {
	       	logger.logException(e);
		}
		return retBuf.toString();
	}

	/**
	 * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if(elemLevel != 0){
			int attLen = attributes.getLength();
			retBuf.append(Constants.LINE_SEPARATOR);
			retBuf.append("<"+qName);
			for (int i = 0; i < attLen; i++){
				retBuf.append(" ").append(attributes.getQName(i)).append("=\"");
				retBuf.append(attributes.getValue(i)).append("\"");
			}
			retBuf.append(">");
		}
		elemLevel ++;
	}

	/**
	 * @see org.xml.sax.ContentHandler#characters(char[], int, int)
	 */
	public void characters(char[] ch, int start, int length) throws SAXException {
		retBuf.append(ch, start, length);
	}

	/**
	 * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void endElement(String uri, String localName, String qName) throws SAXException {
		elemLevel --;
		if(elemLevel != 0){
			retBuf.append(Constants.LINE_SEPARATOR);
			retBuf.append("</"+qName+">");
		}
	}

	/**
	 * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
	 */
	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
		retBuf.append(ch, start, length);
	}

	/**
	 * @see org.xml.sax.ContentHandler#startDocument()
	 */
	public void startDocument() throws SAXException {
		elemLevel = 0;
	}

	
}
