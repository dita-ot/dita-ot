package org.dita.dost;

import static org.dita.dost.AbstractIntegrationTest.Transtype.*;

import java.io.File;
import java.nio.file.Paths;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class EndToEndTest extends AbstractIntegrationTest {

  public AbstractIntegrationTest builder() {
    return new EndToEndTest();
  }

  @Override
  Transtype getTranstype(Transtype transtype) {
    return transtype;
  }

  @Test
  public void xhtml() throws Throwable {
    builder().name("e2e").transtype(XHTML).input(Paths.get("root.ditamap")).run();
  }

  @Test
  public void html5() throws Throwable {
    builder().name("e2e").transtype(HTML5).input(Paths.get("root.ditamap")).run();
  }

  @Test
  public void pdf() throws Throwable {
    builder()
      .name("e2e")
      .transtype(PDF)
      .input(Paths.get("root.ditamap"))
      .put("args.fo.userconfig", new File(resourceDir, "e2e" + File.separator + "fop.xconf").getAbsolutePath())
      .run();
  }

  @Disabled
  @Test
  public void htmlhelp() throws Throwable {
    builder().name("e2e").transtype(HTMLHELP).input(Paths.get("root.ditamap")).run();
  }
}
