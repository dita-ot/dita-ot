/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */
package org.dita.dost.platform;

import java.util.Iterator;

import org.dita.dost.util.Constants;
import org.dita.dost.util.FileUtils;
/**
 * ImportXSLAction class.
 *
 */
public class ImportXSLAction extends ImportAction {

	private StringBuffer retBuf = null;
	/**
	 * Constructor.
	 */
	public ImportXSLAction() {
		super();
		retBuf = new StringBuffer(Constants.INT_1024);
	}
	/**
	 * get result.
	 * @return result
	 */
	public String getResult() {
		Iterator<String> iter;
		String templateFilePath = paramTable.get("template");
		String value = null;
		iter = valueSet.iterator();
		while(iter.hasNext()){
			value = iter.next();
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
