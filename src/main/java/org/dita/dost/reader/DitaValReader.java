/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.reader;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.FilterUtils.DEFAULT;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.dita.dost.exception.DITAOTXMLErrorHandler;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.module.Content;
import org.dita.dost.module.ContentImpl;
import org.dita.dost.util.CatalogUtils;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.FilterUtils.Action;
import org.dita.dost.util.FilterUtils.FilterKey;
import org.dita.dost.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;


/**
 * DitaValReader reads and parses the information from ditaval file which
 * contains the information of filtering and flagging.
 * 
 * @author Zhang, Yuan Peng
 */
public final class DitaValReader extends AbstractXMLReader {
    private final Map<FilterKey, Action> filterMap;

    private final Map<FilterKey, Action> schemeFilterMap;

    private ContentImpl content;

    private XMLReader reader;

    private final List<String> imageList;

    private String ditaVal = null;

    private final List<String> relFlagImageList;

    private final Map<String, Map<String, Set<Element>>> bindingMap;

    private final Map<String, Map<String, Set<String>>> validValuesMap;

    private final Map<String, Map<String, String>> defaultValueMap;

    private Element schemeRoot = null;

    private boolean setSystemid = true;

    /**
     * Default constructor of DitaValReader class.
     */
    public DitaValReader() {
        super();
        filterMap = new HashMap<FilterKey, Action>();
        schemeFilterMap = new HashMap<FilterKey, Action>();
        content = null;
        imageList = new ArrayList<String>(INT_256);
        relFlagImageList= new ArrayList<String>(INT_256);
        validValuesMap = new HashMap<String, Map<String, Set<String>>>();
        defaultValueMap = new HashMap<String, Map<String, String>>();
        bindingMap = new HashMap<String, Map<String, Set<Element>>>();

        try {
            reader = StringUtils.getXMLReader();
            reader.setContentHandler(this);
        } catch (final Exception e) {
            logger.logException(e);
        }

    }

    public void initXMLReader(final boolean arg_setSystemid) {
        setSystemid = arg_setSystemid;
        try {
            Class.forName(RESOLVER_CLASS);
            reader.setEntityResolver(CatalogUtils.getCatalogResolver());
        }catch (final ClassNotFoundException e){
            reader.setEntityResolver(this);
        }
    }

    @Override
    public void read(final String input) {
        ditaVal = input;

        try {

            reader.setErrorHandler(new DITAOTXMLErrorHandler(ditaVal, logger));
            final File file = new File(input);
            final InputSource is = new InputSource(new FileInputStream(file));
            //Set the system ID
            if(setSystemid) {
                //is.setSystemId(URLUtil.correct(file).toString());
                is.setSystemId(file.toURI().toURL().toString());
            }
            reader.parse(is);

        } catch (final Exception e) {
            logger.logException(e);
        }
    }

    /**
     * @return content collection {@code Set<Entry<String, String>>}
     */
    @Override
    public Content getContent() {
        content = new ContentImpl();
        content.setCollection(filterMap.entrySet());
        return content;
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName,
            final Attributes atts) throws SAXException {
        String flagImage = null;
        if(atts.getValue(ATTRIBUTE_NAME_IMG)!=null){
            flagImage = atts.getValue(ATTRIBUTE_NAME_IMG);
        }else if(atts.getValue(ATTRIBUTE_NAME_IMAGEREF)!=null){
            flagImage = atts.getValue(ATTRIBUTE_NAME_IMAGEREF);
        }

        if (ELEMENT_NAME_PROP.equals(qName)) {
            final String attAction = atts.getValue(ELEMENT_NAME_ACTION);
            final String attName = atts.getValue(ATTRIBUTE_NAME_ATT);
            final String attValue = atts.getValue(ATTRIBUTE_NAME_VAL);
            //first to check if the att attribute and val attribute are null
            //which is a default action for elements without mapping with the other filter val
            final FilterKey key = attName != null ? new FilterKey(attName, attValue) : DEFAULT;
            final Action action = attAction != null ? Action.valueOf(attAction.toUpperCase()) : null;
            if (action != null) {
                insertAction(action, key);
            }

            if (attName != null && attValue != null && bindingMap != null && !bindingMap.isEmpty()) {
                final Map<String, Set<Element>> schemeMap = bindingMap.get(attName);
                if (schemeMap != null && !schemeMap.isEmpty()) {
                    final Iterator<Set<Element>> subTreeIter = schemeMap.values().iterator();
                    while (subTreeIter.hasNext()) {
                        final Iterator<Element> subTreeSet = subTreeIter.next().iterator();
                        while (subTreeSet.hasNext()) {
                            final Element subRoot = this.searchForKey(subTreeSet.next(), attValue);
                            if (subRoot != null && action != null) {
                                this.insertAction(subRoot, attName, action);
                            }
                        }
                    }
                }
            }
        }

        /*
         * Parse image files for flagging
         */
        if (flagImage != null && flagImage.trim().length() > 0) {
            if (new File(flagImage).isAbsolute()) {
                imageList.add(flagImage);
                relFlagImageList.add(FileUtils.getRelativePath(ditaVal, flagImage));
                return;
            }

            // img is a relative path to the .ditaval file
            final String filterDir = new File(ditaVal).getParent();
            imageList.add(new File(filterDir, flagImage).getAbsolutePath());
            relFlagImageList.add(flagImage);
        }
    }

    /**
     * Insert action into filetermap if key not present in the map
     * @param action
     * @param key
     */
    private void insertAction(final Action action, final FilterKey key) {
        if (filterMap.get(key) == null) {
            filterMap.put(key, action);
        } else {
            final Properties prop = new Properties();
            prop.put("%1", key);
            logger.logError(MessageUtils.getInstance().getMessage("DOTJ007E", prop)
                    .toString());
        }
    }

    private void insertAction(final Element subTree, final String attName, final Action action) {
        if (subTree == null || action == null) {
            return;
        }

        final LinkedList<Element> queue = new LinkedList<Element>();

        // Skip the sub-tree root because it has been added already.
        NodeList children = subTree.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                queue.offer((Element)children.item(i));
            }
        }

        while (!queue.isEmpty()) {
            final Element node = queue.poll();
            children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    queue.offer((Element)children.item(i));
                }
            }
            final String attrValue = node.getAttribute(ATTRIBUTE_NAME_CLASS);
            if (attrValue != null && SUBJECTSCHEME_SUBJECTDEF.matches(attrValue)) {
                final String key = node.getAttribute(ATTRIBUTE_NAME_KEYS);
                if (!StringUtils.isEmptyString(key)) {
                    final FilterKey k = new FilterKey(attName, key);
                    if (!schemeFilterMap.containsKey(k)) {
                        schemeFilterMap.put(k, action);
                    }
                    //					else {
                    //						Properties prop = new Properties();
                    //						prop.put("%1", key);
                    //						logger.logError(MessageUtils.getInstance().getMessage("DOTJ007E", prop)
                    //								.toString());
                    //					}
                }
            }
        }
    }

    /**
     * Return the image list.
     * @return image list
     */
    public List<String> getImageList() {
        return imageList;
    }

    /**
     * Return the filter map.
     * @return filter map
     */
    public Map<FilterKey, Action> getFilterMap() {
        final Map<FilterKey, Action> res = new HashMap<FilterKey, Action>();
        res.putAll(schemeFilterMap);
        res.putAll(filterMap);
        return Collections.unmodifiableMap(res);
    }
    
    /**
     * Get subject scheme filter map
     *  
     * @return subject scheme filter map
     */
    public Map<FilterKey, Action> getSchemeFilterMap() {
        return Collections.unmodifiableMap(schemeFilterMap);
    }
    
    /**
     * reset.
     */
    public void reset() {
        schemeFilterMap.clear();
        validValuesMap.clear();
        defaultValueMap.clear();
    }
    /**
     * reset filter map.
     */
    public void filterReset() {
        filterMap.clear();
    }
    /**
     * get image list relative to the .ditaval file.
     * @return image list
     */
    public List<String> getRelFlagImageList(){
        return relFlagImageList;
    }
    /**
     * load schema file.
     * @param scheme scheme file
     */
    public void loadSubjectScheme(final String scheme) {

        if (!FileUtils.fileExists(scheme)) {
            return;
        }

        //schemeFilterMap.clear();

        try {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final Document doc = builder.parse(new InputSource(new FileInputStream(
                    new File(scheme))));
            schemeRoot = doc.getDocumentElement();
            if (schemeRoot == null) {
                return;
            }
            final NodeList rootChildren = schemeRoot.getChildNodes();
            for (int i = 0; i < rootChildren.getLength(); i++) {
                if (rootChildren.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element node = (Element)rootChildren.item(i);
                    String attrValue = node.getAttribute(ATTRIBUTE_NAME_CLASS);
                    if (attrValue != null
                            && SUBJECTSCHEME_ENUMERATIONDEF.matches(attrValue)) {
                        final NodeList enumChildren = node.getChildNodes();
                        String elementName = "*";
                        String attributeName = null;
                        for (int j = 0; j < enumChildren.getLength(); j++) {
                            if (enumChildren.item(j).getNodeType() == Node.ELEMENT_NODE) {
                                node = (Element)enumChildren.item(j);
                                attrValue = node.getAttribute(ATTRIBUTE_NAME_CLASS);
                                if (attrValue != null
                                        && SUBJECTSCHEME_ELEMENTDEF.matches(attrValue)) {
                                    elementName = node.getAttribute(ATTRIBUTE_NAME_NAME);
                                } else if (attrValue != null
                                        && SUBJECTSCHEME_ATTRIBUTEDEF.matches(attrValue)) {
                                    attributeName = node.getAttribute(ATTRIBUTE_NAME_NAME);
                                    Map<String, Set<Element>> S = bindingMap.get(attributeName);
                                    if (S == null) {
                                        S = new HashMap<String, Set<Element>>();
                                        bindingMap.put(attributeName, S);
                                    }
                                } else if (attrValue != null
                                        && SUBJECTSCHEME_DEFAULTSUBJECT.matches(attrValue)) {
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
                                } else if (attrValue != null
                                        && SUBJECTSCHEME_SUBJECTDEF.matches(attrValue)) {
                                    // Search for attributeName in schemeRoot
                                    String keyValue = node
                                            .getAttribute(ATTRIBUTE_NAME_KEYREF);
                                    if (StringUtils.isEmptyString(keyValue)) {
                                        keyValue = node
                                                .getAttribute(ATTRIBUTE_NAME_KEYS);
                                    }
                                    final Element subTree = searchForKey(schemeRoot,
                                            keyValue);
                                    if (subTree != null) {
                                        Map<String, Set<Element>> S = bindingMap
                                                .get(attributeName);
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
            this.logger.logException(e);
        }
    }

    private void putValuePairsIntoMap(final Element subtree, final String elementName, final String attName) {
        if (subtree == null || attName == null) {
            return;
        }

        Map<String, Set<String>> valueMap = this.validValuesMap.get(attName);
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
            final String attrValue = node.getAttribute(ATTRIBUTE_NAME_CLASS);
            if (attrValue != null && SUBJECTSCHEME_SUBJECTDEF.matches(attrValue)) {
                final String key = node.getAttribute(ATTRIBUTE_NAME_KEYS);
                if (!StringUtils.isEmptyString(key)) {
                    valueSet.add(key);
                }
            }
        }
        valueMap.put(elementName, valueSet);
        this.validValuesMap.put(attName, valueMap);
    }

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
            final String attrValue = node.getAttribute(ATTRIBUTE_NAME_CLASS);
            if (attrValue != null && SUBJECTSCHEME_SUBJECTDEF.matches(attrValue)) {
                final String key = node.getAttribute(ATTRIBUTE_NAME_KEYS);
                if (keyValue.equals(key)) {
                    return node;
                }
            }
        }
        return null;
    }

    /**
     * @return the validValuesMap
     */
    public Map<String, Map<String, Set<String>>> getValidValuesMap() {
        return validValuesMap;
    }
    /**
     * get map of default value.
     * @return default value map
     */
    public Map<String, Map<String, String>> getDefaultValueMap() {
        return this.defaultValueMap;
    }

}
