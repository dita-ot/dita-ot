/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;

import org.dita.dost.util.MergeUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class TestMergeUtils {

	private final File resourceDir = new File("test-stub");
	
	public static MergeUtils mergeUtils;
	@BeforeClass
	public static void setUp() {
		mergeUtils=MergeUtils.getInstance();
		mergeUtils.reset();
	}
	
	@Test
	public void testGetInstance(){
		//test if this class is singleton
		assertEquals(mergeUtils, MergeUtils.getInstance());
	}
	
	@Test
	public void testFindId() {
		mergeUtils.addId("dir\\\\#topicid");
		mergeUtils.addId("dir\\\\dir1\\\\a.xml#topicid");
		assertTrue(mergeUtils.findId("dir/#topicid"));
		assertTrue(mergeUtils.findId("dir/dir1/a.xml#topicid"));
		assertFalse(mergeUtils.findId("topicid"));
		assertFalse(mergeUtils.findId("dir/a.xml#topicid"));
		
	}
	

	@Test
	public void testAddIdString() {
		assertEquals(null, mergeUtils.addId(null));
		assertEquals("unique_3", mergeUtils.addId("a.xml#topicid"));
	}

	@Test@Ignore
	public void testAddIdStringString() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetIdValue() {
		assertEquals(null, mergeUtils.getIdValue(null));
		assertEquals("unique_3", mergeUtils.getIdValue("a.xml#topicid"));
		assertEquals(null, mergeUtils.getIdValue(" "));
	}

	@Test
	public void testIsVisited() {
		//set visitSet
		mergeUtils.visit("dir/dir1/a.xml#topicid");
		mergeUtils.visit("dir/a.xml");
		assertTrue(mergeUtils.isVisited("dir/a.xml"));
		assertTrue(mergeUtils.isVisited("dir/a.xml#topicid"));
		assertFalse(mergeUtils.isVisited("a.xml"));
		assertTrue(mergeUtils.isVisited("dir/dir1/a.xml"));
		assertTrue(mergeUtils.isVisited("dir/dir1/a.xml#topicid"));
		//if topic id in the path are not the same
		assertTrue(mergeUtils.isVisited("dir/dir1/a.xml#another"));
		assertFalse(mergeUtils.isVisited("a.xml"));
	}

	@Test@Ignore
	// This method has been tested in the previous method.
	public void testVisit() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetFirstTopicId() {
		//assertEquals("task",mergeUtils.getFirstTopicId("stub.xml", "TEST_STUB"));
		assertEquals("task",mergeUtils.getFirstTopicId("stub.xml", resourceDir.getAbsolutePath(),false));
		
	}
	
	@AfterClass
	public static void destroy(){
		//do nothing
	}
}
