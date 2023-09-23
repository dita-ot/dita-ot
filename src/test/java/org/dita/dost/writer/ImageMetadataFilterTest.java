/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.writer;

import static org.apache.commons.io.FileUtils.*;
import static org.dita.dost.TestUtils.assertXMLEqual;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.dita.dost.TestUtils;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.store.StreamStore;
import org.dita.dost.util.Job;
import org.dita.dost.util.XMLUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ImageMetadataFilterTest {

  private static final File resourceDir = TestUtils.getResourceDir(ImageMetadataFilterTest.class);
  private static final File srcDir = new File(resourceDir, "src");
  private static final File expDir = new File(resourceDir, "exp");
  private static File tempDir;

  @BeforeAll
  public static void setup() throws IOException {
    tempDir = TestUtils.createTempDir(ImageMetadataFilterTest.class);
  }

  @Test
  public void testWrite() throws SAXException, IOException {
    final File f = new File(tempDir, "test.dita");
    copyFile(new File(srcDir, "test.dita"), f);

    final Job job = new Job(tempDir, new StreamStore(tempDir, new XMLUtils()));
    job.setProperty("uplevels", "");
    final Map<URI, Attributes> cache = new HashMap<>();
    final ImageMetadataFilter filter = new ImageMetadataFilter(srcDir, job, cache);
    filter.setLogger(new TestUtils.TestLogger());
    filter.setJob(job);
    filter.write(f.getAbsoluteFile());

    assertXMLEqual(
      new InputSource(new File(expDir, "test.dita").toURI().toString()),
      new InputSource(f.toURI().toString())
    );
    assertEquals(
      Arrays
        .asList("img.png", "img.gif", "img.jpg", "img.xxx")
        .stream()
        .map(img -> new File(srcDir, img).toURI())
        .collect(Collectors.toSet()),
      cache.keySet()
    );
  }

  @Test
  public void testUplevelsWrite() throws SAXException, IOException {
    final File f = new File(tempDir, "sub" + File.separator + "test.dita");
    f.getParentFile().mkdirs();
    copyFile(new File(srcDir, "test.dita"), f);

    final Job job = new Job(tempDir, new StreamStore(tempDir, new XMLUtils()));
    job.setProperty("uplevels", ".." + File.separator);
    final Map<URI, Attributes> cache = new HashMap<>();
    final ImageMetadataFilter filter = new ImageMetadataFilter(srcDir, job, cache);
    filter.setLogger(new TestUtils.TestLogger());
    filter.setJob(job);
    filter.write(f.getAbsoluteFile());

    assertXMLEqual(
      new InputSource(new File(expDir, "test.dita").toURI().toString()),
      new InputSource(f.toURI().toString())
    );
    assertEquals(
      Arrays
        .asList("img.png", "img.gif", "img.jpg", "img.xxx")
        .stream()
        .map(img -> new File(srcDir, img).toURI())
        .collect(Collectors.toSet()),
      cache.keySet()
    );
  }

  @AfterAll
  public static void teardown() throws IOException {
    TestUtils.forceDelete(tempDir);
  }
}
