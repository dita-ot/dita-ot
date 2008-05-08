/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2007 All Rights Reserved.
 */
package org.dita.dost.writer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Random;
import java.util.Stack;

import javax.xml.parsers.SAXParser;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.exception.DITAOTXMLErrorHandler;
import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.module.Content;
import org.dita.dost.util.Constants;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;


public class ChunkTopicParser extends AbstractXMLWriter {

	private Hashtable changeTable = null;
	
	private HashSet refFileSet = null;
	
	private Element elem = null;
	
	private boolean separate = false;
	
	private String filePath = null;
	
	private String currentParsingFile = null;
	private String outputFile = null;
	private Stack outputFileNameStack = null;
	
	private String targetTopicId = null;
	
	private String selectMethod = "select-document";
	
	private boolean include = false;
	
	private int includelevel = 0;
	
	private HashSet topicSpecSet = null;
	
	private boolean insideCDATA = false;
	private boolean needResolveEntity = true;
	private boolean startFromFirstTopic = false;
	
	private static XMLReader reader = null;
    private static SAXParser parser = null;
    
    private Writer output = null;
    
    private StringBuffer temp = null;
    
    private Stack fileWriterStack = null;
    private Stack stubStack = null;
    
    private Element stub = null;
    
    DITAOTJavaLogger logger = null;

	private String ditaext = null;
	
	public ChunkTopicParser() {
		super();
		topicSpecSet = new HashSet(Constants.INT_16);
		insideCDATA = false;
		needResolveEntity = true;
		fileWriterStack = new Stack();
		stubStack = new Stack();
		outputFileNameStack = new Stack();
		logger=new DITAOTJavaLogger();
	}

	public void characters(char[] ch, int start, int length) throws SAXException {
		if (include && needResolveEntity) { 
            try {
            	if(insideCDATA)
            		output.write(ch, start, length);
            	else
            		output.write(StringUtils.escapeXML(ch,start, length));
            } catch (Exception e) {
            	logger.logException(e);
            }
        }
	}

	public void comment(char[] ch, int start, int length) throws SAXException {
		// TODO Auto-generated method stub
		super.comment(ch, start, length);
	}

	public void endDocument() throws SAXException {
		include = false;
	}


	public void endElement(String uri, String localName, String qName) throws SAXException {
		if(include){
			try{
				includelevel--;
				if(includelevel >= 0){
					// prevent adding </dita> into output
					output.write("</"+qName+">");
				}
				if (includelevel == 0 &&
						!"select-document".equals(selectMethod)){
					include = false;
				}
				if (topicSpecSet.contains(qName) && 
						separate && !fileWriterStack.isEmpty()){
					// if it is end of topic and separate is true
					output.close();
					output = (OutputStreamWriter)fileWriterStack.pop();
					outputFile = (String)outputFileNameStack.pop();
					stub.getParentNode().removeChild(stub);
					stub = (Element)stubStack.pop();
				}
			}catch (Exception e) {
				logger.logException(e);
			}
		}
		
	}

	public void endEntity(String name) throws SAXException {
		if(!needResolveEntity){
			needResolveEntity = true;
		}
	}

	public void endPrefixMapping(String prefix) throws SAXException {
		// TODO Auto-generated method stub
		super.endPrefixMapping(prefix);
	}

	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
		if (include) {
            try {
                output.write(ch, start, length);
            } catch (Exception e) {
            	logger.logException(e);
            }
        }
	}

	public void processingInstruction(String target, String data) throws SAXException {
		if(include){
			try {
	        	String pi = (data != null) ? target + Constants.STRING_BLANK + data : target;
	            output.write(Constants.LESS_THAN + Constants.QUESTION 
	                    + pi + Constants.QUESTION + Constants.GREATER_THAN);
	        } catch (Exception e) {
	        	logger.logException(e);
	        }	
		}
	}

	public void setContent(Content content) {
		// TODO Auto-generated method stub
		super.setContent(content);
	}

	public void skippedEntity(String name) throws SAXException {
		if(include){
			try {
	            output.write(StringUtils.getEntity(name));
	        } catch (Exception e) {
	        	logger.logException(e);
	        }
		}		
	}

	public void startDocument() throws SAXException {
		if ("select-document".equals(selectMethod)){
			if(currentParsingFile.equals(outputFile)){
				// if current file serves as root of new chunk
				// include will be set to true in startDocument()
				// in order to copy PIs and <dita> element
				// otherwise, if current file is copied to other file
				// do not copy PIs and <dita>element
				include = true;
			}else{
				include = false;
				startFromFirstTopic = true;
			}
		}
		
	}

	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		// TODO Auto-generated method stub
		String classValue = atts.getValue(Constants.ATTRIBUTE_NAME_CLASS);
		String idValue = atts.getValue(Constants.ATTRIBUTE_NAME_ID);
		try{
			if(classValue!=null && classValue.indexOf(Constants.ATTR_CLASS_VALUE_TOPIC)!=-1){
				topicSpecSet.add(qName);
				if (separate && include){
					fileWriterStack.push(output);
					outputFileNameStack.push(outputFile);
					Random random = new Random();
					String newFileName = FileUtils.resolveFile(filePath,"Chunk"
							+new Integer(Math.abs(random.nextInt())).toString())+ditaext;
					outputFile = newFileName;
					output = new OutputStreamWriter(
							new FileOutputStream(newFileName)
							,Constants.UTF8);
					//write xml header and workdir PI to the new generated file
					output.write(Constants.XML_HEAD);
					output.write("<?workdir /"+filePath+"?>");
					changeTable.put(newFileName,newFileName);
					if(idValue != null){
						changeTable.put(currentParsingFile+Constants.SHARP+idValue,
								newFileName+Constants.SHARP+idValue);
					}else{
						changeTable.put(currentParsingFile, newFileName);
					}
					//create a new child element
					//in separate case elem is equals to parameter
					//element in separateChunk(Element element)
					Element newChild = elem.getOwnerDocument()
					.createElement(Constants.ELEMENT_NAME_TOPICREF);
					newChild.setAttribute(Constants.ATTRIBUTE_NAME_HREF,
							FileUtils.getRelativePathFromMap(filePath+Constants.SLASH+"stub.ditamap"
									,newFileName));
					if(stub!=null){
						stub.getParentNode().insertBefore(newChild,stub);
						stubStack.push(stub);
						stub = (Element)stub.cloneNode(false);
						newChild.appendChild(stub);
					}
				}
				if(include && "select-topic".equals(selectMethod)){
					//if select method is "select-topic" and
					//current topic is the nested topic in 
					//target topic.
					include = false;
					output.write("</"+qName+">");
				}else if(include){
					//if select method is "select-document" or "select-branch"
					// and current topic is the nested topic in target topic.
					// if file name has been changed, add an entry in changeTable
					if(!currentParsingFile.equals(outputFile)){
						if(idValue != null){
							changeTable.put(currentParsingFile+Constants.SHARP+idValue,
									outputFile+Constants.SHARP+idValue);
						}else{
							changeTable.put(currentParsingFile, outputFile);
						}
					}
				}
				
				else if(!include && idValue!=null &&
						(idValue.equals(targetTopicId) ||
								startFromFirstTopic)){	
					//if the target topic has not been found and 
					//current topic is the target topic
					include = true;
					includelevel = 0;
					startFromFirstTopic = false;
					if(!currentParsingFile.equals(outputFile)){
						changeTable.put(currentParsingFile+Constants.SHARP+idValue,
								outputFile+Constants.SHARP+idValue);
					}
				}		
			}		
			
			if(include){
				includelevel++;
				output.write("<"+qName);
				for(int i = 0; i<atts.getLength();i++){
					String attrName = atts.getQName(i);
					String attrValue = atts.getValue(i);
					if(Constants.ATTRIBUTE_NAME_HREF.equals(attrName)){
						//update @href value
						output.write(Constants.STRING_BLANK);
						output.write(Constants.ATTRIBUTE_NAME_HREF);
						output.write("=\"");
						if(checkHREF(atts)){
							// if current @href value needs to be updated
							String relative = FileUtils.getRelativePathFromMap(outputFile,currentParsingFile);
							if(attrValue.startsWith(Constants.SHARP)){
								// if @href refers to a location inside current parsing file
								// update @href to point back to current file
								// if the location is moved to chunk, @href will be update again
								// to the new location.
								output.write(relative+attrValue);
							}else if (relative.indexOf(Constants.SLASH)!=-1){
								// if new file is not under the same directory with current file
								// add path information to the @href value
								relative = relative.substring(0,relative.lastIndexOf(Constants.SLASH));						
								output.write(FileUtils.resolveTopic(relative,attrValue));
							}else{
								// if new file is under the same directory with current file
								// do not update the @href value
								output.write(attrValue);
							}
						}else{
							// if current @href value does not need to be updated
							output.write(attrValue);
						}
						
						output.write("\"");
					}else{
						output.write(Constants.STRING_BLANK);
						output.write(attrName);
						output.write("=\"");
						output.write(attrValue);
						output.write("\"");
					}
				}
				output.write(">");
			}
		}catch(Exception e){
			logger.logException(e);
		}
	}

	public void startEntity(String name) throws SAXException {
		if (include) {
            try {
            	needResolveEntity = StringUtils.checkEntity(name);
            	if(!needResolveEntity){
            		output.write(StringUtils.getEntity(name));
            	}
            } catch (Exception e) {
            	logger.logException(e);
            }
        }
	}


	public void endCDATA() throws SAXException {
		insideCDATA = false;
	    try{
	        output.write(Constants.CDATA_END);
	    }catch(Exception e){
	    	logger.logException(e);
	    }
	}

	public void startCDATA() throws SAXException {
		try{
	    	insideCDATA = true;
	        output.write(Constants.CDATA_HEAD);
	    }catch(Exception e){
	    	logger.logException(e);
	    }
	}

	public void write(String filename) throws DITAOTException {
		// pass map's directory path 
		filePath = filename;
		needResolveEntity = true;
		if(!separate){
			output = new StringWriter();
			processChunk(elem,null);
		}else{
			separateChunk(elem);
		}	
	}

	private void separateChunk(Element element) {
		// TODO Auto-generated method stub
		// TODO implement separat = true logic
		String hrefValue = element.getAttribute(Constants.ATTRIBUTE_NAME_HREF);
		String copytoValue = element.getAttribute(Constants.ATTRIBUTE_NAME_COPY_TO);
		String parseFilePath = null;
		Writer tempOutput = null;
		
		
		
		if (!copytoValue.equals(Constants.STRING_EMPTY)){
			if (hrefValue.indexOf(Constants.SHARP)!=-1){
				parseFilePath = copytoValue + hrefValue.substring(hrefValue.indexOf(Constants.SHARP));
			}else{
				parseFilePath = copytoValue;
			}
		}else{
			parseFilePath = hrefValue;
		}
		try {
			if (parseFilePath != null && !parseFilePath.equals(Constants.STRING_EMPTY)){
				// if the path to target file make sense
				currentParsingFile = FileUtils.resolveFile(filePath,parseFilePath);
				tempOutput = output;
				output = new OutputStreamWriter(new FileOutputStream(
						currentParsingFile+".chunk"),
						Constants.UTF8);
				outputFile = currentParsingFile+".chunk";
				targetTopicId = null;
				include = false;
				selectMethod = "select-document";
				stub = element.getOwnerDocument().createElement("stub");
				if(element.hasChildNodes()){
					element.insertBefore(stub,element.getFirstChild());
				}else{
					element.appendChild(stub);
				}
				reader.setErrorHandler(new DITAOTXMLErrorHandler(currentParsingFile));
				reader.parse(currentParsingFile);
				output.flush();
				
			}
		}catch (Exception e) {
			logger.logException(e);
		}finally{
			try{
				if(output!=null){
					output.close();
					if(!new File(currentParsingFile).delete()){
		            	Properties prop = new Properties();
		            	prop.put("%1", currentParsingFile);
		            	prop.put("%2", outputFile);
		            	logger.logError(MessageUtils.getMessage("DOTJ009E", prop).toString());
		            }
		            if(!new File(outputFile).renameTo(new File(currentParsingFile))){
		            	Properties prop = new Properties();
		            	prop.put("%1", currentParsingFile);
		            	prop.put("%2", outputFile);
		            	logger.logError(MessageUtils.getMessage("DOTJ009E", prop).toString());
		            }
				}
				output = tempOutput;
			}catch (Exception ex) {
				logger.logException(ex);
			}
			
		}
		
	}

	private void processChunk(Element element, String outputFile) {
		// TODO Auto-generated method stub
		String hrefValue = element.getAttribute(Constants.ATTRIBUTE_NAME_HREF);
		String chunkValue = element.getAttribute(Constants.ATTRIBUTE_NAME_CHUNK);
		String copytoValue = element.getAttribute(Constants.ATTRIBUTE_NAME_COPY_TO);
		String parseFilePath = null;
		String outputFileName = outputFile;
		Writer tempWriter = null;
		
		targetTopicId = null;
		selectMethod = "select-document";
		include = false;
		
		try {			
			if (!copytoValue.equals(Constants.STRING_EMPTY)){
				if (hrefValue.indexOf(Constants.SHARP)!=-1){
					parseFilePath = copytoValue + hrefValue.substring(hrefValue.indexOf(Constants.SHARP));
				}else{
					parseFilePath = copytoValue;
				}
			}else{
				parseFilePath = hrefValue;
			}
		
			if (parseFilePath != null && !parseFilePath.equals(Constants.STRING_EMPTY)){
				// if the path to target file make sense
				if(chunkValue.indexOf("to-content")!=-1){
					//if current element cotains "to-content" in chunk attribute
					//we need to create new buffer and flush the buffer to file
					//after processing is finished					
					tempWriter = output;
					output = new StringWriter();	
					if (!copytoValue.equals(Constants.STRING_EMPTY)){
						// use @copy-to value as the new file name
						outputFileName = FileUtils.resolveFile(filePath,copytoValue);
					}else if (!hrefValue.equals(Constants.STRING_EMPTY)
							&& hrefValue.indexOf(Constants.SHARP)==-1){
						// use @href value as the new file name
						outputFileName = FileUtils.resolveFile(filePath,hrefValue);
					}else{
						// use randomly generated file name
						Random random = new Random();
						outputFileName = FileUtils.resolveFile(filePath,"Chunk"
								+new Integer(Math.abs(random.nextInt())).toString())+ditaext;
						// add newly generated file to changTable
						// the new entry in changeTable has same key and value
						// in order to indicate it is a newly generated file
						changeTable.put(outputFileName,outputFileName);
					}					
				}
				
				this.outputFile = outputFileName;
				
				if(!FileUtils.resolveFile(filePath,parseFilePath).equals(outputFileName)){
					String path = FileUtils.resolveTopic(filePath,parseFilePath);
					String newpath = null;
					if(path.indexOf(Constants.SHARP)!=-1){
						newpath = outputFileName + path.substring(path.indexOf(Constants.SHARP));
					}else{
						newpath = outputFileName;
					}
					// add file name changes to changeTable
					changeTable.put(path, newpath);
					// update current element's @href value
					element.setAttribute(Constants.ATTRIBUTE_NAME_HREF,
							FileUtils.getRelativePathFromMap(filePath+Constants.SLASH+"stub.ditamap"
									,newpath));
				}
			
				if(parseFilePath.indexOf(Constants.SHARP)!=-1){
					targetTopicId = parseFilePath.substring(parseFilePath.indexOf(Constants.SHARP)+1);
				}
				
				if(chunkValue.indexOf("select")!=-1){
					int endIndex = chunkValue.indexOf(Constants.STRING_BLANK,
							chunkValue.indexOf("select"));
					if (endIndex ==-1){
						// if there is no space after select-XXXX in chunk attribute
						selectMethod = chunkValue.substring(chunkValue.indexOf("select"));
					}else{
						selectMethod = chunkValue.substring(chunkValue.indexOf("select"),
								endIndex);
					}
					
					if ("select-topic".equals(selectMethod) ||
							"select-branch".equals(selectMethod)){
						//if the current topic href referred to a entire topic file,it will be handled in "document" level.
						if(targetTopicId == null){
							selectMethod = "select-document";
						}
					}
					
				}
				String tempPath = currentParsingFile;
				currentParsingFile = FileUtils.resolveFile(filePath,parseFilePath);
				
				reader.parse(currentParsingFile);
				currentParsingFile = tempPath;
			
				if (element.hasChildNodes() && 
						((StringWriter)output).getBuffer().length()>0){
					//if current element has child nodes and chunk results for this element has value
					//which means current element makes sense for chunk action.
					StringWriter temp = (StringWriter)output;
					output = new StringWriter();
					NodeList children = element.getChildNodes();
					for (int i = 0; i < children.getLength(); i++){
						Node current = children.item(i);
						if (current.getNodeType() == Node.ELEMENT_NODE
								&& ((Element)current).getAttribute(Constants.ATTRIBUTE_NAME_CLASS)
								.indexOf(Constants.ATTR_CLASS_VALUE_TOPICREF)!=-1){
							processChunk((Element)current,outputFileName);
						}				
					}
					
					// merge results
					StringBuffer parentResult = temp.getBuffer();
					int insertpoint = parentResult.lastIndexOf("</");
					int end = parentResult.indexOf(">",insertpoint);
					
					if(insertpoint==-1 || end==-1){
						Properties prop=new Properties();
						prop.put("%1", hrefValue);
						logger.logError(MessageUtils.getMessage("DOTJ033E",prop).toString());
					}else{
					
						if (Constants.ELEMENT_NAME_DITA.equalsIgnoreCase(parentResult.substring(insertpoint,end).trim())){
							insertpoint = parentResult.lastIndexOf("</",insertpoint);
						}
						parentResult.insert(insertpoint,((StringWriter)output).getBuffer());
					}
					//restore back to parent's output
					output = temp;
					
				}
				
				if(chunkValue.indexOf("to-content")!=-1){
					//flush the buffer to file after processing is finished
					//and restore back original output
					
										
					FileOutputStream fileOutput = new FileOutputStream(outputFileName);
					OutputStreamWriter ditaFileOutput = new OutputStreamWriter(fileOutput, Constants.UTF8);
					if (outputFileName.equals(changeTable.get(outputFileName))){
						// if the output file is newly generated file
						// write the xml header and workdir PI into new file
						ditaFileOutput.write(Constants.XML_HEAD);
						ditaFileOutput.write("<?workdir /"+new File(outputFileName).getParent()+"?>");
					}					
					ditaFileOutput.write(((StringWriter)output).getBuffer().toString());
					ditaFileOutput.flush();
					ditaFileOutput.close();
					// restore back original output
					output = tempWriter;
				}
			}else{
				logger.logError(MessageUtils.getMessage("DOTJ032E").toString());
			}
			
		} catch (Exception e) {
			logger.logException(e);
		}
	
	}

	public void setup(Hashtable changeTable, HashSet refFileSet, Element elem, boolean separate, String ditaext) {
		// Initialize ChunkTopicParser
		this.changeTable = changeTable;
		this.refFileSet = refFileSet;
		this.elem = elem;
		this.separate = separate;
		this.ditaext  = ditaext;
		
		logger=new DITAOTJavaLogger();
		if (System.getProperty(Constants.SAX_DRIVER_PROPERTY) == null) {
			// The default sax driver is set to xerces's sax driver
			StringUtils.initSaxDriver();
		}
		
		try {
			if (System.getProperty(Constants.SAX_DRIVER_PROPERTY) == null){
                //The default sax driver is set to xerces's sax driver
            	StringUtils.initSaxDriver();
            }
            reader = XMLReaderFactory.createXMLReader();
            reader.setContentHandler(this);
            reader.setProperty(Constants.LEXICAL_HANDLER_PROPERTY,this);
            reader.setFeature(Constants.FEATURE_NAMESPACE_PREFIX, true);	
		} catch (Exception e) {
			logger.logException(e);
		}
	}
	
	private boolean checkHREF(Attributes atts){
    	// check whether current href needs to be updated
    	String scopeValue = atts.getValue(Constants.ATTRIBUTE_NAME_SCOPE);
    	String hrefValue = atts.getValue(Constants.ATTRIBUTE_NAME_HREF);
    	
    	if (scopeValue == null){
    		scopeValue = Constants.ATTR_SCOPE_VALUE_LOCAL;
    	}
    	
    	if (hrefValue == null || hrefValue.indexOf("://")!=-1){    		
    		return false;
    	}
    	
    	if (scopeValue != null && 
    			scopeValue.equalsIgnoreCase(Constants.ATTR_SCOPE_VALUE_EXTERNAL)){
    		return false;
    	}
    	
    	return true;
    }

}
