/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2010 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.reader;

import com.google.common.annotations.VisibleForTesting;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.expr.parser.Loc;
import net.sf.saxon.om.*;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.serialize.SerializationProperties;
import net.sf.saxon.trans.UncheckedXPathException;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.Untyped;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.log.MessageBean;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.util.Job;
import org.dita.dost.util.KeyDef;
import org.dita.dost.util.KeyScope;
import org.dita.dost.util.XMLUtils;

import javax.xml.parsers.DocumentBuilder;
import java.io.File;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static net.sf.saxon.s9api.streams.Predicates.isElement;
import static net.sf.saxon.s9api.streams.Steps.child;
import static net.sf.saxon.s9api.streams.Steps.precedingSibling;
import static net.sf.saxon.type.BuiltInAtomicType.STRING;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.KeyScope.ROOT_ID;
import static org.dita.dost.util.URLUtils.toURI;
import static org.dita.dost.util.XMLUtils.rootElement;

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
            ATTRIBUTE_NAME_REV,
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
    private Job job;
    private final DocumentBuilder builder;
    private KeyScope rootScope;
    private URI currentFile;
    private XMLUtils xmlUtils;

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

    @Override
    public void setJob(final Job job) {
        this.job = job;
    }

    public void setXmlUtils(XMLUtils xmlUtils) {
        this.xmlUtils = xmlUtils;
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
     * @param doc      key definition DITA map
     */
    public void read(final URI filename, final XdmNode doc) {
        currentFile = filename;
        rootScope = null;
        // TODO: use KeyScope implementation that retains order
        final KeyScope keyScope = readScopes(doc);
        final KeyScope keyScopeWithChildren = cascadeChildKeys(keyScope);
        // TODO: determine effective key definitions here
        final KeyScope keyScopeWithParents = inheritParentKeys(keyScopeWithChildren);
        rootScope = resolveIntermediate(keyScopeWithParents);
    }

    /**
     * Read keys scopes in map.
     */
    private KeyScope readScopes(final XdmNode doc) {
        assert doc.getNodeKind() == XdmNodeKind.DOCUMENT;
        final XdmNode root = doc.select(rootElement()).asNode();
        final List<KeyScope> scopes = readScopesRoot(root);
        if (scopes.size() == 1 && scopes.get(0).name == null) {
            return scopes.get(0);
        } else {
            return new KeyScope(ROOT_ID, null, Collections.emptyMap(), scopes);
        }
    }

    private List<KeyScope> readScopesRoot(final XdmNode root) {
        final List<KeyScope> childScopes = new ArrayList<>();
        final Map<String, KeyDef> keyDefs = new HashMap<>();
        readScope(root, keyDefs);
        readChildScopes(root, childScopes);
        final String keyscope = root.attribute(ATTRIBUTE_NAME_KEYSCOPE);
        if (keyscope == null || keyscope.trim().isEmpty()) {
            return Collections.singletonList(new KeyScope(ROOT_ID, null, keyDefs, childScopes));
        } else {
            final List<KeyScope> res = new ArrayList<>();
            for (final String scope : keyscope.split("\\s+")) {
                res.add(new KeyScope(generateId(root, scope), scope, keyDefs, childScopes));
            }
            return res;
        }
    }

    private String generateId(final XdmNode root, final String scope) {
        final StringBuilder res = new StringBuilder();
        XdmNode elem = root;
        while (elem != null) {
            res.append(elem.getNodeName()).append('[');
            int position = 0;
            for (XdmNode n = elem; n != null; position++) {
                n = n.select(precedingSibling().first()).findFirst().orElse(null);
            }
            res.append(Integer.toString(position)).append(']');
            final XdmNode p = elem.getParent();
            if (p != null && p.getNodeKind() == XdmNodeKind.ELEMENT) {
                elem = p;
            } else {
                elem = null;
            }
        }
        res.append('.').append(scope);
        return res.toString();
    }

    private void readChildScopes(final XdmNode elem, final List<KeyScope> childScopes) {
        elem.select(child(isElement())).forEach(child -> {
            if (child.attribute(ATTRIBUTE_NAME_KEYSCOPE) != null) {
                final List<KeyScope> childScope = readScopesRoot(child);
                childScopes.addAll(childScope);
            } else {
                readChildScopes(child, childScopes);
            }
        });
    }

    /**
     * Read key definitions from a key scope.
     */
    private void readScope(final XdmNode scope, final Map<String, KeyDef> keyDefs) {
        final List<XdmNode> maps = new ArrayList<>();
        maps.add(scope);
        for (final XdmNode child : scope.children(isElement())) {
            collectMaps(child, maps);
        }
        for (final XdmNode map : maps) {
            readMap(map, keyDefs);
        }
    }

    private void collectMaps(final XdmNode elem, final List<XdmNode> maps) {
        if (elem.attribute(ATTRIBUTE_NAME_KEYSCOPE) != null) {
            return;
        }
        final String classValue = elem.attribute(ATTRIBUTE_NAME_CLASS);
        if (MAP_MAP.matches(classValue) || SUBMAP.matches(classValue)) {
            maps.add(elem);
        }
        for (final XdmNode child : elem.children(isElement())) {
            collectMaps(child, maps);
        }
    }

    /**
     * Recursively read key definitions from a single map fragment.
     */
    private void readMap(final XdmNode map, final Map<String, KeyDef> keyDefs) {
        readKeyDefinition(map, keyDefs);
        for (final XdmNode elem : map.children(isElement())) {
            if (!(SUBMAP.matches(elem) || elem.attribute(ATTRIBUTE_NAME_KEYSCOPE) != null)) {
                readMap(elem, keyDefs);
            }
        }
    }

    private void readKeyDefinition(final XdmNode elem, final Map<String, KeyDef> keyDefs) {
        final String keyName = elem.attribute(ATTRIBUTE_NAME_KEYS);
        if (keyName != null) {
            for (final String key : keyName.trim().split("\\s+")) {
                if (!keyDefs.containsKey(key)) {
                    final XdmNode copy = elem;
                    final URI href = toURI(copy.attribute(ATTRIBUTE_NAME_COPY_TO) == null
                            ? copy.attribute(ATTRIBUTE_NAME_HREF)
                            : copy.attribute(ATTRIBUTE_NAME_COPY_TO));
                    final String scope = copy.attribute(ATTRIBUTE_NAME_SCOPE);
                    final String format = copy.attribute(ATTRIBUTE_NAME_FORMAT);
                    final KeyDef keyDef = new KeyDef(key, href, scope, format, currentFile, copy);
                    keyDefs.put(key, keyDef);
                }
            }
        }
    }

    /** Cascade child keys with prefixes to parent key scopes. */
    @VisibleForTesting
    KeyScope cascadeChildKeys(final KeyScope rootScope) {
        final Map<String, KeyDef> res = new HashMap<>(rootScope.keyDefinition);
        cascadeChildKeys(rootScope, res, "");
        return new KeyScope(rootScope.id, rootScope.name, res,
                rootScope.childScopes.stream()
                        .map(this::cascadeChildKeys)
                        .collect(Collectors.toList())
        );
    }

    private void cascadeChildKeys(final KeyScope scope, final Map<String, KeyDef> keys, final String prefix) {
        for (final Map.Entry<String, KeyDef> e: scope.keyDefinition.entrySet()) {
            final KeyDef oldKeyDef = e.getValue();
            final KeyDef newKeyDef = new KeyDef(prefix + oldKeyDef.keys, oldKeyDef.href, oldKeyDef.scope, oldKeyDef.format, oldKeyDef.source, oldKeyDef.element);
            if (!keys.containsKey(newKeyDef.keys)) {
                keys.put(newKeyDef.keys, newKeyDef);
            }
        }
        for (final KeyScope child: scope.childScopes) {
            cascadeChildKeys(child, keys, prefix + child.name + ".");
        }
    }


    /**
     * Inherit parent keys to child key scopes.
     */
    private KeyScope inheritParentKeys(final KeyScope rootScope) {
        return inheritParentKeys(rootScope, Collections.emptyMap());
    }

    private KeyScope inheritParentKeys(final KeyScope current, final Map<String, KeyDef> parent) {
        if (parent.keySet().isEmpty() && current.childScopes.isEmpty()) {
            return current;
        } else {
            final Map<String, KeyDef> resKeys = new HashMap<>();
            resKeys.putAll(current.keyDefinition);
            resKeys.putAll(parent);
            final List<KeyScope> resChildren = new ArrayList<>();
            for (final KeyScope child : current.childScopes) {
                final KeyScope resChild = inheritParentKeys(child, resKeys);
                resChildren.add(resChild);
            }
            return new KeyScope(current.id, current.name, resKeys, resChildren);
        }
    }

    /**
     * Resolve intermediate key references.
     */
    private KeyScope resolveIntermediate(final KeyScope scope) {
        final Map<String, KeyDef> keys = new HashMap<>(scope.keyDefinition);
        for (final Map.Entry<String, KeyDef> e : scope.keyDefinition.entrySet()) {
            final KeyDef res = resolveIntermediate(scope, e.getValue(), Collections.singletonList(e.getValue()));
            keys.put(e.getKey(), res);
        }
        final List<KeyScope> children = new ArrayList<>();
        for (final KeyScope child : scope.childScopes) {
            final KeyScope resolvedChild = resolveIntermediate(child);
            children.add(resolvedChild);
        }
        return new KeyScope(scope.id, scope.name, keys, children);
    }

    private KeyDef resolveIntermediate(final KeyScope scope, final KeyDef keyDef, final List<KeyDef> circularityTracker) {
        final XdmNode elem = keyDef.element;
        final String keyref = elem.attribute(ATTRIBUTE_NAME_KEYREF);
        if (keyref != null && !keyref.trim().isEmpty() && scope.keyDefinition.containsKey(keyref)) {
            KeyDef keyRefDef = scope.keyDefinition.get(keyref);
            if (circularityTracker.contains(keyRefDef)) {
                handleCircularDefinitionException(circularityTracker);
                return keyDef;
            }
            XdmNode defElem = keyRefDef.element;
            final String defElemKeyref = defElem.attribute(ATTRIBUTE_NAME_KEYREF);
            if (defElemKeyref != null && !defElemKeyref.isEmpty()) {
                // TODO use immutable List
                final List<KeyDef> ct = new ArrayList<>(circularityTracker.size() + 1);
                ct.addAll(circularityTracker);
                ct.add(keyRefDef);
                keyRefDef = resolveIntermediate(scope, keyRefDef, ct);
            }
            final XdmNode res = mergeMetadata(keyRefDef.element, elem);
            return new KeyDef(keyDef.keys, keyRefDef.href, keyRefDef.scope, keyRefDef.format, keyRefDef.source, res);
        } else {
            return keyDef;
        }
    }

    private void handleCircularDefinitionException(final List<KeyDef> circularityTracker) {
        final StringBuilder sb = new StringBuilder();
        Collections.reverse(circularityTracker);
        for (final KeyDef keyDef : circularityTracker) {
            sb.append(keyDef.keys).append(" -> ");
        }
        sb.append(circularityTracker.get(0).keys);
        final MessageBean ex = MessageUtils
                .getMessage("DOTJ069E", sb.toString())
                .setLocation(circularityTracker.get(0).element);
        logger.error(ex.toString(), ex.toException());
    }

    private XdmNode mergeMetadata(final XdmNode defElem, final XdmNode refElem) {
        try {
            final XdmDestination dst = new XdmDestination();
            dst.setBaseURI(refElem.getBaseURI());
            dst.setDestinationBaseURI(refElem.getBaseURI());
            final PipelineConfiguration pipe = refElem.getUnderlyingNode().getConfiguration().makePipelineConfiguration();
            final Receiver receiver = dst.getReceiver(pipe, new SerializationProperties());
            receiver.open();
            receiver.startDocument(0);

            final NodeInfo rni = refElem.getUnderlyingNode();

            final AttributeMap atts = new SmallAttributeMap(defElem.getUnderlyingNode().attributes().asList().stream()
                    // only attributes from ATTS
                    .filter(attr -> ATTS.contains(attr.getNodeName().getLocalPart()))
                    // only if refElem doesn't have it.
                    .filter(attr -> refElem.attribute(attr.getNodeName().getLocalPart()) == null)
                    .collect(Collectors.toList()));
            receiver.startElement(
                    NameOfNode.makeName(rni),
                    rni.getSchemaType(),
                    atts,
                    rni.getAllNamespaces(),
                    rni.saveLocation(),
                    0);

            final XdmNode defMeta = getTopicmeta(defElem);
            if (defMeta != null) {
                final XdmNode resMeta = getTopicmeta(refElem);
                if (resMeta == null) {
                    final SingletonAttributeMap attrs = SingletonAttributeMap.of(new AttributeInfo(
                            new NoNamespaceName(ATTRIBUTE_NAME_CLASS),
                            STRING,
                            MAP_TOPICMETA.toString(),
                            Loc.NONE,
                            0));
                    receiver.startElement(
                            new NoNamespaceName(MAP_TOPICMETA.localName),
                            Untyped.getInstance(),
                            attrs,
                            rni.getAllNamespaces(),
                            Loc.NONE,
                            0);
                } else {
                    final NodeInfo ni = resMeta.getUnderlyingNode();

                    receiver.startElement(
                            NameOfNode.makeName(ni),
                            ni.getSchemaType(),
                            resMeta.getUnderlyingNode().attributes()
                                    .remove(new NoNamespaceName(ATTRIBUTE_NAME_KEYREF)),
                            resMeta.getUnderlyingNode().getAllNamespaces(),
                            ni.saveLocation(),
                            0);
                }
                defMeta.select(child()).forEach(child -> {
                    try {
                        receiver.append(child.getUnderlyingNode());
                    } catch (XPathException e) {
                        throw new UncheckedXPathException(e);
                    }
                });
                receiver.endElement();
            }

            receiver.endElement();

            receiver.endDocument();
            receiver.close();
            return dst.getXdmNode().select(rootElement()).asNode();
        } catch (XPathException | UncheckedXPathException e) {
            logger.error("Failed to merge topicmeta: " + e.getMessage(), e);
            return refElem;
        }
    }

    private XdmNode getTopicmeta(final XdmNode topicref) {
        return topicref
                .select(child(c -> MAP_TOPICMETA.matches(c)).first())
                .findAny()
                .orElse(null);
    }

}
