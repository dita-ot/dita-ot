/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2022 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.writer.include;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.dita.dost.util.Constants.*;
import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import org.dita.dost.TestUtils;
import org.dita.dost.exception.UncheckedDITAOTException;
import org.dita.dost.store.StreamStore;
import org.dita.dost.util.Configuration;
import org.dita.dost.util.Job;
import org.dita.dost.util.XMLUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class IncludeTextTest {

  private static final String DATA;

  static {
    final byte[] buf = new byte[256];
    for (int i = 0; i < 256; i++) {
      buf[i] = (byte) i;
    }
    DATA = new String(buf, ISO_8859_1);
  }

  private File tempDir;
  private Job job;

  @Before
  public void setUp() throws Exception {
    tempDir = TestUtils.createTempDir(IncludeResolverTest.class);
    job = new Job(tempDir, new StreamStore(tempDir, new XMLUtils()));
    job.add(
      new Job.FileInfo.Builder()
        .uri(URI.create("include.txt"))
        .src(tempDir.toURI().resolve("include.txt"))
        .format(PR_D_CODEREF.localName)
        .build()
    );
  }

  @After
  public void cleanUp() throws IOException {
    TestUtils.forceDelete(tempDir);
  }

  @Test
  public void include_validEncoding() throws IOException {
    final CharacterBufferContentHandler characterBuffer = new CharacterBufferContentHandler();
    final IncludeText includeText = new IncludeText(
      job,
      tempDir.toURI().resolve("topic.dita"),
      characterBuffer,
      new TestUtils.TestLogger(),
      Configuration.Mode.STRICT
    );
    createIncludeFile(UTF_8);

    includeText.include(createAttributes(UTF_8));

    // AllRange will normalize line feeds to '\n', so we cannot compare String directly
    try (
      BufferedReader act = new BufferedReader(new StringReader(characterBuffer.characters.toString()));
      BufferedReader exp = new BufferedReader(new StringReader(DATA))
    ) {
      String line;
      while ((line = exp.readLine()) != null) {
        assertEquals(line, act.readLine());
      }
    }
  }

  @Test(expected = UncheckedDITAOTException.class)
  public void include_invalidEncoding() throws IOException {
    final IncludeText includeText = new IncludeText(
      job,
      tempDir.toURI().resolve("topic.dita"),
      new DefaultHandler(),
      new TestUtils.TestLogger(),
      Configuration.Mode.STRICT
    );
    createIncludeFile(ISO_8859_1);

    includeText.include(createAttributes(UTF_8));
  }

  private Attributes createAttributes(Charset charset) {
    return new XMLUtils.AttributesBuilder()
      .add(ATTRIBUTE_NAME_HREF, "include.txt")
      .add(ATTRIBUTE_NAME_ENCODING, charset.toString())
      .build();
  }

  private Path createIncludeFile(Charset charset) throws IOException {
    return Files.write(new File(tempDir, "include.txt").toPath(), DATA.getBytes(charset));
  }

  private static class CharacterBufferContentHandler extends DefaultHandler {

    StringBuilder characters = new StringBuilder();

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
      characters.append(ch, start, length);
    }
  }
}
