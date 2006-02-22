/*
 * (c) Copyright IBM Corp. 2005 All Rights Reserved.
 */
package org.dita.dost.log;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

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
	private static Hashtable hashTable = null;
	private static final String MESSAGE_FILE = "resource/messages.xml";

	private static void loadMessages() {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(MESSAGE_FILE);

			Element messages = doc.getDocumentElement();
			NodeList messageList = messages.getElementsByTagName("message");

			hashTable = new Hashtable();

			for (int i = 0; i < messageList.getLength(); i++) {
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
			throw new RuntimeException(e);
		}
	}

	public static MessageBean getMessage(String id) {
		MessageBean message = null;
		
		if (hashTable == null) {
			loadMessages();
		}

		message = (MessageBean) hashTable.get(id);
		
		if (message == null) {
			throw new RuntimeException("Can't find message for id: " + id);
		}
		
		return message;
	}

	public static MessageBean getMessage(String id, Properties prop) {		
		String reason = null;
		String response = null;
		Iterator iter = null;
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
