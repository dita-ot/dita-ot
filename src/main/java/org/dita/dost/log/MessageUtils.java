/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2005 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.log;

import static org.dita.dost.util.Constants.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.xml.parsers.DocumentBuilder;

import com.google.common.annotations.VisibleForTesting;
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
    private static final String RESOURCE = "config" + File.separator + CLASSPATH_RESOURCE;

    // Variables

    public static final ResourceBundle msgs = ResourceBundle.getBundle("messages", new Locale("en", "US"), MessageUtils.class.getClassLoader());

    private final Hashtable<String, MessageBean> hashTable = new Hashtable<>();
    private static MessageUtils utils;

    // Constructors

    /**
     * Default construtor
     */
    @VisibleForTesting
    MessageUtils(){
    }

    /**
     * Get singleton instance.
     * 
     * @return MessageUtils singleton instance
     * @deprecated since 3.0
     */
    @Deprecated
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
     * @deprecated since 3.0
     */
    @Deprecated
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
     * @deprecated since 3.0
     */
    @Deprecated
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
     * Get the message respond to the given id with all of the parameters
     * are replaced by those in the given 'prop', if no message found,
     * an empty message with this id will be returned.
     * 
     * @param id id
     * @param params message parameters
     * @return MessageBean
     */
    public static MessageBean getMessage(final String id, final String... params) {
        if (!msgs.containsKey(id)) {
            throw new IllegalArgumentException("Message for ID '" + id + "' not found");
        }
        final String msg = MessageFormat.format(msgs.getString(id), (Object[]) params);
        MessageBean.Type type = null;
        switch (id.substring(id.length() - 1)) {
            case "F":
                type = MessageBean.Type.FATAL;
                break;
            case "E":
                type = MessageBean.Type.ERROR;
                break;
            case "W":
                type = MessageBean.Type.WARN;
                break;
            case "I":
                type = MessageBean.Type.INFO;
                break;
            case "D":
                type = MessageBean.Type.DEBUG;
                break;
        }
        return new MessageBean(id, type, msg, null);
    }

}
