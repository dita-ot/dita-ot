/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.index;

import static org.dita.dost.TestUtils.assertHtmlEqual;
import static org.junit.Assert.*;

import org.dita.dost.TestUtils;
import org.dita.dost.exception.DITAOTException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import org.dita.dost.writer.HTMLIndexWriter;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.InputSource;

public class IndexTermCollectionTest {

    private static final File resourceDir = TestUtils.getResourceDir(IndexTermCollectionTest.class);
    private static final File expDir = new File(resourceDir, "exp");
    private File tempDir;

    @Before
    public void setUp() throws Exception {
        tempDir = TestUtils.createTempDir(getClass());
    }

    @Test
    public void testGetIndexType() {
        final IndexTermCollection i = new IndexTermCollection();
        assertNull(i.getIndexType());
        i.setIndexType("");
        assertEquals("", i.getIndexType());
        i.setIndexType("type");
        assertEquals("type", i.getIndexType());
    }

    @Test
    public void testSetIndexType() {
        final IndexTermCollection i = new IndexTermCollection();
        i.setIndexType(null);
        i.setIndexType("");
    }

    @Test
    public void testGetIndexClass() {
        final IndexTermCollection i = new IndexTermCollection();
        i.setIndexClass(null);
        assertNull(i.getIndexClass());
        i.setIndexClass("");
        assertEquals("", i.getIndexClass());
        i.setIndexClass("class");
        assertEquals("class", i.getIndexClass());
    }

    @Test
    public void testSetIndexClass() {
        final IndexTermCollection i = new IndexTermCollection();
        i.setIndexClass(null);
        i.setIndexClass("");
    }

    @Test
    public void testAddTerm() {
        final IndexTermCollection i = new IndexTermCollection();
        assertEquals(0, i.getTermList().size());
        i.addTerm(new IndexTerm());
        i.addTerm(new IndexTerm());
        assertEquals(1, i.getTermList().size());
        try {
            i.addTerm(null);
            fail();
        } catch (final NullPointerException e) {
        }
    }

    @Test
    public void testGetTermList() {
        final IndexTermCollection i = new IndexTermCollection();
        assertEquals(Collections.emptyList(), i.getTermList());
        final IndexTerm first = new IndexTerm();
        first.setTermName("first");
        first.setTermKey("first");
        i.addTerm(first);
        final IndexTerm second = new IndexTerm();
        second.setTermName("second");
        second.setTermKey("second");
        i.addTerm(second);
        assertEquals(new HashSet<IndexTerm>(new ArrayList<IndexTerm>(Arrays.asList(first, second))),
                new HashSet<IndexTerm>(i.getTermList()));
    }

    @Test
    public void testSort() {
        final IndexTermCollection i = new IndexTermCollection();
        final IndexTerm first = new IndexTerm();
        first.setTermName("first");
        first.setTermKey("first");
        final IndexTerm second = new IndexTerm();
        second.setTermName("second");
        second.setTermKey("second");
        i.addTerm(second);
        i.addTerm(first);
        i.sort();
        assertEquals(new ArrayList<IndexTerm>(Arrays.asList(first, second)),
                i.getTermList());
    }

    @Test
    public void testOutputTerms() throws DITAOTException {
        final IndexTermCollection i = new IndexTermCollection();
        i.setIndexClass(HTMLIndexWriter.class.getCanonicalName());
        i.setOutputFileRoot(new File(tempDir, "foo").getAbsolutePath());
        final IndexTerm first = new IndexTerm();
        first.setTermName("first");
        first.setTermKey("first");
        final IndexTerm second = new IndexTerm();
        second.setTermName("second");
        second.setTermKey("second");
        i.addTerm(second);
        i.addTerm(first);
        i.outputTerms();

        assertHtmlEqual(new InputSource(new File(expDir, "foo.hhk").toURI().toString()),
                new InputSource(new File(tempDir, "foo.hhk").toURI().toString()));
    }

    @Test @Ignore
    public void testSetOutputFileRoot() {
        fail("Not yet implemented");
    }

    @Test @Ignore
    public void testGetPipelineHashIO() {
        fail("Not yet implemented");
    }

    @Test @Ignore
    public void testSetPipelineHashIO() {
        fail("Not yet implemented");
    }

    @After
    public void tearDown() throws IOException {
        TestUtils.forceDelete(tempDir);
    }

}
