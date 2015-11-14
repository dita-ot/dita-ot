/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2007 All Rights Reserved.
 */
package org.dita.dost.reader;

import static java.util.Arrays.asList;
import static org.dita.dost.module.GenMapAndTopicListModule.*;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.*;

import java.io.File;
import java.net.URI;
import java.util.*;
import java.util.Map.Entry;

import org.dita.dost.util.StringUtils;
import org.dita.dost.util.URLUtils;
import org.dita.dost.util.XMLUtils;
import org.dita.dost.writer.AbstractDomFilter;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Cascade map metadata to child topic references and collect metadata for topics.
 */
public final class MapMetaReader extends AbstractDomFilter {

    /**
     * Cascaded metadata. Contents <topic relative URI, <class matcher, cascading metadata elements>>.
     */
    private final Map<URI, Map<String, Element>> resultTable = new HashMap<>(16);

    private static final Set<String> uniqueSet = Collections.unmodifiableSet(new HashSet<>(asList(
            TOPIC_CRITDATES.matcher,
            TOPIC_PERMISSIONS.matcher,
            TOPIC_PUBLISHER.matcher,
            TOPIC_SOURCE.matcher,
            MAP_SEARCHTITLE.matcher
    )));
    private static final Set<String> cascadeSet = Collections.unmodifiableSet(new HashSet<>(asList(
            TOPIC_AUDIENCE.matcher,
            TOPIC_AUTHOR.matcher,
            TOPIC_SOURCE.matcher,
            TOPIC_CATEGORY.matcher,
            TOPIC_COPYRIGHT.matcher,
            TOPIC_CRITDATES.matcher,
            TOPIC_PERMISSIONS.matcher,
            TOPIC_PRODINFO.matcher,
            TOPIC_OTHERMETA.matcher,
            TOPIC_PUBLISHER.matcher
    )));
    private static final Set<String> metaSet = Collections.unmodifiableSet(new HashSet<>(asList(
            MAP_SEARCHTITLE.matcher,
            TOPIC_AUTHOR.matcher,
            TOPIC_SOURCE.matcher,
            TOPIC_PUBLISHER.matcher,
            TOPIC_COPYRIGHT.matcher,
            TOPIC_CRITDATES.matcher,
            TOPIC_PERMISSIONS.matcher,
            TOPIC_AUDIENCE.matcher,
            TOPIC_CATEGORY.matcher,
            TOPIC_KEYWORDS.matcher,
            TOPIC_PRODINFO.matcher,
            TOPIC_OTHERMETA.matcher,
            TOPIC_RESOURCEID.matcher,
            TOPIC_DATA.matcher,
            TOPIC_DATA_ABOUT.matcher,
            TOPIC_FOREIGN.matcher,
            TOPIC_UNKNOWN.matcher
    )));
    private static final List<String> metaPos = Collections.unmodifiableList(asList(
            MAP_SEARCHTITLE.matcher,
            TOPIC_AUTHOR.matcher,
            TOPIC_SOURCE.matcher,
            TOPIC_PUBLISHER.matcher,
            TOPIC_COPYRIGHT.matcher,
            TOPIC_CRITDATES.matcher,
            TOPIC_PERMISSIONS.matcher,
            TOPIC_AUDIENCE.matcher,
            TOPIC_CATEGORY.matcher,
            TOPIC_KEYWORDS.matcher,
            TOPIC_PRODINFO.matcher,
            TOPIC_OTHERMETA.matcher,
            TOPIC_RESOURCEID.matcher,
            TOPIC_DATA.matcher,
            TOPIC_DATA_ABOUT.matcher,
            TOPIC_FOREIGN.matcher,
            TOPIC_UNKNOWN.matcher,
            MAP_LINKTEXT.matcher,
            MAP_SHORTDESC.matcher,
            TOPIC_NAVTITLE.matcher,
            TOPIC_METADATA.matcher,
            DELAY_D_EXPORTANCHORS.matcher
            ));

    private final Map<String, Element> globalMeta;
    /** Current document. */
    private Document doc = null;
    /** Result metadata document. */
    private Document resultDoc = null;
    /** Current file. */
    private File filePath = null;

    /**
     * Constructor.
     */
    public MapMetaReader() {
        super();
        globalMeta = new HashMap<>(16);
        resultDoc = XMLUtils.getDocumentBuilder().newDocument();
        resultTable.clear();
    }
    /**
     * read map files.
     * @param filename filename
     */
    @Override
    public void read(final File filename) {
        filePath = filename;

        //clear the history on global metadata table
        globalMeta.clear();
        super.read(filename);
    }
    
    @Override
    public Document process(final Document doc) {
        this.doc = doc;
        final NodeList list = doc.getDocumentElement().getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            final Node node = list.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                final Element elem = (Element) node;
                final Attr classAttr = elem.getAttributeNode(ATTRIBUTE_NAME_CLASS);
                if (classAttr != null) {
                    // if this node is topicmeta node under root
                    if (MAP_TOPICMETA.matches(classAttr.getNodeValue())) {
                        handleGlobalMeta(elem);
                    // if this node is topicref node under root
                    } else if (MAP_TOPICREF.matches(classAttr.getNodeValue())) {
                        handleTopicref(elem, globalMeta);
                    }
                }
            }
        }
        // Indexterm elements with either start or end attribute should not been
        // move to referenced dita file's prolog section.
        for (final Map<String, Element> resultTableEntry: resultTable.values()) {
            for (final Map.Entry<String, Element> mapEntry: resultTableEntry.entrySet()) {
                final String key = mapEntry.getKey();
                if (TOPIC_KEYWORDS.matcher.equals(key)) {
                    removeIndexTermRecursive(mapEntry.getValue());
                }
            }
        }
        return doc;
    }

    /**
     * traverse the node tree and remove all indexterm elements with either start or
     * end attribute.
     * @param parent root element
     */
    private void removeIndexTermRecursive(final Element parent) {
        if (parent == null) {
            return;
        }
        final NodeList children = parent.getChildNodes();
        Element child;
        for (int i = children.getLength() - 1; i >= 0; i--) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                child = (Element) children.item(i);
                final boolean isIndexTerm = TOPIC_INDEXTERM.matches(child);
                final boolean hasStart = !child.getAttribute(ATTRIBUTE_NAME_START).isEmpty();
                final boolean hasEnd = !child.getAttribute(ATTRIBUTE_NAME_END).isEmpty();
                if (isIndexTerm && (hasStart || hasEnd)) {
                    parent.removeChild(child);
                } else {
                    removeIndexTermRecursive(child);
                }
            }
        }
    }

    private void handleTopicref(final Element topicref, final Map<String, Element> inheritance) {
        final Attr hrefAttr = topicref.getAttributeNode(ATTRIBUTE_NAME_HREF);
        final Attr copytoAttr = topicref.getAttributeNode(ATTRIBUTE_NAME_COPY_TO);
        final Attr scopeAttr = topicref.getAttributeNode(ATTRIBUTE_NAME_SCOPE);
        final Attr formatAttr = topicref.getAttributeNode(ATTRIBUTE_NAME_FORMAT);
        Map<String, Element> current = mergeMeta(null, inheritance, cascadeSet);
        Element metaNode = null;

        final NodeList children = topicref.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            final Node node = children.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                final Element elem = (Element) node;
                Attr classAttr = elem.getAttributeNode(ATTRIBUTE_NAME_CLASS);
                if (classAttr != null) {
                    // if this node is topicmeta and the parent topicref refers to a valid dita topic
                    if (MAP_TOPICMETA.matches(classAttr.getNodeValue()) &&
                        hrefAttr != null && isLocalScope(scopeAttr) && isDitaFormat(formatAttr)) {
                        metaNode = elem;
                        current = handleMeta(elem, inheritance);
                    // if this node is topicref node under topicref
                    } else if (MAP_TOPICREF.matches(classAttr.getNodeValue())) {
                        handleTopicref(elem, current);
                    }
                }
            }
        }

        if (!current.isEmpty() && hrefAttr != null) {// prevent the metadata is empty
            if (isDitaFormat(formatAttr) && isLocalScope(scopeAttr)) {
                URI topicPath;
                if (copytoAttr != null) {
                    final URI copyToUri = stripFragment(URLUtils.toURI(copytoAttr.getNodeValue()));
                    topicPath = job.tempDir.toURI().relativize(filePath.toURI().resolve(copyToUri));
                } else {
                    final URI hrefUri = stripFragment(URLUtils.toURI(hrefAttr.getNodeValue()));
                    topicPath = job.tempDir.toURI().relativize(filePath.toURI().resolve(hrefUri));
                }
                if (resultTable.containsKey(topicPath)) {
                    //if the result table already contains some result
                    //metadata for current topic path.
                    final Map<String, Element> previous = resultTable.get(topicPath);
                    resultTable.put(topicPath, mergeMeta(previous, current, metaSet));
                } else {
                    resultTable.put(topicPath, cloneElementMap(current));
                }
                final Map<String, Element> metas = resultTable.get(topicPath);
                if (!metas.isEmpty()) {
                    if (metaNode != null) {
                        topicref.removeChild(metaNode);
                    }
                    final Element newMeta = doc.createElement(MAP_TOPICMETA.localName);
                    newMeta.setAttribute(ATTRIBUTE_NAME_CLASS, "-" + MAP_TOPICMETA.matcher);
                    for (String metaPo : metaPos) {
                        final Node stub = metas.get(metaPo);
                        if (stub != null) {
                            final NodeList clist = stub.getChildNodes();
                            for (int j = 0; j < clist.getLength(); j++) {
                                newMeta.appendChild(topicref.getOwnerDocument().importNode(clist.item(j), true));
                            }
                        }
                    }
                    topicref.insertBefore(newMeta, topicref.getFirstChild());
                }
            }
        }
    }
    
    private boolean isLocalScope(final Attr scopeAttr) {
        return scopeAttr == null || ATTR_SCOPE_VALUE_LOCAL.equals(scopeAttr.getNodeValue());
    }
    
    private boolean isDitaFormat(final Attr formatAttr) {
        return formatAttr == null ||
            ATTR_FORMAT_VALUE_DITA.equals(formatAttr.getNodeValue()) ||
            ATTR_FORMAT_VALUE_DITAMAP.equals(formatAttr.getNodeValue());
    }

    /**
     * Clone metadata map.
     *
     * @param current metadata map to clone
     * @return a clone of the original map
     */
    private Map<String, Element> cloneElementMap(final Map<String, Element> current) {
        final Map<String, Element> topicMetaTable = new HashMap<>(16);
        for (final Entry<String, Element> topicMetaItem: current.entrySet()) {
            topicMetaTable.put(topicMetaItem.getKey(), (Element) resultDoc.importNode(topicMetaItem.getValue(), true));
        }
        return topicMetaTable;
    }


    private Map<String, Element> handleMeta(final Element meta, final Map<String, Element> inheritance) {
        final Map<String, Element> topicMetaTable = new HashMap<>(16);
        getMeta(meta, topicMetaTable);
        return mergeMeta(topicMetaTable, inheritance, cascadeSet);
    }

    private void getMeta(final Element meta, final Map<String, Element> topicMetaTable) {
        final NodeList children = meta.getChildNodes();
        for(int i = 0; i < children.getLength(); i++) {
            final Node node = children.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                final Element elem = (Element) node;
                final Attr classAttr = elem.getAttributeNode(ATTRIBUTE_NAME_CLASS);
                if (classAttr != null) {
                    final String classValue = classAttr.getNodeValue();
                    // int number 1 is used to remove the first "-" or "+" character in class attribute
                    final String metaKey = classValue.substring(1, classValue.indexOf(STRING_BLANK, classValue.indexOf(SLASH)) + 1);
                    if (TOPIC_METADATA.matches(classValue)) {
                        getMeta(elem, topicMetaTable);
                    } else if (topicMetaTable.containsKey(metaKey)) {
                        //append node to the list if it exist in topic meta table
                        topicMetaTable.get(metaKey).appendChild(resultDoc.importNode(elem, true));
                    } else {
                        final Element stub = resultDoc.createElement(ELEMENT_STUB);
                        stub.appendChild(resultDoc.importNode(elem, true));
                        topicMetaTable.put(metaKey, stub);
                    }
                }
            }
        }
    }

    private Map<String, Element> mergeMeta(Map<String, Element> topicMetaTable,
            final Map<String, Element> inheritance, final Set<String> enableSet) {
        // When inherited metadata need to be merged into current metadata
        // enableSet should be cascadeSet so that only metadata that can
        // be inherited are merged.
        // Otherwise enableSet should be metaSet in order to merge all
        // metadata.
        if (topicMetaTable == null) {
            topicMetaTable = new HashMap<>(16);
        }
        for (String key : enableSet) {
            if (inheritance.containsKey(key)) {
                if (uniqueSet.contains(key)) {
                    if (!topicMetaTable.containsKey(key)) {
                        topicMetaTable.put(key, inheritance.get(key));
                    }
                } else {  // not unique metadata
                    if (!topicMetaTable.containsKey(key)) {
                        topicMetaTable.put(key, inheritance.get(key));
                    } else {
                        //not necessary to do node type check here
                        //because inheritStub doesn't contains any node
                        //other than Element.
                        final Node stub = topicMetaTable.get(key);
                        final Node inheritStub = inheritance.get(key);
                        if (stub != inheritStub) {
                            // Merge the value if stub does not equal to inheritStub
                            // Otherwise it will get into infinitive loop
                            final NodeList children = inheritStub.getChildNodes();
                            for (int i = 0; i < children.getLength(); i++) {
                                Node item = children.item(i).cloneNode(true);
                                item = stub.getOwnerDocument().importNode(item, true);
                                stub.appendChild(item);
                            }
                        }
                        topicMetaTable.put(key, (Element) stub);
                    }
                }
            }
        }
        return topicMetaTable;
    }

    private void handleGlobalMeta(final Element metadata) {
        final NodeList children = metadata.getChildNodes();
        for(int i = 0; i < children.getLength(); i++) {
            final Node node = children.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                final Element elem = (Element) node;
                final Attr classAttr = elem.getAttributeNode(ATTRIBUTE_NAME_CLASS);
                if (classAttr != null) {
                    final String classValue = classAttr.getNodeValue();
                    final String metaKey = classValue.substring(1, classValue.indexOf(STRING_BLANK, classValue.indexOf(SLASH))+1 );
                    if (TOPIC_METADATA.matches(classValue)) {
                        //proceed the metadata in <metadata>
                        handleGlobalMeta(elem);
                    } else if (cascadeSet.contains(metaKey) && globalMeta.containsKey(metaKey)) {
                        //append node to the list if it exist in global meta table
                        globalMeta.get(metaKey).appendChild(resultDoc.importNode(elem, true));
                    } else if (cascadeSet.contains(metaKey)) {
                        final Element stub = resultDoc.createElement(ELEMENT_STUB);
                        stub.appendChild(resultDoc.importNode(elem, true));
                        globalMeta.put(metaKey, stub);
                    }
                }
            }
        }
    }

    /**
     * Get metadata for topics
     * 
     * @return map of metadata by topic path
     */
    public Map<URI, Map<String, Element>> getMapping() {
    	return Collections.unmodifiableMap(resultTable);
    } 

}
