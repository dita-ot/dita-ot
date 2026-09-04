package org.dita.dost.module;

import static org.dita.dost.TestUtils.CachingLogger.Message.Level.WARN;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.dita.dost.TestUtils;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.util.Configuration;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.xml.sax.SAXException;

public class CopyToModuleTest extends AbstractModuleTest {

  @Override
  protected AbstractPipelineInput getAbstractPipelineInput() {
    return new PipelineHashIO();
  }

  @Override
  protected AbstractPipelineModule getModule() {
    return new CopyToModule();
  }

  @ParameterizedTest
  @ValueSource(strings = { "basic", "basic_dita2" })
  public void testCopyTo(String testCase) throws IOException, SAXException {
    this.testCase = testCase;
    tempDir = new File(tempBaseDir, testCase);
    //    params = Map.of("force-unique", "true");
    //    this.warningCount = warningCount;
    logger = new TestUtils.CachingLogger(mode.equals(Configuration.Mode.STRICT));
    //    job = new Job(tempDir, new StreamStore(tempDir, xmlUtils));

    //    initStore(job.getStore());
    super.test();
    final List<TestUtils.CachingLogger.Message> warnings = logger
      .getMessages()
      .stream()
      .filter(m -> m.level() == WARN)
      .toList();
    warnings.forEach(m -> System.err.println(m.level() + ": " + m.message()));
    //    assertEquals(this.warningCount, warnings.size());
  }
}
