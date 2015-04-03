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
import java.io.InputStream;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;

public class NormalizeFilterTest {

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
	
	private void test(final String expFile) throws Exception {
		final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		//dbf.setNamespaceAware(true);
		final DocumentBuilder db = dbf.newDocumentBuilder();

		final Transformer t = TransformerFactory.newInstance().newTransformer();
		final InputStream src = getClass().getClassLoader().getResourceAsStream("NormalizeFilterTest/src/" + expFile);
		final NormalizeFilter f = new NormalizeFilter();
		f.setParent(XMLUtils.getXMLReader());
		f.setLogger(new TestUtils.TestLogger());
		final SAXSource s = new SAXSource(f, new InputSource(src));
		final Document act = db.newDocument();
		final DOMResult d = new DOMResult(act);
		t.transform(s, d);

		final InputStream expStream = getClass().getClassLoader().getResourceAsStream("NormalizeFilterTest/exp/" + expFile);
		final Document exp = db.parse(expStream);

		assertXMLEqual(exp, act);
	}

}
