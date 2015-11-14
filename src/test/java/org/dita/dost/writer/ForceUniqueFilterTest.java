package org.dita.dost.writer;

import org.custommonkey.xmlunit.XMLUnit;
import org.dita.dost.TestUtils;
import org.dita.dost.util.Job;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;
import java.io.File;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.dita.dost.util.Constants.INPUT_DIR_URI;

public class ForceUniqueFilterTest {

    private static final File resourceDir = TestUtils.getResourceDir(ForceUniqueFilterTest.class);
    private static final File srcDir = new File(resourceDir, "src");
    private static final File expDir = new File(resourceDir, "exp");

    private static Job job;

    @Before
    public void setUp() throws Exception {
        job = new Job(srcDir);
        job.setProperty(INPUT_DIR_URI, srcDir.toURI().toString());

        TestUtils.resetXMLUnit();
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreComments(true);
    }

    @Test
    public void test() throws Exception {
        final ForceUniqueFilter f = new ForceUniqueFilter();
        f.setJob(job);
        f.setCurrentFile(new File(srcDir, "test.ditamap").toURI());
        f.setParent(SAXParserFactory.newInstance().newSAXParser().getXMLReader());

        final DOMResult dst = new DOMResult();
        TransformerFactory.newInstance().newTransformer().transform(new SAXSource(f, new InputSource(new File(srcDir, "test.ditamap").toURI().toString())), dst);

        final Document exp = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new File(expDir, "test.ditamap").toURI().toString()));
        assertXMLEqual(exp, (Document) dst.getNode());
    }


}