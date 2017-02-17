package org.dita.dost;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.dita.dost.ProcessorFactory;
import org.junit.Test;

import java.io.File;

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

}
