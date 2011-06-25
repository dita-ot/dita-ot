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
import org.junit.Test;
import org.dita.dost.util.OutputUtils;
public class TestOutputUtils {
	public static OutputUtils outpututils;
	@Test
	public  void testsetoutercontrol()
	{
		OutputUtils.setOutterControl(null);
		assertEquals("WARN",OutputUtils.getOutterControl());
		OutputUtils.setOutterControl("FAIL");
		assertEquals("FAIL",OutputUtils.getOutterControl());
		OutputUtils.setOutterControl("WARN");
		assertEquals("WARN",OutputUtils.getOutterControl());
		OutputUtils.setOutterControl("QUIET");
		assertEquals("QUIET",OutputUtils.getOutterControl());
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
		OutputUtils.setGeneratecopyouter(null);
		assertEquals(1,OutputUtils.getGeneratecopyouter());
		OutputUtils.setGeneratecopyouter("1");
		assertEquals(1,OutputUtils.getGeneratecopyouter());
		OutputUtils.setGeneratecopyouter("2");
		assertEquals(2,OutputUtils.getGeneratecopyouter());
		OutputUtils.setGeneratecopyouter("3");
		assertEquals(3,OutputUtils.getGeneratecopyouter());
	}

}
