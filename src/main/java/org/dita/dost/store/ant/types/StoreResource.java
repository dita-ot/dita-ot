/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2020 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.store.ant.types;

import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.util.FileUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Objects;
import org.dita.dost.util.Job;

public class StoreResource extends Resource
//        implements
        //Touchable,
        //FileProvider,
        //Appendable,
//        ResourceFactory
{

    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    private static final int NULL_FILE = Resource.getMagicNumber("null file".getBytes());

    private URI file;
    private Job job;

    public StoreResource(Job job, URI file) {
        this.job = job;
        this.file = file;
    }

//    /**
//     * Construct a new StoreResource from a File.
//     * @param f the File represented.
//     */
//    public StoreResource(URI f) {
//        setFile(f);
//    }

//    /**
//     * Create a new StoreResource.
//     * @param p Project
//     * @param f File represented
//     * @since Ant 1.8
//     */
//    public StoreResource(Project p, File f) {
//        this(f);
//        setProject(p);
//    }

//    /**
//     * Constructor for Ant attribute introspection.
//     * @param p the Project against which to resolve <code>s</code>.
//     * @param s the absolute or Project-relative filename as a String.
//     * @see org.apache.tools.ant.IntrospectionHelper
//     */
//    public StoreResource(Project p, String s) {
//        this(p, p.resolveFile(s));
//    }

//    /**
//     * Set the File for this StoreResource.
//     * @param f the File to be represented.
//     */
//    public void setFile(File f) {
//        checkAttributesAllowed();
//        file = f;
//        if (f != null && (getBaseDir() == null || !FILE_UTILS.isLeadingPath(getBaseDir(), f))) {
//            setBaseDir(f.getParentFile());
//        }
//    }

    // FileProvider

//    /**
//     * Get the file represented by this StoreResource.
//     * @return the File.
//     */
//    @Override
//    public File getFile() {
//        if (isReference()) {
//            return getRef().getFile();
//        }
//        dieOnCircularReference();
//        synchronized (this) {
//            if (file == null) {
//                //try to resolve file set via basedir/name property setters:
//                File d = getBaseDir();
//                String n = super.getName();
//                if (n != null) {
//                    setFile(FILE_UTILS.resolveFile(d, n));
//                }
//            }
//        }
//        return file;
//    }

//    /**
//     * Set the basedir for this StoreResource.
//     * @param b the basedir as File.
//     */
//    public void setBaseDir(File b) {
//        checkAttributesAllowed();
//        baseDir = b;
//    }

//    /**
//     * Return the basedir to which the name is relative.
//     * @return the basedir as File.
//     */
//    public File getBaseDir() {
//        if (isReference()) {
//            return getRef().getBaseDir();
//        }
//        dieOnCircularReference();
//        return baseDir;
//    }

//    /**
//     * Overrides the super version.
//     * @param r the Reference to set.
//     */
//    @Override
//    public void setRefid(Reference r) {
//        if (file != null || baseDir != null) {
//            throw tooManyAttributes();
//        }
//        super.setRefid(r);
//    }

    /**
     * Get the name of this StoreResource.  If the basedir is set,
     * the name will be relative to that.  Otherwise the basename
     * only will be returned.
     * @return the name of this resource.
     */
    @Override
    public String getName() {
//        if (isReference()) {
//            return getRef().getName();
//        }
//        File b = getBaseDir();
//        return b == null ? getNotNullFile().getName()
//            : FILE_UTILS.removeLeadingPath(b, getNotNullFile());
        return file.getPath();
    }

    /**
     * Learn whether this file exists.
     * @return true if this resource exists.
     */
    @Override
    public boolean isExists() {
//        return isReference() ? getRef().isExists()
//            : getNotNullFile().exists();
        return job.getStore().exists(job.tempDirURI.resolve(file));
    }

    /**
     * Get the modification time in milliseconds since 01.01.1970 .
     * @return 0 if the resource does not exist.
     */
    @Override
    public long getLastModified() {
//        return isReference()
//            ? getRef().getLastModified()
//            : getNotNullFile().lastModified();
        // TODO: Add long lastModified entry to cache Store
        return -1;
    }

    /**
     * Learn whether the resource is a directory.
     * @return boolean flag indicating if the resource is a directory.
     */
    @Override
    public boolean isDirectory() {
//        return isReference() ? getRef().isDirectory()
//            : getNotNullFile().isDirectory();
        return false;
    }

    /**
     * Get the size of this Resource.
     * @return the size, as a long, 0 if the Resource does not exist.
     */
    @Override
    public long getSize() {
//        return isReference() ? getRef().getSize()
//            : getNotNullFile().length();
        return -1;
    }

    /**
     * Return an InputStream for reading the contents of this Resource.
     * @return an InputStream object.
     * @throws IOException if an error occurs.
     */
    @Override
    public InputStream getInputStream() throws IOException {
//        return isReference() ? getRef().getInputStream()
//            : Files.newInputStream(getNotNullFile().toPath());
        return job.getStore().getInputStream(job.tempDirURI.resolve(file));
    }

    /**
     * Get an OutputStream for the Resource.
     * @return an OutputStream to which content can be written.
     * @throws IOException if unable to provide the content of this
     *         Resource as a stream.
     * @throws UnsupportedOperationException if OutputStreams are not
     *         supported for this Resource type.
     */
    @Override
    public OutputStream getOutputStream() throws IOException {
//        if (isReference()) {
//            return getRef().getOutputStream();
//        }
//        return getOutputStream(false);
        return job.getStore().getOutputStream(job.tempDirURI.resolve(file));
    }

//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public OutputStream getAppendOutputStream() throws IOException {
//        if (isReference()) {
//            return getRef().getAppendOutputStream();
//        }
//        return getOutputStream(true);
//    }

//    private OutputStream getOutputStream(boolean append) throws IOException {
//        File f = getNotNullFile();
//        if (f.exists()) {
//            if (f.isFile() && !append) {
//                f.delete();
//            }
//        } else {
//            File p = f.getParentFile();
//            if (p != null && !(p.exists())) {
//                p.mkdirs();
//            }
//        }
//        return FileUtils.newOutputStream(f.toPath(), append);
//    }

    /**
     * Compare this StoreResource to another Resource.
     * @param another the other Resource against which to compare.
     * @return a negative integer, zero, or a positive integer as this StoreResource
     *         is less than, equal to, or greater than the specified Resource.
     */
    @Override
    public int compareTo(Resource another) {
//        if (isReference()) {
//            return getRef().compareTo(another);
//        }
//        if (this.equals(another)) {
//            return 0;
//        }
//        FileProvider otherFP = another.as(FileProvider.class);
//        if (otherFP != null) {
//            File f = getFile();
//            if (f == null) {
//                return -1;
//            }
//            File of = otherFP.getFile();
//            if (of == null) {
//                return 1;
//            }
//            int compareFiles = f.compareTo(of);
//            return compareFiles != 0 ? compareFiles
//                : getName().compareTo(another.getName());
//        }
//        return super.compareTo(another);
        // TODO add a way to compare resources
        return -1;
    }

    /**
     * Compare another Object to this StoreResource for equality.
     * @param another the other Object to compare.
     * @return true if another is a StoreResource representing the same file.
     */
    @Override
    public boolean equals(Object another) {
        if (this == another) {
            return true;
        }
//        if (isReference()) {
//            return getRef().equals(another);
//        }
        if (another == null || !(another.getClass().equals(getClass()))) {
            return false;
        }
        StoreResource otherfr = (StoreResource) another;
        return Objects.equals(file, otherfr.file);
//                getFile() == null
//            ? otherfr.getFile() == null
//            : getFile().equals(otherfr.getFile()) && getName().equals(otherfr.getName());
    }

    /**
     * Get the hash code for this Resource.
     * @return hash code as int.
     */
    @Override
    public int hashCode() {
//        if (isReference()) {
//            return getRef().hashCode();
//        }
//        return MAGIC * (getFile() == null ? NULL_FILE : getFile().hashCode());
        return file.hashCode();
    }

    /**
     * Get the string representation of this Resource.
     * @return this StoreResource formatted as a String.
     */
    @Override
    public String toString() {
//        if (isReference()) {
//            return getRef().toString();
//        }
//        if (file == null) {
//            return "(unbound file resource)";
//        }
//        String absolutePath = file.getAbsolutePath();
//        return FILE_UTILS.normalize(absolutePath).getAbsolutePath();
        return job.tempDirURI.resolve(file).toString();
    }

    /**
     * Fulfill the ResourceCollection contract.
     * @return whether this Resource is a StoreResource.
     */
    @Override
    public boolean isFilesystemOnly() {
//        if (isReference()) {
//            return getRef().isFilesystemOnly();
//        }
//        dieOnCircularReference();
        return true;
    }

//    /**
//     * Implement the Touchable interface.
//     * @param modTime new last modification time.
//     */
//    @Override
//    public void touch(long modTime) {
//        if (isReference()) {
//            getRef().touch(modTime);
//            return;
//        }
//        if (!getNotNullFile().setLastModified(modTime)) {
//            log("Failed to change file modification time", Project.MSG_WARN);
//        }
//    }

//    /**
//     * Get the file represented by this StoreResource, ensuring it is not null.
//     * @return the not-null File.
//     * @throws BuildException if file is null.
//     */
//    protected File getNotNullFile() {
//        if (getFile() == null) {
//            throw new BuildException("file attribute is null!");
//        }
//        dieOnCircularReference();
//        return getFile();
//    }

//    /**
//     * Create a new resource that matches a relative or absolute path.
//     * If the current instance has a compatible baseDir attribute, it is copied.
//     * @param path relative/absolute path to a resource
//     * @return a new resource of type StoreResource
//     * @throws BuildException if desired
//     * @since Ant1.8
//     */
//    @Override
//    public Resource getResource(String path) {
//        File newfile = FILE_UTILS.resolveFile(getFile(), path);
//        StoreResource fileResource = new StoreResource(newfile);
//        if (FILE_UTILS.isLeadingPath(getBaseDir(), newfile)) {
//            fileResource.setBaseDir(getBaseDir());
//        }
//        return fileResource;
//    }

    @Override
    protected StoreResource getRef() {
        return getCheckedRef(StoreResource.class);
    }
}
