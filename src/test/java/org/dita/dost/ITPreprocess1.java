/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2019 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost;

import static org.dita.dost.AbstractIntegrationTest.Transtype.PREPROCESS;

import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

public class ITPreprocess1 extends AbstractIntegrationTest implements ITPreprocessBase {

  public ITPreprocess1 builder() {
    return new ITPreprocess1();
  }

  @Override
  Transtype getTranstype(Transtype transtype) {
    return transtype;
  }

  @Test
  public void testRngGrammarPoolNoValidate() throws Throwable {
    builder()
      .name("bookmap-rng-based-no-validate")
      .transtype(PREPROCESS)
      .input(Paths.get("main.ditamap"))
      .put("validate", "false")
      .warnCount(1)
      .errorCount(0)
      .test();
  }

  @Test
  public void testXSDValidate() throws Throwable {
    builder()
      .name("bookmap-xsd-based-validate")
      .transtype(PREPROCESS)
      .input(Paths.get("main.ditamap"))
      .put("validate", "true")
      .warnCount(0)
      .errorCount(0)
      .test();
  }
}
