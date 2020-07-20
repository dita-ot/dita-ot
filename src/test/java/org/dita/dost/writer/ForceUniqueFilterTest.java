/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2014 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.writer;

import com.google.common.collect.ImmutableMap;
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
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.sax.SAXSource;
import java.io.File;
import java.net.URI;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

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
        job.add(new Builder()
                .src(srcDir.toURI().resolve("test.dita"))
                .uri(URI.create("test.dita"))
                .result(srcDir.toURI().resolve("test.dita"))
                .build());
        job.add(new Builder()
                .src(srcDir.toURI().resolve("test3.dita"))
                .uri(URI.create("test3.dita"))
                .result(srcDir.toURI().resolve("test3.dita"))
                .build());
        job.add(new Builder()
                .uri(URI.create("copy-to.dita"))
                .result(srcDir.toURI().resolve("copy-to.dita"))
                .build());
        job.add(new Builder()
                .src(srcDir.toURI().resolve("test2.dita"))
                .uri(URI.create("test2.dita"))
                .result(srcDir.toURI().resolve("test2.dita"))
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

        final DOMResult dst = new DOMResult();
        TransformerFactory.newInstance().newTransformer().transform(new SAXSource(f, new InputSource(new File(srcDir, "test.ditamap").toURI().toString())), dst);

        final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        builderFactory.setNamespaceAware(true);
        builderFactory.setIgnoringComments(true);
        final Document exp = builderFactory.newDocumentBuilder().parse(new InputSource(new File(expDir, "test.ditamap").toURI().toString()));
        final Diff d = DiffBuilder
                .compare(exp)
                .withTest(dst.getNode())
                .ignoreWhitespace()
                .build();
        assertFalse(d.hasDifferences());

        assertEquals(new HashMap<FileInfo, FileInfo>(ImmutableMap.of(
                createFileInfo("test.dita", "test_3.dita"),
                createFileInfo("test.dita", "test.dita"),
                createFileInfo("test.dita", "test_2.dita"),
                createFileInfo("test.dita", "test.dita"),
                createFileInfo(null, "copy-to_2.dita"),
                createFileInfo(null, "copy-to.dita")
        )), f.copyToMap);
    }

    private FileInfo createFileInfo(final String src, final String tmp) {
        final Builder builder = new Builder();
        if (src != null) {
            builder.src(srcDir.toURI().resolve(src));
        }
        return builder.uri(URI.create(tmp))
                .result(srcDir.toURI().resolve(tmp))
                .build();
    }


}
