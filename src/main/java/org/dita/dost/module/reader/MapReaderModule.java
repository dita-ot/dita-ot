/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2016 Jarno Elovirta
 *
 *  See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.module.reader;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.exists;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.exception.DITAOTXMLErrorHandler;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.reader.GenListModuleReader;
import org.dita.dost.reader.GenListModuleReader.Reference;
import org.dita.dost.reader.KeydefFilter;
import org.dita.dost.writer.DebugFilter;
import org.dita.dost.writer.DitaWriterFilter;
import org.dita.dost.writer.NormalizeFilter;
import org.dita.dost.writer.ValidationFilter;
import org.xml.sax.XMLFilter;

/**
 * Module for reading and serializing maps into temporary directory.
 *
 * <p>The processing is a SAX pipe split into.
 * <dl>
 *     <dt>{@link DebugFilter}
 *     <dd>Add debugging attributes
 *     <dt>{@link ValidationFilter}
 *     <dd>Validation and optional error recovery filter
 *     <dt>{@link NormalizeFilter}
 *     <dd>Normalize content
 *     <dt>{@link KeydefFilter}
 *     <dd>Collect keys for Subject Scheme processing
 *     <dt>{@link GenListModuleReader}
 *     <dd>Collect link information
 *     <dt>{@link DitaWriterFilter}
 *     <dd>Insert document PIs and normalize attributes
 * </dl>
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

    final ValidationFilter validationFilter = new ValidationFilter();
    validationFilter.setLogger(logger);
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
}
