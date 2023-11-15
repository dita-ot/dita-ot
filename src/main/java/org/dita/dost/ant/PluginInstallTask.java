/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2018 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.ant;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.dita.dost.log.DITAOTAntLogger;
import org.dita.dost.platform.PluginInstall;
import org.dita.dost.platform.SemVerMatch;

public final class PluginInstallTask extends Task {

  private Path pluginFile;
  private URI pluginUrl;
  private String pluginName;
  private SemVerMatch pluginVersion;
  private boolean force;

  @Override
  public void execute() throws BuildException {
    if (pluginFile == null && pluginUrl == null && pluginName == null) {
      throw new BuildException(new IllegalStateException("pluginName argument not set"));
    }

    final PluginInstall pluginInstall = new PluginInstall();
    final DITAOTAntLogger logger = new DITAOTAntLogger(getProject());
    logger.setTarget(getOwningTarget());
    logger.setTask(this);
    pluginInstall.setLogger(logger);
    pluginInstall.setForce(force);
    pluginInstall.setDitaDir(getDitaDir());
    pluginInstall.setPluginFile(pluginFile);
    pluginInstall.setPluginUri(pluginUrl);
    pluginInstall.setPluginName(pluginName);
    pluginInstall.setPluginVersion(pluginVersion);

    try {
      pluginInstall.execute();
    } catch (Exception e) {
      throw new BuildException("Failed to install %s %s: %s".formatted(pluginName, pluginVersion, e.getMessage()), e);
    }
  }

  private File getDitaDir() {
    return Paths.get(getProject().getProperty("dita.dir")).toFile();
  }

  public void setPluginFile(final String pluginFile) {
    try {
      this.pluginFile = Paths.get(pluginFile);
    } catch (InvalidPathException e) {
      // Ignore
    }
    try {
      final URI uri = new URI(pluginFile);
      if (uri.isAbsolute()) {
        this.pluginUrl = uri;
      }
    } catch (URISyntaxException e) {
      // Ignore
    }
    if (pluginFile.contains("@")) {
      final String[] tokens = pluginFile.split("@");
      pluginName = tokens[0];
      pluginVersion = new SemVerMatch(tokens[1]);
    } else {
      pluginName = pluginFile;
      pluginVersion = null;
    }
  }

  public void setForce(final boolean force) {
    this.force = force;
  }
}
