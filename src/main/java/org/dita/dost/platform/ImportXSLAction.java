/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2008 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.platform;

import org.dita.dost.util.FileUtils;
import org.dita.dost.util.URLUtils;
import org.dita.dost.util.XMLUtils.AttributesBuilder;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import java.net.URI;

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
        for (final Value value: valueSet) {
            final URI href = getHref(value);
            buf.startElement("http://www.w3.org/1999/XSL/Transform", "import", "xsl:import", new AttributesBuilder()
                .add("href", href.toString())
                .build());
            buf.endElement("http://www.w3.org/1999/XSL/Transform", "import", "xsl:import");
        }
    }

    private URI getHref(final Value value) {
        final URI pluginDir = featureTable.get(value.id).getPluginDir().toURI();
        final URI templateFile = URLUtils.toFile(value.value).toURI().normalize();
        final URI template = pluginDir.relativize(templateFile);
        if (value.id == null || template.isAbsolute()) {
            final String templateFilePath = paramTable.get(FileGenerator.PARAM_TEMPLATE);
            return URLUtils.toURI(FileUtils.getRelativeUnixPath(templateFilePath, value.value));
        }
        return URI.create("plugin:" + value.id + ":" + template);
    }

}
