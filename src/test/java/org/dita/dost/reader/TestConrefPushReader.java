/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2010 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.reader;

import org.dita.dost.TestUtils;
import org.dita.dost.reader.ConrefPushReader.MoveKey;
import org.dita.dost.store.StreamStore;
import org.dita.dost.util.Job;
import org.dita.dost.util.XMLUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;
import static org.apache.commons.io.FileUtils.copyFile;
import static org.dita.dost.TestUtils.assertXMLEqual;
import static org.dita.dost.TestUtils.buildControlDocument;

public class TestConrefPushReader {

    private static final File resourceDir = TestUtils.getResourceDir(TestConrefPushReader.class);
    private static final File srcDir = new File(resourceDir, "src");
    private final DocumentBuilder builder;
    private File tempDir;

    public TestConrefPushReader() throws ParserConfigurationException {
        builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    }

    @Before
    public void setUp() throws IOException {
        tempDir = TestUtils.createTempDir(TestConrefPushReader.class);
        copyFile(new File(srcDir, "conrefpush_stub.xml"), new File(tempDir, "conrefpush_stub.xml"));
    }

    @Test
    public void testRead() throws IOException {
        final File filename = new File(srcDir, "conrefpush_stub.xml");
        final ConrefPushReader pushReader = new ConrefPushReader();
        pushReader.setJob(new Job(tempDir, new StreamStore(tempDir, new XMLUtils())));
        pushReader.read(filename.getAbsoluteFile());

        final Map<File, Hashtable<MoveKey, DocumentFragment>> pushSet = pushReader.getPushMap();
        assertEquals(1, pushSet.entrySet().size());
        final Hashtable<MoveKey, DocumentFragment> act = pushSet.values().iterator().next();
        assertXMLEqual(
                toDocument(act.get(new MoveKey("#X/A", "pushbefore"))),
                buildControlDocument("<step class='- topic/li task/step '><cmd class='- topic/ph task/cmd '>before</cmd></step>"));
        assertXMLEqual(
                toDocument(act.get(new MoveKey("#X/B", "pushafter"))),
                buildControlDocument("<step class='- topic/li task/step '><cmd class='- topic/ph task/cmd '>after</cmd></step>"));
        assertXMLEqual(
                toDocument(act.get(new MoveKey("#X/C", "pushreplace"))),
                buildControlDocument("<step class='- topic/li task/step ' id='C'><cmd class='- topic/ph task/cmd '>replace</cmd></step>"));
    }

    private Document toDocument(final DocumentFragment fragment) {
        final Document doc = builder.newDocument();
        doc.appendChild(doc.adoptNode(fragment));
        return doc;
    }

    @After
    public void teardown() throws IOException {
        TestUtils.forceDelete(tempDir);
    }

}
