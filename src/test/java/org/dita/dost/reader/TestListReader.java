/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.reader;

import static org.junit.Assert.assertEquals;
import static org.dita.dost.util.Constants.*;

import java.io.File;

import org.junit.Test;

import org.dita.dost.TestUtils;
import org.dita.dost.resolver.DitaURIResolverFactory;

public class TestListReader {

    private static final File resourceDir = new File(TestUtils.testStub, TestListReader.class.getSimpleName());

    @Test
    public void testread(){
        final File path = new File(resourceDir, "xhtml" + File.separator);
        DitaURIResolverFactory.setPath(path.getAbsolutePath());
        final ListReader listreader = new ListReader();
        //final String filename = "dita.xml.properties";
        listreader.read(null);
        final String userinputfile = "C:" + UNIX_SEPARATOR + "DITA-OT1.5" + UNIX_SEPARATOR + "SAXONIBMJDK" + UNIX_SEPARATOR + "testcase" + UNIX_SEPARATOR + "12014" + UNIX_SEPARATOR + ".." + UNIX_SEPARATOR + ".." + UNIX_SEPARATOR + "testdata" + UNIX_SEPARATOR + "12014";
        assertEquals(userinputfile, listreader.getContent().getValue().toString());
        final String userinputmap = "map1.ditamap";
        assertEquals(userinputmap, listreader.getInputMap());
        final String subjectschemelist = "[cvf.ditamap]";
        assertEquals(subjectschemelist, listreader.getSchemeSet().toString());
    }


}

