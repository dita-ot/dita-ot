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

public class NormalizeSimpleTableFilterTest {

    private final DocumentBuilderFactory dbf;
    private final TransformerFactory tf;

    public NormalizeSimpleTableFilterTest() {
        dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        tf = TransformerFactory.newInstance();
    }

    @Test
    public void simple() throws Exception {
        test("simple.dita");
    }

    @Test
    public void topic() throws Exception {
        test("topic.dita");
    }

    @Test
    public void nested() throws Exception {
        test("nested.dita");
    }

    @Test
    public void rowspan() throws Exception {
        test("rowspan.dita");
    }

    @Test
    public void parallel() throws Exception {
        test("parallel.dita");
    }

    private void test(final String file) throws Exception {
        final DocumentBuilder db = dbf.newDocumentBuilder();
        final InputStream expStream = getClass().getClassLoader().getResourceAsStream(this.getClass().getSimpleName() + "/exp/" + file);

        final Transformer t = tf.newTransformer();
        final InputStream src = getClass().getClassLoader().getResourceAsStream(this.getClass().getSimpleName() + "/src/" + file);
        final NormalizeSimpleTableFilter f = new NormalizeSimpleTableFilter();
        f.setParent(XMLUtils.getXMLReader());
        f.setLogger(new TestUtils.TestLogger());
        final SAXSource s = new SAXSource(f, new InputSource(src));

        final Document act = db.newDocument();
        t.transform(s, new DOMResult(act));
        assertXMLEqual(db.parse(expStream), act);
    }

}
