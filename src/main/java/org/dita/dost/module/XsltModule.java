/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2013 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.module;

import static org.dita.dost.util.FileUtils.replaceExtension;
import static org.dita.dost.util.XMLUtils.withLogger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;

import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.tools.ant.types.XMLCatalog;
import org.apache.tools.ant.util.FileNameMapper;
import org.apache.tools.ant.util.FileUtils;
import org.apache.xml.resolver.tools.CatalogResolver;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.module.saxon.DelegatingCollationUriResolver;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.util.CatalogUtils;
import org.dita.dost.util.Configuration;
import org.dita.dost.util.Job;
import org.dita.dost.util.XMLUtils;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import net.sf.saxon.TransformerFactoryImpl;
import net.sf.saxon.lib.CollationURIResolver;
import net.sf.saxon.lib.ExtensionFunctionDefinition;

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

    private Templates templates;
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
    private EntityResolver entityResolver;
    private URIResolver uriResolver;
    private FileNameMapper mapper;
    private String extension;
    private Transformer t;
    private XMLReader parser;

    private void init() {
        if (entityResolver == null || uriResolver == null) {
            final CatalogResolver catalogResolver = CatalogUtils.getCatalogResolver();
            entityResolver = catalogResolver;
            uriResolver = catalogResolver;
        }

        if (fileInfoFilter != null) {
            final Collection<Job.FileInfo> res = job.getFileInfo(fileInfoFilter);
            includes = new ArrayList<>(res.size());
            for (final Job.FileInfo f : res) {
                includes.add(f.file);
            }
            baseDir = job.tempDir;
        }
    }

    public AbstractPipelineOutput execute(AbstractPipelineInput input) throws DITAOTException {
        init();
        if ((includes == null || includes.isEmpty()) && (in == null)) {
            return null;
        }

        if (destDir != null) {
            logger.info("Transforming into " + destDir.getAbsolutePath());
        }
        final TransformerFactory tf = TransformerFactory.newInstance();
        configureExtensions(tf);
        configureCollationResolvers(tf);
        tf.setURIResolver(uriResolver);
        try {
            templates = tf.newTemplates(new StreamSource(style));
        } catch (TransformerConfigurationException e) {
            throw new RuntimeException("Failed to compile stylesheet '" + style.getAbsolutePath() + "': " + e.getMessage(), e);
        }
        try {
            parser = XMLUtils.getXMLReader();
        } catch (final SAXException e) {
            throw new RuntimeException("Failed to create XML reader: " + e.getMessage(), e);
        }
        parser.setEntityResolver(entityResolver);

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
                t = withLogger(templates.newTransformer(), logger);
                final URIResolver resolver = Configuration.DEBUG
                        ? new XMLUtils.DebugURIResolver(uriResolver)
                        : uriResolver;
                t.setURIResolver(resolver);
            } catch (final TransformerConfigurationException e) {
                throw new DITAOTException("Failed to create Transformer: " + e.getMessage(), e);
            }
            t.setOutputProperties(properties);
        }

        final boolean same = in.getAbsolutePath().equals(out.getAbsolutePath());
        final File tmp = same ? new File(out.getAbsolutePath() + ".tmp" + Long.toString(System.currentTimeMillis())) : out;
        for (Map.Entry<String, String> e: params.entrySet()) {
            logger.debug("Set parameter " + e.getKey() + " to '" + e.getValue() + "'");
            t.setParameter(e.getKey(), e.getValue());
        }
        if (filenameparameter != null) {
            logger.debug("Set parameter " + filenameparameter + " to '" + in.getName() + "'");
            t.setParameter(filenameparameter, in.getName());
        }
        if (filedirparameter != null) {
            final Path rel = job.tempDir.toPath().relativize(in.getAbsoluteFile().toPath()).getParent();
            final String v = rel != null ? rel.toString() : ".";
            logger.debug("Set parameter " + filedirparameter + " to '" + v + "'");
            t.setParameter(filedirparameter, v);
        }
        if (same) {
            logger.info("Processing " + in.getAbsolutePath());
            logger.debug("Processing " + in.getAbsolutePath() + " to " + tmp.getAbsolutePath());
        } else {
            logger.info("Processing " + in.getAbsolutePath() + " to " + tmp.getAbsolutePath());
        }
        final Source source = new SAXSource(parser, new InputSource(in.toURI().toString()));
        try {
            if (!tmp.getParentFile().exists() && !tmp.getParentFile().mkdirs()) {
                throw new IOException("Failed to create directory " + tmp.getParent());
            }
            t.transform(source, new StreamResult(tmp));
            if (same) {
                logger.debug("Moving " + tmp.getAbsolutePath() + " to " + out.getAbsolutePath());
                if (!out.delete()) {
                    throw new IOException("Failed to to delete input file " + out.getAbsolutePath());
                }
                if (!tmp.renameTo(out)) {
                    throw new IOException("Failed to to replace input file " + out.getAbsolutePath());
                }
            }
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            logger.error("Failed to transform document: " + e.getMessage(), e);
            logger.debug("Remove " + tmp.getAbsolutePath());
            FileUtils.delete(tmp);
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
        this.entityResolver = xmlcatalog;
        this.uriResolver = xmlcatalog;
    }

    public void setMapper(final FileNameMapper mapper) {
        this.mapper = mapper;
    }

    public void setExtension(final String extension) {
        this.extension = extension.startsWith(".") ? extension : ("." + extension);
    }

    private void configureExtensions (TransformerFactory tf) {
        if (tf.getClass().isAssignableFrom(net.sf.saxon.TransformerFactoryImpl.class)
            || tf instanceof net.sf.saxon.TransformerFactoryImpl
            ) {
            configureSaxonExtensions((net.sf.saxon.TransformerFactoryImpl) tf);
        }
    }

    /**
     * Registers Saxon full integrated function definitions.
     * The intgrated function should be an instance of net.sf.saxon.lib.ExtensionFunctionDefinition abstract class.
     * @see <a href="https://www.saxonica.com/html/documentation/extensibility/integratedfunctions/ext-full-J.html">Saxon
     *      Java extension functions: full interface</a>
     */
    private void configureSaxonExtensions(net.sf.saxon.TransformerFactoryImpl tfi) {
        final net.sf.saxon.Configuration conf = tfi.getConfiguration();
        for (ExtensionFunctionDefinition def : ServiceLoader.load(ExtensionFunctionDefinition.class)) {
            try {
                conf.registerExtensionFunction(def.getClass().newInstance());
            } catch (InstantiationException e) {
                throw new RuntimeException("Failed to register " + def.getFunctionQName().getDisplayName()
                        + ". Cannot create instance of " + def.getClass().getName() + ": " + e.getMessage(), e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Registers collation URI resolvers.
     * 
     * @param tf The transformer factory to configure.
     */
    private void configureCollationResolvers(TransformerFactory tf) {
      if (tf.getClass().isAssignableFrom(net.sf.saxon.TransformerFactoryImpl.class)
          || tf instanceof net.sf.saxon.TransformerFactoryImpl
          ) {
          configureSaxonCollationResolvers((net.sf.saxon.TransformerFactoryImpl) tf);
      }
      
    }

    private void configureSaxonCollationResolvers(TransformerFactoryImpl tf) {
      final net.sf.saxon.Configuration conf = tf.getConfiguration();
      for (DelegatingCollationUriResolver resolver : ServiceLoader.load(DelegatingCollationUriResolver.class)) {
          try {
            DelegatingCollationUriResolver newResolver = resolver.getClass().newInstance();
            CollationURIResolver currentResolver = conf.getCollationURIResolver();
              if (currentResolver != null) {
                newResolver.setBaseResolver(currentResolver);
              }
              conf.setCollationURIResolver(newResolver);
          } catch (InstantiationException e) {
            throw new RuntimeException("Failed to register " + resolver.getClass().getSimpleName()
                    + ". Cannot create instance of " + resolver.getClass().getName() + ": " + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
      }
      
    }

}
