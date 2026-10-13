package org.dita.dost.module;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.store.CacheStore;
import org.dita.dost.util.Job;
import org.dita.dost.util.Job.FileInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.xml.sax.SAXException;

class MapModuleTest extends AbstractModuleTest {

  @Override
  protected AbstractPipelineInput getAbstractPipelineInput() {
    throw new UnsupportedOperationException();
  }

  @Override
  protected AbstractPipelineModule getModule() {
    return new MapModule();
  }

  @ParameterizedTest
  @ValueSource(ints = { 1, 2 })
  public void process(int iteration) throws IOException {
    this.testCase = "iteration-" + iteration;
    tempDir = new File(tempBaseDir, testCase);
    job = new Job(tempDir, new CacheStore(tempDir, xmlUtils));
    initStore();

    var input = new PipelineHashIO(
      Map.of(
        "style",
        Path.of("src", "main", "plugins", "org.dita.base", "xsl", "preprocess", "mapref.xsl").toString(),
        "replace-keyref",
        "true",
        "max-iteration-count",
        Integer.toString(iteration)
      )
    );

    test(input);
  }

  private void initStore() throws IOException {
    var store = job.getStore();
    var srcDir = Paths.get(resourceDir.getAbsolutePath(), "src");
    Files
      .walk(srcDir)
      .filter(Files::isRegularFile)
      .forEach(src -> {
        try {
          var dst = tempDir.toPath().resolve(srcDir.relativize(src)).toUri();
          var doc = builder.parse(src.toFile());
          doc.insertBefore(
            doc.createProcessingInstruction("workdir-uri", dst.resolve(".").toString()),
            doc.getDocumentElement()
          );
          store.writeDocument(doc, dst);
          job.add(
            FileInfo
              .builder()
              .src(src.toUri())
              .uri(URI.create(src.getFileName().toString()))
              .format("ditamap")
              .isInput(src.getFileName().toString().equals("mydoc.ditamap"))
              .hasKeyref(true)
              .build()
          );
        } catch (IOException | SAXException e) {
          throw new RuntimeException(e);
        }
      });
  }
}
