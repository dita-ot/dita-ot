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
  private String pathToRootDir;
  private String pathToMapDir;

  public void setFileInfo(Job.FileInfo fi) {
    this.fi = fi;
  }

  @Override
  public void startDocument() throws SAXException {
    final int stepsToRootDir = fi.result.getPath().split("/").length - 1;
    pathToRootDir = stepsToRootDir == 0 ? SINGLE_URI_STEP : URI_STEP.repeat(stepsToRootDir);
    pathToMapDir =
      job
        .getFileInfo(fi -> fi.isInput && Objects.equals(fi.format, ATTR_FORMAT_VALUE_DITAMAP))
        .stream()
        .findAny()
        .map(startFile -> {
          final String relativePath = getRelativePath(fi.uri, startFile.uri).resolve(".").getPath();
          //          return relativePath.getPath().split("/").length;
          return relativePath;
        })
        .orElse(null);
  }

  @Override
  public void processingInstruction(String target, String data) throws SAXException {
    switch (target) {
      case "path2project":
        getContentHandler()
          .processingInstruction(
            target,
            pathToRootDir.equals(SINGLE_URI_STEP) ? "" : pathToRootDir.replace('/', File.separatorChar)
          );
        break;
      case "path2project-uri":
        getContentHandler().processingInstruction(target, pathToRootDir);
        break;
      case "path2rootmap-uri":
        getContentHandler().processingInstruction(target, pathToMapDir != null ? pathToMapDir : data);
        break;
      default:
        getContentHandler().processingInstruction(target, data);
        break;
    }
  }
}
