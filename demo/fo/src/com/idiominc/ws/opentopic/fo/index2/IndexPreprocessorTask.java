package com.idiominc.ws.opentopic.fo.index2;

import com.idiominc.ws.opentopic.fo.index2.configuration.IndexConfiguration;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.xml.resolver.tools.CatalogResolver;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.Locale;

/**
 * User: Ivan Luzyanin
 * Date: Jan 21, 2004
 * Time: 11:40:56 AM
 */
public class IndexPreprocessorTask
		extends Task {
	private String input = null;
	private String output = null;
	private String catalogs = null;
	private String locale = null;
	private String indexConfig;
	private String prefix = "opentopic-index";
	private String namespace_url = "http://www.idiominc.com/opentopic/index";
	private String indexElementName = "indexterm";

	public void execute()
			throws BuildException {
		checkParameters();
		if (this.catalogs != null) {
			System.setProperty("xml.catalog.files", this.catalogs);
		}

		try {
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			documentBuilder.setEntityResolver(new CatalogResolver() {
				public InputSource resolveEntity(String publicId, String systemId) {
					// strip path from DTD location
					int slashIdx = systemId.lastIndexOf("/");
					if (slashIdx >= 0) {
						systemId = systemId.substring(slashIdx + 1);
					}

					// resolve real location with XMLCatalogResolver
					return super.resolveEntity(publicId, systemId);
				}
			});

			Document doc = documentBuilder.parse(input);
			IndexPreprocessor preprocessor = new IndexPreprocessor(this.prefix, this.namespace_url, this.indexElementName);

			// Walks through source document and builds an array of IndexEntry and builds
			// new Document with pre-processed index entries included.
			IndexPreprocessResult result = preprocessor.process(doc);

			final Document resultDoc = result.getDocument();

			// Parse index configuration from file specified from ANT script
			final IndexConfiguration configuration = IndexConfiguration.parse(documentBuilder.parse(this.indexConfig));
			final IndexEntry[] indexEntries = result.getIndexEntries();

			Locale loc;
			// Split passed locale string to lang and country codes
			if (locale.indexOf("-") == 2 || locale.indexOf("_") == 2) {
				loc = new Locale(locale.substring(0, 2), locale.substring(3));
			} else {
				loc = new Locale(this.locale);
			}
			// Append index groups to the end of document
			preprocessor.createAndAddIndexGroups(indexEntries, configuration, resultDoc, loc);

			// Serialize processed document
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			transformer.setOutputProperty(OutputKeys.INDENT, "no");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
			if (doc.getDoctype() != null) {
				if (null != doc.getDoctype().getPublicId()) {
					transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, doc.getDoctype().getPublicId());
				}
				if (null != doc.getDoctype().getSystemId()) {
					transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, doc.getDoctype().getSystemId());
				}
			}
			StreamResult streamResult = new StreamResult(new File(this.output));
			transformer.transform(new DOMSource(resultDoc), streamResult);
		} catch (Exception e) {
			e.printStackTrace();
			throw new BuildException(e);
		}
	}


	private void checkParameters()
			throws BuildException {
		if (null == locale || null == input || null == output || null == indexConfig) {
			throw new BuildException("locale, indexConfig, input, output attributes are required");
		}
	}


	public void setInput(String theInput) {
		this.input = theInput;
	}


	public void setOutput(String theOutput) {
		this.output = theOutput;
	}


	public void setCatalogs(String theCatalogs) {
		this.catalogs = theCatalogs;
	}


	public void setLocale(String theLocale) {
		this.locale = theLocale;
	}


	public void setIndexConfig(String theIndexConfig) {
		this.indexConfig = theIndexConfig;
	}


	public void setIndexElementName(String theIndexElementName) {
		this.indexElementName = theIndexElementName;
	}
}
