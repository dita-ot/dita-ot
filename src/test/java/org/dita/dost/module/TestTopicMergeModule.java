/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.module;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import org.custommonkey.xmlunit.XMLUnit;
import org.dita.dost.TestUtils;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.module.TopicMergeModule;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.util.Job;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class TestTopicMergeModule {

    final File resourceDir = TestUtils.getResourceDir(TestTopicMergeModule.class);
    private File tempDir;

    public TopicMergeModule module;

    //	private AbstractFacade facade;

    private PipelineHashIO pipelineInput;
    final File ditalistfile = new File (resourceDir, "compare.xml");
    File tobecomparefile;
    File temporaryDir;

    @Before
    public void setUp() throws IOException {
        TestUtils.resetXMLUnit();
        XMLUnit.setIgnoreWhitespace(true);
        
        tempDir = TestUtils.createTempDir(getClass());

        //		facade = new PipelineFacade();
        pipelineInput = new PipelineHashIO();
        
        temporaryDir = new File(tempDir, "temp");
        TestUtils.copy(new File(resourceDir, "temp"), temporaryDir);

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
        topicmergemodule.setJob(new Job(temporaryDir));
        topicmergemodule.execute(pipelineInput);
        
        assertXMLEqual(new InputSource(ditalistfile.toURI().toString()),
                       new InputSource(tobecomparefile.toURI().toString()));
    }

    @After
    public void tearDown() throws IOException {
        TestUtils.forceDelete(tempDir);
    }

}
