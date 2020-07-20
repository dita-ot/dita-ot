/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2020 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.store.ant.types;

import org.apache.tools.ant.types.Resource;
import org.dita.dost.util.Job;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Objects;

public class StoreResource extends Resource {

    private URI file;
    private Job job;

    public StoreResource(Job job, URI file) {
        this.job = job;
        this.file = file;
    }

    /**
     * Get the name of this StoreResource.  If the basedir is set,
     * the name will be relative to that.  Otherwise the basename
     * only will be returned.
     * @return the name of this resource.
     */
    @Override
    public String getName() {
        return file.getPath();
    }

    /**
     * Learn whether this file exists.
     * @return true if this resource exists.
     */
    @Override
    public boolean isExists() {
        return job.getStore().exists(job.tempDirURI.resolve(file));
    }

    /**
     * Get the modification time in milliseconds since 01.01.1970 .
     * @return 0 if the resource does not exist.
     */
    @Override
    public long getLastModified() {
        // TODO: Add long lastModified entry to cache Store
        return -1;
    }

    /**
     * Learn whether the resource is a directory.
     * @return boolean flag indicating if the resource is a directory.
     */
    @Override
    public boolean isDirectory() {
        return false;
    }

    /**
     * Get the size of this Resource.
     * @return the size, as a long, 0 if the Resource does not exist.
     */
    @Override
    public long getSize() {
        return -1;
    }

    /**
     * Return an InputStream for reading the contents of this Resource.
     * @return an InputStream object.
     * @throws IOException if an error occurs.
     */
    @Override
    public InputStream getInputStream() throws IOException {
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
        return job.getStore().getOutputStream(job.tempDirURI.resolve(file));
    }

    /**
     * Compare this StoreResource to another Resource.
     * @param another the other Resource against which to compare.
     * @return a negative integer, zero, or a positive integer as this StoreResource
     *         is less than, equal to, or greater than the specified Resource.
     */
    @Override
    public int compareTo(Resource another) {
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
        if (another == null || !(another.getClass().equals(getClass()))) {
            return false;
        }
        StoreResource otherfr = (StoreResource) another;
        return Objects.equals(file, otherfr.file);
    }

    /**
     * Get the hash code for this Resource.
     * @return hash code as int.
     */
    @Override
    public int hashCode() {
        return file.hashCode();
    }

    /**
     * Get the string representation of this Resource.
     * @return this StoreResource formatted as a String.
     */
    @Override
    public String toString() {
        return job.tempDirURI.resolve(file).toString();
    }

    /**
     * Fulfill the ResourceCollection contract.
     * @return whether this Resource is a StoreResource.
     */
    @Override
    public boolean isFilesystemOnly() {
        return true;
    }

    @Override
    protected StoreResource getRef() {
        return getCheckedRef(StoreResource.class);
    }
}
