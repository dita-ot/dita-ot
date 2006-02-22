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
	 * @param id
	 *            The id to set.
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @param params
	 *            The prop to set.
	 */
	public void setParams(String params) {
		prop = new Properties();
		StringTokenizer tokenizer = new StringTokenizer(params, ";");
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			int pos = token.indexOf("=");
			this.prop.put(token.substring(0, pos), token.substring(pos + 1));
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.tools.ant.taskdefs.Echo#execute()
	 */
	public void execute() throws BuildException {
		setMessage(MessageUtils.getMessage(id, prop).toString());
		super.execute();
	}
	
}
