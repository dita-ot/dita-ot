/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2008 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.platform;

import static org.dita.dost.util.Constants.OASIS_CATALOG_NAMESPACE;

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
     */
    @Override
    public void getResult(final ContentHandler buf) throws SAXException {
        final String templateFilePath = paramTable.get(FileGenerator.PARAM_TEMPLATE);
        for (final Value value: valueSet) {
            buf.startElement(OASIS_CATALOG_NAMESPACE, "nextCatalog", "nextCatalog", new AttributesBuilder()
                .add("catalog", FileUtils.getRelativeUnixPath(templateFilePath, value.value))
                .build());
            buf.endElement(OASIS_CATALOG_NAMESPACE, "nextCatalog", "nextCatalog");
        }
    }

}
