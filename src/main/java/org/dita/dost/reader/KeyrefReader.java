/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.reader;

import static org.dita.dost.util.Constants.*;

import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Element;
import org.w3c.dom.Document;

import org.dita.dost.module.Content;
import org.dita.dost.resolver.DitaURIResolverFactory;
import org.dita.dost.resolver.URIResolverAdapter;
import org.dita.dost.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * KeyrefReader class which reads DITA map file to collect key definitions. Instances are reusable but not thread-safe.
 */
public final class KeyrefReader extends AbstractXMLReader {

    /** Key definition. */
    protected static final class KeyDef {
        
        protected final String key;
        protected final StringBuffer keyDefContent;
        protected int keyDefLevel = 1;
        
        /**
         * Construct a new key definition.
         * 
         * @param key key name
         */
        public KeyDef(final String key) {
            this.key = key;
            keyDefContent = new StringBuffer();
        }
        
    }

    private final XMLReader reader;
    /** Key definition map, where map key is the key name and map value is XML definition */  
    private final Map<String, Element> keyDefTable;

    private Stack<KeyDef> keyDefs;

    private Set<String> keys;

    /**
     * Constructor.
     */
    public KeyrefReader(){
        keyDefTable = new HashMap<String, Element>();
        try {
            reader = StringUtils.getXMLReader();
            reader.setFeature(FEATURE_NAMESPACE_PREFIX, true);
            reader.setFeature(FEATURE_NAMESPACE, true);
            reader.setContentHandler(this);
        } catch (final SAXException ex) {
            throw new RuntimeException("Unable to initialize XML parser: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void characters(final char[] ch, final int start, final int length)
            throws SAXException {
        if(!keyDefs.isEmpty()) {
            keyDefAppend(StringUtils.escapeXML(ch, start, length));
        }
    }


    @Override
    public void endElement(final String uri, final String localName, final String name)
            throws SAXException {
        if(!keyDefs.isEmpty()){
            keyDefs.peek().keyDefLevel--;
            keyDefAppend(LESS_THAN);
            keyDefAppend(SLASH);
            keyDefAppend(name);
            keyDefAppend(GREATER_THAN);
            if(keyDefs.peek().keyDefLevel == 0){
                // to the end of the key definition, set the flag false
                // and put the key definition to table.
                final KeyDef keyDef = keyDefs.pop();
                for(final String keyName: keyDef.key.split(" ")){
                    if(!keyName.equals("")) {
                        keyDefTable.put(keyName, keyDefToDoc(keyDef.keyDefContent.toString()).getDocumentElement());
                    }

                }
            }
        }
    }

    @Override
    public Content getContent() {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Get key definitions. Each key definition Element has a distinct Document.
     * 
     * @return key definition map where map key is key name and map value is XML definition of the key 
     */
    public Map<String, Element> getKeyDefinition() {
        return Collections.unmodifiableMap(keyDefTable);
    }

    @Override
    public void read(final String filename) {
        keyDefs = new Stack<KeyDef>();
        try {
            /* filename = tempDir + File.separator + filename; */
            final InputSource source = URIResolverAdapter.convertToInputSource(DitaURIResolverFactory.getURIResolver().resolve(filename, null));
            reader.parse(source);
        } catch (final Exception ex) {
            logger.logError(ex.getMessage(), ex) ;
        } finally {
            keys = null;
        }
    }
    
    /**
     * Set keys set for later comparison.
     * 
     * @param set keys set
     */
    public void setKeys(final Set<String> set){
        keys = set;
    }

    @Override
    public void startElement(final String uri, final String localName, final String name,
            final Attributes atts) throws SAXException {
        final String classValue = atts.getValue(ATTRIBUTE_NAME_CLASS);
        final String keyName = atts.getValue(ATTRIBUTE_NAME_KEYS);
        if(keyName!=null && MAP_TOPICREF.matches(classValue)){

            // if it has @keys and is valid.
            boolean hasKnownKey = false;
            for (final String k: keyName.split(" ")) {
                if(keys.contains(k)){
                    hasKnownKey = true;
                    break;
                }
            }
            if(hasKnownKey){
                keyDefs.push(new KeyDef(keyName));
                putElement(name, atts);
            }
        }else if(!keyDefs.isEmpty()){
            keyDefs.peek().keyDefLevel++;
            putElement(name, atts);
        }
    }

    private void putElement(final String elemName,
            final Attributes atts) {
        keyDefAppend(LESS_THAN);
        keyDefAppend(elemName);
        for (int index=0; index < atts.getLength(); index++){
            keyDefAppend(STRING_BLANK);
            keyDefAppend(atts.getQName(index));
            keyDefAppend(EQUAL);
            keyDefAppend(QUOTATION);
            String value = atts.getValue(index);
            value = StringUtils.escapeXML(value);
            keyDefAppend(value);
            keyDefAppend(QUOTATION);
        }
        keyDefAppend(GREATER_THAN);
    }
    
    /**
     * Set temporary directory.
     * 
     * @param tempDir temporary directory path
     */
    public void setTempDir(final String tempDir) {
    }
    
    /**
     * Append content to every key definition in the stack.
     * 
     * @param content XML content to add to key definitions
     */
    private void keyDefAppend(final String content) {
        for (final KeyDef keyDef : keyDefs) {
            keyDef.keyDefContent.append(content);
        }
    }
    
    /**
     * Read key definition
     * 
     * @param key key definition XML string
     * @return parsed key definition document
     */
    private Document keyDefToDoc(final String key) {
        final InputSource inputSource = new InputSource(new StringReader(key));
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document document = null;
        try {
            final DocumentBuilder documentBuilder = factory.newDocumentBuilder();
            document = documentBuilder.parse(inputSource);
        } catch (final Exception e) {
            logger.logError("Failed to parse key definition: " + e.getMessage(), e);
        }
        return document;
    }
    
}
