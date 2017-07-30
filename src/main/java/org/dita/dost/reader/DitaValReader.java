/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2004, 2005 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.reader;

import org.dita.dost.exception.DITAOTXMLErrorHandler;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.module.GenMapAndTopicListModule;
import org.dita.dost.module.GenMapAndTopicListModule.TempFileNameScheme;
import org.dita.dost.util.FilterUtils.Action;
import org.dita.dost.util.FilterUtils.FilterKey;
import org.dita.dost.util.FilterUtils.Flag;
import org.dita.dost.util.FilterUtils.Flag.FlagImage;
import org.dita.dost.util.Job;
import org.dita.dost.util.URLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.FilterUtils.DEFAULT;
import static org.dita.dost.util.URLUtils.getRelativePath;
import static org.dita.dost.util.XMLUtils.*;


/**
 * DitaValReader reads and parses the information from ditaval file which
 * contains the information of filtering and flagging.
 * 
 * @author Zhang, Yuan Peng
 */
public final class DitaValReader implements AbstractReader {

    protected DITAOTLogger logger;
    protected Job job;
    private final Map<FilterKey, Action> filterMap;
    private String foregroundConflictColor;
    private String backgroundConflictColor;

    private TempFileNameScheme tempFileNameScheme;
    private final DocumentBuilder builder;
    /** List of absolute flagging image paths. */
    private final List<URI> imageList;

    private URI ditaVal = null;

    private Map<String, Map<String, Set<Element>>> bindingMap;
    /** List of relative flagging image paths. */
    private final List<URI> relFlagImageList;

    /**
     * Default constructor of DitaValReader class.
     */
    public DitaValReader() {
        super();
        filterMap = new HashMap<>();
        imageList = new ArrayList<>(256);
        relFlagImageList= new ArrayList<>(256);

        try {
            final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setNamespaceAware(true);
            builder = builderFactory.newDocumentBuilder();
        } catch (final ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
    }

    @Override
    public void setJob(final Job job) {
        this.job = job;
        // XXX: This is a hack to disable temp filename generation, because in gen-list the base input dir is unknown.
        if (job.getInputDir() != null) {
            tempFileNameScheme = job.getTempFileNameScheme();
        }
    }

    /**
     * Set the map of subject scheme definitions. The contents of the map is in pseudo-code
     * {@code Map<AttName, Map<ElemName, Set<Element>>>}. For default element mapping, the value is {@code *}.
     */
    public void setSubjectScheme(final Map<String, Map<String, Set<Element>>> bindingMap) {
        this.bindingMap = bindingMap;
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

        final Document doc;
        builder.setErrorHandler(new DITAOTXMLErrorHandler(ditaVal.toString(), logger));
        try {
            doc = builder.parse(input.toString());
        } catch (SAXException | IOException e) {
            logger.error("Failed to read DITAVAL file: " + e.getMessage(), e);
            return;
        }

        readDocument(doc);
        
        if (bindingMap != null && !bindingMap.isEmpty()) {
            final Map<FilterKey, Action> buf = new HashMap<>(filterMap);
            for (final Map.Entry<FilterKey, Action> e: buf.entrySet()) {
                refineAction(e.getValue(), e.getKey());
            }
        }
    }

    private void readDocument(final Document doc) {
        final Element root = doc.getDocumentElement();
        final List<Element> props = toList(root.getElementsByTagName("prop"));
        for (final Element prop : props) {
            readProp(prop);
        }
        final List<Element> revprops = toList(root.getElementsByTagName("revprop"));
        for (final Element revprop : revprops) {
            readProp(revprop);
        }
        final List<Element> styleConflicts = toList(root.getElementsByTagName("style-conflict"));
        for (final Element styleConflict : styleConflicts) {
            readStyleConflict(styleConflict);
        }
    }

    public void readProp(final Element elem)  {
        final String attAction = elem.getAttribute(ELEMENT_NAME_ACTION);
        Action action;
        switch (attAction) {
            case "include":
                action = Action.INCLUDE;
                break;
            case "exclude":
                action = Action.EXCLUDE;
                break;
            case "passthrough":
                action = Action.PASSTHROUGH;
                break;
            case "flag":
                action = readFlag(elem);
                break;
            default:
                throw new IllegalArgumentException("Invalid action: " + attAction);
        }
        if (action != null) {
            final String attName = getValue(elem, ATTRIBUTE_NAME_ATT);
            final String attValue = getValue(elem, ATTRIBUTE_NAME_VAL);
            final FilterKey key = attName != null ? new FilterKey(attName, attValue) : DEFAULT;
            insertAction(action, key);
        }
    }

    private Flag readFlag(Element elem) {
        final String style = getValue(elem, ATTRIBUTE_NAME_STYLE);
        return new Flag(
                getValue(elem, ATTRIBUTE_NAME_COLOR),
                getValue(elem, ATTRIBUTE_NAME_BACKCOLOR),
                style != null ? style.trim().split("\\s+") : null,
                getValue(elem, ATTRIBUTE_NAME_CHANGEBAR),
                readFlagImage(elem, "startflag"),
                readFlagImage(elem, "endflag"));
    }

    private FlagImage readFlagImage(final Element elem, final String name) {
        final NodeList children = elem.getElementsByTagName(name);
        if (children.getLength() != 0) {
            final Element img = (Element) children.item(0);
            URI absolute = null;
            if (!img.getAttribute(ATTRIBUTE_NAME_IMAGEREF).isEmpty()) {
                absolute = URLUtils.toURI(img.getAttribute(ATTRIBUTE_NAME_IMAGEREF));
                URI relative;
                if (absolute.isAbsolute()) {
                    relative = getRelativePath(ditaVal, absolute);
                } else if (!img.getAttributeNS(DITA_OT_NAMESPACE, ATTRIBUTE_NAME_IMAGEREF_URI).isEmpty()) {
                    absolute = URI.create(img.getAttributeNS(DITA_OT_NAMESPACE, ATTRIBUTE_NAME_IMAGEREF_URI));
                    relative = absolute;
                } else {
                    relative = absolute;
                    absolute = ditaVal.resolve(absolute);
                }
                imageList.add(absolute);
                relFlagImageList.add(relative);

                if (tempFileNameScheme != null && job.getFileInfo(absolute) == null) {
                    final URI dstTemp = tempFileNameScheme.generateTempFileName(absolute);
                    final Job.FileInfo.Builder fi = new Job.FileInfo.Builder()
                            .src(absolute)
                            .uri(dstTemp)
                            .format("flag");
                    job.add(fi.build());
                }
            }

            String altText = null;
            final NodeList alts = img.getElementsByTagName("alt-text");
            if (alts.getLength() != 0) {
                altText = getText(alts.item(0));
            }

            if (absolute != null || altText != null) {
                return new FlagImage(absolute, altText);
            }
        }
        return null;
    }

    private void readStyleConflict(final Element elem) {
        foregroundConflictColor = getValue(elem, "foreground-conflict-color");
        backgroundConflictColor = getValue(elem, "background-conflict-color");
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

    public String getForegroundConflictColor() {
        return foregroundConflictColor;
    }

    public String getBackgroundConflictColor() {
        return backgroundConflictColor;
    }
}
