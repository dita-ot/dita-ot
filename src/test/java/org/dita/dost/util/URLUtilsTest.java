package org.dita.dost.util;

import static org.junit.Assert.*;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Test;

public class URLUtilsTest {

    @Test
    public void testCorrectFile() {
        //fail("Not yet implemented");
    }

    @Test
    public void testCorrectURL() {
        //fail("Not yet implemented");
    }

    @Test
    public void testDecode() {
    	assertEquals("foo bar.dita", URLUtils.decode("foo%20bar.dita"));
        assertEquals("foo bar.dita", URLUtils.decode("foo+bar.dita"));
        
        assertEquals("f\u00f6\u00e5.dita", URLUtils.decode("f%C3%B6%C3%A5.dita"));
        
        assertEquals("foo/bar.dita", URLUtils.decode("foo/bar.dita"));
        assertEquals("foo\\bar.dita", URLUtils.decode("foo%5Cbar.dita"));
        
        assertEquals("foo?bar=baz&qux=quxx", URLUtils.decode("foo?bar=baz&qux=quxx"));
    }
    
    @Test
    public void testUncorrect() {
        //fail("Not yet implemented");
    }

    @Test
    public void testGetCanonicalFileFromFileUrl() {
        //fail("Not yet implemented");
    }

    @Test
    public void testCorrectStringBoolean() {
        //fail("Not yet implemented");
    }

    @Test
    public void testGetURL() {
        //fail("Not yet implemented");
    }

    @Test
    public void testClean() {
        assertEquals("foo%20bar.dita", URLUtils.clean("foo bar.dita"));
        assertEquals("foo+bar.dita", URLUtils.clean("foo+bar.dita"));
        assertEquals("foo%20bar.dita", URLUtils.clean("foo%20bar.dita"));
        
        assertEquals("f%C3%B6%C3%A5.dita", URLUtils.clean("f\u00f6\u00e5.dita"));
        
        assertEquals("foo/bar.dita", URLUtils.clean("foo/bar.dita"));
        assertEquals("foo/bar.dita", URLUtils.clean("foo\\bar.dita"));
        
        assertEquals("foo?bar=baz&qux=quxx", URLUtils.clean("foo?bar=baz&qux=quxx"));
    }
    
    @Test
    public void testCleanASCII() {
        assertEquals("foo.dita", URLUtils.clean("foo.dita", true));
        assertEquals("f%C3%B6%C3%A5.dita", URLUtils.clean("f\u00f6\u00e5.dita", true));
        assertEquals("foo.dita", URLUtils.clean("foo.dita", false));
        assertEquals("f\u00f6\u00e5.dita", URLUtils.clean("f\u00f6\u00e5.dita", false));
    }

    @Test
    public void testDirectoryContains() throws URISyntaxException {
        assertTrue(URLUtils.directoryContains(new URI("file:/src/"), new URI("file:/src/test.txt")));
        assertFalse(URLUtils.directoryContains(new URI("file:/src/"), new URI("file:/src/")));
        assertFalse(URLUtils.directoryContains(new URI("file:/src/test/"), new URI("file:/src/")));
        assertFalse(URLUtils.directoryContains(new URI("file:/src/"), new URI("file:/src/../test.txt")));
    }
    
    @Test
    public void testIsAbsolute() throws URISyntaxException {
        assertTrue(URLUtils.isAbsolute(new URI("file:/foo")));
        assertTrue(URLUtils.isAbsolute(new URI("/foo")));
        assertFalse(URLUtils.isAbsolute(new URI("file:foo")));
        assertFalse(URLUtils.isAbsolute(new URI("foo")));
    }
 
    @Test
    public void testToFileString() throws Exception {
        final Method method = URLUtils.class.getDeclaredMethod("toFile", String.class);
        method.setAccessible(true);        
        assertEquals(new File("test.txt"), method.invoke(null, "test.txt"));
        assertEquals(new File("foo bar.txt"), method.invoke(null, "foo%20bar.txt"));
        assertEquals(new File("foo" + File.separator + "bar.txt"), method.invoke(null, "foo/bar.txt"));
    }
    
    @Test
    public void testToFileUri() throws URISyntaxException {
        assertEquals(new File("test.txt"), URLUtils.toFile(new URI("test.txt")));
        assertEquals(new File(File.separator + "test.txt"), URLUtils.toFile(new URI("file:/test.txt")));
        assertEquals(new File("foo bar.txt"), URLUtils.toFile(new URI("foo%20bar.txt")));
        assertEquals(new File(File.separator + "foo bar.txt"), URLUtils.toFile(new URI("file:/foo%20bar.txt")));
        assertEquals(new File("foo" + File.separator + "bar.txt"), URLUtils.toFile(new URI("foo/bar.txt")));
        assertEquals(new File(File.separator + "foo" + File.separator + "bar.txt"), URLUtils.toFile(new URI("file:/foo/bar.txt")));
    }
    
    @Test
    public void testToUri() throws URISyntaxException {
        assertEquals(new URI("test.txt"), URLUtils.toURI(new File("test.txt")));
        assertEquals(new URI("foo%20bar.txt"), URLUtils.toURI(new File("foo bar.txt")));
        assertEquals(new URI("foo/bar.txt"), URLUtils.toURI(new File("foo" + File.separator + "bar.txt")));
    }

    @Test
    public void testGetRelativePathFromMap() throws URISyntaxException {
        assertEquals(new URI("../a.dita"), URLUtils.getRelativePath(new URI("file:/map/map.ditamap"), new URI("file:/a.dita")));
        assertEquals(new URI("../a.dita"), URLUtils.getRelativePath(new URI("file:/map/"), new URI("file:/a.dita")));
        assertEquals(new URI("a.dita"), URLUtils.getRelativePath(new URI("file:/map.ditamap"), new URI("file:/a.dita")));
        assertEquals(new URI("a.dita"), URLUtils.getRelativePath(new URI("file:/map1/map2/map.ditamap"), new URI("file:/map1/map2/a.dita")));
        assertEquals(new URI("a.dita"), URLUtils.getRelativePath(new URI("file:/map1/map2/"), new URI("file:/map1/map2/a.dita")));
        assertEquals(new URI("map2/a.dita"), URLUtils.getRelativePath(new URI("file:/map1/map.ditamap"), new URI("file:/map1/map2/a.dita")));
        assertEquals(new URI("map2/a.dita"), URLUtils.getRelativePath(new URI("file:/map1/"), new URI("file:/map1/map2/a.dita")));
        assertEquals(new URI("../topic/a.dita"), URLUtils.getRelativePath(new URI("file:/map1/map.ditamap"), new URI("file:/topic/a.dita")));
        assertEquals(new URI("a.dita#bar"), URLUtils.getRelativePath(new URI("file:/map.ditamap#foo"), new URI("file:/a.dita#bar")));
        assertEquals(new URI("a.dita"), URLUtils.getRelativePath(new URI("file:/a.dita"), new URI("file:/a.dita")));
        assertEquals(new URI("#bar"), URLUtils.getRelativePath(new URI("file:/a.dita#foo"), new URI("file:/a.dita#bar")));
        assertEquals(new URI("#bar"), URLUtils.getRelativePath(new URI("file:/a.dita"), new URI("#bar")));
        assertEquals(new URI("file://a.dita"), URLUtils.getRelativePath(new URI("/map.ditamap"), new URI("file://a.dita")));
        assertEquals(new URI("https://localhost/map.ditamap") ,URLUtils.getRelativePath(new URI("http://localhost/map.ditamap"), new URI("https://localhost/map.ditamap")));
        assertEquals(new URI("http:///map.ditamap"), URLUtils.getRelativePath(new URI("http://localhost/map.ditamap"), new URI("http:///map.ditamap")));
    }

    @Test
    public void testGetRelativePath() throws URISyntaxException {
        assertEquals(new URI("../"), URLUtils.getRelativePath(new URI("map/map.ditamap")));
        assertEquals(null, URLUtils.getRelativePath(new URI("map.ditamap")));
        assertEquals(new URI("../../"), URLUtils.getRelativePath(new URI("map1/map2/map.ditamap")));
    } 

    @Test
    public void testSetFragment() throws URISyntaxException {
        assertEquals(new URI("foo#baz"), URLUtils.setFragment(new URI("foo#bar"), "baz"));
        assertEquals(new URI("foo#baz"), URLUtils.setFragment(new URI("foo#"), "baz"));
        assertEquals(new URI("foo#baz"), URLUtils.setFragment(new URI("foo"), "baz"));
        assertEquals(new URI("#baz"), URLUtils.setFragment(new URI("#bar"), "baz"));
        assertEquals(new URI("foo"), URLUtils.setFragment(new URI("foo#bar"), null));
        assertEquals(new URI("foo"), URLUtils.setFragment(new URI("foo#"), null));
        assertEquals(new URI("foo"), URLUtils.setFragment(new URI("foo"), null));
        assertEquals(new URI(""), URLUtils.setFragment(new URI("#bar"), null));
        assertEquals(new URI("file:/foo/bar#baz"), URLUtils.setFragment(new URI("file:/foo/bar"), "baz"));
        assertEquals(new URI("file:/foo/bar"), URLUtils.setFragment(new URI("file:/foo/bar"), null));
        assertEquals(new URI("file://localhost/foo/bar#baz"), URLUtils.setFragment(new URI("file://localhost/foo/bar"), "baz"));
        assertEquals(new URI("file://localhost/foo/bar"), URLUtils.setFragment(new URI("file://localhost/foo/bar"), null));
        assertEquals(new URI("urn:foo:bar#baz"), URLUtils.setFragment(new URI("urn:foo:bar"), "baz"));
        assertEquals(new URI("urn:foo:bar"), URLUtils.setFragment(new URI("urn:foo:bar"), null));
    }
    
    @Test
    public void testStripFragment() throws URISyntaxException {
        assertEquals(new URI("foo"), URLUtils.stripFragment(new URI("foo#bar")));
        assertEquals(new URI("foo"), URLUtils.stripFragment(new URI("foo#")));
        assertEquals(new URI("foo"), URLUtils.stripFragment(new URI("foo")));
    }

    @Test
    public void testAddSuffix() throws URISyntaxException {
        assertEquals(new URI("foo-1.bar"), URLUtils.addSuffix(new URI("foo.bar"), "-1"));
        assertEquals(new URI("baz/foo-1.bar"), URLUtils.addSuffix(new URI("baz/foo.bar"), "-1"));
        assertEquals(new URI("baz.qux/foo-1.bar"), URLUtils.addSuffix(new URI("baz.qux/foo.bar"), "-1"));
    }

}
