/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.util;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.dita.dost.TestUtils;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.util.GenUtils;
import java.io.File;
import java.io.IOException;
public class TestGenutils {

    private File tempDir;

    @Before
    public void setUp() throws IOException, DITAOTException {
        tempDir = TestUtils.createTempDir(getClass());
    }

    @Test
    public void testflush() throws IOException
    {
        final File myFile=new File(tempDir, "genutils.xml");

        GenUtils.clear();
        GenUtils.setOutput(myFile.getAbsolutePath());
        GenUtils.startElement("topic");
        GenUtils.addAttr("id", "this is a id");
        GenUtils.addText("this is a text");
        GenUtils.endElement("topic");
        GenUtils.flush();

        assertEquals("<?xml version=\"1.0\" encoding=\"utf-8\"?><topic id=\"this is a id\">this is a text</topic>",
                TestUtils.readFileToString(myFile));
    }

    @After
    public void tearDown() throws IOException {
        TestUtils.forceDelete(tempDir);
    }

}
