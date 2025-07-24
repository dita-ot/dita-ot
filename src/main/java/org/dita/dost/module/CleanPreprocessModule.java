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
  private URI base;

  private void init(final Map<String, String> input) {
    useResultFilename =
      Optional.ofNullable(input.get(PARAM_USE_RESULT_FILENAME)).map(Boolean::parseBoolean).orElse(false);
    final Resolver catalogResolver = xmlUtils.getCatalogResolver();
    rewriteTransformer =
      Optional
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
    rewriteClass =
      Optional
        .ofNullable(input.get("result.rewrite-rule.class"))
        .map(c -> {
          try {
            final Class<RewriteRule> cls = (Class<RewriteRule>) Class.forName(c);
            return cls.newInstance();
          } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
          }
        })
        .orElse(null);

    linkFilter.setLogger(logger);

    mapFilter.setLogger(logger);
    topicFilter.setLogger(logger);

    base = job.getBaseDir();
  }

  @Override
  public AbstractPipelineOutput execute(final Map<String, String> input) throws DITAOTException {
    init(input);
    cleanFiles();
    fixInputMap();
    writeJob();
    return null;
  }

  private void cleanFiles() throws DITAOTException {
    if (useResultFilename) {
      var fileSet = extractFileInfosFromJob();
      fileSet = rewriteViaExtensions(fileSet);
      rewriteViaInternal(fileSet);
    }

    job.setProperty("uplevels", getUplevels(base));
    job.setInputDir(base);
  }

  private void fixInputMap() throws DITAOTException {
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

  private void writeJob() throws DITAOTException {
    try {
      job.write();
    } catch (IOException e) {
      throw new DITAOTException();
    }
  }

  private Collection<FileInfo> extractFileInfosFromJob() {
    // collect and relativize result
    final Collection<FileInfo> original = job
      .getFileInfo()
      .stream()
      .filter(fileInfo -> fileInfo.result != null)
      .collect(Collectors.toList());
    original.forEach(fi -> job.remove(fi));

    return original;
  }

  private void rewriteViaInternal(Collection<FileInfo> fileInfoCollection) throws DITAOTException {
    final Job tempJob = createTempJob(fileInfoCollection);
    for (final FileInfo fileInfo : fileInfoCollection) {
      rewriteFileAndAddToJob(fileInfo, tempJob);
    }
  }

  private Job createTempJob(Collection<FileInfo> fileInfoCollection) {
    HashMap<String, Object> tempProp = new HashMap<>();
    tempProp.put(Constants.ANT_INVOKER_EXT_PARAM_GENERATECOPYOUTTER, job.getGeneratecopyouter());
    final Job tempJob = new Job(job, tempProp, fileInfoCollection);
    linkFilter.setJob(tempJob);
    mapFilter.setJob(tempJob);
    topicFilter.setJob(tempJob);
    return tempJob;
  }

  private void rewriteFileAndAddToJob(FileInfo fileInfo, Job tempJob) throws DITAOTException {
    try {
      if (fileInfo.format != null && (fileInfo.format.equals("coderef") || fileInfo.format.equals("image"))) {
        logger.debug("Skip format " + fileInfo.format);
      } else {
        rewriteFile(fileInfo, tempJob);
      }

      job.add(finalizeFileInfo(fileInfo));
    } catch (final IOException e) {
      logger.error("Failed to clean " + job.tempDirURI.resolve(fileInfo.uri) + ": " + e.getMessage(), e);
    }
  }

  private FileInfo finalizeFileInfo(FileInfo fileInfo) {
    URI finalUri = base.relativize(fileInfo.result);
    URI finalResult = fileInfo.result;

    FileInfo.Builder builder = FileInfo.builder(fileInfo).uri(finalUri).result(finalResult);

    if (job.getGeneratecopyouter() == Job.Generate.NOT_GENERATEOUTTER && isOutFile(finalResult)) {
      builder.isResourceOnly(true);
    }

    return builder.build();
  }

  private void rewriteFile(FileInfo fileInfo, Job tempJob) throws DITAOTException, IOException {
    final File srcFile = new File(tempJob.tempDirURI.resolve(fileInfo.uri));
    if (tempJob.getStore().exists(srcFile.toURI())) {
      final File destFile = new File(tempJob.tempDirURI.resolve(base.relativize(fileInfo.result)));
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

  private boolean isOutFile(URI resultUri) {
    return !Paths.get(resultUri).startsWith(Paths.get(job.getInputDir()));
  }

  private Collection<FileInfo> rewriteViaExtensions(final Collection<FileInfo> fileInfoCollection)
    throws DITAOTException {
    if (rewriteClass != null) {
      return rewriteClass.rewrite(fileInfoCollection);
    }
    if (rewriteTransformer != null) {
      try {
        final DOMSource source = new DOMSource(serialize(fileInfoCollection));
        final Map<URI, FileInfo> files = new HashMap<>();
        final Destination result = new SAXDestination(new Job.JobHandler(new HashMap<>(), files));
        rewriteTransformer.setSource(source);
        rewriteTransformer.setDestination(result);
        rewriteTransformer.transform();
        return files.values();
      } catch (IOException | SaxonApiException e) {
        throw new DITAOTException(e);
      }
    }
    return fileInfoCollection;
  }

  private Document serialize(final Collection<FileInfo> fis) throws IOException {
    try {
      final Document doc = xmlUtils.newDocument();
      final DOMResult result = new DOMResult(doc);
      XMLStreamWriter out = XMLOutputFactory.newInstance().createXMLStreamWriter(result);
      job.serialize(out, emptyMap(), fis);
      return (Document) result.getNode();
    } catch (final XMLStreamException e) {
      throw new IOException("Failed to serialize job file: " + e.getMessage());
    }
  }

  String getUplevels(final URI base) {
    final URI rel = base.relativize(job.getInputFile());
    final int count = rel.toString().split("/").length - 1;
    return IntStream.range(0, count).boxed().map(i -> "../").collect(Collectors.joining(""));
  }

  private void init() {
    linkFilter.setJob(job);
    linkFilter.setLogger(logger);

    mapFilter.setJob(job);
    mapFilter.setLogger(logger);

    topicFilter.setJob(job);
    topicFilter.setLogger(logger);
  }

  private List<XMLFilter> getProcessingPipe(final FileInfo fileInfo, final File srcFile, final File destFile) {
    final List<XMLFilter> res = new ArrayList<>();

    if (
      fileInfo.format == null ||
      fileInfo.format.equals(ATTR_FORMAT_VALUE_DITA) ||
      fileInfo.format.equals(ATTR_FORMAT_VALUE_DITAMAP)
    ) {
      linkFilter.setCurrentFile(srcFile.toURI());
      linkFilter.setDestFile(destFile.toURI());
      res.add(linkFilter);
    }

    if (fileInfo.format == null || fileInfo.format.equals(ATTR_FORMAT_VALUE_DITA)) {
      topicFilter.setFileInfo(fileInfo);
      res.add(topicFilter);
    } else if (fileInfo.format.equals(ATTR_FORMAT_VALUE_DITAMAP)) {
      mapFilter.setFileInfo(fileInfo);
      res.add(mapFilter);
    }

    for (final XmlFilterModule.FilterPair filterPair : filters) {
      if (filterPair.predicate.test(fileInfo)) {
        final AbstractXMLFilter xmlFilter = filterPair.newInstance();
        logger.debug("Configure filter " + xmlFilter.getClass().getCanonicalName());
        xmlFilter.setCurrentFile(srcFile.toURI());
        xmlFilter.setJob(job);
        xmlFilter.setLogger(logger);
        res.add(xmlFilter);
      }
    }

    return res;
  }
}
