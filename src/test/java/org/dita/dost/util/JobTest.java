/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */
package org.dita.dost.util;

import static org.dita.dost.util.Constants.*;
import static org.junit.Assert.assertEquals;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
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
    private final static Properties prop = new Properties();
    static {
        prop.setProperty("user.input.dir", "/foo/bar");
        prop.setProperty(COPYTO_TARGET_TO_SOURCE_MAP_LIST, "foo=bar,baz=qux");
        prop.setProperty(SUBJEC_SCHEME_LIST, "foo,bar");
        prop.setProperty(INPUT_DITAMAP, "foo");
        prop.setProperty(FULL_DITAMAP_TOPIC_LIST, "foo1,bar1");
        prop.setProperty(CONREF_TARGET_LIST, "foo2,bar2");
        prop.setProperty(COPYTO_SOURCE_LIST, "foo3,bar3");
    }
    
    @BeforeClass
    public static void setUp() throws IOException {
        tempDir = TestUtils.createTempDir(JobTest.class);
        OutputStream ditaList = null;
        OutputStream xmlDitaList = null;
        try {
            ditaList = new BufferedOutputStream(new FileOutputStream(new File(tempDir, FILE_NAME_DITA_LIST)));
            xmlDitaList = new BufferedOutputStream(new FileOutputStream(new File(tempDir, FILE_NAME_DITA_LIST_XML)));
            prop.store(ditaList, null);
            prop.storeToXML(xmlDitaList, null);
        } finally {
            if (ditaList != null) {
                ditaList.close();
            }
            if (xmlDitaList != null) {
                xmlDitaList.close();
            }
        }
        job = new Job(tempDir);
    }
    
    @Test
    public void testWrite() throws IOException {
        final Job j = new Job(tempDir);
        j.write();
        final Properties ditaList = new Properties();
        final Properties xmlDitaList = new Properties();
        InputStream src = null;
        InputStream xmlSrc = null;
        try {
            src = new BufferedInputStream(new FileInputStream(new File(tempDir, FILE_NAME_DITA_LIST)));
            xmlSrc = new BufferedInputStream(new FileInputStream(new File(tempDir, FILE_NAME_DITA_LIST_XML)));
            xmlDitaList.loadFromXML(xmlSrc);
            ditaList.load(src);
        } finally {
            if (src != null) {
                src.close();
            }
            if (xmlSrc != null) {
                xmlSrc.close();
            }
        }
        assertEquals(prop, ditaList);
        assertEquals(prop, xmlDitaList);
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
        final LinkedList<String> exp = new LinkedList<String>();
        exp.add("bar3");
        exp.add("foo3");
        exp.add("bar2");
        exp.add("foo2");
        exp.add("bar1");
        exp.add("foo1");
        assertEquals(exp, job.getCollection());
    }

    @Test
    public void testGetValue() {
        assertEquals("/foo/bar", job.getValue());
    }

    @AfterClass
    public static void tearDown() throws IOException {
        TestUtils.forceDelete(tempDir);
    }
    
}
