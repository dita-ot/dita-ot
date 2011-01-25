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
import org.dita.dost.reader.ListReader;
import org.dita.dost.resolver.DitaURIResolverFactory;
import org.junit.Test;


public class TestListReader {
	public static ListReader listreader;
	public static DitaURIResolverFactory ditaurlresolverfactory =new DitaURIResolverFactory();
	String path="test-stub" + File.separator + "TestListReader" + File.separator + "xhtml" + File.separator;
	String filename="dita.xml.properties";
	String userinputfile="C:" + File.separator + "DITA-OT1.5" + File.separator + "SAXONIBMJDK" + File.separator + "testcase" + File.separator + "12014" + File.separator + ".." + File.separator + ".." + File.separator + "testdata" + File.separator + "12014";
	String userinputmap="map1.ditamap";
	String subjectschemelist="[cvf.ditamap]";
	
    @Test
    public void testread(){
    ditaurlresolverfactory.setPath(path);
	listreader=new ListReader();
	listreader.read(filename);
	assertEquals(userinputfile,listreader.getContent().getValue().toString());
	assertEquals(userinputmap,listreader.getInputMap());	
    assertEquals(subjectschemelist,listreader.getSchemeSet().toString());
   // System.out.println(listreader.getCopytoMap());
    }
   

}

