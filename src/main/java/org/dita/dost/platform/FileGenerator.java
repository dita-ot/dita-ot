/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2005, 2006 All Rights Reserved.
 */
package org.dita.dost.platform;

import static org.dita.dost.util.Constants.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;

import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DefaultHandler2;

/**
 * Generate outputfile with templates.
 * @author Zhang, Yuan Peng
 */
final class FileGenerator extends DefaultHandler2 {

    private static final String EXTENSION_ID_ATTR = "id";
    public static final String EXTENSION_ELEM = "extension";
    public static final String EXTENSION_ATTR = "extension";
    public static final String BEHAVIOR_ATTR = "behavior";
    
    public static final String PARAM_LOCALNAME = "localname";
    public static final String PARAM_TEMPLATE = "template";

    private static final String DITA_OT_NS = "http://dita-ot.sourceforge.net";
    private static final String TEMPLATE_PREFIX = "_template.";

    private final XMLReader reader;
    private DITAOTLogger logger;
    private OutputStreamWriter output = null;
    /** Plug-in features. */
    private final Map<String,String> featureTable;
    private final Map<String, Features> pluginTable;
    /** Template file. */
    private File templateFile;

    /**
     * Default Constructor.
     */
    public FileGenerator() {
        this(null, null);
    }

    /**
     * Constructor init featureTable.
     * @param featureTbl featureTbl
     */
    public FileGenerator(final Hashtable<String,String> featureTbl, final Map<String,Features> pluginTable) {
        featureTable = featureTbl;
        this.pluginTable = pluginTable;
        output = null;
        templateFile = null;

        try {
            reader = StringUtils.getXMLReader();
            reader.setContentHandler(this);
            reader.setProperty(LEXICAL_HANDLER_PROPERTY,this);
            reader.setFeature(FEATURE_NAMESPACE_PREFIX, true);
            //reader.setFeature(FEATURE_VALIDATION, true);
            //reader.setFeature(FEATURE_VALIDATION_SCHEMA, true);

        } catch (final Exception e) {
            throw new RuntimeException("Failed to initialize parser: " + e.getMessage(), e);
        }
    }

    /**
     * Set logger.
     * @param logger logger instance
     */
    public void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
    }

    /**
     * Generator the output file.
     * @param fileName filename
     */
    public void generate(final File fileName){
        if (logger == null) {
            logger = new DITAOTJavaLogger();
        }
        final File outputFile = removeTemplatePrefix(fileName);
        templateFile = fileName;

        try{
            final FileOutputStream fileOutput = new FileOutputStream(outputFile);
            output = new OutputStreamWriter(fileOutput, UTF8);
            reader.parse(fileName.toURI().toString());
        } catch (final Exception e){
            logger.logError(e.getMessage(), e) ;
        }finally {
            if (output != null) {
                try {
                    output.close();
                }catch (final Exception e) {
                    logger.logError(e.getMessage(), e) ;
                }
            }
        }
    }

    private File removeTemplatePrefix(final File templateFile) {
        final String f = templateFile.getAbsolutePath();
        final int i = f.lastIndexOf(TEMPLATE_PREFIX);
        if (i != -1) {
            return new File(f.substring(0, i)
                    + f.substring(i + TEMPLATE_PREFIX.length() - 1));
        } else {
            throw new IllegalArgumentException("File " + templateFile + " does not contain template prefix");
        }
    }

    @Override
    public void characters(final char[] ch, final int start, final int length) throws SAXException {
        try{
            output.write(StringUtils.escapeXML(ch,start,length));
        }catch (final Exception e) {
            logger.logError(e.getMessage(), e) ;
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        try{
            if(!(DITA_OT_NS.equals(uri) && EXTENSION_ELEM.equals(localName))){
                output.write("</");
                output.write(qName);
                output.write(">");
            }
        }catch (final Exception e) {
            logger.logError(e.getMessage(), e) ;
        }
    }

    @Override
    public void ignorableWhitespace(final char[] ch, final int start, final int length) throws SAXException {
        try{
            output.write(ch,start,length);
        }catch (final Exception e) {
            logger.logError(e.getMessage(), e) ;
        }
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
        IAction action = null;
        try{
            if(DITA_OT_NS.equals(uri) && EXTENSION_ELEM.equals(localName)){
                // Element extension: <dita:extension id="extension-point" behavior="classname"/>
                action = (IAction)Class.forName(attributes.getValue(BEHAVIOR_ATTR)).newInstance();
                action.setLogger(logger);
                action.addParam(PARAM_TEMPLATE, templateFile.getAbsolutePath());
                for (int i = 0; i <  attributes.getLength(); i++) {
                    action.addParam(attributes.getLocalName(i), attributes.getValue(i));
                }
                final String extension = attributes.getValue(EXTENSION_ID_ATTR);
                //action.addParam("extension", extension);
                if(featureTable.containsKey(extension)){
                    action.setInput(featureTable.get(extension));
                }
                action.setFeatures(pluginTable);
                output.write(action.getResult());
            }else{
                final int attLen = attributes.getLength();
                output.write("<");
                output.write(qName);
                for(int i = 0; i < attLen; i++){
                    if (DITA_OT_NS.equals(attributes.getURI(i)))
                    {
                        // Attribute extension: <element dita:extension="localname classname ..." dita:localname="...">
                        if (!(EXTENSION_ATTR.equals(attributes.getLocalName(i))))
                        {
                            final String extensions = attributes.getValue(DITA_OT_NS, EXTENSION_ATTR);
                            final StringTokenizer extensionTokenizer = new StringTokenizer(extensions);
                            // Get the classname that implements this localname.
                            while (extensionTokenizer.hasMoreTokens())
                            {
                                final String thisExtension = extensionTokenizer.nextToken();
                                final String thisExtensionClass = extensionTokenizer.nextToken();
                                if (thisExtension.equals(attributes.getLocalName(i)))
                                {
                                    action = (IAction)Class.forName(thisExtensionClass).newInstance();
                                    break;
                                }
                            }
                            action.setLogger(logger);
                            action.setFeatures(pluginTable);
                            action.addParam(PARAM_TEMPLATE, templateFile.getAbsolutePath());
                            action.addParam(PARAM_LOCALNAME, attributes.getLocalName(i));
                            action.setInput(attributes.getValue(i));
                            output.write(action.getResult());
                        }
                    }
                    else if (attributes.getQName(i).startsWith("xmlns:") &&
                            DITA_OT_NS.equals(attributes.getValue(i)))
                    {
                        // Ignore xmlns:dita.
                    }
                    else
                    {
                        // Normal attribute.
                        output.write(" ");
                        output.write(new StringBuffer(attributes.getQName(i)).append("=\"").
                                append(StringUtils.escapeXML(attributes.getValue(i))).append("\"").toString());
                    }
                }
                output.write(">");
            }
        }catch(final Exception e){
            e.printStackTrace();
            logger.logError(e.getMessage(), e) ;
        }
    }

    @Override
    public void endDocument() throws SAXException {
        try{
            output.flush();
            output.close();
        }catch(final Exception e){
            logger.logError(e.getMessage(), e) ;
        }

    }
    @Override
    public void skippedEntity(final String name) throws SAXException {
        logger.logError("Skipped entity " + name);
    }

    @Override
    public void comment(final char[] ch, final int start, final int length) throws SAXException {
        try{
            output.write("<!--");
            output.write(ch, start, length);
            output.write("-->");
        }catch(final Exception e){
            logger.logError(e.getMessage(), e) ;
        }
    }

    @Override
    public void startDocument() throws SAXException {
        try{
            output.write(XML_HEAD);
        }catch(final Exception e){
            logger.logError(e.getMessage(), e) ;
        }

    }

}
