package com.idiominc.ws.opentopic.fo.index2;

import com.idiominc.ws.opentopic.fo.index2.configuration.IndexConfiguration;
import com.idiominc.ws.opentopic.fo.index2.util.IndexStringProcessor;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.*;


/**
 * User: Ivan Luzyanin
 * Date: Jan 14, 2004
 * Time: 12:21:23 PM
 */
public class IndexPreprocessor {
	private final String prefix;
	private final String namespace_url;
	private String elIndexName = "indexterm";


	public IndexPreprocessor(String prefix, String theNamespace_url) {
		this.prefix = prefix;
		this.namespace_url = theNamespace_url;
	}


	public IndexPreprocessor(String prefix, String theNamespace_url, String theElementIndexName) {
		this.prefix = prefix;
		this.namespace_url = theNamespace_url;
		this.elIndexName = theElementIndexName;
	}


	public IndexPreprocessResult process(Document theInput)
			throws ProcessException {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = null;
		try {
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		Document doc = documentBuilder.newDocument();

		Node rootElement = theInput.getDocumentElement();


		final ArrayList indexes = new ArrayList();

		IndexEntryFoundListener listener = new IndexEntryFoundListener() {
			public void foundEntry(IndexEntry theEntry) {
				indexes.add(theEntry);
			}
		};

		Node node = processCurrNode(rootElement, doc, listener)[0];

		doc.appendChild(node);

		doc.getDocumentElement().setAttribute("xmlns:" + this.prefix, this.namespace_url);

		return new IndexPreprocessResult(doc, (IndexEntry[]) indexes.toArray(new IndexEntry[0]));
	}

	public void createAndAddIndexGroups(final IndexEntry[] theIndexEntries, final IndexConfiguration theConfiguration, final Document theDocument, Locale theLocale) {
		final IndexComparator indexEntryComparator = new IndexComparator(theLocale);

		final IndexGroup[] indexGroups = IndexGroupProcessor.process(theIndexEntries, theConfiguration, theLocale);

		final Element rootElement = theDocument.getDocumentElement();

		final Element indexGroupsElement = theDocument.createElementNS(namespace_url, "index.groups");
		indexGroupsElement.setPrefix(prefix);

		for (int i = 0; i < indexGroups.length; i++) {
			IndexGroup group = indexGroups[i];
			//Create group element
			Node groupElement = theDocument.createElementNS(namespace_url, "index.group");
			groupElement.setPrefix(prefix);
			//Create group label element and index entry childs
			Element groupLabelElement = theDocument.createElementNS(namespace_url, "label");
			groupLabelElement.setPrefix(prefix);
			groupLabelElement.appendChild(theDocument.createTextNode(group.getLabel()));
			groupElement.appendChild(groupLabelElement);

			Node[] entryNodes = transformToNodes(group.getEntries(), theDocument, indexEntryComparator);
			for (int j = 0; j < entryNodes.length; j++) {
				Node entryNode = entryNodes[j];
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
	private Node[] processCurrNode(Node theNode, Document theTargetDocument, IndexEntryFoundListener theIndexEntryFoundListener) {
		NodeList childNodes = theNode.getChildNodes();

		if (elIndexName.equals(theNode.getNodeName())) {
			for (int i = 0; i < childNodes.getLength(); i++) {
				Node child = childNodes.item(i);
				if (child.getNodeType() == Node.TEXT_NODE) { //Look for the first non-empty text node
					final String normIndex = IndexStringProcessor.normalizeTextValue(child.getNodeValue());
					if (normIndex.length() > 0) {
						return processIndexTextNode(child, theTargetDocument, theIndexEntryFoundListener);
					}
				}
			}
			return new Node[0]; //Only empty index strings were found, no need to process
		} else {
			Node result = theTargetDocument.importNode(theNode, false);
			for (int i = 0; i < childNodes.getLength(); i++) {
				Node[] processedNodes = processCurrNode(childNodes.item(i), theTargetDocument, theIndexEntryFoundListener);
				for (int j = 0; j < processedNodes.length; j++) {
					Node node = processedNodes[j];
					result.appendChild(node);
				}
			}
			return new Node[]{result};
		}
	}


	private Node[] processIndexTextNode(Node theNode, Document theTargetDocument, IndexEntryFoundListener theIndexEntryFoundListener) {
		theNode.normalize();

		boolean ditastyle = false;

		NodeList childNodes = theNode.getParentNode().getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			final Node child = childNodes.item(i);
			if (elIndexName.equals(child.getNodeName())) {
				ditastyle = true;
				break;
			}
		}

		String[] indexStrings;
		if ((ditastyle)) {
			indexStrings = createIndexStringFromDitastyleIndex(theNode.getParentNode());
		} else {
			indexStrings = new String[]{theNode.getNodeValue()};
		}
		ArrayList res = new ArrayList();
		for (int i = 0; i < indexStrings.length; i++) {
			String indexString = indexStrings[i];
			final Node[] nodes = processIndexString(indexString, theTargetDocument, theIndexEntryFoundListener);
			for (int j = 0; j < nodes.length; j++) {
				Node node = nodes[j];
				res.add(node);
			}
		}
		return (Node[]) res.toArray(new Node[res.size()]);
	}


	private String[] createIndexStringFromDitastyleIndex(Node theNode) {
		//Go through the childs and append text nodes to the index string
		//Index elements on the same level will create separate index strings
		ArrayList resultList = new ArrayList();
		if (elIndexName.equals(theNode.getNodeName())) //Is index element?
		{
			StringBuffer resIndexString = new StringBuffer();
			boolean skipCurrentLevel = false;
			final NodeList childNodes = theNode.getChildNodes();
			for (int i = 0; i < childNodes.getLength(); i++) { //Go through child nodes to find text nodes
				final Node child = childNodes.item(i);
				if (child.getNodeType() == Node.TEXT_NODE) {
					final String val = child.getNodeValue();
					if (null != val) {
						resIndexString.append(val); //append to result index string
					}
				} else if (elIndexName.equals(child.getNodeName())) {
					skipCurrentLevel = true;		//skip adding current level index string because it has continuation on the descendant level
					String[] indexValues = createIndexStringFromDitastyleIndex(child); //call recursevelly but for the found child
					for (int j = 0; j < indexValues.length; j++) {
						String indexValue = indexValues[j];
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
	private Node[] processIndexString(String theIndexString, Document theTargetDocument, IndexEntryFoundListener theIndexEntryFoundListener) {
		IndexEntry[] indexEntries = IndexStringProcessor.processIndexString(theIndexString);

		for (int i = 0; i < indexEntries.length; i++) {
			theIndexEntryFoundListener.foundEntry(indexEntries[i]);
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
	private Node[] transformToNodes(IndexEntry[] theIndexEntries, Document theTargetDocument, Comparator theIndexEntryComparator) {
		if (null != theIndexEntryComparator) {
			Arrays.sort(theIndexEntries, theIndexEntryComparator);
		}

		List result = new ArrayList();
		for (int i = 0; i < theIndexEntries.length; i++) {
			IndexEntry indexEntry = theIndexEntries[i];

			Element indexEntryNode = createElement(theTargetDocument, "index.entry");

			Element formattedStringElement = createElement(theTargetDocument, "formatted-value");
			Text textNode = theTargetDocument.createTextNode(indexEntry.getFormattedString());
			textNode.normalize();
			formattedStringElement.appendChild(textNode);
			indexEntryNode.appendChild(formattedStringElement);

			String[] refIDs = indexEntry.getRefIDs();
			for (int j = 0; j < refIDs.length; j++) {
				String refID = refIDs[j];
				final Element referenceIDElement = createElement(theTargetDocument, "refID");
				referenceIDElement.setAttribute("value", refID);
				indexEntryNode.appendChild(referenceIDElement);
			}

			String val = indexEntry.getValue();
			if (null != val) {
				indexEntryNode.setAttribute("value", val);
			}

			String so = indexEntry.getSoValue();
			if (null != so) {
				indexEntryNode.setAttribute("SO", so);
			}

			String sort = indexEntry.getSortString();
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

			Node[] nodes = transformToNodes(childIndexEntries, theTargetDocument, theIndexEntryComparator);

			for (int j = 0; j < nodes.length; j++) {
				Node node = nodes[j];
				indexEntryNode.appendChild(node);
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
	private Element createElement(Document theTargetDocument, String theName) {
		Element indexEntryNode = theTargetDocument.createElementNS(this.namespace_url, theName);
		indexEntryNode.setPrefix(this.prefix);
		return indexEntryNode;
	}
}
