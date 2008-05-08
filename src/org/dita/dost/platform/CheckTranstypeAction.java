/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */
package org.dita.dost.platform;

import java.util.Iterator;

import org.dita.dost.util.Constants;

public class CheckTranstypeAction extends ImportAction {

	private StringBuffer retBuf = null;

	public CheckTranstypeAction() {
		super();
		retBuf = new StringBuffer(Constants.INT_1024);
	}

	public String getResult() {
		Iterator iter;
		String value = null;
		iter = valueSet.iterator();
		while(iter.hasNext()){
			value = (String)iter.next();
			retBuf.append("<not><equals arg1=\"${transtype}\" arg2=\"")
				.append(value).append("\" casesensitive=\"false\"/></not>");
		}
		return retBuf.toString();
	}

}
