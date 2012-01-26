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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;
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

    /** File name for chuncked dita map list file */
    public static final String CHUNKED_DITAMAP_LIST_FILE = "chunkedditamap.list";
    /** File name for chunked topic list file */
    public static final String CHUNKED_TOPIC_LIST_FILE = "chunkedtopic.list";
    /** File name for skip chunk list file */
    public static final String CHUNK_TOPIC_LIST_FILE = "skipchunk.list";
    /** File name for coderef list file */
    public static final String CODEREF_LIST_FILE = "coderef.list";
    /** File name for conref list file */
    public static final String CONREF_LIST_FILE = "conref.list";
    /** File name for conref push list file */
    public static final String CONREF_PUSH_LIST_FILE = "conrefpush.list";
    /** File name for conref targets list file */
    public static final String CONREF_TARGET_LIST_FILE = "conreftargets.list";
    /** File name for copy-to source list file */
    public static final String COPYTO_SOURCE_LIST_FILE = "copytosource.list";
    /** File name for copy-to target2sourcemap list file */
    public static final String COPYTO_TARGET_TO_SOURCE_MAP_LIST_FILE = "copytotarget2sourcemap.list";
    /** File name for flag image list file */
    public static final String FLAG_IMAGE_LIST_FILE = "flagimage.list";
    /** File name for map list file */
    public static final String FULL_DITAMAP_LIST_FILE = "fullditamap.list";
    /** File name for map and topic list file */
    public static final String FULL_DITAMAP_TOPIC_LIST_FILE = "fullditamapandtopic.list";
    /** File name for topic list file */
    public static final String FULL_DITA_TOPIC_LIST_FILE = "fullditatopic.list";
    /** File name for href topic list file */
    public static final String HREF_DITA_TOPIC_LIST_FILE = "hrefditatopic.list";
    /** File name for href targets list file */
    public static final String HREF_TARGET_LIST_FILE = "hreftargets.list";
    /** File name for candidate topics list file */
    public static final String HREF_TOPIC_LIST_FILE = "canditopics.list";
    /** File name for html list file */
    public static final String HTML_LIST_FILE = "html.list";
    /** File name for image list file */
    public static final String IMAGE_LIST_FILE = "image.list";
    /** File name for input file list file */
    public static final String INPUT_DITAMAP_LIST_FILE = "user.input.file.list";
    /** File name for key definition file */
    public static final String KEYDEF_LIST_FILE = "keydef.xml";
    /** File name for keyref list file */
    public static final String KEYREF_LIST_FILE = "keyref.list";
    /** File name for key list file */
    public static final String KEY_LIST_FILE = "key.list";
    /** File name for out dita files list file */
    public static final String OUT_DITA_FILES_LIST_FILE = "outditafiles.list";
    /** File name for relflag image list file */
    public static final String REL_FLAGIMAGE_LIST_FILE = "relflagimage.list";
    /** File name for resource-only list file */
    public static final String RESOURCE_ONLY_LIST_FILE = "resourceonly.list";
    /** File name for subject scheme list file */
    public static final String SUBJEC_SCHEME_LIST_FILE = "subjectscheme.list";
    /** File name for subtargets list file */
    public static final String SUBSIDIARY_TARGET_LIST_FILE = "subtargets.list";
    /** File name for temporary input file list file */
    public static final String USER_INPUT_FILE_LIST_FILE = "usr.input.file.list";
    
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
     * Create new job configuration instance. Initialise from properties file.
     *  
     * @param props properties file
     * @param tempDir temporary directory
     */
    public Job(final Properties props, final File tempDir) {
        this.tempDir = tempDir;
        prop = new Properties();
        for (final Map.Entry<Object, Object> e: props.entrySet()) {
            prop.put(e.getKey(), e.getValue());
        }
    }
    
    /**
     * Read temporary configuration files. If configuration files are not found,
     * assume an empty job object is being created.
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
     * Searches for the property with the specified key in this property list.
     * 
     * @param key property key
     * @return the value in this property list with the specified key value, empty map if not found
     */
    public Map<String, String> getMap(final String key) {
        return StringUtils.restoreMap(prop.getProperty(key, ""));
    }
    
    /**
     * Searches for the property with the specified key in this property list.
     * 
     * @param key property key
     * @return the value in this property list with the specified key value, empty set if not found
     */
    public Set<String> getSet(final String key) {
        return StringUtils.restoreSet(prop.getProperty(key, ""));
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
     * Set property value.
     * 
     * @param key property key
     * @param value property value
     * @return the previous value of the specified key in this property list, or {@code null} if it did not have one
     */
    public  Set<String> setSet(final String key, final Set<String> value) {
        return (Set<String>) StringUtils.restoreSet((String) prop.setProperty(key, StringUtils.assembleString(value, COMMA)));
    }
    
    /**
     * Set property value.
     * 
     * @param key property key
     * @param value property value
     * @return the previous value of the specified key in this property list, or {@code null} if it did not have one
     */
    public Map<String, String> setMap(final String key, final Map<String, String> value) {        
        return (Map<String, String>) StringUtils.restoreMap((String) prop.setProperty(key, StringUtils.assembleString(value, COMMA)));
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
     * Get input file
     * 
     * @return input file path relative to input directory
     */
    public String getInputMap() {
        return prop.getProperty(INPUT_DITAMAP);
    }
    
    /**
     * Get reference list.
     * 
     * <p>TODO: rename to {@code getReferenceList}</p>
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
     * <p>TODO: rename to {@code File getInputDir()̋}</p>
     * 
     * @return absolute input directory path 
     */
    public String getValue() {
        return prop.getProperty(INPUT_DIR);
    }

    // Utility methods
    
    /**
     * Write list file.
     * 
     * @param prop property name
     * @throws IOException if writing fails
     */
    public void writeList(final String prop) throws IOException {
        final String filename = prop.equals(INPUT_DITAMAP)
                                ? INPUT_DITAMAP_LIST_FILE
                                : prop.substring(0, prop.lastIndexOf("list"))+ ".list";
        writeList(prop, filename);
    }
    
    /**
     * Write list file.
     * 
     * @param tempDir temporary directory
     * @param prop property name
     * @throws IOException if writing fails
     */
    public void writeList(final String prop, final String filename) throws IOException {
        final File listFile = new File(tempDir, filename);
        BufferedWriter topicWriter = null;
        try {
            topicWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(listFile)));
            final Set<String> topics = getSet(prop);
            for (final Iterator<String> i = topics.iterator(); i.hasNext();) {
                topicWriter.write(i.next());
                if (i.hasNext()) {
                    topicWriter.write("\n");
                }
            }
            topicWriter.flush();
        } finally {
            if (topicWriter != null) {
                topicWriter.close();
            }
        }
    }
    
}
