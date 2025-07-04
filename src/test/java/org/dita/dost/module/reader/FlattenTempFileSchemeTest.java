package org.dita.dost.module.reader;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class FlattenTempFileSchemeTest {

  private static FlattenTempFileScheme tempFileNameScheme;

  @BeforeAll
  public static void setUp() {
    tempFileNameScheme = new FlattenTempFileScheme();
  }

  @ParameterizedTest(name = "{0}")
  @CsvSource({ "file:///full/unix/path,full__unix__path", "file:/c:/full/windows/path,c____full__windows__path" })
  public void testFlattenTempFileScheme(String src, String exp) {
    final URI act = tempFileNameScheme.generateTempFileName(URI.create(src));
    assertEquals(URI.create(exp), act);
  }
}
