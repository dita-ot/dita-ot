package com.idiominc.ws.opentopic.xsl.extension;

import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.Project;

/**
 * Created by IntelliJ IDEA.
 * User: blackside
 * Date: Dec 19, 2005
 * Time: 12:14:13 PM
 * To change this template use File | Settings | File Templates.
 */
public class DitaVersion extends Task {

    private String documentPath;

    /**
     * Sets the database connection URL; required.
     *
     * @param path The path to set
     */
    public void setDocumentPath(String path) {
        this.documentPath = path;
    }

    /**
     * Executes the task.
     */
    public void execute() {

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
            if (e.getMessage().equals("Search finished"))
                System.out.println("Search finished");
            else
                e.printStackTrace();
        }

    }

    private class DefaultHandlerImpl
            extends DefaultHandler {

        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (attributes.getValue("class").indexOf(" map/map ") > -1) {
                if (attributes.getIndex("ditaarch:DITAArchVersion") > -1)
                    setActiveProjectProrerty("ws.runtime.publishing.map.dita.version",attributes.getValue("ditaarch:DITAArchVersion"));
                else
                    setActiveProjectProrerty("ws.runtime.publishing.map.dita.version","132");
                throw new SAXException("Search finished");
            }
        }

    }

    /**
     * Sets property in active ant project with name specified inpropertyName,
     * and value specified in propertyValue parameter
     */
    private void setActiveProjectProrerty(String propertyName, String propertyValue) {
        Project activeProject = getProject();
        if (activeProject != null) {
            activeProject.setProperty(propertyName, propertyValue);
        }
    }

}
