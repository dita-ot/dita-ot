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
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.util.*;

import javax.xml.transform.Source;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import static org.dita.dost.util.FileUtils.replaceExtension;
import static org.dita.dost.util.XMLUtils.toErrorListener;

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
    private File style;
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
        xsltCompiler.setErrorListener(toErrorListener(logger));
        try {
            templates = xsltCompiler.compile(new StreamSource(style));
        } catch (SaxonApiException e) {
            throw new RuntimeException("Failed to compile stylesheet '" + style.getAbsolutePath() + "': " + e.getMessage(), e);
        }

        if (in != null) {
            transform(in, out);
        } else {
            for (final File include : includes) {
                final File in = new File(baseDir, include.getPath());
                File out = new File(destDir, include.getPath());
                if (mapper != null) {
                    final String[] outs = mapper.mapFileName(include.getPath());
                    if (outs == null) {
                        continue;
                    }
                    if (outs.length > 1) {
                        throw new RuntimeException("XSLT module only support one to one output mapping");
                    }
                    out = new File(destDir, outs[0]);
                } else if (extension != null) {
                    out = new File(replaceExtension(out.getAbsolutePath(), extension));
                }
                transform(in, out);
            }
        }
        return null;
    }

    private void transform(final File in, final File out) throws DITAOTException {
        if (reloadstylesheet || t == null) {
            logger.info("Loading stylesheet " + style.getAbsolutePath());
            try {
                t = templates.load();
//                final URIResolver resolver = Configuration.DEBUG
//                        ? new XMLUtils.DebugURIResolver(uriResolver)
//                        : uriResolver;
                t.setErrorListener(toErrorListener(logger));
                t.setURIResolver(uriResolver);
            } catch (final Exception e) {
                throw new DITAOTException("Failed to create Transformer: " + e.getMessage(), e);
            }
        }
        final boolean same = in.getAbsolutePath().equals(out.getAbsolutePath());
        for (Map.Entry<String, String> e: params.entrySet()) {
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
                    job.getStore().transform(in.toURI(), t);
                } else {
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
            logger.info("Processing " + in.getAbsolutePath());
            logger.debug("Processing " + in.getAbsolutePath() + " to " + tmp.getAbsolutePath());
        } else {
            logger.info("Processing " + in.getAbsolutePath() + " to " + tmp.getAbsolutePath());
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
            logger.debug("Remove " + tmp.getAbsolutePath());
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
            logger.debug("Remove " + tmp.getAbsolutePath());
            try {
                job.getStore().delete(tmp.toURI());
            } catch (final IOException e1) {
                logger.error("Failed to clean up after failed transformation: " + e1, e1);
            }
        } catch (final Exception e) {
            logger.error("Failed to transform document: " + e.getMessage(), e);
            logger.debug("Remove " + tmp.getAbsolutePath());
            try {
                job.getStore().delete(tmp.toURI());
            } catch (final IOException e1) {
                logger.error("Failed to clean up after failed transformation: " + e1, e1);
            }
        }
    }

    public void setStyle(final File style) {
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
}
