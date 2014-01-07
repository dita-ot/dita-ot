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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
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
import org.dita.dost.module.Content;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.Job;
import org.dita.dost.util.StringUtils;
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
    /**whether an entity needs to be resolved or not flag. */
    private boolean needResolveEntity = true;

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
    private String tempDir;
    /**
     * Constructor.
     */
    public ConrefPushParser(){
        topicSpecSet = new HashSet<String>();
        levelForPushAfterStack = new Stack<Integer>();
        contentForPushAfterStack = new Stack<String>();
        needResolveEntity = true;
        try{
            parser = StringUtils.getXMLReader();
            parser.setFeature(FEATURE_NAMESPACE_PREFIX, true);
            parser.setFeature(FEATURE_NAMESPACE, true);
            parser.setContentHandler(this);
            parser.setProperty(LEXICAL_HANDLER_PROPERTY,this);
            parser.setFeature("http://apache.org/xml/features/scanner/notify-char-refs", true);
            parser.setFeature("http://apache.org/xml/features/scanner/notify-builtin-refs", true);
        }catch (final Exception e) {
            throw new RuntimeException("Failed to initialize XML parser: " + e.getMessage(), e);
        }
    }
    
    /**
     * @param content value {@code Hashtable<String, String>}
     */
    @SuppressWarnings("unchecked")
    @Override
    public void setContent(final Content content) {
        movetable = (Hashtable<String, String>)content.getValue();
        if (movetable == null) {
            throw new IllegalArgumentException("Content value must be non-null Hashtable<String, String>");
        }
    }
    
    /**
     * 
     * @param tempDir tempDir
     */
    public void setTempDir(final String tempDir){
        this.tempDir = tempDir;
    }
    /**
     * @param filename filename
     * @throws DITAOTException exception
     */
    @Override
    public void write(final String filename) throws DITAOTException {
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
            final File inputFile = new File(filename);
            final File outputFile = new File(filename+".cnrfpush");
            output = new OutputStreamWriter(new FileOutputStream(outputFile),UTF8);
            parser.parse(filename);
            if(!movetable.isEmpty()){
                final Properties prop = new Properties();
                String key = null;
                final Iterator<String> iterator = movetable.keySet().iterator();
                while(iterator.hasNext()){
                    key = iterator.next();
                    logger.logWarn(MessageUtils.getInstance().getMessage("DOTJ043W", key.substring(0, key.indexOf(STICK)), filename).toString());
                }
            }
            if(hasConref){
                updateList(filename);
            }
            output.close();
            if(!inputFile.delete()){
                logger.logError(MessageUtils.getInstance().getMessage("DOTJ009E", inputFile.getPath(), outputFile.getPath()).toString());
            }
            if(!outputFile.renameTo(inputFile)){
                logger.logError(MessageUtils.getInstance().getMessage("DOTJ009E", inputFile.getPath(), outputFile.getPath()).toString());
            }
        } catch (final Exception e) {
            logger.logError(e.getMessage(), e) ;
        }finally{
            try{
                output.close();
            }catch (final Exception ex) {
                logger.logError(ex.getMessage(), ex) ;
            }
        }


    }
    /**
     * Update conref list in job configuration and in conref list file.
     * 
     * @param filename filename
     */
    private void updateList(final String filename){
        // this is used to update the conref.list file.
        BufferedWriter bufferedWriter =null;
        try{
            final Job job = new Job(new File(tempDir));

            final Set<String> conreflist = job.getSet(CONREF_LIST);
            // get the reletivePath from tempDir
            final String reletivePath = filename.substring(FileUtils.normalize(tempDir).length() + 1);
            for(final String str: conreflist){
                if(str.equals(reletivePath)){
                    return;
                }
            }
            final Set<String> stringBuffer = new HashSet<String>(job.getSet(CONREF_LIST));
            stringBuffer.add(reletivePath);
            job.setSet(CONREF_LIST, stringBuffer);
            
            job.write();

            try {
                bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(tempDir, CONREF_LIST_FILE))));
                for(final String str: conreflist){
                    bufferedWriter.append(str).append("\n");
                }
                bufferedWriter.append(reletivePath);
            } finally {
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
            }
        }catch (final Exception e){
            logger.logError(e.getMessage(), e) ;
        }

    }

    @Override
    public void characters(final char[] ch, final int start, final int length)
            throws SAXException {
        if (!isReplaced && needResolveEntity){
            try{
                output.write(StringUtils.escapeXML(ch, start, length));
            }catch (final Exception e) {
                logger.logError(e.getMessage(), e) ;
            }
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String name)
            throws SAXException {

        if(isReplaced){
            level--;
            if(level == 0){
                isReplaced = false;
            }
        }else{
            //write the end tag
            try{
                output.write(LESS_THAN);
                output.write(SLASH);
                output.write(name);
                output.write(GREATER_THAN);
            }catch (final Exception e) {
                logger.logError(e.getMessage(), e) ;
            }
        }

        if(hasPushafter){
            levelForPushAfter--;
            if(levelForPushAfter == 0){
                //write the pushcontent after the end tag
                try{
                    if(contentForPushAfter != null){
                        output.write(contentForPushAfter);
                    }
                }catch (final Exception e) {
                    logger.logError(e.getMessage(), e) ;
                }
                if(!levelForPushAfterStack.isEmpty() &&
                        !contentForPushAfterStack.isEmpty()){
                    levelForPushAfter = levelForPushAfterStack.pop().intValue();
                    contentForPushAfter = contentForPushAfterStack.pop();
                }else{
                    hasPushafter = false;
                    //empty the contentForPushAfter since it is write to output
                    contentForPushAfter = null;
                }
            }
        }
        if(!idStack.isEmpty() && topicSpecSet.contains(name)){
            topicId = idStack.pop();
        }
    }

    @Override
    public void processingInstruction(final String target, final String data)
            throws SAXException {
        if (!isReplaced) {
            try {
                final String pi = (data != null) ? target + STRING_BLANK + data : target;
                output.write(LESS_THAN + QUESTION
                        + pi + QUESTION + GREATER_THAN);
            } catch (final Exception e) {
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
    private boolean isPushedTypeMatch(final String targetClassAttribute, String string) {
        String clazz = "";
        InputSource inputSource = null;
        Document document = null;
        //add stub to serve as the root element
        string = "<stub>" + string + "</stub>";
        inputSource = new InputSource(new StringReader(string));
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Element element = null;
        NodeList nodeList = null;

        new StringBuffer();
        try {
            final DocumentBuilder documentBuilder = factory.newDocumentBuilder();
            document = documentBuilder.parse(inputSource);
            element = document.getDocumentElement();
            if(element.hasChildNodes()){
                nodeList = element.getChildNodes();
                for(int i =0;i<nodeList.getLength();i++){
                    final Node node = nodeList.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE){
                        final Element elem = (Element) node;
                        clazz = elem.getAttribute(ATTRIBUTE_NAME_CLASS);
                        break;
                        // get type of the target element
                    }
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
            return false;
        }

        if(clazz.equalsIgnoreCase(targetClassAttribute) || clazz.contains(targetClassAttribute)) {
            return true;
        }else{
            return false;
        }


    }

    /**
     * 
     * @param targetClassAttribute targetClassAttribute
     * @param string string
     * @return string
     */
    private String replaceElementName(final String targetClassAttribute, String string){
        InputSource inputSource = null;
        Document document = null;
        //add stub to serve as the root element
        string = "<stub>" + string + "</stub>";
        inputSource = new InputSource(new StringReader(string));
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Element element = null;
        NodeList nodeList = null;
        String targetElementName ;
        String type;
        final StringBuffer stringBuffer = new StringBuffer();
        try {
            final DocumentBuilder documentBuilder = factory.newDocumentBuilder();
            document = documentBuilder.parse(inputSource);
            element = document.getDocumentElement();
            if(element.hasChildNodes()){
                nodeList = element.getChildNodes();
                for(int i =0;i<nodeList.getLength();i++){
                    final Node node = nodeList.item(i);
                    if (node.getNodeType() == Node.ELEMENT_NODE){
                        final Element elem = (Element) node;
                        NodeList nList = null;
                        final String clazz = elem.getAttribute(ATTRIBUTE_NAME_CLASS);
                        // get type of the target element
                        type = targetClassAttribute.substring(INT_1, targetClassAttribute.indexOf("/")).trim();
                        if(!clazz.equalsIgnoreCase(targetClassAttribute) && clazz.contains(targetClassAttribute)){
                            // Specializing the pushing content is not handled here
                            // but we can catch such a situation to emit a warning by comparing the class values.
                            targetElementName = targetClassAttribute.substring(targetClassAttribute.indexOf("/") +1 ).trim();
                            stringBuffer.append(LESS_THAN).append(targetElementName);
                            final NamedNodeMap namedNodeMap = elem.getAttributes();
                            for(int t=0; t<namedNodeMap.getLength(); t++){
                                //write the attributes to new generated element
                                if(namedNodeMap.item(t).getNodeName().equals("conref") && namedNodeMap.item(t).getNodeValue().length()!=0){
                                    hasConref = true;
                                }
                                stringBuffer.append(STRING_BLANK).append(namedNodeMap.item(t).getNodeName()).
                                append(EQUAL).append(QUOTATION+
                                        StringUtils.escapeXML(namedNodeMap.item(t).getNodeValue())
                                        +QUOTATION);
                            }
                            stringBuffer.append(GREATER_THAN);
                            // process the child nodes of the current node
                            nList = elem.getChildNodes();
                            for(int j=0; j<nList.getLength(); j++){
                                final Node subNode = nList.item(j);
                                if(subNode.getNodeType() == Node.ELEMENT_NODE){
                                    //replace the subElement Name
                                    stringBuffer.append(replaceSubElementName(type, (Element)subNode));
                                }
                                if(subNode.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE){
                                    stringBuffer.append("<?").append(subNode.getNodeName()).append("?>");
                                }
                                if(subNode.getNodeType() == Node.TEXT_NODE){
                                    //stringBuffer.append(subNode.getNodeValue());
                                    stringBuffer.append(StringUtils.escapeXML(subNode.getNodeValue()));
                                }
                            }
                            stringBuffer.append("</").append(targetElementName).append(GREATER_THAN);
                        }else{
                            stringBuffer.append(replaceSubElementName(STRING_BLANK, elem));
                        }
                    }
                }
                return stringBuffer.toString();
            }
            else {
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
    private String replaceSubElementName(final String type, final Element elem){
        final StringBuffer stringBuffer = new StringBuffer();
        final String classValue = elem.getAttribute(ATTRIBUTE_NAME_CLASS);
        String generalizedElemName = elem.getNodeName();
        if(classValue != null){
            if(classValue.contains(type) && !type.equals(STRING_BLANK)){
                generalizedElemName = classValue.substring(classValue.indexOf("/") +1 , classValue.indexOf(STRING_BLANK, classValue.indexOf("/"))).trim();
            }
        }
        stringBuffer.append(LESS_THAN).append(generalizedElemName);
        final NamedNodeMap namedNodeMap = elem.getAttributes();
        for(int i=0; i<namedNodeMap.getLength(); i++){
            if(namedNodeMap.item(i).getNodeName().equals("conref") && namedNodeMap.item(i).getNodeValue().length()!=0){
                hasConref = true;
            }
            stringBuffer.append(STRING_BLANK).append(namedNodeMap.item(i).getNodeName()).
            append(EQUAL).append(QUOTATION+
                    StringUtils.escapeXML(namedNodeMap.item(i).getNodeValue())
                    +QUOTATION);
        }
        stringBuffer.append(GREATER_THAN);
        final NodeList nodeList = elem.getChildNodes();
        for(int i=0; i<nodeList.getLength(); i++){
            final Node node = nodeList.item(i);
            if(node.getNodeType() == Node.ELEMENT_NODE){
                // If the type of current node is ELEMENT_NODE, process current node.
                stringBuffer.append(replaceSubElementName(type, (Element)node));
            }
            if(node.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE){
                stringBuffer.append("<?").append(node.getNodeName()).append("?>");
            }
            if(node.getNodeType() == Node.TEXT_NODE){
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
        if(hasPushafter){
            levelForPushAfter ++;
        }
        if(isReplaced){
            level ++;
        }else{
            try{
                final String classValue = atts.getValue(ATTRIBUTE_NAME_CLASS);
                if (classValue != null && TOPIC_TOPIC.matches(classValue)){
                    if (!topicSpecSet.contains(name)){
                        //add the element name to topicSpecSet if the element
                        //is a topic specialization. This is used when push and pop
                        //topic ids in a stack
                        topicSpecSet.add(name);
                    }
                    final String idValue = atts.getValue(ATTRIBUTE_NAME_ID);
                    if (idValue != null){
                        if (topicId != null){
                            idStack.push(topicId);
                        }
                        topicId = idValue;
                    }
                }else if (atts.getValue(ATTRIBUTE_NAME_ID) != null){
                    String idPath = SHARP+topicId+SLASH+atts.getValue(ATTRIBUTE_NAME_ID);
                    final String defaultidPath = SHARP+atts.getValue(ATTRIBUTE_NAME_ID);
                    String containkey =null;
                    //enable conref push at map level
                    if(classValue != null && (MAP_TOPICREF.matches(classValue)
                            || MAP_MAP.matches(classValue))){
                        final String mapId = atts.getValue(ATTRIBUTE_NAME_ID);
                        idPath = SHARP + mapId;
                        idStack.push(mapId);
                    }
                    atts.getValue(ATTRIBUTE_NAME_CLASS);
                    boolean containpushbefore= false;
                    if (movetable.containsKey(idPath+STICK+"pushbefore")){
                        containkey=idPath+STICK+"pushbefore";
                        if(isPushedTypeMatch(classValue, movetable.get(containkey))) {
                            containpushbefore = true;
                        }

                    }else if (movetable.containsKey(defaultidPath+STICK+"pushbefore")){
                        containkey=defaultidPath+STICK+"pushbefore";
                        if(isPushedTypeMatch(classValue, movetable.get(containkey))) {
                            containpushbefore = true;
                        }
                    }
                    if (containpushbefore){
                        output.write(replaceElementName(classValue, movetable.remove(containkey)));
                    }


                    boolean containpushplace = false;

                    if  (movetable.containsKey(idPath+STICK+"pushreplace")){
                        containkey=idPath+STICK+"pushreplace";
                        if(isPushedTypeMatch(classValue, movetable.get(containkey))) {
                            containpushplace= true;
                        }
                    }else if (movetable.containsKey(defaultidPath+STICK+"pushreplace")){
                        containkey = defaultidPath+STICK+"pushreplace";
                        if(isPushedTypeMatch(classValue, movetable.get(containkey))) {
                            containpushplace= true;
                        }
                    }

                    if (containpushplace){
                        output.write(replaceElementName(classValue, movetable.remove(containkey)));
                        isReplaced = true;
                        level = 0;
                        level ++;
                    }

                    boolean containpushafter = false;
                    if  (movetable.containsKey(idPath+STICK+"pushafter")){
                        containkey= idPath + STICK+"pushafter";
                        if(isPushedTypeMatch(classValue, movetable.get(containkey))) {
                            containpushafter = true;
                        }
                    }else if (movetable.containsKey(defaultidPath+STICK+"pushafter")){
                        containkey = defaultidPath+STICK+"pushafter";
                        if(isPushedTypeMatch(classValue, movetable.get(containkey))) {
                            containpushafter = true;
                        }
                    }
                    if (containpushafter){
                        if (hasPushafter && levelForPushAfter > 0){
                            //there is a "pushafter" action for an ancestor element.
                            //we need to push the levelForPushAfter to stack before
                            //initialize it.
                            levelForPushAfterStack.push(levelForPushAfter);
                            contentForPushAfterStack.push(contentForPushAfter);
                        }else{
                            hasPushafter = true;
                        }
                        levelForPushAfter = 0;
                        levelForPushAfter ++;
                        contentForPushAfter = replaceElementName(classValue, movetable.remove(containkey));
                        //The output for the pushcontent will be in endElement(...)
                    }
                }

                //although the if branch before checked whether isReplaced is true
                //we still need to check here because isReplaced might be turn on.
                if (!isReplaced){
                    //output the element
                    output.write(LESS_THAN);
                    output.write(name);
                    for(int index = 0; index < atts.getLength(); index++){
                        output.write(STRING_BLANK);
                        output.write(atts.getQName(index));
                        output.write("=\"");
                        String value =  atts.getValue(index);
                        value =  StringUtils.escapeXML(value);
                        output.write(value);
                        output.write("\"");
                    }
                    output.write(GREATER_THAN);
                }
            }catch (final Exception e) {
                logger.logError(e.getMessage(), e) ;
            }
        }
    }


    @Override
    public void endDocument() throws SAXException {
        try{
            output.flush();
            output.close();
        }catch (final Exception e) {
            logger.logError(e.getMessage(), e) ;
        }finally{
            try{
                output.close();
            }catch (final Exception e) {
                logger.logError(e.getMessage(), e) ;
            }
        }
    }

    @Override
    public void ignorableWhitespace(final char[] ch, final int start, final int length)
            throws SAXException {
        if(!isReplaced){
            try{
                output.write(ch, start, length);
            }catch (final Exception e) {
                logger.logError(e.getMessage(), e) ;
            }
        }
    }

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
    }

    @Override
    public void startEntity(final String name) throws SAXException {
        try {
            needResolveEntity = StringUtils.checkEntity(name);
            if(!needResolveEntity){
                output.write(StringUtils.getEntity(name));
            }
        } catch (final Exception e) {
            //logger.logError(e.getMessage(), e) ;
        }
    }

    @Override
    public void endEntity(final String name) throws SAXException {
        if(!needResolveEntity){
            needResolveEntity = true;
        }
    }

}
