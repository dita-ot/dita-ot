/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */
package org.dita.dost.platform;

import java.util.Iterator;

import org.dita.dost.util.Constants;
import org.dita.dost.util.FileUtils;

public class ImportXSLAction extends ImportAction {

	private StringBuffer retBuf = null;

	public ImportXSLAction() {
		super();
		retBuf = new StringBuffer(Constants.INT_1024);
	}
	
	public String getResult() {
		Iterator iter;
		String templateFilePath = (String)paramTable.get("template");
		String value = null;
		iter = valueSet.iterator();
		while(iter.hasNext()){
			value = (String)iter.next();
			retBuf.append(Constants.LINE_SEPARATOR);
			retBuf.append("<xsl:import href=\"");				
			retBuf.append(
					FileUtils.getRelativePathFromMap(
							templateFilePath, value));
			retBuf.append("\"/>");
		}
		return retBuf.toString();
	}

}
