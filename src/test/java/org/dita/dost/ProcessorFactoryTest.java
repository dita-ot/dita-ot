package org.dita.dost;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import org.junit.jupiter.api.Test;

public class ProcessorFactoryTest {

  @Test
  public void testNewInstance() {
    String ditaDir = System.getProperty("dita.dir");
    if (ditaDir == null) {
      ditaDir = new File("src" + File.separator + "main").getAbsolutePath();
    }
    assertNotNull(ProcessorFactory.newInstance(new File(ditaDir)));
  }

  @Test
  public void testNewProcessor() {
    String ditaDir = System.getProperty("dita.dir");
    if (ditaDir == null) {
      ditaDir = new File("src" + File.separator + "main").getAbsolutePath();
    }
    final ProcessorFactory pf = ProcessorFactory.newInstance(new File(ditaDir));
    assertNotNull(pf.newProcessor("html5"));
  }

  @Test
  public void testUnsupportedTranstype() {
    assertThrows(
      IllegalArgumentException.class,
      () -> {
        String ditaDir = System.getProperty("dita.dir");
        if (ditaDir == null) {
          ditaDir = new File("src" + File.separator + "main").getAbsolutePath();
        }
        final ProcessorFactory pf = ProcessorFactory.newInstance(new File(ditaDir));
        pf.newProcessor("xxx");
      }
    );
  }
}
