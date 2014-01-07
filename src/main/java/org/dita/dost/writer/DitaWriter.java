/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.writer;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.Configuration.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.Locator;

import org.apache.xml.resolver.tools.CatalogResolver;

import org.xml.sax.helpers.AttributesImpl;

import org.apache.xerces.xni.grammars.XMLGrammarPool;

import org.dita.dost.exception.DITAOTXMLErrorHandler;
import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.module.Content;
import org.dita.dost.reader.GrammarPoolManager;
import org.dita.dost.util.CatalogUtils;
import org.dita.dost.util.Configuration;
import org.dita.dost.util.DelayConrefUtils;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.FilterUtils;
import org.dita.dost.util.KeyDef;
import org.dita.dost.util.OutputUtils;
import org.dita.dost.util.StringUtils;
import org.dita.dost.util.URLUtils;
import org.dita.dost.util.XMLUtils;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.ext.LexicalHandler;



/**
 * DitaWriter reads dita topic file and insert debug information and filter out the
 * content that is not necessary in the output.
 * 
 * <p>The following processing instructions are added before the root element:</p>
 * <dl>
 *   <dt>{@link #PI_WORKDIR_TARGET}<dt>
 *   <dd>Absolute system path of the file parent directory. On Windows, a {@code /}
 *     is added to beginning of the path.</dd>
 *   <dt>{@link #PI_WORKDIR_TARGET_URI}<dt>
 *   <dd>Absolute URI of the file parent directory.</dd>
 *   <dt>{@link #PI_PATH2PROJ_TARGET}<dt>
 *   <dd>Relative system path to the output directory, with a trailing directory separator.
 *     When the source file is in the project root directory, processing instruction has no value.</dd>
 *   <dt>{@link #PI_PATH2PROJ_TARGET_URI}<dt>
 *   <dd>Relative URI to the output directory, with a trailing path separator.
 *     When the source file is in the project root directory, processing instruction has value {@code ./}.</dd>
 * </dl>
 * 
 * <p>The following attributes are added to elements:</p>
 * <dl>
 *   <dt>{@link org.dita.dost.util.Constants#ATTRIBUTE_NAME_XTRF}</dt>
 *   <dd>Absolute system path of the source file.</dd>
 *   <dt>{@link org.dita.dost.util.Constants#ATTRIBUTE_NAME_XTRF}</dt>
 *   <dd>Element location in the document, {@code element-name ":" element-count ";" row-number ":" colum-number}.</dd>
 * </dl>
 * 
 * @author Zhang, Yuan Peng
 */
public final class DitaWriter extends AbstractXMLFilter {

    private static final String ATTRIBUTE_NAME_COLNAME = "colname";
    private static final String ATTRIBUTE_NAME_COLNUM = "colnum";
    private static final String COLUMN_NAME_COL = "col";
    public static final String PI_PATH2PROJ_TARGET = "path2project";
    public static final String PI_PATH2PROJ_TARGET_URI = "path2project-uri";
    public static final String PI_WORKDIR_TARGET = "workdir";
    public static final String PI_WORKDIR_TARGET_URI = "workdir-uri";
    /** To check the URL of href in topicref attribute */
    private static final String NOT_LOCAL_URL = COLON_DOUBLE_SLASH;
    
    /** Generate {@code xtrf} and {@code xtrc} attributes */
    private final boolean genDebugInfo;
    
    private boolean setSystemid = true;

    private boolean checkDITAHREF(final Attributes atts){
        final String classValue = atts.getValue(ATTRIBUTE_NAME_CLASS);
        String scopeValue = atts.getValue(ATTRIBUTE_NAME_SCOPE);
        String formatValue = atts.getValue(ATTRIBUTE_NAME_FORMAT);



        if (classValue == null
                || (!TOPIC_XREF.matches(classValue)
                        && !TOPIC_LINK.matches(classValue)
                        && !MAP_TOPICREF.matches(classValue))
                        && !TOPIC_LONGDESCREF.matches(classValue))
        {
            return false;
        }

        if (scopeValue == null){
            scopeValue = ATTR_SCOPE_VALUE_LOCAL;
        }
        if (formatValue == null){
            formatValue = ATTR_FORMAT_VALUE_DITA;
        }

        if (scopeValue.equalsIgnoreCase(ATTR_SCOPE_VALUE_LOCAL)
                && formatValue.equalsIgnoreCase(ATTR_FORMAT_VALUE_DITA)){
            return true;
        }

        return false;
    }

    /**
     * replace all the backslash with slash in
     * all href and conref attribute
     */
    private String replaceCONREF (final Attributes atts){
        String attValue = atts.getValue(ATTRIBUTE_NAME_CONREF);
        final int sharp_index = attValue.lastIndexOf(SHARP);
        final int dot_index = attValue.lastIndexOf(DOT);
        if(sharp_index != -1 && dot_index < sharp_index){
            final String path = attValue.substring(0, sharp_index);
            final String topic = attValue.substring(sharp_index);
            if(path.length() != 0){
                String relativePath;
                final File target = new File(path);
                if(target.isAbsolute()){
                    relativePath = FileUtils.getRelativePath(outputUtils.getInputMapPathName().getAbsolutePath(), path);
                    attValue = relativePath + topic;
                }

            }
        }else{
            final File target = new File(attValue);
            if(target.isAbsolute()){
                attValue = FileUtils.getRelativePath(outputUtils.getInputMapPathName().getAbsolutePath(), attValue);
            }
        }
        if (attValue != null && processingMode == Mode.LAX){
            attValue = FileUtils.separatorsToUnix(attValue);
        }

        if (extName != null) {
	        if(attValue.indexOf(FILE_EXTENSION_DITAMAP) == -1){
	            return FileUtils.replaceExtension(attValue, extName);
	        }
        }

        return attValue;
    }
    private static boolean notLocalURL(final String valueOfURL){
        if(valueOfURL.indexOf(NOT_LOCAL_URL)==-1) {
            return false;
        } else {
            return true;
        }
    }
    private  static boolean warnOfNoneTopicFormat(final Attributes attrs,final String valueOfHref){
        final String hrefValue = valueOfHref;
        if(notLocalURL(hrefValue)){
            return true;
        }
        else{
            final String classValue = attrs.getValue(ATTRIBUTE_NAME_CLASS);
            if(classValue != null && PR_D_CODEREF.matches(classValue)){
                return true;
            }
            final String formatValue = attrs.getValue(ATTRIBUTE_NAME_FORMAT);
            final String extOfHref = FileUtils.getExtension(valueOfHref);
            if(formatValue == null && extOfHref != null && !extOfHref.equalsIgnoreCase("DITA") && !extOfHref.equalsIgnoreCase("XML") ){
                final DITAOTLogger logger = new DITAOTJavaLogger();
                logger.logError(MessageUtils.getInstance().getMessage("DOTJ028E", hrefValue).toString());
                return true;
            }
        }

        return false;
    }
    
    /**
     * Normalize and validate href attribute.
     * 
     * @param attName attribute name
     * @param atts attributes
     * @return attribute value
     */
    private String replaceHREF (final String attName, final Attributes atts){
        if (attName == null){
            return null;
        }

        String attValue = atts.getValue(attName);
        if(attValue != null){
            final int dot_index = attValue.lastIndexOf(DOT);
            final int sharp_index = attValue.lastIndexOf(SHARP);
            if(sharp_index != -1 && dot_index < sharp_index){
                String path = attValue.substring(0, sharp_index);
                final String topic = attValue.substring(sharp_index);
                if(path.length() != 0){
                    if(path.startsWith("file:/") && path.indexOf("file://") == -1){
                        path = path.substring("file:/".length());
                        //Unix like OS
                        if(UNIX_SEPARATOR.equals(File.separator)){
                            path = UNIX_SEPARATOR + path;
                        }
                    }
                    final File target = new File(path);
                    if(target.isAbsolute()){
                        final String relativePath = FileUtils.getRelativePath(outputUtils.getInputMapPathName().getAbsolutePath(), path);
                        attValue = relativePath + topic;
                    }

                }
            }else{
                if(attValue.startsWith("file:/") && attValue.indexOf("file://") == -1){
                    attValue = attValue.substring("file:/".length());
                    //Unix like OS
                    if(UNIX_SEPARATOR.equals(File.separator)){
                        attValue = UNIX_SEPARATOR + attValue;
                    }
                }
                final File target = new File(attValue);
                if(target.isAbsolute()){
                    attValue = FileUtils.getRelativePath(outputUtils.getInputMapPathName().getAbsolutePath(), attValue);
                }
            }
        } else {
            return null;
        }

        if(checkDITAHREF(atts)){
            if(warnOfNoneTopicFormat(atts,attValue) == false){
            	if (extName != null) {
            		return FileUtils.replaceExtension(attValue, extName);
            	}
            }

        }

        return attValue;
    }
    private File absolutePath;
    private List<String> colSpec;
    private int columnNumber; // columnNumber is used to adjust column name
    private int columnNumberEnd; //columnNumberEnd is the end value for current entry
    /** Stack to store colspec list */
    private final Stack<List<String>> colSpecStack;
    /** Stack for element classes */
    private final Stack<String> classStack;
    /** Stack to store rowNum */
    private final Stack<Integer> rowNumStack;
    /** Stack to store columnNumber */
    private final Stack<Integer> columnNumberStack;
    /** Stack to store columnNumberEnd */
    private final Stack<Integer> columnNumberEndStack;
    /** Stack to store rowsMap */
    private final Stack<Map<String, Integer>> rowsMapStack;
    /** Stack to store colSpanMap */
    private final Stack<Map<String, Integer>> colSpanMapStack;

    /** Store row number */
    private int rowNumber;
    /** Store total column count */
    private int totalColumns;
    /** store morerows attribute */
    private Map<String, Integer> rowsMap;
    private Map<String, Integer> colSpanMap;
    /** Transtype */
    private String transtype;

    private Map<String, Integer> counterMap;
    private int foreignLevel; // foreign/unknown nesting level
    private String path2Project;

    private File tempDir;
    private File traceFilename;

    private Map<String, KeyDef> keys;

    private String inputFile = null;

    private Map<String, Map<String, Set<String>>> validateMap = null;
    private Map<String, Map<String, String>> defaultValueMap = null;
    /** Filter utils */
    private FilterUtils filterUtils;
    /** Delayed conref utils. */
    private DelayConrefUtils delayConrefUtils;
    /** Output utilities */
    private OutputUtils outputUtils;
    /** XMLReader instance for parsing dita file */
    private XMLReader reader = null;
    
    /**
     * Default constructor of DitaWriter class.
     * 
     * {@link #initXMLReader(File, boolean, boolean)} must be called after
     * construction to initialize XML parser.
     */
    public DitaWriter() {
        super();
        
        genDebugInfo = Boolean.parseBoolean(Configuration.configuration.get("generate-debug-attributes"));
        
        columnNumber = 1;
        columnNumberEnd = 0;
        //initialize row number
        rowNumber = 0;
        //initialize total column count
        totalColumns = 0;
        //initialize the map
        rowsMap = new HashMap<String, Integer>();
        colSpanMap = new HashMap<String, Integer>();
        absolutePath = null;
        path2Project = null;
        counterMap = null;
        traceFilename = null;
        foreignLevel = 0;
        tempDir = null;
        colSpec = null;
        //initial the stack
        classStack = new Stack<String>();
        colSpecStack = new Stack<List<String>>();
        rowNumStack = new Stack<Integer>();
        columnNumberStack = new Stack<Integer>();
        columnNumberEndStack = new Stack<Integer>();
        rowsMapStack = new Stack<Map<String,Integer>>();
        colSpanMapStack = new Stack<Map<String,Integer>>();

        validateMap = null;
    }

    /**
     * Set content filter.
     * 
     * @param filterUtils filter utils
     */
    public void setFilterUtils(final FilterUtils filterUtils) {
        this.filterUtils = filterUtils;
    }
    
    public void setDelayConrefUtils(final DelayConrefUtils delayConrefUtils) {
        this.delayConrefUtils = delayConrefUtils;
    }
    
    /**
     * Set output utilities.
     * @param outputUtils output utils
     */
    public void setOutputUtils(final OutputUtils outputUtils) {
        this.outputUtils = outputUtils;
    }
    
    /**
     * Set key definitions.
     * 
     * @param keydefs key definitions
     */
    public void setKeyDefinitions(final Collection<KeyDef> keydefs) {
    	keys = new HashMap<String, KeyDef>();
    	for (final KeyDef k: keydefs) {
    		keys.put(k.keys, k);
    	}
    }
    
    /**
     * Initialize XML reader used for pipeline parsing.
     * @param ditaDir ditaDir
     * @param validate whether validate
     * @throws SAXException SAXException
     */
    public void initXMLReader(final File ditaDir, final boolean validate, final boolean arg_setSystemid) throws SAXException {
        try {
            reader = StringUtils.getXMLReader();
            if(validate == true){
                reader.setFeature(FEATURE_VALIDATION, true);
                try {
                    reader.setFeature(FEATURE_VALIDATION_SCHEMA, true);
                } catch (final SAXNotRecognizedException e) {
                    // Not Xerces, ignore exception
                }
            }
            reader.setFeature(FEATURE_NAMESPACE, true);
            final CatalogResolver resolver = CatalogUtils.getCatalogResolver();
            setEntityResolver(resolver);
            reader.setEntityResolver(resolver);
            //setParent(reader);
        } catch (final Exception e) {
            throw new SAXException("Failed to initialize XML parser: " + e.getMessage(), e);
        }
        setGrammarPool(reader, GrammarPoolManager.getGrammarPool());
        CatalogUtils.setDitaDir(ditaDir);
        setSystemid= arg_setSystemid;
    }
    
    /**
     * Sets the grammar pool on the parser. Note that this is a Xerces-specific
     * feature.
     * @param reader
     * @param grammarPool
     */
    public void setGrammarPool(final XMLReader reader, final XMLGrammarPool grammarPool) {
        try {
            reader.setProperty("http://apache.org/xml/properties/internal/grammar-pool", grammarPool);
            logger.logInfo("Using Xerces grammar pool for DTD and schema caching.");
        } catch (final Exception e) {
            logger.logWarn("Failed to set Xerces grammar pool for parser: " + e.getMessage());
        }
    }

    /**
     * Process attributes
     * 
     * @param qName element name
     * @param atts input attributes
     * @param res attributes to write to
     * @throws IOException if writing to output failed
     */
    private void processAttributes(final String qName, final Attributes atts, final AttributesImpl res) throws IOException {
        // copy the element's attributes
        final int attsLen = atts.getLength();
        boolean conkeyrefValid = false;
        for (int i = 0; i < attsLen; i++) {
            final String attQName = atts.getQName(i);
            String attValue = getAttributeValue(qName, attQName, atts.getValue(i));
            final String nsUri = atts.getURI(i);

            if (attQName.equals(ATTRIBUTE_NAME_XTRF) || attQName.equals(ATTRIBUTE_NAME_XTRC) ||
                    attQName.equals(ATTRIBUTE_NAME_COLNAME)|| attQName.equals(ATTRIBUTE_NAME_NAMEST) || attQName.equals(ATTRIBUTE_NAME_NAMEEND) ||
                    ATTRIBUTE_NAME_CONREF.equals(attQName)) {
                continue;
            } else if(ATTRIBUTE_NAME_HREF.equals(attQName) || ATTRIBUTE_NAME_COPY_TO.equals(attQName)){
                if (atts.getValue(ATTRIBUTE_NAME_SCOPE) == null ||
                        atts.getValue(ATTRIBUTE_NAME_SCOPE).equalsIgnoreCase(ATTR_SCOPE_VALUE_LOCAL)){
                    attValue = replaceHREF(attQName, atts);
                }
                XMLUtils.addOrSetAttribute(res, attQName, attValue);
            } else if(ATTRIBUTE_NAME_CONKEYREF.equals(attQName) && attValue.length() != 0) { // replace conref with conkeyref(using key definition)
                final int sharpIndex = attValue.indexOf(SHARP);
                final int slashIndex = attValue.indexOf(SLASH);
                int keyIndex = -1;
                if(sharpIndex != -1){
                    keyIndex = sharpIndex;
                }else if(slashIndex != -1){
                    keyIndex = slashIndex;
                }
                //conkeyref only accept values such as "key" or "key/id"
                if(sharpIndex == -1){
                    if(keyIndex != -1){
                        //get keyref value
                        final String key = attValue.substring(0,keyIndex);
                        String target;
                        if(key.length() != 0 && keys.containsKey(key)){
                        	
                            //target = FileUtils.replaceExtName(target);
                            //get key's href
                            final KeyDef value = keys.get(key);
                            final String href = value.href;
                            
                            final String updatedHref = updateHref(href);

                            //get element/topic id
                            final String id = attValue.substring(keyIndex+1);

                            boolean idExported = false;
                            boolean keyrefExported = false;
                            List<Boolean> list = null;
                            if(transtype.equals(INDEX_TYPE_ECLIPSEHELP)){
                                list = delayConrefUtils.checkExport(href, id, key, tempDir);
                                idExported = list.get(0).booleanValue();
                                keyrefExported = list.get(1).booleanValue();
                            }
                            //both id and key are exported and transtype is eclipsehelp
                            if(idExported && keyrefExported
                                    && transtype.equals(INDEX_TYPE_ECLIPSEHELP)){
                                //remain the conkeyref attribute.
                                XMLUtils.addOrSetAttribute(res, ATTRIBUTE_NAME_CONKEYREF, attValue);
                            }else {
                                //normal process
                                target = updatedHref;
                                //only replace extension name of topic files.
                                target = replaceExtName(target);
                                String tail ;
                                if(sharpIndex == -1 ){
                                    if(target.indexOf(SHARP) == -1) {
                                        //change to topic id
                                        tail = attValue.substring(keyIndex).replaceAll(SLASH, SHARP);
                                    } else {
                                        //change to element id
                                        tail = attValue.substring(keyIndex);
                                    }
                                }else {
                                    //change to topic id
                                    tail = attValue.substring(keyIndex);
                                    //replace the topic id defined in the key's href
                                    if(target.indexOf(SHARP) != -1){
                                        target = target.substring(0,target.indexOf(SHARP));
                                    }
                                }
                                XMLUtils.addOrSetAttribute(res, ATTRIBUTE_NAME_CONREF, target + tail);
                                conkeyrefValid = true;
                            }
                        }else{
                            logger.logError(MessageUtils.getInstance().getMessage("DOTJ046E", attValue).toString());
                        }
                    }else{
                        //conkeyref just has keyref
                        if(keys.containsKey(attValue)){
                            //get key's href
                            final KeyDef value = keys.get(attValue);
                            final String href = value.href;

                            final String updatedHref = updateHref(href);

                            final String id = null;

                            final List<Boolean> list = delayConrefUtils.checkExport(href, id, attValue, tempDir);
                            final boolean keyrefExported = list.get(1).booleanValue();
                            //key is exported and transtype is eclipsehelp
                            if(keyrefExported && transtype.equals(INDEX_TYPE_ECLIPSEHELP)){
                                //remain the conkeyref attribute.
                                XMLUtils.addOrSetAttribute(res, ATTRIBUTE_NAME_CONKEYREF, attValue);
                            }else{
                                //e.g conref = c.xml
                                String target = updatedHref;
                                target = replaceExtName(target);
                                XMLUtils.addOrSetAttribute(res, ATTRIBUTE_NAME_CONREF, target);
                                conkeyrefValid = true;
                            }
                        }else{
                            logger.logError(MessageUtils.getInstance().getMessage("DOTJ046E", attValue).toString());
                        }
                    }
                }else{
                    //invalid conkeyref value
                    logger.logError(MessageUtils.getInstance().getMessage("DOTJ046E", attValue).toString());
                }
            } else {
                XMLUtils.addOrSetAttribute(res, nsUri, atts.getLocalName(i), attQName, atts.getType(i), attValue);
            }
        }
        String conref = atts.getValue(ATTRIBUTE_NAME_CONREF);
        if(conref != null && !conkeyrefValid){
            conref = replaceCONREF(atts);
            XMLUtils.addOrSetAttribute(res, ATTRIBUTE_NAME_CONREF, conref);
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
        if (StringUtils.isEmptyString(value) && defaultValueMap != null) {
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
     * Replace extension name for non-ditamap file.
     * 
     * @param target String
     * @return String
     */
    private String replaceExtName(String target) {
    	if (extName != null) {
	        final String fileName = FileUtils.resolveFile("", target);
	        if(FileUtils.isDITATopicFile(fileName)){
	            target = FileUtils.replaceExtension(target, extName);
	        }
    	}
        return target;
    }

    /**
     * Update href URI.
     * 
     * @param href href URI
     * @return updated href URI
     */
    private String updateHref(final String href) {
        final String filePath = new File(tempDir, inputFile).getAbsolutePath();

        final String keyValue = new File(tempDir, href).getAbsolutePath();

        final String updatedHref = FileUtils.getRelativePath(filePath, keyValue);


        //String updatedHref = null;
        /*prefix = new File(prefix).getParent();
		if(StringUtils.isEmptyString(prefix)){
			updatedHref = href;
			updatedHref = FileUtils.toUnix(updatedHref);
		}else{
			updatedHref = prefix + UNIX_SEPARATOR +href;
			updatedHref = FileUtils.toUnix(updatedHref);
		}*/

        return updatedHref;
    }

    /**
     * @param qName
     * @param atts
     * @throws IOException
     */
    private AttributesImpl copyElementName(final String qName, final Attributes atts) throws IOException {
        final AttributesImpl res = new AttributesImpl();
        final String cls = classStack.peek();
        if (TOPIC_TGROUP.matches(cls)){

            //push into the stack.
            if(colSpec != null){
                colSpecStack.push(colSpec);
                rowNumStack.push(rowNumber);
                columnNumberStack.push(columnNumber);
                columnNumberEndStack.push(columnNumberEnd);
                rowsMapStack.push(rowsMap);
                colSpanMapStack.push(colSpanMap);
            }

            columnNumber = 1; // initialize the column number
            columnNumberEnd = 0;//totally columns
            rowsMap = new HashMap<String, Integer>();
            colSpanMap = new HashMap<String, Integer>();
            //new table initialize the col list
            colSpec = new ArrayList<String>(INT_16);
            //new table initialize the col list
            rowNumber = 0;
        }else if(TOPIC_ROW.matches(cls)) {
            columnNumber = 1; // initialize the column number
            columnNumberEnd = 0;
            //store the row number
            rowNumber++;
        }else if(TOPIC_COLSPEC.matches(cls)){
            columnNumber = columnNumberEnd +1;
            if(atts.getValue(ATTRIBUTE_NAME_COLNAME) != null){
                colSpec.add(atts.getValue(ATTRIBUTE_NAME_COLNAME));
            }else{
                colSpec.add(COLUMN_NAME_COL+columnNumber);
            }
            columnNumberEnd = columnNumber;
            //change the col name of colspec
            XMLUtils.addOrSetAttribute(res, ATTRIBUTE_NAME_COLNAME, COLUMN_NAME_COL + columnNumber);
            //total columns count
            totalColumns = columnNumberEnd;
        }else if(TOPIC_ENTRY.matches(cls)){

            /*columnNumber = getStartNumber(atts, columnNumberEnd);
			if(columnNumber > columnNumberEnd){
				copyAttribute(ATTRIBUTE_NAME_COLNAME, COLUMN_NAME_COL+columnNumber);
				if (atts.getValue(ATTRIBUTE_NAME_NAMEST) != null){
					copyAttribute(ATTRIBUTE_NAME_NAMEST, COLUMN_NAME_COL+columnNumber);
				}
				if (atts.getValue(ATTRIBUTE_NAME_NAMEEND) != null){
					copyAttribute(ATTRIBUTE_NAME_NAMEEND, COLUMN_NAME_COL+getEndNumber(atts, columnNumber));
				}
			}
			columnNumberEnd = getEndNumber(atts, columnNumber);*/

            columnNumber = getStartNumber(atts, columnNumberEnd);


            if(columnNumber > columnNumberEnd){
                //The first row
                if(rowNumber == 1){
                    XMLUtils.addOrSetAttribute(res, ATTRIBUTE_NAME_COLNAME, COLUMN_NAME_COL + columnNumber);
                    if (atts.getValue(ATTRIBUTE_NAME_NAMEST) != null){
                        XMLUtils.addOrSetAttribute(res, ATTRIBUTE_NAME_NAMEST, COLUMN_NAME_COL + columnNumber);
                    }
                    if (atts.getValue(ATTRIBUTE_NAME_NAMEEND) != null){
                        XMLUtils.addOrSetAttribute(res, ATTRIBUTE_NAME_NAMEEND, COLUMN_NAME_COL + getEndNumber(atts, columnNumber));
                    }
                    //other row
                }else{
                    int offset = 0;
                    int currentCol = columnNumber;
                    while(currentCol<=totalColumns) {
                        int previous_offset = offset;
                        //search from first row
                        for(int row = 1;row<rowNumber;row++){
                            final String pos = String.valueOf(row) +"-"+ String.valueOf(currentCol);
                            if(rowsMap.containsKey(pos)){
                                //get total span rows
                                final int totalSpanRows = rowsMap.get(pos).intValue();
                                if(rowNumber <= totalSpanRows){
                                    //offset ++;
                                	offset += colSpanMap.get(pos).intValue();
                                }
                            }
                        }

                        if(offset>previous_offset) {
                            currentCol = columnNumber + offset;
                            previous_offset = offset;
                        } else {
                            break;
                        }

                    }
                    columnNumber = columnNumber+offset;
                    //if has morerows attribute
                    if(atts.getValue(ATTRIBUTE_NAME_MOREROWS) != null){
                        final String pos = String.valueOf(rowNumber) + "-" + String.valueOf(columnNumber);
                        //total span rows
                        final int total = Integer.parseInt(atts.getValue(ATTRIBUTE_NAME_MOREROWS))+
                                rowNumber;
                        rowsMap.put(pos, Integer.valueOf(total));
                        colSpanMap.put(pos, getColumnSpan(atts));

                    }

                    XMLUtils.addOrSetAttribute(res, ATTRIBUTE_NAME_COLNAME, COLUMN_NAME_COL + columnNumber);
                    if (atts.getValue(ATTRIBUTE_NAME_NAMEST) != null){
                        XMLUtils.addOrSetAttribute(res, ATTRIBUTE_NAME_NAMEST, COLUMN_NAME_COL + columnNumber);
                    }
                    if (atts.getValue(ATTRIBUTE_NAME_NAMEEND) != null){
                        XMLUtils.addOrSetAttribute(res, ATTRIBUTE_NAME_NAMEEND, COLUMN_NAME_COL + getEndNumber(atts, columnNumber));
                    }
                }
            }
            columnNumberEnd = getEndNumber(atts, columnNumber);
        }
        return res;
    }

   private int getColumnSpan(final Attributes atts) {
        if ((atts.getValue(ATTRIBUTE_NAME_NAMEST) == null)||(atts.getValue(ATTRIBUTE_NAME_NAMEEND) == null)){
            return 1;
        }else{
            final int ret = colSpec.indexOf(atts.getValue(ATTRIBUTE_NAME_NAMEEND)) - colSpec.indexOf(atts.getValue(ATTRIBUTE_NAME_NAMEST))+1;
            if(ret <= 0){
                return 1;
            }
            return ret;
        }
    }
    
    @Override
    public void endDocument() throws SAXException {
        try {
            getContentHandler().endDocument();
        } catch (final Exception e) {
            logger.logError(e.getMessage(), e) ;
        }
        classStack.clear();
    }


    @Override
    public void endElement(final String uri, final String localName, final String qName)
            throws SAXException {
        if (foreignLevel > 0){
            foreignLevel --;
        }
        getContentHandler().endElement(uri, localName, qName);
        //note the tag shouldn't be excluded by filter file(bug:2925636 )
        if(TOPIC_TGROUP.matches(classStack.peek())){
            //colSpecStack.pop();
            //rowNumStack.pop();
            //has tgroup tag
            if(!colSpecStack.isEmpty()){

                colSpec = colSpecStack.peek();
                rowNumber = rowNumStack.peek().intValue();
                columnNumber = columnNumberStack.peek().intValue();
                columnNumberEnd = columnNumberEndStack.peek().intValue();
                rowsMap = rowsMapStack.peek();
                colSpanMap = colSpanMapStack.peek();

                colSpecStack.pop();
                rowNumStack.pop();
                columnNumberStack.pop();
                columnNumberEndStack.pop();
                rowsMapStack.pop();
                colSpanMapStack.pop();

            }else{
                //no more tgroup tag
                colSpec = null;
                rowNumber = 0;
                columnNumber = 1;
                columnNumberEnd = 0;
                rowsMap = null;
                colSpanMap = null;
            }
        }
        classStack.pop();
    }

    private int getEndNumber(final Attributes atts, final int columnStart) {
        int ret;
        if (atts.getValue(ATTRIBUTE_NAME_NAMEEND) == null){
            return columnStart;
        }else{
            ret = colSpec.indexOf(atts.getValue(ATTRIBUTE_NAME_NAMEEND)) + 1;
            if(ret == 0){
                return columnStart;
            }
            return ret;
        }
    }

    private int getStartNumber(final Attributes atts, final int previousEnd) {
        if (atts.getValue(ATTRIBUTE_NAME_COLNUM) != null){
            return new Integer(atts.getValue(ATTRIBUTE_NAME_COLNUM)).intValue();
        }else if(atts.getValue(ATTRIBUTE_NAME_NAMEST) != null){
            final int ret = colSpec.indexOf(atts.getValue(ATTRIBUTE_NAME_NAMEST)) + 1;
            if(ret == 0){
                return previousEnd + 1;
            }
            return ret;
        }else if(atts.getValue(ATTRIBUTE_NAME_COLNAME) != null){
            final int ret = colSpec.indexOf(atts.getValue(ATTRIBUTE_NAME_COLNAME)) + 1;
            if(ret == 0){
                return previousEnd + 1;
            }
            return ret;
        }else{
            return previousEnd + 1;
        }
    }

    /**
     * Set temporary directory
     * 
     * @param tempDir absolute path to temporary directory
     */
    public void setTempDir(final File tempDir) {
        if (!tempDir.isAbsolute()) {
            throw new IllegalArgumentException("Temporary directory '" + tempDir.toString() + "' must be an absolute path");
        }
        this.tempDir = tempDir;
    }
    
    @Override
    public void startDocument() throws SAXException {
        try {
            getContentHandler().startDocument();
            if(OS_NAME.toLowerCase().indexOf(OS_NAME_WINDOWS)==-1)
            {
                getContentHandler().processingInstruction(PI_WORKDIR_TARGET, absolutePath.getCanonicalPath());
            }else{
                getContentHandler().processingInstruction(PI_WORKDIR_TARGET, UNIX_SEPARATOR + absolutePath.getCanonicalPath());
            }
            getContentHandler().ignorableWhitespace(new char[] { '\n' }, 0, 1);
            getContentHandler().processingInstruction(PI_WORKDIR_TARGET_URI, absolutePath.toURI().toASCIIString());
            getContentHandler().ignorableWhitespace(new char[] { '\n' }, 0, 1);
            if(path2Project != null){
                getContentHandler().processingInstruction(PI_PATH2PROJ_TARGET, path2Project);
                getContentHandler().processingInstruction(PI_PATH2PROJ_TARGET_URI, URLUtils.correct(FileUtils.separatorsToUnix(path2Project), true));
            }else{
                getContentHandler().processingInstruction(PI_PATH2PROJ_TARGET, "");
                getContentHandler().processingInstruction(PI_PATH2PROJ_TARGET_URI, "." + UNIX_SEPARATOR);
            }
            getContentHandler().ignorableWhitespace(new char[] { '\n' }, 0, 1);
        } catch (final Exception e) {
            e.printStackTrace();
            logger.logError(e.getMessage(), e) ;
        }
    }


    @Override
    public void startElement(final String uri, final String localName, final String qName,
            final Attributes atts) throws SAXException {
        classStack.push(atts.getValue(ATTRIBUTE_NAME_CLASS));
        if (foreignLevel > 0){
            foreignLevel ++;
        }else if( foreignLevel == 0){
            final String attrValue = atts.getValue(ATTRIBUTE_NAME_CLASS);
            if(attrValue == null && !ELEMENT_NAME_DITA.equals(localName)){
                logger.logInfo(MessageUtils.getInstance().getMessage("DOTJ030I", localName).toString());
            }
            if (attrValue != null &&
                    (TOPIC_FOREIGN.matches(attrValue) ||
                            TOPIC_UNKNOWN.matches(attrValue))){
                foreignLevel = 1;
            }
        }

        Integer value;
        Integer nextValue;
        if (counterMap.containsKey(qName)) {
            value = counterMap.get(qName);
            nextValue = value + 1;
        } else {
            nextValue = 1;
        }
        counterMap.put(qName, nextValue);

        try {
            final AttributesImpl res = copyElementName(qName, atts);
            processAttributes(qName, atts, res);
            if (foreignLevel <= 1){
                if (genDebugInfo) {
                    XMLUtils.addOrSetAttribute(res, ATTRIBUTE_NAME_XTRF, traceFilename.getAbsolutePath());
                    final StringBuilder xtrc = new StringBuilder(qName).append(COLON).append(nextValue.toString());
                    if (locator != null) {                                
                        xtrc.append(';')
                            .append(Integer.toString(locator.getLineNumber()))
                            .append(COLON)
                            .append(Integer.toString(locator.getColumnNumber()));
                    }
                    XMLUtils.addOrSetAttribute(res, ATTRIBUTE_NAME_XTRC, xtrc.toString());
                }
            }
            
            getContentHandler().startElement(uri, localName, qName, res);
        } catch (final Exception e) {
            logger.logError(e.getMessage(), e) ;
        }
    }
    
	@Override
	public void ignorableWhitespace(final char[] ch, final int start, final int length) throws SAXException {
        getContentHandler().characters(ch, start, length);
	}
    
    /**
     * Write output
     * 
     * @param baseDir absolute base directory path
     * @param inFile relative file path
     */
    public void write(final File baseDir, final String inFile) {
        inputFile = inFile;

        OutputStream out = null;
        try {
            traceFilename = new File(baseDir, inputFile);
            File outputFile = new File(tempDir, inputFile);
            if (extName != null) {
	            if (FileUtils.isDITAMapFile(inputFile.toLowerCase())) {
	                outputFile = new File(tempDir, inputFile);
	            } else {
	                outputFile = new File(tempDir, FileUtils.replaceExtension(inputFile, extName));
	            }
            }

            path2Project = getPathtoProject(inputFile, traceFilename, outputUtils.getInputMapPathName().getAbsolutePath());            
            counterMap = new HashMap<String, Integer>();
            final File dirFile = outputFile.getParentFile();
            if (!dirFile.exists()) {
                dirFile.mkdirs();
            }
            absolutePath = dirFile;
            out = new FileOutputStream(outputFile);

            // start to parse the file and direct to output in the temp
            // directory
            reader.setErrorHandler(new DITAOTXMLErrorHandler(traceFilename.getAbsolutePath(), logger));
            final InputSource is = new InputSource(traceFilename.toURI().toASCIIString());
            if(setSystemid) {
                //is.setSystemId(URLUtil.correct(file).toString());
                is.setSystemId(traceFilename.toURI().toASCIIString());
            }

            // ContentHandler must be reset so e.g. Saxon 9.1 will reassign ContentHandler
            // when reusing filter with multiple Transformers.
            setContentHandler(null);
            
            final TransformerFactory tf = TransformerFactory.newInstance();
            final Transformer serializer = tf.newTransformer();
            XMLReader xmlSource = reader;
            if (filterUtils != null) {
                final ProfilingFilter profilingFilter = new ProfilingFilter();
                profilingFilter.setLogger(logger);
                profilingFilter.setFilterUtils(filterUtils);
                profilingFilter.setTranstype(transtype);
                profilingFilter.setParent(xmlSource);
                profilingFilter.setEntityResolver(xmlSource.getEntityResolver());
                xmlSource = profilingFilter;
            }
            {
				final ValidationFilter validationFilter = new ValidationFilter();
				validationFilter.setLogger(logger);
				validationFilter.setParent(xmlSource);
				validationFilter.setEntityResolver(xmlSource.getEntityResolver());
				validationFilter.setValidateMap(validateMap);
				xmlSource = validationFilter;
            }
            {
	        	this.setParent(xmlSource);
	        	xmlSource = this;
            }
            final Source source = new SAXSource(xmlSource, is);
            final Result result = new StreamResult(out);
            serializer.transform(source, result);
        } catch (final Exception e) {
            e.printStackTrace();
            logger.logError(e.getMessage(), e) ;
        }finally {
            if (out != null) {
                try {
                    out.close();
                }catch (final Exception e) {
                    logger.logError(e.getMessage(), e) ;
                }
            }
        }
    }

    /**
     * Get path to base directory
     * 
     * @param filename relative input file path from base directory
     * @param traceFilename absolute input file
     * @param inputMap absolute path to start file
     * @return path to base directory, {@code null} if not available
     */
    public String getPathtoProject (final String filename, final File traceFilename, final String inputMap) {
    	String path2Project = null;
    	if(OutputUtils.getGeneratecopyouter() != OutputUtils.Generate.OLDSOLUTION){
            if(isOutFile(traceFilename)){
                
                path2Project = getRelativePathFromOut(traceFilename.getAbsolutePath());
            }else{
                 path2Project = FileUtils.getRelativePath(traceFilename.getAbsolutePath(),inputMap);
                path2Project = new File(path2Project).getParent();
                if(path2Project != null && path2Project.length()>0){
                    path2Project = path2Project+File.separator;
                }
            }
        } else {
            path2Project = FileUtils.getRelativePath(filename);
        }
    	 return path2Project;
    }
    /**
     * Just for the overflowing files.
     * @param overflowingFile overflowingFile
     * @return relative path to out
     */
    public String getRelativePathFromOut(final String overflowingFile){
        final File mapPathName = outputUtils.getInputMapPathName();
        final File currFilePathName = new File(overflowingFile);
        final String relativePath = FileUtils.getRelativePath( mapPathName.toString(),currFilePathName.toString());
        final String outputDir = OutputUtils.getOutputDir().getAbsolutePath();
        final StringBuffer outputPathName = new StringBuffer(outputDir).append(File.separator).append("index.html");
        final String finalOutFilePathName = FileUtils.resolveFile(outputDir,relativePath);
        final String finalRelativePathName = FileUtils.getRelativePath(finalOutFilePathName,outputPathName.toString());
        final String parentDir = new File(finalRelativePathName).getParent();
        final StringBuffer finalRelativePath = new StringBuffer(parentDir);
        if(finalRelativePath.length() > 0){
            finalRelativePath.append(File.separator);
        }else{
            finalRelativePath.append(".").append(File.separator);
        }
        return finalRelativePath.toString();
    }

    /**
     * Check if path falls outside start document directory
     * 
     * @param filePathName path to test
     * @return {@code true} if outside start directory, otherwise {@code false}
     */
    private boolean isOutFile(final File filePathName){
        final String relativePath = FileUtils.getRelativePath(outputUtils.getInputMapPathName().getAbsolutePath(), filePathName.getPath());
        if(relativePath == null || relativePath.length() == 0 || !relativePath.startsWith("..")){
            return false;
        }
        return true;
    }

    /**
     * @return the validateMap
     */
    public Map<String, Map<String, Set<String>>> getValidateMap() {
        return validateMap;
    }

    /**
     * @param validateMap the validateMap to set
     */
    public void setValidateMap(final Map<String, Map<String, Set<String>>> validateMap) {
        this.validateMap = validateMap;
    }
    /**
     * Set default value map.
     * @param defaultMap default value map
     */
    public void setDefaultValueMap(final Map<String, Map<String, String>> defaultMap) {
        defaultValueMap  = defaultMap;
    }

    /**
     * Get transtype.
     * @return the transtype
     */
    public String getTranstype() {
        return transtype;
    }

    /**
     * Set transtype.
     * @param transtype the transtype to set
     */
    public void setTranstype(final String transtype) {
        this.transtype = transtype;
    }

    private String extName;
    /**
     * Get extension name.
     * @return extension name
     */
    public String getExtName() {
        return extName;
    }
    /**
     * Set extension name.
     * @param extName extension name
     */
    public void setExtName(final String extName) {
        this.extName = extName;
    }
    
    @Override
    public void setContent(final Content content) {
        throw new UnsupportedOperationException();
    }

    // Locator methods
    
    private Locator locator;
    @Override
    public void setDocumentLocator(final Locator locator) {
        this.locator = locator;
        getContentHandler().setDocumentLocator(locator);
    }
    
    // LexicalHandler methods
    
    @Override
    public void setProperty(final String name, final Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
        if (getParent().getClass().getName().equals(SAX_DRIVER_DEFAULT_CLASS) && name.equals(LEXICAL_HANDLER_PROPERTY)) {
            getParent().setProperty(name, new XercesFixLexicalHandler((LexicalHandler) value));
        } else {
            getParent().setProperty(name, value);
        }
    }
    
    /**
     * LexicalHandler implementation to work around Xerces bug. When source document root contains
     * 
     * <pre>&lt;!--AAA-->
&lt;!--BBBbbbBBB-->
&lt;!--CCCCCC--></pre>
     *
     * the output will be
     * 
     * <pre>&lt;!--CCC-->
&lt;!--CCCCCCBBB-->
&lt;!--CCCCCC--></pre>
     *
     * This implementation makes a copy of the comment data array and passes the copy forward.
     * 
     * @since 1.6
     */
    private static final class XercesFixLexicalHandler implements LexicalHandler {

        private final LexicalHandler lexicalHandler;
        
        XercesFixLexicalHandler(final LexicalHandler lexicalHandler) {
            this.lexicalHandler = lexicalHandler;
        }
        
        @Override
        public void comment(final char[] arg0, final int arg1, final int arg2) throws SAXException {
            final char[] buf = new char[arg2];
            System.arraycopy(arg0, arg1, buf, 0, arg2);
            lexicalHandler.comment(buf, 0, arg2);
        }
    
        @Override
        public void endCDATA() throws SAXException {
            lexicalHandler.endCDATA();
        }
    
        @Override
        public void endDTD() throws SAXException {
            lexicalHandler.endDTD();
        }
    
        @Override
        public void endEntity(final String arg0) throws SAXException {
            lexicalHandler.endEntity(arg0);
        }
    
        @Override
        public void startCDATA() throws SAXException {
            lexicalHandler.startCDATA();
        }
    
        @Override
        public void startDTD(final String arg0, final String arg1, final String arg2) throws SAXException {
            lexicalHandler.startDTD(arg0, arg1, arg2);
        }
    
        @Override
        public void startEntity(final String arg0) throws SAXException {
            lexicalHandler.startEntity(arg0);
        }
    
    }
    
}
