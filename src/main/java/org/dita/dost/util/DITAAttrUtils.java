/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.util;

import static org.dita.dost.util.Constants.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.dita.dost.log.DITAOTJavaLogger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This util is used for check attributes/nodes of elements.
 * @author william
 *
 */
public final class DITAAttrUtils {

    /** List of print transtypes. */
    private static final List<String> printTranstype;
    static {
        final List<String> types = new ArrayList<String>();
        final String printTranstypes = Configuration.configuration.get(CONF_PRINT_TRANSTYPES);
        if (printTranstypes != null) {
            if (printTranstypes.trim().length() > 0) {
                for (final String transtype: printTranstypes.split(CONF_LIST_SEPARATOR)) {
                    types.add(transtype.trim());
                }
            }
        } else {
            new DITAOTJavaLogger().logError("Failed to read print transtypes from configuration, using defaults.");
            types.add(TRANS_TYPE_PDF);
        }
        printTranstype = Collections.unmodifiableList(types);
    }

    private static final List<String> excludeList;
    static {
        final List<String> el = new ArrayList<String>();
        el.add(TOPIC_INDEXTERM.toString());
        el.add(TOPIC_DRAFT_COMMENT.toString());
        el.add(TOPIC_REQUIRED_CLEANUP.toString());
        el.add(TOPIC_DATA.toString());
        el.add(TOPIC_DATA_ABOUT.toString());
        el.add(TOPIC_UNKNOWN.toString());
        el.add(TOPIC_FOREIGN.toString());
        excludeList = Collections.unmodifiableList(el);
    }

    //Depth inside element for @print.
    /*e.g for <a print="yes">
     *            <b/>
     *        </a>
     * tag b's printLevel is 2
     */
    private int printLevel;

    private static DITAAttrUtils util = new DITAAttrUtils();
    /**
     * Constructor.
     */
    private DITAAttrUtils() {
        printLevel = 0;
    }
    /**
     * Get an instance.
     * @return an instance.
     */
    public static DITAAttrUtils getInstance(){

        return util;
    }
    /**
     * Increase print level.
     * @param printValue value of print attribute.
     * @return whether the level is increased.
     */
    public boolean increasePrintLevel(final String printValue){

        if(printValue != null){
            //@print = "printonly"
            if(ATTR_PRINT_VALUE_PRINT_ONLY.equals(printValue)){
                printLevel ++ ;
                return true;
                //descendant elements
            }else if(printLevel > 0){
                printLevel ++ ;
                return true;
            }
            //@print not set but is descendant tag of "printonly"
        }else if(printLevel > 0){
            printLevel ++ ;
            return true;
        }

        return false;

    }
    /**
     * Decrease print level.
     * @return boolean
     */
    public boolean decreasePrintLevel(){
        if(printLevel > 0){
            printLevel --;
            return true;
        }else{
            return false;
        }
    }
    /**
     * Check whether need to skip for @print.
     * @param transtype String
     * @return boolean
     */
    public boolean needExcludeForPrintAttri(final String transtype){

        if(printLevel > 0 && !printTranstype.contains(transtype)){
            return true;
        }else{
            return false;
        }

    }
    /**
     * Reset the utils.
     */
    public void reset(){

        printLevel = 0;

    }
    /**
     * Search for the special kind of node by specialized value.
     * @param root place may have the node.
     * @param searchKey keyword for search.
     * @param attrName attribute name for search.
     * @param classValue class value for search.
     * @return element.
     */
    public Element searchForNode(final Element root, final String searchKey, final String attrName,
            final String classValue) {
        if (root == null || StringUtils.isEmptyString(searchKey)) {
            return null;
        }
        final Queue<Element> queue = new LinkedList<Element>();
        queue.offer(root);

        while (!queue.isEmpty()) {
            final Element pe = queue.poll();
            final NodeList pchildrenList = pe.getChildNodes();
            for (int i = 0; i < pchildrenList.getLength(); i++) {
                final Node node = pchildrenList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    queue.offer((Element)node);
                }
            }
            //whick kind of node to search
            final String clazzValue = pe.getAttribute(ATTRIBUTE_NAME_CLASS);

            if (StringUtils.isEmptyString(clazzValue)
                    || !clazzValue.contains(classValue)) {
                continue;
            }

            final String value = pe.getAttribute(attrName);

            if (StringUtils.isEmptyString(value)) {
                continue;
            }

            if (searchKey.equals(value)){
                return pe;
            }else{
                continue;
            }
        }
        return null;
    }
    /**
     * Get text value of a node.
     * @param root root node
     * @return text value.
     */
    public String getText(final Node root){

        final StringBuffer result = new StringBuffer(INT_1024);

        if(root == null){
            return "";
        }else{
            if(root.hasChildNodes()){
                final NodeList list = root.getChildNodes();
                for(int i = 0; i < list.getLength(); i++){
                    final Node childNode = list.item(i);
                    if(childNode.getNodeType() == Node.ELEMENT_NODE){
                        final Element e = (Element)childNode;
                        final String value = e.getAttribute(ATTRIBUTE_NAME_CLASS);
                        if(!excludeList.contains(value)){
                            final String s = getText(e);
                            result.append(s);
                        }else{
                            continue;
                        }
                    }else if(childNode.getNodeType() == Node.TEXT_NODE){
                        result.append(childNode.getNodeValue());
                    }
                }
            }else if(root.getNodeType() == Node.TEXT_NODE){
                result.append(root.getNodeValue());
            }
        }
        return result.toString();

    }


    /**
     * get the document node of a topic file.
     * @param absolutePathToFile topic file
     * @return element.
     */
    public Element getTopicDoc(final String absolutePathToFile){
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            final Document doc = builder.parse(absolutePathToFile);
            final Element root = doc.getDocumentElement();

            return root;
        } catch (final ParserConfigurationException e) {
            e.printStackTrace();
        } catch (final SAXException e) {
            e.printStackTrace();
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return null;

    }


    /**
     * get topicmeta's child(e.g navtitle, shortdesc) tag's value(text-only).
     * @param element input element
     * @return text value
     */
    public String getChildElementValueOfTopicmeta(final Element element, final String classValue) {

        //navtitle
        String returnValue = null;
        //has child nodes
        if(element.hasChildNodes()){
            //Get topicmeta element node
            final Element topicMeta = getElementNode(element, MAP_TOPICMETA.matcher);
            //no topicmeta node
            if(topicMeta == null){
                return returnValue;
            }
            //Get element node
            final Element elem = getElementNode(topicMeta, classValue);
            //no navtitle node
            if(elem == null){
                return returnValue;
            }
            //get text value
            returnValue = this.getText(elem);
        }
        return returnValue;
    }

    /**
     * Get specific element node from child nodes.
     * @param element parent node
     * @param classValue @class
     * @return element node.
     */
    public Element getElementNode(final Element element, final String classValue) {

        //Element child = null;

        final NodeList list = element.getChildNodes();

        for(int i = 0; i < list.getLength(); i++){
            final Node node = list.item(i);
            if(node.getNodeType() == Node.ELEMENT_NODE){
                final Element child = (Element) node;
                //node found
                if(child.getAttribute(ATTRIBUTE_NAME_CLASS).contains(classValue)){
                    return child;
                    //break;
                }
            }
        }
        return null;
    }
}
