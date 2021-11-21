/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2016 Jarno Elovirta
 *
 *  See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost;

import org.junit.Ignore;
import org.junit.Test;

import java.nio.file.Paths;

import static org.dita.dost.AbstractIntegrationTest.Transtype.PREPROCESS;

public abstract class IntegrationTest extends AbstractIntegrationTest {

    public abstract AbstractIntegrationTest builder();
    
    @Test
    public void testreltableHeaders() throws Throwable {
        builder().name("reltableHeaders")
                .transtype(PREPROCESS)
                .input(Paths.get("reltableheader.ditamap"))
                .test();
    }    
    
    @Test
    public void testreltableTextlink() throws Throwable {
        builder().name("reltableTextlink")
                .transtype(PREPROCESS)
                .input(Paths.get("1132.ditamap"))
                .errorCount(1)
                .test();
    }
    
    @Test
    public void testlocktitle() throws Throwable {
        builder().name("locktitle")
                .transtype(PREPROCESS)
                .input(Paths.get("TestingLocktitle.ditamap"))
                .test();
    }
    
    @Test
    public void testchunk_uplevel() throws Throwable {
        builder().name("chunk_uplevel")
                .transtype(PREPROCESS)
                .input(Paths.get("main/chunkup.ditamap"))
                .put("outer.control", "quiet")
                .test();
    }

    @Test
    public void testcopyto_extensions_metadata() throws Throwable {
        builder().name("copyto_extensions_metadata")
                .transtype(PREPROCESS)
                .input(Paths.get("TC1.ditamap"))
                .warnCount(3)
                .test();
    }

    @Test
    public void testcopyto() throws Throwable {
        builder().name("copyto")
                .transtype(PREPROCESS)
                .input(Paths.get("TC2.ditamap"))
                .warnCount(2)
                .test();
    }

    @Test
    public void testcopyto_sametarget() throws Throwable {
        builder().name("copyto_sametarget")
                .transtype(PREPROCESS)
                .input(Paths.get("TC3.ditamap"))
                .warnCount(3)
                .test();
    }

    @Test
    public void testcopyto_circulartarget() throws Throwable {
        builder().name("copyto_circulartarget")
                .transtype(PREPROCESS)
                .input(Paths.get("TC4.ditamap"))
                .test();
    }
    
    @Test
    public void testcopyto_linktarget() throws Throwable {
        builder().name("copyto_linktarget")
                .transtype(PREPROCESS)
                .input(Paths.get("linktarget.ditamap"))
                .errorCount(2)
                .warnCount(0)
                .test();
    }

    @Test
    public void testcopyto_sametarget2() throws Throwable {
        builder().name("copyto_sametarget2")
                .transtype(PREPROCESS)
                .input(Paths.get("TC6.ditamap"))
                .warnCount(4)
                .test();
    }

    @Test
    public void testtable_colwidth() throws Throwable {
        builder().name("table_colwidth")
                .transtype(PREPROCESS)
                .input(Paths.get("test.dita"))
                .test();
    }

    @Test
    public void testconkeyref_push() throws Throwable {
        builder().name("conkeyref_push")
                .transtype(PREPROCESS)
                .input(Paths.get("conref-push-test.ditamap"))
                .put("dita.ext", ".dita")
                .test();
    }

    @Test
    public void testlink_foreignshortdesc() throws Throwable {
        builder().name("link_foreignshortdesc")
                .transtype(PREPROCESS)
                .input(Paths.get("main.ditamap"))
                .put("validate", "false")
                .warnCount(1)
                .test();
    }

    @Test
    public void testconref_push() throws Throwable {
        builder().name("conref_push")
                .transtype(PREPROCESS)
                .input(Paths.get("push.ditamap"))
                .put("dita.ext", ".dita")
                .warnCount(2)
                .test();
    }
    
    @Test
    public void testconrefend() throws Throwable {
        builder().name("conrefend")
                .transtype(PREPROCESS)
                .input(Paths.get("range.ditamap"))
                .test();
    }

    @Test
    public void testmapref_topicrefID() throws Throwable {
        builder().name("mapref_topicrefID")
                .transtype(PREPROCESS)
                .input(Paths.get("bookmap.ditamap"))
                .test();
    }
    
    @Test
    public void testmapref_to_conref() throws Throwable {
        builder().name("mapref_to_conref")
                .transtype(PREPROCESS)
                .input(Paths.get("root.ditamap"))
                .test();
    }

    @Test
    public void testmapref_reltables() throws Throwable {
        builder().name("mapref_reltables")
                .transtype(PREPROCESS)
                .input(Paths.get("main.ditamap"))
                .test();
    }

    @Test
    public void testcoderef_source() throws Throwable {
        builder().name("coderef_source")
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
        builder().name("conref")
                .transtype(PREPROCESS)
                .input(Paths.get("lang-common1.dita"))
                .put("validate", "false")
                .warnCount(2)
                .test();
    }

    @Test
    public void testconref_to_specialization() throws Throwable {
        builder().name("conref_to_specialization")
                .transtype(PREPROCESS)
                .input(Paths.get("conref_to_specialization.dita"))
                .put("validate", "false")
                .warnCount(1)
                .test();
    }

    @Test
    public void testpushAfter_between_Specialization() throws Throwable {
        builder().name("pushAfter_between_Specialization")
                .transtype(PREPROCESS)
                .input(Paths.get("pushAfter.ditamap"))
//                        .put("validate", "false")
                .test();
    }

    @Test
    public void testpushAfter_with_crossRef() throws Throwable {
        builder().name("pushAfter_with_crossRef")
                .transtype(PREPROCESS)
                .input(Paths.get("pushAfter.ditamap"))
//                        .put("validate", "false")
                .test();
    }

    @Test
    public void testpushAfter_with_InvalidTarget() throws Throwable {
        builder().name("pushAfter_with_InvalidTarget")
                .transtype(PREPROCESS)
                .input(Paths.get("pushAfter.ditamap"))
//                        .put("validate", "false")
                .warnCount(1)
                .test();
    }

    @Test
    public void testpushAfter_without_conref() throws Throwable {
        builder().name("pushAfter_without_conref")
                .transtype(PREPROCESS)
                .input(Paths.get("pushAfter.ditamap"))
//                        .put("validate", "false")
                .test();
    }

    @Test
    public void testsimple_pushAfter() throws Throwable {
        builder().name("simple_pushAfter")
                .transtype(PREPROCESS)
                .input(Paths.get("pushAfter.ditamap"))
//                        .put("validate", "false")
                .test();
    }

    @Test
    public void testpushBefore_between_Specialization() throws Throwable {
        builder().name("pushBefore_between_Specialization")
                .transtype(PREPROCESS)
                .input(Paths.get("pushBefore.ditamap"))
//                        .put("validate", "false")
                .test();
    }

    @Test
    public void testpushBefore_with_crossRef() throws Throwable {
        builder().name("pushBefore_with_crossRef")
                .transtype(PREPROCESS)
                .input(Paths.get("pushBefore.ditamap"))
//                        .put("validate", "false")
                .warnCount(1)
                .test();
    }

    @Test
    public void testpushBefore_with_InvalidTarget() throws Throwable {
        builder().name("pushBefore_with_InvalidTarget")
                .transtype(PREPROCESS)
                .input(Paths.get("pushBefore.ditamap"))
//                        .put("validate", "false")
                .warnCount(1)
                .test();
    }

    @Test
    public void testpushBefore_without_conref() throws Throwable {
        builder().name("pushBefore_without_conref")
                .transtype(PREPROCESS)
                .input(Paths.get("pushBefore.ditamap"))
//                        .put("validate", "false")
                .test();
    }

    @Test
    public void testsimple_pushBefore() throws Throwable {
        builder().name("simple_pushBefore")
                .transtype(PREPROCESS)
                .input(Paths.get("pushBefore.ditamap"))
//                        .put("validate", "false")
                .test();
    }

    @Test
    public void testpushReplace_between_Specialization() throws Throwable {
        builder().name("pushReplace_between_Specialization")
                .transtype(PREPROCESS)
                .input(Paths.get("pushReplace.ditamap"))
//                        .put("validate", "false")
                .test();
    }

    @Test
    public void testpushReplace_with_crossRef() throws Throwable {
        builder().name("pushReplace_with_crossRef")
                .transtype(PREPROCESS)
                .input(Paths.get("pushReplace.ditamap"))
//                        .put("validate", "false")
                .test();
    }

    @Test
    public void testpushReplace_with_InvalidTarget() throws Throwable {
        builder().name("pushReplace_with_InvalidTarget")
                .transtype(PREPROCESS)
                .input(Paths.get("pushReplace.ditamap"))
//                        .put("validate", "false")
                .warnCount(1).errorCount(4)
                .test();
    }

    @Test
    public void testpushReplace_without_conref() throws Throwable {
        builder().name("pushReplace_without_conref")
                .transtype(PREPROCESS)
                .input(Paths.get("pushReplace.ditamap"))
//                        .put("validate", "false")
                .test();
    }

    @Test
    public void testsimple_pushReplace() throws Throwable {
        builder().name("simple_pushReplace")
                .transtype(PREPROCESS)
                .input(Paths.get("pushReplace.ditamap"))
//                        .put("validate", "false")
                .test();
    }

    @Test
    public void testconrefbreaksxref() throws Throwable {
        builder().name("conrefbreaksxref")
                .transtype(PREPROCESS)
                .input(Paths.get("conrefbreaksxref.dita"))
                .test();
    }
    
    @Test
    public void testconrefinsubmap() throws Throwable {
        builder().name("conrefinsubmap")
                .transtype(PREPROCESS)
                .input(Paths.get("rootmap.ditamap"))
                .test();
    }
    
    @Test
    public void testconrefmissingfile() throws Throwable {
        builder().name("conrefmissingfile")
                .transtype(PREPROCESS)
                .input(Paths.get("badconref.dita"))
                .put("validate", "false")
                .warnCount(1)
                .errorCount(2)
                .test();
    }

    @Test
    public void testcontrolValueFile1() throws Throwable {
        builder().name("map13_filter1")
                .transtype(PREPROCESS)
                .input(Paths.get("map13.ditamap"))
                .put("args.filter", Paths.get("filter1.ditaval"))
                .test();
    }

    @Test
    public void testcontrolValueFile2() throws Throwable {
        builder().name("map13_filter2")
                .transtype(PREPROCESS)
                .input(Paths.get("map13.ditamap"))
                .put("args.filter", Paths.get("filter2.ditaval"))
                .test();
    }

    @Test
    public void testcontrolValueFile3() throws Throwable {
        builder().name("map13_filter3")
                .transtype(PREPROCESS)
                .input(Paths.get("map13.ditamap"))
                .put("args.filter", Paths.get("filter3.ditaval"))
                .test();
    }

    @Test
    public void testcontrolValueFile4() throws Throwable {
        builder().name("map31_filter_multi")
                .transtype(PREPROCESS)
                .input(Paths.get("map31.ditamap"))
                .put("args.filter", Paths.get("filter_multi.ditaval"))
                .warnCount(1)
                .test();
    }

    @Test
    public void testcontrolValueFile5() throws Throwable {
        builder().name("map32_filter_multi")
                .transtype(PREPROCESS)
                .input(Paths.get("map32.ditamap"))
                .put("args.filter", Paths.get("filter_multi.ditaval"))
                .warnCount(1)
                .test();
    }

    @Test
    public void testcontrolValueFile6() throws Throwable {
        builder().name("map33_filter2")
                .transtype(PREPROCESS)
                .input(Paths.get("map33.ditamap"))
                .put("args.filter", Paths.get("filter2.ditaval"))
                .test();
    }

    @Test
    public void testcontrolValueFile7() throws Throwable {
        builder().name("map33_filter3")
                .transtype(PREPROCESS)
                .input(Paths.get("map33.ditamap"))
                .put("args.filter", Paths.get("filter3.ditaval"))
                .test();
    }

    // TODO Move the Eclipse Help plugin
    @Test
    @Ignore
    public void testexportanchors() throws Throwable {
        builder().name("exportanchors")
                .transtype(PREPROCESS)
                .input(Paths.get("test.ditamap"))
                .put("transtype", "eclipsehelp")
                .test();
    }

    @Test
    public void testkeyref() throws Throwable {
        builder().name("keyref")
                .transtype(PREPROCESS)
                .input(Paths.get("test.ditamap"))
                .test();
    }

    @Test
    public void testmapref() throws Throwable {
        builder().name("mapref")
                .transtype(PREPROCESS)
                .input(Paths.get("test.ditamap"))
                .put("generate-debug-attributes", "false")
                .errorCount(0)
                .test();
    }

    @Test
    public void testuplevelslink() throws Throwable {
        builder().name("uplevelslink")
                .transtype(PREPROCESS)
                .input(Paths.get("main/uplevel-in-topic.ditamap"))
                .put("outer.control", "quiet")
                .test();
    }
    
    @Test
    public void testuplevelslinkOnlytopic() throws Throwable {
        builder().name("uplevelslink")
                .transtype(PREPROCESS)
                .input(Paths.get("main/uplevel-in-topic.ditamap"))
                .put("outer.control", "quiet")
                .put("onlytopic.in.map", "true")
                .test();
    }
    
    @Test
    public void testmappull_topicid() throws Throwable {
        builder().name("mappull-topicid")
                .transtype(PREPROCESS)
                .input(Paths.get("reftopicid.ditamap"))
                .put("validate", "false")
                .warnCount(1)
                .test();
    }

    @Test
    public void testCrawlTopicPreprocess() throws Throwable {
        builder().name("crawl_topic")
                .transtype(PREPROCESS)
                .input(Paths.get("input.ditamap"))
                .put("link-crawl", "topic")
                .test();
    }

    @Test
    public void testCrawlMapPreprocess() throws Throwable {
        builder().name("crawl_map")
                .transtype(PREPROCESS)
                .input(Paths.get("input.ditamap"))
                .put("link-crawl", "map")
                .errorCount(2)
                .warnCount(0)
                .test();
    }

    @Test
    public void testRng() throws Throwable {
        builder().name("rng")
                .transtype(PREPROCESS)
                .input(Paths.get("root.ditamap"))
                .test();
    }

    @Test
    public void resource_map() throws Throwable {
        builder().name("resource_map")
                .transtype(PREPROCESS)
                .input(Paths.get("map.ditamap"))
                .put("args.resources", Paths.get("keys.ditamap"))
                .test();
    }

    @Test
    public void resource_topic() throws Throwable {
        builder().name("resource_topic")
                .transtype(PREPROCESS)
                .input(Paths.get("topic.dita"))
                .put("args.resources", Paths.get("keys.ditamap"))
                .test();
    }
}