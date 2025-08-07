/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2017 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.writer;

import static org.dita.dost.util.Constants.ATTR_FORMAT_VALUE_DITAMAP;
import static org.dita.dost.util.URLUtils.getRelativePath;

import java.io.File;
import java.util.Objects;
import org.dita.dost.util.Job;
import org.xml.sax.SAXException;

public class TopicCleanFilter extends AbstractXMLFilter {

  private static final String URI_STEP = "../";
  private static final String SINGLE_URI_STEP = "./";
  private static final String FILE_STEP = ".." + File.separator;
  private static final String SINGLE_FILE_STEP = "";

  private Job.FileInfo fi;
  String pathToRootDir;
  String pathToMapDir;

  public void setFileInfo(Job.FileInfo fi) {
    this.fi = fi;
  }

  @Override
  public void startDocument() throws SAXException {
    calculatePathToProjectDirs();
  }

  @Override
  public void processingInstruction(String target, String data) throws SAXException {
    final String res = getProcessingInstruction(target, data);
    getContentHandler().processingInstruction(target, res);
  }

  void calculatePathToProjectDirs() {
    pathToMapDir =
      job
        .getFileInfo(fi -> fi.isInput && Objects.equals(fi.format, ATTR_FORMAT_VALUE_DITAMAP))
        .stream()
        .findAny()
        .map(startFile -> getRelativePath(fi.result, startFile.result).resolve(".").getPath())
        .orElse("");
    if (job.getGeneratecopyouter() == Job.Generate.OLDSOLUTION) {
      pathToRootDir = getRelativePath(fi.result, job.getResultBaseDirNormal().resolve("dummy")).resolve(".").getPath();
      pathToRootDir = pathToRootDir.isEmpty() ? SINGLE_URI_STEP : pathToRootDir;
    } else {
      pathToRootDir = (pathToMapDir == null || pathToMapDir.isEmpty()) ? SINGLE_URI_STEP : pathToMapDir;
    }
  }

  String getProcessingInstruction(String target, String data) {
    return switch (target) {
      case "path2project" -> pathToRootDir.equals(SINGLE_URI_STEP)
        ? SINGLE_FILE_STEP
        : pathToRootDir.replace('/', File.separatorChar);
      case "path2project-uri" -> pathToRootDir;
      case "path2rootmap-uri" -> pathToMapDir != null
        ? (pathToMapDir.isEmpty() ? SINGLE_URI_STEP : pathToMapDir)
        : data;
      default -> data;
    };
  }
}
