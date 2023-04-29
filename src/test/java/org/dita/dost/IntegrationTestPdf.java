/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2021 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost;

import static org.dita.dost.AbstractIntegrationTest.Transtype.PDF;

import java.nio.file.Paths;
import org.junit.jupiter.api.Test;

public class IntegrationTestPdf extends AbstractIntegrationTest {

  public AbstractIntegrationTest builder() {
    return new IntegrationTestPdf();
  }

  @Override
  Transtype getTranstype(Transtype transtype) {
    return transtype;
  }

  @Test
  public void pdf() throws Throwable {
    builder().name("pdf").transtype(PDF).input(Paths.get("bookmap.ditamap")).warnCount(1).test();
  }
}
