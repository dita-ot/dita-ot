/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2010 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.writer;

import org.dita.dost.TestUtils;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.reader.ConrefPushReader.MoveKey;
import org.dita.dost.store.StreamStore;
import org.dita.dost.util.Job;
import org.dita.dost.util.XMLUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import static org.apache.commons.io.FileUtils.copyFile;
import static org.dita.dost.TestUtils.buildControlDocument;

public class TestConrefPushParser {

    private static final File resourceDir = TestUtils.getResourceDir(TestConrefPushParser.class);
    private static final File srcDir = new File(resourceDir, "src");
    private static final File expDir = new File(resourceDir, "exp");
    private File tempDir;
    private File targetFile;

    @Before
    public void setUp() throws IOException {
        tempDir = TestUtils.createTempDir(TestConrefPushParser.class);
        targetFile = new File(tempDir, "conrefpush_stub2.xml");
        copyFile(new File(srcDir, "conrefpush_stub2.xml"), targetFile);
    }

    @Test
    public void testWrite() throws DITAOTException, SAXException, IOException {
        final Hashtable<MoveKey, DocumentFragment> pushActions = new Hashtable<>();
        pushActions.put(new MoveKey("#X/A", "pushbefore"),
                getFragment("<step class='- topic/li task/step '><cmd class='- topic/ph task/cmd '>before</cmd></step>"));
        pushActions.put(new MoveKey("#X/B", "pushafter"),
                getFragment("<step class='- topic/li task/step '><cmd class='- topic/ph task/cmd '>after</cmd></step>"));
        pushActions.put(new MoveKey("#X/C", "pushreplace"),
                getFragment("<step class='- topic/li task/step ' id='C'><cmd class='- topic/ph task/cmd '>replace</cmd></step>"));

        final ConrefPushParser parser = new ConrefPushParser();
        parser.setLogger(new TestUtils.TestLogger());
        parser.setJob(new Job(tempDir, new StreamStore(tempDir, new XMLUtils())));
        parser.setMoveTable(pushActions);
        parser.read(targetFile);

        TestUtils.assertXMLEqual(
                new InputSource(new File(expDir, "conrefpush_stub2.xml").toURI().toString()),
                new InputSource(targetFile.toURI().toString())
        );
    }

    private DocumentFragment getFragment(final String text) {
        final Document doc = buildControlDocument(text);
        final DocumentFragment fragment = doc.createDocumentFragment();
        fragment.appendChild(doc.removeChild(doc.getDocumentElement()));
        return fragment;
    }

    @After
    public void tearDown() throws IOException {
        TestUtils.forceDelete(tempDir);
    }
}
