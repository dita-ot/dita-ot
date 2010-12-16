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

	private Hashtable<String,String> paramTable = null;
	private String value;
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
	public String getResult() {
		String localname = paramTable.get("localname");
		StringBuffer result = new StringBuffer();
		
		// Parse the attribute value into comma-separated pieces.
		StringTokenizer valueTokenizer = new StringTokenizer(value, ",");
		while (valueTokenizer.hasMoreElements())
		{
			String token = ((String) valueTokenizer.nextElement()).trim();
			
			// Pieces which are surrounded with braces are extension points.
			if (token.startsWith("{") && token.endsWith("}"))
			{
				String extension = token.substring(1, token.length() - 1);
				String extensionInputs = (String)featureTable.get(extension);
				if (extensionInputs != null)
				{
					if (result.length() != 0) { result.append(","); }
					result.append(extensionInputs);
				}
			}
			else
			{
				// Other pieces are literal.
				if (result.length() != 0) { result.append(","); }
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
	 * @see org.dita.dost.platform.IAction#setInput(java.lang.String)
	 */
	public void setInput(String input) {
		value = input;
	}
	/**
	 * Set the input parameters.
	 * @param param param
	 * @see org.dita.dost.platform.IAction#setParam(java.lang.String)
	 */
	public void setParam(String param) {
		StringTokenizer paramTokenizer = new StringTokenizer(param,";");
		String paramExpression = null;
		int index;
		while(paramTokenizer.hasMoreElements()){
			paramExpression = (String) paramTokenizer.nextElement();
			index = paramExpression.indexOf("=");
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
	public void setFeatures(Hashtable<String,String> h) {
		featureTable = h;
	}

}
