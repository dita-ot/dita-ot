package org.dita.dost.module.reader;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class FullPathTempFileSchemeTest {

  private static FullPathTempFileScheme tempFileNameScheme;

  @BeforeAll
  public static void setUp() {
    tempFileNameScheme = new FullPathTempFileScheme();
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({ "file:///full/unix/path,full/unix/path", "file:/c:/full/windows/path,full/windows/path" })
  public void testFullPathTempFileScheme(String src, String exp) {
    final URI act = tempFileNameScheme.generateTempFileName(URI.create(src));
    assertEquals(URI.create(exp), act);
  }
}
