/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2010 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.module;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.Job.FileInfo;
import static org.dita.dost.util.Job.Generate.NOT_GENERATEOUTTER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;
import org.dita.dost.TestUtils;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.store.StreamStore;
import org.dita.dost.util.Job;
import org.dita.dost.util.XMLUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TestGenMapAndTopicListModule {

  private static final File resourceDir = TestUtils.getResourceDir(TestGenMapAndTopicListModule.class);
  private static final File srcDir = new File(resourceDir, "src");
  private static final File expDir = new File(resourceDir, "exp");

  private File tempDir;

  @BeforeEach
  public void setUp() throws IOException {
    tempDir = TestUtils.createTempDir(TestGenMapAndTopicListModule.class);
  }

  private static Job generate(final File inputDir, final File inputMap, final File outDir, final File tempDir)
    throws DITAOTException, IOException {
    final PipelineHashIO pipelineInput = new PipelineHashIO();
    pipelineInput.setAttribute(ANT_INVOKER_PARAM_INPUTMAP, inputMap.getPath());
    pipelineInput.setAttribute(ANT_INVOKER_PARAM_BASEDIR, srcDir.getAbsolutePath());
    pipelineInput.setAttribute(ANT_INVOKER_EXT_PARAM_DITADIR, inputDir.getPath());
    pipelineInput.setAttribute(ANT_INVOKER_EXT_PARAM_OUTPUTDIR, outDir.getPath());
    pipelineInput.setAttribute(ANT_INVOKER_PARAM_TEMPDIR, tempDir.getPath());
    pipelineInput.setAttribute(
      ANT_INVOKER_EXT_PARAM_DITADIR,
      new File("src" + File.separator + "main").getAbsolutePath()
    );
    pipelineInput.setAttribute(ANT_INVOKER_EXT_PARAM_INDEXTYPE, "xhtml");
    pipelineInput.setAttribute(ANT_INVOKER_EXT_PARAM_ENCODING, "en-US");
    pipelineInput.setAttribute(ANT_INVOKER_EXT_PARAM_TARGETEXT, ".html");
    pipelineInput.setAttribute(ANT_INVOKER_EXT_PARAM_VALIDATE, Boolean.FALSE.toString());
    pipelineInput.setAttribute(ANT_INVOKER_EXT_PARAM_GENERATECOPYOUTTER, Integer.toString(NOT_GENERATEOUTTER.type));
    pipelineInput.setAttribute(ANT_INVOKER_EXT_PARAM_OUTTERCONTROL, "warn");
    pipelineInput.setAttribute(ANT_INVOKER_EXT_PARAM_CRAWL, "topic");
    pipelineInput.setAttribute(ANT_INVOKER_EXT_PARAM_ONLYTOPICINMAP, Boolean.FALSE.toString());
    //pipelineInput.setAttribute("ditalist", new File(tempDir, FILE_NAME_DITA_LIST).getPath());
    pipelineInput.setAttribute(ANT_INVOKER_PARAM_MAPLINKS, new File(tempDir, "maplinks.unordered").getPath());
    pipelineInput.setAttribute(ANT_INVOKER_EXT_PARAN_SETSYSTEMID, "no");

    final GenMapAndTopicListModule module = new GenMapAndTopicListModule();
    module.setLogger(new TestUtils.TestLogger());
    final Job job = new Job(tempDir, new StreamStore(tempDir, new XMLUtils()));
    module.setJob(job);
    module.setXmlUtils(new XMLUtils());
    module.execute(pipelineInput);

    return job;
  }

  @Test
  public void testFileContentParallel() throws Exception {
    final File inputDirParallel = new File("maps");
    final File inputMapParallel = new File(inputDirParallel, "root-map-01.ditamap");
    final File outDirParallel = new File(tempDir, "out");
    generate(inputDirParallel, inputMapParallel, outDirParallel, tempDir);

    final File e = new File(expDir, "parallel");

    assertEquals(
      new HashSet<>(
        Arrays.asList("topics/target-topic-c.xml", "topics/target-topic a.xml", "topics/xreffin-topic-1.xml")
      ),
      readLines(new File(e, "canditopics.list"))
    );
    assertEquals(Collections.emptySet(), readLines(new File(e, "coderef.list")));
    assertEquals(Collections.emptySet(), readLines(new File(e, "conref.list")));
    assertEquals(Collections.emptySet(), readLines(new File(e, "conrefpush.list")));
    assertEquals(Collections.emptySet(), readLines(new File(e, "conreftargets.list")));
    assertEquals(Collections.emptySet(), readLines(new File(e, "copytosource.list")));
    assertEquals(Collections.emptySet(), readLines(new File(e, "copytotarget2sourcemap.list")));
    assertEquals(Collections.emptySet(), readLines(new File(e, "flagimage.list")));
    assertEquals(new HashSet<>(List.of("maps/root-map-01.ditamap")), readLines(new File(e, "fullditamap.list")));
    assertEquals(
      new HashSet<>(
        Arrays.asList(
          "topics/target-topic-c.xml",
          "topics/target-topic a.xml",
          "maps/root-map-01.ditamap",
          "topics/xreffin-topic-1.xml"
        )
      ),
      readLines(new File(e, "fullditamapandtopic.list"))
    );
    assertEquals(
      new HashSet<>(
        Arrays.asList("topics/target-topic-c.xml", "topics/target-topic a.xml", "topics/xreffin-topic-1.xml")
      ),
      readLines(new File(e, "fullditatopic.list"))
    );
    assertEquals(new HashSet<>(List.of("topics/xreffin-topic-1.xml")), readLines(new File(e, "hrefditatopic.list")));
    assertEquals(
      new HashSet<>(
        Arrays.asList("topics/target-topic-c.xml", "topics/target-topic a.xml", "topics/xreffin-topic-1.xml")
      ),
      readLines(new File(e, "hreftargets.list"))
    );
    assertEquals(Collections.emptySet(), readLines(new File(e, "html.list")));
    assertEquals(Collections.emptySet(), readLines(new File(e, "image.list")));
    assertEquals(new HashSet<>(List.of("topics/xreffin-topic-1.xml")), readLines(new File(e, "keyref.list")));
    assertEquals(
      new HashSet<>(
        Arrays.asList("topics/target-topic-c.xml", "topics/target-topic a.xml", "topics/xreffin-topic-1.xml")
      ),
      readLines(new File(e, "outditafiles.list"))
    );
    assertEquals(Collections.emptySet(), readLines(new File(e, "relflagimage.list")));
    assertEquals(Collections.emptySet(), readLines(new File(e, "resourceonly.list")));
    assertEquals(Collections.emptySet(), readLines(new File(e, "skipchunk.list")));
    assertEquals(Collections.emptySet(), readLines(new File(e, "subjectscheme.list")));
    assertEquals(Collections.emptySet(), readLines(new File(e, "subtargets.list")));
    assertEquals(new HashSet<>(List.of("maps/root-map-01.ditamap")), readLines(new File(e, "usr.input.file.list")));

    final Job job = new Job(tempDir, new StreamStore(tempDir, new XMLUtils()));
    assertEquals(".." + File.separator, job.getProperty("uplevels"));

    assertEquals(5, job.getFileInfo().size());
    assertPaths(
      job.getFileInfo(new URI("topics/xreffin-topic-1.xml")),
      srcDir.toURI().resolve("topics/xreffin-topic-1.xml"),
      new URI("topics/xreffin-topic-1.xml")
    );
    assertPaths(
      job.getFileInfo(new URI("topics/target-topic%20a.xml")),
      srcDir.toURI().resolve("topics/target-topic%20a.xml"),
      new URI("topics/target-topic%20a.xml")
    );
    assertPaths(
      job.getFileInfo(new URI("topics/target-topic-c.xml")),
      srcDir.toURI().resolve("topics/target-topic-c.xml"),
      new URI("topics/target-topic-c.xml")
    );
    assertPaths(
      job.getFileInfo(new URI("maps/root-map-01.ditamap")),
      srcDir.toURI().resolve("maps/root-map-01.ditamap"),
      new URI("maps/root-map-01.ditamap")
    );
    assertPaths(
      job.getFileInfo(new URI("topics/xreffin-topic-1-copy.xml")),
      null,
      new URI("topics/xreffin-topic-1-copy.xml")
    );
  }

  private void assertPaths(final FileInfo fi, final URI src, final URI path) {
    if (src != null) {
      assertEquals(fi.src, src);
    }
    assertEquals(fi.uri, path);
  }

  @Test
  public void testFileContentAbove() throws Exception {
    final File inputDirAbove = new File(".");
    final File inputMapAbove = new File(inputDirAbove, "root-map-02.ditamap");
    final File outDirAbove = new File(tempDir, "out");
    generate(inputDirAbove, inputMapAbove, outDirAbove, tempDir);

    final File e = new File(expDir, "above");

    assertEquals(
      new HashSet<>(
        Arrays.asList("topics/xreffin-topic-1.xml", "topics/target-topic-c.xml", "topics/target-topic a.xml")
      ),
      readLines(new File(e, "canditopics.list"))
    );
    assertEquals(Collections.emptySet(), readLines(new File(e, "coderef.list")));
    assertEquals(Collections.emptySet(), readLines(new File(e, "conref.list")));
    assertEquals(Collections.emptySet(), readLines(new File(e, "conrefpush.list")));
    assertEquals(Collections.emptySet(), readLines(new File(e, "conreftargets.list")));
    assertEquals(Collections.emptySet(), readLines(new File(e, "copytosource.list")));
    assertEquals(Collections.emptySet(), readLines(new File(e, "copytotarget2sourcemap.list")));
    assertEquals(Collections.emptySet(), readLines(new File(e, "flagimage.list")));
    assertEquals(new HashSet<>(List.of("root-map-02.ditamap")), readLines(new File(e, "fullditamap.list")));
    assertEquals(
      new HashSet<>(
        Arrays.asList(
          "topics/xreffin-topic-1.xml",
          "topics/target-topic-c.xml",
          "topics/target-topic a.xml",
          "root-map-02.ditamap"
        )
      ),
      readLines(new File(e, "fullditamapandtopic.list"))
    );
    assertEquals(
      new HashSet<>(
        Arrays.asList("topics/xreffin-topic-1.xml", "topics/target-topic-c.xml", "topics/target-topic a.xml")
      ),
      readLines(new File(e, "fullditatopic.list"))
    );
    assertEquals(new HashSet<>(List.of("topics/xreffin-topic-1.xml")), readLines(new File(e, "hrefditatopic.list")));
    assertEquals(
      new HashSet<>(
        Arrays.asList("topics/xreffin-topic-1.xml", "topics/target-topic-c.xml", "topics/target-topic a.xml")
      ),
      readLines(new File(e, "hreftargets.list"))
    );
    assertEquals(Collections.emptySet(), readLines(new File(e, "html.list")));
    assertEquals(Collections.emptySet(), readLines(new File(e, "image.list")));
    assertEquals(new HashSet<>(List.of("topics/xreffin-topic-1.xml")), readLines(new File(e, "keyref.list")));
    assertEquals(Collections.emptySet(), readLines(new File(e, "outditafiles.list")));
    assertEquals(Collections.emptySet(), readLines(new File(e, "relflagimage.list")));
    assertEquals(Collections.emptySet(), readLines(new File(e, "resourceonly.list")));
    assertEquals(Collections.emptySet(), readLines(new File(e, "skipchunk.list")));
    assertEquals(Collections.emptySet(), readLines(new File(e, "subjectscheme.list")));
    assertEquals(Collections.emptySet(), readLines(new File(e, "subtargets.list")));
    assertEquals(new HashSet<>(List.of("root-map-02.ditamap")), readLines(new File(e, "usr.input.file.list")));

    final Job job = new Job(tempDir, new StreamStore(tempDir, new XMLUtils()));
    assertEquals("", job.getProperty("uplevels"));

    assertEquals(5, job.getFileInfo().size());
    assertPaths(
      job.getFileInfo(new URI("topics/xreffin-topic-1.xml")),
      srcDir.toURI().resolve("topics/xreffin-topic-1.xml"),
      new URI("topics/xreffin-topic-1.xml")
    );
    assertPaths(
      job.getFileInfo(new URI("topics/target-topic%20a.xml")),
      srcDir.toURI().resolve("topics/target-topic%20a.xml"),
      new URI("topics/target-topic%20a.xml")
    );
    assertPaths(
      job.getFileInfo(new URI("topics/target-topic-c.xml")),
      srcDir.toURI().resolve("topics/target-topic-c.xml"),
      new URI("topics/target-topic-c.xml")
    );
    assertPaths(
      job.getFileInfo(new URI("root-map-02.ditamap")),
      srcDir.toURI().resolve("root-map-02.ditamap"),
      new URI("root-map-02.ditamap")
    );
    assertPaths(
      job.getFileInfo(new URI("topics/xreffin-topic-1-copy.xml")),
      null,
      new URI("topics/xreffin-topic-1-copy.xml")
    );
  }

  @Test
  public void testConref() throws Exception {
    final File inputDirParallel = new File("conref");
    final File inputMapParallel = new File(inputDirParallel, "main.ditamap");
    final File outDirParallel = new File(tempDir, "out");
    final Job job = generate(inputDirParallel, inputMapParallel, outDirParallel, tempDir);

    assertEquals(
      new HashSet<>(
        Arrays.asList(
          "link-from-normal-ALSORESOURCEONLY.dita",
          "conref-from-resource-only-ALSORESOURCEONLY.dita",
          "resourceonly.dita",
          "conref-from-normal.dita",
          "conref-from-resource-only.dita",
          "link-from-resource-only-ALSORESOURCEONLY.dita",
          "conref-from-normal-ALSORESOURCEONLY.dita"
        )
      ),
      job.getFileInfo().stream().filter(f -> f.isResourceOnly).map(fi -> fi.uri.toString()).collect(Collectors.toSet())
    );
    assertEquals(
      new HashSet<>(
        Arrays.asList("main.ditamap", "link-from-normal.dita", "link-from-resource-only.dita", "normal.dita")
      ),
      job.getFileInfo().stream().filter(f -> !f.isResourceOnly).map(fi -> fi.uri.toString()).collect(Collectors.toSet())
    );
  }

  @Test
  public void testConrefLink() throws Exception {
    final File inputDirParallel = new File("conref");
    final File inputMapParallel = new File(inputDirParallel, "link.ditamap");
    final File outDirParallel = new File(tempDir, "out");
    final Job job = generate(inputDirParallel, inputMapParallel, outDirParallel, tempDir);

    assertEquals(
      new HashSet<>(
        Arrays.asList(
          "conref-from-resource-only-ALSORESOURCEONLY.dita",
          "resourceonly.dita",
          "conref-from-normal.dita",
          "conref-from-resource-only.dita",
          "conref-from-normal-ALSORESOURCEONLY.dita"
        )
      ),
      job.getFileInfo().stream().filter(f -> f.isResourceOnly).map(fi -> fi.uri.toString()).collect(Collectors.toSet())
    );
    assertEquals(
      new HashSet<>(
        Arrays.asList(
          "link-from-normal-ALSORESOURCEONLY.dita",
          "link.ditamap",
          "link-from-normal.dita",
          "link-from-resource-only.dita",
          "link-from-resource-only-ALSORESOURCEONLY.dita",
          "normal.dita"
        )
      ),
      job.getFileInfo().stream().filter(f -> !f.isResourceOnly).map(fi -> fi.uri.toString()).collect(Collectors.toSet())
    );
  }

  @Test
  public void testImage() throws Exception {
    final File inputDir = new File("image");
    final File inputMap = new File(inputDir, "image.dita");
    final File outDir = new File(tempDir, "out");

    final Job job = generate(inputDir, inputMap, outDir, tempDir);

    assertNotNull(job.getFileInfo(URI.create("image.svg")));
    assertNotNull(job.getFileInfo(URI.create("image.svg?media=print")));
  }

  @Test
  public void testImageKeydef() throws Exception {
    final File inputDir = new File("image-keydef");
    final File inputMap = new File(inputDir, "svg.ditamap");
    final File outDir = new File(tempDir, "out");

    final Job job = generate(inputDir, inputMap, outDir, tempDir);
    FileInfo fileInfo = job.getFileInfo(new URI("figures/ISO_7010_W012.svg"));
    assertEquals("image", fileInfo.format);
  }

  private Set<String> readLines(final File f) throws IOException {
    final Set<String> lines = new HashSet<>();
    try (BufferedReader in = new BufferedReader(new FileReader(f))) {
      String line = null;
      while ((line = in.readLine()) != null) {
        lines.add(line);
      }
    }
    return lines;
  }

  @AfterEach
  public void tearDown() throws IOException {
    TestUtils.forceDelete(tempDir);
  }

  @Test
  public void testResourcesUplevels() throws Exception {
    final File inputDir = new File(".");
    final File inputMap = new File(inputDir, "image-keydef/svg.ditamap");
    final File outerMap = new File(srcDir, "root-map.ditamap");
    final File outDirAbove = new File(tempDir, "out");
    final PipelineHashIO pipelineInput = new PipelineHashIO();
    pipelineInput.setAttribute(ANT_INVOKER_PARAM_INPUTMAP, inputMap.getPath());
    pipelineInput.setAttribute(ANT_INVOKER_PARAM_BASEDIR, srcDir.getAbsolutePath());
    pipelineInput.setAttribute(ANT_INVOKER_EXT_PARAM_DITADIR, inputDir.getPath());
    pipelineInput.setAttribute(ANT_INVOKER_EXT_PARAM_OUTPUTDIR, outDirAbove.getPath());
    pipelineInput.setAttribute(ANT_INVOKER_PARAM_TEMPDIR, tempDir.getPath());
    pipelineInput.setAttribute(
      ANT_INVOKER_EXT_PARAM_DITADIR,
      new File("src" + File.separator + "main").getAbsolutePath()
    );
    pipelineInput.setAttribute(ANT_INVOKER_EXT_PARAM_GENERATECOPYOUTTER, Integer.toString(NOT_GENERATEOUTTER.type));
    pipelineInput.setAttribute(ANT_INVOKER_EXT_PARAM_OUTTERCONTROL, "warn");
    pipelineInput.setAttribute(ANT_INVOKER_PARAM_RESOURCES, outerMap.getCanonicalPath());

    final GenMapAndTopicListModule module = new GenMapAndTopicListModule();
    module.setLogger(new TestUtils.TestLogger());
    final Job job = new Job(tempDir, new StreamStore(tempDir, new XMLUtils()));
    module.setJob(job);
    module.setXmlUtils(new XMLUtils());
    module.execute(pipelineInput);
    assertEquals(srcDir.toURI().toString(), job.getInputDir().toString());
  }
}
