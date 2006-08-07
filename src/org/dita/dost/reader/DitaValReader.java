/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.reader;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.module.Content;
import org.dita.dost.module.ContentImpl;
import org.dita.dost.util.Constants;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * DitaValReader reads and parses the information from ditaval file which
 * contains the information of filtering and flagging.
 * 
 * @author Zhang, Yuan Peng
 */
public class DitaValReader extends AbstractXMLReader {
	private HashMap filterMap;

	private ContentImpl content;

	private XMLReader reader;

	private DITAOTJavaLogger logger;

	private List imageList = null;

	private String ditaVal = null;

	/**
	 * Default constructor of DitaValReader class.
	 */
	public DitaValReader() {
		super();
		filterMap = new HashMap();
		content = null;
		logger = new DITAOTJavaLogger();
		imageList = new ArrayList(Constants.INT_256);

		try {
			if (System.getProperty(Constants.SAX_DRIVER_PROPERTY) == null) {
				// The default sax driver is set to xerces's sax driver
				System.setProperty(Constants.SAX_DRIVER_PROPERTY,
						Constants.SAX_DRIVER_DEFAULT_CLASS);
			}
			reader = XMLReaderFactory.createXMLReader();
			reader.setContentHandler(this);
		} catch (Exception e) {
			logger.logException(e);
		}

	}

	/**
	 * @see org.dita.dost.reader.AbstractReader#read(java.lang.String)
	 */
	public void read(String input) {
		ditaVal = input;

		try {
			reader.parse(new InputSource(new FileInputStream(input)));
		} catch (Exception e) {
			logger.logException(e);
		}
	}

	/**
	 * @see org.dita.dost.reader.AbstractReader#getContent()
	 */
	public Content getContent() {
		content = new ContentImpl();
		content.setCollection(filterMap.entrySet());
		return content;
	}

	/**
	 * @see org.xml.sax.ContentHandler#startElement(java.lang.String,
	 *      java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 * 
	 */
	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		String flagImage = atts.getValue(Constants.ATTRIBUTE_NAME_IMG);

		if (Constants.ELEMENT_NAME_PROP.equals(qName)) {
			String action = atts.getValue(Constants.ELEMENT_NAME_ACTION);
			String attName = atts.getValue(Constants.ATTRIBUTE_NAME_ATT);
			String attValue = atts.getValue(Constants.ATTRIBUTE_NAME_VAL);
			String key = attName + Constants.EQUAL + attValue;

			if (action != null) {
				if (filterMap.get(key) == null) {
					filterMap.put(key, action);
				} else {
					Properties prop = new Properties();
					prop.put("%1", key);
					logger.logError(MessageUtils.getMessage("DOTJ007E", prop)
							.toString());
				}
			}
		}

		/*
		 * Parse image files for flagging
		 */
		if (flagImage != null && !"".equals(flagImage.trim())) {
			if (new File(flagImage).isAbsolute()) {
				imageList.add(flagImage);
				return;
			}

			// img is a relative path to the .ditaval file
			String filterDir = new File(new File(ditaVal).getAbsolutePath())
					.getParent();
			imageList.add(new File(filterDir, flagImage).getAbsolutePath());
		}
	}

	public List getImageList() {
		return imageList;
	}
	
	public HashMap getFilterMap() {
		return filterMap;
	}
}
