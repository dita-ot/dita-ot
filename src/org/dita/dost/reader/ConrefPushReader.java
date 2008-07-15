package org.dita.dost.reader;

import java.io.File;
import java.util.Hashtable;

import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.module.Content;
import org.dita.dost.module.ContentImpl;
import org.dita.dost.util.Constants;
import org.dita.dost.util.FileUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class ConrefPushReader extends AbstractXMLReader {
	
	private Hashtable<String, Hashtable<String, String>> pushtable;
	
	private DITAOTJavaLogger javaLogger = null;
	
	private XMLReader reader = null;
	
	//keep the file path of current file under parse
	//filePath is useful to get the absolute path of the target file
	private String filePath = null; 
	
	//pushcontent is used to store the content copied to target
	//in pushcontent href will be resolved if it is relative path
	//if @conref is in pushconref the target name should be recorded so that it could be added to conreflist for conref resolution
	private StringBuffer pushcontent = null;
	
	//boolean start is used to control whether sax parser can start to
	//record push content into String pushcontent.
	private boolean start = false;
	//level is used to record the level number to the root element in pushcontent
	//In endElement(...) we can turn start off to terminate adding content to pushcontent
	//if level is zero. That means we reach the end tag of the starting element.
	private int level = 0;

	//target is used to record the target of the conref push
	//if we reach pushafter action but there is no target recorded before, we need
	//to report error.
	private String target = null;
	
	//pushType is used to record the current type of push
	//it is used in endElement(....) to tell whether it is pushafter or replace
	private String pushType = null;
	
	public Content getContent() {
		// TODO Auto-generated method stub
		Content content = new ContentImpl();
		content.setCollection(pushtable.entrySet());
		return content;
	}

	public void read(String filename) {
		// TODO Auto-generated method stub
		filePath = new File(filename).getParentFile().getAbsolutePath();
		start = false;
		pushcontent = new StringBuffer(Constants.INT_256);
		pushType = null;
		try{
			reader.parse(filename);
		}catch (Exception e) {
			javaLogger.logException(e);
		}
	}
	
	public ConrefPushReader(){
		pushtable = new Hashtable<String, Hashtable<String,String>>();
		javaLogger = new DITAOTJavaLogger();
		try{
			reader = XMLReaderFactory.createXMLReader();
			reader.setFeature(Constants.FEATURE_NAMESPACE_PREFIX, true);
			reader.setFeature(Constants.FEATURE_NAMESPACE, true);
		}catch (Exception e) {
			javaLogger.logException(e);
		}
		reader.setContentHandler(this);
	}

	@Override
	public void startElement(String uri, String localName, String name,
			Attributes atts) throws SAXException {
		// TODO Auto-generated method stub
		if(start){
			//if start is true, we need to record content in pushcontent
			//also we need to add level to make sure start is turn off
			//at the corresponding end element
			level ++;
			putElement(pushcontent, name, atts, false);
		}
		
		String conactValue = atts.getValue("conaction");
		if (!start && conactValue != null){
			if ("pushbefore".equalsIgnoreCase(conactValue)){
				start = true;
				putElement(pushcontent, name, atts, true);
				pushType = "pushbefore";
			}else if ("pushafter".equalsIgnoreCase(conactValue)){
				start = true;
				if (target == null){
					//TODO report error
				}else{
					putElement(pushcontent, name, atts, true);
					pushType = "pushafter";
				}
			}else if ("replace".equalsIgnoreCase(conactValue)){
				start = true;
				target = atts.getValue("conref");
				if (target == null){
					//TODO report error
				}else{
					pushType = "replace";
					putElement(pushcontent, name, atts, true);
				}
				
			}else if ("mark".equalsIgnoreCase(conactValue)){
				target = atts.getValue("conref");
				if (pushcontent != null && pushcontent.length() > 0){
					//pushcontent != null means it is pushbefore action
					//we need to add target and content to pushtable					
					addtoPushTable(target, pushcontent.toString(), pushType);
					pushcontent = new StringBuffer(Constants.INT_256);
					target = null;
					pushType = null;
				}
			}
		}else if (pushcontent != null && pushcontent.length() > 0 && level == 0){
			//if there is no element with conaction="mark" after 
			//one with conaction="pushbefore", report syntax error
			
		}
	}	

	private void putElement(StringBuffer buf, String elemName,
			Attributes atts, boolean removeConref) {
		//parameter boolean removeConref specifies whether to remove
		//conref information like @conref @conaction in current element
		//when copying it to pushcontent. True means remove and false means
		//not remove.
		// TODO Auto-generated method stub
		int index = 0;
		buf.append(Constants.LESS_THAN).append(elemName);
		for (index=0; index < atts.getLength(); index++){
			if (!removeConref || 
					!"conref".equals(atts.getQName(index))&&
							!"conaction".equals(atts.getQName(index))){
				buf.append(Constants.STRING_BLANK);
				buf.append(atts.getQName(index)).append(Constants.EQUAL).append(Constants.QUOTATION);
				String value = atts.getValue(index);
				if ("href".equals(atts.getQName(index)) ||
						"conref".equals(atts.getQName(index))){
					// adjust href for pushbefore and replace					
					value = replaceURL(value);
				}
				buf.append(value).append(Constants.QUOTATION);
			}
			
		}
		buf.append(Constants.GREATER_THAN);
	}
	
	private String replaceURL(String value) {
		if(value == null){
			return null;
		}else if(target == null || 
				FileUtils.isAbsolutePath(value) ||
				value.contains("://") ||
				value.startsWith(Constants.SHARP)){
			return value;
		}else{
			String source = FileUtils.resolveFile(filePath, target);
			String urltarget = FileUtils.resolveFile(filePath, value);
			return FileUtils.getRelativePathFromMap(source, urltarget);
		}
		
	}

	private void addtoPushTable(String target, String pushcontent, String type) {
		// TODO Auto-generated method stub
		int sharpIndex = target.indexOf(Constants.SHARP);
		if (sharpIndex == -1){
			//if there is no '#' in target string, report error
			//TODO report error of invalid target
		}
		String key = FileUtils.resolveFile(filePath, target);
		Hashtable<String, String> table = null;
		if (pushtable.containsKey(key)){
			//if there is something else push to the same file
			table = pushtable.get(key);
		}else{
			//if there is nothing else push to the same file
			table = new Hashtable<String, String>();
			pushtable.put(key, table);
		}
		
		String targetLoc = target.substring(sharpIndex+1);
		String addon = null;
		if ("pushbefore".equalsIgnoreCase(type)){
			//add filePath to addon because "pushbefore" cannot know the target
			//within the current element. href value replace should be put off
			//to ConrefPushParser
			addon = "|"+type+"|"+filePath;
		}else{
			addon = "|"+type;
		}
		if (table.containsKey(targetLoc+addon)){
			//if there is something else push to the same target
			//append content if type is 'pushbefore' or 'pushafter'
			//report error if type is 'replace'
			if ("replace".equalsIgnoreCase(type)){
				//TODO report error
			}else{
				table.put(targetLoc+addon, table.get(targetLoc+addon)+pushcontent);
			}
			
		}else{
			//if there is nothing else push to the same target
			table.put(targetLoc+addon, pushcontent);				
		}		
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		// TODO Auto-generated method stub
		if (this.start){
			pushcontent.append(ch, start, length);
		}
	}

	@Override
	public void endElement(String uri, String localName, String name)
			throws SAXException {
		if (start){
			level --;
			pushcontent.append(Constants.LESS_THAN).append(name).append(Constants.GREATER_THAN);
		}
		if (level == 0){
			//turn off start if we reach the end tag of staring element
			start = false;
			if ("pushafter".equals(pushType) ||
					"replace".equals(pushType)){
				//if it is pushafter or replace, we need to record content in pushtable
				if(target != null){
					addtoPushTable(target, pushcontent.toString(), pushType);
					pushcontent = new StringBuffer(Constants.INT_256);
					target = null;
					pushType = null;
				}else{
					//TODO report error
				}
			}
		}		
	}

}
