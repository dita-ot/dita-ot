/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2012 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.util;

import static java.util.Arrays.asList;
import static java.util.Collections.*;
import static org.junit.Assert.*;

import java.util.*;

import com.google.common.collect.ImmutableMap;
import org.dita.dost.TestUtils;
import org.dita.dost.util.FilterUtils.Action;
import org.dita.dost.util.FilterUtils.FilterKey;

import org.dita.dost.util.FilterUtils.Flag;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

import org.junit.Test;

public class FilterUtilsTest {

    private static final Map<FilterKey, Action> filterMap;
    static {
        final Map<FilterKey, Action> fm = new HashMap<>();
        fm.put(new FilterKey("platform", "unix"), Action.INCLUDE);
        fm.put(new FilterKey("platform", "osx"), Action.INCLUDE);
        fm.put(new FilterKey("platform", "linux"), Action.INCLUDE);
        fm.put(new FilterKey("platform", "windows"), Action.EXCLUDE);
        fm.put(new FilterKey("audience", "expert"), Action.INCLUDE);
        fm.put(new FilterKey("audience", "novice"), Action.EXCLUDE);
        filterMap = Collections.unmodifiableMap(fm);
    }

    @Test
    public void testNeedExcludeNoAttribute() {
        final FilterUtils f = new FilterUtils(false, filterMap, null, null);

        assertFalse(f.needExclude(new AttributesImpl(), new String[0][0]));
    }

    @Test
    public void testNeedExcludeDefaultExclude() {
        final Map<FilterKey, Action> fm = new HashMap<>(filterMap);
        fm.put(new FilterKey("platform", null), Action.EXCLUDE);
        final FilterUtils f = new FilterUtils(false, fm, null, null);
        f.setLogger(new TestUtils.TestLogger());

        assertFalse(f.needExclude(new AttributesImpl(), new String[0][0]));
        assertFalse(f.needExclude(attr("platform", "amiga unix windows"), new String[0][0]));
        assertTrue(f.needExclude(attr("platform", "amiga windows"), new String[0][0]));
        assertTrue(f.needExclude(attr("platform", "windows"), new String[0][0]));
    }

    @Test
    public void testNeedExcludeDefaultInclude() {
        final Map<FilterKey, Action> fm = new HashMap<>(filterMap);
        fm.put(new FilterKey("platform", null), Action.INCLUDE);
        final FilterUtils f = new FilterUtils(false, fm, null, null);
        f.setLogger(new TestUtils.TestLogger());

        assertFalse(f.needExclude(attr("platform", "amiga unix windows"), new String[0][0]));
        assertFalse(f.needExclude(attr("platform", "amiga windows"), new String[0][0]));
        assertTrue(f.needExclude(attr("platform", "windows"), new String[0][0]));
    }

    @Test
    public void testNeedExclude() {
        final FilterUtils f = new FilterUtils(false, filterMap, null, null);
        f.setLogger(new TestUtils.TestLogger());

        assertFalse(f.needExclude(attr("platform", "amiga unix windows"), new String[0][0]));
        assertFalse(f.needExclude(attr("platform", "amiga windows"), new String[0][0]));
        assertTrue(f.needExclude(attr("platform", "windows"), new String[0][0]));
    }

    @Test
    public void testNeedExcludeMultipleAttributes() {
        final FilterUtils f = new FilterUtils(false, filterMap, null, null);
        f.setLogger(new TestUtils.TestLogger());

        final AttributesImpl amigaUnix = new AttributesImpl();
        XMLUtils.addOrSetAttribute(amigaUnix, "platform", "amiga unix windows");
        XMLUtils.addOrSetAttribute(amigaUnix, "audience", "expert");
        assertFalse(f.needExclude(amigaUnix, new String[0][0]));

        final AttributesImpl amiga = new AttributesImpl();
        XMLUtils.addOrSetAttribute(amiga, "platform", "amiga windows");
        XMLUtils.addOrSetAttribute(amiga, "audience", "expert");
        assertFalse(f.needExclude(amiga, new String[0][0]));

        final AttributesImpl windows = new AttributesImpl();
        XMLUtils.addOrSetAttribute(windows, "platform", "windows");
        XMLUtils.addOrSetAttribute(windows, "audience", "novice");
        assertTrue(f.needExclude(windows, new String[0][0]));
    }

    @Test
    public void testNeedExcludeDomainAttribute() {
        final Map<FilterKey, Action> fm = new HashMap<>();
        fm.put(new FilterKey("os", "amiga"), Action.INCLUDE);
        fm.put(new FilterKey("os", null), Action.EXCLUDE);
        final FilterUtils f = new FilterUtils(false, fm, null, null);
        f.setLogger(new TestUtils.TestLogger());

        assertFalse(f.needExclude(attr("os", "amiga unix windows"), new String[][] {{"props", "os"}}));
        assertFalse(f.needExclude(attr("os", "amiga windows"), new String[][] {{"props", "os"}}));
        assertFalse(f.needExclude(attr("os", "amiga windows"), new String[][] {{"props", "os", "gui"}}));
        assertFalse(f.needExclude(attr("gui", "amiga windows"),new String[][] {{"props", "os", "gui"}}));
        assertTrue(f.needExclude(attr("os", "windows"), new String[][] {{"props", "os"}}));
    }
    
    @Test
    public void testNeedExcludeLabel() {
        final Map<FilterKey, Action> fm = new HashMap<>();
        fm.put(new FilterKey("os", "amiga"), Action.INCLUDE);
        fm.put(new FilterKey("os", null), Action.EXCLUDE);
        final FilterUtils f = new FilterUtils(false, fm, null, null);
        f.setLogger(new TestUtils.TestLogger());

        assertFalse(f.needExclude(attr("props", "os(amiga unix windows)"), new String[][] {{"props", "os"}}));
        assertFalse(f.needExclude(attr("props", "os(amiga windows)"), new String[][] {{"props", "os", "gui"}}));
        assertFalse(f.needExclude(attr("props", "gui(amiga windows)"), new String[][] {{"props", "os", "gui"}}));
        assertTrue(f.needExclude(attr("props", "os(windows)"), new String[][] {{"props", "os"}}));
        assertTrue(f.needExclude(attr("props", "   os(   windows   )   "), new String[][] {{"props", "os"}}));
    }
    
    @Test
    public void testNeedExcludeOtherpropsLabel() {
        final Map<FilterKey, Action> fm = new HashMap<>();
        fm.put(new FilterKey("os", "amiga"), Action.INCLUDE);
        fm.put(new FilterKey("os", null), Action.EXCLUDE);
        final FilterUtils f = new FilterUtils(false, fm, null, null);
        f.setLogger(new TestUtils.TestLogger());

        assertFalse(f.needExclude(attr("otherprops", "os(amiga unix windows)"), new String[0][0]));
        assertFalse(f.needExclude(attr("otherprops", "os(amiga windows)"), new String[0][0]));
        assertTrue(f.needExclude(attr("otherprops", "os(windows)"), new String[0][0]));
    }

    @Test
    public void testgetFlagsDefaultFlag() {
        final Flag flag = new Flag("red", null, null, null, null, null);
        final FilterUtils f = new FilterUtils(false,
                ImmutableMap.<FilterKey, Action>builder()
                        .put(new FilterKey("platform", null), flag)
                        .build(), null, null);
        f.setLogger(new TestUtils.TestLogger());

        assertEquals(
                emptySet(),
                f.getFlags(new AttributesImpl(), new String[0][0]));
        assertEquals(
                singleton(flag),
                f.getFlags(attr("platform", "amiga unix windows"), new String[0][0]));
    }

    @Test
    public void testGetFlags() {
        final Flag flag = new Flag("red", null, null, null, null, null);
        final FilterUtils f = new FilterUtils(false,
                ImmutableMap.<FilterKey, Action>builder()
                        .put(new FilterKey("platform", "unix"), flag)
                        .put(new FilterKey("platform", "osx"), flag)
                        .put(new FilterKey("platform", "linux"), flag)
                        .put(new FilterKey("audience", "expert"), flag)
                        .build(), null, null);
        f.setLogger(new TestUtils.TestLogger());

        assertEquals(
                singleton(flag),
                f.getFlags(attr("platform", "amiga unix windows"), new String[0][0]));
        assertEquals(
                singleton(flag),
                f.getFlags(attr("platform", "amiga unix"), new String[0][0]));
        assertEquals(
                emptySet(),
                f.getFlags(attr("platform", "amiga"), new String[0][0]));
        assertEquals(
                emptySet(),
                f.getFlags(attr("platform", "windows"), new String[0][0]));
    }

    // DITA 1.3

    @Test
    public void testExtCheckExclude() {
        final Map<FilterKey, Action> fm = new HashMap<>();
        fm.put(new FilterKey("os", "amiga"), Action.INCLUDE);
        fm.put(new FilterKey("os", null), Action.EXCLUDE);
        fm.put(new FilterKey("platform", null), Action.EXCLUDE);
        final FilterUtils f = new FilterUtils(false, fm, null, null);
        f.setLogger(new TestUtils.TestLogger());

        assertFalse(f.extCheckExclude(new String[] {"platform", "os"}, Arrays.asList("amiga", "unix", "windows")));
        assertTrue(f.extCheckExclude(new String[] {"platform", "os"}, Arrays.asList("osx")));
        assertFalse(f.extCheckExclude(new String[] {"platform", "os"}, Arrays.asList( "unix", "amiga", "windows")));
    }
    
    @Test
    public void testNeedExcludeGroup() {
        final Map<FilterKey, Action> fm = new HashMap<>();
        fm.put(new FilterKey("os", "amiga"), Action.INCLUDE);
        fm.put(new FilterKey("os", null), Action.EXCLUDE);
        fm.put(new FilterKey("platform", null), Action.EXCLUDE);
        final FilterUtils f = new FilterUtils(false, fm, null, null);
        f.setLogger(new TestUtils.TestLogger());

        assertFalse(f.needExclude(attr("platform", "os(amiga unix windows)"), new String[0][0]));
        assertFalse(f.needExclude(attr("platform", "os(amiga windows)"), new String[0][0]));
        assertTrue(f.needExclude(attr("platform", "gui(amiga windows)"), new String[0][0]));
        assertTrue(f.needExclude(attr("platform", "os(windows)"), new String[0][0]));
        assertTrue(f.needExclude(attr("platform", "   os(   windows   )   "), new String[0][0]));
    }
    

    @Test
    public void testNeedExcludeGroupMultiple() {
        final Map<FilterKey, Action> fm = new HashMap<>();
        fm.put(new FilterKey("os", "amiga"), Action.EXCLUDE);
        fm.put(new FilterKey("os", "windows"), Action.EXCLUDE);
        fm.put(new FilterKey("os", null), Action.INCLUDE);
        fm.put(new FilterKey("platform", null), Action.INCLUDE);
        final FilterUtils f = new FilterUtils(false, fm, null, null);
        f.setLogger(new TestUtils.TestLogger());

        assertFalse(f.needExclude(attr("platform", "os(amiga unix windows) database(mongo)"), new String[0][0]));
        assertTrue(f.needExclude(attr("platform", "os(amiga windows) database(mongo)"), new String[0][0]));
        assertFalse(f.needExclude(attr("platform", "gui(amiga windows) database(mongo)"), new String[0][0]));
        assertTrue(f.needExclude(attr("platform", "os(windows) database(mongo)"), new String[0][0]));
        assertTrue(f.needExclude(attr("platform", "   os(   windows   )   database(  mongo  )   "), new String[0][0]));
    }
    
    @Test
    public void testNeedExcludeMixedGroups() {
        final Map<FilterKey, Action> fm = new HashMap<>();
        fm.put(new FilterKey("platform", "unix"), Action.EXCLUDE);
        fm.put(new FilterKey("platform", "windows"), Action.EXCLUDE);
        fm.put(new FilterKey("platform", null), Action.INCLUDE);
        final FilterUtils f = new FilterUtils(false, fm, null, null);
        f.setLogger(new TestUtils.TestLogger());

        assertFalse(f.needExclude(attr("platform", "windows database(mongodb couchbase) unix osx"), new String[0][0]));
        assertTrue(f.needExclude(attr("platform", "windows database(mongodb couchbase) unix"), new String[0][0]));
        assertTrue(f.needExclude(attr("platform", "database(mongodb couchbase) unix"), new String[0][0]));
    }
    
    @Test
    public void testGetUngroupedValue() {
        final FilterUtils f = new FilterUtils(false);
        
        {
            final Map<String, List<String>> exp = new HashMap<String, List<String>>();
            exp.put(null, Arrays.asList("foo", "bar", "bax"));
            assertEquals(exp, f.getGroups("foo bar bax"));
        }
        {
            final Map<String, List<String>> exp = new HashMap<String, List<String>>();
            exp.put(null, Arrays.asList("foo", "bar"));
            exp.put("group", Arrays.asList("a", "b", "c"));
            assertEquals(exp, f.getGroups("foo group(a b c) bar"));
        }
        {
            final Map<String, List<String>> exp = new HashMap<String, List<String>>();
            exp.put(null, Arrays.asList("foo"));
            exp.put("group", Arrays.asList("a", "b", "c"));
            assertEquals(exp, f.getGroups("foo group(a b c)"));
        }
        {
            final Map<String, List<String>> exp = new HashMap<String, List<String>>();
            exp.put(null, Arrays.asList("bar"));
            exp.put("group", Arrays.asList("a", "b", "c"));
            assertEquals(exp, f.getGroups("group(a b c) bar"));
        }
        {
            final Map<String, List<String>> exp = new HashMap<String, List<String>>();
            exp.put(null, Arrays.asList("foo", "bar", "baz"));
            exp.put("group1", Arrays.asList("a", "b", "c"));
            exp.put("group2", Arrays.asList("d", "e", "f"));
            assertEquals(exp, f.getGroups("foo group1(a b c) bar group2(d e f) baz"));
        }
        {
            final Map<String, List<String>> exp = new HashMap<String, List<String>>();
            exp.put("group", Arrays.asList("a", "b", "c"));
            assertEquals(exp, f.getGroups("group(a b) group(c)"));
        }
        {
            final Map<String, List<String>> exp = new HashMap<String, List<String>>();
            exp.put("group2", Arrays.asList("a"));
            assertEquals(exp, f.getGroups("group1() group2(a)"));
        }
    }
    
    private Attributes attr(final String name, final String value) {
        final AttributesImpl res = new AttributesImpl();
        XMLUtils.addOrSetAttribute(res, name, value);
        return res;
    }
    
    @Test
    public void testGetFlagLabel() {
        final Flag flagRed = new Flag("red", null, null, null, null, null);
        final Flag flagBlue = new Flag("blue", null, null, null, null, null);
        final FilterUtils f = new FilterUtils(false,
                ImmutableMap.<FilterKey, Action>builder()
                        .put(new FilterKey("os", "amiga"), flagRed)
                        .put(new FilterKey("os", null), flagBlue)
                        .build(), null, null);
        f.setLogger(new TestUtils.TestLogger());

        assertEquals(
                new HashSet(asList(flagRed, flagBlue)),
                f.getFlags(attr("props", "os(amiga unix windows)"), new String[][] {{"props", "os"}}));
        assertEquals(
                new HashSet(asList(flagRed, flagBlue)),
                f.getFlags(attr("props", "os(amiga windows)"), new String[][] {{"props", "os", "gui"}}));
        assertEquals(
                new HashSet(asList(flagRed, flagBlue)),
                f.getFlags(attr("props", "gui(amiga windows)"), new String[][] {{"props", "os", "gui"}}));
        assertEquals(
                singleton(flagBlue),
                f.getFlags(attr("props", "os(windows)"), new String[][] {{"props", "os"}}));
        assertEquals(
                singleton(flagBlue),
                f.getFlags(attr("props", "   os(   windows   )   "), new String[][] {{"props", "os"}}));
    }

    @Test
    public void testConflict() {
        final Flag flagRed = new Flag("red", null, null, null, null, null);
        final Flag flagBlue = new Flag("blue", null, null, null, null, null);
        final FilterUtils f = new FilterUtils(false,
                ImmutableMap.<FilterKey, Action>builder()
                        .put(new FilterKey("os", "amiga"), flagRed)
                        .put(new FilterKey("os", null), flagBlue)
                        .build(), "yellow", "green");
        f.setLogger(new TestUtils.TestLogger());

        final Flag flagYellow = new Flag("yellow", null, null, null, null, null);
        assertEquals(
                singleton(flagYellow),
                f.getFlags(attr("props", "os(amiga unix windows)"), new String[][] {{"props", "os"}}));
        assertEquals(
                singleton(flagYellow),
                f.getFlags(attr("props", "os(amiga windows)"), new String[][] {{"props", "os", "gui"}}));
        assertEquals(
                singleton(flagYellow),
                f.getFlags(attr("props", "gui(amiga windows)"), new String[][] {{"props", "os", "gui"}}));
        assertEquals(
                singleton(flagBlue),
                f.getFlags(attr("props", "os(windows)"), new String[][] {{"props", "os"}}));
        assertEquals(
                singleton(flagBlue),
                f.getFlags(attr("props", "   os(   windows   )   "), new String[][] {{"props", "os"}}));
    }
}
