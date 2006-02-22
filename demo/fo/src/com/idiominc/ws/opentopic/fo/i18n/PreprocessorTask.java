package com.idiominc.ws.opentopic.fo.i18n;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.XMLCatalog;
import org.apache.xml.resolver.tools.CatalogResolver;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;

import com.idiominc.ws.opentopic.fo.i18n.Configuration;
import com.idiominc.ws.opentopic.fo.i18n.MultilanguagePreprocessor;


/**
 * User: Ivan Luzyanin
 * Date: Jan 21, 2004
 * Time: 11:40:56 AM
 */
public class PreprocessorTask
        extends Task {
    private String config = null;
    private String input = null;
    private String output = null;
    private String catalogs = null;


    public void execute()
            throws BuildException {
        checkParameters();

        try {
            if (catalogs != null) {
                System.setProperty("xml.catalog.files", catalogs);
            }

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            documentBuilder.setEntityResolver(new CatalogResolver());

            Document doc = documentBuilder.parse(new File(this.input));
            Document conf = documentBuilder.parse(new File(this.config));
            MultilanguagePreprocessor preprocessor = new MultilanguagePreprocessor(new Configuration(conf));
            Document document = preprocessor.process(doc);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.INDENT, "no");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            if (doc.getDoctype() != null) {
                transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, doc.getDoctype().getPublicId());
                transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, doc.getDoctype().getSystemId());
            }
            StreamResult streamResult = new StreamResult(new File(this.output));
            transformer.transform(new DOMSource(document), streamResult);
        } catch (Exception e) {
            throw new BuildException(e);
        }
    }


    private void checkParameters()
            throws BuildException {
        if (null == config || null == input || null == output) {
            throw new BuildException("config, input, output attributes is required");
        }
    }


    public void setConfig(String theConfig) {
        this.config = theConfig;
    }


    public void setInput(String theInput) {
        this.input = theInput;
    }


    public void setOutput(String theOutput) {
        this.output = theOutput;
    }


    public void setCatalogs(String catalogs) {
        this.catalogs = catalogs;
    }
}
