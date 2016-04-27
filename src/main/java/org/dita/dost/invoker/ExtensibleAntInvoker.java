/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2008 All Rights Reserved.
 */
package org.dita.dost.invoker;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Mapper;
import org.apache.tools.ant.types.XMLCatalog;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTAntLogger;
import org.dita.dost.module.AbstractPipelineModule;
import org.dita.dost.module.XmlFilterModule;
import org.dita.dost.module.XsltModule;
import org.dita.dost.pipeline.PipelineFacade;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.util.Constants;
import org.dita.dost.util.Job;
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.writer.AbstractXMLFilter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static org.dita.dost.util.Constants.*;

/**
 * Ant task for executing pipeline modules.
 * 
 * @author Deborah Pickett
 */
public final class ExtensibleAntInvoker extends Task {
    
    /** Pipeline. */
    private final PipelineFacade pipeline;
    /** Pipeline attributes and parameters */
    private final Map<String, String> attrs = new HashMap<>();
    /** Nested params. */
    private final ArrayList<Param> pipelineParams;
    /** Nested modules. */
    private final ArrayList<Module> modules;
    /** Temporary directory. */
    private File tempDir;

    /**
     * Constructor.
     */
    public ExtensibleAntInvoker() {
        super();
        pipeline = new PipelineFacade();
        pipelineParams = new ArrayList<>();
        modules = new ArrayList<>();
    }

    /**
     * Set message.
     * @param m message
     */
    public void setMessage(final String m) {
        attrs.put("message", m);
    }

    /**
     * Set input map.
     * @param inputmap input map file, may be relative or absolute
     */
    public void setInputmap(final String inputmap) {
        attrs.put(ANT_INVOKER_PARAM_INPUTMAP, inputmap);
    }

    /**
     * Set temporary directory.
     * @param tempdir temporary directory
     */
    public void setTempdir(final File tempdir) {
        this.tempDir = tempdir.getAbsoluteFile();
        attrs.put(ANT_INVOKER_PARAM_TEMPDIR, tempdir.getAbsolutePath());
    }

    /**
     * Handle nested parameters. Add the key/value to the pipeline hash, unless
     * the "if" attribute is set and refers to a unset property.
     * @return parameter
     */
    public Param createParam() {
        final Param p = new Param();
        pipelineParams.add(p);
        return p;
    }

    /**
     * Handle nested module elements.
     * 
     * @since 1.6
     */
    public void addConfiguredModule(final Module m) {
        modules.add(m);
    }
    
    public void addConfiguredXslt(final Xslt xslt) {
        modules.add(xslt);
    }

    public void addConfiguredSax(final Filters filters) {
        filters.setProject(getProject());
        modules.add(filters);
    }

    private void initialize() throws BuildException {
        if (tempDir == null) {
            tempDir = new File(this.getProject().getProperty("dita.temp.dir")).getAbsoluteFile();
        }
        if (tempDir == null) {
            throw new BuildException("Temporary directory not set or available");
        }
        if (modules.isEmpty()) {
            throw new BuildException("Module must be specified");
        }
        if (attrs.get(ANT_INVOKER_PARAM_BASEDIR) == null) {
            attrs.put(ANT_INVOKER_PARAM_BASEDIR, getProject().getBaseDir().getAbsolutePath());
        }
        for (final Param p : pipelineParams) {
            if (!p.isValid()) {
                throw new BuildException("Incomplete parameter");
            }
            final String ifProperty = p.getIf();
            final String unlessProperty = p.getUnless();
            if ((ifProperty == null || getProject().getProperties().containsKey(ifProperty))
                    && (unlessProperty == null || !getProject().getProperties().containsKey(unlessProperty))) {
                attrs.put(p.getName(), p.getValue());
            }
        }
    }

    /**
     * Execution point of this invoker.
     * @throws BuildException exception
     */
    @Override
    public void execute() throws BuildException {
        initialize();
        
        long start, end;
        final DITAOTAntLogger logger = new DITAOTAntLogger(getProject());
        logger.setTask(this);
        pipeline.setLogger(logger);
        pipeline.setJob(getJob(tempDir, getProject()));
        try {
            for (final Module m: modules) {
                final PipelineHashIO pipelineInput = new PipelineHashIO();
                for (final Map.Entry<String, String> e: attrs.entrySet()) {
                    pipelineInput.setAttribute(e.getKey(), e.getValue());
                }
                if (m instanceof Xslt) {
                    final Xslt xm = (Xslt) m;
                    final XsltModule x = new XsltModule();
                    x.setStyle(xm.style);
                    if (xm.in != null) {
                        x.setSource(xm.in);
                        x.setResult(xm.out);
                    } else if (!xm.fileInfoFilters.isEmpty()) {
                        x.setFileInfoFilter(combine(xm.fileInfoFilters));
                        x.setDestinationDir(xm.destDir != null ? xm.destDir : tempDir);
                    } else {
                        final Set<File> inc = readListFile(xm.includes, logger);
                        inc.removeAll(readListFile(xm.excludes, logger));
                        x.setIncludes(inc);
                        x.setDestinationDir(xm.destDir != null ? xm.destDir : xm.baseDir);
                        x.setSorceDir(xm.baseDir);
                    }
                    x.setFilenameParam(xm.filenameparameter);
                    x.setFiledirParam(xm.filedirparameter);
                    x.setReloadstylesheet(xm.reloadstylesheet);
                    x.setXMLCatalog(xm.xmlcatalog);
                    if (xm.mapper != null) {
                        x.setMapper(xm.mapper.getImplementation());
                    }
                    for (final Param p : m.params) {
                        if (!p.isValid()) {
                            throw new BuildException("Incomplete parameter");
                        }
                        if (isValid(getProject(), p.getIf(), p.getUnless())) {
                            x.setParam(p.getName(), p.getValue());
                        }
                    }
                    start = System.currentTimeMillis();
                    pipeline.execute(x, pipelineInput);
                    end = System.currentTimeMillis();
                } else if (m instanceof Filters) {
                    final Filters fm = (Filters) m;
                    final XmlFilterModule module = new XmlFilterModule();
                    final List<String> format = fm.getFormat();
                    module.setFileInfoFilter(new FileInfo.Filter<FileInfo>() {
                             @Override
                             public boolean accept(final FileInfo f) {
                                 return format.contains(f.format);
                             }
                         }
                    );
                    try {
                        module.setProcessingPipe(fm.getFilters());
                    } catch (final InstantiationException | IllegalAccessException e) {
                        throw new BuildException(e);
                    }
                    start = System.currentTimeMillis();
                    pipeline.execute(module, pipelineInput);
                    end = System.currentTimeMillis();
                } else {
                    for (final Param p : m.params) {
                        if (!p.isValid()) {
                            throw new BuildException("Incomplete parameter");
                        }
                        final String ifProperty = p.getIf();
                        final String unlessProperty = p.getUnless();
                        if ((ifProperty == null || getProject().getProperties().containsKey(ifProperty))
                                && (unlessProperty == null || !getProject().getProperties().containsKey(unlessProperty))) {
                            pipelineInput.setAttribute(p.getName(), p.getValue());
                        }
                    }
                    start = System.currentTimeMillis();
                    pipeline.execute(m.getImplementation(), pipelineInput);
                    end = System.currentTimeMillis();
                }
                logger.debug("Module processing took " + (end - start) + " ms");
            }
        } catch (final DITAOTException e) {
            throw new BuildException("Failed to run pipeline: " + e.getMessage(), e);
        }
    }

    private FileInfo.Filter<FileInfo> combine(final Collection<Xslt.FileInfoFilter> filters) {
        final Collection<FileInfo.Filter<FileInfo>> res = new ArrayList<>(filters.size());
        for (final Xslt.FileInfoFilter f : filters) {
            res.add(f.toFilter());
        }
        return new FileInfo.Filter<FileInfo>() {
            @Override
            public boolean accept(FileInfo f) {
                for (final FileInfo.Filter<FileInfo> filter : res) {
                    if (filter.accept(f)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    /**
     * Get job configuration from Ant project reference or create new.
     *    
     * @param tempDir configuration directory 
     * @param project Ant project
     * @return job configuration
     */
    public static Job getJob(final File tempDir, final Project project) {
        Job job = project.getReference(ANT_REFERENCE_JOB);
        if (job != null && job.isStale(tempDir)) {
            project.log("Reload stale job configuration reference", Project.MSG_VERBOSE);
            job = null;
        }
        if (job == null) {
            try {
                job = new Job(tempDir);
            } catch (final IOException ioe) {
                throw new BuildException(ioe);
            }
            project.addReference(ANT_REFERENCE_JOB, job);
        }
        return job;
    }
    
    private Set<File> readListFile(final List<Xslt.IncludesFile> includes, final DITAOTAntLogger logger) {
    	final Set<File> inc = new HashSet<>();
    	for (final Xslt.IncludesFile i: includes) {
            if (!isValid(getProject(), i.ifProperty, null)) {
                continue;
            }
            BufferedReader r = null;
            try {
                r = new BufferedReader(new FileReader(i.file));
                for (String l = r.readLine(); l != null; l = r.readLine()) {
                    inc.add(new File(l));
                }
            } catch (IOException e) {
                logger.error("Failed to read includes file " + i.file + ": " + e.getMessage() , e);
            } finally {
                if (r != null) {
                    try {
                        r.close();
                    } catch (IOException e) {}
                }
            }
        }
    	return inc;
    }
    
    private static boolean isValid(final Project project, final String ifProperty, final String unlessProperty) {
        return (ifProperty == null || project.getProperties().containsKey(ifProperty))
                && (unlessProperty == null || !project.getProperties().containsKey(unlessProperty));
    }
    
    /**
     * Nested pipeline module element configuration.
     * 
     * @since 1.6
     */
    public static class Module {
       
        public final List<Param> params = new ArrayList<>();
        private Class<? extends AbstractPipelineModule> cls;
        
        public void setClass(final Class<? extends AbstractPipelineModule> cls) {
            this.cls = cls;
        }
        
        public void addConfiguredParam(final Param p) {
            params.add(p);
        }
        
        public Class<? extends AbstractPipelineModule> getImplementation() {
            return cls;
        }
    }
    
    /**
     * Nested pipeline XSLT element configuration.
     * @author jelovirt
     */
    public static class Xslt extends Module {
        
        private File style;
        private File baseDir;
        private File destDir;
        private File in;
        private File out;
        private final Collection<FileInfoFilter> fileInfoFilters = new ArrayList<>();
        private final List<IncludesFile> includes = new ArrayList<>();
        private final List<IncludesFile> excludes = new ArrayList<>();
        private Mapper mapper;
        private String filenameparameter;
        private String filedirparameter;
        private XMLCatalog xmlcatalog;
        private boolean reloadstylesheet;
        
        // Ant setters
        
        public void setStyle(final File style) {
            this.style = style;
        }
        
        public void setBasedir(final File baseDir) {
            this.baseDir = baseDir;
        }
        
        public void setDestdir(final File destDir) {
            this.destDir = destDir;
        }
        
        public void setTaskname(final String taskname) {
        }
        
        public void setClasspathref(final String classpath) {
        	// Ignore classpathref attribute
        }
        
        public void setExtension(final String extension) {
        	// Ignore extension attribute
        }
        
        public void setReloadstylesheet(final boolean reloadstylesheet) {
        	this.reloadstylesheet = reloadstylesheet;
        }
        
        public void setIn(final File in) {
        	this.in = in;
        }
        
        public void setOut(final File out) {
        	this.out = out;
        }
        
        public void setIncludesfile(final File includesfile) {
              final IncludesFile i = new IncludesFile();
              i.setName(includesfile);
              includes.add(i);
        }
        
        public void setExcludesfile(final File excludesfile) {
            final IncludesFile i = new IncludesFile();
            i.setName(excludesfile);
            excludes.add(i);
        }
        
        public void setFilenameparameter(final String filenameparameter) {
            this.filenameparameter = filenameparameter;
        }
        
        public void setFiledirparameter(final String filedirparameter) {
            this.filedirparameter = filedirparameter;
        }
                
        public void addConfiguredXmlcatalog(final XMLCatalog xmlcatalog) {
            this.xmlcatalog = xmlcatalog;
        }
        
        public void addConfiguredMapper(final Mapper mapper) {
            this.mapper = mapper;
        }

        public void addConfiguredDitaFileset(final FileInfoFilter fileInfoFilter) {
            fileInfoFilters.add(fileInfoFilter);
        }

        public void addConfiguredIncludesFile(final IncludesFile includesFile) {
            includes.add(includesFile);
        }
        
        public void addConfiguredExcludesFile(final IncludesFile excludesFile) {
            excludes.add(excludesFile);
        }
        
        public static class IncludesFile {
            private File file;
            private String ifProperty;
            public void setName(final File file) {
                this.file = file;
            }
            public void setIf(final String ifProperty) {
                this.ifProperty = ifProperty;
            }
        }

        public static class FileInfoFilter {
            private String format;
            private Boolean hasConref;
            private Boolean isResourceOnly;

            public void setFormat(final String format) {
                this.format = format;
            }

            public void setConref(final boolean conref) {
                this.hasConref = conref;
            }

            public void setProcessingRole(final String processingRole) {
                this.isResourceOnly = processingRole.equals(Constants.ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY);
            }

            public FileInfo.Filter<FileInfo> toFilter() {
                return new FileInfo.Filter<FileInfo>() {
                    @Override
                    public boolean accept(FileInfo f) {
                        return (format == null || format.equals(f.format)) &&
                                (hasConref == null || f.hasConref == hasConref) &&
                                (isResourceOnly == null || f.isResourceOnly == isResourceOnly);
                    }
                };
            }
        }
    }

    /**
     * Nested pipeline SAX filter pipe element configuration.
     * @author jelovirt
     */
    public static class Filters extends Module {

        private List<Filter> filters = new ArrayList<>();
        private Project project;
        private List<String> format;

        // Ant setters

        public void setFormat(final String format) {
            this.format = asList(format);
        }

        public void addConfiguredFilter(final Filter filter) {
            filters.add(filter);
        }

        public List<AbstractXMLFilter> getFilters() throws IllegalAccessException, InstantiationException {
            final List<AbstractXMLFilter> res = new ArrayList<>();
            for (final Filter f: filters) {
                final String ifProperty = f.getIf();
                final String unlessProperty = f.getUnless();
                if (isValid(getProject(), ifProperty, unlessProperty)) {
                    final AbstractXMLFilter fc = f.getImplementation().newInstance();
                    for (final Param p : f.params) {
                        if (!p.isValid()) {
                            throw new BuildException("Incomplete parameter");
                        }
                        if (isValid(getProject(), p.getIf(), p.getUnless())) {
                            fc.setParam(p.getName(), p.getValue());
                        }
                    }
                    res.add(fc);
                }
            }
            return res;
        }

        public List<String> getFormat() {
            if (format != null) {
                return unmodifiableList(format);
            } else {
                return unmodifiableList(asList(ATTR_FORMAT_VALUE_DITA, ATTR_FORMAT_VALUE_DITAMAP));
            }

        }

        public void setProject(final Project project) {
            this.project = project;
        }

        public Project getProject() {
            return project;
        }
    }

    /** Nested pipeline SAX filter element configuration. */
    public static class Filter {

        public final List<Param> params = new ArrayList<>();
        private Class<? extends AbstractXMLFilter> cls;
        private String ifProperty;
        private String unlessProperty;

        public void setClass(final Class<? extends AbstractXMLFilter> cls) {
            this.cls = cls;
        }

        public void addConfiguredParam(final Param p) {
            params.add(p);
        }

        public Class<? extends AbstractXMLFilter> getImplementation() {
            if (cls == null) {
                throw new IllegalArgumentException("class not defined");
            }
            return cls;
        }

        public String getIf() {
            return ifProperty;
        }

        public void setIf(final String p) {
            ifProperty = p;
        }

        public String getUnless() {
            return unlessProperty;
        }

        public void setUnless(final String p) {
            unlessProperty = p;
        }

    }

    /** Nested parameters. */
    public static class Param {

        private String name;
        private String value;
        private String ifProperty;
        private String unlessProperty;

        /**
         * Get parameter name.
         * @return parameter name, {@code null} if not set
         */
        public String getName() {
            return name;
        }

        /**
         * Validate that all required attributes have been set.
         * @return isValid {@code true} is valid object, otherwise {@code false}
         */
        public boolean isValid() {
            return (name != null && value != null);
        }

        /**
         * Set parameter name.
         * @param s name
         */
        public void setName(final String s) {
            name = s;
        }

        /**
         * Get parameter value.
         * @return parameter value, {@code null} if not set
         */
        public String getValue() {
            return value;
        }

        /**
         * Set parameter value.
         * @param v parameter value
         */
        public void setExpression(final String v) {
            value = v;
        }

        /**
         * Set parameter value.
         * @param v parameter value
         */
        public void setValue(final String v) {
            value = v;
        }

        /**
         * Set parameter file value.
         * @param v parameter file value
         */
        public void setLocation(final File v) {
            value = v.getPath();
        }

        /**
         * Get if condition property name
         * @return if condition parameter name, {@code null} if not set
         */
        public String getIf() {
            return ifProperty;
        }

        /**
         * Set if condition parameter name
         * @param p parameter name
         */
        public void setIf(final String p) {
            ifProperty = p;
        }

        /**
         * Get unless condition property name
         * @return unless condition parameter name, {@code null} if not set
         */
        public String getUnless() {
            return unlessProperty;
        }

        /**
         * Set unless condition parameter name
         * @param p parameter name
         */
        public void setUnless(final String p) {
            unlessProperty = p;
        }

    }

}