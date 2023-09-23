/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2017 Jarno Elovirta
 *
 *  See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost;

import static org.dita.dost.AbstractIntegrationTest.Transtype.*;

import java.nio.file.Paths;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class IntegrationTestPreprocess2 extends IntegrationTest {

  public IntegrationTestPreprocess2 builder() {
    return new IntegrationTestPreprocess2();
  }

  @Override
  Transtype getTranstype(Transtype transtype) {
    switch (transtype) {
      case PREPROCESS:
        return PREPROCESS2;
      case XHTML:
        return XHTML_WITH_PREPROCESS2;
      default:
        return transtype;
    }
  }

  @Disabled
  @Test
  public void testcopyto() throws Throwable {
    builder().name(Paths.get("copyto", "basic")).transtype(PREPROCESS).input(Paths.get("TC2.ditamap")).test();
  }

  @Test
  public void testconrefmissingfile() throws Throwable {
    builder()
      .name(Paths.get("conref", "conrefmissingfile"))
      .transtype(PREPROCESS)
      .input(Paths.get("badconref.dita"))
      .put("validate", "false")
      .warnCount(1)
      .errorCount(5)
      .test();
  }

  @Test
  public void testmapref() throws Throwable {
    builder()
      .name(Paths.get("mapref", "basic"))
      .transtype(PREPROCESS)
      .input(Paths.get("test.ditamap"))
      .put("generate-debug-attributes", "false")
      .errorCount(3)
      .warnCount(1)
      .test();
  }

  @Disabled
  @Test
  public void testcopyto_linktarget() throws Throwable {
    builder()
      .name(Paths.get("copyto", "copyto_linktarget"))
      .transtype(PREPROCESS)
      .input(Paths.get("linktarget.ditamap"))
      .errorCount(1)
      .warnCount(0)
      .test();
  }

  @Test
  public void testcontrolValueFile4() throws Throwable {
    builder()
      .name(Paths.get("filter", "map31_filter_multi"))
      .transtype(PREPROCESS)
      .input(Paths.get("map31.ditamap"))
      .put("args.filter", Paths.get("filter_multi.ditaval"))
      .warnCount(0)
      .test();
  }

  @Disabled
  @Test
  public void testcopyto_extensions_metadata() throws Throwable {
    builder()
      .name(Paths.get("copyto", "copyto_extensions_metadata"))
      .transtype(PREPROCESS)
      .input(Paths.get("TC1.ditamap"))
      .test();
  }

  @Disabled
  @Test
  public void testcopyto_circulartarget() throws Throwable {
    builder()
      .name(Paths.get("copyto", "copyto_circulartarget"))
      .transtype(PREPROCESS)
      .input(Paths.get("TC4.ditamap"))
      .test();
  }

  @Disabled
  @Test
  public void testcopyto_sametarget2() throws Throwable {
    builder()
      .name(Paths.get("copyto", "copyto_sametarget2"))
      .transtype(PREPROCESS)
      .input(Paths.get("TC6.ditamap"))
      .warnCount(4)
      .test();
  }

  @Disabled
  @Test
  public void testcopyto_sametarget() throws Throwable {
    builder()
      .name(Paths.get("copyto", "copyto_sametarget"))
      .transtype(PREPROCESS)
      .input(Paths.get("TC3.ditamap"))
      .warnCount(2)
      .test();
  }

  @Test
  public void testcontrolValueFile5() throws Throwable {
    builder()
      .name(Paths.get("filter", "map32_filter_multi"))
      .transtype(PREPROCESS)
      .input(Paths.get("map32.ditamap"))
      .put("args.filter", Paths.get("filter_multi.ditaval"))
      .warnCount(0)
      .test();
  }

  //
  //  @Disabled
  @Test
  public void testuplevelslinkOnlytopic() throws Throwable {
    builder()
      .name("uplevelslink")
      .transtype(PREPROCESS)
      .input(Paths.get("main/uplevel-in-topic.ditamap"))
      .put("outer.control", "quiet")
      .put("onlytopic.in.map", "true")
      .warnCount(1)
      .test();
  }

  @Disabled
  @Test
  public void testCrawlMapPreprocess() throws Throwable {
    builder()
      .name("crawl_map")
      .transtype(PREPROCESS)
      .input(Paths.get("input.ditamap"))
      .put("link-crawl", "map")
      .errorCount(2)
      .warnCount(2)
      .test();
  }

  @Disabled
  @Test
  public void testchunk_uplevel() throws Throwable {
    builder()
      .name("chunk_uplevel")
      .transtype(PREPROCESS)
      .input(Paths.get("main/chunkup.ditamap"))
      .put("outer.control", "quiet")
      .warnCount(0)
      .errorCount(5)
      .test();
  }
}
