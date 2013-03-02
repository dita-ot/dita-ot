/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
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
 * Read Java properties file in XML format. The only reason for not using
 * Ant's property task is that it only support reading the XML format
 * when the property file name has the extension {@code .xml}.
 * 
 * @author wxzhang
 */
public final class XmlPropertyTask extends Task {

    /**
     * 
     */
    public XmlPropertyTask() {
        super();
    }
    private File src;
    private String prefix = "";
    private boolean validate = false;
    private final boolean collapseAttributes = false;
    private File rootDirectory = null;
    private final FileUtils fileUtils = FileUtils.getFileUtils();
    private final XMLCatalog xmlCatalog = new XMLCatalog();
    private final Properties props = new Properties();

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

    @Override
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
     * 
     * TODO: validate the source file is valid before opening, print a better error message
     * TODO: add a verbose level log message listing the name of the file being loaded
     * 
     * @throws BuildException The exception raised during task execution.
     */
    @Override
    public void execute() throws BuildException {

        if (getFile() == null) {
            final String msg = "XmlProperty task requires a file attribute";
            throw new BuildException(msg);
        }
        InputStream in = null;
        try {
            log("Loading " + src.getAbsolutePath(), Project.MSG_VERBOSE);

            if (src.exists()) {
                in = new FileInputStream(src);
                load(in);
            } else {
                log("Unable to find property file: " + src.getAbsolutePath(),
                        Project.MSG_VERBOSE);
            }
        }catch(final SAXException se){
            throw new BuildException(se);
        } catch (final IOException ioe) {
            // I/O error
            throw new BuildException(ioe);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (final IOException e) {
                    log(e, Project.MSG_ERR);
                }
            }
        }
    }

    private void load( final InputStream in) throws IOException,SAXException{
        final Document doc = getLoadingDoc(in);
        final Element propertiesElement = (Element)doc.getChildNodes().item(1);
        final String xmlVersion = propertiesElement.getAttribute("version");
        if (xmlVersion.compareTo(EXTERNAL_XML_VERSION) > 0) {
            throw new SAXException(
                    "Exported Properties file format version " + xmlVersion +
                    " is not supported. This java installation can read" +
                    " versions " + EXTERNAL_XML_VERSION + " or older. You" +
                    " may need to install a newer version of JDK.");
        }
        final NodeList entries = propertiesElement.getChildNodes();
        final int numEntries = entries.getLength();
        final int start = numEntries > 0 &&
                entries.item(0).getNodeName().equals("comment") ? 1 : 0;
        for (int i=start; i<numEntries; i++) {
            final Element entry = (Element)entries.item(i);
            if (entry.hasAttribute("key")) {
                final Node n = entry.getFirstChild();
                final String val = (n == null) ? "" : n.getNodeValue();
                props.setProperty(entry.getAttribute("key"), val);
                addProperty(entry.getAttribute("key"),val,null);
            }
        }

    }

    static Document getLoadingDoc(final InputStream in) throws SAXException,
    IOException {
        final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setIgnoringElementContentWhitespace(true);
        dbf.setValidating(true);
        dbf.setCoalescing(true);
        dbf.setIgnoringComments(true);
        try {
            final DocumentBuilder db = dbf.newDocumentBuilder();
            db.setEntityResolver(new Resolver());
            db.setErrorHandler(new EH());
            final InputSource is = new InputSource(in);
            return db.parse(is);
        } catch (final ParserConfigurationException x) {
            throw new Error(x);
        }
    }
    private static class Resolver implements EntityResolver {
        @Override
        public InputSource resolveEntity(final String pid, final String sid)
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
        @Override
        public void error(final SAXParseException x) throws SAXException {
            throw x;
        }
        @Override
        public void fatalError(final SAXParseException x) throws SAXException {
            throw x;
        }
        @Override
        public void warning(final SAXParseException x) throws SAXException {
            throw x;
        }
    }
    /**
     * Actually add the given property/value to the project
     * after writing a log message.
     */
    private void addProperty(final String name, final String value, final String id) {
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
    public void setFile(final File src) {
        this.src = src;
    }

    /**
     * the prefix to prepend to each property.
     * @param prefix the prefix to prepend to each property
     */
    public void setPrefix(final String prefix) {
        this.prefix = prefix.trim();
    }

    /**
     * flag to validate the XML file; optional, default false.
     * @param validate if true validate the XML file, default false
     */
    public void setValidate(final boolean validate) {
        this.validate = validate;
    }

    /**
     * The directory to use for resolving file references.
     * Ignored if semanticAttributes is not set to true.
     * @param rootDirectory the directory.
     */
    public void setRootDirectory(final File rootDirectory) {
        this.rootDirectory = rootDirectory;
    }

    /**
     * add an XMLCatalog as a nested element; optional.
     * @param catalog the XMLCatalog to use
     */
    public void addConfiguredXMLCatalog(final XMLCatalog catalog) {
        xmlCatalog.addConfiguredXMLCatalog(catalog);
    }

    /* Expose members for extensibility */

    /**
     * @return the file attribute.
     */
    protected File getFile() {
        return src;
    }

    /**
     * @return the prefix attribute.
     */
    protected String getPrefix() {
        return prefix;
    }

    /**
     * @return the validate attribute.
     */
    protected boolean getValidate() {
        return validate;
    }

    /**
     * @return the collapse attributes attribute.
     */
    protected boolean getCollapseAttributes() {
        return collapseAttributes;
    }

    /**
     * @return the root directory attribute.
     */
    protected File getRootDirectory() {
        return rootDirectory;
    }

}