package org.dita.dost.util;

import static org.dita.dost.util.URLUtils.*;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.AbstractFileSet;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.types.resources.URLResource;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import static org.dita.dost.util.Constants.ANT_REFERENCE_JOB;

public class JobSourceSet extends AbstractFileSet implements ResourceCollection {

    private String format;
    private Collection<Resource> res;
    private boolean isFilesystemOnly = true;

    public JobSourceSet() {
        super();
    }

    private Collection<Resource> getResults() {
        if (res == null) {
            if (format == null) {
                throw new IllegalStateException();
            }
            final Job job = getProject().getReference(ANT_REFERENCE_JOB);
            if (job == null) {
                throw new IllegalStateException();
            }
            res = new ArrayList<>();
            for (final Job.FileInfo f : job.getFileInfo(new Job.FileInfo.Filter() {
                @Override
                public boolean accept(final Job.FileInfo f) {
                    return f.format != null && f.format.equals(format);
                }
            })) {
                log("Scanning for " + f.file.getPath(), Project.MSG_VERBOSE);
                final File tempFile = new File(job.tempDir, f.file.getPath());
                if (tempFile.exists()) {
                    log("Found temporary directory file " + tempFile, Project.MSG_VERBOSE);
                    res.add(new FileResource(job.tempDir, f.file.toString()));
                } else if (f.src.getScheme().equals("file")) {
                    final File srcFile = new File(f.src);
                    if (srcFile.exists()) {
                        log("Found source directory file " + srcFile, Project.MSG_VERBOSE);
                        res.add(new FileResource(toFile(job.getInputDir()), f.file.toString()));
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
        this.format = format;
    }

    private static class JobResource extends URLResource {
        private String relPath;
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
                return ((Resource) getCheckedRef()).getName();
            }
            return relPath;
        }
    }

}
