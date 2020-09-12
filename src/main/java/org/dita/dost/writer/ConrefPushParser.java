/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2010 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.writer;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.util.DitaClass;
import org.dita.dost.util.Job.FileInfo;
import org.w3c.dom.*;

import java.io.File;
import java.net.URI;
import java.util.Map;

import static org.apache.commons.io.FilenameUtils.normalize;
import static org.dita.dost.reader.ConrefPushReader.MoveKey;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.toURI;
import static org.dita.dost.util.XMLUtils.*;

/**
 * This class is for writing conref push contents into
 * specific files.
 */
public final class ConrefPushParser extends AbstractDomFilter {

    private Map<MoveKey, DocumentFragment> movetable;
    private boolean hasConref = false;
    private boolean hasKeyref = false;
    private File tempDir;

    public void setMoveTable(final Map<MoveKey, DocumentFragment> movetable) {
        this.movetable = movetable;
    }

    public void setTempDir(final File tempDir) {
        this.tempDir = tempDir;
    }

    @Override
    public void read(final File filename) throws DITAOTException {
        hasConref = false;
        hasKeyref = false;

        super.read(filename);

        for (final MoveKey key : movetable.keySet()) {
            logger.warn(MessageUtils.getMessage("DOTJ043W", key.idPath, filename.getPath()).toString());
        }
        if (hasConref || hasKeyref) {
            updateList(filename);
        }
    }

    @Override
    protected Document process(final Document doc) {
        walk(doc.getDocumentElement(), null);
        return doc;
    }

    /**
     * Update conref list in job configuration and in conref list file.
     *
     * @param filename filename
     */
    private void updateList(final File filename) {
        try {
            final URI relativePath = toURI(filename.getAbsolutePath().substring(new File(normalize(tempDir.toString())).getPath().length() + 1));
            final FileInfo f = job.getOrCreateFileInfo(relativePath);
            if (hasConref) {
                f.hasConref = true;
            }
            if (hasKeyref) {
                f.hasKeyref = true;
            }
            job.write();
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * The function is to judge if the pushed content type march the type of content being pushed/replaced
     *
     * @param targetClassAttribute the class attribute of target element which is being pushed
     * @param content              pushedContent
     * @return boolean: if type match, return true, else return false
     */
    private boolean isPushedTypeMatch(final DitaClass targetClassAttribute, final DocumentFragment content) {
        if (content.hasChildNodes()) {
            final NodeList nodeList = content.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                final Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    final Element elem = (Element) node;
                    final DitaClass cls = DitaClass.getInstance(elem);
                    if (cls != null) {
                        return targetClassAttribute.matches(cls);
                    }
                }
            }
        }
        return false;
    }

    private DocumentFragment replaceElementName(final DitaClass targetClassAttribute, final DocumentFragment content) {
        if (content.hasChildNodes()) {
            final NodeList nodeList = content.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                final Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    final Element elem = (Element) node;
                    final DitaClass cls = DitaClass.getInstance(elem);
                    // get type of the target element
                    final String type = targetClassAttribute.toString().substring(1, targetClassAttribute.toString().indexOf("/")).trim();
                    if (!cls.equals(targetClassAttribute) && targetClassAttribute.matches(cls)) {
                        // Specializing the pushing content is not handled here
                        // but we can catch such a situation to emit a warning by comparing the class values.
                        final String targetElementName = targetClassAttribute.toString().substring(targetClassAttribute.toString().indexOf("/") + 1).trim();
                        if (elem.getAttributeNode(ATTRIBUTE_NAME_CONREF) != null) {
                            hasConref = true;
                        }
                        if (elem.getAttributeNode(ATTRIBUTE_NAME_KEYREF) != null) {
                            hasKeyref = true;
                        }
                        elem.getOwnerDocument().renameNode(elem, elem.getNamespaceURI(), targetElementName);
                        // process the child nodes of the current node
                        final NodeList nList = elem.getChildNodes();
                        for (int j = 0; j < nList.getLength(); j++) {
                            final Node subNode = nList.item(j);
                            if (subNode.getNodeType() == Node.ELEMENT_NODE) {
                                //replace the subElement Name
                                replaceSubElementName(type, (Element) subNode);
                            }
                        }
                    } else {
                        replaceSubElementName(STRING_BLANK, elem);
                    }
                }
            }
        }
        return content;
    }

    private void replaceSubElementName(final String type, final Element elem) {
        if (elem.getAttributeNode(ATTRIBUTE_NAME_CONREF) != null) {
            hasConref = true;
        }
        if (elem.getAttributeNode(ATTRIBUTE_NAME_KEYREF) != null) {
            hasKeyref = true;
        }
        String generalizedElemName = elem.getNodeName();
        final DitaClass cls = DitaClass.getInstance(elem);
        if (cls != null) {
            if (cls.toString().contains(type) && !type.equals(STRING_BLANK)) {
                final int index = cls.toString().indexOf("/");
                generalizedElemName = cls.toString().substring(index + 1, cls.toString().indexOf(STRING_BLANK, index)).trim();
            }
        }
        elem.getOwnerDocument().renameNode(elem, elem.getNamespaceURI(), generalizedElemName);
        final NodeList nodeList = elem.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            final Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                replaceSubElementName(type, (Element) node);
            }
        }
    }

    private MoveKey hasAction(final DitaClass classValue, final String idPath, final String defaultIdPath,
                              final String action) {
        if (movetable.containsKey(new MoveKey(idPath, action))) {
            final MoveKey containkey = new MoveKey(idPath, action);
            if (isPushedTypeMatch(classValue, movetable.get(containkey))) {
                return containkey;
            }
        } else if (movetable.containsKey(new MoveKey(defaultIdPath, action))) {
            final MoveKey containkey = new MoveKey(defaultIdPath, action);
            if (isPushedTypeMatch(classValue, movetable.get(containkey))) {
                return containkey;
            }
        }
        return null;
    }

    private void walk(final Element elem, final String parentTopicId) {
        String topicId = parentTopicId;
        final String idValue = elem.getAttribute(ATTRIBUTE_NAME_ID);
        if (idValue != null) {
            final DitaClass cls = DitaClass.getInstance(elem);
            if (TOPIC_TOPIC.matches(cls)) {
                topicId = idValue;
            } else {
                final String idPath;
                if (MAP_TOPICREF.matches(cls) || MAP_MAP.matches(cls)) {
                    idPath = SHARP + idValue;
                } else {
                    idPath = SHARP + topicId + SLASH + idValue;
                }
                final String defaultidPath = SHARP + idValue;

                final MoveKey pushBefore = hasAction(cls, idPath, defaultidPath, ATTR_CONACTION_VALUE_PUSHBEFORE);
                if (pushBefore != null) {
                    final DocumentFragment fragment = movetable.remove(pushBefore);
                    insertBefore(elem, replaceElementName(cls, fragment));
                }

                final MoveKey pushAfter = hasAction(cls, idPath, defaultidPath, ATTR_CONACTION_VALUE_PUSHAFTER);
                if (pushAfter != null) {
                    final DocumentFragment fragment = movetable.remove(pushAfter);
                    insertAfter(elem, replaceElementName(cls, fragment));
                }

                final MoveKey pushReplace = hasAction(cls, idPath, defaultidPath, ATTR_CONACTION_VALUE_PUSHREPLACE);
                if (pushReplace != null) {
                    final DocumentFragment fragment = movetable.remove(pushReplace);
                    insertBefore(elem, replaceElementName(cls, fragment));
                    elem.getParentNode().removeChild(elem);
                    return;
                }
            }
        }
        for (final Element child : getChildElements(elem)) {
            walk(child, topicId);
        }
    }
}
