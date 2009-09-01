package org.dita.dost.module;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Map.Entry;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.reader.ConrefPushReader;
import org.dita.dost.resolver.DitaURIResolverFactory;
import org.dita.dost.resolver.URIResolverAdapter;
import org.dita.dost.util.Constants;
import org.dita.dost.util.ListUtils;
import org.dita.dost.util.StringUtils;
import org.dita.dost.writer.ConrefPushParser;

public class ConrefPushModule implements AbstractPipelineModule {

	public AbstractPipelineOutput execute(AbstractPipelineInput input)
			throws DITAOTException {
		String tempDir = ((PipelineHashIO) input).getAttribute(Constants.ANT_INVOKER_PARAM_TEMPDIR);
		if (! new File(tempDir).isAbsolute()){
			tempDir = new File(tempDir).getAbsolutePath();
		}

		Properties properties = null;
		try{
			properties = ListUtils.getDitaList();
		}catch(IOException e){
			DITAOTJavaLogger javaLogger = new DITAOTJavaLogger();
			javaLogger.logException(e);
		}

		Set<String> conrefpushlist = StringUtils.restoreSet(properties.getProperty(Constants.CONREF_PUSH_LIST));
		ConrefPushReader reader = new ConrefPushReader();
		for(String fileName:conrefpushlist){
			//FIXME: this reader calculate parent directory
			reader.read(new File(tempDir,fileName).getAbsolutePath());
		}
		
		Set<Map.Entry<String, Hashtable<String, String>>> pushSet = (Set<Map.Entry<String, Hashtable<String,String>>>) reader.getContent().getCollection();
		Iterator<Map.Entry<String, Hashtable<String,String>>> iter = pushSet.iterator();
		
		while(iter.hasNext()){
			Map.Entry<String, Hashtable<String,String>> entry = iter.next();
			ConrefPushParser parser = new ConrefPushParser();
			Content content = new ContentImpl();
			content.setValue(entry.getValue());
			parser.setContent(content);
			//pass the tempdir to ConrefPushParser
			parser.setTempDir(tempDir);
			//FIXME:This writer creates and renames files, have to 
			parser.write(entry.getKey());
		}
		
		return null;
	}

}
