/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2008 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.platform;

import static org.dita.dost.util.Constants.COLON;

import java.io.File;

import org.dita.dost.util.FileUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;


/**
 * InsertCatalogActionRelative inserts the children of the root element of an XML document
 * into a plugin extension point, rewriting relative file references so that they
 * are still correct in their new location.
 *
 * Attributes affected: (public|system|uri)/@uri
 *   (nextCatalog|delegateURI|delegateSystem|delegatePublic)/@catalog
 *   (rewriteSystem|rewriteURI)/@rewritePrefix
 * To do: Handle xml:base.
 *
 * @author Deborah Pickett
 * @deprecated use {@link ImportCatalogActionRelative} instead
 */
@Deprecated
final class InsertCatalogActionRelative extends InsertAction {

    @Override
    public void startElement(final String uri, final String localName, final String qName,
            final Attributes attributes) throws SAXException {
        final AttributesImpl attrBuf = new AttributesImpl();

        final int attLen = attributes.getLength();
        for (int i = 0; i < attLen; i++) {
            String value;
            final File targetFile = new File(new File(currentFile).getParentFile(), attributes.getValue(i));
            final int index = attributes.getIndex("xml:base");
            if ((("public".equals(localName) ||
                    "system".equals(localName) ||
                    "uri".equals(localName)) && "uri".equals(attributes.getQName(i)) ||
                    ("nextCatalog".equals(localName) ||
                            "delegateURI".equals(localName) ||
                            "delegateSystem".equals(localName) ||
                            "delegatePublic".equals(localName)) && "catalog".equals(attributes.getQName(i)) ||
                            ("rewriteSystem".equals(localName) ||
                                    "rewriteURI".equals(localName)) && "rewritePrefix".equals(attributes.getQName(i)))
                                    && !attributes.getValue(i).contains(COLON)) {
                // Rewrite URI to be local to its final resting place.
                if (index == -1) {
                    //If there are no xml:base attributes, then we need to split
                    final String path = FileUtils.getFullPathNoEndSeparator(FileUtils.getRelativeUnixPath(
                            paramTable.get(FileGenerator.PARAM_TEMPLATE),
                            targetFile.toString())) + "/";
                    final String filename = FileUtils.getName(FileUtils.getRelativeUnixPath(
                            paramTable.get(FileGenerator.PARAM_TEMPLATE),
                            targetFile.toString()));
                    attrBuf.addAttribute("http://www.w3.org/XML/1998/namespace", "base",
                            "xml:base", attributes.getType(i), path);
                    attrBuf.addAttribute(attributes.getURI(i), attributes.getLocalName(i),
                            attributes.getQName(i), attributes.getType(i), filename);
                } else {
                    //If there is an xml:base attribute, then we do nothing.
                    value = attributes.getValue(i);
                    attrBuf.addAttribute(attributes.getURI(i), attributes.getLocalName(i),
                            attributes.getQName(i), attributes.getType(i), value);
                }
            } else if (i == index) {
                //We've found xml:base.  Need to add parent plugin directory to the original value.
                value = FileUtils.getFullPathNoEndSeparator(FileUtils.getRelativeUnixPath(
                        paramTable.get(FileGenerator.PARAM_TEMPLATE),
                        targetFile.toString())) + "/" + attributes.getValue(i);
                attrBuf.addAttribute(attributes.getURI(i), attributes.getLocalName(i),
                        attributes.getQName(i), attributes.getType(i), value);
            } else {
                value = attributes.getValue(i);
                attrBuf.addAttribute(attributes.getURI(i), attributes.getLocalName(i),
                        attributes.getQName(i), attributes.getType(i), value);
            }
        }

        super.startElement(uri, localName, qName, attrBuf);
    }
}
