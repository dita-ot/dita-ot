/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2008 All Rights Reserved.
 */
package org.dita.dost.platform;

import static javax.xml.XMLConstants.NULL_NS_URI;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.XMLUtils.AttributesBuilder;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * ImportStringsAction class.
 *
 */
final class ImportStringsAction extends ImportAction {

    /**
     * get result.
     * @return result
     */
    @Override
    public void getResult(final ContentHandler buf) throws SAXException {
        final String templateFilePath = paramTable.get(FileGenerator.PARAM_TEMPLATE);
        for (final String value: valueSet) {
            buf.startElement(NULL_NS_URI, "stringfile", "stringfile", new AttributesBuilder().build());
            final char[] location =  FileUtils.getRelativeUnixPath(templateFilePath, value).toCharArray();
            buf.characters(location, 0, location.length);
            buf.endElement(NULL_NS_URI, "stringfile", "stringfile");
        }
    }

}
