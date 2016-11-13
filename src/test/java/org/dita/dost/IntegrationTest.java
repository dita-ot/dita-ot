/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2016 Jarno Elovirta
 *
 *  See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.nio.file.Paths;

import static java.util.Collections.emptyMap;

public class IntegrationTest extends AbstractIntegrationTest {

    @Test
    public void test03() throws Throwable {
        test("03", Transtype.XHTML,
                Paths.get("03.ditamap"),
                emptyMap());
    }

    @Test
    public void test1_5_2_M4_BUG3052904() throws Throwable {
        test("1.5.2_M4_BUG3052904", Transtype.XHTML,
                Paths.get("keyref-test-01.ditamap"),
                emptyMap());
    }

    @Test
    public void test1_5_2_M4_BUG3052913() throws Throwable {
        test("1.5.2_M4_BUG3052913", Transtype.XHTML,
                Paths.get("keyref-test-01.ditamap"),
                emptyMap());
    }

    @Test
    public void test1_5_2_M4_BUG3056939() throws Throwable {
        test("1.5.2_M4_BUG3056939", Transtype.XHTML,
                Paths.get("test-conref-xref-keyref-bug.ditamap"),
                emptyMap());
    }

    @Test
    public void test1_5_2_M5_BUG3059256() throws Throwable {
        test("1.5.2_M5_BUG3059256", Transtype.XHTML,
                Paths.get("test.ditamap"),
                emptyMap());
    }

    @Test
    public void test1_5_3_M2_BUG3157890() throws Throwable {
        test("1.5.3_M2_BUG3157890", Transtype.XHTML,
                Paths.get("test.ditamap"),
                emptyMap());
    }

    @Test
    public void test1_5_3_M2_BUG3164866() throws Throwable {
        test("1.5.3_M2_BUG3164866", Transtype.XHTML,
                Paths.get("testpng.ditamap"),
                ImmutableMap.<String, Object>builder()
                        .put("onlytopic.in.map", "true")
                        .build());
    }

    @Test
    public void test1_5_3_M3_BUG3178361() throws Throwable {
        test("1.5.3_M3_BUG3178361", Transtype.XHTML,
                Paths.get("test.ditamap"),
                emptyMap());
    }

    @Test
    public void test1_5_3_M3_BUG3191701() throws Throwable {
        test("1.5.3_M3_BUG3191701", Transtype.XHTML,
                Paths.get("test.ditamap"),
                emptyMap());
    }

    @Test
    public void test1_5_3_M3_BUG3191704() throws Throwable {
        test("1.5.3_M3_BUG3191704", Transtype.XHTML,
                Paths.get("test.ditamap"),
                emptyMap());
    }

    @Test
    public void test22_TC1() throws Throwable {
        test("22_TC1", Transtype.PREPROCESS,
                Paths.get("TC1.ditamap"),
                emptyMap(),
                3, 0);
    }

    @Test
    public void test22_TC2() throws Throwable {
        test("22_TC2", Transtype.PREPROCESS,
                Paths.get("TC2.ditamap"),
                emptyMap(),
                2, 0);
    }

    @Test
    public void test22_TC3() throws Throwable {
        test("22_TC3", Transtype.PREPROCESS,
                Paths.get("TC3.ditamap"),
                emptyMap(),
                3, 0);
    }

    @Test
    public void test22_TC4() throws Throwable {
        test("22_TC4", Transtype.PREPROCESS,
                Paths.get("TC4.ditamap"),
                emptyMap());
    }

    @Test
    public void test22_TC6() throws Throwable {
        test("22_TC6", Transtype.PREPROCESS,
                Paths.get("TC6.ditamap"),
                emptyMap(),
                4, 0);
    }

    @Test
    public void test2374525() throws Throwable {
        test("2374525", Transtype.PREPROCESS,
                Paths.get("test.dita"),
                emptyMap());
    }

    @Test
    public void test3178361() throws Throwable {
        test("3178361", Transtype.PREPROCESS,
                Paths.get("conref-push-test.ditamap"),
                ImmutableMap.<String, Object>builder()
                        .put("dita.ext", ".dita")
                        .build());
    }

    @Test
    public void test3189883() throws Throwable {
        test("3189883", Transtype.PREPROCESS,
                Paths.get("main.ditamap"),
                ImmutableMap.<String, Object>builder()
                        .put("validate", "false")
                        .build(), 1, 0);
    }

    @Test
    public void test3191704() throws Throwable {
        test("3191704", Transtype.PREPROCESS,
                Paths.get("jandrew-test.ditamap"),
                ImmutableMap.<String, Object>builder()
                        .put("dita.ext", ".dita")
                        .build());
    }

    @Test
    public void test3344142() throws Throwable {
        test("3344142", Transtype.PREPROCESS,
                Paths.get("push.ditamap"),
                ImmutableMap.<String, Object>builder()
                        .put("dita.ext", ".dita")
                        .build(), 2, 0);
    }

    @Test
    public void test3470331() throws Throwable {
        test("3470331", Transtype.PREPROCESS,
                Paths.get("bookmap.ditamap"), emptyMap());
    }

    @Test
    public void testMetadataInheritance() throws Throwable {
        test("MetadataInheritance");
    }

    @Test
    public void testSF1333481() throws Throwable {
        test("SF1333481", Transtype.PREPROCESS,
                Paths.get("main.ditamap"), emptyMap());
//        test("SF1333481", Transtype.XHTML,
//                Paths.get("subdir", "mapref1.ditamap"), emptyMap());
    }

    @Test
    public void testBookmap1() throws Throwable {
        test("bookmap1", Transtype.XHTML,
                Paths.get("bookmap(2)_testdata1.ditamap"),
                emptyMap(),
                0, 0);
    }

    @Test
    public void testBookmap2() throws Throwable {
        test("bookmap2", Transtype.XHTML,
                Paths.get("bookmap(2)_testdata2.ditamap"),
                emptyMap(),
                0, 1);
    }

    @Test
    public void testBookmap3() throws Throwable {
        test("bookmap3", Transtype.XHTML,
                Paths.get("bookmap(2)_testdata3.ditamap"),
                emptyMap(),
                0, 0);
    }

    @Test
    public void testBookmap4() throws Throwable {
        test("bookmap4", Transtype.XHTML,
                Paths.get("bookmap(2)_testdata4.ditamap"),
                emptyMap(),
                0, 1);
    }

    @Test
    public void testBookmap5() throws Throwable {
        test("bookmap5", Transtype.XHTML,
                Paths.get("bookmap(2)_testdata5.ditamap"),
                emptyMap(),
                0, 0);
    }

    @Test
    public void testBookmap6() throws Throwable {
        test("bookmap6", Transtype.XHTML,
                Paths.get("bookmap(2)_testdata6.ditamap"),
                emptyMap(),
                0, 1);
    }

    @Test
    public void testBookmap7() throws Throwable {
        test("bookmap7", Transtype.XHTML,
                Paths.get("bookmap(2)_testdata7.ditamap"),
                emptyMap(),
                0, 0);
    }

    @Test
    public void testcoderef_source() throws Throwable {
        test("coderef_source", Transtype.PREPROCESS,
                Paths.get("mp.ditamap"),
                ImmutableMap.<String, Object>builder()
                        .put("transtype", "preprocess").put("dita.ext", ".dita").put("validate", "false")
                        .build(), 1, 0);
    }

    @Test
    public void testconref() throws Throwable {
        test("conref", Transtype.PREPROCESS, Paths.get("lang-common1.dita"),
                ImmutableMap.<String, Object>builder()
                        .put("validate", "false")
                        .build(), 1, 0);
    }

    @Test
    public void testpushAfter_between_Specialization() throws Throwable {
        test("pushAfter_between_Specialization", Transtype.PREPROCESS, Paths.get("pushAfter.ditamap"),
                ImmutableMap.<String, Object>builder()
//                        .put("validate", "false")
                        .build(), 0, 0);
    }

    @Test
    public void testpushAfter_with_crossRef() throws Throwable {
        test("pushAfter_with_crossRef", Transtype.PREPROCESS, Paths.get("pushAfter.ditamap"),
                ImmutableMap.<String, Object>builder()
//                        .put("validate", "false")
                        .build(), 0, 0);
    }

    @Test
    public void testpushAfter_with_InvalidTarget() throws Throwable {
        test("pushAfter_with_InvalidTarget", Transtype.PREPROCESS, Paths.get("pushAfter.ditamap"),
                ImmutableMap.<String, Object>builder()
//                        .put("validate", "false")
                        .build(), 1, 0);
    }

    @Test
    public void testpushAfter_without_conref() throws Throwable {
        test("pushAfter_without_conref", Transtype.PREPROCESS, Paths.get("pushAfter.ditamap"),
                ImmutableMap.<String, Object>builder()
//                        .put("validate", "false")
                        .build(), 0, 0);
    }

    @Test
    public void testsimple_pushAfter() throws Throwable {
        test("simple_pushAfter", Transtype.PREPROCESS, Paths.get("pushAfter.ditamap"),
                ImmutableMap.<String, Object>builder()
//                        .put("validate", "false")
                        .build(), 0, 0);
    }

    @Test
    public void testpushBefore_between_Specialization() throws Throwable {
        test("pushBefore_between_Specialization", Transtype.PREPROCESS, Paths.get("pushBefore.ditamap"),
                ImmutableMap.<String, Object>builder()
//                        .put("validate", "false")
                        .build(), 0, 0);
    }

    @Test
    public void testpushBefore_with_crossRef() throws Throwable {
        test("pushBefore_with_crossRef", Transtype.PREPROCESS, Paths.get("pushBefore.ditamap"),
                ImmutableMap.<String, Object>builder()
//                        .put("validate", "false")
                        .build(), 1, 0);
    }

    @Test
    public void testpushBefore_with_InvalidTarget() throws Throwable {
        test("pushBefore_with_InvalidTarget", Transtype.PREPROCESS, Paths.get("pushBefore.ditamap"),
                ImmutableMap.<String, Object>builder()
//                        .put("validate", "false")
                        .build(), 1, 0);
    }

    @Test
    public void testpushBefore_without_conref() throws Throwable {
        test("pushBefore_without_conref", Transtype.PREPROCESS, Paths.get("pushBefore.ditamap"),
                ImmutableMap.<String, Object>builder()
//                        .put("validate", "false")
                        .build(), 0, 0);
    }

    @Test
    public void testsimple_pushBefore() throws Throwable {
        test("simple_pushBefore", Transtype.PREPROCESS, Paths.get("pushBefore.ditamap"),
                ImmutableMap.<String, Object>builder()
//                        .put("validate", "false")
                        .build(), 0, 0);
    }

    @Test
    public void testpushReplace_between_Specialization() throws Throwable {
        test("pushReplace_between_Specialization", Transtype.PREPROCESS, Paths.get("pushReplace.ditamap"),
                ImmutableMap.<String, Object>builder()
//                        .put("validate", "false")
                        .build(), 0, 0);
    }

    @Test
    public void testpushReplace_with_crossRef() throws Throwable {
        test("pushReplace_with_crossRef", Transtype.PREPROCESS, Paths.get("pushReplace.ditamap"),
                ImmutableMap.<String, Object>builder()
//                        .put("validate", "false")
                        .build(), 0, 0);
    }

    @Test
    public void testpushReplace_with_InvalidTarget() throws Throwable {
        test("pushReplace_with_InvalidTarget", Transtype.PREPROCESS, Paths.get("pushReplace.ditamap"),
                ImmutableMap.<String, Object>builder()
//                        .put("validate", "false")
                        .build(), 1, 4);
    }

    @Test
    public void testpushReplace_without_conref() throws Throwable {
        test("pushReplace_without_conref", Transtype.PREPROCESS, Paths.get("pushReplace.ditamap"),
                ImmutableMap.<String, Object>builder()
//                        .put("validate", "false")
                        .build(), 0, 0);
    }

    @Test
    public void testsimple_pushReplace() throws Throwable {
        test("simple_pushReplace", Transtype.PREPROCESS, Paths.get("pushReplace.ditamap"),
                ImmutableMap.<String, Object>builder()
//                        .put("validate", "false")
                        .build(), 0, 0);
    }

    @Test
    public void testconrefbreaksxref() throws Throwable {
        test("conrefbreaksxref");
    }

    @Test
    public void testcontrol_value_file() throws Throwable {
        test("control_value_file");
    }

    @Test
    public void testexportanchors() throws Throwable {
        test("exportanchors", Transtype.PREPROCESS,
                Paths.get("test.ditamap"),
                ImmutableMap.<String, Object>builder()
                        .put("transtype", "eclipsehelp")
                        .build());
    }

    @Test
    public void testimage_scale() throws Throwable {
        test("image-scale", Transtype.XHTML,
                Paths.get("test.dita"),
                emptyMap());
    }

    @Test
    public void testindex_see() throws Throwable {
        test("index-see");
    }

    @Test
    public void testkeyref() throws Throwable {
        test("keyref", Transtype.PREPROCESS,
                Paths.get("test.ditamap"), emptyMap());
    }

    @Test
    public void testkeyref_All_tags() throws Throwable {
        test("keyref_All_tags", Transtype.XHTML,
                Paths.get("mp_author1.ditamap"),
                emptyMap(),
                1, 0);
    }

    @Test
    public void testkeyref_Keyword_links() throws Throwable {
        test("keyref_Keyword_links", Transtype.XHTML,
                Paths.get("mp_author1.ditamap"),
                emptyMap());
    }

    @Test
    public void testkeyref_Redirect_conref() throws Throwable {
        test("keyref_Redirect_conref");
    }

    @Test
    public void testkeyref_Redirect_link_or_xref() throws Throwable {
        test("keyref_Redirect_link_or_xref");
    }

    @Test
    public void testkeyref_Splitting_combining_targets() throws Throwable {
        test("keyref_Splitting_combining_targets");
    }

    @Test
    public void testkeyref_Swap_out_variable_content() throws Throwable {
        test("keyref_Swap_out_variable_content", Transtype.XHTML,
                Paths.get("mp_author1.ditamap"),
                emptyMap());
    }

    @Test
    public void testkeyref_modify() throws Throwable {
        test("keyref_modify", Transtype.XHTML,
                Paths.get("mp_author1.ditamap"),
                emptyMap(),
                1, 0);
    }

    @Test
    public void testlang() throws Throwable {
        test("lang", Transtype.XHTML,
                Paths.get("lang.ditamap"),
                ImmutableMap.<String, Object>builder()
                        .put("validate", "false")
                        .build(),
                1, 0);
    }

    @Test
    public void testmapref() throws Throwable {
        test("mapref", Transtype.PREPROCESS,
                Paths.get("test.ditamap"),
                ImmutableMap.<String, Object>builder()
                        .put("generate-debug-attributes", "false")
                        .build());
    }

    @Test
    public void testsubjectschema_case() throws Throwable {
        test("subjectschema_case", Transtype.XHTML,
                Paths.get("simplemap.ditamap"),
                ImmutableMap.<String, Object>builder()
                        .put("args.filter", Paths.get("filter.ditaval"))
                        .put("clean.temp", "no")
                        .build());
    }

    @Test
    public void testuplevels1() throws Throwable {
        test("uplevels1", Transtype.XHTML,
                Paths.get("maps/above.ditamap"),
                ImmutableMap.<String, Object>builder()
                        .put("generate.copy.outer", "1")
                        .put("outer.control", "quiet")
                        .build());
    }

    @Test
    public void testuplevels3() throws Throwable {
        test("uplevels3", Transtype.XHTML,
                Paths.get("maps/above.ditamap"),
                ImmutableMap.<String, Object>builder()
                        .put("generate.copy.outer", "3")
                        .put("outer.control", "quiet")
                        .build());
    }

}