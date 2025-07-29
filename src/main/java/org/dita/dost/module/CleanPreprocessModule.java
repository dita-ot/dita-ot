/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2016 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.module;

import static java.util.Collections.emptyMap;
import static org.dita.dost.util.Constants.ATTR_FORMAT_VALUE_DITA;
import static org.dita.dost.util.Constants.ATTR_FORMAT_VALUE_DITAMAP;
import static org.dita.dost.util.Job.USER_INPUT_FILE_LIST_FILE;
import static org.dita.dost.util.XMLUtils.toErrorReporter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import net.sf.saxon.s9api.*;
import net.sf.saxon.trans.UncheckedXPathException;
import org.apache.commons.io.FileUtils;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.util.Constants;
import org.dita.dost.util.Job;
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.util.URLUtils;
import org.dita.dost.writer.AbstractXMLFilter;
import org.dita.dost.writer.LinkFilter;
import org.dita.dost.writer.MapCleanFilter;
import org.dita.dost.writer.TopicCleanFilter;
import org.w3c.dom.Document;
import org.xml.sax.XMLFilter;
import org.xmlresolver.Resolver;

/**
 * Move temporary files not based on output URI to match output URI structure.
 *
 * @since 2.5
 */
public class CleanPreprocessModule extends AbstractPipelineModuleImpl {

  private static final String PARAM_USE_RESULT_FILENAME = "use-result-filename";

  private final LinkFilter linkFilter = new LinkFilter();
  private final MapCleanFilter mapFilter = new MapCleanFilter();
  private final TopicCleanFilter topicFilter = new TopicCleanFilter();

  private boolean useResultFilename;
  private XsltTransformer rewriteTransformer;
  private RewriteRule rewriteClass;
  private URI tempBase;
  private Collection<FileInfo> fileInfosFromJob;
  private Job tempJob;

  private void init() {
    linkFilter.setJob(job);
    linkFilter.setLogger(logger);

    mapFilter.setJob(job);
    mapFilter.setLogger(logger);

    topicFilter.setJob(job);
    topicFilter.setLogger(logger);
  }

  @Override
  public AbstractPipelineOutput execute(final Map<String, String> input) throws DITAOTException {
    init(input);
    cleanFiles();
    updateProperties();
    fixInputMap();
    writeJobFile();
    return null;
  }

  private void init(final Map<String, String> input) {
    useResultFilename = getUseResultFilename(input);
    rewriteTransformer = getRewriteTransformerExtension(input);
    rewriteClass = getRewriteClassExtension(input);

    linkFilter.setLogger(logger);
    mapFilter.setLogger(logger);
    topicFilter.setLogger(logger);

    tempBase = job.getBaseDir();
  }

  private static Boolean getUseResultFilename(Map<String, String> input) {
    return Optional.ofNullable(input.get(PARAM_USE_RESULT_FILENAME)).map(Boolean::parseBoolean).orElse(false);
  }

  private XsltTransformer getRewriteTransformerExtension(Map<String, String> input) {
    final Resolver catalogResolver = xmlUtils.getCatalogResolver();
    return Optional
      .ofNullable(input.get("result.rewrite-rule.xsl"))
      .map(file -> {
        try {
          return catalogResolver.resolve(URLUtils.toURI(file).toString(), null);
        } catch (TransformerException e) {
          throw new RuntimeException(e.getMessage(), e);
        }
      })
      .map(f -> {
        try {
          final Processor processor = xmlUtils.getProcessor();
          final XsltCompiler xsltCompiler = processor.newXsltCompiler();
          xsltCompiler.setErrorReporter(toErrorReporter(logger));
          final XsltExecutable xsltExecutable = xsltCompiler.compile(f);
          return xsltExecutable.load();
        } catch (UncheckedXPathException e) {
          throw new RuntimeException("Failed to compile XSLT: " + e.getXPathException().getMessageAndLocation(), e);
        } catch (SaxonApiException e) {
          throw new RuntimeException("Failed to compile XSLT: " + e.getMessage(), e);
        }
      })
      .orElse(null);
  }

  private static RewriteRule getRewriteClassExtension(Map<String, String> input) {
    return Optional
      .ofNullable(input.get("result.rewrite-rule.class"))
      .map(c -> {
        try {
          final Class<RewriteRule> cls = (Class<RewriteRule>) Class.forName(c);
          return cls.getDeclaredConstructor().newInstance();
        } catch (
          ClassNotFoundException
          | InstantiationException
          | IllegalAccessException
          | NoSuchMethodException
          | InvocationTargetException e
        ) {
          throw new RuntimeException(e);
        }
      })
      .orElse(null);
  }

  private void cleanFiles() throws DITAOTException {
    if (useResultFilename) {
      extractFilesFromMainJob();
      rewriteFilesViaExtensions();
      rewriteFilesViaInternal();
    }
  }

  private void updateProperties() {
    job.setProperty("uplevels", getUplevels(tempBase));
    job.setInputDir(tempBase);
  }

  private void fixInputMap() {
    // start map
    final FileInfo start = job.getFileInfo(f -> f.isInput).iterator().next();

    if (start != null) {
      job.setInputMap(start.uri);

      final File inputfile = new File(job.tempDir, USER_INPUT_FILE_LIST_FILE);
      try {
        Files.writeString(inputfile.toPath(), start.file.getPath());
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private void writeJobFile() throws DITAOTException {
    try {
      job.write();
    } catch (IOException e) {
      throw new DITAOTException();
    }
  }

  private void extractFilesFromMainJob() {
    // collect and relativize result
    fileInfosFromJob =
      job
        .getFileInfo()
        .stream()
        .filter(fileInfo -> fileInfo.result != null)
        .map(fi -> FileInfo.builder(fi).result(tempBase.relativize(fi.result)).build())
        .collect(Collectors.toList());
    fileInfosFromJob.forEach(fi -> job.remove(fi));
  }

  /**
   * Rewrites files using internal filters and adds them back to the main job.
   * @throws DITAOTException       if an error occurs during the rewriting process
   */
  private void rewriteFilesViaInternal() throws DITAOTException {
    configureTempJob(fileInfosFromJob);
    for (final FileInfo fileInfo : fileInfosFromJob) {
      rewriteFileAndAddToMainJob(fileInfo);
    }
  }

  /**
   * Sets up a temporary {@link Job} for further processing.
   * @param fileInfoCollection     a collection of {@link FileInfo} objects to be processed
   */
  private void configureTempJob(Collection<FileInfo> fileInfoCollection) {
    HashMap<String, Object> tempProp = new HashMap<>();
    tempProp.put(Constants.ANT_INVOKER_EXT_PARAM_GENERATECOPYOUTTER, job.getGeneratecopyouter());
    tempJob = new Job(job, tempProp, fileInfoCollection);
    linkFilter.setJob(tempJob);
    mapFilter.setJob(tempJob);
    topicFilter.setJob(tempJob);
  }

  /**
   * Rewrites/cleans a file and adds it back to the main job if successful.
   * @param fileInfo               the file to be cleaned
   * @throws DITAOTException       if an error occurs during the rewriting process
   */
  private void rewriteFileAndAddToMainJob(FileInfo fileInfo) throws DITAOTException {
    try {
      if ("coderef".equals(fileInfo.format) || "image".equals(fileInfo.format)) {
        logger.debug("Skip format " + fileInfo.format);
      } else {
        rewriteFile(fileInfo);
      }

      job.add(finalizeFileInfo(fileInfo));
    } catch (final IOException e) {
      logger.error("Failed to clean " + job.tempDirURI.resolve(fileInfo.uri) + ": " + e.getMessage(), e);
    }
  }

  /**
   * Finalizes the relative URI and result URI.
   * @param fileInfo               the file info to process
   * @return                       a new {@link FileInfo} with updated URIs
   */
  private FileInfo finalizeFileInfo(FileInfo fileInfo) {
    URI finalUri = fileInfo.result;
    URI finalResult = tempBase.resolve(fileInfo.result);

    return FileInfo.builder(fileInfo).uri(finalUri).result(finalResult).build();
  }

  /**
   * Transforms a file through the processing pipeline
   * or if there's no pipeline then simply moves it to its destination.
   * @param fileInfo               the file to process
   * @throws DITAOTException       if an error occurs during the transformation
   * @throws IOException           if an I/O error occurs
   */
  private void rewriteFile(FileInfo fileInfo) throws DITAOTException, IOException {
    final File srcFile = new File(tempJob.tempDirURI.resolve(fileInfo.uri));
    if (tempJob.getStore().exists(srcFile.toURI())) {
      final File destFile = new File(tempJob.tempDirURI.resolve(tempBase.relativize(fileInfo.result)));
      final List<XMLFilter> processingPipe = getProcessingPipe(fileInfo, srcFile, destFile);
      if (!processingPipe.isEmpty()) {
        logger.info("Processing " + srcFile.toURI() + " to " + destFile.toURI());
        tempJob.getStore().transform(srcFile.toURI(), destFile.toURI(), processingPipe);
        if (!srcFile.equals(destFile)) {
          logger.debug("Deleting " + srcFile.toURI());
          FileUtils.deleteQuietly(srcFile);
        }
      } else if (!srcFile.equals(destFile)) {
        logger.info("Moving " + srcFile.toURI() + " to " + destFile.toURI());
        FileUtils.moveFile(srcFile, destFile);
      }
    }
  }

  /**
   * Checks if the result URI is an outer file, that is not located in the input directory.
   * @param resultUri              the URI to check
   * @return                       true if the file is an outer file, false otherwise
   */
  private boolean isOuterFile(URI resultUri) {
    return !Paths.get(resultUri).startsWith(Paths.get(job.getInputDir()));
  }

  /**
   * Rewrites files using plugins/extensions.
   * Supports both XSLT and Java-based rewrite rules.
   * @throws DITAOTException       if an error occurs during the rewriting process
   */
  private void rewriteFilesViaExtensions() throws DITAOTException {
    if (rewriteClass != null) {
      fileInfosFromJob = rewriteClass.rewrite(fileInfosFromJob);
    }
    if (rewriteTransformer != null) {
      try {
        final DOMSource source = new DOMSource(serialize(fileInfosFromJob));
        final Map<URI, FileInfo> files = new HashMap<>();
        final Destination result = new SAXDestination(new Job.JobHandler(new HashMap<>(), files));
        rewriteTransformer.setSource(source);
        rewriteTransformer.setDestination(result);
        rewriteTransformer.transform();
        fileInfosFromJob = files.values();
      } catch (IOException | SaxonApiException e) {
        throw new DITAOTException(e);
      }
    }
  }

  /**
   * Serializes a set of files into a single DOM for further processing.
   * @param fileInfoCollection     the collection to serialize
   * @return                       a DOM containing the serialized data
   * @throws IOException           if an error occurs during serialization
   */
  private Document serialize(final Collection<FileInfo> fileInfoCollection) throws IOException {
    try {
      final Document doc = xmlUtils.newDocument();
      final DOMResult result = new DOMResult(doc);
      XMLStreamWriter out = XMLOutputFactory.newInstance().createXMLStreamWriter(result);
      job.serialize(out, emptyMap(), fileInfoCollection);
      return (Document) result.getNode();
    } catch (final XMLStreamException e) {
      throw new IOException("Failed to serialize job file: " + e.getMessage());
    }
  }

  /**
   * If the input file is in a subdirectory of the base directory,
   * returns a relative path to the base directory.
   * This is not used by this class for cleaning the files.
   */
  String getUplevels(final URI base) {
    final URI rel = base.relativize(job.getInputFile());
    final int count = rel.toString().split("/").length - 1;
    return IntStream.range(0, count).boxed().map(i -> "../").collect(Collectors.joining(""));
  }

  private List<XMLFilter> getProcessingPipe(final FileInfo fileInfo, final File srcFile, final File destFile) {
    final List<XMLFilter> xmlFilterList = new ArrayList<>();

    final String format = fileInfo.format;
    final boolean isDita = ATTR_FORMAT_VALUE_DITA.equals(format);
    final boolean isMap = ATTR_FORMAT_VALUE_DITAMAP.equals(format);
    final boolean isUnknown = format == null;

    // add link filter for all DITA
    if (isDita || isMap || isUnknown) {
      linkFilter.setCurrentFile(srcFile.toURI());
      linkFilter.setDestFile(destFile.toURI());
      xmlFilterList.add(linkFilter);
    }

    // add topic filter for topics and map filter for maps
    if (isDita || isUnknown) {
      topicFilter.setFileInfo(fileInfo);
      xmlFilterList.add(topicFilter);
    } else if (isMap) {
      xmlFilterList.add(mapFilter);
    }

    // add remaining filters
    for (final XmlFilterModule.FilterPair filterPair : filters) {
      if (filterPair.predicate.test(fileInfo)) {
        final AbstractXMLFilter xmlFilter = filterPair.newInstance();
        logger.debug("Configure filter " + xmlFilter.getClass().getCanonicalName());
        xmlFilter.setCurrentFile(srcFile.toURI());
        xmlFilter.setJob(job);
        xmlFilter.setLogger(logger);
        xmlFilterList.add(xmlFilter);
      }
    }

    return xmlFilterList;
  }
}
