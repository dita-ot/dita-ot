/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */
package org.dita.dost.reader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.dita.dost.TestUtils;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.reader.GenListModuleReader.Reference;
import org.dita.dost.util.CatalogUtils;
import org.dita.dost.util.FilterUtils;
import org.dita.dost.util.Job;
import org.dita.dost.util.KeyDef;
import org.dita.dost.util.StringUtils;

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
        reader.setInputFile(rootFile.toURI());
        reader.setCurrentDir(null);
        reader.setJob(new Job(tempDir));
        
        reader.setContentHandler(new DefaultHandler());
        
        parser = StringUtils.getXMLReader();
        CatalogUtils.setDitaDir(ditaDir);
        parser.setEntityResolver(CatalogUtils.getCatalogResolver());
        parser.setContentHandler(reader);
    }

    @Test
    public void testParse() throws Exception{
        parser.parse(new File(rootFile.getPath()).toURI().toString());
        
        final Map<String, KeyDef> expKeyDefMap = new HashMap<String, KeyDef>();
        expKeyDefMap.put("target_topic_1", new KeyDef("target_topic_1", toURI(".." + File.separator + "topics" + File.separator + "target-topic-a.xml"), ATTR_SCOPE_VALUE_LOCAL, null));
        expKeyDefMap.put("target_topic_2", new KeyDef("target_topic_2", toURI(".." + File.separator + "topics" + File.separator + "target-topic-c.xml"), ATTR_SCOPE_VALUE_LOCAL, null));
        expKeyDefMap.put("target_topic_3", new KeyDef("target_topic_1", toURI(".." + File.separator + "topics" + File.separator + "target-topic-a.xml"), ATTR_SCOPE_VALUE_LOCAL, null));
        expKeyDefMap.put("target_topic_4", new KeyDef("target_topic_1", toURI(".." + File.separator + "topics" + File.separator + "target-topic-a.xml"), ATTR_SCOPE_VALUE_LOCAL, null));
        expKeyDefMap.put("peer", new KeyDef("peer", toURI(".." + File.separator + "topics" + File.separator + "peer.xml"), ATTR_SCOPE_VALUE_PEER, null));
        expKeyDefMap.put("external", new KeyDef("external", toURI("http://www.example.com/external.xml"), ATTR_SCOPE_VALUE_EXTERNAL, null));
        
        assertEquals(expKeyDefMap, reader.getKeysDMap());                
    }
    
}
