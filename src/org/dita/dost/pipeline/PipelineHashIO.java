/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.pipeline;

import java.util.HashMap;

/**
 * @author Lian, Li
 * 
 */
public class PipelineHashIO implements AbstractPipelineInput,
        AbstractPipelineOutput {
    private HashMap hash;

    /**
     * 
     */
    public PipelineHashIO() {
        super();
        hash = new HashMap();
    }

    public void setAttribute(String name, String value) {
        hash.put(name, value);
    }

    public String getAttribute(String name) {
        String value = null;
        value = (String) hash.get(name);
        return value;
    }
}
