/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2014 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.writer;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;
import org.dita.dost.TestUtils;
import org.dita.dost.module.reader.DefaultTempFileScheme;
import org.dita.dost.module.reader.TempFileNameScheme;
import org.dita.dost.store.StreamStore;
import org.dita.dost.util.Job;
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.util.Job.FileInfo.Builder;
import org.dita.dost.util.XMLUtils;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

public class ForceUniqueFilterTest {

    private static final File resourceDir = TestUtils.getResourceDir(ForceUniqueFilterTest.class);
    private static final File srcDir = new File(resourceDir, "src");
    private static final File expDir = new File(resourceDir, "exp");

    private Job job;
    private TempFileNameScheme tempFileNameScheme;

    @Before
    public void setUp() throws Exception {
        job = new Job(srcDir, new StreamStore(srcDir, new XMLUtils()));
        job.setInputDir(srcDir.toURI());
        job.add(new Builder()
                .src(srcDir.toURI().resolve("test.ditamap"))
                .uri(URI.create("test.ditamap"))
                .result(srcDir.toURI().resolve("test.ditamap"))
                .format("ditamap")
                .build());
        for (String name : new String[] {"test.dita", "test3.dita", "test2.dita", "topic.dita"}) {
            job.add(new Builder()
                    .src(srcDir.toURI().resolve(name))
                    .uri(URI.create(name))
                    .result(srcDir.toURI().resolve(name))
                    .build());
        }
        job.add(new Builder()
                .uri(URI.create("copy-to.dita"))
                .result(srcDir.toURI().resolve("copy-to.dita"))
                .build());
        tempFileNameScheme = new DefaultTempFileScheme();
        tempFileNameScheme.setBaseDir(srcDir.toURI());
    }

    @Test
    public void test() throws Exception {
        final ForceUniqueFilter f = new ForceUniqueFilter();
        f.setJob(job);
        f.setTempFileNameScheme(tempFileNameScheme);
        f.setCurrentFile(new File(srcDir, "test.ditamap").toURI());
        f.setParent(SAXParserFactory.newInstance().newSAXParser().getXMLReader());

        final Document act = filter(new File(srcDir, "test.ditamap"), f);
        final Document exp = parse(new File(expDir, "test.ditamap"));

        TestUtils.assertXMLEqual(exp, act);

        assertEquals(
                new HashMap<>(ImmutableMap.of(
                        createFileInfo("test.dita", "test_3.dita"),
                        createFileInfo("test.dita", "test.dita"),
                        createFileInfo("test.dita", "test_2.dita"),
                        createFileInfo("test.dita", "test.dita"),
                        createFileInfo(null, "copy-to_2.dita"),
                        createFileInfo(null, "copy-to.dita"),
                        createFileInfo("topic.dita", "topic_2.dita"),
                        createFileInfo("topic.dita", "topic.dita"))),
                f.copyToMap);
    }

    private FileInfo createFileInfo(final String src, final String tmp) {
        final Builder builder = new Builder();
        if (src != null) {
            builder.src(srcDir.toURI().resolve(src));
        }
        return builder.uri(URI.create(tmp)).result(srcDir.toURI().resolve(tmp)).build();
    }

    private Document parse(File input) throws ParserConfigurationException, IOException, SAXException {
        final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);
        builderFactory.setIgnoringComments(true);
        return builderFactory
                .newDocumentBuilder()
                .parse(new InputSource(input.toURI().toString()));
    }

    private Document filter(File input, XMLReader f) throws TransformerException {
        final Transformer transformer = TransformerFactory.newInstance().newTransformer();
        final DOMResult dst = new DOMResult();
        transformer.transform(new SAXSource(f, new InputSource(input.toURI().toString())), dst);
        return (Document) dst.getNode();
    }
}
