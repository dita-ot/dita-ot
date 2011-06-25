/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2005 All Rights Reserved.
 */
package org.dita.dost.log;

import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Echo;
import org.dita.dost.util.LogUtils;

/**
 * Class description goes here.
 * 
 * @author Wu, Zhi Qiang
 */
public final class DITAOTEchoTask extends Echo {
	private String id = null;

	private Properties prop = null;

	/**
	 * Default Construtor.
	 *
	 */
	public DITAOTEchoTask(){
	}
	/**
	 * Setter function for id.
	 * @param identifier The id to set.         
	 */
	public void setId(final String identifier) {
		this.id = identifier;
	}

	/**
	 * Set the parameters.
	 * @param params  The prop to set.     
	 */
	public void setParams(final String params) {
		final StringTokenizer tokenizer = new StringTokenizer(params, ";");
		prop = new Properties();
		while (tokenizer.hasMoreTokens()) {
			final String token = tokenizer.nextToken();
			final int pos = token.indexOf("=");
			this.prop.put(token.substring(0, pos), token.substring(pos + 1));
		}
	}

	/**
	 * Task execute point.
	 * @throws BuildException exception
	 * @see org.apache.tools.ant.taskdefs.Echo#execute()
	 */
	public void execute() throws BuildException {
		setMessage(MessageUtils.getMessage(id, prop).toString());
		super.execute();
		final MessageBean msgBean=MessageUtils.getMessage(id, prop);
		if(msgBean!=null){
			LogUtils.increaseNumOfExceptionByType(msgBean.getType());
		}
	}
	
}
