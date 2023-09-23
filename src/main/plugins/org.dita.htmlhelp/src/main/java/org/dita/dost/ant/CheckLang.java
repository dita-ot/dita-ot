/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2010 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.ant;

import static org.dita.dost.ant.ExtensibleAntInvoker.getJob;
import static org.dita.dost.util.Constants.*;

import java.io.File;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.dita.dost.log.DITAOTAntLogger;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.util.Job;
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.util.StringUtils;

/**
 * This class is for get the first xml:lang value set in ditamap/topic files
 *
 * @version 1.0 2010-09-30
 *
 * @author Zhang Di Hua
 */
public final class CheckLang extends Task {

  private File basedir;

  private File outputdir;

  private String inputmap;

  private String message;

  private DITAOTLogger logger;

  /**
   * Executes the Ant task.
   */
  @Override
  public void execute() {
    logger = new DITAOTAntLogger(getProject());
    logger.info(message);

    final Job job = getJob(getProject());

    //ensure outdir is absolute
    if (!outputdir.isAbsolute()) {
      outputdir = new File(basedir, outputdir.getPath()).getAbsoluteFile();
    }
    //ensure inputmap is absolute
    if (!new File(inputmap).isAbsolute()) {
      inputmap = new File(job.tempDir, inputmap).getAbsolutePath();
    }

    final LangParser parser = new LangParser();

    try {
      //parse the user input file(usually a map)
      job.getStore().transform(new File(inputmap).toURI(), parser);
      String langCode = parser.getLangCode();
      if (!StringUtils.isEmptyString(langCode)) {
        setActiveProjectProperty("htmlhelp.locale", langCode);
      } else {
        //parse topic files
        for (final FileInfo f : job.getFileInfo()) {
          if (ATTR_FORMAT_VALUE_DITA.equals(f.format)) {
            final File topicFile = new File(job.tempDir, f.file.getPath());
            if (topicFile.exists()) {
              job.getStore().transform(topicFile.toURI(), parser);
              langCode = parser.getLangCode();
              if (!StringUtils.isEmptyString(langCode)) {
                setActiveProjectProperty("htmlhelp.locale", langCode);
                break;
              }
            }
          }
        }
        //no lang is set
        if (StringUtils.isEmptyString(langCode)) {
          //use default lang code
          setActiveProjectProperty("htmlhelp.locale", "en-us");
        }
      }
    } catch (final Exception e) {
      /* Since an exception is used to stop parsing when the search
       * is successful, catch the exception.
       */
      if (e.getMessage() != null && e.getMessage().equals("Search finished")) {
        System.out.println("Lang search finished");
      } else {
        e.printStackTrace();
      }
    }
  }

  /**
   * Sets property in active ant project with name specified inpropertyName,
   * and value specified in propertyValue parameter
   */
  private void setActiveProjectProperty(final String propertyName, final String propertyValue) {
    final Project activeProject = getProject();
    if (activeProject != null) {
      activeProject.setProperty(propertyName, propertyValue);
    }
  }

  public void setBasedir(final File basedir) {
    this.basedir = basedir;
  }

  @Deprecated
  public void setTempdir(final File tempdir) {
    // NOOP
  }

  public void setInputmap(final String inputmap) {
    this.inputmap = inputmap;
  }

  public void setMessage(final String message) {
    this.message = message;
  }

  public void setOutputdir(final File outputdir) {
    this.outputdir = outputdir;
  }
}
