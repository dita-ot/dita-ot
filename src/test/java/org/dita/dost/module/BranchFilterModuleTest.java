/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2015 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.module;

import org.dita.dost.TestUtils;
import org.dita.dost.TestUtils.CachingLogger;
import org.dita.dost.store.StreamStore;
import org.dita.dost.util.Job;
import org.dita.dost.util.XMLUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

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

public class BranchFilterModuleTest extends BranchFilterModule {

    private final File resourceDir = TestUtils.getResourceDir(BranchFilterModuleTest.class);
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
        job.add(new Job.FileInfo.Builder()
                .src(new File(tempDir, "input.ditamap").toURI())
                .result(new File(tempDir, "input.ditamap").toURI())
                .uri(URI.create("input.ditamap"))
                .format(ATTR_FORMAT_VALUE_DITAMAP)
                .build());
        for (final String uri: Arrays.asList("linux.ditaval", "novice.ditaval", "advanced.ditaval", "mac.ditaval", "win.ditaval")) {
            job.add(new Job.FileInfo.Builder()
                    .src(new File(tempDir, uri).toURI())
                    .result(new File(tempDir, uri).toURI())
                    .uri(URI.create(uri))
                    .format(ATTR_FORMAT_VALUE_DITAVAL)
                    .build());
        }
        for (final String uri: Arrays.asList("install.dita", "perform-install.dita", "configure.dita", "exclude.dita")) {
            job.add(new Job.FileInfo.Builder()
                    .src(new File(tempDir, uri).toURI())
                    .result(new File(tempDir, uri).toURI())
                    .uri(URI.create(uri))
                    .format(ATTR_FORMAT_VALUE_DITA)
                    .build());
        }
        for (final String uri: Arrays.asList("installation-procedure.dita", "getting-started.dita")) {
            job.add(new Job.FileInfo.Builder()
                    .result(new File(tempDir, uri).toURI())
                    .uri(URI.create(uri))
                    .build());
        }
        return job;
    }
    
    private Job getUplevelsJob(File uplevelsTempDir) throws IOException {
        final Job job = new Job(uplevelsTempDir, new StreamStore(uplevelsTempDir, new XMLUtils()));
        job.setInputDir(uplevelsTempDir.toURI());
        job.add(new Job.FileInfo.Builder()
                .src(new File(uplevelsTempDir, "main/testuplevels.ditamap").toURI())
                .result(new File(uplevelsTempDir, "main/testuplevels.ditamap").toURI())
                .uri(URI.create("main/testuplevels.ditamap"))
                .format(ATTR_FORMAT_VALUE_DITAMAP)
                .build());
        for (final String uri: Arrays.asList("main/advanced.ditaval", "main/novice.ditaval")) {
            job.add(new Job.FileInfo.Builder()
                    .src(new File(uplevelsTempDir, uri).toURI())
                    .result(new File(uplevelsTempDir, uri).toURI())
                    .uri(URI.create(uri))
                    .format(ATTR_FORMAT_VALUE_DITAVAL)
                    .build());
        }
        for (final String uri: Arrays.asList("main/parent.dita", "main/kiddo.dita", "main/subdirkiddo.dita",
                "peer/peerkiddo.dita","main/parentdefault.dita", "main/kiddodefault.dita", "main/subdirkiddodefault.dita",
                "peer/peerkiddodefault.dita")) {
            job.add(new Job.FileInfo.Builder()
                    .src(new File(uplevelsTempDir, uri).toURI())
                    .result(new File(uplevelsTempDir, uri).toURI())
                    .uri(URI.create(uri))
                    .format(ATTR_FORMAT_VALUE_DITA)
                    .build());
        }
        for (final String uri: Arrays.asList("main/weechild.dita", "main/subdir/weechild.dita",
                "main/weechilddefault.dita", "main/subdir/weechilddefault.dita")) {
            job.add(new Job.FileInfo.Builder()
                    .result(new File(uplevelsTempDir, uri).toURI())
                    .uri(URI.create(uri))
                    .build());
        }
        return job;
    }

    @After
    public void tearDown() throws Exception {
        TestUtils.forceDelete(tempDir);
    }

//    @Test
//    public void testSplitBranches() throws ParserConfigurationException, IOException, SAXException {
//        final DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
//
//        final Document act = builder.parse(new File(tempDir, "input.ditamap"));
//        currentFile = new File(tempDir, "input.ditamap").toURI();
//        setJob(job);
//        splitBranches(act.getDocumentElement(), Branch.EMPTY);
//
//        final Document exp = builder.parse(new File(expDir, "input_splitBranches.ditamap"));
//        assertXMLEqual(exp, act);
//    }

    @Test
    public void testProcessMap() throws IOException, SAXException {
        final BranchFilterModule m = new BranchFilterModule();
        final Job job = getJob();
        m.setJob(job);
        final CachingLogger logger = new CachingLogger();
        m.setLogger(logger);
        m.setXmlUtils(new XMLUtils());
        
        m.processMap(URI.create("input.ditamap"));
        assertXMLEqual(new InputSource(new File(expDir, "input.ditamap").toURI().toString()),
                new InputSource(new File(tempDir, "input.ditamap").toURI().toString()));

        final List<String> exp = Arrays.asList(
                "installation-procedure.dita", "getting-started.dita",
                //"http://example.com/install.dita",
                //"configure.dita",
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
                "configure-novice-win.dita", "configure-admin-win.dita", "install-linux.dita", "install.dita",
                "exclude.dita"

        );
        Collections.sort(filesExp);
        final List<String> filesAct = Arrays.stream(tempDir.listFiles((dir, name) -> name.endsWith(".dita")))
                .map(f -> f.getName())
                .collect(Collectors.toList());
        Collections.sort(filesAct);
        assertEquals(filesExp, filesAct);
        assertEquals(0, logger.getMessages().stream().filter(msg -> msg.level == ERROR).count());
    }
    
    @Test
    public void testUplevelsMap() throws IOException, SAXException {
        final File uplevelsExpDir = new File (expDir, "uplevels");
        final File uplevelsTempDir = new File (tempDir, "uplevels"); 
        final BranchFilterModule m = new BranchFilterModule();
        final Job job = getUplevelsJob(uplevelsTempDir);
        m.setJob(job);
        final CachingLogger logger = new CachingLogger();
        m.setLogger(logger);
        m.setXmlUtils(new XMLUtils());
        
        m.processMap(URI.create("main/testuplevels.ditamap"));
        assertXMLEqual(new InputSource(new File(uplevelsExpDir, "main/testuplevels.ditamap").toURI().toString()),
                new InputSource(new File(uplevelsTempDir, "main/testuplevels.ditamap").toURI().toString()));

        final List<String> exp = Arrays.asList(
                "main/novice.ditaval", "main/advanced.ditaval",
                "main/kiddodefault.dita", "main/parentdefault.dita", "main/parentdefault-1.dita", 
                "main/parent-novice.dita",  "main/PREFIX-parent-advanced.dita", "main/PREFIX-weechild-advanced.dita", 
                "main/subdirkiddodefault.dita", "main/testuplevels.ditamap", "main/weechilddefault-1.dita", 
                "main/weechild-novice.dita", "main/subdir/PREFIX-weechild-advanced.dita", 
                "main/subdir/weechilddefault-1.dita",  "main/subdir/weechild-novice.dita",
                "peer/peerkiddodefault.dita",  "peer/peerkiddodefault-1.dita",
                "peer/peerkiddo-novice.dita", "peer/PREFIX-peerkiddo-advanced.dita", "main/weechild.dita", 
                "main/subdir/weechilddefault.dita", "main/subdir/weechild.dita", "main/weechilddefault.dita");
        assertEquals(exp.size(), job.getFileInfo().size());
        for (final String f : exp) {
            assertNotNull(job.getFileInfo(URI.create(f)));
        }

        final List<String> filesExpMain = Arrays.asList(
                "main/kiddo.dita", "main/kiddodefault.dita", "main/parent.dita", "main/parentdefault.dita",
                "main/parentdefault-1.dita", "main/parent-novice.dita", "main/PREFIX-parent-advanced.dita",
                "main/PREFIX-weechild-advanced.dita", "main/subdirkiddo.dita", "main/subdirkiddodefault.dita",
                "main/weechilddefault-1.dita", "main/weechild-novice.dita"
        );
        final List<String> filesExpMainSubdir = Arrays.asList(
                "main/subdir/PREFIX-weechild-advanced.dita", "main/subdir/weechilddefault-1.dita", "main/subdir/weechild-novice.dita"
        );
        final List<String> filesExpPeer = Arrays.asList(
                "peer/peerkiddo.dita", "peer/peerkiddodefault.dita", "peer/peerkiddodefault-1.dita",
                "peer/peerkiddo-novice.dita", "peer/PREFIX-peerkiddo-advanced.dita"
        );
        Collections.sort(filesExpMain);
        Collections.sort(filesExpMainSubdir);
        Collections.sort(filesExpPeer);
        final List<String> filesActMain = Arrays.stream(new File (uplevelsTempDir + File.separator + "main").listFiles((dir, name) -> name.endsWith(".dita")))
                .map(f -> "main/" + f.getName())
                .collect(Collectors.toList());
        final List<String> filesActMainSubdir = Arrays.stream(new File (uplevelsTempDir + File.separator + "main/subdir").listFiles((dir, name) -> name.endsWith(".dita")))
                .map(f -> "main/subdir/" + f.getName())
                .collect(Collectors.toList());
        final List<String> filesActPeer = Arrays.stream(new File (uplevelsTempDir + File.separator + "peer").listFiles((dir, name) -> name.endsWith(".dita")))
                .map(f -> "peer/" + f.getName())
                .collect(Collectors.toList());
        
        Collections.sort(filesActMain);
        Collections.sort(filesActMainSubdir);
        Collections.sort(filesActPeer);
        assertEquals(filesExpMain, filesActMain);
        assertEquals(filesExpMainSubdir, filesActMainSubdir);
        assertEquals(filesExpPeer, filesActPeer);
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
        final BranchFilterModule m = new BranchFilterModule();
        final Job job = new Job(tempDir, new StreamStore(tempDir, new XMLUtils()));
        job.setInputDir(tempDir.toURI());
        job.addAll(getDuplicateTopicFileInfos());
        m.setJob(job);
        final CachingLogger logger = new CachingLogger();
        m.setLogger(logger);
        m.setXmlUtils(new XMLUtils());

        m.processMap(URI.create("test.ditamap"));

        final Set<Job.FileInfo> exp = getDuplicateTopicFileInfos();
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
