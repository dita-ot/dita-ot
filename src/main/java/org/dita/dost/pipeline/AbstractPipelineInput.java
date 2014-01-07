/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.pipeline;

/**
 * Pipeline flow information for module input.
 */
public interface AbstractPipelineInput {

    /**
     * Set the attribute value. Existing attribute value will
     * be overwritten.
     * 
     * @param name attribute name
     * @param value attribute value
     */
    public void setAttribute(String name, String value);

    /**
     * Get the attribute value.
     * 
     * @param name attribute name
     * @return String attribute value, <code>null</code> if not defined.
     */
    public String getAttribute(String name);

}
