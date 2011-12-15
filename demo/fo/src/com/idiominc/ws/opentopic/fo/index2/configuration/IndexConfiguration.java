package com.idiominc.ws.opentopic.fo.index2.configuration;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

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

public class IndexConfiguration {
    private final List<ConfigEntry> entries = new ArrayList<ConfigEntry>();
    private static String message;


    public IndexConfiguration() {
    }


    public ConfigEntry[] getEntries() {
        return (ConfigEntry[]) entries.toArray(new ConfigEntry[entries.size()]);
    }


    public void addEntry(final ConfigEntry theEntry) {
        this.entries.add(theEntry);
    }


    public static IndexConfiguration parse(final Document theDocument)
            throws ParseException {
        message = "Invalid configuration format";

        final IndexConfiguration indexConfiguration = new IndexConfiguration();

        final NodeList indexConfigurationSet = theDocument.getElementsByTagName("index.configuration.set");
        if (indexConfigurationSet.getLength() != 1) {
            throw new ParseException(message);
        }
        final Node indexConfigurationSetNode = indexConfigurationSet.item(0);

        if (indexConfigurationSetNode == null) {
            throw new ParseException(message);
        }

        final Node indexConf = getNodeByName("index.configuration", indexConfigurationSetNode.getChildNodes());

        if (indexConf == null) {
            throw new ParseException(message);
        }

        final Node indexGroups = getNodeByName("index.groups", indexConf.getChildNodes());

        if (indexGroups == null) {
            throw new ParseException(message);
        }

        final NodeList indexGroupChilds = indexGroups.getChildNodes();

        for (int i = 0; i < indexGroupChilds.getLength(); i++) {
            final Node node = indexGroupChilds.item(i);
            if ("index.group".equals(node.getNodeName())) {
                final Node key = getNodeByName("group.key", node.getChildNodes());
                final Node label = getNodeByName("group.label", node.getChildNodes());
                final Node members = getNodeByName("group.members", node.getChildNodes());

                final String keyValue = getNodeValue(key);
                final String labelValue = getNodeValue(label);
                String[] groupMembers = new String[0];
                final ArrayList<CharRange> rangeList = new ArrayList<CharRange>();

                if (null != members && members.getChildNodes().getLength() > 0) {
                    final ArrayList<String> nodeValues = new ArrayList<String>();

                    final NodeList membersChilds = members.getChildNodes();
                    for (int j = 0; j < membersChilds.getLength(); j++) {
                        final Node membersChild = membersChilds.item(j);
                        if ("char.set".equals(membersChild.getNodeName())) {
                            if (membersChild.hasAttributes() && membersChild.getAttributes() != null) {
                                final Node startRange = membersChild.getAttributes().getNamedItem("start-range");
                                final Node endRange = membersChild.getAttributes().getNamedItem("end-range");
                                final String startRangeText = getNodeValue(startRange);
                                final String endRangeText = getNodeValue(endRange);
                                if (startRange != null && startRangeText.length() > 0 &&
                                        endRange != null && endRangeText.length() > 0) {
                                    final CharRange range = new CharRange(startRangeText, endRangeText);
                                    rangeList.add(range);
                                    nodeValues.add(startRangeText);
                                }
                            }
                            final String nodeValue = getNodeValue(membersChild);
                            if (nodeValue.length() > 0) {
                                nodeValues.add(nodeValue);
                            }
                        }
                    }
                    groupMembers = (String[])nodeValues.toArray(new String[nodeValues.size()]);
                }
                final ConfigEntryImpl configEntry = new ConfigEntryImpl(labelValue, keyValue, groupMembers);
                for(int j = 0; j<rangeList.size();j++) {
                    configEntry.addRange((CharRange)rangeList.get(j));
                }
                indexConfiguration.addEntry(configEntry);
            }
        }

        return indexConfiguration;
    }


    private static String getNodeValue(final Node theNode) {
        if (theNode.getNodeType() == Node.TEXT_NODE) {
            return theNode.getNodeValue().trim();
        } else {
            final StringBuffer res = new StringBuffer();
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
