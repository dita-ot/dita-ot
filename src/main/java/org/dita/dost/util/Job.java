/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */
package org.dita.dost.util;

import static org.dita.dost.util.Constants.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Definition of current job.
 * 
 * <p>Instances are thread-safe.</p>
 * 
 * @since 1.5.4
 */
public final class Job {

    private static final String JOB_FILE = ".job.xml";
    
    private static final String ELEMENT_JOB = "job";
    private static final String ATTRIBUTE_KEY = "key";
    private static final String ELEMENT_ENTRY = "entry";
    private static final String ELEMENT_MAP = "map";
    private static final String ELEMENT_SET = "set";
    private static final String ELEMENT_STRING = "string";
    private static final String ATTRIBUTE_NAME = "name";
    private static final String ELEMENT_PROPERTY = "property";

    private static final String ELEMENT_FILES = "files";
    private static final String ELEMENT_FILE = "file";
    private static final String ATTRIBUTE_PATH = "path";
    private static final String ATTRIBUTE_FORMAT = "format";
    private static final String ATTRIBUTE_CHUNKED = "chunked";
    private static final String ATTRIBUTE_HAS_CONREF = "has-conref";
    private static final String ATTRIBUTE_HAS_KEYREF = "has-keyref";
    private static final String ATTRIBUTE_HAS_CODEREF = "has-coderef";
    private static final String ATTRIBUTE_RESOURCE_ONLY = "resource-only";
    private static final String ATTRIBUTE_TARGET = "target";
    private static final String ATTRIBUTE_CONREF_TARGET = "conref-target";
    private static final String ATTRIBUTE_NON_CONREF_TARGET = "non-conref-target";
    private static final String ATTRIBUTE_CONREF_PUSH = "conrefpush";
    private static final String ATTRIBUTE_SUBJECT_SCHEME = "subjectscheme";
    private static final String ATTRIBUTE_HAS_LINK = "has-link";
    private static final String ATTRIBUTE_COPYTO_SOURCE_LIST = "copy-to-source";
    private static final String ATTRIBUTE_OUT_DITA_FILES_LIST = "out-dita";
    private static final String ATTRIBUTE_CHUNKED_DITAMAP_LIST = "chunked-ditamap";
    private static final String ATTRIBUTE_FLAG_IMAGE_LIST = "flag-image";
    private static final String ATTRIBUTE_SUBSIDIARY_TARGET_LIST = "subtarget";
    private static final String ATTRIBUTE_CHUNK_TOPIC_LIST = "skip-chunk";
    private static final String ATTRIBUTE_ACTIVE = "active";
    
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
    /** File name for key definition file */
    public static final String SUBJECT_SCHEME_KEYDEF_LIST_FILE = "schemekeydef.xml";
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

    /** Map of legacy list files to file info boolean fields. */
    private static final Map<String, Field> listToFieldMap = new HashMap<String, Field>();
    /** Map of serialization attributes to file info boolean fields. */
    private static final Map<String, Field> attrToFieldMap= new HashMap<String, Field>();
    static {
        try {
            listToFieldMap.put(CHUNKED_TOPIC_LIST, FileInfo.class.getField("isChunked"));
            listToFieldMap.put(CONREF_LIST, FileInfo.class.getField("hasConref"));
            listToFieldMap.put(HREF_DITA_TOPIC_LIST, FileInfo.class.getField("hasLink"));
            listToFieldMap.put(KEYREF_LIST, FileInfo.class.getField("hasKeyref"));
            listToFieldMap.put(CODEREF_LIST, FileInfo.class.getField("hasCoderef"));
            listToFieldMap.put(RESOURCE_ONLY_LIST, FileInfo.class.getField("isResourceOnly"));
            listToFieldMap.put(HREF_TARGET_LIST, FileInfo.class.getField("isTarget"));
            listToFieldMap.put(CONREF_TARGET_LIST, FileInfo.class.getField("isConrefTarget"));
            listToFieldMap.put(HREF_TOPIC_LIST, FileInfo.class.getField("isNonConrefTarget"));
            listToFieldMap.put(CONREF_PUSH_LIST, FileInfo.class.getField("isConrefPush"));
            listToFieldMap.put(SUBJEC_SCHEME_LIST, FileInfo.class.getField("isSubjectScheme"));
            listToFieldMap.put(COPYTO_SOURCE_LIST, FileInfo.class.getField("isCopyToSource"));
            listToFieldMap.put(OUT_DITA_FILES_LIST, FileInfo.class.getField("isOutDita"));
            listToFieldMap.put(CHUNKED_DITAMAP_LIST, FileInfo.class.getField("isChunkedDitaMap"));
            listToFieldMap.put(FLAG_IMAGE_LIST, FileInfo.class.getField("isFlagImage"));
            listToFieldMap.put(SUBSIDIARY_TARGET_LIST, FileInfo.class.getField("isSubtarget"));
            listToFieldMap.put(CHUNK_TOPIC_LIST, FileInfo.class.getField("isSkipChunk"));
            attrToFieldMap.put(ATTRIBUTE_CHUNKED, FileInfo.class.getField("isChunked"));
            attrToFieldMap.put(ATTRIBUTE_HAS_LINK, FileInfo.class.getField("hasLink"));    
            attrToFieldMap.put(ATTRIBUTE_HAS_CONREF, FileInfo.class.getField("hasConref"));    
            attrToFieldMap.put(ATTRIBUTE_HAS_KEYREF, FileInfo.class.getField("hasKeyref"));    
            attrToFieldMap.put(ATTRIBUTE_HAS_CODEREF, FileInfo.class.getField("hasCoderef"));    
            attrToFieldMap.put(ATTRIBUTE_RESOURCE_ONLY, FileInfo.class.getField("isResourceOnly"));    
            attrToFieldMap.put(ATTRIBUTE_TARGET, FileInfo.class.getField("isTarget"));    
            attrToFieldMap.put(ATTRIBUTE_CONREF_TARGET, FileInfo.class.getField("isConrefTarget"));    
            attrToFieldMap.put(ATTRIBUTE_NON_CONREF_TARGET, FileInfo.class.getField("isNonConrefTarget"));    
            attrToFieldMap.put(ATTRIBUTE_CONREF_PUSH, FileInfo.class.getField("isConrefPush"));    
            attrToFieldMap.put(ATTRIBUTE_SUBJECT_SCHEME, FileInfo.class.getField("isSubjectScheme"));
            attrToFieldMap.put(ATTRIBUTE_COPYTO_SOURCE_LIST, FileInfo.class.getField("isCopyToSource"));
            attrToFieldMap.put(ATTRIBUTE_OUT_DITA_FILES_LIST, FileInfo.class.getField("isOutDita"));
            attrToFieldMap.put(ATTRIBUTE_CHUNKED_DITAMAP_LIST, FileInfo.class.getField("isChunkedDitaMap"));
            attrToFieldMap.put(ATTRIBUTE_FLAG_IMAGE_LIST, FileInfo.class.getField("isFlagImage"));
            attrToFieldMap.put(ATTRIBUTE_SUBSIDIARY_TARGET_LIST, FileInfo.class.getField("isSubtarget"));
            attrToFieldMap.put(ATTRIBUTE_CHUNK_TOPIC_LIST, FileInfo.class.getField("isSkipChunk"));
            attrToFieldMap.put(ATTRIBUTE_ACTIVE, FileInfo.class.getField("isActive"));
        } catch (final NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
    
    private final Map<String, Object> prop;
    private final File tempDir;
    private final Map<String, FileInfo> files = new HashMap<String, FileInfo>();

    /**
     * Create new job configuration instance. Initialise by reading temporary configuration files.
     *  
     * @param tempDir temporary directory
     * @throws IOException if reading configuration files failed
     * @throws IllegalStateException if configuration files are missing
     */
    public Job(final File tempDir) throws IOException {
        this.tempDir = tempDir;
        prop = new HashMap<String, Object>();
        read();
    }

    /**
     * Read temporary configuration files. If configuration files are not found,
     * assume an empty job object is being created.
     * 
     * @throws IOException if reading configuration files failed
     * @throws SAXException if XML parsing failed
     * @throws IllegalStateException if configuration files are missing
     */
    private void read() throws IOException {
        final File jobFile = new File(tempDir, JOB_FILE);
        if (jobFile.exists()) {
        	InputStream in = null;
            try {
                final XMLReader parser = StringUtils.getXMLReader();
                parser.setContentHandler(new JobHandler(prop, files));
                in = new FileInputStream(jobFile);
                parser.parse(new InputSource(in));
            } catch (final SAXException e) {
                throw new IOException("Failed to read job file: " + e.getMessage());
            } finally {
            	if (in != null) {
            		in.close();
            	}
            } 
            return;
        }

        final Properties p = new Properties();
        final File ditalist = new File(tempDir, FILE_NAME_DITA_LIST);
        final File xmlDitalist=new File(tempDir, FILE_NAME_DITA_LIST_XML);
        InputStream in = null;
        try{
            if(xmlDitalist.exists()) {
                in = new FileInputStream(xmlDitalist);
                p.loadFromXML(in);
            } else if(ditalist.exists()) {
                in = new FileInputStream(ditalist);
                p.load(in);
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
        
        readProperties(p);
    }

	private void readProperties(final Properties p) {
		for (final Map.Entry<Object, Object> e: p.entrySet()) {
            if (((String) e.getValue()).length() > 0) {
            	final String key = e.getKey().toString();
            	if (key.equals(COPYTO_TARGET_TO_SOURCE_MAP_LIST)) {
            		setMap(e.getKey().toString(), StringUtils.restoreMap(e.getValue().toString()));
            	} else if (key.endsWith("list")) {
            		setSet(e.getKey().toString(), StringUtils.restoreSet(e.getValue().toString()));
            	} else {
            		setProperty(e.getKey().toString(), e.getValue().toString());
            	}
            }
        }
    }
    
    private final static class JobHandler extends DefaultHandler {

        private final Map<String, Object> prop;
        private final Map<String, FileInfo> files;
        private StringBuilder buf;
        private String name;
        private String key;
        private Set<String> set;
        private Map<String, String> map;
        
        JobHandler(final Map<String, Object> prop, final Map<String, FileInfo> files) {
            this.prop = prop;
            this.files = files;
        }
        
        @Override
        public void characters(final char[] ch, final int start, final int length) throws SAXException {
            if (buf != null) {
                buf.append(ch, start, length);
            }
        }

        @Override
        public void ignorableWhitespace(final char[] ch, final int start, final int length) throws SAXException {
            if (buf != null) {
                buf.append(ch, start, length);
            }
        }
        
        @Override
        public void startElement(final String uri, final String localName, final String qName, final Attributes atts) throws SAXException {
            final String n = localName != null ? localName : qName;
            if (n.equals(ELEMENT_PROPERTY)) {
                name = atts.getValue(ATTRIBUTE_NAME);
            } else if (n.equals(ELEMENT_STRING)) {
                buf = new StringBuilder();
            } else if (n.equals(ELEMENT_SET)) {
                set = new HashSet<String>();
            } else if (n.equals(ELEMENT_MAP)) {
                map = new HashMap<String, String>();
            } else if (n.equals(ELEMENT_ENTRY)) {
                key = atts.getValue(ATTRIBUTE_KEY);
            } else if (n.equals(ELEMENT_FILE)) {
                final String path = atts.getValue(ATTRIBUTE_PATH);
                final FileInfo i = new FileInfo(path);
                i.format = atts.getValue(ATTRIBUTE_FORMAT);
                try {
                    for (Map.Entry<String, Field> e: attrToFieldMap.entrySet()) {
                        e.getValue().setBoolean(i, Boolean.parseBoolean(atts.getValue(e.getKey())));
                    }
                } catch (final IllegalAccessException ex) {
                    throw new RuntimeException(ex);
                }
                files.put(path, i);
            }
        }
        
        @Override
        public void endElement(final String uri, final String localName, final String qName) throws SAXException {
            final String n = localName != null ? localName : qName;
            if (n.equals(ELEMENT_PROPERTY)) {
                name = null;
            } else if (n.equals(ELEMENT_STRING)) {
                if (set != null) {
                    set.add(buf.toString());
                } else if (map != null) {
                    map.put(key, buf.toString());
                } else {
                    prop.put(name, buf.toString());
                }
                buf = null;
            } else if (n.equals(ELEMENT_SET)) {
                prop.put(name, set);
                set = null;
            } else if (n.equals(ELEMENT_MAP)) {
                prop.put(name, map);
                map = null;
            } else if (n.equals(ELEMENT_ENTRY)) {
                key = null;
            }
        }
        
    }
    
    /**
     * Store job into temporary configuration files.
     * 
     * @throws IOException if writing configuration files failed
     */
    public void write() throws IOException {
    	OutputStream outStream = null;
        XMLStreamWriter out = null;
        try {
        	outStream = new FileOutputStream(new File(tempDir, JOB_FILE));
            out = XMLOutputFactory.newInstance().createXMLStreamWriter(outStream, "UTF-8");
            out.writeStartDocument();
            out.writeStartElement(ELEMENT_JOB);
            for (final Map.Entry<String, Object> e: prop.entrySet()) {
                out.writeStartElement(ELEMENT_PROPERTY);
                out.writeAttribute(ATTRIBUTE_NAME, e.getKey());
                if (e.getValue() instanceof String) {
                    out.writeStartElement(ELEMENT_STRING);
                    out.writeCharacters(e.getValue().toString());
                    out.writeEndElement(); //string
                } else if (e.getValue() instanceof Set) {
                    out.writeStartElement(ELEMENT_SET);
                    final Set<?> s = (Set<?>) e.getValue();
                    for (final Object o: s) {
                        out.writeStartElement(ELEMENT_STRING);
                        out.writeCharacters(o.toString());
                        out.writeEndElement(); //string
                    }
                    out.writeEndElement(); //set
                } else if (e.getValue() instanceof Map) {
                    out.writeStartElement(ELEMENT_MAP);
                    final Map<?, ?> s = (Map<?, ?>) e.getValue();
                    for (final Map.Entry<?, ?> o: s.entrySet()) {
                        out.writeStartElement(ELEMENT_ENTRY);
                        out.writeAttribute(ATTRIBUTE_KEY, o.getKey().toString());
                        out.writeStartElement(ELEMENT_STRING);
                        out.writeCharacters(o.getValue().toString());
                        out.writeEndElement(); //string
                        out.writeEndElement(); //entry
                    }
                    out.writeEndElement(); //string
                } else {
                    out.writeStartElement(e.getValue().getClass().getName());
                    out.writeCharacters(e.getValue().toString());
                    out.writeEndElement(); //string
                }
                out.writeEndElement(); //property
            }
            out.writeStartElement(ELEMENT_FILES);
            for (final FileInfo i: files.values()) {
                out.writeStartElement(ELEMENT_FILE);
                out.writeAttribute(ATTRIBUTE_PATH, i.file);
                if (i.format != null) {
                	out.writeAttribute(ATTRIBUTE_FORMAT, i.format);
                }
                try {
                    for (Map.Entry<String, Field> e: attrToFieldMap.entrySet()) {
                        out.writeAttribute(e.getKey(), Boolean.toString(e.getValue().getBoolean(i)));
                    }
                } catch (final IllegalAccessException ex) {
                    throw new RuntimeException(ex);
                }
                out.writeEndElement(); //file
            }
            out.writeEndElement(); //files
            out.writeEndElement(); //job
            out.writeEndDocument();
        } catch (final IOException e) {
            throw new IOException("Failed to write file: " + e.getMessage());
        } catch (final XMLStreamException e) {
            throw new IOException("Failed to serialize job file: " + e.getMessage());
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (final XMLStreamException e) {
                    throw new IOException("Failed to close file: " + e.getMessage());
                }
            }
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (final IOException e) {
                    throw new IOException("Failed to close file: " + e.getMessage());
                }
            }
        }

        final Properties p = new Properties();
        for (final Map.Entry<String, Object> e: prop.entrySet()) {
            if (e.getValue() instanceof Set) {
                p.put(e.getKey(), StringUtils.assembleString((Collection) e.getValue(), COMMA));
            } else if (e.getValue() instanceof Map) {
                p.put(e.getKey(), StringUtils.assembleString((Map) e.getValue(), COMMA));
            } else {
                p.put(e.getKey(), e.getValue());
            }
        }
        
        FileOutputStream propertiesOutputStream = null;
        try {
            propertiesOutputStream = new FileOutputStream(new File(tempDir, FILE_NAME_DITA_LIST));
            p.store(propertiesOutputStream, null);
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
            p.storeToXML(xmlOutputStream, null);
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
        final Object value = prop.get(key);
        if (value == null) {
            return null;
        } else if (value instanceof Set) { // migration support
            return StringUtils.assembleString((Collection) value, COMMA);
        } else if (value instanceof Map) { // migration support
            return StringUtils.assembleString((Map) value, COMMA);
        } else {
            return (String) value;
        }
    }
    
    /**
     * Searches for the property with the specified key in this property list.
     * 
     * @param key property key
     * @return the value in this property list with the specified key value, empty map if not found
     */
    public Map<String, String> getMap(final String key) {
        final Object value = prop.get(key);
        if (value == null) {
            return Collections.emptyMap();
        } else if (value instanceof String) { // migration support
            return StringUtils.restoreMap((String) value);
        } else {
            return (Map<String, String>) value;
        }
    }
    
    /**
     * Searches for the property with the specified key in this property list.
     * 
     * @param key property key
     * @return the value in this property list with the specified key value, empty set if not found
     */
    public Set<String> getSet(final String key) {
        if (key.equals(FULL_DITAMAP_TOPIC_LIST)) {
            final Set<String> ret = new HashSet<String>();
            ret.addAll(getSet(FULL_DITA_TOPIC_LIST)); 
            ret.addAll(getSet(FULL_DITAMAP_LIST));
            return ret;
        } else if (key.equals(FULL_DITA_TOPIC_LIST)) {
            final Set<String> ret = new HashSet<String>();
            for (final FileInfo f: files.values()) {
                if (f.isActive && "dita".equals(f.format)) {
                    ret.add(f.file);
                }
            }
            return ret;
        } else if (key.equals(FULL_DITAMAP_LIST)) {
            final Set<String> ret = new HashSet<String>();
            for (final FileInfo f: files.values()) {
                if (f.isActive && "ditamap".equals(f.format)) {
                    ret.add(f.file);
                }
            }
            return ret;
        } else if (key.equals(HTML_LIST)) {
            return getFilesByFormat("html");
        } else if (key.equals(IMAGE_LIST)) {
            return getFilesByFormat("image");
        } else if (listToFieldMap.containsKey(key)) {
            final Set<String> ret = new HashSet<String>();
            try {
                final Field field = listToFieldMap.get(key);
                for (final FileInfo f: files.values()) {
                    if (field.getBoolean(f)) {
                        ret.add(f.file);
                    }
                }
            } catch (final IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            return ret;            
        } else {
            final Object value = prop.get(key);
            if (value == null) {
                return Collections.emptySet();
            } else if (value instanceof String) { // migration support
                return StringUtils.restoreSet((String) value);
            } else {
                return (Set<String>) value;
            }
        }
    }
    
    private Set<String> getFilesByFormat(final String...formats) {
        final Set<String> ret = new HashSet<String>();
        for (final FileInfo f: files.values()) {
            format: for (final String format: formats) {
                if (format.equals(f.format)) {
                    ret.add(f.file);
                    break format;
                }
            }
        }
        return ret;
    }
    
    /**
     * Set property value.
     * 
     * @param key property key
     * @param value property value
     * @return the previous value of the specified key in this property list, or {@code null} if it did not have one
     */
    public Object setProperty(final String key, final String value) {
        return prop.put(key, value);
    }
    
    /**
     * Set property value.
     * 
     * @param key property key
     * @param value property value
     * @return the previous value of the specified key in this property list, or {@code null} if it did not have one
     */
    public Set<String> setSet(final String key, final Set<String> value) {
        Object previous = null;
        if (key.equals(FULL_DITAMAP_TOPIC_LIST)) {
            //throw new RuntimeException(FULL_DITAMAP_TOPIC_LIST + " is a compound set, cannot be directly generated");
        } else if (key.equals(FULL_DITA_TOPIC_LIST)) {
            for (FileInfo f: files.values()) {
                if ("dita".equals(f.format)) {
                    f.isActive = false;
                }
            }
            for (final String f: value) {
            	final FileInfo ff = getOrAdd(f);
            	ff.format = "dita";
            	ff.isActive = true;
            }
        } else if (key.equals(FULL_DITAMAP_LIST)) {
            for (FileInfo f: files.values()) {
                if ("ditamap".equals(f.format)) {
                    f.isActive = false;
                }
            }
            for (final String f: value) {
                final FileInfo ff = getOrAdd(f);
                ff.format = "ditamap";
                ff.isActive = true;
            }
        } else if (key.equals(HTML_LIST)) {
            for (final String f: value) {
            	getOrAdd(f).format = "html";
            }
        } else if (key.equals(IMAGE_LIST)) {
            for (final String f: value) {
            	getOrAdd(f).format = "image";
            }
        } else if (listToFieldMap.containsKey(key)) {
            try {
                final Field field = listToFieldMap.get(key);
                for (final String f: value) {
                    field.setBoolean(getOrAdd(f), true);
                }
            } catch (final IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        } else {
            previous = prop.put(key, value);
            if (previous == null) {
                return null;
            } else if (previous instanceof String) { // migration support
                return StringUtils.restoreSet((String) previous);
            } else {
                return (Set<String>) previous;
            }
        }
        return (Set<String>) previous;
    }

	private FileInfo getOrAdd(final String f) {
		FileInfo i = files.get(f); 
		if (i == null) {
			i = new FileInfo(f);
		    files.put(f, i);
		}
		return i;
	}
    
    /**
     * Set property value.
     * 
     * @param key property key
     * @param value property value
     * @return the previous value of the specified key in this property list, or {@code null} if it did not have one
     */
    public Map<String, String> setMap(final String key, final Map<String, String> value) {        
        final Object previous = prop.put(key, value);
        if (previous == null) {
            return null;
        } else if (previous instanceof String) { // migration support
            return StringUtils.restoreMap((String) previous);
        } else {
            return (Map<String, String>) previous;
        }
    }
    
    /**
     * Return the copy-to map.
     * @return copy-to map
     */
    public Map<String, String> getCopytoMap() {
        return getMap(COPYTO_TARGET_TO_SOURCE_MAP_LIST);
    }

    /**
     * @return the schemeSet
     */
    public Set<String> getSchemeSet() {
        return getSet(SUBJEC_SCHEME_LIST);
        
    }

    /**
     * Get input file
     * 
     * @return input file path relative to input directory
     */
    public String getInputMap() {
        return getProperty(INPUT_DITAMAP);
    }

    /**
     * Get input directory.
     * 
     * @return absolute input directory path 
     */
    public String getInputDir() {
        return getProperty(INPUT_DIR);
    }

    /**
     * Get all file info objects
     * 
     * @return map of file info objects
     */
    public Map<String, FileInfo> getFileInfo() {
        return Collections.unmodifiableMap(files);
    }
    
    /**
     * Add a collection of file info objects
     * 
     * @param fs file info objects
     */
    public void addAll(final Collection<FileInfo> fs) {
    	for (final FileInfo f: fs) {
    		files.put(f.file, f);
    	}
    }
    
    // Utility methods
    
    /**
     * Write list file.
     * 
     * @param prop property name
     * @throws IOException if writing fails
     */
    @Deprecated
    public void writeList(final String prop) throws IOException {
        final String filename = prop.equals(INPUT_DITAMAP)
                                ? INPUT_DITAMAP_LIST_FILE
                                : prop.substring(0, prop.lastIndexOf("list")) + ".list";
        writeList(prop, filename);
    }
    
    /**
     * Write list file.
     * 
     * @param prop property name
     * @param filename list file name
     * @throws IOException if writing fails
     */
    @Deprecated
    private void writeList(final String prop, final String filename) throws IOException {
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
    
    /**
     * File info object.
     */
    public static final class FileInfo {
        
        /** File path. */
        public final String file;
        /** File format. */
    	public String format;
    	/** File has a conref. */
        public boolean hasConref;
        /** File is part of chunk. */
        public boolean isChunked;
        /** File has links. */
        public boolean hasLink;
        /** File is resource only. */
        public boolean isResourceOnly;
        /** File is a link target. */
        public boolean isTarget;
        /** File is a push conref target. */
        public boolean isConrefTarget;
        /** File is a target in non-conref link. Opposite of {@link #isSkipTarget}. */
        public boolean isNonConrefTarget;
        /** File is a push conref source. */
        public boolean isConrefPush;
        /** File has a keyref. */
        public boolean hasKeyref;
        /** File has coderef. */
        public boolean hasCoderef;
        /** File is a subject scheme. */
        public boolean isSubjectScheme;
        /** File is a target in conref link. Opposite of {@link #isNonConrefTarget}. */
        public boolean isSkipChunk;
        /** File is a coderef target. */
        public boolean isSubtarget;
        /** File is a flagging image. */
        public boolean isFlagImage;
        /** File is a chunked map. */
        public boolean isChunkedDitaMap;
        /** Source file is outside base directory. */
        public boolean isOutDita;
        /** File is used only as a source of a copy-to. */
        public boolean isCopyToSource;
        public boolean isActive;
        
        FileInfo(final String file) {
            this.file = file;
        }
        
        public static class Builder {
            
            private String file;
            private String format;
            private boolean hasConref;
            private boolean isChunked;
            private boolean hasLink;
            private boolean isResourceOnly;
            private boolean isTarget;
            private boolean isConrefTarget;
            private boolean isNonConrefTarget;
            private boolean isConrefPush;
            private boolean hasKeyref;
            private boolean hasCoderef;
            private boolean isSubjectScheme;
            private boolean isSkipChunk;
            private boolean isSubtarget;
            private boolean isFlagImage;
            private boolean isChunkedDitaMap;
            private boolean isOutDita;
            private boolean isCopyToSource;
            private boolean isActive;
        
            public Builder() {}
            public Builder(final FileInfo orig) {
                file = orig.file;
                format = orig.format;
                hasConref = orig.hasConref;
                isChunked = orig.isChunked;
                hasLink = orig.hasLink;
                isResourceOnly = orig.isResourceOnly;
                isTarget = orig.isTarget;
                isConrefTarget = orig.isConrefTarget;
                isNonConrefTarget = orig.isNonConrefTarget;
                isConrefPush = orig.isConrefPush;
                hasKeyref = orig.hasKeyref;
                hasCoderef = orig.hasCoderef;
                isSubjectScheme = orig.isSubjectScheme;
                isSkipChunk = orig.isSkipChunk;
                isSubtarget = orig.isSubtarget;
                isFlagImage = orig.isFlagImage;
                isChunkedDitaMap = orig.isChunkedDitaMap;
                isOutDita = orig.isOutDita;
                isCopyToSource = orig.isCopyToSource;
                isActive = orig.isActive;
            }
            
            public Builder file(final String file) { this.file = file; return this; }
            public Builder format(final String format) { this.format = format; return this; }
            public Builder hasConref(final boolean hasConref) { this.hasConref = hasConref; return this; }
            public Builder isChunked(final boolean isChunked) { this.isChunked = isChunked; return this; }
            public Builder hasLink(final boolean hasLink) { this.hasLink = hasLink; return this; }
            public Builder isResourceOnly(final boolean isResourceOnly) { this.isResourceOnly = isResourceOnly; return this; }
            public Builder isTarget(final boolean isTarget) { this.isTarget = isTarget; return this; }
            public Builder isConrefTarget(final boolean isConrefTarget) { this.isConrefTarget = isConrefTarget; return this; }
            public Builder isNonConrefTarget(final boolean isNonConrefTarget) { this.isNonConrefTarget = isNonConrefTarget; return this; }
            public Builder isConrefPush(final boolean isConrefPush) { this.isConrefPush = isConrefPush; return this; }
            public Builder hasKeyref(final boolean hasKeyref) { this.hasKeyref = hasKeyref; return this; }
            public Builder hasCoderef(final boolean hasCoderef) { this.hasCoderef = hasCoderef; return this; }
            public Builder isSubjectScheme(final boolean isSubjectScheme) { this.isSubjectScheme = isSubjectScheme; return this; }
            public Builder isSkipChunk(final boolean isSkipChunk) { this.isSkipChunk = isSkipChunk; return this; }
            public Builder isSubtarget(final boolean isSubtarget) { this.isSubtarget = isSubtarget; return this; }
            public Builder isFlagImage(final boolean isFlagImage) { this.isFlagImage = isFlagImage; return this; }
            public Builder isChunkedDitaMap(final boolean isChunkedDitaMap) { this.isChunkedDitaMap = isChunkedDitaMap; return this; }
            public Builder isOutDita(final boolean isOutDita) { this.isOutDita = isOutDita; return this; }
            public Builder isCopyToSource(final boolean isCopyToSource) { this.isCopyToSource = isCopyToSource; return this; }
            public Builder isActive(final boolean isActive) { this.isActive = isActive; return this; }
            
            public FileInfo build() {
                if (file == null) {
                    throw new IllegalArgumentException("file may not be null");
                }
                final FileInfo fi = new FileInfo(file);
                fi.format = format;
                fi.hasConref = hasConref;
                fi.isChunked = isChunked;
                fi.hasLink = hasLink;
                fi.isResourceOnly = isResourceOnly;
                fi.isTarget = isTarget;
                fi.isConrefTarget = isConrefTarget;
                fi.isNonConrefTarget = isNonConrefTarget;
                fi.isConrefPush = isConrefPush;
                fi.hasKeyref = hasKeyref;
                fi.hasCoderef = hasCoderef;
                fi.isSubjectScheme = isSubjectScheme;
                fi.isSkipChunk = isSkipChunk;
                fi.isSubtarget = isSubtarget;
                fi.isFlagImage = isFlagImage;
                fi.isChunkedDitaMap = isChunkedDitaMap;
                fi.isOutDita = isOutDita;
                fi.isCopyToSource = isCopyToSource;
                fi.isActive = isActive;
                return fi;
            }
            
        }
        
    }
    
}
