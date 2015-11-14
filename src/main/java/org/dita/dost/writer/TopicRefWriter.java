/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2007 All Rights Reserved.
 */
package org.dita.dost.writer;

import static org.dita.dost.util.Constants.*;
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
    private File currentFileDir = null;
    private File currentFilePath = null;
    /** Using for rectify relative path of xml */
    private String fixpath = null;

    @Override
    public void write(final File outputFilename) throws DITAOTException {
        currentFilePath = outputFilename.getAbsoluteFile();
        currentFileDir = outputFilename.getParentFile();
        super.write(outputFilename);
    }

    /**
     * Set up class.
     * 
     * @param conflictTable conflictTable
     */
    public void setup(final Map<String, String> conflictTable) {
        for (final Map.Entry<String, String> e: changeTable.entrySet()) {
            assert new File(e.getKey()).isAbsolute();
            assert new File(e.getValue()).isAbsolute();
        }
        this.conflictTable = conflictTable;
    }

    public void setChangeTable(final Map<String, String> changeTable) {
        for (final Map.Entry<String, String> e: changeTable.entrySet()) {
            assert new File(e.getKey()).isAbsolute();
            assert new File(e.getValue()).isAbsolute();
        }
        this.changeTable = changeTable;
    }

    public void setFixpath(final String fixpath) {
        assert fixpath != null ? new File(fixpath).isAbsolute() : true;
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

        if (TOPIC_OBJECT.matches(atts)) {
            final String data = atts.getValue(ATTRIBUTE_NAME_DATA);
            if (data != null) {
                final AttributesImpl res = new AttributesImpl(atts);
                addOrSetAttribute(res, ATTRIBUTE_NAME_DATA, updateData(data));
                as = res;
            }
        } else {
            final String href = atts.getValue(ATTRIBUTE_NAME_HREF);
            if (href != null) {
                final AttributesImpl res = new AttributesImpl(atts);
                addOrSetAttribute(res, ATTRIBUTE_NAME_HREF, updateHref(as));
                as = res;
            }
        }

        getContentHandler().startElement(uri, localName, qName, as);
    }

    /**
     * Check whether the attributes contains references
     * 
     * @param atts element attributes
     * @return {@code true} if local DITA reference, otherwise {@code false}
     */
    private boolean isLocalDita(final Attributes atts) {
        final String classValue = atts.getValue(ATTRIBUTE_NAME_CLASS);
        if (classValue == null
                || (TOPIC_IMAGE.matches(classValue))) {
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

    private String updateData(final String origValue) {
        String hrefValue = origValue;
        if (fixpath != null && hrefValue.startsWith(fixpath)) {
            hrefValue = hrefValue.substring(fixpath.length());
        }
        return hrefValue;
    }

    private String updateHref(final Attributes atts) {
        String hrefValue = atts.getValue(ATTRIBUTE_NAME_HREF);
        if (hrefValue == null) {
            return null;
        }
        if (fixpath != null && hrefValue.startsWith(fixpath)) {
            hrefValue = hrefValue.substring(fixpath.length());
        }

        if (changeTable == null || changeTable.isEmpty()) {
            return hrefValue;
        }

        if (isLocalDita(atts)) {
            // replace the href value if it's referenced topic is extracted.
            final File rootPathName = currentFilePath;
            String changeTargetkey = resolve(currentFileDir, hrefValue).getPath();
            String changeTarget = changeTable.get(changeTargetkey);

            final String topicID = getTopicID(hrefValue);
            if (topicID != null) {
                changeTargetkey = setFragment(changeTargetkey, topicID);
                final String changeTarget_with_elemt = changeTable.get(changeTargetkey);
                if (changeTarget_with_elemt != null) {
                    changeTarget = changeTarget_with_elemt;
                }
            }

            final String elementID = getElementID(hrefValue);
            final String pathtoElem = getFragment(hrefValue, "");

            if (changeTarget == null || changeTarget.isEmpty()) {
                String absolutePath = resolveTopic(currentFileDir, hrefValue);
                absolutePath = setElementID(absolutePath, null);
                changeTarget = changeTable.get(absolutePath);
            }

            if (changeTarget == null) {
                return hrefValue;// no change
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
        return hrefValue;
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
