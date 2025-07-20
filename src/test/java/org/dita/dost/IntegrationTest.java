/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2016 Jarno Elovirta
 *
 *  See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost;

import static org.dita.dost.AbstractIntegrationTest.Transtype.PREPROCESS;
import static org.dita.dost.AbstractIntegrationTest.Transtype.XHTML;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.dita.dost.reader.GrammarPoolManager;
import org.ditang.relaxng.defaults.pool.RNGDefaultsEnabledSynchronizedXMLGrammarPoolImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public abstract class IntegrationTest extends AbstractIntegrationTest {

  public abstract AbstractIntegrationTest builder();

  @Test
  public void testreltableHeaders() throws Throwable {
    builder().name("reltableHeaders").transtype(PREPROCESS).input(Paths.get("reltableheader.ditamap")).test();
  }

  @Test
  public void testreltableTextlink() throws Throwable {
    builder().name("reltableTextlink").transtype(PREPROCESS).input(Paths.get("1132.ditamap")).errorCount(1).test();
  }

  @Test
  public void testlocktitle() throws Throwable {
    builder().name("locktitle").transtype(PREPROCESS).input(Paths.get("TestingLocktitle.ditamap")).test();
  }

  @Test
  public void testchunk_uplevel() throws Throwable {
    builder()
      .name("chunk_uplevel")
      .transtype(PREPROCESS)
      .input(Paths.get("main/chunkup.ditamap"))
      .put("outer.control", "quiet")
      .test();
  }

  @Test
  public void testcopyto_extensions_metadata() throws Throwable {
    builder()
      .name(Paths.get("copyto", "copyto_extensions_metadata"))
      .transtype(PREPROCESS)
      .input(Paths.get("TC1.ditamap"))
      .warnCount(3)
      .test();
  }

  @Test
  public void testcopyto() throws Throwable {
    builder()
      .name(Paths.get("copyto", "basic"))
      .transtype(PREPROCESS)
      .input(Paths.get("TC2.ditamap"))
      .warnCount(2)
      .test();
  }

  @Test
  public void testcopyto_sametarget() throws Throwable {
    builder()
      .name(Paths.get("copyto", "copyto_sametarget"))
      .transtype(PREPROCESS)
      .input(Paths.get("TC3.ditamap"))
      .warnCount(3)
      .test();
  }

  @Test
  public void testcopyto_circulartarget() throws Throwable {
    builder()
      .name(Paths.get("copyto", "copyto_circulartarget"))
      .transtype(PREPROCESS)
      .input(Paths.get("TC4.ditamap"))
      .test();
  }

  @Test
  public void testcopyto_linktarget() throws Throwable {
    builder()
      .name(Paths.get("copyto", "copyto_linktarget"))
      .transtype(PREPROCESS)
      .input(Paths.get("linktarget.ditamap"))
      .errorCount(2)
      .warnCount(0)
      .test();
  }

  @Test
  public void testcopyto_sametarget2() throws Throwable {
    builder()
      .name(Paths.get("copyto", "copyto_sametarget2"))
      .transtype(PREPROCESS)
      .input(Paths.get("TC6.ditamap"))
      .warnCount(4)
      .test();
  }

  @Test
  public void testconkeyref_push() throws Throwable {
    builder()
      .name(Paths.get("conref", "conkeyref_push"))
      .transtype(PREPROCESS)
      .input(Paths.get("conref-push-test.ditamap"))
      .put("dita.ext", ".dita")
      .test();
  }

  @Test
  public void testlink_foreignshortdesc() throws Throwable {
    builder()
      .name("link_foreignshortdesc")
      .transtype(PREPROCESS)
      .input(Paths.get("main.ditamap"))
      .put("validate", "false")
      .warnCount(1)
      .test();
  }

  @Test
  public void testconrefend() throws Throwable {
    builder().name(Paths.get("conref", "conrefend")).transtype(PREPROCESS).input(Paths.get("range.ditamap")).test();
  }

  @Test
  public void testcoderef_source() throws Throwable {
    builder()
      .name("coderef_source")
      .transtype(PREPROCESS)
      .input(Paths.get("mp.ditamap"))
      .put("transtype", "preprocess")
      .put("dita.ext", ".dita")
      .put("validate", "false")
      .warnCount(1)
      .test();
  }

  @Test
  public void testconref() throws Throwable {
    builder()
      .name(Paths.get("conref", "basic"))
      .transtype(PREPROCESS)
      .input(Paths.get("lang-common1.dita"))
      .put("validate", "false")
      .warnCount(2)
      .test();
  }

  @Test
  public void testconref_to_specialization() throws Throwable {
    builder()
      .name(Paths.get("conref", "conref_to_specialization"))
      .transtype(PREPROCESS)
      .input(Paths.get("conref_to_specialization.dita"))
      .put("validate", "false")
      .warnCount(1)
      .test();
  }

  @Test
  public void testconrefbreaksxref() throws Throwable {
    builder()
      .name(Paths.get("conref", "conrefbreaksxref"))
      .transtype(PREPROCESS)
      .input(Paths.get("conrefbreaksxref.dita"))
      .test();
  }

  @Test
  public void testconrefinsubmap() throws Throwable {
    builder()
      .name(Paths.get("conref", "conrefinsubmap"))
      .transtype(PREPROCESS)
      .input(Paths.get("rootmap.ditamap"))
      .test();
  }

  @Test
  public void testconrefmissingfile() throws Throwable {
    builder()
      .name(Paths.get("conref", "conrefmissingfile"))
      .transtype(PREPROCESS)
      .input(Paths.get("badconref.dita"))
      .put("validate", "false")
      .warnCount(1)
      .errorCount(2)
      .test();
  }

  @Test
  public void testcontrolValueFile1() throws Throwable {
    builder()
      .name(Paths.get("filter", "map13_filter1"))
      .transtype(PREPROCESS)
      .input(Paths.get("map13.ditamap"))
      .put("args.filter", Paths.get("filter1.ditaval"))
      .test();
  }

  @Test
  public void testcontrolValueFile2() throws Throwable {
    builder()
      .name(Paths.get("filter", "map13_filter2"))
      .transtype(PREPROCESS)
      .input(Paths.get("map13.ditamap"))
      .put("args.filter", Paths.get("filter2.ditaval"))
      .test();
  }

  @Test
  public void testcontrolValueFile3() throws Throwable {
    builder()
      .name(Paths.get("filter", "map13_filter3"))
      .transtype(PREPROCESS)
      .input(Paths.get("map13.ditamap"))
      .put("args.filter", Paths.get("filter3.ditaval"))
      .test();
  }

  @Test
  public void testcontrolValueFile4() throws Throwable {
    builder()
      .name(Paths.get("filter", "map31_filter_multi"))
      .transtype(PREPROCESS)
      .input(Paths.get("map31.ditamap"))
      .put("args.filter", Paths.get("filter_multi.ditaval"))
      .warnCount(1)
      .test();
  }

  @Test
  public void testcontrolValueFile5() throws Throwable {
    builder()
      .name(Paths.get("filter", "map32_filter_multi"))
      .transtype(PREPROCESS)
      .input(Paths.get("map32.ditamap"))
      .put("args.filter", Paths.get("filter_multi.ditaval"))
      .warnCount(1)
      .test();
  }

  @Test
  public void testcontrolValueFile6() throws Throwable {
    builder()
      .name(Paths.get("filter", "map33_filter2"))
      .transtype(PREPROCESS)
      .input(Paths.get("map33.ditamap"))
      .put("args.filter", Paths.get("filter2.ditaval"))
      .test();
  }

  @Test
  public void testcontrolValueFile7() throws Throwable {
    builder()
      .name(Paths.get("filter", "map33_filter3"))
      .transtype(PREPROCESS)
      .input(Paths.get("map33.ditamap"))
      .put("args.filter", Paths.get("filter3.ditaval"))
      .test();
  }

  @Test
  public void testkeyref() throws Throwable {
    builder().name(Paths.get("keyref", "basic")).transtype(PREPROCESS).input(Paths.get("test.ditamap")).test();
  }

  @Test
  public void testkeyrefKeywordConref() throws Throwable {
    builder().name(Paths.get("keyref", "keyword_conref")).transtype(PREPROCESS).input(Paths.get("test.ditamap")).test();
  }

  @Test
  public void testuplevelslink() throws Throwable {
    builder()
      .name("uplevelslink")
      .transtype(PREPROCESS)
      .input(Paths.get("main/uplevel-in-topic.ditamap"))
      .put("outer.control", "quiet")
      .test();
  }

  @Test
  public void testuplevelslinkOnlytopic() throws Throwable {
    builder()
      .name("uplevelslink")
      .transtype(PREPROCESS)
      .input(Paths.get("main/uplevel-in-topic.ditamap"))
      .put("outer.control", "quiet")
      .put("onlytopic.in.map", "true")
      .test();
  }

  @Test
  public void testmappull_topicid() throws Throwable {
    builder()
      .name("mappull-topicid")
      .transtype(PREPROCESS)
      .input(Paths.get("reftopicid.ditamap"))
      .put("validate", "false")
      .warnCount(1)
      .test();
  }

  @Test
  public void testCrawlTopicPreprocess() throws Throwable {
    builder()
      .name("crawl_topic")
      .transtype(PREPROCESS)
      .input(Paths.get("input.ditamap"))
      .put("link-crawl", "topic")
      .test();
  }

  @Test
  public void testCrawlMapPreprocess() throws Throwable {
    builder()
      .name("crawl_map")
      .transtype(PREPROCESS)
      .input(Paths.get("input.ditamap"))
      .put("link-crawl", "map")
      .errorCount(2)
      .warnCount(0)
      .test();
  }

  @Test
  public void testRng() throws Throwable {
    builder().name("rng").transtype(PREPROCESS).input(Paths.get("root.ditamap")).test();
  }

  @Test
  public void resource_map() throws Throwable {
    builder()
      .name("resource_map")
      .transtype(PREPROCESS)
      .input(Paths.get("map.ditamap"))
      .put("args.resources", Paths.get("keys.ditamap"))
      .test();
  }

  @Test
  public void resource_topic() throws Throwable {
    builder()
      .name("resource_topic")
      .transtype(PREPROCESS)
      .input(Paths.get("topic.dita"))
      .put("args.resources", Paths.get("keys.ditamap"))
      .test();
  }

  @Test
  public void testRngGrammarPool() throws Throwable {
    RNGDefaultsEnabledSynchronizedXMLGrammarPoolImpl grammarPool =
      (RNGDefaultsEnabledSynchronizedXMLGrammarPoolImpl) GrammarPoolManager.getGrammarPool();
    grammarPool.clear();
    builder().name("bookmap-rng-based").transtype(PREPROCESS).input(Paths.get("main.ditamap")).test();
    assertEquals(3, grammarPool.getCacheSize());
  }

  @Test
  public void testRngNoGrammarPool() throws Throwable {
    RNGDefaultsEnabledSynchronizedXMLGrammarPoolImpl grammarPool =
      (RNGDefaultsEnabledSynchronizedXMLGrammarPoolImpl) GrammarPoolManager.getGrammarPool();
    grammarPool.clear();
    builder()
      .name("bookmap-rng-based")
      .transtype(PREPROCESS)
      .input(Paths.get("main.ditamap"))
      .put("args.grammar.cache", "no")
      .test();
    assertEquals(0, grammarPool.getCacheSize());
  }

  @Test
  public void testRngGrammarPoolValidate() throws Throwable {
    builder()
      .name("bookmap-rng-based-validate")
      .transtype(PREPROCESS)
      .input(Paths.get("main.ditamap"))
      .put("validate", "true")
      .errorCount(1)
      .test();
  }

  @Test
  public void testSubjectSchema() throws Throwable {
    builder()
      .name("subjectschema_case")
      .transtype(PREPROCESS)
      .input(Paths.get("simplemap.ditamap"))
      .put("args.filter", Paths.get("filter.ditaval"))
      .test();
  }

  @Test
  public void testuplevels1() throws Throwable {
    var outerFiles = List.of("images/carwash.gif", "a.html", "b.html", "topics/c.html", "topics/d.html");
    var outDir = new File(
      "build" +
      File.separator +
      "out" +
      File.separator +
      "integrationTest" +
      File.separator +
      "uplevels1" +
      File.separator +
      "temp"
    );
    for (String outerFile : outerFiles) {
      Path outerFilePath = Paths.get(outDir + File.separator + outerFile);
      if (Files.exists(outerFilePath)) Files.delete(outerFilePath);
    }
    var actDir = builder()
      .name("uplevels1")
      .transtype(XHTML)
      .input(Paths.get("maps/above.ditamap"))
      .put("generate.copy.outer", "1")
      .put("outer.control", "quiet")
      .test();
    var outerFilesShouldNotExist = new ArrayList<Executable>();
    for (String outerFile : outerFiles) {
      var outerFilePath = Paths.get(actDir.getParent(), outerFile);
      outerFilesShouldNotExist.add(() -> assertFalse(Files.exists(outerFilePath), "outerFile exists: " + outerFilePath)
      );
    }
    assertAll(outerFilesShouldNotExist);
  }

  @Test
  public void testuplevels1_resource_only() throws Throwable {
    builder()
      .name("uplevels1_resource_only")
      .transtype(XHTML)
      .input(Paths.get("maps/above.ditamap"))
      .put("generate.copy.outer", "1")
      .put("outer.control", "quiet")
      .test();
  }

  @Test
  public void testuplevels3() throws Throwable {
    builder()
      .name("uplevels3")
      .transtype(XHTML)
      .input(Paths.get("maps/above.ditamap"))
      .put("generate.copy.outer", "3")
      .put("outer.control", "quiet")
      .test();
  }

  @Test
  public void testuplevels3_resource_only() throws Throwable {
    builder()
      .name("uplevels3_resource_only")
      .transtype(XHTML)
      .input(Paths.get("maps/above.ditamap"))
      .put("generate.copy.outer", "3")
      .put("outer.control", "quiet")
      .test();
  }
}
