/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.reader;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.*;

import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.dom.DOMResult;

import org.dita.dost.log.MessageUtils;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.XMLUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Class for reading conref push content.
 *
 */
public final class ConrefPushReader extends AbstractXMLReader {

    /** push table.*/
    private final Hashtable<File, Hashtable<MoveKey, DocumentFragment>> pushtable;
    /** Document used to construct push table DocumentFragments. */
    private final Document pushDocument;
    /** push table.*/
    private final XMLReader reader;

    /**keep the file path of current file under parse
	filePath is useful to get the absolute path of the target file.*/
    private File fileDir = null;

    /**keep the file name of  current file under parse */
    private File parsefilename = null;
    /**pushcontent is used to store the content copied to target
	 in pushcontent href will be resolved if it is relative path
	 if @conref is in pushconref the target name should be recorded so that it
	 could be added to conreflist for conref resolution.*/
    private XMLStreamWriter pushcontentWriter;
    /** Common document for all push content document fragments. */
    private DocumentFragment pushcontentDocumentFragment;

    /**boolean start is used to control whether sax parser can start to
	 record push content into String pushcontent.*/
    private boolean start = false;
    /**level is used to record the level number to the root element in pushcontent
	 In endElement(...) we can turn start off to terminate adding content to pushcontent
	 if level is zero. That means we reach the end tag of the starting element.*/
    private int level = 0;

    /**target is used to record the target of the conref push
	 if we reach pushafter action but there is no target recorded before, we need
	 to report error.*/
    private URI target = null;

    /**pushType is used to record the current type of push
	 it is used in endElement(....) to tell whether it is pushafter or replace.*/
    private String pushType = null;
        
    /**
     * Get push table
     * 
     * @return unmodifiable push table
     */
    public Map<File, Hashtable<MoveKey, DocumentFragment>> getPushMap() {
    	return Collections.unmodifiableMap(pushtable);
    }
    
    @Override
    public void read(final File filename) {
        assert filename.isAbsolute();
        fileDir = filename.getParentFile().getAbsoluteFile();
        parsefilename = new File(filename.getName());
        start = false;
        pushcontentWriter = getXMLStreamWriter();
        pushType = null;
        try{
            reader.parse(filename.toURI().toString());
        } catch (final RuntimeException e) {
            throw e;
        }catch (final Exception e) {
            logger.error(e.getMessage(), e) ;
            e.printStackTrace();
        }
    }
    
    private XMLStreamWriter getXMLStreamWriter() {
        pushcontentDocumentFragment = pushDocument.createDocumentFragment();
        try {
            return XMLOutputFactory.newInstance().createXMLStreamWriter(new DOMResult(pushcontentDocumentFragment));
        } catch (final XMLStreamException | FactoryConfigurationError e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Constructor.
     */
    public ConrefPushReader() {
        pushtable = new Hashtable<>();
        try{
            reader = XMLUtils.getXMLReader();
            reader.setFeature(FEATURE_NAMESPACE_PREFIX, false);
            reader.setFeature(FEATURE_NAMESPACE, true);
            reader.setContentHandler(this);
        }catch (final Exception e) {
            throw new RuntimeException("Failed to initialize XML parser: " + e.getMessage(), e);
        }

        final DocumentBuilder documentBuilder = XMLUtils.getDocumentBuilder();
        pushDocument = documentBuilder.newDocument();
    }

    @Override
    public void startElement(final String uri, final String localName, final String name,
            final Attributes atts) throws SAXException {
        if (start) {
            //if start is true, we need to record content in pushcontent
            //also we need to add level to make sure start is turn off
            //at the corresponding end element
            level++;
            putElement(name, atts, false);
        }

        final String conactValue = atts.getValue(ATTRIBUTE_NAME_CONACTION);
        if (!start && conactValue != null) {
            if (ATTR_CONACTION_VALUE_PUSHBEFORE.equals(conactValue)) {
                if (pushcontentDocumentFragment.getChildNodes().getLength() != 0) {
                    // there are redundant "pushbefore", create a new pushcontent and emit a warning message.
                    if (pushcontentWriter != null) {
                        try {
                            pushcontentWriter.close();
                        } catch (final XMLStreamException e) {
                            throw new SAXException(e);
                        }
                    }
                    pushcontentWriter = getXMLStreamWriter();
                    logger.warn(MessageUtils.getInstance().getMessage("DOTJ044W", atts.getValue(ATTRIBUTE_NAME_XTRF), atts.getValue(ATTRIBUTE_NAME_XTRC)).toString());
                }
                start = true;
                level = 1;
                putElement(name, atts, true);
                pushType = ATTR_CONACTION_VALUE_PUSHBEFORE;
            } else if (ATTR_CONACTION_VALUE_PUSHAFTER.equals(conactValue)) {
                start = true;
                level = 1;
                if (target == null) {
                    logger.error(MessageUtils.getInstance().getMessage("DOTJ039E", atts.getValue(ATTRIBUTE_NAME_XTRF), atts.getValue(ATTRIBUTE_NAME_XTRC)).toString());
                } else {
                    putElement(name, atts, true);
                    pushType = ATTR_CONACTION_VALUE_PUSHAFTER;
                }
            } else if (ATTR_CONACTION_VALUE_PUSHREPLACE.equals(conactValue)) {
                start = true;
                level = 1;
                target = toURI(atts.getValue(ATTRIBUTE_NAME_CONREF));
                if (target == null) {
                    logger.error(MessageUtils.getInstance().getMessage("DOTJ040E", atts.getValue(ATTRIBUTE_NAME_XTRF), atts.getValue(ATTRIBUTE_NAME_XTRC)).toString());
                } else {
                    pushType = ATTR_CONACTION_VALUE_PUSHREPLACE;
                    putElement(name, atts, true);
                }

            } else if (ATTR_CONACTION_VALUE_MARK.equals(conactValue)) {
                target = toURI(atts.getValue(ATTRIBUTE_NAME_CONREF));
                if (target != null &&
                        pushcontentDocumentFragment != null && pushcontentDocumentFragment.getChildNodes().getLength() > 0 &&
                        ATTR_CONACTION_VALUE_PUSHBEFORE.equals(pushType)) {
                    //pushcontent != null means it is pushbefore action
                    //we need to add target and content to pushtable
                    if (pushcontentWriter != null) {
                        try {
                            pushcontentWriter.close();
                        } catch (final XMLStreamException e) {
                            throw new SAXException(e);
                        }
                    }
                    addtoPushTable(target, replaceContent(pushcontentDocumentFragment), pushType);
                    pushcontentWriter = getXMLStreamWriter();
                    target = null;
                    pushType = null;
                }
            }
        }//else if (pushcontent != null && pushcontent.length() > 0 && level == 0) {
        //if there is no element with conaction="mark" after
        //one with conaction="pushbefore", report syntax error

        //}
    }
    
    /**
     * Rewrite link attributes.
     */
    private DocumentFragment replaceContent(final DocumentFragment pushcontent) {
        final NodeList children = pushcontent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            final Node child = children.item(i);
            switch(child.getNodeType()) {
            case Node.ELEMENT_NODE:
                final Element e = (Element) child;
                replaceLinkAttributes(e);
                final NodeList elements = e.getElementsByTagName("*");
                for (int j = 0; i < elements.getLength(); i++) {
                    replaceLinkAttributes((Element) elements.item(j));
                }
                break;
            }
        }
        return pushcontent;
    }
    private void replaceLinkAttributes(final Element pushcontent) {
        for (final String attName: new String[] { ATTRIBUTE_NAME_HREF, ATTRIBUTE_NAME_CONREF }) {
            final Attr att = pushcontent.getAttributeNode(attName);
            if (att != null) {
                att.setNodeValue(replaceURL(att.getNodeValue()));
            }
        }
    }
    
    /**
     * Write start element.
     * 
     * @param elemName element name
     * @param atts attribute
     * @param removeConref whether remeove conref info
     * @throws SAXException if writing element failed
     */
    private void putElement(final String elemName, final Attributes atts, final boolean removeConref) throws SAXException {
        //parameter boolean removeConref specifies whether to remove
        //conref information like @conref @conaction in current element
        //when copying it to pushcontent. True means remove and false means
        //not remove.
        try {
            pushcontentWriter.writeStartElement(elemName);
            for (int index = 0; index < atts.getLength(); index++) {
                final String name = atts.getQName(index);
                if (!removeConref ||
                        !ATTRIBUTE_NAME_CONREF.equals(name) && !ATTRIBUTE_NAME_CONACTION.equals(name)) {
                    String value = atts.getValue(index);
                    if (ATTRIBUTE_NAME_HREF.equals(name) || ATTRIBUTE_NAME_CONREF.equals(name)) {
                        // adjust href for pushbefore and replace
                        value = replaceURL(value);
                    }
                    final int offset = atts.getQName(index).indexOf(":");
                    final String prefix = offset != -1 ? atts.getQName(index).substring(0, offset) : "";
                    pushcontentWriter.writeAttribute(prefix, atts.getURI(index), atts.getLocalName(index), value);
                }
    
            }
            //id attribute should only be added to the starting element
            //which dosen't have id attribute set
            if (ATTR_CONACTION_VALUE_PUSHREPLACE.equals(pushType) &&
                    atts.getValue(ATTRIBUTE_NAME_ID) == null &&
                    level == 1) {
                final String fragment = target.getFragment();
                if (fragment == null) {
                    //if there is no '#' in target string, report error
                    logger.error(MessageUtils.getInstance().getMessage("DOTJ041E", target.toString()).toString());
                } else {
                    String id = "";
                    //has element id
                    if (fragment.contains(SLASH)) {
                        id = fragment.substring(fragment.lastIndexOf(SLASH) + 1);
                    } else {
                        id = fragment;
                    }
                    //add id attribute
                    pushcontentWriter.writeAttribute(ATTRIBUTE_NAME_ID, id);
                }
            }
        } catch (final XMLStreamException e) {
            throw new SAXException(e);
        }
    }
    /**
     * 
     * @param value string
     * @return URL
     */
    private String replaceURL(final String value) {
        if (value == null) {
            return null;
        } else if (target == null ||
                FileUtils.isAbsolutePath(value) ||
                value.contains(COLON_DOUBLE_SLASH) ||
                value.startsWith(SHARP)) {
            return value;
        } else {
            final String source = FileUtils.resolve(fileDir, target).getPath();
            final String urltarget = FileUtils.resolveTopic(fileDir, value);
            return FileUtils.getRelativeUnixPath(source, urltarget);
        }
    }
    /**
     * 
     * @param target target
     * @param pushcontent content
     * @param type push type
     */
    private void addtoPushTable(URI target, final DocumentFragment pushcontent, final String type) {
        if (target.getFragment() == null) {
            //if there is no '#' in target string, report error
            logger.error(MessageUtils.getInstance().getMessage("DOTJ041E", target.toString()).toString());
            return;
        }

        if (target.getPath().isEmpty()) {
            //means conref the file itself
            target = toURI(parsefilename.getPath() + target);
        }
        final File key = toFile(FileUtils.resolve(fileDir, target));
        Hashtable<MoveKey, DocumentFragment> table = null;
        if (pushtable.containsKey(key)) {
            //if there is something else push to the same file
            table = pushtable.get(key);
        } else {
            //if there is nothing else push to the same file
            table = new Hashtable<>();
            pushtable.put(key, table);
        }

        final MoveKey moveKey = new MoveKey(SHARP + target.getFragment(), type);

        if (table.containsKey(moveKey)) {
            //if there is something else push to the same target
            //append content if type is 'pushbefore' or 'pushafter'
            //report error if type is 'replace'
            if (ATTR_CONACTION_VALUE_PUSHREPLACE.equals(type)) {
                logger.error(MessageUtils.getInstance().getMessage("DOTJ042E", target.toString()).toString());
            } else {
                table.put(moveKey, appendPushContent(pushcontent, table.get(moveKey)));
            }

        } else {
            //if there is nothing else push to the same target
            table.put(moveKey, appendPushContent(pushcontent, null));
        }
    }
    
    private DocumentFragment appendPushContent(final DocumentFragment pushcontent, final DocumentFragment target) {
        DocumentFragment df = target;
        if (df == null) {
            df = pushDocument.createDocumentFragment();
        }
        final NodeList children = pushcontent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            df.appendChild(pushDocument.importNode(children.item(i), true));
        }
        return df;
    }

    @Override
    public void characters(final char[] ch, final int start, final int length)
            throws SAXException {
        if (this.start) {
            try {
                pushcontentWriter.writeCharacters(ch, start, length);
            } catch (XMLStreamException e) {
                throw new SAXException(e);
            }
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String name)
            throws SAXException {
        if (start) {
            level--;
            try {
                pushcontentWriter.writeEndElement();
            } catch (XMLStreamException e) {
                throw new SAXException(e);
            }
        }
        if (level == 0) {
            //turn off start if we reach the end tag of staring element
            start = false;
            if (ATTR_CONACTION_VALUE_PUSHAFTER.equals(pushType) || ATTR_CONACTION_VALUE_PUSHREPLACE.equals(pushType)) {
                //if it is pushafter or replace, we need to record content in pushtable
                //if target == null we have already reported error in startElement;
                if (target != null) {
                    if (pushcontentWriter != null) {
                        try {
                            pushcontentWriter.close();
                        } catch (final XMLStreamException e) {
                            throw new SAXException(e);
                        }
                    }
                    addtoPushTable(target, pushcontentDocumentFragment, pushType);
                    pushcontentWriter = getXMLStreamWriter();
                    target = null;
                    pushType = null;
                }
            }
        }
    }

    public static class MoveKey {
        public final String idPath;
        public final String action;
        public MoveKey(final String idPath, final String action) {
            this.idPath = idPath;
            this.action = action;
        }
        @Override
        public String toString() {
            return idPath + STICK + action;
        }
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((action == null) ? 0 : action.hashCode());
            result = prime * result + ((idPath == null) ? 0 : idPath.hashCode());
            return result;
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof MoveKey)) {
                return false;
            }
            MoveKey other = (MoveKey) obj;
            if (action == null) {
                if (other.action != null) {
                    return false;
                }
            } else if (!action.equals(other.action)) {
                return false;
            }
            if (idPath == null) {
                if (other.idPath != null) {
                    return false;
                }
            } else if (!idPath.equals(other.idPath)) {
                return false;
            }
            return true;
        }
    }
    
}
