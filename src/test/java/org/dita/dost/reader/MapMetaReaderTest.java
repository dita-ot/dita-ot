/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.reader;

import static org.dita.dost.TestUtils.assertXMLEqual;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.parsers.DocumentBuilder;
import org.dita.dost.TestUtils;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.store.StreamStore;
import org.dita.dost.util.CatalogUtils;
import org.dita.dost.util.Job;
import org.dita.dost.util.XMLUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class MapMetaReaderTest {

  private static final File resourceDir = TestUtils.getResourceDir(MapMetaReaderTest.class);
  private static final File srcDir = new File(resourceDir, "src");
  private static final File expDir = new File(resourceDir, "exp");

  private File tempDir;
  private MapMetaReader reader;
  private DocumentBuilder db;

  @BeforeEach
  public void setUp() throws Exception {
    CatalogUtils.setDitaDir(new File("src" + File.separator + "main").getAbsoluteFile());
    tempDir = TestUtils.createTempDir(MapMetaReaderTest.class);

    reader = new MapMetaReader();
    reader.setLogger(new TestUtils.TestLogger());
    reader.setJob(new Job(tempDir, new StreamStore(tempDir, new XMLUtils())));

    db = XMLUtils.getDocumentBuilder();
    db.setEntityResolver(CatalogUtils.getCatalogResolver());
  }

  @ParameterizedTest
  @ValueSource(strings = { "base", "locktitle" })
  public void testRead(String dir) throws DITAOTException, IOException, SAXException {
    Files
      .list(srcDir.toPath().resolve(dir))
      .forEach(f -> {
        try {
          TestUtils.normalize(f.toFile(), new File(tempDir, f.toFile().getName()));
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      });

    final Path mapFile = tempDir.toPath().resolve("test.ditamap");
    reader.read(mapFile.toAbsolutePath().toFile());

    final Document exp = db.parse(expDir.toPath().resolve(dir).resolve("test.ditamap").toFile());
    final Document act = db.parse(tempDir.toPath().resolve("test.ditamap").toFile());
    assertXMLEqual(exp, act);
  }

  @AfterEach
  public void tearDown() throws IOException {
    TestUtils.forceDelete(tempDir);
  }
}
