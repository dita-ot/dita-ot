/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2008 All Rights Reserved.
 */

package org.dita.dost.invoker;

import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.pipeline.PipelineFacade;
import org.dita.dost.pipeline.PipelineHashIO;
import org.dita.dost.util.StringUtils;

public class ExtensibleAntInvoker extends Task {

	private final static String KEY_VALUE_PAIR_SEPARATOR = ";";

	private final static String KEY_VALUE_EQUAL_SIGN = "=";
	
	private DITAOTJavaLogger javaLogger = null;
	
	private PipelineFacade pipeline;

	private PipelineHashIO pipelineInput;
	
	private ArrayList<Param> params;

	public ExtensibleAntInvoker() {
		super();
		javaLogger = new DITAOTJavaLogger();
		pipeline = new PipelineFacade();
		pipelineInput = new PipelineHashIO();
		params = new ArrayList();
	}
	
	public void setBasedir(String s) {
		pipelineInput.setAttribute("basedir", s);
	}

	public String getBasedir() {
		return pipelineInput.getAttribute("basedir");
	}
	
	public void setModule(String module) {
		pipelineInput.setAttribute("module", module);
	}

	public String getModule() {
		return pipelineInput.getAttribute("module");
	}

	public void setMessage(String m) {
		pipelineInput.setAttribute("message", m);
	}
	
	public String getMessage() {
		return pipelineInput.getAttribute("message");
	}
	
	/**
	 * Set extra parameter values for input
	 * @param extParam extended parameters string, key value pair string separated by
	 *            ";" eg. extparam="maplinks=XXXX;other=YYYY"          
	 */
	public void setExtparam(String extParam) {
		String keyValueStr = null;
		String attrName = null;
		String attrValue = null;
		StringTokenizer extParamStrTokenizer = new StringTokenizer(extParam,
				KEY_VALUE_PAIR_SEPARATOR);

		while (extParamStrTokenizer.hasMoreTokens()) {
			int p;
			keyValueStr = extParamStrTokenizer.nextToken();
			p = keyValueStr.indexOf(KEY_VALUE_EQUAL_SIGN);

			if (p <= 0) {
				String msg = null;
				Properties params = new Properties();

				params.put("%1", keyValueStr);
				msg = MessageUtils.getMessage("DOTJ006F", params).toString();
				throw new RuntimeException(msg);
			}

			attrName = keyValueStr.substring(0, p).trim();
			attrValue = keyValueStr.substring(p + 1).trim();

			if (StringUtils.isEmptyString(attrName) ||
					StringUtils.isEmptyString(attrValue)) {
				String msg = null;
				Properties params = new Properties();

				params.put("%1", keyValueStr);
				msg = MessageUtils.getMessage("DOTJ006F", params).toString();
				throw new RuntimeException(msg);
			}

			pipelineInput.setAttribute(attrName, attrValue);
		}

	}

	/* Handle nested parameters.  Add the key/value to the pipeline hash
	 * only if the "if" attribute refers to a property that exists.
	 */
	public Param createParam() {
		Param p = new Param();
		params.add(p);
		return p;
	}

	/**
	 * execution point of this invoker
	 * @throws BuildException
	 */
	public void execute() throws BuildException {
		if (getModule() == null) {
			throw new BuildException("module attribute must be specified");
		}
		/* Set basedir if not already set. */
		if (pipelineInput.getAttribute("basedir") == null) {
			pipelineInput.setAttribute("basedir", getProject().getBaseDir().getAbsolutePath());
		}
		/* Set params. */
		for (Param p : params) {
			if (!p.isValid()) {
				throw new BuildException("Incomplete parameter");
			}
			// Check the "if" attribute.
			String ifProperty = p.getIf();
			if (ifProperty == null
					|| getProject().getProperties().containsKey(ifProperty))
			{
				pipelineInput.setAttribute(p.getName(), p.getExpression());
			}
		}

		try {
			pipeline.execute(getModule(), pipelineInput);
		} catch (DITAOTException e) {
			throw new BuildException(e.getMessage(), e);
		}
	}
	
	/* Nested parameters. */
	public static class Param {
		private String name;
		private String value;
		private String ifproperty;
		
		public String getName() {
			return name;
		}

		public boolean isValid() {
			return (name != null && value != null);
		}

		public void setName(String s) {
			name = s;
		}

		public String getExpression() {
			return value;
		}

		public void setExpression(String v) {
			value = v;
		}

		public String getIf() {
			return ifproperty;
		}

		public void setIf(String p) {
			ifproperty = p;
		}
	}
}