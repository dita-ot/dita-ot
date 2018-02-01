/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2005 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.log;

import com.google.common.annotations.VisibleForTesting;

import java.io.File;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

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

    // Constructors

    /**
     * Default construtor
     */
    @VisibleForTesting
    MessageUtils() {
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
