package org.dita.dost;

import static org.dita.dost.AbstractIntegrationTest.Transtype.XHTML;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

public interface ITContentUplevels {
  AbstractIntegrationTest builder();

  @Test
  default void uplevels1() throws Throwable {
    var outerFiles = getListOfPotentialOuterFilesInTmpAndOut();
    cleanUpAnyLeftovers(outerFiles);

    builder()
      .name("uplevels1")
      .transtype(XHTML)
      .input(Paths.get("maps/above.ditamap"))
      .put("generate.copy.outer", "1")
      .put("outer.control", "quiet")
      .test();

    assertNoOuterFilesAreOutsideTmpOrOut(outerFiles);
  }

  private static List<Path> getListOfPotentialOuterFilesInTmpAndOut() {
    Path tempDir = Paths.get("build", "tmp", "integrationTest", "uplevels1");
    return Stream
      .of("images/carwash.gif", "a.html", "b.html", "topics/c.html", "topics/d.html")
      .flatMap(file -> Stream.of(tempDir.resolve("out").resolve(file), tempDir.resolve("tmp").resolve(file)))
      .toList();
  }

  private static void assertNoOuterFilesAreOutsideTmpOrOut(List<Path> outerFiles) {
    assertAll(
      "Files should not exist outside the output folder",
      outerFiles
        .stream()
        .map(outerFile -> (Executable) () -> assertFalse(Files.exists(outerFile), "outerFile exists: " + outerFile))
        .toList()
    );
  }

  private static void cleanUpAnyLeftovers(List<Path> outerFiles) {
    outerFiles.forEach(remnantFromThePreviousRun -> {
      try {
        Files.deleteIfExists(remnantFromThePreviousRun);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    });
  }

  @Test
  default void uplevels1_resource_only() throws Throwable {
    builder()
      .name("uplevels1_resource_only")
      .transtype(XHTML)
      .input(Paths.get("maps/above.ditamap"))
      .put("generate.copy.outer", "1")
      .put("outer.control", "quiet")
      .test();
  }

  @Test
  default void uplevels3() throws Throwable {
    builder()
      .name("uplevels3")
      .transtype(XHTML)
      .input(Paths.get("maps/above.ditamap"))
      .put("generate.copy.outer", "3")
      .put("outer.control", "quiet")
      .test();
  }

  @Test
  default void uplevels3_resource_only() throws Throwable {
    builder()
      .name("uplevels3_resource_only")
      .transtype(XHTML)
      .input(Paths.get("maps/above.ditamap"))
      .put("generate.copy.outer", "3")
      .put("outer.control", "quiet")
      .test();
  }
}
