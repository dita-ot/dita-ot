/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */
package org.dita.dost.util;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.dita.dost.util.Job.FileInfo.Filter;
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
    private static final String ATTRIBUTE_SRC = "src";
    private static final String ATTRIBUTE_URI = "uri";
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
    
    private static final String PROPERTY_OUTER_CONTROL = ANT_INVOKER_EXT_PARAM_OUTTERCONTROL;
    private static final String PROPERTY_ONLY_TOPIC_IN_MAP = ANT_INVOKER_EXT_PARAM_ONLYTOPICINMAP;
    private static final String PROPERTY_GENERATE_COPY_OUTER = ANT_INVOKER_EXT_PARAM_GENERATECOPYOUTTER;
    private static final String PROPERTY_OUTPUT_DIR = ANT_INVOKER_EXT_PARAM_OUTPUTDIR;
    /** Deprecated since 2.2 */
    @Deprecated
    private static final String PROPERTY_INPUT_MAP = "InputMapDir";
    private static final String PROPERTY_INPUT_MAP_URI = "InputMapDir.uri";

    /** File name for key definition file */
    public static final String KEYDEF_LIST_FILE = "keydef.xml";
    /** File name for key definition file */
    public static final String SUBJECT_SCHEME_KEYDEF_LIST_FILE = "schemekeydef.xml";
    /** File name for temporary input file list file */
    public static final String USER_INPUT_FILE_LIST_FILE = "usr.input.file.list";

    /** Map of serialization attributes to file info boolean fields. */
    private static final Map<String, Field> attrToFieldMap= new HashMap<>();
    static {
        try {
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
            attrToFieldMap.put(ATTRIBUTE_FLAG_IMAGE_LIST, FileInfo.class.getField("isFlagImage"));
            attrToFieldMap.put(ATTRIBUTE_SUBSIDIARY_TARGET_LIST, FileInfo.class.getField("isSubtarget"));
            attrToFieldMap.put(ATTRIBUTE_CHUNK_TOPIC_LIST, FileInfo.class.getField("isSkipChunk"));
        } catch (final NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }
    
    private final Map<String, Object> prop;
    public final File tempDir;
    private final File jobFile;
    private final ConcurrentMap<URI, FileInfo> files = new ConcurrentHashMap<>();
    private long lastModified;
    
    /**
     * Create new job configuration instance. Initialise by reading temporary configuration files.
     *  
     * @param tempDir temporary directory
     * @throws IOException if reading configuration files failed
     * @throws IllegalStateException if configuration files are missing
     */
    public Job(final File tempDir) throws IOException {
        if (!tempDir.isAbsolute()) {
            throw new IllegalArgumentException("Temporary directory " + tempDir + " must be absolute");
        }
        this.tempDir = tempDir;
        jobFile = new File(tempDir, JOB_FILE);
        prop = new HashMap<>();
        read();
    }

    /**
     * Test if serialized configuration file has been updated.
     * @param tempDir job configuration directory
     * @return {@code true} if configuration file has been update after this object has been created or serialized
     */
    public boolean isStale(final File tempDir) {
        return jobFile.lastModified() > lastModified;
    }
    
    /**
     * Read temporary configuration files. If configuration files are not found,
     * assume an empty job object is being created.
     * 
     * @throws IOException if reading configuration files failed
     * @throws IllegalStateException if configuration files are missing
     */
    private void read() throws IOException {
        lastModified = jobFile.lastModified();
        if (jobFile.exists()) {
        	InputStream in = null;
            try {
                final XMLReader parser = XMLUtils.getXMLReader();
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
        } else {
            // defaults
            prop.put(PROPERTY_GENERATE_COPY_OUTER, Generate.NOT_GENERATEOUTTER.toString());
            prop.put(PROPERTY_ONLY_TOPIC_IN_MAP, Boolean.toString(false));
            prop.put(PROPERTY_OUTER_CONTROL, OutterControl.WARN.toString());
        }
    }
    
    private final static class JobHandler extends DefaultHandler {

        private final Map<String, Object> prop;
        private final Map<URI, FileInfo> files;
        private StringBuilder buf;
        private String name;
        private String key;
        private Set<String> set;
        private Map<String, String> map;
        
        JobHandler(final Map<String, Object> prop, final Map<URI, FileInfo> files) {
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
        public void startElement(final String ns, final String localName, final String qName, final Attributes atts) throws SAXException {
            final String n = localName != null ? localName : qName;
            switch (n) {
                case ELEMENT_PROPERTY:
                    name = atts.getValue(ATTRIBUTE_NAME);
                    break;
                case ELEMENT_STRING:
                    buf = new StringBuilder();
                    break;
                case ELEMENT_SET:
                    set = new HashSet<>();
                    break;
                case ELEMENT_MAP:
                    map = new HashMap<>();
                    break;
                case ELEMENT_ENTRY:
                    key = atts.getValue(ATTRIBUTE_KEY);
                    break;
                case ELEMENT_FILE:
                    final URI src = toURI(atts.getValue(ATTRIBUTE_SRC));
                    final URI uri = toURI(atts.getValue(ATTRIBUTE_URI));
                    final File path = toFile(atts.getValue(ATTRIBUTE_PATH));
                    FileInfo i;
                    if (uri != null) {
                        i = new FileInfo(src, uri, toFile(uri));
                    } else {
                        i = new FileInfo(src, toURI(path), path);
                    }
                    i.format = atts.getValue(ATTRIBUTE_FORMAT);
                    try {
                        for (Map.Entry<String, Field> e : attrToFieldMap.entrySet()) {
                            e.getValue().setBoolean(i, Boolean.parseBoolean(atts.getValue(e.getKey())));
                        }
                    } catch (final IllegalAccessException ex) {
                        throw new RuntimeException(ex);
                    }
                    files.put(i.uri, i);
                    break;
            }
        }
        
        @Override
        public void endElement(final String uri, final String localName, final String qName) throws SAXException {
            final String n = localName != null ? localName : qName;
            switch (n) {
                case ELEMENT_PROPERTY:
                    name = null;
                    break;
                case ELEMENT_STRING:
                    if (set != null) {
                        set.add(buf.toString());
                    } else if (map != null) {
                        map.put(key, buf.toString());
                    } else {
                        prop.put(name, buf.toString());
                    }
                    buf = null;
                    break;
                case ELEMENT_SET:
                    prop.put(name, set);
                    set = null;
                    break;
                case ELEMENT_MAP:
                    prop.put(name, map);
                    map = null;
                    break;
                case ELEMENT_ENTRY:
                    key = null;
                    break;
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
        	outStream = new FileOutputStream(jobFile);
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
                if (i.src != null) {
                    out.writeAttribute(ATTRIBUTE_SRC, i.src.toString());
                }
                out.writeAttribute(ATTRIBUTE_URI, i.uri.toString());
                out.writeAttribute(ATTRIBUTE_PATH, i.file.getPath());
                if (i.format != null) {
                	out.writeAttribute(ATTRIBUTE_FORMAT, i.format);
                }
                try {
                    for (Map.Entry<String, Field> e: attrToFieldMap.entrySet()) {
                        final boolean v = e.getValue().getBoolean(i);
                        if (v) {
                            out.writeAttribute(e.getKey(), Boolean.TRUE.toString());
                        }
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
        lastModified = jobFile.lastModified();
    }
    
    /**
     * Add file info. If file info with the same file already exists, it will be replaced.
     */
    public void add(final FileInfo fileInfo) {
        files.put(fileInfo.uri, fileInfo);
    }
    
    /**
     * Remove file info.
     * 
     * @return removed file info, {@code null} if not found
     */
    public FileInfo remove(final FileInfo fileInfo) {
        return files.remove(fileInfo.uri);
    }
    
    /**
     * Searches for the property with the specified key in this property list.
     * 
     * @param key property key
     * @return the value in this property list with the specified key value, {@code null} if not found
     */
    public String getProperty(final String key) {
        return (String) prop.get(key);
    }
    
    /**
     * Get a map of string properties.
     * 
     * @return map of properties, may be an empty map
     */
    public Map<String, String> getProperties() {
        final Map<String, String> res = new HashMap<>();
        for (final Map.Entry<String, Object> e: prop.entrySet()) {
            if (e.getValue() instanceof String) {
                res.put(e.getKey(), (String) e.getValue());
            }
        }
        return Collections.unmodifiableMap(res);
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
     * Return the copy-to map from target to source.
     *
     * @return copy-to map, empty map if no mapping is defined
     */
    public Map<URI, URI> getCopytoMap() {
        final Map<String, String> value = (Map<String, String>) prop.get(COPYTO_TARGET_TO_SOURCE_MAP_LIST);
        if (value == null) {
            return Collections.emptyMap();
        } else {
            final Map<URI, URI> res = new HashMap<>();
            for (final Map.Entry<String, String> e: value.entrySet()) {
                res.put(toURI(e.getKey()), toURI(e.getValue()));
            }
            return Collections.unmodifiableMap(res);
        }
    }
    
    /**
     * Set copy-to map from target to source.
     */
    public void setCopytoMap(final Map<URI, URI> value) {
        final Map<String, String> res = new HashMap<>();
        for (final Map.Entry<URI, URI> e: value.entrySet()) {
            res.put(e.getKey().toString(), e.getValue().toString());
        }
        prop.put(COPYTO_TARGET_TO_SOURCE_MAP_LIST, res);
    }

    /**
     * Get input file
     *
     * @return input file path relative to input directory
     */
    public URI getInputMap() {
       return toURI(getProperty(INPUT_DITAMAP_URI));
    }

    /**
     * Get input directory.
     * 
     * @return absolute input directory path 
     */
    public URI getInputDir() {
        return toURI(getProperty(INPUT_DIR_URI));
    }

    /**
     * Get all file info objects as a map
     * 
     * @return map of file info objects, where the key is the {@link FileInfo#file} value. May be empty
     */
    public Map<File, FileInfo> getFileInfoMap() {
        final Map<File, FileInfo> ret = new HashMap<>();
        for (final Map.Entry<URI, FileInfo> e: files.entrySet()) {
            ret.put(e.getValue().file, e.getValue());
        }
        return Collections.unmodifiableMap(ret);
    }
    
    /**
     * Get all file info objects
     * 
     * @return collection of file info objects, may be empty
     */
    public Collection<FileInfo> getFileInfo() {
        return Collections.unmodifiableCollection(new ArrayList<>(files.values()));
    }
    
    /**
     * Get file info objects that pass the filter
     * 
     * @param filter filter file info object must pass
     * @return collection of file info objects that pass the filter, may be empty
     */
    public Collection<FileInfo> getFileInfo(final Filter filter) {
        final Collection<FileInfo> ret = new ArrayList<>();
        for (final FileInfo f: files.values()) {
            if (filter.accept(f)) {
                ret.add(f);
            }
        }
        return ret;
    }

    /**
     * Get file info object
     *
     * @param file file URI
     * @return file info object, {@code null} if not found
     */
    public FileInfo getFileInfo(final URI file) {
        if (file == null) {
            return null;
        } else if (files.containsKey(file)) {
            return files.get(file);
        } else if (file.isAbsolute()) {
            final URI relative = getRelativePath(jobFile.toURI(), file);
            return files.get(relative);
        } else {
            return null;
        }
    }
    
    /**
     * Get or create FileInfo for given path.
     * @param file relative URI to temporary directory
     * @return created or existing file info object
     */
    public FileInfo getOrCreateFileInfo(final URI file) {
        assert file.getFragment() == null;
        final URI f = file.normalize();
        FileInfo i = files.get(f); 
        if (i == null) {
            i = new FileInfo(f);
            files.put(i.uri, i);
        }
        return i;
    }
    
    /**
     * Add a collection of file info objects
     * 
     * @param fs file info objects
     */
    public void addAll(final Collection<FileInfo> fs) {
    	for (final FileInfo f: fs) {
    		files.put(f.uri, f);
    	}
    }
        
    /**
     * File info object.
     */
    public static final class FileInfo {
        
        /** Absolute source URI. */
        public final URI src;
        /** File URI. */
        public final URI uri;
        /** File path. */
        public final File file;
        /** File format. */
    	public String format;
    	/** File has a conref. */
        public boolean hasConref;
        /** File is part of chunk. */
        public boolean isChunked;
        /** File has links. Only applies to topics. */
        public boolean hasLink;
        /** File is resource only. */
        public boolean isResourceOnly;
        /** File is a link target. */
        public boolean isTarget;
        /** File is a push conref target. */
        public boolean isConrefTarget;
        /** File is a target in non-conref link. */
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
        /** Source file is outside base directory. */
        public boolean isOutDita;
        /** File is used only as a source of a copy-to. */
        public boolean isCopyToSource;
        
        FileInfo(final URI src, final URI uri, final File file) {
            if (src == null && uri == null && file == null) throw new IllegalArgumentException(new NullPointerException());
            this.src = src;
            this.uri = uri != null ? uri : toURI(file);
            this.file = uri != null ? toFile(uri) : file;
        }
        FileInfo(final URI uri) {
            if (uri == null) throw new IllegalArgumentException(new NullPointerException());
            this.src = null;
            this.uri = uri;
            this.file = toFile(uri);
        }
        FileInfo(final File file) {
            if (file == null) throw new IllegalArgumentException(new NullPointerException());
            this.src = null;
            this.uri =  toURI(file);
            this.file = file;
        }
        
        @Override
        public String toString() {
            return "FileInfo{" +
                    "uri=" + uri +
                    ", file=" + file +
                    ", format='" + format + '\'' +
                    ", hasConref=" + hasConref +
                    ", isChunked=" + isChunked +
                    ", hasLink=" + hasLink +
                    ", isResourceOnly=" + isResourceOnly +
                    ", isTarget=" + isTarget +
                    ", isConrefTarget=" + isConrefTarget +
                    ", isNonConrefTarget=" + isNonConrefTarget +
                    ", isConrefPush=" + isConrefPush +
                    ", hasKeyref=" + hasKeyref +
                    ", hasCoderef=" + hasCoderef +
                    ", isSubjectScheme=" + isSubjectScheme +
                    ", isSkipChunk=" + isSkipChunk +
                    ", isSubtarget=" + isSubtarget +
                    ", isFlagImage=" + isFlagImage +
                    ", isOutDita=" + isOutDita +
                    ", isCopyToSource=" + isCopyToSource +
                    '}';
        }

        public interface Filter {
            
            boolean accept(FileInfo f);
            
        }
        
        public static class Builder {
            
            private URI src;
            private URI uri;
            private File file;
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
            private boolean isOutDita;
            private boolean isCopyToSource;
        
            public Builder() {}
            public Builder(final FileInfo orig) {
                src = orig.src;
                uri = orig.uri;
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
                isOutDita = orig.isOutDita;
                isCopyToSource = orig.isCopyToSource;
            }
            
            /**
             * Add file info to this builder. Only non-null and true values will be added. 
             */
            public Builder add(final FileInfo orig) {
                if (orig.src != null) src = orig.src;
                if (orig.uri != null) uri = orig.uri;
                if (orig.file != null) file = orig.file;
                if (orig.format != null) format = orig.format;
                if (orig.hasConref) hasConref = orig.hasConref;
                if (orig.isChunked) isChunked = orig.isChunked;
                if (orig.hasLink) hasLink = orig.hasLink;
                if (orig.isResourceOnly) isResourceOnly = orig.isResourceOnly;
                if (orig.isTarget) isTarget = orig.isTarget;
                if (orig.isConrefTarget) isConrefTarget = orig.isConrefTarget;
                if (orig.isNonConrefTarget) isNonConrefTarget = orig.isNonConrefTarget;
                if (orig.isConrefPush) isConrefPush = orig.isConrefPush;
                if (orig.hasKeyref) hasKeyref = orig.hasKeyref;
                if (orig.hasCoderef) hasCoderef = orig.hasCoderef;
                if (orig.isSubjectScheme) isSubjectScheme = orig.isSubjectScheme;
                if (orig.isSkipChunk) isSkipChunk = orig.isSkipChunk;
                if (orig.isSubtarget) isSubtarget = orig.isSubtarget;
                if (orig.isFlagImage) isFlagImage = orig.isFlagImage;
                if (orig.isOutDita) isOutDita = orig.isOutDita;
                if (orig.isCopyToSource) isCopyToSource = orig.isCopyToSource;
                return this;
            }
            
            public Builder src(final URI src) { this.src = src; return this; }
            public Builder uri(final URI uri) { this.uri = uri; this.file = null; return this; }
            public Builder file(final File file) { this.file = file; this.uri = null; return this; }
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
            public Builder isOutDita(final boolean isOutDita) { this.isOutDita = isOutDita; return this; }
            public Builder isCopyToSource(final boolean isCopyToSource) { this.isCopyToSource = isCopyToSource; return this; }
            
            public FileInfo build() {
                if (src == null && uri == null && file == null) {
                    throw new IllegalStateException("src, uri, and file may not be null");
                }
                final FileInfo fi = new FileInfo(src, uri, file);
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
                fi.isOutDita = isOutDita;
                fi.isCopyToSource = isCopyToSource;
                return fi;
            }
            
        }
        
    }

    public enum OutterControl {
        /** Fail behavior. */
        FAIL,
        /** Warn behavior. */
        WARN,
        /** Quiet behavior. */
        QUIET
    }
    
    public enum Generate {
        /** Not generate outer files. */
        NOT_GENERATEOUTTER(1),
        /** Old solution. */
        OLDSOLUTION(3);

        public final int type;

        Generate(final int type) {
            this.type = type;
        }

        public static Generate get(final int type) {
            for (final Generate g: Generate.values()) {
                if (g.type == type) {
                    return g;
                }
            }
            throw new IllegalArgumentException();
        }
    }
    
    /**
     * Retrieve the outercontrol.
     * @return String outercontrol behavior
     *
     */
    public OutterControl getOutterControl(){
        return OutterControl.valueOf(prop.get(PROPERTY_OUTER_CONTROL).toString());
    }

    /**
     * Set the outercontrol.
     * @param control control
     */
    public void setOutterControl(final String control){
        prop.put(PROPERTY_OUTER_CONTROL, OutterControl.valueOf(control.toUpperCase()).toString());
    }

    /**
     * Retrieve the flag of onlytopicinmap.
     * @return boolean if only topic in map
     */
    public boolean getOnlyTopicInMap(){
        return Boolean.parseBoolean(prop.get(PROPERTY_ONLY_TOPIC_IN_MAP).toString());
    }

    /**
     * Set the onlytopicinmap.
     * @param flag onlytopicinmap flag
     */
    public void setOnlyTopicInMap(final boolean flag){
        prop.put(PROPERTY_ONLY_TOPIC_IN_MAP, Boolean.toString(flag));
    }

    public Generate getGeneratecopyouter(){
        return Generate.valueOf(prop.get(PROPERTY_GENERATE_COPY_OUTER).toString());
    }

    /**
     * Set the generatecopyouter.
     * @param flag generatecopyouter flag
     */
    public void setGeneratecopyouter(final String flag){
        setGeneratecopyouter(Generate.get(Integer.parseInt(flag)));
    }

    /**
     * Set the generatecopyouter.
     * @param flag generatecopyouter flag
     */
    public void setGeneratecopyouter(final Generate flag){
        prop.put(PROPERTY_GENERATE_COPY_OUTER, flag.toString());
    }
 
    /**
     * Get output dir.
     * @return absolute output dir
     */
    public File getOutputDir(){
        return new File(prop.get(PROPERTY_OUTPUT_DIR).toString());
    }

    /**
     * Set output dir.
     * @param outputDir absolute output dir
     */
    public void setOutputDir(final File outputDir){
        prop.put(PROPERTY_OUTPUT_DIR, outputDir.getAbsolutePath());
    }

    /**
     * Get input file path.
     * @return absolute input file path
     */
    public URI getInputFile() {
        return toURI(prop.get(PROPERTY_INPUT_MAP_URI).toString());
    }

    /**
     * Set input map path.
     * @param inputFile absolute input map path
     */
    public void setInputFile(final URI inputFile) {
        assert inputFile.isAbsolute();
        prop.put(PROPERTY_INPUT_MAP_URI, inputFile.toString());
        // Deprecated since 2.1
        if (inputFile.getScheme().equals("file")) {
            prop.put(PROPERTY_INPUT_MAP, new File(inputFile).getAbsolutePath());
        }
    }

    
}
