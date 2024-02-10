/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2024 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.module;

import static org.dita.dost.util.Constants.ANT_INVOKER_EXT_PARAM_STYLE;
import static org.dita.dost.util.Constants.ANT_INVOKER_EXT_PARAM_TRANSTYPE;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.store.CacheStore;
import org.dita.dost.store.Store;
import org.dita.dost.util.Configuration;
import org.dita.dost.util.Job;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.xml.sax.SAXException;

class MaprefModuleTest extends AbstractModuleTest {

  @Override
  protected AbstractPipelineInput getAbstractPipelineInput() {
    final AbstractPipelineInput input = new PipelineHashIO();
    input.setAttribute(ANT_INVOKER_EXT_PARAM_TRANSTYPE, "html5");
    input.setAttribute(
      ANT_INVOKER_EXT_PARAM_STYLE,
      new File("src/main/plugins/org.dita.base/xsl/preprocess/mapref.xsl").getAbsolutePath()
    );
    return input;
  }

  @Override
  protected AbstractPipelineModule getModule() {
    return new MaprefModule();
  }

  @ParameterizedTest
  @ValueSource(strings = { "basic", "mapref_reltables", "mapref_to_conref", "mapref_topicrefID" })
  public void process(String testCase) throws IOException {
    this.testCase = testCase;
    tempDir = new File(tempBaseDir, testCase);
    job = new Job(tempDir, createStore());
    mode = Configuration.Mode.LAX;
    chunkModule.setProcessingMode(mode);
    test();
  }

  private Store createStore() throws IOException {
    final CacheStore store = new CacheStore(tempDir, xmlUtils);
    var srcDir = Paths.get(resourceDir.getAbsolutePath(), "src", testCase);
    Files
      .walk(srcDir)
      .filter(Files::isRegularFile)
      .filter(file -> !file.getFileName().toString().equals(".job.xml"))
      .forEach(src -> {
        try {
          var dst = tempDir.toPath().resolve(srcDir.relativize(src)).toUri();
          var doc = builder.parse(src.toFile());
          doc.insertBefore(
            doc.createProcessingInstruction("workdir-uri", dst.resolve(".").toString()),
            doc.getDocumentElement()
          );
          store.writeDocument(doc, dst);
        } catch (IOException | SAXException e) {
          throw new RuntimeException(e);
        }
      });
    return store;
  }
}
