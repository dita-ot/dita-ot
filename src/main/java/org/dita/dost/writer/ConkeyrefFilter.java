/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */
package org.dita.dost.writer;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.*;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.dita.dost.log.MessageUtils;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.KeyDef;
import org.dita.dost.util.URLUtils;
import org.dita.dost.util.XMLUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Filter for processing content key reference elements in DITA files. Instances are
 * reusable but not thread-safe.
 */
public final class ConkeyrefFilter extends AbstractXMLFilter {

    private File tempDir;
    private File inputFile;
    private Map<String, KeyDef> keys;

    public void setKeyDefinitions(final Collection<KeyDef> keydefs) {
        keys = new HashMap<String, KeyDef>();
        for (final KeyDef k : keydefs) {
            keys.put(k.keys, k);
        }
    }

    public void setTempDir(final File tempDir) {
        this.tempDir = tempDir;
    }

    public void setCurrentFile(final File inputFile) {
        this.inputFile = inputFile;
    }

    // XML filter methods ------------------------------------------------------

    @Override
    public void startElement(final String uri, final String localName, final String name, final Attributes atts)
            throws SAXException {
        AttributesImpl resAtts = null;
        final String conkeyref = atts.getValue(ATTRIBUTE_NAME_CONKEYREF);
        if (conkeyref != null) {
            final int keyIndex = conkeyref.indexOf(SLASH);
            final String key = keyIndex != -1 ? conkeyref.substring(0, keyIndex) : conkeyref;
            if (keys.containsKey(key)) {
                URI target = getRelativePath(keys.get(key).href);
                if (keyIndex != -1) {
                    target = setFragment(target, conkeyref.substring(keyIndex + 1));
                }
                resAtts = new AttributesImpl(atts);
                XMLUtils.addOrSetAttribute(resAtts, ATTRIBUTE_NAME_CONREF, target.toString());
                XMLUtils.removeAttribute(resAtts, ATTRIBUTE_NAME_CONKEYREF);
            } else {
                logger.logError(MessageUtils.getInstance().getMessage("DOTJ046E", conkeyref).toString());
            }
        }
        getContentHandler().startElement(uri, localName, name, resAtts != null ? resAtts : atts);
    }

    /**
     * Update href URI.
     * 
     * @param href href URI
     * @return updated href URI
     */
    private URI getRelativePath(final URI href) {
        final URI filePath = new File(tempDir, inputFile.getPath()).toURI();
        final URI keyValue = tempDir.toURI().resolve(stripFragment(href));
        return URLUtils.getRelativePath(filePath, keyValue);
    }

}
