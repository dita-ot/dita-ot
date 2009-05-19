package org.dita.dost.junit;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.pipeline.PipelineFacade;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.reader.DitaValReader;
import org.junit.BeforeClass;
import org.junit.Test;

//please create the temp dir manually first.
public class TestDitaValReader {
	
	public static DitaValReader reader;
	private static String baseDir = "test-stub/DITA-OT1.5";
	private static String tempDir = "DITAVAL/temp";
	
	
	@BeforeClass
	public static void setUp() throws Exception{
		reader = new DitaValReader();

		
		PipelineFacade facade = new PipelineFacade();
		PipelineHashIO pipelineInput = new PipelineHashIO();
		
		String inputDir = "DITAVAL";
		String inputMap = "DITAVAL/DITAVAL_testdata1.ditamap";
		String outDir = "DITAVAL/out";
		pipelineInput.setAttribute("inputmap", inputMap);
		pipelineInput.setAttribute("basedir", baseDir);
		pipelineInput.setAttribute("inputdir", inputDir);
		pipelineInput.setAttribute("outputdir", outDir);
		pipelineInput.setAttribute("tempDir", tempDir);
		pipelineInput.setAttribute("ditadir", "");
		pipelineInput.setAttribute("ditaext", ".xml");
		pipelineInput.setAttribute("indextype", "xhtml");
		pipelineInput.setAttribute("encoding", "en-US");
		pipelineInput.setAttribute("targetext", ".html");
		pipelineInput.setAttribute("validate", "false");
		pipelineInput.setAttribute("generatecopyouter", "1");
		pipelineInput.setAttribute("outercontrol", "warn");
		pipelineInput.setAttribute("onlytopicinmap", "false");
		pipelineInput.setAttribute("ditalist", tempDir + "/dita.list");
		pipelineInput.setAttribute("maplinks", tempDir + "/maplinks.unordered");
		
		facade.execute("GenMapAndTopicList", pipelineInput);
		
	
	}
	
	@Test
	public void testRead() throws DITAOTException{
		
		reader.read(baseDir+"/DITAVAL/DITAVAL_1.ditaval");
		
		HashMap map = reader.getFilterMap();
		assertEquals("include", map.get("audience=Cindy"));
		assertEquals("flag", map.get("produt=p1"));
		assertEquals("exclude", map.get("product=ABase_ph"));
		assertEquals("include", map.get("product=AExtra_ph"));
		assertEquals("exclude", map.get("product=Another_ph"));
		assertEquals("flag", map.get("platform=Windows"));
		assertEquals("flag", map.get("platform=Linux"));
		assertEquals("exclude", map.get("keyword=key1"));
		assertEquals("flag", map.get("keyword=key2"));
		assertEquals("include", map.get("keyword=key3"));
		
		/*Set<Map.Entry> set = map.entrySet();
		Iterator<Map.Entry> itr = set.iterator();
		while (itr.hasNext()) {
			Map.Entry mapEntry = (Map.Entry) itr.next();
			mapEntry.getKey();
			mapEntry.getValue();
			//....	
		}*/
	}
	
}
