/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2016 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */

package org.dita.dost.module;

import com.google.common.annotations.VisibleForTesting;
import net.sf.saxon.s9api.*;
import net.sf.saxon.trans.UncheckedXPathException;
import org.apache.commons.io.FileUtils;
import org.apache.xml.resolver.tools.CatalogResolver;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.util.CatalogUtils;
import org.dita.dost.util.Job;
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.util.URLUtils;
import org.dita.dost.util.XMLUtils;
import org.dita.dost.writer.AbstractXMLFilter;
import org.dita.dost.writer.LinkFilter;
import org.dita.dost.writer.MapCleanFilter;
import org.w3c.dom.Document;
import org.xml.sax.XMLFilter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Collections.emptyMap;
import static org.dita.dost.util.Constants.ATTR_FORMAT_VALUE_DITA;
import static org.dita.dost.util.Constants.ATTR_FORMAT_VALUE_DITAMAP;
import static org.dita.dost.util.XMLUtils.toErrorListener;

/**
 * Move temporary files not based on output URI to match output URI structure.
 *
 * @since 2.5
 */
public class CleanPreprocessModule extends AbstractPipelineModuleImpl {

    private static final String PARAM_USE_RESULT_FILENAME = "use-result-filename";

    private final LinkFilter filter = new LinkFilter();
    private final MapCleanFilter mapFilter = new MapCleanFilter();

    private boolean useResultFilename;
    private XsltTransformer rewriteTransformer;
    private RewriteRule rewriteClass;

    private void init(final Map<String, String> input) {
        useResultFilename = Optional.ofNullable(input.get(PARAM_USE_RESULT_FILENAME))
                .map(Boolean::parseBoolean)
                .orElse(false);
        final CatalogResolver catalogResolver = CatalogUtils.getCatalogResolver();
        rewriteTransformer = Optional.ofNullable(input.get("result.rewrite-rule.xsl"))
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
                        xsltCompiler.setErrorListener(toErrorListener(logger));
                        final XsltExecutable xsltExecutable = xsltCompiler.compile(new StreamSource(f.toString()));
                        return xsltExecutable.load();
                    } catch (UncheckedXPathException e) {
                        throw new RuntimeException("Failed to compile XSLT: " + e.getXPathException().getMessageAndLocation(), e);
                    } catch (SaxonApiException e) {
                        throw new RuntimeException("Failed to compile XSLT: " + e.getMessage(), e);
                    }
                })
                .orElse(null);
        rewriteClass = Optional.ofNullable(input.get("result.rewrite-rule.class"))
                .map(c -> {
                    try {
                        final Class<RewriteRule> cls = (Class<RewriteRule>) Class.forName(c);
                        return cls.newInstance();
                    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                })
                .orElse(null);

        filter.setLogger(logger);

        mapFilter.setLogger(logger);
    }

    @Override
    public AbstractPipelineOutput execute(final Map<String, String> input) throws DITAOTException {
        init(input);
        final URI base = getBaseDir();
        if (useResultFilename) {
            // collect and relativize result
            final Collection<FileInfo> original = job.getFileInfo().stream()
                    .filter(fi -> fi.result != null)
                    .map(fi -> FileInfo.builder(fi).result(base.relativize(fi.result)).build())
                    .collect(Collectors.toList());
            original.forEach(fi -> job.remove(fi));
            // rewrite results
            final Collection<FileInfo> rewritten = rewrite(original);
            // move temp files and update links
            final Job tempJob = new Job(job, emptyMap(), rewritten);
            filter.setJob(tempJob);
            mapFilter.setJob(tempJob);
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
                    final FileInfo res = FileInfo.builder(fi)
                            .uri(fi.result)
                            .result(base.resolve(fi.result))
                            .build();
                    job.add(res);
                } catch (final IOException e) {
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
        }

        try {
            job.write();
        } catch (IOException e) {
            throw new DITAOTException();
        }

        return null;
    }

    private Collection<FileInfo> rewrite(final Collection<FileInfo> fis) throws DITAOTException {
        if (rewriteClass != null) {
            return rewriteClass.rewrite(fis);
        }
        if (rewriteTransformer != null) {
            try {
                final DOMSource source = new DOMSource(serialize(fis));
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
        return fis;
    }

    private Document serialize(final Collection<FileInfo> fis) throws IOException {
        try {
            final Document doc = XMLUtils.getDocumentBuilder().newDocument();
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
        return IntStream.range(0, count).boxed()
                .map(i -> "../")
                .collect(Collectors.joining(""));

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
                final String path = Arrays.asList(la)
                        .subList(0, common)
                        .stream()
                        .collect(Collectors.joining("/")) + "/";
                return URLUtils.setPath(left, path);
            }
        }
        return null;
    }

    private void init() {
        filter.setJob(job);
        filter.setLogger(logger);

        mapFilter.setJob(job);
        mapFilter.setLogger(logger);
    }

    private List<XMLFilter> getProcessingPipe(final FileInfo fi, final File srcFile, final File destFile) {
        final List<XMLFilter> res = new ArrayList<>();

        if (fi.format == null || fi.format.equals(ATTR_FORMAT_VALUE_DITA) || fi.format.equals(ATTR_FORMAT_VALUE_DITAMAP)) {
            filter.setCurrentFile(srcFile.toURI());
            filter.setDestFile(destFile.toURI());
            res.add(filter);
        }

        if (fi.format != null && fi.format.equals(ATTR_FORMAT_VALUE_DITAMAP)) {
            res.add(mapFilter);
        }

        for (final XmlFilterModule.FilterPair p : filters) {
            if (p.predicate.test(fi)) {
                final AbstractXMLFilter f = p.filter;
                logger.debug("Configure filter " + f.getClass().getCanonicalName());
                f.setCurrentFile(srcFile.toURI());
                f.setJob(job);
                f.setLogger(logger);
                res.add(f);
            }
        }

        return res;
    }

}
