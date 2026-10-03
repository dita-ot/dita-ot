/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2016 Jarno Elovirta
 *
 *  See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost;

import static org.dita.dost.AbstractIntegrationTest.Transtype.PREPROCESS;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Paths;
import org.dita.dost.reader.GrammarPoolManager;
import org.ditang.relaxng.defaults.pool.RNGDefaultsEnabledSynchronizedXMLGrammarPoolImpl;
import org.junit.jupiter.api.Test;

public interface ITPreprocessBase {
  AbstractIntegrationTest builder();

  @Test
  default void reltableHeaders() throws Throwable {
    builder().name("reltableHeaders").transtype(PREPROCESS).input(Paths.get("reltableheader.ditamap")).test();
  }

  @Test
  default void reltableTextlink() throws Throwable {
    builder().name("reltableTextlink").transtype(PREPROCESS).input(Paths.get("1132.ditamap")).errorCount(1).test();
  }

  @Test
  default void locktitle() throws Throwable {
    builder().name("locktitle").transtype(PREPROCESS).input(Paths.get("TestingLocktitle.ditamap")).test();
  }

  @Test
  default void chunk_uplevel() throws Throwable {
    builder()
      .name("chunk_uplevel")
      .transtype(PREPROCESS)
      .input(Paths.get("main/chunkup.ditamap"))
      .put("outer.control", "quiet")
      .warnCount(4)
      .test();
  }

  @Test
  default void copyto_extensions_metadata() throws Throwable {
    builder()
      .name(Paths.get("copyto", "copyto_extensions_metadata"))
      .transtype(PREPROCESS)
      .input(Paths.get("TC1.ditamap"))
      .warnCount(3)
      .test();
  }

  @Test
  default void copyto() throws Throwable {
    builder()
      .name(Paths.get("copyto", "basic"))
      .transtype(PREPROCESS)
      .input(Paths.get("TC2.ditamap"))
      .warnCount(2)
      .test();
  }

  @Test
  default void copyto_sametarget() throws Throwable {
    builder()
      .name(Paths.get("copyto", "copyto_sametarget"))
      .transtype(PREPROCESS)
      .input(Paths.get("TC3.ditamap"))
      .warnCount(3)
      .test();
  }

  @Test
  default void copyto_circulartarget() throws Throwable {
    builder()
      .name(Paths.get("copyto", "copyto_circulartarget"))
      .transtype(PREPROCESS)
      .input(Paths.get("TC4.ditamap"))
      .test();
  }

  @Test
  default void copyto_linktarget() throws Throwable {
    builder()
      .name(Paths.get("copyto", "copyto_linktarget"))
      .transtype(PREPROCESS)
      .input(Paths.get("linktarget.ditamap"))
      .errorCount(2)
      .warnCount(0)
      .test();
  }

  @Test
  default void copyto_sametarget2() throws Throwable {
    builder()
      .name(Paths.get("copyto", "copyto_sametarget2"))
      .transtype(PREPROCESS)
      .input(Paths.get("TC6.ditamap"))
      .warnCount(4)
      .test();
  }

  @Test
  default void conkeyref_push() throws Throwable {
    builder()
      .name(Paths.get("conref", "conkeyref_push"))
      .transtype(PREPROCESS)
      .input(Paths.get("conref-push-test.ditamap"))
      .put("dita.ext", ".dita")
      .test();
  }

  @Test
  default void link_foreignshortdesc() throws Throwable {
    builder()
      .name("link_foreignshortdesc")
      .transtype(PREPROCESS)
      .input(Paths.get("main.ditamap"))
      .put("validate", "false")
      .warnCount(1)
      .test();
  }

  @Test
  default void conrefend() throws Throwable {
    builder().name(Paths.get("conref", "conrefend")).transtype(PREPROCESS).input(Paths.get("range.ditamap")).test();
  }

  @Test
  default void coderef_source() throws Throwable {
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
  default void conref() throws Throwable {
    builder()
      .name(Paths.get("conref", "basic"))
      .transtype(PREPROCESS)
      .input(Paths.get("lang-common1.dita"))
      .put("validate", "false")
      .warnCount(2)
      .test();
  }

  @Test
  default void conref_to_specialization() throws Throwable {
    builder()
      .name(Paths.get("conref", "conref_to_specialization"))
      .transtype(PREPROCESS)
      .input(Paths.get("conref_to_specialization.dita"))
      .put("validate", "false")
      .warnCount(1)
      .test();
  }

  @Test
  default void conrefbreaksxref() throws Throwable {
    builder()
      .name(Paths.get("conref", "conrefbreaksxref"))
      .transtype(PREPROCESS)
      .input(Paths.get("conrefbreaksxref.dita"))
      .test();
  }

  @Test
  default void conrefinsubmap() throws Throwable {
    builder()
      .name(Paths.get("conref", "conrefinsubmap"))
      .transtype(PREPROCESS)
      .input(Paths.get("rootmap.ditamap"))
      .test();
  }

  @Test
  default void conrefmissingfile() throws Throwable {
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
  default void controlValueFile1() throws Throwable {
    builder()
      .name(Paths.get("filter", "map13_filter1"))
      .transtype(PREPROCESS)
      .input(Paths.get("map13.ditamap"))
      .put("args.filter", Paths.get("filter1.ditaval"))
      .test();
  }

  @Test
  default void controlValueFile2() throws Throwable {
    builder()
      .name(Paths.get("filter", "map13_filter2"))
      .transtype(PREPROCESS)
      .input(Paths.get("map13.ditamap"))
      .put("args.filter", Paths.get("filter2.ditaval"))
      .test();
  }

  @Test
  default void controlValueFile3() throws Throwable {
    builder()
      .name(Paths.get("filter", "map13_filter3"))
      .transtype(PREPROCESS)
      .input(Paths.get("map13.ditamap"))
      .put("args.filter", Paths.get("filter3.ditaval"))
      .test();
  }

  @Test
  default void controlValueFile4() throws Throwable {
    builder()
      .name(Paths.get("filter", "map31_filter_multi"))
      .transtype(PREPROCESS)
      .input(Paths.get("map31.ditamap"))
      .put("args.filter", Paths.get("filter_multi.ditaval"))
      .warnCount(1)
      .test();
  }

  @Test
  default void controlValueFile5() throws Throwable {
    builder()
      .name(Paths.get("filter", "map32_filter_multi"))
      .transtype(PREPROCESS)
      .input(Paths.get("map32.ditamap"))
      .put("args.filter", Paths.get("filter_multi.ditaval"))
      .warnCount(1)
      .test();
  }

  @Test
  default void controlValueFile6() throws Throwable {
    builder()
      .name(Paths.get("filter", "map33_filter2"))
      .transtype(PREPROCESS)
      .input(Paths.get("map33.ditamap"))
      .put("args.filter", Paths.get("filter2.ditaval"))
      .test();
  }

  @Test
  default void controlValueFile7() throws Throwable {
    builder()
      .name(Paths.get("filter", "map33_filter3"))
      .transtype(PREPROCESS)
      .input(Paths.get("map33.ditamap"))
      .put("args.filter", Paths.get("filter3.ditaval"))
      .test();
  }

  @Test
  default void keyref() throws Throwable {
    builder().name(Paths.get("keyref", "basic")).transtype(PREPROCESS).input(Paths.get("test.ditamap")).test();
  }

  @Test
  default void keyrefKeywordConref() throws Throwable {
    builder().name(Paths.get("keyref", "keyword_conref")).transtype(PREPROCESS).input(Paths.get("test.ditamap")).test();
  }

  @Test
  default void uplevelslink() throws Throwable {
    builder()
      .name("uplevelslink")
      .transtype(PREPROCESS)
      .input(Paths.get("main/uplevel-in-topic.ditamap"))
      .put("outer.control", "quiet")
      .test();
  }

  @Test
  default void uplevelslinkOnlytopic() throws Throwable {
    builder()
      .name("uplevelslink")
      .transtype(PREPROCESS)
      .input(Paths.get("main/uplevel-in-topic.ditamap"))
      .put("outer.control", "quiet")
      .put("onlytopic.in.map", "true")
      .test();
  }

  @Test
  default void mappull_topicid() throws Throwable {
    builder()
      .name("mappull-topicid")
      .transtype(PREPROCESS)
      .input(Paths.get("reftopicid.ditamap"))
      .put("validate", "false")
      .warnCount(1)
      .test();
  }

  @Test
  default void crawlTopicPreprocess() throws Throwable {
    builder()
      .name("crawl_topic")
      .transtype(PREPROCESS)
      .input(Paths.get("input.ditamap"))
      .put("link-crawl", "topic")
      .test();
  }

  @Test
  default void crawlMapPreprocess() throws Throwable {
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
  default void rng() throws Throwable {
    builder().name("rng").transtype(PREPROCESS).input(Paths.get("root.ditamap")).test();
  }

  @Test
  default void resource_map() throws Throwable {
    builder()
      .name("resource_map")
      .transtype(PREPROCESS)
      .input(Paths.get("map.ditamap"))
      .put("args.resources", Paths.get("keys.ditamap"))
      .test();
  }

  @Test
  default void resource_topic() throws Throwable {
    builder()
      .name("resource_topic")
      .transtype(PREPROCESS)
      .input(Paths.get("topic.dita"))
      .put("args.resources", Paths.get("keys.ditamap"))
      .test();
  }

  @Test
  default void rngGrammarPool() throws Throwable {
    RNGDefaultsEnabledSynchronizedXMLGrammarPoolImpl grammarPool =
      (RNGDefaultsEnabledSynchronizedXMLGrammarPoolImpl) GrammarPoolManager.getGrammarPool();
    grammarPool.clear();
    builder().name("bookmap-rng-based").transtype(PREPROCESS).input(Paths.get("main.ditamap")).test();
    assertEquals(3, grammarPool.getCacheSize());
  }

  @Test
  default void rngNoGrammarPool() throws Throwable {
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
  default void rngGrammarPoolValidate() throws Throwable {
    builder()
      .name("bookmap-rng-based-validate")
      .transtype(PREPROCESS)
      .input(Paths.get("main.ditamap"))
      .put("validate", "true")
      .errorCount(1)
      .test();
  }

  @Test
  default void subjectSchema() throws Throwable {
    builder()
      .name("subjectschema_case")
      .transtype(PREPROCESS)
      .input(Paths.get("simplemap.ditamap"))
      .put("args.filter", Paths.get("filter.ditaval"))
      .test();
  }
}
