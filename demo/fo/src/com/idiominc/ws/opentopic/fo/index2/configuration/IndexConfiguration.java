package com.idiominc.ws.opentopic.fo.index2.configuration;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Ivan Luzyanin
 * Date: 21.06.2005
 * Time: 16:19:27
 * <br><br>
 * This class parses and represents index configuration xml file
 */
public class IndexConfiguration {
	private List entries = new ArrayList();
	private static String message;


	public IndexConfiguration() {
	}


	public ConfigEntry[] getEntries() {
		return (ConfigEntry[]) entries.toArray(new ConfigEntry[entries.size()]);
	}


	public void addEntry(ConfigEntry theEntry) {
		this.entries.add(theEntry);
	}


	public static IndexConfiguration parse(Document theDocument)
			throws ParseException {
		message = "Invalid configuration format";

		final IndexConfiguration indexConfiguration = new IndexConfiguration();

		final NodeList indexConfigurationSet = theDocument.getElementsByTagName("index.configuration.set");
		if (indexConfigurationSet.getLength() != 1) {
			throw new ParseException(message);
		}
		final Node indexConfigurationSetNode = indexConfigurationSet.item(0);

		if (indexConfigurationSetNode == null) throw new ParseException(message);

		final Node indexConf = getNodeByName("index.configuration", indexConfigurationSetNode.getChildNodes());

		if (indexConf == null) throw new ParseException(message);

		final Node indexGroups = getNodeByName("index.groups", indexConf.getChildNodes());

		if (indexGroups == null) throw new ParseException(message);

		final NodeList indexGroupChilds = indexGroups.getChildNodes();

		for (int i = 0; i < indexGroupChilds.getLength(); i++) {
			final Node node = indexGroupChilds.item(i);
			if ("index.group".equals(node.getNodeName())) {
				final Node key = getNodeByName("group.key", node.getChildNodes());
				final Node label = getNodeByName("group.label", node.getChildNodes());
				final Node members = getNodeByName("group.members", node.getChildNodes());

				final String keyValue = getNodeValue(key);
				final String labelValue = getNodeValue(label);
				char[] groupMemmbers = new char[0];

				if (null != members && members.getChildNodes().getLength() > 0) {
					StringBuffer charBuff = new StringBuffer();
					final NodeList membersChilds = members.getChildNodes();
					for (int j = 0; j < membersChilds.getLength(); j++) {
						final Node membersChild = membersChilds.item(j);
						if ("char.set".equals(membersChild.getNodeName())) {
							final String nodeValue = getNodeValue(membersChild);
							charBuff.append(nodeValue);
						}
					}
					groupMemmbers = charBuff.toString().toCharArray();
				}
				final ConfigEntryImpl configEntry = new ConfigEntryImpl(labelValue, keyValue, groupMemmbers);
				indexConfiguration.addEntry(configEntry);
			}
		}

		return indexConfiguration;
	}


	private static String getNodeValue(Node theNode) {
		if (theNode.getNodeType() == Node.TEXT_NODE) {
			return theNode.getNodeValue().trim();
		} else {
			StringBuffer res = new StringBuffer();
			final NodeList childNodes = theNode.getChildNodes();
			for (int i = 0; i < childNodes.getLength(); i++) {
				final String nodeValue = getNodeValue(childNodes.item(i));
				res.append(nodeValue);
			}
			return res.toString().trim();
		}
	}


	private static Node getNodeByName(final String theNodeName, final NodeList theNodeList) {
		for (int i = 0; i < theNodeList.getLength(); i++) {
			final Node node = theNodeList.item(i);
			if (theNodeName.equals(node.getNodeName())) {
				return node;
			}
		}
		return null;
	}
}
