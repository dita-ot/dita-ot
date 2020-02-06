/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2016 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.chunk;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.io.FilenameUtils;
import org.dita.dost.TestUtils;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.util.Job;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.dita.dost.TestUtils.assertXMLEqual;
import static org.dita.dost.util.Constants.ANT_INVOKER_EXT_PARAM_TRANSTYPE;

public class ChunkModuleTest {

    @Test
    public void testCase1() {
        test("case1.ditamap");
    }

    @Test
    public void testCase2() {
        test("case2.ditamap");
    }

    @Test
    public void testCase3() {
        test("case3.ditamap");
    }

    @Test
    public void testCase4() {
        test("case4.ditamap");
    }

    @Test
    public void testCase5() {
        test("case5.ditamap");
    }

    @Test
    public void testCase6() {
        test("case6.ditamap");
    }

    @Test
    public void testCase7() {
        test("case7.ditamap");
    }

    @Test
    public void testConflictByTopic() {
        test("conflict_by_topic.ditamap");
    }

    @Test
    public void testCopyTo1() {
        test("copy_to1.ditamap");
    }

    @Test
    public void testCopyTo2() {
        test("copy_to2.ditamap");
    }

    @Test
    public void testConflictSameId() {
        test("conflict_same_id.ditamap");
    }

    @Test
    public void testConflictToContent() {
        test("conflict_to_content.ditamap");
    }

    @Test
    public void testExternalChunk() {
        test("external_chunk.ditamap");
    }

    @Test
    public void testLink1() {
        test("link1.ditamap");
    }

    @Test
    public void testLink2() {
        test("link2.ditamap");
    }

    @Test
    public void testAnchor1() {
        test("anchor1.ditamap");
    }

    @Test
    public void testAnchor2() {
        test("anchor2.ditamap");
    }

    @Test
    @Ignore
    // FIXME
    public void testAttributeMap1() {
        test("Attribute_map1.ditamap");
    }

    @Test
    public void testAttributeMap2() {
        test("Attribute_map2.ditamap");
    }

    @Test
    public void testAttributeMap3() {
        test("Attribute_map3.ditamap");
    }

    @Test
    public void testAttributeMap4() {
        test("Attribute_map4.ditamap");
    }

    @Test
    public void testAttributeMap5() {
        test("Attribute_map5.ditamap");
    }

    @Test
    public void testAttributeMap6() {
        test("Attribute_map6.ditamap");
    }

    @Test
    public void testAttributeMap7() {
        test("Attribute_map7.ditamap");
    }

    @Test
    public void testAttributeMap8() {
        test("Attribute_map8.ditamap");
    }

    @Test
    public void testAttributeMap9() {
        test("Attribute_map9.ditamap");
    }

    @Test
    public void testAttributeMap10() {
        test("Attribute_map10.ditamap");
    }

    @Test
    public void testAttributeMap11() {
        test("Attribute_map11.ditamap");
    }

    @Test
    public void testByTopicMap2() {
        test("ByTopic_map2.ditamap");
    }

    @Test
    public void testByTopicMap3() {
        test("ByTopic_map3.ditamap");
    }

    @Test
    public void testByTopicMap4() {
        test("ByTopic_map4.ditamap");
    }

    @Test
    public void testByTopicMap5() {
        test("ByTopic_map5.ditamap");
    }

    @Test
    public void testByTopicMap6() {
        test("ByTopic_map6.ditamap");
    }

    @Test
    public void testByTopicMap7() {
        test("ByTopic_map7.ditamap");
    }

    @Test
    public void testByTopicBatseparate0() {
        test("ByTopic_batseparate0.ditamap");
    }

    @Test
    public void testFixChunkMap1() {
        test("FixChunk_map1.ditamap");
    }

    @Test
    public void testFixChunkMap2() {
        test("FixChunk_map2.ditamap");
    }

    @Test
    public void testFixChunkMap3() {
        test("FixChunk_map3.ditamap");
    }

    @Test
    public void testFixChunkMap4() {
        test("FixChunk_map4.ditamap");
    }

    @Test
    public void testFixChunkMap5() {
        test("FixChunk_map5.ditamap");
    }

    @Test
    public void testFixChunkMap6() {
        test("FixChunk_map6.ditamap");
    }

    @Test
    public void testFixChunkMap7() {
        test("FixChunk_map7.ditamap");
    }

    @Test
    public void testFixChunkMap8() {
        test("FixChunk_map8.ditamap");
    }

    @Test
    public void testChunkDuplicateToContent() {
        test("chunk_duplicate_tocontent.ditamap");
    }

    @Test
    public void testChunkHogsMemory() {
        test("chunk_hogs_memory.ditamap");
    }

    @Test
    public void testChunkMapToContent() {
        test("chunk_map_tocontent.ditamap");
    }

    @Test
    public void testChunkRewriteToContent() {
        test("chunk_rewrite_tocontent.ditamap");
    }

    @Test
    public void testTopicgroupChunk() {
        test("topicgroup_chunk.ditamap");
    }

    @Test
    public void testUnwareChunkContent() {
        test("unware_chunk_content.ditamap");
    }

    @Test
    public void testUnwareChunkContent2() {
        test("unware_chunk_content2.ditamap");
    }
    
    @Test
    public void testWith_non_dita() {
        test("with_non_dita.ditamap");
    }

    private final File resourceDir = TestUtils.getResourceDir("chunk");
    private final File expBaseDir = new File(resourceDir, "exp");
    private File tempBaseDir;
    private final DocumentBuilder builder;

    public ChunkModuleTest() {
        try {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    @Before
    public void setUp() throws Exception {
        tempBaseDir = TestUtils.createTempDir(getClass());
        TestUtils.copy(new File(resourceDir, "src"), tempBaseDir);
    }

    @After
    public void tearDown() throws Exception {
        TestUtils.forceDelete(tempBaseDir);
    }

    private void test(final String testCase) {
        final String testName = FilenameUtils.getBaseName(testCase);
        final File tempDir = new File(tempBaseDir, testName);
        final File expDir = new File(expBaseDir, testName);
        try {
            final ChunkModule chunkModule = new ChunkModule();
            final Job job = new Job(tempDir);
            chunkModule.setJob(job);
            final TestUtils.CachingLogger logger = new TestUtils.CachingLogger(true);
            chunkModule.setLogger(logger);

            final AbstractPipelineInput input = getAbstractPipelineInput();
            chunkModule.execute(input);

            compare(tempDir, expDir);

            logger.getMessages().stream()
                    .filter(m -> m.level == TestUtils.CachingLogger.Message.Level.ERROR)
                    .forEach(m -> System.err.println(m.level + ": " + m.message));
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private AbstractPipelineInput getAbstractPipelineInput() {
        final AbstractPipelineInput input = new PipelineHashIO();
        input.setAttribute(ANT_INVOKER_EXT_PARAM_TRANSTYPE, "html5");
        return input;
    }

    private static final Set<String> IGNORE = ImmutableSet.of(".job.xml", ".DS_Store");

    private void compare(File actDir, File expDir) throws SAXException, IOException {
        final File[] exps = expDir.listFiles();
        for (final File exp : exps) {
            if (exp.isDirectory()) {
                compare(new File(expDir, exp.getName()), new File(actDir, exp.getName()));
            } else if (IGNORE.contains(exp.getName())) {
                // skip
            } else {
                final Document expDoc = getDocument(exp);
                final Document actDoc = getDocument(new File(actDir, exp.getName()));
//                assertXMLEqual("Comparing " + exp + " to " + new File(actDir, exp.getName()) + ":",
//                        expDoc, actDoc);
                assertXMLEqual(expDoc, actDoc);
            }
        }
    }

    private Document getDocument(final File file) {
        try {
            final Document doc = builder.parse(file);
            doc.normalize();
            normalizeSpace(doc.getDocumentElement());
            return doc;
        } catch (SAXException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void normalizeSpace(final Node node) {
        switch (node.getNodeType()) {
            case Node.ELEMENT_NODE:
                for (final Node n : getChildren(node)) {
                    normalizeSpace(n);
                }
                break;
            case Node.TEXT_NODE:
                final String v = node.getNodeValue().replaceAll("\\s+", " ").trim();
                if (v.isEmpty()) {
                    node.getParentNode().removeChild(node);
                } else {
                    node.setNodeValue(v);
                }
                break;
        }
    }

    private List<Node> getChildren(final Node node) {
        final List<Node> res = new ArrayList<>();
        final NodeList ns = node.getChildNodes();
        for (int i = 0; i < ns.getLength(); i++) {
            res.add(ns.item(i));
        }
        return res;
    }

}
