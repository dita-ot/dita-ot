/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2008 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.platform;

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
