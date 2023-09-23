/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2016 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.module;

import static org.dita.dost.TestUtils.assertXMLEqual;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.dita.dost.TestUtils;
import org.dita.dost.TestUtils.CachingLogger;
import org.dita.dost.TestUtils.CachingLogger.Message;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.store.CacheStore;
import org.dita.dost.store.Store;
import org.dita.dost.store.StreamStore;
import org.dita.dost.util.CatalogUtils;
import org.dita.dost.util.Configuration.Mode;
import org.dita.dost.util.Job;
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.util.XMLUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.io.TempDir;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public abstract class AbstractModuleTest {

  File resourceDir = TestUtils.getResourceDir(getClass());
  File expBaseDir = new File(resourceDir, "exp");

  @TempDir
  protected File tempBaseDir;

  private final DocumentBuilder builder;
  protected String testCase;
  protected Map<String, String> params = Collections.emptyMap();
  protected boolean parallel;

  @TempDir
  protected File tempDir;

  protected XMLUtils xmlUtils;
  protected Job job;
  protected AbstractPipelineModule chunkModule;
  protected CachingLogger logger;
  protected Mode mode = Mode.STRICT;

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
      case Node.ELEMENT_NODE -> {
        for (final Node n : getChildren(node)) {
          normalizeSpace(n);
        }
      }
      case Node.TEXT_NODE -> {
        final String v = node.getNodeValue().replaceAll("\\s+", " ").trim();
        if (v.isEmpty()) {
          node.getParentNode().removeChild(node);
        } else {
          node.setNodeValue(v);
        }
      }
    }
  }

  List<Node> getChildren(final Node node) {
    final List<Node> res = new ArrayList<>();
    final NodeList ns = node.getChildNodes();
    for (int i = 0; i < ns.getLength(); i++) {
      res.add(ns.item(i));
    }
    return res;
  }

  @BeforeEach
  public void setUp() throws Exception {
    CatalogUtils.setDitaDir(Paths.get("src", "main").toFile());
    final File srcDir = new File(resourceDir, "src");
    TestUtils.copy(srcDir, tempBaseDir);
    chunkModule = getModule();
    xmlUtils = new XMLUtils();
    chunkModule.setXmlUtils(xmlUtils);
  }

  //  @AfterEach
  //  public void tearDown() throws Exception {
  ////    TestUtils.forceDelete(tempBaseDir);
  //  }

  //  public void serialFile() {
  //    test();
  //  }
  //
  //  public void parallelFile() {
  //    chunkModule.setParallel(true);
  //    test();
  //  }
  //
  //  public void serialMemory() throws IOException {
  //    job = new Job(tempDir, new CacheStore(tempDir, xmlUtils));
  //    chunkModule.setJob(job);
  //    test();
  //  }
  //
  //  public void parallelMemory() throws IOException {
  //    job = new Job(tempDir, new CacheStore(tempDir, xmlUtils));
  //    chunkModule.setJob(job);
  //    chunkModule.setParallel(true);
  //    test();
  //  }

  protected void initStore(Store cache) {
    if (cache instanceof CacheStore) {
      final File srcDir = new File(resourceDir, "src" + File.separator + testCase);
      try {
        Files
          .walk(srcDir.toPath())
          .filter(Files::isRegularFile)
          .forEach(src -> {
            final URI dst = tempDir.toPath().resolve(srcDir.toPath().relativize(src)).toUri();
            try (OutputStream out = cache.getOutputStream(dst)) {
              Files.copy(src, out);
            } catch (IOException e) {
              throw new UncheckedIOException(e);
            }
          });
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }
  }

  public void test() {
    if (job == null) {
      final Store store = new StreamStore(tempDir, xmlUtils);
      try {
        job = new Job(tempDir, store);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    chunkModule.setJob(job);
    logger = new CachingLogger(mode.equals(Mode.STRICT));
    chunkModule.setLogger(logger);
    final File expDir = new File(expBaseDir, testCase);
    try {
      final AbstractPipelineInput input = getAbstractPipelineInput();
      params.forEach(input::setAttribute);
      chunkModule.execute(input);

      compare(tempDir, expDir, job.getStore());

      logger
        .getMessages()
        .stream()
        .filter(m -> m.level == Message.Level.ERROR)
        .forEach(m -> System.err.println(m.level + ": " + m.message));
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected abstract AbstractPipelineInput getAbstractPipelineInput();

  protected abstract AbstractPipelineModule getModule();

  private static final Set<String> IGNORE = ImmutableSet.of(".job.xml", ".DS_Store");

  private void compare(File actDir, File expDir, Store store) throws SAXException, IOException {
    final Set<String> names = new HashSet<>();
    final String[] actList = actDir.list();
    if (actList != null) {
      Stream.of(actList).filter(f -> store.exists(new File(f).toURI())).forEach(names::add);
    }
    final String[] expList = expDir.list();
    if (expList != null) {
      names.addAll(Arrays.asList(expList));
    }
    names.removeAll(IGNORE);

    for (final String name : names) {
      final File act = new File(actDir, name);
      final File exp = new File(expDir, name);
      if (exp.isDirectory() || act.isDirectory()) {
        compare(act, new File(expDir, name), store);
      } else {
        final Document expDoc = getDocument(exp);
        final Document actDoc = store.getDocument(act.toURI());
        //                assertXMLEqual("Comparing " + exp + " to " + act + ":",
        //                        expDoc, actDoc);
        try {
          assertXMLEqual(expDoc, actDoc);
        } catch (AssertionError e) {
          System.out.println(exp);
          Files.copy(act.toPath(), System.out);
          throw e;
        }
      }
    }
    if (new File(expDir, ".job.xml").exists()) {
      final Job expJob = new Job(expDir, new StreamStore(expDir, xmlUtils));
      //            final Job actJob = new Job(actDir, new StreamStore(actDir, xmlUtils));
      final Collection<FileInfo> expFileInfo = new HashSet<>(expJob.getFileInfo());
      final Collection<FileInfo> actFileInfo = new HashSet<>(job.getFileInfo());
      try {
        assertEquals(expFileInfo, actFileInfo);
      } catch (Throwable e) {
        System.out.println(Files.exists(new File(actDir, ".job.xml").toPath()));
        Files.copy(new File(actDir, ".job.xml").toPath(), System.out);
        throw e;
      }
    }
  }
}
