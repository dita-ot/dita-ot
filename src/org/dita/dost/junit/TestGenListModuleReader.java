/**
 * 
 */
package org.dita.dost.junit;


import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Map;
import java.util.Set;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.module.Content;
import org.dita.dost.reader.GenListModuleReader;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * @author william
 *
 */
public class TestGenListModuleReader {
	
	public static GenListModuleReader reader;
	private static String baseDir = "test-stub/DITA-OT1.5";
	
	private static String inputDir = "keyrefs/maps_parallel_to_topics/maps";
	private static String rootFile = inputDir + "/root-map-01.ditamap";
	
	@BeforeClass
	public static void setUp(){
		//parser = new ConrefPushParser();
		String ditaDir = "";
		//get absolute path
		ditaDir = new File(baseDir, "").getAbsolutePath();
		
		boolean validate = false;
		
		try {
			reader.initXMLReader(ditaDir, validate, rootFile);
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		reader = new GenListModuleReader();
	}

	@Test
	public void testParse() throws DITAOTException{
		try {
			//String inputDir = baseDir + "/maps";
			//inputDir = baseDir;
			//String inputMap = inputDir + "/root-map-01.ditamap";
			
			reader.parse(new File(baseDir, rootFile));
			Content content = reader.getContent();
		    Set<String> conref = reader.getConrefTargets();
			Set<String> chunk = reader.getChunkTopicSet();
			Map<String, String> copytoMap = reader.getCopytoMap();
			Set<String> hrefTargets = reader.getHrefTargets();
			Set<String> hrefTopic =reader.getHrefTopicSet();
			Set<String> copytoSet = reader.getIgnoredCopytoSourceSet();
			Map<String, String> keyDMap = reader.getKeysDMap();
			Set<String> nonConref = reader.getNonConrefCopytoTargets();
			Set<String> nonCopyTo = reader.getNonCopytoResult();
			Set<String> outDita = reader.getOutDitaFilesSet();
			Set<String> outFiles = reader.getOutFilesSet();
			Set<String> resourceOnlySet = reader.getResourceOnlySet();
			Set<String> subsidiaryTargets = reader.getSubsidiaryTargets();
			
			assertEquals(0, conref.size());
			
			assertEquals(0, chunk.size());
			
			assertEquals(0, copytoMap.size());
			
			assertTrue(hrefTargets.contains("..\\topics\\xreffin-topic-1.xml"));
			assertTrue(hrefTargets.contains("..\\topics\\target-topic-c.xml"));
			assertTrue(hrefTargets.contains("..\\topics\\target-topic-a.xml"));
			
			assertTrue(hrefTopic.contains("..\\topics\\xreffin-topic-1.xml"));
			assertTrue(hrefTopic.contains("..\\topics\\target-topic-c.xml"));
			assertTrue(hrefTopic.contains("..\\topics\\target-topic-a.xml"));
			
			assertEquals(0, copytoSet.size());
			
			assertEquals("..\\topics\\target-topic-c.xml",keyDMap.get("target_topic_2"));
			assertEquals("..\\topics\\target-topic-a.xml",keyDMap.get("target_topic_1"));
			
			assertTrue(nonConref.contains("..\\topics\\xreffin-topic-1.xml"));
			assertTrue(nonConref.contains("..\\topics\\target-topic-c.xml"));
			assertTrue(nonConref.contains("..\\topics\\target-topic-a.xml"));
			
			assertTrue(nonCopyTo.contains("..\\topics\\xreffin-topic-1.xml"));
			assertTrue(nonCopyTo.contains("..\\topics\\target-topic-c.xml"));
			assertTrue(nonCopyTo.contains("..\\topics\\target-topic-a.xml"));
			
			assertTrue(outDita.contains("..\\topics\\xreffin-topic-1.xml"));
			assertTrue(outDita.contains("..\\topics\\target-topic-c.xml"));
			assertTrue(outDita.contains("..\\topics\\target-topic-a.xml"));
			
			assertTrue(outFiles.contains("..\\topics\\xreffin-topic-1.xml"));
			assertTrue(outFiles.contains("..\\topics\\target-topic-c.xml"));
			assertTrue(outFiles.contains("..\\topics\\target-topic-a.xml"));
			
			assertEquals(0, resourceOnlySet.size());
			
			assertEquals(0, subsidiaryTargets.size());
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
