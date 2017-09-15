/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2004, 2005 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.writer;

import org.dita.dost.module.GenMapAndTopicListModule.TempFileNameScheme;
import org.dita.dost.util.*;
import org.dita.dost.util.Job.FileInfo;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.dita.dost.module.DebugAndFilterModule;

import javax.xml.namespace.QName;
import java.io.File;
import java.net.URI;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.*;
import static org.dita.dost.util.URLUtils.getRelativePath;
import static org.dita.dost.reader.GenListModuleReader.*;


/**
 * Insert document PIs and normalize attributes.
 * 
 * <p>The following processing instructions are added before the root element:</p>
 * <dl>
 *   <dt>{@link Constants#PI_WORKDIR_TARGET}</dt>
 *   <dd>Absolute system path of the file parent directory. On Windows, a {@code /}
 *     is added to beginning of the path.</dd>
 *   <dt>{@link Constants#PI_WORKDIR_TARGET_URI}</dt>
 *   <dd>Absolute URI of the file parent directory.</dd>
 *   <dt>{@link Constants#PI_PATH2PROJ_TARGET}</dt>
 *   <dd>Relative system path to the output directory, with a trailing directory separator.
 *     When the source file is in the project root directory, processing instruction has no value.</dd>
 *   <dt>{@link Constants#PI_PATH2PROJ_TARGET_URI}</dt>
 *   <dd>Relative URI to the output directory, with a trailing path separator.
 *     When the source file is in the project root directory, processing instruction has value {@code ./}.</dd>
 *   <dt>{@link Constants#PI_PATH2ROOTMAP_TARGET_URI}</dt>
 *   <dd>Relative URI to the root map directory, with a trailing path separator.
 *     When the source file is in the project root directory, processing instruction has value {@code ./}.</dd>
 * </dl>
 *
 * @author Zhang, Yuan Peng
 */
public final class DitaWriterFilter extends AbstractXMLFilter {

    /** Default value map. */
    private Map<QName, Map<String, String>> defaultValueMap;
    /** Absolute path to current destination file. */
    private File outputFile;
    /** DITA class values for open elements **/
    private final Deque<DitaClass> classes = new LinkedList<>();
    /** File infos by src. */
    private Map<URI, FileInfo> fileInfoMap;
//    /** File infos by src. */
//    private Map<URI, FileInfo> fileInfoMap;
    private TempFileNameScheme tempFileNameScheme;

    public DitaWriterFilter() {
    }

    public void setTempFileNameScheme(TempFileNameScheme tempFileNameScheme) {
        this.tempFileNameScheme = tempFileNameScheme;
    }

    /**
     * Set default value map.
     * @param defaultMap default value map
     */
    public void setDefaultValueMap(final Map<QName, Map<String, String>> defaultMap) {
        defaultValueMap  = defaultMap;
    }

    public void setOutputFile(final File outputFile) {
        this.outputFile = outputFile;
    }

    @Override
    public void setJob(final Job job) {
        super.setJob(job);
//        fileInfoMap = new HashMap<>();
//        for (final FileInfo f: job.getFileInfo()) {
//            fileInfoMap.put(f.result, f);
//        }
    }

    // ContentHandler methods

    @Override
    public void endElement(final String uri, final String localName, final String qName)
            throws SAXException {
        classes.pop();
        getContentHandler().endElement(uri, localName, qName);
    }

    @Override
    public void startDocument() throws SAXException {
        classes.clear();

        // XXX May be require fixup
        final File path2Project = DebugAndFilterModule.getPathtoProject(FileUtils.getRelativePath(toFile(job.getInputFile()), toFile(currentFile)),
                toFile(currentFile),
                toFile(job.getInputFile()),
                job);
        final File path2rootmap = toFile(getRelativePath(currentFile, job.getInputFile())).getParentFile();
        getContentHandler().startDocument();
        if (!OS_NAME.toLowerCase().contains(OS_NAME_WINDOWS)) {
            getContentHandler().processingInstruction(PI_WORKDIR_TARGET, outputFile.getParentFile().getAbsolutePath());
        } else {
            getContentHandler().processingInstruction(PI_WORKDIR_TARGET, UNIX_SEPARATOR + outputFile.getParentFile().getAbsolutePath());
        }
        getContentHandler().ignorableWhitespace(new char[]{'\n'}, 0, 1);
        getContentHandler().processingInstruction(PI_WORKDIR_TARGET_URI, outputFile.getParentFile().toURI().toASCIIString());
        getContentHandler().ignorableWhitespace(new char[]{'\n'}, 0, 1);
        if (path2Project != null) {
            getContentHandler().processingInstruction(PI_PATH2PROJ_TARGET, path2Project.getPath() + File.separator);
            getContentHandler().processingInstruction(PI_PATH2PROJ_TARGET_URI, toURI(path2Project).toString() + URI_SEPARATOR);
        } else {
            getContentHandler().processingInstruction(PI_PATH2PROJ_TARGET, "");
            getContentHandler().processingInstruction(PI_PATH2PROJ_TARGET_URI, "." + URI_SEPARATOR);
        }
        if (path2rootmap != null) {
            getContentHandler().processingInstruction(PI_PATH2ROOTMAP_TARGET_URI, toURI(path2rootmap).toString() + URI_SEPARATOR);
        } else {
            getContentHandler().processingInstruction(PI_PATH2ROOTMAP_TARGET_URI, "." + URI_SEPARATOR);
        }
        getContentHandler().ignorableWhitespace(new char[]{'\n'}, 0, 1);
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName,
                             final Attributes atts) throws SAXException {
        final DitaClass cls = atts.getValue(ATTRIBUTE_NAME_CLASS) != null ? new DitaClass(atts.getValue(ATTRIBUTE_NAME_CLASS)) : new DitaClass("");
        if (cls.isValid()) {
            classes.addFirst(cls);
        }else {
            classes.addFirst(null);
        }

        final AttributesImpl res = new AttributesImpl();
        processAttributes(qName, atts, res);

        getContentHandler().startElement(uri, localName, qName, res);
    }

    /**
     * Process attributes
     *
     * @param qName element name
     * @param atts input attributes
     * @param res attributes to write to
     */
    private void processAttributes(final String qName, final Attributes atts, final AttributesImpl res) {
        final int attsLen = atts.getLength();
        for (int i = 0; i < attsLen; i++) {
            final QName attQName = new QName(atts.getURI(i), atts.getLocalName(i));
            final String origValue = atts.getValue(i);
            String attValue = origValue;
            if (ATTRIBUTE_NAME_CONREF.equals(attQName)) {
                attValue = replaceHREF(QName.valueOf(ATTRIBUTE_NAME_CONREF), atts).toString();
            } else if(ATTRIBUTE_NAME_HREF.equals(attQName) || ATTRIBUTE_NAME_COPY_TO.equals(attQName)){
                if (atts.getValue(ATTRIBUTE_NAME_SCOPE) == null ||
                        atts.getValue(ATTRIBUTE_NAME_SCOPE).equals(ATTR_SCOPE_VALUE_LOCAL)){
                    attValue = replaceHREF(attQName, atts).toString();
                }
            } else if(ATTRIBUTE_NAME_FORMAT.equals(attQName)) {
                final String format = atts.getValue(ATTRIBUTE_NAME_FORMAT);
                // verify format is correct
                if (isFormatDita(format)) {
                    attValue = ATTR_FORMAT_VALUE_DITA;
                }
            } else {
                attValue = getAttributeValue(qName, attQName, attValue);
            }
            XMLUtils.addOrSetAttribute(res, atts.getURI(i), atts.getLocalName(i), atts.getQName(i), atts.getType(i), attValue);
        }
    }

    /**
     * Get attribute value or default if attribute is not defined
     *
     * @param elemQName element QName
     * @param attQName attribute QName
     * @param value attribute value
     * @return attribute value or default
     */
    private String getAttributeValue(final String elemQName, final QName attQName, final String value) {
        if (StringUtils.isEmptyString(value) && !defaultValueMap.isEmpty()) {
            final Map<String, String> defaultMap = defaultValueMap.get(attQName);
            if (defaultMap != null) {
                final String defaultValue = defaultMap.get(elemQName);
                if (defaultValue != null) {
                    return defaultValue;
                }
            }
        }
        return value;
    }

    /**
     * Relativize absolute references if possible.
     *
     * @param attName attribute name
     * @param atts attributes
     * @return attribute value, may be {@code null}
     */
    private URI replaceHREF(final QName attName, final Attributes atts){
        URI attValue = toURI(atts.getValue(attName.getNamespaceURI(), attName.getLocalPart()));
        if (attValue != null) {
            final String fragment = attValue.getFragment();
            if (fragment != null) {
                attValue = stripFragment(attValue);
            }
            if (attValue.toString().length() != 0) {
                final URI current = currentFile.resolve(attValue);
                final FileInfo f = job.getFileInfo(current);
                if (f != null) {
                    final FileInfo cfi = job.getFileInfo(currentFile);
                    final URI currrentFileTemp = job.tempDirURI.resolve(cfi.uri);
                    final URI targetTemp = job.tempDirURI.resolve(f.uri);
                    attValue = getRelativePath(currrentFileTemp, targetTemp);
                } else if (tempFileNameScheme != null) {
                    final URI currrentFileTemp = job.tempDirURI.resolve(tempFileNameScheme.generateTempFileName(currentFile));
                    final URI targetTemp = job.tempDirURI.resolve(tempFileNameScheme.generateTempFileName(current));
                    final URI relativePath = getRelativePath(currrentFileTemp, targetTemp);
                    attValue = relativePath;
                } else {
                    attValue = getRelativePath(currentFile, current);
                }
            }
            if (fragment != null) {
                attValue = setFragment(attValue, fragment);
            }
        } else {
            return null;
        }
        return attValue;
    }

}
