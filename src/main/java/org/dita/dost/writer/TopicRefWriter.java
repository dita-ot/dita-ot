/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2007 All Rights Reserved.
 */
package org.dita.dost.writer;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.writer.DitaWriter.*;
import static org.dita.dost.util.FileUtils.*;
import static org.dita.dost.util.XMLUtils.*;

import java.io.File;
import java.util.Map;

import org.dita.dost.exception.DITAOTException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * TopicRefWriter which updates the linking elements' value according to the
 * mapping table.
 */
public final class TopicRefWriter extends AbstractXMLFilter {

    private Map<String, String> changeTable = null;
    private Map<String, String> conflictTable = null;
    private File currentFilePath = null;
    private File currentFilePathName = null;
    /** Using for rectify relative path of xml */
    private String fixpath = null;

    @Override
    public void write(final File outputFilename) throws DITAOTException {
        currentFilePathName = outputFilename.getAbsoluteFile();
        currentFilePath = outputFilename.getParentFile();
        super.write(outputFilename);
    }

    /**
     * Set up class.
     * 
     * @param conflictTable conflictTable
     */
    public void setup(final Map<String, String> conflictTable) {
        this.conflictTable = conflictTable;
    }

    public void setChangeTable(final Map<String, String> changeTable) {
        this.changeTable = changeTable;
    }

    public void setFixpath(final String fixpath) {
        this.fixpath = fixpath;
    }

    @Override
    public void processingInstruction(final String target, String data) throws SAXException {
        if (fixpath != null && target.equals(PI_WORKDIR_TARGET)) {
            final String tmp = fixpath.substring(0, fixpath.lastIndexOf(SLASH));
            if (!data.endsWith(tmp)) {
                data = data + File.separator + tmp;
            }
        } else if (fixpath != null && target.equals(PI_WORKDIR_TARGET_URI)) {
            final String tmp = fixpath.substring(0, fixpath.lastIndexOf(URI_SEPARATOR) + 1);
            if (!data.endsWith(tmp)) {
                data = data + tmp;
            }
        }
        getContentHandler().processingInstruction(target, data);
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes atts)
            throws SAXException {
        Attributes as = atts;
        final String href = atts.getValue(ATTRIBUTE_NAME_HREF);
        if (href != null) {
            final AttributesImpl res = new AttributesImpl(atts);
            addOrSetAttribute(res, ATTRIBUTE_NAME_HREF, updateHref(as));
            as = res;
        }
        getContentHandler().startElement(uri, localName, qName, as);
    }

    /**
     * Check whether the attributes contains references
     * 
     * @param atts
     * @return true/false
     */
    private boolean isLocalDita(final Attributes atts) {
        final String classValue = atts.getValue(ATTRIBUTE_NAME_CLASS);
        if (classValue == null
                || (!TOPIC_XREF.matches(classValue) && !TOPIC_LINK.matches(classValue) && !MAP_TOPICREF.matches(classValue))) {
            return false;
        }

        String scopeValue = atts.getValue(ATTRIBUTE_NAME_SCOPE);
        if (scopeValue == null) {
            scopeValue = ATTR_SCOPE_VALUE_LOCAL;
        }
        String formatValue = atts.getValue(ATTRIBUTE_NAME_FORMAT);
        if (formatValue == null) {
            formatValue = ATTR_FORMAT_VALUE_DITA;
        }

        return scopeValue.equals(ATTR_SCOPE_VALUE_LOCAL) && formatValue.equals(ATTR_FORMAT_VALUE_DITA);

    }

    private String updateHref(final Attributes atts) {
        String attValue = atts.getValue(ATTRIBUTE_NAME_HREF);
        if (attValue == null) {
            return null;
        }
        if (fixpath != null && attValue.startsWith(fixpath)) {
            attValue = attValue.substring(fixpath.length());
        }

        if (changeTable == null || changeTable.isEmpty()) {
            return attValue;
        }

        if (isLocalDita(atts)) {
            // replace the href value if it's referenced topic is extracted.
            final File rootPathName = currentFilePathName;
            String changeTargetkey = resolve(currentFilePath, attValue).getPath();
            String changeTarget = changeTable.get(changeTargetkey);

            final String topicID = getTopicID(attValue);
            if (topicID != null) {
                changeTargetkey = setFragment(changeTargetkey, topicID);
                final String changeTarget_with_elemt = changeTable.get(changeTargetkey);
                if (changeTarget_with_elemt != null) {
                    changeTarget = changeTarget_with_elemt;
                }
            }

            final String elementID = getElementID(attValue);
            final String pathtoElem = getFragment(attValue, "");

            if (changeTarget == null || changeTarget.isEmpty()) {
                String absolutePath = resolveTopic(currentFilePath, attValue);
                absolutePath = setElementID(absolutePath, null);
                changeTarget = changeTable.get(absolutePath);
            }

            if (changeTarget == null) {
                return attValue;// no change
            } else {
                final String conTarget = conflictTable.get(stripFragment(changeTarget));
                if (conTarget != null && !conTarget.isEmpty()) {
                    final String p = getRelativeUnixPath(rootPathName, conTarget);
                    if (elementID == null) {
                        return setFragment(p, getElementID(changeTarget));
                    } else {
                        if (getFragment(conTarget) != null) {
                            if (!pathtoElem.contains(SLASH)) {
                                return p;
                            } else {
                                return setElementID(p, elementID);
                            }

                        } else {
                            return setFragment(p, pathtoElem);
                        }
                    }
                } else {
                    final String p = getRelativeUnixPath(rootPathName, changeTarget);
                    if (elementID == null) {
                        return p;
                    } else {
                        if (getFragment(changeTarget) != null) {
                            if (!pathtoElem.contains(SLASH)) {
                                return p;
                            } else {
                                return setElementID(p, elementID);
                            }
                        } else {
                            return setFragment(p, pathtoElem);
                        }
                    }
                }
            }
        }
        return attValue;
    }

    /**
     * Retrieve the element ID from the path. If there is no element ID, return topic ID.
     *
     * @param relativePath
     * @return String
     */
    private String getElementID(final String relativePath) {
        String elementID = null;
        String topicWithelement = null;
        final String fragment = getFragment(relativePath);
        if (fragment != null) {
            topicWithelement = getFragment(relativePath);
            if (topicWithelement.lastIndexOf(SLASH) != -1) {
                elementID = topicWithelement.substring(topicWithelement.lastIndexOf(SLASH) + 1);
            } else {
                elementID = topicWithelement;
            }
        }
        return elementID;
    }

}
