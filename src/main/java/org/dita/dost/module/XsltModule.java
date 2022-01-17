/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2013 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.module;

import net.sf.saxon.s9api.*;
import net.sf.saxon.trans.UncheckedXPathException;
import net.sf.saxon.trans.XPathException;
import org.apache.tools.ant.types.XMLCatalog;
import org.apache.tools.ant.util.FileNameMapper;
import org.apache.xml.resolver.tools.CatalogResolver;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.exception.UncheckedDITAOTException;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.util.CatalogUtils;
import org.dita.dost.util.DelegatingURIResolver;
import org.dita.dost.util.Job;

import javax.xml.transform.Source;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.AbstractMap.SimpleEntry;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static org.dita.dost.util.Constants.FILE_EXTENSION_TEMP;
import static org.dita.dost.util.FileUtils.replaceExtension;
import static org.dita.dost.util.XMLUtils.toErrorReporter;
import static org.dita.dost.util.XMLUtils.toMessageListener;

/**
 * XSLT processing module.
 *
 * <p>The module matches Ant's XSLT task with the following exceptions:</p>
 * <ul>
 *   <li>If source and destination directories are same, transformation results are saved to a temporary file
 *   and the original source file is replaced after a successful transformation.</li>
 *   <li>If no {@code extension} attribute is set, the target file extension is the same as the source file extension.</li>
 * </ul>
 *
 */
public final class XsltModule extends AbstractPipelineModuleImpl {

    private XsltExecutable templates;
    private final Map<String, String> params = new HashMap<>();
    private final Properties properties = new Properties();
    private Source style;
    private File in;
    private File out;
    private File destDir;
    private File baseDir;
    private Collection<File> includes;
    private String filenameparameter;
    private String filedirparameter;
    private boolean reloadstylesheet;
    private URIResolver catalog;
    private URIResolver uriResolver;
    private FileNameMapper mapper;
    private String extension;
    private XsltTransformer t;
    private Processor processor;
    private boolean parallel;

    private void init() {
        if (catalog == null) {
            final CatalogResolver catalogResolver = CatalogUtils.getCatalogResolver();
            catalog = catalogResolver;
        }
        uriResolver = new DelegatingURIResolver(catalog, job.getStore());

        if (fileInfoFilter != null) {
            final Collection<Job.FileInfo> res = job.getFileInfo(fileInfoFilter);
            includes = new ArrayList<>(res.size());
            for (final Job.FileInfo f : res) {
                includes.add(f.file);
            }
            baseDir = job.tempDir;
        }
    }

    @Override
    public AbstractPipelineOutput execute(AbstractPipelineInput input) throws DITAOTException {
        init();
        if ((includes == null || includes.isEmpty()) && (in == null)) {
            return null;
        }

        if (destDir != null) {
            logger.info("Transforming into " + destDir.getAbsolutePath());
        }
        processor = xmlUtils.getProcessor();
        final XsltCompiler xsltCompiler = processor.newXsltCompiler();
        xsltCompiler.setURIResolver(uriResolver);
        xsltCompiler.setErrorReporter(toErrorReporter(logger));
        logger.info("Loading stylesheet " + style.getSystemId());
        try {
            templates = xsltCompiler.compile(style);
        } catch (SaxonApiException e) {
            throw new RuntimeException("Failed to compile stylesheet '" + style.getSystemId() + "': " + e.getMessage(), e);
        }
        if (in != null) {
            transform(in, out);
        } else if (parallel) {
            try {
                final List<Entry<File, File>> tmps = includes.stream().parallel()
                        .map(include -> {
                            try {
                                final File in = new File(baseDir, include.getPath());
                                final File out = getOutput(include.getPath());
                                if (out == null) {
                                    return null;
                                }
                                final XsltTransformer transformer = getTransformer();
                                if (in.equals(out)) {
                                    final File tmp = new File(out.getAbsolutePath() + FILE_EXTENSION_TEMP);
                                    transform(in, tmp, transformer);
                                    return new SimpleEntry<>(tmp, out);
                                } else {
                                    transform(in, out, transformer);
                                    return null;
                                }
                            } catch (DITAOTException e) {
                                throw new UncheckedDITAOTException(e);
                            }
                        })
                        .filter(entry -> entry != null)
                        .collect(Collectors.toList());
                for (Entry<File, File> entry : tmps) {
                    try {
                        logger.info("Move " + entry.getKey().toURI() + " to " + entry.getValue().toURI());
                        job.getStore().move(entry.getKey().toURI(), entry.getValue().toURI());
                    } catch (IOException e) {
                        logger.error(String.format("Failed to move %s to %s: %s", entry.getKey().toURI(), entry.getValue().toURI(), e.getMessage()), e);
                    }
                }
            } catch (UncheckedDITAOTException e) {
                throw e.getDITAOTException();
            }
        } else {
            for (final File include : includes) {
                final File in = new File(baseDir, include.getPath());
                final File out = getOutput(include.getPath());
                if (out == null) {
                    continue;
                }
                transform(in, out);
            }
        }
        return null;
    }

    private File getOutput(final String path) {
        File out = new File(destDir, path);
        if (mapper != null) {
            final String[] outs = mapper.mapFileName(path);
            if (outs == null) {
                return null;
            }
            if (outs.length > 1) {
                throw new RuntimeException("XSLT module only support one to one output mapping");
            }
            out = new File(destDir, outs[0]);
        } else if (extension != null) {
            out = new File(replaceExtension(out.getAbsolutePath(), extension));
        }
        return out;
    }

    private XsltTransformer getTransformer() throws DITAOTException {
        try {
            XsltTransformer transformer = templates.load();
//            final URIResolver resolver = Configuration.DEBUG
//                    ? new XMLUtils.DebugURIResolver(uriResolver)
//                    : uriResolver;
            transformer.setErrorReporter(toErrorReporter(logger));
            transformer.setURIResolver(uriResolver);
            transformer.setMessageListener(toMessageListener(logger));
            return transformer;
        } catch (final Exception e) {
            throw new DITAOTException("Failed to create Transformer: " + e.getMessage(), e);
        }
    }

    private void transform(final File in, final File out) throws DITAOTException {
        if (reloadstylesheet || t == null) {
            logger.info("Loading stylesheet " + style.getSystemId());
            t = getTransformer();
        }
        transform(in, out, t);
    }

    private void transform(final File in, final File out, final XsltTransformer t) throws DITAOTException {
        final boolean same = in.getAbsolutePath().equals(out.getAbsolutePath());
        for (Entry<String, String> e: params.entrySet()) {
            logger.debug("Set parameter " + e.getKey() + " to '" + e.getValue() + "'");
            t.setParameter(new QName(e.getKey()), new XdmAtomicValue(e.getValue()));
        }
        if (filenameparameter != null) {
            logger.debug("Set parameter " + filenameparameter + " to '" + in.getName() + "'");
            t.setParameter(new QName(filenameparameter), new XdmAtomicValue(in.getName()));
        }
        if (filedirparameter != null) {
            final Path rel = job.tempDir.toPath().relativize(in.getAbsoluteFile().toPath()).getParent();
            final String v = rel != null ? rel.toString() : ".";
            logger.debug("Set parameter " + filedirparameter + " to '" + v + "'");
            t.setParameter(new QName(filedirparameter), new XdmAtomicValue(v));
        }

        if (properties.isEmpty()) {
            try {
                if (same) {
                    logger.info("Processing " + in.toURI());
                    job.getStore().transform(in.toURI(), t);
                } else {
                    logger.info("Processing " + in.toURI() + " to " + out.toURI());
                    job.getStore().transform(in.toURI(), out.toURI(), t);
                }
            } catch (final UncheckedXPathException e) {
                logger.error("Failed to transform document: " + e.getXPathException().getMessageAndLocation(), e);
            } catch (final RuntimeException e) {
                throw e;
            } catch (final Exception e) {
                logger.error("Failed to transform document: " + e.getMessage(), e);
            }
            return;
        }

        final File tmp = same ? new File(out.getAbsolutePath() + ".tmp" + Long.toString(System.currentTimeMillis())) : out;
        if (same) {
            logger.info("Processing " + in.toURI());
            logger.debug("Processing " + in.toURI() + " to " + tmp.toURI());
        } else {
            logger.info("Processing " + in.toURI() + " to " + tmp.toURI());
        }
        try {
            final Source source = job.getStore().getSource(in.toURI());
            t.setSource(source);
            final Destination destination = job.getStore().getDestination(tmp.toURI());
            if (same) {
                destination.setDestinationBaseURI(out.toURI());
            }
            if (destination instanceof Serializer) {
                final Serializer serializer = (Serializer) destination;
                for (final String key : properties.stringPropertyNames()) {
                    serializer.setOutputProperty(new QName(key), properties.getProperty(key));
                }
            }
            t.setDestination(destination);
            t.transform();
            if (same) {
                logger.debug("Moving " + tmp.getAbsolutePath() + " to " + out.getAbsolutePath());
                job.getStore().move(tmp.toURI(), out.toURI());
            }
        } catch (final UncheckedXPathException e) {
            logger.error("Failed to transform document: " + e.getXPathException().getMessageAndLocation(), e);
            logger.debug("Remove " + tmp.toURI());
            try {
                job.getStore().delete(tmp.toURI());
            } catch (final IOException e1) {
                logger.error("Failed to clean up after failed transformation: " + e1, e1);
            }
        } catch (final RuntimeException e) {
            throw e;
        } catch (final SaxonApiException e) {
            try {
                throw e.getCause();
            } catch (final XPathException cause) {
                logger.error("Failed to transform document: " + cause.getMessageAndLocation(), e);
            } catch (Throwable throwable) {
                logger.error("Failed to transform document: " + e.getMessage(), e);
            }
            logger.debug("Remove " + tmp.toURI());
            try {
                job.getStore().delete(tmp.toURI());
            } catch (final IOException e1) {
                logger.error("Failed to clean up after failed transformation: " + e1, e1);
            }
        } catch (final Exception e) {
            logger.error("Failed to transform document: " + e.getMessage(), e);
            logger.debug("Remove " + tmp.toURI());
            try {
                job.getStore().delete(tmp.toURI());
            } catch (final IOException e1) {
                logger.error("Failed to clean up after failed transformation: " + e1, e1);
            }
        }
    }

    /**
     * @deprecated use {@link #setStyle(Source)} instead
     */
    @Deprecated
    public void setStyle(final File style) {
        this.style = new StreamSource(style);
    }

    public void setStyle(final Source style) {
        this.style = style;
    }

    public void setParam(final String key, final String value) {
        params.put(key, value);
    }

    public void setOutputProperty(final String name, final String value) {
        properties.setProperty(name, value);
    }

    public void setIncludes(final Collection<File> includes) {
        this.includes = includes;
    }

    public void setDestinationDir(final File destDir) {
        this.destDir = destDir;
    }

    public void setSorceDir(final File baseDir) {
        this.baseDir = baseDir;
    }

    public void setFilenameParam(final String filenameparameter) {
        this.filenameparameter = filenameparameter;
    }

    public void setFiledirParam(final String filedirparameter) {
        this.filedirparameter = filedirparameter;
    }

    public void setReloadstylesheet(final boolean reloadstylesheet) {
        this.reloadstylesheet = reloadstylesheet;
    }

    public void setSource(final File in) {
        this.in = in;
    }

    public void setResult(final File out) {
        this.out = out;
    }

    public void setXMLCatalog(final XMLCatalog xmlcatalog) {
        this.catalog = xmlcatalog;
    }

    public void setMapper(final FileNameMapper mapper) {
        this.mapper = mapper;
    }

    public void setExtension(final String extension) {
        this.extension = extension.startsWith(".") ? extension : ("." + extension);
    }

    public void setParallel(final boolean parallel) {
        this.parallel = parallel;
    }

}
