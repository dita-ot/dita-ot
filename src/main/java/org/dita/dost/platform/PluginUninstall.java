/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2023 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.platform;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.BuildException;
import org.dita.dost.invoker.CliException;
import org.dita.dost.invoker.Main;
import org.dita.dost.log.DITAOTLogger;

public final class PluginUninstall {

  private String id;
  private DITAOTLogger logger;
  private File ditaDir;

  private Integrator integrator;

  private void init() {
    integrator = new Integrator(ditaDir);
    integrator.setLogger(logger);
  }

  public void execute() throws Exception {
    init();
    if (id == null) {
      throw new BuildException(new IllegalStateException("id argument not set"));
    }

    final File pluginDir = Paths.get(ditaDir.getAbsolutePath(), "plugins", id).toFile();
    if (!pluginDir.exists()) {
      throw new CliException(Main.locale.getString("uninstall.error.plugin_not_found").formatted(id));
    }

    logger.info("Delete plug-in directory {0}", pluginDir);
    try {
      FileUtils.deleteDirectory(pluginDir);
    } catch (IOException e) {
      throw new BuildException("Failed to delete plug-in directory %s".formatted(pluginDir), e);
    }

    integrator.setLogger(logger);
    try {
      integrator.execute();
    } catch (final Exception e) {
      throw new BuildException("Integration failed: " + e.getMessage(), e);
    }
  }

  public void setLogger(DITAOTLogger logger) {
    this.logger = logger;
  }

  public void setDitaDir(File ditaDir) {
    this.ditaDir = ditaDir;
  }

  public void setId(final String id) {
    this.id = id;
  }
}
