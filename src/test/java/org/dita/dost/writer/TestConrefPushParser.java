/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.writer;

import static org.apache.commons.io.FileUtils.*;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.dita.dost.util.Job;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import static org.junit.Assert.assertEquals;

import org.dita.dost.TestUtils;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.reader.ConrefPushReader;
import org.dita.dost.reader.ConrefPushReader.MoveKey;
import org.dita.dost.util.Constants;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class TestConrefPushParser {

    private static final File resourceDir = TestUtils.getResourceDir(TestConrefPushParser.class);
    private static final File srcDir = new File(resourceDir, "src");
    private static File tempDir;
    private static File inputFile;


    @BeforeClass
    public static void setUp() throws IOException{
        tempDir = TestUtils.createTempDir(TestConrefPushParser.class);

        inputFile = new File(tempDir, "conrefpush_stub.xml");
        copyFile(new File(srcDir, "conrefpush_stub.xml"), inputFile);
        copyFile(new File(srcDir, "conrefpush_stub2.xml"), new File(tempDir, "conrefpush_stub2.xml"));
    }

    @Test
    public void testWrite() throws DITAOTException, ParserConfigurationException, SAXException, IOException{
        /*
         * the part of content of conrefpush_stub2.xml is
         * <ol>
         * 	<li id="A">A</li>
         * 	<li id="B">B</li>
         * 	<li id="C">C</li>
         * </ol>
         * 
         * the part of content of conrefpush_stup.xml is
         *  <steps>
         * 	 <step conaction="pushbefore"><cmd>before</cmd></step>
         *   <step conref="conrefpush_stub2.xml#X/A" conaction="mark"/>
         *   <step conref="conrefpush_stub2.xml#X/B" conaction="mark"/>
         *	 <step conaction="pushafter"><cmd>after</cmd></step>
         *	 <step conref="conrefpush_stub2.xml#X/C" conaction="pushreplace"><cmd>replace</cmd></step>
         *	</steps>
         *
         * after conrefpush the part of conrefpush_stub2.xml should be like this
         * <ol class="- topic/ol ">
         *  <li class="- topic/li task/step ">
         *  	<ph class="- topic/ph task/cmd ">
         *  	before
         *  	</ph>
         *  </li>
         *  <li id="A" class="- topic/li ">A</li>
         *	<li id="B" class="- topic/li ">B</li>
         *	<li class="- topic/li task/step ">
         *		<ph class="- topic/ph task/cmd ">
         *		after
         *		</ph>
         *	</li>
         *	<li class="- topic/li task/step ">
         *		<ph class="- topic/ph task/cmd ">
         *		replace
         *		</ph>
         *	</li>
         * </ol>
         */
        final ConrefPushParser parser = new ConrefPushParser();
        parser.setLogger(new TestUtils.TestLogger());
        parser.setJob(new Job(tempDir));
        final ConrefPushReader reader = new ConrefPushReader();

        reader.read(inputFile.getAbsoluteFile());
        final Map<File, Hashtable<MoveKey, DocumentFragment>> pushSet = reader.getPushMap();
        final Iterator<Map.Entry<File, Hashtable<MoveKey, DocumentFragment>>> iter = pushSet.entrySet().iterator();
        if(iter.hasNext()){
            final Map.Entry<File, Hashtable<MoveKey, DocumentFragment>> entry = iter.next();
            // initialize the parsed file
            copyFile(new File(srcDir, "conrefpush_stub2_backup.xml"), entry.getKey());
            //            final Content content = new ContentImpl();
//            content.setValue(entry.getValue());
//            parser.setContent(content);
            parser.setMoveTable(entry.getValue());
            parser.write(entry.getKey());
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder builder = factory.newDocumentBuilder();
            final Document document = builder.parse(entry.getKey());
            final Element elem = document.getDocumentElement();
            NodeList nodeList = elem.getChildNodes();
            // according to the structure, it comes to the <li> after 2 iterations.
            for(int i=0;i<2;i++){
                for(int j=0;j<nodeList.getLength();j++){
                    if(nodeList.item(j).getNodeType() == Node.ELEMENT_NODE){
                        nodeList= nodeList.item(j).getChildNodes();
                        break;
                    }
                }
            }
            Element element;
            for(int i=0; i<nodeList.getLength(); i++){
                Node node = nodeList.item(i);
                if(node.getNodeType() == Node.ELEMENT_NODE){
                    element = (Element)node;
                    if(element.getAttributes().getNamedItem("id")!=null && element.getAttributes().getNamedItem("id").getNodeValue().equals("A")){
                        // get node of before
                        node = element.getPreviousSibling();
                        while(node.getNodeType() != Node.ELEMENT_NODE){
                            node = node.getPreviousSibling();
                        }
                        assertEquals("<li class=\"- topic/li task/step \"><ph class=\"- topic/ph task/cmd \">before</ph></li>", nodeToString((Element)node));
                    }else if(element.getAttributes().getNamedItem("id")!=null && element.getAttributes().getNamedItem("id").getNodeValue().equals("B")){
                        // get node of after
                        node = element.getNextSibling();
                        while(node.getNodeType() != Node.ELEMENT_NODE){
                            node = node.getNextSibling();
                        }
                        assertEquals("<li class=\"- topic/li task/step \"><ph class=\"- topic/ph task/cmd \">after</ph></li>", nodeToString((Element)node));

                        // get node of replacement
                        node = node.getNextSibling();
                        while(node.getNodeType()!=Node.ELEMENT_NODE){
                            node = node.getNextSibling();
                        }
                        assertEquals("<li class=\"- topic/li task/step \" id=\"C\"><ph class=\"- topic/ph task/cmd \">replace</ph></li>", nodeToString((Element)node));
                    }
                }
            }

        }
    }

    private String nodeToString(final Element elem){
        final StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(Constants.LESS_THAN).append(elem.getNodeName());
        final NamedNodeMap namedNodeMap = elem.getAttributes();
        for(int i=0; i<namedNodeMap.getLength(); i++){
            stringBuffer.append(Constants.STRING_BLANK).append(namedNodeMap.item(i).getNodeName()).append(Constants.EQUAL).append(Constants.QUOTATION+namedNodeMap.item(i).getNodeValue()+Constants.QUOTATION);
        }
        stringBuffer.append(Constants.GREATER_THAN);
        final NodeList nodeList = elem.getChildNodes();
        for(int i=0; i<nodeList.getLength(); i++){
            final Node node = nodeList.item(i);
            if(node.getNodeType() == Node.ELEMENT_NODE){
                // If the type of current node is ELEMENT_NODE, process it
                stringBuffer.append(nodeToString((Element)node));
            }
            if(node.getNodeType() == Node.TEXT_NODE){
                stringBuffer.append(node.getNodeValue());
            }
        }
        stringBuffer.append("</").append(elem.getNodeName()).append(Constants.GREATER_THAN);
        return stringBuffer.toString();
    }

    @AfterClass
    public static void tearDown() throws IOException {
        TestUtils.forceDelete(tempDir);
    }

}
