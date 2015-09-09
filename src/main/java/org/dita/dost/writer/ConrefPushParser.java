/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.writer;

import static org.apache.commons.io.FilenameUtils.*;
import static javax.xml.XMLConstants.*;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.reader.ConrefPushReader.*;
import static org.dita.dost.util.URLUtils.*;

import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.util.XMLUtils.*;

import java.io.File;
import java.net.URI;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.Stack;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.util.DitaClass;
import org.w3c.dom.Attr;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * This class is for writing conref push contents into
 * specific files.
 */
public final class ConrefPushParser extends AbstractXMLFilter {

    /**table containing conref push contents.*/
    private Hashtable<MoveKey, DocumentFragment> movetable = null;

    /**topicId keep the current topic id value.*/
    private String topicId = null;

    /**idStack keeps the history of topicId because topics can be nested.*/
    private Stack<String> idStack = null;

    /**topicSpecSet is used to store all kinds of names for elements which is
	specialized from <topic>. It is useful in endElement(...) because we don't
	know the value of class attribute of the element when processing its end
	tag. That's why we need to store the element's name to the set when we first
	met it in startElement(...).*/
    private Set<String> topicSpecSet = null;

    /**boolean isReplaced show whether current content is replace
	because of "pushreplace" action in conref push. If the current
	content is replaced, the output will neglect it until isReplaced
	is turned off*/
    private boolean isReplaced = false;

    /**int level is used the count the level number to the element which
	is the starting point that is neglected because of "pushreplace" action
	The initial value of level is 0. It will add one if element level
	increases in startElement(....) and minus one if level decreases in
	endElement(...). When it turns out to be 0 again, boolean isReplaced
	needs to be turn off.*/
    private int level = 0;

    /**boolean hasPushafter show whether there is something we need to write
	after the current element. If so the counter levelForPushAfter should
	count the levels to make sure we insert the push content after the right
	end tag.*/
    private boolean hasPushafter = false;

    /**int levelForPushAfter is used to count the levels to the element which
	is the starting point for "pushafter" action. It will add one in startElement(...)
	and minus one in endElement(...). When it turns out to be 0 again, we
	should append the push content right after the current end tag.*/
    private int levelForPushAfter = 0;

    /**levelForPushAfterStack is used to store the history value of levelForPushAfter
	It is possible that we have pushafter action for both parent and child element.
	In this case, we need to push the parent's value of levelForPushAfter to Stack
	before initializing levelForPushAfter for child element. When we finished
	pushafter action for child element, we need to restore the original value for
	parent. As to "pushreplace" action, we don't need this because if we replaced the
	parent, the replacement of child is meaningless.*/
    private Stack<Integer> levelForPushAfterStack = null;

    /**contentForPushAfter is used to store the content that will push after the end
	tag of the element when levelForPushAfter is decreased to zero. This is useful
	to "pushafter" action because we don't know the value of id when processing the
	end tag of an element. That's why we need to store the content for push after
	into variable in startElement(...)*/
    private DocumentFragment contentForPushAfter = null;

    /**contentForPushAfterStack is used to store the history value of contentForPushAfter
	It is possible that we have pushafter action for both parent and child element.
	In this case, we need to push the parent's value of contentForPushAfter to Stack
	before getting value contentForPushAfter for child element from movetable. When we
	finished pushafter action for child element, we need to restore the original value for
	parent. */
    private Stack<DocumentFragment> contentForPushAfterStack = null;

    /**if the pushcontent has @conref, it should be paid attention to it. Because the current
	file may not contain any @conref attribute, it will not resolved by the conref.xsl,
	while it may contain @conref after pushing. So the dita.list file should be updated, if
	the pushcontent has @conref.*/
    private boolean hasConref = false;
    private boolean hasKeyref = false;
    /**tempDir.*/
    private File tempDir;
    
    /**
     * Constructor.
     */
    public ConrefPushParser() {
        topicSpecSet = new HashSet<>();
        levelForPushAfterStack = new Stack<>();
        contentForPushAfterStack = new Stack<>();
    }

    public void setMoveTable(final Hashtable<MoveKey, DocumentFragment> movetable) {
        this.movetable = movetable;
    }
    
    /**
     * 
     * @param tempDir tempDir
     */
    public void setTempDir(final File tempDir) {
        this.tempDir = tempDir;
    }
    /**
     * @param filename filename
     * @throws DITAOTException exception
     */
    @Override
    public void write(final File filename) throws DITAOTException {
        hasConref = false;
        hasKeyref = false;
        isReplaced = false;
        hasPushafter = false;
        level = 0;
        levelForPushAfter = 0;
        idStack = new Stack<>();
        topicSpecSet = new HashSet<>();
        levelForPushAfterStack = new Stack<>();
        contentForPushAfterStack = new Stack<>();

        super.write(filename);

        for (final MoveKey key: movetable.keySet()) {
            logger.warn(MessageUtils.getInstance().getMessage("DOTJ043W", key.idPath, filename.getPath()).toString());
        }
        if (hasConref || hasKeyref) {
            updateList(filename);
        }
    }
    /**
     * Update conref list in job configuration and in conref list file.
     * 
     * @param filename filename
     */
    private void updateList(final File filename) {
        try {
            final URI reletivePath = toURI(filename.getAbsolutePath().substring(new File(normalize(tempDir.toString())).getPath().length() + 1));
            final FileInfo f = job.getOrCreateFileInfo(reletivePath);
            if (hasConref) {
                f.hasConref = true;
            }
            if (hasKeyref) {
                f.hasKeyref = true;
            }
            job.write();
        } catch (final Exception e) {
            logger.error(e.getMessage(), e) ;
        }
    }

    @Override
    public void characters(final char[] ch, final int start, final int length)
            throws SAXException {
        if (!isReplaced) {
            getContentHandler().characters(ch, start, length);
        }
    }

    @Override
    public void ignorableWhitespace(final char[] ch, final int start, final int length)
            throws SAXException {
        if (!isReplaced) {
            getContentHandler().ignorableWhitespace(ch, start, length);
        }
    }

    @Override
    public void processingInstruction(final String target, final String data)
            throws SAXException {
        if (!isReplaced) {
            getContentHandler().processingInstruction(target, data);
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String name)
            throws SAXException {

        if (isReplaced) {
            level--;
            if (level == 0) {
                isReplaced = false;
            }
        } else {
            getContentHandler().endElement(uri, localName, name);
        }

        if (hasPushafter) {
            levelForPushAfter--;
            if (levelForPushAfter == 0) {
                //write the pushcontent after the end tag
                try {
                    if (contentForPushAfter != null) {
                        writeNode(contentForPushAfter);
                    }
                } catch (final Exception e) {
                    logger.error(e.getMessage(), e) ;
                }
                if (!levelForPushAfterStack.isEmpty() &&
                        !contentForPushAfterStack.isEmpty()) {
                    levelForPushAfter = levelForPushAfterStack.pop();
                    contentForPushAfter = contentForPushAfterStack.pop();
                } else {
                    hasPushafter = false;
                    //empty the contentForPushAfter since it is write to output
                    contentForPushAfter = null;
                }
            }
        }
        if (!idStack.isEmpty() && topicSpecSet.contains(name)) {
            topicId = idStack.pop();
        }
    }

    /**
     * The function is to judge if the pushed content type march the type of content being pushed/replaced
     * @param targetClassAttribute the class attribute of target element which is being pushed
     * @param content pushedContent
     * @return boolean: if type match, return true, else return false
     */
    private boolean isPushedTypeMatch(final DitaClass targetClassAttribute, final DocumentFragment content) {
        DitaClass clazz = null;

        if (content.hasChildNodes()) {
            final NodeList nodeList = content.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                final Node node = nodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    final Element elem = (Element) node;
                    clazz = new DitaClass(elem.getAttribute(ATTRIBUTE_NAME_CLASS));
                    break;
                    // get type of the target element
                }
            }
        }

        return targetClassAttribute.matches(clazz);
    }

    /**
     * 
     * @param targetClassAttribute targetClassAttribute
     * @param content string
     * @return string
     */
    private DocumentFragment replaceElementName(final DitaClass targetClassAttribute, final DocumentFragment content) {        
        try {
            if (content.hasChildNodes()) {
                final NodeList nodeList = content.getChildNodes();
                for (int i = 0; i < nodeList.getLength(); i++) {
                    final Node node = nodeList.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE) {
                        final Element elem = (Element) node;
                        final DitaClass clazz = DitaClass.getInstance(elem);
                        // get type of the target element
                        final String type = targetClassAttribute.toString().substring(1, targetClassAttribute.toString().indexOf("/")).trim();
                        if (!clazz.equals(targetClassAttribute) && targetClassAttribute.matches(clazz)) {
                            // Specializing the pushing content is not handled here
                            // but we can catch such a situation to emit a warning by comparing the class values.
                            final String targetElementName = targetClassAttribute.toString().substring(targetClassAttribute.toString().indexOf("/") + 1 ).trim();
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
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return content;
    }
    
    /**
     * 
     * @param type pushtype
     * @param elem element
     * @return string
     */
    private void replaceSubElementName(final String type, final Element elem) {
        final DitaClass classValue = DitaClass.getInstance(elem);
        if (elem.getAttributeNode(ATTRIBUTE_NAME_CONREF) != null) {
            hasConref = true;
        }
        if (elem.getAttributeNode(ATTRIBUTE_NAME_KEYREF) != null) {
            hasKeyref = true;
        }
        String generalizedElemName = elem.getNodeName();
        if (classValue != null) {
            if (classValue.toString().contains(type) && !type.equals(STRING_BLANK)) {
                generalizedElemName = classValue.toString().substring(classValue.toString().indexOf("/") + 1, classValue.toString().indexOf(STRING_BLANK, classValue.toString().indexOf("/"))).trim();
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


    @Override
    public void startElement(final String uri, final String localName, final String name,
            final Attributes atts) throws SAXException {
        if (hasPushafter) {
            levelForPushAfter++;
        }
        if (isReplaced) {
            level++;
        } else {
            final String idValue = atts.getValue(ATTRIBUTE_NAME_ID);
            final DitaClass classValue = DitaClass.getInstance(atts);
            if (TOPIC_TOPIC.matches(classValue)) {
                if (!topicSpecSet.contains(name)) {
                    //add the element name to topicSpecSet if the element
                    //is a topic specialization. This is used when push and pop
                    //topic ids in a stack
                    topicSpecSet.add(name);
                }
                if (idValue != null) {
                    if (topicId != null) {
                        idStack.push(topicId);
                    }
                    topicId = idValue;
                }
            } else if (idValue != null) {
                String idPath = SHARP + topicId + SLASH + idValue;
                final String defaultidPath = SHARP + idValue;
                //enable conref push at map level
                if (MAP_TOPICREF.matches(classValue) || MAP_MAP.matches(classValue)) {
                    idPath = SHARP + idValue;
                    idStack.push(idValue);
                }
                handlePushBefore(classValue, idPath, defaultidPath);
                handlePushReplace(classValue, idPath, defaultidPath);
                handlePushAfter(classValue, idPath, defaultidPath);
            }

            //although the if branch before checked whether isReplaced is true
            //we still need to check here because isReplaced might be turn on.
            if (!isReplaced) {
                getContentHandler().startElement(uri, localName, name, atts);
            }
        }
    }

    private void handlePushAfter(final DitaClass classValue, final String idPath, final String defaultidPath) {
        MoveKey containkey = null;
        boolean containpushafter = false;
        if  (movetable.containsKey(new MoveKey(idPath, ATTR_CONACTION_VALUE_PUSHAFTER))) {
            containkey = new MoveKey(idPath, ATTR_CONACTION_VALUE_PUSHAFTER);
            if (isPushedTypeMatch(classValue, movetable.get(containkey))) {
                containpushafter = true;
            }
        } else if (movetable.containsKey(new MoveKey(defaultidPath, ATTR_CONACTION_VALUE_PUSHAFTER))) {
            containkey = new MoveKey(defaultidPath, ATTR_CONACTION_VALUE_PUSHAFTER);
            if (isPushedTypeMatch(classValue, movetable.get(containkey))) {
                containpushafter = true;
            }
        }
        if (containpushafter) {
            if (hasPushafter && levelForPushAfter > 0) {
                //there is a "pushafter" action for an ancestor element.
                //we need to push the levelForPushAfter to stack before
                //initialize it.
                levelForPushAfterStack.push(levelForPushAfter);
                contentForPushAfterStack.push(contentForPushAfter);
            } else {
                hasPushafter = true;
            }
            levelForPushAfter = 0;
            levelForPushAfter++;
            contentForPushAfter = replaceElementName(classValue, movetable.remove(containkey));
            //The output for the pushcontent will be in endElement(...)
        }
    }

    private void handlePushReplace(final DitaClass classValue, final String idPath, final String defaultidPath)
            throws SAXException {
        MoveKey containkey = null;
        boolean containpushplace = false;
        if (movetable.containsKey(new MoveKey(idPath, ATTR_CONACTION_VALUE_PUSHREPLACE))) {
            containkey = new MoveKey(idPath, ATTR_CONACTION_VALUE_PUSHREPLACE);
            if (isPushedTypeMatch(classValue, movetable.get(containkey))) {
                containpushplace = true;
            }
        } else if (movetable.containsKey(new MoveKey(defaultidPath, ATTR_CONACTION_VALUE_PUSHREPLACE))) {
            containkey = new MoveKey(defaultidPath, ATTR_CONACTION_VALUE_PUSHREPLACE);
            if (isPushedTypeMatch(classValue, movetable.get(containkey))) {
                containpushplace = true;
            }
        }
        if (containpushplace) {
            writeNode(replaceElementName(classValue, movetable.remove(containkey)));
            isReplaced = true;
            level = 0;
            level++;
        }
    }

    private void handlePushBefore(final DitaClass classValue, final String idPath, final String defaultidPath)
            throws SAXException {
        MoveKey containkey = null;
        boolean containpushbefore = false;
        if (movetable.containsKey(new MoveKey(idPath, ATTR_CONACTION_VALUE_PUSHBEFORE))) {
            containkey = new MoveKey(idPath, ATTR_CONACTION_VALUE_PUSHBEFORE);
            if (isPushedTypeMatch(classValue, movetable.get(containkey))) {
                containpushbefore = true;
            }

        } else if (movetable.containsKey(new MoveKey(defaultidPath, ATTR_CONACTION_VALUE_PUSHBEFORE))) {
            containkey = new MoveKey(defaultidPath, ATTR_CONACTION_VALUE_PUSHBEFORE);
            if (isPushedTypeMatch(classValue, movetable.get(containkey))) {
                containpushbefore = true;
            }
        }
        if (containpushbefore) {
            writeNode(replaceElementName(classValue, movetable.remove(containkey)));
        }
    }

    private void writeNode(final Node node) throws SAXException {
        switch(node.getNodeType()) {
        case Node.DOCUMENT_FRAGMENT_NODE: {
            final NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                writeNode(children.item(i));
            }
            break;
        }
        case Node.ELEMENT_NODE:
            final Element e = (Element) node;
            final AttributesBuilder b = new AttributesBuilder();
            final NamedNodeMap atts = e.getAttributes();
            for (int i = 0; i < atts.getLength(); i++) {
                b.add((Attr) atts.item(i));
            }
            final String ns = e.getNamespaceURI() != null ? e.getNamespaceURI() : NULL_NS_URI;
            getContentHandler().startElement(ns, e.getTagName(), e.getNodeName(), b.build());
            final NodeList children = e.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                writeNode(children.item(i));
            }
            getContentHandler().endElement(ns, e.getTagName(), e.getNodeName());
            break;
        case Node.TEXT_NODE:
            final char[] data = node.getNodeValue().toCharArray();
            getContentHandler().characters(data, 0, data.length);
            break;
        case Node.PROCESSING_INSTRUCTION_NODE:
            getContentHandler().processingInstruction(node.getNodeName(), node.getNodeValue());
            break;
        default:
            throw new UnsupportedOperationException();
        }
    }
    
}
