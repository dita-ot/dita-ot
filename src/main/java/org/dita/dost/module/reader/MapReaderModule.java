/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2016 Jarno Elovirta
 *
 *  See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.module.reader;

import static org.dita.dost.reader.GenListModuleReader.ROOT_URI;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.exists;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.exception.DITAOTXMLErrorHandler;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.reader.GenListModuleReader.Reference;
import org.dita.dost.reader.SubjectSchemeReader;
import org.dita.dost.writer.DebugFilter;
import org.dita.dost.writer.NormalizeFilter;
import org.dita.dost.writer.ValidationFilter;
import org.xml.sax.XMLFilter;

/**
 * ModuleElem for reading and serializing topics into temporary directory.
 *
 * @since 2.5
 */
public final class MapReaderModule extends AbstractReaderModule {

  public MapReaderModule() {
    super();
    formatFilter = v -> Objects.equals(v, ATTR_FORMAT_VALUE_DITAMAP) || Objects.equals(v, ATTR_FORMAT_VALUE_DITAVAL);
  }

  @Override
  public AbstractPipelineOutput execute(final AbstractPipelineInput input) throws DITAOTException {
    try {
      parseInputParameters(input);
      init();

      readResourceFiles();
      readStartFile();
      processWaitList();

      handleConref();
      outputResult();

      job.write();
    } catch (final RuntimeException | DITAOTException e) {
      throw e;
    } catch (final Exception e) {
      throw new DITAOTException(e.getMessage(), e);
    }

    //    if (profilingEnabled) {
    //      var profileModule = new ProfileModule();
    //      profileModule.setJob(job);
    //      profileModule.setLogger(logger);
    //      profileModule.setXmlUtils(xmlUtils);
    //      profileModule.setParallel(parallel);
    //      profileModule.setProcessingMode(processingMode);
    //      profileModule.setFileInfoFilter((fileInfo -> Objects.equals(fileInfo.format, ATTR_FORMAT_VALUE_DITAMAP)));
    //
    //      profileModule.execute(input);
    //    }

    return null;
  }

  @Override
  void init() throws DITAOTException {
    super.init();
    listFilter.setForceType(MAP_MAP);
  }

  @Override
  public void readStartFile() throws DITAOTException {
    addToWaitList(new Reference(rootFile, getFormatFromPath(rootFile)));
  }

  @Override
  List<XMLFilter> getProcessingPipe(final URI fileToParse) {
    assert fileToParse.isAbsolute();
    final List<XMLFilter> pipe = new ArrayList<>();

    if (genDebugInfo) {
      final DebugFilter debugFilter = new DebugFilter();
      debugFilter.setLogger(logger);
      debugFilter.setCurrentFile(currentFile);
      pipe.add(debugFilter);
    }

    //    if (filterUtils != null) {
    //      final ProfilingFilter profilingFilter = new ProfilingFilter();
    //      profilingFilter.setLogger(logger);
    //      profilingFilter.setJob(job);
    //      profilingFilter.setFilterUtils(filterUtils);
    //      profilingFilter.setCurrentFile(fileToParse);
    //      pipe.add(profilingFilter);
    //    }

    final ValidationFilter validationFilter = new ValidationFilter();
    validationFilter.setLogger(logger);
    validationFilter.setValidateMap(validateMap);
    validationFilter.setCurrentFile(fileToParse);
    validationFilter.setJob(job);
    validationFilter.setProcessingMode(processingMode);
    pipe.add(validationFilter);

    final NormalizeFilter normalizeFilter = new NormalizeFilter();
    normalizeFilter.setLogger(logger);
    pipe.add(normalizeFilter);

    keydefFilter.setCurrentDir(fileToParse.resolve("."));
    keydefFilter.setErrorHandler(new DITAOTXMLErrorHandler(fileToParse.toString(), logger, processingMode));
    pipe.add(keydefFilter);

    listFilter.setCurrentFile(fileToParse);
    listFilter.setErrorHandler(new DITAOTXMLErrorHandler(fileToParse.toString(), logger, processingMode));
    pipe.add(listFilter);

    ditaWriterFilter.setDefaultValueMap(defaultValueMap);
    ditaWriterFilter.setCurrentFile(currentFile);
    ditaWriterFilter.setOutputFile(outputFile);
    pipe.add(ditaWriterFilter);

    return pipe;
  }

  @Override
  void categorizeReferenceFile(final Reference file) {
    if (file.format == null || ATTR_FORMAT_VALUE_DITA.equals(file.format)) {
      return;
    }
    // Ignore topics
    //        if (formatFilter.test(file.format)) {
    switch (file.format) {
      case ATTR_FORMAT_VALUE_DITAMAP -> addToWaitList(file);
      case ATTR_FORMAT_VALUE_IMAGE -> {
        formatSet.add(file);
        if (!exists(file.filename)) {
          logger.warn(MessageUtils.getMessage("DOTX008E", file.filename.toString()).toString());
        }
      }
      case ATTR_FORMAT_VALUE_DITAVAL -> formatSet.add(file);
      default -> htmlSet.put(file.format, file.filename);
    }
    //        }
  }

  @Override
  void outputResult() throws DITAOTException {
    super.outputResult();

    try {
      final SubjectSchemeReader subjectSchemeReader = new SubjectSchemeReader();
      subjectSchemeReader.setLogger(logger);
      subjectSchemeReader.setJob(job);
      subjectSchemeReader.writeMapToXML(
        addMapFilePrefix(listFilter.getRelationshipGrap()),
        new File(job.tempDir, FILE_NAME_SUBJECT_RELATION)
      );
      subjectSchemeReader.writeMapToXML(
        addMapFilePrefix(schemeDictionary),
        new File(job.tempDir, FILE_NAME_SUBJECT_DICTIONARY)
      );
    } catch (final IOException e) {
      throw new DITAOTException("Failed to serialize subject scheme files: " + e.getMessage(), e);
    }
  }

  /**
   * Convert absolute paths to relative temporary directory paths
   * @return map with relative keys and values
   */
  private Map<URI, Set<URI>> addMapFilePrefix(final Map<URI, Set<URI>> map) {
    final Map<URI, Set<URI>> res = new HashMap<>();
    for (final Map.Entry<URI, Set<URI>> e : map.entrySet()) {
      final URI key = e.getKey();
      final Set<URI> newSet = new HashSet<>();
      for (final URI file : e.getValue()) {
        newSet.add(tempFileNameScheme.generateTempFileName(file));
      }
      res.put(key.equals(ROOT_URI) ? key : tempFileNameScheme.generateTempFileName(key), newSet);
    }
    return res;
  }
}
