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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.exception.DITAOTXMLErrorHandler;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.module.Content;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;


/**
 * TopicRefWriter which updates the linking elements' value according to the mapping table.
 * 
 * <p>TODO: Refactor to be a SAX filter.</p>
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
    private boolean needResolveEntity;
    private boolean insideCDATA;
    private String currentFilePath = null;
    private String currentFilePathName=null;
    /** XMLReader instance for parsing dita file */
    private final XMLReader reader;

    /**
     * using for rectify relative path of xml
     */
    private String fixpath= null;

    /**
     * 
     */
    public TopicRefWriter() {
        super();
        output = null;
        insideCDATA = false;

        try {
            reader = StringUtils.getXMLReader();
            reader.setContentHandler(this);
            reader.setProperty(LEXICAL_HANDLER_PROPERTY,this);
            reader.setFeature(FEATURE_NAMESPACE_PREFIX, true);
            reader.setFeature("http://apache.org/xml/features/scanner/notify-char-refs", true);
            reader.setFeature("http://apache.org/xml/features/scanner/notify-builtin-refs", true);
        } catch (final Exception e) {
            throw new RuntimeException("Failed to initialize XML parser: " + e.getMessage(), e);
        }
    }
    /**
     * Set up class.
     * @param conflictTable conflictTable
     */
    public void setup(final Hashtable<String,String> conflictTable) {
        this.conflictTable = conflictTable;
    }


    @Override
    public void startEntity(final String name) throws SAXException {
        try {
            needResolveEntity = StringUtils.checkEntity(name);
            if (!needResolveEntity) {
                output.write(StringUtils.getEntity(name));
            }
        } catch (final Exception e) {
            logger.logError(e.getMessage(), e) ;
        }

    }

    @Override
    public void processingInstruction(final String target, String data)
            throws SAXException {
        String pi;
        try {
            if (fixpath!=null&&target.equalsIgnoreCase(PI_WORKDIR_TARGET)){
                final String tmp = fixpath.substring(0,fixpath.lastIndexOf(SLASH));
                if (!data.endsWith(tmp)){
                    data = data+File.separator+tmp;
                }
            } else if (fixpath != null && target.equals(PI_WORKDIR_TARGET_URI)){
                final String tmp = fixpath.substring(0, fixpath.lastIndexOf(URI_SEPARATOR) + 1);
                if (!data.endsWith(tmp)){
                    data = data + tmp;
                }
            }
            pi = (data != null) ? target + STRING_BLANK + data
                    : target;
            output.write(LESS_THAN + QUESTION + pi
                    + QUESTION + GREATER_THAN);
        } catch (final Exception e) {
            logger.logError(e.getMessage(), e) ;
        }
    }

    @Override
    public void ignorableWhitespace(final char[] ch, final int start, final int length)
            throws SAXException {
        try {
            output.write(ch, start, length);
        } catch (final Exception e) {
            logger.logError(e.getMessage(), e) ;
        }
    }

    @Override
    public void characters(final char[] ch, final int start, final int length)
            throws SAXException {
        if (needResolveEntity) {
            try {
                if (insideCDATA) {
                    output.write(ch, start, length);
                } else {
                    output.write(StringUtils.escapeXML(ch, start, length));
                }
            } catch (final Exception e) {
                logger.logError(e.getMessage(), e) ;
            }
        }
    }

    @Override
    public void endEntity(final String name) throws SAXException {
        if (!needResolveEntity) {
            needResolveEntity = true;
        }
    }

    @Override
    public void endCDATA() throws SAXException {
        insideCDATA = false;
        try {
            output.write(CDATA_END);
        } catch (final Exception e) {
            logger.logError(e.getMessage(), e) ;
        }
    }

    @Override
    public void endDocument() throws SAXException {
        try {
            output.flush();
        } catch (final Exception e) {
            logger.logError(e.getMessage(), e) ;
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName)
            throws SAXException {
        try {
            output.write(LESS_THAN + SLASH + qName
                    + GREATER_THAN);
        } catch (final Exception e) {
            logger.logError(e.getMessage(), e) ;
        }
    }

    @Override
    public void setContent(final Content content) {
        throw new UnsupportedOperationException();
    }
    
    public void setChangeTable(final Map<String,String> changeTable) {
        this.changeTable = changeTable;
    }

    @Override
    public void startCDATA() throws SAXException {
        try {
            insideCDATA = true;
            output.write(CDATA_HEAD);
        } catch (final Exception e) {
            logger.logError(e.getMessage(), e) ;
        }
    }

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
        try{
            output.write(XML_HEAD);
            output.write(LINE_SEPARATOR);
        }catch(final IOException io){
            logger.logError(io.getMessage(), io) ;
        }
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName,
            final Attributes atts) throws SAXException {

        try {
            copyElementName(qName, atts);
            copyElementAttribute(atts);
            output.write(GREATER_THAN);
        } catch (final Exception e) {
            logger.logError(e.getMessage(), e) ;
        }// try

    }

    /**
     * @param attQName
     * @param attValue
     * @throws IOException
     */
    private void copyAttribute(final String attQName, final String attValue)
            throws IOException {
        output.write(new StringBuffer().append(STRING_BLANK).append(
                attQName).append(EQUAL).append(QUOTATION)
                .append(attValue).append(QUOTATION).toString());
    }

    /**
     * @param atts
     * @throws IOException
     */
    private void copyElementAttribute(final Attributes atts) throws IOException {
        // copy the element's attributes
        final int attsLen = atts.getLength();
        for (int i = 0; i < attsLen; i++) {
            final String attQName = atts.getQName(i);
            String attValue;

            if (ATTRIBUTE_NAME_HREF.equals(attQName)) {
                attValue = updateHref(attQName, atts);
            } else {
                attValue = atts.getValue(i);
            }
            // consider whether the attvalue needs to be escaped
            attValue = StringUtils.escapeXML(attValue);
            // output all attributes
            copyAttribute(attQName, attValue);
        }
    }

    /**
     * Check whether the attributes contains references
     * @param atts
     * @return true/false
     */
    private boolean checkDITAHREF(final Attributes atts) {

        final String classValue = atts.getValue(ATTRIBUTE_NAME_CLASS);
        String scopeValue = atts.getValue(ATTRIBUTE_NAME_SCOPE);
        String formatValue = atts.getValue(ATTRIBUTE_NAME_FORMAT);

        if (classValue == null
                || (!TOPIC_XREF.matches(classValue)
                        && !TOPIC_LINK.matches(classValue) && !MAP_TOPICREF.matches(classValue))) {
            return false;
        }

        if (scopeValue == null) {
            scopeValue = ATTR_SCOPE_VALUE_LOCAL;
        }
        if (formatValue == null) {
            formatValue = ATTR_FORMAT_VALUE_DITA;
        }

        if (scopeValue.equalsIgnoreCase(ATTR_SCOPE_VALUE_LOCAL)
                && formatValue.equalsIgnoreCase(ATTR_FORMAT_VALUE_DITA)) {
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
            attValue = FileUtils.separatorsToUnix(attValue);
        } else {
            return null;
        }

        if (fixpath!=null && attValue.startsWith(fixpath)){
            attValue = attValue.substring(fixpath.length());
        }

        if(changeTable==null || changeTable.isEmpty()) {
            return attValue;
        }

        if (checkDITAHREF(atts)) {
            // replace the href value if it's referenced topic is extracted.
            final String rootPathName=currentFilePathName;
            String changeTargetkey = FileUtils.resolveFile(currentFilePath,
                    attValue);
            String changeTarget = changeTable.get(changeTargetkey);

            final int sharpIndex = attValue.lastIndexOf(SHARP);
            if (sharpIndex != -1) {
                final int slashIndex = attValue.indexOf(SLASH,
                        sharpIndex);
                if (slashIndex != -1) {
                    changeTargetkey = changeTargetkey
                            + attValue.substring(sharpIndex, slashIndex);
                } else {
                    changeTargetkey = changeTargetkey
                            + attValue.substring(sharpIndex);
                }
                final String changeTarget_with_elemt = changeTable
                        .get(changeTargetkey);
                if (changeTarget_with_elemt != null) {
                    changeTarget = changeTarget_with_elemt;
                }
            }

            final String elementID=getElementID(attValue);
            final String pathtoElem =
                    attValue.contains(SHARP) ? attValue.substring(attValue.indexOf(SHARP)+1) : "";

                    if (StringUtils.isEmptyString(changeTarget)) {
                        String absolutePath = FileUtils.resolveTopic(currentFilePath, attValue);
                        if (absolutePath.contains(SHARP) &&
                                absolutePath.substring(absolutePath.indexOf(SHARP)).contains(SLASH)){
                            absolutePath = absolutePath.substring(0, absolutePath.indexOf(SLASH, absolutePath.indexOf(SHARP)));
                        }
                        changeTarget = changeTable.get(absolutePath);
                    }



                    if(!notTopicFormat(atts,attValue)){
                        if(changeTarget == null) {
                            return attValue;//no change
                        }else{
                            final String conTarget = conflictTable.get(removeAnchor(changeTarget));
                            if (!StringUtils.isEmptyString(conTarget)) {
                                if (elementID == null) {
                                    final String idpath = getElementID(changeTarget);
                                    return FileUtils.getRelativePath(
                                            rootPathName, conTarget) + (idpath != null ? SHARP + idpath : "");
                                }else {
                                    if (conTarget.contains(SHARP)){
                                        //conTarget points to topic
                                        if (!pathtoElem.contains(SLASH)){
                                            //if pathtoElem does no have '/' slash. it means elementID is topic id
                                            return FileUtils.getRelativePath(
                                                    rootPathName, conTarget);
                                        }else{
                                            return FileUtils.getRelativePath(
                                                    rootPathName, conTarget) + SLASH + elementID;
                                        }

                                    }else{
                                        return FileUtils.getRelativePath(
                                                rootPathName, conTarget) + SHARP + pathtoElem;
                                    }
                                }
                            } else {
                                if (elementID == null){
                                    return FileUtils.getRelativePath(
                                            rootPathName, changeTarget);
                                }else{
                                    if (changeTarget.contains(SHARP)){
                                        //changeTarget points to topic
                                        if(!pathtoElem.contains(SLASH)){
                                            //if pathtoElem does no have '/' slash. it means elementID is topic id
                                            return FileUtils.getRelativePath(
                                                    rootPathName, changeTarget);
                                        }else{
                                            return FileUtils.getRelativePath(
                                                    rootPathName, changeTarget) + SLASH + elementID;
                                        }
                                    }else{
                                        return FileUtils.getRelativePath(
                                                rootPathName, changeTarget) + SHARP + pathtoElem;
                                    }
                                }
                            }
                        }
                    }
        }
        return attValue;
    }

    private String removeAnchor(final String s) {
        if (s.lastIndexOf(SHARP) != -1) {
            return s.substring(0, s.lastIndexOf(SHARP));
        } else {
            return s;
        }
    }

    /**
     * Retrieve the element ID from the path
     * @param relativePath
     * @return String
     */
    private String getElementID(final String relativePath){
        String elementID=null;
        String topicWithelement=null;
        if(relativePath.indexOf(SHARP)!=-1){
            topicWithelement=relativePath.substring(relativePath.lastIndexOf(SHARP)+1);
            if(topicWithelement.lastIndexOf(SLASH)!=-1) {
                elementID=topicWithelement.substring(topicWithelement.lastIndexOf(SLASH)+1);
            } else {
                elementID = topicWithelement;
            }
        }
        return elementID;
    }
    /**
     * Check whether it is a local URL
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
     * @param attrs attributes to check
     * @param valueOfHref href attribute value
     * @return boolean
     */
    private boolean notTopicFormat(final Attributes attrs, final String valueOfHref) {
        final String hrefValue = valueOfHref;
        final String formatValue = attrs.getValue(ATTRIBUTE_NAME_FORMAT);
        final String extOfHref = FileUtils.getExtension(valueOfHref);
        if (notLocalURL(hrefValue)) {
            return true;
        } else {
            if (formatValue == null && extOfHref != null
                    && !extOfHref.equalsIgnoreCase("DITA")
                    && !extOfHref.equalsIgnoreCase("XML")) {
                return true;
            }
        }

        return false;
    }


    /**
     * @param qName
     * @param atts
     * @throws IOException
     */
    private void copyElementName(final String qName, final Attributes atts)
            throws IOException {
        // copy the element name
        output.write(LESS_THAN + qName);
    }



    public void write (final String tempDir, final String topicfile,final Map relativePath2fix) throws DITAOTException{
        if (relativePath2fix.containsKey(topicfile)){
            fixpath= (String)relativePath2fix.get(topicfile);
        }
        write(new File(tempDir,topicfile).getAbsolutePath());
        fixpath= null;
    }

    @Override
    public void write(final String outputFilename) throws DITAOTException {
        String filename = outputFilename;
        String file = null;
        currentFilePathName=new File(outputFilename).getAbsolutePath();
        currentFilePath = new File(outputFilename).getParent();
        File inputFile = null;
        File outputFile = null;
        FileOutputStream fileOutput = null;
        needResolveEntity=true;

        try {
            if (filename.endsWith(SHARP)) {
                // prevent the empty topic id causing error
                filename = filename.substring(0, filename.length() - 1);
            }

            if (filename.lastIndexOf(SHARP) != -1) {
                file = filename.substring(0, filename
                        .lastIndexOf(SHARP));
            } else {
                file = filename;
            }
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
                logger.logError(MessageUtils.getInstance().getMessage("DOTJ009E", inputFile.getPath(), outputFile.getPath()).toString());
            }
            if (!outputFile.renameTo(inputFile)) {
                logger.logError(MessageUtils.getInstance().getMessage("DOTJ009E", inputFile.getPath(), outputFile.getPath()).toString());
            }
        } catch (final Exception e) {
            logger.logError(e.getMessage(), e) ;
        } finally {
            if (fileOutput != null) {
                try {
                    fileOutput.close();
                } catch (final Exception e) {
                    logger.logError(e.getMessage(), e) ;
                }
            }
        }
    }

}
