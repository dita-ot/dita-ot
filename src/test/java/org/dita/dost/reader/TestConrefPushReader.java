/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2010 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.reader;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.junit.AfterClass;
import org.dita.dost.TestUtils;
import org.dita.dost.reader.ConrefPushReader;
import org.dita.dost.reader.ConrefPushReader.MoveKey;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static org.dita.dost.TestUtils.assertXMLEqual;
import static org.dita.dost.TestUtils.buildControlDocument;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class TestConrefPushReader {

    private static final File resourceDir = TestUtils.getResourceDir(TestConrefPushReader.class);
    private static final File srcDir = new File(resourceDir, "src");
    //private static final File expDir = new File(resourceDir, "exp");
    private static File tempDir;

    @BeforeClass
    public static void setUp() throws IOException{
        tempDir = TestUtils.createTempDir(TestConrefPushReader.class);
    }

    @Test
    public void testRead() throws SAXException, IOException{
        /*
         * the part of content of conrefpush_stup.xml is
         *  <steps>
         * 	 <step conaction="pushbefore"><cmd>before</cmd></step>
         *   <step conref="conrefpush_stub2.xml#X/A" conaction="mark"/>
         *   <step conref="conrefpush_stub2.xml#X/B" conaction="mark"/>
         *	 <step conaction="pushafter"><cmd>after</cmd></step>
         *	 <step conref="conrefpush_stub2.xml#X/C" conaction="pushreplace"><cmd>replace</cmd></step>
         *	</steps>
         */
        final File filename = new File(srcDir, "conrefpush_stub.xml");
        final ConrefPushReader pushReader = new ConrefPushReader();
        pushReader.read(filename.getAbsoluteFile());
        final Map<File, Hashtable<MoveKey, DocumentFragment>> pushSet = pushReader.getPushMap();
        final Iterator<Map.Entry<File, Hashtable<MoveKey, DocumentFragment>>> it= pushSet.entrySet().iterator();
        if (it.hasNext()){
            // pushSet has only one entry, so there is no need to iterate it.
            final Hashtable<MoveKey, DocumentFragment> table = it.next().getValue();
            assertTrue(table.containsKey(new MoveKey("#X/A", "pushbefore")));
            assertXMLEqual(
                    toDocument(table.get(new MoveKey("#X/A", "pushbefore"))),
                    buildControlDocument("<step class=\"- topic/li task/step \"><cmd class=\"- topic/ph task/cmd \">before</cmd></step>"));
            assertTrue(table.containsKey(new MoveKey("#X/B", "pushafter")));
            assertXMLEqual(
                    toDocument(table.get(new MoveKey("#X/B", "pushafter"))),
                    buildControlDocument("<step class=\"- topic/li task/step \"><cmd class=\"- topic/ph task/cmd \">after</cmd></step>"));
            assertTrue(table.containsKey(new MoveKey("#X/C", "pushreplace")));
            assertXMLEqual(
                    toDocument(table.get(new MoveKey("#X/C", "pushreplace"))),
                    buildControlDocument("<step class=\"- topic/li task/step \" id=\"C\"><cmd class=\"- topic/ph task/cmd \">replace</cmd></step>"));
        }
    }

    private Document toDocument(final DocumentFragment fragment) {
        try {
            final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            doc.appendChild(doc.adoptNode(fragment));
            return doc;
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    @AfterClass
    public static void teardown() throws IOException {
        TestUtils.forceDelete(tempDir);
    }

}
