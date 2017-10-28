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
import org.dita.dost.ant.UriBasenameTask;
import org.junit.Before;
import org.junit.Test;

public class UriBasenameTaskTest {

    private UriBasenameTask basename;
    
    @Before
    public void setUp() throws URISyntaxException {
        final Project project = new Project();
        basename = new UriBasenameTask();
        basename.setProject(project);
        basename.setProperty("test");
    }
    
    @Test
    public void testPlain() throws URISyntaxException {
        basename.setFile(new URI("foo/bar.baz"));
        basename.execute();
        assertEquals("bar.baz", basename.getProject().getProperty("test"));
    }

    @Test
    public void testAbsolute() throws URISyntaxException {
        basename.setFile(new URI("file:/foo/bar.baz"));
        basename.execute();
        assertEquals("bar.baz", basename.getProject().getProperty("test"));
    }

    @Test
    public void testSuffix() throws URISyntaxException {
        basename.setFile(new URI("file:/foo/bar.baz"));
        basename.setSuffix("baz");
        basename.execute();
        assertEquals("bar", basename.getProject().getProperty("test"));
    }
    
    @Test
    public void testDotSuffix() throws URISyntaxException {
        basename.setFile(new URI("file:/foo/bar.baz"));
        basename.setSuffix(".baz");
        basename.execute();
        assertEquals("bar", basename.getProject().getProperty("test"));
    }
    
    @Test
    public void testSuffixNoMatch() throws URISyntaxException {
        basename.setFile(new URI("file:/foo/bar.baz"));
        basename.setSuffix(".qux");
        basename.execute();
        assertEquals("bar.baz", basename.getProject().getProperty("test"));
    }
    
    @Test
    public void testWildcardSuffix() throws URISyntaxException {
        basename.setFile(new URI("file:/foo/bar.baz"));
        basename.setSuffix(".*");
        basename.execute();
        assertEquals("bar", basename.getProject().getProperty("test"));
    }
    
    @Test
    public void testWildcardSuffixNoMatch() throws URISyntaxException {
        basename.setFile(new URI("file:/foo/bar"));
        basename.setSuffix(".*");
        basename.execute();
        assertEquals("bar", basename.getProject().getProperty("test"));
    }
    
}
