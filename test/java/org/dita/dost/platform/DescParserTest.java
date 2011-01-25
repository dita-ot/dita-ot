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
import java.util.Iterator;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLReaderFactory;

public class DescParserTest {

	private final File resourceDir = new File("test-stub", IntegratorTest.class.getSimpleName());
	
	final DescParser p = new DescParser("base");
	
	@Before
	public void setUp() throws Exception {
		final XMLReader parser = XMLReaderFactory.createXMLReader();
		parser.setContentHandler(p);
		parser.parse(new File(resourceDir, "src" + File.separator + "plugins" + File.separator
				+ "dummy" + File.separator + "plugin.xml").toURI().toString());
	}

	@Test
	public void testGetFeatures() {
		final Features f = p.getFeatures();
		assertEquals("foo,bar,baz", f.getFeature("CheckTranstypeAction"));
		assertEquals("base/foo.xml", f.getFeature("ImportStringsAction"));
		assertEquals("bar", f.getMeta("foo"));
		for (final Iterator<PluginRequirement> i = f.getRequireListIter(); i.hasNext();) {
			final PluginRequirement r = i.next();
			for (final Iterator<String> ps = r.getPlugins(); ps.hasNext();) {
				assertEquals("base", ps.next());
			}
			assertTrue(r.getRequired());	
		}
		assertArrayEquals(new String[] {"xsl/shell_template.xsl"},
				    	  f.getAllTemplates().toArray(new String[0]));
	}

	@Test
	public void testGetPluginId() {
		assertEquals("dummy", p.getPluginId());
	}

}
