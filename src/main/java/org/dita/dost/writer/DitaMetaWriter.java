/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.writer;

import static org.dita.dost.util.Constants.*;
import static java.util.Arrays.*;
import static org.dita.dost.util.XMLUtils.*;
import static org.dita.dost.util.URLUtils.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.dita.dost.exception.DITAOTXMLErrorHandler;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.reader.MapMetaReader;
import org.dita.dost.util.DitaClass;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

/**
 * DitaMetaWriter reads dita topic file and insert the metadata information into it.
 * 
 * @author Zhang, Yuan Peng
 */
public final class DitaMetaWriter extends AbstractXMLWriter {
    private String firstMatchTopic;
    private String lastMatchTopic;
    private Hashtable<String, Element> metaTable;
    /** topic path that topicIdList need to match */
    private List<String> matchList;
    private Writer output;
    private OutputStreamWriter ditaFileOutput;
    private StringWriter strOutput;
    private final XMLReader reader;
    /** whether to insert links at this topic */
    private boolean startTopic;
    /** Whether to cache the current stream into a buffer for building DOM tree */
    private boolean startDOM;
    /** whether metadata has been written */
    private boolean hasWritten;
    /** array list that is used to keep the hierarchy of topic id */
    private final List<String> topicIdList;
    private final ArrayList<String> topicSpecList;

    private static final Map<String, List<String>> moveTable;
    static{
        final Map<String, List<String>> mt = new HashMap<String, List<String>>(32);
        mt.put(MAP_SEARCHTITLE.matcher, asList(TOPIC_TITLEALTS.localName, TOPIC_SEARCHTITLE.localName));
        mt.put(TOPIC_AUDIENCE.matcher, asList(TOPIC_PROLOG.localName, TOPIC_METADATA.localName, TOPIC_AUDIENCE.localName));
        mt.put(TOPIC_AUTHOR.matcher, asList(TOPIC_PROLOG.localName, TOPIC_AUTHOR.localName));
        mt.put(TOPIC_CATEGORY.matcher, asList(TOPIC_PROLOG.localName, TOPIC_METADATA.localName, TOPIC_CATEGORY.localName));
        mt.put(TOPIC_COPYRIGHT.matcher, asList(TOPIC_PROLOG.localName, TOPIC_COPYRIGHT.localName));
        mt.put(TOPIC_CRITDATES.matcher, asList(TOPIC_PROLOG.localName, TOPIC_CRITDATES.localName));
        mt.put(TOPIC_DATA.matcher, asList(TOPIC_PROLOG.localName, TOPIC_DATA.localName));
        mt.put(TOPIC_DATA_ABOUT.matcher, asList(TOPIC_PROLOG.localName, TOPIC_DATA_ABOUT.localName));
        mt.put(TOPIC_FOREIGN.matcher, asList(TOPIC_PROLOG.localName, TOPIC_FOREIGN.localName));
        mt.put(TOPIC_KEYWORDS.matcher, asList(TOPIC_PROLOG.localName, TOPIC_METADATA.localName, TOPIC_KEYWORDS.localName));
        mt.put(TOPIC_OTHERMETA.matcher, asList(TOPIC_PROLOG.localName, TOPIC_METADATA.localName, TOPIC_OTHERMETA.localName));
        mt.put(TOPIC_PERMISSIONS.matcher, asList(TOPIC_PROLOG.localName, TOPIC_PERMISSIONS.localName));
        mt.put(TOPIC_PRODINFO.matcher, asList(TOPIC_PROLOG.localName, TOPIC_METADATA.localName, TOPIC_PRODINFO.localName));
        mt.put(TOPIC_PUBLISHER.matcher, asList(TOPIC_PROLOG.localName, TOPIC_PUBLISHER.localName));
        mt.put(TOPIC_RESOURCEID.matcher, asList(TOPIC_PROLOG.localName, TOPIC_RESOURCEID.localName));
        mt.put(MAP_MAP.matcher, asList(TOPIC_TITLEALTS.localName, TOPIC_SEARCHTITLE.localName));
        mt.put(TOPIC_SOURCE.matcher, asList(TOPIC_PROLOG.localName, TOPIC_SOURCE.localName));
        mt.put(TOPIC_UNKNOWN.matcher, asList(TOPIC_PROLOG.localName, TOPIC_UNKNOWN.localName));
        moveTable = Collections.unmodifiableMap(mt);
    }

    private static final Map<String, Integer> compareTable;
    static{
        final Map<String, Integer> ct = new HashMap<String, Integer>(32);
        ct.put(TOPIC_TITLEALTS.localName, 1);
        ct.put(TOPIC_NAVTITLE.localName, 2);
        ct.put(TOPIC_SEARCHTITLE.localName, 3);
        ct.put(TOPIC_ABSTRACT.localName, 4);
        ct.put(TOPIC_SHORTDESC.localName, 5);
        ct.put(TOPIC_PROLOG.localName, 6);
        ct.put(TOPIC_AUTHOR.localName, 7);
        ct.put(TOPIC_SOURCE.localName, 8);
        ct.put(TOPIC_PUBLISHER.localName, 9);
        ct.put(TOPIC_COPYRIGHT.localName, 10);
        ct.put(TOPIC_CRITDATES.localName, 11);
        ct.put(TOPIC_PERMISSIONS.localName, 12);
        ct.put(TOPIC_METADATA.localName, 13);
        ct.put(TOPIC_AUDIENCE.localName, 14);
        ct.put(TOPIC_CATEGORY.localName, 15);
        ct.put(TOPIC_KEYWORDS.localName, 16);
        ct.put(TOPIC_PRODINFO.localName, 17);
        ct.put(TOPIC_OTHERMETA.localName, 18);
        ct.put(TOPIC_RESOURCEID.localName, 19);
        ct.put(TOPIC_DATA.localName, 20);
        ct.put(TOPIC_DATA_ABOUT.localName, 21);
        ct.put(TOPIC_FOREIGN.localName, 22);
        ct.put(TOPIC_UNKNOWN.localName, 23);
        compareTable = Collections.unmodifiableMap(ct);
    }



    /**
     * Default constructor of DitaIndexWriter class.
     */
    public DitaMetaWriter() {
        super();
        topicIdList = new ArrayList<String>(16);
        topicSpecList = new ArrayList<String>(16);

        metaTable = null;
        matchList = null;
        output = null;
        startTopic = false;

        try {
            reader = StringUtils.getXMLReader();
            reader.setContentHandler(this);
            reader.setProperty(LEXICAL_HANDLER_PROPERTY,this);
            reader.setFeature(FEATURE_NAMESPACE_PREFIX, true);
        } catch (final Exception e) {
            throw new RuntimeException("Failed to initialize XML parser: " + e.getMessage(), e);
        }

    }


    @Override
    public void characters(final char[] ch, final int start, final int length)
            throws SAXException {
        try {
            writeCharacters(ch, start, length);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e) ;
        }
    }

    /**
     * Check whether the hierarchy of current node match the matchList.
     */
    private boolean checkMatch() {
        if (matchList == null){
            return true;
        }
        final int matchSize = matchList.size();
        final int ancestorSize = topicIdList.size();
        final List<String> tail = topicIdList.subList(ancestorSize - matchSize, ancestorSize);
        return matchList.equals(tail);
    }

    @Override
    public void endDocument() throws SAXException {

        try {
            output.flush();
        } catch (final Exception e) {
            logger.error(e.getMessage(), e) ;
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName)
            throws SAXException {
        if (!startTopic){
            topicIdList.remove(topicIdList.size() - 1);
        }
        try {
            if (startTopic && topicSpecList.contains(qName)){
                if (startDOM){
                    startDOM = false;
                    writeEndElement(TOPIC_TOPIC.localName);
                    output = ditaFileOutput;
                    processDOM();
                }else if (!hasWritten){
                    output = ditaFileOutput;
                    processDOM();
                }
            }
            writeEndElement(qName);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e) ;
        }
    }

    private void processDOM() {
        try{
            final DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            final DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc;

            if (strOutput.getBuffer().length() > 0){
                builder.setErrorHandler(new DITAOTXMLErrorHandler(strOutput.toString(), logger));
                doc = builder.parse(new InputSource(new StringReader(strOutput.toString())));
            }else {
                doc = builder.newDocument();
                doc.appendChild(doc.createElement("topic"));
            }

            final Element root = doc.getDocumentElement();

            for (Entry<String, Element> entry : metaTable.entrySet()) {
                moveMeta(entry, root);
            }

            outputMeta(root);

        } catch (final Exception e){
            logger.error(e.getMessage(), e) ;
        }
        hasWritten = true;
    }

    private void outputMeta(final Node root) throws IOException {
        final NodeList children = root.getChildNodes();
        Node child = null;
        for (int i = 0; i<children.getLength(); i++){
            child = children.item(i);
            switch (child.getNodeType()){
            case Node.TEXT_NODE:
                output((Text) child); break;
            case Node.PROCESSING_INSTRUCTION_NODE:
                output((ProcessingInstruction) child); break;
            case Node.ELEMENT_NODE:
                output((Element) child); break;
            }
        }

    }

    private void moveMeta(final Entry<String, Element> entry, final Element root) {
        final List<String> metaPath = moveTable.get(entry.getKey());
        if (metaPath == null){
            // for the elements which doesn't need to be moved to topic
            // the processor need to neglect them.
            return;
        }
        final Iterator<String> token = metaPath.iterator();
        Node parent = null;
        Node child = root;
        Node current = null;
        NodeList childElements;
        boolean createChild = false;

        while (token.hasNext()){// find the element, if cannot find create one.
            final String next = token.next();
            parent = child;
            final Integer nextIndex = compareTable.get(next);
            Integer currentIndex = null;
            childElements = parent.getChildNodes();
            for (int i = 0; i < childElements.getLength(); i++){
                String name = null;
                current = childElements.item(i);
                if (current.getNodeType() == Node.ELEMENT_NODE){
                    name = current.getNodeName();
                }

                if (name != null && current.getNodeName().equals(next)){
                    child = current;
                    break;
                } else if (name != null){
                    currentIndex = compareTable.get(name);
                    if (currentIndex == null){
                        // if compareTable doesn't contains the number for current name
                        // change to generalized element name to search again
                        final String classValue = ((Element)current).getAttribute(ATTRIBUTE_NAME_CLASS);
                        String generalizedName = classValue.substring(classValue.indexOf(SLASH)+1);
                        generalizedName = generalizedName.substring(0, generalizedName.indexOf(STRING_BLANK));
                        currentIndex = compareTable.get(generalizedName);
                    }
                    if(currentIndex==null){
                        // if there is no generalized tag corresponding this tag
                        logger.error(MessageUtils.getInstance().getMessage("DOTJ038E", name).toString());
                        break;
                    }
                    if(currentIndex.compareTo(nextIndex) > 0){
                        // if currentIndex > nextIndex
                        // it means we have passed to location to insert
                        // and we don't need to go to following child nodes
                        break;
                    }
                }
            }

            if (child==parent){
                // if there is no such child under current element,
                // create one
                child = parent.getOwnerDocument().createElement(next);
                final DitaClass cls = new DitaClass("- topic/" + next + " ");
                ((Element)child).setAttribute(ATTRIBUTE_NAME_CLASS, cls.toString());

                if (current == null ||
                        currentIndex == null ||
                        nextIndex.compareTo(currentIndex)>= 0){
                    parent.appendChild(child);
                    current = null;
                }else {
                    parent.insertBefore(child, current);
                    current = null;
                }

                createChild = true;
            }
        }

        // the root element of entry value is "stub"
        // there isn't any types of node other than Element under "stub"
        // when it is created. Therefore, the item here doesn't need node
        // type check.
        final NodeList list = entry.getValue().getChildNodes();
        for (int i = 0; i < list.getLength(); i++){
            Node item = list.item(i);
            if ((i == 0 && createChild) || MapMetaReader.uniqueSet.contains(entry.getKey()) ){
                item = parent.getOwnerDocument().importNode(item,true);
                parent.replaceChild(item, child);
                child = item; // prevent insert action still want to operate child after it is removed.
            } else {
                item = parent.getOwnerDocument().importNode(item,true);
                parent.insertBefore(item, child);
            }
        }

    }

    @Override
    public void ignorableWhitespace(final char[] ch, final int start, final int length)
            throws SAXException {
        try {
            writeCharacters(ch, start, length);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e) ;
        }
    }

    @Override
    public void processingInstruction(final String target, final String data)
            throws SAXException {
        try {
            writeProcessingInstruction(target, data);
        } catch (final IOException e) {
            logger.error(e.getMessage(), e) ;
        }
    }
    
    public void setMetaTable(final Hashtable<String, Element> metaTable) {
        this.metaTable = metaTable;
    }
    
    private void setMatch(final String match) {
        int index = 0;
        matchList = new ArrayList<String>(16);

        firstMatchTopic = (match.contains(SLASH)) ? match.substring(0, match.indexOf(SLASH)) : match;

        while (index != -1) {
            final int end = match.indexOf(SLASH, index);
            if (end == -1) {
                matchList.add(match.substring(index));
                lastMatchTopic = match.substring(index);
                index = end;
            } else {
                matchList.add(match.substring(index, end));
                index = end + 1;
            }
        }
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName,
            final Attributes atts) throws SAXException {
        final String classAttrValue = atts.getValue(ATTRIBUTE_NAME_CLASS);

        try {
            if (classAttrValue != null &&
                    TOPIC_TOPIC.matches(classAttrValue) &&
                    !topicSpecList.contains(qName)){
                //add topic qName to topicSpecList
                topicSpecList.add(qName);
            }

            if ( startTopic && !startDOM && classAttrValue != null && !hasWritten
                    &&(
                            TOPIC_PROLOG.matches(classAttrValue) ||
                            TOPIC_ABSTRACT.matches(classAttrValue) ||
                            TOPIC_SHORTDESC.matches(classAttrValue) ||
                            TOPIC_TITLEALTS.matches(classAttrValue)
                            )){
                startDOM = true;
                output = strOutput;
                writeStartElement(TOPIC_TOPIC.localName, new AttributesImpl());
            }

            if ( startTopic && classAttrValue != null && !hasWritten &&(
                    TOPIC_TOPIC.matches(classAttrValue) ||
                    TOPIC_RELATED_LINKS.matches(classAttrValue) ||
                    TOPIC_BODY.matches(classAttrValue)
                    )){
                if (startDOM){
                    startDOM = false;
                    writeEndElement(TOPIC_TOPIC.localName);
                    output = ditaFileOutput;
                    processDOM();
                }else{
                    processDOM();
                }

            }

            if ( !startTopic && !ELEMENT_NAME_DITA.equals(qName)){
                if (atts.getValue(ATTRIBUTE_NAME_ID) != null){
                    topicIdList.add(atts.getValue(ATTRIBUTE_NAME_ID));
                }else{
                    topicIdList.add("null");
                }
                if (matchList == null){
                    startTopic = true;
                }else if ( topicIdList.size() >= matchList.size()){
                    //To access topic by id globally
                    startTopic = checkMatch();
                }
            }

            writeStartElement(qName, atts);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e) ;
        }
    }

    /**
     * @deprecated use {@link #write(URI)} instead
     */
    @Deprecated
    @Override
    public void write(final File outputFilename) {
        throw new UnsupportedOperationException();
    }
    
    public void write(final URI outputFilename) {
        File inputFile = null;
        File outputFile = null;
        FileOutputStream fileOutput = null;

        try {
            if(outputFilename.getFragment() != null){
                setMatch(outputFilename.getFragment());
            }else{
                matchList = null;
            }
            startTopic = false;
            hasWritten = false;
            startDOM = false;
            inputFile = toFile(outputFilename);
            outputFile = new File(inputFile.getAbsolutePath() + FILE_EXTENSION_TEMP);
            fileOutput = new FileOutputStream(outputFile);
            ditaFileOutput = new OutputStreamWriter(fileOutput, UTF8);
            strOutput = new StringWriter();
            output = ditaFileOutput;

            topicIdList.clear();
            reader.parse(inputFile.toURI().toString());
        } catch (final Exception e) {
            logger.error(e.getMessage(), e) ;
        }finally {
            try{
                output.close();
            } catch (final Exception e) {
                logger.error(e.getMessage(), e) ;
            }
            try{
                ditaFileOutput.close();
            } catch (final Exception e) {
                logger.error(e.getMessage(), e) ;
            }
        }
        try {
            FileUtils.moveFile(outputFile, inputFile);
        } catch (final Exception e) {
            logger.error(MessageUtils.getInstance().getMessage("DOTJ009E", inputFile.getPath(), outputFile.getPath()).toString());
        }
    }
    
    // DOM to SAX conversion methods
    
    private void output(final ProcessingInstruction instruction) throws IOException{
        String data = instruction.getData();
        if (data != null && data.isEmpty()) {
            data = null;
        }
        writeProcessingInstruction(instruction.getTarget(), data);
    }

    private void output(final Text text) throws IOException{
        final char[] cs = text.getData().toCharArray();
        writeCharacters(cs, 0, cs.length);
    }

    private void output(final Element elem) throws IOException{
        final AttributesImpl atts = new AttributesImpl();
        final NamedNodeMap attrMap = elem.getAttributes();
        for (int i = 0; i<attrMap.getLength(); i++){
            addOrSetAttribute(atts, attrMap.item(i)); 
        }
        writeStartElement(elem.getNodeName(), atts);
        final NodeList children = elem.getChildNodes();
        for (int j = 0; j<children.getLength(); j++){
            final Node child = children.item(j);
            switch (child.getNodeType()){
            case Node.TEXT_NODE:
                output((Text) child); break;
            case Node.PROCESSING_INSTRUCTION_NODE:
                output((ProcessingInstruction) child); break;
            case Node.ELEMENT_NODE:
                output((Element) child); break;
            }
        }
        writeEndElement(elem.getNodeName());
    }
    
    // SAX serializer methods
    
    private void writeStartElement(final String qName, final Attributes atts) throws IOException {
        final int attsLen = atts.getLength();
        output.write(LESS_THAN + qName);
        for (int i = 0; i < attsLen; i++) {
            final String attQName = atts.getQName(i);
            final String attValue = StringUtils.escapeXML(atts.getValue(i));
            output.write(STRING_BLANK + attQName + EQUAL + QUOTATION + attValue + QUOTATION);
        }
        output.write(GREATER_THAN);
    }
    
    private void writeEndElement(final String qName) throws IOException {
        output.write(LESS_THAN + SLASH + qName + GREATER_THAN);
    }
    
    private void writeCharacters(final char[] ch, final int start, final int length) throws IOException {
        output.write(StringUtils.escapeXML(ch, start, length));
    }

    private void writeProcessingInstruction(final String target, final String data) throws IOException {
        final String pi = data != null ? target + STRING_BLANK + data : target;
        output.write(LESS_THAN + QUESTION + pi + QUESTION + GREATER_THAN);
    }
    
}
