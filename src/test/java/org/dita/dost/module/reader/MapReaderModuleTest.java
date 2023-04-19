/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2017 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.module.reader;

import static org.dita.dost.util.Constants.*;
import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import org.dita.dost.TestUtils;
import org.dita.dost.reader.GenListModuleReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MapReaderModuleTest {

  private MapReaderModule reader;

  @BeforeEach
  public void setUp() {
    reader = new MapReaderModule();
    reader.setLogger(new TestUtils.TestLogger());
  }

  @Test
  public void categorizeReferenceFileTopic() throws Exception {
    reader.categorizeReferenceFile(new GenListModuleReader.Reference(URI.create("file:///foo/bar/baz.dita")));
    assertEquals(0, reader.waitList.size());
  }

  @Test
  public void categorizeReferenceFileDitamap() throws Exception {
    reader.categorizeReferenceFile(
      new GenListModuleReader.Reference(URI.create("file:///foo/bar/baz.ditamap"), ATTR_FORMAT_VALUE_DITAMAP)
    );
    assertEquals(1, reader.waitList.size());
  }

  @Test
  public void categorizeReferenceFileDitaval() throws Exception {
    reader.categorizeReferenceFile(
      new GenListModuleReader.Reference(URI.create("file:///foo/bar/baz.ditaval"), ATTR_FORMAT_VALUE_DITAVAL)
    );
    assertEquals(1, reader.formatSet.size());
  }

  @Test
  public void categorizeReferenceFileImage() throws Exception {
    reader.categorizeReferenceFile(
      new GenListModuleReader.Reference(URI.create("file:///foo/bar/baz.jpg"), ATTR_FORMAT_VALUE_IMAGE)
    );
    assertEquals(1, reader.formatSet.size());
  }
}
