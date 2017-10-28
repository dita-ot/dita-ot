/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2016 Jarno Elovirta
 *
 *  See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.module;

import com.google.common.collect.ImmutableMap;
import org.dita.dost.TestUtils;
import org.dita.dost.TestUtils.TestLogger;
import org.dita.dost.module.KeyrefModule.ResolveTask;
import org.dita.dost.util.Job;
import org.dita.dost.util.Job.FileInfo.Builder;
import org.dita.dost.util.KeyDef;
import org.dita.dost.util.KeyScope;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.net.URI.create;
import static java.util.Collections.*;
import static org.dita.dost.TestUtils.assertXMLEqual;
import static org.dita.dost.TestUtils.createTempDir;
import static org.dita.dost.util.Constants.INPUT_DITAMAP_URI;
import static org.junit.Assert.assertEquals;

public class KeyrefModuleTest {

    private static final File baseDir = TestUtils.getResourceDir(KeyrefModuleTest.class);
    private static final URI inputMap = new File(baseDir, "xsrc" + File.separator + "test.ditamap").toURI();

    KeyrefModule module;

    @Before
    public void setUp() throws IOException {
        final File tempDir = createTempDir(KeyrefModuleTest.class);

        module = new KeyrefModule();
        final Job job = new Job(tempDir);
        job.setInputDir(new File(baseDir, "xsrc").toURI());
        job.setInputMap(URI.create("test.ditamap"));
        job.add(new Job.FileInfo.Builder()
                .uri(URI.create("topic.dita"))
                .src(new File(baseDir, "xsrc" + File.separator + "topic.dita").toURI())
                .result(new File(baseDir, "xsrc" + File.separator + "topic.dita").toURI())
                .format("dita")
                .hasKeyref(true)
                .build());
        job.add(new Job.FileInfo.Builder()
                .uri(URI.create("test.ditamap"))
                .src(new File(baseDir, "xsrc" + File.separator + "test.ditamap").toURI())
                .result(new File(baseDir, "xsrc" + File.separator + "test.ditamap").toURI())
                .format("ditamap")
                .build());
        module.setJob(job);
        module.setLogger(new TestLogger());
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
    public void testWalkMap() throws ParserConfigurationException, IOException, SAXException {
        final DocumentBuilder b = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        final Document act = b.parse(new File(baseDir, "src" + File.separator + "test.ditamap"));
        final KeyScope keyScope =
                new KeyScope("#root", null,
                            ImmutableMap.of(
                                    "VAR", new KeyDef("VAR", null, "local", "dita", inputMap, null),
                                    "A.VAR", new KeyDef("VAR", null, "local", "dita", inputMap, null)),
                            singletonList(
                                new KeyScope("A", "A",
                                            ImmutableMap.of(
                                                    "VAR", new KeyDef("VAR", null, "local", "dita", inputMap, null),
                                                    "A.VAR", new KeyDef("VAR", null, "local", "dita", inputMap, null)),
                                            EMPTY_LIST)
        ));
        final List<ResolveTask> res = new ArrayList<>();
        module.walkMap(act.getDocumentElement(), keyScope, res);
        final Document exp = b.parse(new File(baseDir, "exp" + File.separator + "test.ditamap"));

        assertXMLEqual(exp, act);
    }

}