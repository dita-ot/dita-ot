package com.idiominc.ws.opentopic.fo.index2;

import static org.dita.dost.util.Constants.*;
import static javax.xml.XMLConstants.*;

import com.idiominc.ws.opentopic.fo.index2.configuration.IndexConfiguration;
import com.idiominc.ws.opentopic.fo.index2.util.IndexStringProcessor;
import com.idiominc.ws.opentopic.fo.index2.util.IndexDitaProcessor;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.*;

import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.util.Configuration;
import org.dita.dost.util.XMLUtils;

/*
Copyright (c) 2004-2006 by Idiom Technologies, Inc. All rights reserved.
IDIOM is a registered trademark of Idiom Technologies, Inc. and WORLDSERVER
and WORLDSTART are trademarks of Idiom Technologies, Inc. All other
trademarks are the property of their respective owners.

IDIOM TECHNOLOGIES, INC. IS DELIVERING THE SOFTWARE "AS IS," WITH
ABSOLUTELY NO WARRANTIES WHATSOEVER, WHETHER EXPRESS OR IMPLIED,  AND IDIOM
TECHNOLOGIES, INC. DISCLAIMS ALL WARRANTIES, EXPRESS OR IMPLIED, INCLUDING
BUT NOT LIMITED TO WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
PURPOSE AND WARRANTY OF NON-INFRINGEMENT. IDIOM TECHNOLOGIES, INC. SHALL NOT
BE LIABLE FOR INDIRECT, INCIDENTAL, SPECIAL, COVER, PUNITIVE, EXEMPLARY,
RELIANCE, OR CONSEQUENTIAL DAMAGES (INCLUDING BUT NOT LIMITED TO LOSS OF
ANTICIPATED PROFIT), ARISING FROM ANY CAUSE UNDER OR RELATED TO  OR ARISING
OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN IF IDIOM
TECHNOLOGIES, INC. HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.

Idiom Technologies, Inc. and its licensors shall not be liable for any
damages suffered by any person as a result of using and/or modifying the
Software or its derivatives. In no event shall Idiom Technologies, Inc.'s
liability for any damages hereunder exceed the amounts received by Idiom
Technologies, Inc. as a result of this transaction.

These terms and conditions supersede the terms and conditions in any
licensing agreement to the extent that such terms and conditions conflict
with those set forth herein.

This file is part of the DITA Open Toolkit project.
See the accompanying LICENSE file for applicable license.
 */
/**
 * @deprecated since 3.4
 */
@Deprecated
public final class IndexPreprocessor {
    
    /** Index term level separator. */
    public static final String VALUE_SEPARATOR = ":";
    
    private final String prefix;
    private final String namespace_url;
    private final Stack<Boolean> excludedDraftSection = new Stack<>();
    private final IndexDitaProcessor indexDitaProcessor;
    private final IndexGroupProcessor indexGroupProcessor;
    private boolean includeDraft = false;
    private DITAOTLogger logger;
    private static final String elIndexRangeStartName = "start";
    private static final String elIndexRangeEndName = "end";
    private static final String hashPrefix = "indexid";

    /**
     * Create new index preprocessor.
     * 
     * @param prefix index prefix
     * @param theNamespace_url index element namespace URI
     */
    public IndexPreprocessor(final String prefix, final String theNamespace_url, final String draftParameter) {
        this.prefix = prefix;
        this.namespace_url = theNamespace_url;
        this.excludedDraftSection.clear();
        this.excludedDraftSection.add(false);
        includeDraft = draftParameter.equals(ARGS_DRAFT_YES);
        indexDitaProcessor = new IndexDitaProcessor();
        indexGroupProcessor = new IndexGroupProcessor();
    }

    public void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
        indexDitaProcessor.setLogger(logger);
        indexGroupProcessor.setLogger(logger);
    }
    
    /**
     * Process index terms.
     * 
     * @param theInput input document
     * @return read index terms
     * @throws ProcessException if processing index terms failed
     */
    public IndexPreprocessResult process(final Document theInput)
            throws ProcessException {
        final DocumentBuilder documentBuilder = XMLUtils.getDocumentBuilder();
        final Document doc = documentBuilder.newDocument();

        final Node rootElement = theInput.getDocumentElement();


        final ArrayList<IndexEntry> indexes = new ArrayList<IndexEntry>();

        final IndexEntryFoundListener listener = new IndexEntryFoundListener() {
            public void foundEntry(final IndexEntry theEntry) {
                indexes.add(theEntry);
            }
        };

        final Node node = processCurrNode(rootElement, doc, listener)[0];

        doc.appendChild(node);

        doc.getDocumentElement().setAttribute(XMLNS_ATTRIBUTE + ":" + this.prefix, this.namespace_url);

        return new IndexPreprocessResult(doc, (IndexEntry[]) indexes.toArray(new IndexEntry[0]));
    }

    public void createAndAddIndexGroups(final IndexEntry[] theIndexEntries, final IndexConfiguration theConfiguration, final Document theDocument, final Locale theLocale) {
        final IndexComparator indexEntryComparator = new IndexComparator(theLocale);

        final IndexGroup[] indexGroups = indexGroupProcessor.process(theIndexEntries, theConfiguration, theLocale);

        final Element rootElement = theDocument.getDocumentElement();

        final Element indexGroupsElement = theDocument.createElementNS(namespace_url, "index.groups");
        indexGroupsElement.setPrefix(prefix);

        for (final IndexGroup group : indexGroups) {
            //Create group element
            final Node groupElement = theDocument.createElementNS(namespace_url, "index.group");
            groupElement.setPrefix(prefix);
            //Create group label element and index entry childs
            final Element groupLabelElement = theDocument.createElementNS(namespace_url, "label");
            groupLabelElement.setPrefix(prefix);
            groupLabelElement.appendChild(theDocument.createTextNode(group.getLabel()));
            groupElement.appendChild(groupLabelElement);

            final Node[] entryNodes = transformToNodes(group.getEntries(), theDocument, indexEntryComparator);
            for (final Node entryNode : entryNodes) {
                groupElement.appendChild(entryNode);
            }

            indexGroupsElement.appendChild(groupElement);
        }

        rootElement.appendChild(indexGroupsElement);
    }


    /**
     * Processes curr node. Copies node to the target document if its is not a text node of index entry element.
     * Otherwise it process it and creates nodes with "prefix" in given "namespace_url" from the parsed index entry text.
     *
     * @param theNode                    node to process
     * @param theTargetDocument          target document used to import and create nodes
     * @param theIndexEntryFoundListener listener to notify that new index entry was found
     * @return the array of nodes after processing input node
     */
    private Node[] processCurrNode(final Node theNode, final Document theTargetDocument, final IndexEntryFoundListener theIndexEntryFoundListener) {
        final NodeList childNodes = theNode.getChildNodes();

        if (checkElementName(theNode) && !excludedDraftSection.peek()) {
            return processIndexNode(theNode, theTargetDocument, theIndexEntryFoundListener);
        } else {
            final Node result = theTargetDocument.importNode(theNode, false);
            if (!includeDraft && checkDraftNode(theNode)) {
                excludedDraftSection.add(true);
            }
            for (int i = 0; i < childNodes.getLength(); i++) {
                final Node[] processedNodes = processCurrNode(childNodes.item(i), theTargetDocument, theIndexEntryFoundListener);
                for (final Node node : processedNodes) {
                    result.appendChild(node);
                }
            }
            if (!includeDraft && checkDraftNode(theNode)) {
                excludedDraftSection.pop();
            }
            return new Node[]{result};
        }
    }

    private Node[] processIndexNode(final Node theNode, final Document theTargetDocument, final IndexEntryFoundListener theIndexEntryFoundListener) {
        theNode.normalize();

        boolean ditastyle = false;
        String textNode = null;

        final NodeList childNodes = theNode.getChildNodes();
        final StringBuilder textBuf = new StringBuilder();
        final List<Node> contents = new ArrayList<Node>();
        for (int i = 0; i < childNodes.getLength(); i++) {
            final Node child = childNodes.item(i);
            if (checkElementName(child)) {
                ditastyle = true;
                break;
            } else if (child.getNodeType() == Node.ELEMENT_NODE) {
                textBuf.append(XMLUtils.getStringValue((Element) child));
                contents.add(child);
            } else if (child.getNodeType() == Node.TEXT_NODE) {
                textBuf.append(child.getNodeValue());
                contents.add(child);
            }
        }
        textNode = IndexStringProcessor.normalizeTextValue(textBuf.toString());
        if (textNode.length() == 0) {
            textNode = null;
        }

        if (theNode.getAttributes().getNamedItem(elIndexRangeStartName) != null ||
                theNode.getAttributes().getNamedItem(elIndexRangeEndName) != null) {
            ditastyle = true;
        }

        final ArrayList<Node> res = new ArrayList<Node>();
        if ((ditastyle)) {
            final IndexEntry[] indexEntries = indexDitaProcessor.processIndexDitaNode(theNode,"");

            for (final IndexEntry indexEntrie : indexEntries) {
                theIndexEntryFoundListener.foundEntry(indexEntrie);
            }

            final Node[] nodes = transformToNodes(indexEntries, theTargetDocument, null);
            for (final Node node : nodes) {
                res.add(node);
            }

        } else if (textNode != null) {
            final Node[] nodes = processIndexString(textNode, contents, theTargetDocument, theIndexEntryFoundListener);
            for (final Node node : nodes) {
                res.add(node);
            }
        } else {
            return new Node[0];
        }

        return (Node[]) res.toArray(new Node[res.size()]);

    }

    /**
     * Check if node is an index term element or specialization of one.
     * 
     * @param node element to test
     * @return {@code true} if node is an index term element, otherwise {@code false}
     */
    private boolean checkElementName(final Node node) {
        return TOPIC_INDEXTERM.matches(node)
                || INDEXING_D_INDEX_SORT_AS.matches(node)
                || INDEXING_D_INDEX_SEE.matches(node)
                || INDEXING_D_INDEX_SEE_ALSO.matches(node);
    }
    
    private boolean checkDraftNode(final Node node) {
        return TOPIC_DRAFT_COMMENT.matches(node)
                || TOPIC_REQUIRED_CLEANUP.matches(node);
    }

    /**
     * Processes index string and creates nodes with "prefix" in given "namespace_url" from the parsed index entry text.
     *
     * @param theIndexString             index string
     * param contents index contents 
     * @param theTargetDocument          target document to create new nodes
     * @param theIndexEntryFoundListener listener to notify that new index entry was found
     * @return the array of nodes after processing index string
     */
    private Node[] processIndexString(final String theIndexString, final List<Node> contents, final Document theTargetDocument, final IndexEntryFoundListener theIndexEntryFoundListener) {
        final IndexEntry[] indexEntries = IndexStringProcessor.processIndexString(theIndexString, contents);


        for (final IndexEntry indexEntrie : indexEntries) {
            theIndexEntryFoundListener.foundEntry(indexEntrie);
        }

        return transformToNodes(indexEntries, theTargetDocument, null);
    }


    /**
     * Creates nodes from index entries
     *
     * @param theIndexEntries         index entries
     * @param theTargetDocument       target document
     * @param theIndexEntryComparator comparator to sort the index entries. if it is null the index entries will be unsorted
     * @return nodes for the target document
     */
    private Node[] transformToNodes(final IndexEntry[] theIndexEntries, final Document theTargetDocument, final Comparator<IndexEntry> theIndexEntryComparator) {
        if (null != theIndexEntryComparator) {
            Arrays.sort(theIndexEntries, theIndexEntryComparator);
        }

        final List<Element> result = new ArrayList<Element>();
        for (final IndexEntry indexEntry : theIndexEntries) {
            final Element indexEntryNode = createElement(theTargetDocument, "index.entry");

            final Element formattedStringElement = createElement(theTargetDocument, "formatted-value");
            if (indexEntry.getContents() != null) {
                for (final Iterator<Node> i = indexEntry.getContents().iterator(); i.hasNext();) {
                    final Node child = i.next();
                    final Node clone = theTargetDocument.importNode(child, true);
                    if (!i.hasNext() && clone.getNodeType() == Node.TEXT_NODE) {
                        final Text t = (Text) clone;
                        t.setData(t.getData().replaceAll("[\\s\\n]+$", ""));
                    }
                    formattedStringElement.appendChild(clone);
                }
            } else {
                final Text textNode = theTargetDocument.createTextNode(indexEntry.getFormattedString());
                textNode.normalize();
                formattedStringElement.appendChild(textNode);
            }
            indexEntryNode.appendChild(formattedStringElement);

            final String[] refIDs = indexEntry.getRefIDs();
            for (final String refID : refIDs) {
                final Element referenceIDElement = createElement(theTargetDocument, "refID");
                referenceIDElement.setAttribute("indexid", hashPrefix + Integer.toString(refID.hashCode()));
                referenceIDElement.setAttribute("value", refID);
                indexEntryNode.appendChild(referenceIDElement);
            }

            final String val = indexEntry.getValue();
            if (null != val) {
                indexEntryNode.setAttribute("value", val);
            }

            final String sort = indexEntry.getSortString();
            if (null != sort) {
                indexEntryNode.setAttribute("sort-string", sort);
            }

            if (indexEntry.isStartingRange()) {
                indexEntryNode.setAttribute("start-range", "true");
            } else if (indexEntry.isEndingRange()) {
                indexEntryNode.setAttribute("end-range", "true");
            }
            if (indexEntry.isSuppressesThePageNumber()) {
                indexEntryNode.setAttribute("no-page", "true");
            } else if (indexEntry.isRestoresPageNumber()) {
                indexEntryNode.setAttribute("single-page", "true");
            }

            final IndexEntry[] childIndexEntries = indexEntry.getChildIndexEntries();

            final Node[] nodes = transformToNodes(childIndexEntries, theTargetDocument, theIndexEntryComparator);

            for (final Node node : nodes) {
                indexEntryNode.appendChild(node);
            }

            final IndexEntry[] seeChildIndexEntries = indexEntry.getSeeChildIndexEntries();
            if (seeChildIndexEntries != null) {
                final Element seeElement = createElement(theTargetDocument, "see-childs");
                final Node[] seeNodes = transformToNodes(seeChildIndexEntries, theTargetDocument, theIndexEntryComparator);
                for (final Node node : seeNodes) {
                    seeElement.appendChild(node);
                }

                indexEntryNode.appendChild(seeElement);
            }

            final IndexEntry[] seeAlsoChildIndexEntries = indexEntry.getSeeAlsoChildIndexEntries();
            if (seeAlsoChildIndexEntries != null) {
                final Element seeAlsoElement = createElement(theTargetDocument, "see-also-childs");
                final Node[] seeAlsoNodes = transformToNodes(seeAlsoChildIndexEntries, theTargetDocument, theIndexEntryComparator);
                for (final Node node : seeAlsoNodes) {
                    seeAlsoElement.appendChild(node);
                }

                indexEntryNode.appendChild(seeAlsoElement);
            }

            result.add(indexEntryNode);
        }
        return (Node[]) result.toArray(new Node[result.size()]);
    }


    /**
     * Creates element with "prefix" in "namespace_url" with given name for the target document
     *
     * @param theTargetDocument target document
     * @param theName           name
     * @return new element
     */
    private Element createElement(final Document theTargetDocument, final String theName) {
        final Element indexEntryNode = theTargetDocument.createElementNS(this.namespace_url, theName);
        indexEntryNode.setPrefix(this.prefix);
        return indexEntryNode;
    }
}
