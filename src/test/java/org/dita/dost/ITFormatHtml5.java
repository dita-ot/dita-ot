/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2018 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost;

import static org.dita.dost.AbstractIntegrationTest.Transtype.HTML5;
import static org.dita.dost.AbstractIntegrationTest.Transtype.PREPROCESS2;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

public class ITFormatHtml5 extends AbstractIntegrationTest {

  public AbstractIntegrationTest builder() {
    return new ITFormatHtml5();
  }

  @Override
  Transtype getTranstype(Transtype transtype) {
    return switch (transtype) {
      case PREPROCESS -> PREPROCESS2;
      case XHTML -> HTML5;
      default -> transtype;
    };
  }

  @Test
  public void html5() throws Throwable {
    builder().name("html5").transtype(HTML5).input(Paths.get("map.ditamap")).put("args.gen.task.lbl", "YES").test();
  }

  @Test
  public void html5_cssNoCopy() throws Throwable {
    final File srcDir = new File(resourceDir, "html5_css" + File.separator + "src");
    final File actDir = builder()
      .name("html5_css")
      .transtype(HTML5)
      .input(Paths.get("topic.dita"))
      .put("args.css", "custom.css")
      .put("args.cssroot", srcDir.getAbsolutePath())
      .put("args.copycss", "no")
      .test();
    assertTrue(new File(actDir, "commonltr.css").exists());
    assertFalse(new File(actDir, "custom.css").exists());
  }

  @Test
  public void html5_css() throws Throwable {
    final File srcDir = new File(resourceDir, "html5_css" + File.separator + "src");
    final File actDir = builder()
      .name("html5_css")
      .transtype(HTML5)
      .input(Paths.get("topic.dita"))
      .put("args.css", "custom.css")
      .put("args.cssroot", srcDir.getAbsolutePath())
      .put("args.copycss", "yes")
      .test();
    assertTrue(new File(actDir, "commonltr.css").exists());
    assertTrue(new File(actDir, "custom.css").exists());
  }

  @Test
  public void html5_csspath() throws Throwable {
    final File srcDir = new File(resourceDir, "html5_csspath" + File.separator + "src");
    final File actDir = builder()
      .name("html5_csspath")
      .transtype(HTML5)
      .input(Paths.get("topic.dita"))
      .put("args.css", "custom.css")
      .put("args.cssroot", srcDir.getAbsolutePath())
      .put("args.csspath", "styles")
      .put("args.copycss", "yes")
      .test();
    assertTrue(new File(actDir, "styles" + File.separator + "commonltr.css").exists());
    assertTrue(new File(actDir, "styles" + File.separator + "custom.css").exists());
  }

  @Test
  public void html5_csspathAbsolute() throws Throwable {
    final File srcDir = new File(resourceDir, "html5_csspath" + File.separator + "src");
    final File actDir = builder()
      .name("html5_csspath")
      .transtype(HTML5)
      .input(Paths.get("topic.dita"))
      .put("args.css", new File(srcDir, "custom.css").getAbsolutePath())
      .put("args.csspath", "styles")
      .put("args.copycss", "yes")
      .test();
    assertTrue(new File(actDir, "styles" + File.separator + "commonltr.css").exists());
    assertTrue(new File(actDir, "styles" + File.separator + "custom.css").exists());
  }

  @Test
  public void html5_cssExternal() throws Throwable {
    builder()
      .name("html5_cssExternal")
      .transtype(HTML5)
      .input(Paths.get("topic.dita"))
      .put("args.css", "custom.css")
      .put("args.csspath", "https://example.com/styles")
      .test();
  }

  @Test
  public void html5_ditaval_passthrough_all() throws Throwable {
    final File srcDir = new File(resourceDir, "html5_ditaval" + File.separator + "src");
    builder()
      .name("html5_ditaval")
      .transtype(HTML5)
      .input(Paths.get("all.ditamap"))
      .put("validate", "no")
      .put("dita.input.valfile", new File(srcDir, "all.ditaval").getAbsolutePath())
      .warnCount(1)
      .test();
  }

  @Test
  public void html5_ditaval_passthrough_groups_all() throws Throwable {
    final File srcDir = new File(resourceDir, "html5_ditaval_groups" + File.separator + "src");
    builder()
      .name("html5_ditaval_groups")
      .transtype(HTML5)
      .input(Paths.get("all.dita"))
      .put("validate", "no")
      .put("dita.input.valfile", new File(srcDir, "all.ditaval").getAbsolutePath())
      .warnCount(1)
      .test();
  }
}
