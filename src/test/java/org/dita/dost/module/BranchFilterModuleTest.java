/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2015 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.module;

import org.custommonkey.xmlunit.XMLUnit;
import org.dita.dost.TestUtils;
import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.util.Job;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static junit.framework.Assert.assertEquals;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.toURI;
import static org.junit.Assert.assertNotNull;

public class BranchFilterModuleTest extends BranchFilterModule {

    private final File resourceDir = TestUtils.getResourceDir(BranchFilterModuleTest.class);
    private final File expDir = new File(resourceDir, "exp");
    private File tempDir;
    private Job job;

    
    @Before
    public void setUp() throws Exception {
        tempDir = TestUtils.createTempDir(getClass());
        TestUtils.copy(new File(resourceDir, "src"), tempDir);
        
        TestUtils.resetXMLUnit();
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreComments(true);

        job = new Job(tempDir);
        job.setProperty(INPUT_DIR_URI, tempDir.toURI().toString());
        job.add(new Job.FileInfo.Builder()
                .src(new File(tempDir, "input.ditamap").toURI())
                .result(new File(tempDir, "input.ditamap").toURI())
                .uri(new URI("input.ditamap"))
                .format(ATTR_FORMAT_VALUE_DITAMAP)
                .build());
        for (final String uri: Arrays.asList("linux.ditaval", "novice.ditaval", "advanced.ditaval", "mac.ditaval", "win.ditaval")) {
            job.add(new Job.FileInfo.Builder()
                    .src(new File(tempDir, uri).toURI())
                    .result(new File(tempDir, uri).toURI())
                    .uri(new URI(uri))
                    .format(ATTR_FORMAT_VALUE_DITAVAL)
                    .build());
        }
        for (final String uri: Arrays.asList("install.dita", "perform-install.dita", "configure.dita")) {
            job.add(new Job.FileInfo.Builder()
                    .src(new File(tempDir, uri).toURI())
                    .result(new File(tempDir, uri).toURI())
                    .uri(new URI(uri))
                    .format(ATTR_FORMAT_VALUE_DITA)
                    .build());
        }
        for (final String uri: Arrays.asList("installation-procedure.dita", "getting-started.dita")) {
            job.add(new Job.FileInfo.Builder()
                    .result(new File(tempDir, uri).toURI())
                    .uri(new URI(uri))
                    .build());
        }
    }

    @After
    public void tearDown() throws Exception {
        TestUtils.forceDelete(tempDir);
    }

    @Test
    public void testSplitBranches() throws ParserConfigurationException, IOException, SAXException {
        final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();

        final Document act = builder.parse(new File(tempDir, "input.ditamap"));
        currentFile = new File(tempDir, "input.ditamap").toURI();
        setJob(job);
        splitBranches(act.getDocumentElement(), Branch.EMPTY);

        final Document exp = builder.parse(new File(expDir, "input_splitBranches.ditamap"));
        assertXMLEqual(exp, act);
    }

    @Test
    public void testProcessMap() throws SAXException, IOException {
        final BranchFilterModule m = new BranchFilterModule();
        m.setJob(job);
        m.setLogger(new DITAOTJavaLogger());
        
        m.processMap(toURI("input.ditamap"));
        assertXMLEqual(new InputSource(new File(expDir, "input.ditamap").toURI().toString()),
                new InputSource(new File(tempDir, "input.ditamap").toURI().toString()));

        final List<String> exp = Arrays.asList(
                "installation-procedure.dita", "getting-started.dita",
                //"http://example.com/install.dita",
                "configure.dita",
                "input.ditamap", "install.dita", "linux.ditaval", "perform-install.dita",
                "configure-novice.dita", "novice.ditaval", "configure-admin.dita",
                "advanced.ditaval", "install-mac.dita", "mac.ditaval",
                "perform-install-mac.dita", "installation-procedure-mac.dita", "configure-novice-mac.dita",
                "configure-admin-mac.dita", "install-win.dita", "win.ditaval", "perform-install-win.dita",
                "installation-procedure-win.dita", "configure-novice-win.dita", "configure-admin-win.dita",
                "install-linux.dita"
        );
        assertEquals(exp.size(), job.getFileInfo().size());
        for (final String f : exp) {
            assertNotNull(job.getFileInfo(URI.create(f)));
        }

        final List<String> filesExp = Arrays.asList(
                //"install.dita",
                "configure.dita",
                "perform-install.dita", "configure-novice.dita",
                "configure-admin.dita", "install-mac.dita", "perform-install-mac.dita",
                "installation-procedure-mac.dita", "configure-novice-mac.dita", "configure-admin-mac.dita",
                "install-win.dita", "perform-install-win.dita", "installation-procedure-win.dita",
                "configure-novice-win.dita", "configure-admin-win.dita", "install-linux.dita", "install.dita"

        );
        Collections.sort(filesExp);
        final List<String> filesAct = Arrays.stream(tempDir.listFiles((dir, name) -> name.endsWith(".dita")))
                .map(f -> f.getName())
                .collect(Collectors.toList());
        Collections.sort(filesAct);
        assertEquals(filesExp, filesAct);
    }

    private static final Optional<String> ABSENT_STRING = Optional.empty();
    
    @Test
    public void testGenerateCopyTo() throws URISyntaxException {
        assertEquals(new URI("foo.bar"), generateCopyTo(new URI("foo.bar"), new Branch(ABSENT_STRING, ABSENT_STRING, ABSENT_STRING, ABSENT_STRING)));
        assertEquals(new URI("baz-foo.bar"), generateCopyTo(new URI("foo.bar"), new Branch(Optional.of("baz-"), ABSENT_STRING, ABSENT_STRING, ABSENT_STRING)));
        assertEquals(new URI("foo-baz.bar"), generateCopyTo(new URI("foo.bar"), new Branch(ABSENT_STRING, Optional.of("-baz"), ABSENT_STRING, ABSENT_STRING)));
        assertEquals(new URI("qux-foo-baz.bar"), generateCopyTo(new URI("foo.bar"), new Branch(Optional.of("qux-"), Optional.of("-baz"), ABSENT_STRING, ABSENT_STRING)));
        assertEquals(new URI("sub.dir/foo.bar"), generateCopyTo(new URI("sub.dir/foo.bar"), new Branch(ABSENT_STRING, ABSENT_STRING, ABSENT_STRING, ABSENT_STRING)));
        assertEquals(new URI("sub.dir/baz-foo.bar"), generateCopyTo(new URI("sub.dir/foo.bar"), new Branch(Optional.of("baz-"), ABSENT_STRING, ABSENT_STRING, ABSENT_STRING)));
        assertEquals(new URI("sub.dir/foo-baz.bar"), generateCopyTo(new URI("sub.dir/foo.bar"), new Branch(ABSENT_STRING, Optional.of("-baz"), ABSENT_STRING, ABSENT_STRING)));
        assertEquals(new URI("sub.dir/qux-foo-baz.bar"), generateCopyTo(new URI("sub.dir/foo.bar"), new Branch(Optional.of("qux-"), Optional.of("-baz"), ABSENT_STRING, ABSENT_STRING)));
        assertEquals(new URI("foo"), generateCopyTo(new URI("foo"), new Branch(ABSENT_STRING, ABSENT_STRING, ABSENT_STRING, ABSENT_STRING)));
        assertEquals(new URI("baz-foo"), generateCopyTo(new URI("foo"), new Branch(Optional.of("baz-"), ABSENT_STRING, ABSENT_STRING, ABSENT_STRING)));
        assertEquals(new URI("foo-baz"), generateCopyTo(new URI("foo"), new Branch(ABSENT_STRING, Optional.of("-baz"), ABSENT_STRING, ABSENT_STRING)));
        assertEquals(new URI("qux-foo-baz"), generateCopyTo(new URI("foo"), new Branch(Optional.of("qux-"), Optional.of("-baz"), ABSENT_STRING, ABSENT_STRING)));
        assertEquals(new URI("sub.dir/foo"), generateCopyTo(new URI("sub.dir/foo"), new Branch(ABSENT_STRING, ABSENT_STRING, ABSENT_STRING, ABSENT_STRING)));
        assertEquals(new URI("sub.dir/baz-foo"), generateCopyTo(new URI("sub.dir/foo"), new Branch(Optional.of("baz-"), ABSENT_STRING, ABSENT_STRING, ABSENT_STRING)));
        assertEquals(new URI("sub.dir/foo-baz"), generateCopyTo(new URI("sub.dir/foo"), new Branch(ABSENT_STRING, Optional.of("-baz"), ABSENT_STRING, ABSENT_STRING)));
        assertEquals(new URI("sub.dir/qux-foo-baz"), generateCopyTo(new URI("sub.dir/foo"), new Branch(Optional.of("qux-"), Optional.of("-baz"), ABSENT_STRING, ABSENT_STRING)));
    }

}
