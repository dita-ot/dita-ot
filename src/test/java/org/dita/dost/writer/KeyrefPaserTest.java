/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.writer;

import static org.dita.dost.TestUtils.assertXMLEqual;
import static org.dita.dost.TestUtils.createTempDir;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import org.dita.dost.TestUtils;
import org.dita.dost.reader.KeyrefReader;
import org.dita.dost.store.StreamStore;
import org.dita.dost.util.Job;
import org.dita.dost.util.KeyScope;
import org.dita.dost.util.XMLUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xml.sax.InputSource;

public class KeyrefPaserTest {

  private static final File resourceDir = TestUtils.getResourceDir(KeyrefPaserTest.class);
  private static final File srcDir = new File(resourceDir, "src");
  private static final File expDir = new File(resourceDir, "exp");

  private final XMLUtils xmlUtils = new XMLUtils();
  private KeyScope keyDefinition;
  private File tempDir;

  @BeforeEach
  public void setUp() throws Exception {
    tempDir = createTempDir(KeyrefPaserTest.class);
    TestUtils.copy(srcDir, tempDir);

    keyDefinition = readKeyMap(Paths.get("keys.ditamap"));
  }

  @AfterEach
  public void tearDown() throws IOException {
    TestUtils.forceDelete(tempDir);
  }

  @Test
  public void testTopicWrite() throws Exception {
    final KeyrefPaser parser = new KeyrefPaser();
    parser.setLogger(new TestUtils.TestLogger());
    parser.setJob(new Job(tempDir, new StreamStore(tempDir, new XMLUtils())));
    parser.setKeyDefinition(keyDefinition);
    parser.setCurrentFile(new File(tempDir, "a.xml").toURI());
    parser.write(new File(tempDir, "a.xml"));

    assertXMLEqual(
      new InputSource(new File(expDir, "a.xml").toURI().toString()),
      new InputSource(new File(tempDir, "a.xml").toURI().toString())
    );
  }

  @Test
  public void testTopicWriteSubdir() throws Exception {
    final KeyrefPaser parser = new KeyrefPaser();
    parser.setLogger(new TestUtils.TestLogger());
    parser.setJob(new Job(tempDir, new StreamStore(tempDir, new XMLUtils())));
    parser.setKeyDefinition(keyDefinition);
    final String tempSubDir = tempDir + File.separator + "subdir";
    parser.setCurrentFile(new File(tempSubDir, "subdirtopic.xml").toURI());
    parser.write(new File(tempSubDir, "subdirtopic.xml"));

    assertXMLEqual(
      new InputSource(new File(expDir + File.separator + "subdir", "subdirtopic.xml").toURI().toString()),
      new InputSource(new File(tempSubDir, "subdirtopic.xml").toURI().toString())
    );
  }

  @Test
  public void testFragment() throws Exception {
    final KeyrefPaser parser = new KeyrefPaser();
    parser.setLogger(new TestUtils.TestLogger());
    parser.setJob(new Job(tempDir, new StreamStore(tempDir, new XMLUtils())));
    parser.setKeyDefinition(keyDefinition);
    parser.setCurrentFile(new File(tempDir, "id.xml").toURI());
    parser.write(new File(tempDir, "id.xml"));

    assertXMLEqual(
      new InputSource(new File(expDir, "id.xml").toURI().toString()),
      new InputSource(new File(tempDir, "id.xml").toURI().toString())
    );
  }

  @Test
  public void testFallback() throws Exception {
    final KeyrefPaser parser = new KeyrefPaser();
    parser.setLogger(new TestUtils.TestLogger());
    parser.setJob(new Job(tempDir, new StreamStore(tempDir, new XMLUtils())));
    parser.setKeyDefinition(keyDefinition);
    parser.setCurrentFile(new File(tempDir, "fallback.xml").toURI());
    parser.write(new File(tempDir, "fallback.xml"));

    assertXMLEqual(
      new InputSource(new File(expDir, "fallback.xml").toURI().toString()),
      new InputSource(new File(tempDir, "fallback.xml").toURI().toString())
    );
  }

  @Test
  public void testMapWrite() throws Exception {
    final KeyrefPaser parser = new KeyrefPaser();
    parser.setLogger(new TestUtils.TestLogger());
    parser.setJob(new Job(tempDir, new StreamStore(tempDir, new XMLUtils())));
    parser.setKeyDefinition(keyDefinition);
    parser.setCurrentFile(new File(tempDir, "b.ditamap").toURI());
    parser.write(new File(tempDir, "b.ditamap"));

    assertXMLEqual(
      new InputSource(new File(expDir, "b.ditamap").toURI().toString()),
      new InputSource(new File(tempDir, "b.ditamap").toURI().toString())
    );
  }

  @Test
  public void testUpLevelMapWrite() throws Exception {
    final KeyrefPaser parser = new KeyrefPaser();
    parser.setLogger(new TestUtils.TestLogger());
    parser.setJob(new Job(tempDir, new StreamStore(tempDir, new XMLUtils())));
    parser.setKeyDefinition(readKeyMap(Paths.get("subdir", "c.ditamap")));
    parser.setCurrentFile(new File(tempDir, "subdir" + File.separator + "c.ditamap").toURI());
    parser.write(new File(tempDir, "subdir" + File.separator + "c.ditamap"));

    assertXMLEqual(
      new InputSource(new File(expDir, "subdir" + File.separator + "c.ditamap").toURI().toString()),
      new InputSource(new File(tempDir, "subdir" + File.separator + "c.ditamap").toURI().toString())
    );
  }

  @Test
  public void testMapWithKeyScopes() throws Exception {
    final KeyrefPaser parser = new KeyrefPaser();
    parser.setLogger(new TestUtils.TestLogger());
    parser.setJob(new Job(tempDir, new StreamStore(tempDir, new XMLUtils())));
    parser.setKeyDefinition(keyDefinition);
    parser.setCurrentFile(new File(tempDir, "d.ditamap").toURI());
    parser.write(new File(tempDir, "d.ditamap"));

    assertXMLEqual(
      new InputSource(new File(expDir, "d.ditamap").toURI().toString()),
      new InputSource(new File(tempDir, "d.ditamap").toURI().toString())
    );
  }

  private KeyScope readKeyMap(final Path map) {
    KeyrefReader reader = new KeyrefReader();
    final URI keyMapFile = tempDir.toPath().resolve(map).toUri();
    final XdmNode document = parse(keyMapFile);

    reader.read(keyMapFile, document);

    return reader.getKeyDefinition();
  }

  private XdmNode parse(final URI in) {
    try {
      final StreamSource source = new StreamSource(in.toString());
      source.setSystemId(in.toString());
      return xmlUtils.getProcessor().newDocumentBuilder().build(source);
    } catch (SaxonApiException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void testTopicWriteCopyTo() throws Exception {
    final KeyrefPaser parser = new KeyrefPaser();
    parser.setLogger(new TestUtils.TestLogger());
    parser.setJob(new Job(tempDir, new StreamStore(tempDir, new XMLUtils())));
    KeyScope kd = readKeyMap(Paths.get("copy-to-keys.ditamap"));
    parser.setKeyDefinition(kd);
    parser.setCurrentFile(new File(tempDir, "copy-to-keys.dita").toURI());
    parser.write(new File(tempDir, "copy-to-keys.dita"));

    assertXMLEqual(
      new InputSource(new File(expDir, "copy-to-keys.dita").toURI().toString()),
      new InputSource(new File(tempDir, "copy-to-keys.dita").toURI().toString())
    );
  }
}
