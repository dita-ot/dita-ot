/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.reader;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.Stack;

import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.module.Content;
import org.dita.dost.module.ContentImpl;
import org.dita.dost.resolver.DitaURIResolverFactory;
import org.dita.dost.resolver.URIResolverAdapter;
import org.dita.dost.util.Constants;
import org.dita.dost.util.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
/**
 * KeyrefReader class which reads ditamap file to collect key definitions.
 *
 */
public class KeyrefReader extends AbstractXMLReader {

	protected static class KeyDef
	{
		protected String key;
		protected StringBuffer keyDefContent;
		protected int keyDefLevel = 0;
		public KeyDef(String key)
		{
			this.key=key;
			keyDefContent = new StringBuffer();
		}
	}
	private DITAOTJavaLogger javaLogger;
	
	private XMLReader reader;
	
	private Hashtable<String, String> keyDefTable;
	
	private Stack<KeyDef> keyDefs;
	
	private Set<String> keys;
	
	private String tempDir;
	
	
	// flag for the start of key definition;
	
	/**
	 * Constructor.
	 */
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
		if(isStart())
			keyDefAppend(StringUtils.escapeXML(ch, start, length));
	}


	@Override
	public void endElement(String uri, String localName, String name)
			throws SAXException {
		if(isStart()){
			decKeyDefLevel();
			keyDefAppend(Constants.LESS_THAN);
			keyDefAppend(Constants.SLASH);
			keyDefAppend(name);
			keyDefAppend(Constants.GREATER_THAN);
		}
		if(isStart() && getKeyDefLevel() == 0){
			// to the end of the key definition, set the flag false 
			// and put the key definition to table.
			KeyDef keyDef = popKeyDef();
			for(String keyName: keyDef.key.split(" ")){
				if(!keyName.equals(""))
				keyDefTable.put(keyName, keyDef.keyDefContent.toString());
				
			}
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
		keyDefs = new Stack<KeyDef>();
		try {
			//AlanChanged: by refactoring Adding URIResolver Date:2009-08-13 --begin
			/* filename = tempDir + File.separator + filename; */
			InputSource source = URIResolverAdapter.convertToInputSource(DitaURIResolverFactory.getURIResolver().resolve(filename, null));			
			reader.parse(source);
			//edit by Alan: by refactoring Adding URIResolver Date:2009-08-13 --end
		} catch (Exception ex) {
			javaLogger.logException(ex);
		}
	}
	/**
	 * set keys set for later comparison.
	 * @param set keys set
	 */
	public void setKeys(Set<String> set){
		this.keys = set;
	}
	
	@Override
	public void startElement(String uri, String localName, String name,
			Attributes atts) throws SAXException {
		String classValue = atts.getValue(Constants.ATTRIBUTE_NAME_CLASS);
		String keyName = atts.getValue(Constants.ATTRIBUTE_NAME_KEYS);
		if(keyName!=null && classValue.contains(" map/topicref ")){
			
			// if it has @keys and is valid.
			boolean flag = false;
			String[] keyNames = keyName.split(" ");
			int index = 0;
			while(index < keyNames.length){
				if(keys.contains(keyNames[index++])){
					flag = true;
					break;
				}
			}
			if(keyName != null && flag){
				pushKeyDef(keyName);
				incKeyDefLevel();
				putElement(name, atts);
			}
		}else if(isStart()){
			incKeyDefLevel();
			putElement(name, atts);
		}
	}

	private void putElement(String elemName,
			Attributes atts) {
		int index = 0;
		keyDefAppend(Constants.LESS_THAN);
		keyDefAppend(elemName);
		for (index=0; index < atts.getLength(); index++){
			keyDefAppend(Constants.STRING_BLANK);
			keyDefAppend(atts.getQName(index));
			keyDefAppend(Constants.EQUAL);
			keyDefAppend(Constants.QUOTATION);
			String value = atts.getValue(index);
			//Added by William on 2009-10-15 for ampersand bug:2878492 start
			value = StringUtils.escapeXML(value);
			//Added by William on 2009-10-15 for ampersand bug:2878492 end
			keyDefAppend(value);
			keyDefAppend(Constants.QUOTATION);
		}
		keyDefAppend(Constants.GREATER_THAN);
	}
	/**
	 * Set temp dir.
	 * @param tempDir temp dir
	 */
	public void setTempDir(String tempDir){
		this.tempDir = tempDir;
	}
	private void pushKeyDef(String keyName)
	{
		keyDefs.push(new KeyDef(keyName));
	}
	private KeyDef popKeyDef()
	{
		return keyDefs.pop();
	}
	private void keyDefAppend(String content)
	{
		for (KeyDef keyDef : keyDefs)
		{
			keyDef.keyDefContent.append(content);
		}
	}
	private boolean isStart()
	{
		return keyDefs.size()>0;
	}
	private void incKeyDefLevel()
	{
		addKeyDefLevel(1);
	}
	private void decKeyDefLevel()
	{
		addKeyDefLevel(-1);
	}
	private void addKeyDefLevel(int dif)
	{
		keyDefs.peek().keyDefLevel+=dif;
	}
	private int getKeyDefLevel()
	{
		return keyDefs.peek().keyDefLevel;
	}
}
