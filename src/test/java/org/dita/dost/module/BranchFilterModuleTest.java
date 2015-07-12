package org.dita.dost.module;

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;

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
                .build());
    }

    @After
    public void tearDown() throws Exception {
        TestUtils.forceDelete(tempDir);
        //System.err.println(tempDir.getAbsolutePath());
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

}
