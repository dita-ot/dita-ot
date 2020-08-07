/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2010 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.module;

import static org.dita.dost.TestUtils.assertXMLEqual;
import static org.dita.dost.util.Constants.ATTR_FORMAT_VALUE_DITAMAP;
import static org.dita.dost.util.Constants.INPUT_DIR_URI;
import static org.dita.dost.util.Constants.INPUT_DITAMAP_URI;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;

import org.dita.dost.TestUtils;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.module.TopicMergeModule;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.store.StreamStore;
import org.dita.dost.util.Job;
import org.dita.dost.util.XMLUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class TestTopicMergeModule {

    private final File resourceDir = TestUtils.getResourceDir(TestTopicMergeModule.class);
    private File tempDir;

    public TopicMergeModule module;

    //    private AbstractFacade facade;

    private PipelineHashIO pipelineInput;
    private final File ditalistfile = new File (resourceDir, "compare.xml");
    private File tobecomparefile;
    private File temporaryDir;
    private Job job;

    @Before
    public void setUp() throws IOException {
        tempDir = TestUtils.createTempDir(getClass());

        //        facade = new PipelineFacade();
        pipelineInput = new PipelineHashIO();
        
        temporaryDir = new File(tempDir, "temp");
        final File srcDir = new File(tempDir, "src");
        srcDir.mkdirs();
        TestUtils.copy(new File(resourceDir, "temp"), temporaryDir);

        job = new Job(temporaryDir, new StreamStore(temporaryDir, new XMLUtils()));
        job.setInputMap(URI.create("test.ditamap"));
        job.add(new Job.FileInfo.Builder()
                .src(new File(srcDir, "test.ditamap").toURI())
                .uri(URI.create("test.ditamap"))
                .format(ATTR_FORMAT_VALUE_DITAMAP)
                .isInput(true)
                .build());
        job.setProperty(INPUT_DIR_URI, srcDir.toURI().toString());

        final File inputMap = new File(temporaryDir, "test.ditamap");
        
        final File outDir = new File(tempDir, "out");
        tobecomparefile = new File(outDir, "tobecompared.xml");

        pipelineInput.setAttribute("inputmap", inputMap.getPath());
        pipelineInput.setAttribute("basedir", resourceDir.getPath());
        pipelineInput.setAttribute("inputdir", temporaryDir.getPath());
        pipelineInput.setAttribute("output", tobecomparefile.getPath());
        pipelineInput.setAttribute("outputdir", outDir.getPath());
        pipelineInput.setAttribute("tempDir", temporaryDir.getPath());
        pipelineInput.setAttribute("ditadir", new File("src" + File.separator + "main").getAbsolutePath());
        pipelineInput.setAttribute("indextype", "xhtml");
        pipelineInput.setAttribute("encoding", "en-US");
        pipelineInput.setAttribute("targetext", ".html");
        pipelineInput.setAttribute("validate", "false");
        pipelineInput.setAttribute("generatecopyouter", "1");
        pipelineInput.setAttribute("outercontrol", "warn");
        pipelineInput.setAttribute("onlytopicinmap", "false");
        pipelineInput.setAttribute("ditalist", new File(temporaryDir, "dita.list").getPath());
        pipelineInput.setAttribute("maplinks", new File(temporaryDir, "maplinks.unordered").getPath());

    }

    @Test
    public void testtopicmergemodule() throws DITAOTException, IOException, SAXException
    {
        final TopicMergeModule topicmergemodule = new TopicMergeModule();
        topicmergemodule.setLogger(new TestUtils.TestLogger());
        topicmergemodule.setJob(job);
        topicmergemodule.execute(pipelineInput);
        
        assertXMLEqual(new InputSource(ditalistfile.toURI().toString()),
                       new InputSource(tobecomparefile.toURI().toString()));
    }

    @After
    public void tearDown() throws IOException {
        TestUtils.forceDelete(tempDir);
    }

}
