/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2023 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.writer;

import static org.dita.dost.util.Constants.ATTR_FORMAT_VALUE_DITAMAP;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Objects;
import org.dita.dost.store.CacheStore;
import org.dita.dost.util.Job;
import org.dita.dost.util.XMLUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

class TopicCleanFilterTest {

  private TopicCleanFilter filter;
  private Job job;

  TopicCleanFilterTest() throws IOException {
    filter = new TopicCleanFilter();
    final File temp = new File("").getAbsoluteFile();
    job = new Job(temp, new CacheStore(temp, new XMLUtils()));
  }

  @AfterEach
  void cleanUp() {
    job.getFileInfo().forEach(job::remove);
  }

  @ParameterizedTest
  @CsvSource(
    {
      "path2project,topic.dita,root.ditamap,",
      "path2project,dir/topic.dita,root.ditamap,../",
      "path2project,dir/sub/topic.dita,root.ditamap,../../",
      "path2project-uri,topic.dita,root.ditamap,./",
      "path2project-uri,dir/topic.dita,root.ditamap,../",
      "path2project-uri,dir/sub/topic.dita,root.ditamap,../../",
      "path2rootmap-uri,topic.dita,root.ditamap,",
      "path2rootmap-uri,dir/topic.dita,root.ditamap,../",
      "path2rootmap-uri,dir/sub/topic.dita,root.ditamap,../../",
      "path2rootmap-uri,topic.dita,maps/root.ditamap,maps/",
      "path2rootmap-uri,dir/topic.dita,maps/root.ditamap,../maps/",
      "path2rootmap-uri,dir/sub/topic.dita,maps/root.ditamap,../../maps/",
    }
  )
  void processingInstruction(String name, String src, String input, String exp) throws SAXException {
    job.add(
      Job.FileInfo
        .builder()
        .src(URI.create("file:///Volume/src/").resolve(input))
        .format(ATTR_FORMAT_VALUE_DITAMAP)
        .isInput(true)
        .uri(URI.create(input))
        .result(URI.create(input))
        .build()
    );
    filter.setJob(job);
    filter.setFileInfo(
      Job.FileInfo
        .builder()
        .src(URI.create("file:///Volume/src/").resolve(src))
        .uri(URI.create(src))
        .result(URI.create(src))
        .build()
    );
    filter.setContentHandler(
      new DefaultHandler() {
        @Override
        public void processingInstruction(String target, String data) {
          assertEquals(name, target);
          assertEquals(Objects.requireNonNullElse(exp, ""), data);
        }
      }
    );

    filter.startDocument();
    filter.processingInstruction(name, "target");
  }

  @Test
  void processingInstruction_other() throws SAXException {
    filter.setJob(job);
    filter.setFileInfo(
      Job.FileInfo
        .builder()
        .src(URI.create("file:///Volume/src/topic.dita"))
        .uri(URI.create("topic.dita"))
        .result(URI.create("topic.dita"))
        .build()
    );
    filter.setContentHandler(
      new DefaultHandler() {
        @Override
        public void processingInstruction(String target, String data) {
          assertEquals("target", target);
          assertEquals("data", data);
        }
      }
    );

    filter.startDocument();
    filter.processingInstruction("target", "data");
  }
}
