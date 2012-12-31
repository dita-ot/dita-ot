package org.dita.dost.writer;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;

import org.custommonkey.xmlunit.XMLUnit;
import org.dita.dost.TestUtils;
import org.dita.dost.reader.DitaValReader;
import org.dita.dost.util.FilterUtils;
import org.dita.dost.util.StringUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

public class ProfilingFilterTest {

	@BeforeClass
    public static void setUp() {
		TestUtils.resetXMLUnit();
		XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreComments(true);
	}
	
	@Test
	public void testNoFilter() throws Exception {
		test(new FilterUtils(), "topic.dita", "topic.dita", null);
		
		final FilterUtils filterUtils = new FilterUtils();
		final DitaValReader filterReader = new DitaValReader();
		filterReader.read(new File(getClass().getClassLoader().getResource("ProfilingFilterTest/src/topic1.ditaval").toURI()).getAbsolutePath());
        filterUtils.setFilterMap(filterReader.getFilterMap());
		filterUtils.setLogger(new TestUtils.TestLogger());
        test(filterUtils, "topic.dita", "topic1.dita", null);

        test(new FilterUtils(), "map.ditamap", "map_xhtml.ditamap", "xhtml");
        test(new FilterUtils(), "map.ditamap", "map_pdf.ditamap", "pdf");
	}
	
	private void test(final FilterUtils filterUtils, final String srcFile, final String expFile, final String transtype) throws Exception {
		final Transformer t = TransformerFactory.newInstance().newTransformer();
		final InputStream src = getClass().getClassLoader().getResourceAsStream("ProfilingFilterTest/src/" + srcFile);
		final ProfilingFilter f = new ProfilingFilter();
		f.setParent(StringUtils.getXMLReader());
		f.setFilterUtils(filterUtils);
		if (transtype != null) {
			f.setTranstype(transtype);
		}
		f.setLogger(new TestUtils.TestLogger());
		final SAXSource s = new SAXSource(f, new InputSource(src));
		final DOMResult d = new DOMResult();
		t.transform(s, d);
		
		final DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		final InputStream exp = getClass().getClassLoader().getResourceAsStream("ProfilingFilterTest/exp/" + expFile);
		assertXMLEqual(db.parse(exp), (Document) d.getNode());
	}


}
