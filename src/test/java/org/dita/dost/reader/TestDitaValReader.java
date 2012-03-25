/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.reader;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.dita.dost.TestUtils;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.pipeline.PipelineFacade;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.reader.DitaValReader;
import org.dita.dost.util.FilterUtils.Action;
import org.dita.dost.util.FilterUtils.FilterKey;
import org.dita.dost.util.Constants;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class TestDitaValReader {

    private final File resourceDir = TestUtils.testStub;
    private File tempDir;

    public DitaValReader reader;
    private final File baseDir = new File(resourceDir, "DITA-OT1.5");

    @Before
    public void setUp() throws Exception{
        tempDir = TestUtils.createTempDir(getClass());
        reader = new DitaValReader();

        final PipelineFacade facade = new PipelineFacade();
        facade.setLogger(new TestUtils.TestLogger());
        final PipelineHashIO pipelineInput = new PipelineHashIO();

        final File inputDir = new File("DITAVAL");
        final File inputMap = new File(inputDir, "DITAVAL_testdata1.ditamap");
        final File outDir = new File(inputDir, "out");
        pipelineInput.setAttribute("inputmap", inputMap.getPath());
        pipelineInput.setAttribute("basedir", baseDir.getPath());
        pipelineInput.setAttribute("inputdir", inputDir.getPath());
        pipelineInput.setAttribute("outputdir", outDir.getAbsolutePath());
        pipelineInput.setAttribute("tempDir", tempDir.getPath());
        pipelineInput.setAttribute("ditadir", new File("src" + File.separator + "main").getAbsolutePath());
        pipelineInput.setAttribute("ditaext", ".xml");
        pipelineInput.setAttribute("indextype", "xhtml");
        pipelineInput.setAttribute("encoding", "en-US");
        pipelineInput.setAttribute("targetext", ".html");
        pipelineInput.setAttribute("validate", "false");
        pipelineInput.setAttribute("generatecopyouter", "1");
        pipelineInput.setAttribute("outercontrol", "warn");
        pipelineInput.setAttribute("onlytopicinmap", "false");
        pipelineInput.setAttribute("ditalist", new File(tempDir, "dita.list").getPath());
        pipelineInput.setAttribute("maplinks", new File(tempDir, "maplinks.unordered").getPath());
        pipelineInput.setAttribute("transtype", "xhtml");
        pipelineInput.setAttribute(Constants.ANT_INVOKER_EXT_PARAN_SETSYSTEMID, "no");
        facade.execute("GenMapAndTopicList", pipelineInput);


    }

    @Test
    public void testRead() throws DITAOTException{
        final File ditavalFile = new File(baseDir, "DITAVAL" + File.separator + "DITAVAL_1.ditaval");
        reader.read(ditavalFile.getAbsolutePath());
        final Map<FilterKey, Action> map = reader.getFilterMap();
        assertEquals(Action.INCLUDE, map.get(new FilterKey("audience", "Cindy")));
        assertEquals(Action.FLAG, map.get(new FilterKey("produt", "p1")));
        assertEquals(Action.EXCLUDE, map.get(new FilterKey("product", "ABase_ph")));
        assertEquals(Action.INCLUDE, map.get(new FilterKey("product", "AExtra_ph")));
        assertEquals(Action.EXCLUDE, map.get(new FilterKey("product", "Another_ph")));
        assertEquals(Action.FLAG, map.get(new FilterKey("platform", "Windows")));
        assertEquals(Action.FLAG, map.get(new FilterKey("platform", "Linux")));
        assertEquals(Action.EXCLUDE, map.get(new FilterKey("keyword", "key1")));
        assertEquals(Action.FLAG, map.get(new FilterKey("keyword", "key2")));
        assertEquals(Action.INCLUDE, map.get(new FilterKey("keyword", "key3")));
    }

    @After
    public void tearDown() throws IOException {
        TestUtils.forceDelete(tempDir);
    }

}
