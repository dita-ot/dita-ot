/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.reader;

import static javax.xml.transform.OutputKeys.*;
import static org.dita.dost.util.Constants.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.helpers.XMLFilterImpl;
import org.xml.sax.helpers.AttributesImpl;

import org.dita.dost.exception.DITAOTXMLErrorHandler;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.Job;
import org.dita.dost.util.MergeUtils;
import org.dita.dost.util.StringUtils;
import org.dita.dost.util.XMLUtils;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * MergeMapParser reads the ditamap file after preprocessing and merges
 * different files into one intermediate result. It calls MergeTopicParser
 * to process the topic file. Instances are reusable but not thread-safe.
 */
public final class MergeMapParser extends XMLFilterImpl {
    
    public static final String ATTRIBUTE_NAME_FIRST_TOPIC_ID = "first_topic_id";
    public static final String ATTRIBUTE_NAME_OHREF = "ohref";
    public static final String ATTRIBUTE_NAME_OID = "oid";
    
    private final XMLReader reader;
    private final MergeTopicParser topicParser;
    private final MergeUtils util;
    private String dirPath = null;
    private String tempdir = null;

    private final Stack<String> processStack;
    private int processLevel;
    private final ByteArrayOutputStream topicBuffer;
    private final SAXTransformerFactory stf;
    private OutputStream output;
    private DITAOTLogger logger;

    /**
     * Default Constructor.
     */
    public MergeMapParser() {
        processStack = new Stack<String>();
        processLevel = 0;
        util = new MergeUtils();
        topicParser = new MergeTopicParser(util);
        topicBuffer = new ByteArrayOutputStream();
        try{
            reader = StringUtils.getXMLReader();
            reader.setContentHandler(this);
            reader.setFeature(FEATURE_NAMESPACE_PREFIX, true);
            
            final TransformerFactory tf = TransformerFactory.newInstance();
            if (!tf.getFeature(SAXTransformerFactory.FEATURE)) {
                throw new RuntimeException("SAX transformation factory not supported");
            }
            stf = (SAXTransformerFactory) tf;
            final TransformerHandler s = stf.newTransformerHandler();
            s.getTransformer().setOutputProperty(OMIT_XML_DECLARATION , "yes");
            s.setResult(new StreamResult(topicBuffer));
            topicParser.setContentHandler(s);
        }catch (final Exception e){
            throw new RuntimeException("Failed to initialize XML parser: " + e.getMessage(), e);
        }
    }
    
    public final void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
        topicParser.setLogger(logger);
    }

    /**
     * Set output.
     * 
     * @param output output stream
     */
    public void setOutputStream(final OutputStream output) {
        this.output = output;
    }

    /**
     * Read map.
     * 
     * @param filename map file path
     * @param tmpDir temporary directory path, may be {@code null}
     */
    public void read(final String filename, final String tmpDir) {
        tempdir = tmpDir != null ? tmpDir : new File(filename).getParent();
        try{
            final TransformerHandler s = stf.newTransformerHandler();
            s.getTransformer().setOutputProperty(OMIT_XML_DECLARATION, "yes");
            s.setResult(new StreamResult(output));
            setContentHandler(s);
            final File input = new File(filename);
            dirPath = input.getParent();
            reader.setErrorHandler(new DITAOTXMLErrorHandler(input.getAbsolutePath(), logger));
            topicParser.getContentHandler().startDocument();
            reader.parse(input.toURI().toString());
            topicParser.getContentHandler().endDocument();
            output.write(topicBuffer.toByteArray());
        }catch(final Exception e){
            logger.logError(e.getMessage(), e) ;
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        if (processLevel > 0) {
            String value = processStack.peek();
            if (processLevel == processStack.size()) {
                value = processStack.pop();
            }
            processLevel--;
            if (ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY.equalsIgnoreCase(value)) {
                return;
            }
        }
        getContentHandler().endElement(uri, localName, qName);
    }

    @Override
    public void characters(final char[] ch, final int start, final int length) throws SAXException {
        if (processStack.empty() || !ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY.equalsIgnoreCase(processStack.peek())){
            getContentHandler().characters(ch, start, length);
        }
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
        final String attrValue = attributes.getValue(ATTRIBUTE_NAME_PROCESSING_ROLE);
        if (attrValue != null) {
            processStack.push(attrValue);
            processLevel++;
            // @processing-role='resource-only'
            if (ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY.equalsIgnoreCase(attrValue)) {
                return;
            }
        } else if (processLevel > 0) {
            processLevel++;
            // Child of @processing-role='resource-only'
            if (ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY.equalsIgnoreCase(processStack.peek())) {
                return;
            }
        }
        AttributesImpl atts = null;
        if (MAP_TOPICREF.matches(attributes)) {
            String attValue = attributes.getValue(ATTRIBUTE_NAME_HREF);
            if (attValue != null) {
                atts = new AttributesImpl(attributes);
                final String scopeValue = atts.getValue(ATTRIBUTE_NAME_SCOPE);
                final String formatValue = atts.getValue(ATTRIBUTE_NAME_FORMAT);
                if ((scopeValue == null || ATTR_SCOPE_VALUE_LOCAL.equalsIgnoreCase(scopeValue))
                        && (formatValue == null || ATTR_FORMAT_VALUE_DITA.equalsIgnoreCase(formatValue))) {
                    final String ohref = attValue;
                    final String copyToValue = atts.getValue(ATTRIBUTE_NAME_COPY_TO);
                    if (!StringUtils.isEmptyString(copyToValue)) {
                        attValue = copyToValue;
                    }
                    XMLUtils.addOrSetAttribute(atts, ATTRIBUTE_NAME_OHREF, ohref);
                    if (util.isVisited(attValue)){
                        attValue = SHARP + util.getIdValue(attValue);
                    } else {
                        //parse the topic
                        String p = null;
                        try {
                            p = FileUtils.normalize(URLDecoder.decode(FileUtils.stripFragment(attValue), UTF8));
                        } catch (final UnsupportedEncodingException e) {
                        	throw new RuntimeException(e);
                        }
                        util.visit(p);
                        if (p != null) {
                            final File f = new File(dirPath, p);
                            if (f.exists()) {
                                topicParser.parse(p,dirPath);
                                final String fileId = topicParser.getFirstTopicId();
                                util.addId(attValue, fileId);
                                if (FileUtils.getFragment(attValue) != null) {
                                    util.addId(FileUtils.stripFragment(attValue), fileId);
                                }
                                final String firstTopicId = SHARP + fileId;
                                if (util.getIdValue(attValue) != null) {
                                	attValue = SHARP + util.getIdValue(attValue);
                                } else {
                                	attValue = firstTopicId;
                                }                                                     
                                XMLUtils.addOrSetAttribute(atts, ATTRIBUTE_NAME_FIRST_TOPIC_ID, firstTopicId);
                            } else {
                                final String fileName = new File(dirPath, attValue).getAbsolutePath();
                                logger.logError(MessageUtils.getInstance().getMessage("DOTX008E", fileName).toString());
                            }
                        }
                        }
                    }
                XMLUtils.addOrSetAttribute(atts, ATTRIBUTE_NAME_HREF, attValue);
            }
        }
        getContentHandler().startElement(uri, localName, qName, atts != null ? atts : attributes);
    }
    
    @Override
    public void endDocument() throws SAXException {
        // read href dita topic list
        // compare visitedSet with the list
        // if list item not in visitedSet then call MergeTopicParser to parse it
        try{
            final Job job = new Job(new File(tempdir));
            final Set<String> resourceOnlySet = job.getSet(RESOURCE_ONLY_LIST);
            final Set<String> skipTopicSet = job.getSet(CHUNK_TOPIC_LIST);
            final Set<String> chunkedTopicSet = job.getSet(CHUNKED_TOPIC_LIST);
            for (String element: job.getSet(HREF_TARGET_LIST)) {
                if (!new File(dirPath).equals(new File(tempdir))) {
                    element = FileUtils.getRelativePath(new File(dirPath,"a.ditamap").getAbsolutePath(),
                                                               new File(tempdir, element).getAbsolutePath());
                }
                if (!util.isVisited(element)) {
                    util.visit(element);
                    if (!resourceOnlySet.contains(element) && (chunkedTopicSet.contains(element)
                            || !skipTopicSet.contains(element))){
                        //ensure the file exists
                        final File f = new File(dirPath, element);
                        if (f.exists()) {
                            topicParser.parse(element, dirPath);
                        } else {
                            final String fileName = f.getAbsolutePath();
                            logger.logError(MessageUtils.getInstance().getMessage("DOTX008E", fileName).toString());
                        }
                    }

                }
            }
        }catch (final Exception e){
            logger.logError(e.getMessage(), e) ;
        }
        
        getContentHandler().endDocument();
    }
    
}
