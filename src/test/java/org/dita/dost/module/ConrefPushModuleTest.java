/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2022 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.module;

import static org.dita.dost.util.Constants.ATTR_FORMAT_VALUE_DITA;
import static org.dita.dost.util.Constants.ATTR_FORMAT_VALUE_DITAMAP;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.util.Configuration.Mode;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class ConrefPushModuleTest extends AbstractModuleTest {
  @Parameterized.Parameters(name = "{0}")
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {"conref_push", Mode.STRICT},
          {"pushAfter_between_Specialization", Mode.STRICT},
          {"pushAfter_with_crossRef", Mode.STRICT},
          {"pushAfter_with_InvalidTarget", Mode.STRICT},
          {"pushAfter_without_conref", Mode.STRICT},
          {"pushBefore_between_Specialization", Mode.STRICT},
          {"pushBefore_with_crossRef", Mode.STRICT},
          {"pushBefore_with_InvalidTarget", Mode.LAX},
          {"pushBefore_without_conref", Mode.STRICT},
          {"pushReplace_between_Specialization", Mode.STRICT},
          {"pushReplace_with_crossRef", Mode.STRICT},
          {"pushReplace_with_InvalidTarget", Mode.LAX},
          {"pushReplace_without_conref", Mode.STRICT},
          {"simple_pushAfter", Mode.STRICT},
          {"simple_pushBefore", Mode.STRICT},
          {"simple_pushReplace", Mode.STRICT}
        });
  }

  public ConrefPushModuleTest(String testCase, Mode mode) {
    super(testCase, Collections.emptyMap());
    this.mode = mode;
  }

  @Override
  protected AbstractPipelineInput getAbstractPipelineInput() {
    return new PipelineHashIO();
  }

  @Override
  protected AbstractPipelineModule getModule(final File tempDir) {
    final ConrefPushModule conrefPushModule = new ConrefPushModule();
    conrefPushModule.setFileInfoFilter(
        fileInfo ->
            fileInfo.format.equals(ATTR_FORMAT_VALUE_DITA)
                || fileInfo.format.equals(ATTR_FORMAT_VALUE_DITAMAP) && fileInfo.isInput);
    return conrefPushModule;
  }
}
