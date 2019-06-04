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

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.dita.dost.AbstractIntegrationTest.Transtype.*;

public class IntegrationTest extends AbstractIntegrationTest {

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
    public void testtitle_includes_markup() throws Throwable {
        builder().name("title_includes_markup")
                .transtype(XHTML)
                .input(Paths.get("test.ditamap"))
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
    public void testchunk_uplevel() throws Throwable {
        builder().name("chunk_uplevel")
                .transtype(PREPROCESS)
                .input(Paths.get("main/chunkup.ditamap"))
                .put("outer.control", "quiet")
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
                .warnCount(1)
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
    public void testMetadataInheritance() throws Throwable {
        test("MetadataInheritance");
    }

    @Test
    public void testmapref_reltables() throws Throwable {
        builder().name("mapref_reltables")
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
                .warnCount(2)
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
                .warnCount(2)
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
    public void testfilterflag_peer() throws Throwable {
        final Path testDir = Paths.get("src", "test", "resources", "filterflag_peer", "src");
        final String filters = asList(
                Paths.get("revisions", "revs.ditaval"),
                Paths.get("flags", "flags.ditaval"))
                .stream()
                .map(path -> testDir.resolve(path).toAbsolutePath().toString())
                .collect(Collectors.joining(File.pathSeparator));
        builder().name("filterflag_peer")
                .transtype(XHTML)
                .input(Paths.get("content", "flagpeer.ditamap"))
                .put("args.filter", filters)
                .put("clean.temp", "no")
                .test();
    }
    
    @Test
    public void testfilterflag_updir() throws Throwable {
        final Path testDir = Paths.get("src", "test", "resources", "filterflag_updir", "src");
        final String filters = asList(
                Paths.get("content", "revs.ditaval"),
                Paths.get("flags", "flags.ditaval"))
                .stream()
                .map(path -> testDir.resolve(path).toAbsolutePath().toString())
                .collect(Collectors.joining(File.pathSeparator));
        builder().name("filterflag_updir")
                .transtype(XHTML)
                .input(Paths.get("content", "flagupdir.ditamap"))
                .put("args.filter", filters)
                .put("clean.temp", "no")
                .warnCount(2)
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
    public void testmappull_topicid() throws Throwable {
        builder().name("mappull-topicid")
                .transtype(PREPROCESS)
                .input(Paths.get("reftopicid.ditamap"))
                .put("validate", "false")
                .warnCount(1)
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
    public void testCrawlTopicPreprocess() throws Throwable {
        builder().name("crawl_topic")
                .transtype(PREPROCESS)
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
                .warnCount(2)
                .test();
    }

    @Test
    public void testCrawlMapPreprocess() throws Throwable {
        builder().name("crawl_map")
                .transtype(PREPROCESS)
                .input(Paths.get("input.ditamap"))
                .put("link-crawl", "map")
                .errorCount(2)
                .warnCount(2)
                .test();
    }

    @Test
    public void testRng() throws Throwable {
        builder().name("rng")
                .transtype(PREPROCESS)
                .input(Paths.get("root.ditamap"))
                .test();
    }

}