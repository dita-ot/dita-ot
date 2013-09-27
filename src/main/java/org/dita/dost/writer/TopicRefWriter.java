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
import java.util.Hashtable;
import java.util.Map;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.exception.DITAOTXMLErrorHandler;
import org.dita.dost.log.MessageUtils;
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

    // To check the URL of href in topicref attribute
    private static final String NOT_LOCAL_URL = COLON_DOUBLE_SLASH;

    private Map<String, String> changeTable = null;
    private Hashtable<String, String> conflictTable = null;
    private OutputStreamWriter output;
    private OutputStreamWriter ditaFileOutput;
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
    public void setup(final Hashtable<String, String> conflictTable) {
        this.conflictTable = conflictTable;
    }

    @Override
    public void processingInstruction(final String target, String data) throws SAXException {
        String pi;
        try {
            if (fixpath != null && target.equalsIgnoreCase(PI_WORKDIR_TARGET)) {
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
            logger.logError(e.getMessage(), e);
        }
    }

    @Override
    public void ignorableWhitespace(final char[] ch, final int start, final int length) throws SAXException {
        try {
            writeCharacters(ch, start, length);
        } catch (final IOException e) {
            logger.logError(e.getMessage(), e);
        }
    }

    @Override
    public void characters(final char[] ch, final int start, final int length) throws SAXException {
        try {
            writeCharacters(ch, start, length);
        } catch (final Exception e) {
            logger.logError(e.getMessage(), e);
        }
    }

    @Override
    public void endDocument() throws SAXException {
        try {
            output.flush();
        } catch (final Exception e) {
            logger.logError(e.getMessage(), e);
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        try {
            writeEndElement(qName);
        } catch (final Exception e) {
            logger.logError(e.getMessage(), e);
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
            logger.logError(io.getMessage(), io);
        }
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes atts)
            throws SAXException {
        try {
            final AttributesImpl res = new AttributesImpl();
            final int attsLen = atts.getLength();
            for (int i = 0; i < attsLen; i++) {
                final String attQName = atts.getQName(i);
                String attValue;
                if (ATTRIBUTE_NAME_HREF.equals(attQName)) {
                    attValue = updateHref(attQName, atts);
                } else {
                    attValue = atts.getValue(i);
                }
                addOrSetAttribute(res, attQName, attValue);
            }
            writeStartElement(qName, res);
        } catch (final Exception e) {
            logger.logError(e.getMessage(), e);
        }
    }

    /**
     * Check whether the attributes contains references
     * 
     * @param atts
     * @return true/false
     */
    private boolean checkDITAHREF(final Attributes atts) {

        final String classValue = atts.getValue(ATTRIBUTE_NAME_CLASS);
        String scopeValue = atts.getValue(ATTRIBUTE_NAME_SCOPE);
        String formatValue = atts.getValue(ATTRIBUTE_NAME_FORMAT);

        if (classValue == null
                || (!TOPIC_XREF.matches(classValue) && !TOPIC_LINK.matches(classValue) && !MAP_TOPICREF
                        .matches(classValue))) {
            return false;
        }

        if (scopeValue == null) {
            scopeValue = ATTR_SCOPE_VALUE_LOCAL;
        }
        if (formatValue == null) {
            formatValue = ATTR_FORMAT_VALUE_DITA;
        }

        if (scopeValue.equalsIgnoreCase(ATTR_SCOPE_VALUE_LOCAL) && formatValue.equalsIgnoreCase(ATTR_FORMAT_VALUE_DITA)) {
            return true;
        }

        return false;
    }

    private String updateHref(final String attQName, final Attributes atts) {
        String attValue = null;

        if (attQName == null) {
            return null;
        }

        attValue = atts.getValue(attQName);

        if (attValue != null) {
            /*
             * replace all the backslash with slash in all href and conref
             * attribute
             */
            attValue = separatorsToUnix(attValue);
        } else {
            return null;
        }

        if (fixpath != null && attValue.startsWith(fixpath)) {
            attValue = attValue.substring(fixpath.length());
        }

        if (changeTable == null || changeTable.isEmpty()) {
            return attValue;
        }

        if (checkDITAHREF(atts)) {
            // replace the href value if it's referenced topic is extracted.
            final File rootPathName = currentFilePathName;
            String changeTargetkey = resolveFile(currentFilePath, attValue).getPath();
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

            if (StringUtils.isEmptyString(changeTarget)) {
                String absolutePath = resolveTopic(currentFilePath, attValue);
                absolutePath = setElementID(absolutePath, null);
                changeTarget = changeTable.get(absolutePath);
            }

            if (!notTopicFormat(atts, attValue)) {
                if (changeTarget == null) {
                    return attValue;// no change
                } else {
                    final String conTarget = conflictTable.get(stripFragment(changeTarget));
                    if (!StringUtils.isEmptyString(conTarget)) {
                        if (elementID == null) {
                            final String idpath = getElementID(changeTarget);
                            return setFragment(getRelativeUnixPath(rootPathName, conTarget), idpath);
                        } else {
                            if (getFragment(conTarget) != null) {
                                // conTarget points to topic
                                if (!pathtoElem.contains(SLASH)) {
                                    // if pathtoElem does no have '/' slash. it
                                    // means elementID is topic id
                                    return getRelativeUnixPath(rootPathName, conTarget);
                                } else {
                                    return setElementID(getRelativeUnixPath(rootPathName, conTarget), elementID);
                                }

                            } else {
                                return setFragment(getRelativeUnixPath(rootPathName, conTarget), pathtoElem);
                            }
                        }
                    } else {
                        if (elementID == null) {
                            return getRelativeUnixPath(rootPathName, changeTarget);
                        } else {
                            if (getFragment(changeTarget) != null) {
                                // changeTarget points to topic
                                if (!pathtoElem.contains(SLASH)) {
                                    // if pathtoElem does no have '/' slash. it
                                    // means elementID is topic id
                                    return getRelativeUnixPath(rootPathName, changeTarget);
                                } else {
                                    return setElementID(getRelativeUnixPath(rootPathName, changeTarget), elementID);
                                }
                            } else {
                                return setFragment(getRelativeUnixPath(rootPathName, changeTarget), pathtoElem);
                            }
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

    /**
     * Check whether it is a local URL
     * 
     * @param valueOfURL
     * @return boolean
     */
    private boolean notLocalURL(final String valueOfURL) {
        if (valueOfURL.indexOf(NOT_LOCAL_URL) == -1) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Check whether it is a Topic format
     * 
     * @param attrs attributes to check
     * @param valueOfHref href attribute value
     * @return boolean
     */
    private boolean notTopicFormat(final Attributes attrs, final String valueOfHref) {
        final String hrefValue = valueOfHref;
        final String formatValue = attrs.getValue(ATTRIBUTE_NAME_FORMAT);
        final String extOfHref = getExtension(valueOfHref);
        if (notLocalURL(hrefValue)) {
            return true;
        } else {
            if (formatValue == null && extOfHref != null && !extOfHref.equalsIgnoreCase("DITA")
                    && !extOfHref.equalsIgnoreCase("XML")) {
                return true;
            }
        }

        return false;
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
        String filename = outputFilename.getPath();
        String file = null;
        currentFilePathName = outputFilename.getAbsoluteFile();
        currentFilePath = outputFilename.getParentFile();
        File inputFile = null;
        File outputFile = null;
        FileOutputStream fileOutput = null;

        try {
            file = stripFragment(filename);
            inputFile = new File(file);
            if (!inputFile.exists()) {
                logger.logError(MessageUtils.getInstance().getMessage("DOTX008E", file).toString());
                return;
            }
            outputFile = new File(file + FILE_EXTENSION_TEMP);
            fileOutput = new FileOutputStream(outputFile);
            ditaFileOutput = new OutputStreamWriter(fileOutput, UTF8);
            output = ditaFileOutput;
            reader.setErrorHandler(new DITAOTXMLErrorHandler(file, logger));
            reader.parse(inputFile.toURI().toString());

            output.close();
            if (!inputFile.delete()) {
                logger.logError(MessageUtils.getInstance()
                        .getMessage("DOTJ009E", inputFile.getPath(), outputFile.getPath()).toString());
            }
            if (!outputFile.renameTo(inputFile)) {
                logger.logError(MessageUtils.getInstance()
                        .getMessage("DOTJ009E", inputFile.getPath(), outputFile.getPath()).toString());
            }
        } catch (final Exception e) {
            logger.logError(e.getMessage(), e);
        } finally {
            if (fileOutput != null) {
                try {
                    fileOutput.close();
                } catch (final Exception e) {
                    logger.logError(e.getMessage(), e);
                }
            }
        }
    }

    // SAX serializer methods
    
    private void writeStartElement(final String qName, final Attributes atts) throws IOException {
        final int attsLen = atts.getLength();
        output.write(LESS_THAN + qName);
        for (int i = 0; i < attsLen; i++) {
            final String attQName = atts.getQName(i);
            final String attValue = StringUtils.escapeXML(atts.getValue(i));
            output.write(new StringBuffer().append(STRING_BLANK)
                    .append(attQName).append(EQUAL).append(QUOTATION)
                    .append(attValue).append(QUOTATION).toString());
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
