/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2008 All Rights Reserved.
 */
package org.dita.dost.platform;

import org.dita.dost.util.StringUtils;
/**
 * CheckTranstypeAction class.
 *
 */
final class CheckTranstypeAction extends ImportAction {

    /**
     * Get result.
     * @return result
     */
    @Override
    public String getResult() {
        final StringBuilder retBuf = new StringBuilder();
        for (final String value: valueSet) {
            retBuf.append("<not><equals arg1=\"${transtype}\" arg2=\"")
            .append(StringUtils.escapeXML(value)).append("\" casesensitive=\"false\"/></not>");
        }
        return retBuf.toString();
    }

}
