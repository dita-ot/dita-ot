/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2007 All Rights Reserved.
 */
package org.dita.dost.reader;

import static java.util.Arrays.*;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.module.GenMapAndTopicListModule.*;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.dita.dost.exception.DITAOTXMLErrorHandler;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.module.Content;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.StringUtils;
import org.dita.dost.util.URLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
/**
 * MapMetaReader class which reads map meta data.
 *
 */
public final class MapMetaReader implements AbstractReader {
    private static final String INTERNET_LINK_MARK = COLON_DOUBLE_SLASH;

    private final Hashtable<String, Hashtable<String, Element>> resultTable = new Hashtable<String, Hashtable<String, Element>>(INT_16);

    public static final Set<String> uniqueSet = Collections.unmodifiableSet(new HashSet<String>(asList(
            TOPIC_CRITDATES.matcher,
            TOPIC_PERMISSIONS.matcher,
            TOPIC_PUBLISHER.matcher,
            TOPIC_SOURCE.matcher,
            MAP_SEARCHTITLE.matcher
            )));
    private static final Set<String> cascadeSet = Collections.unmodifiableSet(new HashSet<String>(asList(
            TOPIC_AUDIENCE.matcher,
            TOPIC_AUTHOR.matcher,
            TOPIC_CATEGORY.matcher,
            TOPIC_COPYRIGHT.matcher,
            TOPIC_CRITDATES.matcher,
            TOPIC_PERMISSIONS.matcher,
            TOPIC_PRODINFO.matcher,
            TOPIC_PUBLISHER.matcher
            )));
    private static final Set<String> metaSet = Collections.unmodifiableSet(new HashSet<String>(asList(
            MAP_SEARCHTITLE.matcher,
            TOPIC_AUTHOR.matcher,
            TOPIC_SOURCE.matcher,
            TOPIC_PUBLISHER.matcher,
            TOPIC_COPYRIGHT.matcher,
            TOPIC_CRITDATES.matcher,
            TOPIC_PERMISSIONS.matcher,
            TOPIC_AUDIENCE.matcher,
            TOPIC_CATEGORY.matcher,
            TOPIC_KEYWORDS.matcher,
            TOPIC_PRODINFO.matcher,
            TOPIC_OTHERMETA.matcher,
            TOPIC_RESOURCEID.matcher,
            TOPIC_DATA.matcher,
            TOPIC_DATA_ABOUT.matcher,
            TOPIC_FOREIGN.matcher,
            TOPIC_UNKNOWN.matcher
            )));
    private static final List<String> metaPos = Collections.unmodifiableList(asList(
            MAP_SEARCHTITLE.matcher,
            TOPIC_AUTHOR.matcher,
            TOPIC_SOURCE.matcher,
            TOPIC_PUBLISHER.matcher,
            TOPIC_COPYRIGHT.matcher,
            TOPIC_CRITDATES.matcher,
            TOPIC_PERMISSIONS.matcher,
            TOPIC_AUDIENCE.matcher,
            TOPIC_CATEGORY.matcher,
            TOPIC_KEYWORDS.matcher,
            TOPIC_PRODINFO.matcher,
            TOPIC_OTHERMETA.matcher,
            TOPIC_RESOURCEID.matcher,
            TOPIC_DATA.matcher,
            TOPIC_DATA_ABOUT.matcher,
            TOPIC_FOREIGN.matcher,
            TOPIC_UNKNOWN.matcher,
            MAP_LINKTEXT.matcher,
            MAP_SHORTDESC.matcher,
            TOPIC_NAVTITLE.matcher,
            TOPIC_METADATA.matcher,
            DELAY_D_EXPORTANCHORS.matcher
            ));

    private DITAOTLogger logger;

    private final Hashtable<String, Element> globalMeta;

    private Document doc = null;

    private String filePath = null;


    /**
     * Constructor.
     */
    public MapMetaReader() {
        super();
        globalMeta = new Hashtable<String, Element>(INT_16);
        resultTable.clear();
    }
    /**
     * read map files.
     * @param filename filename
     */
    @Override
    public void read(final String filename) {
        final File inputFile = new File(filename);
        filePath = inputFile.getParent();
        inputFile.getPath();

        //clear the history on global metadata table
        globalMeta.clear();


        try{
            final DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            final DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setErrorHandler(new DITAOTXMLErrorHandler(filename, logger));
            doc = builder.parse(inputFile);

            final Element root = doc.getDocumentElement();
            final NodeList list = root.getChildNodes();
            for (int i = 0; i < list.getLength(); i++){
                final Node node = list.item(i);
                Node classAttr = null;
                if (node.getNodeType() == Node.ELEMENT_NODE){
                    classAttr = node.getAttributes().getNamedItem(ATTRIBUTE_NAME_CLASS);
                }
                if(classAttr != null && MAP_TOPICMETA.matches(classAttr.getNodeValue())){
                    //if this node is topicmeta node under root
                    handleGlobalMeta(node);
                }else if(classAttr != null && MAP_TOPICREF.matches(classAttr.getNodeValue())){
                    //if this node is topicref node under root
                    handleTopicref(node, globalMeta);
                }
            }

            // Indexterm elements with either start or end attribute should not been
            // move to referenced dita file's prolog section.
            // <!--start
            for (final Hashtable<String, Element> resultTableEntry : resultTable.values()) {
                for (final Map.Entry<String, Element> mapEntry : resultTableEntry.entrySet()) {
                    final String key = mapEntry.getKey();
                    if (TOPIC_KEYWORDS.matcher.equals(key)) {
                        removeIndexTermRecursive(mapEntry.getValue());
                    }
                }
            }
            // end -->

            FileOutputStream file = null;
            try {
                file = new FileOutputStream(inputFile.getCanonicalPath()+ ".temp");
                final StreamResult res = new StreamResult(file);
                final DOMSource ds = new DOMSource(doc);
                final TransformerFactory tff = TransformerFactory.newInstance();
                final Transformer tf = tff.newTransformer();
                tf.transform(ds, res);
            } finally {
                if (file != null) {
                    file.close();
                }
            }

        }catch (final Exception e){
            logger.logError(e.getMessage(), e) ;
        }
    }

    @Override
    public void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
    }

    /**
     * traverse the node tree and remove all indexterm elements with either start or
     * end attribute.
     * @param parent root element
     */
    private void removeIndexTermRecursive(final Element parent) {
        if (parent == null) {
            return;
        }
        final NodeList children = parent.getChildNodes();
        Element child = null;
        for (int i = 0; i < children.getLength(); i++) {
            if(children.item(i).getNodeType() == Node.ELEMENT_NODE){
                child = (Element) children.item(i);
                final boolean isIndexTerm = TOPIC_INDEXTERM.matches(child.getAttribute(ATTRIBUTE_NAME_CLASS));
                final boolean hasStart = !StringUtils.isEmptyString(child.getAttribute(ATTRIBUTE_NAME_START));
                final boolean hasEnd = !StringUtils.isEmptyString(child.getAttribute(ATTRIBUTE_NAME_END));

                if(isIndexTerm && (hasStart || hasEnd)){
                    parent.removeChild(child);
                } else{
                    removeIndexTermRecursive(child);
                }
            }
        }
    }

    private void handleTopicref(final Node topicref, final Hashtable<String, Element> inheritance) {
        final Node hrefAttr = topicref.getAttributes().getNamedItem(ATTRIBUTE_NAME_HREF);
        final Node copytoAttr = topicref.getAttributes().getNamedItem(ATTRIBUTE_NAME_COPY_TO);
        final Node scopeAttr = topicref.getAttributes().getNamedItem(ATTRIBUTE_NAME_SCOPE);
        final Node formatAttr = topicref.getAttributes().getNamedItem(ATTRIBUTE_NAME_FORMAT);
        Hashtable<String, Element> current = mergeMeta(null,inheritance,cascadeSet);
        String topicPath = null;
        Node metaNode = null;

        final NodeList children = topicref.getChildNodes();
        for (int i = 0; i < children.getLength(); i++){
            final Node node = children.item(i);
            Node classAttr = null;
            if(node.getNodeType() == Node.ELEMENT_NODE){
                classAttr = node.getAttributes().getNamedItem(ATTRIBUTE_NAME_CLASS);
            }

            if(classAttr != null && hrefAttr != null &&
                    MAP_TOPICMETA.matches(classAttr.getNodeValue()) &&
                    hrefAttr != null && hrefAttr.getNodeValue().indexOf(INTERNET_LINK_MARK) == -1
                    && (scopeAttr == null || ATTR_SCOPE_VALUE_LOCAL.equalsIgnoreCase(scopeAttr.getNodeValue()))
                    && ((formatAttr == null || ATTR_FORMAT_VALUE_DITA.equalsIgnoreCase(formatAttr.getNodeValue()))
                            || (formatAttr == null || ATTR_FORMAT_VALUE_DITAMAP.equalsIgnoreCase(formatAttr.getNodeValue())))
                    ){
                //if this node is topicmeta and the parent topicref refers to a valid dita topic
                metaNode = node;
                current = handleMeta(node, inheritance);

            }else if(classAttr != null &&
                    MAP_TOPICREF.matches(classAttr.getNodeValue())){
                //if this node is topicref node under topicref
                handleTopicref(node, current);
            }
        }

        if (!current.isEmpty() && hrefAttr != null){// prevent the metadata is empty
            if (copytoAttr != null && new File(FileUtils.resolveFile(filePath, URLUtils.decode(copytoAttr.getNodeValue()))).exists()){
                // if there is @copy-to and the file exists, @copy-to will take the place of @href
                topicPath = FileUtils.resolveTopic(filePath, URLUtils.decode(copytoAttr.getNodeValue()));
            }else{
                // if there is no copy-to attribute in current element
                topicPath = FileUtils.resolveTopic(filePath, URLUtils.decode(hrefAttr.getNodeValue()));
            }

            if(((formatAttr == null || ATTR_FORMAT_VALUE_DITA.equalsIgnoreCase(formatAttr.getNodeValue()))||(formatAttr == null || ATTR_FORMAT_VALUE_DITAMAP.equalsIgnoreCase(formatAttr.getNodeValue())))
                    &&(scopeAttr == null || ATTR_SCOPE_VALUE_LOCAL.equalsIgnoreCase(scopeAttr.getNodeValue()))
                    &&(hrefAttr.getNodeValue().indexOf(INTERNET_LINK_MARK) == -1)){
                if(resultTable.containsKey(topicPath)){
                    //if the result table already contains some result
                    //metadata for current topic path.
                    final Hashtable<String, Element> previous = resultTable.get(topicPath);
                    resultTable.put(topicPath, mergeMeta(previous, current, metaSet));
                }else{

                    resultTable.put(topicPath, cloneElementMap(current));

                }

                final Hashtable<String, Element> metas = resultTable.get(topicPath);
                if (!metas.isEmpty()) {
                    if (metaNode != null) {
                        topicref.removeChild(metaNode);
                    }
                    final Element newMeta = doc.createElement(MAP_TOPICMETA.localName);
                    newMeta.setAttribute(ATTRIBUTE_NAME_CLASS, "-" + MAP_TOPICMETA.matcher);
                    for (int i = 0; i < metaPos.size(); i++) {
                        final Node stub = metas.get(metaPos.get(i));
                        if (stub != null) {
                            final NodeList clist = stub.getChildNodes();
                            for (int j = 0; j < clist.getLength(); j++) {
                                newMeta.appendChild(topicref.getOwnerDocument().importNode(clist.item(j), true));
                            }
                        }
                    }
                    topicref.insertBefore(
                            newMeta,
                            topicref.getFirstChild());
                }
            }
        }
    }
    private Hashtable<String, Element> cloneElementMap(final Hashtable<String, Element> current) {
        final Hashtable<String, Element> topicMetaTable = new Hashtable<String, Element>(INT_16);
        for (final Entry<String, Element> topicMetaItem: current.entrySet()) {
            final Element inheritStub = doc.createElement(ELEMENT_STUB);
            final Node currentStub = topicMetaItem.getValue();
            final NodeList stubChildren = currentStub.getChildNodes();
            for (int i = 0; i < stubChildren.getLength(); i++){
                Node item = stubChildren.item(i).cloneNode(true);
                item = inheritStub.getOwnerDocument().importNode(item, true);
                inheritStub.appendChild(item);
            }
            topicMetaTable.put(topicMetaItem.getKey(), inheritStub);
        }
        return topicMetaTable;
    }


    private Hashtable<String, Element> handleMeta(final Node meta, final Hashtable<String, Element> inheritance) {

        final Hashtable<String, Element> topicMetaTable = new Hashtable<String, Element>(INT_16);

        getMeta(meta, topicMetaTable);

        return mergeMeta(topicMetaTable, inheritance, cascadeSet);

    }

    private void getMeta(final Node meta, final Hashtable<String, Element> topicMetaTable){
        final NodeList children = meta.getChildNodes();
        for(int i = 0; i < children.getLength(); i++){
            final Node node = children.item(i);
            Node attr = null;
            if(node.getNodeType() == Node.ELEMENT_NODE){
                attr = node.getAttributes().getNamedItem(ATTRIBUTE_NAME_CLASS);
            }
            if (attr != null){
                final String attrValue = attr.getNodeValue();
                // int number 1 is used to remove the first "-" or "+" character in class attribute
                final String metaKey = attrValue.substring(1,
                        attrValue.indexOf(STRING_BLANK,attrValue.indexOf(SLASH))+1 );
                if (TOPIC_METADATA.matches(attrValue)){
                    getMeta(node, topicMetaTable);
                }else if(topicMetaTable.containsKey(metaKey)){
                    //append node to the list if it exist in topic meta table
                    //use clone here to prevent the node is removed from original DOM tree;
                    topicMetaTable.get(metaKey).appendChild(node.cloneNode(true));
                } else{
                    final Element stub = doc.createElement(ELEMENT_STUB);
                    // use clone here to prevent the node is removed from original DOM tree;
                    stub.appendChild(node.cloneNode(true));
                    topicMetaTable.put(metaKey, stub);
                }
            }
        }
    }

    private Hashtable<String, Element> mergeMeta(Hashtable<String, Element> topicMetaTable,
            final Hashtable<String, Element> inheritance, final Set<String> enableSet) {

        // When inherited metadata need to be merged into current metadata
        // enableSet should be cascadeSet so that only metadata that can
        // be inherited are merged.
        // Otherwise enableSet should be metaSet in order to merge all
        // metadata.
        if (topicMetaTable == null){
            topicMetaTable = new Hashtable<String, Element>(INT_16);
        }
        Node item = null;
        final Iterator<String> iter = enableSet.iterator();
        while (iter.hasNext()){
            final String key = iter.next();
            if (inheritance.containsKey(key)){
                if(uniqueSet.contains(key) ){
                    if(!topicMetaTable.containsKey(key)){
                        topicMetaTable.put(key, inheritance.get(key));
                    }

                }else{  // not unique metadata

                    if(!topicMetaTable.containsKey(key)){
                        topicMetaTable.put(key, inheritance.get(key));
                    }else{
                        //not necessary to do node type check here
                        //because inheritStub doesn't contains any node
                        //other than Element.
                        final Node stub = topicMetaTable.get(key);
                        final Node inheritStub = inheritance.get(key);
                        if (stub != inheritStub){
                            // Merge the value if stub does not equal to inheritStub
                            // Otherwise it will get into infinitive loop
                            final NodeList children = inheritStub.getChildNodes();
                            for(int i = 0; i < children.getLength(); i++){
                                item = children.item(i).cloneNode(true);
                                item = stub.getOwnerDocument().importNode(item,true);
                                stub.appendChild(item);
                            }
                        }

                        topicMetaTable.put(key, (Element)stub);
                    }
                }
            }
        }
        return topicMetaTable;
    }

    private void handleGlobalMeta(final Node metadata) {

        final NodeList children = metadata.getChildNodes();
        for(int i = 0; i < children.getLength(); i++){
            final Node node = children.item(i);
            Node attr = null;
            if (node.getNodeType() == Node.ELEMENT_NODE){
                attr = node.getAttributes().getNamedItem(ATTRIBUTE_NAME_CLASS);
            }
            if (attr != null){
                final String attrValue = attr.getNodeValue();
                final String metaKey = attrValue.substring(1,
                        attrValue.indexOf(STRING_BLANK,attrValue.indexOf(SLASH))+1 );
                if (TOPIC_METADATA.matches(attrValue)){
                    //proceed the metadata in <metadata>
                    handleGlobalMeta(node);
                }else if(cascadeSet.contains(metaKey) && globalMeta.containsKey(metaKey)){
                    //append node to the list if it exist in global meta table
                    //use clone here to prevent the node is removed from original DOM tree;
                    globalMeta.get(metaKey).appendChild(node.cloneNode(true));
                } else if(cascadeSet.contains(metaKey)){
                    final Element stub = doc.createElement(ELEMENT_STUB);
                    stub.appendChild(node.cloneNode(true));
                    globalMeta.put(metaKey, stub);
                }
            }
        }

    }

    /**
     * @deprecated use {@link #getMapping()} instead
     */
    @Override
    @Deprecated
    public Content getContent() {
        throw new UnsupportedOperationException();
    }

    /**
     * Get metadata for topics
     * 
     * @return map of metadata by topic path
     */
    public Map<String, Hashtable<String, Element>> getMapping() {
    	return Collections.unmodifiableMap(resultTable);
    } 

}
