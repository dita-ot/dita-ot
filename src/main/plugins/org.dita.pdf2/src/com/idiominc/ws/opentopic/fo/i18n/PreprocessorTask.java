package com.idiominc.ws.opentopic.fo.i18n;

import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import org.apache.commons.io.IOUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.XMLCatalog;
import org.dita.dost.util.Job;
import org.dita.dost.util.XMLUtils;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URI;

import static org.dita.dost.util.Constants.ANT_REFERENCE_JOB;
import static org.dita.dost.util.Constants.ANT_REFERENCE_XML_UTILS;

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

This file is part of the DITA Open Toolkit project.
See the accompanying LICENSE file for applicable license.
 */
public class PreprocessorTask extends Task {
     private File config = null;
     private URI input = null;
     private URI output = null;
     private URI style = null;
     private XMLCatalog xmlcatalog;

     @Override
     public void execute()
             throws BuildException {
         final Job job = getProject().getReference(ANT_REFERENCE_JOB);
         final XMLUtils xmlUtils = getProject().getReference(ANT_REFERENCE_XML_UTILS);

         checkParameters();

         log("Processing " + input + " to " + output, Project.MSG_INFO);
         try {
             final DocumentBuilder documentBuilder = XMLUtils.getDocumentBuilder();
             documentBuilder.setEntityResolver(xmlcatalog);

             final Document doc = job.getStore().getDocument(input);
             final Document conf = documentBuilder.parse(config);
             final MultilanguagePreprocessor preprocessor = new MultilanguagePreprocessor(new Configuration(conf));
             final Document document = preprocessor.process(doc);

             if (style != null) {
                 log("Loading stylesheet " + style, Project.MSG_INFO);
                 final XsltExecutable compile = xmlUtils.getProcessor().newXsltCompiler().compile(job.getStore().getSource(style));
                 final XsltTransformer t = compile.load();
                 t.setSource(new DOMSource(document));
                 t.setDestination(job.getStore().getDestination(output));
                 t.transform();
             } else {
                 job.getStore().writeDocument(document, output);
             }
         } catch (final RuntimeException e) {
             throw e;
         } catch (final Exception e) {
             throw new BuildException(e);
         }
     }


     private void checkParameters()
             throws BuildException {
         if (null == config || null == input || null == output) {
             throw new BuildException("config, input, output attributes is required");
         }
     }


     public void setConfig(final File theConfig) {
         this.config = theConfig;
     }


     public void setInput(final File theInput) {
         this.input = theInput.toURI();
     }


     public void setOutput(final File theOutput) {
         this.output = theOutput.toURI();
     }

     /** @deprecated since 2.3 */
     @Deprecated
     public void setCatalogs(final String catalogs) {
         log("catalogs attribute has been deprecated, use xmlcatalog nested element", Project.MSG_WARN);
     }

    public void setStyle(final File style) {
        this.style = style.toURI();
    }

    public void addConfiguredXmlcatalog(final XMLCatalog xmlcatalog) {
        this.xmlcatalog = xmlcatalog;
    }

 }
