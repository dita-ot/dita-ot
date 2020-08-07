/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.util;

import org.dita.dost.module.reader.TempFileNameScheme;
import org.dita.dost.store.Store;
import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.dom.DOMResult;
import java.io.*;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.dita.dost.util.Configuration.configuration;
import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.*;

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
    private static final String ATTRIBUTE_RESULT = "result";
    private static final String ATTRIBUTE_FORMAT = "format";
    private static final String ATTRIBUTE_CHUNKED = "chunked";
    private static final String ATTRIBUTE_HAS_CONREF = "has-conref";
    private static final String ATTRIBUTE_HAS_KEYREF = "has-keyref";
    private static final String ATTRIBUTE_HAS_CODEREF = "has-coderef";
    private static final String ATTRIBUTE_RESOURCE_ONLY = "resource-only";
    private static final String ATTRIBUTE_TARGET = "target";
    private static final String ATTRIBUTE_CONREF_TARGET = "conref-target";
    private static final String ATTRIBUTE_CONREF_PUSH = "conrefpush";
    private static final String ATTRIBUTE_SUBJECT_SCHEME = "subjectscheme";
    private static final String ATTRIBUTE_HAS_LINK = "has-link";
    private static final String ATTRIBUTE_INPUT = "input";
    private static final String ATTRIBUTE_COPYTO_SOURCE_LIST = "copy-to-source";
    private static final String ATTRIBUTE_OUT_DITA_FILES_LIST = "out-dita";
    private static final String ATTRIBUTE_CHUNKED_DITAMAP_LIST = "chunked-ditamap";
    private static final String ATTRIBUTE_FLAG_IMAGE_LIST = "flag-image";
    private static final String ATTRIBUTE_SUBSIDIARY_TARGET_LIST = "subtarget";

    private static final String PROPERTY_OUTER_CONTROL = ANT_INVOKER_EXT_PARAM_OUTTERCONTROL;
    private static final String PROPERTY_ONLY_TOPIC_IN_MAP = ANT_INVOKER_EXT_PARAM_ONLYTOPICINMAP;
    private static final String PROPERTY_LINK_CRAWLER = ANT_INVOKER_EXT_PARAM_CRAWL;
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
    private static final Map<String, Field> attrToFieldMap = new HashMap<>();
    static {
        try {
            attrToFieldMap.put(ATTRIBUTE_CHUNKED, FileInfo.class.getField("isChunked"));
            attrToFieldMap.put(ATTRIBUTE_HAS_LINK, FileInfo.class.getField("hasLink"));
            attrToFieldMap.put(ATTRIBUTE_INPUT, FileInfo.class.getField("isInput"));
            attrToFieldMap.put(ATTRIBUTE_HAS_CONREF, FileInfo.class.getField("hasConref"));
            attrToFieldMap.put(ATTRIBUTE_HAS_KEYREF, FileInfo.class.getField("hasKeyref"));
            attrToFieldMap.put(ATTRIBUTE_HAS_CODEREF, FileInfo.class.getField("hasCoderef"));
            attrToFieldMap.put(ATTRIBUTE_RESOURCE_ONLY, FileInfo.class.getField("isResourceOnly"));
            attrToFieldMap.put(ATTRIBUTE_TARGET, FileInfo.class.getField("isTarget"));
            attrToFieldMap.put(ATTRIBUTE_CONREF_PUSH, FileInfo.class.getField("isConrefPush"));
            attrToFieldMap.put(ATTRIBUTE_SUBJECT_SCHEME, FileInfo.class.getField("isSubjectScheme"));
            attrToFieldMap.put(ATTRIBUTE_OUT_DITA_FILES_LIST, FileInfo.class.getField("isOutDita"));
            attrToFieldMap.put(ATTRIBUTE_FLAG_IMAGE_LIST, FileInfo.class.getField("isFlagImage"));
            attrToFieldMap.put(ATTRIBUTE_SUBSIDIARY_TARGET_LIST, FileInfo.class.getField("isSubtarget"));
        } catch (final NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private final Map<String, Object> prop;
    public final File tempDir;
    public final URI tempDirURI;
    private final File jobFile;
    private final Map<URI, FileInfo> files = new ConcurrentHashMap<>();
    private long lastModified;
    private final Store store;

    /**
     * Create new job configuration instance. Initialise by reading temporary configuration files.
     *
     * @param tempDir temporary directory
     * @param store IO store
     * @throws IOException if reading configuration files failed
     * @throws IllegalStateException if configuration files are missing
     */
    public Job(final File tempDir, final Store store) throws IOException {
        if (!tempDir.isAbsolute()) {
            throw new IllegalArgumentException("Temporary directory " + tempDir + " must be absolute");
        }
        this.tempDir = tempDir;
        this.store = store;
        tempDirURI = tempDir.toURI();
        jobFile = new File(tempDir, JOB_FILE);
        prop = new HashMap<>();
        read();
        for (Map.Entry<String, String> e : configuration.entrySet()) {
            if (!prop.containsKey(e.getKey())) {
                prop.put(e.getKey(), e.getValue());
            }
        }
    }

    public Job(final Job job, final Map<String, Object> prop, final Collection<FileInfo> files) {
        this.tempDir = job.tempDir;
        this.store = job.store;
        this.tempDirURI = tempDir.toURI();
        this.jobFile = new File(tempDir, JOB_FILE);
        this.prop = prop;
        this.files.putAll(files.stream().collect(Collectors.toMap(fi -> fi.uri, Function.identity())));
    }

    public Store getStore() {
        return store;
    }

    /**
     * Test if serialized configuration file has been updated.
     * @return {@code true} if configuration file has been update after this object has been created or serialized
     */
    public boolean isStale() {
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
            try (final InputStream in = new FileInputStream(jobFile)) {
                final XMLReader parser = XMLUtils.getXMLReader();
                parser.setContentHandler(new JobHandler(prop, files));

                parser.parse(new InputSource(in));
            } catch (final SAXException e) {
                throw new IOException("Failed to read job file: " + e.getMessage());
            }
        } else {
            // defaults
            prop.put(PROPERTY_GENERATE_COPY_OUTER, Generate.NOT_GENERATEOUTTER.toString());
            prop.put(PROPERTY_ONLY_TOPIC_IN_MAP, Boolean.toString(false));
            prop.put(PROPERTY_OUTER_CONTROL, OutterControl.WARN.toString());
        }
    }

    public final static class JobHandler extends DefaultHandler {

        private final Map<String, Object> prop;
        private final Map<URI, FileInfo> files;
        private StringBuilder buf;
        private String name;
        private String key;
        private Set<String> set;
        private Map<String, String> map;

        public JobHandler(final Map<String, Object> prop, final Map<URI, FileInfo> files) {
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
                    i.result = toURI(atts.getValue(ATTRIBUTE_RESULT));
                    if (i.result == null) {
                        i.result = src;
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
        if (!tempDir.exists() && !tempDir.mkdirs()) {
            throw new IOException("Failed to create " + tempDir + " directory");
        }
        OutputStream outStream = null;
        XMLStreamWriter out = null;
        try {
            outStream = new FileOutputStream(jobFile);
            out = XMLOutputFactory.newInstance().createXMLStreamWriter(outStream, "UTF-8");
            serialize(out, prop, files.values());
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

    public Document serialize() throws IOException {
        try {
            final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            final DOMResult result = new DOMResult(doc);
            XMLStreamWriter out = XMLOutputFactory.newInstance().createXMLStreamWriter(result);
            serialize(out, prop, files.values());
            return (Document) result.getNode();
        } catch (final XMLStreamException | ParserConfigurationException e) {
            throw new IOException("Failed to serialize job file: " + e.getMessage());
        }
    }

    public void serialize(XMLStreamWriter out, Map<String, Object> props, Collection<FileInfo> fs) throws XMLStreamException {
        out.writeStartDocument();
        out.writeStartElement(ELEMENT_JOB);
        for (final Map.Entry<String, Object> e: props.entrySet()) {
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
        for (final FileInfo i: fs) {
            out.writeStartElement(ELEMENT_FILE);
            if (i.src != null) {
                out.writeAttribute(ATTRIBUTE_SRC, i.src.toString());
            }
            out.writeAttribute(ATTRIBUTE_URI, i.uri.toString());
            out.writeAttribute(ATTRIBUTE_PATH, i.file.getPath());
            if (i.result != null) {
                out.writeAttribute(ATTRIBUTE_RESULT, i.result.toString());
            }
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
     * Get input file
     *
     * @return input file path relative to input directory, {@code null} if not set
     */
    public URI getInputMap() {
//       return toURI(getProperty(INPUT_DITAMAP_URI));
        return files.values().stream()
                .filter(fi -> fi.isInput)
                .map(fi -> getInputDir().relativize(fi.src))
                .findAny()
                .orElse(null);
    }

    /**
     * set input file
     *
     * @param map input file path relative to input directory
     */
    public void setInputMap(final URI map) {
        assert !map.isAbsolute();
        setProperty(INPUT_DITAMAP_URI, map.toString());
        // Deprecated since 2.2
        setProperty(INPUT_DITAMAP, toFile(map).getPath());
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
     * Set input directory
     * @param dir absolute input directory path
     */
    public void setInputDir(final URI dir) {
        assert dir.isAbsolute();
        setProperty(INPUT_DIR_URI, dir.toString());
        // Deprecated since 2.2
        if (dir.getScheme().equals("file")) {
            setProperty(INPUT_DIR, new File(dir).getAbsolutePath());
        }
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
    public Collection<FileInfo> getFileInfo(final Predicate<FileInfo> filter) {
        return files.values().stream()
                .filter(filter)
                .collect(Collectors.toList());
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
        } else if (file.isAbsolute() && file.toString().startsWith(tempDirURI.toString())) {
            final URI relative = getRelativePath(jobFile.toURI(), file);
            return files.get(relative);
        } else {
            return files.values().stream()
                    .filter(fileInfo -> file.equals(fileInfo.src) || file.equals(fileInfo.result))
                    .findFirst()
                    .orElse(null);
        }
    }

    /**
     * Get or create FileInfo for given path.
     * @param file relative URI to temporary directory
     * @return created or existing file info object
     */
    public FileInfo getOrCreateFileInfo(final URI file) {
        assert file.getFragment() == null;
        URI f = file.normalize();
        if (f.isAbsolute()) {
            f = tempDirURI.relativize(f);
        }
        FileInfo i = getFileInfo(file);
        if (i == null) {
            i = new FileInfo(f);
            add(i);
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
            add(f);
        }
    }

    /**
     * File info object.
     */
    public static final class FileInfo {

        /** Absolute source URI. */
        public URI src;
        /** File URI. */
        public final URI uri;
        /** File path. */
        public final File file;
        /** Absolute result URI. */
        public URI result;
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
        /** File is a push conref source. */
        public boolean isConrefPush;
        /** File has a keyref. */
        public boolean hasKeyref;
        /** File has coderef. */
        public boolean hasCoderef;
        /** File is a subject scheme. */
        public boolean isSubjectScheme;
        /** File is a coderef target. */
        public boolean isSubtarget;
        /** File is a flagging image. */
        public boolean isFlagImage;
        /** Source file is outside base directory. */
        public boolean isOutDita;
        /** File is input document that is used as processing root. */
        public boolean isInput;
        /** Additional input resource. */
        public boolean isInputResource;

        FileInfo(final URI src, final URI uri, final File file) {
            if (uri == null && file == null) throw new IllegalArgumentException(new NullPointerException());
            this.src = src;
            this.uri = uri != null ? uri : toURI(file);
            this.file = uri != null ? toFile(uri) : file;
            this.result = src;
        }
        FileInfo(final URI uri) {
            if (uri == null) throw new IllegalArgumentException(new NullPointerException());
            this.src = null;
            this.uri = uri;
            this.file = toFile(uri);
            this.result = src;
        }

        @Override
        public String toString() {
            return "FileInfo{" +
                    "src=" + src +
                    ", result=" + result +
                    ", uri=" + uri +
                    ", file=" + file +
                    ", format='" + format + '\'' +
                    ", hasConref=" + hasConref +
                    ", isChunked=" + isChunked +
                    ", hasLink=" + hasLink +
                    ", isResourceOnly=" + isResourceOnly +
                    ", isTarget=" + isTarget +
                    ", isConrefPush=" + isConrefPush +
                    ", isInput=" + isInput +
                    ", isInputResource=" + isInputResource +
                    ", hasKeyref=" + hasKeyref +
                    ", hasCoderef=" + hasCoderef +
                    ", isSubjectScheme=" + isSubjectScheme +
                    ", isSubtarget=" + isSubtarget +
                    ", isFlagImage=" + isFlagImage +
                    ", isOutDita=" + isOutDita +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FileInfo fileInfo = (FileInfo) o;
            return hasConref == fileInfo.hasConref &&
                    isChunked == fileInfo.isChunked &&
                    hasLink == fileInfo.hasLink &&
                    isResourceOnly == fileInfo.isResourceOnly &&
                    isTarget == fileInfo.isTarget &&
                    isConrefPush == fileInfo.isConrefPush &&
                    hasKeyref == fileInfo.hasKeyref &&
                    hasCoderef == fileInfo.hasCoderef &&
                    isSubjectScheme == fileInfo.isSubjectScheme &&
                    isSubtarget == fileInfo.isSubtarget &&
                    isFlagImage == fileInfo.isFlagImage &&
                    isOutDita == fileInfo.isOutDita &&
                    isInput == fileInfo.isInput &&
                    isInputResource == fileInfo.isInputResource &&
                    Objects.equals(src, fileInfo.src) &&
                    Objects.equals(uri, fileInfo.uri) &&
                    Objects.equals(file, fileInfo.file) &&
                    Objects.equals(result, fileInfo.result) &&
                    Objects.equals(format, fileInfo.format);
        }

        @Override
        public int hashCode() {
            return Objects.hash(src, uri, file, result, format, hasConref, isChunked, hasLink, isResourceOnly, isTarget,
                    isConrefPush, hasKeyref, hasCoderef, isSubjectScheme, isSubtarget, isFlagImage, isOutDita, isInput,
                    isInputResource);
        }

        public static Builder builder() {
            return new Builder();
        }

        public static Builder builder(final FileInfo fileInfo) {
            return new Builder(fileInfo);
        }

        public static class Builder {

            private URI src;
            private URI uri;
            private File file;
            private URI result;
            private String format;
            private boolean hasConref;
            private boolean isChunked;
            private boolean hasLink;
            private boolean isResourceOnly;
            private boolean isTarget;
            private boolean isConrefPush;
            private boolean hasKeyref;
            private boolean hasCoderef;
            private boolean isSubjectScheme;
            private boolean isSubtarget;
            private boolean isFlagImage;
            private boolean isOutDita;
            private boolean isInput;
            private boolean isInputResource;

            public Builder() {}
            public Builder(final FileInfo orig) {
                src = orig.src;
                uri = orig.uri;
                file = orig.file;
                result = orig.result;
                format = orig.format;
                hasConref = orig.hasConref;
                isChunked = orig.isChunked;
                hasLink = orig.hasLink;
                isResourceOnly = orig.isResourceOnly;
                isTarget = orig.isTarget;
                isConrefPush = orig.isConrefPush;
                hasKeyref = orig.hasKeyref;
                hasCoderef = orig.hasCoderef;
                isSubjectScheme = orig.isSubjectScheme;
                isSubtarget = orig.isSubtarget;
                isFlagImage = orig.isFlagImage;
                isOutDita = orig.isOutDita;
                isInput = orig.isInput;
                isInputResource = orig.isInputResource;
            }

            /**
             * Add file info to this builder. Only non-null and true values will be added.
             */
            public Builder add(final FileInfo orig) {
                if (orig.src != null) src = orig.src;
                if (orig.uri != null) uri = orig.uri;
                if (orig.file != null) file = orig.file;
                if (orig.result != null) result = orig.result;
                if (orig.format != null) format = orig.format;
                if (orig.hasConref) hasConref = orig.hasConref;
                if (orig.isChunked) isChunked = orig.isChunked;
                if (orig.hasLink) hasLink = orig.hasLink;
                if (orig.isResourceOnly) isResourceOnly = orig.isResourceOnly;
                if (orig.isTarget) isTarget = orig.isTarget;
                if (orig.isConrefPush) isConrefPush = orig.isConrefPush;
                if (orig.hasKeyref) hasKeyref = orig.hasKeyref;
                if (orig.hasCoderef) hasCoderef = orig.hasCoderef;
                if (orig.isSubjectScheme) isSubjectScheme = orig.isSubjectScheme;
                if (orig.isSubtarget) isSubtarget = orig.isSubtarget;
                if (orig.isFlagImage) isFlagImage = orig.isFlagImage;
                if (orig.isOutDita) isOutDita = orig.isOutDita;
                if (orig.isInput) isInput = orig.isInput;
                if (orig.isInputResource) isInputResource = orig.isInputResource;
                return this;
            }

            public Builder addContentFields(final FileInfo orig) {
//                if (orig.src != null) src = orig.src;
//                if (orig.uri != null) uri = orig.uri;
//                if (orig.file != null) file = orig.file;
//                if (orig.result != null) result = orig.result;
                if (orig.format != null) format = orig.format;
                if (orig.hasConref) hasConref = orig.hasConref;
                if (orig.isChunked) isChunked = orig.isChunked;
                if (orig.hasLink) hasLink = orig.hasLink;
//                if (orig.isResourceOnly) isResourceOnly = orig.isResourceOnly;
//                if (orig.isTarget) isTarget = orig.isTarget;
                if (orig.isConrefPush) isConrefPush = orig.isConrefPush;
                if (orig.hasKeyref) hasKeyref = orig.hasKeyref;
                if (orig.hasCoderef) hasCoderef = orig.hasCoderef;
//                if (orig.isSubjectScheme) isSubjectScheme = orig.isSubjectScheme;
//                if (orig.isSubtarget) isSubtarget = orig.isSubtarget;
//                if (orig.isFlagImage) isFlagImage = orig.isFlagImage;
//                if (orig.isOutDita) isOutDita = orig.isOutDita;
                return this;
            }

            public Builder src(final URI src) { assert src.isAbsolute(); this.src = src; return this; }
            public Builder uri(final URI uri) { this.uri = uri; this.file = null; return this; }
            public Builder file(final File file) { this.file = file; this.uri = null; return this; }
            public Builder result(final URI result) { this.result = result; return this; }
            public Builder format(final String format) { this.format = format; return this; }
            public Builder hasConref(final boolean hasConref) { this.hasConref = hasConref; return this; }
            public Builder isChunked(final boolean isChunked) { this.isChunked = isChunked; return this; }
            public Builder hasLink(final boolean hasLink) { this.hasLink = hasLink; return this; }
            public Builder isResourceOnly(final boolean isResourceOnly) { this.isResourceOnly = isResourceOnly; return this; }
            public Builder isTarget(final boolean isTarget) { this.isTarget = isTarget; return this; }
            public Builder isConrefPush(final boolean isConrefPush) { this.isConrefPush = isConrefPush; return this; }
            public Builder hasKeyref(final boolean hasKeyref) { this.hasKeyref = hasKeyref; return this; }
            public Builder hasCoderef(final boolean hasCoderef) { this.hasCoderef = hasCoderef; return this; }
            public Builder isSubjectScheme(final boolean isSubjectScheme) { this.isSubjectScheme = isSubjectScheme; return this; }
            public Builder isSubtarget(final boolean isSubtarget) { this.isSubtarget = isSubtarget; return this; }
            public Builder isFlagImage(final boolean isFlagImage) { this.isFlagImage = isFlagImage; return this; }
            public Builder isOutDita(final boolean isOutDita) { this.isOutDita = isOutDita; return this; }
            public Builder isInput(final boolean isInput) { this.isInput = isInput; return this; }
            public Builder isInputResource(final boolean isInputResource) { this.isInputResource = isInputResource; return this; }

            public FileInfo build() {
                if (uri == null && file == null) {
                    throw new IllegalStateException("uri and file may not be null");
                }
                final FileInfo fi = new FileInfo(src, uri, file);
                if (result != null) {
                    fi.result = result;
                }
                fi.format = format;
                fi.hasConref = hasConref;
                fi.isChunked = isChunked;
                fi.hasLink = hasLink;
                fi.isResourceOnly = isResourceOnly;
                fi.isTarget = isTarget;
                fi.isConrefPush = isConrefPush;
                fi.hasKeyref = hasKeyref;
                fi.hasCoderef = hasCoderef;
                fi.isSubjectScheme = isSubjectScheme;
                fi.isSubtarget = isSubtarget;
                fi.isFlagImage = isFlagImage;
                fi.isOutDita = isOutDita;
                fi.isInput = isInput;   
                fi.isInputResource = isInputResource;
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
    public OutterControl getOutterControl() {
        return OutterControl.valueOf(prop.get(PROPERTY_OUTER_CONTROL).toString());
    }

    /**
     * Set the outercontrol.
     * @param control control
     */
    public void setOutterControl(final String control) {
        prop.put(PROPERTY_OUTER_CONTROL, OutterControl.valueOf(control.toUpperCase()).toString());
    }

    /**
     * Retrieve the flag of onlytopicinmap.
     * @return boolean if only topic in map
     */
    public boolean getOnlyTopicInMap() {
        return Boolean.parseBoolean(prop.get(PROPERTY_ONLY_TOPIC_IN_MAP).toString());
    }

    /**
     * Retrieve the link crawling behaviour.
     * @return {@code true} if crawl links in topics, {@code false} if only crawl links in maps
     */
    public boolean crawlTopics() {
        if (prop.get(PROPERTY_LINK_CRAWLER) == null) {
            return true;
        }
        return prop.get(PROPERTY_LINK_CRAWLER).toString().equals(ANT_INVOKER_EXT_PARAM_CRAWL_VALUE_TOPIC);
    }

    /**
     * Set the onlytopicinmap.
     * @param flag onlytopicinmap flag
     */
    public void setOnlyTopicInMap(final boolean flag) {
        prop.put(PROPERTY_ONLY_TOPIC_IN_MAP, Boolean.toString(flag));
    }
    
    /**
     * Set the link crawling property.
     * @param crawlvalue crawl mode
     */
    public void setCrawl(final String crawlvalue) {
        if (crawlvalue != null) {
            prop.put(PROPERTY_LINK_CRAWLER, crawlvalue);
        }
    }

    public Generate getGeneratecopyouter() {
        return Generate.valueOf(prop.get(PROPERTY_GENERATE_COPY_OUTER).toString());
    }

    /**
     * Set the generatecopyouter.
     * @param flag generatecopyouter flag
     */
    public void setGeneratecopyouter(final String flag) {
        setGeneratecopyouter(Generate.get(Integer.parseInt(flag)));
    }

    /**
     * Set the generatecopyouter.
     * @param flag generatecopyouter flag
     */
    public void setGeneratecopyouter(final Generate flag) {
        prop.put(PROPERTY_GENERATE_COPY_OUTER, flag.toString());
    }

    /**
     * Get output dir.
     * @return absolute output dir
     */
    public File getOutputDir() {
        if (prop.containsKey(PROPERTY_OUTPUT_DIR)) {
            return new File(prop.get(PROPERTY_OUTPUT_DIR).toString());
        }
        return null;
    }

    /**
     * Set output dir.
     * @param outputDir absolute output dir
     */
    public void setOutputDir(final File outputDir) {
        prop.put(PROPERTY_OUTPUT_DIR, outputDir.getAbsolutePath());
    }

    /**
     * Get input file path.
     * @return absolute input file path, {@code null} if not set
     */
    public URI getInputFile() {
//        if (prop.containsKey(PROPERTY_INPUT_MAP_URI)) {
//            return toURI(prop.get(PROPERTY_INPUT_MAP_URI).toString());
//        }
//        return null;
        return files.values().stream()
                .filter(fi -> fi.isInput)
                .map(fi -> fi.src)
                .findAny()
                .orElseGet(() -> Optional.ofNullable((String) prop.get(PROPERTY_INPUT_MAP_URI))
                        .map(URLUtils::toURI)
                        .orElse(null)
                );
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

    /**
     * Get temporary file name generator.
     */
    public TempFileNameScheme getTempFileNameScheme() {
        final TempFileNameScheme tempFileNameScheme;
        try {
            final String cls = Optional
                    .ofNullable(getProperty("temp-file-name-scheme"))
                    .orElse(configuration.get("temp-file-name-scheme"));
            tempFileNameScheme = (TempFileNameScheme) Class.forName(cls).newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        tempFileNameScheme.setBaseDir(getInputDir());
        return tempFileNameScheme;
    }

}
