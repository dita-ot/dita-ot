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

import java.io.File;

import org.junit.Test;

import org.dita.dost.resolver.DitaURIResolverFactory;

public class TestListReader {

    private static final File resourceDir = new File("test-stub", TestListReader.class.getSimpleName());

    @Test
    public void testread(){
        final File path = new File(resourceDir, "xhtml" + File.separator);
        DitaURIResolverFactory.setPath(path.getAbsolutePath());
        final ListReader listreader = new ListReader();
        //final String filename = "dita.xml.properties";
        listreader.read(null);
        final String userinputfile = "C:" + File.separator + "DITA-OT1.5" + File.separator + "SAXONIBMJDK" + File.separator + "testcase" + File.separator + "12014" + File.separator + ".." + File.separator + ".." + File.separator + "testdata" + File.separator + "12014";
        assertEquals(userinputfile, listreader.getContent().getValue().toString());
        final String userinputmap = "map1.ditamap";
        assertEquals(userinputmap, listreader.getInputMap());
        final String subjectschemelist = "[cvf.ditamap]";
        assertEquals(subjectschemelist, listreader.getSchemeSet().toString());
    }


}

