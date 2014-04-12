/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2007 All Rights Reserved.
 */
package org.dita.dost.writer;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.writer.DitaWriter.*;
import static org.dita.dost.util.FileUtils.*;
import static org.dita.dost.util.XMLUtils.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Map;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.exception.DITAOTXMLErrorHandler;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

/**
 * TopicRefWriter which updates the linking elements' value according to the
 * mapping table.
 * 
 * <p>
 * TODO: Refactor to be a SAX filter.
 * </p>
 * 
 * @author wxzhang
 * 
 */
public final class TopicRefWriter extends AbstractXMLWriter {

    private Map<String, String> changeTable = null;
    private Map<String, String> conflictTable = null;
    private OutputStreamWriter output;
    private File currentFilePath = null;
    private File currentFilePathName = null;
    /** XMLReader instance for parsing dita file */
    private final XMLReader reader;

    /**
     * using for rectify relative path of xml
     */
    private String fixpath = null;

    /**
     * 
     */
    public TopicRefWriter() {
        super();
        output = null;

        try {
            reader = StringUtils.getXMLReader();
            reader.setContentHandler(this);
            reader.setProperty(LEXICAL_HANDLER_PROPERTY, this);
            reader.setFeature(FEATURE_NAMESPACE_PREFIX, true);
        } catch (final Exception e) {
            throw new RuntimeException("Failed to initialize XML parser: " + e.getMessage(), e);
        }
    }

    /**
     * Set up class.
     * 
     * @param conflictTable conflictTable
     */
    public void setup(final Map<String, String> conflictTable) {
        this.conflictTable = conflictTable;
    }

    @Override
    public void processingInstruction(final String target, String data) throws SAXException {
        try {
            if (fixpath != null && target.equals(PI_WORKDIR_TARGET)) {
                final String tmp = fixpath.substring(0, fixpath.lastIndexOf(SLASH));
                if (!data.endsWith(tmp)) {
                    data = data + File.separator + tmp;
                }
            } else if (fixpath != null && target.equals(PI_WORKDIR_TARGET_URI)) {
                final String tmp = fixpath.substring(0, fixpath.lastIndexOf(URI_SEPARATOR) + 1);
                if (!data.endsWith(tmp)) {
                    data = data + tmp;
                }
            }
            writeProcessingInstruction(target, data);
        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void ignorableWhitespace(final char[] ch, final int start, final int length) throws SAXException {
        try {
            writeCharacters(ch, start, length);
        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void characters(final char[] ch, final int start, final int length) throws SAXException {
        try {
            writeCharacters(ch, start, length);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void endDocument() throws SAXException {
        try {
            output.flush();
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        try {
            writeEndElement(qName);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void setChangeTable(final Map<String, String> changeTable) {
        this.changeTable = changeTable;
    }

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
        try {
            output.write(XML_HEAD);
            output.write(LINE_SEPARATOR);
        } catch (final IOException io) {
            logger.error(io.getMessage(), io);
        }
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes atts)
            throws SAXException {
        try {
            Attributes as = atts;
            final String href = atts.getValue(ATTRIBUTE_NAME_HREF);
            if (href != null) {
                final AttributesImpl res = new AttributesImpl(atts);
                addOrSetAttribute(res, ATTRIBUTE_NAME_HREF, updateHref(as));
                as = res;
            }
            writeStartElement(qName, as);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Check whether the attributes contains references
     * 
     * @param atts
     * @return true/false
     */
    private boolean isLocalDita(final Attributes atts) {
        final String classValue = atts.getValue(ATTRIBUTE_NAME_CLASS);
        if (classValue == null
                || (!TOPIC_XREF.matches(classValue) && !TOPIC_LINK.matches(classValue) && !MAP_TOPICREF.matches(classValue))) {
            return false;
        }

        String scopeValue = atts.getValue(ATTRIBUTE_NAME_SCOPE);
        if (scopeValue == null) {
            scopeValue = ATTR_SCOPE_VALUE_LOCAL;
        }
        String formatValue = atts.getValue(ATTRIBUTE_NAME_FORMAT);
        if (formatValue == null) {
            formatValue = ATTR_FORMAT_VALUE_DITA;
        }

        return scopeValue.equals(ATTR_SCOPE_VALUE_LOCAL) && formatValue.equals(ATTR_FORMAT_VALUE_DITA);

    }

    private String updateHref(final Attributes atts) {
        String attValue = atts.getValue(ATTRIBUTE_NAME_HREF);
        if (attValue == null) {
            return null;
        }
        if (fixpath != null && attValue.startsWith(fixpath)) {
            attValue = attValue.substring(fixpath.length());
        }

        if (changeTable == null || changeTable.isEmpty()) {
            return attValue;
        }

        if (isLocalDita(atts)) {
            // replace the href value if it's referenced topic is extracted.
            final File rootPathName = currentFilePathName;
            String changeTargetkey = resolve(currentFilePath, attValue).getPath();
            String changeTarget = changeTable.get(changeTargetkey);

            final String topicID = getTopicID(attValue);
            if (topicID != null) {
                changeTargetkey = setFragment(changeTargetkey, topicID);
                final String changeTarget_with_elemt = changeTable.get(changeTargetkey);
                if (changeTarget_with_elemt != null) {
                    changeTarget = changeTarget_with_elemt;
                }
            }

            final String elementID = getElementID(attValue);
            final String pathtoElem = getFragment(attValue, "");

            if (changeTarget == null || changeTarget.isEmpty()) {
                String absolutePath = resolveTopic(currentFilePath, attValue);
                absolutePath = setElementID(absolutePath, null);
                changeTarget = changeTable.get(absolutePath);
            }

            if (changeTarget == null) {
                return attValue;// no change
            } else {
                final String conTarget = conflictTable.get(stripFragment(changeTarget));
                if (conTarget != null && !conTarget.isEmpty()) {
                    final String p = getRelativeUnixPath(rootPathName, conTarget);
                    if (elementID == null) {
                        return setFragment(p, getElementID(changeTarget));
                    } else {
                        if (getFragment(conTarget) != null) {
                            if (!pathtoElem.contains(SLASH)) {
                                return p;
                            } else {
                                return setElementID(p, elementID);
                            }

                        } else {
                            return setFragment(p, pathtoElem);
                        }
                    }
                } else {
                    final String p = getRelativeUnixPath(rootPathName, changeTarget);
                    if (elementID == null) {
                        return p;
                    } else {
                        if (getFragment(changeTarget) != null) {
                            if (!pathtoElem.contains(SLASH)) {
                                return p;
                            } else {
                                return setElementID(p, elementID);
                            }
                        } else {
                            return setFragment(p, pathtoElem);
                        }
                    }
                }
            }
        }
        return attValue;
    }

    /**
     * Retrieve the element ID from the path. If there is no element ID, return topic ID.
     * 
     * @param relativePath
     * @return String
     */
    private String getElementID(final String relativePath) {
        String elementID = null;
        String topicWithelement = null;
        final String fragment = getFragment(relativePath);
        if (fragment != null) {
            topicWithelement = getFragment(relativePath);
            if (topicWithelement.lastIndexOf(SLASH) != -1) {
                elementID = topicWithelement.substring(topicWithelement.lastIndexOf(SLASH) + 1);
            } else {
                elementID = topicWithelement;
            }
        }
        return elementID;
    }

    public void write(final File tempDir, final File topicfile, final Map relativePath2fix) throws DITAOTException {
        if (relativePath2fix.containsKey(topicfile)) {
            fixpath = (String) relativePath2fix.get(topicfile);
        }
        write(new File(tempDir, topicfile.getPath()).getAbsoluteFile());
        fixpath = null;
    }

    @Override
    public void write(final File outputFilename) throws DITAOTException {
        currentFilePathName = outputFilename.getAbsoluteFile();
        currentFilePath = outputFilename.getParentFile();
        final File inputFile = new File(stripFragment(outputFilename.getPath()));
        if (!inputFile.exists()) {
            logger.error(MessageUtils.getInstance().getMessage("DOTX008E", inputFile.getPath()).toString());
            return;
        }
        final File outputFile = new File(inputFile.getPath() + FILE_EXTENSION_TEMP);

        try {
            
            output = new OutputStreamWriter(new FileOutputStream(outputFile), UTF8);
            reader.setErrorHandler(new DITAOTXMLErrorHandler(inputFile.getPath(), logger));
            reader.parse(inputFile.toURI().toString());
        } catch (final Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (final Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        try {
            FileUtils.moveFile(outputFile, inputFile);
        } catch (final Exception e) {
            logger.error("Failed to replace " + inputFile + ": " + e.getMessage());
        }
    }

    // SAX serializer methods
    
    private void writeStartElement(final String qName, final Attributes atts) throws IOException {
        final int attsLen = atts.getLength();
        output.write(LESS_THAN + qName);
        for (int i = 0; i < attsLen; i++) {
            final String attQName = atts.getQName(i);
            final String attValue = StringUtils.escapeXML(atts.getValue(i));
            output.write(STRING_BLANK + attQName + EQUAL + QUOTATION + attValue + QUOTATION);
        }
        output.write(GREATER_THAN);
    }
    
    private void writeEndElement(final String qName) throws IOException {
        output.write(LESS_THAN + SLASH + qName + GREATER_THAN);
    }
    
    private void writeCharacters(final char[] ch, final int start, final int length) throws IOException {
        output.write(StringUtils.escapeXML(ch, start, length));
    }

    private void writeProcessingInstruction(final String target, final String data) throws IOException {
        final String pi = data != null ? target + STRING_BLANK + data : target;
        output.write(LESS_THAN + QUESTION + pi + QUESTION + GREATER_THAN);
    }
    
}
