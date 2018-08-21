/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2005 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.index;

/**
 * This class represent the target of an index term.
 *
 * @version 1.0 2005-05-11
 *
 * @author Wu, Zhi Qiang
 */
public final class IndexTermTarget {
    /** Name (title) of the target topic. */
    private String targetName = null;

    /** URI of the target topic. */
    private String targetURI = null;

    /**
     * Create a empty target.
     */
    public IndexTermTarget() {
    }

    /**
     * Get the target topic's name (title).
     *
     * @return Returns the targetName.
     */
    public String getTargetName() {
        return targetName;
    }

    /**
     * Set the target topic's name (title).
     * @param name The targetName to set.
     */
    public void setTargetName(final String name) {
        targetName = name;
    }

    /**
     * Get the target topic's URI.
     *
     * @return Returns the targetURI.
     */
    public String getTargetURI() {
        return targetURI;
    }

    /**
     * Set the target topic's URI.
     * @param uri The targetURI to set.
     */
    public void setTargetURI(final String uri) {
        targetURI = uri;
    }

    /**
     * The index term targets will be equal if the target topics have same name and URI value.
     *
     * @param obj object to compare
     * @return boolean true if equals
     */
    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof IndexTermTarget) {
            final IndexTermTarget target = (IndexTermTarget) obj;

            if (targetName.equals(target.getTargetName())
                    && targetURI.equals(target.getTargetURI())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Generate hash code for IndexTermTarget.
     * @return has code.
     */
    @Override
    public int hashCode() {
        int result = 17;

        result = 37 * result + targetName.hashCode();
        result = 37 * result + targetURI.hashCode();

        return result;
    }

    /**
     * Generate String for IndexTermTarget, with the format "{Target name: name, Target URL: uri}".
     * @see java.lang.Object#toString()
     * @return string
     */
    @Override
    public String toString() {
        return "{Target name: " + targetName + ", Target URL: " + targetURI + "}";
    }
}
