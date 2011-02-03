/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2005 All Rights Reserved.
 */
package org.dita.dost.log;

import java.io.File;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.dita.dost.util.Constants;
import org.dita.dost.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class is used to get message info from message file.
 * 
 * @author Wu, Zhi Qiang
 */
public class MessageUtils {
	private static Hashtable<String, MessageBean> hashTable = null;
	private static String messageFile = null;
	private static DITAOTJavaLogger fileLogger = new DITAOTJavaLogger();
	private static String defaultResource = "resource/messages.xml";
	private static MessageUtils utils = null;
	
	/**
	 * Default Construtor
	 *
	 */
	private MessageUtils(){
	}
	
	/**
	 * Internal Singleton getInstance() method, for Classloader to locate the CLASSPATH
	 * @return
	 */
	private static synchronized MessageUtils getInstance(){
		if(utils == null){
			utils = new MessageUtils();
		}
		return utils;
	}

	/**
	 * Just bypass to invoke member function loadDefMsg().
	 *
	 */
	public static void loadDefaultMessages() {
		getInstance().loadDefMsg();
	}
	
	/**
	 * Load Default Messages.
	 * If not exist in the relative path, search the CLASSPATH
	 */
	private void loadDefMsg(){
		if(!new File(defaultResource).exists()){
			loadMessages(getClass().getClassLoader().getResource(defaultResource).toString());
		}else{
			loadMessages(defaultResource);
		}
	}
	
	/**
	 * Load message from message file.
	 * @param newMessageFile newMessageFile
	 */
	public static void loadMessages(String newMessageFile) {
		if (!updateMessageFile(newMessageFile)) {
			// this message file has been loaded
			return;
		}
		
		// always assign a new instance to hashTable to avoid 
		// to reload this method again and again when messages 
		// loading failed.
		hashTable = new Hashtable<String, MessageBean>();
		
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(messageFile);

			Element messages = doc.getDocumentElement();
			NodeList messageList = messages.getElementsByTagName("message");

			int messageListLength = messageList.getLength();
			for (int i = 0; i < messageListLength; i++) {
				Element message = (Element) messageList.item(i);
				Node reason = message.getElementsByTagName("reason").item(0);
				Node response = message.getElementsByTagName("response")
						.item(0);

				MessageBean messageBean = new MessageBean();
				NamedNodeMap attrs = message.getAttributes();
				String id = attrs.getNamedItem("id").getNodeValue();

				messageBean.setId(id);
				messageBean.setType(attrs.getNamedItem("type").getNodeValue());
				messageBean.setReason(reason.getFirstChild().getNodeValue());
				messageBean
						.setResponse(response.getFirstChild().getNodeValue());

				hashTable.put(id, messageBean);
			}
		} catch (Exception e) {
			StringBuffer buff = new StringBuffer(Constants.INT_128);
			
			buff.append("  Failed to load messages from '");
			buff.append(messageFile);
			buff.append("' due to exception: ");
			buff.append(e.getMessage());
			buff.append(". Please check if there are files 'resource/messages.xml'");
			buff.append(" and 'resource/messages.dtd' in the directory");
			buff.append(" that you run the toolkit. If not, please copy them");
			buff.append(" from the toolkit's root directory.");
			
			fileLogger.logError(buff.toString());
		}
	}

	private static boolean updateMessageFile(String newMessageFile) {
		String oldMessagePath = null;
		String newMessagePath = null;

		if (messageFile == null) {
			messageFile = newMessageFile;
			return true;
		}
		
		oldMessagePath = new File(MessageUtils.messageFile).getAbsolutePath();
		newMessagePath = new File(newMessageFile).getAbsolutePath();
		
		if (oldMessagePath.equalsIgnoreCase(newMessagePath)) {
			return false;
		}
		
		messageFile = newMessageFile;
		
		return true;
	}
	
	/**
	 * Get the message respond to the given id, if no message found, 
	 * an empty message with this id will be returned. 
	 * 
	 * @param id message di
	 * @return messageBean
	 */
	public static MessageBean getMessage(String id) {
		MessageBean message = null;
		MessageBean hashMessage = null;
		
		if (hashTable == null) {
			loadDefaultMessages();
		}

		hashMessage = (MessageBean) hashTable.get(id);
		if (hashMessage != null) {
			message = new MessageBean(hashMessage);
		}
		
		if (message == null) {
			// return a empty message when no message found, 
			// and notify the user with a warning message.
			message = new MessageBean(id, "", "", "");
			fileLogger.logWarn("  Can't find message for id: " + id);			
		}
		
		return message;
	}

	/**
	 * Get the message respond to the given id with all of the parameters
	 * are replaced by those in the given 'prop', if no message found, 
	 * an empty message with this id will be returned. 
	 * 
	 * @param id id
	 * @param prop prop
	 * @return MessageBean
	 */
	public static MessageBean getMessage(String id, Properties prop) {		
		String reason = null;
		String response = null;
		Iterator<Object> iter = null;
		MessageBean messageBean = getMessage(id);
		
		if (prop == null || prop.size() == 0) {
			return messageBean;
		}
		
		reason = messageBean.getReason();
		response = messageBean.getResponse();
		iter = prop.keySet().iterator();

		while (iter.hasNext()) {
			String key = (String) iter.next();
			String replacement = prop.getProperty(key);
			reason = StringUtils.replaceAll(reason, key, replacement);
			response = StringUtils.replaceAll(response, key, replacement);
		}

		messageBean.setReason(reason);
		messageBean.setResponse(response);

		return messageBean;
	}
	
}
