package org.dita.dost.writer;

import static org.dita.dost.TestUtils.assertAttributesEquals;
import static org.dita.dost.TestUtils.assertXMLEqual;
import static org.dita.dost.util.Constants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.dita.dost.TestUtils;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.module.reader.DefaultTempFileScheme;
import org.dita.dost.module.reader.TempFileNameScheme;
import org.dita.dost.store.Store;
import org.dita.dost.store.StreamStore;
import org.dita.dost.util.DitaClass;
import org.dita.dost.util.Job;
import org.dita.dost.util.XMLUtils;
import org.dita.dost.util.XMLUtils.AttributesBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

class DitaWriterFilterTest {

  private static final File resourceDir = TestUtils.getResourceDir(DitaWriterFilterTest.class);
  private static final File srcDir = new File(resourceDir, "src");
  private static final File expDir = new File(resourceDir, "exp");

  @TempDir
  private File tempDir;

  private XMLUtils xmlUtils;
  private Store store;
  private Job job;
  private TempFileNameScheme tempFileNameScheme;

  @BeforeEach
  void setUp() throws IOException {
    xmlUtils = new XMLUtils();
    store = new StreamStore(tempDir, xmlUtils);
    job = new Job(tempDir, store);
    tempFileNameScheme = new DefaultTempFileScheme();
    tempFileNameScheme.setBaseDir(tempDir.toURI());
  }

  @ParameterizedTest
  @CsvSource({ "topic.dita, dita", "map.ditamap, ditamap" })
  void testFilter(String file, String format) throws IOException, DITAOTException {
    var filter = new DitaWriterFilter();
    filter.setTempFileNameScheme(tempFileNameScheme);
    filter.setLogger(new TestUtils.TestLogger());
    filter.setJob(job);

    job.setInputDir(srcDir.toURI());

    var input = Job.FileInfo
      .builder()
      .src(srcDir.toURI().resolve("root.ditamap"))
      .uri(URI.create("root.ditamap"))
      .format("ditamap")
      .isInput(true)
      .build();
    job.add(input);

    var fi = Job.FileInfo.builder().src(srcDir.toURI().resolve(file)).uri(URI.create(file)).format(format).build();
    job.add(fi);
    try (var out = store.getOutputStream(fi.uri())) {
      Files.copy(Path.of(fi.src()), out);
    }
    filter.setCurrentFile(job.tempDirURI.resolve(fi.uri()));
    filter.setOutputFile(Path.of(job.tempDirURI.resolve(fi.uri())).toFile());

    store.transform(job.tempDirURI.resolve(fi.uri()), List.of(filter));

    assertXMLEqual(
      new InputSource(expDir.toURI().resolve(fi.uri()).toString()),
      new InputSource(store.getInputStream(fi.uri()))
    );
  }

  @Nested
  class ProcessAttributesTest {

    private DitaWriterFilter filter;

    @BeforeEach
    void setUp() {
      filter = new DitaWriterFilter();
      filter.setTempFileNameScheme(tempFileNameScheme);
      filter.setLogger(new TestUtils.TestLogger());
      filter.setJob(job);
      filter.setCurrentFile(job.tempDirURI.resolve("map.ditamap"));
      filter.setContentHandler(new DefaultHandler());

      var fi = Job.FileInfo
        .builder()
        .src(srcDir.toURI().resolve("map.ditamap"))
        .uri(URI.create("map.ditamap"))
        .format("ditamap")
        .build();
      job.add(fi);
    }

    @Test
    void processAttributes_absolute() throws SAXException {
      var src = new AttributesBuilder()
        .add("class", TOPIC_XREF.toString())
        .add("href", job.tempDirURI.resolve("topic.dita").toString())
        .build();
      var exp = new AttributesBuilder().add("class", TOPIC_XREF.toString()).add("href", "topic.dita").build();
      assertProcessAttributes(src, exp);
    }

    @Test
    void processAttributes_external() throws SAXException {
      var src = new AttributesBuilder()
        .add("class", TOPIC_XREF.toString())
        .add("href", "topic.dita")
        .add("scope", "external")
        .build();
      var exp = new AttributesBuilder()
        .add("class", TOPIC_XREF.toString())
        .add("href", "topic.dita")
        .add("scope", "external")
        .build();
      assertProcessAttributes(src, exp);
    }

    @Test
    void processAttributes_foundInJob() throws SAXException {
      job.add(
        Job.FileInfo
          .builder()
          .src(srcDir.toURI().resolve("topic.dita"))
          .uri(URI.create("topic.dita"))
          .format("dita")
          .build()
      );

      var src = new AttributesBuilder().add("class", TOPIC_XREF.toString()).add("href", "topic.dita").build();
      var exp = new AttributesBuilder().add("class", TOPIC_XREF.toString()).add("href", "topic.dita").build();
      assertProcessAttributes(src, exp);
    }

    @Test
    void processAttributes_notFoundInJob() throws SAXException {
      var src = new AttributesBuilder().add("class", TOPIC_XREF.toString()).add("href", "topic.dita").build();
      var exp = new AttributesBuilder().add("class", TOPIC_XREF.toString()).add("href", "topic.dita").build();
      assertProcessAttributes(src, exp);
    }

    @Test
    void processAttributes_noTempFileNameScheme() throws SAXException {
      filter.setTempFileNameScheme(null);

      var src = new AttributesBuilder().add("class", TOPIC_XREF.toString()).add("href", "topic.dita").build();
      var exp = new AttributesBuilder().add("class", TOPIC_XREF.toString()).add("href", "topic.dita").build();
      assertProcessAttributes(src, exp);
    }

    static Stream<Arguments> processAttributes_formatArgs() {
      return Stream.of(Arguments.of(TOPIC_XREF, "dita"), Arguments.of(MAPGROUP_D_MAPREF, "ditamap"));
    }

    @ParameterizedTest
    @MethodSource("processAttributes_formatArgs")
    void processAttributes_format(DitaClass cls, String expFormat) throws SAXException {
      var src = new AttributesBuilder()
        .add("class", cls.toString())
        .add("format", "custom")
        .add("scope", "local")
        .build();
      var exp = new AttributesBuilder()
        .add("class", cls.toString())
        .add("format", expFormat)
        .add(DITA_OT_NS, "orig-format", "custom")
        .add("scope", "local")
        .build();
      assertProcessAttributes(src, exp);
    }

    private void assertProcessAttributes(Attributes src, Attributes exp) throws SAXException {
      filter.startElement(null, null, null, src);

      var act = filter.processAttributes(null, src);

      assertAttributesEquals(exp, act);
    }
  }
}
