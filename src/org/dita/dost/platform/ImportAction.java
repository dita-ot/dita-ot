/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2005, 2006 All Rights Reserved.
 */
package org.dita.dost.platform;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.dita.dost.util.Constants;
import org.dita.dost.util.FileUtils;

/**
 * ImportAction implements IAction and import the resource 
 * provided by plug-ins into the xsl files and ant scripts.
 * @author Zhang, Yuan Peng
 */
public class ImportAction implements IAction {
	private Set valueSet = null;
	private Hashtable paramTable = null;
	private StringBuffer retBuf = null;
	
	/**
	 * Default Constructor
	 */
	public ImportAction() {
		valueSet = new LinkedHashSet(Constants.INT_16);
		paramTable = new Hashtable();
		retBuf = new StringBuffer(Constants.INT_1024);
	}

	/**
	 * @see org.dita.dost.platform.IAction#getResult()
	 */
	public String getResult() {
		Iterator iter;
		String templateFilePath = (String)paramTable.get("template");
		String extensionId = (String)paramTable.get("extension");
		String value = null;
		iter = valueSet.iterator();
		while(iter.hasNext()){
			value = (String)iter.next();
			if(templateFilePath.endsWith(".xsl")){
				retBuf.append(Constants.LINE_SEPARATOR);
				retBuf.append("<xsl:import href=\"");				
				retBuf.append(
						FileUtils.getRelativePathFromMap(
								templateFilePath.replaceAll("\\\\","/"),
								value.replaceAll("\\\\","/")));
				retBuf.append("\"/>");
			}else if("dita.conductor.lib.import".equals(extensionId)){
				retBuf.append(Constants.LINE_SEPARATOR);
				retBuf.append("<pathelement location=\"");
				retBuf.append(
						FileUtils.getRelativePathFromMap(
								templateFilePath.replaceAll("\\\\","/"),
								value.replaceAll("\\\\","/")));
				retBuf.append("\"/>");
			}else if("dita.conductor.transtype.check".equals(extensionId)){
				retBuf.append("<not><equals arg1=\"${transtype}\" arg2=\"")
					.append(value).append("\" casesensitive=\"false\"/></not>");
			}
		}
		return retBuf.toString();
	}

	/**
	 * @see org.dita.dost.platform.IAction#setInput(java.lang.String)
	 */
	public void setInput(String input) {
		StringTokenizer inputTokenizer = new StringTokenizer(input,",");
		while(inputTokenizer.hasMoreElements()){
			valueSet.add(inputTokenizer.nextElement());
		}
	}

	/**
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
				paramTable.put(paramExpression.substring(0,index),
						paramExpression.substring(index+1));
			}
		}	
	}

}
