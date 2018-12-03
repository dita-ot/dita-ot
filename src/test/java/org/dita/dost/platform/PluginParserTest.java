/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.platform;

import static org.junit.Assert.*;
import static java.util.Arrays.*;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.dita.dost.TestUtils;

import org.junit.Before;
import org.junit.Test;

public class PluginParserTest {

    final File resourceDir = TestUtils.getResourceDir(PluginParserTest.class);
    final PluginParser p = new PluginParser(resourceDir);

    @Before
    public void setUp() throws Exception {
        p.setPluginDir(resourceDir);
        p.parse(new File(resourceDir, "plugin.xml"));
    }

    @Test
    public void testGetAllTemplates() {
        final Features f = p.getFeatures();
        assertEquals(
                Arrays.asList(
                        new Value("dummy", "xsl/shell_template.xsl"),
                        new Value("dummy", "xsl/shell2_template.xsl")),
                f.getAllTemplates());
    }

    @Test
    public void testRequirements() {
        final Features f = p.getFeatures();
        final Map<String, Boolean> exp = new HashMap<String, Boolean>();
        exp.put("foo", true);
        exp.put("bar", true);
        exp.put("baz", false);
        for (final Iterator<PluginRequirement> i = f.getRequireListIter(); i.hasNext();) {
            final PluginRequirement r = i.next();
            for (final Iterator<String> ps = r.getPlugins(); ps.hasNext();) {
                final String p = ps.next();
                assertTrue(exp.containsKey(p));
                assertEquals(exp.get(p), r.getRequired());
            }
        }
    }

    @Test
    public void testGetMeta() {
        final Features f = p.getFeatures();
        assertEquals("bar", f.getMeta("foo"));
        assertEquals("quxx", f.getMeta("baz"));
        assertNull(f.getMeta("undefined"));
    }

    @Test
    public void testValueFeature() {
        final Features f = p.getFeatures();
        assertEquals(asList("foo", "bar"), f.getFeature("type_text"));
        assertEquals(asList("foo", "bar"), f.getFeature("multiple_type_text"));
        assertNull(f.getFeature("undefined"));
    }

    @Test
    public void testFileValueFeature() {
        final Features f = p.getFeatures();
        assertEquals(asList(new File(resourceDir, "foo").toString(), new File(resourceDir, "bar").toString()),
                f.getFeature("type_file"));
        assertEquals(asList(new File(resourceDir, "foo").toString(), new File(resourceDir, "bar").toString()),
                f.getFeature("multiple_type_file"));
    }

    @Test
    public void testFileFeature() {
        final Features f = p.getFeatures();
        assertEquals(asList(new File(resourceDir, "foo").toString(), new File(resourceDir, "bar").toString()),
                f.getFeature("file"));
        assertEquals(asList(new File(resourceDir, "foo").toString(), new File(resourceDir, "bar").toString()),
                f.getFeature("multiple_file"));
    }

}
