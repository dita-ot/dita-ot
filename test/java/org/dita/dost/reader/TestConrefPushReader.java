/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.reader;

import java.io.File;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.dita.dost.module.Content;
import org.dita.dost.reader.ConrefPushReader;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class TestConrefPushReader {
	
	public static ConrefPushReader pushReader;
	
	@BeforeClass
	public static void setUp(){
		pushReader = new ConrefPushReader();
	}
	
	@Test
	public void testRead(){
		/*
		 * the part of content of conrefpush_stup.xml is
		 *  <steps>
		 * 	 <step conaction="pushbefore"><cmd>before</cmd></step>
         *   <step conref="conrefpush_stub2.xml#X/A" conaction="mark"/>
         *   <step conref="conrefpush_stub2.xml#X/B" conaction="mark"/>
		 *	 <step conaction="pushafter"><cmd>after</cmd></step>
		 *	 <step conref="conrefpush_stub2.xml#X/C" conaction="pushreplace"><cmd>replace</cmd></step>
         *	</steps>
		 */
		String filename = "test-stub" + File.separator + "conrefpush_stub.xml";
		pushReader.read(filename);
		Content content = pushReader.getContent();
		Set<Map.Entry<String, Hashtable<String,String>>> pushSet =(Set<Map.Entry<String, Hashtable<String,String>>>) content.getCollection();
		Iterator<Map.Entry<String, Hashtable<String,String>>> it= pushSet.iterator();
		Hashtable<String, String> table = null;
		if(it.hasNext()){
			// pushSet has only one entry, so there is no need to iterate it.
			table = it.next().getValue();
			assertTrue(table.containsKey("#X/A|pushbefore"));
			assertEquals(table.get("#X/A|pushbefore"), "<step class=\"- topic/li task/step \"><cmd class=\"- topic/ph task/cmd \">before</cmd></step>");
			assertTrue(table.containsKey("#X/B|pushafter"));
			assertEquals(table.get("#X/B|pushafter"), "<step class=\"- topic/li task/step \"><cmd class=\"- topic/ph task/cmd \">after</cmd></step>");
			assertTrue(table.containsKey("#X/C|pushreplace"));
			assertEquals( "<step class=\"- topic/li task/step \" id=\"C\"><cmd class=\"- topic/ph task/cmd \">replace</cmd></step>",table.get("#X/C|pushreplace"));
			
		}
	}
}
