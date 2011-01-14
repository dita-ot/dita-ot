/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */
package org.dita.dost.platform;

import java.util.Hashtable;
import java.util.StringTokenizer;

import org.dita.dost.util.StringUtils;

/**
 * InsertDependsAction implements IAction.
 * Parses an attribute value containing comma-separated identifiers.
 * Identifiers inside braces are replaced with the plugin features for the corresponding extension point.
 * @author Deborah Pickett
 */
public class InsertDependsAction implements IAction {

	/** Action parameters. */
	private final Hashtable<String,String> paramTable;
	/** Action value. */
	private String value;
	/** Plug-in features. */
	private Hashtable<String,String> featureTable = null;
	/**
	 * Constructor.
	 */
	public InsertDependsAction() {
		paramTable = new Hashtable<String,String>();
	}
	/**
	 * Get result.
	 * @return result
	 */
	@Override
	public String getResult() {
		final String localname = paramTable.get(FileGenerator.PARAM_LOCALNAME);
		final StringBuffer result = new StringBuffer();
		
		// Parse the attribute value into comma-separated pieces.
		final StringTokenizer valueTokenizer = new StringTokenizer(value, Integrator.FEAT_VALUE_SEPARATOR);
		while (valueTokenizer.hasMoreElements())
		{
			final String token = valueTokenizer.nextToken().trim();
			
			// Pieces which are surrounded with braces are extension points.
			if (token.startsWith("{") && token.endsWith("}"))
			{
				final String extension = token.substring(1, token.length() - 1);
				final String extensionInputs = featureTable.get(extension);
				if (extensionInputs != null)
				{
					if (result.length() != 0) { result.append(Integrator.FEAT_VALUE_SEPARATOR); }
					result.append(extensionInputs);
				}
			}
			else
			{
				// Other pieces are literal.
				if (result.length() != 0) { result.append(Integrator.FEAT_VALUE_SEPARATOR); }
				result.append(token);
			}
		}
		if (result.length() != 0)
		{
			final StringBuilder buf = new StringBuilder();
			
			buf.append(" ").append(localname).append("=\"")
				.append(StringUtils.escapeXML(result.toString())).append("\"");
			return buf.toString();
		}
		else
		{
			return "";
		}
	}
	/**
	 * Set input.
	 * @param input input
	 */
	@Override
	public void setInput(final String input) {
		value = input;
	}
	/**
	 * Set the input parameters.
	 * @param param param
	 */
	@Override
	public void setParam(final String param) {
		final StringTokenizer paramTokenizer = new StringTokenizer(param, Integrator.PARAM_VALUE_SEPARATOR);
		while(paramTokenizer.hasMoreElements()){
			final String paramExpression = paramTokenizer.nextToken();
			final int index = paramExpression.indexOf(Integrator.PARAM_NAME_SEPARATOR);
			if(index > 0){
				paramTable .put(paramExpression.substring(0,index),
						paramExpression.substring(index+1));
			}
		}	
	}
	/**
	 * Set the feature table.
	 * @param h hastable
	 */
	@Override
	public void setFeatures(final Hashtable<String,String> h) {
		featureTable = h;
	}

}
