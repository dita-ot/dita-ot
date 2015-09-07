/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.pipeline;

import java.util.Map;

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
    void setAttribute(String name, String value);

    /**
     * Get the attribute value.
     * 
     * @param name attribute name
     * @return String attribute value, <code>null</code> if not defined.
     */
    String getAttribute(String name);

    /**
     * Get the attributes.
     *
     * @return Map of attribute values, empty Map is no attributes have been defined.
     */
    Map<String, String> getAttributes();

}
