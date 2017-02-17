package org.dita.dost;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.helpers.NOPLogger;

import java.io.File;

import static org.junit.Assert.fail;

public class ProcessorTest {

    @Rule
    public final TemporaryFolder tmpDir = new TemporaryFolder();

    private Processor p;

    @Before
    public void setUp() throws Exception {
        String ditaDir = System.getProperty("dita.dir");
        if (ditaDir == null) {
            ditaDir = new File("src" + File.separator + "main").getAbsolutePath();
        }
        final ProcessorFactory pf = ProcessorFactory.newInstance(new File(ditaDir));
        pf.setTempDir(tmpDir.newFolder("tmp"));
        p = pf.newProcessor("html5");
    }

    @Test
    public void testRunWithoutArgs() throws Exception {
        try {
            p.run();
            fail();
        } catch (final IllegalStateException e) {
        }
    }

    @Test
    public void testRun() throws Exception {
        final File mapFile = new File(getClass().getClassLoader().getResource("ProcessorTest/test.ditamap").toURI());
        p.setInput(mapFile)
                .setOutput(tmpDir.newFolder("out"))
                .setLogger(NOPLogger.NOP_LOGGER)
                .run();
    }

}
