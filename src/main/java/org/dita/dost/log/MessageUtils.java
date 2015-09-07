/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2005 All Rights Reserved.
 */
package org.dita.dost.log;

import static org.dita.dost.util.Constants.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;

import org.dita.dost.util.StringUtils;
import org.dita.dost.util.XMLUtils;
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
public final class MessageUtils {

    // Constants

    private static final String ELEMENT_MESSAGE = "message";
    private static final String ELEMENT_REASON = "reason";
    private static final String ELEMENT_RESPONSE = "response";
    private static final String ATTRIBUTE_ID = "id";
    private static final String ATTRIBUTE_TYPE = "type";
    private static final String CLASSPATH_RESOURCE = "messages.xml";
    private static final String RESOURCE = RESOURCES_DIR + "/" + CLASSPATH_RESOURCE;

    // Variables

    private final Hashtable<String, MessageBean> hashTable = new Hashtable<>();
    private static MessageUtils utils;

    // Constructors

    /**
     * Default construtor
     */
    private MessageUtils(){
    }

    /**
     * Get singleton instance.
     * 
     * @return MessageUtils singleton instance
     */
    public static synchronized MessageUtils getInstance(){
        if(utils == null){
            utils = new MessageUtils();
            utils.loadDefaultMessages();
        }
        return utils;
    }

    // Methods

    /**
     * Just bypass to invoke member function loadDefMsg().
     *
     */
    void loadDefaultMessages() {
		InputStream msg = null;
		try {
		    if (new File(RESOURCE).exists()) {
		    	msg = new FileInputStream(new File(RESOURCE));
		    } else {
		    	msg = this.getClass().getClassLoader().getResourceAsStream(CLASSPATH_RESOURCE);
			}
		    if (msg == null) {
                throw new RuntimeException("Message configuration file not found");
            }
		    loadMessages(msg);
		} catch (final Exception e) {
		    throw new RuntimeException("Failed to load messages configuration file: " + e.getMessage(), e);
		} finally {
			if (msg != null) {
		        try {
		            msg.close();
		        } catch (final IOException e) {
		            // NOOP
		        }
			}
		}
    }

    /**
     * Load message from message file.
     * @param in message file input stream
     */
    void loadMessages(final InputStream in) throws Exception {
    	synchronized (hashTable) {
    		hashTable.clear();
	        try {
	            final DocumentBuilder builder = XMLUtils.getDocumentBuilder();
	            final Document doc = builder.parse(in);
	
	            final Element messages = doc.getDocumentElement();
	            final NodeList messageList = messages.getElementsByTagName(ELEMENT_MESSAGE);
	
	            final int messageListLength = messageList.getLength();
	            for (int i = 0; i < messageListLength; i++) {
	                final Element message = (Element) messageList.item(i);
	                final Node reason = message.getElementsByTagName(ELEMENT_REASON).item(0);
	                final Node response = message.getElementsByTagName(ELEMENT_RESPONSE)
	                        .item(0);
	
	                final NamedNodeMap attrs = message.getAttributes();
	
	                final MessageBean messageBean = new MessageBean(
	                        attrs.getNamedItem(ATTRIBUTE_ID).getNodeValue(),
	                        attrs.getNamedItem(ATTRIBUTE_TYPE).getNodeValue(),
	                        reason.getFirstChild().getNodeValue(),
	                        response.getFirstChild() != null ? response.getFirstChild().getNodeValue() : null);
	
	                hashTable.put(messageBean.getId(), messageBean);
	            }
	        } catch (final Exception e) {
	            throw new Exception("Failed to read messages configuration file: " + e.getMessage(), e);
	        }
    	}
    }

    /**
     * Get the message respond to the given id, if no message found,
     * an empty message with this id will be returned.
     * 
     * @param id message ID
     * @return messageBean
     */
    private MessageBean getMessage(final String id) {
        if (hashTable == null) {
        	throw new IllegalStateException("Messages have not been loaded");
        }

        final MessageBean hashMessage = hashTable.get(id);
        if (hashMessage == null) {
            throw new IllegalArgumentException("Message for ID '" + id + "' not found");
        }
        return new MessageBean(hashMessage);
    }
    
    /**
     * Get the message respond to the given id with all of the parameters
     * are replaced by those in the given 'prop', if no message found,
     * an empty message with this id will be returned.
     * 
     * @param id id
     * @param params message parameters
     * @return MessageBean
     */
    public MessageBean getMessage(final String id, final String... params) {
        final MessageBean messageBean = getMessage(id);
        if (params.length == 0) {
            return messageBean;
        }
        String reason = messageBean.getReason();
        String response = messageBean.getResponse();
        for (int i = 0; i < params.length; i++) {
            final String key = "%" + Integer.toString(i + 1);
            final String replacement = params[i];
            reason = StringUtils.replaceAll(reason, key, replacement);
            if (response != null) {
                response = StringUtils.replaceAll(response, key, replacement);
            }
        }

        return new MessageBean(messageBean.getId(), messageBean.getType(), reason, response);
    }

}
