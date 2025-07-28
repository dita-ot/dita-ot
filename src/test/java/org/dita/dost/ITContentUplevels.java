package org.dita.dost;

import static org.dita.dost.AbstractIntegrationTest.Transtype.XHTML;

import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

public interface ITContentUplevels {
  AbstractIntegrationTest builder();

  @Test
  default void testuplevels1() throws Throwable {
    builder()
      .name("uplevels1")
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
}
