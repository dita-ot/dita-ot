/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2016 Jarno Elovirta
 *
 *  See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost;

import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.dita.dost.AbstractIntegrationTest.Transtype.PREPROCESS;
import static org.dita.dost.AbstractIntegrationTest.Transtype.XHTML;

public class IntegrationTestXhtml extends AbstractIntegrationTest {

    public IntegrationTestXhtml builder() {
        return new IntegrationTestXhtml();
    }

    @Override
    Transtype getTranstype(Transtype transtype) {
        return transtype;
    }

    @Test
    public void testlink_parentchild() throws Throwable {
        builder().name("link_parentchild")
                .transtype(XHTML)
                .input(Paths.get("03.ditamap"))
                .test();
    }

    @Test
    public void testkeyref_dupkey() throws Throwable {
        builder().name("keyref_dupkey")
                .transtype(XHTML)
                .input(Paths.get("keyref-test-01.ditamap"))
                .test();
    }

    @Test
    public void testkeyref_to_keyref() throws Throwable {
        builder().name("keyref_to_keyref")
                .transtype(XHTML)
                .input(Paths.get("keyref-test-01.ditamap"))
                .test();
    }

    @Test
    public void testconref_with_xref() throws Throwable {
        builder().name("conref_with_xref")
                .transtype(XHTML)
                .input(Paths.get("test-conref-xref-keyref-bug.ditamap"))
                .test();
    }

    @Test
    public void testlink_xref_extensiontest() throws Throwable {
        builder().name("link_xref_extensiontest")
                .transtype(XHTML)
                .input(Paths.get("test.ditamap"))
                .test();
    }

    @Test
    public void testtitle_includes_markup() throws Throwable {
        builder().name("title_includes_markup")
                .transtype(XHTML)
                .input(Paths.get("test.ditamap"))
                .test();
    }

    @Test
    public void testimage_extension_mixedcase() throws Throwable {
        builder().name("image_extension_mixedcase")
                .transtype(XHTML)
                .input(Paths.get("testpng.ditamap"))
                .test();
    }

    @Test
    public void testcascade_processingrole() throws Throwable {
        builder().name("cascade_processingrole")
                .transtype(XHTML)
                .input(Paths.get("test.ditamap"))
                .test();
    }

    @Test
    public void testconref_pushreplace() throws Throwable {
        builder().name("conref_pushreplace")
                .transtype(XHTML)
                .input(Paths.get("test.ditamap"))
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
    public void testconref_topiconly() throws Throwable {
        builder().name("conref_topiconly")
                .transtype(XHTML)
                .input(Paths.get("conref_to_self.dita"))
                .put("validate", "false")
                .warnCount(1)
                .test();
    }

    @Test
    public void testcontrolValueFile8() throws Throwable {
        builder().name("map13_flag")
                .transtype(XHTML)
                .input(Paths.get("map13.ditamap"))
                .put("args.filter", Paths.get("flag.ditaval"))
                .test();
    }

    @Test
    public void testcontrolValueFile9() throws Throwable {
        builder().name("map13_flag2")
                .transtype(XHTML)
                .input(Paths.get("map13.ditamap"))
                .put("args.filter", Paths.get("flag2.ditaval"))
                .test();
    }

    @Test
    public void testcontrolValueFile10() throws Throwable {
        builder().name("map33_flag")
                .transtype(XHTML)
                .input(Paths.get("map33.ditamap"))
                .put("args.filter", Paths.get("flag.ditaval"))
                .test();
    }

    @Test
    public void testcontrolValueFile11() throws Throwable {
        builder().name("map33_flag2")
                .transtype(XHTML)
                .input(Paths.get("map33.ditamap"))
                .put("args.filter", Paths.get("flag2.ditaval"))
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
                .errorCount(0)
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
    public void testfilterlist() throws Throwable {
        final Path testDir = Paths.get("src", "test", "resources", "filterlist", "src");
        final String filters = asList(
                Paths.get("filter1.ditaval"),
                Paths.get("subdir", "filter2.ditaval"),
                Paths.get("missing.ditaval"))
                .stream()
                .map(path -> testDir.resolve(path).toAbsolutePath().toString())
                .collect(Collectors.joining(File.pathSeparator));
        builder().name("filterlist")
                .transtype(XHTML)
                .input(Paths.get("simplemap.ditamap"))
                .put("args.filter", filters)
                .put("clean.temp", "no")
                .errorCount(1)
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
    
    @Test
    public void testonlytopic_in_map() throws Throwable {
        builder().name("onlytopic.in.map")
                .transtype(XHTML)
                .input(Paths.get("input.ditamap"))
                .put("onlytopic.in.map", "true")
                .test();
    }
    
    @Test
    public void testonlytopic_in_map_false() throws Throwable {
        builder().name("onlytopic.in.map.false")
                .transtype(XHTML)
                .input(Paths.get("input.ditamap"))
                .put("onlytopic.in.map", "false")
                .test();
    }

    @Test
    public void testCrawlTopic() throws Throwable {
        builder().name("crawl_topic")
                .transtype(XHTML)
                .input(Paths.get("input.ditamap"))
                .put("link-crawl", "topic")
                .test();
    }

    @Test
    public void testCrawlMap() throws Throwable {
        builder().name("crawl_map")
                .transtype(XHTML)
                .input(Paths.get("input.ditamap"))
                .put("link-crawl", "map")
                .errorCount(2)
                .test();
    }
}