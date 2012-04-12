/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.util;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

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
    public void testIsDITAFile() {
        assertTrue(FileUtils.isDITAFile("file.xml"));
        assertTrue(FileUtils.isDITAFile("file.xml#topicid"));
        assertTrue(FileUtils.isDITAFile("file.dita"));
        assertTrue(FileUtils.isDITAFile("file.dita#topicid"));
        assertFalse(FileUtils.isDITAFile("file.xm"));
        assertFalse(FileUtils.isDITAFile("file.dit#xml"));
    }

    @Test
    public void testIsDITATopicFile() {
        assertTrue(FileUtils.isDITATopicFile("file.xml"));
        assertTrue(FileUtils.isDITATopicFile("file.dita"));
        assertFalse(FileUtils.isDITATopicFile("file"));
        assertFalse(FileUtils.isDITATopicFile("file.XML"));
        assertFalse(FileUtils.isDITATopicFile("file.DITA"));
    }

    @Test
    public void testIsDITAMapFile() {
        assertTrue(FileUtils.isDITAMapFile("file.ditamap"));
        assertFalse(FileUtils.isDITAMapFile("file.Ditamap"));
        assertFalse(FileUtils.isDITAMapFile("file.DITAMAP"));
        assertFalse(FileUtils.isDITAMapFile("file"));
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
    public void testIsValidTarget() {
        assertTrue(FileUtils.isValidTarget("file.ditamap"));
        assertTrue(FileUtils.isValidTarget("file.xml"));
        assertTrue(FileUtils.isValidTarget("file.dita"));
        assertTrue(FileUtils.isValidTarget("file.jpg"));
        assertTrue(FileUtils.isValidTarget("file.gif"));
        assertTrue(FileUtils.isValidTarget("file.eps"));
        assertTrue(FileUtils.isValidTarget("file.html"));
        assertTrue(FileUtils.isValidTarget("file.jpeg"));
        assertTrue(FileUtils.isValidTarget("file.png"));
        assertTrue(FileUtils.isValidTarget("file.svg"));
        assertTrue(FileUtils.isValidTarget("file.tiff"));
        assertTrue(FileUtils.isValidTarget("file.tif"));
        assertFalse(FileUtils.isValidTarget("file.abc"));
        assertFalse(FileUtils.isValidTarget("file"));
    }

    @Test
    public void testGetRelativePathFromMap() {
        assertEquals("../a.dita",FileUtils.getRelativePath("c:/map/map.ditamap", "c:/a.dita"));
        assertEquals("../a.dita",FileUtils.getRelativePath("c:\\map\\map.ditamap", "c:\\a.dita"));
        assertEquals("d:/a.dita",FileUtils.getRelativePath("c:/map.ditamap", "d:/a.dita"));
        assertEquals("d:\\a.dita",FileUtils.getRelativePath("c:\\map.ditamap", "d:\\a.dita"));
        assertEquals("a.dita", FileUtils.getRelativePath("c:/map1/map2/map.ditamap", "c:/map1/map2/a.dita"));
        assertEquals("a.dita", FileUtils.getRelativePath("c:\\map1\\map2\\map.ditamap", "c:\\map1\\map2\\a.dita"));
        assertEquals("../topic/a.dita",FileUtils.getRelativePath("c:/map1/map.ditamap", "c:/topic/a.dita"));
        assertEquals("../topic/a.dita",FileUtils.getRelativePath("c:\\map1\\map.ditamap", "c:\\topic\\a.dita"));
    }

    @Test
    public void testGetPathtoProject() {
        assertEquals("../../", FileUtils.getRelativePath("/dir/dir/file.xml"));
        assertEquals("../../", FileUtils.getRelativePath("dir/dir/file.xml"));
        assertEquals("../", FileUtils.getRelativePath("dir/file.xml"));
        assertEquals(null, FileUtils.getRelativePath("file.xml"));

    }

    @Test
    public void testResolveTopic() {
        if (File.separator.equals(SEPARATOR_WINDOWS)) {
            assertEquals("c:\\dir\\file.xml", FileUtils.resolveTopic("c:\\dir","file.xml"));
            assertEquals("c:\\dir\\file.xml#topicid", FileUtils.resolveTopic("c:\\dir","file.xml#topicid"));
            assertEquals("c:\\file.xml", FileUtils.resolveTopic("c:\\dir","..\\file.xml"));
            assertEquals("\\file.xml", FileUtils.resolveTopic("","file.xml"));
            assertEquals("file.xml", FileUtils.resolveTopic(null,"file.xml"));
        } else {
            assertEquals("/dir/file.xml", FileUtils.resolveTopic("/dir","file.xml"));
            assertEquals("/dir/file.xml#topicid", FileUtils.resolveTopic("/dir","file.xml#topicid"));
            assertEquals("/file.xml", FileUtils.resolveTopic("/dir","../file.xml"));
            assertEquals("/file.xml", FileUtils.resolveTopic("","file.xml"));
            assertEquals("file.xml", FileUtils.resolveTopic(null,"file.xml"));
        }
    }

    @Test
    public void testResolveFile() {
        if (File.separator.equals(SEPARATOR_WINDOWS)) {
            assertEquals("c:\\dir\\file.xml", FileUtils.resolveFile("c:\\dir","file.xml"));
            assertEquals("c:\\dir\\file.xml", FileUtils.resolveFile("c:\\dir","file.xml#topicid"));
            assertEquals("c:\\file.xml", FileUtils.resolveFile("c:\\dir","..\\file.xml"));
            assertEquals("\\file.xml", FileUtils.resolveFile("","file.xml"));
            assertEquals("file.xml", FileUtils.resolveFile(null,"file.xml"));
        } else {
            assertEquals("/dir/file.xml", FileUtils.resolveFile("/dir","file.xml"));
            assertEquals("/dir/file.xml", FileUtils.resolveFile("/dir","file.xml#topicid"));
            assertEquals("/file.xml", FileUtils.resolveFile("/dir","../file.xml"));
            assertEquals("/file.xml", FileUtils.resolveFile("","file.xml"));
            assertEquals("file.xml", FileUtils.resolveFile(null,"file.xml"));
        }
    }

    @Test
    public void testNormalizeDirectory() {
        if (File.separator.equals(SEPARATOR_WINDOWS)) {
            assertEquals("c:\\dir1\\dir2\\file.xml",FileUtils.normalizeDirectory("c:\\dir1", "dir2\\file.xml"));
            assertEquals("c:\\dir1\\file.xml",FileUtils.normalizeDirectory("c:\\dir1\\dir2", "..\\file.xml"));
            assertEquals("\\file.xml",FileUtils.normalizeDirectory("", "\\file.xml#topicid"));
            //should be c:\\file.xml?
            assertEquals("\\c:\\file.xml",FileUtils.normalizeDirectory("", "c:\\file.xml"));
            assertEquals("c:\\file.xml",FileUtils.normalizeDirectory(null, "c:\\file.xml#topicid"));
        } else {
            assertEquals("/dir1/dir2/file.xml",FileUtils.normalizeDirectory("/dir1", "dir2/file.xml"));
            assertEquals("/dir1/file.xml",FileUtils.normalizeDirectory("/dir1/dir2", "../file.xml"));
            assertEquals("/file.xml",FileUtils.normalizeDirectory("", "/file.xml#topicid"));
            //should be /file.xml?
            assertEquals("/file.xml",FileUtils.normalizeDirectory("", "/file.xml"));
            assertEquals("/file.xml",FileUtils.normalizeDirectory(null, "/file.xml#topicid"));
        }
    }

    @Test
    public void testNormalizeStringStringWindows() {
        assertEquals("a\\c\\file.xml",FileUtils.normalize("a\\b\\..\\c\\file.xml", SEPARATOR_WINDOWS));
        assertEquals("a\\b\\file.xml",FileUtils.normalize("a\\.\\b\\.\\file.xml", SEPARATOR_WINDOWS));
        assertEquals("..\\a\\file.xml",FileUtils.normalize("..\\a\\file.xml", SEPARATOR_WINDOWS));
        assertEquals("..\\file.xml", FileUtils.normalize("a\\..\\..\\file.xml", SEPARATOR_WINDOWS));
        assertEquals("file.xml", FileUtils.normalize("a\\b\\..\\..\\file.xml", SEPARATOR_WINDOWS));
        assertEquals("\\a\\b\\file.xml", FileUtils.normalize("\\a\\.\\b\\c\\..\\file.xml", SEPARATOR_WINDOWS));
        assertEquals("\\\\server\\dir\\file.xml", FileUtils.normalize("\\\\server\\a\\..\\dir\\file.xml", SEPARATOR_WINDOWS));
    }

    @Test
    public void testNormalizeStringStringUnix() {
        assertEquals("a/c/file.xml",FileUtils.normalize("a/b/../c/file.xml", SEPARATOR_UNIX));
        assertEquals("a/b/file.xml",FileUtils.normalize("a/./b/./file.xml", SEPARATOR_UNIX));
        assertEquals("../a/file.xml",FileUtils.normalize("../a/file.xml", SEPARATOR_UNIX));
        assertEquals("../file.xml", FileUtils.normalize("a/../../file.xml", SEPARATOR_UNIX));
        assertEquals("file.xml", FileUtils.normalize("a/b/../../file.xml", SEPARATOR_UNIX));
        assertEquals("/a/b/file.xml", FileUtils.normalize("/a/./b/c/../file.xml", SEPARATOR_UNIX));
        assertEquals("//server/dir/file.xml", FileUtils.normalize("//server/a/../dir/file.xml", SEPARATOR_UNIX));
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
    public void testCopyFile() throws IOException {
        final File src = new File(srcDir, "ibmrnr.txt");
        final File dst = new File(tempDir, "ibmrnr.txt");
        assertFalse(dst.exists());
        FileUtils.copyFile(src, dst);
        assertTrue(dst.exists());
        assertEquals(TestUtils.readFileToString(src),
                TestUtils.readFileToString(dst));
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
        assertEquals(null, FileUtils.getExtension("#topicid"));
        assertEquals("xml", FileUtils.getExtension("file.name.xml"));
        assertEquals("xml", FileUtils.getExtension("file.name.xml#topicid"));
        assertEquals(null, FileUtils.getExtension("file"));
    }

    @Test
    public void testFileExists() {
        assertTrue(FileUtils.fileExists(new File(srcDir, "ibmrnr.txt").getPath()));
        assertTrue(FileUtils.fileExists(new File(srcDir, "ibmrnr.txt#topicid").getPath()));
        assertFalse(FileUtils.fileExists(new File(srcDir, "ibmrnr").getPath()));
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

    
    @AfterClass
    public static void tearDown() throws IOException {
        TestUtils.forceDelete(tempDir);
    }

}
