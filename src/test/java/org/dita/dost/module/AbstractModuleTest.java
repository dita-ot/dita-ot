package org.dita.dost.module;

import org.apache.commons.io.FilenameUtils;
import org.custommonkey.xmlunit.XMLUnit;
import org.dita.dost.TestUtils;
import org.dita.dost.TestUtils.CachingLogger;
import org.dita.dost.TestUtils.CachingLogger.Message;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.util.Job;
import org.junit.After;
import org.junit.Before;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;

import static junit.framework.Assert.assertEquals;
import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;

public abstract class AbstractModuleTest {

    private final File resourceDir = TestUtils.getResourceDir(getClass());
    private final File expBaseDir = new File(resourceDir, "exp");
    private File tempBaseDir;

    @Before
    public void setUp() throws Exception {
        tempBaseDir = TestUtils.createTempDir(getClass());
        TestUtils.copy(new File(resourceDir, "src"), tempBaseDir);

        TestUtils.resetXMLUnit();
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreComments(true);
    }

    @After
    public void tearDown() throws Exception {
        TestUtils.forceDelete(tempBaseDir);
    }

    public void test(final String testCase) {
        final String testName = FilenameUtils.getBaseName(testCase);
        final File tempDir = new File(tempBaseDir, testName);
        final File expDir = new File(expBaseDir, testName);
        try {
            final AbstractPipelineModule chunkModule = getModule(tempDir);
            final Job job = new Job(tempDir);
            chunkModule.setJob(job);
            final CachingLogger logger = new CachingLogger();
            chunkModule.setLogger(logger);

            final AbstractPipelineInput input = getAbstractPipelineInput();
            chunkModule.execute(input);

            compare(tempDir, expDir);

            for (Message m : logger.getMessages()) {
                assertEquals(false, m.level == Message.Level.ERROR);
            }
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    abstract AbstractPipelineInput getAbstractPipelineInput();

    abstract AbstractPipelineModule getModule(File tempDir);

    private void compare(File actDir, File expDir) throws SAXException, IOException {
        final File[] exps = expDir.listFiles();
        for (final File exp : exps) {
            if (exp.isDirectory()) {
                compare(new File(expDir, exp.getName()), new File(actDir, exp.getName()));
            } else if (exp.getName().equals(".job.xml")) {
                // skip
            } else {
                assertXMLEqual(new InputSource(exp.toURI().toString()),
                        new InputSource(new File(actDir, exp.getName()).toURI().toString()));
            }
        }
    }

}
