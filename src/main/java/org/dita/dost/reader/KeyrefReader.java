/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.reader;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.*;

import java.io.File;
import java.net.URI;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;

import org.dita.dost.util.DitaClass;
import org.dita.dost.util.KeyDef;
import org.dita.dost.util.XMLUtils;
import org.w3c.dom.*;
import org.dita.dost.log.DITAOTLogger;
import org.xml.sax.InputSource;

/**
 * KeyrefReader class which reads DITA map file to collect key definitions. Instances are reusable but not thread-safe.
 */
public final class KeyrefReader implements AbstractReader {

    private static final List<String> ATTS = Collections.unmodifiableList(Arrays.asList(
            ATTRIBUTE_NAME_HREF,
            ATTRIBUTE_NAME_AUDIENCE,
            ATTRIBUTE_NAME_PLATFORM,
            ATTRIBUTE_NAME_PRODUCT,
            ATTRIBUTE_NAME_OTHERPROPS,
            "rev",
            ATTRIBUTE_NAME_PROPS,
            "linking",
            ATTRIBUTE_NAME_TOC,
            ATTRIBUTE_NAME_PRINT,
            "search",
            ATTRIBUTE_NAME_FORMAT,
            ATTRIBUTE_NAME_SCOPE,
            ATTRIBUTE_NAME_TYPE,
            ATTRIBUTE_NAME_XML_LANG,
            "dir",
            "translate",
            ATTRIBUTE_NAME_PROCESSING_ROLE,
            ATTRIBUTE_NAME_CASCADE));

    private DITAOTLogger logger;
    private final DocumentBuilder builder;
    /** Key definition map, where map key is the key name and map value is XML definition */  
    private final Map<String, KeyDef> keyDefTable;

    /**
     * Constructor.
     */
    public KeyrefReader() {
        keyDefTable = new HashMap<String, KeyDef>();
        builder = XMLUtils.getDocumentBuilder();
    }
    
    @Override
    public void read(final File filename) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
    }
    
    /**
     * Get key definitions. Each key definition Element has a distinct Document.
     * 
     * @return key definition map where map key is key name and map value is XML definition of the key 
     */
    public Map<String, KeyDef> getKeyDefinition() {
        return Collections.unmodifiableMap(keyDefTable);
    }

    /**
     * Read key definitions
     * 
     * @param filename absolute URI to DITA map with key definitions
     */
    public void read(final URI filename) {
        Document doc = null;
        try {
            doc = builder.parse(new InputSource(filename.toString()));
        } catch (final Exception e) {
            logger.error("Failed to parse map: " + e.getMessage(), e);
            return;
        }
        readMergedMap(doc);
        resolveIntermediate();
    }
    
    private static final DitaClass SUBMAP = new DitaClass("+ map/topicref mapgroup-d/topicgroup ditaot-d/submap ");

    private void readMergedMap(final Document doc) {
        // get maps
        final List<Element> maps = new ArrayList<Element>();
        maps.add(doc.getDocumentElement());
        final NodeList elems = doc.getDocumentElement().getElementsByTagName("*");
        for (int i = 0; i < elems.getLength(); i++) {
            final Element elem = (Element) elems.item(i);
            final String classValue = elem.getAttribute(ATTRIBUTE_NAME_CLASS);
            if (MAP_MAP.matches(classValue) || SUBMAP.matches(classValue)) {
                maps.add(elem);
            }
        }
        for (final Element map: maps) {
            readMap(map);
        }
    }

    private void readMap(final Element map) {
        final NodeList elems = map.getChildNodes();
        for (int i = 0; i < elems.getLength(); i++) {
            if (elems.item(i).getNodeType() == Node.ELEMENT_NODE) {
                final Element elem = (Element) elems.item(i);
                final String keyName = elem.getAttribute(ATTRIBUTE_NAME_KEYS);
                if (!keyName.isEmpty()) {
                    for (final String key: keyName.trim().split("\\s+")) {
                        if (!keyDefTable.containsKey(key)) {
                            final Document d = builder.newDocument();
                            final Element copy = (Element) d.importNode(elem, true);
                            d.appendChild(copy);
                            final String h = copy.getAttribute(ATTRIBUTE_NAME_HREF);
                            final URI href = h.isEmpty() ? null : toURI(h);
                            final String s = copy.getAttribute(ATTRIBUTE_NAME_SCOPE);
                            final String scope = s.isEmpty() ? null : s;
                            final KeyDef keyDef = new KeyDef(key, href, scope, null, copy);
                            keyDefTable.put(key, keyDef);
                        }
                    }
                }
                final String classValue = elem.getAttribute(ATTRIBUTE_NAME_CLASS);
                if (!SUBMAP.matches(classValue)) {
                    readMap(elem);
                }
            }
        }
    }

    /** Resolve intermediate key references. */
    private void resolveIntermediate() {
        final Map<String, KeyDef> entries = new HashMap<String, KeyDef>(keyDefTable);
        for (final Map.Entry<String, KeyDef> e: entries.entrySet()) {
            final KeyDef res = resolveIntermediate(e.getValue());
            keyDefTable.put(e.getKey(), res);
        }
    }

    private KeyDef resolveIntermediate(final KeyDef keyDef) {
        final Element elem = keyDef.element;
        final String keyref = elem.getAttribute(ATTRIBUTE_NAME_KEYREF);
        if (!keyref.isEmpty() && keyDefTable.containsKey(keyref)) {
            KeyDef keyRefDef = keyDefTable.get(keyref);
            Element defElem = keyRefDef.element;
            final String defElemKeyref = defElem.getAttribute(ATTRIBUTE_NAME_KEYREF);
            if (!defElemKeyref.isEmpty()) {
                keyRefDef = resolveIntermediate(keyRefDef);
            }
            final Element res = mergeMetadata(keyRefDef.element, elem);
            res.removeAttribute(ATTRIBUTE_NAME_KEYREF);
            return new KeyDef(keyDef.keys, keyRefDef.href, keyRefDef.scope, keyRefDef.source, res);
        } else {
            return keyDef;
        }
    }

    private Element mergeMetadata(final Element defElem, final Element elem) {
        final Element res = (Element) elem.cloneNode(true);
        final Document d = res.getOwnerDocument();
        final Element defMeta = getTopicmeta(defElem);
        if (defMeta != null) {
            Element resMeta = getTopicmeta(res);
            if (resMeta == null) {
                resMeta = d.createElement(MAP_TOPICMETA.localName);
                resMeta.setAttribute(ATTRIBUTE_NAME_CLASS, MAP_TOPICMETA.toString());
                res.appendChild(resMeta);
            }
            final NodeList cs = defMeta.getChildNodes();
            for (int i = 0; i < cs.getLength(); i++) {
                final Node c = cs.item(i);
                final Node copy = d.importNode(c, true);
                resMeta.appendChild(copy);
            }
        }

        for (final String attr: ATTS) {
            if (res.getAttributeNode(attr) == null) {
                final Attr defAttr = defElem.getAttributeNode(attr);
                if (defAttr != null) {
                    final Attr copy = (Attr) d.importNode(defAttr, true);
                    res.setAttributeNode(copy);
                }
            }
        }
        return res;
    }

    private Element getTopicmeta(final Element topicref) {
        final NodeList ns = topicref.getChildNodes();
        for (int i = 0; i < ns.getLength(); i++) {
            final Node n = ns.item(i);
            if (MAP_TOPICMETA.matches(n)) {
                return (Element) n;
            }
        }
        return null;
    }

}
