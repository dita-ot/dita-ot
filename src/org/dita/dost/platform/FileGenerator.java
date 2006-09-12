/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2005, 2006 All Rights Reserved.
 */
package org.dita.dost.platform;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Hashtable;

import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.util.Constants;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Generate outputfile with templates
 * @author Zhang, Yuan Peng
 */
public class FileGenerator extends DefaultHandler {
	
	private XMLReader reader = null;
	private DITAOTJavaLogger logger = null;
	private OutputStreamWriter output = null;
	private Hashtable featureTable = null;
	private String templateFileName = null;

	/**
	 * Defautl Constructor
	 */
	public FileGenerator() {
		this(null);
	}

	/**
	 * Constructor init featureTable
	 * @param featureTbl
	 */
	public FileGenerator(Hashtable featureTbl) {
		this.featureTable = featureTbl;
		output = null;
		templateFileName = null;	
		logger = new DITAOTJavaLogger();
		
		try {
            if (System.getProperty(Constants.SAX_DRIVER_PROPERTY) == null){
                //The default sax driver is set to xerces's sax driver
                System.setProperty(Constants.SAX_DRIVER_PROPERTY, Constants.SAX_DRIVER_DEFAULT_CLASS);
            }
            reader = XMLReaderFactory.createXMLReader();
            reader.setContentHandler(this);
            //reader.setProperty(Constants.LEXICAL_HANDLER_PROPERTY,this);
            reader.setFeature(Constants.FEATURE_NAMESPACE_PREFIX, true);
            //reader.setFeature(Constants.FEATURE_VALIDATION, true); 
            //reader.setFeature(Constants.FEATURE_VALIDATION_SCHEMA, true);

        } catch (Exception e) {
        	logger.logException(e);
        }
	}
	
	/**
	 * Generator the output file
	 * @param fileName
	 */
	public void generate(String fileName){
		FileOutputStream fileOutput = null;
		File outputFile = new File(fileName.substring(0,
				fileName.lastIndexOf("_template")) + 
				fileName.substring(fileName.lastIndexOf('.')));
		templateFileName = fileName;
				
		try{
			fileOutput = new FileOutputStream(outputFile);
	        output = new OutputStreamWriter(fileOutput, Constants.UTF8);
			reader.parse(fileName);
		} catch (Exception e){
			logger.logException(e);
		}finally {
            try {
                fileOutput.close();
            }catch (Exception e) {
            	logger.logException(e);
            }
        }
	}

	/**
	 * @see org.xml.sax.ContentHandler#characters(char[], int, int)
	 */
	public void characters(char[] ch, int start, int length) throws SAXException {
		try{
			output.write(ch,start,length);
		}catch (Exception e) {
        	logger.logException(e);
        }
	}

	/**
	 * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void endElement(String uri, String localName, String qName) throws SAXException {
		try{
			if(!"dita:extension".equals(qName)){
				output.write("</"+qName+">");
			}
		}catch (Exception e) {
        	logger.logException(e);
        }
	}

	/**
	 * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
	 */
	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
		try{
			output.write(ch,start,length);
		}catch (Exception e) {
        	logger.logException(e);
        }
	}

	/**
	 * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		IAction action = null;
		String input = null;
		try{
			if("dita:extension".equals(qName)){
				action = (IAction)Class.forName(attributes.getValue("behavior")).newInstance();
				action.setParam("template="+templateFileName+";extension="+attributes.getValue("id"));
				input = (String)featureTable.get(attributes.getValue("id"));
				if(input!=null){
					action.setInput(input);
					output.write(action.getResult());
				}
			}else{
				int attLen = attributes.getLength();
				output.write("<"+qName);
				for(int i = 0; i < attLen; i++){
					output.write(" ");
					output.write(new StringBuffer(attributes.getQName(i)).append("=\"").
							append(attributes.getValue(i)).append("\"").toString());
				}
				output.write(">");
			}
		}catch(Exception e){
			logger.logException(e);
		}
	}

	/**
	 * @see org.xml.sax.ContentHandler#endDocument()
	 */
	public void endDocument() throws SAXException {
		try{
			output.flush();
			output.close();
		}catch(Exception e){
			logger.logException(e);
		}
		
	}

	
}
