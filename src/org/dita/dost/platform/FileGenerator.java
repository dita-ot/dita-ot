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
 *
 * @author Zhang, Yuan Peng
 */
public class FileGenerator extends DefaultHandler {
	
	private XMLReader reader;
	private DITAOTJavaLogger logger;
	private OutputStreamWriter output;
	private Hashtable featureTable;
	private String templateFileName;

	public FileGenerator(Hashtable featureTable) {
		this.featureTable = featureTable;		
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

	public void characters(char[] ch, int start, int length) throws SAXException {
		try{
			output.write(ch,start,length);
		}catch (Exception e) {
        	logger.logException(e);
        }
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		try{
			if("dita:extension".equals(qName)){
				//output nothing
			}else{
				output.write("</"+qName+">");
			}
		}catch (Exception e) {
        	logger.logException(e);
        }
	}

	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
		try{
			output.write(ch,start,length);
		}catch (Exception e) {
        	logger.logException(e);
        }
	}

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
				output.write("<"+qName);
				for(int i = 0; i < attributes.getLength(); i++){
					output.write(" ");
					output.write(attributes.getQName(i)+"=\""+attributes.getValue(i)+"\"");
				}
				output.write(">");
			}
		}catch(Exception e){
			logger.logException(e);
		}
	}

	public void endDocument() throws SAXException {
		try{
			output.flush();
			output.close();
		}catch(Exception e){
			logger.logException(e);
		}
		
	}

	
}
