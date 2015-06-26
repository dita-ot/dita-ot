package org.dita.dost.writer;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;

import org.custommonkey.xmlunit.XMLUnit;
import org.dita.dost.TestUtils;
import org.dita.dost.reader.DitaValReader;
import org.dita.dost.util.FilterUtils;
import org.dita.dost.util.XMLUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class ProfilingFilterTest {

	@BeforeClass
    public static void setUp() {
		TestUtils.resetXMLUnit();
		XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreComments(true);
	}
	
	@Test
	public void testNoFilter() throws Exception {
		test(new FilterUtils(false, Collections.EMPTY_MAP), "topic.dita", "topic.dita");

		final DitaValReader filterReader = new DitaValReader();
		filterReader.read(new File(getClass().getClassLoader().getResource("ProfilingFilterTest/src/topic1.ditaval").toURI()).getAbsoluteFile());
        final FilterUtils filterUtils = new FilterUtils(false, filterReader.getFilterMap());
		filterUtils.setLogger(new TestUtils.TestLogger());
        test(filterUtils, "topic.dita", "topic1.dita");

        test(new FilterUtils(false, Collections.EMPTY_MAP), "map.ditamap", "map_xhtml.ditamap");
        test(new FilterUtils(true, Collections.EMPTY_MAP), "map.ditamap", "map_pdf.ditamap");
	}
	
	private void test(final FilterUtils filterUtils, final String srcFile, final String expFile) throws Exception {
		final Transformer t = TransformerFactory.newInstance().newTransformer();
		final InputStream src = getClass().getClassLoader().getResourceAsStream("ProfilingFilterTest/src/" + srcFile);
		final ProfilingFilter f = new ProfilingFilter();
		f.setParent(XMLUtils.getXMLReader());
		filterUtils.setLogger(new TestUtils.TestLogger());
		f.setFilterUtils(filterUtils);
		f.setLogger(new TestUtils.TestLogger());
		final SAXSource s = new SAXSource(f, new InputSource(src));
		final DOMResult d = new DOMResult();
		t.transform(s, d);
		
		final DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		final InputStream exp = getClass().getClassLoader().getResourceAsStream("ProfilingFilterTest/exp/" + expFile);
		assertXMLEqual(db.parse(exp), (Document) d.getNode());
	}


}
