/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2008 All Rights Reserved.
 */
package org.dita.dost.platform;

import java.io.File;

import org.dita.dost.util.FileUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * InsertAntActionRelative inserts the children of the root element of an XML document
 * into a plugin extension point, rewriting relative file references so that they
 * are still correct in their new location.
 *
 * Attributes affected: import/@file
 * 
 * @author Deborah Pickett
 *
 */
final class InsertAntActionRelative extends InsertAction {

    @Override
    public void startElement(final String uri, final String localName, final String qName,
            final Attributes attributes) throws SAXException {
        final AttributesImpl attrBuf = new AttributesImpl();

        final int attLen = attributes.getLength();
        for (int i = 0; i < attLen; i++){
            String value;
            if ("import".equals(localName) && "file".equals(attributes.getQName(i))
                    && !FileUtils.isAbsolutePath(attributes.getValue(i))) {
                // Rewrite file path to be local to its final resting place.
                final File targetFile = new File(
                        new File(currentFile).getParentFile(),
                        attributes.getValue(i));
                value = FileUtils.getRelativePath(
                        paramTable.get(FileGenerator.PARAM_TEMPLATE),
                        targetFile.toString());
            }
            else {
                value = attributes.getValue(i);
            }
            attrBuf.addAttribute(attributes.getURI(i), attributes.getLocalName(i),
                    attributes.getQName(i), attributes.getType(i), value);
        }

        super.startElement(uri, localName, qName, attrBuf);
    }
}
