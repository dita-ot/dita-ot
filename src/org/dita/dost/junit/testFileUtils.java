package org.dita.dost.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;

import org.dita.dost.module.DebugAndFilterModule;
import org.dita.dost.util.FileUtils;
import org.junit.Ignore;
import org.junit.Test;

public class testFileUtils {

	@Test
	public void testIsHTMLFile() {

		assertTrue(FileUtils.isHTMLFile("file.html"));

		assertTrue(FileUtils.isHTMLFile("file.htm"));
		assertFalse(FileUtils.isHTMLFile("file"));
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
	public void testIsTopicFile() {
		assertTrue(FileUtils.isTopicFile("file.dita")
				&& FileUtils.isTopicFile("file.xml"));
		assertFalse(FileUtils.isTopicFile("file"));
		assertFalse(FileUtils.isTopicFile(""));
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
		assertEquals("../a.dita",FileUtils.getRelativePathFromMap("c:/map/map.ditamap", "c:/a.dita"));
		assertEquals("d:/a.dita",FileUtils.getRelativePathFromMap("c:/map.ditamap", "d:/a.dita"));
		assertEquals("a.dita", FileUtils.getRelativePathFromMap("c:/map1/map2/map.ditamap", "c:/map1/map2/a.dita"));
		assertEquals("../topic/a.dita",FileUtils.getRelativePathFromMap("c:/map1/map.ditamap", "c:/topic/a.dita"));
	}

	@Test
	public void testGetPathtoProject() {
		assertEquals("../../", FileUtils.getPathtoProject("/dir/dir/file.xml"));
		assertEquals("../../", FileUtils.getPathtoProject("dir/dir/file.xml"));
		assertEquals("../", FileUtils.getPathtoProject("dir/file.xml"));
		assertEquals(null, FileUtils.getPathtoProject("file.xml"));
		
	}

	@Test
	public void testResolveTopic() {
		assertEquals("c:\\dir\\file.xml", FileUtils.resolveTopic("c:\\dir","file.xml"));
		assertEquals("c:\\dir\\file.xml#topicid", FileUtils.resolveTopic("c:\\dir","file.xml#topicid"));
		assertEquals("c:\\file.xml", FileUtils.resolveTopic("c:\\dir","..\\file.xml"));
		assertEquals("\\file.xml", FileUtils.resolveTopic("","file.xml"));
		assertEquals("file.xml", FileUtils.resolveTopic(null,"file.xml"));
	}

	@Test
	public void testResolveFile() {
		assertEquals("c:\\dir\\file.xml", FileUtils.resolveFile("c:\\dir","file.xml"));
		assertEquals("c:\\dir\\file.xml", FileUtils.resolveFile("c:\\dir","file.xml#topicid"));
		assertEquals("c:\\file.xml", FileUtils.resolveFile("c:\\dir","..\\file.xml"));
		assertEquals("\\file.xml", FileUtils.resolveFile("","file.xml"));
		assertEquals("file.xml", FileUtils.resolveFile(null,"file.xml"));
	}

	@Test
	public void testNormalizeDirectory() {
		assertEquals("c:\\dir1\\dir2\\file.xml",FileUtils.normalizeDirectory("c:\\dir1", "dir2\\file.xml"));
		assertEquals("c:\\dir1\\file.xml",FileUtils.normalizeDirectory("c:\\dir1\\dir2", "..\\file.xml"));
		assertEquals("\\file.xml",FileUtils.normalizeDirectory("", "\\file.xml#topicid"));
		//should be c:\\file.xml?
		assertEquals("\\c:\\file.xml",FileUtils.normalizeDirectory("", "c:\\file.xml"));
		assertEquals("c:\\file.xml",FileUtils.normalizeDirectory(null, "c:\\file.xml#topicid"));
	}

	@Test
	public void testRemoveRedundantNamesStringString() {
		assertEquals("a\\c\\file.xml",FileUtils.removeRedundantNames("a\\b\\..\\c\\file.xml", File.separator));
		assertEquals("a\\b\\file.xml",FileUtils.removeRedundantNames("a\\.\\b\\.\\file.xml", File.separator));
		assertEquals("..\\a\\file.xml",FileUtils.removeRedundantNames("..\\a\\file.xml", File.separator));
		assertEquals("..\\file.xml", FileUtils.removeRedundantNames("a\\..\\..\\file.xml", File.separator));
		assertEquals("file.xml", FileUtils.removeRedundantNames("a\\b\\..\\..\\file.xml", File.separator));
		assertEquals("\\a\\b\\file.xml", FileUtils.removeRedundantNames("\\a\\.\\b\\c\\..\\file.xml", File.separator));
	}

	@Test
	public void testIsAbsolutePath() {
		if(File.separator.endsWith("\\")){
			assertTrue(FileUtils.isAbsolutePath("C:\\\\file.xml"));
			assertTrue(FileUtils.isAbsolutePath("c:\\\\file.xml"));
			assertFalse(FileUtils.isAbsolutePath(""));
			assertFalse(FileUtils.isAbsolutePath(" "));
			assertFalse(FileUtils.isAbsolutePath("\\dic\\file.xml"));
			assertFalse(FileUtils.isAbsolutePath("file.xml"));
		}else if(File.separator.endsWith("/")){
			assertTrue(FileUtils.isAbsolutePath("/file.xml"));
			assertFalse(FileUtils.isAbsolutePath("file.xml"));
		}else {
			assertFalse(FileUtils.isAbsolutePath("file.xml"));
			assertFalse(FileUtils.isAbsolutePath("/file.xml"));
		}
	}

	@Ignore @Test
	public void testCopyFile() {
		fail("Not yet implemented");
	}

	@Test
	public void testReplaceExtName() {
		// initial the extName of Class DebugAndFilterModule.
		DebugAndFilterModule.extName = ".dita";
		assertEquals("filename.dita", FileUtils.replaceExtName("filename.xml"));
		// if there is a topic marked with sharp
		assertEquals("filename.dita#topicid", FileUtils
				.replaceExtName("filename.xml#topicid"));
		// if the input just is a topicid
		assertEquals("#topicid", FileUtils.replaceExtName("#topicid"));
		// if there is an extra dot.
		assertEquals("file.name.dita", FileUtils
				.replaceExtName("file.name.xml"));
		assertEquals("file.name.dita#topicid", FileUtils
				.replaceExtName("file.name.xml#topicid"));
		// if there is no extension.
		assertEquals("file", FileUtils.replaceExtName("file"));
	}

	@Test
	public void testFileExists() {
		assertTrue(FileUtils.fileExists("test-stub\\ibmrnr.txt"));
		assertTrue(FileUtils.fileExists("test-stub\\ibmrnr.txt#topicid"));
		assertFalse(FileUtils.fileExists("test-stub\\ibmrnr"));
	}

}
