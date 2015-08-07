/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.writer;

import org.dita.dost.util.DitaClass;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.*;

import static org.dita.dost.util.Constants.*;

/**
 * Base class for metadata filter that reads dita files and inserts metadata.
 */
public abstract class AbstractDitaMetaWriter extends AbstractDomFilter {

    private static final Set<DitaClass> uniqueSet = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            TOPIC_CRITDATES,
            TOPIC_PERMISSIONS,
            TOPIC_PUBLISHER,
            TOPIC_SOURCE,
            MAP_SEARCHTITLE
    )));

    private Map<String, Element> metaTable;

    public void setMetaTable(final Map<String, Element> metaTable) {
        this.metaTable = metaTable;
    }

    public abstract Document process(final Document doc);

    void processMetadata(final Element metadataContainer, final List<DitaClass> order) {
        for (int i = 0; i < order.size(); i++) {
            final DitaClass cls = order.get(i);
            final List<Element> newChildren = getNewChildren(cls, metadataContainer.getOwnerDocument());
            if (!newChildren.isEmpty()) {
                final Element insertPoint = getInsertionRef(metadataContainer, order.subList(i, order.size()));
                for (final Element newChild: newChildren) {
                    if (insertPoint != null) {
                        if (uniqueSet.contains(cls) && cls.matches(insertPoint)) {
                            metadataContainer.replaceChild(newChild, insertPoint);
                        } else {
                            metadataContainer.insertBefore(newChild, insertPoint);
                        }
                    } else {
                        metadataContainer.appendChild(newChild);
                    }
                }
            }
        }
    }

    boolean hasMetadata(final List<DitaClass> order) {
        for (final DitaClass cls: order) {
            if (metaTable.containsKey(cls.matcher)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get metadata elements to add to current document. Elements have been cloned and imported
     * into the current document.
     *
     * @param cls element class of metadata elements
     * @param doc current document
     * @return list of metadata elements, may be empty
     */
    private List<Element> getNewChildren(final DitaClass cls, final Document doc) {
        final List<Element> res = new ArrayList<>();
        if (metaTable.containsKey(cls.matcher)) {
            metaTable.get(cls.matcher);
            final NodeList list = metaTable.get(cls.matcher).getChildNodes();
            for (int i = 0; i < list.getLength(); i++) {
                Node item = list.item(i);
                res.add((Element) doc.importNode(item, true));
            }
        }
        Collections.reverse(res);
        return res;
    }

    private Element getInsertionRef(final Element metadataContainer, final List<DitaClass> order) {
        if (order.isEmpty()) {
            return null;
        } else {
            final Element elem = getFirstChildElement(metadataContainer, order.get(0));
            if (elem != null) {
                return elem;
            } else {
                return getInsertionRef(metadataContainer, order.subList(1, order.size()));
            }
        }
    }

    Element findMetadataContainer(final Element root, List<DitaClass> position, final DitaClass container) {
        Element prolog = getFirstChildElement(root, container);
        if (prolog == null) {
            prolog = root.getOwnerDocument().createElement(container.localName);
            prolog.setAttribute(ATTRIBUTE_NAME_CLASS, container.toString());
            Element insertPoint = null;
            for (int i = position.size() - 1; i >= 0; i--) {
                insertPoint = getLastChildElement(root, position.get(i));
                if (insertPoint != null) {
                    break;
                }
            }
            if (insertPoint != null) {
                insertAfter(prolog, insertPoint);
            } else if (root.hasChildNodes()) {
                root.insertBefore(prolog, root.getFirstChild());
            } else {
                root.appendChild(prolog);
            }
        }
        return prolog;
    }

    Element getFirstChildElement(final Element root, final DitaClass cls) {
        final NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            final Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                final Element elem = (Element) child;
                if (cls.matches(elem)) {
                    return elem;
                }
            }
        }
        return null;
    }

    private Element getLastChildElement(final Element root, final DitaClass cls) {
        Element res = null;
        final NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            final Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                final Element elem = (Element) child;
                if (cls.matches(elem)) {
                    res = elem;
                }
            }
        }
        return res;
    }

    private void insertAfter(final Node newChild, final Node refChild) {
        final Node next = refChild.getNextSibling();
        final Node parent = refChild.getParentNode();
        if (next != null) {
            parent.insertBefore(newChild, next);
        } else {
            parent.appendChild(newChild);
        }
    }

}