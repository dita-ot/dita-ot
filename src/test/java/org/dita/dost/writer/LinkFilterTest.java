/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2017 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.writer;

import static org.dita.dost.util.Constants.ATTRIBUTE_NAME_COPY_TO;
import static org.dita.dost.util.Constants.ATTRIBUTE_NAME_HREF;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import javax.xml.XMLConstants;
import org.dita.dost.store.StreamStore;
import org.dita.dost.util.Job;
import org.dita.dost.util.XMLUtils;
import org.dita.dost.util.XMLUtils.AttributesBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class LinkFilterTest {

  private URI tempDir;
  private URI srcDir;

  final LinkFilter linkFilter = new LinkFilter();

  @BeforeEach
  public void setUp() throws Exception {
    tempDir = new File(System.getProperty("java.io.tmpdir")).toURI().resolve("./");
    srcDir = new File("").toURI().resolve("./");

    final Job job = getJob();
    job.setInputDir(srcDir.resolve("maps"));
    linkFilter.setJob(job);
    linkFilter.setDestFile(tempDir.resolve("maps/test.ditamap"));
    linkFilter.setCurrentFile(tempDir.resolve("xyz.ditamap"));
  }

  @Test
  public void getHrefFragment() throws Exception {
    assertEquals(URI.create("#foo"), linkFilter.getHref(URI.create("#foo")));
  }

  @Test
  public void getHref() {
    assertEquals(URI.create("../topics/topic.dita"), linkFilter.getHref(URI.create("abc.dita")));
  }

  @Test
  void startElement_href() throws SAXException {
    linkFilter.setContentHandler(
      new DefaultHandler() {
        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) {
          assertEquals(URI.create("../topics/topic.dita"), URI.create(atts.getValue(ATTRIBUTE_NAME_HREF)));
        }
      }
    );
    linkFilter.startElement(
      XMLConstants.NULL_NS_URI,
      "topicref",
      "topicref",
      new AttributesBuilder().add(ATTRIBUTE_NAME_HREF, "abc.dita").build()
    );
  }

  @Test
  void startElement_copyTo() throws SAXException {
    linkFilter.setContentHandler(
      new DefaultHandler() {
        @Override
        public void startElement(String uri, String localName, String qName, Attributes atts) {
          assertEquals(URI.create("../topics/topic.dita"), URI.create(atts.getValue(ATTRIBUTE_NAME_HREF)));
          assertEquals(URI.create("../topics/topic_copy.dita"), URI.create(atts.getValue(ATTRIBUTE_NAME_COPY_TO)));
        }
      }
    );
    linkFilter.startElement(
      XMLConstants.NULL_NS_URI,
      "topicref",
      "topicref",
      new AttributesBuilder()
        .add(ATTRIBUTE_NAME_HREF, "abc.dita")
        .add(ATTRIBUTE_NAME_COPY_TO, "9905ee35-8276-4a95-bf97-33d5a05b1f60.dita")
        .build()
    );
  }

  private Job getJob() {
    try {
      final Job job = new Job(new File(tempDir), new StreamStore(new File(tempDir), new XMLUtils()));
      job.add(
        new Job.FileInfo.Builder()
          .uri(URI.create("abc.dita"))
          .src(srcDir.resolve("topics/topic.dita"))
          .result(srcDir.resolve("topics/topic.dita"))
          .build()
      );
      job.add(
        new Job.FileInfo.Builder()
          .uri(URI.create("9905ee35-8276-4a95-bf97-33d5a05b1f60.dita"))
          .src(srcDir.resolve("topics/topic.dita"))
          .result(srcDir.resolve("topics/topic_copy.dita"))
          .build()
      );
      job.add(
        new Job.FileInfo.Builder()
          .uri(URI.create("xyz.ditamap"))
          .src(srcDir.resolve("maps/test.ditamap"))
          .result(srcDir.resolve("maps/test.ditamap"))
          .build()
      );
      return job;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
