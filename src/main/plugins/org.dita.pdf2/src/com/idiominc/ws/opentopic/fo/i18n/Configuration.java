package com.idiominc.ws.opentopic.fo.i18n;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.List;
import java.util.ArrayList;

import com.idiominc.ws.opentopic.fo.i18n.Alphabet;

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

This file is part of the DITA Open Toolkit project hosted on Sourceforge.net.
See the accompanying license.txt file for applicable licenses.
 */
public class Configuration {
    private static final String BAD_CONF_MESSAGE = "Bad configuration file format!";

    private final Alphabet[] alphabets;


    public Configuration(final Document theConfigurationFile)
            throws ConfigurationException {
        this.alphabets = initAlphabets(theConfigurationFile);
    }


    public Alphabet[] getAlphabets() {
        return this.alphabets;
    }


    /**
     * Searches alphabets for a char
     * @return first founded alphabet that contains given char
     *      or <code>null</code> if no alphabets contains given char.
     */
    public Alphabet getAlphabetForChar(final char theChar) {
        Alphabet result = null;
        for (final Alphabet alphabet : this.alphabets) {
            if (alphabet.isContain(theChar)) {
                result = alphabet;
                break;
            }
        }
        return result;
    }


    private Alphabet[] initAlphabets(final Document theConfigurationFile)
            throws ConfigurationException {
        final List<Alphabet> alphabetList = new ArrayList<Alphabet>();

        final Node firstChild = theConfigurationFile.getDocumentElement();
        if (!"configuration".equals(firstChild.getNodeName())) {
            throw new ConfigurationException(BAD_CONF_MESSAGE);
        }

        final NodeList alphabets = firstChild.getChildNodes();
        for (int i = 0; i < alphabets.getLength(); i++) {
            final Node alphabet = alphabets.item(i);
            if (
                    !"alphabet".equals(alphabet.getNodeName())
                    || !(alphabet.getAttributes() != null && alphabet.getAttributes().getNamedItem("char-set") != null)
                    ) {
                continue;
            }
            final String charSetName = alphabet.getAttributes().getNamedItem("char-set").getNodeValue();

            final NodeList alphabetChildNodes = alphabet.getChildNodes();

            for (int j = 0; j < alphabetChildNodes.getLength(); j++) {
                final Node alphabetChildNode = alphabetChildNodes.item(j);
                final String childNodeName = alphabetChildNode.getNodeName();
                if ("character-set".equals(childNodeName)) {
                    final Character[] chars = processCharacterSetNode(alphabetChildNode);
                    alphabetList.add(new Alphabet(charSetName, chars));
                } else {
                    //                    System.out.println("Unprocessed element [" + childNodeName + "]");
                }
            }
        }
        return (Alphabet[]) alphabetList.toArray(new Alphabet[alphabetList.size()]);
    }


    private Character[] processCharacterSetNode(final Node theNode)
            throws ConfigurationException {
        final List<Character> characterList = new ArrayList<Character>();

        final NodeList ranges = theNode.getChildNodes();
        for (int i = 0; i < ranges.getLength(); i++) {
            final Node node = ranges.item(i);

            if ("character".equals(node.getNodeName())) {
                final char[] aChars = node.getFirstChild().getNodeValue().toCharArray();
                if (aChars.length != 1) {
                    throw new ConfigurationException(BAD_CONF_MESSAGE);
                }
                characterList.add(new Character(aChars[0]));
            } else if ("character-range".equals(node.getNodeName())) {
                Node start = null;
                Node end = null;

                final NodeList childNodes = node.getChildNodes();
                for (int j = 0; j < childNodes.getLength(); j++) {
                    final Node aNode = childNodes.item(j);
                    if ("start".equals(aNode.getNodeName())) {
                        start = aNode;
                    } else if ("end".equals(aNode.getNodeName())) {
                        end = aNode;
                    } else {
                        //                        System.out.println("Unprocessed element [" + aNode.getNodeName() + "]");
                    }
                }
                if (null == start || null == end) {
                    throw new ConfigurationException(BAD_CONF_MESSAGE);
                }

                final char[] startChars = start.getFirstChild().getNodeValue().toCharArray();
                final char[] endChars = end.getFirstChild().getNodeValue().toCharArray();

                if (startChars.length != 1 || endChars.length != 1) {
                    throw new ConfigurationException(BAD_CONF_MESSAGE);
                }

                final char startChar = startChars[0];
                final char endChar = endChars[0];

                for (char ch = startChar; ch <= endChar; ch++) {
                    characterList.add(new Character(ch));
                }
            } else {
                //                System.out.println("Unprocessed element [" + node + "]");
            }
        }

        return (Character[]) characterList.toArray(new Character[characterList.size()]);
    }
}
