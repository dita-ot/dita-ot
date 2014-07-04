/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2008 All Rights Reserved.
 */
package org.dita.dost.platform;

import java.io.IOException;

import org.dita.dost.util.FileUtils;
import org.dita.dost.util.XMLUtils.AttributesBuilder;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

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
    public void getResult(final ContentHandler buf) throws SAXException {
        final String templateFilePath = paramTable.get(FileGenerator.PARAM_TEMPLATE);
        for (final String value: valueSet) {
            buf.startElement("http://www.w3.org/1999/XSL/Transform", "import", "xsl:import", new AttributesBuilder()
                .add("href", FileUtils.getRelativeUnixPath(templateFilePath, value))
                .build());
            buf.endElement("http://www.w3.org/1999/XSL/Transform", "import", "xsl:import");
        }
    }

}
