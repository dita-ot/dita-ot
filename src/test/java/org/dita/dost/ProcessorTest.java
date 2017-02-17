package org.dita.dost;

import org.dita.dost.exception.DITAOTException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.helpers.NOPLogger;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

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
    public void testRun() throws DITAOTException {
        final File mapFile;
        final File out;
        try {
            mapFile = new File(getClass().getClassLoader().getResource("ProcessorTest/test.ditamap").toURI());
            out = tmpDir.newFolder("out");
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
        p.setInput(mapFile)
                .setOutput(out)
                .setLogger(NOPLogger.NOP_LOGGER)
                .run();
    }


    @Test(expected = org.dita.dost.exception.DITAOTException.class)
    public void testBroken() throws DITAOTException {
        final File mapFile;
        final File out;
        try {
            mapFile = new File(getClass().getClassLoader().getResource("ProcessorTest/broken.dita").toURI());
            out = tmpDir.newFolder("out");
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
        p.setInput(mapFile)
                .setOutput(out)
                .setLogger(NOPLogger.NOP_LOGGER)
                .run();
    }

}
