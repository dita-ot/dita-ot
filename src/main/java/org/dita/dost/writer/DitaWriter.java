/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.writer;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.dita.dost.exception.DITAOTXMLErrorHandler;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.reader.GrammarPoolManager;
import org.dita.dost.util.CatalogUtils;
import org.dita.dost.util.Configuration;
import org.dita.dost.util.DelayConrefUtils;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.FilterUtils;
import org.dita.dost.util.Job;
import org.dita.dost.util.KeyDef;
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
    
    /** Generate {@code xtrf} and {@code xtrc} attributes */
    private final boolean genDebugInfo;
    
    private boolean setSystemid = true;

    /**
     * replace all the backslash with slash in
     * all href and conref attribute
     */
    private URI replaceCONREF(final Attributes atts){
        URI attValue = toURI(atts.getValue(ATTRIBUTE_NAME_CONREF));
        final String fragment = attValue.getFragment();
        if(fragment != null){
            final URI path = stripFragment(attValue);
            if(path.toString().length() != 0){
                final File target = toFile(path);
                if(target.isAbsolute()){
                    final URI relativePath = getRelativePath(job.getInputFile().toURI(), path);
                    attValue = setFragment(relativePath, fragment);
                }
            }
        }else{
            final File target = toFile(attValue);
            if(target.isAbsolute()){
                attValue = getRelativePath(job.getInputFile().toURI(), attValue);
            }
        }

        return attValue;
    }
    
    /**
     * Normalize and validate href attribute.
     * 
     * @param attName attribute name
     * @param atts attributes
     * @return attribute value
     */
    private URI replaceHREF(final String attName, final Attributes atts){
        if (attName == null){
            return null;
        }

        URI attValue = toURI(atts.getValue(attName));
        if(attValue != null){
            final String fragment = attValue.getFragment();
            if(fragment != null){
                URI path = stripFragment(attValue);
                if(path.toString().length() != 0){
                    final File target = toFile(path);
                    if(target.isAbsolute()){
                        final URI relativePath = getRelativePath(job.getInputFile().toURI(), path);
                        attValue = setFragment(relativePath, fragment);
                    }
                }
            }else{
                final File target = toFile(attValue);
                if(target.isAbsolute()){
                    attValue = getRelativePath(job.getInputFile().toURI(), attValue);
                }
            }
        } else {
            return null;
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

    private File inputFile = null;

    private Map<String, Map<String, Set<String>>> validateMap = null;
    private Map<String, Map<String, String>> defaultValueMap = null;
    /** Filter utils */
    private FilterUtils filterUtils;
    /** Delayed conref utils. */
    private DelayConrefUtils delayConrefUtils;
    /** Output utilities */
    private Job job;
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
     * @param job output utils
     */
    public void setJob(final Job job) {
        this.job = job;
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
        CatalogUtils.setDitaDir(ditaDir);
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
        setGrammarPool(reader);
        setSystemid= arg_setSystemid;
    }
    
    /**
     * Sets the grammar pool on the parser. Note that this is a Xerces-specific
     * feature.
     * @param reader
     * @param grammarPool
     */
    public void setGrammarPool(final XMLReader reader) {
        try {
            reader.setProperty("http://apache.org/xml/properties/internal/grammar-pool", GrammarPoolManager.getGrammarPool());
            logger.logInfo("Using Xerces grammar pool for DTD and schema caching.");
        } catch (final NoClassDefFoundError e) {
            logger.logDebug("Xerces not available, not using grammar caching");
        } catch (final SAXNotRecognizedException e) {
            e.printStackTrace();
            logger.logWarn("Failed to set Xerces grammar pool for parser: " + e.getMessage());
        } catch (final SAXNotSupportedException e) {
            e.printStackTrace();
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
            if (attQName.equals(ATTRIBUTE_NAME_XTRF) || attQName.equals(ATTRIBUTE_NAME_XTRC) ||
                    attQName.equals(ATTRIBUTE_NAME_COLNAME)|| attQName.equals(ATTRIBUTE_NAME_NAMEST) || attQName.equals(ATTRIBUTE_NAME_NAMEEND) ||
                    ATTRIBUTE_NAME_CONREF.equals(attQName)) {
                continue;
            } else if(ATTRIBUTE_NAME_HREF.equals(attQName) || ATTRIBUTE_NAME_COPY_TO.equals(attQName)){
                if (atts.getValue(ATTRIBUTE_NAME_SCOPE) == null ||
                        atts.getValue(ATTRIBUTE_NAME_SCOPE).equals(ATTR_SCOPE_VALUE_LOCAL)){
                    attValue = replaceHREF(attQName, atts).toString();
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
                        URI target;
                        if(key.length() != 0 && keys.containsKey(key)){
                        	
                            //target = FileUtils.replaceExtName(target);
                            //get key's href
                            final KeyDef value = keys.get(key);
                            final URI href = value.href;
                            
                            final URI updatedHref = updateHref(href);

                            //get element/topic id
                            final String id = attValue.substring(keyIndex+1);

                            boolean idExported = false;
                            boolean keyrefExported = false;
                            List<Boolean> list = null;
                            if(transtype.equals(INDEX_TYPE_ECLIPSEHELP)){
                                list = delayConrefUtils.checkExport(href.toString(), id, key, tempDir);
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
                                String tail ;
                                if(sharpIndex == -1 ){
                                    if(target.getFragment() == null) {
                                        //change to topic id
                                        tail = attValue.substring(keyIndex + 1).replaceAll(SLASH, SHARP);
                                    } else {
                                        //change to element id
                                        tail = attValue.substring(keyIndex + 1);
                                    }
                                }else {
                                    //change to topic id
                                    tail = attValue.substring(keyIndex + 1);
                                    //replace the topic id defined in the key's href
                                    if(target.getFragment() != null){
                                        target = stripFragment(target);
                                    }
                                }
                                XMLUtils.addOrSetAttribute(res, ATTRIBUTE_NAME_CONREF, setFragment(target, tail).toString());
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
                            final URI href = value.href;

                            final URI updatedHref = updateHref(href);

                            final String id = null;

                            final List<Boolean> list = delayConrefUtils.checkExport(href.toString(), id, attValue, tempDir);
                            final boolean keyrefExported = list.get(1).booleanValue();
                            //key is exported and transtype is eclipsehelp
                            if(keyrefExported && transtype.equals(INDEX_TYPE_ECLIPSEHELP)){
                                //remain the conkeyref attribute.
                                XMLUtils.addOrSetAttribute(res, ATTRIBUTE_NAME_CONKEYREF, attValue);
                            }else{
                                //e.g conref = c.xml
                                XMLUtils.addOrSetAttribute(res, ATTRIBUTE_NAME_CONREF, updatedHref.toString());
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
                XMLUtils.addOrSetAttribute(res, atts.getURI(i), atts.getLocalName(i), attQName, atts.getType(i), attValue);
            }
        }
        final String conref = atts.getValue(ATTRIBUTE_NAME_CONREF);
        if(conref != null && !conkeyrefValid){
            XMLUtils.addOrSetAttribute(res, ATTRIBUTE_NAME_CONREF, replaceCONREF(atts).toString());
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
     * Update href URI.
     * 
     * @param href href URI
     * @return updated href URI
     */
    private URI updateHref(final URI href) {
        final URI tempDirUri = tempDir.toURI();
        final URI filePath = tempDirUri.resolve(toURI(inputFile));
        final URI keyValue = tempDirUri.resolve(href);
        final URI updatedHref = URLUtils.getRelativePath(filePath, keyValue);
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
            colSpec = new ArrayList<String>(16);
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
                getContentHandler().processingInstruction(PI_PATH2PROJ_TARGET_URI, URLUtils.toURI(path2Project).toString());
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
    public void write(final File baseDir, final File inFile) {
        inputFile = inFile;

        OutputStream out = null;
        try {
            traceFilename = new File(baseDir, inputFile.getPath());
            File outputFile = new File(tempDir, inputFile.getPath());

            path2Project = getPathtoProject(inputFile, traceFilename, job.getInputFile().getAbsoluteFile());            
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
            final InputSource is = new InputSource(traceFilename.toURI().toString());
            if(setSystemid) {
                is.setSystemId(traceFilename.toURI().toString());
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
    public String getPathtoProject (final File filename, final File traceFilename, final File inputMap) {
    	String path2Project = null;
    	if(job.getGeneratecopyouter() != Job.Generate.OLDSOLUTION){
            if(isOutFile(traceFilename, inputMap)){
                
                path2Project = getRelativePathFromOut(traceFilename.getAbsoluteFile());
            }else{
                 path2Project = FileUtils.getRelativeUnixPath(traceFilename.getAbsolutePath(),inputMap.getAbsolutePath());
                path2Project = new File(path2Project).getParent();
                if(path2Project != null && path2Project.length()>0){
                    path2Project = path2Project+File.separator;
                }
            }
        } else {
            final File p = FileUtils.getRelativePath(filename);
            path2Project = p != null ? p.getPath() : null;
            if (path2Project != null && !path2Project.endsWith(File.separator)) {
                path2Project = path2Project + File.separator;
            }
        }
    	 return path2Project;
    }
    /**
     * Just for the overflowing files.
     * @param overflowingFile overflowingFile
     * @return relative path to out
     */
    public String getRelativePathFromOut(final File overflowingFile) {
        final File relativePath = FileUtils.getRelativePath(job.getInputFile(), overflowingFile);
        final File outputDir = job.getOutputDir().getAbsoluteFile();
        final File outputPathName = new File(outputDir, "index.html");
        final File finalOutFilePathName = FileUtils.resolveFile(outputDir, relativePath.getPath());
        final File finalRelativePathName = FileUtils.getRelativePath(finalOutFilePathName, outputPathName);
        File parentDir = finalRelativePathName.getParentFile();
        if (parentDir == null || parentDir.getPath().isEmpty()) {
            parentDir = new File(".");
        }
        return parentDir.getPath() + File.separator;
    }

    /**
     * Check if path falls outside start document directory
     * 
     * @param filePathName absolute path to test
     * @param inputMap absolute input map path
     * @return {@code true} if outside start directory, otherwise {@code false}
     */
    private boolean isOutFile(final File filePathName, final File inputMap){
        final File relativePath = FileUtils.getRelativePath(inputMap.getAbsoluteFile(), filePathName.getAbsoluteFile());
        if(relativePath == null || relativePath.getPath().length() == 0 || !relativePath.getPath().startsWith("..")){
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
