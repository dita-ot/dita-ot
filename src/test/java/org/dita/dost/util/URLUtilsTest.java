package org.dita.dost.util;

import static org.junit.Assert.*;

import java.io.File;
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
        assertEquals("foo%5Cbar.dita", URLUtils.clean("foo\\bar.dita"));
        
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
    public void testToFileString() {
        assertEquals(new File("test.txt"), URLUtils.toFile("test.txt"));
        assertEquals(new File("foo bar.txt"), URLUtils.toFile("foo%20bar.txt"));
        assertEquals(new File("foo" + File.separator + "bar.txt"), URLUtils.toFile("foo/bar.txt"));
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
}
