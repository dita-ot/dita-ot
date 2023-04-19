/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2020 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.writer.include;

import static java.net.URI.create;
import static org.dita.dost.TestUtils.assertXMLEqual;
import static org.dita.dost.util.Constants.ATTR_FORMAT_VALUE_DITA;
import static org.dita.dost.util.Constants.PR_D_CODEREF;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.dita.dost.TestUtils;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.store.StreamStore;
import org.dita.dost.util.Job;
import org.dita.dost.util.Job.FileInfo.Builder;
import org.dita.dost.util.XMLUtils;
import org.dita.dost.writer.CoderefResolver;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.xml.sax.InputSource;

public class IncludeResolverTest {

  private static final File resourceDir = TestUtils.getResourceDir(IncludeResolverTest.class);
  private static final File srcDir = new File(resourceDir, "src");
  private static final File expDir = new File(resourceDir, "exp");

  public static Stream<Arguments> parameters() {
    return Stream.of(
      Arguments.of("coderef.dita"),
      Arguments.of("include_text.dita"),
      Arguments.of("include_xml.dita"),
      Arguments.of("include_xml_schema.dita")
    );
  }

  @TempDir
  private File tempDir;

  private CoderefResolver filter;

  public void setup(String test) throws IOException {
    Files.copy(new File(srcDir, test).toPath(), new File(tempDir, test).toPath());
    Files.writeString(new File(tempDir, "topic.dita").toPath(), "dummy");
    for (final String file : new String[] { "code.xml", "utf-8.xml", "plain.txt", "range.txt", "schema.xml" }) {
      Files.copy(new File(srcDir, file).toPath(), new File(tempDir, file).toPath());
    }

    filter = new CoderefResolver();
    filter.setLogger(new TestUtils.TestLogger());
    final Job job = new Job(tempDir, new StreamStore(tempDir, new XMLUtils()));
    job.addAll(
      Stream
        .of("topic.dita", test)
        .map(p -> new Builder().uri(create(p)).src(new File(srcDir, p).toURI()).format(ATTR_FORMAT_VALUE_DITA).build())
        .collect(Collectors.toList())
    );
    job.addAll(
      Stream
        .of("code.xml", "utf-8.xml", "plain.txt", "range.txt", "schema.xml")
        .map(p -> new Builder().uri(create(p)).src(new File(srcDir, p).toURI()).format(PR_D_CODEREF.localName).build())
        .collect(Collectors.toList())
    );
    filter.setJob(job);
  }

  @ParameterizedTest
  @MethodSource("parameters")
  public void testWrite(String test) throws DITAOTException, IOException {
    setup(test);
    final File f = new File(tempDir, test);

    filter.write(f.getAbsoluteFile());

    //        assertEquals(Files.readLines(new File(expDir, test), StandardCharsets.UTF_8).stream().collect(Collectors.joining("\n")),
    //                Files.readLines(f, StandardCharsets.UTF_8).stream().collect(Collectors.joining("\n")));
    assertXMLEqual(new InputSource(new File(expDir, test).toURI().toString()), new InputSource(f.toURI().toString()));
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
  //    @Disabled
  //    @Test
  //    public void include_dita() throws DITAOTException {
  //        final File f = new File(tempDir, "include_dita.dita");
  //
  //        filter.write(f.getAbsoluteFile());
  //
  //        assertXMLEqual(new InputSource(new File(expDir, "include_dita.dita").toURI().toString()),
  //                new InputSource(f.toURI().toString()));
  //    }
  //
  //  @AfterEach
  //  public void teardown() throws IOException {
  //    TestUtils.forceDelete(tempDir);
  //  }
}
