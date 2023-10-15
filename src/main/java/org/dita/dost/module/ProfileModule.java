/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2013 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.module;

import static java.util.stream.Collectors.mapping;
import static org.dita.dost.util.Configuration.printTranstype;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.XMLUtils.ancestors;
import static org.dita.dost.util.XMLUtils.toList;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.tools.ant.util.FileUtils;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.reader.DitaValReader;
import org.dita.dost.reader.SubjectSchemeReader;
import org.dita.dost.util.FilterUtils;
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.writer.ProfilingFilter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Filter module class.
 */
final class ProfileModule extends AbstractPipelineModuleImpl {

  private FilterUtils filterUtils;
  private ProfilingFilter writer;

  @Override
  public AbstractPipelineOutput execute(final AbstractPipelineInput input) throws DITAOTException {
    init(input);

    for (final FileInfo f : job.getFileInfo(fileInfoFilter)) {
      final File file = new File(job.tempDir, f.file.getPath());
      logger.info("Processing " + file.getAbsolutePath());

      writer.setCurrentFile(file.toURI());
      try {
        writer.write(file.getAbsoluteFile());
        if (!writer.hasElementOutput()) {
          logger.info("All content in " + file.getAbsolutePath() + " was filtered out");
          job.remove(f);
          FileUtils.delete(file);
        }
      } catch (final Exception e) {
        logger.error("Failed to profile " + file.getAbsolutePath() + ": " + e.getMessage());
      }
    }

    try {
      job.write();
    } catch (final IOException e) {
      throw new DITAOTException(e);
    }

    return null;
  }

  private void init(final AbstractPipelineInput input) throws DITAOTException {
    if (logger == null) {
      throw new IllegalStateException("Logger not set");
    }
    final String transtype = input.getAttribute(ANT_INVOKER_EXT_PARAM_TRANSTYPE);
    final File ditavalFile = Optional
      .of(new File(job.tempDir, FILE_NAME_MERGED_DITAVAL))
      .filter(File::exists)
      .orElse(null);

    final DitaValReader ditaValReader = new DitaValReader();
    ditaValReader.setLogger(logger);
    ditaValReader.setJob(job);
    if (ditavalFile != null) {
      ditaValReader.read(ditavalFile.toURI());
      filterUtils =
        new FilterUtils(
          printTranstype.contains(transtype),
          ditaValReader.getFilterMap(),
          ditaValReader.getForegroundConflictColor(),
          ditaValReader.getBackgroundConflictColor()
        );
    } else {
      filterUtils = new FilterUtils(printTranstype.contains(transtype));
    }
    filterUtils.setLogger(logger);

    initSubjectScheme();

    writer = new ProfilingFilter();
    writer.setLogger(logger);
    writer.setJob(job);
    writer.setFilterUtils(filterUtils);
  }

  private void initSubjectScheme() throws DITAOTException {
    if (filterUtils != null) {
      final Document doc = getMapDocument();
      if (doc != null) {
        final SubjectSchemeReader subjectSchemeReader = new SubjectSchemeReader();
        subjectSchemeReader.setLogger(logger);
        subjectSchemeReader.setJob(job);
        logger.info("Loading subject schemes");
        final List<Element> subjectSchemes = toList(doc.getDocumentElement().getElementsByTagName("*"));
        subjectSchemes
          .stream()
          .filter(SUBJECTSCHEME_ENUMERATIONDEF::matches)
          .map(enumerationDef1 ->
            Map.entry(
              ancestors(enumerationDef1).filter(SUBMAP::matches).findFirst().orElse(doc.getDocumentElement()),
              enumerationDef1
            )
          )
          .collect(Collectors.groupingBy(Map.Entry::getKey, mapping(Map.Entry::getValue, Collectors.toList())))
          .forEach((schemeRoot, enumerationDefs) -> {
            var subjectDefinitions = subjectSchemeReader.getSubjectDefinition(schemeRoot);
            for (Element enumerationDef : enumerationDefs) {
              subjectSchemeReader.processEnumerationDef(subjectDefinitions, enumerationDef);
            }
          });
        var subjectSchemeMap = subjectSchemeReader.getSubjectSchemeMap();
        if (subjectSchemeMap.subjectSchemeMap().isEmpty()) {
          return;
        }
        filterUtils = filterUtils.refine(subjectSchemeMap);
      }
    }
  }

  private Document getMapDocument() throws DITAOTException {
    final Collection<FileInfo> fis = job.getFileInfo(f ->
      f.isInput && Objects.equals(f.format, ATTR_FORMAT_VALUE_DITAMAP)
    );
    if (fis.isEmpty()) {
      return null;
    }
    final FileInfo fi = fis.iterator().next();
    final URI currentFile = job.tempDirURI.resolve(fi.uri);
    try {
      logger.debug("Reading " + currentFile);
      return job.getStore().getDocument(currentFile);
    } catch (final IOException e) {
      throw new DITAOTException(new SAXException("Failed to parse " + currentFile, e));
    }
  }
}
