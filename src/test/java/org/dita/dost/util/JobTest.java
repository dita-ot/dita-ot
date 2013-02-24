/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */
package org.dita.dost.util;

import static org.dita.dost.util.Constants.*;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.dita.dost.TestUtils;

public final class JobTest {

    private static File tempDir;
    private static Job job;
    
    @BeforeClass
    public static void setUp() throws IOException {
        tempDir = TestUtils.createTempDir(JobTest.class);
        final Job j = new Job(tempDir);
        j.setProperty("user.input.dir", "/foo/bar");
        j.setProperty(COPYTO_TARGET_TO_SOURCE_MAP_LIST, "foo=bar,baz=qux");
        j.setProperty(SUBJEC_SCHEME_LIST, "foo,bar");
        j.setProperty(INPUT_DITAMAP, "foo");
        j.setProperty(FULL_DITAMAP_TOPIC_LIST, "foo1,bar1");
        j.setProperty(CONREF_TARGET_LIST, "foo2,bar2");
        j.setProperty(COPYTO_SOURCE_LIST, "foo3,bar3");
        j.write();
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
        final Map<String, String> exp = new HashMap<String, String>();
        exp.put("foo", "bar");
        exp.put("baz", "qux");
        assertEquals(exp, job.getCopytoMap());
    }

    @Test
    public void testGetSchemeSet() {
        final Set<String> exp = new HashSet<String>();
        exp.add("foo");
        exp.add("bar");
        assertEquals(exp, job.getSchemeSet());
    }

    @Test
    public void testGetInputMap() {
        assertEquals("foo", job.getInputMap());
    }

    @Test
    public void testGetCollection() {
        final Set<String> exp = new HashSet<String>();
        exp.add("bar3");
        exp.add("foo3");
        exp.add("bar2");
        exp.add("foo2");
        exp.add("bar1");
        exp.add("foo1");
        final Set<String> act = new HashSet<String>();
        act.addAll(job.getSet(FULL_DITAMAP_TOPIC_LIST));
        act.addAll(job.getSet(CONREF_TARGET_LIST));
        act.addAll(job.getSet(COPYTO_SOURCE_LIST));
        assertEquals(exp, act);
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
