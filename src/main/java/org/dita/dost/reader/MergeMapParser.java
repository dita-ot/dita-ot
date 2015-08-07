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
import static org.dita.dost.util.URLUtils.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.net.URI;
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
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.util.MergeUtils;
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
    
    private static final String ATTRIBUTE_NAME_FIRST_TOPIC_ID = "first_topic_id";
    public static final String ATTRIBUTE_NAME_OHREF = "ohref";
    public static final String ATTRIBUTE_NAME_OID = "oid";
    
    private final XMLReader reader;
    private final MergeTopicParser topicParser;
    private final MergeUtils util;
    private File dirPath = null;
    private File tempdir = null;

    private final Stack<String> processStack;
    private int processLevel;
    private final ByteArrayOutputStream topicBuffer;
    private final SAXTransformerFactory stf;
    private OutputStream output;
    private DITAOTLogger logger;
    private Job job;

    /**
     * Default Constructor.
     */
    public MergeMapParser() {
        processStack = new Stack<>();
        processLevel = 0;
        util = new MergeUtils();
        topicParser = new MergeTopicParser(util);
        topicBuffer = new ByteArrayOutputStream();
        try{
            reader = XMLUtils.getXMLReader();
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
    
    public final void setJob(final Job job) {
        this.job = job;
    }

    /**
     * Set merge output file
     * 
     * @param outputFile merge output file
     */
    public void setOutput(final File outputFile) {
        topicParser.setOutput(outputFile);
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
    public void read(final File filename, final File tmpDir) {
        tempdir = tmpDir != null ? tmpDir : filename.getParentFile();
        try{
            final TransformerHandler s = stf.newTransformerHandler();
            s.getTransformer().setOutputProperty(OMIT_XML_DECLARATION, "yes");
            s.setResult(new StreamResult(output));
            setContentHandler(s);
            dirPath = filename.getParentFile();
            reader.setErrorHandler(new DITAOTXMLErrorHandler(filename.getAbsolutePath(), logger));
            topicParser.getContentHandler().startDocument();
            logger.info("Processing " + filename.getAbsolutePath());
            reader.parse(filename.toURI().toString());
            topicParser.getContentHandler().endDocument();
            output.write(topicBuffer.toByteArray());
        }catch(final Exception e){
            logger.error(e.getMessage(), e) ;
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
            if (ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY.equals(value)) {
                return;
            }
        }
        getContentHandler().endElement(uri, localName, qName);
    }

    @Override
    public void characters(final char[] ch, final int start, final int length) throws SAXException {
        if (processStack.empty() || !ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY.equals(processStack.peek())){
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
            if (ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY.equals(attrValue)) {
                return;
            }
        } else if (processLevel > 0) {
            processLevel++;
            // Child of @processing-role='resource-only'
            if (ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY.equals(processStack.peek())) {
                return;
            }
        }
        AttributesImpl atts = null;
        if (MAP_TOPICREF.matches(attributes)) {
            URI attValue = toURI(attributes.getValue(ATTRIBUTE_NAME_HREF));
            if (attValue != null) {
                atts = new AttributesImpl(attributes);
                final String scopeValue = atts.getValue(ATTRIBUTE_NAME_SCOPE);
                final String formatValue = atts.getValue(ATTRIBUTE_NAME_FORMAT);
                if ((scopeValue == null || ATTR_SCOPE_VALUE_LOCAL.equals(scopeValue))
                        && (formatValue == null || ATTR_FORMAT_VALUE_DITA.equals(formatValue))) {
                    final URI ohref = attValue;
                    final URI copyToValue = toURI(atts.getValue(ATTRIBUTE_NAME_COPY_TO));
                    if (copyToValue != null && !copyToValue.toString().isEmpty()) {
                        attValue = copyToValue;
                    }
                    XMLUtils.addOrSetAttribute(atts, ATTRIBUTE_NAME_OHREF, ohref.toString());
                    if (util.isVisited(attValue)) {
                        attValue = toURI(SHARP + util.getIdValue(attValue));
                    } else {
                        //parse the topic
                        final URI p = stripFragment(attValue).normalize();
                        util.visit(p);
                        final File f = new File(dirPath, toFile(p).getPath());
                        if (f.exists()) {
                            topicParser.parse(toFile(p).getPath(), dirPath);
                            final String fileId = topicParser.getFirstTopicId();
                            util.addId(attValue, fileId);
                            if (attValue.getFragment() != null) {
                                util.addId(stripFragment(attValue), fileId);
                            }
                            final URI firstTopicId = toURI(SHARP + fileId);
                            if (util.getIdValue(attValue) != null) {
                                attValue = toURI(SHARP + util.getIdValue(attValue));
                            } else {
                                attValue = firstTopicId;
                            }
                            XMLUtils.addOrSetAttribute(atts, ATTRIBUTE_NAME_FIRST_TOPIC_ID, firstTopicId.toString());
                        } else {
                            final URI fileName = dirPath.toURI().resolve(attValue);
                            logger.error(MessageUtils.getInstance().getMessage("DOTX008E", fileName.toString()).toString());
                        }
                    }
                    }
                XMLUtils.addOrSetAttribute(atts, ATTRIBUTE_NAME_HREF, attValue.toString());
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
            for (final FileInfo f: job.getFileInfo()) {
                if (f.isTarget) {
                    String element = f.file.getPath();
                    if (!dirPath.equals(tempdir)) {
                        element = FileUtils.getRelativeUnixPath(new File(dirPath,"a.ditamap").getAbsolutePath(),
                                                                   new File(tempdir, element).getAbsolutePath());
                    }
                    if (!util.isVisited(toURI(element))) {
                        util.visit(toURI(element));
                        if (!f.isResourceOnly && (f.isChunked || !f.isSkipChunk)){
                            //ensure the file exists
                            final File file = new File(dirPath, element);
                            if (file.exists()) {
                                topicParser.parse(element, dirPath);
                            } else {
                                final String fileName = file.getAbsolutePath();
                                logger.error(MessageUtils.getInstance().getMessage("DOTX008E", fileName).toString());
                            }
                        }
                    }
                }
            }
        }catch (final Exception e){
            logger.error(e.getMessage(), e) ;
        }
        
        getContentHandler().endDocument();
    }
    
}
