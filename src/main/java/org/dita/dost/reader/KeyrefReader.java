/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.reader;

import static java.util.Arrays.*;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.*;
import static org.dita.dost.util.XMLUtils.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;

import org.dita.dost.util.KeyDef;
import org.dita.dost.util.KeyScope;
import org.dita.dost.util.XMLUtils;
import org.w3c.dom.*;
import org.dita.dost.log.DITAOTLogger;
import org.xml.sax.InputSource;

/**
 * KeyrefReader class which reads DITA map file to collect key definitions. Instances are reusable but not thread-safe.
 */
public final class KeyrefReader implements AbstractReader {

    private static final List<String> ATTS = Collections.unmodifiableList(asList(
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
    private KeyScope rootScope;

    /**
     * Constructor.
     */
    public KeyrefReader() {
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
     * Get key definitions for root scope. Each key definition Element has a distinct Document.
     * 
     * @return root key scope
     */
    public KeyScope getKeyDefinition() {
        return rootScope;
    }

    /**
     * Read key definitions
     * 
     * @param filename absolute URI to DITA map with key definitions
     */
    public void read(final URI filename, final Document doc) {
        rootScope = null;
        // TODO: use KeyScope implementation that retains order
        KeyScope keyScope = readScopes(doc);
        keyScope = cascadeChildKeys(keyScope);
        // TODO: determine effective key definitions here
        keyScope = inheritParentKeys(keyScope);
        rootScope = resolveIntermediate(keyScope);
    }

    /** Read keys scopes in map. */
    private KeyScope readScopes(final Document doc) {
        final List<KeyScope> scopes = readScopes(doc.getDocumentElement());
        if (scopes.size() == 1) {
            return scopes.get(0);
        } else {
            return new KeyScope(null, Collections.<String, KeyDef>emptyMap(), scopes);
        }
    }
    private List<KeyScope> readScopes(final Element root) {
        final List<KeyScope> childScopes = new ArrayList<>();
        final Map<String, KeyDef> keyDefs = new HashMap<>();
        readScope(root, keyDefs);
        readChildScopes(root, childScopes);
        final String keyscope = root.getAttribute(ATTRIBUTE_NAME_KEYSCOPE).trim();
        if (keyscope.isEmpty()) {
            return asList(new KeyScope(null, keyDefs, childScopes));
        } else {
            final List<KeyScope> res = new ArrayList<>();
            for (final String scope: keyscope.split("\\s+")) {
                res.add(new KeyScope(scope, keyDefs, childScopes));
            }
            return res;
        }
    }

    private void readChildScopes(final Element elem, final List<KeyScope> childScopes) {
        for (final Element child: getChildElements(elem)) {
            if (child.getAttributeNode(ATTRIBUTE_NAME_KEYSCOPE) != null) {
                final List<KeyScope> childScope = readScopes(child);
                childScopes.addAll(childScope);
            }
            else {
                readChildScopes(child, childScopes);
            }
        }
    }

    /** Read key definitions from a key scope. */
    private void readScope(final Element scope, final Map<String, KeyDef> keyDefs) {
        final List<Element> maps = new ArrayList<>();
        maps.add(scope);
        for (final Element child: getChildElements(scope)) {
            collectMaps(child, maps);
        }
        for (final Element map: maps) {
            readMap(map, keyDefs);
        }
    }

    private void collectMaps(final Element elem, final List<Element> maps) {
        if (elem.getAttributeNode(ATTRIBUTE_NAME_KEYSCOPE) != null) {
            return;
        }
        final String classValue = elem.getAttribute(ATTRIBUTE_NAME_CLASS);
        if (MAP_MAP.matches(classValue) || SUBMAP.matches(classValue)) {
            maps.add(elem);
        }
        for (final Element child: getChildElements(elem)) {
            collectMaps(child, maps);
        }
    }

    /** Recursively read key definitions from a single map fragment. */
    private void readMap(final Element map, final Map<String, KeyDef> keyDefs) {
        readKeyDefinition(map, keyDefs);
        for (final Element elem: getChildElements(map)) {
            if (!(SUBMAP.matches(elem) || elem.getAttributeNode(ATTRIBUTE_NAME_KEYSCOPE) != null)) {
                readMap(elem, keyDefs);
            }
        }
    }

    private void readKeyDefinition(final Element elem, final Map<String, KeyDef> keyDefs) {
        final String keyName = elem.getAttribute(ATTRIBUTE_NAME_KEYS);
        if (!keyName.isEmpty()) {
            for (final String key: keyName.trim().split("\\s+")) {
                if (!keyDefs.containsKey(key)) {
                    final Document d = builder.newDocument();
                    final Element copy = (Element) d.importNode(elem, true);
                    d.appendChild(copy);
                    final String h = copy.getAttribute(ATTRIBUTE_NAME_HREF);
                    final URI href = h.isEmpty() ? null : toURI(h);
                    final String s = copy.getAttribute(ATTRIBUTE_NAME_SCOPE);
                    final String scope = s.isEmpty() ? null : s;
                    final KeyDef keyDef = new KeyDef(key, href, scope, null, copy);
                    keyDefs.put(key, keyDef);
                }
            }
        }
    }

    /** Cascade child keys with prefixes to parent key scopes. */
    private KeyScope cascadeChildKeys(final KeyScope rootScope) {
        final Map<String, KeyDef> res = new HashMap<>(rootScope.keyDefinition);
        cascadeChildKeys(rootScope, res, "");
        return new KeyScope(rootScope.name, res, new ArrayList<>(rootScope.childScopes.values()));
    }
    private void cascadeChildKeys(final KeyScope scope, final Map<String, KeyDef> keys, final String prefix) {
        final StringBuilder buf = new StringBuilder(prefix);
        if (scope.name != null) {
            buf.append(scope.name).append(".");
        }
        final String p = buf.toString();
        for (final Map.Entry<String, KeyDef> e: scope.keyDefinition.entrySet()) {
            final KeyDef oldKeyDef = e.getValue();
            final KeyDef newKeyDef = new KeyDef(p + oldKeyDef.keys, oldKeyDef.href, oldKeyDef.scope, oldKeyDef.source, oldKeyDef.element);
            if (!keys.containsKey(newKeyDef.keys)) {
                keys.put(newKeyDef.keys, newKeyDef);
            }
        }
        for (final KeyScope child: scope.childScopes.values()) {
            cascadeChildKeys(child, keys, p);
        }
    }


    /** Inherit parent keys to child key scopes. */
    private KeyScope inheritParentKeys(final KeyScope rootScope) {
        return inheritParentKeys(rootScope, Collections.<String, KeyDef>emptyMap());
    }
    private KeyScope inheritParentKeys(final KeyScope current, final Map<String, KeyDef> parent) {
        if (parent.keySet().isEmpty() && current.childScopes.isEmpty()) {
            return current;
        } else {
            final Map<String, KeyDef> resKeys = new HashMap<>();
            resKeys.putAll(current.keyDefinition);
            resKeys.putAll(parent);
            final List<KeyScope> resChildren = new ArrayList<>();
            for (final KeyScope child: current.childScopes.values()) {
                final KeyScope resChild = inheritParentKeys(child, resKeys);
                resChildren.add(resChild);
            }
            return new KeyScope(current.name, resKeys, resChildren);
        }
    }

    /** Resolve intermediate key references. */
    private KeyScope resolveIntermediate(final KeyScope scope) {
        final Map<String, KeyDef> keys = new HashMap<>(scope.keyDefinition);
        for (final Map.Entry<String, KeyDef> e: scope.keyDefinition.entrySet()) {
            final KeyDef res = resolveIntermediate(scope, e.getValue());
            keys.put(e.getKey(), res);
        }
        final List<KeyScope> children = new ArrayList<>();
        for (final KeyScope child: scope.childScopes.values()) {
            final KeyScope resolvedChild = resolveIntermediate(child);
            children.add(resolvedChild);
        }
        return new KeyScope(scope.name, keys, children);
    }

    private KeyDef resolveIntermediate(final KeyScope scope, final KeyDef keyDef) {
        final Element elem = keyDef.element;
        final String keyref = elem.getAttribute(ATTRIBUTE_NAME_KEYREF);
        if (!keyref.isEmpty() && scope.keyDefinition.containsKey(keyref)) {
            KeyDef keyRefDef = scope.keyDefinition.get(keyref);
            Element defElem = keyRefDef.element;
            final String defElemKeyref = defElem.getAttribute(ATTRIBUTE_NAME_KEYREF);
            if (!defElemKeyref.isEmpty()) {
                keyRefDef = resolveIntermediate(scope, keyRefDef);
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
