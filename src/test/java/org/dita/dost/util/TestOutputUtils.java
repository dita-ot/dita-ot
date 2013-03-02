/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.util;

import static org.junit.Assert.*;
import org.junit.Test;
import org.dita.dost.util.OutputUtils;
public class TestOutputUtils {
    public static OutputUtils outpututils;
    @Test
    public  void testsetoutercontrol()
    {
        final OutputUtils outputUtils = new OutputUtils();
        assertEquals(OutputUtils.OutterControl.WARN, outputUtils.getOutterControl());
        outputUtils.setOutterControl("FAIL");
        assertEquals(OutputUtils.OutterControl.FAIL, outputUtils.getOutterControl());
        outputUtils.setOutterControl("WARN");
        assertEquals(OutputUtils.OutterControl.WARN ,outputUtils.getOutterControl());
        outputUtils.setOutterControl("QUIET");
        assertEquals(OutputUtils.OutterControl.QUIET, outputUtils.getOutterControl());
        try {
            outputUtils.setOutterControl(null);
            fail();
        } catch (final NullPointerException e) {}
    }


    @Test
    public void testsetonlytopicinmap()
    {
        final OutputUtils outputUtils = new OutputUtils();
        outputUtils.setOnlyTopicInMap(null);
        assertEquals(false,outputUtils.getOnlyTopicInMap());

        outputUtils.setOnlyTopicInMap("false");
        assertEquals(false,outputUtils.getOnlyTopicInMap());
        outputUtils.setOnlyTopicInMap("true");
        assertEquals(true,outputUtils.getOnlyTopicInMap());

    }

    @Test
    public void testsetgeneratecopyouter()
    {
        final OutputUtils outputUtils = new OutputUtils();
        assertEquals(OutputUtils.Generate.NOT_GENERATEOUTTER, outputUtils.getGeneratecopyouter());
        outputUtils.setGeneratecopyouter("1");
        assertEquals(OutputUtils.Generate.NOT_GENERATEOUTTER, outputUtils.getGeneratecopyouter());
        outputUtils.setGeneratecopyouter("2");
        assertEquals(OutputUtils.Generate.GENERATEOUTTER, outputUtils.getGeneratecopyouter());
        outputUtils.setGeneratecopyouter("3");
        assertEquals(OutputUtils.Generate.OLDSOLUTION, outputUtils.getGeneratecopyouter());
        try {
            outputUtils.setGeneratecopyouter(null);
            fail();
        } catch (final NumberFormatException e) {}
    }

}
