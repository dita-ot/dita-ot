/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2008 All Rights Reserved.
 */
package org.dita.dost.platform;

import static org.dita.dost.util.Constants.*;

import org.dita.dost.util.FileUtils;
import org.dita.dost.util.StringUtils;

/**
 * ImportStringsAction class.
 *
 */
final class ImportStringsAction extends ImportAction {

    /**
     * get result.
     * @return result
     */
    @Override
    public String getResult() {
        final StringBuilder retBuf = new StringBuilder();
        final String templateFilePath = paramTable.get(FileGenerator.PARAM_TEMPLATE);
        for (final String value: valueSet) {
            retBuf.append(LINE_SEPARATOR);
            retBuf.append("<stringfile>");
            retBuf.append(StringUtils.escapeXML(
                    FileUtils.getRelativePath(
                            templateFilePath, value)));
            retBuf.append("</stringfile>");
        }
        return retBuf.toString();
    }

}
