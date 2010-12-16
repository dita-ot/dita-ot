package com.idiominc.ws.opentopic.fo.i18n;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
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
import java.io.FileOutputStream;

import com.idiominc.ws.opentopic.fo.i18n.Configuration;
import com.idiominc.ws.opentopic.fo.i18n.MultilanguagePreprocessor;

/*
Copyright (c) 2004-2006 by Idiom Technologies, Inc. All rights reserved. 
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
*/public class PreprocessorTask
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

            FileOutputStream out = new FileOutputStream(this.output);
            StreamResult streamResult = new StreamResult(out);
            transformer.transform(new DOMSource(document), streamResult);
            out.close();
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
