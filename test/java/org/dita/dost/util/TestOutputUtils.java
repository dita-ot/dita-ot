/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
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
		assertEquals(OutputUtils.OutterControl.WARN, OutputUtils.getOutterControl());
		OutputUtils.setOutterControl("FAIL");
		assertEquals(OutputUtils.OutterControl.FAIL, OutputUtils.getOutterControl());
		OutputUtils.setOutterControl("WARN");
		assertEquals(OutputUtils.OutterControl.WARN ,OutputUtils.getOutterControl());
		OutputUtils.setOutterControl("QUIET");
		assertEquals(OutputUtils.OutterControl.QUIET, OutputUtils.getOutterControl());
		try {
            OutputUtils.setOutterControl(null);
            fail();
        } catch (final NullPointerException e) {}
	}
	
	
	@Test
	public void testsetonlytopicinmap()
	{
		
		
		OutputUtils.setOnlyTopicInMap(null);
		assertEquals(false,OutputUtils.getOnlyTopicInMap());
		
		OutputUtils.setOnlyTopicInMap("false");
		assertEquals(false,OutputUtils.getOnlyTopicInMap());
		OutputUtils.setOnlyTopicInMap("true");
		assertEquals(true,OutputUtils.getOnlyTopicInMap());
		
	}
	
	@Test
	public void testsetgeneratecopyouter()
	{
		assertEquals(OutputUtils.Generate.NOT_GENERATEOUTTER, OutputUtils.getGeneratecopyouter());
		OutputUtils.setGeneratecopyouter("1");
		assertEquals(OutputUtils.Generate.NOT_GENERATEOUTTER, OutputUtils.getGeneratecopyouter());
		OutputUtils.setGeneratecopyouter("2");
		assertEquals(OutputUtils.Generate.GENERATEOUTTER, OutputUtils.getGeneratecopyouter());
		OutputUtils.setGeneratecopyouter("3");
		assertEquals(OutputUtils.Generate.OLDSOLUTION, OutputUtils.getGeneratecopyouter());
		try {
		    OutputUtils.setGeneratecopyouter(null);
            fail();
        } catch (final NumberFormatException e) {}
	}

}
