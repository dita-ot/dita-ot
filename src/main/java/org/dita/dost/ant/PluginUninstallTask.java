/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2023 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.ant;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.dita.dost.log.DITAOTAntLogger;
import org.dita.dost.platform.Integrator;

public final class PluginUninstallTask extends Task {

  private String id;

  @Override
  public void execute() throws BuildException {
    if (id == null) {
      throw new BuildException(new IllegalStateException("id argument not set"));
    }

    final File pluginDir = getPluginDir(id);
    if (!pluginDir.exists()) {
      throw new BuildException("Plug-in directory %s doesn't exist".formatted(pluginDir));
    }

    log("Delete plug-in directory %s".formatted(pluginDir), Project.MSG_DEBUG);
    try {
      FileUtils.deleteDirectory(pluginDir);
    } catch (IOException e) {
      throw new BuildException("Failed to delete plug-in directory %s".formatted(pluginDir), e);
    }

    final DITAOTAntLogger logger = new DITAOTAntLogger(getProject());
    logger.setTarget(getOwningTarget());
    logger.setTask(this);
    final Integrator integrator = new Integrator(getDitaDir());
    integrator.setLogger(logger);
    try {
      integrator.execute();
    } catch (final Exception e) {
      throw new BuildException("Integration failed: " + e.getMessage(), e);
    }
  }

  private File getDitaDir() {
    return Paths.get(getProject().getProperty("dita.dir")).toFile();
  }

  private File getPluginDir(final String id) {
    return Paths.get(getDitaDir().getAbsolutePath(), "plugins", id).toFile();
  }

  public void setId(final String id) {
    this.id = id;
  }
}
