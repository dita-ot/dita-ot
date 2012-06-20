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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
public final class MessageUtils {

    // Constants

    private static final String ELEMENT_MESSAGE = "message";
    private static final String ELEMENT_REASON = "reason";
    private static final String ELEMENT_RESPONSE = "response";
    private static final String ATTRIBUTE_ID = "id";
    private static final String ATTRIBUTE_TYPE = "type";
    private static String defaultResource = "resource/messages.xml";

    // Variables

    private static Hashtable<String, MessageBean> hashTable;
    private static DITAOTLogger logger = new DITAOTJavaLogger();
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
    private static synchronized MessageUtils getInstance(){
        if(utils == null){
            utils = new MessageUtils();
        }
        return utils;
    }

    // Methods

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
        if (new File(defaultResource).exists()) {
            loadMessages(defaultResource);
        } else {
            final InputStream msg = getClass().getClassLoader().getResourceAsStream(defaultResource);
            if (msg != null) {
                try {
                    loadMessages(msg);
                } catch (final Exception e) {
                    throw new RuntimeException("Failed to load messages configuration file: " + e.getMessage(), e);
                } finally {
                    try {
                        msg.close();
                    } catch (final IOException e) {
                        // NOOP
                    }
                }
            }
        }
    }

    /**
     * Load message from message file.
     * @param newMessageFile message file
     */
    public static void loadMessages(final String newMessageFile) {
        InputStream in = null;
        try {
            in = new FileInputStream(new File(newMessageFile));
            getInstance().loadMessages(in);
        } catch (final FileNotFoundException e) {
            throw new RuntimeException("Failed to load messages configuration file: " + e.getMessage(), e);
        } catch (final Exception e) {
            throw new RuntimeException("Failed to load messages configuration file: " + e.getMessage(), e);
        } finally {
            if (in != null) {
                try {
                    in.close();
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
    private void loadMessages(final InputStream in) throws Exception {
        // always assign a new instance to hashTable to avoid
        // to reload this method again and again when messages
        // loading failed.
        hashTable = new Hashtable<String, MessageBean>();

        try {
            final DocumentBuilderFactory factory = DocumentBuilderFactory
                    .newInstance();
            final DocumentBuilder builder = factory.newDocumentBuilder();
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

    /**
     * Get the message respond to the given id, if no message found,
     * an empty message with this id will be returned.
     * 
     * @param id message ID
     * @return messageBean
     */
    public static MessageBean getMessage(final String id) {
        if (hashTable == null) {
            loadDefaultMessages();
        }

        final MessageBean hashMessage = hashTable.get(id);
        if (hashMessage != null) {
            return new MessageBean(hashMessage);
        }

        // return a empty message when no message found,
        // and notify the user with a warning message.
        logger.logWarn("  Can't find message for id: " + id);
        return new MessageBean(id, "", "", "");
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
    public static MessageBean getMessage(final String id, final Properties prop) {
        final MessageBean messageBean = getMessage(id);

        if (prop == null || prop.size() == 0) {
            return messageBean;
        }

        String reason = messageBean.getReason();
        String response = messageBean.getResponse();
        final Iterator<Object> iter = prop.keySet().iterator();

        while (iter.hasNext()) {
            final String key = (String) iter.next();
            final String replacement = prop.getProperty(key);
            reason = StringUtils.replaceAll(reason, key, replacement);
            if (response != null) {
                response = StringUtils.replaceAll(response, key, replacement);
            }
        }

        return new MessageBean(messageBean.getId(), messageBean.getType(), reason, response);
    }

}
