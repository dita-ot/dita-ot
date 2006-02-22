package com.idiominc.ws.opentopic.fo.i18n;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.List;
import java.util.ArrayList;

import com.idiominc.ws.opentopic.fo.i18n.Alphabet;


/**
 * User: Ivan Luzyanin
 * Date: Jan 14, 2004
 * Time: 12:26:03 PM
 */
public class Configuration {
    private static final String BAD_CONF_MESSAGE = "Bad configuration file format!";

    private final Alphabet[] alphabets;


    public Configuration(Document theConfigurationFile)
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
    public Alphabet getAlphabetForChar(char theChar) {
        Alphabet result = null;
        for (int i = 0; i < this.alphabets.length; i++) {
            Alphabet alphabet = this.alphabets[i];
            if (alphabet.isContain(theChar)) {
                result = alphabet;
                break;
            }
        }
        return result;
    }


    private Alphabet[] initAlphabets(Document theConfigurationFile)
            throws ConfigurationException {
        List alphabetList = new ArrayList();

        Node firstChild = theConfigurationFile.getDocumentElement();
        if (!"configuration".equals(firstChild.getNodeName())) {
            throw new ConfigurationException(BAD_CONF_MESSAGE);
        }

        NodeList alphabets = firstChild.getChildNodes();
        for (int i = 0; i < alphabets.getLength(); i++) {
            Node alphabet = alphabets.item(i);
            if (
                    !"alphabet".equals(alphabet.getNodeName())
                    || !(alphabet.getAttributes() != null && alphabet.getAttributes().getNamedItem("char-set") != null)
            ) {
                continue;
            }
            String charSetName = alphabet.getAttributes().getNamedItem("char-set").getNodeValue();

            NodeList alphabetChildNodes = alphabet.getChildNodes();

            for (int j = 0; j < alphabetChildNodes.getLength(); j++) {
                Node alphabetChildNode = alphabetChildNodes.item(j);
                String childNodeName = alphabetChildNode.getNodeName();
                if ("character-set".equals(childNodeName)) {
                    Character[] chars = processCharacterSetNode(alphabetChildNode);
                    alphabetList.add(new Alphabet(charSetName, chars));
                } else {
                    //                    System.out.println("Unprocessed element [" + childNodeName + "]");
                }
            }
        }
        return (Alphabet[]) alphabetList.toArray(new Alphabet[alphabetList.size()]);
    }


    private Character[] processCharacterSetNode(Node theNode)
            throws ConfigurationException {
        List characterList = new ArrayList();

        NodeList ranges = theNode.getChildNodes();
        for (int i = 0; i < ranges.getLength(); i++) {
            Node node = ranges.item(i);

            if ("character".equals(node.getNodeName())) {
                char[] aChars = node.getFirstChild().getNodeValue().toCharArray();
                if (aChars.length != 1) {
                    throw new ConfigurationException(BAD_CONF_MESSAGE);
                }
                characterList.add(new Character(aChars[0]));
            } else if ("character-range".equals(node.getNodeName())) {
                Node start = null;
                Node end = null;

                NodeList childNodes = node.getChildNodes();
                for (int j = 0; j < childNodes.getLength(); j++) {
                    Node aNode = childNodes.item(j);
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

                char[] startChars = start.getFirstChild().getNodeValue().toCharArray();
                char[] endChars = end.getFirstChild().getNodeValue().toCharArray();

                if (startChars.length != 1 || endChars.length != 1) {
                    throw new ConfigurationException(BAD_CONF_MESSAGE);
                }

                char startChar = startChars[0];
                char endChar = endChars[0];

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
