package org.dita.dost.module;

import static org.dita.dost.TestUtils.CachingLogger.Message.Level.WARN;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.dita.dost.TestUtils;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.util.Configuration;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

public class CopyToModuleTest extends AbstractModuleTest {

  @Override
  protected AbstractPipelineInput getAbstractPipelineInput() {
    var input = new PipelineHashIO();
    //    input.setAttribute(ANT_INVOKER_EXT_PARAM_TRANSTYPE, "html5");
    return input;
  }

  @Override
  protected AbstractPipelineModule getModule() {
    return new CopyToModule();
  }

  @Test
  public void testCopyTo() throws IOException, SAXException {
    testCase = "basic";
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
