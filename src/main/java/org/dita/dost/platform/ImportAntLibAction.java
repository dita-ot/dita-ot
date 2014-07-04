/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2008 All Rights Reserved.
 */
package org.dita.dost.platform;

import static javax.xml.XMLConstants.*;
import static org.dita.dost.util.XMLUtils.*;

import org.dita.dost.util.FileUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * ImportAntLibAction class.
 *
 */
final class ImportAntLibAction extends ImportAction {

    /**
     * get result.
     * @return result
     * @throws SAXException 
     */
    @Override
    public void getResult(final ContentHandler retBuf) throws SAXException {
        final String templateFilePath = paramTable.get(FileGenerator.PARAM_TEMPLATE);
        for (final String value: valueSet) {
            final String resolvedValue = FileUtils.getRelativeUnixPath(
                    templateFilePath, value);
            if(FileUtils.isAbsolutePath(resolvedValue)){
                // if resolvedValue is absolute path
                retBuf.startElement(NULL_NS_URI, "pathelement", "pathelement", new AttributesBuilder()
                    .add("location", resolvedValue)
                    .build());
                retBuf.endElement(NULL_NS_URI, "pathelement", "pathelement");
            }else{// if resolvedValue is relative path
                retBuf.startElement(NULL_NS_URI, "pathelement", "pathelement", new AttributesBuilder()
                    .add("location", "${dita.dir}${file.separator}" + resolvedValue)
                    .build());
                retBuf.endElement(NULL_NS_URI, "pathelement", "pathelement");
            }
        }
    }

}
