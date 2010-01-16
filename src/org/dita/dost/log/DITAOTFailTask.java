/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2005 All Rights Reserved.
 */
package org.dita.dost.log;

import java.io.File;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Exit;
import org.dita.dost.exception.DITAOTException;
/**
 * Class description goes here. 
 *
 * @author Wu, Zhi Qiang
 */
public class DITAOTFailTask extends Exit {
	private String id = null;

	private Properties prop = null;

	/**
	 * Default Construtor.
	 *
	 */
	public DITAOTFailTask(){
	}
	/**
	 * Set the id.
	 * @param identifier The id to set.
	 * 
	 */
	public void setId(String identifier) {
		this.id = identifier;
	}

	/**
	 * Set the parameters.
	 * @param params The prop to set.          
	 */
	public void setParams(String params) {
		StringTokenizer tokenizer = new StringTokenizer(params, ";");
		prop = new Properties();
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			int pos = token.indexOf("=");
			this.prop.put(token.substring(0, pos), token.substring(pos + 1));
		}
	}

	/**
	 * Task execute point.
	 * @throws BuildException exception
	 * @see org.apache.tools.ant.taskdefs.Exit#execute()
	 */
	public void execute() throws BuildException {
		initMessageFile();
		MessageBean msgBean=MessageUtils.getMessage(id, prop);
		setMessage(msgBean.toString());
		try{
			super.execute();
		}catch(BuildException ex){
			throw new BuildException(msgBean.toString(),new DITAOTException(msgBean,null,msgBean.toString()));
		}
	}
	
	private void initMessageFile() {
		String messageFile = getProject().getProperty(
				"args.message.file");
		
		if(!new File(messageFile).exists()){
			MessageUtils.loadDefaultMessages();
			return;
		}
		
		if (!new File(messageFile).isAbsolute()) {
			messageFile = new File(getProject().getBaseDir(), messageFile)
					.getAbsolutePath();
		}
		
		MessageUtils.loadMessages(messageFile);
	}
	
}
