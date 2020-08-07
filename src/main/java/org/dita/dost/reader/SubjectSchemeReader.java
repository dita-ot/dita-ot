/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2013 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.reader;

import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.module.filter.SubjectScheme;
import org.dita.dost.util.Job;
import org.dita.dost.util.StringUtils;
import org.dita.dost.util.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.QName;
import java.io.*;
import java.net.URI;
import java.util.*;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.toURI;

/**
 * Subject scheme reader.
 *
 * @since 1.8
 */
public class SubjectSchemeReader {

    private DITAOTLogger logger;
    private Job job;
    private final Map<QName, Map<String, Set<Element>>> bindingMap;
    private final Map<QName, Map<String, Set<String>>> validValuesMap;
    private final Map<QName, Map<String, String>> defaultValueMap;

    public SubjectSchemeReader() {
        validValuesMap = new HashMap<>();
        defaultValueMap = new HashMap<>();
        bindingMap = new HashMap<>();
    }

    /**
     * reset.
     */
    public void reset() {
        validValuesMap.clear();
        defaultValueMap.clear();
        bindingMap.clear();
    }

    /**
     * Get map of valid attribute values based on subject scheme. The
     * contents of the map is in pseudo-code
     * {@code Map<AttName, Map<ElemName, <Set<Value>>>}. For default element
     * mapping, the value is {@code *}.
     *
     * @return valid attribute values or empty map
     */
    public Map<QName, Map<String, Set<String>>> getValidValuesMap() {
        return validValuesMap;
    }

    /**
     * Get map of default values based on subject scheme. The
     * contents of the map is in pseudo-code
     * {@code Map<AttName, Map<ElemName, Default>>}. For default element
     * mapping, the value is {@code *}.
     *
     * @return default values or empty map
     */
    public Map<QName, Map<String, String>> getDefaultValueMap() {
        return defaultValueMap;
    }

    /**
     * Get map subject scheme definitions. The
     * contents of the map is in pseudo-code
     * {@code Map<AttName, Map<ElemName, Set<Element>>>}. For default element
     * mapping, the value is {@code *}.
     *
     * @return subject scheme definitions
     */
    public SubjectScheme getSubjectSchemeMap() {
        return new SubjectScheme(bindingMap);
    }

    public void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
    }

    public void setJob(final Job job) {
        this.job = job;
    }

    /**
     * Read a map from XML properties file. Values are split by {@link org.dita.dost.util.Constants#COMMA COMMA} into a set.
     *
     * @param inputFile XML properties file absolute path
     */
    public static Map<URI, Set<URI>> readMapFromXML(final File inputFile) throws IOException {
        final Map<URI, Set<URI>> graph = new HashMap<>();
        if (!inputFile.exists()) {
            return Collections.emptyMap();
        }
        final Properties prop = new Properties();
        try (FileInputStream in = new FileInputStream(inputFile)) {
            prop.loadFromXML(in);
        } catch (final IOException e) {
            throw new IOException("Failed to read subject scheme graph: " + e.getMessage(), e);
        }

        for (final Map.Entry<Object, Object> entry: prop.entrySet()) {
            final String key = (String) entry.getKey();
            final String value = (String) entry.getValue();
            final Set<URI> r = new HashSet<>();
            for (final String v: StringUtils.restoreSet(value, COMMA)) {
                r.add(toURI(v));
            }
            graph.put(toURI(key), r);
        }

        return Collections.unmodifiableMap(graph);
    }

    /**
     * Write map of sets to a file.
     *
     * <p>The serialization format is XML properties format where values are comma
     * separated lists.</p>
     *
     * @param m map to serialize
     * @param outputFile output filename, relative to temporary directory
     */
    public static void writeMapToXML(final Map<URI, Set<URI>> m, final File outputFile) throws IOException {
        if (m == null) {
            return;
        }
        final Properties prop = new Properties();
        for (final Map.Entry<URI, Set<URI>> entry: m.entrySet()) {
            final URI key = entry.getKey();
            final String value = StringUtils.join(entry.getValue(), COMMA);
            prop.setProperty(key.getPath(), value);
        }
        try (OutputStream os = new FileOutputStream(outputFile)) {
            prop.storeToXML(os, null);
        } catch (final IOException e) {
            throw new IOException("Failed to write subject scheme graph: " + e.getMessage(), e);
        }
    }

    /**
     * Load schema file.
     *
     * @param scheme absolute path for subject scheme
     */
    public void loadSubjectScheme(final File scheme) {
        assert scheme.isAbsolute();
        if (!job.getStore().exists(scheme.toURI())) {
            throw new IllegalStateException();
        }
        logger.debug("Load subject scheme " + scheme);

        try {
            final Document doc = job.getStore().getDocument(scheme.toURI());
            final Element schemeRoot = doc.getDocumentElement();
            if (schemeRoot == null) {
                return;
            }
            loadSubjectScheme(schemeRoot);
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            logger.error(e.getMessage(), e) ;
        }
    }

    public void loadSubjectScheme(final Element schemeRoot) {
        final List<Element> rootChildren = XMLUtils.getChildElements(schemeRoot);
        for (Element child : rootChildren) {
            final String attrValue = child.getAttribute(ATTRIBUTE_NAME_CLASS);
            if (SUBJECTSCHEME_ENUMERATIONDEF.matches(attrValue)) {
                processEnumerationDef(schemeRoot, child);
            }
        }
    }

    public void processEnumerationDef(final Element schemeRoot, final Element enumerationDef) {
        final List<Element> enumChildren = XMLUtils.getChildElements(enumerationDef);
        String elementName = "*";
        QName attributeName = null;
        for (Element child : enumChildren) {
            final String attrValue = child.getAttribute(ATTRIBUTE_NAME_CLASS);
            if (SUBJECTSCHEME_ELEMENTDEF.matches(attrValue)) {
                elementName = child.getAttribute(ATTRIBUTE_NAME_NAME);
            } else if (SUBJECTSCHEME_ATTRIBUTEDEF.matches(attrValue)) {
                final String name = child.getAttribute(ATTRIBUTE_NAME_NAME);
                attributeName = name != null ? QName.valueOf(name) : null;
                Map<String, Set<Element>> S = bindingMap.computeIfAbsent(attributeName, k -> new HashMap<>());
            } else if (SUBJECTSCHEME_DEFAULTSUBJECT.matches(attrValue)) {
                // Put default values.
                final String keyValue = child.getAttribute(ATTRIBUTE_NAME_KEYREF);
                if (keyValue != null) {
                    Map<String, String> S = defaultValueMap.get(attributeName);
                    if (S == null) {
                        S = new HashMap<>();
                    }
                    S.put(elementName, keyValue);
                    defaultValueMap.put(attributeName, S);
                }
            } else if (SUBJECTSCHEME_SUBJECTDEF.matches(attrValue)) {
                // Search for attributeName in schemeRoot
                String keyValue = child.getAttribute(ATTRIBUTE_NAME_KEYREF);
                if (StringUtils.isEmptyString(keyValue)) {
                    keyValue = child.getAttribute(ATTRIBUTE_NAME_KEYS);
                }
                final Element subTree = searchForKey(schemeRoot, keyValue);
                if (subTree != null) {
                    Map<String, Set<Element>> S = bindingMap.get(attributeName);
                    if (S == null) {
                        S = new HashMap<>();
                    }
                    Set<Element> A = S.get(elementName);
                    if (A == null) {
                        A = new HashSet<>();
                    }
                    if (!A.contains(subTree)) {
                        // Add sub-tree to valid values map
                        putValuePairsIntoMap(subTree, elementName, attributeName, keyValue);
                    }
                    A.add(subTree);
                    S.put(elementName, A);
                    bindingMap.put(attributeName, S);
                }
            }
        }
    }

    /**
     * Search subject scheme elements for a given key
     * @param root subject scheme element tree to search through
     * @param keyValue key to locate
     * @return element that matches the key, otherwise {@code null}
     */
    private Element searchForKey(final Element root, final String keyValue) {
        if (root == null || keyValue == null) {
            return null;
        }
        final LinkedList<Element> queue = new LinkedList<>();
        queue.add(root);
        while (!queue.isEmpty()) {
            final Element node = queue.removeFirst();
            final NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    queue.add((Element)children.item(i));
                }
            }
            if (SUBJECTSCHEME_SUBJECTDEF.matches(node)) {
                final String key = node.getAttribute(ATTRIBUTE_NAME_KEYS);
                if (keyValue.equals(key)) {
                    return node;
                }
            }
        }
        return null;
    }

    /**
     * Populate valid values map
     *
     * @param subtree subject scheme definition element
     * @param elementName element name
     * @param attName attribute name
     * @param category enumeration category name
     */
    private void putValuePairsIntoMap(final Element subtree, final String elementName, final QName attName, final String category) {
        if (subtree == null || attName == null) {
            return;
        }

        Map<String, Set<String>> valueMap = validValuesMap.get(attName);
        if (valueMap == null) {
            valueMap = new HashMap<>();
        }

        Set<String> valueSet = valueMap.get(elementName);
        if (valueSet == null) {
            valueSet = new HashSet<>();
        }

        final LinkedList<Element> queue = new LinkedList<>();
        queue.offer(subtree);

        while (!queue.isEmpty()) {
            final Element node = queue.poll();
            final NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    queue.offer((Element)children.item(i));
                }
            }
            if (SUBJECTSCHEME_SUBJECTDEF.matches(node)) {
                final String key = node.getAttribute(ATTRIBUTE_NAME_KEYS);
                if (!(key == null || key.trim().isEmpty() || key.equals(category))) {
                    valueSet.add(key);
                }
            }
        }
        valueMap.put(elementName, valueSet);
        validValuesMap.put(attName, valueMap);
    }

}
