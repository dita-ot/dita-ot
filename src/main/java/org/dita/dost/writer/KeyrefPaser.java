/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2010 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.writer;

import static java.util.Arrays.asList;
import static javax.xml.XMLConstants.NULL_NS_URI;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.*;
import static org.dita.dost.util.XMLUtils.toList;

import java.io.File;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.log.MessageBean;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.util.*;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Filter for processing key reference elements in DITA files.
 * Instances are reusable but not thread-safe.
 */
public final class KeyrefPaser extends AbstractXMLFilter {

    /**
     * Set of attributes which should not be copied from
     * key definition to key reference which is {@code <topicref>}.
     */
    private static final Set<String> no_copy;
    static {
        final Set<String> nc = new HashSet<>();
        nc.add(ATTRIBUTE_NAME_ID);
        nc.add(ATTRIBUTE_NAME_CLASS);
        nc.add(ATTRIBUTE_NAME_XTRC);
        nc.add(ATTRIBUTE_NAME_XTRF);
        nc.add(ATTRIBUTE_NAME_HREF);
        nc.add(ATTRIBUTE_NAME_KEYS);
        nc.add(ATTRIBUTE_NAME_TOC);
        nc.add(ATTRIBUTE_NAME_PROCESSING_ROLE);
        no_copy = Collections.unmodifiableSet(nc);
    }

    /**
     * Set of attributes which should not be copied from
     * key definition to key reference which is not {@code <topicref>}.
     */
    private static final Set<String> no_copy_topic;
    static {
        final Set<String> nct = new HashSet<>(no_copy);
        nct.add("query");
        nct.add("search");
        nct.add(ATTRIBUTE_NAME_TOC);
        nct.add(ATTRIBUTE_NAME_PRINT);
        nct.add(ATTRIBUTE_NAME_COPY_TO);
        nct.add(ATTRIBUTE_NAME_CHUNK);
        nct.add(ATTRIBUTE_NAME_NAVTITLE);
        no_copy_topic = Collections.unmodifiableSet(nct);
    }

    /** List of key reference element definitions. */
    private final static List<KeyrefInfo> keyrefInfos;
    static {
        final List<KeyrefInfo> ki = new ArrayList<>();
        ki.add(new KeyrefInfo(TOPIC_AUTHOR, ATTRIBUTE_NAME_HREF, false, true));
        ki.add(new KeyrefInfo(TOPIC_DATA, ATTRIBUTE_NAME_HREF, false, true));
        ki.add(new KeyrefInfo(TOPIC_DATA_ABOUT, ATTRIBUTE_NAME_HREF, false, true));
        ki.add(new KeyrefInfo(TOPIC_IMAGE, ATTRIBUTE_NAME_HREF, false, true));
        ki.add(new KeyrefInfo(SVG_D_SVGREF, ATTRIBUTE_NAME_HREF, true, false));
        ki.add(new KeyrefInfo(TOPIC_LINK, ATTRIBUTE_NAME_HREF, false, true));
        ki.add(new KeyrefInfo(TOPIC_LQ, ATTRIBUTE_NAME_HREF, false, true));
        ki.add(new KeyrefInfo(MAP_NAVREF, "mapref", true, false));
        ki.add(new KeyrefInfo(TOPIC_PUBLISHER, ATTRIBUTE_NAME_HREF, false, true));
        ki.add(new KeyrefInfo(TOPIC_SOURCE, ATTRIBUTE_NAME_HREF, false, true));
        ki.add(new KeyrefInfo(MAP_TOPICREF, ATTRIBUTE_NAME_HREF, false, false));
        ki.add(new KeyrefInfo(PR_D_CODEREF, ATTRIBUTE_NAME_HREF, true, false));
        ki.add(new KeyrefInfo(TOPIC_XREF, ATTRIBUTE_NAME_HREF, false, true));
        ki.add(new KeyrefInfo(TOPIC_INCLUDE, ATTRIBUTE_NAME_HREF, true, true));
        ki.add(new KeyrefInfo(TOPIC_CITE, ATTRIBUTE_NAME_HREF, false, false));
        ki.add(new KeyrefInfo(TOPIC_DT, ATTRIBUTE_NAME_HREF, false, false));
        ki.add(new KeyrefInfo(TOPIC_KEYWORD, ATTRIBUTE_NAME_HREF, false, false));
        ki.add(new KeyrefInfo(TOPIC_TERM, ATTRIBUTE_NAME_HREF, false, false));
        ki.add(new KeyrefInfo(TOPIC_PH, ATTRIBUTE_NAME_HREF, false, false));
        ki.add(new KeyrefInfo(TOPIC_INDEXTERM, ATTRIBUTE_NAME_HREF, false, false));
        ki.add(new KeyrefInfo(TOPIC_INDEX_BASE, ATTRIBUTE_NAME_HREF, false, false));
        ki.add(new KeyrefInfo(TOPIC_INDEXTERMREF, ATTRIBUTE_NAME_HREF, false, false));
        ki.add(new KeyrefInfo(TOPIC_LONGQUOTEREF, ATTRIBUTE_NAME_HREF, false, false));
        final Map<String, String> objectAttrs = new HashMap<>();
        objectAttrs.put(ATTRIBUTE_NAME_ARCHIVEKEYREFS, ATTRIBUTE_NAME_ARCHIVE);
        objectAttrs.put(ATTRIBUTE_NAME_CLASSIDKEYREF, ATTRIBUTE_NAME_CLASSID);
        objectAttrs.put(ATTRIBUTE_NAME_CODEBASEKEYREF, ATTRIBUTE_NAME_CODEBASE);
        objectAttrs.put(ATTRIBUTE_NAME_DATAKEYREF, ATTRIBUTE_NAME_DATA);
        ki.add(new KeyrefInfo(TOPIC_OBJECT, objectAttrs, true, false));
        final Map<String, String> paramAttrs = new HashMap<>();
        paramAttrs.put(ATTRIBUTE_NAME_KEYREF, ATTRIBUTE_NAME_VALUE);
        ki.add(new KeyrefInfo(TOPIC_PARAM, paramAttrs, true, false));
        keyrefInfos = Collections.unmodifiableList(ki);
    }

    private final static List<String> KEYREF_ATTRIBUTES = Collections.unmodifiableList(asList(
            ATTRIBUTE_NAME_KEYREF,
            ATTRIBUTE_NAME_ARCHIVEKEYREFS,
            ATTRIBUTE_NAME_CLASSIDKEYREF,
            ATTRIBUTE_NAME_CODEBASEKEYREF,
            ATTRIBUTE_NAME_DATAKEYREF
    ));


    /**
     * Stack used to store the current KeyScope, and its start uri.
     */
    private final Deque<KeyScope> definitionMaps;

    /**
     * Stack used to store the place of current element
     * relative to the key reference element.
     */
    private final Deque<Integer> keyrefLevalStack;

    /**
     * Used to store the place of current element
     * relative to the key reference element. If it is out of range of key
     * reference element it is zero, otherwise it is positive number.
     * It is also used to indicate whether current element is descendant of the
     * key reference element.
     */
    private int keyrefLevel;


    /**
     * Indicates whether the keyref is valid.
     * The descendant element should know whether keyref is valid because keyrefs can be nested.
     */
    private final Deque<Boolean> validKeyref;

    /**
     * Flag indicating whether the key reference element is empty,
     * if it is empty, it should pull matching content from the key definition.
     */
    private boolean empty;

    /** Stack of element names of the element containing keyref attribute. */
    private final Deque<String> elemName;

    /** Current element keyref info, {@code null} if not keyref type element. */
    private KeyrefInfo currentElement;

    private boolean hasChecked;

    /** Flag stack to indicate whether key reference element has sub-elements. */
    private final Deque<Boolean> hasSubElem;

    /** Current key definition. */
    private KeyDef keyDef;

    /** Set of link targets which are not resource-only */
    private Set<URI> normalProcessingRoleTargets;
    private MergeUtils mergeUtils;

    /**
     * Constructor.
     */
    public KeyrefPaser() {
        keyrefLevel = 0;
        definitionMaps = new ArrayDeque<>();
        keyrefLevalStack = new ArrayDeque<>();
        validKeyref = new ArrayDeque<>();
        empty = true;
        elemName = new ArrayDeque<>();
        hasSubElem = new ArrayDeque<>();
        mergeUtils = new MergeUtils();
    }

    @Override
    public void setLogger(final DITAOTLogger logger) {
        super.setLogger(logger);
        mergeUtils.setLogger(logger);
    }

    @Override
    public void setJob(final Job job) {
        super.setJob(job);
        mergeUtils.setJob(job);
    }

    public void setKeyDefinition(final KeyScope definitionMap) {
        this.definitionMaps.push(definitionMap);
    }

    /**
     * Get set of link targets which have normal processing role. Paths are relative to current file.
     */
    public Set<URI> getNormalProcessingRoleTargets() {
        return Collections.unmodifiableSet(normalProcessingRoleTargets);
    }

    /**
     * Process key references.
     *
     * @param filename file to process
     * @throws DITAOTException if key reference resolution failed
     */
    @Override
    public void write(final File filename) throws DITAOTException {
        assert filename.isAbsolute();
        super.write(new File(currentFile));
    }

    // XML filter methods ------------------------------------------------------

    @Override
    public void startDocument() throws SAXException {
        normalProcessingRoleTargets = new HashSet<>();
        getContentHandler().startDocument();
    }

    @Override
    public void characters(final char[] ch, final int start, final int length) throws SAXException {
        if (keyrefLevel != 0 && (length == 0 || new String(ch, start, length).trim().isEmpty())) {
            if (!hasChecked) {
                empty = true;
            }
        } else {
            hasChecked = true;
            empty = false;
        }
        getContentHandler().characters(ch, start, length);
    }

    @Override
    public void endElement(final String uri, final String localName, final String name) throws SAXException {
        if (keyrefLevel != 0 && empty) {
            // If current element is in the scope of key reference element
            // and the element is empty
            if (!validKeyref.isEmpty() && validKeyref.peek()) {
                final Element elem = keyDef.element;
                // Key reference is valid,
                // need to pull matching content from the key definition
                // If keyref on topicref, and no topicmeta, copy topicmeta from key definition
                if (elemName.peek().equals(MAP_TOPICREF.localName)) {
                    final Optional<Element> topicmetaNode = XMLUtils.getChildElement(elem, MAP_TOPICMETA);
                    if (topicmetaNode.isPresent()) {
                        domToSax(topicmetaNode.get(), true, false);
                    }
                } else if (!name.equals(elemName.peek())) {
                    // If current element name doesn't equal the key reference element
                    // just grab the content from the matching element of key definition
                    final NodeList nodeList = elem.getElementsByTagName(name);
                    if (nodeList.getLength() > 0) {
                        final Element node = (Element) nodeList.item(0);
                        final NodeList nList = node.getChildNodes();
                        for (int index = 0; index < nList.getLength(); index++) {
                            final Node n = nList.item(index);
                            if (n.getNodeType() == Node.TEXT_NODE) {
                                final char[] ch = n.getNodeValue().toCharArray();
                                getContentHandler().characters(ch, 0, ch.length);
                                break;
                            }
                        }
                    }
                } else {
                    // Current element name equals the key reference element
                    // grab keyword or term from key definition
                    if (!hasSubElem.peek() && currentElement != null) {
                        final List<Element> keywords = toList(elem.getElementsByTagName(TOPIC_KEYWORD.localName));
                        final List<Element> keywordsInKeywords = keywords.stream()
                                .filter(item -> TOPIC_KEYWORDS.matches(item.getParentNode()))
                                .collect(Collectors.toList());
                        // XXX: No need to look for term as content model for keywords doesn't allow it
//                        if (nodeList.getLength() == 0) {
//                            nodeList = elem.getElementsByTagName(TOPIC_TERM.localName);
//                        }
                        if (!keywordsInKeywords.isEmpty()) {
                            if (!currentElement.hasNestedElements) {
                                // only one keyword or term is used.
                                if (!currentElement.isEmpty) {
                                    domToSax(keywordsInKeywords.get(0), false);
                                }
                            } else {
                                // If the key reference element carries href attribute
                                // all keyword or term are used.
                                if (TOPIC_LINK.matches(currentElement.type)) {
                                    final AttributesImpl atts = new AttributesImpl();
                                    XMLUtils.addOrSetAttribute(atts, ATTRIBUTE_NAME_CLASS, TOPIC_LINKTEXT.toString());
                                    getContentHandler().startElement(NULL_NS_URI, TOPIC_LINKTEXT.localName, TOPIC_LINKTEXT.localName, atts);
                                } else if (TOPIC_IMAGE.matches(currentElement.type)) {
                                    final AttributesImpl atts = new AttributesImpl();
                                    XMLUtils.addOrSetAttribute(atts, ATTRIBUTE_NAME_CLASS, TOPIC_ALT.toString());
                                    getContentHandler().startElement(NULL_NS_URI, TOPIC_ALT.localName, TOPIC_ALT.localName, atts);
                                }
                                if (!currentElement.isEmpty) {
                                    for (final Element onekeyword: keywordsInKeywords) {
                                        domToSax(onekeyword, true);
                                    }
                                }
                                if (TOPIC_LINK.matches(currentElement.type)) {
                                    getContentHandler().endElement(NULL_NS_URI, TOPIC_LINKTEXT.localName, TOPIC_LINKTEXT.localName);
                                } else if (TOPIC_IMAGE.matches(currentElement.type)) {
                                    getContentHandler().endElement(NULL_NS_URI, TOPIC_ALT.localName, TOPIC_ALT.localName);
                                }
                            }
                        } else {
                            if (TOPIC_LINK.matches(currentElement.type)) {
                                // If the key reference element is link or its specialization,
                                // should pull in the linktext
                                final NodeList linktext = elem.getElementsByTagName(TOPIC_LINKTEXT.localName);
                                if (linktext.getLength() > 0) {
                                    domToSax((Element) linktext.item(0), true);
                                } else if (fallbackToNavtitleOrHref(elem)) {
                                    final NodeList navtitleElement = elem.getElementsByTagName(TOPIC_NAVTITLE.localName);
                                    if (navtitleElement.getLength() > 0) {
                                        writeLinktext((Element) navtitleElement.item(0));
                                    } else {
                                        final String navtitle = elem.getAttribute(ATTRIBUTE_NAME_NAVTITLE);
                                        if (!navtitle.trim().isEmpty()) {
                                            writeLinktext(navtitle);
                                        } else {
                                            final String hrefAtt = elem.getAttribute(ATTRIBUTE_NAME_HREF);
                                            if (!hrefAtt.trim().isEmpty()) {
                                                writeLinktext(hrefAtt);
                                            }
                                        }
                                    }
                                }
                            } else if (TOPIC_IMAGE.matches(currentElement.type)) {
                                // If the key reference element is an image or its specialization,
                                // should pull in the linktext
                                final NodeList linktext = elem.getElementsByTagName(TOPIC_LINKTEXT.localName);
                                 if (linktext.getLength() > 0) {
                                    writeAlt((Element) linktext.item(0));
                                } else if (fallbackToNavtitleOrHref(elem)) {
                                    final NodeList navtitleElement = elem.getElementsByTagName(TOPIC_NAVTITLE.localName);
                                    if (navtitleElement.getLength() > 0) {
                                        writeAlt((Element) navtitleElement.item(0));
                                    } else {
                                        final String navtitle = elem.getAttribute(ATTRIBUTE_NAME_NAVTITLE);
                                        if (!navtitle.trim().isEmpty()) {
                                            writeAlt(navtitle);
                                        }
                                    }
                                }
                            } else if (!currentElement.isEmpty && fallbackToNavtitleOrHref(elem)) {
                                final NodeList linktext = elem.getElementsByTagName(TOPIC_LINKTEXT.localName);
                                if (linktext.getLength() > 0) {
                                    domToSax((Element) linktext.item(0), false);
                                } else {
                                    final NodeList navtitleElement = elem.getElementsByTagName(TOPIC_NAVTITLE.localName);
                                    if (navtitleElement.getLength() > 0) {
                                        domToSax((Element) navtitleElement.item(0), false);
                                    } else {
                                        final String navtitle = elem.getAttribute(ATTRIBUTE_NAME_NAVTITLE);
                                        if (!navtitle.trim().isEmpty()) {
                                            final char[] ch = navtitle.toCharArray();
                                            getContentHandler().characters(ch, 0, ch.length);
                                        } else {
                                            final String hrefAtt = elem.getAttribute(ATTRIBUTE_NAME_HREF);
                                            if (!hrefAtt.trim().isEmpty()) {
                                                final char[] ch = hrefAtt.toCharArray();
                                                getContentHandler().characters(ch, 0, ch.length);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (keyrefLevel != 0) {
            keyrefLevel--;
            empty = false;
        }

        if (keyrefLevel == 0 && !keyrefLevalStack.isEmpty()) {
            // To the end of key reference, pop the stacks.
            keyrefLevel = keyrefLevalStack.pop();
            validKeyref.pop();
            elemName.pop();
            hasSubElem.pop();
        }

        definitionMaps.pop();

        getContentHandler().endElement(uri, localName, name);
    }

    /**
     * Write linktext element
     *
     * @param srcElem element content
     */
    private void writeLinktext(Element srcElem) throws SAXException {
        final AttributesImpl atts = new AttributesImpl();
        XMLUtils.addOrSetAttribute(atts, ATTRIBUTE_NAME_CLASS, TOPIC_LINKTEXT.toString());
        getContentHandler().startElement(NULL_NS_URI, TOPIC_LINKTEXT.localName, TOPIC_LINKTEXT.localName, atts);
        domToSax(srcElem, false);
        getContentHandler().endElement(NULL_NS_URI, TOPIC_LINKTEXT.localName, TOPIC_LINKTEXT.localName);
    }

    /**
     * Write linktext element
     *
     * @param navtitle element text content
     */
    private void writeLinktext(final String navtitle) throws SAXException {
        final AttributesImpl atts = new AttributesImpl();
        XMLUtils.addOrSetAttribute(atts, ATTRIBUTE_NAME_CLASS, TOPIC_LINKTEXT.toString());
        getContentHandler().startElement(NULL_NS_URI, TOPIC_LINKTEXT.localName, TOPIC_LINKTEXT.localName, atts);
        final char[] ch = navtitle.toCharArray();
        getContentHandler().characters(ch, 0, ch.length);
        getContentHandler().endElement(NULL_NS_URI, TOPIC_LINKTEXT.localName, TOPIC_LINKTEXT.localName);
    }

    /**
     * Write alt element
     *
     * @param srcElem element content
     */
    private void writeAlt(Element srcElem) throws SAXException {
        final AttributesImpl atts = new AttributesImpl();
        XMLUtils.addOrSetAttribute(atts, ATTRIBUTE_NAME_CLASS, TOPIC_ALT.toString());
        getContentHandler().startElement(NULL_NS_URI, TOPIC_ALT.localName, TOPIC_ALT.localName, atts);
        domToSax(srcElem, false);
        getContentHandler().endElement(NULL_NS_URI, TOPIC_ALT.localName, TOPIC_ALT.localName);
    }

    /**
     * Write alt element
     *
     * @param navtitle element text content
     */
    private void writeAlt(final String navtitle) throws SAXException {
        final AttributesImpl atts = new AttributesImpl();
        XMLUtils.addOrSetAttribute(atts, ATTRIBUTE_NAME_CLASS, TOPIC_ALT.toString());
        getContentHandler().startElement(NULL_NS_URI, TOPIC_ALT.localName, TOPIC_ALT.localName, atts);
        final char[] ch = navtitle.toCharArray();
        getContentHandler().characters(ch, 0, ch.length);
        getContentHandler().endElement(NULL_NS_URI, TOPIC_ALT.localName, TOPIC_ALT.localName);
    }

    @Override
    public void startElement(final String uri, final String localName, final String name,
            final Attributes atts) throws SAXException {
        final KeyScope childScope = Optional.ofNullable(atts.getValue(ATTRIBUTE_NAME_KEYSCOPE))
                .flatMap(n -> Optional.ofNullable(definitionMaps.peek().getChildScope(n)))
                .orElse(definitionMaps.peek());
        definitionMaps.push(childScope);

        currentElement = null;
        final String cls = atts.getValue(ATTRIBUTE_NAME_CLASS);
        for (final KeyrefInfo k : keyrefInfos) {
            if (k.type.matches(cls)) {
                currentElement = k;
                break;
            }
        }
        Attributes resAtts = atts;
        hasChecked = false;
        empty = true;
        if (!hasKeyref(atts) || currentElement == null) {
            // If the keyrefLevel doesn't equal 0, it means that current element is under the key reference element;
            if (keyrefLevel != 0) {
                keyrefLevel++;
                hasSubElem.pop();
                hasSubElem.push(true);
            }
        } else {
            elemName.push(name);
            if (keyrefLevel != 0) {
                keyrefLevalStack.push(keyrefLevel);
                hasSubElem.pop();
                hasSubElem.push(true);
            }
            hasSubElem.push(false);
            keyrefLevel = 1;

            resAtts = processElement(atts);
        }

        getContentHandler().startElement(uri, localName, name, resAtts);
    }

    private Attributes processElement(final Attributes atts) {
        final AttributesImpl resAtts = new AttributesImpl(atts);
        boolean valid = false;

        for (final Map.Entry<String, String> attrPair: currentElement.attrs.entrySet()) {
            final String keyrefAttr = attrPair.getKey();
            final String refAttr = attrPair.getValue();

            final String keyrefValue = atts.getValue(keyrefAttr);
            if (keyrefValue != null) {
                final int slashIndex = keyrefValue.indexOf(SLASH);
                String keyName = keyrefValue;
                String elementId = "";
                if (slashIndex != -1) {
                    keyName = keyrefValue.substring(0, slashIndex);
                    elementId = keyrefValue.substring(slashIndex);
                }

                keyDef = definitionMaps.peek().get(keyName);
                final Element elem = keyDef != null ? keyDef.element : null;

                // If definition is not null
                if (keyDef != null) {
                    if (currentElement != null) {
                        final NamedNodeMap attrs = elem.getAttributes();
                        final URI href = keyDef.href;

                        if (href != null && !href.toString().isEmpty()) {
                            if (TOPIC_IMAGE.matches(currentElement.type)) {
                                valid = true;
                                final URI target = keyDef.source.resolve(href);
                                final URI relativeTarget = URLUtils.getRelativePath(currentFile, target);
                                final URI targetOutput = normalizeHrefValue(relativeTarget, elementId);
                                XMLUtils.addOrSetAttribute(resAtts, refAttr, targetOutput.toString());
                            } else if (isLocalDita(elem) && keyDef.source != null) {
                                valid = true;
                                final URI target = keyDef.source.resolve(href);
                                final URI topicFile = currentFile.resolve(stripFragment(target));
                                final URI relativeTarget = setFragment(URLUtils.getRelativePath(currentFile, topicFile), target.getFragment());
                                String topicId = null;
                                if (relativeTarget.getFragment() == null && !"".equals(elementId)) {
                                    topicId = getFirstTopicId(topicFile);
                                }
                                final URI targetOutput = normalizeHrefValue(relativeTarget, elementId, topicId);
                                XMLUtils.addOrSetAttribute(resAtts, refAttr, targetOutput.toString());

                                if (keyDef.scope != null && !keyDef.scope.equals(ATTR_SCOPE_VALUE_LOCAL)) {
                                    XMLUtils.addOrSetAttribute(resAtts, ATTRIBUTE_NAME_SCOPE, keyDef.scope);
                                } else {
                                    XMLUtils.removeAttribute(resAtts, ATTRIBUTE_NAME_SCOPE);
                                }
                                if (keyDef.format != null && !keyDef.format.equals(ATTR_FORMAT_VALUE_DITA)) {
                                    XMLUtils.addOrSetAttribute(resAtts, ATTRIBUTE_NAME_FORMAT, keyDef.format);
                                } else {
                                    XMLUtils.removeAttribute(resAtts, ATTRIBUTE_NAME_FORMAT);
                                }

                                // TODO: This should be a separate SAX filter
                                if (!ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY.equals(atts.getValue(ATTRIBUTE_NAME_PROCESSING_ROLE))) {
                                    final URI f = currentFile.resolve(targetOutput);
                                    normalProcessingRoleTargets.add(f);
                                }
                            } else {
                                valid = true;
                                if (href.isAbsolute() || 
                                        (keyDef.scope != null && keyDef.scope.equals(ATTR_SCOPE_VALUE_EXTERNAL))) {
                                    final URI targetOutput = normalizeHrefValue(href, elementId);
                                    XMLUtils.addOrSetAttribute(resAtts, refAttr, targetOutput.toString());
                                } else { //Adjust path for peer or local references with relative path
                                    final URI target = keyDef.source.resolve(href);
                                    final URI relativeTarget = URLUtils.getRelativePath(currentFile, target);
                                    final URI targetOutput = normalizeHrefValue(relativeTarget, elementId);
                                    XMLUtils.addOrSetAttribute(resAtts, refAttr, targetOutput.toString());
                                }

                                if (keyDef.scope != null && !keyDef.scope.equals(ATTR_SCOPE_VALUE_LOCAL)) {
                                    XMLUtils.addOrSetAttribute(resAtts, ATTRIBUTE_NAME_SCOPE, keyDef.scope);
                                } else {
                                    XMLUtils.removeAttribute(resAtts, ATTRIBUTE_NAME_SCOPE);
                                }
                                if (keyDef.format != null && !keyDef.format.equals(ATTR_FORMAT_VALUE_DITA)) {
                                    XMLUtils.addOrSetAttribute(resAtts, ATTRIBUTE_NAME_FORMAT, keyDef.format);
                                } else {
                                    XMLUtils.removeAttribute(resAtts, ATTRIBUTE_NAME_FORMAT);
                                }
                            }
                        } else if (href == null || href.toString().isEmpty()) {
                            // Key definition does not carry an href or href equals "".
                            valid = true;
                            XMLUtils.removeAttribute(resAtts, ATTRIBUTE_NAME_SCOPE);
                            XMLUtils.removeAttribute(resAtts, ATTRIBUTE_NAME_HREF);
                            XMLUtils.removeAttribute(resAtts, ATTRIBUTE_NAME_TYPE);
                            XMLUtils.removeAttribute(resAtts, ATTRIBUTE_NAME_FORMAT);
                        } else {
                            // key does not exist.
                            final MessageBean m = definitionMaps.peek().name == null
                                    ? MessageUtils.getMessage("DOTJ047I", atts.getValue(ATTRIBUTE_NAME_KEYREF))
                                    : MessageUtils.getMessage("DOTJ048I", atts.getValue(ATTRIBUTE_NAME_KEYREF), definitionMaps.peek().name);
                            logger.info(m.setLocation(atts).toString());
                        }

                        if (valid) {
                            if (MAP_TOPICREF.matches(currentElement.type)) {
                                for (int index = 0; index < attrs.getLength(); index++) {
                                    final Attr attr = (Attr) attrs.item(index);
                                    if (!no_copy.contains(attr.getNodeName())) {
                                        XMLUtils.removeAttribute(resAtts, attr.getNodeName());
                                        XMLUtils.addOrSetAttribute(resAtts, attr);
                                    }
                                }
                            } else {
                                for (int index = 0; index < attrs.getLength(); index++) {
                                    final Attr attr = (Attr) attrs.item(index);
                                    if (!no_copy_topic.contains(attr.getNodeName())
                                            && (attr.getNodeName().equals(refAttr) || resAtts.getIndex(attr.getNodeName()) == -1)) {
                                        XMLUtils.removeAttribute(resAtts, attr.getNodeName());
                                        XMLUtils.addOrSetAttribute(resAtts, attr);
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // key does not exist
                    final MessageBean m = definitionMaps.peek().name == null
                            ? MessageUtils.getMessage("DOTJ047I", atts.getValue(ATTRIBUTE_NAME_KEYREF))
                            : MessageUtils.getMessage("DOTJ048I", atts.getValue(ATTRIBUTE_NAME_KEYREF), definitionMaps.peek().name);
                    logger.info(m.setLocation(atts).toString());
                }

                validKeyref.push(valid);
            }
        }


        return resAtts;
    }

    private boolean hasKeyref(final Attributes atts) {
        if (TOPIC_PARAM.matches(atts) && (atts.getValue(ATTRIBUTE_NAME_VALUETYPE) != null
                && !atts.getValue(ATTRIBUTE_NAME_VALUETYPE).equals(ATTRIBUTE_VALUETYPE_VALUE_REF))) {
            return false;
        }
        for (final String attr: KEYREF_ATTRIBUTES) {
            if (atts.getIndex(attr) != -1) {
                return true;
            }
        }
        return false;
    }

    // Private methods ---------------------------------------------------------

    private boolean isLocalDita(final Element elem) {
        final String scopeValue = elem.getAttribute(ATTRIBUTE_NAME_SCOPE);
        final String formatValue = elem.getAttribute(ATTRIBUTE_NAME_FORMAT);
        return ("".equals(scopeValue) || ATTR_SCOPE_VALUE_LOCAL.equals(scopeValue)) &&
                ("".equals(formatValue) || ATTR_FORMAT_VALUE_DITA.equals(formatValue) || ATTR_FORMAT_VALUE_DITAMAP.equals(formatValue));
    }

    /**
     * Return true when keyref text resolution should use navtitle as a final fallback.
     * @param elem Key definition element
     */
    private boolean fallbackToNavtitleOrHref(final Element elem) {
        final String hrefValue = elem.getAttribute(ATTRIBUTE_NAME_HREF);
        final String locktitleValue = elem.getAttribute(ATTRIBUTE_NAME_LOCKTITLE);
        return ((ATTRIBUTE_NAME_LOCKTITLE_VALUE_YES.equals(locktitleValue)) ||
                ("".equals(hrefValue)) ||
                !(isLocalDita(elem)));
    }

    /**
     * Serialize DOM node into a SAX stream, while modifying map classes to topic classes for common elements.
     *
     * @param elem element to serialize
     * @param retainElements {@code true} to serialize elements, {@code false} to only serialize text nodes.
     */
    private void domToSax(final Element elem, final boolean retainElements) throws SAXException {
        domToSax(elem, retainElements, true);
    }
    
    /**
     * Serialize DOM node into a SAX stream.
     *
     * @param elem element to serialize
     * @param retainElements {@code true} to serialize elements, {@code false} to only serialize text nodes.
     * @param swapMapClass {@code true} to change map/ to topic/ in common class attributes, {@code false} to leave as is
     */
    private void domToSax(final Element elem, final boolean retainElements, final boolean swapMapClass) throws SAXException {
        if (retainElements) {
            final AttributesImpl atts = new AttributesImpl();
            final NamedNodeMap attrs = elem.getAttributes();
            for (int i = 0; i < attrs.getLength(); i++) {
                final Attr a = (Attr) attrs.item(i);
                if (a.getNodeName().equals(ATTRIBUTE_NAME_CLASS) && swapMapClass) {
                    XMLUtils.addOrSetAttribute(atts, ATTRIBUTE_NAME_CLASS, changeclassValue(a.getNodeValue()));
                } else {
                    XMLUtils.addOrSetAttribute(atts, a);
                }
            }
            getContentHandler().startElement(NULL_NS_URI, elem.getNodeName(), elem.getNodeName(), atts);
        }
        final NodeList nodeList = elem.getChildNodes();
        for (int i = 0; i<nodeList.getLength(); i++) {
            final Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                final Element e = (Element) node;
                // retain tm and text elements
                if (TOPIC_TM.matches(e) || TOPIC_TEXT.matches(e)) {
                    domToSax(e, true, swapMapClass);
                } else {
                    domToSax(e, retainElements, swapMapClass);
                }
            } else if (node.getNodeType() == Node.TEXT_NODE) {
                final char[] ch = node.getNodeValue().toCharArray();
                getContentHandler().characters(ch, 0, ch.length);
            }
        }
        if (retainElements) {
            getContentHandler().endElement(NULL_NS_URI, elem.getNodeName(), elem.getNodeName());
        }
    }

    /**
     * Change map type to topic type.
     */
    private String changeclassValue(final String classValue) {
        final DitaClass cls = new DitaClass(classValue);
        if (cls.equals(MAP_LINKTEXT)) {
            return TOPIC_LINKTEXT.toString();
        } else if (cls.equals(MAP_SEARCHTITLE)) {
            return TOPIC_SEARCHTITLE.toString();
        } else if (cls.equals(MAP_SHORTDESC)) {
            return TOPIC_SHORTDESC.toString();
        } else {
            return cls.toString();
        }
    }

    /**
     * change elementId into topicId if there is no topicId in key definition.
     */
    private static URI normalizeHrefValue(final URI keyName, final String tail) {
        if (keyName.getFragment() == null) {
            return toURI(keyName + tail.replaceAll(SLASH, SHARP));
        }
        return toURI(keyName + tail);
    }

    /**
     * Get first topic id
     */
    private String getFirstTopicId(final URI topicFile) {
        return mergeUtils.getFirstTopicId(topicFile, false);
    }

    /**
     * Insert topic id into href
     */
    private static URI normalizeHrefValue(final URI fileName, final String tail, final String topicId) {
        //Insert first topic id only when topicid is not set in keydef
        //and keyref has elementid
        if (fileName.getFragment() == null && !"".equals(tail)) {
            return setFragment(fileName, topicId + tail);
        }
        return toURI(fileName + tail);
    }

    // Inner classes -----------------------------------------------------------

    private static final class KeyrefInfo {

        /** DITA class. */
        final DitaClass type;
        /** Map of key reference to reference attributes. */
        final Map<String, String> attrs;
        /** Element has nested elements. */
        final boolean hasNestedElements;
        /** Element is empty. */
        final boolean isEmpty;

        /**
         * Construct a new key reference info object.
         *
         * @param type element type
         * @param attrs Map of key reference to reference attributes
         * @param isEmpty flag if element is empty
         * @param hasNestedElements element is a reference type
         */
        KeyrefInfo(final DitaClass type, final Map<String, String> attrs, final boolean isEmpty, final boolean hasNestedElements) {
            this.type = type;
            this.attrs = attrs;
            this.isEmpty = isEmpty;
            this.hasNestedElements = hasNestedElements;
        }

        /**
         * Construct a new key reference info object.
         *
         * @param type element type
         * @param refAttr reference attribute name
         * @param isEmpty flag if element is empty
         * @param hasNestedElements element is a reference type
         */
        KeyrefInfo(final DitaClass type, final String refAttr, final boolean isEmpty, final boolean hasNestedElements) {
            final Map<String, String> attrs = new HashMap<>();
            attrs.put(ATTRIBUTE_NAME_KEYREF, refAttr);
            this.type = type;
            this.attrs = attrs;
            this.isEmpty = isEmpty;
            this.hasNestedElements = hasNestedElements;
        }
    }

}
