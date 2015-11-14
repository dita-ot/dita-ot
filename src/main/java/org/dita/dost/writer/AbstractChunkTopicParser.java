/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2007 All Rights Reserved.
 */
package org.dita.dost.writer;

import static org.dita.dost.writer.ImageMetadataFilter.*;
import static javax.xml.XMLConstants.*;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.module.ChunkModule.ChunkFilenameGenerator;
import org.dita.dost.util.DitaClass;
import org.dita.dost.util.TopicIdParser;
import org.w3c.dom.*;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

import java.io.*;
import java.net.URI;
import java.util.*;

import static org.dita.dost.reader.ChunkMapReader.*;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.FileUtils.*;
import static org.dita.dost.util.URLUtils.toURI;
import static org.dita.dost.util.XMLUtils.*;

/**
 * ChunkTopicParser class, writing chunking content into relative topic files
 * and then update list. Not reusable and not thread-safe.
 * 
 * <p>
 * TODO: Refactor to be a SAX filter.
 * </p>
 */
public abstract class AbstractChunkTopicParser extends AbstractXMLWriter {

    /** Keys and values are absolute chimera paths, i.e. systems paths with fragments */
    LinkedHashMap<String, String> changeTable = null;
    /** Keys and values are absolute chimera paths, i.e. systems paths with fragments */
    Map<String, String> conflictTable = null;

    Element rootTopicref = null;

    Element topicDoc = null;

    private final boolean separate;
    /** Input file's parent absolute directory path. */
    File filePath = null;

    File currentParsingFile = null;
    File outputFile = null;
    private final Stack<File> outputFileNameStack = new Stack<>();

    String targetTopicId = null;

    String selectMethod = CHUNK_SELECT_DOCUMENT;
    // flag whether output the nested nodes
    boolean include = false;
    private boolean skip = false;

    private int includelevel = 0;
    private int skipLevel = 0;

    private final Set<String> topicSpecSet = new HashSet<>(16);

    boolean startFromFirstTopic = false;

    Writer output = null;

    private final Stack<Writer> outputStack = new Stack<>();
    private final Stack<Element> stubStack = new Stack<>();

    // stub is used as the anchor to mark where to insert generated child
    // topicref inside current topicref
    Element stub = null;

    // siblingStub is similar to stub. The only different is it is used to
    // insert generated topicref sibling to current topicref
    Element siblingStub = null;

    Set<String> topicID = new HashSet<>();

    final Set<String> copyto = new HashSet<>();

    final Set<String> copytoSource = new HashSet<>();

    final Map<URI, URI> copytotarget2source = new HashMap<>();

    Map<String, String> currentParsingFileTopicIDChangeTable;

    private ChunkFilenameGenerator chunkFilenameGenerator;

    /**
     * Constructor.
     */
    AbstractChunkTopicParser(final boolean separate) {
        super();
        this.separate = separate;
    }

    abstract public void write(final File filename) throws DITAOTException;

    /**
     * Set up the class.
     *
     * @param changeTable changeTable
     * @param conflictTable conflictTable
     * @param rootTopicref chunking topicref
     */
    public void setup(final LinkedHashMap<String, String> changeTable, final Map<String, String> conflictTable,
                      final Element rootTopicref,
                      final ChunkFilenameGenerator chunkFilenameGenerator) {
        this.changeTable = changeTable;
        this.rootTopicref = rootTopicref;
        this.conflictTable = conflictTable;
        this.chunkFilenameGenerator = chunkFilenameGenerator;
    }

    // Filter methods

    @Override
    public void characters(final char[] ch, final int start, final int length) throws SAXException {
        if (include) {
            try {
                output.write(escapeXML(ch, start, length));
            } catch (final IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public void endDocument() throws SAXException {
        include = false;
        skip = false;
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        if (skip && skipLevel > 0) {
            skipLevel--;
        } else if (skip) {
            include = true;
            skip = false;
            skipLevel = 0;
        }

        if (include) {
            includelevel--;
            // prevent adding </dita> into output
            if (includelevel >= 0) {
                try {
                    writeEndElement(output, qName);
                } catch(final IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
            if (includelevel == 0 && !CHUNK_SELECT_DOCUMENT.equals(selectMethod)) {
                include = false;
            }
            if (separate && topicSpecSet.contains(qName) && !outputStack.isEmpty()) {
                // if it is end of topic and separate is true
                try {
                    output.close();
                } catch (final IOException e) {
                    logger.error(e.getMessage(), e);
                }
                output = outputStack.pop();
                outputFile = outputFileNameStack.pop();
                stub.getParentNode().removeChild(stub);
                stub = stubStack.pop();
            }
        }
    }

    @Override
    public void ignorableWhitespace(final char[] ch, final int start, final int length) throws SAXException {
        if (include) {
            try {
                output.write(ch, start, length);
            } catch (final IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public void processingInstruction(final String target, final String data) throws SAXException {
        if (include
                || PI_WORKDIR_TARGET.equals(target)
                || PI_WORKDIR_TARGET_URI.equals(target)
                || PI_PATH2PROJ_TARGET.equals(target)
                || PI_PATH2PROJ_TARGET_URI.equals(target)) {
            try {
                writeProcessingInstruction(output, target, data);
            } catch (final IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public void startDocument() throws SAXException {
        // difference between to-content & select-topic
        if (CHUNK_SELECT_DOCUMENT.equals(selectMethod)) {
            // currentParsingFile can never equal outputFile except when
            // chunk="to-content" is set at map level
            if ((currentParsingFile).equals(outputFile)) {
                // if current file serves as root of new chunk
                // include will be set to true in startDocument()
                // in order to copy PIs and <dita> element
                // otherwise, if current file is copied to other file
                // do not copy PIs and <dita>element
                include = true;
                skip = false;
                skipLevel = 0;
            } else {
                include = false;
                startFromFirstTopic = true;
                skip = false;
                skipLevel = 0;
            }
        }
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes atts)
            throws SAXException {
        final String classValue = atts.getValue(ATTRIBUTE_NAME_CLASS);
        final String idValue = atts.getValue(ATTRIBUTE_NAME_ID);

        if (skip && skipLevel > 0) {
            skipLevel++;
        }

        try {
            if (TOPIC_TOPIC.matches(classValue)) {
                topicSpecSet.add(qName);
                final String id = atts.getValue(ATTRIBUTE_NAME_ID);
                // search node by id.
                final Element topic = searchForNode(topicDoc, id, ATTRIBUTE_NAME_ID, TOPIC_TOPIC);

                // only by-topic
                if (separate && include && !CHUNK_SELECT_TOPIC.equals(selectMethod)) {
                    // chunk="by-topic" and next topic element found
                    outputStack.push(output);
                    outputFileNameStack.push(outputFile);
                    outputFile = generateOutputFilename(idValue);
                    output = new OutputStreamWriter(new FileOutputStream(outputFile), UTF8);
                    // write xml header and workdir PI to the new generated file
                    writeStartDocument(output);
                    if (!OS_NAME.toLowerCase().contains(OS_NAME_WINDOWS)) {
                        writeProcessingInstruction(output, PI_WORKDIR_TARGET, filePath.getAbsolutePath());
                    } else {
                        writeProcessingInstruction(output, PI_WORKDIR_TARGET, UNIX_SEPARATOR + filePath);
                    }
                    writeProcessingInstruction(output, PI_WORKDIR_TARGET_URI, filePath.toURI().toString());
                    changeTable.put(outputFile.getPath(), outputFile.getPath());
                    if (idValue != null) {
                        changeTable.put(setFragment(currentParsingFile.getPath(), idValue), setFragment(outputFile.getPath(), idValue));
                    } else {
                        changeTable.put(currentParsingFile.getPath(), outputFile.getPath());
                    }
                    // create a new child element in separate case topicref is equals to parameter
                    // element in separateChunk(Element element)
                    final Element newTopicref = rootTopicref.getOwnerDocument().createElement(MAP_TOPICREF.localName);
                    newTopicref.setAttribute(ATTRIBUTE_NAME_CLASS, MAP_TOPICREF.toString());
                    newTopicref.setAttribute(ATTRIBUTE_NAME_XTRF, ATTR_XTRF_VALUE_GENERATED);
                    newTopicref.setAttribute(ATTRIBUTE_NAME_HREF, toURI(getRelativePath(new File(filePath, FILE_NAME_STUB_DITAMAP), outputFile)).toString());

                    final Element topicmeta = createTopicMeta(topic);
                    newTopicref.appendChild(topicmeta);

                    if (stub != null) {
                        if (includelevel == 0 && siblingStub != null) {
                            // if it is the following sibling topic to the first topic in ditabase
                            // The first topic will not enter the logic at here because when meeting
                            // with first topic in ditabase, the include value is false
                            siblingStub.getParentNode().insertBefore(newTopicref, siblingStub);
                        } else {
                            stub.getParentNode().insertBefore(newTopicref, stub);
                        }
                        stubStack.push(stub);
                        stub = (Element) stub.cloneNode(false);
                        newTopicref.appendChild(stub);
                    }
                }
                if (include) {
                    if (CHUNK_SELECT_TOPIC.equals(selectMethod)) {
                        // if select method is "select-topic" and current topic is the nested topic in target topic, skip it.
                        include = false;
                        skipLevel = 1;
                        skip = true;
                    } else  {
                        // if select method is "select-document" or "select-branch"
                        // and current topic is the nested topic in target topic.
                        // if file name has been changed, add an entry in changeTable
                        if (!currentParsingFile.equals(outputFile)) {
                            if (idValue != null) {
                                changeTable.put(setFragment(currentParsingFile.getPath(), idValue), setFragment(outputFile.getPath(), idValue));
                            } else {
                                changeTable.put(currentParsingFile.getPath(), outputFile.getPath());
                            }
                        }
                    }
                } else if (skip) {
                    skipLevel = 1;
                } else if (idValue != null && (idValue.equals(targetTopicId) || startFromFirstTopic)) {
                    // if the target topic has not been found and current topic is the target topic
                    include = true;
                    includelevel = 0;
                    skip = false;
                    skipLevel = 0;
                    startFromFirstTopic = false;
                    if (!currentParsingFile.equals(outputFile)) {
                        changeTable.put(setFragment(currentParsingFile.getPath(), idValue), setFragment(outputFile.getPath(), idValue));
                    }
                }
            }

            if (include) {
                includelevel++;
                final Attributes resAtts = processAttributes(atts);
                writeStartElement(output, qName, resAtts);
            }
        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private File generateOutputFilename(final String idValue) {
        File newFileName = resolve(filePath, idValue + FILE_EXTENSION_DITA);
        if (idValue == null || newFileName.exists()) {
            final File t = newFileName;
            newFileName = resolve(filePath, generateFilename());
            conflictTable.put(newFileName.getPath(), t.getPath());
        }
        return newFileName;
    }

    private Attributes processAttributes(final Attributes atts) {
        final String classValue = atts.getValue(ATTRIBUTE_NAME_CLASS);
        final AttributesImpl resAtts = new AttributesImpl(atts);
        for (int i = 0; i < resAtts.getLength(); i++) {
            final String attrName = resAtts.getQName(i);
            String attrValue = resAtts.getValue(i);

            if (ATTRIBUTE_NAME_ID.equals(attrName)) {
                final String idValue = attrValue;
                if (TOPIC_TOPIC.matches(classValue)) {
                    // change topic @id if there are conflicts.
                    if (topicID.contains(attrValue)) {
                        final String oldAttrValue = attrValue;
                        attrValue = chunkFilenameGenerator.generateID();
                        topicID.add(attrValue);

                        final String tmpValId = changeTable.get(setFragment(currentParsingFile.getPath(), idValue));
                        if (tmpValId != null && tmpValId.equals(setFragment(outputFile.getPath(), idValue))) {
                            changeTable.put(setFragment(currentParsingFile.getPath(), idValue), setFragment(outputFile.getPath(), attrValue));
                        }

                        final String tmpVal = changeTable.get(currentParsingFile.getPath());
                        if (tmpVal != null && tmpVal.equals(setFragment(outputFile.getPath(), idValue))) {
                            changeTable.put(currentParsingFile.getPath(), setFragment(outputFile.getPath(), attrValue));
                        }
                        currentParsingFileTopicIDChangeTable.put(oldAttrValue, attrValue);
                    } else {
                        topicID.add(attrValue);
                    }
                }
            } else if (ATTRIBUTE_NAME_HREF.equals(attrName)) {
                // update @href value
                if (checkHREF(resAtts)) {
                    // if current @href value needs to be updated
                    String relative = getRelativeUnixPath(outputFile, currentParsingFile.getPath());
                    if (conflictTable.containsKey(outputFile.getPath())) {
                        final String realoutputfile = conflictTable.get(outputFile.getPath());
                        relative = getRelativeUnixPath(realoutputfile, currentParsingFile.getPath());
                    }
                    if (attrValue.startsWith(SHARP)) {
                        // if @href refers to a location inside current parsing file
                        // update @href to point back to current file
                        // if the location is moved to chunk, @href will
                        // be update again to the new location.
                        attrValue = relative + attrValue;
                    } else if (relative.contains(SLASH)) {
                        // if new file is not under the same directory with current file
                        // add path information to the @href value
                        relative = relative.substring(0, relative.lastIndexOf(SLASH));
                        attrValue = resolveTopic(relative, attrValue);
                    }
                }
            }
            resAtts.setValue(i, attrValue);
        }
        if (TOPIC_TOPIC.matches(classValue) && resAtts.getValue(ATTRIBUTE_NAMESPACE_PREFIX_DITAARCHVERSION) == null) {
            addOrSetAttribute(resAtts, ATTRIBUTE_NAMESPACE_PREFIX_DITAARCHVERSION, DITA_NAMESPACE);
        }
        if (TOPIC_TOPIC.matches(classValue) && resAtts.getValue(XMLNS_ATTRIBUTE + ":" + DITA_OT_PREFIX) == null) {
            addOrSetAttribute(resAtts, XMLNS_ATTRIBUTE + ":" + DITA_OT_PREFIX, DITA_OT_NS);
        }
        return resAtts;
    }

    void updateList() {
        try {
            // XXX: Why do we need to update copy-to map because all copy-to have been processed already
            final Map<URI, URI> copytotarget2sourcemaplist = job.getCopytoMap();
            copytotarget2source.putAll(copytotarget2sourcemaplist);
            for (final String file : copytoSource) {
                job.getOrCreateFileInfo(toURI(file)).isCopyToSource = true;
            }
            job.setCopytoMap(copytotarget2source);
            job.write();
        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
        } finally {

        }
    }

    /**
     * Generate file name.
     * 
     * @return generated file name
     */
    String generateFilename() {
        return chunkFilenameGenerator.generateFilename("Chunk", FILE_EXTENSION_DITA);
    }

    /** Check whether href needs to be updated */
    private boolean checkHREF(final Attributes atts) {
        if (atts.getValue(ATTRIBUTE_NAME_HREF) == null) {
            return false;
        }
        return !ATTR_SCOPE_VALUE_EXTERNAL.equals(atts.getValue(ATTRIBUTE_NAME_SCOPE));
    }

    /**
     * Create topicmeta node.
     * 
     * @param topic document element of a topic file.
     * @return created and populated topicmeta
     */
    private Element createTopicMeta(final Element topic) {
        final Document doc = rootTopicref.getOwnerDocument();
        final Element topicmeta = doc.createElement(MAP_TOPICMETA.localName);
        topicmeta.setAttribute(ATTRIBUTE_NAME_CLASS, MAP_TOPICMETA.toString());

        // iterate the node.
        if (topic != null) {
            final Element title = getElementNode(topic, TOPIC_TITLE);
            final Element titlealts = getElementNode(topic, TOPIC_TITLEALTS);
            final Element navtitle = titlealts != null ? getElementNode(titlealts, TOPIC_NAVTITLE) : null;
            final Element shortDesc = getElementNode(topic, TOPIC_SHORTDESC);

            final Element navtitleNode = doc.createElement(TOPIC_NAVTITLE.localName);
            navtitleNode.setAttribute(ATTRIBUTE_NAME_CLASS, TOPIC_NAVTITLE.toString());
            // append navtitle node
            if (navtitle != null) {
                final String text = getText(navtitle);
                final Text titleText = doc.createTextNode(text);
                navtitleNode.appendChild(titleText);
                topicmeta.appendChild(navtitleNode);
            } else {
                final String text = getText(title);
                final Text titleText = doc.createTextNode(text);
                navtitleNode.appendChild(titleText);
                topicmeta.appendChild(navtitleNode);
            }

            // append gentext pi
            final Node pi = doc.createProcessingInstruction("ditaot", "gentext");
            topicmeta.appendChild(pi);

            // append linktext
            final Element linkTextNode = doc.createElement(TOPIC_LINKTEXT.localName);
            linkTextNode.setAttribute(ATTRIBUTE_NAME_CLASS, MAP_LINKTEXT.toString());
            final String text = getText(title);
            final Text textNode = doc.createTextNode(text);
            linkTextNode.appendChild(textNode);
            topicmeta.appendChild(linkTextNode);

            // append genshortdesc pi
            final Node pii = doc.createProcessingInstruction("ditaot", "genshortdesc");
            topicmeta.appendChild(pii);

            // append shortdesc
            final Element shortDescNode = doc.createElement(TOPIC_SHORTDESC.localName);
            shortDescNode.setAttribute(ATTRIBUTE_NAME_CLASS, MAP_SHORTDESC.toString());
            final String shortDescText = getText(shortDesc);
            final Text shortDescTextNode = doc.createTextNode(shortDescText);
            shortDescNode.appendChild(shortDescTextNode);
            topicmeta.appendChild(shortDescNode);
        }
        return topicmeta;
    }

    /**
     *
     * Get the first topic id from the given dita file.
     *
     * @param absolutePathToFile The absolute path to a dita file.
     * @return The first topic id from the given dita file if success, otherwise
     *         {@code null} string is returned.
     */
    String getFirstTopicId(final String absolutePathToFile) {
        assert new File(absolutePathToFile).isAbsolute();
        if (absolutePathToFile == null || !isAbsolutePath(absolutePathToFile)) {
            return null;
        }
        final StringBuilder firstTopicId = new StringBuilder();
        final TopicIdParser parser = new TopicIdParser(firstTopicId);
        try {
            final XMLReader reader = getXMLReader();
            reader.setContentHandler(parser);
            reader.parse(new File(absolutePathToFile).toURI().toString());
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
        if (firstTopicId.length() == 0) {
            return null;
        }
        return firstTopicId.toString();
    }

    // DOM utility methods

    private static final List<String> excludeList;
    static {
        final List<String> el = new ArrayList<>();
        el.add(TOPIC_INDEXTERM.toString());
        el.add(TOPIC_DRAFT_COMMENT.toString());
        el.add(TOPIC_REQUIRED_CLEANUP.toString());
        el.add(TOPIC_DATA.toString());
        el.add(TOPIC_DATA_ABOUT.toString());
        el.add(TOPIC_UNKNOWN.toString());
        el.add(TOPIC_FOREIGN.toString());
        excludeList = Collections.unmodifiableList(el);
    }

    /**
     * Search for the special kind of node by specialized value.
     * @param root place may have the node.
     * @param searchKey keyword for search.
     * @param attrName attribute name for search.
     * @param classValue class value for search.
     * @return element.
     */
    private Element searchForNode(final Element root, final String searchKey, final String attrName,
                                  final DitaClass classValue) {
        if (root == null) {
            return null;
        }
        final Queue<Element> queue = new LinkedList<>();
        queue.offer(root);
        while (!queue.isEmpty()) {
            final Element pe = queue.poll();
            final NodeList pchildrenList = pe.getChildNodes();
            for (int i = 0; i < pchildrenList.getLength(); i++) {
                final Node node = pchildrenList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    queue.offer((Element) node);
                }
            }
            if (pe.getAttribute(ATTRIBUTE_NAME_CLASS) == null || !classValue.matches(pe)) {
                continue;
            }
            final Attr value = pe.getAttributeNode(attrName);
            if (value == null) {
                continue;
            }
            if (searchKey.equals(value.getValue())){
                return pe;
            }
        }
        return null;
    }
    
    /**
     * Get text value of a node.
     *
     * @param root root node
     * @return text value
     */
    public static String getText(final Node root) {
        if (root == null) {
            return "";
        } else {
            final StringBuilder result = new StringBuilder(1024);
            if (root.hasChildNodes()) {
                final NodeList list = root.getChildNodes();
                for (int i = 0; i < list.getLength(); i++) {
                    final Node childNode = list.item(i);
                    if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                        final Element e = (Element)childNode;
                        final String value = e.getAttribute(ATTRIBUTE_NAME_CLASS);
                        if (!excludeList.contains(value)) {
                            final String s = getText(e);
                            result.append(s);
                        }
                    } else if(childNode.getNodeType() == Node.TEXT_NODE) {
                        result.append(childNode.getNodeValue());
                    }
                }
            } else if(root.getNodeType() == Node.TEXT_NODE) {
                result.append(root.getNodeValue());
            }
            return result.toString();
        }
    }

    /**
     * Get specific element node from child nodes.
     *
     * @param element parent node
     * @param classValue DITA class to search for
     * @return element node, {@code null} if not found
     */
    public static Element getElementNode(final Element element, final DitaClass classValue) {
        final NodeList list = element.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            final Node node = list.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                final Element child = (Element) node;
                if (classValue.matches(child)) {
                    return child;
                }
            }
        }
        return null;
    }

    // SAX serialization methods

    /**
     * Convenience method to write document start.
     */
    void writeStartDocument(final Writer output) throws IOException {
        output.write(XML_HEAD);
    }

    /**
     * Convenience method to write an end element.
     *
     * @param name element name
     */
    void writeStartElement(final Writer output, final String name, final Attributes atts) throws IOException {
        output.write(LESS_THAN);
        output.write(name);
        for (int i = 0; i < atts.getLength(); i++) {
            output.write(STRING_BLANK);
            output.write(atts.getQName(i));
            output.write(EQUAL);
            output.write(QUOTATION);
            output.write(escapeXML(atts.getValue(i)));
            output.write(QUOTATION);
        }
        output.write(GREATER_THAN);
    }

    /**
     * Convenience method to write an end element.
     *
     * @param name element name
     */
    void writeEndElement(final Writer output, final String name) throws IOException {
        output.write(LESS_THAN);
        output.write(SLASH);
        output.write(name);
        output.write(GREATER_THAN);
    }

    /**
     * Convenience method to write a processing instruction.
     *
     * @param name PI name
     * @param value PI value, may be {@code null}
     */
    void writeProcessingInstruction(final Writer output, final String name, final String value)
            throws IOException {
        output.write(LESS_THAN);
        output.write(QUESTION);
        output.write(name);
        if (value != null) {
            output.write(STRING_BLANK);
            output.write(value);
        }
        output.write(QUESTION);
        output.write(GREATER_THAN);
    }

}
