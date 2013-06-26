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
 * ImportAntLibAction class.
 *
 */
final class ImportAntLibAction extends ImportAction {

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
            final String resolvedValue = FileUtils.getRelativePath(
                    templateFilePath, value);
            if(FileUtils.isAbsolutePath(resolvedValue)){
                // if resolvedValue is absolute path
                retBuf.append("<pathelement location=\"");
                retBuf.append(StringUtils.escapeXML(resolvedValue));
                retBuf.append("\"/>");
            }else{// if resolvedValue is relative path
                retBuf.append("<pathelement location=\"${dita.dir}${file.separator}");
                retBuf.append(StringUtils.escapeXML(resolvedValue));
                retBuf.append("\"/>");
            }
        }
        return retBuf.toString();
    }

}
