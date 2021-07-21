/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2016 Jarno Elovirta
 *
 *  See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.module;

import com.google.common.collect.ImmutableMap;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.s9api.DOMDestination;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.serialize.SerializationProperties;
import net.sf.saxon.trans.XPathException;
import org.dita.dost.TestUtils;
import org.dita.dost.TestUtils.TestLogger;
import org.dita.dost.module.KeyrefModule.ResolveTask;
import org.dita.dost.store.StreamStore;
import org.dita.dost.util.Job;
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.util.Job.FileInfo.Builder;
import org.dita.dost.util.KeyDef;
import org.dita.dost.util.KeyScope;
import org.dita.dost.util.XMLUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import static java.net.URI.create;
import static java.util.Collections.*;
import static org.dita.dost.TestUtils.assertXMLEqual;
import static org.dita.dost.TestUtils.createTempDir;
import static org.junit.Assert.assertEquals;

public class KeyrefModuleTest {

    private static final File baseDir = TestUtils.getResourceDir(KeyrefModuleTest.class);
    private static final URI subMap = new File(baseDir, "src" + File.separator + "submap.ditamap").toURI();

    private final DocumentBuilder b;
    private File tempDir;
    private KeyrefModule module;
    private Job job;
    private FileInfo inputMapFileInfo;
    private final XMLUtils xmlUtils = new XMLUtils();

    public KeyrefModuleTest() throws ParserConfigurationException {
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        b = documentBuilderFactory.newDocumentBuilder();
    }

    @Before
    public void setUp() throws IOException {
        tempDir = createTempDir(KeyrefModuleTest.class);

        module = new KeyrefModule();
        job = new Job(tempDir, new StreamStore(tempDir, new XMLUtils()));
        job.setInputDir(new File(baseDir, "src").toURI());
        job.setInputMap(URI.create("test.ditamap"));
        job.add(new Job.FileInfo.Builder()
                .uri(URI.create("submap.ditamap"))
                .src(new File(baseDir, "src" + File.separator + "submap.ditamap").toURI())
                .result(new File(baseDir, "src" + File.separator + "submap.ditamap").toURI())
                .format("ditamap")
                .hasKeyref(true)
                .build());
        job.add(new FileInfo.Builder()
                .uri(URI.create("topic.dita"))
                .src(new File(baseDir, "src" + File.separator + "topic.dita").toURI())
                .result(new File(baseDir, "src" + File.separator + "topic.dita").toURI())
                .format("dita")
                .hasKeyref(true)
                .build());
        job.add(new FileInfo.Builder()
                .uri(URI.create("res.dita"))
                .src(new File(baseDir, "src" + File.separator + "res.dita").toURI())
                .result(new File(baseDir, "src" + File.separator + "res.dita").toURI())
                .format("dita")
                .hasKeyref(true)
                .build());
        job.add(new FileInfo.Builder()
                .uri(URI.create("res.png"))
                .src(new File(baseDir, "src" + File.separator + "res.png").toURI())
                .result(new File(baseDir, "src" + File.separator + "res.png").toURI())
                .format("png")
                .hasKeyref(true)
                .build());
        module.setJob(job);
        module.setLogger(new TestLogger());
    }

    @After
    public void tearDown() throws IOException {
       TestUtils.forceDelete(tempDir);
    }

    @Test
    public void testAdjustResourceRenames() {
        final KeyScope scope = new KeyScope("scope", "scope",
                ImmutableMap.<String, KeyDef>builder()
                        .put("key", new KeyDef("key", create("target.dita"), null, null, null, null))
                        .build(),
                emptyList());
        final List<ResolveTask> src = singletonList(
                new ResolveTask(
                        scope,
                        new Builder().uri(create("target.dita")).build(),
                        new Builder().uri(create("target-1.dita")).build()));
        final List<ResolveTask> act = module.adjustResourceRenames(src);

        final KeyScope exp = new KeyScope("scope", "scope",
                ImmutableMap.<String, KeyDef>builder()
                        .put("key", new KeyDef("key", create("target-1.dita"), null, null, null, null))
                        .build(),
                emptyList());

        assertEquals(exp, act.get(0).scope);
    }

    @Test
    public void testRewriteScopeTargets() {
        final KeyScope src = new KeyScope("scope", "scope",
                ImmutableMap.<String, KeyDef>builder()
                        .put("key", new KeyDef("key", create("target.dita"), null, null, null, null))
                        .put("element", new KeyDef("element", create("target.dita#target/element"), null, null, null, null))
                        .build(),
                emptyList());
        final Map<URI, URI> rewrites = ImmutableMap.<URI, URI>builder()
                .put(create("target.dita"), create("target-1.dita"))
                .build();
        final KeyScope act = module.rewriteScopeTargets(src, rewrites);

        final KeyScope exp = new KeyScope("scope", "scope",
                ImmutableMap.<String, KeyDef>builder()
                        .put("key", new KeyDef("key", create("target-1.dita"), null, null, null, null))
                        .put("element", new KeyDef("element", create("target-1.dita#target/element"), null, null, null, null))
                        .build(),
                emptyList());

        assertEquals(exp, act);
    }

    @Test
    public void testWalkMap() throws IOException, SAXException, XPathException {
        inputMapFileInfo = new Builder()
                .uri(create("test.ditamap"))
                .src(new File(baseDir, "src" + File.separator + "test.ditamap").toURI())
                .result(new File(baseDir, "src" + File.separator + "test.ditamap").toURI())
                .format("ditamap")
                .isInput(true)
                .build();
        job.add(inputMapFileInfo);

        final XdmNode src = parse(inputMapFileInfo.src);
        final KeyScope childScope = new KeyScope("A", "A",
                            ImmutableMap.of(
                                    "VAR", new KeyDef("VAR", null, "local", "dita", inputMapFileInfo.src, null),
                                    "A.VAR", new KeyDef("VAR", null, "local", "dita", inputMapFileInfo.src, null)
                            ),
                            EMPTY_LIST
        );
        final KeyScope keyScope =
                new KeyScope("#root", null,
                            ImmutableMap.of(
                                    "VAR", new KeyDef("VAR", null, "local", "dita", inputMapFileInfo.src, null),
                                    "A.VAR", new KeyDef("VAR", null, "local", "dita", inputMapFileInfo.src, null)
                            ),
                            singletonList(childScope)
        );
        final List<ResolveTask> res = new ArrayList<>();
        final XdmDestination destination = new XdmDestination();
        final Receiver receiver = destination.getReceiver(
                xmlUtils.getProcessor().getUnderlyingConfiguration().makePipelineConfiguration(),
                new SerializationProperties());
        receiver.open();
        module.walkMap(inputMapFileInfo, src, singletonList(keyScope), res, receiver);
        receiver.close();

        final Document exp = b.parse(new File(baseDir, "exp" + File.separator + "test.ditamap"));

        final ResolveTask subMapTask = res.stream().filter(r -> r.in.src.equals(subMap)).findFirst().get();
        assertEquals(subMapTask.scope, childScope);

        final Document act = toDocument(destination.getXdmNode());
        assertXMLEqual(exp, act);
    }

	@Test
	public void testWalkMapAndRewriteKeydefHref() throws ParserConfigurationException, IOException, SAXException, URISyntaxException, XPathException {
        inputMapFileInfo = new Builder()
                .uri(create("test2.ditamap"))
                .src(new File(baseDir, "src" + File.separator + "test2.ditamap").toURI())
                .result(new File(baseDir, "src" + File.separator + "test2.ditamap").toURI())
                .format("ditamap")
                .isInput(true)
                .build();
        job.add(inputMapFileInfo);
	    final XdmNode act = parse(inputMapFileInfo.src);
	    final KeyScope childScope1 = new KeyScope("A", "A",
	    		ImmutableMap.of(
	    				"VAR", new KeyDef("VAR", new URI("topic.dita"), "local", "dita", inputMapFileInfo.src, null),
	    				"A.VAR", new KeyDef("A.VAR", new URI("topic.dita"), "local", "dita", inputMapFileInfo.src, null),
	    				"A.VAR2", new KeyDef("A.VAR2", new URI("res.dita"), "local", "dita", inputMapFileInfo.src, null),
	    				"A.VAR3", new KeyDef("A.VAR3", new URI("res.png"), "local", "png", inputMapFileInfo.src, null)
	    				),
	    		EMPTY_LIST
	    		);
	    final KeyScope childScope2 = new KeyScope("B", "B",
	    		ImmutableMap.of(
	    				"VAR", new KeyDef("VAR", new URI("topic.dita"), "local", "dita", inputMapFileInfo.src, null),
	    				"B.VAR", new KeyDef("B.VAR", new URI("topic.dita"), "local", "dita", inputMapFileInfo.src, null),
	    				"B.VAR2", new KeyDef("B.VAR2", new URI("res.dita"), "local", "dita", inputMapFileInfo.src, null),
	    				"B.VAR3", new KeyDef("B.VAR3", new URI("res.png"), "local", "png", inputMapFileInfo.src, null)
	    				),
	    		EMPTY_LIST
	    		);
	    final KeyScope keyScope =
	            new KeyScope("#root", null, new HashMap<String, KeyDef>(), 
	                        Arrays.asList(new KeyScope[] {childScope1, childScope2})
	    );
	    final List<ResolveTask> res = new ArrayList<>();
        final XdmDestination destination = new XdmDestination();
        final Receiver receiver = destination.getReceiver(
                xmlUtils.getProcessor().getUnderlyingConfiguration().makePipelineConfiguration(),
                new SerializationProperties());
        receiver.open();
        module.walkMap(inputMapFileInfo, act, singletonList(keyScope), res, receiver);
        receiver.close();
        
	    ResolveTask task = res.get(0);
	    assertEquals("topic.dita", task.in.file.toString());
	    assertEquals(null, task.scope.name);
	    
	    task = res.get(1);
	    assertEquals("topic.dita", task.in.file.toString());
	    assertEquals("A", task.scope.name);
	    KeyDef keyDef = task.scope.keyDefinition.get("VAR");
	    assertEquals(new URI("topic.dita"), keyDef.href);
	    keyDef = task.scope.keyDefinition.get("A.VAR");
	    assertEquals(new URI("topic-1.dita"), keyDef.href);
	    
	    task = res.get(3);
	    assertEquals("res.png", task.in.file.toString());
	    assertEquals("A", task.scope.name);
	    keyDef = task.scope.keyDefinition.get("A.VAR3");
	    assertEquals(new URI("res.png"), keyDef.href);
	    
	    task = res.get(4);
	    assertEquals("topic.dita", task.in.file.toString());
	    assertEquals("B", task.scope.name);
	    keyDef = task.scope.keyDefinition.get("VAR");
	    assertEquals(new URI("topic.dita"), keyDef.href);
	    keyDef = task.scope.keyDefinition.get("B.VAR");
	    assertEquals(new URI("topic-2.dita"), keyDef.href);
	    
	    task = res.get(5);
	    assertEquals("res.dita", task.in.file.toString());
	    assertEquals("B", task.scope.name);
	    keyDef = task.scope.keyDefinition.get("B.VAR2");
	    assertEquals(new URI("res-1.dita"), keyDef.href);
	    
	    task = res.get(6);
	    assertEquals("res.png", task.in.file.toString());
	    assertEquals("B", task.scope.name);
	    keyDef = task.scope.keyDefinition.get("B.VAR3");
	    assertEquals(new URI("res.png"), keyDef.href);
	}

    private XdmNode parse(final URI in) {
        try {
            final StreamSource source = new StreamSource(in.toString());
            source.setSystemId(in.toString());
            return xmlUtils.getProcessor().newDocumentBuilder().build(source);
        } catch (SaxonApiException e) {
            throw new RuntimeException(e);
        }
    }

    private Document toDocument(final XdmNode node) {
        try {
            final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            final Document document = documentBuilderFactory.newDocumentBuilder().newDocument();
            final DOMDestination destination = new DOMDestination(document);
            final Receiver receiver = destination.getReceiver(xmlUtils.getProcessor().getUnderlyingConfiguration().makePipelineConfiguration(), new SerializationProperties());
            receiver.open();
            receiver.append(node.getUnderlyingNode());
            receiver.close();
            return document;
        } catch (XPathException | ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }
}