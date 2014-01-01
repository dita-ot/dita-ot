/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2008 All Rights Reserved.
 */
package org.dita.dost.platform;

import static org.dita.dost.util.Constants.*;

import java.io.IOException;

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
     * @throws IOException 
     */
    @Override
    public void getResult(final Appendable retBuf) throws IOException {
        final String templateFilePath = paramTable.get(FileGenerator.PARAM_TEMPLATE);
        for (final String value: valueSet) {
            retBuf.append(LINE_SEPARATOR);
            retBuf.append("<xsl:import href=\"");
            retBuf.append(StringUtils.escapeXML(
                    FileUtils.getRelativeUnixPath(
                            templateFilePath, value)));
            retBuf.append("\"/>");
        }
    }

}
