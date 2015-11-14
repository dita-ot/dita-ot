/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.writer;

import org.dita.dost.log.MessageUtils;
import org.dita.dost.util.Constants;
import org.dita.dost.util.Job;
import org.dita.dost.util.Job.FileInfo;
import org.dita.dost.util.StringUtils;
import org.dita.dost.util.XMLUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.dita.dost.module.DebugAndFilterModule;

import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.FileUtils.*;
import static org.dita.dost.util.URLUtils.*;
import static org.dita.dost.reader.GenListModuleReader.*;


/**
 * Insert document PIs and normalize attributes.
 * 
 * <p>The following processing instructions are added before the root element:</p>
 * <dl>
 *   <dt>{@link Constants#PI_WORKDIR_TARGET}<dt>
 *   <dd>Absolute system path of the file parent directory. On Windows, a {@code /}
 *     is added to beginning of the path.</dd>
 *   <dt>{@link Constants#PI_WORKDIR_TARGET_URI}<dt>
 *   <dd>Absolute URI of the file parent directory.</dd>
 *   <dt>{@link Constants#PI_PATH2PROJ_TARGET}<dt>
 *   <dd>Relative system path to the output directory, with a trailing directory separator.
 *     When the source file is in the project root directory, processing instruction has no value.</dd>
 *   <dt>{@link Constants#PI_PATH2PROJ_TARGET_URI}<dt>
 *   <dd>Relative URI to the output directory, with a trailing path separator.
 *     When the source file is in the project root directory, processing instruction has value {@code ./}.</dd>
 * </dl>
 *
 * @author Zhang, Yuan Peng
 */
public final class DitaWriterFilter extends AbstractXMLFilter {

    /** Default value map. */
    private Map<String, Map<String, String>> defaultValueMap;
    /** Absolute path to current source file. */
    private URI currentFile;
    /** Absolute path to current destination file. */
    private File outputFile;
    /** Foreign/unknown nesting level. */
    private int foreignLevel;
    /** File infos by src. */
    private Map<URI, FileInfo> fileInfoMap;


    /**
     * Set default value map.
     * @param defaultMap default value map
     */
    public void setDefaultValueMap(final Map<String, Map<String, String>> defaultMap) {
        defaultValueMap  = defaultMap;
    }

    public void setCurrentFile(final URI currentFile) {
        this.currentFile = currentFile;
    }

    public void setOutputFile(final File outputFile) {
        this.outputFile = outputFile;
    }

    @Override
    public void setJob(final Job job) {
        this.job = job;
        fileInfoMap = new HashMap<>();
        for (final FileInfo f: job.getFileInfo()) {
            fileInfoMap.put(f.src, f);
        }
    }

    // ContentHandler methods

    @Override
    public void endElement(final String uri, final String localName, final String qName)
            throws SAXException {
        if (foreignLevel > 0) {
            foreignLevel--;
        }
        getContentHandler().endElement(uri, localName, qName);
    }

    @Override
    public void startDocument() throws SAXException {
        final File path2Project = DebugAndFilterModule.getPathtoProject(getRelativePath(toFile(job.getInputDir().resolve("dummy")), toFile(currentFile)),
                toFile(currentFile),
                toFile(job.getInputFile()),
                job);
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
        getContentHandler().ignorableWhitespace(new char[]{'\n'}, 0, 1);
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName,
                             final Attributes atts) throws SAXException {
        if (foreignLevel > 0) {
            foreignLevel++;
        } else if (foreignLevel == 0) {
            final String attrValue = atts.getValue(ATTRIBUTE_NAME_CLASS);
            if (attrValue == null && !ELEMENT_NAME_DITA.equals(localName)) {
                logger.info(MessageUtils.getInstance().getMessage("DOTJ030I", localName).toString());
            }
            if (attrValue != null &&
                    (TOPIC_FOREIGN.matches(attrValue) ||
                            TOPIC_UNKNOWN.matches(attrValue))) {
                foreignLevel = 1;
            }
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
     * @throws java.io.IOException if writing to output failed
     */
    private void processAttributes(final String qName, final Attributes atts, final AttributesImpl res) {
        final int attsLen = atts.getLength();
        for (int i = 0; i < attsLen; i++) {
            final String attQName = atts.getQName(i);
            String attValue = getAttributeValue(qName, attQName, atts.getValue(i));
            if (ATTRIBUTE_NAME_CONREF.equals(attQName)) {
                attValue = replaceHREF(ATTRIBUTE_NAME_CONREF, atts).toString();
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
            }
            XMLUtils.addOrSetAttribute(res, atts.getURI(i), atts.getLocalName(i), attQName, atts.getType(i), attValue);
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
    private String getAttributeValue(final String elemQName, final String attQName, final String value) {
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
    private URI replaceHREF(final String attName, final Attributes atts){
        URI attValue = toURI(atts.getValue(attName));
        if (attValue != null) {
            final String fragment = attValue.getFragment();
            if (fragment != null) {
                attValue = stripFragment(attValue);
            }
            if (attValue.toString().length() != 0) {
                final URI current = currentFile.resolve(attValue);
                final FileInfo f = fileInfoMap.get(current);
                if (f != null) {
                    final FileInfo cfi = fileInfoMap.get(currentFile);
                    final URI currrentFileTemp = job.tempDir.toURI().resolve(cfi.uri);
                    final URI targetTemp = job.tempDir.toURI().resolve(f.uri);
                    attValue = getRelativePath(currrentFileTemp, targetTemp);
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
