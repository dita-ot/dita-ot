package org.dita.dost;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import org.apache.tools.ant.BuildException;
import org.dita.dost.exception.DITAOTException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class ProcessorTest {

  private Processor p;

  @TempDir
  private File tempDir;

  @TempDir
  private File out;

  @BeforeEach
  public void setUp() throws Exception {
    String ditaDir = System.getProperty("dita.dir");
    if (ditaDir == null) {
      ditaDir = new File("src" + File.separator + "main").getAbsolutePath();
    }
    final ProcessorFactory pf = ProcessorFactory.newInstance(
      null
      //            new File(ditaDir)
    );
    pf.setBaseTempDir(tempDir);
    p = pf.newProcessor("html5");
  }

  @Test
  public void testRunWithoutArgs() throws Exception {
    assertThrows(IllegalStateException.class, () -> p.run());
  }

  @Test
  public void testRun() throws DITAOTException, IOException, URISyntaxException {
    final File mapFile = new File(getClass().getClassLoader().getResource("ProcessorTest/test.ditamap").toURI());
    p.setInput(mapFile).setOutputDir(out).run();

    assertEquals(1, tempDir.listFiles(f -> f.isFile() && f.getName().endsWith(".log")).length);
  }

  @Test
  public void testBroken() throws DITAOTException, IOException, URISyntaxException {
    assertThrows(
      DITAOTException.class,
      () -> {
        final File mapFile = new File(getClass().getClassLoader().getResource("ProcessorTest/broken.dita").toURI());
        try {
          p.setInput(mapFile).setOutputDir(out).createDebugLog(false).run();
        } catch (Exception e) {
          assertEquals(0, tempDir.listFiles(File::isDirectory).length);
          assertEquals(0, tempDir.listFiles(f -> f.isFile() && f.getName().endsWith(".log")).length);
          throw e;
        }
      }
    );
  }

  @Test
  public void testCleanTempOnFailure() throws DITAOTException, IOException, URISyntaxException {
    assertThrows(
      DITAOTException.class,
      () -> {
        final File mapFile = new File(getClass().getClassLoader().getResource("ProcessorTest/broken.dita").toURI());
        try {
          p.setInput(mapFile).setOutputDir(out).cleanOnFailure(false).run();
        } catch (BuildException e) {
          assertEquals(1, tempDir.listFiles(File::isDirectory).length);
          assertEquals(1, tempDir.listFiles(f -> f.isFile() && f.getName().endsWith(".log")).length);
          throw e;
        }
      }
    );
  }
}
