/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2023 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.ant;

import java.io.File;
import java.nio.file.Paths;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.dita.dost.log.DITAOTAntLogger;
import org.dita.dost.platform.PluginUninstall;

public final class PluginUninstallTask extends Task {

  private String id;

  @Override
  public void execute() throws BuildException {
    if (id == null) {
      throw new BuildException(new IllegalStateException("id argument not set"));
    }

    final PluginUninstall pluginUninstall = new PluginUninstall();
    final DITAOTAntLogger logger = new DITAOTAntLogger(getProject());
    logger.setTarget(getOwningTarget());
    logger.setTask(this);
    pluginUninstall.setLogger(logger);
    pluginUninstall.setId(id);
    pluginUninstall.setDitaDir(getDitaDir());

    try {
      pluginUninstall.execute();
    } catch (Exception e) {
      throw new BuildException("Failed to uninstall %s: %s".formatted(id, e.getMessage()), e);
    }
  }

  private File getDitaDir() {
    return Paths.get(getProject().getProperty("dita.dir")).toFile();
  }

  public void setId(final String id) {
    this.id = id;
  }
}
