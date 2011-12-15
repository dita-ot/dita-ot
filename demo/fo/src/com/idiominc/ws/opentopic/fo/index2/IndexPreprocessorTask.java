package com.idiominc.ws.opentopic.fo.index2;

import static org.dita.dost.util.Constants.*;

import com.idiominc.ws.opentopic.fo.index2.configuration.IndexConfiguration;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.Project;
import org.apache.xml.resolver.tools.CatalogResolver;
import org.dita.dost.log.DITAOTAntLogger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileOutputStream;
import java.util.Locale;

/*
Copyright � 2004-2006 by Idiom Technologies, Inc. All rights reserved.
IDIOM is a registered trademark of Idiom Technologies, Inc. and WORLDSERVER
and WORLDSTART are trademarks of Idiom Technologies, Inc. All other
trademarks are the property of their respective owners.

IDIOM TECHNOLOGIES, INC. IS DELIVERING THE SOFTWARE "AS IS," WITH
ABSOLUTELY NO WARRANTIES WHATSOEVER, WHETHER EXPRESS OR IMPLIED,  AND IDIOM
TECHNOLOGIES, INC. DISCLAIMS ALL WARRANTIES, EXPRESS OR IMPLIED, INCLUDING
BUT NOT LIMITED TO WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
PURPOSE AND WARRANTY OF NON-INFRINGEMENT. IDIOM TECHNOLOGIES, INC. SHALL NOT
BE LIABLE FOR INDIRECT, INCIDENTAL, SPECIAL, COVER, PUNITIVE, EXEMPLARY,
RELIANCE, OR CONSEQUENTIAL DAMAGES (INCLUDING BUT NOT LIMITED TO LOSS OF
ANTICIPATED PROFIT), ARISING FROM ANY CAUSE UNDER OR RELATED TO  OR ARISING
OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN IF IDIOM
TECHNOLOGIES, INC. HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.

Idiom Technologies, Inc. and its licensors shall not be liable for any
damages suffered by any person as a result of using and/or modifying the
Software or its derivatives. In no event shall Idiom Technologies, Inc.'s
liability for any damages hereunder exceed the amounts received by Idiom
Technologies, Inc. as a result of this transaction.

These terms and conditions supersede the terms and conditions in any
licensing agreement to the extent that such terms and conditions conflict
with those set forth herein.

This file is part of the DITA Open Toolkit project hosted on Sourceforge.net.
See the accompanying license.txt file for applicable licenses.
 */
public class IndexPreprocessorTask
extends Task {
    //	private String input = null;
    private String input = "";
    private String output = "";
    private String catalogs = null;
    private String locale = "ja";
    private String indexConfig = "";
    public static boolean failOnError = false;
    public static boolean processingFaild = false;
    private static final String prefix = "opentopic-index";
    private static final String namespace_url = "http://www.idiominc.com/opentopic/index";

    public static void main(final String[] args) {
        new IndexPreprocessorTask().execute();
    }
    @Override
    public void execute()
            throws BuildException {
        checkParameters();
        if (this.catalogs != null) {
            System.setProperty("xml.catalog.files", this.catalogs);
        }

        try {
            final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            documentBuilder.setEntityResolver(new CatalogResolver() {
                @Override
                public InputSource resolveEntity(final String publicId, String systemId) {
                    // strip path from DTD location
                    final int slashIdx = systemId.lastIndexOf("/");
                    if (slashIdx >= 0) {
                        systemId = systemId.substring(slashIdx + 1);
                    }

                    // resolve real location with XMLCatalogResolver
                    return super.resolveEntity(publicId, systemId);
                }
            });

            final Document doc = documentBuilder.parse(input);
            final IndexPreprocessor preprocessor = new IndexPreprocessor(this.prefix, this.namespace_url);
            preprocessor.setLogger(new DITAOTAntLogger(getProject()));

            // Walks through source document and builds an array of IndexEntry and builds
            // new Document with pre-processed index entries included.
            final IndexPreprocessResult result = preprocessor.process(doc);

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

            if (processingFaild) {
                setActiveProjectProperty("ws.runtime.index.preprocess.fail","true");
            }
            // Serialize processed document
            final TransformerFactory transformerFactory = TransformerFactory.newInstance();
            final Transformer transformer = transformerFactory.newTransformer();
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
            final FileOutputStream out = new FileOutputStream(this.output);
            final StreamResult streamResult = new StreamResult(out);
            transformer.transform(new DOMSource(resultDoc), streamResult);
            out.close();
        } catch (final Exception e) {
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


    public void setInput(final String theInput) {
        this.input = theInput;
    }


    public void setOutput(final String theOutput) {
        this.output = theOutput;
    }


    public void setCatalogs(final String theCatalogs) {
        this.catalogs = theCatalogs;
    }


    public void setLocale(final String theLocale) {
        this.locale = theLocale;
    }


    public void setIndexConfig(final String theIndexConfig) {
        this.indexConfig = theIndexConfig;
    }

    public void setFailOnError(final String theFailOnErro) {
        this.failOnError = theFailOnErro.equals("true");
    }

    private void setActiveProjectProperty(final String propertyName, final String propertyValue) {
        final Project activeProject = getProject();
        if (activeProject != null) {
            activeProject.setProperty(propertyName, propertyValue);
        }
    }

}
