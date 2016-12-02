/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2014 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.writer;

import org.custommonkey.xmlunit.XMLUnit;
import org.dita.dost.TestUtils;
import org.dita.dost.module.GenMapAndTopicListModule;
import org.dita.dost.module.GenMapAndTopicListModule.TempFileNameScheme;
import org.dita.dost.util.Job;
import org.dita.dost.util.Job.FileInfo.Builder;
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
import java.net.URI;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.dita.dost.util.Constants.INPUT_DIR_URI;

public class ForceUniqueFilterTest {

    private static final File resourceDir = TestUtils.getResourceDir(ForceUniqueFilterTest.class);
    private static final File srcDir = new File(resourceDir, "src");
    private static final File expDir = new File(resourceDir, "exp");

    private Job job;
    private TempFileNameScheme tempFileNameScheme;

    @Before
    public void setUp() throws Exception {
        job = new Job(srcDir);
        job.setProperty(INPUT_DIR_URI, srcDir.toURI().toString());
        job.add(new Builder()
                .src(srcDir.toURI().resolve("test.dita"))
                .uri(URI.create("test.dita"))
                .result(srcDir.toURI().resolve("test.dita"))
                .build());

        tempFileNameScheme = new GenMapAndTopicListModule.DefaultTempFileScheme();
        tempFileNameScheme.setBaseDir(srcDir.toURI());

        TestUtils.resetXMLUnit();
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreComments(true);
    }

    @Test
    public void test() throws Exception {
        final ForceUniqueFilter f = new ForceUniqueFilter();
        f.setJob(job);
        f.setTempFileNameScheme(tempFileNameScheme);
        f.setCurrentFile(new File(srcDir, "test.ditamap").toURI());
        f.setParent(SAXParserFactory.newInstance().newSAXParser().getXMLReader());

        final DOMResult dst = new DOMResult();
        TransformerFactory.newInstance().newTransformer().transform(new SAXSource(f, new InputSource(new File(srcDir, "test.ditamap").toURI().toString())), dst);

        final Document exp = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new File(expDir, "test.ditamap").toURI().toString()));
        assertXMLEqual(exp, (Document) dst.getNode());
    }


}
