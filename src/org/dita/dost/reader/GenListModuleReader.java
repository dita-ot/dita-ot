/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.util.CatalogUtils;
import org.dita.dost.util.Constants;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.FilterUtils;
import org.dita.dost.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;

import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * This class extends AbstractReader, used to parse relevant dita topics 
 * and ditamap files for GenMapAndTopicListModule.
 * 
 * @version 1.0 2004-11-25
 * 
 * @author Wu, Zhi Qiang
 */
public class GenListModuleReader extends AbstractXMLReader {
	/** XMLReader instance for parsing dita file */
	private static XMLReader reader = null;

	private static SAXParser parser = null;
	
	/** Map of XML catalog info */
	private static HashMap catalogMap = null;

	/** Basedir of the current parsing file */
	private String currentDir = null;

	/** Flag for conref in parsing file */
	private boolean hasConRef = false;

	/** Flag for href in parsing file */
	private boolean hasHref = false;

	/** Set of all the non-conref and non-copyto targets refered in current parsing file */
	private Set nonConrefCopytoTargets = null;
	
	/** Set of conref targets refered in current parsing file */
	private Set conrefTargets = null;
	
	/** Set of href nonConrefCopytoTargets refered in current parsing file */
	private Set hrefTargets = null;
	
	/** Set of subsidiary files */
	private Set subsidiarySet = null;

	/** Set of sources of those copy-to that were ignored */
	private Set ignoredCopytoSourceSet = null;
	
	/** Map of copy-to target to souce	*/
	private Map copytoMap = null;
	
	/** Flag used to mark if parsing entered into excluded element */
	private boolean insideExcludedElement = false;
	
	/** Used to record the excluded level */
	private int excludedLevel = 0;
	
	/** Flag used to mark if current file is still valid after filtering */
	private boolean isValidInput = false;
	
	private String props; // contains the attribution specialization from props
	
	private DITAOTJavaLogger javaLogger = new DITAOTJavaLogger();
	
	private String ditaDir = null;
	//private static GenListModuleReader parserInstance = new GenListModuleReader();
	
	/**
	 * Constructor
	 */
	public GenListModuleReader() {
		Class c = null;
		nonConrefCopytoTargets = new HashSet(Constants.INT_64);
		hrefTargets = new HashSet(Constants.INT_32);
		conrefTargets = new HashSet(Constants.INT_32);
		copytoMap = new HashMap(Constants.INT_16);
		subsidiarySet = new HashSet(Constants.INT_16);
		ignoredCopytoSourceSet = new HashSet(Constants.INT_16);
		props = null;
		reader.setContentHandler(this);
		try {
			reader.setProperty(Constants.LEXICAL_HANDLER_PROPERTY,this);
		} catch (SAXNotRecognizedException e1) {
			javaLogger.logException(e1);
		} catch (SAXNotSupportedException e1) {
			javaLogger.logException(e1);
		}
		
		try {
			c = Class.forName(Constants.RESOLVER_CLASS);
			reader.setEntityResolver(CatalogUtils.getCatalogResolver());
		}catch (ClassNotFoundException e){
			reader.setEntityResolver(this);
		}
		
		//System.out.println(reader.getEntityResolver());
	}

	/**
     * Init xml reader used for pipeline parsing.
	 *
     * @throws SAXException
     * @param ditaDir 
     */
	public static void initXMLReader(String ditaDir) throws SAXException {
		DITAOTJavaLogger javaLogger=new DITAOTJavaLogger();
		if (System.getProperty(Constants.SAX_DRIVER_PROPERTY) == null) {
			// The default sax driver is set to xerces's sax driver
			StringUtils.initSaxDriver();
		}
		

		reader = XMLReaderFactory.createXMLReader();
		reader.setFeature(Constants.FEATURE_NAMESPACE_PREFIX, true);
		reader.setFeature(Constants.FEATURE_VALIDATION, true);
		reader.setFeature(Constants.FEATURE_VALIDATION_SCHEMA, true);
		
		CatalogUtils.initCatalogResolver(ditaDir);
		catalogMap = CatalogUtils.getCatalog(ditaDir);
	}

	/**
	 * 
	 * Reset the internal variables
	 */
    public void reset() {
		hasConRef = false;
		hasHref = false;
		currentDir = null;
		insideExcludedElement = false;
		excludedLevel = 0;
		isValidInput = false;
		nonConrefCopytoTargets.clear();
		hrefTargets.clear();
		conrefTargets.clear();
		copytoMap.clear();
		ignoredCopytoSourceSet.clear();
	}

	/**
	 * To see if the parsed file has conref inside.
	 * 
	 * @return
	 */
	public boolean hasConRef() {
		return hasConRef;
	}

	/**
	 * To see if the parsed file has href inside.
	 * 
	 * @return
	 */
	public boolean hasHref() {
		return hasHref;
	}

	/**
	 * Get all targets except copy-to.
	 * 
	 * @return Returns allTargets.
	 */
	public Set getNonCopytoResult() {
		Set nonCopytoSet = new HashSet(Constants.INT_128);
		
		nonCopytoSet.addAll(nonConrefCopytoTargets);
		nonCopytoSet.addAll(conrefTargets);
		nonCopytoSet.addAll(copytoMap.values());
		nonCopytoSet.addAll(ignoredCopytoSourceSet);
		
		return nonCopytoSet;
	}

	/**
	 * Get the href target.
	 * 
	 * @return Returns the hrefTargets.
	 */
	public Set getHrefTargets() {
		return hrefTargets;
	}
	
	/**
	 * Get conref targets.
	 * 
	 * @return Returns the conrefTargets.
	 */
	public Set getConrefTargets() {
		return conrefTargets;
	}
	
	/**
	 * Get subsidiary targets.
	 * 
	 * @return Returns the subsidiarySet.
	 */
	public Set getSubsidiaryTargets() {
		return subsidiarySet;
	}
	
	/**
	 * Get non-conref and non-copyto targets.
	 * 
	 * @return Returns the nonConrefCopytoTargets.
	 */
	public Set getNonConrefCopytoTargets() {
		return nonConrefCopytoTargets;
	}
	
	/**
     * Returns the ignoredCopytoSourceSet
     *
     * @return Returns the ignoredCopytoSourceSet.
     */
	public Set getIgnoredCopytoSourceSet() {
		return ignoredCopytoSourceSet;
	}

	/**
	 * Get the copy-to map.
	 * 
	 * @return
	 */
	public Map getCopytoMap() {
		return copytoMap;
	}
	
	/**
	 * Set the relative directory of current file.
	 * 
	 * @param dir
	 */
	public void setCurrentDir(String dir) {
		this.currentDir = dir;
	}

	/**
	 * Check if the current file is valid after filtering.
	 * 
	 * @return
	 */
	public boolean isValidInput() {
		return isValidInput;
	}
	
	/**
	 * Parse input xml file.
	 * 
	 * @param file
	 * @throws SAXException 
	 * @throws IOException 
	 * @throws FileNotFoundException
	 */
	public void parse(File file) throws FileNotFoundException, IOException, SAXException {	
		reader.parse(new InputSource(new FileInputStream(file)));		
	}

	/**
	 * Parse specific attributes for info used in later process.
	 * 
	 * @param uri
	 * @param localName
	 * @param qName
	 * @param atts
	 * 
	 * @exception org.xml.sax.SAXException
	 */
	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		String domains = null;
		Properties params = new Properties();
		String msg = null;
        String attrValue = atts.getValue(Constants.ATTRIBUTE_NAME_CLASS);
		if(attrValue==null){
    		params.clear();
			msg = null;
			params.put("%1", localName);
    		javaLogger.logInfo(MessageUtils.getMessage("DOTJ030I", params).toString());			
		}
        if (attrValue != null && attrValue.indexOf(Constants.ATTR_CLASS_VALUE_TOPIC) != -1){
        	domains = atts.getValue(Constants.ATTRIBUTE_NAME_DOMAINS);
        	if(domains==null){
        		params.clear();
				msg = null;
				params.put("%1", localName);
        		javaLogger.logInfo(MessageUtils.getMessage("DOTJ029I", params).toString());
        	}else
        		props = StringUtils.getExtProps(domains);
        }
		
		if (insideExcludedElement) {
			++excludedLevel;
			return;
		}
		
		// Ignore element that has been filtered out.
		if (FilterUtils.needExclude(atts, props)) {
			insideExcludedElement = true;
			++excludedLevel;
			return;
		}
		
		/* 
		 * For ditamap, set it to valid if element <map> or extended from 
		 * <map> was found, this kind of element's class attribute must 
		 * contains 'map/map';
		 * For topic files, set it to valid if element <title> or extended 
		 * from <title> was found, this kind of element's class attribute 
		 * must contains 'topic/title'.
		 */
		
		if (attrValue != null) {
			if ((attrValue.indexOf(Constants.ATTR_CLASS_VALUE_MAP) != -1)
					|| (attrValue.indexOf(Constants.ATTR_CLASS_VALUE_TITLE) != -1)) {
				isValidInput = true;
			}else if (attrValue.indexOf(Constants.ATTR_CLASS_VALUE_OBJECT) != -1){
				parseAttribute(atts, Constants.ATTRIBUTE_NAME_DATA);
			}
		}
		
		parseAttribute(atts, Constants.ATTRIBUTE_NAME_CONREF);
		parseAttribute(atts, Constants.ATTRIBUTE_NAME_HREF);
		parseAttribute(atts, Constants.ATTRIBUTE_NAME_COPY_TO);
		parseAttribute(atts, Constants.ATTRIBUTE_NAME_IMG);
	}

	/** (non-Javadoc)
	 * @see org.dita.dost.reader.AbstractXMLReader#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void endElement(String uri, String localName, String qName) throws SAXException {		
		if (insideExcludedElement) {
			// end of the excluded element, mark the flag as false 
			if (excludedLevel == 1) {
				insideExcludedElement = false;
			}
			--excludedLevel;
		}
	}

	/**
	 * Resolve the publicId used in XMLCatalog.
	 * 
	 * @param publicId
	 * @param systemId
	 * @throws java.io.IOException
	 * @exception org.xml.sax.SAXException
	 */
	public InputSource resolveEntity(String publicId, String systemId)
			throws SAXException, IOException {
		if (catalogMap.get(publicId) != null) {
			File dtdFile = new File((String) catalogMap.get(publicId));
			return new InputSource(dtdFile.getAbsolutePath());
		}else if (catalogMap.get(systemId) != null){
			File schemaFile = new File((String) catalogMap.get(systemId));
			return new InputSource(schemaFile.getAbsolutePath());
		}

		return null;
	}

	/*
	 * Parse the input attributes for needed information.
	 */
	private void parseAttribute(Attributes atts, String attrName) {
		String attrValue = atts.getValue(attrName);
		String filename = null;
		String attrScope = atts.getValue(Constants.ATTRIBUTE_NAME_SCOPE);
		String attrFormat = atts.getValue(Constants.ATTRIBUTE_NAME_FORMAT);
		String attrType = atts.getValue(Constants.ATTRIBUTE_NAME_TYPE);

		if (attrValue == null) {
			return;
		}
		
		if (Constants.ATTRIBUTE_NAME_CONREF.equals(attrName)) {
			hasConRef = true;
		} else if (Constants.ATTRIBUTE_NAME_HREF.equals(attrName)) {
			hasHref = true;
		}
		
		

		if (attrValue.startsWith(Constants.SHARP)
				|| attrValue.indexOf(Constants.COLON_DOUBLE_SLASH) != -1){
			return;
		}
		if ("external".equalsIgnoreCase(attrScope)
				|| "peer".equalsIgnoreCase(attrScope)) {
			return;
		}

		filename = FileUtils.normalizeDirectory(currentDir, attrValue);
		try{
			filename = URLDecoder.decode(filename, Constants.UTF8);
		}catch(UnsupportedEncodingException e){
			
		}
		
		if ("DITA-foreign".equals(attrType) &&
				Constants.ATTRIBUTE_NAME_DATA.equals(attrName)){
			subsidiarySet.add(filename);
			return;
		}

		/*
		 * Collect non-conref and non-copyto targets
		 */
		if (FileUtils.isValidTarget(filename.toLowerCase()) && 
				(StringUtils.isEmptyString(atts.getValue(Constants.ATTRIBUTE_NAME_COPY_TO)) ||
						!FileUtils.isTopicFile(atts.getValue(Constants.ATTRIBUTE_NAME_COPY_TO).toLowerCase()))
				&& !Constants.ATTRIBUTE_NAME_CONREF.equals(attrName)
				&& !Constants.ATTRIBUTE_NAME_COPY_TO.equals(attrName)) {
			nonConrefCopytoTargets.add(filename);
		}
		
		if (attrFormat != null &&
				!Constants.ATTR_FORMAT_VALUE_DITA.equalsIgnoreCase(attrFormat)){
			//The format of the href is not dita topic
			//The logic after this "if" clause is not related to files other than dita topic.
			//Therefore, we need to return here to filter out those files in other format.
			return;
		}

		/*
		 * Collect only href target topic files for index extracting.
		 */
		if (Constants.ATTRIBUTE_NAME_HREF.equals(attrName)
				&& FileUtils.isTopicFile(filename)) {
			hrefTargets.add(new File(filename).getPath());
		}
		
		/*
		 * Collect only conref target topic files.
		 */
		if (Constants.ATTRIBUTE_NAME_CONREF.equals(attrName)
				&& FileUtils.isDITAFile(filename)) {
			conrefTargets.add(filename);
		}
		
		// Collect copy-to (target,source) into hash map
		if (Constants.ATTRIBUTE_NAME_COPY_TO.equals(attrName)
				&& FileUtils.isTopicFile(filename)) {
			String href = atts.getValue(Constants.ATTRIBUTE_NAME_HREF);
			
			if (StringUtils.isEmptyString(href)) {
				StringBuffer buff = new StringBuffer();
				buff.append("Copy-to task [href=\"\" copy-to=\"");
				buff.append(filename);
				buff.append("\"] was ignored.");
				javaLogger.logWarn(buff.toString());
			} else if (copytoMap.get(filename) != null){
				StringBuffer buff = new StringBuffer();
				buff.append("Copy-to task [href=\"");
				buff.append(href);
				buff.append("\" copy-to=\"");
				buff.append(filename);
				buff.append("\"] which points to another copy-to target");
				buff.append(" was ignored.");
				javaLogger.logWarn(buff.toString());
				ignoredCopytoSourceSet.add(href);
			} else {
				copytoMap.put(filename, FileUtils.normalizeDirectory(currentDir, href));
			}
				
		}
	}
}