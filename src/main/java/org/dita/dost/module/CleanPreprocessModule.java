/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2016 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.module;

import static java.util.Collections.emptyMap;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.Job.USER_INPUT_FILE_LIST_FILE;
import static org.dita.dost.util.XMLUtils.toErrorReporter;

import com.google.common.annotations.VisibleForTesting;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
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
import org.dita.dost.util.Job;
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.util.URLUtils;
import org.dita.dost.util.XMLUtils;
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
  }

  @Override
  public AbstractPipelineOutput execute(final Map<String, String> input) throws DITAOTException {
    init(input);
    final URI base = getBaseDir();
    if (useResultFilename) {
      // collect and relativize result
      final Collection<FileInfo> original = job
        .getFileInfo()
        .stream()
        .filter(fi -> fi.result != null)
        .map(fi -> FileInfo.builder(fi).result(URI.create(Paths.get(base).relativize(Path.of(fi.result)).toString().replace("\\", "/"))).build())
        .collect(Collectors.toList());
      original.forEach(fi -> job.remove(fi));
      // rewrite results
      final Collection<FileInfo> rewritten = rewrite(original);
      // move temp files and update links
      final Job tempJob = new Job(job, emptyMap(), rewritten);
      linkFilter.setJob(tempJob);
      mapFilter.setJob(tempJob);
      topicFilter.setJob(tempJob);
      for (final FileInfo fi : rewritten) {
        try {
          assert !fi.result.isAbsolute();
          if (fi.format != null && (fi.format.equals("coderef") || fi.format.equals("image"))) {
            logger.debug("Skip format " + fi.format);
          } else {
            final File srcFile = new File(job.tempDirURI.resolve(fi.uri));
            if (job.getStore().exists(srcFile.toURI())) {
              final File destFile = new File(job.tempDirURI.resolve(fi.result));
              final List<XMLFilter> processingPipe = getProcessingPipe(fi, srcFile, destFile);
              if (!processingPipe.isEmpty()) {
                logger.info("Processing " + srcFile.toURI() + " to " + destFile.toURI());
                job.getStore().transform(srcFile.toURI(), destFile.toURI(), processingPipe);
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
          final FileInfo res = FileInfo.builder(fi).uri(fi.result).result(base.resolve(fi.result)).build();
          job.add(res);
        } catch (final IOException | AssertionError e) {
          logger.error("Failed to clean " + job.tempDirURI.resolve(fi.uri) + ": " + e.getMessage(), e);
        }
      }
    }

    job.setProperty("uplevels", getUplevels(base));
    job.setInputDir(base);

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

    try {
      job.write();
    } catch (IOException e) {
      throw new DITAOTException();
    }

    return null;
  }

  private Collection<FileInfo> rewrite(final Collection<FileInfo> fileInfoCollection) throws DITAOTException {
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

  /**
   * Get common base directory for all files
   */
  @VisibleForTesting
  URI getBaseDir() {
    final Collection<FileInfo> fis = job.getFileInfo();
    URI baseDir = job.getFileInfo(fi -> fi.isInput).iterator().next().result.resolve(".");
    for (final FileInfo fi : fis) {
      if (fi.result != null) {
        final URI res = fi.result.resolve(".");
        baseDir = Optional.ofNullable(getCommonBase(baseDir, res)).orElse(baseDir);
      }
    }

    return baseDir;
  }

  @VisibleForTesting
  URI getCommonBase(final URI left, final URI right) {
    assert left.isAbsolute();
    assert right.isAbsolute();
    if (!left.getScheme().equals(right.getScheme())) {
      return null;
    }
    final URI l = left.resolve(".");
    final URI r = right.resolve(".");
    final String lp = l.getPath();
    final String rp = r.getPath();
    if (lp.equals(rp)) {
      return l;
    }
    if (lp.startsWith(rp)) {
      return r;
    }
    if (rp.startsWith(lp)) {
      return l;
    }
    final String[] la = left.getPath().split("/");
    final String[] ra = right.getPath().split("/");
    int i = 0;
    final int len = Math.min(la.length, ra.length);
    for (; i < len; i++) {
      if (la[i].equals(ra[i])) {
        //
      } else {
        final int common = Math.max(0, i);
        final List<String> commons = Arrays.asList(la).subList(0, common);
        if (OS_NAME.toLowerCase().contains(OS_NAME_WINDOWS) && commons.size() <= 1) {
          return null;
        } else {
          final String path = String.join("/", commons) + "/";
          return URLUtils.setPath(left, path);
        }
      }
    }
    return null;
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
