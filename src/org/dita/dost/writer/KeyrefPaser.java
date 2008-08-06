package org.dita.dost.writer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Properties;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.module.Content;
import org.dita.dost.util.Constants;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class KeyrefPaser extends AbstractXMLWriter {

	private XMLReader parser = null;
	
	private DITAOTJavaLogger javaLogger = null;
	
	private OutputStreamWriter output = null;
	
	private Content content;

	private String tempDir;
	
	// relative path of the filename to the temp directory
	private String filepath;
	
	public KeyrefPaser(){
		javaLogger = new DITAOTJavaLogger();
		try{
			parser = XMLReaderFactory.createXMLReader();
			parser.setFeature(Constants.FEATURE_NAMESPACE_PREFIX, true);
			parser.setFeature(Constants.FEATURE_NAMESPACE, true);
			parser.setContentHandler(this);
		}catch(Exception e){
			javaLogger.logException(e);
		}
	}
	
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		try {
			output.write(StringUtils.escapeXML(ch, start, length));
		} catch (IOException e) {
			
			javaLogger.logException(e);
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
	public void endElement(String uri, String localName, String name)
			throws SAXException {
		// write the end element
		try{
			output.write(Constants.LESS_THAN);
			output.write(Constants.SLASH);
			output.write(name);
			output.write(Constants.GREATER_THAN);
		}catch (Exception e) {
			javaLogger.logException(e);
		}
	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {
		try{
			output.write(ch, start, length);
		}catch (Exception e) {
			javaLogger.logException(e);
		}
	}

	@Override
	public void processingInstruction(String target, String data)
			throws SAXException {
		try {
        	String pi = (data != null) ? target + Constants.STRING_BLANK + data : target;
            output.write(Constants.LESS_THAN + Constants.QUESTION 
                    + pi + Constants.QUESTION + Constants.GREATER_THAN);
        } catch (Exception e) {
        	javaLogger.logException(e);
        }
	}

	@Override
	public void setContent(Content content) {
		this.content = content;
	}

	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
	}

	@Override
	public void startElement(String uri, String localName, String name,
			Attributes atts) throws SAXException {
		try {
			output.write(Constants.LESS_THAN);
			output.write(name);
			boolean validKeyRef = false;
			for(int index = 0; index < atts.getLength(); index++){
				output.write(Constants.STRING_BLANK);
				if(atts.getQName(index).equals(Constants.ATTRIBUTE_NAME_KEYREF)){
					String target = ((HashMap<String, String>)content.getValue()).get(atts.getValue(index));
					String target_output;
					if(target != null && new File(tempDir,target).exists()){
						output.write(Constants.ATTRIBUTE_NAME_HREF);
						target_output = FileUtils.getRelativePathFromMap(filepath, new File(tempDir,target).getAbsolutePath());
						validKeyRef = true;
						output.write("=\"");
						output.write(target_output);
						output.write("\"");
					}else{
					// if the target is null or the target does not exist emit a warning message
						Properties prop = new Properties();
						prop.setProperty("%1", atts.getValue(index));
						prop.setProperty("%2", atts.getValue("xtrf"));
						prop.setProperty("%3", atts.getValue("xtrc"));
						javaLogger.logWarn(MessageUtils.getMessage("DOTJ045W", prop).toString());
					}
				}else if(!atts.getQName(index).equals(Constants.ATTRIBUTE_NAME_HREF) && !atts.getQName(index).equals(Constants.ATTRIBUTE_NAME_SCOPE)){
					// write the attribute excepte the href and scope
					// because they may be excluded if there is keyref attribute and it is resolved.
					output.write(atts.getQName(index));
					output.write("=\"");
					output.write(atts.getValue(index));
					output.write("\"");
				}
			}
			if(!validKeyRef && atts.getValue(Constants.ATTRIBUTE_NAME_HREF) != null){
			// if the keyref is not valid and href exist, write it
				output.write(Constants.STRING_BLANK);
				output.write(Constants.ATTRIBUTE_NAME_HREF);
				output.write("=\"");
				output.write(atts.getValue(Constants.ATTRIBUTE_NAME_HREF));
				output.write("\"");
			}
			if(!validKeyRef && atts.getValue(Constants.ATTRIBUTE_NAME_SCOPE) != null){
			// if the keyref is not valid and href exist, write it
				output.write(Constants.STRING_BLANK);
				output.write(Constants.ATTRIBUTE_NAME_SCOPE);
				output.write("=\"");
				output.write(atts.getValue(Constants.ATTRIBUTE_NAME_SCOPE));
				output.write("\"");
			}
			output.write(Constants.GREATER_THAN);
			
		} catch (IOException e) {
			javaLogger.logException(e);
		}
		
	}

	@Override
	public void write(String filename) throws DITAOTException {
		try{
			File inputFile = new File(tempDir,filename);
			filepath = inputFile.getAbsolutePath();
			File outputFile = new File(tempDir,filename + "keyref");
			output = new OutputStreamWriter(new FileOutputStream(outputFile));
			parser.parse(inputFile.getAbsolutePath());
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
		}catch(Exception e){
			javaLogger.logException(e);
		}finally{
			try{
				output.close();
			}catch (Exception ex) {
				javaLogger.logException(ex);
			}
		}
		
	}
	
	public void setTempDir(String tempDir){
		this.tempDir = tempDir;
	}

}
