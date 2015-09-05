/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.module;

import static org.dita.dost.util.Job.Generate.NOT_GENERATEOUTTER;
import static org.dita.dost.util.Job.Generate.OLDSOLUTION;
import static org.junit.Assert.*;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.Job.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.dita.dost.TestUtils;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.pipeline.AbstractFacade;
import org.dita.dost.pipeline.PipelineFacade;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.util.Job;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class TestGenMapAndTopicListModule {

    private static final File resourceDir = TestUtils.getResourceDir(TestGenMapAndTopicListModule.class);
    private static final File srcDir = new File(resourceDir, "src");
    private static final File expDir = new File(resourceDir, "exp");
    
    private static File tempDir;
    private static File tempDirParallel;
    private static File tempDirAbove;
    private static File tempDirBelow;

    @BeforeClass
    public static void setUp() throws IOException, DITAOTException {
        tempDir = TestUtils.createTempDir(TestGenMapAndTopicListModule.class);
        
        tempDirParallel = new File(tempDir, "parallel");
        tempDirParallel.mkdirs();
        final File inputDirParallel = new File("maps");
        final File inputMapParallel = new File(inputDirParallel, "root-map-01.ditamap");
        final File outDirParallel = new File(tempDirParallel, "out");
        generate(inputDirParallel, inputMapParallel, outDirParallel, tempDirParallel, Integer.toString(NOT_GENERATEOUTTER.type));
        
        tempDirAbove = new File(tempDir, "above");
        tempDirAbove.mkdirs();
        final File inputDirAbove = new File(".");
        final File inputMapAbove = new File(inputDirAbove, "root-map-02.ditamap");
        final File outDirAbove = new File(tempDirAbove, "out");
        generate(inputDirAbove, inputMapAbove, outDirAbove, tempDirAbove, Integer.toString(NOT_GENERATEOUTTER.type));
    }

    private static void generate(final File inputDir, final File inputMap, final File outDir, final File tempDir, final String genCopy) throws DITAOTException, IOException {
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
        pipelineInput.setAttribute(ANT_INVOKER_EXT_PARAM_GENERATECOPYOUTTER, genCopy);
        pipelineInput.setAttribute(ANT_INVOKER_EXT_PARAM_OUTTERCONTROL, "warn");
        pipelineInput.setAttribute(ANT_INVOKER_EXT_PARAM_ONLYTOPICINMAP, Boolean.FALSE.toString());
        //pipelineInput.setAttribute("ditalist", new File(tempDir, FILE_NAME_DITA_LIST).getPath());
        pipelineInput.setAttribute(ANT_INVOKER_PARAM_MAPLINKS, new File(tempDir, "maplinks.unordered").getPath());
        pipelineInput.setAttribute(ANT_INVOKER_EXT_PARAN_SETSYSTEMID, "no");

        final AbstractFacade facade = new PipelineFacade();
        facade.setLogger(new TestUtils.TestLogger());
        facade.setJob(new Job(tempDir));
        facade.execute("GenMapAndTopicList", pipelineInput);
        if (genCopy.equals(Integer.toString(OLDSOLUTION.type))) {
            facade.execute("Coderef", null);
        }
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
    }

    @Test
    public void testFileContentBelow() throws Exception{
        try {
            tempDirBelow = new File(tempDir, "below");
            tempDirBelow.mkdirs();
            final File inputDirBelow = new File("below");
            final File inputMapBelow = new File(inputDirBelow, "maps/root-map-03.ditamap");
            final File outDirBelow = new File(tempDirBelow, "out");
            generate(inputDirBelow, inputMapBelow, outDirBelow, tempDirBelow, Integer.toString(OLDSOLUTION.type));
        } catch (Exception e) {
            // This method tests that coderef code is not parsed. Generation may have failed for a different reason.
            fail(e.getMessage());
        }

        // Parsing generated files
        final File jobXml = new File(tempDirBelow, ".job.xml");

        jobHelper(jobXml, new File(tempDirBelow, "canditopics.list"), "canditopicslist");
        assertEquals(new HashSet<String>(Arrays.asList(
                     "topics/target-topic-d.xml",
                     "topics/target-topic a.xml")),
                     readLines(new File(tempDirBelow, "canditopics.list")));

        jobHelper(jobXml, new File(tempDirBelow, "coderef.list"), "codereflist");
        assertEquals(new HashSet<String>(Arrays.asList(
                     "topics/target-topic-d.xml")),
                     readLines(new File(tempDirBelow, "coderef.list")));

        jobHelper(jobXml, new File(tempDirBelow, "conref.list"), "conreflist");
        assertEquals(Collections.emptySet(),
                     readLines(new File(tempDirBelow, "conref.list")));

        jobHelper(jobXml, new File(tempDirBelow, "conrefpush.list"), "conrefpushlist");
        assertEquals(Collections.emptySet(),
                     readLines(new File(tempDirBelow, "conrefpush.list")));

        jobHelper(jobXml, new File(tempDirBelow, "conreftargets.list"), "conreftargetslist");
        assertEquals(Collections.emptySet(),
                     readLines(new File(tempDirBelow, "conreftargets.list")));

        jobHelper(jobXml, new File(tempDirBelow, "copytosource.list"), "copytosourcelist");
        assertEquals(Collections.emptySet(),
                     readLines(new File(tempDirBelow, "copytosource.list")));

        jobHelper(jobXml, new File(tempDirBelow, "copytotarget2sourcemap.list"), "copytotarget2sourcemaplist");
        assertEquals(Collections.emptySet(),
                     readLines(new File(tempDirBelow, "copytotarget2sourcemap.list")));

        jobHelper(jobXml, new File(tempDirBelow, "flagimage.list"), "flagimagelist");
        assertEquals(Collections.emptySet(),
                     readLines(new File(tempDirBelow, "flagimage.list")));

        jobHelper(jobXml, new File(tempDirBelow, "fullditamap.list"), "fullditamaplist");
        assertEquals(new HashSet<String>(Arrays.asList(
                     "maps/root-map-03.ditamap")),
                     readLines(new File(tempDirBelow, "fullditamap.list")));

        jobHelper(jobXml, new File(tempDirBelow, "fullditamapandtopic.list"), "fullditamapandtopiclist");
        assertEquals(new HashSet<String>(Arrays.asList(
                     "topics/target-topic-d.xml",
                     "topics/target-topic a.xml",
                     "maps/root-map-03.ditamap")),
                     readLines(new File(tempDirBelow, "fullditamapandtopic.list")));

        jobHelper(jobXml, new File(tempDirBelow, "fullditatopic.list"), "fullditatopiclist");
        assertEquals(new HashSet<String>(Arrays.asList(
                     "topics/target-topic-d.xml",
                     "topics/target-topic a.xml")),
                     readLines(new File(tempDirBelow, "fullditatopic.list")));

        jobHelper(jobXml, new File(tempDirBelow, "hrefditatopic.list"), "hrefditatopiclist");
        assertEquals(new HashSet<String>(Arrays.asList(
                     "topics/target-topic-d.xml")),
                     readLines(new File(tempDirBelow, "hrefditatopic.list")));

        jobHelper(jobXml, new File(tempDirBelow, "hreftargets.list"), "hreftargetslist");
        assertEquals(new HashSet<String>(Arrays.asList(
                     "topics/target-topic-d.xml",
                     "topics/target-topic a.xml")),
                     readLines(new File(tempDirBelow, "hreftargets.list")));

        jobHelper(jobXml, new File(tempDirBelow, "html.list"), "htmllist");
        assertEquals(Collections.emptySet(),
                     readLines(new File(tempDirBelow, "html.list")));

        jobHelper(jobXml, new File(tempDirBelow, "image.list"), "imagelist");
        assertEquals(new HashSet<String>(Arrays.asList(
                     "images/carwash.jpg")),
                     readLines(new File(tempDirBelow, "image.list")));

        jobHelper(jobXml, new File(tempDirBelow, "keyref.list"), "keyreflist");
        assertEquals(Collections.emptySet(),
                     readLines(new File(tempDirBelow, "keyref.list")));

        jobHelper(jobXml, new File(tempDirBelow, "outditafiles.list"), "outditafileslist");
        assertEquals(Collections.emptySet(),
                     readLines(new File(tempDirBelow, "outditafiles.list")));

        jobHelper(jobXml, new File(tempDirBelow, "resourceonly.list"), "resourceonlylist");
        assertEquals(Collections.emptySet(),
                     readLines(new File(tempDirBelow, "resourceonly.list")));

        jobHelper(jobXml, new File(tempDirBelow, "subjectscheme.list"), "subjectschemelist");
        assertEquals(Collections.emptySet(),
                     readLines(new File(tempDirBelow, "subjectscheme.list")));

        jobHelper(jobXml, new File(tempDirBelow, "subtargets.list"), "subtargetslist");
        assertEquals(new HashSet<String>(Arrays.asList(
                     "aux/test.json")),
                     readLines(new File(tempDirBelow, "subtargets.list")));

        assertEquals(new HashSet<String>(Arrays.asList(
                     "maps/root-map-03.ditamap")),
                     readLines(new File(tempDirBelow, "usr.input.file.list")));
                
        final Job job = new Job(tempDirBelow);
        assertEquals(".." + File.separator, job.getProperty("uplevels"));
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

    private void jobHelper(File jobXml, File outFile, String property) throws Exception {
        File sourceDitaDir = TestUtils.getSourceDitaDir();
        StreamSource xs = new StreamSource(new FileInputStream(new File(sourceDitaDir, "xsl/job-helper.xsl")));
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer trans = tf.newTransformer(xs);
        trans.setParameter("property", property);
        StreamSource ss = new StreamSource(new FileInputStream(jobXml));
        StreamResult sr = new StreamResult(new FileOutputStream(outFile));
        trans.transform(ss, sr);
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
