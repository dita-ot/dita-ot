/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.reader;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.dita.dost.util.URLUtils.*;

import java.io.File;
import java.util.Map;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import org.dita.dost.TestUtils;
import org.dita.dost.reader.GenListModuleReader.Reference;
import org.dita.dost.util.FilterUtils;
import org.dita.dost.util.KeyDef;
import org.dita.dost.util.OutputUtils;

/**
 * @author william
 *
 */
public class TestGenListModuleReader {

    public static GenListModuleReader reader;
    
    private static final File baseDir = TestUtils.getResourceDir(TestGenListModuleReader.class);
    private static final File srcDir = new File(baseDir, "src");
    private static final File inputDir = new File(srcDir, "maps");
    private static final File rootFile = new File(inputDir, "root-map-01.ditamap");

    @BeforeClass
    public static void setUp() throws Exception{
        //parser = new ConrefPushParser();
        File ditaDir = new File("src" + File.separator + "main").getAbsoluteFile();

        final boolean validate = false;
        reader = new GenListModuleReader();
        reader.setLogger(new TestUtils.TestLogger());
        reader.initXMLReader(ditaDir, validate, new File(rootFile.getPath()).getCanonicalFile(), true);
        reader.setFilterUtils(new FilterUtils());
        reader.setOutputUtils(new OutputUtils());
    }

    @Test
    public void testParse() throws Exception{
        //String inputDir = baseDir + "/maps";
        //inputDir = baseDir;
        //String inputMap = inputDir + "/root-map-01.ditamap";

        reader.parse(new File(rootFile.getPath()));
        final Set<File> conref = reader.getConrefTargets();
        final Set<File> chunk = reader.getChunkTopicSet();
        final Map<File, File> copytoMap = reader.getCopytoMap();
        final Set<File> hrefTargets = reader.getHrefTargets();
        final Set<File> hrefTopic =reader.getHrefTopicSet();
        final Set<File> copytoSet = reader.getIgnoredCopytoSourceSet();
        final Map<String, KeyDef> keyDMap = reader.getKeysDMap();
        final Set<File> nonConref = reader.getNonConrefCopytoTargets();
        final Set<Reference> nonCopyTo = reader.getNonCopytoResult();
        final Set<File> outDita = reader.getOutDitaFilesSet();
        final Set<File> outFiles = reader.getOutFilesSet();
        final Set<File> resourceOnlySet = reader.getResourceOnlySet();
        final Set<File> subsidiaryTargets = reader.getSubsidiaryTargets();

        assertEquals(0, conref.size());

        assertEquals(0, chunk.size());

        assertEquals(0, copytoMap.size());

        assertTrue(hrefTargets.contains(new File(".." + File.separator + "topics" + File.separator + "xreffin-topic-1.xml")));
        assertTrue(hrefTargets.contains(new File(".." + File.separator + "topics" + File.separator + "target-topic-c.xml")));
        assertTrue(hrefTargets.contains(new File(".." + File.separator + "topics" + File.separator + "target-topic-a.xml")));

        assertTrue(hrefTopic.contains(new File(".." + File.separator + "topics" + File.separator + "xreffin-topic-1.xml")));
        assertTrue(hrefTopic.contains(new File(".." + File.separator + "topics" + File.separator + "target-topic-c.xml")));
        assertTrue(hrefTopic.contains(new File(".." + File.separator + "topics" + File.separator + "target-topic-a.xml")));

        assertEquals(0, copytoSet.size());

        assertEquals(toURI(".." + File.separator + "topics" + File.separator + "target-topic-c.xml") ,keyDMap.get("target_topic_2").href);
        assertEquals(toURI(".." + File.separator + "topics" + File.separator + "target-topic-a.xml") ,keyDMap.get("target_topic_1").href);

        assertTrue(nonConref.contains(new File(".." + File.separator + "topics" + File.separator + "xreffin-topic-1.xml")));
        assertTrue(nonConref.contains(new File(".." + File.separator + "topics" + File.separator + "target-topic-c.xml")));
        assertTrue(nonConref.contains(new File(".." + File.separator + "topics" + File.separator + "target-topic-a.xml")));

        assertTrue(nonCopyTo.contains(new Reference(".." + File.separator + "topics" + File.separator + "xreffin-topic-1.xml")));
        assertTrue(nonCopyTo.contains(new Reference(".." + File.separator + "topics" + File.separator + "target-topic-c.xml")));
        assertTrue(nonCopyTo.contains(new Reference(".." + File.separator + "topics" + File.separator + "target-topic-a.xml")));

        assertTrue(outDita.contains(new File(".." + File.separator + "topics" + File.separator + "xreffin-topic-1.xml")));
        assertTrue(outDita.contains(new File(".." + File.separator + "topics" + File.separator + "target-topic-c.xml")));
        assertTrue(outDita.contains(new File(".." + File.separator + "topics" + File.separator + "target-topic-a.xml")));

        assertTrue(outFiles.contains(new File(".." + File.separator + "topics" + File.separator + "xreffin-topic-1.xml")));
        assertTrue(outFiles.contains(new File(".." + File.separator + "topics" + File.separator + "target-topic-c.xml")));
        assertTrue(outFiles.contains(new File(".." + File.separator + "topics" + File.separator + "target-topic-a.xml")));

        assertEquals(0, resourceOnlySet.size());

        assertEquals(0, subsidiaryTargets.size());
    }
    
}
