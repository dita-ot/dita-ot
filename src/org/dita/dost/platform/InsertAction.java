/*
 * (c) Copyright IBM Corp. 2005, 2006 All Rights Reserved.
 */
package org.dita.dost.platform;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.util.Constants;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 *
 * @author Zhang, Yuan Peng
 */
public class InsertAction extends DefaultHandler implements IAction {

	private XMLReader reader;
	private DITAOTJavaLogger logger;
	private HashSet fileNameSet = null;
	private StringBuffer retBuf;
	private Hashtable paramTable = null;
	private int elemLevel = 0;
	
	public InsertAction() {
		fileNameSet = new HashSet(16);
		logger = new DITAOTJavaLogger();
		retBuf = new StringBuffer(4096);
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

	public void setInput(String input) {
		StringTokenizer inputTokenizer = new StringTokenizer(input,",");
		while(inputTokenizer.hasMoreElements()){
			fileNameSet.add(inputTokenizer.nextElement());
		}
	}

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

	public String getResult() {
		Iterator iter;
		iter = fileNameSet.iterator();
		while(iter.hasNext()){
			try{
				reader.parse((String)iter.next());
			} catch (Exception e) {
	        	logger.logException(e);
	        }
		}
		return retBuf.toString();
	}

	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if(elemLevel != 0){
			retBuf.append(Constants.LINE_SEPARATOR);
			retBuf.append("<"+qName);
			for (int i = 0; i < attributes.getLength(); i++){
				retBuf.append(" ").append(attributes.getQName(i)).append("=\"");
				retBuf.append(attributes.getValue(i)).append("\"");
			}
			retBuf.append(">");
		}
		elemLevel ++;
	}

	public void characters(char[] ch, int start, int length) throws SAXException {
		retBuf.append(ch, start, length);
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		elemLevel --;
		if(elemLevel != 0){
			retBuf.append(Constants.LINE_SEPARATOR);
			retBuf.append("</"+qName+">");
		}
	}

	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
		retBuf.append(ch, start, length);
	}

	public void startDocument() throws SAXException {
		elemLevel = 0;
	}

	
}
