/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2016 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.ant.types;

import static org.dita.dost.util.URLUtils.toURI;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import org.apache.commons.io.FilenameUtils;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.util.FileNameMapper;
import org.dita.dost.ant.ExtensibleAntInvoker;
import org.dita.dost.util.Job;

/**
 * File mapper that uses job configuration's {@link org.dita.dost.util.Job.FileInfo#result result}
 * to resolve output file. If {@link org.dita.dost.util.Job.FileInfo#result result} is not defined,
 * the original output filename is used.
 *
 * @since 3.0
 */
public class JobMapper implements FileNameMapper {

  private enum Type {
    TEMP,
    RESULT,
  }

  private Type type = Type.RESULT;
  private Job job;
  private String to;
  private String from;

  public void setProject(Project project) {
    job = ExtensibleAntInvoker.getJob(project);
  }

  @Override
  public void setFrom(String from) {
    this.from = from;
  }

  public String getFrom() {
    return this.from;
  }

  @Override
  public void setTo(String to) {
    this.to = to.contains(".") ? to : ("." + to);
  }

  public String getTo() {
    return this.to;
  }

  public void setType(TypeAttribute attr) {
    type = Type.valueOf(attr.getValue().toUpperCase());
  }

  @Override
  public String[] mapFileName(String sourceFileName) {
    final String filePath = getFilePath(sourceFileName);

    String result;
    if (to == null) {
      result = filePath;
    } else if (from == null) {
      result = FilenameUtils.removeExtension(filePath) + to;
    } else {
      result = filePath.replace(from, to);
    }
    return new String[] { result };
  }

  private String getFilePath(String sourceFileName) {
    final URI uri = toURI(sourceFileName);
    Job.FileInfo fileInfo = job.getFileInfo(uri);
    if (fileInfo == null) {
      fileInfo = job.getFileInfo(job.getInputDir().resolve(uri));
    }

    final String filePath =
      switch (type) {
        case TEMP -> fileInfo.file.getPath();
        case RESULT -> fileInfo.result == null
          ? sourceFileName
          : getBase().relativize(Paths.get(fileInfo.result)).toString();
      };
    return filePath;
  }

  private Path getBase() {
    return (job.getGeneratecopyouter() == Job.Generate.NOT_GENERATEOUTTER)
      ? Paths.get(job.getInputDir().resolve(job.getInputMap())).getParent()
      : Paths.get(job.getResultBaseDirNormal());
  }

  public static class TypeAttribute extends EnumeratedAttribute {

    @Override
    public String[] getValues() {
      return Arrays
        .stream(Type.values())
        .map(t -> t.toString().toLowerCase())
        .toList()
        .toArray(new String[Type.values().length]);
    }
  }
}
