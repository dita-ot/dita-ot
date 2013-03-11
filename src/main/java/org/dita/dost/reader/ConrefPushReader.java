/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.reader;

import static org.dita.dost.util.Constants.*;

import java.io.File;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import org.dita.dost.log.MessageUtils;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Class for reading conref push content.
 *
 */
public final class ConrefPushReader extends AbstractXMLReader {
    
    /** Conaction mark value */
    private static final String ATTR_CONACTION_VALUE_MARK = "mark";
    /** Conaction push after value */
    private static final String ATTR_CONACTION_VALUE_PUSHAFTER = "pushafter";
    /** Conaction push before value */
    private static final String ATTR_CONACTION_VALUE_PUSHBEFORE = "pushbefore";
    /** Conaction push replace value */
    private static final String ATTR_CONACTION_VALUE_PUSHREPLACE = "pushreplace";
    
    /** push table.*/
    private final Hashtable<String, Hashtable<String, String>> pushtable;
    /** push table.*/
    private final XMLReader reader;
    /**whether an entity needs to be resolved or not flag. */
    private boolean needResolveEntity = true;

    /**keep the file path of current file under parse
	filePath is useful to get the absolute path of the target file.*/
    private String filePath = null;

    /**keep the file name of  current file under parse */
    private String parsefilename = null;
    /**pushcontent is used to store the content copied to target
	 in pushcontent href will be resolved if it is relative path
	 if @conref is in pushconref the target name should be recorded so that it
	 could be added to conreflist for conref resolution.*/
    private StringBuffer pushcontent = null;

    /**boolean start is used to control whether sax parser can start to
	 record push content into String pushcontent.*/
    private boolean start = false;
    /**level is used to record the level number to the root element in pushcontent
	 In endElement(...) we can turn start off to terminate adding content to pushcontent
	 if level is zero. That means we reach the end tag of the starting element.*/
    private int level = 0;

    /**target is used to record the target of the conref push
	 if we reach pushafter action but there is no target recorded before, we need
	 to report error.*/
    private String target = null;

    /**pushType is used to record the current type of push
	 it is used in endElement(....) to tell whether it is pushafter or replace.*/
    private String pushType = null;
        
    /**
     * Get push table
     * 
     * @return unmodifiable push table
     */
    public Map<String, Hashtable<String, String>> getPushMap() {
    	return Collections.unmodifiableMap(pushtable);
    }
    
    /**
     * @param filename filename
     */
    @Override
    public void read(final String filename) {
        filePath = new File(filename).getParentFile().getAbsolutePath();
        parsefilename = new File(filename).getName();
        start = false;
        pushcontent = new StringBuffer(INT_256);
        pushType = null;
        try{
            reader.parse(new File(filename).toURI().toString());
        }catch (final Exception e) {
            logger.logError(e.getMessage(), e) ;
        }
    }
    /**
     * Constructor.
     */
    public ConrefPushReader(){
        pushtable = new Hashtable<String, Hashtable<String,String>>();
        try{
            reader = StringUtils.getXMLReader();
            reader.setFeature(FEATURE_NAMESPACE_PREFIX, true);
            reader.setFeature(FEATURE_NAMESPACE, true);

            reader.setProperty(LEXICAL_HANDLER_PROPERTY,this);
            reader.setFeature("http://apache.org/xml/features/scanner/notify-char-refs", true);
            reader.setFeature("http://apache.org/xml/features/scanner/notify-builtin-refs", true);
            needResolveEntity = true;
            reader.setContentHandler(this);
        }catch (final Exception e) {
            throw new RuntimeException("Failed to initialize XML parser: " + e.getMessage(), e);
        }
    }

    @Override
    public void startElement(final String uri, final String localName, final String name,
            final Attributes atts) throws SAXException {
        if(start){
            //if start is true, we need to record content in pushcontent
            //also we need to add level to make sure start is turn off
            //at the corresponding end element
            level ++;
            putElement(pushcontent, name, atts, false);
        }

        final String conactValue = atts.getValue(ATTRIBUTE_NAME_CONACTION);
        if (!start && conactValue != null){
            if (ATTR_CONACTION_VALUE_PUSHBEFORE.equalsIgnoreCase(conactValue)){
                if(pushcontent.length() != 0){
                    // there are redundant "pushbefore", create a new pushcontent and emit a warning message.
                    pushcontent = new StringBuffer();
                    logger.logWarn(MessageUtils.getInstance().getMessage("DOTJ044W", atts.getValue(ATTRIBUTE_NAME_XTRF), atts.getValue(ATTRIBUTE_NAME_XTRC)).toString());
                }
                start = true;
                level =0;
                level ++;
                putElement(pushcontent, name, atts, true);
                pushType = ATTR_CONACTION_VALUE_PUSHBEFORE;
            }else if (ATTR_CONACTION_VALUE_PUSHAFTER.equalsIgnoreCase(conactValue)){
                start = true;
                level = 0;
                level ++;
                if (target == null){
                    logger.logError(MessageUtils.getInstance().getMessage("DOTJ039E", atts.getValue(ATTRIBUTE_NAME_XTRF), atts.getValue(ATTRIBUTE_NAME_XTRC)).toString());
                }else{
                    putElement(pushcontent, name, atts, true);
                    pushType = ATTR_CONACTION_VALUE_PUSHAFTER;
                }
            }else if (ATTR_CONACTION_VALUE_PUSHREPLACE.equalsIgnoreCase(conactValue)){
                start = true;
                level = 0;
                level ++;
                target = atts.getValue(ATTRIBUTE_NAME_CONREF);
                if (target == null){
                    logger.logError(MessageUtils.getInstance().getMessage("DOTJ040E", atts.getValue(ATTRIBUTE_NAME_XTRF), atts.getValue(ATTRIBUTE_NAME_XTRC)).toString());
                }else{
                    pushType = ATTR_CONACTION_VALUE_PUSHREPLACE;
                    putElement(pushcontent, name, atts, true);
                }

            }else if (ATTR_CONACTION_VALUE_MARK.equalsIgnoreCase(conactValue)){
                target = atts.getValue(ATTRIBUTE_NAME_CONREF);
                if (target != null &&
                        pushcontent != null && pushcontent.length() > 0 &&
                        ATTR_CONACTION_VALUE_PUSHBEFORE.equals(pushType)){
                    //pushcontent != null means it is pushbefore action
                    //we need to add target and content to pushtable
                    replaceContent();
                    addtoPushTable(target, pushcontent.toString(), pushType);
                    pushcontent = new StringBuffer(INT_256);
                    target = null;
                    pushType = null;
                }
            }
        }//else if (pushcontent != null && pushcontent.length() > 0 && level == 0){
        //if there is no element with conaction="mark" after
        //one with conaction="pushbefore", report syntax error

        //}
    }
    /**
     * replace content.
     */
    private void replaceContent() {
        // replace all conref and href value in pushcontent according to target
        // this is useful to "pushbefore" action because it doesn't know the target
        // when processing these content
        int index = 0;
        int nextindex = 0;
        int hrefindex = pushcontent.indexOf("href=\"", index);
        int conrefindex = pushcontent.indexOf("conref=\"", index);
        final StringBuffer resultBuffer = new StringBuffer(INT_256);
        if(hrefindex < 0 && conrefindex < 0){
            return;
        }

        while (hrefindex >= 0 ||
                conrefindex >= 0){

            if (hrefindex > 0 && conrefindex > 0){
                nextindex = hrefindex < conrefindex ? hrefindex : conrefindex;
            }else if(hrefindex > 0){
                nextindex = hrefindex;
            }else if(conrefindex > 0){
                nextindex = conrefindex;
            }

            final int valueindex = pushcontent.indexOf(QUOTATION,nextindex)+1;
            resultBuffer.append(pushcontent.substring(index, valueindex));
            resultBuffer.append(replaceURL(pushcontent.substring(valueindex, pushcontent.indexOf(QUOTATION, valueindex))));
            index = pushcontent.indexOf(QUOTATION, valueindex);

            if(hrefindex > 0){
                hrefindex = pushcontent.indexOf("href=\"", index);
            }
            if(conrefindex > 0){
                conrefindex = pushcontent.indexOf("conref=\"", index);
            }
        }

        resultBuffer.append(pushcontent.substring(index));
        pushcontent = resultBuffer;
    }
    /**
     * 
     * @param buf buffer
     * @param elemName element name
     * @param atts attribute
     * @param removeConref whether remeove conref info
     */
    private void putElement(final StringBuffer buf, final String elemName,
            final Attributes atts, final boolean removeConref) {
        //parameter boolean removeConref specifies whether to remove
        //conref information like @conref @conaction in current element
        //when copying it to pushcontent. True means remove and false means
        //not remove.
        int index = 0;
        buf.append(LESS_THAN).append(elemName);
        for (index=0; index < atts.getLength(); index++){
            if (!removeConref ||
                    !ATTRIBUTE_NAME_CONREF.equals(atts.getQName(index))&&
                    !ATTRIBUTE_NAME_CONACTION.equals(atts.getQName(index))){
                buf.append(STRING_BLANK);
                buf.append(atts.getQName(index)).append(EQUAL).append(QUOTATION);
                String value = atts.getValue(index);
                value = StringUtils.escapeXML(value);
                if (ATTRIBUTE_NAME_HREF.equals(atts.getQName(index)) ||
                        ATTRIBUTE_NAME_CONREF.equals(atts.getQName(index))){
                    // adjust href for pushbefore and replace
                    value = replaceURL(value);
                }
                buf.append(value).append(QUOTATION);
            }

        }
        //id attribute should only be added to the starting element
        //which dosen't have id attribute set
        if(ATTR_CONACTION_VALUE_PUSHREPLACE.equals(pushType) &&
                atts.getValue(ATTRIBUTE_NAME_ID) == null &&
                level == 1){
            final int sharpIndex = target.indexOf(SHARP);
            if (sharpIndex == -1){
                //if there is no '#' in target string, report error
                logger.logError(MessageUtils.getInstance().getMessage("DOTJ041E", target).toString());
            }else{
                final String targetLoc = target.substring(sharpIndex + 1);
                String id = "";
                //has element id
                if(targetLoc.contains(SLASH)){
                    id = targetLoc.substring(targetLoc.lastIndexOf(SLASH) + 1);
                }else{
                    id = targetLoc;
                }
                //add id attribute
                buf.append(STRING_BLANK);
                buf.append(ATTRIBUTE_NAME_ID).append(EQUAL).append(QUOTATION);
                buf.append(id).append(QUOTATION);
            }
        }
        buf.append(GREATER_THAN);
    }
    /**
     * 
     * @param value string
     * @return URL
     */
    private String replaceURL(final String value) {
        if(value == null){
            return null;
        }else if(target == null ||
                FileUtils.isAbsolutePath(value) ||
                value.contains(COLON_DOUBLE_SLASH) ||
                value.startsWith(SHARP)){
            return value;
        }else{
            final String source = FileUtils.resolveFile(filePath, target);
            final String urltarget = FileUtils.resolveTopic(filePath, value);
            return FileUtils.getRelativePath(source, urltarget);


        }

    }
    /**
     * 
     * @param target target
     * @param pushcontent content
     * @param type push type
     */
    private void addtoPushTable(String target, final String pushcontent, final String type) {
        int sharpIndex = target.indexOf(SHARP);
        if (sharpIndex == -1){
            //if there is no '#' in target string, report error
            logger.logError(MessageUtils.getInstance().getMessage("DOTJ041E", target).toString());
            return;
        }

        if (sharpIndex == 0){
            //means conref the file itself
            target= parsefilename+target;
            sharpIndex = target.indexOf(SHARP);
        }
        final String key = FileUtils.resolveFile(filePath, target);
        Hashtable<String, String> table = null;
        if (pushtable.containsKey(key)){
            //if there is something else push to the same file
            table = pushtable.get(key);
        }else{
            //if there is nothing else push to the same file
            table = new Hashtable<String, String>();
            pushtable.put(key, table);
        }

        final String targetLoc = target.substring(sharpIndex);
        final String addon = STICK+type;

        if (table.containsKey(targetLoc+addon)){
            //if there is something else push to the same target
            //append content if type is 'pushbefore' or 'pushafter'
            //report error if type is 'replace'
            if (ATTR_CONACTION_VALUE_PUSHREPLACE.equalsIgnoreCase(type)){
                logger.logError(MessageUtils.getInstance().getMessage("DOTJ042E", target).toString());
                return;
            }else{
                table.put(targetLoc+addon, table.get(targetLoc+addon)+pushcontent);
            }

        }else{
            //if there is nothing else push to the same target
            table.put(targetLoc+addon, pushcontent);
        }
    }

    @Override
    public void characters(final char[] ch, final int start, final int length)
            throws SAXException {
        if (this.start && needResolveEntity){
            pushcontent.append(StringUtils.escapeXML(ch, start, length));
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String name)
            throws SAXException {
        if (start){
            level --;
            pushcontent.append(LESS_THAN).append(SLASH).append(name).append(GREATER_THAN);
        }
        if (level == 0){
            //turn off start if we reach the end tag of staring element
            start = false;
            if (ATTR_CONACTION_VALUE_PUSHAFTER.equals(pushType) ||
                    ATTR_CONACTION_VALUE_PUSHREPLACE.equals(pushType)){
                //if it is pushafter or replace, we need to record content in pushtable
                //if target == null we have already reported error in startElement;
                if(target != null){
                    addtoPushTable(target, pushcontent.toString(), pushType);
                    pushcontent = new StringBuffer(INT_256);
                    target = null;
                    pushType = null;
                }
            }
        }
    }

    @Override
    public void startEntity(final String name) throws SAXException {
        try {
            needResolveEntity = StringUtils.checkEntity(name);
            if(!needResolveEntity){
                pushcontent.append(StringUtils.getEntity(name));
            }
        } catch (final Exception e) {
            //logger.logError(e.getMessage(), e) ;
        }
    }

    @Override
    public void endEntity(final String name) throws SAXException {
        if(!needResolveEntity){
            needResolveEntity = true;
        }
    }

}
