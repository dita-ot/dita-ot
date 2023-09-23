/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2010 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.util;

import static org.dita.dost.util.URLUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import org.dita.dost.TestUtils;
import org.dita.dost.store.StreamStore;
import org.dita.dost.util.MergeUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class TestMergeUtils {

  private static final File resourceDir = TestUtils.getResourceDir(TestMergeUtils.class);
  private static final File srcDir = new File(resourceDir, "src");

  public static MergeUtils mergeUtils;

  @BeforeAll
  public static void setUp() throws IOException {
    mergeUtils = new MergeUtils();
    mergeUtils.setJob(new Job(srcDir, new StreamStore(srcDir, new XMLUtils())));
  }

  @AfterEach
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

  @Test
  @Disabled
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

  @Test
  @Disabled
  // This method has been tested in the previous method.
  public void testVisit() {
    fail("Not yet implemented");
  }

  @Test
  public void testGetFirstTopicId() {
    //assertEquals("task",mergeUtils.getFirstTopicId("stub.xml", "TEST_STUB"));
    assertEquals("task", mergeUtils.getFirstTopicId(srcDir.toURI().resolve("stub.xml"), false));
    assertEquals("task", mergeUtils.getFirstTopicId(srcDir.toURI().resolve("stub.xml"), true));
    assertNull(mergeUtils.getFirstTopicId(srcDir.toURI().resolve("filedoesnotexist.xml"), false));
  }
}
