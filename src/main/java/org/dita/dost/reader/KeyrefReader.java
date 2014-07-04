/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.reader;

import static org.dita.dost.util.Constants.*;

import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.dita.dost.log.DITAOTLogger;
import org.xml.sax.InputSource;

/**
 * KeyrefReader class which reads DITA map file to collect key definitions. Instances are reusable but not thread-safe.
 */
public final class KeyrefReader implements AbstractReader {

    private DITAOTLogger logger;
    private final DocumentBuilder builder;
    /** Key definition map, where map key is the key name and map value is XML definition */  
    private final Map<String, Element> keyDefTable;

    private Set<String> keys;

    /**
     * Constructor.
     */
    public KeyrefReader() {
        keyDefTable = new HashMap<String, Element>();
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (final ParserConfigurationException e) {
            throw new RuntimeException("Unable to initialize XML parser: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void read(final File filename) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
    }
    
    /**
     * Get key definitions. Each key definition Element has a distinct Document.
     * 
     * @return key definition map where map key is key name and map value is XML definition of the key 
     */
    public Map<String, Element> getKeyDefinition() {
        return Collections.unmodifiableMap(keyDefTable);
    }

    /**
     * Read key definitions
     * 
     * @param filename absolute URI to DITA map with key definitions
     */
    public void read(final URI filename) {
        Document doc = null;
        try {
            doc = builder.parse(new InputSource(filename.toString()));
        } catch (final Exception e) {
            logger.error("Failed to parse map: " + e.getMessage(), e);
            return;
        }
        final NodeList elems = doc.getDocumentElement().getElementsByTagName("*");
        for (int i = 0; i < elems.getLength(); i++) {
            final Element elem = (Element) elems.item(i);
            final String classValue = elem.getAttribute(ATTRIBUTE_NAME_CLASS);
            final String keyName = elem.getAttribute(ATTRIBUTE_NAME_KEYS);
            if (!keyName.isEmpty() && MAP_TOPICREF.matches(classValue)) {
                for (final String key: keyName.trim().split("\\s+")) {
                  if (keys.contains(key) && !keyDefTable.containsKey(key)){
                      final Document d = builder.newDocument();
                      final Element copy = (Element) d.importNode(elem, true);
                      d.appendChild(copy);
                      keyDefTable.put(key, copy);
                  }
              }
            }
        }
    }
    
    /**
     * Set keys to be read.
     * 
     * @param set key set
     */
    public void setKeys(final Set<String> keys){
        this.keys = keys;
    }
    
}
