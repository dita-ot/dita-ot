/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2007 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.reader;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.module.ChunkModule.ChunkFilenameGenerator;
import org.dita.dost.module.ChunkModule.ChunkFilenameGeneratorFactory;
import org.dita.dost.module.reader.TempFileNameScheme;
import org.dita.dost.util.DitaClass;
import org.dita.dost.util.Job;
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.util.XMLSerializer;
import org.dita.dost.writer.AbstractDomFilter;
import org.dita.dost.writer.ChunkTopicParser;
import org.dita.dost.writer.SeparateChunkTopicParser;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.unmodifiableSet;
import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.FileUtils.getFragment;
import static org.dita.dost.util.FileUtils.replaceExtension;
import static org.dita.dost.util.StringUtils.join;
import static org.dita.dost.util.StringUtils.split;
import static org.dita.dost.util.URLUtils.*;
import static org.dita.dost.util.XMLUtils.*;

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
    public static final String CHUNK_PREFIX = "Chunk";

    private TempFileNameScheme tempFileNameScheme;
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
    private ProcessingInstruction path2rootmapUrl = null;

    private final ChunkFilenameGenerator chunkFilenameGenerator = ChunkFilenameGeneratorFactory.newInstance();

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

    public void setRootChunkOverride(final String chunkValue) {
        rootChunkOverride = split(chunkValue);
    }

    /**
     * Absolute URI to file being processed
     */
    private URI currentFile;

    /**
     * read input file.
     *
     * @param inputFile filename
     */
    @Override
    public void read(final File inputFile) throws DITAOTException {
        this.currentFile = inputFile.toURI();

        super.read(inputFile);
    }

    @Override
    public Document process(final Document doc) {
        readLinks(doc);
        readProcessingInstructions(doc);

        final Element root = doc.getDocumentElement();
        if (rootChunkOverride != null) {
            final String c = join(rootChunkOverride, " ");
            logger.debug("Use override root chunk \"" + c + "\"");
            root.setAttribute(ATTRIBUTE_NAME_CHUNK, c);
        }
        final Collection<String> rootChunk = split(root.getAttribute(ATTRIBUTE_NAME_CHUNK));
        defaultChunkByToken = getChunkByToken(rootChunk, "by-", CHUNK_BY_DOCUMENT);

        if (rootChunk.contains(CHUNK_TO_CONTENT)) {
            chunkMap(root);
        } else {
            for (final Element currentElem : getChildElements(root)) {
                if (MAP_RELTABLE.matches(currentElem)) {
                    updateReltable(currentElem);
                } else if (MAP_TOPICREF.matches(currentElem)) {
                    processTopicref(currentElem);
                }
            }
        }

        return buildOutputDocument(root);
    }

    private final Set<URI> chunkTopicSet = new HashSet<>();

    /**
     * @return absolute temporary files
     */
    public Set<URI> getChunkTopicSet() {
        return unmodifiableSet(chunkTopicSet);
    }

    private void readLinks(final Document doc) {
        final Element root = doc.getDocumentElement();
        readLinks(root, false, false);
    }

    private void readLinks(final Element elem, final boolean chunk, final boolean disabled) {
        final boolean c = chunk || elem.getAttributeNode(ATTRIBUTE_NAME_CHUNK) != null;
        final boolean d = disabled
                || elem.getAttribute(ATTRIBUTE_NAME_CHUNK).contains(CHUNK_TO_NAVIGATION)
                || (MAPGROUP_D_TOPICGROUP.matches(elem) && !SUBMAP.matches(elem))
                || MAP_RELTABLE.matches(elem);
        final Attr href = elem.getAttributeNode(ATTRIBUTE_NAME_HREF);
        if (href != null) {
            final URI filename = stripFragment(currentFile.resolve(href.getValue()));
            if (c && !d) {
                chunkTopicSet.add(filename);
                final Attr copyTo = elem.getAttributeNode(ATTRIBUTE_NAME_COPY_TO);
                if (copyTo != null) {
                    final URI copyToFile = stripFragment(currentFile.resolve(copyTo.getValue()));
                    chunkTopicSet.add(copyToFile);
                }
            }
        }

        for (final Element topicref : getChildElements(elem, MAP_TOPICREF)) {
            readLinks(topicref, c, d);
        }
    }

    public static String getChunkByToken(final Collection<String> chunkValue, final String category, final String defaultToken) {
        if (chunkValue.isEmpty()) {
            return defaultToken;
        }
        for (final String token : chunkValue) {
            if (token.startsWith(category)) {
                return token;
            }
        }
        return defaultToken;
    }

    /**
     * Process map when "to-content" is specified on map element.
     * <p>
     * TODO: Instead of reclassing map element to be a topicref, add a topicref
     * into the map root and move all map content into that topicref.
     */
    private void chunkMap(final Element root) {
        // create the reference to the new file on root element.
        String newFilename = replaceExtension(new File(currentFile).getName(), FILE_EXTENSION_DITA);
        URI newFile = currentFile.resolve(newFilename);
        if (job.getStore().exists(newFile)) {
            final URI oldFile = newFile;
            newFilename = chunkFilenameGenerator.generateFilename(CHUNK_PREFIX, FILE_EXTENSION_DITA);
            newFile = currentFile.resolve(newFilename);
            // Mark up the possible name changing, in case that references might be updated.
            conflictTable.put(newFile, oldFile.normalize());
        }
        changeTable.put(newFile, newFile);

        // change the class attribute to "topicref"
        final String origCls = root.getAttribute(ATTRIBUTE_NAME_CLASS);
        root.setAttribute(ATTRIBUTE_NAME_CLASS, origCls + MAP_TOPICREF.matcher);
        root.setAttribute(ATTRIBUTE_NAME_HREF, toURI(newFilename).toString());

        createTopicStump(newFile);

        // process chunk
        processTopicref(root);

        // restore original root element
        if (origCls != null) {
            root.setAttribute(ATTRIBUTE_NAME_CLASS, origCls);
        }
        root.removeAttribute(ATTRIBUTE_NAME_HREF);
    }

    /**
     * Create the new topic stump.
     */
    private void createTopicStump(final URI newFile) {
        try (final OutputStream newFileWriter = job.getStore().getOutputStream(newFile)) {
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
                switch (pi.getNodeName()) {
                    case PI_WORKDIR_TARGET:
                        workdir = pi;
                        break;
                    case PI_WORKDIR_TARGET_URI:
                        workdirUrl = pi;
                        break;
                    case PI_PATH2PROJ_TARGET:
                        path2proj = pi;
                        break;
                    case PI_PATH2PROJ_TARGET_URI:
                        path2projUrl = pi;
                        break;
                    case PI_PATH2ROOTMAP_TARGET_URI:
                        path2rootmapUrl = pi;
                        break;
                }
            }
        }
    }

    private void outputMapFile(final URI file, final Document doc) {
        try {
            job.getStore().writeDocument(doc, file);
        } catch (final IOException e) {
            logger.error("Failed to serialize map: " + e.getMessage(), e);
        }
    }

    private Document buildOutputDocument(final Element root) {
        final Document doc = getDocumentBuilder().newDocument();
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
        if (path2rootmapUrl != null) {
            doc.appendChild(doc.importNode(path2rootmapUrl, true));
        }
        doc.appendChild(doc.importNode(root, true));
        return doc;
    }

    private void processTopicref(final Element topicref) {
        final String xtrf = getValue(topicref, ATTRIBUTE_NAME_XTRF);
        if (xtrf != null && xtrf.contains(ATTR_XTRF_VALUE_GENERATED)) {
            return;
        }

        final Collection<String> chunk = split(getValue(topicref, ATTRIBUTE_NAME_CHUNK));

        final URI href = toURI(getValue(topicref, ATTRIBUTE_NAME_HREF));
        final URI copyTo = toURI(getValue(topicref, ATTRIBUTE_NAME_COPY_TO));
        final String scope = getCascadeValue(topicref, ATTRIBUTE_NAME_SCOPE);
        final String chunkByToken = getChunkByToken(chunk, "by-", defaultChunkByToken);

        if (ATTR_SCOPE_VALUE_EXTERNAL.equals(scope)
                || (href != null && !job.getStore().exists(currentFile.resolve(href.toString())))
                || (chunk.isEmpty() && href == null)) {
            processChildTopicref(topicref);
        } else if (chunk.contains(CHUNK_TO_CONTENT)) {
            if (href != null || copyTo != null || topicref.hasChildNodes()) {
                if (chunk.contains(CHUNK_BY_TOPIC)) {
                    logger.warn(MessageUtils.getMessage("DOTJ064W").setLocation(topicref).toString());
                }
                if (href == null) {
                    generateStumpTopic(topicref);
                }
                processCombineChunk(topicref);
            }
        } else if (chunk.contains(CHUNK_TO_NAVIGATION)
                && supportToNavigation) {
            processChildTopicref(topicref);
            processNavitation(topicref);
        } else if (chunkByToken.equals(CHUNK_BY_TOPIC)) {
            if (href != null) {
                processSeparateChunk(topicref);
            }
            processChildTopicref(topicref);
        } else { // chunkByToken.equals(CHUNK_BY_DOCUMENT)
            URI currentPath = null;
            if (copyTo != null) {
                currentPath = currentFile.resolve(copyTo);
            } else if (href != null) {
                currentPath = currentFile.resolve(href);
            }
            if (currentPath != null) {
                changeTable.remove(currentPath);
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
        final URI navmap = currentFile.resolve(newMapFile);
        changeTable.put(stripFragment(navmap), stripFragment(navmap));
        outputMapFile(navmap, buildOutputDocument(root));
    }

    /**
     * Generate file name.
     *
     * @return generated file name
     */
    private String generateFilename() {
        return chunkFilenameGenerator.generateFilename(CHUNK_PREFIX, FILE_EXTENSION_DITA);
    }

    /**
     * Generate stump topic for to-content content.
     *
     * @param topicref topicref without href to generate stump topic for
     */
    private void generateStumpTopic(final Element topicref) {
        final URI result = getResultFile(topicref);
        final URI temp = tempFileNameScheme.generateTempFileName(result);
        final URI absTemp = job.tempDir.toURI().resolve(temp);

        final String name = getBaseName(new File(result).getName());
        String navtitle = getChildElementValueOfTopicmeta(topicref, TOPIC_NAVTITLE);
        if (navtitle == null) {
            navtitle = getValue(topicref, ATTRIBUTE_NAME_NAVTITLE);
        }
        String shortDesc = getChildElementValueOfTopicmeta(topicref, TOPIC_SHORTDESC);
        if (shortDesc == null) {
            shortDesc = getChildElementValueOfTopicmeta(topicref, MAP_SHORTDESC);
        }

        writeChunk(absTemp, name, navtitle, shortDesc);

        // update current element's @href value
        final URI relativePath = getRelativePath(currentFile.resolve(FILE_NAME_STUB_DITAMAP), absTemp);
        topicref.setAttribute(ATTRIBUTE_NAME_HREF, relativePath.toString());
        if (MAPGROUP_D_TOPICGROUP.matches(topicref)) {
            topicref.setAttribute(ATTRIBUTE_NAME_CLASS, MAP_TOPICREF.toString());
        }

        final URI relativeToBase = getRelativePath(job.tempDirURI.resolve("dummy"), absTemp);
        final FileInfo fi = new FileInfo.Builder()
                .uri(temp)
                .result(result)
                .format(ATTR_FORMAT_VALUE_DITA)
                .build();
        job.add(fi);
    }

    private void writeChunk(final URI outputFileName, String id, String title, String shortDesc) {
        try (final OutputStream output = job.getStore().getOutputStream(outputFileName)) {
            final XMLSerializer serializer = XMLSerializer.newInstance(output);
            serializer.writeStartDocument();
            if (title == null && shortDesc == null) {
                //topicgroup with no title, no shortdesc, just need a non titled stub
                serializer.writeStartElement(ELEMENT_NAME_DITA);
                serializer.writeAttribute(DITA_NAMESPACE, ATTRIBUTE_PREFIX_DITAARCHVERSION + ":" + ATTRIBUTE_NAME_DITAARCHVERSION, "1.3");
                serializer.writeEndElement(); // dita
            } else {
                serializer.writeStartElement(TOPIC_TOPIC.localName);
                serializer.writeAttribute(DITA_NAMESPACE, ATTRIBUTE_PREFIX_DITAARCHVERSION + ":" + ATTRIBUTE_NAME_DITAARCHVERSION, "1.3");
                serializer.writeAttribute(ATTRIBUTE_NAME_ID, id);
                serializer.writeAttribute(ATTRIBUTE_NAME_CLASS, TOPIC_TOPIC.toString());
                serializer.writeAttribute(ATTRIBUTE_NAME_DOMAINS, "");
                serializer.writeAttribute(ATTRIBUTE_NAME_SPECIALIZATIONS, "");
                serializer.writeStartElement(TOPIC_TITLE.localName);
                serializer.writeAttribute(ATTRIBUTE_NAME_CLASS, TOPIC_TITLE.toString());
                if (title != null) {
                    serializer.writeCharacters(title);
                }
                serializer.writeEndElement(); // title
                if (shortDesc != null) {
                    serializer.writeStartElement(TOPIC_SHORTDESC.localName);
                    serializer.writeAttribute(ATTRIBUTE_NAME_CLASS, TOPIC_SHORTDESC.toString());
                    serializer.writeCharacters(shortDesc);
                    serializer.writeEndElement(); // shortdesc
                }
                serializer.writeEndElement(); // topic
            }
            serializer.writeEndDocument();
            serializer.close();
        } catch (final IOException | SAXException e) {
            logger.error("Failed to write generated chunk: " + e.getMessage(), e);
        }
    }

    private URI getResultFile(final Element topicref) {
        final FileInfo curr = job.getFileInfo(currentFile);
        final URI copyTo = toURI(getValue(topicref, ATTRIBUTE_NAME_COPY_TO));
        final String id = getValue(topicref, ATTRIBUTE_NAME_ID);

        URI outputFileName;
        if (copyTo != null) {
            outputFileName = curr.result.resolve(copyTo);
        } else if (id != null) {
            outputFileName = curr.result.resolve(id + FILE_EXTENSION_DITA);
        } else {
            final Set<URI> results = job.getFileInfo().stream().map(fi -> fi.result).collect(Collectors.toSet());
            do {
                outputFileName = curr.result.resolve(generateFilename());
            } while (results.contains(outputFileName));
        }
        return outputFileName;
    }

    /**
     * get topicmeta's child(e.g navtitle, shortdesc) tag's value(text-only).
     *
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

    private void processChildTopicref(final Element node) {
        final List<Element> children = getChildElements(node, MAP_TOPICREF);
        for (final Element currentElem : children) {
            final URI href = toURI(getValue(currentElem, ATTRIBUTE_NAME_HREF));
            final String xtrf = currentElem.getAttribute(ATTRIBUTE_NAME_XTRF);
            if (href == null) {
                processTopicref(currentElem);
            } else if (!ATTR_XTRF_VALUE_GENERATED.equals(xtrf)
                    && !currentFile.resolve(href).equals(changeTable.get(currentFile.resolve(href)))) {
                processTopicref(currentElem);
            }
        }
    }

    private void processSeparateChunk(final Element topicref) {
        final SeparateChunkTopicParser chunkParser = new SeparateChunkTopicParser();
        chunkParser.setLogger(logger);
        chunkParser.setJob(job);
        chunkParser.setup(changeTable, conflictTable, topicref, chunkFilenameGenerator);
        chunkParser.write(currentFile);
    }

    private void processCombineChunk(final Element topicref) {
        final ChunkTopicParser chunkParser = new ChunkTopicParser();
        chunkParser.setLogger(logger);
        chunkParser.setJob(job);
        createChildTopicrefStubs(getChildElements(topicref, MAP_TOPICREF));
        chunkParser.setup(changeTable, conflictTable, topicref, chunkFilenameGenerator);
        chunkParser.write(currentFile);
    }
    
    /** Before combining topics in a branch, ensure any descendant topicref with @chunk and no @href has a stub */
    private void createChildTopicrefStubs(final List<Element> topicrefs) {
        if (!topicrefs.isEmpty()) {
            for (final Element currentElem : topicrefs) {
                final String href = getValue(currentElem, ATTRIBUTE_NAME_HREF);
                final String chunk = getValue(currentElem,ATTRIBUTE_NAME_CHUNK);
                if (href == null && chunk != null) {
                    generateStumpTopic(currentElem);
                }
                createChildTopicrefStubs(getChildElements(currentElem, MAP_TOPICREF));
            }
        }
    }

    private void updateReltable(final Element elem) {
        final String href = elem.getAttribute(ATTRIBUTE_NAME_HREF);
        if (href.length() != 0) {
            if (changeTable.containsKey(currentFile.resolve(href))) {
                URI res = getRelativePath(currentFile.resolve(FILE_NAME_STUB_DITAMAP),
                        currentFile.resolve(href));
                final String fragment = getFragment(href);
                if (fragment != null) {
                    res = setFragment(res, fragment);
                }
                elem.setAttribute(ATTRIBUTE_NAME_HREF, res.toString());
            }
        }
        final NodeList children = elem.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            final Node current = children.item(i);
            if (current.getNodeType() == Node.ELEMENT_NODE) {
                final Element currentElem = (Element) current;
                final String cls = currentElem.getAttribute(ATTRIBUTE_NAME_CLASS);
                if (MAP_TOPICREF.matches(cls)) {
                    // FIXME: What should happen here?
                }
            }
        }
    }

    /**
     * Get changed files table.
     *
     * @return map of changed files, absolute temporary files
     */
    public Map<URI, URI> getChangeTable() {
        for (final Map.Entry<URI, URI> e : changeTable.entrySet()) {
            assert e.getKey().isAbsolute();
            assert e.getValue().isAbsolute();
        }
        return Collections.unmodifiableMap(changeTable);
    }

    /**
     * get conflict table.
     *
     * @return conflict table, absolute temporary files
     */
    public Map<URI, URI> getConflicTable() {
        for (final Map.Entry<URI, URI> e : conflictTable.entrySet()) {
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
