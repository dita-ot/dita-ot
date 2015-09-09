/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */
package org.dita.dost.writer;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.*;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    /** Topicmeta set for merge multiple exportanchors into one. Each topicmeta/prolog can define many exportanchors */
    private final Set<String> topicMetaSet = new HashSet<>(16);
    /** Flag to show whether a file has <exportanchors> tag */
    private boolean hasExport = false;
    /** Absolute system path to file being processed */
    private URI currentFile = null;
    private final List<ExportAnchor> exportAnchors = new ArrayList<>();
    private ExportAnchor currentExportAnchor;
    /** Refered topic id */
    private String topicId = "";
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
        this.rootFilePath = inputFile;
    }
    
    /**
     * Set current file absolute path
     * 
     * @param currentFile absolute path to current file
     */
    public void setCurrentFile(final URI currentFile) {
        this.currentFile = currentFile;
        this.currentDir = currentFile.resolve(".");
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
        if (rootClass == null) {
            rootClass = new DitaClass(classValue);
        }
        final String formatValue = atts.getValue(ATTRIBUTE_NAME_FORMAT);
        // when meets topic tag
        if (TOPIC_TOPIC.matches(classValue)) {
            topicId = atts.getValue(ATTRIBUTE_NAME_ID);
            // relpace place holder with first topic id
            final String filename = currentFile.toString() + QUESTION;
            for (final ExportAnchor e: exportAnchors) {
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
        } else if (MAP_TOPICMETA.matches(classValue) || TOPIC_PROLOG.matches(classValue)) {
            // merge multiple exportanchors into one
            // Each <topicref> can only have one <topicmeta>.
            // Each <topic> can only have one <prolog>
            // and <metadata> can have more than one exportanchors
            topicMetaSet.add(qName);
        } else if (DELAY_D_EXPORTANCHORS.matches(classValue)) {
            hasExport = true;
            // If current file is a ditamap file
            if (rootClass != null && rootClass.matches(MAP_MAP)) {
                // if dita file's extension name is ".xml"
                currentExportAnchor = new ExportAnchor(topicHref);
                // if <exportanchors> is defined in topicmeta(topicref), there is only one topic id
                currentExportAnchor.topicids.add(topicId);
            // If current file is topic file
            } else if (rootClass == null || rootClass.matches(TOPIC_TOPIC)) {
                currentExportAnchor = new ExportAnchor(currentFile);
                // if <exportanchors> is defined in metadata(topic), there can be many topic ids
                currentExportAnchor.topicids.add(topicId);
                shouldAppendEndTag = true;
            }
        } else if (DELAY_D_ANCHORKEY.matches(classValue)) {
            // create keyref element in the StringBuilder
            // TODO in topic file is no keys
            final String keyref = atts.getValue(ATTRIBUTE_NAME_KEYREF);
            currentExportAnchor.keys.add(keyref);
        } else if (DELAY_D_ANCHORID.matches(classValue)) {
            // create keyref element in the StringBuilder
            final String id = atts.getValue(ATTRIBUTE_NAME_ID);
            // If current file is a ditamap file
            // The id can only be element id within a topic
            if (rootClass != null && rootClass.matches(MAP_MAP)) {
                // id shouldn't be same as topic id in the case of duplicate insert
                if (!topicId.equals(id)) {
                    currentExportAnchor.ids.add(id);
                }
            } else if (rootClass == null || rootClass.matches(TOPIC_TOPIC)) {
                // id shouldn't be same as topic id in the case of duplicate insert
                if (!topicId.equals(id)) {
                    // topic id found
                    currentExportAnchor.ids.add(id);
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

        // external resource is filtered here.
        final String attrScope = atts.getValue(ATTRIBUTE_NAME_SCOPE);
        if (ATTR_SCOPE_VALUE_EXTERNAL.equals(attrScope) || ATTR_SCOPE_VALUE_PEER.equals(attrScope)
                || attrValue.toString().contains(COLON_DOUBLE_SLASH) || attrValue.toString().startsWith(SHARP)) {
            return;
        }
        // For only format of the href is dita topic
        String attrFormat = atts.getValue(ATTRIBUTE_NAME_FORMAT);
        if (attrFormat == null || ATTR_FORMAT_VALUE_DITA.equals(attrFormat)) {
            final File target = toFile(attrValue);
            if (target.isAbsolute()) {
                topicHref = attrValue;
            } else {
                topicHref = currentDir.resolve(attrValue);
            }

            // attrValue has topicId
            if (attrValue.getFragment() != null) {
                topicId = attrValue.getFragment();
            } else {
                // get the first topicId(vaild href file)
                if (attrFormat == null || attrFormat.equals(ATTR_FORMAT_VALUE_DITA) || attrFormat.equals(ATTR_FORMAT_VALUE_DITAMAP)) {
                    topicId = topicHref + QUESTION;
                }
            }   
        } else {
            topicHref = null;
            topicId = null;
        }
    }

	@Override
	public void endElement(final String uri, final String localName, final String qName)
			throws SAXException {
        // <exportanchors> over should write </file> tag
        if (topicMetaSet.contains(qName) && hasExport) {
            // If current file is a ditamap file
            if (rootClass != null && rootClass.matches(MAP_MAP)) {
                exportAnchors.add(currentExportAnchor);
                currentExportAnchor = null;
                // If current file is topic file
            }
            hasExport = false;
            topicMetaSet.clear();
        }
	    
	    getContentHandler().endElement(uri, localName, qName);
	}
	
    /**
     * Clean up.
     */
    @Override
    public void endDocument() throws SAXException {
        if ((rootClass == null || rootClass.matches(TOPIC_TOPIC)) && shouldAppendEndTag) {
            exportAnchors.add(currentExportAnchor);
            currentExportAnchor = null;
            // should reset
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
