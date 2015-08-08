/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.pipeline;

import java.util.HashMap;
import java.util.Map;

/**
 * PipelineHashIO implements AbstractPipelineInput. It put all of the input information
 * in a hash map.
 * 
 * @author Lian, Li
 * 
 */
public final class PipelineHashIO implements AbstractPipelineInput,
AbstractPipelineOutput {
    private final Map<String, String> hash;


    /**
     * Default contructor of PipelineHashIO class.
     */
    public PipelineHashIO() {
        super();
        hash = new HashMap<>();
    }

    /**
     * Set the attribute vale with name into hash map.
     * 
     * @param name name
     * @param value value
     */
    @Override
    public void setAttribute(final String name, final String value) {
        hash.put(name, value);
    }

    /**
     * Get the attribute value according to its name.
     * 
     * @param name name
     * @return String value
     */
    @Override
    public String getAttribute(final String name) {
        String value = null;
        value = hash.get(name);
        return value;
    }

    @Override
    public Map<String, String> getAttributes() {
        return new HashMap<>(hash);
    }
}
