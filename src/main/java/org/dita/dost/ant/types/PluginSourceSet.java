/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2015 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.ant.types;

import org.apache.tools.ant.types.AbstractFileSet;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.FileResourceIterator;

import java.util.Iterator;

/**
 * Resource collection that finds matching resources from job configuration.
 */
public class PluginSourceSet extends AbstractFileSet implements ResourceCollection {

    private String pluginName;

    /**
     * Constructor for FileSet.
     */
    public PluginSourceSet() {
        super();
    }

    /**
     * Constructor for FileSet, with FileSet to shallowly clone.
     * @param fileset the fileset to clone
     */
    protected PluginSourceSet(FileSet fileset) {
        super(fileset);
    }

    /**
     * Return a FileSet that has the same basedir and same patternsets
     * as this one.
     * @return the cloned fileset
     */
    @Override
    public Object clone() {
        if (isReference()) {
            return ((FileSet) getRef(getProject())).clone();
        }
        return super.clone();
    }

    /**
     * Fulfill the ResourceCollection contract.
     * @return an Iterator of Resources.
     * @since Ant 1.7
     */
    @Override
    public Iterator<Resource> iterator() {
        if (isReference()) {
            return ((FileSet) getRef(getProject())).iterator();
        }
        return new FileResourceIterator(getProject(), getDir(getProject()),
                getDirectoryScanner(getProject()).getIncludedFiles());
    }

    /**
     * Fulfill the ResourceCollection contract.
     * @return number of elements as int.
     * @since Ant 1.7
     */
    @Override
    public int size() {
        if (isReference()) {
            return ((FileSet) getRef(getProject())).size();
        }
        return getDirectoryScanner(getProject()).getIncludedFilesCount();
    }

    /**
     * Always returns true.
     * @return true indicating that all elements will be FileResources.
     * @since Ant 1.7
     */
    @Override
    public boolean isFilesystemOnly() {
        return true;
    }

//
//    private Set<String> formats = Collections.emptySet();
//    private Boolean hasConref;
//    private Boolean isInput;
//    private Boolean isResourceOnly;
//    private Collection<Resource> res;
//    private boolean isFilesystemOnly = true;
//
//    public JobSourceSet() {
//        super();
//    }
//
//    private Collection<Resource> getResults() {
//        if (res == null) {
//            final Job job = getJob();
//            res = new ArrayList<>();
//            for (final FileInfo f : job.getFileInfo(this::filter)) {
//                log("Scanning for " + f.file.getPath(), Project.MSG_VERBOSE);
//                final File tempFile = new File(job.tempDir, f.file.getPath());
//                if (tempFile.exists()) {
//                    log("Found temporary directory file " + tempFile, Project.MSG_VERBOSE);
//                    res.add(new FileResource(job.tempDir, f.file.toString()));
//                } else if (f.src.getScheme().equals("file")) {
//                    final File srcFile = new File(f.src);
//                    if (srcFile.exists()) {
//                        log("Found source directory file " + srcFile, Project.MSG_VERBOSE);
//                        final File rel = FileUtils.getRelativePath(new File(new File(job.getInputDir()), "dummy"), srcFile);
//                        res.add(new FileResource(toFile(job.getInputDir()), rel.getPath()));
//                    } else {
//                        log("File " + f.src + " not found", Project.MSG_ERR);
//                    }
//                } else if (f.src.getScheme().equals("data")) {
//                    log("Ignore data URI", Project.MSG_VERBOSE);
//                } else {
//                    log("Found source URI " + f.src.toString(), Project.MSG_VERBOSE);
//                    try {
//                        final JobResource r = new JobResource(job.getInputDir().toURL(), f.uri.toString());
//                        res.add(r);
//                    } catch (final MalformedURLException e) {
//                        throw new IllegalArgumentException(e);
//                    }
//                    isFilesystemOnly = false;
//                }
//            }
//        }
//        return res;
//    }
//
//    private Job getJob() {
//        String tempDir = getProject().getUserProperty(ANT_TEMP_DIR);
//        if (tempDir == null) {
//            tempDir = getProject().getProperty(ANT_TEMP_DIR);
//        }
//        if (tempDir == null) {
//            throw new IllegalStateException();
//        }
//        final Job job = ExtensibleAntInvoker.getJob(new File(tempDir), getProject());
//        if (job == null) {
//            throw new IllegalStateException();
//        }
//        return job;
//    }
//
//    @Override
//    public Iterator<Resource> iterator() {
//        return getResults().iterator();
//    }
//
//    @Override
//    public int size() {
//        return getResults().size();
//    }
//
//    @Override
//    public boolean isFilesystemOnly() {
//        getResults();
//        return isFilesystemOnly;
//    }
//
//    public void setFormat(final String format) {
//        final ImmutableSet.Builder<String> builder = ImmutableSet.<String>builder().add(format);
//        if (format.equals(ATTR_FORMAT_VALUE_IMAGE)) {
//            supportedImageExtensions.stream().map(ext -> ext.substring(1)).forEach(builder::add);
//        }
//        this.formats = builder.build();
//    }
//
//    public void setConref(final boolean conref) {
//        this.hasConref = conref;
//    }
//
//    public void setInput(final boolean isInput) {
//        this.isInput = isInput;
//    }
//
//    public void setProcessingRole(final String processingRole) {
//        this.isResourceOnly = processingRole.equals(Constants.ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY);
//    }
//
//    private boolean filter(final FileInfo f) {
//        return (formats == null || (formats.contains(f.format)/* || (format.equals(ATTR_FORMAT_VALUE_DITA) && f.format == null)*/)) &&
//                (hasConref == null || f.hasConref == hasConref) &&
//                (isInput == null || f.isInput == isInput) &&
//                (isResourceOnly == null || f.isResourceOnly == isResourceOnly);
//    }
//
//    private static class JobResource extends URLResource {
//        private final String relPath;
//        public JobResource(final URL baseURL, final String relPath) {
//            super();
//            setBaseURL(baseURL);
//            setRelativePath(relPath);
//            this.relPath = relPath;
//        }
//        /**
//         * Get the name of this URLResource with original relative path.
//         *
//         * URLResource will return full URL file part that also contains ancestor directories.
//         **/
//        @Override
//        public synchronized String getName() {
//            if (isReference()) {
//                return getCheckedRef().getName();
//            }
//            return relPath;
//        }
//    }

}
