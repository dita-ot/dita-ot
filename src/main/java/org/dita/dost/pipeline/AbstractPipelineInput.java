/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2004, 2005 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.pipeline;

import java.util.Map;

/**
 * Pipeline flow information for module input.
 *
 * @deprecated use {@link java.util.Map} instead. Deprecated since 2.3
 */
@Deprecated
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
