/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2004, 2005 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.reader;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.module.reader.TempFileNameScheme;
import org.dita.dost.util.Configuration;
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

import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static javax.xml.XMLConstants.XML_NS_PREFIX;
import static javax.xml.XMLConstants.XML_NS_URI;
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

    private static final QName REV = QName.valueOf(ATTRIBUTE_NAME_REV);

    private Set<QName> filterAttributes;
    private Set<QName> flagAttributes;
    protected DITAOTLogger logger;
    protected Job job;
    private final Map<FilterKey, Action> filterMap;
    private String foregroundConflictColor;
    private String backgroundConflictColor;

    private TempFileNameScheme tempFileNameScheme;
    /** List of absolute flagging image paths. */
    private final List<URI> imageList;

    private URI ditaVal = null;

    private Map<QName, Map<String, Set<Element>>> bindingMap;
    /** List of relative flagging image paths. */
    private final List<URI> relFlagImageList;

    /**
     * Default constructor of DitaValReader class.
     */
    public DitaValReader() {
        super();
        filterMap = new HashMap<>();
        imageList = new ArrayList<>(256);
        relFlagImageList = new ArrayList<>(256);

        filterAttributes = Stream.of(Configuration.configuration.getOrDefault("filter-attributes", "")
                .trim().split("\\s*,\\s*"))
                .map(QName::valueOf)
                .collect(Collectors.toSet());
        flagAttributes = Stream.of(Configuration.configuration.getOrDefault("flag-attributes", "")
                .trim().split("\\s*,\\s*"))
                .map(QName::valueOf)
                .collect(Collectors.toSet());
    }

    @VisibleForTesting
    DitaValReader(Set<QName> filterAttributes, Set<QName> flagAttributes) {
        this();
        this.filterAttributes = Sets.union(this.filterAttributes, filterAttributes);
        this.flagAttributes = Sets.union(this.flagAttributes, flagAttributes);
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
    public void setSubjectScheme(final Map<QName, Map<String, Set<Element>>> bindingMap) {
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
        try {
            doc = job.getStore().getDocument(input);
        } catch (IOException e) {
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
        final List<Element> props = toList(root.getElementsByTagName(ELEMENT_NAME_PROP));
        for (final Element prop : props) {
            readProp(prop);
        }
        final List<Element> revprops = toList(root.getElementsByTagName(ELEMENT_NAME_REVPROP));
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
                throw new IllegalArgumentException(MessageUtils.getMessage("DOTJ077F", attAction).toString());
        }
        if (action != null) {
            final QName attName;
            if (elem.getTagName().equals(ELEMENT_NAME_REVPROP)) {
                attName = REV;
            } else {
                final String attValue = getValue(elem, ATTRIBUTE_NAME_ATT);
                if (attValue != null) {
                    if (attValue.contains(":")) {
                        final String[] parts = attValue.split(":");
                        final String uri;
                        if (parts[0].equals(XML_NS_PREFIX)) {
                            uri = XML_NS_URI;
                        } else {
                            uri = elem.lookupNamespaceURI(parts[0]);
                        }
                        attName = new QName(uri, parts[1], parts[0]);
                    } else {
                        attName = QName.valueOf(attValue);
                    }
                } else {
                    attName = null;
                }
                if (attName != null && attName.equals(REV)
                        && !filterAttributes.isEmpty() && !filterAttributes.contains(REV)) {
                    logger.warn(MessageUtils.getMessage("DOTJ074W").toString());
                    return;
                }
            }
            final String attValue = getValue(elem, ATTRIBUTE_NAME_VAL);
            final FilterKey key = attName != null ? new FilterKey(attName, attValue) : DEFAULT;
            insertAction(action, key);
        }
    }

    private Flag readFlag(Element elem) {
        final String style = getValue(elem, ATTRIBUTE_NAME_STYLE);
        return new Flag(
                elem.getLocalName(),
                getValue(elem, ATTRIBUTE_NAME_COLOR),
                getValue(elem, ATTRIBUTE_NAME_BACKCOLOR),
                style != null ? style.trim().split("\\s+") : null,
                getValue(elem, ATTRIBUTE_NAME_CHANGEBAR),
                readFlagImage(elem, "startflag"),
                readFlagImage(elem, "endflag"),
                getValue(elem, ATTRIBUTE_NAME_OUTPUTCLASS));
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
    private void insertAction(final Element subTree, final QName attName, final Action action) {
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
            logger.info(MessageUtils.getMessage("DOTJ007I", key.toString()).toString());
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
     * reset filter map.
     */
    public void filterReset() {
        filterMap.clear();
    }
    /**
     * get image list relative to the .ditaval file.
     * @return image list
     */
    public List<URI> getRelFlagImageList() {
        return relFlagImageList;
    }

    public String getForegroundConflictColor() {
        return foregroundConflictColor;
    }

    public String getBackgroundConflictColor() {
        return backgroundConflictColor;
    }
}
