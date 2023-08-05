/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.writer;

import static org.dita.dost.TestUtils.assertXMLEqual;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import org.dita.dost.TestUtils;
import org.dita.dost.reader.KeyrefReader;
import org.dita.dost.store.StreamStore;
import org.dita.dost.util.Job;
import org.dita.dost.util.KeyScope;
import org.dita.dost.util.XMLUtils;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.xml.sax.InputSource;

public class KeyrefPaserTest {

  private static final File resourceDir = TestUtils.getResourceDir(KeyrefPaserTest.class);
  private static final File srcDir = new File(resourceDir, "src");
  private static final File expDir = new File(resourceDir, "exp");

  private final XMLUtils xmlUtils = new XMLUtils();

  @TempDir
  private File tempDir;

  @ParameterizedTest
  @MethodSource("testWriteArguments")
  public void testWrite(Path file, Path map) throws Exception {
    TestUtils.copy(srcDir, tempDir);

    final KeyScope keyDefinition = readKeyMap(map);
    final KeyrefPaser parser = new KeyrefPaser();
    parser.setLogger(new TestUtils.TestLogger());
    parser.setJob(new Job(tempDir, new StreamStore(tempDir, new XMLUtils())));
    parser.setKeyDefinition(keyDefinition);
    parser.setCurrentFile(Paths.get(tempDir.getAbsolutePath(), file.toString()).toUri());
    parser.write(Paths.get(tempDir.getAbsolutePath(), file.toString()).toFile());

    assertXMLEqual(
      new InputSource(Paths.get(expDir.getAbsolutePath(), file.toString()).toUri().toString()),
      new InputSource(Paths.get(tempDir.getAbsolutePath(), file.toString()).toUri().toString())
    );
  }

  private static Stream<Arguments> testWriteArguments() {
    return Stream.of(
      Arguments.of(Paths.get("a.xml"), Paths.get("keys.ditamap")),
      Arguments.of(Paths.get("subdir", "subdirtopic.xml"), Paths.get("keys.ditamap")),
      Arguments.of(Paths.get("id.xml"), Paths.get("keys.ditamap")),
      Arguments.of(Paths.get("fallback.xml"), Paths.get("keys.ditamap")),
      Arguments.of(Paths.get("b.ditamap"), Paths.get("keys.ditamap")),
      Arguments.of(Paths.get("d.ditamap"), Paths.get("keys.ditamap")),
      Arguments.of(Paths.get("copy-to-keys.ditamap"), Paths.get("keys.ditamap")),
      Arguments.of(Paths.get("subdir", "c.ditamap"), Paths.get("subdir", "c.ditamap"))
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
}
