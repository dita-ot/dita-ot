/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.reader;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.FilterUtils.DEFAULT;
import static org.dita.dost.util.URLUtils.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.*;

import org.dita.dost.exception.DITAOTXMLErrorHandler;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.util.CatalogUtils;
import org.dita.dost.util.FilterUtils.Action;
import org.dita.dost.util.FilterUtils.FilterKey;
import org.dita.dost.util.XMLUtils;
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

    private XMLReader reader;
    /** List of absolute flagging image paths. */
    private final List<URI> imageList;

    private URI ditaVal = null;

    private Map<String, Map<String, Set<Element>>> bindingMap;
    /** List of relative flagging image paths. */
    private final List<URI> relFlagImageList;

    private boolean setSystemid = true;

    /**
     * Default constructor of DitaValReader class.
     */
    public DitaValReader() {
        super();
        filterMap = new HashMap<>();
        imageList = new ArrayList<>(256);
        relFlagImageList= new ArrayList<>(256);

        try {
            reader = XMLUtils.getXMLReader();
            reader.setContentHandler(this);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e) ;
        }

    }

    /**
     * Set the map of subject scheme definitions. The contents of the map is in pseudo-code
     * {@code Map<AttName, Map<ElemName, Set<Element>>>}. For default element mapping, the value is {@code *}.
     */
    public void setSubjectScheme(final Map<String, Map<String, Set<Element>>> bindingMap) {
        this.bindingMap = bindingMap;
    }
    
    public void initXMLReader(final boolean arg_setSystemid) {
        setSystemid = arg_setSystemid;
        reader.setEntityResolver(CatalogUtils.getCatalogResolver());
    }

    /** Use {@link #read(URI)} instead. */
    @Deprecated
    @Override
    public void read(final File input) {
        assert input.isAbsolute();
        read(input.toURI());
    }

    public void read(final URI input) {
        assert input.isAbsolute();
        ditaVal = input;

        try {
            reader.setErrorHandler(new DITAOTXMLErrorHandler(ditaVal.toString(), logger));
            final InputSource is = new InputSource(input.toString());
            if (setSystemid) {
                is.setSystemId(input.toString());
            }
            reader.parse(is);
        } catch (final IOException | SAXException e) {
            logger.error("Failed to read DITAVAL file: " + e.getMessage(), e);
            return;
        }

        if (bindingMap != null && !bindingMap.isEmpty()) {
            final Map<FilterKey, Action> buf = new HashMap<>(filterMap);
            for (final Map.Entry<FilterKey, Action> e: buf.entrySet()) {
                refineAction(e.getValue(), e.getKey());
            }
        }
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName,
            final Attributes atts) throws SAXException {
        if (ELEMENT_NAME_PROP.equals(qName)) {
            final String attAction = atts.getValue(ELEMENT_NAME_ACTION);
            //first to check if the att attribute and val attribute are null
            //which is a default action for elements without mapping with the other filter val
            final Action action = attAction != null ? Action.valueOf(attAction.toUpperCase()) : null;
            if (action != null) {
                final String attName = atts.getValue(ATTRIBUTE_NAME_ATT);
                final String attValue = atts.getValue(ATTRIBUTE_NAME_VAL);
                final FilterKey key = attName != null ? new FilterKey(attName, attValue) : DEFAULT;
                insertAction(action, key);
            }
        }

        /*
         * Parse image files for flagging
         */
        URI flagImage = null;
        if(atts.getValue(ATTRIBUTE_NAME_IMG)!=null){
            flagImage = toURI(atts.getValue(ATTRIBUTE_NAME_IMG));
        }else if(atts.getValue(ATTRIBUTE_NAME_IMAGEREF)!=null){
            flagImage = toURI(atts.getValue(ATTRIBUTE_NAME_IMAGEREF));
        }
        if (flagImage != null) {
            final URI f = flagImage;
            if (f.isAbsolute()) {
                imageList.add(f);
                relFlagImageList.add(getRelativePath(ditaVal, f));
            } else {
                imageList.add(ditaVal.resolve(f));
                relFlagImageList.add(f);
            }
        }
    }
    
    /**
     * Refine action key with information from subject schemes.
     */
    private void refineAction(final Action action, final FilterKey key) {
        if (key.value != null && bindingMap != null && !bindingMap.isEmpty()) {
            final Map<String, Set<Element>> schemeMap = bindingMap.get(key.attribute);
            if (schemeMap != null && !schemeMap.isEmpty()) {
                for (final Set<Element> submap: schemeMap.values()) {
                    for (final Element e: submap) {
                        final Element subRoot = searchForKey(e, key.value);
                        if (subRoot != null) {
                            insertAction(subRoot, key.attribute, action);
                        }
                    }
                }
            }
        }
    }

    /**
     * Insert subject scheme based action into filetermap if key not present in the map
     * 
     * @param subTree subject scheme definition element
     * @param attName attribute name
     * @param action action to insert
     */
    private void insertAction(final Element subTree, final String attName, final Action action) {
        if (subTree == null || action == null) {
            return;
        }

        final LinkedList<Element> queue = new LinkedList<>();

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
            if (SUBJECTSCHEME_SUBJECTDEF.matches(node)) {
                final String key = node.getAttribute(ATTRIBUTE_NAME_KEYS);
                if (key != null && !key.trim().isEmpty()) {
                    final FilterKey k = new FilterKey(attName, key);
                    if (!filterMap.containsKey(k)) {
                        filterMap.put(k, action);
                    }
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
     * Insert action into filetermap if key not present in the map
     * @param action
     * @param key
     */
    private void insertAction(final Action action, final FilterKey key) {
        if (filterMap.get(key) == null) {
            filterMap.put(key, action);
        } else {
            logger.error(MessageUtils.getInstance().getMessage("DOTJ007E", key.toString()).toString());
        }
    }

    /**
     * Return the image list.
     * @return image list
     */
    public List<URI> getImageList() {
        return imageList;
    }

    /**
     * Return the filter map.
     * @return filter map
     */
    public Map<FilterKey, Action> getFilterMap() {
        return Collections.unmodifiableMap(filterMap);
    }
    
    /**
     * reset.
     */
    public void reset() {
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
    public List<URI> getRelFlagImageList(){
        return relFlagImageList;
    }

}
