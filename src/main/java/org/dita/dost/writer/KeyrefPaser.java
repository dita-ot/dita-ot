/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.writer;

import static javax.xml.XMLConstants.NULL_NS_URI;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.*;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.util.DitaClass;
import org.dita.dost.util.MergeUtils;
import org.dita.dost.util.StringUtils;
import org.dita.dost.util.URLUtils;
import org.dita.dost.util.XMLUtils;
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
        final Set<String> nc = new HashSet<String>();
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
        final Set<String> nct = new HashSet<String>();
        nct.addAll(no_copy);
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
        final List<KeyrefInfo> ki = new ArrayList<KeyrefInfo>();
        ki.add(new KeyrefInfo(TOPIC_AUTHOR, ATTRIBUTE_NAME_HREF, false));
        ki.add(new KeyrefInfo(TOPIC_DATA, ATTRIBUTE_NAME_HREF, false));
        ki.add(new KeyrefInfo(TOPIC_DATA_ABOUT, ATTRIBUTE_NAME_HREF, false));
        ki.add(new KeyrefInfo(TOPIC_IMAGE, ATTRIBUTE_NAME_HREF, true));
        ki.add(new KeyrefInfo(TOPIC_LINK, ATTRIBUTE_NAME_HREF, false));
        ki.add(new KeyrefInfo(TOPIC_LQ, ATTRIBUTE_NAME_HREF, false));
        ki.add(new KeyrefInfo(MAP_NAVREF, "mapref", true));
        ki.add(new KeyrefInfo(TOPIC_PUBLISHER, ATTRIBUTE_NAME_HREF, false));
        ki.add(new KeyrefInfo(TOPIC_SOURCE, ATTRIBUTE_NAME_HREF, false));
        ki.add(new KeyrefInfo(MAP_TOPICREF, ATTRIBUTE_NAME_HREF, false));
        ki.add(new KeyrefInfo(TOPIC_XREF, ATTRIBUTE_NAME_HREF, false));
        ki.add(new KeyrefInfo(TOPIC_CITE, null, false));
        ki.add(new KeyrefInfo(TOPIC_DT, null, false));
        // links are processed for glossentry processing
        ki.add(new KeyrefInfo(TOPIC_KEYWORD, ATTRIBUTE_NAME_HREF, false, false));
        // links are processed for glossentry processing
        ki.add(new KeyrefInfo(TOPIC_TERM, ATTRIBUTE_NAME_HREF, false, false));
        ki.add(new KeyrefInfo(TOPIC_PH, null, false));
        ki.add(new KeyrefInfo(TOPIC_INDEXTERM, null, false));
        ki.add(new KeyrefInfo(TOPIC_INDEX_BASE, null, false));
        ki.add(new KeyrefInfo(TOPIC_INDEXTERMREF, null, false));
        keyrefInfos = Collections.unmodifiableList(ki);
    }
        
    private Map<String, Element> definitionMap;
    private File tempDir;
    /** File name with relative path to the temporary directory of input file. */
    private File inputFile;

    /**
     * It is stack used to store the place of current element
     * relative to the key reference element. Because keyref can be nested.
     */
    private final Stack<Integer> keyrefLevalStack;

    /**
     * It is used to store the place of current element
     * relative to the key reference element. If it is out of range of key
     * reference element it is zero, otherwise it is positive number.
     * It is also used to indicate whether current element is descendant of the
     * key reference element.
     */
    private int keyrefLeval;

    /**
     * It is used to store the target of the keys
     * In the from the map <keys, target>.
     */
    private Map<String, URI> keyMap;

    /**
     * It is used to indicate whether the keyref is valid.
     * The descendant element should know whether keyref is valid because keyrefs can be nested.
     */
    private final Stack<Boolean> validKeyref;

    /**
     * Flag indicating whether the key reference element is empty,
     * if it is empty, it should pull matching content from the key definition.
     */
    private boolean empty;

    /** Stack of element names of the element containing keyref attribute. */
    private final Stack<String> elemName;

    /** Current element keyref info, {@code null} if not keyref type element. */
    private KeyrefInfo currentElement;

    private boolean hasChecked;

    /** Flag stack to indicate whether key reference element has sub-elements. */
    private final Stack<Boolean> hasSubElem;

    /** Current key definition. */
    private Element elem;

    /** Set of link targets which are not resource-only */
    private Set<File> normalProcessingRoleTargets;
    
    /**
     * Constructor.
     */
    public KeyrefPaser() {
        keyrefLeval = 0;
        keyrefLevalStack = new Stack<Integer>();
        validKeyref = new Stack<Boolean>();
        empty = true;
        keyMap = new HashMap<String, URI>();
        elemName = new Stack<String>();
        hasSubElem = new Stack<Boolean>();
    }
    
    public void setKeyDefinition(final Map<String, Element> definitionMap) {
        this.definitionMap = definitionMap;
    }
    
    /**
     * Set temp dir.
     * @param tempDir temp dir
     */
    public void setTempDir(final File tempDir) {
        this.tempDir = tempDir;
    }
    
    /**
     * Set current file.
     */
    public void setCurrentFile(final File inputFile) {
        this.inputFile = inputFile;
    }
    
    /**
     * Set key map.
     * @param map key map
     */
    public void setKeyMap(final Map<String, URI> map) {
        keyMap = map;
    }
    
    /**
     * Get set of link targets which have normal processing role. Paths are relative to current file.
     */
    public Set<File> getNormalProcessingRoleTargets() {
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
        super.write(new File(tempDir, inputFile.getPath()).getAbsoluteFile());
    }
        
    // XML filter methods ------------------------------------------------------

    @Override
    public void startDocument() throws SAXException {
        normalProcessingRoleTargets = new HashSet<File>();
        getContentHandler().startDocument();
    }
    
    @Override
    public void characters(final char[] ch, final int start, final int length) throws SAXException {
        if (keyrefLeval != 0 && (length == 0 || new String(ch,start,length).trim().length() == 0)) {
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
        if (keyrefLeval != 0 && empty && !elemName.peek().equals(MAP_TOPICREF.localName)) {
            // If current element is in the scope of key reference element
            // and the element is empty
            if (!validKeyref.isEmpty() && validKeyref.peek()) {
                // Key reference is valid,
                // need to pull matching content from the key definition
                NodeList nodeList = null;
                // If current element name doesn't equal the key reference element
                // just grab the content from the matching element of key definition
                if (!name.equals(elemName.peek())) {
                    nodeList = elem.getElementsByTagName(name);
                    if (nodeList.getLength() > 0) {
                        final Node node = nodeList.item(0);
                        final NodeList nList = node.getChildNodes();
                        int index = 0;
                        while (index < nList.getLength()) {
                            final Node n = nList.item(index++);
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
                    nodeList = elem.getElementsByTagName(TOPIC_KEYWORD.localName);
                    if (nodeList.getLength() == 0 ) {
                        nodeList = elem.getElementsByTagName(TOPIC_TERM.localName);
                    }
                    if (!hasSubElem.peek()) {
                        if (nodeList.getLength() > 0) {
                            if (currentElement != null && !currentElement.isRefType) {
                                // only one keyword or term is used.
                                domToSax((Element) nodeList.item(0), false);
                            } else if (currentElement != null) {
                                // If the key reference element carries href attribute
                                // all keyword or term are used.
                                if (TOPIC_LINK.matches(currentElement.type)) {
                                    final AttributesImpl atts = new AttributesImpl();
                                    XMLUtils.addOrSetAttribute(atts, ATTRIBUTE_NAME_CLASS, TOPIC_LINKTEXT.toString());
                                    getContentHandler().startElement(NULL_NS_URI, TOPIC_LINKTEXT.localName, TOPIC_LINKTEXT.localName, atts);
                                }
                                if (!currentElement.isEmpty) {
                                    for(int index = 0; index < nodeList.getLength(); index++) {
                                        final Node node = nodeList.item(index);
                                        if (node.getNodeType() == Node.ELEMENT_NODE) {
                                            domToSax((Element) node, true);
                                        }
                                    }
                                }
                                if (TOPIC_LINK.matches(currentElement.type)) {
                                    getContentHandler().endElement(NULL_NS_URI, TOPIC_LINKTEXT.localName, TOPIC_LINKTEXT.localName);
                                }
                            }
                        } else {
                            if (currentElement != null && TOPIC_LINK.matches(currentElement.type)) {
                                // If the key reference element is link or its specification,
                                // should pull in the linktext
                                final NodeList linktext = elem.getElementsByTagName(TOPIC_LINKTEXT.localName);
                                if (linktext.getLength() > 0) {
                                    domToSax((Element) linktext.item(0), true);
                                } else if (!StringUtils.isEmptyString(elem.getAttribute(ATTRIBUTE_NAME_NAVTITLE))) {
                                    final AttributesImpl atts = new AttributesImpl();
                                    XMLUtils.addOrSetAttribute(atts, ATTRIBUTE_NAME_CLASS, TOPIC_LINKTEXT.toString());
                                    getContentHandler().startElement(NULL_NS_URI, TOPIC_LINKTEXT.localName, TOPIC_LINKTEXT.localName, atts);
                                    if (elem.getAttribute(ATTRIBUTE_NAME_NAVTITLE) != null) {
                                        final char[] ch = elem.getAttribute(ATTRIBUTE_NAME_NAVTITLE).toCharArray();
                                        getContentHandler().characters(ch, 0, ch.length);
                                    }
                                    getContentHandler().endElement(NULL_NS_URI, TOPIC_LINKTEXT.localName, TOPIC_LINKTEXT.localName);
                                }
                            } else if (currentElement != null && currentElement.isRefType) {
                                final NodeList linktext = elem.getElementsByTagName(TOPIC_LINKTEXT.localName);
                                if (linktext.getLength() > 0) {
                                    domToSax((Element) linktext.item(0), false);
                                } else {
                                    if (elem.getAttribute(ATTRIBUTE_NAME_NAVTITLE) != null) {
                                        final char[] ch = elem.getAttribute(ATTRIBUTE_NAME_NAVTITLE).toCharArray();
                                        getContentHandler().characters(ch, 0, ch.length);
                                    }
                                }
                            }
                        }

                    }
                }
            }
        }
        if (keyrefLeval != 0) {
            keyrefLeval--;
            empty = false;
        }

        if (keyrefLeval == 0 && !keyrefLevalStack.empty()) {
            // To the end of key reference, pop the stacks.
            keyrefLeval = keyrefLevalStack.pop();
            validKeyref.pop();
            elemName.pop();
            hasSubElem.pop();
        }
        getContentHandler().endElement(uri, localName, name);
    }
    
    @Override
    public void startElement(final String uri, final String localName, final String name,
            final Attributes atts) throws SAXException {
        currentElement = null;
        final String cls = atts.getValue(ATTRIBUTE_NAME_CLASS);
        for (final KeyrefInfo k: keyrefInfos) {
            if (k.type.matches(cls)) {
                currentElement = k;
            }
        }
        final AttributesImpl resAtts = new AttributesImpl(atts);
        hasChecked = false;
        empty = true;
        boolean valid = false;
        if (atts.getIndex(ATTRIBUTE_NAME_KEYREF) == -1) {
            // If the keyrefLeval doesn't equal 0, it means that current element is under the key reference element
            if (keyrefLeval != 0) {
                keyrefLeval++;
                hasSubElem.pop();
                hasSubElem.push(true);
            }
        } else {
            // If there is @keyref, use the key definition to do
            // combination.
            // HashSet to store the attributes copied from key
            // definition to key reference.

            elemName.push(name);
            //hasKeyref = true;
            if (keyrefLeval != 0) {
                keyrefLevalStack.push(keyrefLeval);
                hasSubElem.pop();
                hasSubElem.push(true);
            }
            hasSubElem.push(false);
            keyrefLeval = 0;
            keyrefLeval++;
            //keyref.push(atts.getValue(ATTRIBUTE_NAME_KEYREF));
            //the @keyref could be in the following forms:
            // 1.keyName 2.keyName/elementId
            /*String definition = ((Hashtable<String, String>) content
                    .getValue()).get(atts
                    .getValue(ATTRIBUTE_NAME_KEYREF));*/
            final String keyrefValue = atts.getValue(ATTRIBUTE_NAME_KEYREF);
            final int slashIndex = keyrefValue.indexOf(SLASH);
            String keyName= keyrefValue;
            String elementId= "";
            if (slashIndex != -1) {
                keyName = keyrefValue.substring(0, slashIndex);
                elementId = keyrefValue.substring(slashIndex);
            }
            elem = definitionMap.get(keyName);

            // If definition is not null
            if (elem!=null) {
                final NamedNodeMap namedNodeMap = elem.getAttributes();
                // first resolve the keyref attribute
                if (currentElement != null && currentElement.refAttr != null) {
                    final URI target = keyMap.get(keyName);
                    if (target != null && target.toString().length() != 0) {
                        URI target_output = target;
                        // if the scope equals local, the target should be verified that
                        // it exists.
                        final String scopeValue = elem.getAttribute(ATTRIBUTE_NAME_SCOPE);
                        final String formatValue = elem.getAttribute(ATTRIBUTE_NAME_FORMAT);
                        if (TOPIC_IMAGE.matches(currentElement.type)) {
                            valid = true;
                            target_output = normalizeHrefValue(URLUtils.getRelativePath(tempDir.toURI().resolve(inputFile.getPath()), tempDir.toURI().resolve(target)), elementId);
                            XMLUtils.addOrSetAttribute(resAtts, currentElement.refAttr, target_output.toString());
                        } else if (("".equals(scopeValue) || ATTR_SCOPE_VALUE_LOCAL.equals(scopeValue)) &&
                                ("".equals(formatValue) || ATTR_FORMAT_VALUE_DITA.equals(formatValue)  || ATTR_FORMAT_VALUE_DITAMAP.equals(formatValue))) {
                            final File topicFile = toFile(tempDir.toURI().resolve(stripFragment(target)));
                            if (topicFile.exists()) {   
                                valid = true;
                                final String topicId = getFirstTopicId(topicFile);
                                target_output = normalizeHrefValue(URLUtils.getRelativePath(tempDir.toURI().resolve(toURI(inputFile)), tempDir.toURI().resolve(target)), elementId, topicId);
                                XMLUtils.addOrSetAttribute(resAtts, currentElement.refAttr, target_output.toString());
                                if (!ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY.equals(atts.getValue(ATTRIBUTE_NAME_PROCESSING_ROLE))) {
                                    final URI f = toURI(inputFile).resolve(target_output);
                                    normalProcessingRoleTargets.add(URLUtils.toFile(f));
                                }
                            } else {
                                // referenced file does not exist, emits a message.
                                // Should only emit this if in a debug mode; comment out for now
                                /*Properties prop = new Properties();
                                prop.put("%1", atts.getValue(ATTRIBUTE_NAME_KEYREF));
                                javaLogger
                                        .logInfo(MessageUtils.getInstance().getMessage("DOTJ047I", prop)
                                                .toString());*/
                            }
                        }
                        // scope equals peer or external
                        else {
                            valid = true;
                            target_output = normalizeHrefValue(target_output, elementId);
                            XMLUtils.addOrSetAttribute(resAtts, currentElement.refAttr, target_output.toString());
                        }

                    } else if (target == null || target.toString().length() == 0) {
                        // Key definition does not carry an href or href equals "".
                        valid = true;
                        XMLUtils.removeAttribute(resAtts, ATTRIBUTE_NAME_SCOPE);
                        XMLUtils.removeAttribute(resAtts, ATTRIBUTE_NAME_HREF);
                        XMLUtils.removeAttribute(resAtts, ATTRIBUTE_NAME_TYPE);
                        XMLUtils.removeAttribute(resAtts, ATTRIBUTE_NAME_FORMAT);
                    } else {
                        // key does not exist.
                        logger.info(MessageUtils.getInstance().getMessage("DOTJ047I", atts.getValue(ATTRIBUTE_NAME_KEYREF)).setLocation(atts).toString());
                    }

                } else if (currentElement != null && !currentElement.isRefType) {
                    valid = true;
                    XMLUtils.removeAttribute(resAtts, ATTRIBUTE_NAME_SCOPE);
                    XMLUtils.removeAttribute(resAtts, ATTRIBUTE_NAME_HREF);
                    XMLUtils.removeAttribute(resAtts, ATTRIBUTE_NAME_TYPE);
                    XMLUtils.removeAttribute(resAtts, ATTRIBUTE_NAME_FORMAT);
                }


                // copy attributes in key definition to key reference
                // Set no_copy and no_copy_topic define some attributes should not be copied.
                if (valid) {
                    if (currentElement != null && MAP_TOPICREF.matches(currentElement.type)) {
                        // @keyref in topicref
                        for (int index = 0; index < namedNodeMap.getLength(); index++) {
                            final Node node = namedNodeMap.item(index);
                            if (node.getNodeType() == Node.ATTRIBUTE_NODE
                                    && !no_copy.contains(node.getNodeName())) {
                                XMLUtils.removeAttribute(resAtts, node.getNodeName());
                                XMLUtils.addOrSetAttribute(resAtts, node);
                            }
                        }
                    } else {
                        // @keyref not in topicref
                        // different elements have different attributes
                        if (currentElement != null && currentElement.isRefType) {
                            // current element with href attribute
                            for (int index = 0; index < namedNodeMap.getLength(); index++) {
                                final Node node = namedNodeMap.item(index);
                                if (node.getNodeType() == Node.ATTRIBUTE_NODE
                                        && !no_copy_topic.contains(node.getNodeName())
                                        && (node.getNodeName().equals(currentElement.refAttr) || resAtts.getIndex(node.getNodeName()) == -1)) {
                                    XMLUtils.removeAttribute(resAtts, node.getNodeName());
                                    XMLUtils.addOrSetAttribute(resAtts, node);
                                }
                            }
                        } else if (currentElement != null && !currentElement.isRefType) {
                            // current element without href attribute
                            // so attributes about href should not be copied.
                            for (int index = 0; index < namedNodeMap.getLength(); index++) {
                                final Node node = namedNodeMap.item(index);
                                if (node.getNodeType() == Node.ATTRIBUTE_NODE
                                        && !no_copy_topic.contains(node.getNodeName())
                                        && !(node.getNodeName().equals(ATTRIBUTE_NAME_SCOPE)
                                                || node.getNodeName().equals(ATTRIBUTE_NAME_FORMAT)
                                                || node.getNodeName().equals(ATTRIBUTE_NAME_TYPE))) {
                                    XMLUtils.removeAttribute(resAtts, node.getNodeName());
                                    XMLUtils.addOrSetAttribute(resAtts, node);
                                }
                            }
                        }

                    }
                } else {
                    // keyref is not valid, don't copy any attribute.
                }
            } else {
                // key does not exist
                logger.info(MessageUtils.getInstance().getMessage("DOTJ047I", atts.getValue(ATTRIBUTE_NAME_KEYREF)).setLocation(atts).toString());
            }

            validKeyref.push(valid);
        }

        getContentHandler().startElement(uri, localName, name, resAtts);
    }

    // Private methods ---------------------------------------------------------

    /**
     * Serialize DOM node into a SAX stream.
     * 
     * @param elem element to serialize
     * @param retainElements {@code true} to serialize elements, {@code false} to only serialize text nodes.
     */
    private void domToSax(final Element elem, final boolean retainElements) throws SAXException{
        // use retainElements to indicate that whether there is need to copy the element name
        if (retainElements) {
            final AttributesImpl atts = new AttributesImpl();
            final NamedNodeMap namedNodeMap = elem.getAttributes();
            for (int i = 0; i < namedNodeMap.getLength(); i++) {
                final Attr a = (Attr) namedNodeMap.item(i);
                if (a.getNodeName().equals(ATTRIBUTE_NAME_CLASS)) {
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
                //special process for tm tag.
                if (TOPIC_TM.matches(e)) {
                    domToSax(e, true);
                } else {
                    // If the type of current node is ELEMENT_NODE, process current node.
                    domToSax(e, retainElements);
                }
                // If the type of current node is ELEMENT_NODE, process current node.
                //stringBuffer.append(nodeToString((Element) node, retainElements));
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
    private String getFirstTopicId(final File topicFile) {
        final File path = topicFile.getParentFile();
        final URI name = toURI(topicFile.getName());
        return MergeUtils.getFirstTopicId(name, path, false);
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
        /** Reference attribute name. */
        final String refAttr;
        /** Element is reference type. */
        final boolean isRefType;
        /** Element is empty. */
        final boolean isEmpty;
        /**
         * Construct a new key reference info object.
         * 
         * @param type element type
         * @param refAttr hyperlink attribute name
         * @param isEmpty flag if element is empty
         * @param isRefType element is a reference type
         */
        KeyrefInfo(final DitaClass type, final String refAttr, final boolean isEmpty, final boolean isRefType) {
            this.type = type;
            this.refAttr = refAttr;
            this.isEmpty = isEmpty;
            this.isRefType = isRefType;
        }
        /**
         * Construct a new key reference info object.
         * 
         * @param type element type
         * @param refAttr hyperlink attribute name
         * @param isEmpty flag if element is empty
         */
        KeyrefInfo(final DitaClass type, final String refAttr, final boolean isEmpty) {
            this(type, refAttr, isEmpty, refAttr != null);
        }
    }
    
}
