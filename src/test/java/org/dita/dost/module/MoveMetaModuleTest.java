/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2021 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.module;

import com.google.common.collect.ImmutableSet;
import nu.validator.htmlparser.dom.HtmlDocumentBuilder;
import org.apache.tools.ant.*;
import org.dita.dost.TestUtils;
import org.dita.dost.chunk.ChunkModule;
import org.dita.dost.module.AbstractModuleTest;
import org.dita.dost.module.AbstractPipelineModule;
import org.dita.dost.module.MoveMetaModule;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.store.CacheStore;
import org.dita.dost.store.Store;
import org.dita.dost.store.StreamStore;
import org.dita.dost.util.CatalogUtils;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.Job;
import org.dita.dost.util.XMLUtils;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static junit.framework.Assert.assertEquals;
import static org.apache.commons.io.FileUtils.deleteDirectory;
//import static org.dita.dost.MoveMetaModuleTest.Transtype.PREPROCESS;
import static org.dita.dost.TestUtils.assertXMLEqual;
import static org.dita.dost.util.Constants.*;

@RunWith(Parameterized.class)
public class MoveMetaModuleTest extends AbstractModuleTest {
    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"MatadataInheritance_foreign"},
                {"MatadataInheritance_keywords"},
                {"MatadataInheritance_linktext"},
                {"MatadataInheritance_othermeta"},
                {"MatadataInheritance_pemissions"},
                {"MatadataInheritance_pemissions_replace"},
                {"MatadataInheritance_prodinfo"},
                {"MatadataInheritance_publisher"},
                {"MatadataInheritance_resourceid"},
                {"MatadataInheritance_searchtitle"},
                {"MatadataInheritance_shortdesc"},
                {"MatadataInheritance_source"},
                {"MatadataInheritance_source_replace"},
                {"MatadataInheritance_unknown"},
                {"MetadataInheritance_audience"},
                {"MetadataInheritance_author"},
                {"MetadataInheritance_category"},
                {"MetadataInheritance_copyright"},
                {"MetadataInheritance_critdates"},
                {"MetadataInheritance_critdates_replace"},
                {"MetadataInheritance_data"},
                {"MetadataInheritance_dataabout"}
        });
    }

    public MoveMetaModuleTest(String testCase) {
        super(testCase, Collections.emptyMap());
    }

    @Override
    protected AbstractPipelineInput getAbstractPipelineInput() {
        final AbstractPipelineInput input = new PipelineHashIO();
        input.setAttribute(ANT_INVOKER_EXT_PARAM_TRANSTYPE, "html5");
        input.setAttribute(ANT_INVOKER_EXT_PARAM_STYLE,
                Paths.get("src", "main", "plugins", "org.dita.base", "xsl", "preprocess", "mappull.xsl").toString());
        return input;
    }

    @Override
    protected AbstractPipelineModule getModule(final File tempDir) {
        return new MoveMetaModule();
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        final XMLUtils xmlUtils = new XMLUtils();
        final DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
        f.setNamespaceAware(true);
        final DocumentBuilder b = f.newDocumentBuilder();
        for (File file : tempDir.listFiles((dir, name) -> name.endsWith("dita") || name.endsWith("ditamap"))) {
            final Document d = b.parse(file);
            d.appendChild(d.createProcessingInstruction("workdir-uri", tempDir.toURI().toString()));
            xmlUtils.writeDocument(d, file);
        }
    }

    @Test
    @Ignore
    public void parallelFile() {
        chunkModule.setParallel(true);
        test();
    }

    @Test
    @Ignore
    public void serialMemory() throws IOException {
        job = new Job(tempDir, new CacheStore(tempDir, xmlUtils));
        chunkModule.setJob(job);
        test();
    }

    @Test
    @Ignore
    public void parallelMemory() throws IOException {
        job = new Job(tempDir, new CacheStore(tempDir, xmlUtils));
        chunkModule.setJob(job);
        chunkModule.setParallel(true);
        test();
    }
}
