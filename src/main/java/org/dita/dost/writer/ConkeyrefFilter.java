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
import java.util.List;
import java.util.Map;

import org.dita.dost.log.MessageUtils;
import org.dita.dost.util.DelayConrefUtils;
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
    /** Delayed conref utils, may be {@code null} */
    private DelayConrefUtils delayConrefUtils;
    
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

    public void setDelayConrefUtils(final DelayConrefUtils delayConrefUtils) {
        this.delayConrefUtils = delayConrefUtils;
    }
    
    // XML filter methods ------------------------------------------------------

    @Override
    public void startElement(final String uri, final String localName, final String name, final Attributes atts)
            throws SAXException {
        AttributesImpl resAtts = null;
        final String conkeyref = atts.getValue(ATTRIBUTE_NAME_CONKEYREF);
        conkeyref: if (conkeyref != null) {
            final int keyIndex = conkeyref.indexOf(SLASH);
            final String key = keyIndex != -1 ? conkeyref.substring(0, keyIndex) : conkeyref;
            final KeyDef keyDef = keys.get(key);
            if (keyDef != null) {
                final String id = keyIndex != -1 ? conkeyref.substring(keyIndex + 1) : null;
                if (delayConrefUtils != null) {
                    final List<Boolean> list = delayConrefUtils.checkExport(stripFragment(keyDef.href).toString(), id, key, tempDir);
                    final boolean idExported = list.get(0);
                    final boolean keyrefExported = list.get(1);
                    //both id and key are exported and transtype is eclipsehelp
                    if (idExported && keyrefExported) {
                        break conkeyref;
                    }
                }
                resAtts = new AttributesImpl(atts);
                XMLUtils.removeAttribute(resAtts, ATTRIBUTE_NAME_CONKEYREF);
                final KeyDef k = keys.get(key);
                if (k.href != null && (k.scope == null || k.scope.equals(ATTR_SCOPE_VALUE_LOCAL))) {
                    URI target = getRelativePath(k.href);
                    if (id != null) {
                        target = setFragment(target, id);
                    }
                    XMLUtils.addOrSetAttribute(resAtts, ATTRIBUTE_NAME_CONREF, target.toString());
                } else {
                    logger.warn(MessageUtils.getInstance().getMessage("DOTJ060W", key, conkeyref).toString());
                }
            } else {
                logger.error(MessageUtils.getInstance().getMessage("DOTJ046E", conkeyref).toString());
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
