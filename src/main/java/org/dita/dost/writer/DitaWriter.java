/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.writer;

import static org.dita.dost.util.Constants.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;

import org.apache.xerces.xni.grammars.XMLGrammarPool;

import org.dita.dost.exception.DITAOTXMLErrorHandler;
import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.module.Content;
import org.dita.dost.reader.GrammarPoolManager;
import org.dita.dost.util.CatalogUtils;
import org.dita.dost.util.Configuration;
import org.dita.dost.util.DITAAttrUtils;
import org.dita.dost.util.DelayConrefUtils;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.FilterUtils;
import org.dita.dost.util.Job;
import org.dita.dost.util.OutputUtils;
import org.dita.dost.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;



/**
 * DitaWriter reads dita topic file and insert debug information and filter out the
 * content that is not necessary in the output.
 * 
 * <p>The following processing instructions are added before the root element:</p>
 * <dl>
 *   <dt>{@link #PI_WORKDIR_TARGET}<dt>
 *   <dd>Absolute system path of the file parent directory. On Windows, a {@code /}
 *     is added to beginning of the path.</dd>
 *   <dt>{@link #PI_PATH2PROJ_TARGET}<dt>
 *   <dd>Relative system path to the project root directory, with a trailing directory separator.
 *     When the file is in the project root directory, processing instruction has no value.</dd>
 * </dl>
 * 
 * <p>The following attributes are added to elements:</p>
 * <dl>
 *   <dt>{@link org.dita.dost.util.Constants#ATTRIBUTE_NAME_XTRF}</dt>
 *   <dd>Absolute system path of the source file.</dd>
 *   <dt>{@link org.dita.dost.util.Constants#ATTRIBUTE_NAME_XTRF}</dt>
 *   <dd>Element name and count in the document, {@code :} separated.</dd>
 * </dl>
 * 
 * @author Zhang, Yuan Peng
 */
public final class DitaWriter extends AbstractXMLWriter {

    private static final String ATTRIBUTE_NAME_COLNAME = "colname";
    private static final String ATTRIBUTE_NAME_COLNUM = "colnum";
    private static final String COLUMN_NAME_COL = "col";
    public static final String PI_PATH2PROJ_TARGET = "path2project";
    public static final String PI_WORKDIR_TARGET = "workdir";
    /** To check the URL of href in topicref attribute */
    private static final String NOT_LOCAL_URL = COLON_DOUBLE_SLASH;
    
    /** Generate {@code xtrf} and {@code xtrc} attributes */
    private final boolean genDebugInfo;
    
    //Added on 2010-08-24 for bug:3086552 start
    private boolean setSystemid = true;
    //Added on 2010-08-24 for bug:3086552 end

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
                    relativePath = FileUtils.getRelativePath(outputUtils.getInputMapPathName(), path);
                    attValue = relativePath + topic;
                }

            }
        }else{
            final File target = new File(attValue);
            if(target.isAbsolute()){
                attValue = FileUtils.getRelativePath(outputUtils.getInputMapPathName(), attValue);
            }
        }
        if (attValue != null){
            attValue = FileUtils.separatorsToUnix(attValue);
        }

        if(attValue.indexOf(FILE_EXTENSION_DITAMAP) == -1){
            return FileUtils.replaceExtension(attValue, extName);
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
        final String hrefValue=valueOfHref;
        if(notLocalURL(hrefValue)){
            return true;
        }
        else{
            final String classValue=attrs.getValue(ATTRIBUTE_NAME_CLASS);
            if(classValue!=null && PR_D_CODEREF.matches(classValue)){
                return true;
            }
            final String formatValue=attrs.getValue(ATTRIBUTE_NAME_FORMAT);
            final String extOfHref = FileUtils.getExtension(valueOfHref);
            if(formatValue==null && extOfHref!=null && !extOfHref.equalsIgnoreCase("DITA") && !extOfHref.equalsIgnoreCase("XML") ){
                final DITAOTLogger logger=new DITAOTJavaLogger();
                final Properties params = new Properties();
                params.put("%1", hrefValue);
                logger.logError(MessageUtils.getMessage("DOTJ028E", params).toString());
                return true;
            }
        }

        return false;
    }
    
    /**
     * Normalize href attribute.
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
        if(attValue!=null){
            final int dot_index = attValue.lastIndexOf(DOT);
            final int sharp_index = attValue.lastIndexOf(SHARP);
            if(sharp_index != -1 && dot_index < sharp_index){
                String path = attValue.substring(0, sharp_index);
                final String topic = attValue.substring(sharp_index);
                if(path.length() != 0){
                    String relativePath;
                    //Added by William on 2010-01-05 for bug:2926417 start
                    if(path.startsWith("file:/") && path.indexOf("file://") == -1){
                        path = path.substring("file:/".length());
                        //Unix like OS
                        if(UNIX_SEPARATOR.equals(File.separator)){
                            path = UNIX_SEPARATOR + path;
                        }
                    }
                    //Added by William on 2010-01-05 for bug:2926417 end
                    final File target = new File(path);
                    if(target.isAbsolute()){
                        relativePath = FileUtils.getRelativePath(outputUtils.getInputMapPathName(), path);
                        attValue = relativePath + topic;
                    }

                }
            }else{
                //Added by William on 2010-01-05 for bug:2926417 start
                if(attValue.startsWith("file:/") && attValue.indexOf("file://") == -1){
                    attValue = attValue.substring("file:/".length());
                    //Unix like OS
                    if(UNIX_SEPARATOR.equals(File.separator)){
                        attValue = UNIX_SEPARATOR + attValue;
                    }
                }
                //Added by William on 2010-01-05 for bug:2926417 end
                final File target = new File(attValue);
                if(target.isAbsolute()){
                    attValue = FileUtils.getRelativePath(outputUtils.getInputMapPathName(), attValue);
                }
            }

            /*
             * replace all the backslash with slash in
             * all href and conref attribute
             */
            attValue = FileUtils.separatorsToUnix(attValue);
        } else {
            return null;
        }

        if(checkDITAHREF(atts)){
            if(warnOfNoneTopicFormat(atts,attValue)==false){
                return FileUtils.replaceExtension(attValue, extName);
            }

        }

        return attValue;
    }
    private String absolutePath;
    private Map<String, String> catalogMap; //map that contains the information from XML Catalog
    private List<String> colSpec;
    private int columnNumber; // columnNumber is used to adjust column name
    private int columnNumberEnd; //columnNumberEnd is the end value for current entry
    //Added by William on 2009-11-27 for bug:1846993 embedded table bug start
    /** Stack to store colspec list */
    private final Stack<List<String>> colSpecStack;
    //Added by William on 2009-11-27 for bug:1846993 embedded table bug end

    //Added by William on 2010-07-01 for bug:3023642 start
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
    //Added by William on 2010-07-01 for bug:3023642 end


    //Added by William on 2009-06-30 for colname bug:2811358 start
    /** Store row number */
    private int rowNumber;
    /** Store total column count */
    private int totalColumns;
    /** store morerows attribute */
    private Map<String, Integer> rowsMap;
    private Map<String, Integer> colSpanMap;
    //Added by William on 2009-06-30 for colname bug:2811358 end
    //Added by William on 2009-07-18 for req #12014 start
    /** Transtype */
    private String transtype;
    //Added by William on 2009-07-18 for req #12014 start

    private Map<String, Integer> counterMap;
    private boolean exclude; // when exclude is true the tag will be excluded.
    private int foreignLevel; // foreign/unknown nesting level
    private int level;// level is used to count the element level in the filtering
    private boolean needResolveEntity; //check whether the entity need resolve.
    private OutputStreamWriter output;
    private String path2Project;
    /** Contains the attribution specialization paths for {@code props} attribute */
    private String[][] props;

    private String tempDir;
    private File traceFilename;
    private boolean insideCDATA;

    private Map<String, String> keys = null;

    //Added by William on 2010-02-25 for bug:2957456 start
    private String inputFile = null;
    //Added by William on 2010-02-25 for bug:2957456 end

    //Added by William on 2010-06-01 for bug:3005748 start
    //Get DITAAttrUtil
    private final DITAAttrUtils ditaAttrUtils = DITAAttrUtils.getInstance();
    //Added by William on 2010-06-01 for bug:3005748 end

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
     * {@link #initXMLReader(String, boolean, boolean)} must be called after
     * construction to initialize XML parser.
     */
    public DitaWriter() {
        super();
        
        genDebugInfo = Boolean.parseBoolean(Configuration.configuration.get("generate-debug-attributes"));
        
        exclude = false;
        columnNumber = 1;
        columnNumberEnd = 0;
        //Added by William on 2009-06-30 for colname bug:2811358 start
        //initialize row number
        rowNumber = 0;
        //initialize total column count
        totalColumns = 0;
        //initialize the map
        rowsMap = new HashMap<String, Integer>();
        colSpanMap = new HashMap<String, Integer>();
        //Added by William on 2009-06-30 for colname bug:2811358 start
        absolutePath = null;
        path2Project = null;
        counterMap = null;
        traceFilename = null;
        foreignLevel = 0;
        level = 0;
        needResolveEntity = false;
        insideCDATA = false;
        output = null;
        tempDir = null;
        colSpec = null;
        //initial the stack
        colSpecStack = new Stack<List<String>>();
        //added by William on 20100701 for bug:3023642 start
        rowNumStack = new Stack<Integer>();
        columnNumberStack = new Stack<Integer>();
        columnNumberEndStack = new Stack<Integer>();
        rowsMapStack = new Stack<Map<String,Integer>>();
        colSpanMapStack = new Stack<Map<String,Integer>>();
        //added by William on 20100701 for bug:3023642 end

        props = null;
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
     * Initialize XML reader used for pipeline parsing.
     * @param ditaDir ditaDir
     * @param validate whether validate
     * @throws SAXException SAXException
     */
    public void initXMLReader(final String ditaDir, final boolean validate, final boolean arg_setSystemid) throws SAXException {
        try {
            reader = StringUtils.getXMLReader();

            reader.setProperty(LEXICAL_HANDLER_PROPERTY,this);
            reader.setFeature("http://apache.org/xml/features/scanner/notify-char-refs", true);
            reader.setFeature("http://apache.org/xml/features/scanner/notify-builtin-refs", true);
            reader.setFeature(FEATURE_NAMESPACE_PREFIX, true);
            if(validate==true){
                reader.setFeature(FEATURE_VALIDATION, true);
                reader.setFeature(FEATURE_VALIDATION_SCHEMA, true);
            }
            reader.setFeature(FEATURE_NAMESPACE, true);
            reader.setContentHandler(this);
            reader.setEntityResolver(CatalogUtils.getCatalogResolver());
        } catch (final Exception e) {
            throw new SAXException("Failed to initialize XML parser: " + e.getMessage(), e);
        }
        setGrammarPool(reader, GrammarPoolManager.getGrammarPool());
        CatalogUtils.setDitaDir(ditaDir);
        catalogMap = CatalogUtils.getCatalog(ditaDir);
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

    @Override
    public void characters(final char[] ch, final int start, final int length)
            throws SAXException {
        if (!exclude && needResolveEntity) {
            // exclude shows whether it's excluded by filtering
            // isEntity shows whether it's an entity.
            try {
                if(insideCDATA) {
                    output.write(ch, start, length);
                } else {
                    output.write(StringUtils.escapeXML(ch,start, length));
                }
            } catch (final Exception e) {
                logger.logException(e);
            }
        }
    }

    /**
     * Write attribute to output. The method does not escape XML delimiter chacters in the value.
     * 
     * @param attQName attribute name
     * @param attValue attribute value
     * @throws IOException if writing to output failed
     */
    private void copyAttribute(final String attQName, final String attValue) throws IOException{
        output.write(STRING_BLANK);
        output.write(attQName);
        output.write(EQUAL);
        output.write(QUOTATION);
        output.write(attValue);
        output.write(QUOTATION);
    }

    /**
     * Process all attributes and write them to output
     * 
     * @param qName element name
     * @param atts attributes
     * @throws IOException if writing to output failed
     */
    private void copyElementAttribute(final String qName, final Attributes atts) throws IOException {
        // copy the element's attributes
        final int attsLen = atts.getLength();
        boolean conkeyrefValid = false;
        for (int i = 0; i < attsLen; i++) {
            final String attQName = atts.getQName(i);
            String attValue = atts.getValue(i);
            final String nsUri = atts.getURI(i);

            //ignore the xtrf and xtrc attribute ,and not copy
            if(attQName.equals(ATTRIBUTE_NAME_XTRF)|| attQName.equals(ATTRIBUTE_NAME_XTRC)) {
                continue;
            }

            //Probe for default values
            if (StringUtils.isEmptyString(attValue) && defaultValueMap != null) {
                final Map<String, String> defaultMap = defaultValueMap.get(attQName);
                if (defaultMap != null) {
                    final String defaultValue = defaultMap.get(qName);
                    if (defaultValue != null) {
                        attValue = defaultValue;
                    }
                }
            }

            if(ATTRIBUTE_NAME_HREF.equals(attQName)
                    || ATTRIBUTE_NAME_COPY_TO.equals(attQName)){
                if(atts.getValue(ATTRIBUTE_NAME_SCOPE)!=null &&
                        (atts.getValue(ATTRIBUTE_NAME_SCOPE).equalsIgnoreCase(ATTR_SCOPE_VALUE_EXTERNAL) ||
                                atts.getValue(ATTRIBUTE_NAME_SCOPE).equalsIgnoreCase(ATTR_SCOPE_VALUE_PEER))){
                    attValue = atts.getValue(i);
                }else{
                    attValue = replaceHREF(attQName, atts);
                    //added on 2010-09-02 for bug:3058124(decode escaped string)
                    attValue = URLDecoder.decode(attValue, UTF8);
                }

            } else if (ATTRIBUTE_NAME_CONREF.equals(attQName)){

                attValue = replaceCONREF(atts);
                //added on 2010-09-02 for bug:3058124(decode escaped string)
                attValue = URLDecoder.decode(attValue, UTF8);
            } else {
                attValue = atts.getValue(i);
            }

            if (ATTRIBUTE_NAME_DITAARCHVERSION.equals(attQName)){
                final String attName = ATTRIBUTE_PREFIX_DITAARCHVERSION + COLON + attQName;

                copyAttribute(attName, attValue);
                copyAttribute(ATTRIBUTE_NAMESPACE_PREFIX_DITAARCHVERSION, nsUri);

            }

            // replace conref with conkeyref(using key definition)
            if(ATTRIBUTE_NAME_CONKEYREF.equals(attQName) && attValue.length() != 0){
                final int sharpIndex = attValue.indexOf(SHARP);
                final int slashIndex = attValue.indexOf(SLASH);
                int keyIndex = -1;
                if(sharpIndex != -1){
                    keyIndex = sharpIndex;
                }else if(slashIndex != -1){
                    keyIndex = slashIndex;
                }
                //conkeyref only accept values such as "key" or "key/id"
                //bug:3081597
                if(sharpIndex == -1){
                    if(keyIndex != -1){
                        //get keyref value
                        final String key = attValue.substring(0,keyIndex);
                        String target;
                        if(key.length() != 0 && keys.containsKey(key)){

                            //target = FileUtils.replaceExtName(target);
                            //Added by William on 2009-06-25 for #12014 start
                            //get key's href
                            final String value = keys.get(key);
                            final String href = value.substring(0, value.lastIndexOf(LEFT_BRACKET));

                            //Added by William on 2010-02-25 for bug:2957456 start
                            final String updatedHref = updateHref(href);
                            //Added by William on 2010-02-25 for bug:2957456 end

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
                                copyAttribute(ATTRIBUTE_NAME_CONKEYREF, attValue);
                                //Added by William on 2009-06-25 for #12014 end
                            }else {
                                //normal process
                                target = updatedHref;
                                //Added by William on 2010-05-17 for conkeyrefbug:3001705 start
                                //only replace extension name of topic files.
                                target = replaceExtName(target);
                                //Added by William on 2010-05-17 for conkeyrefbug:3001705 end
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
                                copyAttribute(ATTRIBUTE_NAME_CONREF, target + tail);
                                conkeyrefValid = true;
                            }
                        }else{
                            final Properties prop = new Properties();
                            prop.setProperty("%1", attValue);
                            logger.logError(MessageUtils.getMessage("DOTJ046E", prop).toString());
                        }
                    }else{
                        //conkeyref just has keyref
                        if(keys.containsKey(attValue)){
                            //get key's href
                            final String value = keys.get(attValue);
                            final String href = value.substring(0, value.lastIndexOf(LEFT_BRACKET));

                            //Added by William on 2010-02-25 for bug:2957456 start
                            final String updatedHref = updateHref(href);
                            //Added by William on 2010-02-25 for bug:2957456 end

                            //Added by William on 2009-06-25 for #12014 start
                            final String id = null;

                            final List<Boolean> list = delayConrefUtils.checkExport(href, id, attValue, tempDir);
                            final boolean keyrefExported = list.get(1).booleanValue();
                            //key is exported and transtype is eclipsehelp
                            if(keyrefExported && transtype.equals(INDEX_TYPE_ECLIPSEHELP)){
                                //remain the conkeyref attribute.
                                copyAttribute(ATTRIBUTE_NAME_CONKEYREF, attValue);
                                //Added by William on 2009-06-25 for #12014 end
                            }else{
                                //e.g conref = c.xml
                                String target = updatedHref;
                                //Added by William on 2010-05-17 for conkeyrefbug:3001705 start
                                target = replaceExtName(target);
                                //Added by William on 2010-05-17 for conkeyrefbug:3001705 end
                                copyAttribute(ATTRIBUTE_NAME_CONREF, target);

                                conkeyrefValid = true;
                            }
                        }else{
                            final Properties prop = new Properties();
                            prop.setProperty("%1", attValue);
                            logger.logError(MessageUtils.getMessage("DOTJ046E", prop).toString());
                        }
                    }
                }else{
                    //invalid conkeyref value
                    final Properties prop = new Properties();
                    prop.setProperty("%1", attValue);
                    logger.logError(MessageUtils.getMessage("DOTJ046E", prop).toString());
                }
            }

            // replace '&' with '&amp;'
            //if (attValue.indexOf('&') > 0) {
            //attValue = StringUtils.replaceAll(attValue, "&", "&amp;");
            //}
            attValue = StringUtils.escapeXML(attValue);

            //output all attributes except colname and conkeyref,
            // if conkeyrefValid is true, then conref is not copied.
            if (!ATTRIBUTE_NAME_COLNAME.equals(attQName)
                    && !ATTRIBUTE_NAME_NAMEST.equals(attQName)
                    && !ATTRIBUTE_NAME_DITAARCHVERSION.equals(attQName)
                    && !ATTRIBUTE_NAME_NAMEEND.equals(attQName)
                    && !ATTRIBUTE_NAME_CONKEYREF.equals(attQName)
                    && !ATTRIBUTE_NAME_CONREF.equals(attQName) ){
                copyAttribute(attQName, attValue);
            }

        }
        String conref = atts.getValue(ATTRIBUTE_NAME_CONREF);
        if(conref != null && !conkeyrefValid){
            conref = replaceCONREF(atts);
            conref = StringUtils.escapeXML(conref);
            copyAttribute(ATTRIBUTE_NAME_CONREF, conref);
        }
    }

    /**
     * Replace extension name for non-ditamap file.
     * 
     * @param target String
     * @return String
     */
    private String replaceExtName(String target) {
        final String fileName = FileUtils.resolveFile("", target);
        if(FileUtils.isDITATopicFile(fileName)){
            target = FileUtils.replaceExtension(target, extName);
        }
        return target;
    }

    /**
     * Update href.
     * 
     * @param href String key's href
     * @return updated href value
     */
    private String updateHref(final String href) {

        //Added by William on 2010-05-18 for bug:3001705 start
        final String filePath = new File(tempDir, inputFile).getAbsolutePath();

        final String keyValue = new File(tempDir, href).getAbsolutePath();

        final String updatedHref = FileUtils.getRelativePath(filePath, keyValue);
        //Added by William on 2010-05-18 for bug:3001705 end


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
    private void copyElementName(final String qName, final Attributes atts) throws IOException {
        if (TOPIC_TGROUP.localName.equals(qName)){

            //Edited by William on 2009-11-27 for bug:1846993 start
            //push into the stack.
            if(colSpec!=null){
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
            //Edited by William on 2009-11-27 for bug:1846993 end
        }else if(TOPIC_ROW.localName.equals(qName)) {
            columnNumber = 1; // initialize the column number
            columnNumberEnd = 0;
            //Added by William on 2009-06-30 for colname bug:2811358 start
            //store the row number
            rowNumber++;
            //Added by William on 2009-06-30 for colname bug:2811358 end
        }else if(TOPIC_COLSPEC.localName.equals(qName)){
            columnNumber = columnNumberEnd +1;
            if(atts.getValue(ATTRIBUTE_NAME_COLNAME) != null){
                colSpec.add(atts.getValue(ATTRIBUTE_NAME_COLNAME));
            }else{
                colSpec.add(COLUMN_NAME_COL+columnNumber);
            }
            columnNumberEnd = columnNumber;
            //change the col name of colspec
            copyAttribute(ATTRIBUTE_NAME_COLNAME, COLUMN_NAME_COL+columnNumber);
            //Added by William on 2009-06-30 for colname bug:2811358 start
            //total columns count
            totalColumns = columnNumberEnd;
            //Added by William on 2009-06-30 for colname bug:2811358 end
        }else if(TOPIC_ENTRY.localName.equals(qName)){

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



            //Added by William on 2009-06-30 for colname bug:2811358 start
            //Changed on 2010-11-19 for duplicate colname bug:3110418 start
            columnNumber = getStartNumber(atts, columnNumberEnd);


            if(columnNumber > columnNumberEnd){
                //The first row
                if(rowNumber == 1){
                    copyAttribute(ATTRIBUTE_NAME_COLNAME, COLUMN_NAME_COL+columnNumber);
                    if (atts.getValue(ATTRIBUTE_NAME_NAMEST) != null){
                        copyAttribute(ATTRIBUTE_NAME_NAMEST, COLUMN_NAME_COL+columnNumber);
                    }
                    if (atts.getValue(ATTRIBUTE_NAME_NAMEEND) != null){
                        copyAttribute(ATTRIBUTE_NAME_NAMEEND, COLUMN_NAME_COL+getEndNumber(atts, columnNumber));
                    }
                    //other row
                }else{
                    int offset = 0;
                    int currentCol = columnNumber;
                    while(currentCol<=totalColumns) {
                        int previous_offset=offset;
                        //search from first row
                        for(int row=1;row<rowNumber;row++){
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
                            previous_offset=offset;
                        } else {
                            break;
                        }

                    }
                    columnNumber = columnNumber+offset;
                    //if has morerows attribute
                    if(atts.getValue(ATTRIBUTE_NAME_MOREROWS)!=null){
                        final String pos = String.valueOf(rowNumber) + "-" + String.valueOf(columnNumber);
                        //total span rows
                        final int total = Integer.parseInt(atts.getValue(ATTRIBUTE_NAME_MOREROWS))+
                                rowNumber;
                        rowsMap.put(pos, Integer.valueOf(total));
                        colSpanMap.put(pos, getColumnSpan(atts));

                    }

                    copyAttribute(ATTRIBUTE_NAME_COLNAME, COLUMN_NAME_COL+columnNumber);
                    if (atts.getValue(ATTRIBUTE_NAME_NAMEST) != null){
                        copyAttribute(ATTRIBUTE_NAME_NAMEST, COLUMN_NAME_COL+columnNumber);
                    }
                    if (atts.getValue(ATTRIBUTE_NAME_NAMEEND) != null){
                        copyAttribute(ATTRIBUTE_NAME_NAMEEND, COLUMN_NAME_COL+getEndNumber(atts, columnNumber));
                    }
                }
            }
            columnNumberEnd = getEndNumber(atts, columnNumber);
            //Changed on 2010-11-19 for duplicate colname bug:3110418 end
            //Added by William on 2009-06-30 for colname bug:2811358 end
        }
    }

   private int getColumnSpan(final Attributes atts) {
        int ret;
        if ((atts.getValue(ATTRIBUTE_NAME_NAMEST) == null)||(atts.getValue(ATTRIBUTE_NAME_NAMEEND) == null)){
            return 1;
        }else{
            ret = colSpec.indexOf(atts.getValue(ATTRIBUTE_NAME_NAMEEND)) - colSpec.indexOf(atts.getValue(ATTRIBUTE_NAME_NAMEST))+1;
            if(ret <= 0){
                return 1;
            }
            return ret;
        }
    }
    
    @Override
    public void endCDATA() throws SAXException {
        insideCDATA = false;
        try{
            output.write(CDATA_END);
        }catch(final Exception e){
            logger.logException(e);
        }
    }


    @Override
    public void endDocument() throws SAXException {
        try {
            output.flush();
        } catch (final Exception e) {
            logger.logException(e);
        }

        //Added by William on 2010-06-01 for bug:3005748 start
        //@print
        ditaAttrUtils.reset();
        //Added by William on 2010-06-01 for bug:3005748 end
    }


    @Override
    public void endElement(final String uri, final String localName, final String qName)
            throws SAXException {

        //Added by William on 2010-06-01 for bug:3005748 start
        //need to skip the tag
        if(ditaAttrUtils.needExcludeForPrintAttri(transtype)){
            //decrease level
            ditaAttrUtils.decreasePrintLevel();
            //don't write the end tag
            return;
        }
        //Added by William on 2010-06-01 for bug:3005748 end

        if (foreignLevel > 0){
            foreignLevel --;
        }
        if (exclude) {
            if (level > 0) {
                // If it is the end of a child of an excluded tag, level
                // decrease
                level--;
            } else {
                exclude = false;
            }
        } else { // exclude shows whether it's excluded by filtering
            try {
                output.write(LESS_THAN + SLASH);
                output.write(qName);
                output.write(GREATER_THAN);
            } catch (final Exception e) {
                logger.logException(e);
            }
        }
        //Added by William on 2009-11-27 for bug:1846993 embedded table bug start
        //note the tag shouldn't be excluded by filter file(bug:2925636 )
        if(TOPIC_TGROUP.localName.equals(qName) && !exclude){
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
        //Added by William on 2009-11-27 for bug:1846993 embedded table bug end

    }

    @Override
    public void endEntity(final String name) throws SAXException {
        if(!needResolveEntity){
            needResolveEntity = true;
        }
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
        int ret;
        if (atts.getValue(ATTRIBUTE_NAME_COLNUM) != null){
            return new Integer(atts.getValue(ATTRIBUTE_NAME_COLNUM)).intValue();
        }else if(atts.getValue(ATTRIBUTE_NAME_NAMEST) != null){
            ret = colSpec.indexOf(atts.getValue(ATTRIBUTE_NAME_NAMEST)) + 1;
            if(ret == 0){
                return previousEnd + 1;
            }
            return ret;
        }else if(atts.getValue(ATTRIBUTE_NAME_COLNAME) != null){
            ret = colSpec.indexOf(atts.getValue(ATTRIBUTE_NAME_COLNAME)) + 1;
            if(ret == 0){
                return previousEnd + 1;
            }
            return ret;
        }else{
            return previousEnd + 1;
        }
    }

    @Override
    public void ignorableWhitespace(final char[] ch, final int start, final int length)
            throws SAXException {
        if (!exclude) { // exclude shows whether it's excluded by filtering
            try {
                output.write(ch, start, length);
            } catch (final Exception e) {
                logger.logException(e);
            }
        }
    }

    @Override
    public void processingInstruction(final String target, final String data) throws SAXException {
        if (!exclude) { // exclude shows whether it's excluded by filtering
            try {
                super.processingInstruction(target, data);
                output.write(LESS_THAN + QUESTION);
                output.write(target);
                if (data != null) {
                    output.write(STRING_BLANK + data);
                }
                output.write(QUESTION + GREATER_THAN);
            } catch (final Exception e) {
                logger.logException(e);
            }
        }
    }

    @Override
    public InputSource resolveEntity(final String publicId, final String systemId)
            throws SAXException, IOException {
        if (catalogMap.get(publicId)!=null){
            final File dtdFile = new File(catalogMap.get(publicId));
            return new InputSource(dtdFile.getAbsolutePath());
        }else if (catalogMap.get(systemId) != null){
            final File schemaFile = new File(catalogMap.get(systemId));
            return new InputSource(schemaFile.getAbsolutePath());
        }
        return null;
    }

    /**
     * Set temporary directory
     * 
     * @param tempDir temporary directory
     */
    public void setTempDir(final String tempDir) {
        this.tempDir = tempDir;
    }

    @Override
    public void skippedEntity(final String name) throws SAXException {
        if (!exclude) { // exclude shows whether it's excluded by filtering
            try {
                output.write(StringUtils.getEntity(name));
            } catch (final Exception e) {
                logger.logException(e);
            }
        }
    }

    @Override
    public void startCDATA() throws SAXException {
        try{
            insideCDATA = true;
            output.write(CDATA_HEAD);
        }catch(final Exception e){
            logger.logException(e);
        }
    }


    @Override
    public void startDocument() throws SAXException {
        try {
            output.write(XML_HEAD);
            output.write(LINE_SEPARATOR);
            if(OS_NAME.toLowerCase().indexOf(OS_NAME_WINDOWS)==-1)
            {
                processingInstruction(PI_WORKDIR_TARGET, absolutePath);
            }else{
                processingInstruction(PI_WORKDIR_TARGET, UNIX_SEPARATOR + absolutePath);
            }
            output.write(LINE_SEPARATOR);
            if(path2Project != null){
                processingInstruction(PI_PATH2PROJ_TARGET, path2Project);
            }else{
                processingInstruction(PI_PATH2PROJ_TARGET, null);
            }
            output.write(LINE_SEPARATOR);
        } catch (final Exception e) {
            logger.logException(e);
        }
    }


    @Override
    public void startElement(final String uri, final String localName, final String qName,
            final Attributes atts) throws SAXException {
        Integer value;
        Integer nextValue;
        String domains = null;
        final String attrValue = atts.getValue(ATTRIBUTE_NAME_CLASS);

        //Added by William on 2010-06-01 for bug:3005748 start
        final String printValue = atts.getValue(ATTRIBUTE_NAME_PRINT);
        //increase element level for nested tags.
        ditaAttrUtils.increasePrintLevel(printValue);

        if(ditaAttrUtils.needExcludeForPrintAttri(transtype)){
            return;
        }
        //Added by William on 2010-06-01 for bug:3005748 end

        if (foreignLevel > 0){
            foreignLevel ++;
        }else if( foreignLevel == 0){

            if(attrValue==null && !ELEMENT_NAME_DITA.equals(localName)){
                final Properties params = new Properties();
                params.put("%1", localName);
                logger.logInfo(MessageUtils.getMessage("DOTJ030I", params).toString());
            }
            if (attrValue != null && (TOPIC_TOPIC.matches(attrValue)||MAP_MAP.matches(attrValue))){
                domains = atts.getValue(ATTRIBUTE_NAME_DOMAINS);
                if(domains==null){
                    final Properties params = new Properties();
                    params.put("%1", localName);
                    logger.logInfo(MessageUtils.getMessage("DOTJ029I", params).toString());
                } else {
                    props = StringUtils.getExtProps(domains);
                }
            }
            if (attrValue != null &&
                    (TOPIC_FOREIGN.matches(attrValue) ||
                            TOPIC_UNKNOWN.matches(attrValue))){
                foreignLevel = 1;
            }
        }

        validateAttributeValues(qName, atts);

        if (counterMap.containsKey(qName)) {
            value = counterMap.get(qName);
            nextValue = value + 1;
        } else {
            nextValue = 1;
        }
        counterMap.put(qName, nextValue);

        if (exclude) {
            // If it is the start of a child of an excluded tag, level increase
            level++;
        } else { // exclude shows whether it's excluded by filtering
            if (foreignLevel <= 1 && filterUtils.needExclude(atts, props)){
                exclude = true;
                level = 0;
            }else{
                try {
                    output.write(LESS_THAN);
                    output.write(qName);
                    copyElementName(qName, atts);

                    copyElementAttribute(qName, atts);
                    // write the xtrf and xtrc attributes which contain debug
                    // information if it is dita elements (elements not in foreign/unknown)
                    if (foreignLevel <= 1){
                        if (genDebugInfo) {
                            copyAttribute(ATTRIBUTE_NAME_XTRF, traceFilename.getAbsolutePath());
                            copyAttribute(ATTRIBUTE_NAME_XTRC, qName + COLON + nextValue.toString());
                        }
                    }
                    output.write(GREATER_THAN);

                } catch (final Exception e) {
                    logger.logException(e);
                }// try
            }
        }
    }

    @Override
    public void startEntity(final String name) throws SAXException {
        if (!exclude) { // exclude shows whether it's excluded by filtering
            try {
                needResolveEntity = StringUtils.checkEntity(name);
                if(!needResolveEntity){
                    output.write(StringUtils.getEntity(name));
                }
            } catch (final Exception e) {
                logger.logException(e);
            }
        }

    }


    /**
     * @deprecated use {@link #write(String, String))} instead
     */
    @Override
    @Deprecated
    public void write(final String filename) {
        final int index = filename.indexOf(STICK);
        final String baseDir = filename.substring(0, index);
        inputFile = filename.substring(index + 1);
        write(baseDir, inputFile);
    }
    
    /**
     * Write output
     * 
     * @param baseDir base directory path
     * @param inFile relative file path
     */
    public void write(final String baseDir, final String inFile) {
        exclude = false;
        needResolveEntity = true;

        inputFile = inFile;

        if(null == keys){
            keys = new HashMap<String, String>();
            if (! new File(tempDir).isAbsolute()){
                tempDir = new File(tempDir).getAbsolutePath();
            }

            Job job = null;
            try{
                job = new Job(new File(tempDir));
            }catch (final IOException e) {
                logger.logException(new Exception("Failed to read job configuration file: " + e.getMessage(), e));
            }

            for(final String keyinfo: job.getSet(KEY_LIST)){
                //get the key name
                final String key = keyinfo.substring(0, keyinfo.indexOf(EQUAL));

                //Edited by William on 2010-02-25 for bug:2957456 start
                //value = keyinfo.substring(keyinfo.indexOf(EQUAL)+1, keyinfo.indexOf("("));
                //get the href value and source file name
                //e.g topics/target-topic-a.xml(maps/root-map-01.ditamap)
                final String value = keyinfo.substring(keyinfo.indexOf(EQUAL)+1);
                //Edited by William on 2010-02-25 for bug:2957456 end
                keys.put(key, value);
            }
        }

        try {
            traceFilename = new File(baseDir, inputFile);
            File outputFile;
            if (FileUtils.isDITAMapFile(inputFile.toLowerCase())) {
                outputFile = new File(tempDir, inputFile);
            } else {
                outputFile = new File(tempDir, FileUtils.replaceExtension(inputFile, extName));
            }

            //when it is not the old solution 3
            if(outputUtils.getGeneratecopyouter()!=OutputUtils.Generate.OLDSOLUTION){
                if(isOutFile(traceFilename)){
                    path2Project=getRelativePathFromOut(traceFilename.getAbsolutePath());
                }else{
                    path2Project=FileUtils.getRelativePath(traceFilename.getAbsolutePath(), outputUtils.getInputMapPathName());
                    path2Project=new File(path2Project).getParent();
                    if(path2Project!=null && path2Project.length()>0){
                        path2Project=path2Project+File.separator;
                    }
                }
            } else {
                path2Project = FileUtils.getRelativePath(inputFile);
            }
            counterMap = new HashMap<String, Integer>();
            final File dirFile = outputFile.getParentFile();
            if (!dirFile.exists()) {
                dirFile.mkdirs();
            }
            absolutePath = dirFile.getCanonicalPath();
            output = new OutputStreamWriter(new FileOutputStream(outputFile), UTF8);


            // start to parse the file and direct to output in the temp
            // directory
            reader.setErrorHandler(new DITAOTXMLErrorHandler(traceFilename.getAbsolutePath(), logger));
            //Added on 2010-08-24 for bug:3086552 start
            final InputSource is = new InputSource(traceFilename.toURI().toString());
            //set system id bug:3086552
            if(setSystemid) {
                //is.setSystemId(URLUtil.correct(file).toString());
                is.setSystemId(traceFilename.toURI().toURL().toString());
            }

            //Added on 2010-08-24 for bug:3086552 end
            reader.parse(is);
        } catch (final Exception e) {
            e.printStackTrace();
            logger.logException(e);
        }finally {
            if (output != null) {
                try {
                    output.close();
                }catch (final Exception e) {
                    logger.logException(e);
                }
            }
        }
    }

    public String getPathtoProject (String filename, File traceFilename, String inputMap) {
    	String path2Project = null;
    	 if(outputUtils.getGeneratecopyouter()!=OutputUtils.Generate.OLDSOLUTION){
             if(isOutFile(traceFilename)){

                 path2Project=getRelativePathFromOut(traceFilename.getAbsolutePath());
             }else{
                 path2Project=FileUtils.getRelativePath(traceFilename.getAbsolutePath(),inputMap);
                 path2Project=new File(path2Project).getParent();
                 if(path2Project!=null && path2Project.length()>0){
                     path2Project=path2Project+File.separator;
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
        final File mapPathName=new File(outputUtils.getInputMapPathName());
        final File currFilePathName=new File(overflowingFile);
        final String relativePath=FileUtils.getRelativePath( mapPathName.toString(),currFilePathName.toString());
        final String outputDir=outputUtils.getOutputDir();
        final StringBuffer outputPathName=new StringBuffer(outputDir).append(File.separator).append("index.html");
        final String finalOutFilePathName=FileUtils.resolveFile(outputDir,relativePath);
        final String finalRelativePathName=FileUtils.getRelativePath(finalOutFilePathName,outputPathName.toString());
        final String parentDir=new File(finalRelativePathName).getParent();
        final StringBuffer finalRelativePath=new StringBuffer(parentDir);
        if(finalRelativePath.length()>0){
            finalRelativePath.append(File.separator);
        }else{
            finalRelativePath.append(".").append(File.separator);
        }
        return finalRelativePath.toString();
    }

    private boolean isOutFile(final File filePathName){
        final String relativePath=FileUtils.getRelativePath(outputUtils.getInputMapPathName(), filePathName.getPath());
        if(relativePath==null || relativePath.length()==0 || !relativePath.startsWith("..")){
            return false;
        }
        return true;
    }

    /**
     * Validate attribute values
     * 
     * @param qName element name
     * @param atts attributes
     */
    private void validateAttributeValues(final String qName, final Attributes atts) {
        if (validateMap == null) {
            return;
        }
        for (int i = 0; i < atts.getLength(); i++) {
            final String attrName = atts.getQName(i);
            final String attrValue = atts.getValue(i);

            final Map<String, Set<String>> valueMap = validateMap.get(attrName);
            if (valueMap != null) {
                Set<String> valueSet = valueMap.get(qName);
                if (valueSet == null) {
                    valueSet = valueMap.get("*");
                }
                if (valueSet != null) {
                    final String[] keylist = attrValue.trim().split("\\s+");
                    for (final String s : keylist) {
                        // Warning ? Value not valid.
                        if (!StringUtils.isEmptyString(s) && !valueSet.contains(s)) {
                            final Properties prop = new Properties();
                            prop.put("%1", attrName);
                            prop.put("%2", qName);
                            prop.put("%3", attrValue);
                            prop.put("%4", StringUtils.assembleString(valueSet,
                                    COMMA));
                            logger.logWarn(MessageUtils.getMessage("DOTJ049W",
                                    prop).toString());
                        }
                    }
                }
            }
        }
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

    //Added by William on 2009-07-18 for req #12014 start
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
    //Added by William on 2009-07-18 for req #12014 end

    //Added by Alan Date:2009-08-04 --begin
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
    //Added by Alan Date:2009-08-04 --end

}
