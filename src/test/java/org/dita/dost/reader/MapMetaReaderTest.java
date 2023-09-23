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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.dita.dost.TestUtils;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.store.StreamStore;
import org.dita.dost.util.CatalogUtils;
import org.dita.dost.util.Job;
import org.dita.dost.util.XMLUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

public class MapMetaReaderTest {

  private static final File resourceDir = TestUtils.getResourceDir(MapMetaReaderTest.class);
  private static final File srcDir = new File(resourceDir, "src");
  private static final File expDir = new File(resourceDir, "exp");
  private static File tempDir;
  private static MapMetaReader reader;

  @BeforeAll
  public static void setUp() throws Exception {
    CatalogUtils.setDitaDir(new File("src" + File.separator + "main").getAbsoluteFile());
    tempDir = TestUtils.createTempDir(MapMetaReaderTest.class);
    for (final File f : srcDir.listFiles()) {
      TestUtils.normalize(f, new File(tempDir, f.getName()));
    }

    reader = new MapMetaReader();
    reader.setLogger(new TestUtils.TestLogger());
    reader.setJob(new Job(tempDir, new StreamStore(tempDir, new XMLUtils())));

    final File mapFile = new File(tempDir, "test.ditamap");
    reader.read(mapFile.getAbsoluteFile());
  }

  @Test
  public void testRead() throws SAXException, IOException, ParserConfigurationException {
    final DocumentBuilder db = XMLUtils.getDocumentBuilder();
    db.setEntityResolver(CatalogUtils.getCatalogResolver());

    assertXMLEqual(db.parse(new File(expDir, "test.ditamap")), db.parse(new File(tempDir, "test.ditamap")));
  }

  @AfterAll
  public static void tearDown() throws IOException {
    TestUtils.forceDelete(tempDir);
  }
}
