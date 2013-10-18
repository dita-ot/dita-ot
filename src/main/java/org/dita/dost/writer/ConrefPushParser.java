/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.writer;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.Job.*;
import static org.dita.dost.reader.ConrefPushReader.*;

import org.dita.dost.util.XMLUtils.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.util.DitaClass;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.Job;
import org.dita.dost.util.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
/**
 * This class is for writing conref push contents into
 * specific files.
 *
 */
public final class ConrefPushParser extends AbstractXMLWriter {

    /**table containing conref push contents.*/
    private Hashtable<String, String> movetable = null;

    /**topicId keep the current topic id value.*/
    private String topicId = null;

    /**idStack keeps the history of topicId because topics can be nested.*/
    private Stack<String> idStack = null;
    /**parser.*/
    private final XMLReader parser;
    /**output.*/
    private OutputStreamWriter output = null;

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
    private String contentForPushAfter = null;

    /**contentForPushAfterStack is used to store the history value of contentForPushAfter
	It is possible that we have pushafter action for both parent and child element.
	In this case, we need to push the parent's value of contentForPushAfter to Stack
	before getting value contentForPushAfter for child element from movetable. When we
	finished pushafter action for child element, we need to restore the original value for
	parent. */
    private Stack<String> contentForPushAfterStack = null;

    /**if the pushcontent has @conref, it should be paid attention to it. Because the current
	file may not contain any @conref attribute, it will not resolved by the conref.xsl,
	while it may contain @conref after pushing. So the dita.list file should be updated, if
	the pushcontent has @conref.*/
    private boolean hasConref = false;
    /**tempDir.*/
    private File tempDir;
    private Job job;
    
    /**
     * Constructor.
     */
    public ConrefPushParser() {
        topicSpecSet = new HashSet<String>();
        levelForPushAfterStack = new Stack<Integer>();
        contentForPushAfterStack = new Stack<String>();
        try {
            parser = StringUtils.getXMLReader();
            parser.setFeature(FEATURE_NAMESPACE_PREFIX, true);
            parser.setFeature(FEATURE_NAMESPACE, true);
            parser.setContentHandler(this);
            parser.setProperty(LEXICAL_HANDLER_PROPERTY,this);
        } catch (final Exception e) {
            throw new RuntimeException("Failed to initialize XML parser: " + e.getMessage(), e);
        }
    }
    
    public void setJob(final Job job) {
        this.job = job;
    }
    
    public void setMoveTable(final Hashtable<String, String> movetable) {
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
        isReplaced = false;
        hasPushafter = false;
        level = 0;
        levelForPushAfter = 0;
        idStack = new Stack<String>();
        topicSpecSet = new HashSet<String>();
        levelForPushAfterStack = new Stack<Integer>();
        contentForPushAfterStack = new Stack<String>();
        try {
            final File inputFile = filename;
            final File outputFile = new File(filename+".cnrfpush");
            output = new OutputStreamWriter(new FileOutputStream(outputFile),UTF8);
            parser.parse(filename.toURI().toString());
            if (!movetable.isEmpty()) {
                final Properties prop = new Properties();
                final Iterator<String> iterator = movetable.keySet().iterator();
                while(iterator.hasNext()) {
                    final String key = iterator.next();
                    logger.logWarn(MessageUtils.getInstance().getMessage("DOTJ043W", key.substring(0, key.indexOf(STICK)), filename.getPath()).toString());
                }
            }
            if (hasConref) {
                updateList(filename);
            }
            output.close();
            if (!inputFile.delete()) {
                logger.logError(MessageUtils.getInstance().getMessage("DOTJ009E", inputFile.getPath(), outputFile.getPath()).toString());
            }
            if (!outputFile.renameTo(inputFile)) {
                logger.logError(MessageUtils.getInstance().getMessage("DOTJ009E", inputFile.getPath(), outputFile.getPath()).toString());
            }
        } catch (final Exception e) {
            logger.logError(e.getMessage(), e) ;
        } finally {
            try {
                output.close();
            } catch (final Exception ex) {
                logger.logError(ex.getMessage(), ex) ;
            }
        }


    }
    /**
     * Update conref list in job configuration and in conref list file.
     * 
     * @param filename filename
     */
    private void updateList(final File filename) {
        // this is used to update the conref.list file.
        BufferedWriter bufferedWriter =null;
        try {
            // get the reletivePath from tempDir
            final String reletivePath = filename.getAbsolutePath().substring(FileUtils.normalize(tempDir.toString()).getPath().length() + 1);
            for (final FileInfo f: job.getFileInfo()) {
                final String str = f.file.getPath();
                if (f.hasConref) {
                    if (str.equals(reletivePath)) {
                        return;
                    }
                }
            }
            job.getOrCreateFileInfo(reletivePath).hasConref = true;
            
            job.write();

            try {
                bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(tempDir, CONREF_LIST_FILE))));
                for (final FileInfo f: job.getFileInfo()) {
                    final String str = f.file.getPath();
                    if (f.hasConref) {
                        bufferedWriter.append(str).append("\n");
                    }
                }
                bufferedWriter.append(reletivePath);
            } finally {
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
            }
        } catch (final Exception e) {
            logger.logError(e.getMessage(), e) ;
        }

    }

    @Override
    public void characters(final char[] ch, final int start, final int length)
            throws SAXException {
        if (!isReplaced) {
            try {
                writeCharacters(ch, start, length);
            } catch (final IOException e) {
                logger.logError(e.getMessage(), e) ;
            }
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
            //write the end tag
            try {
                writeEndElement(name);
            } catch (final Exception e) {
                logger.logError(e.getMessage(), e) ;
            }
        }

        if (hasPushafter) {
            levelForPushAfter--;
            if (levelForPushAfter == 0) {
                //write the pushcontent after the end tag
                try {
                    if (contentForPushAfter != null) {
                        output.write(contentForPushAfter);
                    }
                } catch (final Exception e) {
                    logger.logError(e.getMessage(), e) ;
                }
                if (!levelForPushAfterStack.isEmpty() &&
                        !contentForPushAfterStack.isEmpty()) {
                    levelForPushAfter = levelForPushAfterStack.pop().intValue();
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

    @Override
    public void processingInstruction(final String target, final String data)
            throws SAXException {
        if (!isReplaced) {
            try {
                writeProcessingInstruction(target, data);
            } catch (final IOException e) {
                logger.logError(e.getMessage(), e) ;
            }
        }
    }

    /**
     * The function is to judge if the pushed content type march the type of content being pushed/replaced
     * @param targetClassAttribute the class attribute of target element which is being pushed
     * @param string pushedContent
     * @return boolean: if type match, return true, else return false
     */
    private boolean isPushedTypeMatch(final DitaClass targetClassAttribute, final String content) {
        DitaClass clazz = null;
        //add stub to serve as the root element
        final String string = "<stub>" + content + "</stub>";
        final InputSource inputSource = new InputSource(new StringReader(string));
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        try {
            final DocumentBuilder documentBuilder = factory.newDocumentBuilder();
            final Document document = documentBuilder.parse(inputSource);
            final Element element = document.getDocumentElement();
            if (element.hasChildNodes()) {
                final NodeList nodeList = element.getChildNodes();
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
        } catch (final Exception e) {
            e.printStackTrace();
            return false;
        }

        return targetClassAttribute.matches(clazz);
    }

    /**
     * 
     * @param targetClassAttribute targetClassAttribute
     * @param string string
     * @return string
     */
    private String replaceElementName(final DitaClass targetClassAttribute, final String content) {        
        //add stub to serve as the root element
        final String string = "<stub>" + content + "</stub>";
        final InputSource inputSource = new InputSource(new StringReader(string));
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final StringBuffer stringBuffer = new StringBuffer();
        try {
            final DocumentBuilder documentBuilder = factory.newDocumentBuilder();
            final Document document = documentBuilder.parse(inputSource);
            final Element element = document.getDocumentElement();
            if (element.hasChildNodes()) {
                final NodeList nodeList = element.getChildNodes();
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
                            stringBuffer.append(LESS_THAN).append(targetElementName);
                            final NamedNodeMap namedNodeMap = elem.getAttributes();
                            for (int t = 0; t < namedNodeMap.getLength(); t++) {
                                //write the attributes to new generated element
                                final Attr attr = (Attr) namedNodeMap.item(t);
                                if (attr.getNodeName().equals(ATTRIBUTE_NAME_CONREF) && attr.getNodeValue().length() != 0) {
                                    hasConref = true;
                                }
                                stringBuffer.append(STRING_BLANK)
                                    .append(attr.getNodeName())
                                    .append(EQUAL)
                                    .append(QUOTATION)
                                    .append(StringUtils.escapeXML(attr.getNodeValue()))
                                    .append(QUOTATION);
                            }
                            stringBuffer.append(GREATER_THAN);
                            // process the child nodes of the current node
                            final NodeList nList = elem.getChildNodes();
                            for (int j = 0; j < nList.getLength(); j++) {
                                final Node subNode = nList.item(j);
                                if (subNode.getNodeType() == Node.ELEMENT_NODE) {
                                    //replace the subElement Name
                                    stringBuffer.append(replaceSubElementName(type, (Element)subNode));
                                }
                                if (subNode.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
                                    stringBuffer.append("<?").append(subNode.getNodeName()).append("?>");
                                }
                                if (subNode.getNodeType() == Node.TEXT_NODE) {
                                    //stringBuffer.append(subNode.getNodeValue());
                                    stringBuffer.append(StringUtils.escapeXML(subNode.getNodeValue()));
                                }
                            }
                            stringBuffer.append("</").append(targetElementName).append(GREATER_THAN);
                        } else {
                            stringBuffer.append(replaceSubElementName(STRING_BLANK, elem));
                        }
                    }
                }
                return stringBuffer.toString();
            } else {
                return string;
            }
        } catch (final Exception e) {
            e.printStackTrace();
            return string;
        }
    }
    /**
     * 
     * @param type pushtype
     * @param elem element
     * @return string
     */
    private String replaceSubElementName(final String type, final Element elem) {
        final StringBuffer stringBuffer = new StringBuffer();
        final DitaClass classValue = DitaClass.getInstance(elem);
        String generalizedElemName = elem.getNodeName();
        if (classValue != null) {
            if (classValue.toString().contains(type) && !type.equals(STRING_BLANK)) {
                generalizedElemName = classValue.toString().substring(classValue.toString().indexOf("/") + 1, classValue.toString().indexOf(STRING_BLANK, classValue.toString().indexOf("/"))).trim();
            }
        }
        stringBuffer.append(LESS_THAN).append(generalizedElemName);
        final NamedNodeMap namedNodeMap = elem.getAttributes();
        for (int i = 0; i < namedNodeMap.getLength(); i++) {
            final Attr attr = (Attr) namedNodeMap.item(i);
            if (attr.getNodeName().equals(ATTRIBUTE_NAME_CONREF) && attr.getNodeValue().length()!=0) {
                hasConref = true;
            }
            stringBuffer.append(STRING_BLANK)
                .append(attr.getNodeName())
                .append(EQUAL)
                .append(QUOTATION)
                .append(StringUtils.escapeXML(attr.getNodeValue()))
                .append(QUOTATION);
        }
        stringBuffer.append(GREATER_THAN);
        final NodeList nodeList = elem.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            final Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                // If the type of current node is ELEMENT_NODE, process current node.
                stringBuffer.append(replaceSubElementName(type, (Element)node));
            }
            if (node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE) {
                stringBuffer.append("<?").append(node.getNodeName()).append("?>");
            }
            if (node.getNodeType() == Node.TEXT_NODE) {
                //stringBuffer.append(node.getNodeValue());
                stringBuffer.append(StringUtils.escapeXML(node.getNodeValue()));
            }
        }
        stringBuffer.append("</").append(generalizedElemName).append(GREATER_THAN);
        return stringBuffer.toString();
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
            try {
                final DitaClass classValue = DitaClass.getInstance(atts);
                if (classValue != null && TOPIC_TOPIC.matches(classValue)) {
                    if (!topicSpecSet.contains(name)) {
                        //add the element name to topicSpecSet if the element
                        //is a topic specialization. This is used when push and pop
                        //topic ids in a stack
                        topicSpecSet.add(name);
                    }
                    final String idValue = atts.getValue(ATTRIBUTE_NAME_ID);
                    if (idValue != null) {
                        if (topicId != null) {
                            idStack.push(topicId);
                        }
                        topicId = idValue;
                    }
                } else if (atts.getValue(ATTRIBUTE_NAME_ID) != null) {
                    String idPath = SHARP + topicId + SLASH + atts.getValue(ATTRIBUTE_NAME_ID);
                    final String defaultidPath = SHARP + atts.getValue(ATTRIBUTE_NAME_ID);
                    String containkey = null;
                    //enable conref push at map level
                    if (classValue != null && (MAP_TOPICREF.matches(classValue)
                            || MAP_MAP.matches(classValue))) {
                        final String mapId = atts.getValue(ATTRIBUTE_NAME_ID);
                        idPath = SHARP + mapId;
                        idStack.push(mapId);
                    }
                    boolean containpushbefore = false;
                    if (movetable.containsKey(idPath + STICK + ATTR_CONACTION_VALUE_PUSHBEFORE)) {
                        containkey = idPath + STICK + ATTR_CONACTION_VALUE_PUSHBEFORE;
                        if (isPushedTypeMatch(classValue, movetable.get(containkey))) {
                            containpushbefore = true;
                        }

                    } else if (movetable.containsKey(defaultidPath + STICK + ATTR_CONACTION_VALUE_PUSHBEFORE)) {
                        containkey = defaultidPath + STICK + ATTR_CONACTION_VALUE_PUSHBEFORE;
                        if (isPushedTypeMatch(classValue, movetable.get(containkey))) {
                            containpushbefore = true;
                        }
                    }
                    if (containpushbefore) {
                        output.write(replaceElementName(classValue, movetable.remove(containkey)));
                    }

                    boolean containpushplace = false;
                    if (movetable.containsKey(idPath + STICK + ATTR_CONACTION_VALUE_PUSHREPLACE)) {
                        containkey = idPath + STICK + ATTR_CONACTION_VALUE_PUSHREPLACE;
                        if (isPushedTypeMatch(classValue, movetable.get(containkey))) {
                            containpushplace= true;
                        }
                    } else if (movetable.containsKey(defaultidPath + STICK + ATTR_CONACTION_VALUE_PUSHREPLACE)) {
                        containkey = defaultidPath + STICK + ATTR_CONACTION_VALUE_PUSHREPLACE;
                        if (isPushedTypeMatch(classValue, movetable.get(containkey))) {
                            containpushplace= true;
                        }
                    }

                    if (containpushplace) {
                        output.write(replaceElementName(classValue, movetable.remove(containkey)));
                        isReplaced = true;
                        level = 0;
                        level++;
                    }

                    boolean containpushafter = false;
                    if  (movetable.containsKey(idPath + STICK + ATTR_CONACTION_VALUE_PUSHAFTER)) {
                        containkey = idPath + STICK + ATTR_CONACTION_VALUE_PUSHAFTER;
                        if (isPushedTypeMatch(classValue, movetable.get(containkey))) {
                            containpushafter = true;
                        }
                    } else if (movetable.containsKey(defaultidPath + STICK + ATTR_CONACTION_VALUE_PUSHAFTER)) {
                        containkey = defaultidPath + STICK + ATTR_CONACTION_VALUE_PUSHAFTER;
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

                //although the if branch before checked whether isReplaced is true
                //we still need to check here because isReplaced might be turn on.
                if (!isReplaced) {
                    //output the element
                    writeStartElement(name, atts);
                }
            } catch (final Exception e) {
                logger.logError(e.getMessage(), e) ;
            }
        }
    }


    @Override
    public void endDocument() throws SAXException {
        try {
            output.flush();
            output.close();
        } catch (final Exception e) {
            logger.logError(e.getMessage(), e) ;
        } finally {
            try {
                output.close();
            } catch (final Exception e) {
                logger.logError(e.getMessage(), e) ;
            }
        }
    }

    @Override
    public void ignorableWhitespace(final char[] ch, final int start, final int length)
            throws SAXException {
        if (!isReplaced) {
            try {
                writeCharacters(ch, start, length);
            } catch (final IOException e) {
                logger.logError(e.getMessage(), e) ;
            }
        }
    }

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
    }

    // SAX serializer methods
    
    private void writeStartElement(final String qName, final Attributes atts) throws IOException {
        final int attsLen = atts.getLength();
        output.write(LESS_THAN);
        output.write(qName);
        for (int i = 0; i < attsLen; i++) {
            output.append(STRING_BLANK)
                .append(atts.getQName(i))
                .append(EQUAL)
                .append(QUOTATION)
                .append(StringUtils.escapeXML(atts.getValue(i)))
                .append(QUOTATION);
        }
        output.write(GREATER_THAN);
    }
    
    private void writeEndElement(final String qName) throws IOException {
        output.write(LESS_THAN);
        output.write(SLASH);
        output.write(qName);
        output.write(GREATER_THAN);
    }
    
    private void writeCharacters(final char[] ch, final int start, final int length) throws IOException {
        output.write(StringUtils.escapeXML(ch, start, length));
    }

    private void writeProcessingInstruction(final String target, final String data) throws IOException {
        final String pi = data != null ? target + STRING_BLANK + data : target;
        output.write(LESS_THAN);
        output.write(QUESTION);
        output.write(pi);
        output.write(QUESTION);
        output.write(GREATER_THAN);
    }
    
}
