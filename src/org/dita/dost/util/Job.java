/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for
 * applicable licenses.
 */

/**
 * (c) Copyright IBM Corp. 2011 All Rights Reserved.
 */
package org.dita.dost.util;

import static org.dita.dost.util.Constants.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Definition of current job.
 * 
 * <p>Instances are thread-safe.</p>
 * 
 * @since 1.5.4
 */
public final class Job {

    private final Properties prop;
    private final File tempDir;

    /**
     * Create new job configuration instance. Initialise by reading temporary configuration files.
     *  
     * @param tempDir temporary directory
     * @throws IOException if reading configuration files failed
     * @throws IllegalStateException if configuration files are missing
     */
    public Job(final File tempDir) throws IOException {
        this.tempDir = tempDir;
        prop = new Properties();
        read();
    }

    /**
     * Read temporary configuration files.
     * 
     * @throws IOException if reading configuration files failed
     * @throws IllegalStateException if configuration files are missing
     */
    private void read() throws IOException {
        final File ditalist = new File(tempDir, FILE_NAME_DITA_LIST);
        final File xmlDitalist=new File(tempDir, FILE_NAME_DITA_LIST_XML);
        InputStream in = null;
        try{
            if(xmlDitalist.exists()) {
                in = new FileInputStream(xmlDitalist);
                prop.loadFromXML(in);
            } else if(ditalist.exists()) {
                in = new FileInputStream(ditalist);
                prop.load(in);
            } else {
                throw new IllegalStateException("Job configuration files not found");
            }
        } catch(final IOException e) {
            throw new IOException("Failed to read file: " + e.getMessage());
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (final IOException e) {
                    throw new IOException("Failed to close file: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Store job into temporary configuration files.
     * 
     * @throws IOException if writing configuration files failed
     */
    public void write() throws IOException {
        FileOutputStream propertiesOutputStream = null;
        try {
            propertiesOutputStream = new FileOutputStream(new File(tempDir, FILE_NAME_DITA_LIST));
            prop.store(propertiesOutputStream, null);
            propertiesOutputStream.flush();
        } catch (final IOException e) {
            throw new IOException("Failed to write file: " + e.getMessage());
        } finally {
            if (propertiesOutputStream != null) {
                try {
                    propertiesOutputStream.close();
                } catch (final IOException e) {
                    throw new IOException("Failed to close file: " + e.getMessage());
                }
            }
        }
        FileOutputStream xmlOutputStream = null;
        try {
            xmlOutputStream = new FileOutputStream(new File(tempDir, FILE_NAME_DITA_LIST_XML));
            prop.storeToXML(xmlOutputStream, null);
            xmlOutputStream.flush();
        } catch (final IOException e) {
            throw new IOException("Failed to write file: " + e.getMessage());
        } finally {
            if (xmlOutputStream != null) {
                try {
                    xmlOutputStream.close();
                } catch (final IOException e) {
                    throw new IOException("Failed to close file: " + e.getMessage());
                }
            }
        }
    }
        
    /**
     * Searches for the property with the specified key in this property list.
     * 
     * @param key property key
     * @return the value in this property list with the specified key value, {@code null} if not found
     */
    public String getProperty(final String key) {
        return prop.getProperty(key);
    }
    
    /**
     * Set property value.
     * 
     * @param key property key
     * @param value property value
     * @return the previous value of the specified key in this property list, or {@code null} if it did not have one
     */
    public String setProperty(final String key, final String value) {
        return (String) prop.setProperty(key, value);
    }
    
    /**
     * Return the copy-to map.
     * @return copy-to map
     */
    public Map<String, String> getCopytoMap() {
        return StringUtils.restoreMap(prop.getProperty(COPYTO_TARGET_TO_SOURCE_MAP_LIST, ""));
    }

    /**
     * @return the schemeSet
     */
    public Set<String> getSchemeSet() {
        return StringUtils.restoreSet(prop.getProperty(SUBJEC_SCHEME_LIST, ""));
    }

    /**
     * @return the inputMap
     */
    public String getInputMap() {
        return prop.getProperty(INPUT_DITAMAP);
    }
    
    /**
     * Get reference list.
     * 
     * <p>TODO: rename to getReferenceList</p>
     * 
     * @return reference list
     */
    public LinkedList<String> getCollection() {
        final LinkedList<String> refList = new LinkedList<String>();
        final String liststr = prop.getProperty(FULL_DITAMAP_TOPIC_LIST, "")
                + COMMA
                + prop.getProperty(CONREF_TARGET_LIST, "")
                + COMMA
                + prop.getProperty(COPYTO_SOURCE_LIST, "");
        final StringTokenizer tokenizer = new StringTokenizer(liststr, COMMA);
        while (tokenizer.hasMoreTokens()) {
            refList.addFirst(tokenizer.nextToken());
        }
        return refList;
    }

    /**
     * Get input directory.
     * 
     * <p>TODO: rename to getInputDir</p>
     * 
     * @return input directory
     */
    public String getValue() {
        return prop.getProperty("user.input.dir");
    }
    
}
