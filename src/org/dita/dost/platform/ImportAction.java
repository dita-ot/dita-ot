/*
 * (c) Copyright IBM Corp. 2005, 2006 All Rights Reserved.
 */
package org.dita.dost.platform;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.dita.dost.util.Constants;
import org.dita.dost.util.FileUtils;

/**
 *
 * @author Zhang, Yuan Peng
 */
public class ImportAction implements IAction {

	private HashSet valueSet = null;
	private Hashtable paramTable = null;
	private StringBuffer retBuf = null;
	
	public ImportAction() {
		valueSet = new HashSet(16);
		paramTable = new Hashtable();
		retBuf = new StringBuffer(1024);
	}

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
				retBuf.append("<not><equals arg1=\"${transtype}\" arg2=\""+ value +"\" casesensitive=\"false\"/></not>");
			}
		}
		return retBuf.toString();
	}

	public void setInput(String input) {
		StringTokenizer inputTokenizer = new StringTokenizer(input,",");
		while(inputTokenizer.hasMoreElements()){
			valueSet.add(inputTokenizer.nextElement());
		}
	}

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
