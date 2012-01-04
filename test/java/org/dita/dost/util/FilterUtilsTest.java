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

import org.xml.sax.helpers.AttributesImpl;

import org.junit.Test;

public class FilterUtilsTest {

    private static final Map<String, String> filterMap;
    static {
        final Map<String, String> fm = new HashMap<String, String>();
        fm.put("platform=unix", "include");
        fm.put("platform=osx", "include");
        fm.put("platform=linux", "include");
        fm.put("platform=windows", "exclude");
        fm.put("audience=expert", "include");
        fm.put("audience=novice", "exclude");
        filterMap = Collections.unmodifiableMap(fm);
    }

    @Test
    public void testNeedExcludeNoAttribute() {
        final FilterUtils f = new FilterUtils();
        f.setFilterMap(filterMap);

        assertFalse(f.needExclude(new AttributesImpl(), ""));
    }

    @Test
    public void testNeedExcludeDefaultExclude() {
        final FilterUtils f = new FilterUtils();
        final Map<String, String> fm = new HashMap<String, String>(filterMap);
        fm.put("platform", "exclude");
        f.setFilterMap(fm);

        assertFalse(f.needExclude(new AttributesImpl(), ""));

        final AttributesImpl amigaUnix = new AttributesImpl();
        XMLUtils.addOrSetAttribute(amigaUnix, "platform", "amiga unix windows");
        assertFalse(f.needExclude(amigaUnix, ""));

        final AttributesImpl amiga = new AttributesImpl();
        XMLUtils.addOrSetAttribute(amiga, "platform", "amiga windows");
        assertTrue(f.needExclude(amiga, ""));

        final AttributesImpl windows = new AttributesImpl();
        XMLUtils.addOrSetAttribute(windows, "platform", "windows");
        assertTrue(f.needExclude(windows, ""));
    }

    @Test
    public void testNeedExcludeDefaultInclude() {
        final FilterUtils f = new FilterUtils();
        final Map<String, String> fm = new HashMap<String, String>(filterMap);
        fm.put("platform", "include");
        f.setFilterMap(fm);

        final AttributesImpl amigaUnix = new AttributesImpl();
        XMLUtils.addOrSetAttribute(amigaUnix, "platform", "amiga unix windows");
        assertFalse(f.needExclude(amigaUnix, ""));

        final AttributesImpl amiga = new AttributesImpl();
        XMLUtils.addOrSetAttribute(amiga, "platform", "amiga windows");
        assertFalse(f.needExclude(amiga, ""));

        final AttributesImpl windows = new AttributesImpl();
        XMLUtils.addOrSetAttribute(windows, "platform", "windows");
        assertTrue(f.needExclude(windows, ""));
    }

    @Test
    public void testNeedExclude() {
        final FilterUtils f = new FilterUtils();
        f.setFilterMap(filterMap);

        final AttributesImpl amigaUnix = new AttributesImpl();
        XMLUtils.addOrSetAttribute(amigaUnix, "platform", "amiga unix windows");
        assertFalse(f.needExclude(amigaUnix, ""));

        final AttributesImpl amiga = new AttributesImpl();
        XMLUtils.addOrSetAttribute(amiga, "platform", "amiga windows");
        assertFalse(f.needExclude(amiga, ""));

        final AttributesImpl windows = new AttributesImpl();
        XMLUtils.addOrSetAttribute(windows, "platform", "windows");
        assertTrue(f.needExclude(windows, ""));
    }

    @Test
    public void testNeedExcludeMultipleAttributes() {
        final FilterUtils f = new FilterUtils();
        f.setFilterMap(filterMap);

        final AttributesImpl amigaUnix = new AttributesImpl();
        XMLUtils.addOrSetAttribute(amigaUnix, "platform", "amiga unix windows");
        XMLUtils.addOrSetAttribute(amigaUnix, "audience", "expert");
        assertFalse(f.needExclude(amigaUnix, ""));

        final AttributesImpl amiga = new AttributesImpl();
        XMLUtils.addOrSetAttribute(amiga, "platform", "amiga windows");
        XMLUtils.addOrSetAttribute(amiga, "audience", "expert");
        assertFalse(f.needExclude(amiga, ""));

        final AttributesImpl windows = new AttributesImpl();
        XMLUtils.addOrSetAttribute(windows, "platform", "windows");
        XMLUtils.addOrSetAttribute(windows, "audience", "novice");
        assertTrue(f.needExclude(windows, ""));
    }
    
}
