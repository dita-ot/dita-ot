/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2007 All Rights Reserved.
 */
package org.dita.dost.writer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Properties;
import java.io.File;

import org.dita.dost.util.Constants;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.exception.DITAOTXMLErrorHandler;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.module.Content;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.StringUtils;
import org.dita.dost.log.DITAOTJavaLogger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * TopicRefWriter which updates the linking elements' value according to the mapping table
 * @author wxzhang
 * 
 */
public class TopicRefWriter extends AbstractXMLWriter {

	// To check the URL of href in topicref attribute
	private static final String NOT_LOCAL_URL = "://";

	private Hashtable changeTable = null;
	private DITAOTJavaLogger logger = new DITAOTJavaLogger();
	private OutputStreamWriter output;
	private OutputStreamWriter ditaFileOutput;
	private boolean needResolveEntity;
	private boolean insideCDATA;
	private String currentFilePath = null;
	private String currentFilePathName=null;
	/** XMLReader instance for parsing dita file */
	private  XMLReader reader = null;

	/**
	 * 
	 */
	public TopicRefWriter() {
		super();
		output = null;
		logger = null;
		insideCDATA = false;
		
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


	/**
	 * @see org.xml.sax.ext.LexicalHandler#startEntity(java.lang.String)
	 * 
	 */
	public void startEntity(String name) throws SAXException {
		try {
			needResolveEntity = StringUtils.checkEntity(name);
			if (!needResolveEntity) {
				output.write(StringUtils.getEntity(name));
			}
		} catch (Exception e) {
			logger.logException(e);
		}

	}

	/**
	 * @see org.xml.sax.ContentHandler#processingInstruction(java.lang.String,
	 *      java.lang.String)
	 * 
	 */
	public void processingInstruction(String target, String data)
			throws SAXException {
		String pi;
		try {
			pi = (data != null) ? target + Constants.STRING_BLANK + data
					: target;
			output.write(Constants.LESS_THAN + Constants.QUESTION + pi
					+ Constants.QUESTION + Constants.GREATER_THAN);
		} catch (Exception e) {
			logger.logException(e);
		}
	}

	/**
	 * @see org.xml.sax.ContentHandler#ignorableWhitespace(char[], int, int)
	 * 
	 */
	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {
		try {
			output.write(ch, start, length);
		} catch (Exception e) {
			logger.logException(e);
		}
	}

	/**
	 * @see org.xml.sax.ContentHandler#characters(char[], int, int)
	 * 
	 */
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if (needResolveEntity) {
			try {
				if (insideCDATA)
					output.write(ch, start, length);
				else
					output.write(StringUtils.escapeXML(ch, start, length));
			} catch (Exception e) {
				logger.logException(e);
			}
		}
	}

	/**
	 * @see org.xml.sax.ext.LexicalHandler#endEntity(java.lang.String)
	 * 
	 */
	public void endEntity(String name) throws SAXException {
		if (!needResolveEntity) {
			needResolveEntity = true;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dita.dost.writer.AbstractXMLWriter#endCDATA()
	 */

	public void endCDATA() throws SAXException {
		insideCDATA = false;
		try {
			output.write(Constants.CDATA_END);
		} catch (Exception e) {
			logger.logException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dita.dost.writer.AbstractXMLWriter#endDocument()
	 */

	public void endDocument() throws SAXException {
		try {
			output.flush();
		} catch (Exception e) {
			logger.logException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dita.dost.writer.AbstractXMLWriter#endElement(java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */

	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		try {
			output.write(Constants.LESS_THAN + Constants.SLASH + qName
					+ Constants.GREATER_THAN);
		} catch (Exception e) {
			logger.logException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dita.dost.writer.AbstractXMLWriter#setContent(org.dita.dost.module.Content)
	 */

	public void setContent(Content content) {
		changeTable = (Hashtable) content.getValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dita.dost.writer.AbstractXMLWriter#startCDATA()
	 */

	public void startCDATA() throws SAXException {
		try {
			insideCDATA = true;
			output.write(Constants.CDATA_HEAD);
		} catch (Exception e) {
			logger.logException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dita.dost.writer.AbstractXMLWriter#startDocument()
	 */

	public void startDocument() throws SAXException {
		// TODO Auto-generated method stub
		super.startDocument();
		try{
			output.write(Constants.XML_HEAD);
			output.write(Constants.LINE_SEPARATOR);
			}catch(IOException io){
			logger.logException(io);	
        }
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dita.dost.writer.AbstractXMLWriter#startElement(java.lang.String,
	 *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */

	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		
		try {
			copyElementName(qName, atts);
			copyElementAttribute(atts);
			output.write(Constants.GREATER_THAN);
		} catch (Exception e) {
			logger.logException(e);
		}// try
		
	}

	/**
	 * @param attQName
	 * @param attValue
	 * @throws IOException
	 */
	private void copyAttribute(String attQName, String attValue)
			throws IOException {
		output.write(new StringBuffer().append(Constants.STRING_BLANK).append(
				attQName).append(Constants.EQUAL).append(Constants.QUOTATION)
				.append(attValue).append(Constants.QUOTATION).toString());
	}

	/**
	 * @param atts
	 * @throws IOException
	 */
	private void copyElementAttribute(Attributes atts) throws IOException {
		// copy the element's attributes
		int attsLen = atts.getLength();
		for (int i = 0; i < attsLen; i++) {
			String attQName = atts.getQName(i);
			String attValue;

			if (Constants.ATTRIBUTE_NAME_HREF.equals(attQName)) {
				attValue = updateHref(attQName, atts);
			} else {
				attValue = atts.getValue(i);
			}
			// consider whether the attvalue needs to be escaped
			attValue = StringUtils.escapeXML(attValue);
			// output all attributes
			copyAttribute(attQName, attValue);
		}
	}
	
	/**
	 * Check whether the attributes contains references
	 * @param atts
	 * @return true/false
	 */
	private boolean checkDITAHREF(Attributes atts) {

		String classValue = atts.getValue(Constants.ATTRIBUTE_NAME_CLASS);
		String scopeValue = atts.getValue(Constants.ATTRIBUTE_NAME_SCOPE);
		String formatValue = atts.getValue(Constants.ATTRIBUTE_NAME_FORMAT);

		if (classValue == null
				|| (classValue.indexOf(Constants.ATTR_CLASS_VALUE_XREF) == -1
						&& classValue.indexOf(Constants.ATTR_CLASS_VALUE_LINK) == -1 && classValue
						.indexOf(Constants.ATTR_CLASS_VALUE_TOPICREF) == -1)) {
			return false;
		}

		if (scopeValue == null) {
			scopeValue = Constants.ATTR_SCOPE_VALUE_LOCAL;
		}
		if (formatValue == null) {
			formatValue = Constants.ATTR_FORMAT_VALUE_DITA;
		}

		if (scopeValue.equalsIgnoreCase(Constants.ATTR_SCOPE_VALUE_LOCAL)
				&& formatValue.equalsIgnoreCase(Constants.ATTR_FORMAT_VALUE_DITA)) {
			return true;
		}

		return false;
	}

	private String updateHref(String attQName, Attributes atts) {
		String attValue = null;

		if (attQName == null) {
			return null;
		}

		attValue = atts.getValue(attQName);

		if (attValue != null) {
			/*
			 * replace all the backslash with slash in all href and conref
			 * attribute
			 */
			attValue = attValue.replaceAll(Constants.DOUBLE_BACK_SLASH,
					Constants.SLASH);
		} else {
			return null;
		}
		
		if(changeTable==null)
			return attValue;
		
		if (checkDITAHREF(atts)) {
				// replace the href value if it's referenced topic is extracted.
			String rootPathName=currentFilePathName;
			String topicFileWithTopicPathName=(String)changeTable.get(resolveTopicWithoutElement(currentFilePath, attValue));
			String topicFilePathName=(String)changeTable.get(FileUtils.resolveFile(currentFilePath, attValue));
			String elementID=getElementID(attValue);
			if(!notTopicFormat(atts,attValue)){
					if(topicFileWithTopicPathName==null || (!changeTable.containsKey(topicFileWithTopicPathName)
							&&!changeTable.containsKey(topicFilePathName))){
						return attValue;//no change
					}else{
						//chunked file
						if(changeTable.containsKey(topicFileWithTopicPathName)){
							if(rootPathName.equalsIgnoreCase(topicFilePathName)){
								if(attValue.indexOf(Constants.SHARP)!=-1)
									return attValue.substring(attValue.indexOf(Constants.SHARP));
							}
							if (elementID == null)
								return FileUtils.getRelativePathFromMap(
										rootPathName, topicFileWithTopicPathName);
							else
								return new StringBuffer().append(
										FileUtils.getRelativePathFromMap(
												rootPathName,
												topicFileWithTopicPathName))
												.append(Constants.SLASH).append(elementID)
												.toString();
						}
							
						else
							return FileUtils.getRelativePathFromMap(rootPathName,topicFilePathName);
					}				
			}
		}	
		return attValue;
	}
	
	private String resolveTopicWithoutElement(String rootPath, String relativePath){
		String withoutElement=null;
		if(relativePath.indexOf(Constants.SHARP)!=-1)
			if(relativePath.lastIndexOf(Constants.SLASH)!=-1)
				withoutElement=relativePath.substring(0, relativePath.lastIndexOf(Constants.SLASH));
			else
				withoutElement=relativePath;
		else
			withoutElement=relativePath;
		return FileUtils.resolveFile(rootPath,withoutElement);
	}
	/**
	 * Retrieve the element ID from the path
	 * @param relativePath
	 * @return String
	 */
	private String getElementID(String relativePath){
		String elementID=null;
		String topicWithelement=null;
		if(relativePath.indexOf(Constants.SHARP)!=-1){
			topicWithelement=relativePath.substring(relativePath.lastIndexOf(Constants.SHARP)+1);
			if(topicWithelement.lastIndexOf(Constants.SLASH)!=-1)
				elementID=topicWithelement.substring(topicWithelement.lastIndexOf(Constants.SLASH)+1);
		}
		return elementID;
	}
	/**
	 * Check whether it is a local URL
	 * @param valueOfURL
	 * @return boolean
	 */
	private boolean notLocalURL(String valueOfURL) {
		if (valueOfURL.indexOf(NOT_LOCAL_URL) == -1)
			return false;
		else
			return true;
	}
	
	/**
	 * Retrive the extension name from the attribute
	 * @param attValue
	 * @return String
	 */
	public String getExtName(String attValue) {
		String fileName;
		int fileExtIndex;
		int index;

		index = attValue.indexOf(Constants.SHARP);

		if (attValue.startsWith(Constants.SHARP)) {
			return null;
		} else if (index != -1) {
			fileName = attValue.substring(0, index);
			fileExtIndex = fileName.lastIndexOf(Constants.DOT);
			return (fileExtIndex != -1) ? fileName.substring(fileExtIndex + 1,
					fileName.length()) : null;
		} else {
			fileExtIndex = attValue.lastIndexOf(Constants.DOT);
			return (fileExtIndex != -1) ? attValue.substring(fileExtIndex + 1,
					attValue.length()) : null;
		}
	}
	
	/**
	 * Check whether it is a Topic format
	 * @param attrs,valueOfHref
	 * @return boolean
	 */
	private boolean notTopicFormat(Attributes attrs, String valueOfHref) {
		String hrefValue = valueOfHref;
		String formatValue = attrs.getValue(Constants.ATTRIBUTE_NAME_FORMAT);
		String extOfHref = getExtName(valueOfHref);
		if (notLocalURL(hrefValue)) {
			return true;
		} else {
			if (formatValue == null && extOfHref != null
					&& !extOfHref.equalsIgnoreCase("DITA")
					&& !extOfHref.equalsIgnoreCase("XML")) {
				return true;
			}
		}

		return false;
	}


	/**
	 * @param qName
	 * @param atts
	 * @throws IOException
	 */
	private void copyElementName(String qName, Attributes atts)
			throws IOException {
		// copy the element name
		output.write(Constants.LESS_THAN + qName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dita.dost.writer.AbstractXMLWriter#write(java.lang.String)
	 */

	public void write(String outputFilename) throws DITAOTException {
		String filename = outputFilename;
		String file = null;
		String topic = null;
		currentFilePathName=new File(outputFilename).getAbsolutePath();
		currentFilePath = new File(outputFilename).getParent();
		File inputFile = null;
		File outputFile = null;
		FileOutputStream fileOutput = null;
		needResolveEntity=true;

		try {
			if (filename.endsWith(Constants.SHARP)) {
				// prevent the empty topic id causing error
				filename = filename.substring(0, filename.length() - 1);
			}

			if (filename.lastIndexOf(Constants.SHARP) != -1) {
				file = filename.substring(0, filename
						.lastIndexOf(Constants.SHARP));
				topic = filename.substring(filename
						.lastIndexOf(Constants.SHARP) + 1);
			} else {
				file = filename;
			}
			inputFile = new File(file);
			outputFile = new File(file + Constants.FILE_EXTENSION_TEMP);
			fileOutput = new FileOutputStream(outputFile);
			ditaFileOutput = new OutputStreamWriter(fileOutput, Constants.UTF8);
			output = ditaFileOutput;
			reader.setErrorHandler(new DITAOTXMLErrorHandler(file));
			reader.parse(file);

			output.close();
			if (!inputFile.delete()) {
				Properties prop = new Properties();
				prop.put("%1", inputFile.getPath());
				prop.put("%2", outputFile.getPath());
				logger.logError(MessageUtils.getMessage("DOTJ009E", prop)
						.toString());
			}
			if (!outputFile.renameTo(inputFile)) {
				Properties prop = new Properties();
				prop.put("%1", inputFile.getPath());
				prop.put("%2", outputFile.getPath());
				logger.logError(MessageUtils.getMessage("DOTJ009E", prop)
						.toString());
			}
		} catch (Exception e) {
			logger.logException(e);
		} finally {
			try {
				fileOutput.close();
			} catch (Exception e) {
				logger.logException(e);
			}
		}
	}

}
