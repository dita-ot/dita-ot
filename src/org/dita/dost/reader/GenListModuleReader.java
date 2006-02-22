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
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * This class extends AbstractReader, used to parse relevant targets for
 * GenMapAndTopicListModule.
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
		parseAttribute(atts, Constants.ATTRIBUTE_NAME_CONREF);
		parseAttribute(atts, Constants.ATTRIBUTE_NAME_HREF);
		parseAttribute(atts, Constants.ATTRIBUTE_NAME_COPY_TO);
		parseAttribute(atts, Constants.ATTRIBUTE_NAME_IMG);
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

		if (attrValue == null) {
			return;
		}

		if (Constants.ATTRIBUTE_NAME_CONREF.equals(attrName)) {
			hasConRef = true;
		} else if (Constants.ATTRIBUTE_NAME_HREF.equals(attrName)) {
			hasHref = true;
		}

		if (attrValue.startsWith(Constants.SHARP)
				|| attrValue.indexOf(Constants.COLON_DOUBLE_SLASH) != -1) {
			return;
		}

		filename = normalizeDirectory(attrValue);

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

	/*
	 * Normalize the file directory, replace all the '\\', '/' with
	 * File.seperator, and remove '..' from the directory.
	 */
	private String normalizeDirectory(String dir) {
		String normilizedPath = null;
		int index = dir.indexOf(Constants.SHARP);
		String pathname = (index == -1) ? dir : dir.substring(0, index);

		/*
		 * Normilize file path using java.io.File
		 */
		normilizedPath = new File(currentDir, pathname).getPath();

		if (currentDir == null || currentDir.length() == 0) {
			return normilizedPath;
		}

		return FileUtils.removeRedundantNames(normilizedPath);
	}
}