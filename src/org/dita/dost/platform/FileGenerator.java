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
import java.util.StringTokenizer;

import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.util.Constants;
import org.dita.dost.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DefaultHandler2;

/**
 * Generate outputfile with templates.
 * @author Zhang, Yuan Peng
 */
public class FileGenerator extends DefaultHandler2 {
	
	public static final String PARAM_LOCALNAME = "localname";
	public static final String PARAM_TEMPLATE = "template";

	private static final String DITA_OT_NS = "http://dita-ot.sourceforge.net";
	
	private final XMLReader reader;
	private final DITAOTJavaLogger logger;
	private OutputStreamWriter output = null;
	/** Plug-in features. */
	private final Hashtable<String,String> featureTable;
	/** Template file. */
	private String templateFileName = null;

	/**
	 * Default Constructor.
	 */
	public FileGenerator() {
		this(null);
	}

	/**
	 * Constructor init featureTable.
	 * @param featureTbl featureTbl
	 */
	public FileGenerator(final Hashtable<String,String> featureTbl) {
		this.featureTable = featureTbl;
		output = null;
		templateFileName = null;	
		logger = new DITAOTJavaLogger();
		
		try {
            reader = StringUtils.getXMLReader();
            reader.setContentHandler(this);
            reader.setProperty(Constants.LEXICAL_HANDLER_PROPERTY,this);
            reader.setFeature(Constants.FEATURE_NAMESPACE_PREFIX, true);
            //reader.setFeature(Constants.FEATURE_VALIDATION, true); 
            //reader.setFeature(Constants.FEATURE_VALIDATION_SCHEMA, true);

        } catch (final Exception e) {
        	throw new RuntimeException("Failed to initialize parser: " + e.getMessage(), e);
        }
	}
	
	/**
	 * Generator the output file.
	 * @param fileName filename
	 */
	public void generate(final String fileName){
		FileOutputStream fileOutput = null;
		final File outputFile = new File(fileName.substring(0,
				fileName.lastIndexOf("_template")) + 
				fileName.substring(fileName.lastIndexOf('.')));
		templateFileName = fileName;
				
		try{
			fileOutput = new FileOutputStream(outputFile);
	        output = new OutputStreamWriter(fileOutput, Constants.UTF8);
			reader.parse(fileName);
		} catch (final Exception e){
			logger.logException(e);
		}finally {
			if (fileOutput != null) {
	            try {
	                fileOutput.close();
	            }catch (final Exception e) {
	            	logger.logException(e);
	            }
			}
        }
	}

	@Override
	public void characters(final char[] ch, final int start, final int length) throws SAXException {
		try{
			output.write(StringUtils.escapeXML(ch,start,length));
		}catch (final Exception e) {
        	logger.logException(e);
        }
	}

	@Override
	public void endElement(final String uri, final String localName, final String qName) throws SAXException {
		try{
			if(!(DITA_OT_NS.equals(uri) && "extension".equals(localName))){
				output.write("</");
				output.write(qName);
				output.write(">");
			}
		}catch (final Exception e) {
        	logger.logException(e);
        }
	}

	@Override
	public void ignorableWhitespace(final char[] ch, final int start, final int length) throws SAXException {
		try{
			output.write(ch,start,length);
		}catch (final Exception e) {
        	logger.logException(e);
        }
	}

	@Override
	public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
		IAction action = null;
		String input = null;
		try{
			if(DITA_OT_NS.equals(uri) && "extension".equals(localName)){
				// Element extension: <dita:extension id="extension-point" behavior="classname"/>
				action = (IAction)Class.forName(attributes.getValue("behavior")).newInstance();
				action.addParam(PARAM_TEMPLATE, templateFileName);
				action.addParam("extension", attributes.getValue("id"));
				input = featureTable.get(attributes.getValue("id"));
				if(input!=null){
					action.setFeatures(featureTable);
					action.setInput(input);
					output.write(action.getResult());
				}
			}else{
				final int attLen = attributes.getLength();
				output.write("<");
				output.write(qName);
				for(int i = 0; i < attLen; i++){
					if (DITA_OT_NS.equals(attributes.getURI(i)))
					{
						// Attribute extension: <element dita:extension="localname classname ..." dita:localname="...">
						if (!("extension".equals(attributes.getLocalName(i))))
						{
							final String extensions = attributes.getValue(DITA_OT_NS, "extension");
							final StringTokenizer extensionTokenizer = new StringTokenizer(extensions);
							// Get the classname that implements this localname.
							while (extensionTokenizer.hasMoreTokens())
							{
								final String thisExtension = extensionTokenizer.nextToken();
								final String thisExtensionClass = extensionTokenizer.nextToken();
								if (thisExtension.equals(attributes.getLocalName(i)))
								{
									action = (IAction)Class.forName(thisExtensionClass).newInstance();
									break;
								}
							}
							action.setFeatures(featureTable);
							action.addParam(PARAM_TEMPLATE, templateFileName);
							action.addParam(PARAM_LOCALNAME, attributes.getLocalName(i));
							action.setInput(attributes.getValue(i));
							output.write(action.getResult());
						}
					}
					else if (attributes.getQName(i).startsWith("xmlns:") &&
							DITA_OT_NS.equals(attributes.getValue(i)))
					{
						// Ignore xmlns:dita.
					}
					else
					{
						// Normal attribute.
						output.write(" ");
						output.write(new StringBuffer(attributes.getQName(i)).append("=\"").
								append(StringUtils.escapeXML(attributes.getValue(i))).append("\"").toString());
					}
				}
				output.write(">");
			}
		}catch(final Exception e){
			logger.logException(e);
		}
	}

	@Override
	public void endDocument() throws SAXException {
		try{
			output.flush();
			output.close();
		}catch(final Exception e){
			logger.logException(e);
		}
		
	}
	@Override
	public void skippedEntity(final String name) throws SAXException {
		logger.logError("Skipped entity " + name);
	}
	
	@Override
	public void comment(final char[] ch, final int start, final int length) throws SAXException {
		try{
    		output.write("<!--");
    		output.write(ch, start, length);
    		output.write("-->");
		}catch(final Exception e){
			logger.logException(e);
		}
	}
	
}
