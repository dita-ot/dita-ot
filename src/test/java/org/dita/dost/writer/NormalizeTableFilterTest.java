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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;
import java.io.InputStream;

import static org.dita.dost.TestUtils.assertXMLEqual;

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

		final Document act = db.newDocument();
		t.transform(s, new DOMResult(act));
		assertXMLEqual(db.parse(expStream), act);
	}

}
