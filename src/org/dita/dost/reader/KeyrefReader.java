/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.reader;

import static org.dita.dost.util.Constants.*;

import java.util.Hashtable;
import java.util.Set;
import java.util.Stack;

import org.dita.dost.module.Content;
import org.dita.dost.module.ContentImpl;
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

    private final Hashtable<String, String> keyDefTable;

    private Stack<KeyDef> keyDefs;

    private Set<String> keys;

    /**
     * Constructor.
     */
    public KeyrefReader(){
        keyDefTable = new Hashtable<String, String>();
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
                        keyDefTable.put(keyName, keyDef.keyDefContent.toString());
                    }

                }
            }
        }
    }

    /**
     * @return content collection {@code Hashtable<String, String>}
     */
    @Override
    public Content getContent() {
        final Content content = new ContentImpl();
        content.setValue(keyDefTable);
        return content;
    }

    @Override
    public void read(final String filename) {
        keyDefs = new Stack<KeyDef>();
        try {
            //AlanChanged: by refactoring Adding URIResolver Date:2009-08-13 --begin
            /* filename = tempDir + File.separator + filename; */
            final InputSource source = URIResolverAdapter.convertToInputSource(DitaURIResolverFactory.getURIResolver().resolve(filename, null));
            reader.parse(source);
            //edit by Alan: by refactoring Adding URIResolver Date:2009-08-13 --end
        } catch (final Exception ex) {
            logger.logException(ex);
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
        this.keys = set;
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
            //Added by William on 2009-10-15 for ampersand bug:2878492 start
            value = StringUtils.escapeXML(value);
            //Added by William on 2009-10-15 for ampersand bug:2878492 end
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
        
}
