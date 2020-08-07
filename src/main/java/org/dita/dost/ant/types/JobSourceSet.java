/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2015 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.ant.types;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.FileUtils.supportedImageExtensions;
import static org.dita.dost.util.URLUtils.*;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.AbstractFileSet;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.types.resources.URLResource;
import org.dita.dost.ant.ExtensibleAntInvoker;
import org.dita.dost.store.ant.types.StoreResource;
import org.dita.dost.util.Constants;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.Job;
import org.dita.dost.util.Job.FileInfo;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Resource collection that finds matching resources from job configuration.
 */
public class JobSourceSet extends AbstractFileSet implements ResourceCollection {

    private SelectorElem include;
    private List<SelectorElem> includes;
    private List<SelectorElem> excludes;
    private Collection<Resource> res;
    private boolean isFilesystemOnly = true;

    public JobSourceSet() {
        super();
        include = new SelectorElem();
        includes = new ArrayList<>();
        excludes = new ArrayList<>();
    }

    private Collection<Resource> getResults() {
        if (res == null) {
            if (!include.isEmpty()) {
                includes.add(include);
            }
            final Job job = getJob();
            res = new ArrayList<>();
            for (final FileInfo f : job.getFileInfo(this::filter)) {
                log("Scanning for " + f.file.getPath(), Project.MSG_VERBOSE);
                final File tempFile = new File(job.tempDir, f.file.getPath());
                if (job.getStore().exists(tempFile.toURI())) {
                    log("Found temporary directory file " + tempFile, Project.MSG_VERBOSE);
                    res.add(new StoreResource(job, job.tempDirURI.relativize(f.uri)));
                } else if (f.src.getScheme().equals("file")) {
                    final File srcFile = new File(f.src);
                    if (srcFile.exists()) {
                        log("Found source directory file " + srcFile, Project.MSG_VERBOSE);
                        final File rel = FileUtils.getRelativePath(new File(new File(job.getInputDir()), "dummy"), srcFile);
                        res.add(new FileResource(toFile(job.getInputDir()), rel.getPath()));
                    } else {
                        log("File " + f.src + " not found", Project.MSG_ERR);
                    }
                } else if (f.src.getScheme().equals("data")) {
                    log("Ignore data URI", Project.MSG_VERBOSE);
                } else {
                    log("Found source URI " + f.src.toString(), Project.MSG_VERBOSE);
                    try {
                        final JobResource r = new JobResource(job.getInputDir().toURL(), f.uri.toString());
                        res.add(r);
                    } catch (final MalformedURLException e) {
                        throw new IllegalArgumentException(e);
                    }
                    isFilesystemOnly = false;
                }
            }
        }
        return res;
    }

    @VisibleForTesting
    Job getJob() {
        final Job job = ExtensibleAntInvoker.getJob(getProject());
        if (job == null) {
            throw new IllegalStateException();
        }
        return job;
    }

    @Override
    public Iterator<Resource> iterator() {
        return getResults().iterator();
    }

    @Override
    public int size() {
        return getResults().size();
    }

    @Override
    public boolean isFilesystemOnly() {
        getResults();
        return isFilesystemOnly;
    }

    public void setFormat(final String format) {
        include.setFormat(format);
    }

    public void setConref(final boolean conref) {
        include.setConref(conref);
    }

    public void setInput(final boolean isInput) {
        include.setInput(isInput);
    }

    public void setInputResource(final boolean isInputResource) {
        include.setInputResource(isInputResource);
    }

    public void setProcessingRole(final String processingRole) {
        include.setProcessingRole(processingRole);
    }

    public void addConfiguredIncludes(final SelectorElem include) {
        includes.add(include);
    }

    public void addConfiguredExcludes(final SelectorElem exclude) {
        excludes.add(exclude);
    }

    @VisibleForTesting
    public boolean filter(final FileInfo f) {
        for (final SelectorElem excl: excludes) {
            if (filter(f, excl)) {
                return false;
            }
        }
        if (includes.isEmpty()) {
            return true;
        } else {
            for (final SelectorElem incl: includes) {
                if (filter(f, incl)) {
                    return true;
                }
            }
            return false;
        }
    }

    private boolean filter(final FileInfo f, final SelectorElem incl) {
        final String format = f.format != null ? f.format : ATTR_FORMAT_VALUE_DITA;
        return (incl.formats.isEmpty() || (incl.formats.contains(format))) &&
                (incl.hasConref == null || f.hasConref == incl.hasConref) &&
                (incl.isInput == null || f.isInput == incl.isInput) &&
                (incl.isInputResource == null || f.isInputResource == incl.isInputResource) &&
                (incl.isResourceOnly == null || f.isResourceOnly == incl.isResourceOnly);
    }

    private static class JobResource extends URLResource {
        private final String relPath;
        public JobResource(final URL baseURL, final String relPath) {
            super();
            setBaseURL(baseURL);
            setRelativePath(relPath);
            this.relPath = relPath;
        }
        /**
         * Get the name of this URLResource with original relative path.
         *
         * URLResource will return full URL file part that also contains ancestor directories.
         **/
        @Override
        public synchronized String getName() {
            if (isReference()) {
                return getCheckedRef(getClass(), getDataTypeName(), getProject()).getName();
            }
            return relPath;
        }
    }

    public static class SelectorElem {
        private Set<String> formats = Collections.emptySet();
        private Boolean hasConref;
        private Boolean isInput;
        private Boolean isInputResource;
        private Boolean isResourceOnly;

        public SelectorElem() {
        }

        public SelectorElem(Set<String> formats, Boolean hasConref, Boolean isInput,
                            Boolean isInputResource, Boolean isResourceOnly) {
            this.formats = formats != null ? formats : Collections.emptySet();
            this.hasConref = hasConref;
            this.isInput = isInput;
            this.isInputResource = isInputResource;
            this.isResourceOnly = isResourceOnly;
        }

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

        public boolean isEmpty() {
            return formats.isEmpty() &&
                    hasConref == null &&
                    isInput == null &&
                    isInputResource == null &&
                    isResourceOnly == null;
        }
    }
}
