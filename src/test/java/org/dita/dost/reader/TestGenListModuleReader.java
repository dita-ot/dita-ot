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
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Set;

import org.dita.dost.util.*;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.dita.dost.TestUtils;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.reader.GenListModuleReader.Reference;

/**
 * @author william
 *
 */
public class TestGenListModuleReader {

    public static GenListModuleReader reader;
    private static XMLReader parser;
    
    private static final File baseDir = TestUtils.getResourceDir(TestGenListModuleReader.class);
    private static final File srcDir = new File(baseDir, "src");
    private static final File inputDir = new File(srcDir, "maps");
    private static final File rootFile = new File(inputDir, "root-map-01.ditamap");
    private static File tempDir;

    @BeforeClass
    public static void setUp() throws Exception{
        tempDir = TestUtils.createTempDir(TestGenListModuleReader.class);
        //parser = new ConrefPushParser();
        File ditaDir = new File("src" + File.separator + "main").getAbsoluteFile();

        final boolean validate = false;
        reader = new GenListModuleReader();
        reader.setLogger(new TestUtils.TestLogger());
//        reader.initXMLReader(ditaDir, validate, new File(rootFile.getPath()).getCanonicalFile(), true);
        reader.setCurrentFile(rootFile.toURI());
        reader.setInputDir(rootFile.getParentFile().toURI());
        reader.setJob(new Job(tempDir));
        
        reader.setContentHandler(new DefaultHandler());
        
        initXMLReader(ditaDir, validate, new File(rootFile.getPath()).getCanonicalFile());
        parser.setContentHandler(reader);
    }

    @Test
    public void testParse() throws Exception{
        //String inputDir = baseDir + "/maps";
        //inputDir = baseDir;
        //String inputMap = inputDir + "/root-map-01.ditamap";
        
        parser.parse(new File(rootFile.getPath()).toURI().toString());
        
        final Set<URI> conref = reader.getConrefTargets();
        final Set<URI> chunk = reader.getChunkTopicSet();
        final Map<URI, URI> copytoMap = reader.getCopytoMap();
        final Set<URI> hrefTargets = reader.getHrefTargets();
        final Set<URI> hrefTopic =reader.getHrefTopicSet();
        final Set<URI> copytoSet = reader.getIgnoredCopytoSourceSet();
        final Set<URI> nonConref = reader.getNonConrefCopytoTargets();
        final Set<Reference> nonCopyTo = reader.getNonCopytoResult();
        final Set<URI> outDita = reader.getOutDitaFilesSet();
        final Set<URI> outFiles = reader.getOutFilesSet();
        final Set<URI> resourceOnlySet = reader.getResourceOnlySet();
        final Set<URI> subsidiaryTargets = reader.getSubsidiaryTargets();

        assertEquals(0, conref.size());

        assertEquals(0, chunk.size());

        assertEquals(0, copytoMap.size());

        assertTrue(hrefTargets.contains(new File(srcDir, "topics" + File.separator + "xreffin-topic-1.xml").toURI()));
        assertTrue(hrefTargets.contains(new File(srcDir, "topics" + File.separator + "target-topic-c.xml").toURI()));
        assertTrue(hrefTargets.contains(new File(srcDir, "topics" + File.separator + "target-topic-a.xml").toURI()));

        assertTrue(hrefTopic.contains(new File(srcDir, "topics" + File.separator + "xreffin-topic-1.xml").toURI()));
        assertTrue(hrefTopic.contains(new File(srcDir, "topics" + File.separator + "target-topic-c.xml").toURI()));
        assertTrue(hrefTopic.contains(new File(srcDir, "topics" + File.separator + "target-topic-a.xml").toURI()));

        assertEquals(0, copytoSet.size());
        
        assertTrue(nonConref.contains(new File(srcDir, "topics" + File.separator + "xreffin-topic-1.xml").toURI()));
        assertTrue(nonConref.contains(new File(srcDir, "topics" + File.separator + "target-topic-c.xml").toURI()));
        assertTrue(nonConref.contains(new File(srcDir, "topics" + File.separator + "target-topic-a.xml").toURI()));

        assertTrue(nonCopyTo.contains(new Reference(new File(srcDir, "topics" + File.separator + "xreffin-topic-1.xml").toURI())));
        assertTrue(nonCopyTo.contains(new Reference(new File(srcDir, "topics" + File.separator + "target-topic-c.xml").toURI())));
        assertTrue(nonCopyTo.contains(new Reference(new File(srcDir, "topics" + File.separator + "target-topic-a.xml").toURI())));

        assertTrue(outDita.contains(new File(srcDir, "topics" + File.separator + "xreffin-topic-1.xml").toURI()));
        assertTrue(outDita.contains(new File(srcDir, "topics" + File.separator + "target-topic-c.xml").toURI()));
        assertTrue(outDita.contains(new File(srcDir, "topics" + File.separator + "target-topic-a.xml").toURI()));

        assertTrue(outFiles.contains(new File(srcDir, "topics" + File.separator + "xreffin-topic-1.xml").toURI()));
        assertTrue(outFiles.contains(new File(srcDir, "topics" + File.separator + "target-topic-c.xml").toURI()));
        assertTrue(outFiles.contains(new File(srcDir, "topics" + File.separator + "target-topic-a.xml").toURI()));

        assertEquals(0, resourceOnlySet.size());

        assertEquals(0, subsidiaryTargets.size());
    }
    
    private static void initXMLReader(final File ditaDir, final boolean validate, final File rootFile) throws SAXException, IOException {
        parser = XMLUtils.getXMLReader();
        // to check whether the current parsing file's href value is out of inputmap.dir
//        reader.setFeature(FEATURE_NAMESPACE_PREFIX, true);
        if (validate == true) {
            parser.setFeature(FEATURE_VALIDATION, true);
            try {
                parser.setFeature(FEATURE_VALIDATION_SCHEMA, true);
            } catch (final SAXNotRecognizedException e) {
                // Not Xerces, ignore exception
            }
        } else {
            final String msg = MessageUtils.getInstance().getMessage("DOTJ037W").toString();
//            logger.logWarn(msg);
        }
        // set grammar pool flag
//        if (gramcache) {
//            GrammarPoolManager.setGramCache(gramcache);
//            final XMLGrammarPool grammarPool = GrammarPoolManager.getGrammarPool();
//            try {
//                reader.setProperty("http://apache.org/xml/properties/internal/grammar-pool", grammarPool);
//                logger.logInfo("Using Xerces grammar pool for DTD and schema caching.");
//            } catch (final Exception e) {
//                logger.logWarn("Failed to set Xerces grammar pool for parser: " + e.getMessage());
//            }
//        }
        CatalogUtils.setDitaDir(ditaDir);
        parser.setEntityResolver(CatalogUtils.getCatalogResolver());
    }
    
}
