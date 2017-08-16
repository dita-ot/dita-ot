/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2013 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.util;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.tools.ant.Project;
import org.dita.dost.ant.UriDirnameTask;
import org.junit.Before;
import org.junit.Test;

public class UriDirnameTaskTest {

    private UriDirnameTask dirname;
    
    @Before
    public void setUp() throws URISyntaxException {
        final Project project = new Project();
        dirname = new UriDirnameTask();
        dirname.setProject(project);
        dirname.setProperty("test");
    }
    
    @Test
    public void testPlain() throws URISyntaxException {
        dirname.setFile(new URI("foo/"));
        dirname.execute();
        assertEquals("foo/", dirname.getProject().getProperty("test"));
    }
    
    @Test
    public void testFile() throws URISyntaxException {
        dirname.setFile(new URI("foo/bar.baz"));
        dirname.execute();
        assertEquals("foo/", dirname.getProject().getProperty("test"));
    }

}
