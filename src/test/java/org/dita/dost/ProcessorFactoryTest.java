package org.dita.dost;

import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertNotNull;

public class ProcessorFactoryTest {

    @Test
    public void testNewInstance() {
        String ditaDir = System.getProperty("dita.dir");
        if (ditaDir == null) {
            ditaDir = new File("src" + File.separator + "main").getAbsolutePath();
        }
        assertNotNull(ProcessorFactory.newInstance(new File(ditaDir)));
    }

    @Test
    public void testNewProcessor() {
        String ditaDir = System.getProperty("dita.dir");
        if (ditaDir == null) {
            ditaDir = new File("src" + File.separator + "main").getAbsolutePath();
        }
        final ProcessorFactory pf = ProcessorFactory.newInstance(new File(ditaDir));
        assertNotNull(pf.newProcessor("html5"));
    }

    @Test(expected = java.lang.IllegalArgumentException.class)
    public void testUnsupportedTranstype() {
        String ditaDir = System.getProperty("dita.dir");
        if (ditaDir == null) {
            ditaDir = new File("src" + File.separator + "main").getAbsolutePath();
        }
        final ProcessorFactory pf = ProcessorFactory.newInstance(new File(ditaDir));
        pf.newProcessor("xxx");
    }

}
