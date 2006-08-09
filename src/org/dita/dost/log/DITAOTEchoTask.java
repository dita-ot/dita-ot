/*
 * (c) Copyright IBM Corp. 2005 All Rights Reserved.
 */
package org.dita.dost.log;

import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Echo;

/**
 * Class description goes here.
 * 
 * @author Wu, Zhi Qiang
 */
public class DITAOTEchoTask extends Echo {
	private String id = null;

	private Properties prop = null;

	/**
	 * Default Construtor
	 *
	 */
	public DITAOTEchoTask(){
	}
	/**
	 * Setter function for id
	 * @param identifier The id to set.         
	 */
	public void setId(String identifier) {
		this.id = identifier;
	}

	/**
	 * Set the parameters
	 * @param params  The prop to set.     
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
	 * Task execute point
	 * @see org.apache.tools.ant.taskdefs.Echo#execute()
	 */
	public void execute() throws BuildException {
		setMessage(MessageUtils.getMessage(id, prop).toString());
		super.execute();
	}
	
}
