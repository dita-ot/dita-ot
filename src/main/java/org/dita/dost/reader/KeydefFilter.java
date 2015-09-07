/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.reader;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.*;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.dita.dost.log.MessageUtils;
import org.dita.dost.util.Job;
import org.dita.dost.util.KeyDef;
import org.dita.dost.util.StringUtils;
import org.dita.dost.writer.AbstractXMLFilter;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * This class extends AbstractReader, used to parse relevant dita topics and
 * ditamap files for GenMapAndTopicListModule.
 * 
 * <p>
 * <strong>Not thread-safe</strong>. Instances can be reused by calling
 * {@link #reset()} between calls to {@link #parse(File)}.
 * </p>
 */
public final class KeydefFilter extends AbstractXMLFilter {
    
    /** Output utilities */
    private Job job;
    private URI inputFile = null;
    /** Basedir of the current parsing file */
    private URI currentDir = null;
    /** Map of key definitions */
    private final Map<String, KeyDef> keysDefMap;
    /** Map to store multi-level keyrefs */
    private final Map<String, String> keysRefMap;

    /**
     * Constructor.
     */
    public KeydefFilter() {
        keysDefMap = new HashMap<>();
        keysRefMap = new HashMap<>();
    }

    /**
     * Set output utilities.
     * 
     * @param job output utils
     */
    public void setJob(final Job job) {
        this.job = job;
    }

    
    /**
     * Set processing input file absolute path.
     * 
     * @param inputFile absolute path to root file
     */
    public void setInputFile(final URI inputFile) {
        this.inputFile = inputFile;
    }
    
    /**
     * Get the Key definitions.
     * 
     * @return Key definitions map
     */
    public Map<String, KeyDef> getKeysDMap() {
        return keysDefMap;
    }
    
    /**
     * Set the relative directory of current file.
     * 
     * @param dir dir
     */
    public void setCurrentDir(final URI dir) {
        currentDir = dir;
    }

    /**
     * 
     * Reset the internal variables.
     */
    public void reset() {
        currentDir = null;
        keysDefMap.clear();
        keysRefMap.clear();
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes atts)
            throws SAXException {
        handleKeysAttr(atts);

        getContentHandler().startElement(uri, localName, qName, atts);
    }


    /**
     * Clean up.
     */
    @Override
    public void endDocument() throws SAXException {
        checkMultiLevelKeys(keysDefMap, keysRefMap);
        
        getContentHandler().endDocument();
    }

    /**
     * Parse the keys attributes.
     * 
     * @param atts all attributes
     */
    private void handleKeysAttr(final Attributes atts) {
        final String attrValue = atts.getValue(ATTRIBUTE_NAME_KEYS);
        if (attrValue != null) {
            URI target = toURI(atts.getValue(ATTRIBUTE_NAME_HREF));
            final URI copyTo = toURI(atts.getValue(ATTRIBUTE_NAME_COPY_TO));
            if (copyTo != null) {
                target = copyTo;
            }
    
            final String keyRef = atts.getValue(ATTRIBUTE_NAME_KEYREF);
            
            // Many keys can be defined in a single definition, like
            // keys="a b c", a, b and c are seperated by blank.
            for (final String key : attrValue.trim().split("\\s+")) {
                if (!keysDefMap.containsKey(key)) {
                    if (target != null && !target.toString().isEmpty()) {
                        final String attrScope = atts.getValue(ATTRIBUTE_NAME_SCOPE);
                        if (attrScope != null && (attrScope.equals(ATTR_SCOPE_VALUE_EXTERNAL) || attrScope.equals(ATTR_SCOPE_VALUE_PEER))) {
                            keysDefMap.put(key, new KeyDef(key, target, attrScope, null, null));
                        } else {
                            String tail = null;
                            if (target.getFragment() != null) {
                                tail = target.getFragment();
                                target = stripFragment(target);
                            }
                            if (!target.isAbsolute()) {
                                target = currentDir.resolve(target);
                            }
                            keysDefMap.put(key, new KeyDef(key, setFragment(target, tail), ATTR_SCOPE_VALUE_LOCAL, null, null));
                        }
                    } else if (!StringUtils.isEmptyString(keyRef)) {
                        // store multi-level keys.
                        keysRefMap.put(key, keyRef);
                    } else {
                        // target is null or empty, it is useful in the future
                        // when consider the content of key definition
                        keysDefMap.put(key, new KeyDef(key, null, null, null, null));
                    }
                } else {
                    logger.info(MessageUtils.getInstance().getMessage("DOTJ045I", key, target != null ? target.toString() : null).toString());
                }
            }
        }
    }
    
    /**
     * Get multi-level keys list
     */
    private List<String> getKeysList(final String key, final Map<String, String> keysRefMap) {
        final List<String> list = new ArrayList<>();
        // Iterate the map to look for multi-level keys
        for (Entry<String, String> entry : keysRefMap.entrySet()) {
            // Multi-level key found
            if (entry.getValue().equals(key)) {
                // add key into the list
                final String entryKey = entry.getKey();
                list.add(entryKey);
                // still have multi-level keys
                if (keysRefMap.containsValue(entryKey)) {
                    // rescuive point
                    final List<String> tempList = getKeysList(entryKey, keysRefMap);
                    list.addAll(tempList);
                }
            }
        }
        return list;
    }

    /**
     * Update keysDefMap for multi-level keys
     */
    private void checkMultiLevelKeys(final Map<String, KeyDef> keysDefMap, final Map<String, String> keysRefMap) {
        String key = null;
        KeyDef value = null;
        // tempMap storing values to avoid ConcurrentModificationException
        final Map<String, KeyDef> tempMap = new HashMap<>();
        for (Entry<String, KeyDef> entry : keysDefMap.entrySet()) {
            key = entry.getKey();
            value = entry.getValue();
            // there is multi-level keys exist.
            if (keysRefMap.containsValue(key)) {
                // get multi-level keys
                final List<String> keysList = getKeysList(key, keysRefMap);
                for (final String multikey : keysList) {
                    // update tempMap
                    tempMap.put(multikey, value);
                }
            }
        }
        // update keysDefMap.
        keysDefMap.putAll(tempMap);
    }

}