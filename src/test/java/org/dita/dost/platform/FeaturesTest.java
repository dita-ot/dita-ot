/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
        f.addTemplate("foo");
        f.addTemplate("foo");
        f.addTemplate("bar");
        f.addTemplate(null);
    }

    @Test
    public void testGetAllTemplates() {
        final Features f = new Features(new File("base", "plugins"), new File("base"));
        f.addTemplate("foo");
        f.addTemplate("foo");
        f.addTemplate("bar");
        f.addTemplate(null);

        final List<String> act = f.getAllTemplates();
        Collections.sort(act, new Comparator<String>() {
            public int compare(final String arg0, final String arg1) {
                if (arg0 == null && arg1 == null) {
                    return 0;
                } else if (arg0 == null) {
                    return 1;
                } else if (arg1 == null) {
                    return -1;
                } else {
                    return arg0.compareTo(arg1);
                }
            }
        });
        assertArrayEquals(new String[] {"bar", "foo", "foo", null},
                act.toArray(new String[0]));
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
