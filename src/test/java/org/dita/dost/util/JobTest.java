/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.util;

import static org.junit.Assert.*;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.dita.dost.store.StreamStore;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.dita.dost.TestUtils;

public final class JobTest {

    private static final File resourceDir = TestUtils.getResourceDir(JobTest.class);
    private static final File srcDir = new File(resourceDir, "src");
    private static File tempDir;
    private static Job job;
    
    @BeforeClass
    public static void setUp() throws IOException {
        tempDir = TestUtils.createTempDir(JobTest.class);
        TestUtils.copy(srcDir, tempDir);
        job = new Job(tempDir, new StreamStore(tempDir, new XMLUtils()));
    }

    @Test
    public void testGetProperty() {
        assertEquals("/foo/bar", job.getProperty(INPUT_DIR));
        assertEquals("file:/foo/bar", job.getProperty(INPUT_DIR_URI));
    }

    @Test
    public void testSetProperty() {
        job.setProperty("foo", "bar");
        assertEquals("bar", job.getProperty("foo"));
    }

    @Test
    public void testGetFileInfo() throws URISyntaxException {
        final URI relative = new URI("foo/bar.dita");
        final URI absolute = tempDir.toURI().resolve(relative);
        final Job.FileInfo fi = new Job.FileInfo.Builder().uri(relative).build();
        job.add(fi);
        assertEquals(fi, job.getFileInfo(relative));
        assertEquals(fi, job.getFileInfo(absolute));
        assertNull(job.getFileInfo((URI) null));
    }

    @Test
    public void testGetInputMap() {
        assertEquals(toURI("foo"), job.getInputMap());
    }

    @Test
    public void testGetValue() throws URISyntaxException {
        assertEquals(new URI("file:/foo/bar"), job.getInputDir());
    }

    @AfterClass
    public static void tearDown() throws IOException {
        TestUtils.forceDelete(tempDir);
    }
    
}
