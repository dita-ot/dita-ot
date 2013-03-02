/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.util;

import static org.junit.Assert.*;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.dita.dost.util.StringUtils;
import org.junit.Test;

public class TestStringUtils {

    @Test
    public void testAssembleString() {
        String result = null;
        final Collection<Object> input = new ArrayList<Object>();
        result = StringUtils.assembleString((Collection<Object>) null, ";");
        assertEquals("", result);
        result = StringUtils.assembleString(Collections.emptyList(), ";");
        assertEquals("", result);
        input.add("first");
        input.add("second");
        input.add("third");
        result = StringUtils.assembleString(input, ";");
        assertEquals("first;second;third", result);
    }

    @Test
    public void testEscapeXMLString() {
        String result = null;
        final String input = "<this is test of char update for xml href=\" see link: http://www.ibm.com/download.php?abc=123&def=456\">'test' </test>";
        final String expected = "&lt;this is test of char update for xml href=&quot; see link: http://www.ibm.com/download.php?abc=123&amp;def=456&quot;&gt;&apos;test&apos; &lt;/test&gt;";
        result = StringUtils.escapeXML(input);
        assertEquals(expected, result);
    }

    @Test
    public void testEscapeXMLCharArrayIntInt() {
        String result = null;
        final char[] input = "<this is test of char update for xml href=\" see link: http://www.ibm.com/download.php?abc=123&def=456\">'test' </test>".toCharArray();
        final String expected = "&lt;this is test of char update for xml href=&quot; see link: http://www.ibm.com/download.php?abc=123&amp;def=456&quot;&gt;&apos;test&apos; &lt;/test&gt;";
        result = StringUtils.escapeXML(input,0,input.length);
        assertEquals(expected, result);
    }

    @Test
    public void testGetEntity() {
        String result = null;
        result = StringUtils.getEntity("abc");
        assertEquals("&abc;", result);
        result = StringUtils.getEntity("%xyz");
        assertEquals("%xyz;", result);
    }

    @Test
    public void testCheckEntity() {
        assertFalse(StringUtils.checkEntity("lt"));
        assertFalse(StringUtils.checkEntity("gt"));
        assertFalse(StringUtils.checkEntity("quot"));
        assertFalse(StringUtils.checkEntity("amp"));
        assertTrue(StringUtils.checkEntity("abc"));
    }

    @Test
    public void testReplaceAll() {
        String result = null;
        result = StringUtils.replaceAll("abababa", "aba", "c");
        assertEquals("cbc", result);
    }

    @Test
    public void testGetAscii() {
        assertEquals("\\'66\\'6f\\'6f", StringUtils.getAscii("foo"));
        final byte[] nonAscii = "\u00e4\u00f6\u00e5".getBytes(Charset.defaultCharset());
        final StringBuilder buf = new StringBuilder();
        for (final byte b: nonAscii) {
            final String s = Integer.toHexString(b);
            buf.append('\\').append('\'').append(s.substring(s.length() - 2));
        }
        assertEquals(buf.toString(), StringUtils.getAscii("\u00e4\u00f6\u00e5"));
    }

    @Test
    public void testGetExtProps() {
        assertArrayEquals(new String[][] {new String[] {"props", "foo"}, new String[] {"props", "bar"}},
                          StringUtils.getExtProps("a(props foo) a(props bar)"));
        assertArrayEquals(new String[][] {new String[] {"props", "bar"}, new String[] {"props", "qux"}},
                          StringUtils.getExtProps("(topic foo) a(props bar) (topic baz) a(props qux)"));
        assertArrayEquals(new String[][] {new String[] {"props", "foo"}},
                          StringUtils.getExtProps("  a(props   foo  )   "));
        assertArrayEquals(new String[0][0],
                          StringUtils.getExtProps("(topic task)"));
    }

    @Test
    public void testRestoreMap() {
        final Map<String, String> expected = new HashMap<String, String>();
        expected.put("abc", "def");
        expected.put("ghi", "jkl");
        expected.put("mno", "pqr");
        final Map<String, String> result = StringUtils.restoreMap("abc=def,ghi=jkl,mno=pqr");
        assertEquals(expected, result);
    }

    @Test
    public void testIsEmptyString() {
        assertTrue(StringUtils.isEmptyString(null));
        assertTrue(StringUtils.isEmptyString(""));
        assertTrue(StringUtils.isEmptyString("      "));
        assertFalse(StringUtils.isEmptyString("abc"));
    }

    @Test
    public void testSetOrAppend() {
        final String input1 = "input1";
        final String input2 = "input2";
        String result = null;
        result = StringUtils.setOrAppend(null, input2, true);
        assertEquals("input2", result);
        result = StringUtils.setOrAppend(input1, null, true);
        assertEquals("input1", result);
        result = StringUtils.setOrAppend(input1, input2, false);
        assertEquals("input1input2", result);
        result = StringUtils.setOrAppend(input1, input2, true);
        assertEquals("input1 input2", result);

    }

    @Test
    public void testGetLocale() {
        final Locale expected1 = new Locale("zh","cn");
        final Locale result1 = StringUtils.getLocale("zh-cn");
        assertEquals(expected1, result1);
        final Locale expected2 = new Locale("zh");
        final Locale result2 = StringUtils.getLocale("zh_cn");
        assertEquals(expected2, result2);
        final Locale expected3 = new Locale("zh","cn","gb2312");
        final Locale result3 = StringUtils.getLocale("zh-cn-gb2312");
        assertEquals(expected3, result3);
        assertNull(StringUtils.getLocale("xx-1234567890"));
        try {
            assertNull(StringUtils.getLocale("xx-1234567890-xx"));
            fail();
        } catch (final NullPointerException e) {}
        try {
            StringUtils.getLocale(null);
            fail();
        } catch (final NullPointerException e) {}
    }

    @Test
    public void testGetFileName() {
        assertEquals("foo.bar", StringUtils.getFileName("foo.bar.baz", "."));
        assertEquals("foo", StringUtils.getFileName("foo.bar", "."));
        assertEquals("foo", StringUtils.getFileName("foo", "."));
    }

    @Test
    public void testGetMaxFive() {
        assertEquals(Integer.valueOf(5), StringUtils.getMax("1", "2", "3", "4", "5"));
        assertEquals(Integer.valueOf(5), StringUtils.getMax("5", "4", "3", "2", "1"));
        assertEquals(Integer.valueOf(5), StringUtils.getMax("5", "5", "5", "5", "5"));
    }

    @Test
    public void testGetMaxSix() {
        assertEquals(Integer.valueOf(6), StringUtils.getMax("1", "2", "3", "4", "5", "6"));
        assertEquals(Integer.valueOf(6), StringUtils.getMax("6", "5", "4", "3", "2", "1"));
        assertEquals(Integer.valueOf(6), StringUtils.getMax("6", "6", "6", "6", "6", "6"));
    }

}
