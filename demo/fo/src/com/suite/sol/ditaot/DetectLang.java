package com.suite.sol.ditaot;

import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.Project;

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

Parts copyright by Suite Solutions, released under the same terms as the DITA-OT.
*/
public class DetectLang extends Task {

    private String documentPath;

    /**
     * Sets the path to the _MERGED file which will be parsed.
     *
     * @param path The path to set
     */
    public void setDocumentPath(String path) {
        this.documentPath = path;
    }

    /**
     * Executes the Ant task.
     */
    public void execute() {

        Project activeProject = getProject();
        if (activeProject != null) {
            if (activeProject.getProperty("document.locale") != null) {
                /* document.locale already set, nothing to do. */
                return;
            }
        }

        try {

            documentPath = documentPath.replace(File.separatorChar, '/');
            File file = documentPath.startsWith("file:") ? 
                    new File(new java.net.URI(documentPath)) : 
                    new File(documentPath);   
            if (!file.exists()) {
                throw new Exception("File does not exist");
            }
            if (!file.canRead()) {
                throw new Exception("Can't read input file");
            }

            SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
            SAXParser saxParser = saxParserFactory.newSAXParser();

            saxParser.parse(file, new DefaultHandlerImpl());

        } catch (Exception e) {
            /* Since an exception is used to stop parsing when the search
             * is successful, catch the exception.
             */
            if (e.getMessage() != null &&
                e.getMessage().equals("Search finished")) {
                System.out.println("Lang search finished");
            } else {
                e.printStackTrace();
            }
        }

    }

    private class DefaultHandlerImpl
            extends DefaultHandler {

        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            String processedString;
            String classAttr = attributes.getValue("class");
            String langAttr = attributes.getValue("xml:lang");

            if(classAttr != null && langAttr != null) {
                if ((classAttr.indexOf(" map/map ") > -1) ||
                    (classAttr.indexOf(" topic/topic ") > -1)) {
                        String partProcessedString = langAttr.replace('-','_')
                            .toLowerCase();
                        int length;
                        if ((length = partProcessedString.length()) > 4) {
                            processedString
                                = partProcessedString.substring(0, length - 2)
                                + partProcessedString.substring(length - 2, length).toUpperCase();
                        }
                        else {
                            processedString = partProcessedString;
                        }


                        setActiveProjectProperty("document.locale",
                                processedString);
                        /* Successfully found xml:lang, so stop parsing. */
                        throw new SAXException("Search finished");
                }

            }

        }

    }

    /**
     * Sets property in active ant project with name specified inpropertyName,
     * and value specified in propertyValue parameter
     */
    private void setActiveProjectProperty(String propertyName, String propertyValue) {
        Project activeProject = getProject();
        if (activeProject != null) {
            activeProject.setProperty(propertyName, propertyValue);
        }
    }

}
