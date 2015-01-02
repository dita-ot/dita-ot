/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.util;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;

import org.junit.Test;

public class TestStringUtils {

    @Test
    public void testAssembleString() {
        String result = null;
        final Collection<Object> input = new ArrayList<Object>();
        result = StringUtils.join((Collection<Object>) null, ";");
        assertEquals("", result);
        result = StringUtils.join(Collections.emptyList(), ";");
        assertEquals("", result);
        input.add("first");
        input.add("second");
        input.add("third");
        result = StringUtils.join(input, ";");
        assertEquals("first;second;third", result);
    }

    @Test
    public void testReplaceAll() {
        String result = null;
        result = StringUtils.replaceAll("abababa", "aba", "c");
        assertEquals("cbc", result);
    }

    @Test
    public void testGetExtProps() {
        assertArrayEquals(new String[][] {{"props", "foo"}, {"props", "bar"}},
                          StringUtils.getExtProps("a(props foo) a(props bar)"));
        assertArrayEquals(new String[][] {{"props", "bar"}, {"props", "qux"}},
                          StringUtils.getExtProps("(topic foo) a(props bar) (topic baz) a(props qux)"));
        assertArrayEquals(new String[][] {{"props", "foo"}},
                          StringUtils.getExtProps("  a(props   foo  )   "));
        assertArrayEquals(new String[0][0],
                          StringUtils.getExtProps("(topic task)"));
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
    public void testNormalizeAndCollapseWhitespace() {
        assertEquals("foo bar baz", runNormalizeAndCollapseWhitespace("foo bar baz"));
        assertEquals(" foo bar baz ", runNormalizeAndCollapseWhitespace(" foo bar baz "));
        assertEquals(" foo bar baz ", runNormalizeAndCollapseWhitespace("  foo  bar  baz  "));
        assertEquals(" foo bar baz ", runNormalizeAndCollapseWhitespace("\nfoo\nbar\nbaz\n"));
    }
    
    private String runNormalizeAndCollapseWhitespace(final String text) {
        final StringBuilder sb = new StringBuilder(text);
        StringUtils.normalizeAndCollapseWhitespace(sb);
        return sb.toString();
    }
    
}
