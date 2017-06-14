/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.writer;

import com.google.common.io.Files;
import org.dita.dost.TestUtils;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.util.Job;
import org.dita.dost.util.Job.FileInfo.Builder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.net.URI.create;
import static org.apache.commons.io.FileUtils.copyFile;
import static org.dita.dost.TestUtils.assertXMLEqual;
import static org.dita.dost.util.Constants.ATTR_FORMAT_VALUE_DITA;
import static org.dita.dost.util.Constants.PR_D_CODEREF;

public class CoderefResolverTest {

    private static final File resourceDir = TestUtils.getResourceDir(CoderefResolverTest.class);
    private static final File srcDir = new File(resourceDir, "src");
    private static final File expDir = new File(resourceDir, "exp");
    private static File tempDir;

    @BeforeClass
    public static void setup() throws IOException {
        tempDir = TestUtils.createTempDir(CoderefResolverTest.class);
    }

    @Test
    public void testWrite() throws DITAOTException, SAXException, IOException {
        final File f = new File(tempDir, "test.dita");
        copyFile(new File(srcDir, "test.dita"), f);
        Files.write("dummy", new File(tempDir, "topic.dita"), Charset.forName("UTF-8"));
        copyFile(new File(srcDir, "code.xml"), new File(tempDir, "code.xml"));
        copyFile(new File(srcDir, "utf-8.xml"), new File(tempDir, "utf-8.xml"));
        copyFile(new File(srcDir, "plain.txt"), new File(tempDir, "plain.txt"));
        copyFile(new File(srcDir, "range.txt"), new File(tempDir, "range.txt"));

        final CoderefResolver filter = new CoderefResolver();
        filter.setLogger(new TestUtils.TestLogger());
        final Job job = new Job(tempDir);
        job.addAll(Stream.of("test.dita", "topic.dita")
                .map(p -> new Builder()
                        .uri(create(p))
                        .src(new File(srcDir, p).toURI())
                        .format(ATTR_FORMAT_VALUE_DITA)
                        .build())
                .collect(Collectors.toList()));
        job.addAll(Stream.of("code.xml", "utf-8.xml", "plain.txt", "range.txt")
                .map(p -> new Builder()
                        .uri(create(p))
                        .src(new File(srcDir, p).toURI())
                        .format(PR_D_CODEREF.localName)
                        .build())
                .collect(Collectors.toList()));
        filter.setJob(job);
        filter.write(f.getAbsoluteFile());

        assertXMLEqual(new InputSource(new File(expDir, "test.dita").toURI().toString()),
                new InputSource(f.toURI().toString()));
    }

    @AfterClass
    public static void teardown() throws IOException {
        TestUtils.forceDelete(tempDir);
    }

}
