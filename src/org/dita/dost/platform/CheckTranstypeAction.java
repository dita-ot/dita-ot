/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */
package org.dita.dost.platform;

import org.dita.dost.util.Constants;
import org.dita.dost.util.StringUtils;
/**
 * CheckTranstypeAction class.
 *
 */
public class CheckTranstypeAction extends ImportAction {

	private final StringBuffer retBuf;
	/**
	 * Constructor.
	 */
	public CheckTranstypeAction() {
		super();
		retBuf = new StringBuffer(Constants.INT_1024);
	}
	/**
	 * Get result.
	 * @return result
	 */
	@Override
	public String getResult() {
		for (final String value: valueSet) {
			retBuf.append("<not><equals arg1=\"${transtype}\" arg2=\"")
				.append(StringUtils.escapeXML(value)).append("\" casesensitive=\"false\"/></not>");
		}
		return retBuf.toString();
	}

}
