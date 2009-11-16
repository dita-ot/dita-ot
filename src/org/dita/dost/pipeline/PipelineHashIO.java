/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.pipeline;

import java.util.HashMap;

/**
 * PipelineHashIO implements AbstractPipelineInput. It put all of the input information
 * in a hash map.
 * 
 * @author Lian, Li
 * 
 */
public class PipelineHashIO implements AbstractPipelineInput,
        AbstractPipelineOutput {
    private HashMap hash;


    /**
     * Default contructor of PipelineHashIO class.
     */
    public PipelineHashIO() {
        super();
        hash = new HashMap();
    }

    /**
     * Set the attribute vale with name into hash map.
     * 
     * @param name name
     * @param value value
     */
    public void setAttribute(String name, String value) {
        hash.put(name, value);
    }

    /**
     * Get the attribute value according to its name.
     * 
     * @param name name
     * @return String value
     */
    public String getAttribute(String name) {
        String value = null;
        value = (String) hash.get(name);
        return value;
    }
}
