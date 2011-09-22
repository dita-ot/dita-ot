package com.idiominc.ws.opentopic.fo.index2;

import static org.dita.dost.util.Constants.*;

import com.idiominc.ws.opentopic.fo.index2.configuration.IndexConfiguration;
import com.idiominc.ws.opentopic.fo.index2.util.IndexStringProcessor;
import com.idiominc.ws.opentopic.fo.index2.util.IndexDitaProcessor;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.*;


/*
Copyright � 2004-2006 by Idiom Technologies, Inc. All rights reserved.
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

This file is part of the DITA Open Toolkit project hosted on Sourceforge.net.
See the accompanying license.txt file for applicable licenses.
 */
public class IndexPreprocessor {
    private final String prefix;
    private final String namespace_url;
    private static final String elIndexRangeStartName = "start";
    private static final String elIndexRangeEndName = "end";


    public IndexPreprocessor(final String prefix, final String theNamespace_url) {
        this.prefix = prefix;
        this.namespace_url = theNamespace_url;
    }

    public IndexPreprocessResult process(final Document theInput)
            throws ProcessException {
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = null;
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (final ParserConfigurationException e) {
            e.printStackTrace();
        }
        final Document doc = documentBuilder.newDocument();

        final Node rootElement = theInput.getDocumentElement();


        final ArrayList indexes = new ArrayList();

        final IndexEntryFoundListener listener = new IndexEntryFoundListener() {
            public void foundEntry(final IndexEntry theEntry) {
                indexes.add(theEntry);
            }
        };

        final Node node = processCurrNode(rootElement, doc, listener)[0];

        doc.appendChild(node);

        doc.getDocumentElement().setAttribute("xmlns:" + this.prefix, this.namespace_url);

        return new IndexPreprocessResult(doc, (IndexEntry[]) indexes.toArray(new IndexEntry[0]));
    }

    public void createAndAddIndexGroups(final IndexEntry[] theIndexEntries, final IndexConfiguration theConfiguration, final Document theDocument, final Locale theLocale) {
        final IndexComparator indexEntryComparator = new IndexComparator(theLocale);

        if (null == indexEntryComparator) {
            System.out.println("Collator is not found, sort order of index group can be wrong");
        }

        final IndexGroup[] indexGroups = IndexGroupProcessor.process(theIndexEntries, theConfiguration, theLocale);

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

        if (checkElementName(theNode)) {
            return processIndexNode(theNode, theTargetDocument, theIndexEntryFoundListener);
            /*
            for (int i = 0; i < childNodes.getLength(); i++) {
				Node child = childNodes.item(i);
				if (child.getNodeType() == Node.TEXT_NODE) { //Look for the first non-empty text node
					final String normIndex = IndexStringProcessor.normalizeTextValue(child.getNodeValue());
					if (normIndex.length() > 0) {
						return processIndexTextNode(child, theTargetDocument, theIndexEntryFoundListener);
					}
				}
			}
             */
        } else {
            final Node result = theTargetDocument.importNode(theNode, false);
            for (int i = 0; i < childNodes.getLength(); i++) {
                final Node[] processedNodes = processCurrNode(childNodes.item(i), theTargetDocument, theIndexEntryFoundListener);
                for (final Node node : processedNodes) {
                    result.appendChild(node);
                }
            }
            return new Node[]{result};
        }
    }

    private Node[] processIndexNode(final Node theNode, final Document theTargetDocument, final IndexEntryFoundListener theIndexEntryFoundListener) {
        theNode.normalize();

        boolean ditastyle = false;
        boolean textNode = false;

        final NodeList childNodes = theNode.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            final Node child = childNodes.item(i);
            if (checkElementName(child)) {
                ditastyle = true;
                break;
            }
            if (child.getNodeType() == Node.TEXT_NODE) {
                final String normIndex = IndexStringProcessor.normalizeTextValue(child.getNodeValue());
                if (normIndex.length() > 0) {
                    textNode = true;
                }
            }
        }

        if (theNode.getAttributes().getNamedItem(elIndexRangeStartName) != null ||
                theNode.getAttributes().getNamedItem(elIndexRangeEndName) != null) {
            ditastyle = true;
        }

        String[] indexStrings;
        final ArrayList res = new ArrayList();
        if ((ditastyle)) {
            final IndexEntry[] indexEntries = IndexDitaProcessor.processIndexDitaNode(theNode,"");

            for (final IndexEntry indexEntrie : indexEntries) {
                theIndexEntryFoundListener.foundEntry(indexEntrie);
            }

            final Node[] nodes = transformToNodes(indexEntries, theTargetDocument, null);
            for (final Node node : nodes) {
                res.add(node);
            }

        } else if (textNode) {
            for (int k = 0; k < childNodes.getLength(); k++) {
                final Node child = childNodes.item(k);
                indexStrings = new String[]{child.getNodeValue()};
                for (final String indexString : indexStrings) {
                    final Node[] nodes = processIndexString(indexString, theTargetDocument, theIndexEntryFoundListener);
                    for (final Node node : nodes) {
                        res.add(node);
                    }
                }
            }
        } else {
            return new Node[0];
        }

        return (Node[]) res.toArray(new Node[res.size()]);

    }

    private Node[] processIndexTextNode(final Node theNode, final Document theTargetDocument, final IndexEntryFoundListener theIndexEntryFoundListener) {
        theNode.normalize();

        boolean ditastyle = false;

        final NodeList childNodes = theNode.getParentNode().getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            final Node child = childNodes.item(i);
            if (checkElementName(child)) {
                ditastyle = true;
                break;
            }
        }

        String[] indexStrings;
        final ArrayList res = new ArrayList();
        if ((ditastyle)) {
            final IndexEntry[] indexEntries = IndexDitaProcessor.processIndexDitaNode(theNode.getParentNode(),"");

            for (final IndexEntry indexEntrie : indexEntries) {
                theIndexEntryFoundListener.foundEntry(indexEntrie);
            }

            final Node[] nodes = transformToNodes(indexEntries, theTargetDocument, null);
            for (final Node node : nodes) {
                res.add(node);
            }

        } else {
            indexStrings = new String[]{theNode.getNodeValue()};
            for (final String indexString : indexStrings) {
                final Node[] nodes = processIndexString(indexString, theTargetDocument, theIndexEntryFoundListener);
                for (final Node node : nodes) {
                    res.add(node);
                }
            }
        }

        return (Node[]) res.toArray(new Node[res.size()]);
    }

    private boolean checkElementName(final Node node) {
        return TOPIC_INDEXTERM.matches(node)
                || INDEXING_D_INDEX_SORT_AS.matches(node)
                || INDEXING_D_INDEX_SEE.matches(node)
                || INDEXING_D_INDEX_SEE_ALSO.matches(node);
    }

    private String[] createIndexStringFromDitastyleIndex(final Node theNode) {
        //Go through the childs and append text nodes to the index string
        //Index elements on the same level will create separate index strings
        final ArrayList resultList = new ArrayList();
        if (TOPIC_INDEXTERM.matches(theNode)) //Is index element?
        {
            final StringBuffer resIndexString = new StringBuffer();
            boolean skipCurrentLevel = false;
            final NodeList childNodes = theNode.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) { //Go through child nodes to find text nodes
                final Node child = childNodes.item(i);
                if (child.getNodeType() == Node.TEXT_NODE) {
                    final String val = child.getNodeValue();
                    if (null != val) {
                        resIndexString.append(val); //append to result index string
                    }
                } else if (TOPIC_INDEXTERM.matches(child)) {
                    skipCurrentLevel = true;		//skip adding current level index string because it has continuation on the descendant level
                    final String[] indexValues = createIndexStringFromDitastyleIndex(child); //call recursevelly but for the found child
                    for (final String indexValue : indexValues) {
                        resultList.add(resIndexString.toString() + ':' + indexValue); //append to result list prefixed by current level
                    }
                }
            }
            if (!skipCurrentLevel) {
                //No descendant index elements were found so add this level as final
                resultList.add(resIndexString.toString());
            }
        }
        return (String[]) resultList.toArray(new String[resultList.size()]); //return result
    }


    /**
     * Processes index string and creates nodes with "prefix" in given "namespace_url" from the parsed index entry text.
     *
     * @param theIndexString             index string
     * @param theTargetDocument          target document to create new nodes
     * @param theIndexEntryFoundListener listener to notify that new index entry was found
     * @return the array of nodes after processing index string
     */
    private Node[] processIndexString(final String theIndexString, final Document theTargetDocument, final IndexEntryFoundListener theIndexEntryFoundListener) {
        final IndexEntry[] indexEntries = IndexStringProcessor.processIndexString(theIndexString);


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
    private Node[] transformToNodes(final IndexEntry[] theIndexEntries, final Document theTargetDocument, final Comparator theIndexEntryComparator) {
        if (null != theIndexEntryComparator) {
            Arrays.sort(theIndexEntries, theIndexEntryComparator);
        }

        final List result = new ArrayList();
        for (final IndexEntry indexEntry : theIndexEntries) {
            final Element indexEntryNode = createElement(theTargetDocument, "index.entry");

            final Element formattedStringElement = createElement(theTargetDocument, "formatted-value");
            final Text textNode = theTargetDocument.createTextNode(indexEntry.getFormattedString());
            textNode.normalize();
            formattedStringElement.appendChild(textNode);
            indexEntryNode.appendChild(formattedStringElement);

            final String[] refIDs = indexEntry.getRefIDs();
            for (final String refID : refIDs) {
                final Element referenceIDElement = createElement(theTargetDocument, "refID");
                referenceIDElement.setAttribute("value", refID);
                indexEntryNode.appendChild(referenceIDElement);
            }

            final String val = indexEntry.getValue();
            if (null != val) {
                indexEntryNode.setAttribute("value", val);
            }

            final String so = indexEntry.getSoValue();
            if (null != so) {
                indexEntryNode.setAttribute("SO", so);
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
