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

public class ITPreprocess2 extends AbstractIntegrationTest implements ITPreprocessBase, ITContentUplevels {

  public ITPreprocess2 builder() {
    return new ITPreprocess2();
  }

  @Override
  Transtype getTranstype(Transtype transtype) {
    return switch (transtype) {
      case PREPROCESS -> PREPROCESS2;
      case XHTML -> XHTML_WITH_PREPROCESS2;
      default -> transtype;
    };
  }

  @Override
  @Disabled
  @Test
  public void copyto() throws Throwable {
    builder().name(Paths.get("copyto", "basic")).transtype(PREPROCESS).input(Paths.get("TC2.ditamap")).test();
  }

  @Override
  @Disabled
  @Test
  public void copyto_linktarget() throws Throwable {
    builder()
      .name(Paths.get("copyto", "copyto_linktarget"))
      .transtype(PREPROCESS)
      .input(Paths.get("linktarget.ditamap"))
      .errorCount(1)
      .warnCount(0)
      .test();
  }

  @Override
  @Disabled
  @Test
  public void copyto_extensions_metadata() throws Throwable {
    builder()
      .name(Paths.get("copyto", "copyto_extensions_metadata"))
      .transtype(PREPROCESS)
      .input(Paths.get("TC1.ditamap"))
      .test();
  }

  @Override
  @Disabled
  @Test
  public void copyto_circulartarget() throws Throwable {
    builder()
      .name(Paths.get("copyto", "copyto_circulartarget"))
      .transtype(PREPROCESS)
      .input(Paths.get("TC4.ditamap"))
      .test();
  }

  @Override
  @Disabled
  @Test
  public void copyto_sametarget2() throws Throwable {
    builder()
      .name(Paths.get("copyto", "copyto_sametarget2"))
      .transtype(PREPROCESS)
      .input(Paths.get("TC6.ditamap"))
      .warnCount(4)
      .test();
  }

  @Override
  @Disabled
  @Test
  public void copyto_sametarget() throws Throwable {
    builder()
      .name(Paths.get("copyto", "copyto_sametarget"))
      .transtype(PREPROCESS)
      .input(Paths.get("TC3.ditamap"))
      .warnCount(2)
      .test();
  }

  @Override
  @Disabled
  @Test
  public void uplevelslinkOnlytopic() throws Throwable {
    builder()
      .name("uplevelslink")
      .transtype(PREPROCESS)
      .input(Paths.get("main/uplevel-in-topic.ditamap"))
      .put("outer.control", "quiet")
      .put("onlytopic.in.map", "true")
      .warnCount(1)
      .test();
  }

  @Override
  @Disabled
  @Test
  public void CrawlMapPreprocess() throws Throwable {
    builder()
      .name("crawl_map")
      .transtype(PREPROCESS)
      .input(Paths.get("input.ditamap"))
      .put("link-crawl", "map")
      .errorCount(2)
      .warnCount(2)
      .test();
  }

  @Override
  @Disabled
  @Test
  public void chunk_uplevel() throws Throwable {
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
