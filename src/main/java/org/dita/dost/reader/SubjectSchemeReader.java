/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

package org.dita.dost.reader;

import static org.dita.dost.util.Constants.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Subject scheme reader.
 * 
 * @since 1.8
 */
public class SubjectSchemeReader {
    
    private DITAOTLogger logger;
    private final Map<String, Map<String, Set<Element>>> bindingMap;
    private final Map<String, Map<String, Set<String>>> validValuesMap;
    private final Map<String, Map<String, String>> defaultValueMap;

    public SubjectSchemeReader() {
        validValuesMap = new HashMap<String, Map<String, Set<String>>>();
        defaultValueMap = new HashMap<String, Map<String, String>>();
        bindingMap = new HashMap<String, Map<String, Set<Element>>>();
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
     * @return valid attribute values
     */
    public Map<String, Map<String, Set<String>>> getValidValuesMap() {
        return validValuesMap;
    }

    /**
     * Get map of default values based on subject scheme. The
     * contents of the map is in pseudo-code
     * {@code Map<AttName, Map<ElemName, Default>>}. For default element
     * mapping, the value is {@code *}.
     * 
     * @return default values
     */
    public Map<String, Map<String, String>> getDefaultValueMap() {
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
    public Map<String, Map<String, Set<Element>>> getSubjectSchemeMap() {
        return bindingMap;
    }
    
    public void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
    }
    
    /**
     * load schema file.
     * @param scheme scheme file
     */
    public void loadSubjectScheme(final String scheme) {

        if (!FileUtils.fileExists(scheme)) {
            return;
        }
        logger.logDebug("Load subject scheme " + scheme);

        try {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final Document doc = builder.parse(new InputSource(new FileInputStream(new File(scheme))));
            final Element schemeRoot = doc.getDocumentElement();
            if (schemeRoot == null) {
                return;
            }
            final NodeList rootChildren = schemeRoot.getChildNodes();
            for (int i = 0; i < rootChildren.getLength(); i++) {
                if (rootChildren.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element node = (Element)rootChildren.item(i);
                    String attrValue = node.getAttribute(ATTRIBUTE_NAME_CLASS);
                    if (SUBJECTSCHEME_ENUMERATIONDEF.matches(attrValue)) {
                        final NodeList enumChildren = node.getChildNodes();
                        String elementName = "*";
                        String attributeName = null;
                        for (int j = 0; j < enumChildren.getLength(); j++) {
                            if (enumChildren.item(j).getNodeType() == Node.ELEMENT_NODE) {
                                node = (Element)enumChildren.item(j);
                                attrValue = node.getAttribute(ATTRIBUTE_NAME_CLASS);
                                if (SUBJECTSCHEME_ELEMENTDEF.matches(attrValue)) {
                                    elementName = node.getAttribute(ATTRIBUTE_NAME_NAME);
                                } else if (SUBJECTSCHEME_ATTRIBUTEDEF.matches(attrValue)) {
                                    attributeName = node.getAttribute(ATTRIBUTE_NAME_NAME);
                                    Map<String, Set<Element>> S = bindingMap.get(attributeName);
                                    if (S == null) {
                                        S = new HashMap<String, Set<Element>>();
                                        bindingMap.put(attributeName, S);
                                    }
                                } else if (SUBJECTSCHEME_DEFAULTSUBJECT.matches(attrValue)) {
                                    // Put default values.
                                    final String keyValue = node.getAttribute(ATTRIBUTE_NAME_KEYREF);
                                    if (keyValue != null) {
                                        Map<String, String> S = defaultValueMap.get(attributeName);
                                        if (S == null) {
                                            S = new HashMap<String, String>();
                                        }
                                        S.put(elementName, keyValue);
                                        defaultValueMap.put(attributeName, S);
                                    }
                                } else if (SUBJECTSCHEME_SUBJECTDEF.matches(attrValue)) {
                                    // Search for attributeName in schemeRoot
                                    String keyValue = node.getAttribute(ATTRIBUTE_NAME_KEYREF);
                                    if (StringUtils.isEmptyString(keyValue)) {
                                        keyValue = node.getAttribute(ATTRIBUTE_NAME_KEYS);
                                    }
                                    final Element subTree = searchForKey(schemeRoot, keyValue);
                                    if (subTree != null) {
                                        Map<String, Set<Element>> S = bindingMap.get(attributeName);
                                        if (S == null) {
                                            S = new HashMap<String, Set<Element>>();
                                        }
                                        Set<Element> A = S.get(elementName);
                                        if (A == null) {
                                            A = new HashSet<Element>();
                                        }
                                        if (!A.contains(subTree)) {
                                            // Add sub-tree to valid values map
                                            this.putValuePairsIntoMap(subTree, elementName, attributeName);
                                        }
                                        A.add(subTree);
                                        S.put(elementName, A);
                                        bindingMap.put(attributeName, S);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (final Exception e) {
            logger.logError(e.getMessage(), e) ;
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
        final LinkedList<Element> queue = new LinkedList<Element>();
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
     */
    private void putValuePairsIntoMap(final Element subtree, final String elementName, final String attName) {
        if (subtree == null || attName == null) {
            return;
        }

        Map<String, Set<String>> valueMap = validValuesMap.get(attName);
        if (valueMap == null) {
            valueMap = new HashMap<String, Set<String>>();
        }

        Set<String> valueSet = valueMap.get(elementName);
        if (valueSet == null) {
            valueSet = new HashSet<String>();
        }

        final LinkedList<Element> queue = new LinkedList<Element>();
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
                if (!StringUtils.isEmptyString(key)) {
                    valueSet.add(key);
                }
            }
        }
        valueMap.put(elementName, valueSet);
        validValuesMap.put(attName, valueMap);
    }

}
