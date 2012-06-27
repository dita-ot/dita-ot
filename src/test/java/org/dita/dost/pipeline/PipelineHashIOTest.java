/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */
package org.dita.dost.pipeline;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Unit test for {@link PipelineHashIO}
 * 
 * @author Jarno Elovirta
 */
public class PipelineHashIOTest {

    @Test
    public void testSetAttribute() {
        final AbstractPipelineInput p = new PipelineHashIO();
        p.setAttribute("foo", "bar");
        assertEquals("bar", p.getAttribute("foo"));
        p.setAttribute("foo", "baz");
        assertEquals("baz", p.getAttribute("foo"));
        p.setAttribute("foo", null);
        assertEquals(null, p.getAttribute("foo"));
    }

    @Test
    public void testGetAttribute() {
        final AbstractPipelineInput p = new PipelineHashIO();
        assertEquals(null, p.getAttribute("foo"));
        p.setAttribute("foo", "bar");
        assertEquals("bar", p.getAttribute("foo"));
    }

}
