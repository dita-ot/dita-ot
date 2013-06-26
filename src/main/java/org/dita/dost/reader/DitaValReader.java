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

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.dita.dost.exception.DITAOTXMLErrorHandler;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.module.Content;
import org.dita.dost.module.ContentImpl;
import org.dita.dost.util.CatalogUtils;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.FilterUtils.Action;
import org.dita.dost.util.FilterUtils.FilterKey;
import org.dita.dost.util.StringUtils;
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

    private ContentImpl content;

    private XMLReader reader;

    private final List<String> imageList;

    private String ditaVal = null;

    private Map<String, Map<String, Set<Element>>> bindingMap;
    
    private final List<String> relFlagImageList;

    private boolean setSystemid = true;

    /**
     * Default constructor of DitaValReader class.
     */
    public DitaValReader() {
        super();
        filterMap = new HashMap<FilterKey, Action>();
        content = null;
        imageList = new ArrayList<String>(INT_256);
        relFlagImageList= new ArrayList<String>(INT_256);

        try {
            reader = StringUtils.getXMLReader();
            reader.setContentHandler(this);
        } catch (final Exception e) {
            logger.logError(e.getMessage(), e) ;
        }

    }

    public void setSubjectScheme(final Map<String, Map<String, Set<Element>>> bindingMap) {
        this.bindingMap = bindingMap;
    }
    
    public void initXMLReader(final boolean arg_setSystemid) {
        setSystemid = arg_setSystemid;
        reader.setEntityResolver(CatalogUtils.getCatalogResolver());
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
            logger.logError(e.getMessage(), e) ;
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
            //first to check if the att attribute and val attribute are null
            //which is a default action for elements without mapping with the other filter val
            final Action action = attAction != null ? Action.valueOf(attAction.toUpperCase()) : null;
            if (action != null) {
                final String attName = atts.getValue(ATTRIBUTE_NAME_ATT);
                final String attValue = atts.getValue(ATTRIBUTE_NAME_VAL);
                final FilterKey key = attName != null ? new FilterKey(attName, attValue) : DEFAULT;
                insertAction(action, key);
                // Subject scheme
                if (attName != null && attValue != null && bindingMap != null && !bindingMap.isEmpty()) {
                    final Map<String, Set<Element>> schemeMap = bindingMap.get(attName);
                    if (schemeMap != null && !schemeMap.isEmpty()) {
                        for (final Set<Element> submap: schemeMap.values()) {                    
                            for (final Element e: submap) {
                                final Element subRoot = searchForKey(e, attValue);
                                if (subRoot != null) {
                                    insertAction(subRoot, attName, action);
                                }
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
            if (SUBJECTSCHEME_SUBJECTDEF.matches(node)) {
                final String key = node.getAttribute(ATTRIBUTE_NAME_KEYS);
                if (!StringUtils.isEmptyString(key)) {
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
     * Insert action into filetermap if key not present in the map
     * @param action
     * @param key
     */
    private void insertAction(final Action action, final FilterKey key) {
        if (filterMap.get(key) == null) {
            filterMap.put(key, action);
        } else {
            logger.logError(MessageUtils.getInstance().getMessage("DOTJ007E", key.toString()).toString());
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
    public List<String> getRelFlagImageList(){
        return relFlagImageList;
    }

}
