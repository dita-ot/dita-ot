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
 * ImportXSLAction class.
 *
 */
final class ImportXSLAction extends ImportAction {

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
            retBuf.append("<xsl:import href=\"");
            retBuf.append(StringUtils.escapeXML(
                    FileUtils.getRelativePath(
                            templateFilePath, value)));
            retBuf.append("\"/>");
        }
        return retBuf.toString();
    }

}
