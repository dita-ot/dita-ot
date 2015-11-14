package org.dita.dost.module;

import static junit.framework.Assert.assertEquals;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.module.BranchFilterModule.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import org.custommonkey.xmlunit.XMLUnit;
import org.dita.dost.TestUtils;
import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.util.Job;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class BranchFilterModuleTest {

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
        job.add(new Job.FileInfo.Builder()
                .src(new File(tempDir, "input.ditamap").toURI())
                .uri(new URI("input.ditamap"))
                .format(ATTR_FORMAT_VALUE_DITAMAP)
                .build());
        for (final String uri: Arrays.asList("install.dita", "perform-install.dita", "configure.dita")) {
            job.add(new Job.FileInfo.Builder()
                    .src(new File(tempDir, uri).toURI())
                    .uri(new URI(uri))
                    .format(ATTR_FORMAT_VALUE_DITA)
                    .build());
        }
    }

    @After
    public void tearDown() throws Exception {
        TestUtils.forceDelete(tempDir);
    }

    @Test
    public void testProcessMap() throws SAXException, IOException {
        final BranchFilterModule m = new BranchFilterModule();
        m.setJob(job);
        m.setLogger(new DITAOTJavaLogger());
        
        m.processMap(new File(tempDir, "input.ditamap").toURI());
        assertXMLEqual(new InputSource(new File(expDir, "input.ditamap").toURI().toString()),
                new InputSource(new File(tempDir, "input.ditamap").toURI().toString()));
    }

    @Test
    public void testGenerateCopyTo() throws URISyntaxException {
        assertEquals(new URI("foo.bar"), generateCopyTo("foo.bar", new Branch(null, null, null, null)));
        assertEquals(new URI("baz-foo.bar"), generateCopyTo("foo.bar", new Branch("baz-", null, null, null)));
        assertEquals(new URI("foo-baz.bar"), generateCopyTo("foo.bar", new Branch(null, "-baz", null, null)));
        assertEquals(new URI("qux-foo-baz.bar"), generateCopyTo("foo.bar", new Branch("qux-", "-baz", null, null)));
        assertEquals(new URI("sub.dir/foo.bar"), generateCopyTo("sub.dir/foo.bar", new Branch(null, null, null, null)));
        assertEquals(new URI("sub.dir/baz-foo.bar"), generateCopyTo("sub.dir/foo.bar", new Branch("baz-", null, null, null)));
        assertEquals(new URI("sub.dir/foo-baz.bar"), generateCopyTo("sub.dir/foo.bar", new Branch(null, "-baz", null, null)));
        assertEquals(new URI("sub.dir/qux-foo-baz.bar"), generateCopyTo("sub.dir/foo.bar", new Branch("qux-", "-baz", null, null)));
        assertEquals(new URI("foo"), generateCopyTo("foo", new Branch(null, null, null, null)));
        assertEquals(new URI("baz-foo"), generateCopyTo("foo", new Branch("baz-", null, null, null)));
        assertEquals(new URI("foo-baz"), generateCopyTo("foo", new Branch(null, "-baz", null, null)));
        assertEquals(new URI("qux-foo-baz"), generateCopyTo("foo", new Branch("qux-", "-baz", null, null)));
        assertEquals(new URI("sub.dir/foo"), generateCopyTo("sub.dir/foo", new Branch(null, null, null, null)));
        assertEquals(new URI("sub.dir/baz-foo"), generateCopyTo("sub.dir/foo", new Branch("baz-", null, null, null)));
        assertEquals(new URI("sub.dir/foo-baz"), generateCopyTo("sub.dir/foo", new Branch(null, "-baz", null, null)));
        assertEquals(new URI("sub.dir/qux-foo-baz"), generateCopyTo("sub.dir/foo", new Branch("qux-", "-baz", null, null)));
    }

}
