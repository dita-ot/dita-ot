/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2004, 2008 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.ant;

import com.google.common.collect.ImmutableSet;
import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.types.resources.Resources;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTAntLogger;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.module.AbstractPipelineModule;
import org.dita.dost.module.ModuleFactory;
import org.dita.dost.module.XmlFilterModule;
import org.dita.dost.module.XmlFilterModule.FilterPair;
import org.dita.dost.module.XsltModule;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.store.Store;
import org.dita.dost.store.StreamStore;
import org.dita.dost.util.Constants;
import org.dita.dost.util.Job;
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.util.XMLUtils;
import org.dita.dost.writer.AbstractXMLFilter;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.FileUtils.supportedImageExtensions;
import static org.dita.dost.util.URLUtils.toFile;

/**
 * Ant task for executing pipeline modules.
 *
 * @author Deborah Pickett
 */
public final class ExtensibleAntInvoker extends Task {

    private DITAOTAntLogger logger;
    private final ModuleFactory factory = ModuleFactory.instance();
    /**
     * Pipeline attributes and parameters
     */
    private final Map<String, String> attrs = new HashMap<>();
    /**
     * Nested params.
     */
    private final ArrayList<ParamElem> pipelineParams;
    /**
     * Nested modules.
     */
    private final ArrayList<ModuleElem> modules;
    /**
     * Temporary directory.
     */
    private File tempDir;

    /**
     * Constructor.
     */
    public ExtensibleAntInvoker() {
        super();
        pipelineParams = new ArrayList<>();
        modules = new ArrayList<>();

    }

    /**
     * Set message.
     *
     * @param m message
     */
    public void setMessage(final String m) {
        attrs.put("message", m);
    }

    /**
     * Set input map.
     *
     * @param inputmap input map file, may be relative or absolute
     */
    public void setInputmap(final String inputmap) {
        attrs.put(ANT_INVOKER_PARAM_INPUTMAP, inputmap);
    }

    /**
     * Set temporary directory.
     *
     * @param tempdir temporary directory
     */
    public void setTempdir(final File tempdir) {
        this.tempDir = tempdir.getAbsoluteFile();
        attrs.put(ANT_INVOKER_PARAM_TEMPDIR, tempdir.getAbsolutePath());
    }

    /**
     * Handle nested parameters. Add the key/value to the pipeline hash, unless
     * the "if" attribute is set and refers to a unset property.
     *
     * @return parameter
     */
    public ParamElem createParam() {
        final ParamElem p = new ParamElem();
        pipelineParams.add(p);
        return p;
    }

    /**
     * Handle nested module elements.
     *
     * @since 1.6
     */
    public void addConfiguredModule(final ModuleElem m) {
        modules.add(m);
    }

    public void addConfiguredXslt(final XsltElem xslt) {
        modules.add(xslt);
    }

    public void addConfiguredSax(final SaxPipeElem filters) {
//        filters.setProject(getProject());
        modules.add(filters);
    }

    private void initialize() throws BuildException {
        if (tempDir == null) {
            tempDir = new File(this.getProject().getProperty(ANT_TEMP_DIR));
            if (!tempDir.isAbsolute()) {
                tempDir = new File(this.getProject().getBaseDir(), tempDir.getPath());
            }
        }
        if (modules.isEmpty()) {
            throw new BuildException("ModuleElem must be specified");
        }
        attrs.computeIfAbsent(ANT_INVOKER_PARAM_BASEDIR, k -> getProject().getBaseDir().getAbsolutePath());
        for (final ParamElem p : pipelineParams) {
            if (!p.isValid()) {
                throw new BuildException("Incomplete parameter");
            }
            if (isValid(getProject(), getLocation(), p.getIf(), p.getUnless())) {
                attrs.put(p.getName(), p.getValue());
            }
        }
        logger = new DITAOTAntLogger(getProject());
        logger.setTask(this);
    }

    /**
     * Execution point of this invoker.
     *
     * @throws BuildException exception
     */
    @Override
    public void execute() throws BuildException {
        initialize();

        final Job job = getJob(getProject());
        final XMLUtils xmlUtils = getXmlUtils();

        try {
            for (final ModuleElem m : modules) {
                m.setProject(getProject());
                m.setLocation(getLocation());
                final PipelineHashIO pipelineInput = new PipelineHashIO();
                for (final Map.Entry<String, String> e : attrs.entrySet()) {
                    pipelineInput.setAttribute(e.getKey(), e.getValue());
                }
                AbstractPipelineModule mod = getPipelineModule(m, pipelineInput);
                long start = System.currentTimeMillis();
                mod.setLogger(logger);
                mod.setJob(job);
                mod.setXmlUtils(xmlUtils);
                mod.execute(pipelineInput);
                long end = System.currentTimeMillis();
                logger.debug("{0} processing took {1} ms", mod.getClass().getSimpleName(), end - start);
            }
        } catch (final DITAOTException e) {
            throw new BuildException("Failed to run pipeline: " + e.getMessage(), e);
        }
    }

    private AbstractPipelineModule getPipelineModule(final ModuleElem m, final PipelineHashIO pipelineInput) throws DITAOTException {
        if (m instanceof XsltElem) {
            final XsltElem xm = (XsltElem) m;
            if (xm.reloadstylesheet && xm.parallel) {
                throw new BuildException("Both reloadstylesheet and parallel cannot be true");
            }
            if (xm.parallel && xm.xmlcatalog != null) {
                throw new DITAOTException("Pipeline XSLT task with parallel=true cannot be used with Ant's xmlcatalog");
            }
            final XsltModule module = new XsltModule();
            module.setStyle(toSource(xm.xslResource));
            if (xm.in != null) {
                module.setSource(xm.in);
                module.setResult(xm.out);
            } else if (!xm.fileInfoFilters.isEmpty()) {
                module.setFileInfoFilter(combine(xm.fileInfoFilters));
                module.setDestinationDir(xm.destDir != null ? xm.destDir : tempDir);
            } else {
                final Set<File> inc = readListFile(xm.includes, logger);
                inc.removeAll(readListFile(xm.excludes, logger));
                module.setIncludes(inc);
                module.setDestinationDir(xm.destDir != null ? xm.destDir : xm.baseDir);
                module.setSorceDir(xm.baseDir);
            }
            module.setFilenameParam(xm.filenameparameter);
            module.setFiledirParam(xm.filedirparameter);
            module.setReloadstylesheet(xm.reloadstylesheet);
            module.setParallel(xm.parallel);
            module.setXMLCatalog(xm.xmlcatalog);
            if (xm.mapper != null) {
                module.setMapper(xm.mapper.getImplementation());
            }
            if (xm.extension != null) {
                module.setExtension(xm.extension);
            }
            for (final ParamElem p : m.params) {
                if (!p.isValid()) {
                    throw new BuildException("Incomplete parameter");
                }
                if (isValid(getProject(), getLocation(), p.getIf(), p.getUnless())) {
                    module.setParam(p.getName(), p.getValue());
                }
            }
            for (final OutputPropertyElem o : ((XsltElem) m).outputProperties) {
                if (!o.isValid()) {
                    throw new BuildException("Incomplete outputproperty");
                }
                module.setOutputProperty(o.name, o.value);
            }
            return module;
        } else if (m instanceof SaxPipeElem) {
            final SaxPipeElem fm = (SaxPipeElem) m;
            final XmlFilterModule module = new XmlFilterModule();
            module.setParallel(fm.parallel);
            final List<FileInfoFilterElem> predicates = new ArrayList<>(fm.getFormat());
            predicates.addAll(m.fileInfoFilters);
            module.setFileInfoFilter(combine(predicates));
            module.setProcessingPipe(fm.getFilters());
            return module;
        } else {
            for (final ParamElem p : m.params) {
                if (!p.isValid()) {
                    throw new BuildException("Incomplete parameter");
                }
                if (isValid(getProject(), getLocation(), p.getIf(), p.getUnless())) {
                    pipelineInput.setAttribute(p.getName(), p.getValue());
                }
            }
            final AbstractPipelineModule module = factory.createModule(m.getImplementation());
            module.setProcessingPipe(m.getFilters());
            if (!m.fileInfoFilters.isEmpty()) {
                module.setFileInfoFilter(combine(m.fileInfoFilters));
            }
            module.setParallel(m.parallel);
            return module;
        }
    }

    private Source toSource(final Resource style) {
        if (style instanceof FileResource) {
            return new StreamSource(((FileResource) style).getFile());
        } else {
            throw new BuildException(String.format("%s not supported", style.getClass().toString()));
        }
    }

    private static Predicate<FileInfo> combine(final Collection<FileInfoFilterElem> filters) {
        if (filters.isEmpty()) {
            return f -> true;
        }
        final List<Predicate<FileInfo>> res = filters.stream()
                .map(FileInfoFilterElem::toFilter)
                .collect(Collectors.toList());
        return f -> {
            for (final Predicate<FileInfo> filter : res) {
                if (filter.test(f)) {
                    return true;
                }
            }
            return false;
        };
    }

    /**
     * Get job configuration from Ant project reference or create new.
     *
     * @param project Ant project
     * @return job configuration
     */
    public static Job getJob(final Project project) {
        File tempDir = toFile(project.getUserProperty(ANT_TEMP_DIR));
        if (tempDir == null) {
            tempDir = toFile(project.getProperty(ANT_TEMP_DIR));
        }
        if (tempDir == null) {
            throw new IllegalStateException(String.format("Ant property %s not set", ANT_TEMP_DIR));
        }
        Job job = project.getReference(ANT_REFERENCE_JOB);
        if (job != null) {
            if (job.isStale()) {
                project.log("Reload stale job configuration reference", Project.MSG_ERR);
                try {
                    job = new Job(tempDir, job.getStore());
                } catch (final IOException ioe) {
                    throw new BuildException(ioe);
                }
                project.addReference(ANT_REFERENCE_JOB, job);
            }
        } else {
            XMLUtils xmlUtils = project.getReference(ANT_REFERENCE_XML_UTILS);
            if (xmlUtils == null) {
                project.log("XML utils not found from Ant project reference", Project.MSG_ERR);
                xmlUtils = new XMLUtils();
                xmlUtils.setLogger(new DITAOTAntLogger(project));
            }
            Store store = project.getReference(ANT_REFERENCE_STORE);
            if (store == null) {
                project.log("Store not found from Ant project reference", Project.MSG_ERR);
                store = new StreamStore(tempDir, xmlUtils);
            }
            project.log("Job not found from Ant project reference", Project.MSG_VERBOSE);
            try {
                job = new Job(tempDir, store);
            } catch (final IOException ioe) {
                throw new BuildException(ioe);
            }
            project.addReference(ANT_REFERENCE_JOB, job);
        }
        return job;
    }

    @Deprecated
    public static Job getJob(final File tempDir, final Project project) {
        return getJob(project);
    }

    /**
     * Get XML utils from Ant project reference or create new.
     *
     * @return XML utils
     */
    public XMLUtils getXmlUtils() {
        XMLUtils xmlUtils = getProject().getReference(ANT_REFERENCE_XML_UTILS);
        if (xmlUtils == null) {
            xmlUtils = new XMLUtils();
            xmlUtils.setLogger(logger);
            getProject().addReference(ANT_REFERENCE_XML_UTILS, xmlUtils);
        }
        return xmlUtils;
    }

    private Set<File> readListFile(final List<IncludesFileElem> includes, final DITAOTAntLogger logger) {
        final Set<File> inc = new HashSet<>();
        for (final IncludesFileElem i : includes) {
            if (!isValid(getProject(), getLocation(), i.ifProperty, null)) {
                continue;
            }
            try (BufferedReader r = new BufferedReader(new FileReader(i.file))) {
                for (String l = r.readLine(); l != null; l = r.readLine()) {
                    inc.add(new File(l));
                }
            } catch (IOException e) {
                logger.error("Failed to read includes file " + i.file + ": " + e.getMessage(), e);
            }
        }
        return inc;
    }

    public static boolean isValid(final Project project, final Location location, final String ifProperty, final String unlessProperty) {
        if (ifProperty != null) {
            final String msg = MessageUtils.getMessage("DOTA014W", "if", "if:set")
                    .setLocation(location)
                    .toString();
            project.log(msg, Project.MSG_WARN);
        }
        if (unlessProperty != null) {
            final String msg = MessageUtils.getMessage("DOTA014W", "unless", "unless:set")
                    .setLocation(location)
                    .toString();
            project.log(msg, Project.MSG_WARN);
        }
        return (ifProperty == null || project.getProperties().containsKey(ifProperty))
                && (unlessProperty == null || !project.getProperties().containsKey(unlessProperty));
    }

    /**
     * Nested pipeline module element configuration.
     *
     * @since 1.6
     */
    public static class ModuleElem {

        public final List<XmlFilterElem> filters = new ArrayList<>();
        public final List<ParamElem> params = new ArrayList<>();
        private Class<? extends AbstractPipelineModule> cls;
        public final Collection<FileInfoFilterElem> fileInfoFilters = new ArrayList<>();
        private Project project;
        private Location location;
        protected boolean parallel;

        public void setClass(final Class<? extends AbstractPipelineModule> cls) {
            this.cls = cls;
        }

        public void addConfiguredParam(final ParamElem p) {
            params.add(p);
        }

        public void addConfiguredDitaFileset(final FileInfoFilterElem fileInfoFilter) {
            fileInfoFilters.add(fileInfoFilter);
        }

        public void addConfiguredFilter(final XmlFilterElem filter) {
            filters.add(filter);
        }

        public List<FilterPair> getFilters() {
            return filters.stream()
                    .filter(f -> isValid(getProject(), getLocation(), f.getIf(), f.getUnless()))
                    .map(f -> {
                        final Map<String, String> params = f.params.stream()
                                .filter(p -> {
                                    if (!p.isValid()) {
                                        throw new IllegalArgumentException(new BuildException("Incomplete parameter"));
                                    }
                                    return isValid(getProject(), getLocation(), p.getIf(), p.getUnless());
                                })
                                .collect(Collectors.toMap(ParamElem::getName, ParamElem::getValue));
                        final List<FileInfoFilterElem> predicates = new ArrayList<>(f.fileInfoFilters);
//                            predicates.addAll(getFormat());
//                            assert !predicates.isEmpty();
                        Predicate<FileInfo> fs = combine(predicates);
                        return new FilterPair(f.getImplementation(), fs, params);
                    })
                    .collect(Collectors.toList());
        }

        public Class<? extends AbstractPipelineModule> getImplementation() {
            return cls;
        }

        public void setProject(final Project project) {
            this.project = project;
        }

        public Project getProject() {
            return project;
        }

        public void setLocation(final Location location) {
            this.location = location;
        }

        public Location getLocation() {
            return location;
        }

        public void setParallel(final boolean parallel) {
            this.parallel = parallel;
        }

    }

    /**
     * Nested pipeline XSLT element configuration.
     *
     * @author jelovirt
     */
    public static class XsltElem extends ModuleElem {

        private File baseDir;
        private File destDir;
        private File in;
        private File out;
        private final List<IncludesFileElem> includes = new ArrayList<>();
        private final List<IncludesFileElem> excludes = new ArrayList<>();
        private final List<OutputPropertyElem> outputProperties = new ArrayList<>();
        private Mapper mapper;
        private String extension;
        private String filenameparameter;
        private String filedirparameter;
        private XMLCatalog xmlcatalog;
        private boolean reloadstylesheet;
        private boolean parallel;
        private Resource xslResource;

        // Ant setters

        public void setStyle(final File style) {
            final FileResource fr = new FileResource();
            fr.setProject(getProject());
            fr.setFile(style);
            this.xslResource = fr;
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
            this.extension = extension;
        }

        public void setReloadstylesheet(final boolean reloadstylesheet) {
            this.reloadstylesheet = reloadstylesheet;
        }

        public void setParallel(final boolean parallel) {
            this.parallel = parallel;
        }

        public void setIn(final File in) {
            this.in = in;
        }

        public void setOut(final File out) {
            this.out = out;
        }

        public void setIncludesfile(final File includesfile) {
            final IncludesFileElem i = new IncludesFileElem();
            i.setName(includesfile);
            includes.add(i);
        }

        public void setExcludesfile(final File excludesfile) {
            final IncludesFileElem i = new IncludesFileElem();
            i.setName(excludesfile);
            excludes.add(i);
        }

        public void setFilenameparameter(final String filenameparameter) {
            this.filenameparameter = filenameparameter;
        }

        public void setFiledirparameter(final String filedirparameter) {
            this.filedirparameter = filedirparameter;
        }

        public void addConfiguredStyle(final Resources rc) {
            if (rc.size() != 1) {
                throw new BuildException("The style element must be specified with exactly one nested resource.");
            } else {
                this.xslResource = rc.iterator().next();
            }
        }

        public void addConfiguredXmlcatalog(final XMLCatalog xmlcatalog) {
            this.xmlcatalog = xmlcatalog;
        }

        public void addConfiguredMapper(final Mapper mapper) {
            if (this.mapper != null) {
                throw new BuildException("Cannot define more than one mapper");
            } else {
                this.mapper = mapper;
            }
        }

        public void addConfiguredIncludesFile(final IncludesFileElem includesFile) {
            includes.add(includesFile);
        }

        public void addConfiguredExcludesFile(final IncludesFileElem excludesFile) {
            excludes.add(excludesFile);
        }

        public void addOutputProperty(final OutputPropertyElem outputProperty) {
            outputProperties.add(outputProperty);
        }
    }

    public static class StyleElem extends ConfElem {
        // XXX This should be List<ResourceCollection>
        private List<FileSet> filesets = new ArrayList<>();

        public void addFileset(final FileSet fileset) {
            filesets.add(fileset);
        }
    }

    public static class OutputPropertyElem extends ConfElem {
        private String name;
        private String value;

        public void setName(final String name) {
            this.name = name;
        }

        public void setValue(final String value) {
            this.value = value;
        }

        public boolean isValid() {
            return (name != null && value != null);
        }
    }

    public static class IncludesFileElem extends ConfElem {
        private File file;

        public void setName(final File file) {
            this.file = file;
        }
    }

    public static class FileInfoFilterElem extends ConfElem {
        private Set<String> formats = Collections.emptySet();
        private Boolean hasConref;
        private Boolean isInput;
        private Boolean isInputResource;
        private Boolean isResourceOnly;

        public void setFormat(final String format) {
            final ImmutableSet.Builder<String> builder = ImmutableSet.<String>builder().add(format);
            if (format.equals(ATTR_FORMAT_VALUE_IMAGE)) {
                supportedImageExtensions.stream().map(ext -> ext.substring(1)).forEach(builder::add);
            }
            this.formats = builder.build();

        }

        public void setConref(final boolean conref) {
            this.hasConref = conref;
        }

        public void setInput(final boolean isInput) {
            this.isInput = isInput;
        }

        public void setInputResource(final boolean isInputResource) {
            this.isInputResource = isInputResource;
        }

        public void setProcessingRole(final String processingRole) {
            this.isResourceOnly = processingRole.equals(Constants.ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY);
        }

        public Predicate<FileInfo> toFilter() {
            return f -> (formats.isEmpty() || formats.contains(f.format != null ? f.format : ATTR_FORMAT_VALUE_DITA)) &&
                    (hasConref == null || f.hasConref == hasConref) &&
                    (isInput == null || f.isInput == isInput) &&
                    (isInputResource == null || f.isInputResource == isInputResource) &&
                    (isResourceOnly == null || f.isResourceOnly == isResourceOnly);
        }
    }

    /**
     * Nested pipeline SAX filter pipe element configuration.
     *
     * @author jelovirt
     */
    public static class SaxPipeElem extends ModuleElem {

        private List<String> format;

        // Ant setters

        public void setFormat(final String format) {
            this.format = Collections.singletonList(format);
        }

        @Override
        public List<FilterPair> getFilters() {
            return filters.stream()
                    .filter(f -> isValid(getProject(), getLocation(), f.getIf(), f.getUnless()))
                    .map(f -> {
                        final Map<String, String> params = f.params.stream()
                                .filter(p -> {
                                    if (!p.isValid()) {
                                        throw new IllegalArgumentException(new BuildException("Incomplete parameter"));
                                    }
                                    return isValid(getProject(), getLocation(), p.getIf(), p.getUnless());
                                })
                                .collect(Collectors.toMap(ParamElem::getName, ParamElem::getValue));
                        final List<FileInfoFilterElem> predicates = new ArrayList<>(f.fileInfoFilters);
                        predicates.addAll(getFormat());
                        assert !predicates.isEmpty();
                        Predicate<FileInfo> fs = combine(predicates);
                        return new FilterPair(f.getImplementation(), fs, params);
                    })
                    .collect(Collectors.toList());
        }

        public List<FileInfoFilterElem> getFormat() {
            return (format != null ? format : asList(ATTR_FORMAT_VALUE_DITA, ATTR_FORMAT_VALUE_DITAMAP)).stream()
                    .map(f -> {
                        final FileInfoFilterElem ff = new FileInfoFilterElem();
                        ff.setFormat(f);
                        return ff;
                    })
                    .collect(Collectors.toList());

        }
    }

    /**
     * Nested pipeline SAX filter element configuration.
     */
    public static class XmlFilterElem extends ConfElem {

        public final List<FileInfoFilterElem> fileInfoFilters = new ArrayList<>();
        public final List<ParamElem> params = new ArrayList<>();
        private Class<? extends AbstractXMLFilter> cls;

        public void setClass(final Class<? extends AbstractXMLFilter> cls) {
            this.cls = cls;
        }

        public void addConfiguredParam(final ParamElem p) {
            params.add(p);
        }

        public void addConfiguredDitaFileset(final FileInfoFilterElem fileInfoFilter) {
            fileInfoFilters.add(fileInfoFilter);
        }

        public Class<? extends AbstractXMLFilter> getImplementation() {
            if (cls == null) {
                throw new IllegalArgumentException("class not defined");
            }
            return cls;
        }

    }

    /**
     * Nested parameters.
     */
    public static class ParamElem extends ConfElem {

        private String name;
        private String value;
        private List<ResourceCollection> rcs = new ArrayList<>();

        /**
         * Get parameter name.
         *
         * @return parameter name, {@code null} if not set
         */
        public String getName() {
            return name;
        }

        /**
         * Validate that all required attributes have been set.
         *
         * @return isValid {@code true} is valid object, otherwise {@code false}
         */
        public boolean isValid() {
            return (name != null && (value != null || !rcs.isEmpty()));
        }

        /**
         * Set parameter name.
         *
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
            if (!rcs.isEmpty()) {
                return rcs.stream()
                        .flatMap(rc -> rc.stream())
                        .map(ParamElem::resourceToString)
                        .collect(Collectors.joining(File.pathSeparator));
            } else {
                return value;
            }
        }

        private static String resourceToString(final Resource r) {
            if (r instanceof FileResource) {
                return ((FileResource) r).getFile().getAbsolutePath();
            } else {
                throw new BuildException(String.format("%s not supported as param value", r.getClass()));
            }
        }

        /**
         * Set parameter value.
         *
         * @param v parameter value
         */
        public void setExpression(final String v) {
            value = v;
        }

        /**
         * Set parameter value.
         *
         * @param v parameter value
         */
        public void setValue(final String v) {
            value = v;
        }

        /**
         * Set parameter file value.
         *
         * @param v parameter file value
         */
        public void setLocation(final File v) {
            value = v.getPath();
        }

        /**
         * Add resource collection as value.
         * @param res resource collection
         */
        public void add(final ResourceCollection res) {
            rcs.add(res);
        }
    }

    /**
     * Add if and unless attributes to element. Use https://ant.apache.org/manual/ifunless.html instead.
     *
     * @deprecated since 3.0
     */
    @Deprecated
    public static abstract class ConfElem {

        String ifProperty;
        String unlessProperty;

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

}