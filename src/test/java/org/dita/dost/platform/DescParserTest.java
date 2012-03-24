/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2011 All Rights Reserved.
 */
package org.dita.dost.platform;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.dita.dost.TestUtils;

import org.junit.Before;
import org.junit.Test;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class DescParserTest {

    private final File resourceDir = new File(TestUtils.testStub, DescParserTest.class.getSimpleName());

    final File base = new File("base", "plugins");
    final String basePrefix = base.getPath() + File.separator;
    final DescParser p = new DescParser(base, base.getParentFile());

    @Before
    public void setUp() throws Exception {
        final XMLReader parser = XMLReaderFactory.createXMLReader();
        parser.setContentHandler(p);
        parser.parse(new File(resourceDir, "plugin.xml").toURI().toString());
    }

    @Test
    public void testGetAllTemplates() {
        final Features f = p.getFeatures();
        assertEquals(Arrays.asList("xsl/shell_template.xsl", "xsl/shell2_template.xsl"),
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
        assertEquals("foo,bar", f.getFeature("type_text"));
        assertEquals("foo,bar", f.getFeature("multiple_type_text"));
        assertNull(f.getFeature("undefined"));
    }

    @Test
    public void testFileValueFeature() {
        final Features f = p.getFeatures();
        assertEquals(basePrefix + "foo," + basePrefix + "bar",
                f.getFeature("type_file"));
        assertEquals(basePrefix + "foo," + basePrefix + "bar",
                f.getFeature("multiple_type_file"));
    }

    @Test
    public void testFileFeature() {
        final Features f = p.getFeatures();
        assertEquals(basePrefix + "foo," + basePrefix + "bar",
                f.getFeature("file"));
        assertEquals(basePrefix + "foo," + basePrefix + "bar",
                f.getFeature("multiple_file"));
    }

}
