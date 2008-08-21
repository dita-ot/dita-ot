package org.dita.dost.reader;

import java.io.File;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.module.Content;
import org.dita.dost.module.ContentImpl;
import org.dita.dost.util.Constants;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class KeyrefReader extends AbstractXMLReader {

	private DITAOTJavaLogger javaLogger;
	
	private XMLReader reader;
	
	private Hashtable<String, String> keyDefTable;
	
	private StringBuffer keyDefContent;
	
	private Set<String> keys;
	
	private String tempDir;
	
	private String key;
	
	// flag for the start of key definition;
	private boolean start;
	
	private int keyDefLevel = 0;
	
	public KeyrefReader(){
		javaLogger = new DITAOTJavaLogger();
		keyDefTable = new Hashtable<String, String>();
		keys = new HashSet<String>();
		try {
			reader = XMLReaderFactory.createXMLReader();
			reader.setFeature(Constants.FEATURE_NAMESPACE_PREFIX, true);
			reader.setFeature(Constants.FEATURE_NAMESPACE, true);
		} catch (SAXException ex) {
			javaLogger.logException(ex);
		}
		reader.setContentHandler(this);
	}
	
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if(this.start)
			keyDefContent.append(StringUtils.escapeXML(ch, start, length));
	}


	@Override
	public void endElement(String uri, String localName, String name)
			throws SAXException {
		if(start){
			keyDefLevel --;
			keyDefContent.append(Constants.LESS_THAN).append(Constants.SLASH).append(name).append(Constants.GREATER_THAN);
		}
		if(start && keyDefLevel == 0){
			// to the end of the key definition, set the flag false 
			// and put the key definition to table.
			start = false;
			keyDefTable.put(key, keyDefContent.toString());
			keyDefContent = new StringBuffer();
		}
	}

	@Override
	public Content getContent() {
		Content content = new ContentImpl();
		content.setValue(keyDefTable);
		return content;
	}


	@Override
	public void read(String filename) {
		keyDefContent = new StringBuffer();
		start = false;
		try {
			filename = tempDir + File.separator + filename;
			reader.parse(filename);
		} catch (Exception ex) {
			javaLogger.logException(ex);
		}
	}

	public void setKeys(Set<String> set){
		this.keys = set;
	}
	
	@Override
	public void startElement(String uri, String localName, String name,
			Attributes atts) throws SAXException {
		String classValue = atts.getValue(Constants.ATTRIBUTE_NAME_CLASS);
		if(classValue.contains(" map/topicref ")){
			String keyName = atts.getValue(Constants.ATTRIBUTE_NAME_KEYS);
			// if it has @keys and is valid.
			if(keyName != null && keys.contains(keyName)){
				key = keyName;
				start = true;
				keyDefLevel ++;
				putElement(keyDefContent, name, atts);
			}
		}else if(start){
			keyDefLevel++;
			putElement(keyDefContent, name, atts);
		}
	}

	private void putElement(StringBuffer buf, String elemName,
			Attributes atts) {
		int index = 0;
		buf.append(Constants.LESS_THAN).append(elemName);
		for (index=0; index < atts.getLength(); index++){
			buf.append(Constants.STRING_BLANK);
			buf.append(atts.getQName(index)).append(Constants.EQUAL).append(Constants.QUOTATION);
			String value = atts.getValue(index);
			buf.append(value).append(Constants.QUOTATION);
		}
		buf.append(Constants.GREATER_THAN);
	}
	
	public void setTempDir(String tempDir){
		this.tempDir = tempDir;
	}
}
