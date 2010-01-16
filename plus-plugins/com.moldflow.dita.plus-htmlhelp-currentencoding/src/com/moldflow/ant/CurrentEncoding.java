package com.moldflow.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import java.nio.charset.Charset;

public class CurrentEncoding extends Task {

	private String property = null;
	
	public void execute() throws BuildException {
		if (property == null) {
			throw new BuildException("'property' attribute required");
		}
		 getProject().setProperty(property, Charset.defaultCharset().name());
	}
	
	public void setProperty(String s) {
		property = s;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println(Charset.defaultCharset().name());
	}

}
