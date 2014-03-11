/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */
package org.dita.dost.platform;

import java.io.IOException;

import org.dita.dost.util.FileUtils;
import org.dita.dost.util.XMLUtils.AttributesBuilder;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Import catalog file references.
 */
final class ImportCatalogActionRelative extends ImportAction {

    /**
     * get result.
     * @return result
     * @throws IOException 
     */
    @Override
    public void getResult(final ContentHandler buf) throws SAXException {
        final String templateFilePath = paramTable.get(FileGenerator.PARAM_TEMPLATE);
        for (final String value: valueSet) {
            buf.startElement("urn:oasis:names:tc:entity:xmlns:xml:catalog", "nextCatalog", "nextCatalog", new AttributesBuilder()
                .add("catalog", FileUtils.getRelativeUnixPath(templateFilePath, value))
                .build());
            buf.endElement("urn:oasis:names:tc:entity:xmlns:xml:catalog", "nextCatalog", "nextCatalog");
        }
    }

}
