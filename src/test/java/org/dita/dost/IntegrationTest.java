/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2016 Jarno Elovirta
 *
 *  See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost;

import org.junit.Test;

import java.nio.file.Paths;

import static org.dita.dost.AbstractIntegrationTest.Transtype.*;

public class IntegrationTest extends AbstractIntegrationTest {

    @Test
    public void test03() throws Throwable {
        builder().name("03")
                .transtype(XHTML)
                .input(Paths.get("03.ditamap"))
                .test();
    }

    @Test
    public void test1_5_2_M4_BUG3052904() throws Throwable {
        builder().name("1.5.2_M4_BUG3052904")
                .transtype(XHTML)
                .input(Paths.get("keyref-test-01.ditamap"))
                .test();
    }

    @Test
    public void test1_5_2_M4_BUG3052913() throws Throwable {
        builder().name("1.5.2_M4_BUG3052913")
                .transtype(XHTML)
                .input(Paths.get("keyref-test-01.ditamap"))
                .test();
    }

    @Test
    public void test1_5_2_M4_BUG3056939() throws Throwable {
        builder().name("1.5.2_M4_BUG3056939")
                .transtype(XHTML)
                .input(Paths.get("test-conref-xref-keyref-bug.ditamap"))
                .test();
    }

    @Test
    public void test1_5_2_M5_BUG3059256() throws Throwable {
        builder().name("1.5.2_M5_BUG3059256")
                .transtype(XHTML)
                .input(Paths.get("test.ditamap"))
                .test();
    }

    @Test
    public void test1_5_3_M2_BUG3157890() throws Throwable {
        builder().name("1.5.3_M2_BUG3157890")
                .transtype(XHTML)
                .input(Paths.get("test.ditamap"))
                .test();
    }

    @Test
    public void test1_5_3_M2_BUG3164866() throws Throwable {
        builder().name("1.5.3_M2_BUG3164866")
                .transtype(XHTML)
                .input(Paths.get("testpng.ditamap"))
                .put("onlytopic.in.map", "true")
                .test();
    }

    @Test
    public void test1_5_3_M3_BUG3178361() throws Throwable {
        builder().name("1.5.3_M3_BUG3178361")
                .transtype(XHTML)
                .input(Paths.get("test.ditamap"))
                .test();
    }

    @Test
    public void test1_5_3_M3_BUG3191701() throws Throwable {
        builder().name("1.5.3_M3_BUG3191701")
                .transtype(XHTML)
                .input(Paths.get("test.ditamap"))
                .test();
    }

    @Test
    public void test1_5_3_M3_BUG3191704() throws Throwable {
        builder().name("1.5.3_M3_BUG3191704")
                .transtype(XHTML)
                .input(Paths.get("test.ditamap"))
                .test();
    }

    @Test
    public void test22_TC1() throws Throwable {
        builder().name("22_TC1")
                .transtype(PREPROCESS)
                .input(Paths.get("TC1.ditamap"))
                .warnCount(3)
                .test();
    }

    @Test
    public void test22_TC2() throws Throwable {
        builder().name("22_TC2")
                .transtype(PREPROCESS)
                .input(Paths.get("TC2.ditamap"))
                .warnCount(2)
                .test();
    }

    @Test
    public void test22_TC3() throws Throwable {
        builder().name("22_TC3")
                .transtype(PREPROCESS)
                .input(Paths.get("TC3.ditamap"))
                .warnCount(3)
                .test();
    }

    @Test
    public void test22_TC4() throws Throwable {
        builder().name("22_TC4")
                .transtype(PREPROCESS)
                .input(Paths.get("TC4.ditamap"))
                .test();
    }

    @Test
    public void test22_TC6() throws Throwable {
        builder().name("22_TC6")
                .transtype(PREPROCESS)
                .input(Paths.get("TC6.ditamap"))
                .warnCount(4)
                .test();
    }

    @Test
    public void test2374525() throws Throwable {
        builder().name("2374525")
                .transtype(PREPROCESS)
                .input(Paths.get("test.dita"))
                .test();
    }

    @Test
    public void test3178361() throws Throwable {
        builder().name("3178361")
                .transtype(PREPROCESS)
                .input(Paths.get("conref-push-test.ditamap"))
                .put("dita.ext", ".dita")
                .test();
    }

    @Test
    public void test3189883() throws Throwable {
        builder().name("3189883")
                .transtype(PREPROCESS)
                .input(Paths.get("main.ditamap"))
                .put("validate", "false")
                .warnCount(1)
                .test();
    }

    @Test
    public void test3191704() throws Throwable {
        builder().name("3191704")
                .transtype(PREPROCESS)
                .input(Paths.get("jandrew-test.ditamap"))
                .put("dita.ext", ".dita")
                .test();
    }

    @Test
    public void test3344142() throws Throwable {
        builder().name("3344142")
                .transtype(PREPROCESS)
                .input(Paths.get("push.ditamap"))
                .put("dita.ext", ".dita")
                .warnCount(2)
                .test();
    }

    @Test
    public void test3470331() throws Throwable {
        builder().name("3470331")
                .transtype(PREPROCESS)
                .input(Paths.get("bookmap.ditamap"))
                .test();
    }

    @Test
    public void testMetadataInheritance() throws Throwable {
        test("MetadataInheritance");
    }

    @Test
    public void testSF1333481() throws Throwable {
        builder().name("SF1333481")
                .transtype(PREPROCESS)
                .input(Paths.get("main.ditamap"))
                .test();
    }

    @Test
    public void testBookmap1() throws Throwable {
        builder().name("bookmap1")
                .transtype(XHTML)
                .input(Paths.get("bookmap(2)_testdata1.ditamap"))
                .test();
    }

    @Test
    public void testBookmap2() throws Throwable {
        builder().name("bookmap2")
                .transtype(XHTML)
                .input(Paths.get("bookmap(2)_testdata2.ditamap"))
                .errorCount(1)
                .test();
    }

    @Test
    public void testBookmap3() throws Throwable {
        builder().name("bookmap3")
                .transtype(XHTML)
                .input(Paths.get("bookmap(2)_testdata3.ditamap"))
                .test();
    }

    @Test
    public void testBookmap4() throws Throwable {
        builder().name("bookmap4")
                .transtype(XHTML)
                .input(Paths.get("bookmap(2)_testdata4.ditamap"))
                .errorCount(1)
                .test();
    }

    @Test
    public void testBookmap5() throws Throwable {
        builder().name("bookmap5")
                .transtype(XHTML)
                .input(Paths.get("bookmap(2)_testdata5.ditamap"))
                .test();
    }

    @Test
    public void testBookmap6() throws Throwable {
        builder().name("bookmap6")
                .transtype(XHTML)
                .input(Paths.get("bookmap(2)_testdata6.ditamap"))
                .errorCount(1)
                .test();
    }

    @Test
    public void testBookmap7() throws Throwable {
        builder().name("bookmap7")
                .transtype(XHTML)
                .input(Paths.get("bookmap(2)_testdata7.ditamap"))
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
    public void testcontrol_value_file() throws Throwable {
        test("control_value_file");
    }

    @Test
    public void testexportanchors() throws Throwable {
        builder().name("exportanchors")
                .transtype(PREPROCESS)
                .input(Paths.get("test.ditamap"))
                .put("transtype", "eclipsehelp")
                .test();
    }

    @Test
    public void testimage_scale() throws Throwable {
        builder().name("image-scale")
                .transtype(XHTML)
                .input(Paths.get("test.dita"))
                .test();
    }

    @Test
    public void testindex_seeEclipseHelp() throws Throwable {
        builder().name("index-see")
                .transtype(ECLIPSEHELP)
                .input(Paths.get("bookmap.ditamap"))
                .warnCount(3)
                .test();
    }
    @Test
    public void testindex_seeHtmlhelp() throws Throwable {
        builder().name("index-see")
                .transtype(HTMLHELP)
                .input(Paths.get("bookmap.ditamap"))
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
    public void testkeyref_All_tags() throws Throwable {
        builder().name("keyref_All_tags")
                .transtype(XHTML)
                .input(Paths.get("mp_author1.ditamap"))
                .warnCount(1)
                .test();
    }

    @Test
    public void testkeyref_Keyword_links() throws Throwable {
        builder().name("keyref_Keyword_links")
                .transtype(XHTML)
                .input(Paths.get("mp_author1.ditamap"))
                .test();
    }

    @Test
    public void testkeyref_Redirect_conref_1() throws Throwable {
        builder().name("keyref_Redirect_conref_1")
                .transtype(XHTML)
                .input(Paths.get("mp_author1.ditamap"))
                .test();
    }

    @Test
    public void testkeyref_Redirect_conref_2() throws Throwable {
        builder().name("keyref_Redirect_conref_2")
                .transtype(XHTML)
                .input(Paths.get("mp_author2.ditamap"))
                .test();
    }

    @Test
    public void testkeyref_Redirect_link_or_xref_1() throws Throwable {
        builder().name("keyref_Redirect_link_or_xref_1")
                .transtype(XHTML)
                .input(Paths.get("mp_author1.ditamap"))
                .test();
    }

    @Test
    public void testkeyref_Redirect_link_or_xref_2() throws Throwable {
        builder().name("keyref_Redirect_link_or_xref_2")
                .transtype(XHTML)
                .input(Paths.get("mp_author2.ditamap"))
                .test();
    }

    @Test
    public void testkeyref_Redirect_link_or_xref_3() throws Throwable {
        builder().name("keyref_Redirect_link_or_xref_3")
                .transtype(XHTML)
                .input(Paths.get("mp_author3.ditamap"))
                .test();
    }

    @Test
    public void testkeyref_Redirect_link_or_xref_4() throws Throwable {
        builder().name("keyref_Redirect_link_or_xref_4")
                .transtype(XHTML)
                .input(Paths.get("mp_author4.ditamap"))
                .test();
    }

    @Test
    public void testkeyref_Redirect_link_or_xref_5() throws Throwable {
        builder().name("keyref_Redirect_link_or_xref_5")
                .transtype(XHTML)
                .input(Paths.get("mp_author5.ditamap"))
                .test();
    }

    @Test
    public void testkeyref_Redirect_link_or_xref_6() throws Throwable {
        builder().name("keyref_Redirect_link_or_xref_6")
                .transtype(XHTML)
                .input(Paths.get("mp_author6.ditamap"))
                .test();
    }

    @Test
    public void testkeyref_Splitting_combining_targets1() throws Throwable {
        builder().name("keyref_Splitting_combining_targets_1")
                .transtype(XHTML)
                .input(Paths.get("mp_author1.ditamap"))
                .test();
    }

    @Test
    public void testkeyref_Splitting_combining_targets2() throws Throwable {
        builder().name("keyref_Splitting_combining_targets_2")
                .transtype(XHTML)
                .input(Paths.get("mp_author2.ditamap"))
                .test();
    }

    @Test
    public void testkeyref_Splitting_combining_targets3() throws Throwable {
        builder().name("keyref_Splitting_combining_targets_3")
                .transtype(XHTML)
                .input(Paths.get("mp_author3.ditamap"))
                .test();
    }

    @Test
    public void testkeyref_Swap_out_variable_content() throws Throwable {
        builder().name("keyref_Swap_out_variable_content")
                .transtype(XHTML)
                .input(Paths.get("mp_author1.ditamap"))
                .test();
    }

    @Test
    public void testkeyref_modify() throws Throwable {
        builder().name("keyref_modify")
                .transtype(XHTML)
                .input(Paths.get("mp_author1.ditamap"))
                .warnCount(1)
                .test();
    }

    @Test
    public void testlang() throws Throwable {
        builder().name("lang")
                .transtype(XHTML)
                .input(Paths.get("lang.ditamap"))
                .put("validate", "false")
                .warnCount(1)
                .test();
    }

    @Test
    public void testmapref() throws Throwable {
        builder().name("mapref")
                .transtype(PREPROCESS)
                .input(Paths.get("test.ditamap"))
                .put("generate-debug-attributes", "false")
                .test();
    }

    @Test
    public void testsubjectschema_case() throws Throwable {
        builder().name("subjectschema_case")
                .transtype(XHTML)
                .input(Paths.get("simplemap.ditamap"))
                .put("args.filter", Paths.get("filter.ditaval"))
                .put("clean.temp", "no")
                .test();
    }

    @Test
    public void testuplevels1() throws Throwable {
        builder().name("uplevels1")
                .transtype(XHTML)
                .input(Paths.get("maps/above.ditamap"))
                .put("generate.copy.outer", "1")
                .put("outer.control", "quiet")
                .test();
    }

    @Test
    public void testuplevels3() throws Throwable {
        builder().name("uplevels3")
                .transtype(XHTML)
                .input(Paths.get("maps/above.ditamap"))
                .put("generate.copy.outer", "3")
                .put("outer.control", "quiet")
                .test();
    }

}