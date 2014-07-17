/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */
package org.dita.dost.util;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

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
        job = new Job(tempDir);
    }

    @Test
    public void testGetProperty() {
        assertEquals("/foo/bar", job.getProperty("user.input.dir"));
    }

    @Test
    public void testSetProperty() {
        job.setProperty("foo", "bar");
        assertEquals("bar", job.getProperty("foo"));
    }

    @Test
    public void testGetCopytoMap() {
        final Map<File, File> exp = new HashMap<File, File>();
        exp.put(new File("foo"), new File("bar"));
        exp.put(new File("baz"), new File("qux"));
        assertEquals(exp, job.getCopytoMap());
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
        assertEquals("foo", job.getInputMap());
    }

    @Test
    public void testGetValue() {
        assertEquals("/foo/bar", job.getInputDir());
    }

    @AfterClass
    public static void tearDown() throws IOException {
        TestUtils.forceDelete(tempDir);
    }
    
}
