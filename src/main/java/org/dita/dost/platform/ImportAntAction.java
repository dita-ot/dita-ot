/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2017 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.platform;

import org.dita.dost.util.XMLUtils;
import org.dita.dost.util.XMLUtils.AttributesBuilder;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import static javax.xml.XMLConstants.NULL_NS_URI;

/**
 * Ant import task.
 *
 * @since 3.0
 */
final class ImportAntAction extends ImportAction {

    /**
     * Generate Ant import task.
     */
    @Override
    public void getResult(final ContentHandler buf) throws SAXException {
        for (final Value value: valueSet) {
            final String[] tokens = value.value.split("[/\\\\]", 2);
            buf.startElement(NULL_NS_URI, "import", "import", XMLUtils.EMPTY_ATTRIBUTES);
            buf.startElement(NULL_NS_URI, "first", "first", XMLUtils.EMPTY_ATTRIBUTES);
            buf.startElement(NULL_NS_URI, "multirootfileset", "multirootfileset", new AttributesBuilder()
                    .add("basedirs", tokens[0])
                    .add("includes", tokens[1])
                    .build());
            buf.endElement(NULL_NS_URI, "multirootfileset", "multirootfileset");
            buf.endElement(NULL_NS_URI, "first", "first");
            buf.endElement(NULL_NS_URI, "import", "import");
        }
    }

}
