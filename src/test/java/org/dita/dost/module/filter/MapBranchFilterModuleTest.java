/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2016 Jarno Elovirta
 *
 *  See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.module.filter;

import org.dita.dost.TestUtils;
import org.dita.dost.TestUtils.CachingLogger;
import org.dita.dost.module.BranchFilterModule.Branch;
import org.dita.dost.store.StreamStore;
import org.dita.dost.util.Job;
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.util.XMLUtils;
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
import java.util.*;
import java.util.stream.Collectors;

import static junit.framework.Assert.assertEquals;
import static org.dita.dost.TestUtils.CachingLogger.Message.Level.ERROR;
import static org.dita.dost.TestUtils.assertXMLEqual;
import static org.dita.dost.util.Constants.*;
import static org.junit.Assert.assertNotNull;

public class MapBranchFilterModuleTest extends MapBranchFilterModule {

    private final File resourceDir = TestUtils.getResourceDir(MapBranchFilterModuleTest.class);
    private final File expDir = new File(resourceDir, "exp");
    private File tempDir;

    @Before
    public void setUp() throws Exception {
        tempDir = TestUtils.createTempDir(getClass());
        TestUtils.copy(new File(resourceDir, "src"), tempDir);
    }

    private Job getJob() throws IOException {
        final Job job = new Job(tempDir, new StreamStore(tempDir, new XMLUtils()));
        job.setInputDir(tempDir.toURI());
        job.add(new FileInfo.Builder()
                .src(new File(tempDir, "input.ditamap").toURI())
                .uri(URI.create("input.ditamap"))
                .format(ATTR_FORMAT_VALUE_DITAMAP)
                .build());
        for (final String uri: Arrays.asList("linux.ditaval", "novice.ditaval", "advanced.ditaval", "mac.ditaval", "win.ditaval")) {
            job.add(new FileInfo.Builder()
                    .src(new File(tempDir, uri).toURI())
                    .uri(URI.create(uri))
                    .format(ATTR_FORMAT_VALUE_DITAVAL)
                    .build());
        }
        for (final String uri: Arrays.asList("install.dita", "perform-install.dita", "configure.dita")) {
            job.add(new FileInfo.Builder()
                    .src(new File(tempDir, uri).toURI())
                    .uri(URI.create(uri))
                    .format(ATTR_FORMAT_VALUE_DITA)
                    .build());
        }
        return job;
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
        final Job job = getJob();
        setJob(job);
        splitBranches(act.getDocumentElement(), Branch.EMPTY);

        final Document exp = builder.parse(new File(expDir, "input_splitBranches.ditamap"));
        assertXMLEqual(exp, act);
    }

    @Test
    public void testProcessMap() throws SAXException, IOException {
        final MapBranchFilterModule m = new MapBranchFilterModule();
        final Job job = getJob();
        m.setJob(job);
        final CachingLogger logger = new CachingLogger();
        m.setLogger(logger);
        m.setXmlUtils(new XMLUtils());

        final FileInfo fi = new FileInfo.Builder()
                .uri(URI.create("input.ditamap"))
                .build();
        m.processMap(fi);
        assertXMLEqual(new InputSource(new File(expDir, "input.ditamap").toURI().toString()),
                new InputSource(new File(tempDir, "input.ditamap").toURI().toString()));

        final List<String> exp = Arrays.asList(
//                "installation-procedure.dita",
//                "getting-started.dita",
//                "http://example.com/install.dita",
                "configure.dita",
                "input.ditamap",
                "install.dita",
                "linux.ditaval",
                "perform-install.dita",
                "configure-novice.dita",
                "novice.ditaval",
                "configure-admin.dita",
                "advanced.ditaval",
                "install-mac.dita",
                "mac.ditaval",
                "perform-install-mac.dita",
//                "installation-procedure-mac.dita",
                "configure-novice-mac.dita",
                "configure-admin-mac.dita",
                "install-win.dita",
                "win.ditaval",
                "perform-install-win.dita",
//                "installation-procedure-win.dita",
                "configure-novice-win.dita",
                "configure-admin-win.dita",
                "install-linux.dita"
        );
        assertEquals(exp.size(), job.getFileInfo().size());
        for (final String f : exp) {
            assertNotNull(job.getFileInfo(URI.create(f)));
        }

        final List<String> filesExp = Arrays.asList(
//                "install.dita",
                "configure.dita",
                "perform-install.dita",
//                "configure-novice.dita",
//                "configure-admin.dita",
//                "install-mac.dita",
//                "perform-install-mac.dita",
//                "installation-procedure-mac.dita",
//                "configure-novice-mac.dita",
//                "configure-admin-mac.dita",
//                "install-win.dita",
//                "perform-install-win.dita",
//                "installation-procedure-win.dita",
//                "configure-novice-win.dita",
//                "configure-admin-win.dita",
//                "install-linux.dita",
                "install.dita"
        );
        Collections.sort(filesExp);
        final List<String> filesAct = Arrays.stream(tempDir.listFiles((dir, name) -> name.endsWith(".dita")))
                .map(f -> f.getName())
                .collect(Collectors.toList());
        Collections.sort(filesAct);
        assertEquals(filesExp, filesAct);
        assertEquals(0, logger.getMessages().stream().filter(msg -> msg.level == ERROR).count());
    }

    private static final Optional<String> ABSENT_STRING = Optional.empty();
    
    @Test
    public void testGenerateCopyTo() {
        assertEquals(URI.create("foo.bar"), generateCopyTo(URI.create("foo.bar"), new Branch(ABSENT_STRING, ABSENT_STRING, ABSENT_STRING, ABSENT_STRING)));
        assertEquals(URI.create("baz-foo.bar"), generateCopyTo(URI.create("foo.bar"), new Branch(Optional.of("baz-"), ABSENT_STRING, ABSENT_STRING, ABSENT_STRING)));
        assertEquals(URI.create("foo-baz.bar"), generateCopyTo(URI.create("foo.bar"), new Branch(ABSENT_STRING, Optional.of("-baz"), ABSENT_STRING, ABSENT_STRING)));
        assertEquals(URI.create("qux-foo-baz.bar"), generateCopyTo(URI.create("foo.bar"), new Branch(Optional.of("qux-"), Optional.of("-baz"), ABSENT_STRING, ABSENT_STRING)));
        assertEquals(URI.create("sub.dir/foo.bar"), generateCopyTo(URI.create("sub.dir/foo.bar"), new Branch(ABSENT_STRING, ABSENT_STRING, ABSENT_STRING, ABSENT_STRING)));
        assertEquals(URI.create("sub.dir/baz-foo.bar"), generateCopyTo(URI.create("sub.dir/foo.bar"), new Branch(Optional.of("baz-"), ABSENT_STRING, ABSENT_STRING, ABSENT_STRING)));
        assertEquals(URI.create("sub.dir/foo-baz.bar"), generateCopyTo(URI.create("sub.dir/foo.bar"), new Branch(ABSENT_STRING, Optional.of("-baz"), ABSENT_STRING, ABSENT_STRING)));
        assertEquals(URI.create("sub.dir/qux-foo-baz.bar"), generateCopyTo(URI.create("sub.dir/foo.bar"), new Branch(Optional.of("qux-"), Optional.of("-baz"), ABSENT_STRING, ABSENT_STRING)));
        assertEquals(URI.create("foo"), generateCopyTo(URI.create("foo"), new Branch(ABSENT_STRING, ABSENT_STRING, ABSENT_STRING, ABSENT_STRING)));
        assertEquals(URI.create("baz-foo"), generateCopyTo(URI.create("foo"), new Branch(Optional.of("baz-"), ABSENT_STRING, ABSENT_STRING, ABSENT_STRING)));
        assertEquals(URI.create("foo-baz"), generateCopyTo(URI.create("foo"), new Branch(ABSENT_STRING, Optional.of("-baz"), ABSENT_STRING, ABSENT_STRING)));
        assertEquals(URI.create("qux-foo-baz"), generateCopyTo(URI.create("foo"), new Branch(Optional.of("qux-"), Optional.of("-baz"), ABSENT_STRING, ABSENT_STRING)));
        assertEquals(URI.create("sub.dir/foo"), generateCopyTo(URI.create("sub.dir/foo"), new Branch(ABSENT_STRING, ABSENT_STRING, ABSENT_STRING, ABSENT_STRING)));
        assertEquals(URI.create("sub.dir/baz-foo"), generateCopyTo(URI.create("sub.dir/foo"), new Branch(Optional.of("baz-"), ABSENT_STRING, ABSENT_STRING, ABSENT_STRING)));
        assertEquals(URI.create("sub.dir/foo-baz"), generateCopyTo(URI.create("sub.dir/foo"), new Branch(ABSENT_STRING, Optional.of("-baz"), ABSENT_STRING, ABSENT_STRING)));
        assertEquals(URI.create("sub.dir/qux-foo-baz"), generateCopyTo(URI.create("sub.dir/foo"), new Branch(Optional.of("qux-"), Optional.of("-baz"), ABSENT_STRING, ABSENT_STRING)));
    }

    @Test
    public void testDuplicateTopic() throws IOException, SAXException {
        final MapBranchFilterModule m = new MapBranchFilterModule();
        final Job job = new Job(tempDir, new StreamStore(tempDir, new XMLUtils()));
        job.setInputDir(tempDir.toURI());
        job.addAll(getDuplicateTopicFileInfos());
        m.setJob(job);
        final CachingLogger logger = new CachingLogger();
        m.setLogger(logger);
        m.setXmlUtils(new XMLUtils());

        m.processMap(job.getFileInfo(URI.create("test.ditamap")));

        final Set<FileInfo> exp = getDuplicateTopicFileInfos();
        exp.add(new Job.FileInfo.Builder()
                .src(new File(tempDir, "t1.xml").toURI())
                .result(new File(tempDir, "t1-1.xml").toURI())
                .uri(URI.create("t1-1.xml"))
                .format(ATTR_FORMAT_VALUE_DITA)
                .build());

        assertEquals(exp, new HashSet<>(job.getFileInfo()));
        assertEquals(0, logger.getMessages().stream().filter(msg -> msg.level == ERROR).count());
    }

    private Set<Job.FileInfo> getDuplicateTopicFileInfos() {
        final Set<Job.FileInfo> res = new HashSet<>();
        res.add(new Job.FileInfo.Builder()
                .src(new File(tempDir, "test.ditamap").toURI())
                .result(new File(tempDir, "test.ditamap").toURI())
                .uri(URI.create("test.ditamap"))
                .format(ATTR_FORMAT_VALUE_DITAMAP)
                .build());
        for (final String uri: Arrays.asList("test.ditaval", "test2.ditaval")) {
            res.add(new Job.FileInfo.Builder()
                    .src(new File(tempDir, uri).toURI())
                    .result(new File(tempDir, uri).toURI())
                    .uri(URI.create(uri))
                    .format(ATTR_FORMAT_VALUE_DITAVAL)
                    .build());
        }
        for (final String uri: Arrays.asList("t1.xml")) {
            res.add(new Job.FileInfo.Builder()
                    .src(new File(tempDir, uri).toURI())
                    .result(new File(tempDir, uri).toURI())
                    .uri(URI.create(uri))
                    .format(ATTR_FORMAT_VALUE_DITA)
                    .build());
        }
        return res;
    }

}
