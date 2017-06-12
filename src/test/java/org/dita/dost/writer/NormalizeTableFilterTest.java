/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2015 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.writer;

import org.dita.dost.TestUtils;
import org.dita.dost.util.XMLUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.Assert.assertFalse;


public class NormalizeTableFilterTest {

	@BeforeClass
    public static void setUp() {
	}
	
	@Test
	public void testNoFilter() throws Exception {
        test("topic.dita");
	}
	
	private void test(final String file) throws Exception {
		final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		final DocumentBuilder db = dbf.newDocumentBuilder();
		final InputStream expStream = getClass().getClassLoader().getResourceAsStream(this.getClass().getSimpleName() + "/exp/" + file);

		final Transformer t = TransformerFactory.newInstance().newTransformer();
		final InputStream src = getClass().getClassLoader().getResourceAsStream(this.getClass().getSimpleName() + "/src/" + file);
		final NormalizeTableFilter f = new NormalizeTableFilter();
		f.setParent(XMLUtils.getXMLReader());
		f.setLogger(new TestUtils.TestLogger());
		final SAXSource s = new SAXSource(f, new InputSource(src));
		if (false) { // XXX: comparing resulting DOM document will fail even thought the XML content is identical
			final Document act = db.newDocument();
			t.transform(s, new DOMResult(act));
			final Diff d = DiffBuilder
					.compare(db.parse(expStream))
					.withTest(act)
					.ignoreWhitespace()
					.ignoreComments()
					.build();
			assertFalse(d.hasDifferences());
		} else {
			final StringWriter w = new StringWriter();
			t.transform(s, new StreamResult(w));


			final Diff d = DiffBuilder
					.compare(new InputSource(new StringReader(w.toString())))
					.withTest(new InputSource(expStream))
					.ignoreWhitespace()
					.ignoreComments()
					.build();
			assertFalse(d.hasDifferences());
		}
	}

}
