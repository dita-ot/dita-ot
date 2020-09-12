/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2014 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.writer;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.*;

import java.io.File;
import java.net.URI;
import java.util.*;

import org.dita.dost.util.DitaClass;
import org.dita.dost.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Export anchor collector filter.
 */
public final class ExportAnchorsFilter extends AbstractXMLFilter {

    /** Basedir of the current parsing file */
    private URI currentDir = null;
    private final Deque<String> stack = new LinkedList<>();
    /** Flag to show whether a file has <exportanchors> tag */
    private boolean hasExport = false;
    private final List<ExportAnchor> exportAnchors = new ArrayList<>();
    private ExportAnchor currentExportAnchor;
    /** Refered topic id */
    private String topicId;
    /** Absolute path to root file */
    private URI rootFilePath = null;
    /** Map to store plugin id */
    private final Map<String, Set<String>> pluginMap = new HashMap<>();
    /** Store the href of topicref tag */
    private URI topicHref;
    /** For topic/dita files whether a </file> tag should be added */
    private boolean shouldAppendEndTag = false;
    private DitaClass rootClass;

    /**
     * Create new export antchors filter.
     */
    public ExportAnchorsFilter() {
        super();
    }

    /**
     * Set processing input file absolute path.
     *
     * @param inputFile absolute path to root file
     */
    public void setInputFile(final URI inputFile) {
        assert inputFile.isAbsolute();
        this.rootFilePath = inputFile;
    }

    /**
     * Set current file absolute path
     *
     * @param currentFile absolute path to current file
     */
    public void setCurrentFile(final URI currentFile) {
        assert currentFile.isAbsolute();
        super.setCurrentFile(currentFile);
        currentDir = currentFile.resolve(".");
    }

    /**
     * Get export anchors.
     *
     * @return list of export anchors
     */
    public List<ExportAnchor> getExportAnchors() {
        return exportAnchors;
    }

    /**
     * @return the pluginMap
     */
    public Map<String, Set<String>> getPluginMap() {
        return pluginMap;
    }

    // SAX methods

    @Override
    public void startDocument() throws SAXException {
        rootClass = null;

        getContentHandler().startDocument();
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes atts)
            throws SAXException {
        final String classValue = atts.getValue(ATTRIBUTE_NAME_CLASS);
        stack.addFirst(classValue);
        if (classValue != null) {
            if (rootClass == null) {
                rootClass = DitaClass.getInstance(classValue);
            }
            if (TOPIC_TOPIC.matches(classValue)) {
                topicId = atts.getValue(ATTRIBUTE_NAME_ID);
                final String filename = currentFile.toString() + QUESTION;
                for (final ExportAnchor e : exportAnchors) {
                    if (e.topicids.contains(filename)) {
                        e.topicids.add(topicId);
                        e.topicids.remove(filename);
                    }
                }
            } else if (MAP_TOPICREF.matches(classValue)) {
                parseAttribute(atts);
            } else if (MAP_MAP.matches(classValue)) {
                if (rootFilePath.equals(currentFile)) {
                    String pluginId = atts.getValue(ATTRIBUTE_NAME_ID);
                    if (pluginId == null) {
                        pluginId = "org.sample.help.doc";
                    }
                    final Set<String> set = StringUtils.restoreSet(pluginId, COMMA);
                    pluginMap.put("pluginId", set);
                }
            } else if (DELAY_D_EXPORTANCHORS.matches(classValue)) {
                hasExport = true;
                if (MAP_MAP.matches(rootClass)) {
                    currentExportAnchor = new ExportAnchor(topicHref);
                    currentExportAnchor.topicids.add(topicId);
                } else if (rootClass == null || TOPIC_TOPIC.matches(rootClass)) {
                    currentExportAnchor = new ExportAnchor(currentFile);
                    currentExportAnchor.topicids.add(topicId);
                    shouldAppendEndTag = true;
                }
            } else if (DELAY_D_ANCHORKEY.matches(classValue)) {
                // TODO in topic file is no keys
                final String keyref = atts.getValue(ATTRIBUTE_NAME_KEYREF);
                currentExportAnchor.keys.add(keyref);
            } else if (DELAY_D_ANCHORID.matches(classValue)) {
                final String id = atts.getValue(ATTRIBUTE_NAME_ID);
                if (MAP_MAP.matches(rootClass)) {
                    if (!topicId.equals(id)) {
                        currentExportAnchor.ids.add(id);
                    }
                } else if (rootClass == null || TOPIC_TOPIC.matches(rootClass)) {
                    if (!topicId.equals(id)) {
                        currentExportAnchor.ids.add(id);
                    }
                }
            }
        }

        getContentHandler().startElement(uri, localName, qName, atts);
    }

    /**
     * Parse the href attribute for needed information.
     *
     * @param atts attributes to process
     */
    private void parseAttribute(final Attributes atts) {
        final URI attrValue = toURI(atts.getValue(ATTRIBUTE_NAME_HREF));
        if (attrValue == null) {
            return;
        }

        final String attrScope = atts.getValue(ATTRIBUTE_NAME_SCOPE);
        if (isExternal(attrValue, attrScope)) {
            return;
        }

        String attrFormat = atts.getValue(ATTRIBUTE_NAME_FORMAT);
        if (attrFormat == null || ATTR_FORMAT_VALUE_DITA.equals(attrFormat)) {
            final File target = toFile(attrValue);
            topicHref = target.isAbsolute() ? attrValue : currentDir.resolve(attrValue);

            if (attrValue.getFragment() != null) {
                topicId = attrValue.getFragment();
            } else {
                if (attrFormat == null || attrFormat.equals(ATTR_FORMAT_VALUE_DITA) || attrFormat.equals(ATTR_FORMAT_VALUE_DITAMAP)) {
                    topicId = topicHref + QUESTION;
                }
            }
        } else {
            topicHref = null;
            topicId = null;
        }
    }

    private boolean isExternal(final URI attrValue, final String attrScope) {
        return ATTR_SCOPE_VALUE_EXTERNAL.equals(attrScope) || ATTR_SCOPE_VALUE_PEER.equals(attrScope)
                || attrValue.toString().contains(COLON_DOUBLE_SLASH) || attrValue.toString().startsWith(SHARP);
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName)
            throws SAXException {
        final String classValue = stack.removeFirst();
        if (classValue != null) {
            if ((MAP_TOPICMETA.matches(classValue) || TOPIC_PROLOG.matches(classValue))
                    && hasExport) {
                if (MAP_MAP.matches(rootClass)) {
                    exportAnchors.add(currentExportAnchor);
                    currentExportAnchor = null;
                }
                hasExport = false;
            }
        }

        getContentHandler().endElement(uri, localName, qName);
    }

    /**
     * Clean up.
     */
    @Override
    public void endDocument() throws SAXException {
        if ((rootClass == null || TOPIC_TOPIC.matches(rootClass)) && shouldAppendEndTag) {
            exportAnchors.add(currentExportAnchor);
            currentExportAnchor = null;
            shouldAppendEndTag = false;
        }

        getContentHandler().endDocument();
    }

    public static class ExportAnchor {
        public final URI file;
        public final Set<String> topicids = new HashSet<>();
        public final Set<String> keys = new HashSet<>();
        public final Set<String> ids = new HashSet<>();

        public ExportAnchor(final URI file) {
            this.file = file;
        }
    }

}
