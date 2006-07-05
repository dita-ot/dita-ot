/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.reader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.dita.dost.util.CatalogUtils;
import org.dita.dost.util.Constants;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.FilterUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
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

	/** Map of XML catalog info */
	private static HashMap catalogMap = null;

	/** Basedir of the current parsing file */
	private String currentDir = null;

	/** Flag for conref in parsing file */
	private boolean hasConRef = false;

	/** Flag for href in parsing file */
	private boolean hasHref = false;

	/** List of the parsing result */
	private List result = null;

	/** List of href targets refered in current parsing file */
	private List hrefTargets = null;

	/** Flag used to mark if parsing entered into excluded element */
	private boolean insideExcludedElement = false;
	
	/** Used to record the excluded level */
	private int excludedLevel = 0;
	
	/** Flag used to mark if current file is still valid after filtering */
	private boolean isValidInput = false;
	
	private String props; // contains the attribution specialization from props
	
	/**
	 * Constructor
	 */
	public GenListModuleReader() {
		result = new ArrayList(Constants.INT_64);
		hrefTargets = new ArrayList(Constants.INT_32);
		reader.setContentHandler(this);
		reader.setEntityResolver(this);
	}

	/**
	 * Init xml reader used for pipeline parsing.
	 * 
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 */
	public static void initXMLReader(String ditaDir) throws SAXException {
		if (System.getProperty(Constants.SAX_DRIVER_PROPERTY) == null) {
			// The default sax driver is set to xerces's sax driver
			System.setProperty(Constants.SAX_DRIVER_PROPERTY,
					Constants.SAX_DRIVER_DEFAULT_CLASS);
		}

		reader = XMLReaderFactory.createXMLReader();
		reader.setFeature(Constants.FEATURE_NAMESPACE_PREFIX, true);
		reader.setFeature(Constants.FEATURE_VALIDATION, true);
		reader.setFeature(Constants.FEATURE_VALIDATION_SCHEMA, true);
		catalogMap = CatalogUtils.getCatalog(ditaDir);
	}

	public void reset() {
		hasConRef = false;
		hasHref = false;
		currentDir = null;
		insideExcludedElement = false;
		excludedLevel = 0;
		isValidInput = false;
		result.clear();
		hrefTargets.clear();
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
	 * Get the parsing result.
	 * 
	 * @return Returns the result.
	 */
	public List getResult() {
		return result;
	}

	/**
	 * Get the href target.
	 * 
	 * @return Returns the hrefTargets.
	 */
	public List getHrefTargets() {
		return hrefTargets;
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
        int propsStart;
        int propsEnd;
        String attrValue = atts.getValue(Constants.ATTRIBUTE_NAME_CLASS);
		
		if (attrValue != null && attrValue.indexOf(Constants.ATTR_CLASS_VALUE_TOPIC) != -1){
        	domains = atts.getValue(Constants.ATTRIBUTE_NAME_DOMAINS);
        	propsStart = domains.indexOf("(props");
        	propsEnd = domains.indexOf(")",propsStart);
        	if(propsStart != -1 && propsEnd != -1){
        		props = domains.substring(propsStart+6,propsEnd).trim();
        	}else{
        		props = null;
        	}
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
		}

		return null;
	}

	/*
	 * Parse the input attributes for needed information.
	 */
	private void parseAttribute(Attributes atts, String attrName) {
		String attrValue = atts.getValue(attrName);
		String filename = null;
		String attrScope = atts.getValue("scope");

		if (attrValue == null) {
			return;
		}
		
		if (Constants.ATTRIBUTE_NAME_CONREF.equals(attrName)) {
			hasConRef = true;
		} else if (Constants.ATTRIBUTE_NAME_HREF.equals(attrName)) {
			hasHref = true;
		}

		if (attrValue.startsWith(Constants.SHARP)
				|| attrValue.indexOf(Constants.COLON_DOUBLE_SLASH) != -1
				|| "external".equalsIgnoreCase(attrScope)
				|| "peer".equalsIgnoreCase(attrScope)) {
			return;
		}

		filename = FileUtils.normalizeDirectory(currentDir, attrValue);

		if (FileUtils.isValidTarget(filename)) {
			result.add(filename);
		}

		/*
		 * Collect only href target topic files for index extracting.
		 */
		if (Constants.ATTRIBUTE_NAME_HREF.equals(attrName)
				&& FileUtils.isTopicFile(filename)) {
			hrefTargets.add(new File(filename).getPath());
		}

	}

}