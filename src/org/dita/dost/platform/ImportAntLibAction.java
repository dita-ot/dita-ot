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
 * ImportAntLibAction class.
 *
 */
public class ImportAntLibAction extends ImportAction {

	private StringBuffer retBuf = null;
	/**
	 * Constructor.
	 */
	public ImportAntLibAction() {
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
            String resolvedValue = FileUtils.getRelativePathFromMap(
                templateFilePath, value);
			if(FileUtils.isAbsolutePath(resolvedValue)){
				// if resolvedValue is absolute path
				retBuf.append("<pathelement location=\"");
				retBuf.append(resolvedValue);
				retBuf.append("\"/>");
			}else{// if resolvedValue is relative path
				retBuf.append("<pathelement location=\"${dita.dir}${file.separator}");
				retBuf.append(resolvedValue);
				retBuf.append("\"/>");
			}
		}
		return retBuf.toString();
	}

}
