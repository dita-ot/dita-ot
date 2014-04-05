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

import org.dita.dost.util.FileUtils;
import org.dita.dost.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Export anchor collector filter.
 */
public final class ExportAnchorsFilter extends AbstractXMLFilter {

    /** Basedir of the current parsing file */
    private File currentDir = null;
    /** Topicmeta set for merge multiple exportanchors into one. Each topicmeta/prolog can define many exportanchors */
    private final Set<String> topicMetaSet = new HashSet<String>(16);
    /** Flag to show whether a file has <exportanchors> tag */
    private boolean hasExport = false;
    /** Absolute system path to file being processed */
    private URI currentFile = null;
    private final List<ExportAnchor> exportAnchors = new ArrayList<ExportAnchor>();
    private ExportAnchor currentExportAnchor;
    /** Refered topic id */
    private String topicId = "";
    private URI rootFilePath = null;
    /** Map to store plugin id */
    private final Map<String, Set<String>> pluginMap = new HashMap<String, Set<String>>();
    /** Store the href of topicref tag */
    private URI topicHref;
    /** For topic/dita files whether a </file> tag should be added */
    private boolean shouldAppendEndTag = false;
    
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
    }
    
    /**
     * Set the relative directory of current file.
     * 
     * @param dir dir
     */
    public void setCurrentDir(final File dir) {
        currentDir = dir;
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
	public void startElement(final String uri, final String localName, final String qName, final Attributes atts)
			throws SAXException {
	    final String classValue = atts.getValue(ATTRIBUTE_NAME_CLASS);
        // when meets topic tag
        if (TOPIC_TOPIC.matches(classValue)) {
            topicId = atts.getValue(ATTRIBUTE_NAME_ID);
            // relpace place holder with first topic id
            // Get relative file name
            final String filename = FileUtils.getRelativeUnixPath(rootFilePath.toString(), currentFile.toString());
            for (final ExportAnchor e: exportAnchors) {
                if (e.topicids.contains(filename + QUESTION)) {
                    e.topicids.add(topicId);
                    e.topicids.remove(filename + QUESTION);
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
            if (FileUtils.isDITAMapFile(currentFile.getPath())) {
                // if dita file's extension name is ".xml"
                currentExportAnchor = new ExportAnchor(topicHref);
                // if <exportanchors> is defined in topicmeta(topicref), there is only one topic id
                currentExportAnchor.topicids.add(topicId);
            // If current file is topic file
            } else if (FileUtils.isDITATopicFile(currentFile.getPath())) {
                URI filename = getRelativePath(rootFilePath, currentFile);
                currentExportAnchor = new ExportAnchor(filename);
                // if <exportanchors> is defined in metadata(topic), there can be many topic ids
                currentExportAnchor.topicids.add(topicId);
                shouldAppendEndTag = true;
            }
        } else if (DELAY_D_ANCHORKEY.matches(classValue)) {
            // create keyref element in the StringBuffer
            // TODO in topic file is no keys
            final String keyref = atts.getValue(ATTRIBUTE_NAME_KEYREF);
            currentExportAnchor.keys.add(keyref);
        } else if (DELAY_D_ANCHORID.matches(classValue)) {
            // create keyref element in the StringBuffer
            final String id = atts.getValue(ATTRIBUTE_NAME_ID);
            // If current file is a ditamap file
            // The id can only be element id within a topic
            if (FileUtils.isDITAMapFile(currentFile.getPath())) {
                // id shouldn't be same as topic id in the case of duplicate insert
                if (!topicId.equals(id)) {
                    currentExportAnchor.ids.add(id);
                }
            } else if (FileUtils.isDITATopicFile(currentFile.getPath())) {
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
        String attrValue = atts.getValue(ATTRIBUTE_NAME_HREF);
        if (attrValue == null) {
            return;
        }

        // external resource is filtered here.
        final String attrScope = atts.getValue(ATTRIBUTE_NAME_SCOPE);
        if (ATTR_SCOPE_VALUE_EXTERNAL.equals(attrScope) || ATTR_SCOPE_VALUE_PEER.equals(attrScope)
                || attrValue.contains(COLON_DOUBLE_SLASH) || attrValue.startsWith(SHARP)) {
            return;
        }
        // For only format of the href is dita topic
        String attrFormat = atts.getValue(ATTRIBUTE_NAME_FORMAT);
        if (attrFormat == null || ATTR_FORMAT_VALUE_DITA.equalsIgnoreCase(attrFormat)) {
            if (attrValue.startsWith("file:/") && !attrValue.contains("file://")) {
                attrValue = attrValue.substring("file:/".length());
                // Unix like OS
                if (UNIX_SEPARATOR.equals(File.separator)) {
                    attrValue = UNIX_SEPARATOR + attrValue;
                }
            } else if (attrValue.startsWith("file:") && !attrValue.startsWith("file:/")) {
                attrValue = attrValue.substring("file:".length());
            }
            String filename = null;
            final File target = new File(attrValue);
            if (target.isAbsolute()) {
                attrValue = FileUtils.getRelativeUnixPath(rootFilePath.getPath(), attrValue);
            } else {
                filename = FileUtils.resolve(currentDir, attrValue).getPath();
            }
    
            filename = toFile(filename).getPath();
            // XXX: At this point, filename should be a system path
        
            topicHref = toURI(filename);
            // attrValue has topicId
            if (attrValue.lastIndexOf(SHARP) != -1) {
                // get the topicId position
                final int position = attrValue.lastIndexOf(SHARP);
                topicId = attrValue.substring(position + 1);
            } else {
                // get the first topicId(vaild href file)
                if (FileUtils.isDITAFile(topicHref.getPath())) {
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
            if (FileUtils.isDITAMapFile(currentFile.getPath())) {
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
        if (FileUtils.isDITATopicFile(currentFile.getPath()) && shouldAppendEndTag) {
            exportAnchors.add(currentExportAnchor);
            currentExportAnchor = null;
            // should reset
            shouldAppendEndTag = false;
        }
        
        getContentHandler().endDocument();
    }

    public static class ExportAnchor {
        public final URI file;
        public final Set<String> topicids = new HashSet<String>();
        public final Set<String> keys = new HashSet<String>();
        public final Set<String> ids = new HashSet<String>();

        public ExportAnchor(final URI file) {
            this.file = file;
        }
    }
    
}
