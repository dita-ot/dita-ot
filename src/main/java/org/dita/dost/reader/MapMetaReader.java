/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2007 All Rights Reserved.
 */
package org.dita.dost.reader;

import static org.dita.dost.util.Constants.*;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
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
import org.dita.dost.module.ContentImpl;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.StringUtils;
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

    private static final Set<String> uniqueSet;

    static{
        uniqueSet = new HashSet<String>(INT_16);
        uniqueSet.add(TOPIC_CRITDATES.matcher);
        uniqueSet.add(TOPIC_PERMISSIONS.matcher);
        uniqueSet.add(TOPIC_PUBLISHER.matcher);
        uniqueSet.add(TOPIC_SOURCE.matcher);
        uniqueSet.add(MAP_SEARCHTITLE.matcher);
    }

    private static final Set<String> cascadeSet;

    static{
        cascadeSet = new HashSet<String>(INT_16);
        cascadeSet.add(TOPIC_AUDIENCE.matcher);
        cascadeSet.add(TOPIC_AUTHOR.matcher);
        cascadeSet.add(TOPIC_CATEGORY.matcher);
        cascadeSet.add(TOPIC_COPYRIGHT.matcher);
        cascadeSet.add(TOPIC_CRITDATES.matcher);
        cascadeSet.add(TOPIC_PERMISSIONS.matcher);
        cascadeSet.add(TOPIC_PRODINFO.matcher);
        cascadeSet.add(TOPIC_PUBLISHER.matcher);
    }

    private static final Set<String> metaSet;

    static{
        metaSet = new HashSet<String>(INT_16);
        metaSet.add(MAP_SEARCHTITLE.matcher);
        metaSet.add(TOPIC_AUTHOR.matcher);
        metaSet.add(TOPIC_SOURCE.matcher);
        metaSet.add(TOPIC_PUBLISHER.matcher);
        metaSet.add(TOPIC_COPYRIGHT.matcher);
        metaSet.add(TOPIC_CRITDATES.matcher);
        metaSet.add(TOPIC_PERMISSIONS.matcher);
        metaSet.add(TOPIC_AUDIENCE.matcher);
        metaSet.add(TOPIC_CATEGORY.matcher);
        metaSet.add(TOPIC_KEYWORDS.matcher);
        metaSet.add(TOPIC_PRODINFO.matcher);
        metaSet.add(TOPIC_OTHERMETA.matcher);
        metaSet.add(TOPIC_RESOURCEID.matcher);
        metaSet.add(TOPIC_DATA.matcher);
        metaSet.add(TOPIC_DATA_ABOUT.matcher);
        metaSet.add(TOPIC_FOREIGN.matcher);
        metaSet.add(TOPIC_UNKNOWN.matcher);
    }

    private static final Vector<String> metaPos;

    static {
        metaPos = new Vector<String>(INT_16);
        metaPos.add(MAP_SEARCHTITLE.matcher);
        metaPos.add(TOPIC_AUTHOR.matcher);
        metaPos.add(TOPIC_SOURCE.matcher);
        metaPos.add(TOPIC_PUBLISHER.matcher);
        metaPos.add(TOPIC_COPYRIGHT.matcher);
        metaPos.add(TOPIC_CRITDATES.matcher);
        metaPos.add(TOPIC_PERMISSIONS.matcher);
        metaPos.add(TOPIC_AUDIENCE.matcher);
        metaPos.add(TOPIC_CATEGORY.matcher);
        metaPos.add(TOPIC_KEYWORDS.matcher);
        metaPos.add(TOPIC_PRODINFO.matcher);
        metaPos.add(TOPIC_OTHERMETA.matcher);
        metaPos.add(TOPIC_RESOURCEID.matcher);
        metaPos.add(TOPIC_DATA.matcher);
        metaPos.add(TOPIC_DATA_ABOUT.matcher);
        metaPos.add(TOPIC_FOREIGN.matcher);
        metaPos.add(TOPIC_UNKNOWN.matcher);
        //Added by William on 2009-07-25 for bug:2826143 start
        metaPos.add(MAP_LINKTEXT.matcher);
        metaPos.add(MAP_SHORTDESC.matcher);
        //Added by William on 2009-07-25 for bug:2826143 end
        //Added by William on 2009-12-21 for bug:2916469 start
        metaPos.add(TOPIC_NAVTITLE.matcher);
        metaPos.add(TOPIC_METADATA.matcher);
        metaPos.add(DELAY_D_EXPORTANCHORS.matcher);
        //Added by William on 2009-12-21 for bug:2916469 end
    }

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

            // Fix bug on SourceForge ID:#2891736
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
            logger.logException(e);
        }
    }

    public void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
    }

    //added by Alan for bug ID:#2891736 on Date: 2009-11-16 begin
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
    //added by Alan for bug ID:#2891736 on Date: 2009-11-16 end

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
            if (copytoAttr != null && new File(FileUtils.resolveFile(filePath, copytoAttr.getNodeValue())).exists()){
                // if there is @copy-to and the file exists, @copy-to will take the place of @href
                topicPath = FileUtils.resolveTopic(filePath,copytoAttr.getNodeValue());
            }else{
                // if there is no copy-to attribute in current element
                topicPath = FileUtils.resolveTopic(filePath,hrefAttr.getNodeValue());
            }

            //edited by william on 2009-08-06 for bug:2832696 start
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
            //edited by william on 2009-08-06 for bug:2832696 end
        }
    }
    private Hashtable<String, Element> cloneElementMap(final Hashtable<String, Element> current) {
        final Hashtable<String, Element> topicMetaTable = new Hashtable<String, Element>(INT_16);
        for (final Entry<String, Element> topicMetaItem: current.entrySet()) {
            final Element inheritStub = doc.createElement("stub");
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
                    final Element stub = doc.createElement("stub");
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
                    final Element stub = doc.createElement("stub");
                    stub.appendChild(node.cloneNode(true));
                    globalMeta.put(metaKey, stub);
                }
            }
        }

    }

    /**
     * @return content collection {@code Set<Entry<String, Hashtable<String, Element>>>}
     */
    public Content getContent() {
        final ContentImpl result = new ContentImpl();
        result.setCollection( resultTable.entrySet());
        return result;
    }


}
