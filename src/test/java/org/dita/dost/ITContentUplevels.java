package org.dita.dost;

import static org.dita.dost.AbstractIntegrationTest.Transtype.XHTML;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public interface ITContentUplevels {
  AbstractIntegrationTest builder();

  @Test
  default void testuplevels1() throws Throwable {
    List<String> outerFiles = List.of("images/carwash.gif", "a.html", "b.html", "topics/c.html", "topics/d.html");
    Path outDir = Paths.get("build", "tmp", "integrationTest", "uplevels1", "out");
    outerFiles
      .stream()
      .map(outDir::resolve)
      .forEach(remnantFromThePreviousRun -> {
        try {
          Files.deleteIfExists(remnantFromThePreviousRun);
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
      });

    File actDir = builder()
      .name("uplevels1")
      .transtype(XHTML)
      .input(Paths.get("maps/above.ditamap"))
      .put("generate.copy.outer", "1")
      .put("outer.control", "quiet")
      .test();

    assertAll(
      "Files should not exist outside the output folder",
      outerFiles
        .stream()
        .map(outerFile -> actDir.getParentFile().toPath().resolve(outerFile))
        .map(outerFile -> (Executable) () -> assertFalse(Files.exists(outerFile), "outerFile exists: " + outerFile))
        .toList()
    );
  }

  @Test
  default void testuplevels1_resource_only() throws Throwable {
    builder()
      .name("uplevels1_resource_only")
      .transtype(XHTML)
      .input(Paths.get("maps/above.ditamap"))
      .put("generate.copy.outer", "1")
      .put("outer.control", "quiet")
      .test();
  }

  @Test
  default void testuplevels3() throws Throwable {
    builder()
      .name("uplevels3")
      .transtype(XHTML)
      .input(Paths.get("maps/above.ditamap"))
      .put("generate.copy.outer", "3")
      .put("outer.control", "quiet")
      .test();
  }

  @Test
  default void testuplevels3_resource_only() throws Throwable {
    builder()
      .name("uplevels3_resource_only")
      .transtype(XHTML)
      .input(Paths.get("maps/above.ditamap"))
      .put("generate.copy.outer", "3")
      .put("outer.control", "quiet")
      .test();
  }
}
