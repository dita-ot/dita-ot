/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for
 * applicable licenses.
 */
package org.dita.dost.util;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.dita.dost.TestUtils;
import org.dita.dost.util.FilterUtils.Action;
import org.dita.dost.util.FilterUtils.FilterKey;

import org.xml.sax.helpers.AttributesImpl;

import org.junit.Test;

public class FilterUtilsTest {

    private static final Map<FilterKey, Action> filterMap;
    static {
        final Map<FilterKey, Action> fm = new HashMap<FilterKey, Action>();
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
        final FilterUtils f = new FilterUtils();
        f.setFilterMap(filterMap);

        assertFalse(f.needExclude(new AttributesImpl(), new String[0][0]));
    }

    @Test
    public void testNeedExcludeDefaultExclude() {
        final FilterUtils f = new FilterUtils();
        f.setLogger(new TestUtils.TestLogger());
        final Map<FilterKey, Action> fm = new HashMap<FilterKey, Action>(filterMap);
        fm.put(new FilterKey("platform", null), Action.EXCLUDE);
        f.setFilterMap(fm);

        assertFalse(f.needExclude(new AttributesImpl(), new String[0][0]));

        final AttributesImpl amigaUnix = new AttributesImpl();
        XMLUtils.addOrSetAttribute(amigaUnix, "platform", "amiga unix windows");
        assertFalse(f.needExclude(amigaUnix, new String[0][0]));

        final AttributesImpl amiga = new AttributesImpl();
        XMLUtils.addOrSetAttribute(amiga, "platform", "amiga windows");
        assertTrue(f.needExclude(amiga, new String[0][0]));

        final AttributesImpl windows = new AttributesImpl();
        XMLUtils.addOrSetAttribute(windows, "platform", "windows");
        assertTrue(f.needExclude(windows, new String[0][0]));
    }

    @Test
    public void testNeedExcludeDefaultInclude() {
        final FilterUtils f = new FilterUtils();
        f.setLogger(new TestUtils.TestLogger());
        final Map<FilterKey, Action> fm = new HashMap<FilterKey, Action>(filterMap);
        fm.put(new FilterKey("platform", null), Action.INCLUDE);
        f.setFilterMap(fm);

        final AttributesImpl amigaUnix = new AttributesImpl();
        XMLUtils.addOrSetAttribute(amigaUnix, "platform", "amiga unix windows");
        assertFalse(f.needExclude(amigaUnix, new String[0][0]));

        final AttributesImpl amiga = new AttributesImpl();
        XMLUtils.addOrSetAttribute(amiga, "platform", "amiga windows");
        assertFalse(f.needExclude(amiga, new String[0][0]));

        final AttributesImpl windows = new AttributesImpl();
        XMLUtils.addOrSetAttribute(windows, "platform", "windows");
        assertTrue(f.needExclude(windows, new String[0][0]));
    }

    @Test
    public void testNeedExclude() {
        final FilterUtils f = new FilterUtils();
        f.setLogger(new TestUtils.TestLogger());
        f.setFilterMap(filterMap);

        final AttributesImpl amigaUnix = new AttributesImpl();
        XMLUtils.addOrSetAttribute(amigaUnix, "platform", "amiga unix windows");
        assertFalse(f.needExclude(amigaUnix, new String[0][0]));

        final AttributesImpl amiga = new AttributesImpl();
        XMLUtils.addOrSetAttribute(amiga, "platform", "amiga windows");
        assertFalse(f.needExclude(amiga, new String[0][0]));

        final AttributesImpl windows = new AttributesImpl();
        XMLUtils.addOrSetAttribute(windows, "platform", "windows");
        assertTrue(f.needExclude(windows, new String[0][0]));
    }

    @Test
    public void testNeedExcludeMultipleAttributes() {
        final FilterUtils f = new FilterUtils();
        f.setLogger(new TestUtils.TestLogger());
        f.setFilterMap(filterMap);

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
    
}
