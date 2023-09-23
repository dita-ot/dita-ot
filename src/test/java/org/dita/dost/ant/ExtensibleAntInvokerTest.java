/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2020 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.ant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.IOException;
import org.apache.tools.ant.Project;
import org.dita.dost.store.StreamStore;
import org.dita.dost.util.Job;
import org.dita.dost.util.XMLUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class ExtensibleAntInvokerTest {

  private Project project;

  @TempDir
  private File tempDir;

  @BeforeEach
  public void setUp() throws IOException {
    project = new Project();
    project.setUserProperty("dita.temp.dir", tempDir.getAbsolutePath());
  }

  @Test
  public void getJob_witJobReference() throws IOException {
    final Job job = new Job(tempDir, new StreamStore(tempDir, new XMLUtils()));
    project.addReference("job", job);
    final Job act = ExtensibleAntInvoker.getJob(project);
    assertNotNull(act);
    assertEquals(job, act);
  }

  @Test
  public void getJob_withoutJobReference() {
    final Job act = ExtensibleAntInvoker.getJob(project);
    assertNotNull(act);
  }
}
