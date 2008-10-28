package org.dita.dost.module;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.StringTokenizer;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.pipeline.AbstractPipelineInput;
import org.dita.dost.pipeline.AbstractPipelineOutput;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.reader.ChunkMapReader;
import org.dita.dost.util.Constants;
import org.dita.dost.writer.CoderefResolver;

public class CoderefModule implements AbstractPipelineModule {

	public CoderefModule() {
		// TODO Auto-generated constructor stub
	}

	public AbstractPipelineOutput execute(AbstractPipelineInput input)
			throws DITAOTException {
		// TODO Auto-generated method stub
		String baseDir = ((PipelineHashIO) input).getAttribute(Constants.ANT_INVOKER_PARAM_BASEDIR);
		String tempDir = ((PipelineHashIO) input).getAttribute(Constants.ANT_INVOKER_PARAM_TEMPDIR);
					    
	    File ditalist = null;
	    File xmlDitalist=null;
	    Properties prop = new Properties();
	    StringTokenizer st = null;
	    String file = null;
	    CoderefResolver resolver = new CoderefResolver();
	    
	    Content content;

	    DITAOTJavaLogger javaLogger = new DITAOTJavaLogger();
	    
        if (!new File(tempDir).isAbsolute()) {
        	tempDir = new File(baseDir, tempDir).getAbsolutePath();
        }
        //change to xml property
	    ditalist = new File(tempDir, Constants.FILE_NAME_DITA_LIST);
	    xmlDitalist=new File(tempDir,Constants.FILE_NAME_DITA_LIST_XML);
	    
		     
	    try{
	    	if(xmlDitalist.exists())
	    		prop.loadFromXML(new FileInputStream(xmlDitalist));
	    	else 
	    		prop.load(new FileInputStream(ditalist));
		}catch(IOException ioe){
			throw new DITAOTException(ioe);
		}
		
		st = new StringTokenizer(prop.getProperty(Constants.CODEREF_LIST),Constants.COMMA);
		while (st.hasMoreTokens()){
			file = st.nextToken();
			if(file != null && !Constants.STRING_EMPTY.equals(file.trim())){
				resolver.write(new File(tempDir,file.trim()).getAbsolutePath());
			}
		}
		
		return null;
	}

}
