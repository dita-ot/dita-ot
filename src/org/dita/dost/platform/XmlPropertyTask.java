/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2007 All Rights Reserved.
 */
package org.dita.dost.platform;

import org.apache.tools.ant.Task;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.FileInputStream;
import java.util.Properties;
import java.io.InputStream;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.XMLCatalog;
import org.apache.tools.ant.util.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
/**
 * @author wxzhang
 *
 */
public class XmlPropertyTask extends Task {

	/**
	 * 
	 */
	public XmlPropertyTask() {
		super();
	}
	private File src;
	private String prefix = "";
	private boolean validate = false;
	private boolean collapseAttributes = false;
	private File rootDirectory = null;
	private FileUtils fileUtils = FileUtils.getFileUtils();
	private XMLCatalog xmlCatalog = new XMLCatalog();
	private Properties props = new Properties();
	
    // XML loading and saving methods for Properties

    // The required DTD URI for exported properties
    private static final String PROPS_DTD_URI =
    "http://java.sun.com/dtd/properties.dtd";

    private static final String PROPS_DTD =
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
    "<!-- DTD for properties -->"                +
    "<!ELEMENT properties ( comment?, entry* ) >"+
    "<!ATTLIST properties"                       +
        " version CDATA #FIXED \"1.0\">"         +
    "<!ELEMENT comment (#PCDATA) >"              +
    "<!ELEMENT entry (#PCDATA) >"                +
    "<!ATTLIST entry "                           +
        " key CDATA #REQUIRED>";

    /**
     * Version number for the format of exported properties files.
     */
    private static final String EXTERNAL_XML_VERSION = "1.0";


	/**
	 * Initializes the task.
	 */

	public void init() {
		super.init();
		xmlCatalog.setProject(getProject());
	}

	/**
	 * @return the xmlCatalog as the entityresolver.
	 */
	protected EntityResolver getEntityResolver() {
		return xmlCatalog;
	}

	/**
	 * Run the task.
	 * @throws BuildException The exception raised during task execution.
	 * @todo validate the source file is valid before opening, print a better error message
	 * @todo add a verbose level log message listing the name of the file being loaded
	 */
	public void execute() throws BuildException {

		if (getFile() == null) {
			String msg = "XmlProperty task requires a file attribute";
			throw new BuildException(msg);
		}

		try {
			log("Loading " + src.getAbsolutePath(), Project.MSG_VERBOSE);

			if (src.exists()) {
				load(new FileInputStream(src));
			} else {
				log("Unable to find property file: " + src.getAbsolutePath(),
						Project.MSG_VERBOSE);
			}
		}catch(SAXException se){
			throw new BuildException(se);
		} catch (IOException ioe) {
			// I/O error
			throw new BuildException(ioe);
		}
	}

	private void load( InputStream in) throws IOException,SAXException{
        Document doc = null;
        doc = getLoadingDoc(in);
        Element propertiesElement = (Element)doc.getChildNodes().item(1);
        String xmlVersion = propertiesElement.getAttribute("version");
        if (xmlVersion.compareTo(EXTERNAL_XML_VERSION) > 0)
            throw new SAXException(
                "Exported Properties file format version " + xmlVersion +
                " is not supported. This java installation can read" +
                " versions " + EXTERNAL_XML_VERSION + " or older. You" +
                " may need to install a newer version of JDK.");
        NodeList entries = propertiesElement.getChildNodes();
        int numEntries = entries.getLength();
        int start = numEntries > 0 && 
            entries.item(0).getNodeName().equals("comment") ? 1 : 0;
        for (int i=start; i<numEntries; i++) {
            Element entry = (Element)entries.item(i);
            if (entry.hasAttribute("key")) {
                Node n = entry.getFirstChild();
                String val = (n == null) ? "" : n.getNodeValue();
                props.setProperty(entry.getAttribute("key"), val);
                addProperty(entry.getAttribute("key"),val,null);
            }
        }

	}

	static Document getLoadingDoc(InputStream in) throws SAXException,
			IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setIgnoringElementContentWhitespace(true);
		dbf.setValidating(true);
		dbf.setCoalescing(true);
		dbf.setIgnoringComments(true);
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			db.setEntityResolver(new Resolver());
			db.setErrorHandler(new EH());
			InputSource is = new InputSource(in);
			return db.parse(is);
		} catch (ParserConfigurationException x) {
			throw new Error(x);
		}
	}
    private static class Resolver implements EntityResolver {
        public InputSource resolveEntity(String pid, String sid)
            throws SAXException
        {
            if (sid.equals(PROPS_DTD_URI)) {
                InputSource is;
                is = new InputSource(new StringReader(PROPS_DTD));
                is.setSystemId(PROPS_DTD_URI);
                return is;
            }
            throw new SAXException("Invalid system identifier: " + sid);
        }
    }

    private static class EH implements ErrorHandler {
        public void error(SAXParseException x) throws SAXException {
            throw x;
        }
        public void fatalError(SAXParseException x) throws SAXException {
            throw x;
        }
        public void warning(SAXParseException x) throws SAXException {
            throw x;
        }
    }
	/**
	 * Actually add the given property/value to the project
	 * after writing a log message.
	 */
	private void addProperty(String name, String value, String id) {
		String msg = name + ":" + value;
		if (id != null) {
			msg += ("(id=" + id + ")");
		}
		log(msg, Project.MSG_DEBUG);
		getProject().setProperty(name, value);
		if (id != null) {
			getProject().addReference(id, value);
		}
	}

	/**
	 * The XML file to parse; required.
	 * @param src the file to parse
	 */
	public void setFile(File src) {
		this.src = src;
	}

	/**
	 * the prefix to prepend to each property
	 * @param prefix the prefix to prepend to each property
	 */
	public void setPrefix(String prefix) {
		this.prefix = prefix.trim();
	}

	/**
	 * flag to validate the XML file; optional, default false
	 * @param validate if true validate the XML file, default false
	 */
	public void setValidate(boolean validate) {
		this.validate = validate;
	}

	/**
	 * The directory to use for resolving file references.
	 * Ignored if semanticAttributes is not set to true.
	 * @param rootDirectory the directory.
	 */
	public void setRootDirectory(File rootDirectory) {
		this.rootDirectory = rootDirectory;
	}

	/**
	 * add an XMLCatalog as a nested element; optional.
	 * @param catalog the XMLCatalog to use
	 */
	public void addConfiguredXMLCatalog(XMLCatalog catalog) {
		xmlCatalog.addConfiguredXMLCatalog(catalog);
	}

	/* Expose members for extensibility */

	/**
	 * @return the file attribute.
	 */
	protected File getFile() {
		return this.src;
	}

	/**
	 * @return the prefix attribute.
	 */
	protected String getPrefix() {
		return this.prefix;
	}

	/**
	 * @return the validate attribute.
	 */
	protected boolean getValidate() {
		return this.validate;
	}

	/**
	 * @return the collapse attributes attribute.
	 */
	protected boolean getCollapseAttributes() {
		return this.collapseAttributes;
	}

	/**
	 * @return the root directory attribute.
	 */
	protected File getRootDirectory() {
		return this.rootDirectory;
	}

	/**
	 * Let project resolve the file - or do it ourselves if
	 * rootDirectory has been set.
	 */
	private File resolveFile(String fileName) {
		if (rootDirectory == null) {
			return getProject().resolveFile(fileName);
		}
		return fileUtils.resolveFile(rootDirectory, fileName);
	}

}