/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import static org.dita.dost.util.URLUtils.*;

import java.io.File;

import org.junit.After;

import org.dita.dost.TestUtils;
import org.dita.dost.util.MergeUtils;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class TestMergeUtils {

    private static final File resourceDir = TestUtils.getResourceDir(TestMergeUtils.class);
    private static final File srcDir = new File(resourceDir, "src");

    public static MergeUtils mergeUtils;

    @BeforeClass
    public static void setUp() {
        mergeUtils = new MergeUtils();
    }

    @After
    public void reset() {
        mergeUtils.reset();
    }

    @Test
    public void testFindId() {
        mergeUtils.addId(toURI("dir\\\\#topicid"));
        mergeUtils.addId(toURI("dir\\\\dir1\\\\a.xml#topicid"));
        assertTrue(mergeUtils.findId(toURI("dir/#topicid")));
        assertTrue(mergeUtils.findId(toURI("dir/dir1/a.xml#topicid")));
        assertFalse(mergeUtils.findId(toURI("topicid")));
        assertFalse(mergeUtils.findId(toURI("dir/a.xml#topicid")));
    }


    @Test
    public void testAddIdString() {
        assertEquals(null, mergeUtils.addId(null));
        assertEquals("unique_1", mergeUtils.addId(toURI("a.xml#topicid")));
        assertEquals("unique_2", mergeUtils.addId(toURI("a.xml#topicid2")));
        assertNull(mergeUtils.addId(null));
    }

    @Test@Ignore
    public void testAddIdStringString() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetIdValue() {
        mergeUtils.addId(toURI("a.xml#topicid"));
        mergeUtils.addId(toURI("a.xml#topicid2"));
        assertEquals(null, mergeUtils.getIdValue(null));
        assertEquals("unique_1", mergeUtils.getIdValue(toURI("a.xml#topicid")));
        assertEquals("unique_2", mergeUtils.getIdValue(toURI("a.xml#topicid2")));
        assertEquals(null, mergeUtils.getIdValue(toURI(" ")));
    }

    @Test
    public void testIsVisited() {
        //set visitSet
        mergeUtils.visit(toURI("dir/dir1/a.xml#topicid"));
        mergeUtils.visit(toURI("dir/a b.xml"));
        assertTrue(mergeUtils.isVisited(toURI("dir/a%20b.xml")));
        assertTrue(mergeUtils.isVisited(toURI("dir/a%20b.xml#topicid")));
        assertFalse(mergeUtils.isVisited(toURI("a%20b.xml")));
        assertTrue(mergeUtils.isVisited(toURI("dir/dir1/a.xml")));
        assertTrue(mergeUtils.isVisited(toURI("dir/dir1/a.xml#topicid")));
        //if topic id in the path are not the same
        assertTrue(mergeUtils.isVisited(toURI("dir/dir1/a.xml#another")));
    }

    @Test@Ignore
    // This method has been tested in the previous method.
    public void testVisit() {
        fail("Not yet implemented");
    }

    @Test
    public void testGetFirstTopicId() {
        //assertEquals("task",mergeUtils.getFirstTopicId("stub.xml", "TEST_STUB"));
        assertEquals("task", MergeUtils.getFirstTopicId(toURI("stub.xml"), srcDir.getAbsoluteFile(), false));
        assertEquals("task", MergeUtils.getFirstTopicId(toURI("stub.xml"), srcDir.getAbsoluteFile(), true));
    }

}
