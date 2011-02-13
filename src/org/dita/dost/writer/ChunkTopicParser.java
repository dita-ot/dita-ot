/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2007 All Rights Reserved.
 */
package org.dita.dost.writer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.Stack;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.exception.DITAOTXMLErrorHandler;
import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.module.Content;
import org.dita.dost.util.Constants;
import org.dita.dost.util.DITAAttrUtils;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.StringUtils;
import org.dita.dost.util.TopicIdParser;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * ChunkTopicParser class, writing chunking content into relative topic files
 * and then update list.
 * 
 *
 */
public class ChunkTopicParser extends AbstractXMLWriter {

    private static final String OS_NAME_WINDOWS = "windows";
    private static final String PI_END = "?>";
    private static final String PI_WORKDIR_HEAD = "<?workdir ";
    //edited by william on 2009-09-10 for maintain iteration order start
	private LinkedHashMap<String,String> changeTable = null;
	//edited by william on 2009-09-10 for maintain iteration order start
	private Hashtable<String,String> conflictTable = null;
	
	private Element elem = null;
	
	//Added by William on 20100628 for bug:3020314 start
	private Element topicDoc = null;
	//Added by William on 20100628 for bug:3020314 end
	
	private boolean separate = false;
	
	private String filePath = null;
	
	private String currentParsingFile = null;
	private String outputFile = null;
	private Stack<String> outputFileNameStack = null;
	
	private String targetTopicId = null;
	
	private String selectMethod = "select-document";
	//flag whether output the nested nodes
	private boolean include = false;
	private boolean skip = false;
	
	private int includelevel = 0;
	private int skipLevel = 0;
	
	private HashSet<String> topicSpecSet = null;
	
	private boolean insideCDATA = false;
	private boolean needResolveEntity = true;
	private boolean startFromFirstTopic = false;
	
	private static XMLReader reader = null;
    private Writer output = null;
    
    private StringBuffer temp = null;
    
    private Stack<Writer> fileWriterStack = null;
    private Stack<Element> stubStack = null;
    
    //stub is used as the anchor to mark where to insert generated child topicref inside current topicref
    private Element stub = null;
    
    //siblingStub is similar to stub. The only different is it is used to insert generated topicref sibling to current topicref
    private Element siblingStub = null;
    
    DITAOTJavaLogger logger = null;

	private String ditaext = null;
	
	private Set<String> topicID;
	
	private Set<String> copyto;
	
	private Set<String>	copytoSource;
	
	private HashMap<String,String>	copytotarget2source;
	
	// Added on 2010-11-12 for bug 3090803 start
	private HashMap<String, String> currentParsingFileTopicIDChangeTable;
	// Added on 2010-11-12 for bug 3090803 end
	
	private final String ditaarchNSQName = "xmlns:ditaarch";
	private final String ditaarchNSValue = "http://dita.oasis-open.org/architecture/2005/";
	/**
	 * Constructor.
	 */
	public ChunkTopicParser() {
		super();
		topicSpecSet = new HashSet<String>(Constants.INT_16);
		insideCDATA = false;
		needResolveEntity = true;
		fileWriterStack = new Stack<Writer>();
		stubStack = new Stack<Element>();
		outputFileNameStack = new Stack<String>();
		logger=new DITAOTJavaLogger();
		topicID = new HashSet<String>();
		copyto = new HashSet<String>();
		copytoSource = new HashSet<String>();
		copytotarget2source = new HashMap<String,String>();
		new HashSet<String>();
	}
	@Override
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
	@Override
	public void comment(char[] ch, int start, int length) throws SAXException {
		super.comment(ch, start, length);
	}
	@Override
	public void endDocument() throws SAXException {
		include = false;
		skip = false;
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (skip && skipLevel > 0) {
			skipLevel--;
		} else if (skip) {
			include = true;
			skip = false;
			skipLevel = 0;
		}
		
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
					try {
						output.close();
					} catch (IOException e) {
						logger.logException(e);
					}
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
	@Override
	public void endEntity(String name) throws SAXException {
		if(!needResolveEntity){
			needResolveEntity = true;
		}
	}
	@Override
	public void endPrefixMapping(String prefix) throws SAXException {
		super.endPrefixMapping(prefix);
	}
	@Override
	public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
		if (include) {
            try {
                output.write(ch, start, length);
            } catch (Exception e) {
            	logger.logException(e);
            }
        }
	}
	@Override
	public void processingInstruction(String target, String data) throws SAXException {
		if(include || 
				"workdir".equalsIgnoreCase(target) ||
				"path2proj".equalsIgnoreCase(target)){
			try {
	        	String pi = (data != null) ? target + Constants.STRING_BLANK + data : target;
	            output.write(Constants.LESS_THAN + Constants.QUESTION 
	                    + pi + Constants.QUESTION + Constants.GREATER_THAN);
	        } catch (Exception e) {
	        	logger.logException(e);
	        }	
		}
	}
	@Override
	public void setContent(Content content) {
		super.setContent(content);
	}
	@Override
	public void skippedEntity(String name) throws SAXException {
		if(include){
			try {
	            output.write(StringUtils.getEntity(name));
	        } catch (Exception e) {
	        	logger.logException(e);
	        }
		}		
	}
	@Override
	public void startDocument() throws SAXException {
		//dontWriteDita = true;
		//difference between to-content & select-topic
		if ("select-document".equals(selectMethod)){
			//currentParsingFile can never equal outputFile except when chunk="to-content"
			//is set at map level
			//TODO former is set and line 606(895) and the later is set at line 838
			if((currentParsingFile).equals(outputFile)){
				// if current file serves as root of new chunk
				// include will be set to true in startDocument()
				// in order to copy PIs and <dita> element
				// otherwise, if current file is copied to other file
				// do not copy PIs and <dita>element
				
				include = true;
				skip = false;
				skipLevel = 0;
			}else{
				include = false;
				startFromFirstTopic = true;
				skip = false;
				skipLevel = 0;
			}
		}
		
	}
	@Override
	public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
		String classValue = atts.getValue(Constants.ATTRIBUTE_NAME_CLASS);
		String idValue = atts.getValue(Constants.ATTRIBUTE_NAME_ID);
		
		if (skip && skipLevel > 0) {
			skipLevel++;
		}
		
		try{
			if(classValue!=null && classValue.indexOf(Constants.ATTR_CLASS_VALUE_TOPIC)!=-1){
				topicSpecSet.add(qName);
				//Added by William on 20100628 for bug:3020314 start
				String id = atts.getValue(Constants.ATTRIBUTE_NAME_ID);
				//search node by id.
				Element element = DITAAttrUtils.getInstance().
				searchForNode(topicDoc, id, Constants.ATTRIBUTE_NAME_ID, Constants.ATTR_CLASS_VALUE_TOPIC);
				//Added by William on 20100628 for bug:3020314 end
				
				//only by topic
				if (separate && include && !"select-topic".equals(selectMethod)){
					//chunk="by-topic" and next topic element found
					fileWriterStack.push(output);
					outputFileNameStack.push(outputFile);
					Random random = new Random();
					
					//need generate new file based on new topic id
					String newFileName = FileUtils.resolveFile(filePath, idValue+ditaext);
					if(StringUtils.isEmptyString(idValue) || FileUtils.fileExists(newFileName)) {
						String t = newFileName;
						newFileName = FileUtils.resolveFile(filePath,"Chunk"
								+ random.nextInt(Integer.MAX_VALUE)) + ditaext;
						conflictTable.put(newFileName, t);
					} 
					outputFile = newFileName;
					output = new OutputStreamWriter(
							new FileOutputStream(newFileName)
							,Constants.UTF8);
					//write xml header and workdir PI to the new generated file
					output.write(Constants.XML_HEAD);
					if(Constants.OS_NAME.toLowerCase().indexOf(OS_NAME_WINDOWS)==-1)
		            {
		                output.write(PI_WORKDIR_HEAD + filePath + PI_END);
		            }else{
		                output.write(PI_WORKDIR_HEAD + Constants.SLASH + filePath + PI_END);
		            }
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

					newChild.setAttribute(Constants.ATTRIBUTE_NAME_CLASS,
							"-" + Constants.ATTR_CLASS_VALUE_TOPICREF);
					newChild.setAttribute(Constants.ATTRIBUTE_NAME_XTRF, "generated_by_chunk");
					
					//Added by William on 20100628 for bug:3020314 start
					createTopicMeta(element, newChild);
					//Added by William on 20100628 for bug:3020314 end
					
					if(stub!=null){
						if (includelevel == 0 && siblingStub != null){
							//if it is the following sibling topic to the first topic in ditabase
							//The first topic will not enter the logic at here because when meeting
							//with first topic in ditabase, the include value is false
							siblingStub.getParentNode().insertBefore(newChild, siblingStub);
							
						}else{
							stub.getParentNode().insertBefore(newChild,stub);
						}
						//<element>
						//  <newchild/>
						//      <stub/>
						//  <stub/>
						//  ...
						//</element>  
						//<siblingstub/>
						//...
						stubStack.push(stub);
						stub = (Element)stub.cloneNode(false);
						newChild.appendChild(stub);
					}
				}
				if(include && "select-topic".equals(selectMethod)){
					//if select method is "select-topic" and
					//current topic is the nested topic in 
					//target topic-->skip it.
					include = false;
					skipLevel = 1;
					skip = true;
					// Fix: no need to close the tag, just skip the nested topic.
					//output.write("</"+qName+">");
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
				} else if(skip) {
					skipLevel = 1;
				} else if(!include && idValue!=null &&
						(idValue.equals(targetTopicId) ||
								startFromFirstTopic)){	
					//if the target topic has not been found and 
					//current topic is the target topic
					include = true;
					includelevel = 0;
					skip = false;
					skipLevel = 0;
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
					
					//Added by William on 2009-08-18 for chunkbug id:2839035 start
					attrValue = StringUtils.escapeXML(attrValue);
			    	//Added by William on 2009-08-18 for chunkbug id:2839035 end
					
					if(Constants.ATTRIBUTE_NAME_ID.equals(attrName) && classValue.indexOf(Constants.ATTR_CLASS_VALUE_TOPIC)!=-1){
						//change topic @id if there are conflicts. 
						if(topicID.contains(attrValue)){
							// Added on 2010-11-12 for bug 3090803 start
							String oldAttrValue = attrValue;
							// Added on 2010-11-12 for bug 3090803 end
							Random random = new Random();
							attrValue = "unique_" + random.nextInt(Integer.MAX_VALUE);
							topicID.add(attrValue);
							// Added on 2010-11-12 for bug 3090803 start
							currentParsingFileTopicIDChangeTable.put(oldAttrValue, attrValue);
							// Added on 2010-11-12 for bug 3090803 end
						}else
							topicID.add(attrValue);
					}
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
				
				if (classValue != null && 
						classValue.contains(Constants.ATTR_CLASS_VALUE_TOPIC) &&
						atts.getValue("xmlns:ditaarch") == null){
					//if there is none declaration for ditaarch namespace, 
					//processor need to add it
					output.write(Constants.STRING_BLANK);
					output.write(ditaarchNSQName);
					output.write("=\"");
					output.write(ditaarchNSValue);
					output.write("\"");
				}
				
				output.write(">");
			}
			
		}catch(Exception e){
			logger.logException(e);
		}
	}
	@Override
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

	@Override
	public void endCDATA() throws SAXException {
		insideCDATA = false;
	    try{
	        output.write(Constants.CDATA_END);
	    }catch(Exception e){
	    	logger.logException(e);
	    }
	}
	@Override
	public void startCDATA() throws SAXException {
		try{
	    	insideCDATA = true;
	        output.write(Constants.CDATA_HEAD);
	    }catch(Exception e){
	    	logger.logException(e);
	    }
	}
	@Override
	public void write(String filename) throws DITAOTException {
		// pass map's directory path 
		filePath = filename;
		needResolveEntity = true;
		//not chunk "by-topic"
		if(!separate){
			//TODO create the initial output
			output = new StringWriter();
			processChunk(elem,null);
		}else{
			//chunk "by-topic"
			separateChunk(elem);
		}
		if(!copyto.isEmpty()){
			updateList(); // now only update copyto list and does not update any topic ditamap ditamaptopic list
		}
	}

	private void updateList(){
		//in the special case of the concurrence of copy-to and chunk='to-content', @copy-to is handled in chunk module 
		//instead of genlist module and debugandfilter module, so the list should be updated.
		//and this method is used to update the list file. 
		Properties property = new Properties();
		InputStream in = null;
		FileOutputStream output = null;
		FileOutputStream xmlDitaList = null;
		String key = null;
		String filename = null;
		BufferedWriter bufferedWriter = null;
		try{
			in = new FileInputStream(new File(FileUtils.resolveFile(filePath,Constants.FILE_NAME_DITA_LIST_XML)));
			property.loadFromXML(in);
			output = new FileOutputStream(new File(FileUtils.resolveFile(filePath, Constants.FILE_NAME_DITA_LIST)));
			xmlDitaList = new FileOutputStream(new File(FileUtils.resolveFile(filePath, Constants.FILE_NAME_DITA_LIST_XML)));
			String copytosourcelist[] = property.getProperty(Constants.COPYTO_SOURCE_LIST).split(Constants.COMMA);
			String copytotarget2sourcemaplist[] = property.getProperty(Constants.COPYTO_TARGET_TO_SOURCE_MAP_LIST).split(Constants.COMMA);
			//in the following, all the 4 arrays are updated according to the set copyto and 
			//map copytotarget2source.
			
//			//copy all the file name in copytosourcelist to a new set
			for(String source:copytosourcelist){
				copytoSource.add(source);
			}
			//copy all the copytotarget2sourcemaplist to a new hashmap
			for(String target2source:copytotarget2sourcemaplist){
				if(target2source.indexOf(Constants.EQUAL)!=-1)
					copytotarget2source.put(target2source.substring(0, target2source.indexOf(Constants.EQUAL)), target2source.substring(target2source.indexOf(Constants.EQUAL)-1));
			}
			//in the case of chunk='to-content' and copy-to='*.dita' 
			//the @href value are added in fullditatopic and fullditamapandtopic, 
			//while they are not supposed to be contained, so should be be removed 
			
			temp = new StringBuffer();
			Iterator<String> it = copytoSource.iterator();
			filename = Constants.COPYTO_SOURCE_LIST.substring(Constants.INT_0, Constants.COPYTO_SOURCE_LIST
					.lastIndexOf("list"))
					+ ".list";
			
			try {
    			bufferedWriter = new BufferedWriter(
    					new OutputStreamWriter(new FileOutputStream(new File(FileUtils.resolveFile(filePath, filename)
    							))));
    			while(it.hasNext()){
    				key = it.next();
    				temp.append(key);
    				if(it.hasNext())
    					temp.append(Constants.COMMA);
    					bufferedWriter.append("\n");
    			}
    			property.setProperty(Constants.COPYTO_SOURCE_LIST, temp.toString());
    			bufferedWriter.flush();
    		} finally {
    			if (bufferedWriter != null) {
    				bufferedWriter.close();
    			}
    		}
			
			temp = new StringBuffer();
			it = copytotarget2source.keySet().iterator();
			filename = Constants.COPYTO_TARGET_TO_SOURCE_MAP_LIST.substring(Constants.INT_0, Constants.COPYTO_TARGET_TO_SOURCE_MAP_LIST
					.lastIndexOf("list"))
					+ ".list";
			bufferedWriter = null;
			try {
    			bufferedWriter = new BufferedWriter(
    					new OutputStreamWriter(new FileOutputStream(new File(FileUtils.resolveFile(filePath, filename)
    							))));
    			while(it.hasNext()){
    				key = it.next();
    				temp.append(key).append(Constants.EQUAL).append(copytotarget2source.get(key));
    				bufferedWriter.append(key).append(Constants.EQUAL).append(copytotarget2source.get(key));
    				if(it.hasNext())
    					temp.append(Constants.COMMA);
    					bufferedWriter.append("\n");
    			}
    			property.setProperty(Constants.COPYTO_TARGET_TO_SOURCE_MAP_LIST, temp.toString());
    			bufferedWriter.flush();
			} finally {
				bufferedWriter.close();
			}
			
    		property.store(output, null);
    		property.storeToXML(xmlDitaList, null);
    		
		}catch (Exception e){
			//edited by Alan on Date:2009-11-02 for Work Item:#1590 start
			/*logger.logWarn(e.toString());*/
			logger.logException(e);
			//edited by Alan on Date:2009-11-02 for Work Item:#1590 end
		} finally {
        	if (in != null) {
        		try {
        			in.close();
        		} catch (IOException e) {
        			logger.logException(e);
        		}
        	}
        	if (output != null) {
        		try {
        			output.close();
        		} catch (IOException e) {
        			logger.logException(e);
        		}
        	}
        	if (xmlDitaList != null) {
        		try {
        			xmlDitaList.close();
        		} catch (IOException e) {
        			logger.logException(e);
        		}
        	}
        }
	}
	
	private void separateChunk(Element element) {
		String hrefValue = element.getAttribute(Constants.ATTRIBUTE_NAME_HREF);
		String copytoValue = element.getAttribute(Constants.ATTRIBUTE_NAME_COPY_TO);
		String scopeValue = element.getAttribute(Constants.ATTRIBUTE_NAME_SCOPE);
		String parseFilePath = null;
		Writer tempOutput = null;
		String chunkValue = element.getAttribute(Constants.ATTRIBUTE_NAME_CHUNK);
		String processRoleValue = element.getAttribute(Constants.ATTRIBUTE_NAME_PROCESSING_ROLE);
		boolean dotchunk = false;
		
		
		if (!copytoValue.equals(Constants.STRING_EMPTY) && !chunkValue.contains("to-content")){
			if (hrefValue.indexOf(Constants.SHARP)!=-1){
				parseFilePath = copytoValue + hrefValue.substring(hrefValue.indexOf(Constants.SHARP));
			}else{
				parseFilePath = copytoValue;
			}
		}else{
			parseFilePath = hrefValue;
		}
		
		// if @copy-to is processed in chunk module, the list file needs to be updated. 
		// Because @copy-to should be included in fulltopiclist, and the source of coyy-to should be excluded in fulltopiclist.
		if(!copytoValue.equals(Constants.STRING_EMPTY) && chunkValue.contains("to-content")){
			copyto.add(copytoValue);
			if(hrefValue.indexOf(Constants.SHARP) != -1){
				copytoSource.add(hrefValue.substring(0, hrefValue.indexOf(Constants.SHARP)));
				copytotarget2source.put(copytoValue, hrefValue.substring(0, hrefValue.indexOf(Constants.SHARP)));
			}else{
				copytoSource.add(hrefValue);
				copytotarget2source.put(copytoValue,hrefValue);
			}
		}
		try {
			if (!StringUtils.isEmptyString(parseFilePath) 
					&& !"external".equalsIgnoreCase(scopeValue)
					&& !Constants.ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY.equalsIgnoreCase(processRoleValue)) {
				// if the path to target file make sense
				currentParsingFile = FileUtils.resolveFile(filePath,parseFilePath);
				String outputFileName = null;
				/*
				 * FIXME: we have code flaws here, references in ditamap need to be updated to 
				 * new created file.
				 */
				String id = null;
				String firstTopicID = null;
				if (parseFilePath.contains(Constants.SHARP) 
						&& parseFilePath.indexOf(Constants.SHARP) < parseFilePath.length() - 1) {
					id = parseFilePath.substring(parseFilePath.indexOf(Constants.SHARP)+1);
					if (chunkValue.contains("select-branch")) {
						outputFileName = FileUtils.resolveFile(filePath, id) + ditaext;
						this.targetTopicId = id;
						this.startFromFirstTopic = false;
						selectMethod = "select-branch";
					} else if (chunkValue.contains("select-document")) {
						firstTopicID = this.getFirstTopicId(FileUtils.resolveFile(filePath, parseFilePath));
						
						//Added by William on 20100628 for bug:3020314 start
						topicDoc = DITAAttrUtils.getInstance().getTopicDoc(FileUtils.resolveFile(filePath, parseFilePath));
						//Added by William on 20100628 for bug:3020314 end
						
						if (!StringUtils.isEmptyString(firstTopicID)) {
							outputFileName = FileUtils.resolveFile(filePath, firstTopicID) + ditaext;
							this.targetTopicId = firstTopicID;
						} else {
							outputFileName = currentParsingFile + ".chunk";
							dotchunk = true;
							this.targetTopicId = null;
						}
						selectMethod = "select-document";
					} else {
						outputFileName = FileUtils.resolveFile(filePath, id) + ditaext;
						this.targetTopicId = id;
						this.startFromFirstTopic = false;
						selectMethod = "select-topic";
					}
				} else {
					firstTopicID = this.getFirstTopicId(FileUtils.resolveFile(filePath, parseFilePath));
					
					//Added by William on 20100628 for bug:3020314 start
					topicDoc = DITAAttrUtils.getInstance().getTopicDoc(FileUtils.resolveFile(filePath, parseFilePath));
					//Added by William on 20100628 for bug:3020314 end
					
					if (!StringUtils.isEmptyString(firstTopicID)) {
						outputFileName = FileUtils.resolveFile(filePath, firstTopicID) + ditaext;
						this.targetTopicId = firstTopicID;
					} else {
						outputFileName = currentParsingFile + ".chunk";
						dotchunk = true;
						this.targetTopicId = null;
					}
					selectMethod = "select-document";
				}
				if (!copytoValue.equals(Constants.STRING_EMPTY)){
					// use @copy-to value as the new file name
					outputFileName = FileUtils.resolveFile(filePath,copytoValue);
				}
				
				if (FileUtils.fileExists(outputFileName)) {
					Random random = new Random();
					String t = outputFileName;
					outputFileName = FileUtils.resolveFile(filePath,"Chunk"
							+ random.nextInt(Integer.MAX_VALUE)) + ditaext;
					conflictTable.put(outputFileName, t);
					dotchunk = false;
				}
				tempOutput = output;
				output = new OutputStreamWriter(new FileOutputStream(
						outputFileName),
						Constants.UTF8);
				outputFile = outputFileName;
				if (!dotchunk) {
					changeTable.put(FileUtils.resolveTopic(filePath, parseFilePath),
							outputFileName + (id == null ? "" : "#"+id));
					//new generated file
					changeTable.put(outputFileName, outputFileName);
				}
				//change the href value
				if (StringUtils.isEmptyString(firstTopicID)) {
					element.setAttribute(Constants.ATTRIBUTE_NAME_HREF,
							FileUtils.getRelativePathFromMap(filePath+Constants.SLASH+"stub.ditamap"
									,outputFileName) + (id == null ? "" : "#"+id));
				} else {
					element.setAttribute(Constants.ATTRIBUTE_NAME_HREF,
							FileUtils.getRelativePathFromMap(filePath+Constants.SLASH+"stub.ditamap"
									,outputFileName) + "#" + firstTopicID);
				}
				include = false;
				//just a mark?
				stub = element.getOwnerDocument().createElement("stub");
				siblingStub = element.getOwnerDocument().createElement("stub");
				//<element>
				//	<stub/>
				//  ...
				//</element>
				//<siblingstub/>
				//...
				//Place stub
				if(element.hasChildNodes()){
					//Added by William on 2010-06-24 for bug:3020313 start
					NodeList list = element.getElementsByTagName(Constants.ELEMENT_NAME_TOPICMETA);
					if(list.getLength() > 0){
						Node node = list.item(0);
						Node nextSibling = node.getNextSibling();
						//no sibling so node is the last child
						if(nextSibling == null){
							node.getParentNode().appendChild(stub);
						}else{
							//has sibling node
							node.getParentNode().insertBefore(stub, nextSibling);
						}
					}else{
						//no topicmeta tag.
						element.insertBefore(stub,element.getFirstChild());
					}
					//Added by William on 2010-06-24 for bug:3020313 end
					
					//element.insertBefore(stub,element.getFirstChild());
				}else{
					element.appendChild(stub);
				}
				
				//Place siblingStub
				if(element.getNextSibling() != null){
					element.getParentNode().insertBefore(siblingStub, element.getNextSibling());
				}else{
					element.getParentNode().appendChild(siblingStub);
				}
				
				reader.setErrorHandler(new DITAOTXMLErrorHandler(currentParsingFile));
				reader.parse(currentParsingFile);
				output.flush();
				
				//remove stub and siblingStub
				stub.getParentNode().removeChild(stub);
				siblingStub.getParentNode().removeChild(siblingStub);
				
			}
		}catch (Exception e) {
			logger.logException(e);
		}finally{
			try{
				if(output!=null){
					output.close();
					if(dotchunk && !new File(currentParsingFile).delete()){
		            	Properties prop = new Properties();
		            	prop.put("%1", currentParsingFile);
		            	prop.put("%2", outputFile);
		            	logger.logError(MessageUtils.getMessage("DOTJ009E", prop).toString());
		            }
		            if(dotchunk && !new File(outputFile).renameTo(new File(currentParsingFile))){
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
		String hrefValue = element.getAttribute(Constants.ATTRIBUTE_NAME_HREF);
		String chunkValue = element.getAttribute(Constants.ATTRIBUTE_NAME_CHUNK);
		String copytoValue = element.getAttribute(Constants.ATTRIBUTE_NAME_COPY_TO);
		String scopeValue = element.getAttribute(Constants.ATTRIBUTE_NAME_SCOPE);
		String classValue = element.getAttribute(Constants.ATTRIBUTE_NAME_CLASS);
		String processRoleValue = element.getAttribute(Constants.ATTRIBUTE_NAME_PROCESSING_ROLE);
		//Added on 20100818 for bug:3042978 start
		//Get id value
		String id = element.getAttribute(Constants.ATTRIBUTE_NAME_ID);
		//Get navtitle value
		String navtitle = element.getAttribute(Constants.ATTRIBUTE_NAME_NAVTITLE);
		//Added on 20100818 for bug:3042978 end
		
		
		//file which will be parsed
		String parseFilePath = null;
		String outputFileName = outputFile;
		//Writer tempWriter = null;
		Writer tempWriter = new StringWriter();
		//Set<String> tempTopicID = null;
		Set<String> tempTopicID = new HashSet<String>();
		
		targetTopicId = null;
		selectMethod = "select-document";
		include = false;
		
		boolean needWriteDitaTag = true;
		
		try {			
			//Get target chunk file name
			if (!copytoValue.equals(Constants.STRING_EMPTY) && !chunkValue.contains("to-content")){
				if (hrefValue.indexOf(Constants.SHARP)!=-1){
					parseFilePath = copytoValue + hrefValue.substring(hrefValue.indexOf(Constants.SHARP));
				}else{
					parseFilePath = copytoValue;
				}
			}else{
				parseFilePath = hrefValue;
			}
			
			// if @copy-to is processed in chunk module, the list file needs to be updated. 
			// Because @copy-to should be included in fulltopiclist, and the source of coyy-to should be excluded in fulltopiclist.
			if(!copytoValue.equals(Constants.STRING_EMPTY) && chunkValue.contains("to-content") 
				&& ! hrefValue.equals(Constants.STRING_EMPTY)){
				copyto.add(copytoValue);
				if(hrefValue.indexOf(Constants.SHARP) != -1){
					copytoSource.add(hrefValue.substring(0, hrefValue.indexOf(Constants.SHARP)));
					copytotarget2source.put(copytoValue, hrefValue.substring(0, hrefValue.indexOf(Constants.SHARP)));
				}else{
					copytoSource.add(hrefValue);
					copytotarget2source.put(copytoValue,hrefValue);
				}
			}
		
			if ( !StringUtils.isEmptyString(classValue) ) {
				if ((!classValue.contains(Constants.ATTR_CLASS_VALUE_TOPIC_GROUP))
						&& (!StringUtils.isEmptyString(parseFilePath))
						&& (!"external".equalsIgnoreCase(scopeValue))) {
					// now the path to target file make sense
					if(chunkValue.indexOf("to-content")!=-1){
						//if current element contains "to-content" in chunk attribute
						//we need to create new buffer and flush the buffer to file
						//after processing is finished
						tempWriter = output;
						tempTopicID = topicID;
						output = new StringWriter();
						topicID = new HashSet<String>();
						//if (Constants.ELEMENT_NAME_MAP.equalsIgnoreCase(element.getNodeName())) {
						if (classValue.contains(Constants.ATTR_CLASS_VALUE_MAP)) {
							// Very special case, we have a map element with href value.
							// This is a map that needs to be chunked to content.
							// No need to parse any file, just generate a stub output.
							outputFileName = FileUtils.resolveFile(filePath, parseFilePath);
							needWriteDitaTag = false;
						} else if (!copytoValue.equals(Constants.STRING_EMPTY)){
							// use @copy-to value as the new file name
							outputFileName = FileUtils.resolveFile(filePath,copytoValue);
						} else if (!hrefValue.equals(Constants.STRING_EMPTY)) {
							// try to use href value as the new file name
							if (chunkValue.contains("select-topic") || chunkValue.contains("select-branch")) {
								if (hrefValue.contains(Constants.SHARP)
										&& hrefValue.indexOf(Constants.SHARP) < hrefValue.length() - 1) {
									// if we have an ID here, use it.
									outputFileName = FileUtils.resolveFile(filePath,hrefValue.substring(hrefValue.indexOf(Constants.SHARP)+1))+ditaext;
								} else {
									// Find the first topic id in target file if any.
									String firstTopic = this.getFirstTopicId(FileUtils.resolveFile(filePath, hrefValue));
									if (!StringUtils.isEmptyString(firstTopic)) {
										outputFileName = FileUtils.resolveFile(filePath, firstTopic) + ditaext;
									} else {
										outputFileName = FileUtils.resolveFile(filePath,hrefValue);
									}
								}
							} else {
								// otherwise, use the href value instead
								outputFileName = FileUtils.resolveFile(filePath,hrefValue);
							}
						} else {
							// use randomly generated file name
							Random random = new Random();
							outputFileName = FileUtils.resolveFile(filePath,"Chunk"
									+ random.nextInt(Integer.MAX_VALUE)) + ditaext;
						}
						
						// Check if there is any conflict
						if(FileUtils.fileExists(outputFileName)
								&& !classValue.contains(Constants.ATTR_CLASS_VALUE_MAP)) {
							String t = outputFileName;
							Random random = new Random();
							outputFileName = FileUtils.resolveFile(filePath,"Chunk"
									+ random.nextInt(Integer.MAX_VALUE)) +ditaext;
							conflictTable.put(outputFileName, t);
						}
						// add newly generated file to changTable
						// the new entry in changeTable has same key and value
						// in order to indicate it is a newly generated file
						changeTable.put(outputFileName,outputFileName);
					}
					//"by-topic" couldn't reach here
					this.outputFile = outputFileName;
					
					{
						String path = FileUtils.resolveTopic(filePath,parseFilePath);
						String newpath = null;
						if(path.indexOf(Constants.SHARP)!=-1){
							newpath = outputFileName + path.substring(path.indexOf(Constants.SHARP));
						}else{
							String firstTopicID = this.getFirstTopicId(path);
							if(!StringUtils.isEmptyString(firstTopicID)) {
								newpath = outputFileName + "#" + firstTopicID;
							} else {
								newpath = outputFileName;
							}
						}
						// add file name changes to changeTable, this will be used in 
						//TopicRefWriter's updateHref method, very important!!!
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
					
					if ( !Constants.ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY.equalsIgnoreCase(processRoleValue)) {
						
						// Added on 2010-11-12 for bug 3090803 start
						currentParsingFileTopicIDChangeTable = new HashMap<String, String>();
						// Added on 2010-11-12 for bug 3090803 end
						//TODO recursive point
						reader.parse(currentParsingFile);
						// Added on 2010-11-12 for bug 3090803 start
						if(currentParsingFileTopicIDChangeTable.size()>0) {
							String href = element.getAttribute(Constants.ATTRIBUTE_NAME_HREF);
							href = href.replaceAll(Constants.DOUBLE_BACK_SLASH,
									Constants.SLASH);
							String pathtoElem = 
								href.contains(Constants.SHARP) ? href.substring(href.indexOf(Constants.SHARP)+1) : "";
							
							String old_elementid =  pathtoElem.contains(Constants.SLASH) ? pathtoElem.substring(0, pathtoElem.indexOf(Constants.SLASH)) : pathtoElem;
							
							if(old_elementid.length()>0) {
								String new_elementid = currentParsingFileTopicIDChangeTable.get(old_elementid);
								if(new_elementid!=null&&new_elementid.length()>0) {
									href = href.replaceFirst(Constants.SHARP + old_elementid, Constants.SHARP + new_elementid);
									element.setAttribute(Constants.ATTRIBUTE_NAME_HREF, href);
								}
								
							}
						}
						currentParsingFileTopicIDChangeTable = null;
						// Added on 2010-11-12 for bug 3090803 end
					}
					//restore the currentParsingFile
					currentParsingFile = tempPath;
				}
				
				//Added on 20100818 for bug:3042978 start
				//use @copy-to value(dita spec v1.2)
				if(outputFileName == null){
					if (!StringUtils.isEmptyString(copytoValue)){
						outputFileName = FileUtils.resolveFile(filePath,
								copytoValue);
					//use id value	
					}else if(!StringUtils.isEmptyString(id)){
						outputFileName = FileUtils.resolveFile(filePath,
								id + ditaext);
					}else{
						// use randomly generated file name
						Random random = new Random();
						outputFileName = FileUtils.resolveFile(filePath,"Chunk"
								+ random.nextInt(Integer.MAX_VALUE)) + ditaext;
						// Check if there is any conflict
						if(FileUtils.fileExists(outputFileName)
						   && !classValue.contains(Constants.ATTR_CLASS_VALUE_MAP)) {
							String t = outputFileName;
							outputFileName = FileUtils.resolveFile(filePath,"Chunk"
									+ random.nextInt(Integer.MAX_VALUE)) + ditaext;
							conflictTable.put(outputFileName, t);
						}
					}
				
					//if topicref has child node or topicref has @navtitle
					if(element.hasChildNodes() || !StringUtils.isEmptyString(navtitle)){
						
						DITAAttrUtils utils = DITAAttrUtils.getInstance();
						
						String navtitleValue = null;
						String shortDescValue = null;
						//get navtitle value.
						navtitleValue = utils.getChildElementValueOfTopicmeta(element, Constants.ATTR_CLASS_VALUE_NAVTITLE);
						//get shortdesc value
						shortDescValue = utils.getChildElementValueOfTopicmeta(element, Constants.ATTR_CLASS_VALUE_MAP_SHORTDESC);
						//no navtitle tag exists.
						if(navtitleValue == null){
							//use @navtitle
							navtitleValue = navtitle;
						}
						
						
						// add newly generated file to changTable
						// the new entry in changeTable has same key and value
						// in order to indicate it is a newly generated file
						changeTable.put(outputFileName,outputFileName);
						// update current element's @href value
						//create a title-only topic when there is a title
						if(!StringUtils.isEmptyString(navtitleValue)){
							element.setAttribute(Constants.ATTRIBUTE_NAME_HREF,
							FileUtils.getRelativePathFromMap(filePath+Constants.SLASH+"stub.ditamap", outputFileName));
							//manually create a new topic chunk
							StringBuffer buffer = new StringBuffer();
							buffer.append("<topic id=\"topic\" class=\"- topic/topic \">")
							.append("<title class=\"- topic/title \">")
							.append(navtitleValue).append("</title>");
							//has shortdesc value
							if(shortDescValue != null){
								buffer.append("<shortdesc class=\"- topic/shortdesc \">")
								.append(shortDescValue).append("</shortdesc>");
							}
							buffer.append("</topic>");
					
							StringReader rder = new StringReader(buffer.toString());
							InputSource source = new InputSource(rder);
							
							//for recursive
							String tempPath = currentParsingFile;
							currentParsingFile = outputFileName;
							//insert not append the nested topic
							parseFilePath = outputFileName;
							//create chunk
							reader.parse(source);
							//restore the currentParsingFile
							currentParsingFile = tempPath;
						}
						
					}
						
				}
				//Added 20100818 for bug:3042978 end
				
				if (element.hasChildNodes()){
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
					// Skip empty parents and @processing-role='resource-only' entries.
					if (parentResult.length() > 0
							&& !StringUtils.isEmptyString(parseFilePath)
							&& !Constants.ATTR_PROCESSING_ROLE_VALUE_RESOURCE_ONLY.equalsIgnoreCase(processRoleValue)) {
						int insertpoint = parentResult.lastIndexOf("</");
						int end = parentResult.indexOf(">",insertpoint);
						
						if(insertpoint==-1 || end==-1){
							Properties prop=new Properties();
							prop.put("%1", hrefValue);
							logger.logError(MessageUtils.getMessage("DOTJ033E",prop).toString());
						} else {
							if (Constants.ELEMENT_NAME_DITA.equalsIgnoreCase(parentResult.substring(insertpoint,end).trim())){
								insertpoint = parentResult.lastIndexOf("</",insertpoint);
							}
							parentResult.insert(insertpoint,((StringWriter)output).getBuffer());
						}
					} else {
						parentResult.append(((StringWriter)output).getBuffer());
					}
					//restore back to parent's output this is a different temp
					output = temp;
					
				}
				
				if(chunkValue.indexOf("to-content")!=-1){
					//flush the buffer to file after processing is finished
					//and restore back original output
					
										
					FileOutputStream fileOutput = new FileOutputStream(outputFileName);
					OutputStreamWriter ditaFileOutput = null;
					try {
    					ditaFileOutput = new OutputStreamWriter(fileOutput, Constants.UTF8);
    					if (outputFileName.equals(changeTable.get(outputFileName))){
    						// if the output file is newly generated file
    						// write the xml header and workdir PI into new file
    						ditaFileOutput.write(Constants.XML_HEAD);
    						if(Constants.OS_NAME.toLowerCase().indexOf(OS_NAME_WINDOWS)==-1)
    			            {
    			                ditaFileOutput.write(PI_WORKDIR_HEAD + new File(outputFileName).getParent() + PI_END);
    			            }else{
    			                ditaFileOutput.write(PI_WORKDIR_HEAD + Constants.SLASH + new File(outputFileName).getParent() + PI_END);
    			            }
    						
    						//Added on 20101210 for bug:3126578 start
    						if ((conflictTable.get(outputFileName)!=null)){
    							String relativePath = FileUtils
    									.getRelativePathFromMap(filePath
    											+ Constants.SLASH + "stub.ditamap",
    											conflictTable.get(outputFileName));
    							String path2project = FileUtils
    									.getPathtoProject(relativePath);
    							if (null==path2project){
    								path2project="";
    							}
    							ditaFileOutput.write("<?path2project "
    									+ path2project + "?>");
    						}
    						//Added on 20101210 for bug:3126578 end
    					}
    					if (needWriteDitaTag) ditaFileOutput.write("<dita>");
    					//write the final result to the output file
    					ditaFileOutput.write(((StringWriter)output).getBuffer().toString());
    					if (needWriteDitaTag) ditaFileOutput.write("</dita>");
    					ditaFileOutput.flush();
					} finally {
						ditaFileOutput.close();	
					}
					// restore back original output
					output = tempWriter;
					topicID = tempTopicID;
				}
			}
		} catch (Exception e) {
			logger.logException(e);
		}
	
	}
	/**
	 * Set up the class.
	 * @param changeTable changeTable
	 * @param conflictTable conflictTable
	 * @param refFileSet refFileSet
	 * @param elem elem
	 * @param separate separate
	 * @param chunkByTopic chunkByTopic
	 * @param ditaext ditaext
	 */
	public void setup(LinkedHashMap<String, String> changeTable, Hashtable<String, String> conflictTable, 
			HashSet<String> refFileSet, Element elem, boolean separate, boolean chunkByTopic, String ditaext) {
		// Initialize ChunkTopicParser
		this.changeTable = changeTable;
		this.elem = elem;
		this.separate = separate;
		this.ditaext  = ditaext;
		this.conflictTable = conflictTable;
		logger=new DITAOTJavaLogger();
		
		try {
            reader = StringUtils.getXMLReader();
            reader.setContentHandler(this);
            reader.setProperty(Constants.LEXICAL_HANDLER_PROPERTY,this);
            reader.setFeature(Constants.FEATURE_NAMESPACE_PREFIX, true);
            
            //Edited by william on 2009-11-8 for ampbug:2893664 start
			reader.setFeature("http://apache.org/xml/features/scanner/notify-char-refs", true);
			reader.setFeature("http://apache.org/xml/features/scanner/notify-builtin-refs", true);
			//Edited by william on 2009-11-8 for ampbug:2893664 end
			
		} catch (Exception e) {
			logger.logException(e);
		}
	}
	
	private boolean checkHREF(Attributes atts){
    	// check whether current href needs to be updated
    	String scopeValue = atts.getValue(Constants.ATTRIBUTE_NAME_SCOPE);
    	String hrefValue = atts.getValue(Constants.ATTRIBUTE_NAME_HREF);
    	//Added by William on 2009-08-18 for chunkbug id:2839035 start
    	hrefValue = StringUtils.escapeXML(hrefValue);
    	//Added by William on 2009-08-18 for chunkbug id:2839035 end
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
	
	/**
	 * 
	 * Get the first topic id from the given dita file.
	 * @param absolutePathToFile The absolute path to a dita file.
	 * @return The first topic id from the given dita file if success,
	 * otherwise an empty string is returned.
	 */
	private String getFirstTopicId(String absolutePathToFile){
		TopicIdParser parser;
		XMLReader reader;
		StringBuffer firstTopicId = new StringBuffer("");
		
		if(absolutePathToFile == null || !FileUtils.isAbsolutePath(absolutePathToFile))
			return firstTopicId.toString();
		
		parser = new TopicIdParser(firstTopicId);
		try{
            reader = StringUtils.getXMLReader();
            reader.setContentHandler(parser);            
            reader.parse(absolutePathToFile);
        }catch (Exception e){
            logger.logException(e);
        }
		return firstTopicId.toString();
		
	}
	
	/**
	 * Create topicmeta node.
	 * @param element document element of a topic file.
	 * @param newChild node to be appended by topicmeta.
	 */
	private void createTopicMeta(Element element, Element newChild) {
		
		//create topicmeta element
		Element topicmeta = elem.getOwnerDocument()
		.createElement(Constants.ELEMENT_NAME_TOPICMETA);
		topicmeta.setAttribute(Constants.ATTRIBUTE_NAME_CLASS,
				"-" + Constants.ATTR_CLASS_VALUE_TOPICMETA);
		newChild.appendChild(topicmeta);
		
		DITAAttrUtils utils = DITAAttrUtils.getInstance();
		
		//iterate the node.
		if(element != null){
			//search for title and navtitle tag
			NodeList list = element.getChildNodes();
			Node title = null;
			Node navtitle = null;
			for (int i = 0; i < list.getLength(); i++) {
				Node node = list.item(i);
				if(node.getNodeType() == Node.ELEMENT_NODE){
					Element childNode = (Element)node;
					
					if(childNode.getAttribute(Constants.ATTRIBUTE_NAME_CLASS).
							contains(Constants.ATTR_CLASS_VALUE_TITLE)){
						//set title node
						title = childNode;
					}
					//navtitle node
					if(childNode.getAttribute(Constants.ATTRIBUTE_NAME_CLASS).
							contains(Constants.ATTR_CLASS_VALUE_TITLEALTS)){
						NodeList subList = childNode.getChildNodes();
						for(int j = 0; j < subList.getLength(); j ++ ){
							Node subNode = subList.item(j);
							if(subNode.getNodeType() == Node.ELEMENT_NODE){
								Element subChildNode = (Element)subNode;
								if(subChildNode.getAttribute(Constants.ATTRIBUTE_NAME_CLASS).
										contains(Constants.ATTR_CLASS_VALUE_NAVTITLE)){
									//set navtitle node
									navtitle = subChildNode;	
								}
							}
						}
					}
				}
			}
			//shordesc node
			Node shortDesc = null;
			NodeList nodeList = element.getChildNodes();
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element elem = (Element) node;
					String clazzValue = elem
							.getAttribute(Constants.ATTRIBUTE_NAME_CLASS);
					//if needed node is found
					if (clazzValue != null && clazzValue.contains(Constants.ATTR_CLASS_VALUE_SHORTDESC)) {
							shortDesc = elem;
					}
				}
			}
			
			Element navtitleNode = elem.getOwnerDocument()
			.createElement(Constants.ELEMENT_NAME_NAVTITLE);
			navtitleNode.setAttribute(Constants.ATTRIBUTE_NAME_CLASS,
					"-" + Constants.ATTR_CLASS_VALUE_NAVTITLE);
			//append navtitle node
			if(navtitle != null){
				//Get text value
				String text = utils.getText(navtitle);
				Text titleText = elem.getOwnerDocument().createTextNode(text);
				navtitleNode.appendChild(titleText);
				topicmeta.appendChild(navtitleNode);
				
			}else{
				//Get text value
				String text = utils.getText(title);
				Text titleText = elem.getOwnerDocument().createTextNode(text);
				navtitleNode.appendChild(titleText);
				topicmeta.appendChild(navtitleNode);
			}
			
			//append  gentext pi
			Node pi = elem.getOwnerDocument()
			.createProcessingInstruction("ditaot", "gentext");
			topicmeta.appendChild(pi);
			
			//append  linktext
			Element linkTextNode = elem.getOwnerDocument()
			.createElement(Constants.ELEMENT_NAME_LINKTEXT);
			linkTextNode.setAttribute(Constants.ATTRIBUTE_NAME_CLASS,
					"-" + Constants.ATTR_CLASS_VALUE_MAP_LINKTEXT);
			//Get text value
			String text = utils.getText(title);
			Text textNode = elem.getOwnerDocument().createTextNode(text);
			linkTextNode.appendChild(textNode);
			topicmeta.appendChild(linkTextNode);
			
			//append  genshortdesc pi
			Node pii = elem.getOwnerDocument()
			.createProcessingInstruction("ditaot", "genshortdesc");
			topicmeta.appendChild(pii);
			
			//append  shortdesc
			Element shortDescNode = elem.getOwnerDocument()
			.createElement(Constants.ELEMENT_NAME_SHORTDESC);
			shortDescNode.setAttribute(Constants.ATTRIBUTE_NAME_CLASS,
					"-" + Constants.ATTR_CLASS_VALUE_MAP_SHORTDESC);
			//Get text value
			String shortDescText = utils.getText(shortDesc);
			Text shortDescTextNode = elem.getOwnerDocument().createTextNode(shortDescText);
			shortDescNode.appendChild(shortDescTextNode);
			topicmeta.appendChild(shortDescNode);
			
		}
	}
	//Added by William on 20100628 for bug:3020314 end
	

}
