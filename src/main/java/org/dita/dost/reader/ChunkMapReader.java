/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2007 All Rights Reserved.
 */
package org.dita.dost.reader;

import static org.dita.dost.util.URLUtils.*;
import static org.dita.dost.util.FileUtils.*;
import static org.apache.commons.io.FilenameUtils.*;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.StringUtils.join;
import static org.dita.dost.util.XMLUtils.close;
import static org.dita.dost.writer.AbstractChunkTopicParser.getElementNode;
import static org.dita.dost.writer.AbstractChunkTopicParser.getText;
import static java.util.Arrays.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.*;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.module.ChunkModule.ChunkFilenameGeneratorFactory;
import org.dita.dost.module.ChunkModule.ChunkFilenameGenerator;
import org.dita.dost.util.*;
import org.dita.dost.writer.AbstractDomFilter;
import org.dita.dost.writer.ChunkTopicParser;
import org.dita.dost.writer.SeparateChunkTopicParser;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 * ChunkMapReader class, read and filter ditamap file for chunking.
 */
// TODO rename this because this is not a reader, it's a filter
public final class ChunkMapReader extends AbstractDomFilter {

    public static final String FILE_NAME_STUB_DITAMAP = "stub.ditamap";
    public static final String FILE_EXTENSION_CHUNK = ".chunk";
    public static final String ATTR_XTRF_VALUE_GENERATED = "generated_by_chunk";

    public static final String CHUNK_SELECT_BRANCH = "select-branch";
    public static final String CHUNK_SELECT_TOPIC = "select-topic";
    public static final String CHUNK_SELECT_DOCUMENT = "select-document";
    private static final String CHUNK_BY_DOCUMENT = "by-document";
    private static final String CHUNK_BY_TOPIC = "by-topic";
    public static final String CHUNK_TO_CONTENT = "to-content";
    public static final String CHUNK_TO_NAVIGATION = "to-navigation";

    private Collection<String> rootChunkOverride;
    private String defaultChunkByToken;

    // ChunkTopicParser assumes keys and values are chimera paths, i.e. systems paths with fragments.
    private final LinkedHashMap<URI, URI> changeTable = new LinkedHashMap<>(128);

    private final Map<URI, URI> conflictTable = new HashMap<>(128);

    private boolean supportToNavigation;

    private ProcessingInstruction workdir = null;
    private ProcessingInstruction workdirUrl = null;
    private ProcessingInstruction path2proj = null;
    private ProcessingInstruction path2projUrl = null;

    private final ChunkFilenameGenerator chunkFilenameGenerator = ChunkFilenameGeneratorFactory.newInstance();

    /**
     * Constructor.
     */
    public ChunkMapReader() {
        super();
    }

    public void setRootChunkOverride(final String chunkValue) {
        rootChunkOverride = split(chunkValue);
    }

    private URI inputFile;
    
    /**
     * read input file.
     * 
     * @param inputFile filename
     */
    @Override
    public void read(final File inputFile) {
        this.inputFile = inputFile.toURI();

        super.read(inputFile);
    }

    @Override
    public Document process(final Document doc) {
        readProcessingInstructions(doc);

        final Element root = doc.getDocumentElement();
        if (rootChunkOverride != null) {
            final String c = join(rootChunkOverride, " ");
            logger.debug("Use override root chunk \"" + c + "\"");
            root.setAttribute(ATTRIBUTE_NAME_CHUNK, c);
        }
        final Collection<String> rootChunkValue =  split(root.getAttribute(ATTRIBUTE_NAME_CHUNK));
        defaultChunkByToken = getChunkByToken(rootChunkValue, "by-", CHUNK_BY_DOCUMENT);
        // chunk value = "to-content"
        // When @chunk="to-content" is specified on "map" element,
        // chunk module will change its @class attribute to "topicref"
        // and process it as if it were a normal topicref wich
        // @chunk="to-content"
        if (rootChunkValue.contains(CHUNK_TO_CONTENT)) {
            chunkMap(root);
        } else {
            // if to-content is not specified on map element
            // process the map element's immediate child node(s)
            // get the immediate child nodes
            final NodeList list = root.getChildNodes();
            for (int i = 0; i < list.getLength(); i++) {
                final Node node = list.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    final Element currentElem = (Element) node;
                    if (MAP_RELTABLE.matches(currentElem)) {
                        updateReltable(currentElem);
                    } else if (MAPGROUP_D_TOPICGROUP.matches(currentElem)) {
                    	processChildTopicref(currentElem);
                    } else if (MAP_TOPICREF.matches(currentElem)) {
                        processTopicref(currentElem);
                    }

                }
            }
        }

        return buildOutputDocument(root);
    }

    public static String getChunkByToken(final Collection<String> chunkValue, final String category, final String defaultToken) {
        if (chunkValue.isEmpty()) {
            return defaultToken;
        }
        for (final String token: chunkValue) {
            if (token.startsWith(category)) {
                return token;
            }
        }
        return defaultToken;
    }

    /**
     * Process map when "to-content" is specified on map element.
     *
     * TODO: Instead of reclassing map element to be a topicref, add a topicref
     * into the map root and move all map content into that topicref.
     */
    private void chunkMap(final Element root) {
        // create the reference to the new file on root element.
        String newFilename = replaceExtension(new File(inputFile).getName(), FILE_EXTENSION_DITA);
        URI newFile = inputFile.resolve(newFilename);
        if (new File(newFile).exists()) {
            final URI oldFile = newFile;
            newFilename = chunkFilenameGenerator.generateFilename("Chunk", FILE_EXTENSION_DITA);
            newFile = inputFile.resolve(newFilename);
            // Mark up the possible name changing, in case that references might be updated.
            conflictTable.put(newFile, oldFile.normalize());
        }
        changeTable.put(newFile, newFile);

        // change the class attribute to "topicref"
        final String originClassValue = root.getAttribute(ATTRIBUTE_NAME_CLASS);
        root.setAttribute(ATTRIBUTE_NAME_CLASS, originClassValue + MAP_TOPICREF.matcher);
        root.setAttribute(ATTRIBUTE_NAME_HREF, toURI(newFilename).toString());

        createTopicStump(newFile);

        // process chunk
        processTopicref(root);

        // restore original root element
        if (originClassValue != null) {
            root.setAttribute(ATTRIBUTE_NAME_CLASS, originClassValue);
        }
        root.removeAttribute(ATTRIBUTE_NAME_HREF);
    }

    /**
     * Create the new topic stump.
     */
    private void createTopicStump(final URI newFile) {
        try (final OutputStream newFileWriter = new FileOutputStream(new File(newFile))) {
            final XMLStreamWriter o = XMLOutputFactory.newInstance().createXMLStreamWriter(newFileWriter, UTF8);
            o.writeStartDocument();
            o.writeProcessingInstruction(PI_WORKDIR_TARGET, UNIX_SEPARATOR + new File(newFile.resolve(".")).getAbsolutePath());
            o.writeProcessingInstruction(PI_WORKDIR_TARGET_URI, newFile.resolve(".").toString());
            o.writeStartElement(ELEMENT_NAME_DITA);
            o.writeEndElement();
            o.writeEndDocument();
            o.close();
            newFileWriter.flush();
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Read processing metadata from processing instructions.
     */
    private void readProcessingInstructions(final Document doc) {
        final NodeList docNodes = doc.getChildNodes();
        for (int i = 0; i < docNodes.getLength(); i++) {
            final Node node = docNodes.item(i);
            if (node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
                final ProcessingInstruction pi = (ProcessingInstruction) node;
                if (pi.getNodeName().equals(PI_WORKDIR_TARGET)) {
                    workdir = pi;
                } else if (pi.getNodeName().equals(PI_WORKDIR_TARGET_URI)) {
                    workdirUrl = pi;
                } else if (pi.getNodeName().equals(PI_PATH2PROJ_TARGET)) {
                    path2proj = pi;
                } else if (pi.getNodeName().equals(PI_PATH2PROJ_TARGET_URI)) {
                    path2projUrl = pi;
                }
            }
        }
    }

    private void outputMapFile(final URI file, final Document doc) {
        StreamResult result = null;
        try {
            final Transformer t = TransformerFactory.newInstance().newTransformer();
            result = new StreamResult(new FileOutputStream(new File(file)));
            t.transform(new DOMSource(doc), result);
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                close(result);
            } catch (final IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private Document buildOutputDocument(final Element root) {
        final Document doc = XMLUtils.getDocumentBuilder().newDocument();
        if (workdir != null) {
            doc.appendChild(doc.importNode(workdir, true));
        }
        if (workdirUrl != null) {
            doc.appendChild(doc.importNode(workdirUrl, true));
        }
        if (path2proj != null) {
            doc.appendChild(doc.importNode(path2proj, true));
        }
        if (path2projUrl != null) {
            doc.appendChild(doc.importNode(path2projUrl, true));
        }
        doc.appendChild(doc.importNode(root, true));
        return doc;
    } 

    public static Collection<String> split(final String value) {
        if (value == null) {
            return Collections.emptyList();
        }
        final String[] tokens = value.trim().split("\\s+");
        return asList(tokens);
    }

    private void processTopicref(final Element topicref) {
        final String xtrfValue = getValue(topicref, ATTRIBUTE_NAME_XTRF);
        if (xtrfValue != null && xtrfValue.contains(ATTR_XTRF_VALUE_GENERATED)) {
            return;
        }

        final Collection<String> chunkValue = split(getValue(topicref, ATTRIBUTE_NAME_CHUNK));

        if (topicref.getAttributeNode(ATTRIBUTE_NAME_HREF) == null && chunkValue.contains(CHUNK_TO_CONTENT)) {
            generateStumpTopic(topicref);
        }

        final URI hrefValue = toURI(getValue(topicref,ATTRIBUTE_NAME_HREF));
        final URI copytoValue = toURI(getValue(topicref, ATTRIBUTE_NAME_COPY_TO));
        final String scopeValue = getCascadeValue(topicref, ATTRIBUTE_NAME_SCOPE);
        final String chunkByToken = getChunkByToken(chunkValue, "by-", defaultChunkByToken);

        if (ATTR_SCOPE_VALUE_EXTERNAL.equals(scopeValue)
                || (hrefValue != null && !toFile(inputFile.resolve(hrefValue.toString())).exists())
                || (chunkValue.isEmpty() && hrefValue == null)) {
            processChildTopicref(topicref);
        } else if (chunkValue.contains(CHUNK_TO_CONTENT)
                && (hrefValue != null || copytoValue != null || topicref.hasChildNodes())) {
            if (chunkValue.contains(CHUNK_BY_TOPIC)) {
                logger.warn(MessageUtils.getInstance().getMessage("DOTJ064W").setLocation(topicref).toString());
            }
            processChunk(topicref, false);
        } else if (chunkValue.contains(CHUNK_TO_NAVIGATION)
                && supportToNavigation) {
            processChildTopicref(topicref);
            processNavitation(topicref);
        } else if (chunkByToken.equals(CHUNK_BY_TOPIC)) {
            processChunk(topicref, true);
            processChildTopicref(topicref);
        } else { // chunkByToken.equals(CHUNK_BY_DOCUMENT)
            URI currentPath = null;
            if (copytoValue != null) {
                currentPath = inputFile.resolve(copytoValue);
            } else if (hrefValue != null) {
                currentPath = inputFile.resolve(hrefValue);
            }
            if (currentPath != null) {
                if (changeTable.containsKey(currentPath)) {
                    changeTable.remove(currentPath);
                }
                final String processingRole = getCascadeValue(topicref, ATTRIBUTE_NAME_PROCESSING_ROLE);
                if (!ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY.equals(processingRole)) {
                    changeTable.put(currentPath, currentPath);
                }
            }
            processChildTopicref(topicref);
        }
    }

    /**
     * Create new map and refer to it with navref.
     * @param topicref
     */
    private void processNavitation(final Element topicref) {
        // create new map's root element
        final Element root = (Element) topicref.getOwnerDocument().getDocumentElement().cloneNode(false);
        // create navref element
        final Element navref = topicref.getOwnerDocument().createElement(MAP_NAVREF.localName);
        final String newMapFile = chunkFilenameGenerator.generateFilename("MAPCHUNK", FILE_EXTENSION_DITAMAP);
        navref.setAttribute(ATTRIBUTE_NAME_MAPREF, newMapFile);
        navref.setAttribute(ATTRIBUTE_NAME_CLASS, MAP_NAVREF.toString());
        // replace topicref with navref
        topicref.getParentNode().replaceChild(navref, topicref);
        root.appendChild(topicref);
        // generate new file
        final URI navmap = inputFile.resolve(newMapFile);
        changeTable.put(stripFragment(navmap), stripFragment(navmap));
        outputMapFile(navmap, buildOutputDocument(root));
    }

    /**
     * Generate file name.
     *
     * @return generated file name
     */
    private String generateFilename() {
        return chunkFilenameGenerator.generateFilename("Chunk", FILE_EXTENSION_DITA);
    }

    /**
     * Generate stump topic for to-content content.
     * @param topicref topicref without href to generate stump topic for
     */
    private void generateStumpTopic(final Element topicref) {
        logger.info("generateStumpTopic: "+ topicref.toString());
        final URI copytoValue = toURI(getValue(topicref, ATTRIBUTE_NAME_COPY_TO));
        final String idValue = getValue(topicref, ATTRIBUTE_NAME_ID);

        URI outputFileName;
        if (copytoValue != null) {
            outputFileName = inputFile.resolve(copytoValue.toString());
        } else if (idValue != null) {
            outputFileName = inputFile.resolve(idValue + FILE_EXTENSION_DITA);
        } else {
            do {
                outputFileName = inputFile.resolve(generateFilename());
            } while (new File(outputFileName).exists());
        }

        final String id = getBaseName(new File(outputFileName).getName());
        String navtitleValue = getChildElementValueOfTopicmeta(topicref, TOPIC_NAVTITLE);
        if (navtitleValue == null) {
            navtitleValue = getValue(topicref, ATTRIBUTE_NAME_NAVTITLE);
        }
        if (navtitleValue == null) {
            navtitleValue = id;
        }
        final String shortDescValue = getChildElementValueOfTopicmeta(topicref, MAP_SHORTDESC);

        OutputStream output = null;
        try {
            output = new FileOutputStream(new File(outputFileName));
            final XMLSerializer serializer = XMLSerializer.newInstance(output);
            serializer.writeStartDocument();
            serializer.writeStartElement(TOPIC_TOPIC.localName);
            serializer.writeAttribute(DITA_NAMESPACE, ATTRIBUTE_PREFIX_DITAARCHVERSION + ":" + ATTRIBUTE_NAME_DITAARCHVERSION, "1.2");
            serializer.writeAttribute(ATTRIBUTE_NAME_ID, id);
            serializer.writeAttribute(ATTRIBUTE_NAME_CLASS, TOPIC_TOPIC.toString());
            serializer.writeAttribute(ATTRIBUTE_NAME_DOMAINS, "");
            serializer.writeStartElement(TOPIC_TITLE.localName);
            serializer.writeAttribute(ATTRIBUTE_NAME_CLASS, TOPIC_TITLE.toString());
            serializer.writeCharacters(navtitleValue);
            serializer.writeEndElement(); // title
            if (shortDescValue != null) {
                serializer.writeStartElement(TOPIC_SHORTDESC.localName);
                serializer.writeAttribute(ATTRIBUTE_NAME_CLASS, TOPIC_SHORTDESC.toString());
                serializer.writeCharacters(shortDescValue);
                serializer.writeEndElement(); // shortdesc
            }
            serializer.writeEndElement(); // topic
            serializer.writeEndDocument();
            serializer.close();
        } catch (final IOException | SAXException e) {
            logger.error("Failed to write generated chunk: " + e.getMessage(), e);
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    logger.error("Failed to close output stream: " + e.getMessage(), e);
                }
            }
        }

        // update current element's @href value
        final URI relativePath = getRelativePath(inputFile.resolve(FILE_NAME_STUB_DITAMAP), outputFileName);
        topicref.setAttribute(ATTRIBUTE_NAME_HREF, relativePath.toString());

        final URI relativeToBase = URLUtils.getRelativePath(job.tempDir.toURI().resolve("dummy"), outputFileName);
        job.add(new Job.FileInfo.Builder().uri(relativeToBase).format(ATTR_FORMAT_VALUE_DITA).build());
    }

    /**
     * get topicmeta's child(e.g navtitle, shortdesc) tag's value(text-only).
     * @param element input element
     * @return text value
     */
    private String getChildElementValueOfTopicmeta(final Element element, final DitaClass classValue) {
        if (element.hasChildNodes()) {
            final Element topicMeta = getElementNode(element, MAP_TOPICMETA);
            if (topicMeta != null) {
                final Element elem = getElementNode(topicMeta, classValue);
                if (elem != null) {
                    return getText(elem);
                }
            }
        }
        return null;
    }

    public static String getValue(final Element elem, final String attrName) {
        final Attr attr = elem.getAttributeNode(attrName);
        if (attr != null && !attr.getValue().isEmpty()) {
            return attr.getValue();
        }
        return null;
    }

    public static String getCascadeValue(final Element elem, final String attrName) {
        Element current = elem;
        while (current != null) {
            final Attr attr = current.getAttributeNode(attrName);
            if (attr != null) {
                return attr.getValue();
            }
            final Node parent = current.getParentNode();
            if (parent != null && parent.getNodeType() == Node.ELEMENT_NODE) {
                current = (Element) parent;
            } else {
                break;
            }
        }
        return null;
    }

    private void processChildTopicref(final Element node) {
        final NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            final Node current = children.item(i);
            if (current.getNodeType() == Node.ELEMENT_NODE) {
                final Element currentElem = (Element) current;
                final String classValue = currentElem.getAttribute(ATTRIBUTE_NAME_CLASS);
                if (MAP_TOPICREF.matches(classValue)) {
                    final String hrefValue = currentElem.getAttribute(ATTRIBUTE_NAME_HREF);
                    final String xtrfValue = currentElem.getAttribute(ATTRIBUTE_NAME_XTRF);
                    if (hrefValue.length() == 0) {
                        processTopicref(currentElem);
                    } else if (!ATTR_XTRF_VALUE_GENERATED.equals(xtrfValue)
                            && !inputFile.resolve(hrefValue).equals(changeTable.get(inputFile.resolve(hrefValue)))) {
                        processTopicref(currentElem);
                    }
                }
            }
        }
    }

    private void processChunk(final Element topicref, final boolean separate) {
        try {
            if (separate) {
                final SeparateChunkTopicParser chunkParser = new SeparateChunkTopicParser();
                chunkParser.setLogger(logger);
                chunkParser.setJob(job);
                chunkParser.setup(changeTable, conflictTable, topicref, chunkFilenameGenerator);
                chunkParser.write(new File(inputFile.resolve(".")));
            } else {
                final ChunkTopicParser chunkParser = new ChunkTopicParser();
                chunkParser.setLogger(logger);
                chunkParser.setJob(job);
                chunkParser.setup(changeTable, conflictTable, topicref, chunkFilenameGenerator);
                chunkParser.write(new File(inputFile.resolve(".")));
            }
        } catch (final DITAOTException e) {
            logger.error("Failed to process chunk: " + e.getMessage(), e);
        }
    }

    private void updateReltable(final Element elem) {
        final String hrefValue = elem.getAttribute(ATTRIBUTE_NAME_HREF);
        if (hrefValue.length() != 0) {
            if (changeTable.containsKey(inputFile.resolve(hrefValue))) {
                URI resulthrefValue = getRelativePath(inputFile.resolve(FILE_NAME_STUB_DITAMAP),
                                                      inputFile.resolve(hrefValue));
                final String fragment = getFragment(hrefValue);
                if (fragment != null) {
                    resulthrefValue = setFragment(resulthrefValue, fragment);
                }
                elem.setAttribute(ATTRIBUTE_NAME_HREF, resulthrefValue.toString());
            }
        }
        final NodeList children = elem.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            final Node current = children.item(i);
            if (current.getNodeType() == Node.ELEMENT_NODE) {
                final Element currentElem = (Element) current;
                final String classValue = currentElem.getAttribute(ATTRIBUTE_NAME_CLASS);
                if (MAP_TOPICREF.matches(classValue)) {
                    // FIXME: What should happen here?
                }
            }
        }
    }

    /**
     * Get changed files table.
     * 
     * @return map of changed files
     */
    public Map<URI, URI> getChangeTable() {
        for (final Map.Entry<URI, URI> e: changeTable.entrySet()) {
            assert e.getKey().isAbsolute();
            assert e.getValue().isAbsolute();
        }
        return Collections.unmodifiableMap(changeTable);
    }

    /**
     * get conflict table.
     * 
     * @return conflict table
     */
    public Map<URI, URI> getConflicTable() {
        for (final Map.Entry<URI, URI> e: conflictTable.entrySet()) {
            assert e.getKey().isAbsolute();
            assert e.getValue().isAbsolute();
        }
        return conflictTable;
    }

    /**
     * Support chunk token to-navigation.
     * 
     * @param supportToNavigation flag to enable to-navigation support
     */
    public void supportToNavigation(final boolean supportToNavigation) {
        this.supportToNavigation = supportToNavigation;
    }

}
