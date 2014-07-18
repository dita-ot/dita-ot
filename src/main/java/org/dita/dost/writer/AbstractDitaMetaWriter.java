/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.writer;

import org.dita.dost.exception.DITAOTXMLErrorHandler;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.reader.MapMetaReader;
import org.dita.dost.util.DitaClass;
import org.dita.dost.util.FileUtils;
import org.w3c.dom.*;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

import javax.xml.parsers.DocumentBuilder;
import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.Map.Entry;

import static java.util.Arrays.asList;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.toFile;
import static org.dita.dost.util.XMLUtils.*;

/**
 * Base class for metadata filter that reads dita files and inserts the metadata.
 */
public abstract class AbstractDitaMetaWriter extends AbstractXMLWriter {
    protected String firstMatchTopic;
    protected String lastMatchTopic;
    protected Map<String, Element> metaTable;
    /** topic path that topicIdList need to match */
    protected List<String> matchList;
    protected Writer output;
    protected OutputStreamWriter ditaFileOutput;
    protected StringWriter strOutput;
    protected final XMLReader reader;
    /** whether to insert links at this topic */
    protected boolean startTopic;
    /** Whether to cache the current stream into a buffer for building DOM tree */
    protected boolean startDOM;
    /** whether metadata has been written */
    protected boolean hasWritten;
    /** array list that is used to keep the hierarchy of topic id */
    protected final List<String> topicIdList;
    protected final ArrayList<String> topicSpecList;

    /**
     * Default constructor of DitaIndexWriter class.
     */
    public AbstractDitaMetaWriter() {
        super();
        topicIdList = new ArrayList<String>(16);
        topicSpecList = new ArrayList<String>(16);

        metaTable = null;
        matchList = null;
        output = null;
        startTopic = false;

        try {
            reader = getXMLReader();
            reader.setContentHandler(this);
            reader.setProperty(LEXICAL_HANDLER_PROPERTY,this);
            reader.setFeature(FEATURE_NAMESPACE_PREFIX, true);
        } catch (final Exception e) {
            throw new RuntimeException("Failed to initialize XML parser: " + e.getMessage(), e);
        }

    }

    protected abstract DitaClass getRootClass();

    protected abstract boolean isEndPoint(final String classAttrValue);

    protected abstract boolean isStartPoint(final String classAttrValue);

    protected abstract DitaClass rewriteDitaClass(String next);

    protected abstract Map<String, List<String>> moveTable();

    protected abstract Map<String, Integer> compareTable();

    // Content handler methods

    @Override
    public void characters(final char[] ch, final int start, final int length)
            throws SAXException {
        try {
            writeCharacters(ch, start, length);
        } catch (final IOException e) {
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
        } catch (final IOException e) {
            logger.error(e.getMessage(), e) ;
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName)
            throws SAXException {
        if (!startTopic) {
            topicIdList.remove(topicIdList.size() - 1);
        }
        try {
            if (startTopic && topicSpecList.contains(qName)) {
                if (startDOM) {
                    startDOM = false;
                    writeEndElement(getRootClass().localName);
                    output = ditaFileOutput;
                    processDOM();
                } else if (!hasWritten) {
                    output = ditaFileOutput;
                    processDOM();
                }
            }
            writeEndElement(qName);
        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void processDOM() {
        try {
            final DocumentBuilder builder = getDocumentBuilder();
            Document doc;

            if (strOutput.getBuffer().length() > 0) {
                builder.setErrorHandler(new DITAOTXMLErrorHandler(strOutput.toString(), logger));
                doc = builder.parse(new InputSource(new StringReader(strOutput.toString())));
            } else {
                doc = builder.newDocument();
                doc.appendChild(doc.createElement(getRootClass().localName));
            }

            final Element root = doc.getDocumentElement();

            for (Entry<String, Element> entry : metaTable.entrySet()) {
                moveMeta(entry, root);
            }
            outputMeta(root);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e) ;
        }
        hasWritten = true;
    }

    private void outputMeta(final Node root) throws IOException {
        final NodeList children = root.getChildNodes();
        Node child = null;
        for (int i = 0; i < children.getLength(); i++){
            child = children.item(i);
            switch (child.getNodeType()) {
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
        final List<String> metaPath = moveTable().get(entry.getKey());
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

        while (token.hasNext()) {// find the element, if cannot find create one.
            final String next = token.next();
            parent = child;
            final Integer nextIndex = compareTable().get(next);
            Integer currentIndex = null;
            childElements = parent.getChildNodes();
            for (int i = 0; i < childElements.getLength(); i++) {
                String name = null;
                String classValue = null;
                current = childElements.item(i);
                if (current.getNodeType() == Node.ELEMENT_NODE) {
                    name = current.getNodeName();
                    classValue = ((Element) current).getAttribute(ATTRIBUTE_NAME_CLASS);
                }

                if ((name != null && current.getNodeName().equals(next)) || (classValue != null&&(classValue.contains(next)))) {
                    child = current;
                    break;
                } else if (name != null) {
                    currentIndex = compareTable().get(name);
                    if (currentIndex == null) {
                        // if compareTable doesn't contains the number for current name
                        // change to generalized element name to search again
                        String generalizedName = classValue.substring(classValue.indexOf(SLASH) + 1);
                        generalizedName = generalizedName.substring(0, generalizedName.indexOf(STRING_BLANK));
                        currentIndex = compareTable().get(generalizedName);
                    }
                    if (currentIndex == null) {
                        // if there is no generalized tag corresponding this tag
                        logger.error(MessageUtils.getInstance().getMessage("DOTJ038E", name).toString());
                        break;
                    }
                    if (currentIndex.compareTo(nextIndex) > 0) {
                        // if currentIndex > nextIndex
                        // it means we have passed to location to insert
                        // and we don't need to go to following child nodes
                        break;
                    }
                }
            }

            if (child==parent) {
                // if there is no such child under current element,
                // create one
                child = parent.getOwnerDocument().createElement(next);
                final DitaClass cls = rewriteDitaClass(next);
                ((Element)child).setAttribute(ATTRIBUTE_NAME_CLASS, cls.toString());

                if (current == null ||
                        currentIndex == null ||
                        nextIndex.compareTo(currentIndex)>= 0) {
                    parent.appendChild(child);
                    current = null;
                } else {
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
        } catch (final IOException e) {
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

    public void setMetaTable(final Map<String, Element> metaTable) {
        this.metaTable = metaTable;
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName,
                             final Attributes atts) throws SAXException {
        final String classAttrValue = atts.getValue(ATTRIBUTE_NAME_CLASS);

        try {
            if (getRootClass().matches(classAttrValue) && !topicSpecList.contains(qName)){
                //add topic qName to topicSpecList
                topicSpecList.add(qName);
            }

            if (startTopic) {
                if (!hasWritten) {
                    if (!startDOM && isStartPoint(classAttrValue)) {
                        startDOM = true;
                        output = strOutput;
                        writeStartElement(getRootClass().localName, new AttributesImpl());
                    } else if (isEndPoint(classAttrValue)) {
                        if (startDOM) {
                            startDOM = false;
                            writeEndElement(getRootClass().localName);
                            output = ditaFileOutput;
                        }
                        processDOM();
                    }
                }
            } else {
                if (!ELEMENT_NAME_DITA.equals(qName)) {
                    if (atts.getValue(ATTRIBUTE_NAME_ID) != null) {
                        topicIdList.add(atts.getValue(ATTRIBUTE_NAME_ID));
                    } else {
                        topicIdList.add("null");
                    }
                    if (matchList == null) {
                        startTopic = true;
                    } else if (topicIdList.size() >= matchList.size()) {
                        //To access topic by id globally
                        startTopic = checkMatch();
                    }
                }
            }
            writeStartElement(qName, atts);
        } catch (final IOException e) {
            logger.error(e.getMessage(), e) ;
        }
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

    /**
     * @deprecated use {@link #write(java.net.URI)} instead
     */
    @Deprecated
    @Override
    public void write(final File outputFilename) {
        throw new UnsupportedOperationException();
    }

    public void write(final URI outputFilename) {
        File inputFile = null;
        File outputFile = null;

        try {
            if (outputFilename.getFragment() != null) {
                setMatch(outputFilename.getFragment());
            } else {
                matchList = null;
            }
            startTopic = false;
            hasWritten = false;
            startDOM = false;
            inputFile = toFile(outputFilename);
            outputFile = new File(inputFile.getAbsolutePath() + FILE_EXTENSION_TEMP);
            ditaFileOutput = new OutputStreamWriter(new FileOutputStream(outputFile), UTF8);
            strOutput = new StringWriter();
            output = ditaFileOutput;

            topicIdList.clear();
            reader.parse(inputFile.toURI().toString());
        } catch (final Exception e) {
            logger.error(e.getMessage(), e) ;
        } finally {
            try {
                output.close();
            } catch (final Exception e) {
                logger.error(e.getMessage(), e) ;
            }
            try {
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
            final String attValue = escapeXML(atts.getValue(i));
            output.write(STRING_BLANK + attQName + EQUAL + QUOTATION + attValue + QUOTATION);
        }
        output.write(GREATER_THAN);
    }

    private void writeEndElement(final String qName) throws IOException {
        output.write(LESS_THAN + SLASH + qName + GREATER_THAN);
    }

    private void writeCharacters(final char[] ch, final int start, final int length) throws IOException {
        output.write(escapeXML(ch, start, length));
    }

    private void writeProcessingInstruction(final String target, final String data) throws IOException {
        final String pi = data != null ? target + STRING_BLANK + data : target;
        output.write(LESS_THAN + QUESTION + pi + QUESTION + GREATER_THAN);
    }

}
