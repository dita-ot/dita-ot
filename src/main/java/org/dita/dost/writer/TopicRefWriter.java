/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2007 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.writer;

import org.dita.dost.exception.DITAOTException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import java.io.File;
import java.net.URI;
import java.util.Map;

import static org.dita.dost.chunk.ChunkModule.isLocalScope;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.FileUtils.getFragment;
import static org.dita.dost.util.URLUtils.*;
import static org.dita.dost.util.XMLUtils.addOrSetAttribute;

/**
 * TopicRefWriter which updates the linking elements' value according to the
 * mapping table.
 */
public final class TopicRefWriter extends AbstractXMLFilter {

    private Map<URI, URI> changeTable = null;
    private Map<URI, URI> conflictTable = null;
    /** Using for rectify relative path of xml */
    private String fixpath = null;

    @Override
    public void write(final File outputFilename) throws DITAOTException {
        setCurrentFile(outputFilename.toURI());
        logger.info("Process " + outputFilename.toURI());
        super.write(outputFilename);
    }

    /**
     * Set up class.
     *
     * @param conflictTable conflictTable
     */
    public void setup(final Map<URI, URI> conflictTable) {
        for (final Map.Entry<URI, URI> e: conflictTable.entrySet()) {
            assert e.getKey().isAbsolute();
            assert e.getValue().isAbsolute();
        }
        this.conflictTable = conflictTable;
    }

    public void setChangeTable(final Map<URI, URI> changeTable) {
        assert changeTable != null && !changeTable.isEmpty();
        for (final Map.Entry<URI, URI> e: changeTable.entrySet()) {
            assert e.getKey().isAbsolute();
            assert e.getValue().isAbsolute();
        }
        this.changeTable = changeTable;
    }

    public void setFixpath(final String fixpath) {
        assert fixpath == null || !(toFile(fixpath).isAbsolute());
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
        String formatValue = atts.getValue(ATTRIBUTE_NAME_FORMAT);
        if (formatValue == null) {
            formatValue = ATTR_FORMAT_VALUE_DITA;
        }

        return isLocalScope(scopeValue) && formatValue.equals(ATTR_FORMAT_VALUE_DITA);

    }

    private String updateData(final String origValue) {
        String hrefValue = origValue;
        if (fixpath != null && hrefValue.startsWith(fixpath)) {
            hrefValue = hrefValue.substring(fixpath.length());
        }
        return hrefValue;
    }

    private String updateHref(final Attributes atts) {
        // FIXME should be URI
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
            final URI rootPathName = currentFile;
            URI target = currentFile.resolve(hrefValue);
            URI changeTargetkey = stripFragment(target);
            URI changeTarget = changeTable.get(changeTargetkey);

            final String topicID = getTopicID(toURI(hrefValue));
            if (topicID != null) {
                changeTargetkey = setFragment(changeTargetkey, topicID);
                final URI changeTarget_with_elemt = changeTable.get(changeTargetkey);
                if (changeTarget_with_elemt != null) {
                    changeTarget = changeTarget_with_elemt;
                }
            }

            final String elementID = getElementID(hrefValue);
            final String pathtoElem = getFragment(hrefValue, "");

            if (changeTarget == null || changeTarget.toString().isEmpty()) {
                URI absolutePath = currentFile.resolve(hrefValue);
                absolutePath = setElementID(absolutePath, null);
                changeTarget = changeTable.get(absolutePath);
            }

            if (changeTarget == null) {
                return hrefValue;// no change
            } else {
                final URI conTarget = conflictTable.get(stripFragment(changeTarget));
                logger.debug("Update " + changeTarget + " to " + conTarget);
                if (conTarget != null && !conTarget.toString().isEmpty()) {
                    final URI p = getRelativePath(rootPathName, conTarget);
                    if (elementID == null) {
                        return setFragment(p, getElementID(changeTarget.toString())).toString();
                    } else {
                        if (conTarget.getFragment() != null) {
                            if (!pathtoElem.contains(SLASH)) {
                                return p.toString();
                            } else {
                                return setElementID(p, elementID).toString();
                            }

                        } else {
                            return setFragment(p, pathtoElem).toString();
                        }
                    }
                } else {
                    final URI p = getRelativePath(rootPathName, changeTarget);
                    if (elementID == null) {
                        return p.toString();
                    } else {
                        if (changeTarget.getFragment() != null) {
                            if (!pathtoElem.contains(SLASH)) {
                                return p.toString();
                            } else {
                                return setElementID(p, elementID).toString();
                            }
                        } else {
                            return setFragment(p, pathtoElem).toString();
                        }
                    }
                }
            }
        }
        return hrefValue;
    }

    /**
     * Retrieve the element ID from the path. If there is no element ID, return topic ID.
     */
    private String getElementID(final String relativePath) {
        final String fragment = getFragment(relativePath);
        if (fragment != null) {
            if (fragment.lastIndexOf(SLASH) != -1) {
                return fragment.substring(fragment.lastIndexOf(SLASH) + 1);
            } else {
                return fragment;
            }
        }
        return null;
    }

}
