package org.dita.dost.junit;

import static org.junit.Assert.assertEquals;
import org.dita.dost.reader.ListReader;
import org.dita.dost.resolver.DitaURIResolverFactory;
import org.junit.Test;


public class TestListReader {
	public static ListReader listreader;
	public static DitaURIResolverFactory ditaurlresolverfactory =new DitaURIResolverFactory();
	String path="test-stub/TestListReader/xhtml/";
	String filename="dita.xml.properties";
	String userinputfile="C:/DITA-OT1.5/SAXONIBMJDK/testcase/12014/../../testdata/12014";
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

