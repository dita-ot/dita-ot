/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2011 All Rights Reserved.
 */
package org.dita.dost.module;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import org.junit.Test;

import org.dita.dost.exception.DITAOTException;

public class ModuleFactoryTest {

    @Test
    public void testInstance() {
        assertNotNull(ModuleFactory.instance());
        assertSame(ModuleFactory.instance(), ModuleFactory.instance());
    }

    @Test
    public void testCreateModule() throws DITAOTException {
        final ModuleFactory f = ModuleFactory.instance();
        final AbstractPipelineModule d = f.createModule("DummyPipeline");
        assertEquals(DummyPipelineModule.class, d.getClass());
        try {
            f.createModule((String) null);
            fail();
        } catch (final DITAOTException e) {
            // NOOP
        }
    }

}
