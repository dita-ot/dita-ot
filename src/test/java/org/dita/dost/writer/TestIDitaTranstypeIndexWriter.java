/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2010 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.writer;
import java.io.File;

import org.dita.dost.TestUtils;
import org.dita.dost.writer.CHMIndexWriter;
import org.dita.dost.writer.EclipseIndexWriter;
import org.dita.dost.writer.IDitaTranstypeIndexWriter;
import org.dita.dost.writer.JavaHelpIndexWriter;
import org.junit.Test;
import static org.junit.Assert.assertEquals;


public class TestIDitaTranstypeIndexWriter {
    public static IDitaTranstypeIndexWriter idita1= new CHMIndexWriter();
    public static IDitaTranstypeIndexWriter idita2= new EclipseIndexWriter();
    public static IDitaTranstypeIndexWriter idita3= new JavaHelpIndexWriter();
    @Test
    public void testiditatranstypeindexwriter()
    {
        final String rootpath=System.getProperty("user.dir");
        final String path=rootpath + File.separator + "resources" + File.separator + "index.xml";
        final String outputfilename="resources" + File.separator + "iditatranstypewriter";
        assertEquals(TestUtils.testStub.getName() + File.separator + "iditatranstypewriter.hhk",idita1.getIndexFileName(outputfilename));
        assertEquals(path,idita2.getIndexFileName(outputfilename));
        assertEquals(TestUtils.testStub.getName() + File.separator + "iditatranstypewriter_index.xml",idita3.getIndexFileName(outputfilename));
    }

}
