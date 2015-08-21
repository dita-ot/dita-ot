package org.dita.dost.writer;

import org.custommonkey.xmlunit.XMLUnit;
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
import javax.xml.transform.stream.StreamResult;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;

public class NormalizeTableFilterTest {

	@BeforeClass
    public static void setUp() {
		TestUtils.resetXMLUnit();
		XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreComments(true);
		//XMLUnit.setIgnoreAttributeOrder(true);
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
			assertXMLEqual(db.parse(expStream), act);
		} else {
			final StringWriter w = new StringWriter();
			t.transform(s, new StreamResult(w));
			assertXMLEqual(new InputSource(expStream), new InputSource(new StringReader(w.toString())));
		}
	}

}
