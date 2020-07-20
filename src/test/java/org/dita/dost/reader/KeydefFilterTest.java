/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2014 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.reader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.dita.dost.store.StreamStore;
import org.dita.dost.util.*;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.dita.dost.TestUtils;

public class KeydefFilterTest {

    public static KeydefFilter reader;
    private static XMLReader parser;
    
    private static final File baseDir = TestUtils.getResourceDir(KeydefFilterTest.class);
    private static final File srcDir = new File(baseDir, "src");
    private static final File inputDir = new File(srcDir, "maps");
    private static final File rootFile = new File(inputDir, "root-map-01.ditamap");
    private static File tempDir;

    @Before
    public void setUp() throws Exception{
        tempDir = TestUtils.createTempDir(KeydefFilterTest.class);
        File ditaDir = new File("src" + File.separator + "main").getAbsoluteFile();

        reader = new KeydefFilter();
        reader.setLogger(new TestUtils.TestLogger());
        reader.setCurrentFile(rootFile.toURI());
        reader.setCurrentDir(inputDir.toURI());
        reader.setJob(new Job(tempDir, new StreamStore(tempDir, new XMLUtils())));
        
        reader.setContentHandler(new DefaultHandler());
        
        parser = XMLUtils.getXMLReader();
        CatalogUtils.setDitaDir(ditaDir);
        parser.setEntityResolver(CatalogUtils.getCatalogResolver());
        parser.setContentHandler(reader);
    }

    @Test
    public void testParse() throws Exception{
        parser.parse(new File(rootFile.getPath()).toURI().toString());
        
        final Map<String, KeyDef> expKeyDefMap = new HashMap<String, KeyDef>();
        expKeyDefMap.put("target_topic_1", new KeyDef("target_topic_1", new File(srcDir, "topics" + File.separator + "target-topic-a.xml").toURI(), ATTR_SCOPE_VALUE_LOCAL, null, null,null));
        expKeyDefMap.put("target_topic_2", new KeyDef("target_topic_2", new File(srcDir, "topics" + File.separator + "target-topic-c.xml").toURI(), ATTR_SCOPE_VALUE_LOCAL, null, null,null));
        expKeyDefMap.put("target_topic_3", new KeyDef("target_topic_1", new File(srcDir, "topics" + File.separator + "target-topic-a.xml").toURI(), ATTR_SCOPE_VALUE_LOCAL, null, null,null));
        expKeyDefMap.put("target_topic_4", new KeyDef("target_topic_1", new File(srcDir, "topics" + File.separator + "target-topic-a.xml").toURI(), ATTR_SCOPE_VALUE_LOCAL, null, null,null));
        expKeyDefMap.put("peer", new KeyDef("peer", toURI("../topics/peer.xml"), ATTR_SCOPE_VALUE_PEER, null,null, null));
        expKeyDefMap.put("external", new KeyDef("external", toURI("http://www.example.com/external.xml"), ATTR_SCOPE_VALUE_EXTERNAL, null, null,null));
        
        assertEquals(expKeyDefMap, reader.getKeysDMap());                
    }
    
}
