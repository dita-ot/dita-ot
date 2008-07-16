package org.dita.dost.writer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Stack;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.module.Content;
import org.dita.dost.util.Constants;
import org.dita.dost.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class ConrefPushParser extends AbstractXMLWriter {

	private Hashtable<String, String> movetable = null;
	
	//topicId keep the current topic id value
	private String topicId = null;
	
	//idStack keeps the history of topicId because topics can be nested.
	private Stack<String> idStack = null;
	
	private XMLReader parser = null;
	
	private DITAOTJavaLogger javaLogger = null;
	
	private OutputStreamWriter output = null;
	
	//topicSpecSet is used to store all kinds of names for elements which is 
	//specialized from <topic>. It is useful in endElement(...) because we don't
	//know the value of class attribute of the element when processing its end
	//tag. That's why we need to store the element's name to the set when we first
	//met it in startElement(...)
	private HashSet<String> topicSpecSet = null;
	
	//boolean isReplaced show whether current content is replace
	//because of "pushreplace" action in conref push. If the current
	//content is replaced, the output will neglect it until isReplaced
	//is turned off
	private boolean isReplaced = false;
	
	//int level is used the count the level number to the element which
	//is the starting point that is neglected because of "pushreplace" action
	//The initial value of level is 0. It will add one if element level
	//increases in startElement(....) and minus one if level decreases in 
	//endElement(...). When it turns out to be 0 again, boolean isReplaced 
	//needs to be turn off.
	private int level = 0;
	
	//boolean hasPushafter show whether there is something we need to write
	//after the current element. If so the counter levelForPushAfter should
	//count the levels to make sure we insert the push content after the right
	//end tag.
	private boolean hasPushafter = false;
	
	//int levelForPushAfter is used to count the levels to the element which
	//is the starting point for "pushafter" action. It will add one in startElement(...)
	//and minus one in endElement(...). When it turns out to be 0 again, we
	//should append the push content right after the current end tag.
	private int levelForPushAfter = 0;
	
	//levelForPushAfterStack is used to store the history value of levelForPushAfter
	//It is possible that we have pushafter action for both parent and child element.
	//In this case, we need to push the parent's value of levelForPushAfter to Stack
	//before initializing levelForPushAfter for child element. When we finished
	//pushafter action for child element, we need to restore the original value for
	//parent. As to "pushreplace" action, we don't need this because if we replaced the
	//parent, the replacement of child is meaningless.
	private Stack<Integer> levelForPushAfterStack = null;
	
	//contentForPushAfter is used to store the content that will push after the end
	//tag of the element when levelForPushAfter is decreased to zero. This is useful
	//to "pushafter" action because we don't know the value of id when processing the
	//end tag of an element. That's why we need to store the content for push after
	//into variable in startElement(...)
	private String contentForPushAfter = null;
	
	//contentForPushAfterStack is used to store the history value of contentForPushAfter
	//It is possible that we have pushafter action for both parent and child element.
	//In this case, we need to push the parent's value of contentForPushAfter to Stack
	//before getting value contentForPushAfter for child element from movetable. When we 
	//finished pushafter action for child element, we need to restore the original value for
	//parent. 
	private Stack<String> contentForPushAfterStack = null;
	
	public ConrefPushParser(){
		javaLogger = new DITAOTJavaLogger();
		topicSpecSet = new HashSet<String>();
		levelForPushAfterStack = new Stack<Integer>();
		contentForPushAfterStack = new Stack<String>();
		try{
			parser = XMLReaderFactory.createXMLReader();
			parser.setFeature(Constants.FEATURE_NAMESPACE_PREFIX, true);
			parser.setFeature(Constants.FEATURE_NAMESPACE, true);
			parser.setContentHandler(this);
		}catch (Exception e) {
			javaLogger.logException(e);
		}
	}
	
	public void setContent(Content content) {
		movetable = (Hashtable<String, String>)content.getValue();
	}

	public void write(String filename) throws DITAOTException {
		isReplaced = false;
		hasPushafter = false;
		level = 0;
		levelForPushAfter = 0;
		idStack = new Stack<String>();
		topicSpecSet = new HashSet<String>();
		levelForPushAfterStack = new Stack<Integer>();
		contentForPushAfterStack = new Stack<String>();
		try {
			File inputFile = new File(filename);
			File outputFile = new File(filename+".cnrfpush");
			output = new OutputStreamWriter(new FileOutputStream(outputFile),Constants.UTF8);
			parser.parse(filename);
			
			output.close();
            if(!inputFile.delete()){
            	Properties prop = new Properties();
            	prop.put("%1", inputFile.getPath());
            	prop.put("%2", outputFile.getPath());
            	javaLogger.logError(MessageUtils.getMessage("DOTJ009E", prop).toString());
            }
            if(!outputFile.renameTo(inputFile)){
            	Properties prop = new Properties();
            	prop.put("%1", inputFile.getPath());
            	prop.put("%2", outputFile.getPath());
            	javaLogger.logError(MessageUtils.getMessage("DOTJ009E", prop).toString());
            }
		} catch (Exception e) {
			javaLogger.logException(e);
		}finally{
			try{
				output.close();
			}catch (Exception ex) {
				javaLogger.logException(ex);
			}
		}
		
		
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if (!isReplaced){
			try{
				output.write(StringUtils.escapeXML(ch, start, length));
			}catch (Exception e) {
				javaLogger.logException(e);
			}
		}
	}

	@Override
	public void endElement(String uri, String localName, String name)
			throws SAXException {
		
		if(isReplaced){
			level--;
			if(level == 0){
				isReplaced = false;
			}
		}else{
			//write the end tag
			try{
				output.write(Constants.LESS_THAN);
				output.write(Constants.SLASH);
				output.write(name);
				output.write(Constants.GREATER_THAN);
			}catch (Exception e) {
				javaLogger.logException(e);
			}
		}
		
		if(hasPushafter){
			levelForPushAfter--;
			if(levelForPushAfter == 0){
				//write the pushcontent after the end tag
				try{
					output.write(contentForPushAfter);
				}catch (Exception e) {
					javaLogger.logException(e);
				}
				if(!levelForPushAfterStack.isEmpty() &&
						!contentForPushAfterStack.isEmpty()){
					levelForPushAfter = levelForPushAfterStack.pop().intValue();
					contentForPushAfter = contentForPushAfterStack.pop();
				}else{
					hasPushafter = false;
				}
			}
		}
		if(!idStack.isEmpty() && topicSpecSet.contains(name)){
			topicId = idStack.pop();
		}
	}

	@Override
	public void processingInstruction(String target, String data)
			throws SAXException {
		if (!isReplaced) { 
            try {
            	String pi = (data != null) ? target + Constants.STRING_BLANK + data : target;
                output.write(Constants.LESS_THAN + Constants.QUESTION 
                        + pi + Constants.QUESTION + Constants.GREATER_THAN);
            } catch (Exception e) {
            	javaLogger.logException(e);
            }
        }
	}

	@Override
	public void startElement(String uri, String localName, String name,
			Attributes atts) throws SAXException {
		if(hasPushafter){
			levelForPushAfter ++;
		}
		if(isReplaced){
			level ++;
		}else{
			try{
				String classValue = atts.getValue(Constants.ATTRIBUTE_NAME_CLASS);
				if (classValue != null && classValue.contains(Constants.ATTR_CLASS_VALUE_TOPIC)){
					if (!topicSpecSet.contains(name)){
						//add the element name to topicSpecSet if the element
						//is a topic specialization. This is used when push and pop
						//topic ids in a stack
						topicSpecSet.add(name);
					}
					String idValue = atts.getValue(Constants.ATTRIBUTE_NAME_ID);
					if (idValue != null){
						if (topicId != null){
							idStack.push(topicId);
						}				
						topicId = idValue;
					}
				}else if (atts.getValue(Constants.ATTRIBUTE_NAME_ID) != null){
					String idPath = Constants.SHARP+topicId+Constants.SLASH+atts.getValue(Constants.ATTRIBUTE_NAME_ID);
					if (movetable.containsKey(idPath+Constants.STICK+"pushbefore")){
						output.write(movetable.get(idPath+Constants.STICK+"pushbefore"));
					}
					if (movetable.containsKey(idPath+Constants.STICK+"pushreplace")){
						output.write(movetable.get(idPath+Constants.STICK+"pushreplace"));
						isReplaced = true;
						level = 0;
						level ++;
					}
					if (movetable.containsKey(idPath+Constants.STICK+"pushbeafter")){
						if (hasPushafter && levelForPushAfter > 0){
							//there is a "pushafter" action for an ancestor element.
							//we need to push the levelForPushAfter to stack before
							//initialize it.
							levelForPushAfterStack.push(new Integer(levelForPushAfter));
							contentForPushAfterStack.push(contentForPushAfter);
						}else{
							hasPushafter = true;
						}						
						levelForPushAfter = 0;
						levelForPushAfter ++;
						contentForPushAfter = movetable.get(idPath + Constants.STICK+"pushafter");
						//The output for the pushcontent will be in endElement(...)
					}
				}
			
				//although the if branch before checked whether isReplaced is true
				//we still need to check here because isReplaced might be turn on.
				if (!isReplaced){
					//output the element
					output.write(Constants.LESS_THAN);
					output.write(name);
					for(int index = 0; index < atts.getLength(); index++){
						output.write(Constants.STRING_BLANK);
						output.write(atts.getQName(index));
						output.write("=\"");
						output.write(atts.getValue(index));
						output.write("\"");
					}
					output.write(Constants.GREATER_THAN);
				}
			}catch (Exception e) {
				javaLogger.logException(e);
			}
		}
	}


	@Override
	public void endDocument() throws SAXException {
		try{
			output.flush();
			output.close();
		}catch (Exception e) {
			javaLogger.logException(e);
		}finally{
			try{
				output.close();
			}catch (Exception e) {
				javaLogger.logException(e);
			}
		}
	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {
		if(!isReplaced){
			try{
				output.write(ch, start, length);
			}catch (Exception e) {
				javaLogger.logException(e);
			}
		}
	}

	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
	}	

}
