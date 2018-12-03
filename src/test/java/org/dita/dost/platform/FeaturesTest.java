/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.platform;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static java.util.Arrays.*;
import static org.dita.dost.platform.PluginParser.*;

import java.io.File;
import java.util.*;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;

public class FeaturesTest {

    private static Document doc;

    @BeforeClass
    public static void setUp() throws Exception {
       doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    }

    @Test
    public void testFeaturesString() {
        assertNotNull(new Features(new File("base", "plugins"), new File("base")));
    }

    @Test
    public void testGetLocation() {
        assertEquals(new File("base", "plugins"), new Features(new File("base", "plugins"), new File("base")).getPluginDir());
    }

    @Test
    public void testAddExtensionPoint() {
        final Features f = new Features(new File("base", "plugins"), new File("base"));
        final ExtensionPoint e = new ExtensionPoint("id", "name", "plugin");
        f.addExtensionPoint(e);
        try {
            f.addExtensionPoint(null);
            fail();
        } catch (final NullPointerException ex) {}
    }

    @Test
    public void testGetExtensionPoints() {
        final Features f = new Features(new File("base", "plugins"), new File("base"));
        final ExtensionPoint e = new ExtensionPoint("id", "name", "plugin");
        f.addExtensionPoint(e);
        final ExtensionPoint e2 = new ExtensionPoint("id2", "name2", "plugin");
        f.addExtensionPoint(e2);

        assertEquals(2, f.getExtensionPoints().size());
        assertEquals(e, f.getExtensionPoints().get("id"));
        assertEquals(e2, f.getExtensionPoints().get("id2"));
    }

    @Test
    public void testGetFeature() {
        final Features f = new Features(new File("base", "plugins"), new File("base"));
        f.addFeature("foo", getElement("bar", null));

        assertEquals(asList("bar"), f.getFeature("foo"));
    }

    @Test
    public void testGetAllFeatures() {
        final Features f = new Features(new File("base", "plugins"), new File("base"));
        f.addFeature("foo", getElement("bar", null));
        f.addFeature("foo", getElement("baz", null));
        f.addFeature("bar", getElement("qux", null));

        final Map<String, List<String>> exp = new HashMap<String, List<String>>();
        exp.put("foo", asList("bar", "baz"));
        exp.put("bar", asList("qux"));

        assertEquals(exp, f.getAllFeatures());
    }

    @Test
    public void testAddFeature() {
        final Features f = new Features(new File("base", "plugins"), new File("base"));
        f.addFeature("foo", getElement(null, null));
        f.addFeature("foo", getElement(" bar, baz ", null));
        assertEquals(asList("bar", "baz"), f.getFeature("foo"));
        f.addFeature("foo", getElement("bar, baz", "file"));
        assertEquals(asList("bar","baz",
                "base" + File.separator + "plugins" + File.separator + "bar",
                "base" + File.separator + "plugins" + File.separator + "baz"),
                f.getFeature("foo"));
    }

    @Test
    public void testAddRequireString() {
        final Features f = new Features(new File("base", "plugins"), new File("base"));
        f.addRequire("foo");
        try {
            f.addRequire(null);
            fail();
        } catch (final NullPointerException e) {}
    }

    @Test
    public void testAddRequireStringString() {
        final Features f = new Features(new File("base", "plugins"), new File("base"));
        f.addRequire("foo");
        f.addRequire("foo", null);
        try {
            f.addRequire(null, null);
            fail();
        } catch (final NullPointerException e) {}
    }

    @Test
    public void testGetRequireListIter() {
        final Features f = new Features(new File("base", "plugins"), new File("base"));
        f.addRequire("foo | bar ");
        f.addRequire("baz", "unrequired");
        f.addRequire("qux", "required");

        final Map<List<String>, Boolean> act = new HashMap<List<String>, Boolean>();
        final Iterator<PluginRequirement> requirements = f.getRequireListIter();
        while (requirements.hasNext()) {
            final PluginRequirement requirement = requirements.next();
            final List<String> plugins = new ArrayList<String>();
            for (final Iterator<String> ps = requirement.getPlugins(); ps.hasNext();) {
                plugins.add(ps.next());
            }
            Collections.sort(plugins);
            act.put(plugins, requirement.getRequired());
        }

        final Map<List<String>, Boolean> exp = new HashMap<List<String>, Boolean>();
        exp.put(Arrays.asList(new String[] {" bar ", "foo "}), Boolean.TRUE);
        exp.put(Arrays.asList(new String[] {"baz"}), Boolean.FALSE);
        exp.put(Arrays.asList(new String[] {"qux"}), Boolean.TRUE);

        assertEquals(exp, act);
    }

    @Test
    public void testAddMeta() {
        final Features f = new Features(new File("base", "plugins"), new File("base"));
        f.addMeta("foo", "bar");
        f.addMeta("foo", "baz");
        f.addMeta("bar", "baz");
        try {
            f.addMeta("bar", null);
            fail();
        } catch (final NullPointerException e) {}
    }

    @Test
    public void testGetMeta() {
        final Features f = new Features(new File("base", "plugins"), new File("base"));
        f.addMeta("foo", "bar");
        f.addMeta("foo", "baz");
        f.addMeta("bar", "baz");

        assertEquals("baz", f.getMeta("foo"));
        assertEquals("baz", f.getMeta("bar"));
        assertNull(f.getMeta("qux"));
    }

    @Test
    public void testAddTemplate() {
        final Features f = new Features(new File("base", "plugins"), new File("base"));
        f.addTemplate(new Value("base", "foo"));
        f.addTemplate(new Value("base", "foo"));
        f.addTemplate(new Value("base", "bar"));
        f.addTemplate(null);
    }

    @Test
    public void testGetAllTemplates() {
        final Features f = new Features(new File("base", "plugins"), new File("base"));
        f.addTemplate(new Value("base", "foo"));
        f.addTemplate(new Value("base", "foo"));
        f.addTemplate(new Value("base", "bar"));
        f.addTemplate(null);

        final List<Value> act = f.getAllTemplates();
        Collections.sort(act, new Comparator<Value>() {
            public int compare(final Value a0, final Value a1) {
                if (a0 == null || a1 == null) {
                    return -1;
                }
                return Objects.compare(a0.value, a1.value, String::compareTo);
            }
        });
        assertEquals(Arrays.asList(
                null,
                new Value("base", "bar"),
                new Value("base", "foo"),
                new Value("base", "foo")),
                act);
    }

    private static Element getElement(final String value, final String type) {
        final Element feature = doc.createElement(FEATURE_ELEM);
        if (value != null) {
            feature.setAttribute("value", value);
        }
        if (type != null) {
            feature.setAttribute("type", type);
        }
        return feature;
    }

}
