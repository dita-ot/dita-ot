/*
 * (c) Copyright IBM Corp. 2005 All Rights Reserved.
 */
package org.dita.dost.index;

import org.dita.dost.util.Constants;

/**
 * This class represent the target of the index term.
 * 
 * @version 1.0 2005-05-11
 * 
 * @author Wu, Zhi Qiang
 */
public class IndexTermTarget {
    /** Name of the target */
    private String targetName = null;

    /** URI of the target */
    private String targetURI = null;

    /**
     * Create a empty target.
     */
    public IndexTermTarget() {
    }

    /**
     * Get the target's name.
     * 
     * @return Returns the targetName.
     */
    public String getTargetName() {
        return targetName;
    }

    /**
     * Set the target's name.
     * 
     * @param name
     *            The targetName to set.
     */
    public void setTargetName(String name) {
        this.targetName = name;
    }

    /**
     * Get the target's target uri.
     * 
     * @return Returns the targetURI.
     */
    public String getTargetURI() {
        return targetURI;
    }

    /**
     * Set the target url.
     * 
     * @param uri
     *            The targetURI to set.
     */
    public void setTargetURI(String uri) {
        this.targetURI = uri;
    }

    /**
     * The index term target will be equal if they have same name and uri value.
     * 
     * @param obj
     */
    public boolean equals(Object obj) {
        if (obj instanceof IndexTermTarget) {
            IndexTermTarget target = (IndexTermTarget) obj;
            
            if (targetName.equals(target.getTargetName())
                    && targetURI.equals(target.getTargetURI())) {
                return true;
            }
        }

        return false;
    }
    
    /**
     * Generate hash code for IndexTerm
     */
    public int hashCode() {
        int result = Constants.INT_17;

        result = Constants.INT_37 * result + targetName.hashCode();
        result = Constants.INT_37 * result + targetURI.hashCode();

        return result;
    }

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new StringBuffer("{Target name: ").append(targetName).append(
				", Target URL: ").append(targetURI).append("}").toString();
	}
}
