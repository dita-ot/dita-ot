/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2020 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.writer.include;

import com.google.common.io.Files;
import org.dita.dost.TestUtils;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.store.StreamStore;
import org.dita.dost.util.Job;
import org.dita.dost.util.Job.FileInfo.Builder;
import org.dita.dost.util.XMLUtils;
import org.dita.dost.writer.CoderefResolver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.net.URI.create;
import static org.apache.commons.io.FileUtils.copyFile;
import static org.dita.dost.TestUtils.assertXMLEqual;
import static org.dita.dost.util.Constants.ATTR_FORMAT_VALUE_DITA;
import static org.dita.dost.util.Constants.PR_D_CODEREF;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class IncludeResolverTest {
    private static final File resourceDir = TestUtils.getResourceDir(IncludeResolverTest.class);
    private static final File srcDir = new File(resourceDir, "src");
    private static final File expDir = new File(resourceDir, "exp");

    @Parameters(name = "{0}")
    public static Collection<Object[]> parameters() {
        return Arrays.asList(new Object[][]{
                {"coderef.dita"},
                {"include_text.dita"},
                {"include_xml.dita"},
                {"include_xml_schema.dita"}
        });
    }

    private File tempDir;
    private CoderefResolver filter;
    private final String test;

    public IncludeResolverTest(final String test) {
        this.test = test;
    }

    @Before
    public void setup() throws IOException {
        tempDir = TestUtils.createTempDir(IncludeResolverTest.class);

        copyFile(new File(srcDir, test), new File(tempDir, test));
        Files.write("dummy", new File(tempDir, "topic.dita"), Charset.forName("UTF-8"));
        for (final String file : new String[]{"code.xml", "utf-8.xml", "plain.txt", "range.txt", "schema.xml"}) {
            copyFile(new File(srcDir, file), new File(tempDir, file));
        }

        filter = new CoderefResolver();
        filter.setLogger(new TestUtils.TestLogger());
        final Job job = new Job(tempDir, new StreamStore(tempDir, new XMLUtils()));
        job.addAll(Stream.of("topic.dita", test)
                .map(p -> new Builder()
                        .uri(create(p))
                        .src(new File(srcDir, p).toURI())
                        .format(ATTR_FORMAT_VALUE_DITA)
                        .build())
                .collect(Collectors.toList()));
        job.addAll(Stream.of("code.xml", "utf-8.xml", "plain.txt", "range.txt", "schema.xml")
                .map(p -> new Builder()
                        .uri(create(p))
                        .src(new File(srcDir, p).toURI())
                        .format(PR_D_CODEREF.localName)
                        .build())
                .collect(Collectors.toList()));
        filter.setJob(job);
    }

    @Test
    public void testWrite() throws DITAOTException, IOException {
        final File f = new File(tempDir, test);

        filter.write(f.getAbsoluteFile());

//        assertEquals(Files.readLines(new File(expDir, test), StandardCharsets.UTF_8).stream().collect(Collectors.joining("\n")),
//                Files.readLines(f, StandardCharsets.UTF_8).stream().collect(Collectors.joining("\n")));
        assertXMLEqual(new InputSource(new File(expDir, test).toURI().toString()),
                new InputSource(f.toURI().toString()));
    }
//
//    @Test
//    public void include_text() throws DITAOTException {
//        final File f = new File(tempDir, "include_text.dita");
//
//        filter.write(f.getAbsoluteFile());
//
//        assertXMLEqual(new InputSource(new File(expDir, "include_text.dita").toURI().toString()),
//                new InputSource(f.toURI().toString()));
//    }
//
//    @Test
//    public void include_xml() throws DITAOTException {
//        final File f = new File(tempDir, "include_xml.dita");
//
//        filter.write(f.getAbsoluteFile());
//
//        assertXMLEqual(new InputSource(new File(expDir, "include_xml.dita").toURI().toString()),
//                new InputSource(f.toURI().toString()));
//    }
//
//    @Ignore
//    @Test
//    public void include_dita() throws DITAOTException {
//        final File f = new File(tempDir, "include_dita.dita");
//
//        filter.write(f.getAbsoluteFile());
//
//        assertXMLEqual(new InputSource(new File(expDir, "include_dita.dita").toURI().toString()),
//                new InputSource(f.toURI().toString()));
//    }

    @After
    public void teardown() throws IOException {
        TestUtils.forceDelete(tempDir);
    }

}
