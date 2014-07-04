/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2007 All Rights Reserved.
 */
package org.dita.dost.reader;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.writer.DitaWriter.*;
import static org.dita.dost.util.FileUtils.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.module.ChunkModule.ChunkFilenameGeneratorFactory;
import org.dita.dost.module.ChunkModule.ChunkFilenameGenerator;
import org.dita.dost.util.Job;
import org.dita.dost.writer.ChunkTopicParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;

/**
 * ChunkMapReader class, read ditamap file for chunking.
 * 
 */
public final class ChunkMapReader implements AbstractReader {

    public static final String FILE_NAME_STUB_DITAMAP = "stub.ditamap";
    public static final String FILE_EXTENSION_CHUNK = ".chunk";
    public static final String ATTR_XTRF_VALUE_GENERATED = "generated_by_chunk";

    public static final String CHUNK_BY_DOCUMENT = "by-document";
    public static final String CHUNK_BY_TOPIC = "by-topic";
    public static final String CHUNK_TO_CONTENT = "to-content";
    public static final String CHUNK_TO_NAVIGATION = "to-navigation";

    private DITAOTLogger logger;

    private boolean chunkByTopic = false;

    /** Input file's parent directory */
    private File filePath = null;
    // ChunkTopicParser assumes keys and values are chimera paths, i.e. systems paths with fragments.
    private LinkedHashMap<String, String> changeTable = null;

    private Map<String, String> conflictTable = null;

    private Set<String> refFileSet = null;

    private String transtype = null;

    private ProcessingInstruction workdir = null;
    private ProcessingInstruction workdirUrl = null;
    private ProcessingInstruction path2proj = null;
    private ProcessingInstruction path2projUrl = null;

    private String processingRole = ATTR_PROCESSING_ROLE_VALUE_NORMAL;
    private final ChunkFilenameGenerator chunkFilenameGenerator = ChunkFilenameGeneratorFactory.newInstance();
    private Job job;

    /**
     * Constructor.
     */
    public ChunkMapReader() {
        super();
        chunkByTopic = false;// By default, processor should chunk by document.
        changeTable = new LinkedHashMap<String, String>(128);
        refFileSet = new HashSet<String>(128);
        conflictTable = new HashMap<String, String>(128);
    }

    public void setJob(final Job job) {
        this.job = job;
    }
    
    private File inputFile;
    
    /**
     * read input file.
     * 
     * @param inputFile filename
     */
    @Override
    public void read(final File inputFile) {
        this.inputFile = inputFile;
        filePath = inputFile.getParentFile();
        
        Document doc = null;
        try {
            final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            doc = builder.parse(inputFile);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            return;
        }
        
        doc = process(doc);
        
        try {
            outputMapFile(inputFile, doc);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }

    }

    private Document process(final Document doc) {
        try {   
            // workdir and path2proj processing instructions.
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

            // get the document node
            final Element root = doc.getDocumentElement();
            final String rootChunkValue = root.getAttribute(ATTRIBUTE_NAME_CHUNK);
            chunkByTopic = rootChunkValue != null && rootChunkValue.contains(CHUNK_BY_TOPIC);
            // chunk value = "to-content"
            // When @chunk="to-content" is specified on "map" element,
            // chunk module will change its @class attribute to "topicref"
            // and process it as if it were a normal topicref wich
            // @chunk="to-content"
            if (rootChunkValue != null && rootChunkValue.contains(CHUNK_TO_CONTENT)) {
                // if to-content is specified on map element

                // create the reference to the new file on root element.
                String newFilename = inputFile.getName().substring(0,
                        inputFile.getName().indexOf(FILE_EXTENSION_DITAMAP))
                        + FILE_EXTENSION_DITA;
                File newFile = new File(inputFile.getParentFile().getAbsolutePath(), newFilename);
                if (newFile.exists()) {
                    newFilename = chunkFilenameGenerator.generateFilename("Chunk", FILE_EXTENSION_DITA);
                    final String oldpath = newFile.getAbsolutePath();
                    newFile = resolve(inputFile.getParentFile().getAbsolutePath(), newFilename);
                    // Mark up the possible name changing, in case that
                    // references might be updated.
                    conflictTable.put(newFile.getAbsolutePath(), normalize(oldpath).getPath());
                }
                // change the class attribute to "topicref"
                final String originClassValue = root.getAttribute(ATTRIBUTE_NAME_CLASS);
                root.setAttribute(ATTRIBUTE_NAME_CLASS, originClassValue + MAP_TOPICREF.matcher);
                root.setAttribute(ATTRIBUTE_NAME_HREF, newFilename);

                // create the new file
                OutputStream newFileWriter = null;
                try {
                    newFileWriter = new FileOutputStream(newFile);
                    final XMLStreamWriter o = XMLOutputFactory.newInstance().createXMLStreamWriter(newFileWriter, UTF8);
                    o.writeStartDocument();
                    o.writeProcessingInstruction(PI_WORKDIR_TARGET, UNIX_SEPARATOR
                            + newFile.getParentFile().getAbsolutePath());
                    o.writeProcessingInstruction(PI_WORKDIR_TARGET_URI, newFile.getParentFile().toURI().toString());
                    o.writeStartElement(ELEMENT_NAME_DITA);
                    o.writeEndElement();
                    o.writeEndDocument();
                    o.close();
                    newFileWriter.flush();
                } catch (final Exception e) {
                    logger.error(e.getMessage(), e);
                } finally {
                    try {
                        if (newFileWriter != null) {
                            newFileWriter.close();
                        }
                    } catch (final Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }

                // process chunk
                processTopicref(root);

                // add newly created file to changeTable
                changeTable.put(newFile.getAbsolutePath(), newFile.getAbsolutePath());

                // restore original root element
                if (originClassValue != null) {
                    root.setAttribute(ATTRIBUTE_NAME_CLASS, originClassValue);
                }
                // remove the href
                root.removeAttribute(ATTRIBUTE_NAME_HREF);

            } else {
                // if to-content is not specified on map element
                // process the map element's immediate child node(s)
                // get the immediate child nodes
                final NodeList list = root.getChildNodes();
                for (int i = 0; i < list.getLength(); i++) {
                    final Node node = list.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        final Element currentElem = (Element) node;
                        final Node classAttr = node.getAttributes().getNamedItem(ATTRIBUTE_NAME_CLASS);

                        String classValue = null;
                        if (classAttr != null) {
                            classValue = classAttr.getNodeValue();
                        }

                        if (classValue != null && MAP_RELTABLE.matches(classValue)) {
                            updateReltable(currentElem);
                        }
                        if (classValue != null && MAP_TOPICREF.matches(classValue)
                                && !MAPGROUP_D_TOPICGROUP.matches(classValue)) {
                            processTopicref(currentElem);
                        }

                    }
                }
            }
            
            return buildOutputDocument(root);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
    }

    private void outputMapFile(final File file, final Document doc) {  
        OutputStream output = null;
        try {
            output = new FileOutputStream(file);
            final Transformer t = TransformerFactory.newInstance().newTransformer();
            t.transform(new DOMSource(doc), new StreamResult(output));
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                if (output != null) {
                    output.close();
                }
            } catch (final Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private Document buildOutputDocument(final Element root) {
        Document doc = null;
        try {
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (final ParserConfigurationException e) {
            throw new RuntimeException("Failed to create empty document: " + e.getMessage(), e);
        }
        
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

    // process chunk
    private void processTopicref(final Element node) {
        String hrefValue = null;
        String chunkValue = null;
        String copytoValue = null;
        String scopeValue = null;
        String classValue = null;
        String xtrfValue = null;
        String processValue = null;
        final String tempRole = processingRole;
        boolean prevChunkByTopic = false;

        final Node hrefAttr = node.getAttributeNode(ATTRIBUTE_NAME_HREF);
        final Node chunkAttr = node.getAttributeNode(ATTRIBUTE_NAME_CHUNK);
        final Node copytoAttr = node.getAttributeNode(ATTRIBUTE_NAME_COPY_TO);
        final Node scopeAttr = node.getAttributeNode(ATTRIBUTE_NAME_SCOPE);
        final Node classAttr = node.getAttributeNode(ATTRIBUTE_NAME_CLASS);
        final Node xtrfAttr = node.getAttributeNode(ATTRIBUTE_NAME_XTRF);
        final Node processAttr = node.getAttributeNode(ATTRIBUTE_NAME_PROCESSING_ROLE);

        if (hrefAttr != null) {
            hrefValue = hrefAttr.getNodeValue();
        }
        if (chunkAttr != null) {
            chunkValue = chunkAttr.getNodeValue();
        }
        if (copytoAttr != null) {
            copytoValue = copytoAttr.getNodeValue();
        }
        if (scopeAttr != null) {
            scopeValue = scopeAttr.getNodeValue();
        }
        if (classAttr != null) {
            classValue = classAttr.getNodeValue();
        }
        if (xtrfAttr != null) {
            xtrfValue = xtrfAttr.getNodeValue();
        }
        if (processAttr != null) {
            processValue = processAttr.getNodeValue();
            processingRole = processValue;
        }
        // This file is chunked(by-topic)
        if (xtrfValue != null && xtrfValue.contains(ATTR_XTRF_VALUE_GENERATED)) {
            return;
        }

        // set chunkByTopic if there is "by-topic" or "by-document" in
        // chunkValue
        if (chunkValue != null && (chunkValue.contains(CHUNK_BY_TOPIC) || chunkValue.contains(CHUNK_BY_DOCUMENT))) {
            // a temp value to store the flag
            prevChunkByTopic = chunkByTopic;
            // if there is "by-topic" then chunkByTopic should be set to true;
            chunkByTopic = chunkValue.contains(CHUNK_BY_TOPIC);
        }

        if (ATTR_SCOPE_VALUE_EXTERNAL.equalsIgnoreCase(scopeValue)
                || (hrefValue != null && !resolve(filePath, hrefValue).exists())
                || (MAPGROUP_D_TOPICHEAD.matches(classValue) && chunkValue == null) ||
                // //support topicref without href attribute
                (MAP_TOPICREF.matches(classValue) && chunkValue == null && hrefValue == null)) {
            // ||
            // (ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY.equalsIgnoreCase(processValue)))
            // {
            // Skip external links or non-existing href files.
            // Skip topic head entries.
            // Skip @processing-role=resource-only entries.
            if (chunkValue != null && (chunkValue.contains(CHUNK_BY_TOPIC) || chunkValue.contains(CHUNK_BY_DOCUMENT))) {
                chunkByTopic = prevChunkByTopic;
            }
            processChildTopicref(node);
            // chunk "to-content"
        } else if (chunkValue != null
                &&
                // edited on 20100818 for bug:3042978
                chunkValue.contains(CHUNK_TO_CONTENT)
                && (hrefAttr != null || copytoAttr != null || node.hasChildNodes())) {
            // if this is the start point of the content chunk
            // TODO very important start point(to-content).
            processChunk(node, false, chunkByTopic);
        } else if (chunkValue != null && chunkValue.contains(CHUNK_TO_NAVIGATION)
                && INDEX_TYPE_ECLIPSEHELP.equals(transtype)) {
            // if this is the start point of the navigation chunk
            if (chunkValue != null && (chunkValue.contains(CHUNK_BY_TOPIC) || chunkValue.contains(CHUNK_BY_DOCUMENT))) {
                // restore the chunkByTopic value
                chunkByTopic = prevChunkByTopic;
            }
            processChildTopicref(node);
            // create new map file
            // create new map's root element
            final Element root = (Element) node.getOwnerDocument().getDocumentElement().cloneNode(false);
            // create navref element
            final Element navref = node.getOwnerDocument().createElement(MAP_NAVREF.localName);
            final String newMapFile = chunkFilenameGenerator.generateFilename("MAPCHUNK", FILE_EXTENSION_DITAMAP);
            navref.setAttribute(MAPGROUP_D_MAPREF.localName, newMapFile);
            navref.setAttribute(ATTRIBUTE_NAME_CLASS, MAP_NAVREF.toString());
            // replace node with navref
            node.getParentNode().replaceChild(navref, node);
            root.appendChild(node);
            // generate new file
            final File navmap = resolve(filePath, newMapFile);
            changeTable.put(navmap.getPath(), navmap.getPath());
            outputMapFile(navmap, buildOutputDocument(root));
            // chunk "by-topic"
        } else if (chunkByTopic) {
            // TODO very important start point(by-topic).
            processChunk(node, true, chunkByTopic);
            if (chunkValue != null && (chunkValue.contains(CHUNK_BY_TOPIC) || chunkValue.contains(CHUNK_BY_DOCUMENT))) {
                chunkByTopic = prevChunkByTopic;
            }
            processChildTopicref(node);
        } else {
            String currentPath = null;
            if (copytoValue != null) {
                currentPath = resolve(filePath, copytoValue).getPath();
            } else if (hrefValue != null) {
                currentPath = resolve(filePath, hrefValue).getPath();
            }
            if (currentPath != null) {
                if (changeTable.containsKey(currentPath)) {
                    changeTable.remove(currentPath);
                }
                if (!refFileSet.contains(currentPath)) {
                    refFileSet.add(currentPath);
                }
            }

            // Here, we have a "by-document" chunk, simply
            // send it to the output.
            if ((chunkValue != null || !chunkByTopic) && currentPath != null
                    && !ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY.equals(processingRole)) {
                changeTable.put(currentPath, currentPath);
            }

            if (chunkValue != null && (chunkValue.contains(CHUNK_BY_TOPIC) || chunkValue.contains(CHUNK_BY_DOCUMENT))) {
                chunkByTopic = prevChunkByTopic;
            }

            processChildTopicref(node);
        }

        // restore chunkByTopic if there is "by-topic" or "by-document" in
        // chunkValue
        if (chunkValue != null && (chunkValue.contains(CHUNK_BY_TOPIC) || chunkValue.contains(CHUNK_BY_DOCUMENT))) {
            chunkByTopic = prevChunkByTopic;
        }

        processingRole = tempRole;

    }

    private void processChildTopicref(final Node node) {
        final NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            final Node current = children.item(i);
            if (current.getNodeType() == Node.ELEMENT_NODE) {
                final Element currentElem = (Element) current;
                final String classValue = currentElem.getAttribute(ATTRIBUTE_NAME_CLASS);
                final String hrefValue = currentElem.getAttribute(ATTRIBUTE_NAME_HREF);
                final String xtrfValue = currentElem.getAttribute(ATTRIBUTE_NAME_XTRF);
                if (MAP_TOPICREF.matches(classValue)) {
                    if ((hrefValue.length() != 0 && !ATTR_XTRF_VALUE_GENERATED.equals(xtrfValue) && !resolve(
                            filePath, hrefValue).getPath().equals(changeTable.get(resolve(filePath, hrefValue).getPath())))
                            || MAPGROUP_D_TOPICHEAD.matches(classValue)) {

                        // make sure hrefValue make sense and target file
                        // is not generated file or the element is topichead
                        processTopicref(currentElem);
                        // support topicref without href attribute
                    } else if (hrefValue.length() == 0) {
                        processTopicref(currentElem);
                    }
                }
            }
        }

    }

    private void processChunk(final Element elem, final boolean separate, final boolean chunkByTopic) {
        // set up ChunkTopicParser
        try {
            final ChunkTopicParser chunkParser = new ChunkTopicParser();
            chunkParser.setLogger(logger);
            chunkParser.setJob(job);
            chunkParser.setup(changeTable, conflictTable, refFileSet, elem, separate, chunkByTopic,
                    chunkFilenameGenerator);
            chunkParser.write(filePath);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void updateReltable(final Element elem) {
        final String hrefValue = elem.getAttribute(ATTRIBUTE_NAME_HREF);
        if (hrefValue.length() != 0) {
            if (changeTable.containsKey(resolve(filePath, hrefValue).getPath())) {
                String resulthrefValue = null;
                final String fragment = getFragment(hrefValue);
                if (fragment != null) {
                    resulthrefValue = getRelativeUnixPath(filePath + UNIX_SEPARATOR + FILE_NAME_STUB_DITAMAP,
                            resolve(filePath, hrefValue).getPath())
                            + fragment;
                } else {
                    resulthrefValue = getRelativeUnixPath(filePath + UNIX_SEPARATOR + FILE_NAME_STUB_DITAMAP,
                            resolve(filePath, hrefValue).getPath());
                }
                elem.setAttribute(ATTRIBUTE_NAME_HREF, resulthrefValue);
            }
        }
        final NodeList children = elem.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            final Node current = children.item(i);
            if (current.getNodeType() == Node.ELEMENT_NODE) {
                final Element currentElem = (Element) current;
                final String classValue = currentElem.getAttribute(ATTRIBUTE_NAME_CLASS);
                if (MAP_TOPICREF.matches(classValue)) {

                }
            }
        }
    }

    /**
     * Get changed files table.
     * 
     * @return map of changed files
     */
    public Map<String, String> getChangeTable() {
        return Collections.unmodifiableMap(changeTable);
    }

    /**
     * get conflict table.
     * 
     * @return conflict table
     */
    public Map<String, String> getConflicTable() {
        return conflictTable;
    }

    /**
     * Set up environment.
     * 
     * @param transtype transtype
     */
    public void setup(final String transtype) {
        this.transtype = transtype;

    }

}
