/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2005 All Rights Reserved.
 */

package org.dita.dost.reader;

import static org.apache.commons.io.FilenameUtils.*;
import static org.dita.dost.util.Constants.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.dita.dost.index.IndexTerm;
import org.dita.dost.index.IndexTermCollection;
import org.dita.dost.index.IndexTermTarget;
import org.dita.dost.index.TopicrefElement;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * This class extends SAX's DefaultHandler, used for parsing indexterm in
 * ditamap.
 * 
 * @version 1.0 2005-04-30
 * 
 * @author Wu, Zhi Qiang
 */

public final class DitamapIndexTermReader extends AbstractXMLReader {
    /** The stack used to store elements */
    private final Stack<Object> elementStack;

    /** List used to store all the specialized index terms */
    private final List<String> indexTermSpecList;

    /** List used to store all the specialized topicref tags */
    private final List<String> topicrefSpecList;

    /** List used to store all the specialized index-see tags */
    private final List<String> indexSeeSpecList;

    /** List used to store all the specialized index-see-also tags */
    private final List<String> indexSeeAlsoSpecList;

    private String mapPath = null;

    private final IndexTermCollection result;
    // assumes index terms have been moved by preprocess
    private boolean indexMoved = true;

    public DitamapIndexTermReader(final IndexTermCollection result, final boolean indexMoved) {
        super();
		elementStack = new Stack<>();
		indexTermSpecList = new ArrayList<>(16);
		topicrefSpecList = new ArrayList<>(16);
		indexSeeSpecList = new ArrayList<>(16);
		indexSeeAlsoSpecList = new ArrayList<>(16);
		this.result = result != null ? result : IndexTermCollection.getInstantce();
        this.indexMoved = indexMoved;
    }

    @Override
    public void characters(final char[] ch, final int start, final int length)
            throws SAXException {
        String temp = new String(ch, start, length);
        IndexTerm indexTerm = null;
        //boolean withSpace = (ch[start] == '\n' || temp.startsWith(LINE_SEPARATOR));
        if (ch[start] == '\n' || temp.startsWith(LINE_SEPARATOR)) {
            temp = " " + temp.substring(1);
        }

        //		if (temp.length() == 0) {
        //			return;
        //		}

        //used for store the space
        final char[] chars = temp.toCharArray();
        char flag = '\n';
        //used for store the new String
        final StringBuilder sb = new StringBuilder();
        for(final char c : chars){
            //when a whitespace is met
            if(c==' '){
                //this is the first whitespace
                if(flag!=' '){
                    //put it in the result string
                    sb.append(c);
                    //store the space in the flag
                    flag = c;
                }else{
                    //abundant space, ignore it
                }
                //the consecutive whitespace is interrupted
            }else{
                //put it in the result string
                sb.append(c);
                //clear the flag
                flag = c;
            }
        }
        temp = sb.toString();

        if (elementStack.empty() || !(elementStack.peek() instanceof IndexTerm)) {
            return;
        }

        indexTerm = (IndexTerm) elementStack.peek();

        indexTerm.setTermName(StringUtils.setOrAppend(indexTerm.getTermName(), temp, false));

    }

    @Override
    public void endElement(final String uri, final String localName, final String qName)
            throws SAXException {
        if (topicrefSpecList.contains(localName)) {
            elementStack.pop();
            return;
        }

        // check to see it the indexterm element or a specialized version is
        // in the list.
        if (indexTermSpecList.contains(localName) && needPushTerm()) {
            final IndexTerm indexTerm = (IndexTerm) elementStack.pop();
            Object obj = null;

            if (indexTerm.getTermName() == null || indexTerm.getTermName().trim().equals("")) {
                if(indexTerm.getEndAttribute() != null && !indexTerm.hasSubTerms()){
                    return;
                }else{
                    indexTerm.setTermName("***");
                    logger.warn(MessageUtils.getInstance().getMessage("DOTJ014W").toString());
                }
            }

            if(indexTerm.getTermKey() == null){
                indexTerm.setTermKey(indexTerm.getTermName());
            }

            obj = elementStack.peek();

            if (obj instanceof TopicrefElement) {
                if(((TopicrefElement)obj).getHref()!=null){
                    genTargets(indexTerm, (TopicrefElement)obj);
                    //IndexTermCollection.getInstantce().addTerm(indexTerm);
                    result.addTerm(indexTerm);
                }
            } else {
                final IndexTerm parentTerm = (IndexTerm) obj;
                parentTerm.addSubTerm(indexTerm);
            }
        }

        // Check to see if the index-see or index-see-also or a specialized
        // version is in the list.
        if (indexSeeSpecList.contains(localName)
                || indexSeeAlsoSpecList.contains(localName)) {
            final IndexTerm term = (IndexTerm) elementStack.pop();
            if (term.getTermKey() == null) {
                term.setTermKey(term.getTermFullName());
            }
            if (elementStack.peek() instanceof IndexTerm){
                final IndexTerm parentTerm = (IndexTerm) elementStack.peek();
                parentTerm.addSubTerm(term);
            }
        }
    }

    private void genTargets(final IndexTerm indexTerm, final TopicrefElement obj) {

        final IndexTermTarget target = new IndexTermTarget();
        String targetURI = null;

        final String href = obj.getHref();

        final StringBuilder buffer = new StringBuilder();
        if (!href.contains(COLON_DOUBLE_SLASH) && !FileUtils.isAbsolutePath(href)){
            if (mapPath != null && mapPath.length() != 0) {
                buffer.append(mapPath);
                buffer.append(SLASH);
            }
            buffer.append(href);
            targetURI = new File(normalize(buffer.toString())).getPath();
        }else{
            targetURI = href;
        }

        if (obj.getNavTitle() != null){
            target.setTargetName(obj.getNavTitle());
        }else {
            target.setTargetName(href);
        }

        target.setTargetURI(targetURI);

        assignTarget(indexTerm, target);

    }

    private void assignTarget(final IndexTerm indexTerm, final IndexTermTarget target) {
        if (indexTerm.isLeaf()){
            indexTerm.addTarget(target);
        }

        if (indexTerm.hasSubTerms()){
            for (final Object subTerm : indexTerm.getSubTerms()){
                assignTarget((IndexTerm)subTerm, target);
            }
        }
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName,
            final Attributes attributes) throws SAXException {
        final String classAttr = attributes.getValue(ATTRIBUTE_NAME_CLASS);

        if (classAttr != null
                && TOPIC_INDEXTERM.matches(classAttr)) {
            // add the element name to the indexterm specialization element
            // list if it does not already exist in that list.
            if (!indexTermSpecList.contains(localName)){
                indexTermSpecList.add(localName);
            }
        }

        if (classAttr != null
                && MAP_TOPICREF.matches(classAttr)){
            if (!topicrefSpecList.contains(localName)){
                topicrefSpecList.add(localName);
            }
        }

        if (classAttr != null
                && INDEXING_D_INDEX_SEE.matches(classAttr)){
            if (!indexSeeSpecList.contains(localName)){
                indexSeeSpecList.add(localName);
            }
        }

        if (classAttr != null
                && INDEXING_D_INDEX_SEE_ALSO.matches(classAttr)){
            if (!indexSeeAlsoSpecList.contains(localName)){
                indexSeeAlsoSpecList.add(localName);
            }
        }

        if (topicrefSpecList.contains(localName)) {
            final String href = attributes.getValue(ATTRIBUTE_NAME_HREF);
            final String format = attributes
                    .getValue(ATTRIBUTE_NAME_FORMAT);
            final String navtitle =  attributes.getValue(ATTRIBUTE_NAME_NAVTITLE);
            final TopicrefElement topicref = new TopicrefElement();

            topicref.setHref(href);
            topicref.setFormat(format);
            topicref.setNavTitle(navtitle);
            elementStack.push(topicref);

            return;
        }

        parseIndexTerm(localName, attributes);
        parseIndexSee(localName);
        parseIndexSeeAlso(localName);

    }

    private void parseIndexSeeAlso(final String localName) {
        // check to see it the index-see-also element or a specialized version
        // is in the list.
        if (indexSeeAlsoSpecList.contains(localName)
                && needPushTerm()) {
            final IndexTerm indexTerm = new IndexTerm();
            IndexTerm parentTerm = null;
            if(!elementStack.isEmpty()
                    && elementStack.peek() instanceof IndexTerm){
                parentTerm = (IndexTerm)elementStack.peek();
                if(parentTerm.hasSubTerms()){
                    parentTerm.updateSubTerm();
                }
            }
            indexTerm.setTermPrefix(IndexTerm_Prefix_See_Also);
            elementStack.push(indexTerm);
        }
    }

    private void parseIndexSee(final String localName) {
        // check to see it the index-see element or a specialized version is
        // in the list.
        if (indexSeeSpecList.contains(localName)
                && needPushTerm()) {
            final IndexTerm indexTerm = new IndexTerm();
            IndexTerm parentTerm = null;

            indexTerm.setTermPrefix(IndexTerm_Prefix_See);

            if(!elementStack.isEmpty()
                    && elementStack.peek() instanceof IndexTerm){
                parentTerm = (IndexTerm)elementStack.peek();
                if(parentTerm.hasSubTerms()){
                    parentTerm.updateSubTerm();
                    indexTerm.setTermPrefix(IndexTerm_Prefix_See_Also);
                }
            }
            elementStack.push(indexTerm);
        }
    }

    private void parseIndexTerm(final String localName, final Attributes attributes) {
        // check to see it the indexterm element or a specialized version is
        // in the list.
        if (indexTermSpecList.contains(localName) && needPushTerm()) {
            final IndexTerm indexTerm = new IndexTerm();
            indexTerm.setStartAttribute(attributes.getValue(ATTRIBUTE_NAME_END));
            indexTerm.setEndAttribute(attributes.getValue(ATTRIBUTE_NAME_END));
            IndexTerm parentTerm = null;
            if(!elementStack.isEmpty()
                    && elementStack.peek() instanceof IndexTerm){
                parentTerm = (IndexTerm)elementStack.peek();
                if(parentTerm.hasSubTerms()){
                    parentTerm.updateSubTerm();
                }
            }
            elementStack.push(indexTerm);
        }
    }

    /**
     * Check element stack for the root topicref or indexterm element.
     */
    private boolean needPushTerm() {
        if (elementStack.empty()) {
            return false;
        }


        if (elementStack.peek() instanceof TopicrefElement) {
            //			if (!FileUtils.isHTMLFile(((TopicrefElement) elementStack.peek()).getHref())){ //Eric
            //				return false;
            //			}
            //			return ((TopicrefElement) elementStack.peek()).needExtractTerm();
            // for dita files the indexterm has been moved to its <prolog>
            // therefore we don't need to collect these terms again.
            final TopicrefElement elem = (TopicrefElement) elementStack.peek();
            if (indexMoved && (elem.getFormat() == null || elem.getFormat().equals(ATTR_FORMAT_VALUE_DITA) || elem.getFormat().equals(ATTR_FORMAT_VALUE_DITAMAP))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Set map path.
     * 
     * @param mappath path of map file
     */
    public void setMapPath(final String mappath) {
        mapPath = mappath;
    }

}
