package org.dita.dost;

import org.apache.tools.ant.BuildException;
import org.dita.dost.exception.DITAOTException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ProcessorTest {

    @Rule
    public final TemporaryFolder tempDirGenerator = new TemporaryFolder();

    private Processor p;
    private File tempDir;

    @Before
    public void setUp() throws Exception {
        String ditaDir = System.getProperty("dita.dir");
        if (ditaDir == null) {
            ditaDir = new File("src" + File.separator + "main").getAbsolutePath();
        }
        final ProcessorFactory pf = ProcessorFactory.newInstance(new File(ditaDir));

        tempDir = tempDirGenerator.newFolder("tmp");
        pf.setBaseTempDir(tempDir);
        p = pf.newProcessor("html5");
    }

    @Test(expected = IllegalStateException.class)
    public void testRunWithoutArgs() throws Exception {
        p.run();
    }

    @Test
    public void testRun() throws DITAOTException, IOException, URISyntaxException {
        final File mapFile = new File(getClass().getClassLoader().getResource("ProcessorTest/test.ditamap").toURI());
        final File out = tempDirGenerator.newFolder("out");
        p.setInput(mapFile)
                .setOutputDir(out)
                .run();

        assertEquals(1, tempDir.listFiles(f -> f.isFile() && f.getName().endsWith(".log")).length);
    }


    @Test(expected = DITAOTException.class)
    public void testBroken() throws DITAOTException, IOException, URISyntaxException {
        final File mapFile = new File(getClass().getClassLoader().getResource("ProcessorTest/broken.dita").toURI());
        final File out = tempDirGenerator.newFolder("out");
        try {
            p.setInput(mapFile)
                    .setOutputDir(out)
                    .createDebugLog(false)
                    .run();
        } catch (Exception e) {
            assertEquals(0, tempDir.listFiles(f -> f.isDirectory()).length);
            assertEquals(0, tempDir.listFiles(f -> f.isFile() && f.getName().endsWith(".log")).length);
            throw e;
        }
    }

    @Test(expected = DITAOTException.class)
    public void testCleanTempOnFailure() throws DITAOTException, IOException, URISyntaxException {
        final File mapFile = new File(getClass().getClassLoader().getResource("ProcessorTest/broken.dita").toURI());
        final File out = tempDirGenerator.newFolder("out");
        try {
            p.setInput(mapFile)
                    .setOutputDir(out)
                    .cleanOnFailure(false)
                    .run();
        } catch (BuildException e) {
            assertEquals(1, tempDir.listFiles(f -> f.isDirectory()).length);
            assertEquals(1, tempDir.listFiles(f -> f.isFile() && f.getName().endsWith(".log")).length);
            throw e;
        }
    }

}
