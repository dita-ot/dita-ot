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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.dita.dost.exception.DITAOTXMLErrorHandler;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.module.Content;
import org.dita.dost.reader.MapMetaReader;
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

/**
 * DitaMetaWriter reads dita topic file and insert the metadata information into it.
 * 
 * @author Zhang, Yuan Peng
 */
public final class DitaMetaWriter extends AbstractXMLWriter {
    private String firstMatchTopic;
    private String lastMatchTopic;
    private Hashtable<String, Node> metaTable;
    /** topic path that topicIdList need to match */
    private List<String> matchList;
    private boolean needResolveEntity;
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
    private boolean insideCDATA;
    private final ArrayList<String> topicSpecList;

    private static final Map<String, List<String>> moveTable;
    static{
        final Map<String, List<String>> mt = new HashMap<String, List<String>>(INT_32);
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
        final Map<String, Integer> ct = new HashMap<String, Integer>(INT_32);
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
        topicIdList = new ArrayList<String>(INT_16);
        topicSpecList = new ArrayList<String>(INT_16);

        metaTable = null;
        matchList = null;
        needResolveEntity = false;
        output = null;
        startTopic = false;
        insideCDATA = false;

        try {
            reader = StringUtils.getXMLReader();
            reader.setContentHandler(this);
            reader.setProperty(LEXICAL_HANDLER_PROPERTY,this);
            reader.setFeature(FEATURE_NAMESPACE_PREFIX, true);
            reader.setFeature("http://apache.org/xml/features/scanner/notify-char-refs", true);
            reader.setFeature("http://apache.org/xml/features/scanner/notify-builtin-refs", true);
        } catch (final Exception e) {
            throw new RuntimeException("Failed to initialize XML parser: " + e.getMessage(), e);
        }

    }


    @Override
    public void characters(final char[] ch, final int start, final int length)
            throws SAXException {
        if(needResolveEntity){
            try {
                if(insideCDATA) {
                    output.write(ch, start, length);
                } else {
                    output.write(StringUtils.escapeXML(ch, start, length));
                }
            } catch (final Exception e) {
                logger.logError(e.getMessage(), e) ;
            }
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
    public void endCDATA() throws SAXException {
        insideCDATA = false;
        try{
            output.write(CDATA_END);
        }catch(final Exception e){
            logger.logError(e.getMessage(), e) ;
        }
    }

    @Override
    public void endDocument() throws SAXException {

        try {
            output.flush();
        } catch (final Exception e) {
            logger.logError(e.getMessage(), e) ;
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
                    output.write("</topic>");
                    output = ditaFileOutput;
                    processDOM();
                }else if (!hasWritten){
                    output = ditaFileOutput;
                    processDOM();
                }
            }

            output.write(LESS_THAN + SLASH + qName
                    + GREATER_THAN);


        } catch (final Exception e) {
            logger.logError(e.getMessage(), e) ;
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

            final Node root = doc.getDocumentElement();

            final Iterator<Map.Entry<String, Node>> iter = metaTable.entrySet().iterator();

            while (iter.hasNext()){
                final Map.Entry<String, Node> entry = iter.next();
                moveMeta(entry,root);
            }

            outputMeta(root);

        } catch (final Exception e){
            logger.logError(e.getMessage(), e) ;
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

    private void output(final ProcessingInstruction instruction) throws IOException{
        output.write("<?"+instruction.getTarget()+" "+instruction.getData()+"?>");
    }


    private void output(final Text text) throws IOException{
        output.write(StringUtils.escapeXML(text.getData()));
    }


    private void output(final Element elem) throws IOException{
        output.write("<"+elem.getNodeName());
        final NamedNodeMap attrMap = elem.getAttributes();
        for (int i = 0; i<attrMap.getLength(); i++){
            //edited on 2010-08-04 for bug:3038941 start
            //get node name
            final String nodeName = attrMap.item(i).getNodeName();
            //escape entity to avoid entity resolving
            final String nodeValue = StringUtils.escapeXML(attrMap.item(i).getNodeValue());
            //write into target file
            output.write(" "+ nodeName
                    +"=\""+ nodeValue
                    +"\"");
            //edited on 2010-08-04 for bug:3038941 end
        }
        output.write(">");
        final NodeList children = elem.getChildNodes();
        Node child;
        for (int j = 0; j<children.getLength(); j++){
            child = children.item(j);
            switch (child.getNodeType()){
            case Node.TEXT_NODE:
                output((Text) child); break;
            case Node.PROCESSING_INSTRUCTION_NODE:
                output((ProcessingInstruction) child); break;
            case Node.ELEMENT_NODE:
                output((Element) child); break;
            }
        }

        output.write("</"+elem.getNodeName()+">");
    }

    private void moveMeta(final Entry<String, Node> entry, final Node root) {
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
                        logger.logError(MessageUtils.getInstance().getMessage("DOTJ038E", name).toString());
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
                ((Element)child).setAttribute(ATTRIBUTE_NAME_CLASS,"- topic/"+next+" ");

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
                ((Element) parent).insertBefore(item, child);
            }
        }

    }


    @Override
    public void endEntity(final String name) throws SAXException {
        if(!needResolveEntity){
            needResolveEntity = true;
        }
    }

    @Override
    public void ignorableWhitespace(final char[] ch, final int start, final int length)
            throws SAXException {
        try {
            output.write(ch, start, length);
        } catch (final Exception e) {
            logger.logError(e.getMessage(), e) ;
        }
    }

    @Override
    public void processingInstruction(final String target, final String data)
            throws SAXException {
        String pi;
        try {
            pi = (data != null) ? target + STRING_BLANK + data : target;
            output.write(LESS_THAN + QUESTION
                    + pi + QUESTION + GREATER_THAN);
        } catch (final Exception e) {
            logger.logError(e.getMessage(), e) ;
        }
    }

    /**
     * @param content value {@code Hashtable<String, Node>}
     */
    @SuppressWarnings("unchecked")
    @Override
    public void setContent(final Content content) {
        metaTable = (Hashtable<String, Node>) content.getValue();
        if (metaTable == null) {
            throw new IllegalArgumentException("Content value must be non-null Hashtable<String, Node>");
        }
    }
    
    private void setMatch(final String match) {
        int index = 0;
        matchList = new ArrayList<String>(INT_16);

        firstMatchTopic = (match.indexOf(SLASH) != -1) ? match.substring(0, match.indexOf('/')) : match;

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
    public void skippedEntity(final String name) throws SAXException {
        try {
            output.write(StringUtils.getEntity(name));
        } catch (final Exception e) {
            logger.logError(e.getMessage(), e) ;
        }
    }

    @Override
    public void startCDATA() throws SAXException {
        insideCDATA = true;
        try{
            output.write(CDATA_HEAD);
        }catch(final Exception e){
            logger.logError(e.getMessage(), e) ;
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
                output.write("<topic>");
            }

            if ( startTopic && classAttrValue != null && !hasWritten &&(
                    TOPIC_TOPIC.matches(classAttrValue) ||
                    TOPIC_RELATED_LINKS.matches(classAttrValue) ||
                    TOPIC_BODY.matches(classAttrValue)
                    )){
                if (startDOM){
                    startDOM = false;
                    output.write("</topic>");
                    output = ditaFileOutput;
                    processDOM();
                }else{
                    processDOM();
                }

            }

            if ( !startTopic && !ELEMENT_NAME_DITA.equalsIgnoreCase(qName)){
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

            outputElement(qName, atts);
        } catch (final Exception e) {
            logger.logError(e.getMessage(), e) ;
        }
    }

    private void outputElement(final String qName, final Attributes atts) throws IOException {
        final int attsLen = atts.getLength();
        output.write(LESS_THAN + qName);
        for (int i = 0; i < attsLen; i++) {
            final String attQName = atts.getQName(i);
            String attValue;
            attValue = atts.getValue(i);

            // replace '&' with '&amp;'
            //if (attValue.indexOf('&') > 0) {
            //	attValue = StringUtils.replaceAll(attValue, "&", "&amp;");
            //}
            attValue = StringUtils.escapeXML(attValue);

            output.write(new StringBuffer().append(STRING_BLANK)
                    .append(attQName).append(EQUAL).append(QUOTATION)
                    .append(attValue).append(QUOTATION).toString());
        }
        output.write(GREATER_THAN);
    }

    @Override
    public void startEntity(final String name) throws SAXException {
        try {
            needResolveEntity = StringUtils.checkEntity(name);
            if(!needResolveEntity){
                output.write(StringUtils.getEntity(name));
            }
        } catch (final Exception e) {
            logger.logError(e.getMessage(), e) ;
        }
    }

    @Override
    public void write(final String outputFilename) {
        String filename = outputFilename;
        String file = null;
        String topic = null;
        File inputFile = null;
        File outputFile = null;
        FileOutputStream fileOutput = null;

        try {
            if(filename.endsWith(SHARP)){
                // prevent the empty topic id causing error
                filename = filename.substring(0, filename.length()-1);
            }

            if(filename.lastIndexOf(SHARP)!=-1){
                file = filename.substring(0,filename.lastIndexOf(SHARP));
                topic = filename.substring(filename.lastIndexOf(SHARP)+1);
                setMatch(topic);
                startTopic = false;
            }else{
                file = filename;
                matchList = null;
                startTopic = false;
            }
            needResolveEntity = true;
            hasWritten = false;
            startDOM = false;
            inputFile = new File(file);
            outputFile = new File(file + FILE_EXTENSION_TEMP);
            fileOutput = new FileOutputStream(outputFile);
            ditaFileOutput = new OutputStreamWriter(fileOutput, UTF8);
            strOutput = new StringWriter();
            output = ditaFileOutput;

            topicIdList.clear();
            reader.parse(inputFile.toURI().toString());

            output.close();
            if(!inputFile.delete()){
                logger.logError(MessageUtils.getInstance().getMessage("DOTJ009E", inputFile.getPath(), outputFile.getPath()).toString());
            }
            if(!outputFile.renameTo(inputFile)){
                logger.logError(MessageUtils.getInstance().getMessage("DOTJ009E", inputFile.getPath(), outputFile.getPath()).toString());
            }
        } catch (final Exception e) {
            logger.logError(e.getMessage(), e) ;
        }finally {
            try{
                fileOutput.close();
            } catch (final Exception e) {
                logger.logError(e.getMessage(), e) ;
            }
        }
    }
}
