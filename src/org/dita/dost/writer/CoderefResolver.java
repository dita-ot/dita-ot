/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.writer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Properties;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.exception.DITAOTXMLErrorHandler;
import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.module.Content;
import org.dita.dost.util.Constants;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
/**
 * CoderefResolver class, resolving 
 * coderef attribute in topic file.
 *
 */
public class CoderefResolver extends AbstractXMLWriter {
	
	private OutputStreamWriter output = null;
	
	private XMLReader reader = null;
	
	private DITAOTJavaLogger logger = null;
	
	private File currentFile = null;
	
	private HashSet<String> coderefSpec = null;
	/**
	 * Constructor.
	 */
	public CoderefResolver() {
		// TODO Auto-generated constructor stub
		logger = new DITAOTJavaLogger();
		
		coderefSpec = new HashSet<String>();
		
		try {
            if (System.getProperty(Constants.SAX_DRIVER_PROPERTY) == null){
                //The default sax driver is set to xerces's sax driver
            	StringUtils.initSaxDriver();
            }
            reader = XMLReaderFactory.createXMLReader();
            reader.setContentHandler(this);
            reader.setProperty(Constants.LEXICAL_HANDLER_PROPERTY,this);
            reader.setFeature(Constants.FEATURE_NAMESPACE_PREFIX, true);
        } catch (Exception e) {
        	logger.logException(e);
        }

	}
	@Override
	public void setContent(Content content) {
		// TODO Auto-generated method stub

	}
	@Override
	public void write(String filename) throws DITAOTException {
		// TODO Auto-generated method stub
		String file = null;
		File inputFile = null;
		File outputFile = null;
		FileOutputStream fileOutput = null;

        try {
            
            file = filename;                
            
            // ignore in-exists file
            if (file == null || !new File(file).exists()) {
            	return;
            }
                     
            inputFile = new File(file);
            currentFile = inputFile;
            outputFile = new File(file + Constants.FILE_EXTENSION_TEMP);
            fileOutput = new FileOutputStream(outputFile);
            output = new OutputStreamWriter(fileOutput, Constants.UTF8);
            reader.setErrorHandler(new DITAOTXMLErrorHandler(file));
            reader.parse(file);
            output.flush();
            output.close();
            
            if(!inputFile.delete()){
            	Properties prop = new Properties();
            	prop.put("%1", inputFile.getPath());
            	prop.put("%2", outputFile.getPath());
            	logger.logError(MessageUtils.getMessage("DOTJ009E", prop).toString());

            }
            if(!outputFile.renameTo(inputFile)){
            	Properties prop = new Properties();
            	prop.put("%1", inputFile.getPath());
            	prop.put("%2", outputFile.getPath());
            	logger.logError(MessageUtils.getMessage("DOTJ009E", prop).toString());
            }
        } catch (Exception e) {
        	logger.logException(e);
        }finally {
            try {
            	if (fileOutput != null) {
            		fileOutput.close();
            	}
            }catch (Exception e) {
				logger.logException(e);
            }
        }

	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		try {
            output.write(StringUtils.escapeXML(ch, start, length));
        } catch (Exception e) {
        	logger.logException(e);
        }
	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {
		try {
            output.write(ch, start, length);
        } catch (Exception e) {
        	logger.logException(e);
        }
	}

	@Override
	public void processingInstruction(String target, String data)
			throws SAXException {
		try {
        	super.processingInstruction(target, data);
        	String pi = (data != null) ? target + Constants.STRING_BLANK + data : target;
            output.write(Constants.LESS_THAN + Constants.QUESTION 
                    + pi + Constants.QUESTION + Constants.GREATER_THAN);
        } catch (Exception e) {
        	logger.logException(e);
        }
	}

	@Override
	public void startElement(String uri, String localName, String name,
			Attributes atts) throws SAXException {
		String classValue = atts.getValue(Constants.ATTRIBUTE_NAME_CLASS);
		String hrefValue = atts.getValue(Constants.ATTRIBUTE_NAME_HREF);
		try{
			if (classValue != null
					&& classValue.contains(Constants.ATTR_CLASS_VALUE_CODEREF)){
				//TODO resolve coderef and pull in program content
				coderefSpec.add(name);
				if (hrefValue != null){
					String codeFile = FileUtils.normalizeDirectory(
							currentFile.getParentFile().getAbsolutePath(), hrefValue);
					if (new File(codeFile).exists()){
						FileReader codeReader = new FileReader(new File(codeFile));
						char[] buffer = new char[Constants.INT_1024 * Constants.INT_4];
						int len;
						while((len = codeReader.read(buffer)) != -1){
							output.write(StringUtils.escapeXML(buffer, 0, len));
						}
						codeReader.close();
					}else{
						//report error of href target is not valid
					}
				}else{
					//report error of href attribute is null
				}
			}else{
				output.write(Constants.LESS_THAN + name);
				for (int i=0; i<atts.getLength(); i++){
					output.write(Constants.STRING_BLANK + atts.getQName(i)
							+ Constants.EQUAL + Constants.QUOTATION
							+ atts.getValue(i) + Constants.QUOTATION);
				}
				output.write(Constants.GREATER_THAN);
			}
		}catch (Exception e){
			logger.logException(e);
		}
	}

	@Override
	public void endElement(String uri, String localName, String name)
			throws SAXException {
		try{
			if(!coderefSpec.contains(name)){
				output.write(Constants.LESS_THAN + Constants.SLASH 
						+ name + Constants.GREATER_THAN);
			}
		}catch (Exception e){
			logger.logException(e);
		}		
	}
}
