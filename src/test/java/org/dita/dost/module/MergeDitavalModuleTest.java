/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2024 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.module;

import static org.dita.dost.TestUtils.assertXMLEqual;
import static org.dita.dost.util.Constants.ANT_INVOKER_PARAM_DITAVAL;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Map;
import javax.xml.parsers.ParserConfigurationException;
import org.dita.dost.TestUtils;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.store.StreamStore;
import org.dita.dost.util.Job;
import org.dita.dost.util.XMLUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

class MergeDitavalModuleTest {

  @TempDir
  private File tempDir;

  private MergeDitavalModule module;
  private Job job;

  @BeforeEach
  void setUp() throws IOException {
    module = new MergeDitavalModule();
    module.setLogger(new TestUtils.TestLogger());
    job = new Job(tempDir, new StreamStore(tempDir, new XMLUtils()));
    module.setJob(job);
  }

  @Test
  public void test_single()
    throws DITAOTException, URISyntaxException, ParserConfigurationException, IOException, SAXException {
    final Map<String, String> input = Map.of(
      ANT_INVOKER_PARAM_DITAVAL,
      Paths.get(getClass().getResource("/MergeDitavalModuleTest/src/base.ditaval").toURI()).toString()
    );

    module.execute(input);

    assertXMLEqual(
      new InputSource(getClass().getResource("/MergeDitavalModuleTest/exp/base.ditaval").toString()),
      new InputSource(new File(job.getProperty("dita.input.valfile")).toURI().toString())
    );
  }

  @Test
  public void test_combine() throws DITAOTException, URISyntaxException {
    final Map<String, String> input = Map.of(
      ANT_INVOKER_PARAM_DITAVAL,
      Paths.get(getClass().getResource("/MergeDitavalModuleTest/src/override.ditaval").toURI()) +
      File.pathSeparator +
      Paths.get(getClass().getResource("/MergeDitavalModuleTest/src/base.ditaval").toURI())
    );

    module.execute(input);

    assertXMLEqual(
      new InputSource(getClass().getResource("/MergeDitavalModuleTest/exp/override.ditaval").toString()),
      new InputSource(new File(job.getProperty("dita.input.valfile")).toURI().toString())
    );
  }
}
