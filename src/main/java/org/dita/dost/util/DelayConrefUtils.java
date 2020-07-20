/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2004, 2005 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.util;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.module.reader.TempFileNameScheme;
import org.dita.dost.writer.ExportAnchorsFilter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.toFile;

/**
 *
 * Delay conref feature related utility functions.
 * @author william
 *
 */
public final class DelayConrefUtils {

    /** Root element of export.xml Document */
    private Document root = null;

    private DITAOTLogger logger;
    private Job job;

    /**
     * Constructor.
     */
    public DelayConrefUtils() {
        super();
        root = null;
    }

    public void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
    }


    public void setJob(final Job job) {
        this.job = job;
    }

    /**
     * Find whether an id is refer to a topic in a dita file.
     * @param absolutePathToFile the absolute path of dita file
     * @param id topic id
     * @return true if id find and false otherwise
     */
    public boolean findTopicId(final File absolutePathToFile, final String id) {
        if (!job.getStore().exists(absolutePathToFile.toURI())) {
            return false;
        }
        try {
            //load the file
            final Document root = job.getStore().getImmutableDocument(absolutePathToFile.toURI());

            //get root element
            final Element doc = root.getDocumentElement();
            //do BFS
            final Queue<Element> queue = new LinkedList<>();
            queue.offer(doc);
            while (!queue.isEmpty()) {
                final Element pe = queue.poll();
                final NodeList pchildrenList = pe.getChildNodes();
                for (int i = 0; i < pchildrenList.getLength(); i++) {
                    final Node node = pchildrenList.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        queue.offer((Element)node);
                    }
                }
                final String classValue = pe.getAttribute(ATTRIBUTE_NAME_CLASS);
                if (classValue != null && TOPIC_TOPIC.matches(classValue)) {
                    //topic id found
                    if (pe.getAttribute(ATTRIBUTE_NAME_ID).equals(id)) {
                        return true;
                    }
                }
            }
            return false;
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            logger.error("Failed to read document: " + e.getMessage(), e);
        }
        return false;
    }

    /**check whether the href/id element defined by keys has been exported.
     * @param href href
     * @param id id
     * @param key keyname
     * @param tempDir absolute path to temporary director
     * @return result list
     */
    public List<Boolean> checkExport(String href, final String id, final String key, final File tempDir) {
        //parsed export .xml to get exported elements
        final File exportFile = new File(tempDir, FILE_NAME_EXPORT_XML);

        boolean idExported = false;
        boolean keyrefExported = false;
        try {
            //load export.xml only once
            if (root == null) {
                root = job.getStore().getImmutableDocument(exportFile.toURI());
            }
            //get file node which contains the export node
            final Element fileNode = searchForKey(root.getDocumentElement(), href, "file");
            if (fileNode != null) {
                //iterate the child nodes
                final NodeList pList = fileNode.getChildNodes();
                for (int j = 0; j < pList.getLength(); j++) {
                    final Node node = pList.item(j);
                    if (Node.ELEMENT_NODE == node.getNodeType()) {
                        final Element child = (Element)node;
                        //compare keys
                        if (child.getNodeName().equals("keyref")&&
                                child.getAttribute(ATTRIBUTE_NAME_NAME)
                                .equals(key)) {
                            keyrefExported = true;
                            //compare topic id
                        } else if (child.getNodeName().equals("topicid")&&
                                child.getAttribute(ATTRIBUTE_NAME_NAME)
                                .equals(id)) {
                            idExported = true;
                            //compare element id
                        } else if (child.getNodeName().equals("id")&&
                                child.getAttribute(ATTRIBUTE_NAME_NAME)
                                .equals(id)) {
                            idExported = true;
                        }
                    }
                    if (idExported && keyrefExported) {
                        break;
                    }
                }
            }
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            e.printStackTrace();
        }
        final List<Boolean> list = new ArrayList<>();
        list.add(idExported);
        list.add(keyrefExported);
        return list;
    }
    /**
     * Search specific element by key and tagName.
     * @param root root element
     * @param key search keyword
     * @param tagName search tag name
     * @return search result, null of either input is invalid or the looking result is not found.
     */
    private Element searchForKey(final Element root, final String key, final String tagName) {
        if (root == null || StringUtils.isEmptyString(key)) {
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
                    queue.offer((Element)node);
                }
            }
            String value = pe.getNodeName();
            if (StringUtils.isEmptyString(value)||
                    !value.equals(tagName)) {
                continue;
            }

            value = pe.getAttribute(ATTRIBUTE_NAME_NAME);
            if (StringUtils.isEmptyString(value)) {
                continue;
            }

            if (value.equals(key)) {
                return pe;
            }
        }
        return null;
    }
    /**
     * Write map into xml file.
     * @param m map
     */
    public void writeMapToXML(final Map<String, Set<String>> m) {
        final File outputFile = new File(job.tempDir, FILE_NAME_PLUGIN_XML);
        if (m == null) {
            return;
        }
        final Properties prop = new Properties();
        for (Map.Entry<String, Set<String>> entry : m.entrySet()) {
            final String key = entry.getKey();
            final String value = StringUtils.join(entry.getValue(),
                    COMMA);
            prop.setProperty(key, value);
        }
        //File outputFile = new File(tempDir, filename);

        final Document doc = XMLUtils.getDocumentBuilder().newDocument();
        final Element properties = (Element) doc.appendChild(doc
                .createElement("properties"));

        final Set<Object> keys = prop.keySet();
        for (Object key1 : keys) {
            final String key = (String) key1;
            final Element entry = (Element) properties.appendChild(doc
                    .createElement("entry"));
            entry.setAttribute("key", key);
            entry.appendChild(doc.createTextNode(prop.getProperty(key)));
        }

        try {
            job.getStore().writeDocument(doc, outputFile.toURI());
        } catch (final IOException e) {
            logger.error("Failed to process map: " + e.getMessage(), e);
        }
    }

    public void writeExportAnchors(final ExportAnchorsFilter exportAnchorsFilter,
                                   final TempFileNameScheme tempFileNameScheme)
            throws DITAOTException {
        XMLStreamWriter export = null;
        try (OutputStream exportStream = new FileOutputStream(new File(job.tempDir, FILE_NAME_EXPORT_XML))) {
            export = XMLOutputFactory.newInstance().createXMLStreamWriter(exportStream, "UTF-8");
            export.writeStartDocument();
            export.writeStartElement("stub");
            for (final ExportAnchorsFilter.ExportAnchor e: exportAnchorsFilter.getExportAnchors()) {
                export.writeStartElement("file");
                export.writeAttribute("name", tempFileNameScheme.generateTempFileName(toFile(e.file).toURI()).toString());
                for (final String t: sort(e.topicids)) {
                    export.writeStartElement("topicid");
                    export.writeAttribute("name", t);
                    export.writeEndElement();
                }
                for (final String i: sort(e.ids)) {
                    export.writeStartElement("id");
                    export.writeAttribute("name", i);
                    export.writeEndElement();
                }
                for (final String k: sort(e.keys)) {
                    export.writeStartElement("keyref");
                    export.writeAttribute("name", k);
                    export.writeEndElement();
                }
                export.writeEndElement();
            }
            export.writeEndElement();
            export.writeEndDocument();
        } catch (final IOException e) {
            throw new DITAOTException("Failed to write export anchor file: " + e.getMessage(), e);
        } catch (final XMLStreamException e) {
            throw new DITAOTException("Failed to serialize export anchor file: " + e.getMessage(), e);
        } finally {
            if (export != null) {
                try {
                    export.close();
                } catch (final XMLStreamException e) {
                    logger.error("Failed to close export anchor file: " + e.getMessage(), e);
                }
            }
        }
    }

    private List<String> sort(final Set<String> set) {
        final List<String> sorted = new ArrayList<>(set);
        Collections.sort(sorted);
        return sorted;
    }

}
