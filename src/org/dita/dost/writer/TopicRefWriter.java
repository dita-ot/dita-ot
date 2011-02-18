/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2007 All Rights Reserved.
 */
package org.dita.dost.writer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Hashtable;
import java.util.LinkedHashMap;
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


/**
 * TopicRefWriter which updates the linking elements' value according to the mapping table.
 * @author wxzhang
 * 
 */
public class TopicRefWriter extends AbstractXMLWriter {

	// To check the URL of href in topicref attribute
	private static final String NOT_LOCAL_URL = "://";

	private LinkedHashMap<String, String> changeTable = null;
	private Hashtable<String, String> conflictTable = null;
	private DITAOTJavaLogger logger = null;
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
		logger = new DITAOTJavaLogger();
		
        try {
            reader = StringUtils.getXMLReader();
            reader.setContentHandler(this);
            reader.setProperty(Constants.LEXICAL_HANDLER_PROPERTY,this);
            reader.setFeature(Constants.FEATURE_NAMESPACE_PREFIX, true);
            //Edited by william on 2009-11-8 for ampbug:2893664 start
			reader.setFeature("http://apache.org/xml/features/scanner/notify-char-refs", true);
			reader.setFeature("http://apache.org/xml/features/scanner/notify-builtin-refs", true);
			//Edited by william on 2009-11-8 for ampbug:2893664 end
        } catch (Exception e) {
        	logger.logException(e);
        }
	}
	/**
	 * Set up class.
	 * @param conflictTable conflictTable
	 */
	public void setup(Hashtable<String,String> conflictTable) {
		this.conflictTable = conflictTable;
	}


	@Override
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

	@Override
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

	@Override
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
	@Override
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
	@Override
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
	@Override
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
	@Override
	public void setContent(Content content) {
		changeTable = (LinkedHashMap<String,String>) content.getValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.dita.dost.writer.AbstractXMLWriter#startCDATA()
	 */
	@Override
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
	@Override
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
	@Override
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
			// Added on 20110125 for bug:Chunking remaps in-file <xref> to
			// invalid value - ID: 3162808 start
			String changeTargetkey = FileUtils.resolveFile(currentFilePath,
					attValue);
			String changeTarget = (String) changeTable.get(changeTargetkey);
 
			final int sharpIndex = attValue.lastIndexOf(Constants.SHARP);
			if (sharpIndex != -1) {
				final int slashIndex = attValue.indexOf(Constants.SLASH,
						sharpIndex);
				if (slashIndex != -1) {
					changeTargetkey = changeTargetkey
							+ attValue.substring(sharpIndex, slashIndex);
				} else {
					changeTargetkey = changeTargetkey
							+ attValue.substring(sharpIndex);
				}
				String changeTarget_with_elemt = (String) changeTable
						.get(changeTargetkey);
				if (changeTarget_with_elemt != null) {
					changeTarget = changeTarget_with_elemt;
				}
			}				
			
			// Added on 20110125 for bug:Chunking remaps in-file <xref> to invalid value - ID: 3162808   end 
			String elementID=getElementID(attValue);
			String pathtoElem = 
				attValue.contains(Constants.SHARP) ? attValue.substring(attValue.indexOf(Constants.SHARP)+1) : "";
			
			if (StringUtils.isEmptyString(changeTarget)) {
				String absolutePath = FileUtils.resolveTopic(currentFilePath, attValue);
				if (absolutePath.contains(Constants.SHARP) &&
						absolutePath.substring(absolutePath.indexOf(Constants.SHARP)).contains(Constants.SLASH)){
					absolutePath = absolutePath.substring(0, absolutePath.indexOf(Constants.SLASH, absolutePath.indexOf(Constants.SHARP)));
				}
				changeTarget = (String)changeTable.get(absolutePath);
			}
			if(!notTopicFormat(atts,attValue)){
				if(changeTarget == null) {
					return attValue;//no change
				}else{
					String conTarget = (String)conflictTable.get(removeAnchor(changeTarget));
					if (!StringUtils.isEmptyString(conTarget)) {
						if (elementID == null) {
							String idpath = getElementID(changeTarget);
							return FileUtils.getRelativePathFromMap(
									rootPathName, conTarget) + (idpath != null ? Constants.SHARP + idpath : "");
						}else {
							if (conTarget.contains(Constants.SHARP)){
								//conTarget points to topic
								if (!pathtoElem.contains(Constants.SLASH)){
									//if pathtoElem does no have '/' slash. it means elementID is topic id
									return FileUtils.getRelativePathFromMap(
											rootPathName, conTarget);
								}else{
									return FileUtils.getRelativePathFromMap(
											rootPathName, conTarget) + Constants.SLASH + elementID;
								}
								
							}else{
								return FileUtils.getRelativePathFromMap(
										rootPathName, conTarget) + Constants.SHARP + pathtoElem;
							}							
						}
					} else {
						if (elementID == null){
							return FileUtils.getRelativePathFromMap(
									rootPathName, changeTarget);
						}else{
							if (changeTarget.contains(Constants.SHARP)){
								//changeTarget points to topic
								if(!pathtoElem.contains(Constants.SLASH)){
									//if pathtoElem does no have '/' slash. it means elementID is topic id
									return FileUtils.getRelativePathFromMap(
											rootPathName, changeTarget);
								}else{
									return FileUtils.getRelativePathFromMap(
											rootPathName, changeTarget) + Constants.SLASH + elementID;
								}
							}else{
								return FileUtils.getRelativePathFromMap(
										rootPathName, changeTarget) + Constants.SHARP + pathtoElem;
							}
						}						
					}
				}				
			}
		}
		return attValue;
	}
	
	private String removeAnchor(String s) {
		if (s.lastIndexOf(Constants.SHARP) != -1) {
			return s.substring(0, s.lastIndexOf(Constants.SHARP));
		} else {
			return s;
		}
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
			else elementID = topicWithelement;
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
	 * Retrive the extension name from the attribute.
	 * @param attValue attribute value
	 * @return String the extension
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
	 * @param attrs attributes to check
	 * @param valueOfHref href attribute value
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
	@Override
	public void write(String outputFilename) throws DITAOTException {
		String filename = outputFilename;
		String file = null;
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
