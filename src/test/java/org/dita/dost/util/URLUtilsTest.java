package org.dita.dost.util;

import static org.junit.Assert.*;

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

}
