/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2010 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.module;

import org.dita.dost.TestUtils;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.pipeline.AbstractFacade;
import org.dita.dost.pipeline.PipelineFacade;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.util.Job;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;
import java.net.URI;
import java.util.*;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.Job.FileInfo;
import static org.dita.dost.util.Job.Generate.NOT_GENERATEOUTTER;
import static org.junit.Assert.assertEquals;

public class TestGenMapAndTopicListModule {

    private static final File resourceDir = TestUtils.getResourceDir(TestGenMapAndTopicListModule.class);
    private static final File srcDir = new File(resourceDir, "src");
    private static final File expDir = new File(resourceDir, "exp");
    
    private static File tempDir;
    private static File tempDirParallel;
    private static File tempDirAbove;

    @BeforeClass
    public static void setUp() throws IOException, DITAOTException {
        tempDir = TestUtils.createTempDir(TestGenMapAndTopicListModule.class);
        
        tempDirParallel = new File(tempDir, "parallel");
        tempDirParallel.mkdirs();
        final File inputDirParallel = new File("maps");
        final File inputMapParallel = new File(inputDirParallel, "root-map-01.ditamap");
        final File outDirParallel = new File(tempDirParallel, "out");
        generate(inputDirParallel, inputMapParallel, outDirParallel, tempDirParallel);
        
        tempDirAbove = new File(tempDir, "above");
        tempDirAbove.mkdirs();
        final File inputDirAbove = new File(".");
        final File inputMapAbove = new File(inputDirAbove, "root-map-02.ditamap");
        final File outDirAbove = new File(tempDirAbove, "out");
        generate(inputDirAbove, inputMapAbove, outDirAbove, tempDirAbove);
    }

    private static void generate(final File inputDir, final File inputMap, final File outDir, final File tempDir) throws DITAOTException, IOException {
        final PipelineHashIO pipelineInput = new PipelineHashIO();
        pipelineInput.setAttribute(ANT_INVOKER_PARAM_INPUTMAP, inputMap.getPath());
        pipelineInput.setAttribute(ANT_INVOKER_PARAM_BASEDIR, srcDir.getAbsolutePath());
        pipelineInput.setAttribute(ANT_INVOKER_EXT_PARAM_DITADIR, inputDir.getPath());
        pipelineInput.setAttribute(ANT_INVOKER_EXT_PARAM_OUTPUTDIR, outDir.getPath());
        pipelineInput.setAttribute(ANT_INVOKER_PARAM_TEMPDIR, tempDir.getPath());
        pipelineInput.setAttribute(ANT_INVOKER_EXT_PARAM_DITADIR, new File("src" + File.separator + "main").getAbsolutePath());
        pipelineInput.setAttribute(ANT_INVOKER_EXT_PARAM_INDEXTYPE, "xhtml");
        pipelineInput.setAttribute(ANT_INVOKER_EXT_PARAM_ENCODING, "en-US");
        pipelineInput.setAttribute(ANT_INVOKER_EXT_PARAM_TARGETEXT, ".html");
        pipelineInput.setAttribute(ANT_INVOKER_EXT_PARAM_VALIDATE, Boolean.FALSE.toString());
        pipelineInput.setAttribute(ANT_INVOKER_EXT_PARAM_GENERATECOPYOUTTER, Integer.toString(NOT_GENERATEOUTTER.type));
        pipelineInput.setAttribute(ANT_INVOKER_EXT_PARAM_OUTTERCONTROL, "warn");
        pipelineInput.setAttribute(ANT_INVOKER_EXT_PARAM_ONLYTOPICINMAP, Boolean.FALSE.toString());
        //pipelineInput.setAttribute("ditalist", new File(tempDir, FILE_NAME_DITA_LIST).getPath());
        pipelineInput.setAttribute(ANT_INVOKER_PARAM_MAPLINKS, new File(tempDir, "maplinks.unordered").getPath());
        pipelineInput.setAttribute(ANT_INVOKER_EXT_PARAN_SETSYSTEMID, "no");

        final AbstractFacade facade = new PipelineFacade();
        facade.setLogger(new TestUtils.TestLogger());
        facade.setJob(new Job(tempDir));
        facade.execute("GenMapAndTopicList", pipelineInput);
    }
        
    @Test
    public void testFileContentParallel() throws Exception{
        final File e = new File(expDir, "parallel");
        
        assertEquals(new HashSet<String>(Arrays.asList(
                    "topics/target-topic-c.xml",
                    "topics/target-topic a.xml",
                    "topics/xreffin-topic-1.xml")),
                readLines(new File(e, "canditopics.list")));
        assertEquals(Collections.emptySet(), 
                readLines(new File(e, "coderef.list")));
        assertEquals(Collections.emptySet(),
                readLines(new File(e, "conref.list")));
        assertEquals(Collections.emptySet(),
                readLines(new File(e, "conrefpush.list")));
        assertEquals(Collections.emptySet(),
                readLines(new File(e, "conreftargets.list")));
        assertEquals(Collections.emptySet(),
                readLines(new File(e, "copytosource.list")));
        assertEquals(Collections.emptySet(),
                readLines(new File(e, "copytotarget2sourcemap.list")));
        assertEquals(Collections.emptySet(),
                readLines(new File(e, "flagimage.list")));
        assertEquals(new HashSet<String>(Arrays.asList(
                    "maps/root-map-01.ditamap")),
                readLines(new File(e, "fullditamap.list")));
        assertEquals(new HashSet<String>(Arrays.asList(
                    "topics/target-topic-c.xml",
                    "topics/target-topic a.xml",
                    "maps/root-map-01.ditamap",
                    "topics/xreffin-topic-1.xml")),
                readLines(new File(e, "fullditamapandtopic.list")));
        assertEquals(new HashSet<String>(Arrays.asList(
                    "topics/target-topic-c.xml",
                    "topics/target-topic a.xml",
                    "topics/xreffin-topic-1.xml")),
                readLines(new File(e, "fullditatopic.list")));
        assertEquals(new HashSet<String>(Arrays.asList(
                    "topics/xreffin-topic-1.xml")),
                readLines(new File(e, "hrefditatopic.list")));
        assertEquals(new HashSet<String>(Arrays.asList(
                    "topics/target-topic-c.xml",
                    "topics/target-topic a.xml",
                    "topics/xreffin-topic-1.xml")),
                readLines(new File(e, "hreftargets.list")));
        assertEquals(Collections.emptySet(),
                readLines(new File(e, "html.list")));
        assertEquals(Collections.emptySet(),
                readLines(new File(e, "image.list")));
        assertEquals(new HashSet<String>(Arrays.asList(
                    "topics/xreffin-topic-1.xml")),
                readLines(new File(e, "keyref.list")));
        assertEquals(new HashSet<String>(Arrays.asList(
                    "topics/target-topic-c.xml",
                    "topics/target-topic a.xml",
                    "topics/xreffin-topic-1.xml")),
                readLines(new File(e, "outditafiles.list")));
        assertEquals(Collections.emptySet(),
                readLines(new File(e, "relflagimage.list")));
        assertEquals(Collections.emptySet(),
                readLines(new File(e, "resourceonly.list")));
        assertEquals(Collections.emptySet(),
                readLines(new File(e, "skipchunk.list")));
        assertEquals(Collections.emptySet(),
                readLines(new File(e, "subjectscheme.list")));
        assertEquals(Collections.emptySet(),
                readLines(new File(e, "subtargets.list")));
        assertEquals(new HashSet<String>(Arrays.asList(
                    "maps/root-map-01.ditamap")),
                readLines(new File(e, "usr.input.file.list")));
        
        final Job job = new Job(tempDirParallel);
        assertEquals(".." + File.separator, job.getProperty("uplevels"));

        assertEquals(5, job.getFileInfo().size());
        assertPaths(job.getFileInfo(new URI("topics/xreffin-topic-1.xml")),
                srcDir.toURI().resolve("topics/xreffin-topic-1.xml"),
                new URI("topics/xreffin-topic-1.xml"));
        assertPaths(job.getFileInfo(new URI("topics/target-topic%20a.xml")),
                srcDir.toURI().resolve("topics/target-topic%20a.xml"),
                new URI("topics/target-topic%20a.xml"));
        assertPaths(job.getFileInfo(new URI("topics/target-topic-c.xml")),
                srcDir.toURI().resolve("topics/target-topic-c.xml"),
                new URI("topics/target-topic-c.xml"));
        assertPaths(job.getFileInfo(new URI("maps/root-map-01.ditamap")),
                srcDir.toURI().resolve("maps/root-map-01.ditamap"),
                new URI("maps/root-map-01.ditamap"));
        assertPaths(job.getFileInfo(new URI("topics/xreffin-topic-1-copy.xml")),
                null,
                new URI("topics/xreffin-topic-1-copy.xml"));
    }
    
    private void assertPaths(final FileInfo fi, final URI src, final URI path) {
        if (src != null) {
            assertEquals(fi.src, src);
        }
        assertEquals(fi.uri, path);
    }

    @Test
    public void testFileContentAbove() throws Exception{
        final File e = new File(expDir, "above");
                
        assertEquals(new HashSet<String>(Arrays.asList(
                    "topics/xreffin-topic-1.xml",
                    "topics/target-topic-c.xml",
                    "topics/target-topic a.xml")),
                readLines(new File(e, "canditopics.list")));
        assertEquals(Collections.emptySet(),
                readLines(new File(e, "coderef.list")));
        assertEquals(Collections.emptySet(),
                readLines(new File(e, "conref.list")));
        assertEquals(Collections.emptySet(),
                readLines(new File(e, "conrefpush.list")));
        assertEquals(Collections.emptySet(),
                readLines(new File(e, "conreftargets.list")));
        assertEquals(Collections.emptySet(),
                readLines(new File(e, "copytosource.list")));
        assertEquals(Collections.emptySet(),
                readLines(new File(e, "copytotarget2sourcemap.list")));
        assertEquals(Collections.emptySet(),
                readLines(new File(e, "flagimage.list")));
        assertEquals(new HashSet<String>(Arrays.asList(
                    "root-map-02.ditamap")),
                readLines(new File(e, "fullditamap.list")));
        assertEquals(new HashSet<String>(Arrays.asList(
                    "topics/xreffin-topic-1.xml",
                    "topics/target-topic-c.xml",
                    "topics/target-topic a.xml",
                    "root-map-02.ditamap")),
                readLines(new File(e, "fullditamapandtopic.list")));
        assertEquals(new HashSet<String>(Arrays.asList(
                    "topics/xreffin-topic-1.xml",
                    "topics/target-topic-c.xml",
                    "topics/target-topic a.xml")),
                readLines(new File(e, "fullditatopic.list")));
        assertEquals(new HashSet<String>(Arrays.asList(
                    "topics/xreffin-topic-1.xml")),
                readLines(new File(e, "hrefditatopic.list")));
        assertEquals(new HashSet<String>(Arrays.asList(
                    "topics/xreffin-topic-1.xml",
                    "topics/target-topic-c.xml",
                    "topics/target-topic a.xml")),
                readLines(new File(e, "hreftargets.list")));
        assertEquals(Collections.emptySet(),
                readLines(new File(e, "html.list")));
        assertEquals(Collections.emptySet(),
                readLines(new File(e, "image.list")));
        assertEquals(new HashSet<String>(Arrays.asList(
                    "topics/xreffin-topic-1.xml")),
                readLines(new File(e, "keyref.list")));
        assertEquals(Collections.emptySet(),
                readLines(new File(e, "outditafiles.list")));
        assertEquals(Collections.emptySet(),
                readLines(new File(e, "relflagimage.list")));
        assertEquals(Collections.emptySet(),
                readLines(new File(e, "resourceonly.list")));
        assertEquals(Collections.emptySet(),
                readLines(new File(e, "skipchunk.list")));
        assertEquals(Collections.emptySet(),
                readLines(new File(e, "subjectscheme.list")));
        assertEquals(Collections.emptySet(),
                readLines(new File(e, "subtargets.list")));
        assertEquals(new HashSet<String>(Arrays.asList(
                    "root-map-02.ditamap")),
                readLines(new File(e, "usr.input.file.list")));
                
        final Job job = new Job(tempDirAbove);
        assertEquals("", job.getProperty("uplevels"));

        assertEquals(5, job.getFileInfo().size());
        assertPaths(job.getFileInfo(new URI("topics/xreffin-topic-1.xml")),
                srcDir.toURI().resolve("topics/xreffin-topic-1.xml"),
                new URI("topics/xreffin-topic-1.xml"));
        assertPaths(job.getFileInfo(new URI("topics/target-topic%20a.xml")),
                srcDir.toURI().resolve("topics/target-topic%20a.xml"),
                new URI("topics/target-topic%20a.xml"));
        assertPaths(job.getFileInfo(new URI("topics/target-topic-c.xml")),
                srcDir.toURI().resolve("topics/target-topic-c.xml"),
                new URI("topics/target-topic-c.xml"));
        assertPaths(job.getFileInfo(new URI("root-map-02.ditamap")),
                srcDir.toURI().resolve("root-map-02.ditamap"),
                new URI("root-map-02.ditamap"));
        assertPaths(job.getFileInfo(new URI("topics/xreffin-topic-1-copy.xml")),
                null,
                new URI("topics/xreffin-topic-1-copy.xml"));
    }
        
    private Properties readProperties(final File f)
            throws IOException, FileNotFoundException {
        final Properties p = new Properties();
        InputStream in = null;
        try {
            in = new FileInputStream(f);
            p.load(in);
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return p;
    }
    
    private Set<String> readLines(final File f) throws IOException {
        final Set<String> lines = new HashSet<String>();
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(f));
            String line = null;
            while ((line = in.readLine()) != null) {
                lines.add(line);
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return lines;
    }

//    @Test
//    public void testUpdateUplevels() throws NoSuchMethodException, SecurityException, SAXException, ParserConfigurationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException {
//        final Method updateUplevels = GenMapAndTopicListModule.class.getDeclaredMethod("updateUplevels", File.class);
//        updateUplevels.setAccessible(true);
//        final Field uplevels = GenMapAndTopicListModule.class.getDeclaredField("uplevels");
//        uplevels.setAccessible(true);
//        {
//            final GenMapAndTopicListModule m = new GenMapAndTopicListModule();
//            updateUplevels.invoke(m, new File("foo" + File.separator + "bar" + File.separator + "foo"));
//            assertEquals(0, uplevels.getInt(m));
//        }
//        {
//            final GenMapAndTopicListModule m = new GenMapAndTopicListModule();
//            updateUplevels.invoke(m, new File(".." + File.separator + "foo" + File.separator + "bar"));
//            assertEquals(1, uplevels.getInt(m));
//        }
//        {
//            final GenMapAndTopicListModule m = new GenMapAndTopicListModule();
//            updateUplevels.invoke(m, new File(".." + File.separator + ".." + File.separator + "foo"));
//            assertEquals(2, uplevels.getInt(m));
//        }
//        {
//            final GenMapAndTopicListModule m = new GenMapAndTopicListModule();
//            updateUplevels.invoke(m, new File(".." + File.separator + "foo" + File.separator + ".." + File.separator + "bar"));
//            assertEquals(1, uplevels.getInt(m));
//        }
//    }
    
    @AfterClass
    public static void tearDown() throws IOException {
        TestUtils.forceDelete(tempDir);
    }

}
