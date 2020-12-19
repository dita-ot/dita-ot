/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2016 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.module;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.io.FilenameUtils;
import org.dita.dost.TestUtils;
import org.dita.dost.TestUtils.CachingLogger;
import org.dita.dost.TestUtils.CachingLogger.Message;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.store.StreamStore;
import org.dita.dost.util.Job;
import org.dita.dost.util.XMLUtils;
import org.junit.After;
import org.junit.Before;
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

public abstract class AbstractModuleTest {

    private final File resourceDir = TestUtils.getResourceDir(getClass());
    private final File expBaseDir = new File(resourceDir, "exp");
    private File tempBaseDir;
    private final DocumentBuilder builder;

    public AbstractModuleTest() {
        try {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
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

    @Before
    public void setUp() throws Exception {
        tempBaseDir = TestUtils.createTempDir(getClass());
        TestUtils.copy(new File(resourceDir, "src"), tempBaseDir);
    }

    @After
    public void tearDown() throws Exception {
        TestUtils.forceDelete(tempBaseDir);
    }

    public void test(final String testCase) {
        final String testName = FilenameUtils.getBaseName(testCase);
        final File tempDir = new File(tempBaseDir, testName);
        final File expDir = new File(expBaseDir, testName);
        try {
            final AbstractPipelineModule chunkModule = getModule(tempDir);
            final Job job = new Job(tempDir, new StreamStore(tempDir, new XMLUtils()));
            chunkModule.setJob(job);
            final CachingLogger logger = new CachingLogger(true);
            chunkModule.setLogger(logger);

            final AbstractPipelineInput input = getAbstractPipelineInput();
            chunkModule.execute(input);

            compare(tempDir, expDir);

            logger.getMessages().stream()
                    .filter(m -> m.level == Message.Level.ERROR)
                    .forEach(m -> System.err.println(m.level + ": " + m.message));
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    abstract AbstractPipelineInput getAbstractPipelineInput();

    abstract AbstractPipelineModule getModule(File tempDir);

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

}
