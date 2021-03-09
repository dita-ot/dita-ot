/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2007 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.writer;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.module.ChunkModule.ChunkFilenameGenerator;
import org.dita.dost.module.reader.TempFileNameScheme;
import org.dita.dost.util.Job;
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.util.TopicIdParser;
import org.dita.dost.util.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.NamespaceSupport;

import java.io.*;
import java.net.URI;
import java.util.*;

import static javax.xml.XMLConstants.XMLNS_ATTRIBUTE;
import static org.dita.dost.reader.ChunkMapReader.*;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.*;
import static org.dita.dost.util.XMLUtils.*;

/**
 * ChunkTopicParser class, writing chunking content into relative topic files
 * and then update list. Not reusable and not thread-safe.
 */
public abstract class AbstractChunkTopicParser extends AbstractXMLWriter {

    /**
     * Keys and values are absolute chimera paths, i.e. systems paths with fragments
     */
    LinkedHashMap<URI, URI> changeTable = null;
    /**
     * Keys and values are absolute chimera paths, i.e. systems paths with fragments
     */
    Map<URI, URI> conflictTable = null;

    Element rootTopicref = null;

    /** Input file's parent absolute path. */
    URI currentFile = null;

    /** Absolute temporary file */
    URI currentParsingFile = null;
    /** Absolute temporary output file */
    URI outputFile = null;

    String targetTopicId = null;

    String selectMethod = CHUNK_SELECT_DOCUMENT;
    // flag whether output the nested nodes
    boolean include = false;
    boolean skip = false;

    int includelevel = 0;
    int skipLevel = 0;

    final Set<String> topicSpecSet = new HashSet<>(16);

    boolean startFromFirstTopic = false;

    Writer output = null;

    Set<String> topicID = new HashSet<>();

    Map<String, String> currentParsingFileTopicIDChangeTable;

    TempFileNameScheme tempFileNameScheme;
    private ChunkFilenameGenerator chunkFilenameGenerator;

    NamespaceSupport namespaces = new NamespaceSupport();
    HashMap<String, Integer> namespaceMap = new HashMap<String, Integer>();

    @Override
    public void setJob(final Job job) {
        super.setJob(job);
        try {
            tempFileNameScheme = (TempFileNameScheme) Class.forName(job.getProperty("temp-file-name-scheme")).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        tempFileNameScheme.setBaseDir(job.getInputDir());
    }

    abstract public void write(final URI filename);

    @Override
    public void write(final File fileDir) throws DITAOTException {
        throw new UnsupportedOperationException();
    }

    /**
     * Set up the class.
     *
     * @param changeTable   changeTable
     * @param conflictTable conflictTable
     * @param rootTopicref  chunking topicref
     */
    public void setup(final LinkedHashMap<URI, URI> changeTable, final Map<URI, URI> conflictTable,
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
                throw new SAXException(e);
            }
        }
    }

    @Override
    public void endDocument() throws SAXException {
        include = false;
        skip = false;
    }

    @Override
    public abstract void endElement(final String uri, final String localName, final String qName) throws SAXException;

    @Override
    public void ignorableWhitespace(final char[] ch, final int start, final int length) throws SAXException {
        if (include) {
            try {
                output.write(ch, start, length);
            } catch (final IOException e) {
                throw new SAXException(e);
            }
        }
    }

    @Override
    public void processingInstruction(final String target, final String data) throws SAXException {
        if (include
                || PI_WORKDIR_TARGET.equals(target)
                || PI_WORKDIR_TARGET_URI.equals(target)
                || PI_PATH2PROJ_TARGET.equals(target)
                || PI_PATH2PROJ_TARGET_URI.equals(target)
                || PI_PATH2ROOTMAP_TARGET_URI.equals(target)) {
            writeProcessingInstruction(output, target, data);
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
    public abstract void startElement(final String uri, final String localName, final String qName, final Attributes atts)
            throws SAXException;

    void processSelect(String id) {
        if (include) {
            if (CHUNK_SELECT_TOPIC.equals(selectMethod)) {
                // if select method is "select-topic" and current topic is the nested topic in target topic, skip it.
                include = false;
                skipLevel = 1;
                skip = true;
            } else {
                // if select method is "select-document" or "select-branch"
                // and current topic is the nested topic in target topic.
                // if file name has been changed, add an entry in changeTable
                if (!currentParsingFile.equals(outputFile)) {
                    if (id != null) {
                        changeTable.put(setFragment(currentParsingFile, id), setFragment(outputFile, id));
                    } else {
                        changeTable.put(stripFragment(currentParsingFile), stripFragment(outputFile));
                    }
                }
            }
        } else if (skip) {
            skipLevel = 1;
        } else if (id != null && (id.equals(targetTopicId) || startFromFirstTopic)) {
            // if the target topic has not been found and current topic is the target topic
            include = true;
            includelevel = 0;
            skip = false;
            skipLevel = 0;
            startFromFirstTopic = false;
            if (!currentParsingFile.equals(outputFile)) {
                changeTable.put(setFragment(currentParsingFile, id), setFragment(outputFile, id));
            }
        }
    }

    FileInfo generateFileInfo(final URI output) {
        assert output.isAbsolute();
        final URI t = job.tempDirURI.relativize(output);
        final URI result = job.getInputDir().resolve(t);
        final URI temp = tempFileNameScheme.generateTempFileName(result);
        final FileInfo.Builder b = (currentParsingFile != null && job.getFileInfo(stripFragment(currentParsingFile)) != null)
                ? new FileInfo.Builder(job.getFileInfo(stripFragment(currentParsingFile)))
                : new FileInfo.Builder();
        final FileInfo fi = b
                .uri(temp)
                .result(result)
                .format(ATTR_FORMAT_VALUE_DITA)
                .build();
        return fi;
    }

    URI generateOutputFilename(final String id) {
        final FileInfo cfi = job.getFileInfo(stripFragment(currentParsingFile));
        URI result = cfi.result.resolve(id + FILE_EXTENSION_DITA);
        URI temp = tempFileNameScheme.generateTempFileName(result);
        if (id == null || job.getStore().exists(job.tempDirURI.resolve(temp))) { //job.getFileInfo(result) != null
            final URI t = temp;

            result = cfi.result.resolve(generateFilename());
            temp = tempFileNameScheme.generateTempFileName(result);

            final FileInfo.Builder b = new FileInfo.Builder(cfi);
            final FileInfo fi = b
                    .uri(temp)
                    .result(result)
                    .format(ATTR_FORMAT_VALUE_DITA)
                    .build();
            job.add(fi);

            conflictTable.put(job.tempDirURI.resolve(temp), job.tempDirURI.resolve(t));
        }
        return job.tempDirURI.resolve(temp);
    }

    Attributes processAttributes(final Attributes atts) {
        final String cls = atts.getValue(ATTRIBUTE_NAME_CLASS);
        final AttributesImpl resAtts = new AttributesImpl(atts);

        final String id = resAtts.getValue(ATTRIBUTE_NAME_ID);
        if (id != null && TOPIC_TOPIC.matches(cls)) {
            if (topicID.contains(id)) {
                final String newId = chunkFilenameGenerator.generateID();
                topicID.add(newId);

                final URI tmpId = changeTable.get(setFragment(currentParsingFile, id));
                if (tmpId != null && tmpId.equals(setFragment(outputFile, id))) {
                    changeTable.put(setFragment(currentParsingFile, id), setFragment(outputFile, newId));
                }

                final URI tmpVal = changeTable.get(currentParsingFile);
                if (tmpVal != null && tmpVal.equals(setFragment(outputFile, id))) {
                    changeTable.put(currentParsingFile, setFragment(outputFile, newId));
                }
                currentParsingFileTopicIDChangeTable.put(id, newId);
                XMLUtils.addOrSetAttribute(resAtts, ATTRIBUTE_NAME_ID, newId);
            } else {
                topicID.add(id);
            }
        }
        final String href = resAtts.getValue(ATTRIBUTE_NAME_HREF);
        final String scope = resAtts.getValue(ATTRIBUTE_NAME_SCOPE);
        if (href != null && !ATTR_SCOPE_VALUE_EXTERNAL.equals(scope)) {
            // if current @href value needs to be updated
            URI relative = getRelativePath(outputFile, currentParsingFile);
            if (conflictTable.containsKey(outputFile)) {
                final URI realoutputfile = conflictTable.get(outputFile);
                relative = getRelativePath(realoutputfile, currentParsingFile);
            }
            if (href.startsWith(SHARP)) {
                // if @href refers to a location inside current parsing file
                // update @href to point back to current file
                // if the location is moved to chunk, @href will
                // be update again to the new location.
                XMLUtils.addOrSetAttribute(resAtts, ATTRIBUTE_NAME_HREF, relative + href);
            } else if (relative.toString().contains(SLASH)) {
                // if new file is not under the same directory with current file
                // add path information to the @href value
                XMLUtils.addOrSetAttribute(resAtts, ATTRIBUTE_NAME_HREF, relative.resolve(href).toString());
            }
        }
        if (TOPIC_TOPIC.matches(cls) && resAtts.getValue(ATTRIBUTE_NAMESPACE_PREFIX_DITAARCHVERSION) == null) {
            addOrSetAttribute(resAtts, ATTRIBUTE_NAMESPACE_PREFIX_DITAARCHVERSION, DITA_NAMESPACE);
        }
        if (TOPIC_TOPIC.matches(cls) && resAtts.getValue(XMLNS_ATTRIBUTE + ":" + DITA_OT_NS_PREFIX) == null) {
            addOrSetAttribute(resAtts, XMLNS_ATTRIBUTE + ":" + DITA_OT_NS_PREFIX, DITA_OT_NS);
        }
        //Need to add a check to see if root element of the topic uses schema validation or not.
        if (TOPIC_TOPIC.matches(cls) && resAtts.getValue(ATTRIBUTE_NAME_NONAMESPACESCHEMALOCATION) != null) {
            addOrSetAttribute(resAtts, ATTRIBUTE_NAMESPACE_PREFIX_XSI, XML_SCHEMA_NS);
        }

        return resAtts;
    }

    /**
     * Add namespace declaration attribute if required.
     *
     * @param - Atributes from element to be processed.
     * @uri - uri of the namespace of the element to be processed
     *
     * @return - Attributes with the extra namespace declaration, if required.
     */
    Attributes processAttributesNS(Attributes atts, String uri) {
        final AttributesImpl resAtts = new AttributesImpl(processAttributes(atts));

        //Check to see we are at the root element to be processed is the start of the namespaced element
        if (namespaceMap.get(uri) == 1) {
            String prefix = namespaces.getPrefix(uri);
            if (prefix != null) {
                if (prefix != ""){
                    addOrSetAttribute(resAtts, "xmlns:" + prefix, uri);
                }else {
                    addOrSetAttribute(resAtts, "xmlns", uri);
                }
            }
        }

        return resAtts;
    }

    /**
     * Generate file name.
     *
     * @return generated file name
     */
    String generateFilename() {
        return chunkFilenameGenerator.generateFilename(CHUNK_PREFIX, FILE_EXTENSION_DITA);
    }

    /**
     * Generate output file.
     *
     * @return absolute temporary file
     */
    URI generateOutputFile(final URI ref) {
        final FileInfo srcFi = job.getFileInfo(ref);
        final URI newSrc = srcFi.src.resolve(generateFilename());
        final URI tmp = tempFileNameScheme.generateTempFileName(newSrc);

        if (job.getFileInfo(tmp) == null) {
            job.add(new FileInfo.Builder()
                    .result(newSrc)
                    .uri(tmp)
                    .build());
        }

        return job.tempDirURI.resolve(tmp);
    }

    /**
     * Create topicmeta node.
     *
     * @param topic document element of a topic file.
     * @return created and populated topicmeta
     */
    Element createTopicMeta(final Element topic) {
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
     * Get the first topic id from the given dita file.
     *
     * @param ditaTopicFile a dita file.
     * @return The first topic id from the given dita file if success, otherwise
     * {@code null} string is returned.
     */
    String getFirstTopicId(final File ditaTopicFile) {
        assert ditaTopicFile.isAbsolute();
        if (!ditaTopicFile.isAbsolute()) {
            return null;
        }
        final StringBuilder firstTopicId = new StringBuilder();
        final TopicIdParser parser = new TopicIdParser(firstTopicId);
        try {
            job.getStore().transform(ditaTopicFile.toURI(), parser);
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

    // SAX serialization methods

    /**
     * Convenience method to write document start.
     */
    void writeStartDocument(final Writer output) throws SAXException {
        try {
            output.write(XML_HEAD);
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    /**
     * Convenience method to write an end element.
     *
     * @param name element name
     */
    void writeStartElement(final Writer output, final String name, final Attributes atts) throws SAXException {
        try {
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
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    /**
     * Convenience method to write an end element.
     *
     * @param name element name
     */
    void writeEndElement(final Writer output, final String name) throws SAXException {
        try {
            output.write(LESS_THAN);
            output.write(SLASH);
            output.write(name);
            output.write(GREATER_THAN);
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    /**
     * Convenience method to write a processing instruction.
     *
     * @param name  PI name
     * @param value PI value, may be {@code null}
     */
    void writeProcessingInstruction(final Writer output, final String name, final String value)
            throws SAXException {
        try {
            output.write(LESS_THAN);
            output.write(QUESTION);
            output.write(name);
            if (value != null) {
                output.write(STRING_BLANK);
                output.write(value);
            }
            output.write(QUESTION);
            output.write(GREATER_THAN);
        } catch (IOException e) {
            throw new SAXException(e);
        }
    }

    /**
     * start of namespace associated with Element
     *
     * @param prefix
     * @param uri
     */
    public void startPrefixMapping(String prefix, String uri) {
    	namespaces.pushContext();        
        namespaces.declarePrefix(prefix, uri);
    }

    /**
     * end of namespace associated with Element
     *
     * @param prefix
     */
    public void endPrefixMapping(String prefix) {
        namespaces.popContext();
    }

}
