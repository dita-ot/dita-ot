/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2010 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.util;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import java.io.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import org.dita.dost.TestUtils;

public class TestFileUtils {

    private static final String SEPARATOR_WINDOWS = "\\";
    private static final String SEPARATOR_UNIX = "/";

    private static final File resourceDir = TestUtils.getResourceDir(TestFileUtils.class);
    private static final File srcDir = new File(resourceDir, "src");
    private static File tempDir;

    @BeforeClass
    public static void setUp() throws IOException {
        tempDir = TestUtils.createTempDir(TestFileUtils.class);
    }

    @Test
    public void testIsHTMLFile() {

        assertTrue(FileUtils.isHTMLFile("file.html"));

        assertTrue(FileUtils.isHTMLFile("file.htm"));
        assertFalse(FileUtils.isHTMLFile("file"));
        assertFalse(FileUtils.isHTMLFile("file.HTML"));
        assertFalse(FileUtils.isHTMLFile("file.HTM"));
    }

    @Test
    public void testIsSupportedImageFile() {
        assertTrue(FileUtils.isSupportedImageFile("image.jpg"));
        assertTrue(FileUtils.isSupportedImageFile("image.gif"));
        assertTrue(FileUtils.isSupportedImageFile("image.eps"));
        assertTrue(FileUtils.isSupportedImageFile("image.jpeg"));
        assertTrue(FileUtils.isSupportedImageFile("image.png"));
        assertTrue(FileUtils.isSupportedImageFile("image.svg"));
        assertTrue(FileUtils.isSupportedImageFile("image.tiff"));
        assertTrue(FileUtils.isSupportedImageFile("image.tif"));
        assertFalse(FileUtils.isSupportedImageFile("image.abc"));
        assertFalse(FileUtils.isSupportedImageFile("image"));

    }

    @Test
    public void testGetRelativePathFromMap() {
        assertEquals("../a.dita",FileUtils.getRelativeUnixPath("c:/map/map.ditamap", "c:/a.dita"));
        assertEquals("../a.dita",FileUtils.getRelativeUnixPath("c:\\map\\map.ditamap", "c:\\a.dita"));
        assertEquals("d:/a.dita",FileUtils.getRelativeUnixPath("c:/map.ditamap", "d:/a.dita"));
        assertEquals("d:\\a.dita",FileUtils.getRelativeUnixPath("c:\\map.ditamap", "d:\\a.dita"));
        assertEquals("a.dita", FileUtils.getRelativeUnixPath("c:/map1/map2/map.ditamap", "c:/map1/map2/a.dita"));
        assertEquals("a.dita", FileUtils.getRelativeUnixPath("c:\\map1\\map2\\map.ditamap", "c:\\map1\\map2\\a.dita"));
        assertEquals("../topic/a.dita",FileUtils.getRelativeUnixPath("c:/map1/map.ditamap", "c:/topic/a.dita"));
        assertEquals("../topic/a.dita",FileUtils.getRelativeUnixPath("c:\\map1\\map.ditamap", "c:\\topic\\a.dita"));
    }
    
    @Test
    public void testGetRelativePathFromMapFileFile() {
        if (File.separator.equals(SEPARATOR_WINDOWS)) {
            assertEquals(new File("../a.dita"), FileUtils.getRelativePath(new File("c:\\map\\map.ditamap"), new File("c:\\a.dita")));
            assertEquals(new File("d:\\a.dita"), FileUtils.getRelativePath(new File("c:\\map.ditamap"), new File("d:\\a.dita")));
            assertEquals(new File("a.dita"), FileUtils.getRelativePath(new File("c:\\map1\\map2\\map.ditamap"), new File("c:\\map1\\map2\\a.dita")));
            assertEquals(new File("../topic/a.dita"), FileUtils.getRelativePath(new File("c:\\map1\\map.ditamap"), new File("c:\\topic\\a.dita")));
        } else {
            assertEquals(new File("../a.dita"), FileUtils.getRelativePath(new File("/map/map.ditamap"), new File("/a.dita")));
            assertEquals(new File("a.dita"), FileUtils.getRelativePath(new File("/map.ditamap"), new File("/a.dita")));
            assertEquals(new File("a.dita"), FileUtils.getRelativePath(new File("/map1/map2/map.ditamap"), new File("/map1/map2/a.dita")));
            assertEquals(new File("../topic/a.dita"), FileUtils.getRelativePath(new File("/map1/map.ditamap"), new File("/topic/a.dita")));
        }
    }
    
    @Test
    public void testGetRelativePathFile() {
        if (File.separator.equals(SEPARATOR_WINDOWS)) {
            assertEquals(new File(".."), FileUtils.getRelativePath(new File("map\\map.ditamap")));
            assertEquals(null, FileUtils.getRelativePath(new File("map.ditamap")));
            assertEquals(new File("..\\..\\"), FileUtils.getRelativePath(new File("map1\\map2\\map.ditamap")));
        } else {
            assertEquals(new File(".."), FileUtils.getRelativePath(new File("map/map.ditamap")));
            assertEquals(null, FileUtils.getRelativePath(new File("map.ditamap")));
            assertEquals(new File("../../"), FileUtils.getRelativePath(new File("map1/map2/map.ditamap")));
        }
    }    

    @Test
    public void testGetPathtoProject() {
        assertEquals("../../", FileUtils.getRelativeUnixPath("/dir/dir/file.xml"));
        assertEquals("../../", FileUtils.getRelativeUnixPath("dir/dir/file.xml"));
        assertEquals("../", FileUtils.getRelativeUnixPath("dir/file.xml"));
        assertNull(FileUtils.getRelativeUnixPath("file.xml"));

    }

    @Test
    public void testGetPathtoProjectFile() {
        assertEquals(new File(".." + File.separator + ".." + File.separator), FileUtils.getRelativePath(new File(File.separator + "dir" + File.separator + "dir" + File.separator + "file.xml")));
        assertEquals(new File(".." + File.separator + ".." + File.separator), FileUtils.getRelativePath(new File("dir" + File.separator + "dir" + File.separator + "file.xml")));
        assertEquals(new File(".." + File.separator), FileUtils.getRelativePath(new File("dir" + File.separator + "file.xml")));
        assertNull(FileUtils.getRelativePath(new File("file.xml")));
    }
    
    @Test
    public void testResolveTopic() {
        if (File.separator.equals(SEPARATOR_WINDOWS)) {
            assertEquals("c:\\dir\\file.xml", FileUtils.resolveTopic("c:\\dir","file.xml"));
            assertEquals("c:\\dir\\file.xml#topicid", FileUtils.resolveTopic("c:\\dir","file.xml#topicid"));
            assertEquals("c:\\file.xml", FileUtils.resolveTopic("c:\\dir","..\\file.xml"));
            assertEquals("file.xml", FileUtils.resolveTopic("","file.xml"));
            assertEquals("file.xml", FileUtils.resolveTopic((String) null,"file.xml"));
        } else {
            assertEquals("/dir/file.xml", FileUtils.resolveTopic("/dir","file.xml"));
            assertEquals("/dir/file.xml#topicid", FileUtils.resolveTopic("/dir","file.xml#topicid"));
            assertEquals("/file.xml", FileUtils.resolveTopic("/dir","../file.xml"));
            assertEquals("file.xml", FileUtils.resolveTopic("","file.xml"));
            assertEquals("file.xml", FileUtils.resolveTopic((String) null,"file.xml"));
        }
    }

    @Test
    public void testResolveFile() {
        if (File.separator.equals(SEPARATOR_WINDOWS)) {
            assertEquals(new File("c:\\dir\\file.xml"), FileUtils.resolve("c:\\dir","file.xml"));
            assertEquals(new File("c:\\dir\\file.xml"), FileUtils.resolve("c:\\dir","file.xml#topicid"));
            assertEquals(new File("c:\\file.xml"), FileUtils.resolve("c:\\dir","..\\file.xml"));
            assertEquals(new File("file.xml"), FileUtils.resolve("","file.xml"));
            assertEquals(new File("file.xml"), FileUtils.resolve((String) null,"file.xml"));
        } else {
            assertEquals(new File("/dir/file.xml"), FileUtils.resolve("/dir","file.xml"));
            assertEquals(new File("/dir/file.xml"), FileUtils.resolve("/dir","file.xml#topicid"));
            assertEquals(new File("/file.xml"), FileUtils.resolve("/dir","../file.xml"));
            assertEquals(new File("file.xml"), FileUtils.resolve("","file.xml"));
            assertEquals(new File("file.xml"), FileUtils.resolve((String) null,"file.xml"));
        }
    }

    @Test
    public void testNormalizeDirectory() {
        if (File.separator.equals(SEPARATOR_WINDOWS)) {
            assertEquals(new File("c:\\dir1\\dir2\\file.xml"),FileUtils.resolve("c:\\dir1", "dir2\\file.xml"));
            assertEquals(new File("c:\\dir1\\file.xml"),FileUtils.resolve("c:\\dir1\\dir2", "..\\file.xml"));
            assertEquals(new File("\\file.xml"),FileUtils.resolve("", "\\file.xml#topicid"));
            assertEquals(new File("c:\\file.xml"),FileUtils.resolve("", "c:\\file.xml"));
            assertEquals(new File("c:\\file.xml"),FileUtils.resolve((String) null, "c:\\file.xml#topicid"));
        } else {
            assertEquals(new File("/dir1/dir2/file.xml"),FileUtils.resolve("/dir1", "dir2/file.xml"));
            assertEquals(new File("/dir1/file.xml"),FileUtils.resolve("/dir1/dir2", "../file.xml"));
            assertEquals(new File("/file.xml"),FileUtils.resolve("", "/file.xml#topicid"));
            assertEquals(new File("/file.xml"),FileUtils.resolve("", "/file.xml"));
            assertEquals(new File("/file.xml"),FileUtils.resolve((String) null, "/file.xml#topicid"));
            assertEquals(new File("file.xml"), FileUtils.resolve("","file.xml"));
            assertEquals(new File("file.xml"), FileUtils.resolve((String) null,"file.xml"));
        }
    }
    
    @Test
    public void testIsAbsolutePath() {
        assertFalse(FileUtils.isAbsolutePath(null));
        assertFalse(FileUtils.isAbsolutePath(""));
        assertFalse(FileUtils.isAbsolutePath(" "));
        if(File.separator.equals(SEPARATOR_WINDOWS)){
            assertTrue(FileUtils.isAbsolutePath("C:\\file.xml"));
            assertTrue(FileUtils.isAbsolutePath("c:\\file.xml"));
            assertFalse(FileUtils.isAbsolutePath("\\dic\\file.xml"));
            assertFalse(FileUtils.isAbsolutePath("file.xml"));
            // Microsoft Windows UNC
            assertTrue(FileUtils.isAbsolutePath("\\\\ComputerName\\SharedFolder\\Resource"));
        }else if(File.separator.equals(SEPARATOR_UNIX)){
            assertTrue(FileUtils.isAbsolutePath("/file.xml"));
            assertFalse(FileUtils.isAbsolutePath("file.xml"));
        }
    }

    @Test
    public void testReplaceExtName() {
        // initial the extName of Class DebugAndFilterModule.
        final String extName = ".dita";
        assertEquals("filename.dita", FileUtils.replaceExtension("filename.xml", extName));
        // if there is a topic marked with sharp
        assertEquals("filename.dita#topicid", FileUtils
                .replaceExtension("filename.xml#topicid", extName));
        // if the input just is a topicid
        assertEquals("#topicid", FileUtils.replaceExtension("#topicid", extName));
        // if there is an extra dot.
        assertEquals("file.name.dita", FileUtils
                .replaceExtension("file.name.xml", extName));
        assertEquals("file.name.dita#topicid", FileUtils
                .replaceExtension("file.name.xml#topicid", extName));
        // if there is no extension.
        assertEquals("file", FileUtils.replaceExtension("file", extName));
    }
    
    @Test
    public void testGetExtName() {
        assertEquals("xml", FileUtils.getExtension("filename.xml"));
        assertEquals("xml", FileUtils.getExtension("filename.xml#topicid"));
        assertNull(FileUtils.getExtension("#topicid"));
        assertEquals("xml", FileUtils.getExtension("file.name.xml"));
        assertEquals("xml", FileUtils.getExtension("file.name.xml#topicid"));
        assertNull(FileUtils.getExtension("file"));
    }
    
    @Test
    public void testDeriveFilename() {
        assertEquals("baz.qux", FileUtils.getName("/foo/bar/baz.qux"));
        assertEquals("baz.qux", FileUtils.getName("baz.qux"));
        assertEquals("", FileUtils.getName("/foo/bar/"));
    }

    @Test
    public void testDerivePath() {
        assertEquals("/foo/bar", FileUtils.getFullPathNoEndSeparator("/foo/bar/baz.qux"));
        assertEquals("foo/bar", FileUtils.getFullPathNoEndSeparator("foo/bar/baz.qux"));
        //assertEquals("", FileUtils.derivePath("baz.qux"));
    }

    @Test
    public void testStripFragment() {
        assertEquals("foo", FileUtils.stripFragment("foo#bar"));
        assertEquals("foo", FileUtils.stripFragment("foo#"));
        assertEquals("foo", FileUtils.stripFragment("foo"));
    }

    @Test
    public void testGetFragment() {
        assertEquals("bar", FileUtils.getFragment("foo#bar"));
        assertEquals("", FileUtils.getFragment("foo#"));
        assertNull(FileUtils.getFragment("foo"));
    }
    
    @Test
    public void testGetFragmentStringString() {
        assertEquals("bar", FileUtils.getFragment("foo#bar", "baz"));
        assertEquals("", FileUtils.getFragment("foo#", "baz"));
        assertEquals("baz", FileUtils.getFragment("foo", "baz"));
        assertEquals("bar", FileUtils.getFragment("foo#bar", null));
        assertEquals("", FileUtils.getFragment("foo#", null));
        assertEquals(null, FileUtils.getFragment("foo", null));
    }

    @Test
    public void testSetFragment() {
        assertEquals("foo#baz", FileUtils.setFragment("foo#bar", "baz"));
        assertEquals("foo#baz", FileUtils.setFragment("foo#", "baz"));
        assertEquals("foo#baz", FileUtils.setFragment("foo", "baz"));
        assertEquals("#baz", FileUtils.setFragment("#bar", "baz"));
        assertEquals("foo", FileUtils.setFragment("foo#bar", null));
        assertEquals("foo", FileUtils.setFragment("foo#", null));
        assertEquals("foo", FileUtils.setFragment("foo", null));
        assertEquals("", FileUtils.setFragment("#bar", null));
    }
    
    @Test
    public void testDirectoryContains() {
        assertTrue(FileUtils.directoryContains(srcDir, new File(srcDir, "test.txt")));
        assertFalse(FileUtils.directoryContains(srcDir, srcDir));
        assertFalse(FileUtils.directoryContains(new File(srcDir, "test"), srcDir));
        assertFalse(FileUtils.directoryContains(srcDir, new File(srcDir, ".." + File.separator + "test.txt")));
    }
    
    @AfterClass
    public static void tearDown() throws IOException {
        TestUtils.forceDelete(tempDir);
    }

}
