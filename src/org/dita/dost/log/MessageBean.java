/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2005 All Rights Reserved.
 */
package org.dita.dost.log;

import static org.dita.dost.util.Constants.*;

/**
 * Class description goes here.
 * 
 * @author Wu, Zhi Qiang
 */
public final class MessageBean {
    
    public static final String FATAL = "FATAL";
    public static final String ERROR = "ERROR";
    public static final String WARN = "WARN";
    public static final String INFO = "INFO";
    public static final String DEBUG = "DEBUG";
    
    private String id;

    private String type;

    private String reason;

    private String response;

    /**
     * Default constructor.
     * 
     * @deprecated use {@link #MessageBean(String, String, String, String)} with {@code null} arguments instead
     */
    @Deprecated
    public MessageBean() {
        this(null, null, null, null);
    }

    /**
     * Constructor with params to init.
     * @param mbId id
     * @param mbType type
     * @param mbReason reason
     * @param mbResponse response
     */
    public MessageBean(final String mbId, final String mbType, final String mbReason, final String mbResponse) {
        this.id = mbId;
        this.type = mbType;
        this.reason = mbReason;
        this.response = mbResponse;
    }

    /**
     * Copy constructor.
     * 
     * @param message message
     */
    public MessageBean(final MessageBean message) {
        this(message.getId(), message.getType(), message.getReason(), message.getResponse());
    }

    /**
     * Getter function of id.
     * @return Returns the id.
     */
    public String getId() {
        return id;
    }

    /**
     * Setter funciton of id.
     * @param mbId The id to set.
     * @deprecated this setter will be removed in the future when the object is changed to be immutable
     */
    @Deprecated
    public void setId(final String mbId) {
        this.id = mbId;
    }

    /**
     * Getter function of reason.
     * @return Returns the reason
     */
    public String getReason() {
        return reason;
    }

    /**
     * Setter function of reason.
     * @param mbReason The reason to set.
     * @deprecated this setter will be removed in the future when the object is changed to be immutable
     */
    @Deprecated
    public void setReason(final String mbReason) {
        this.reason = mbReason;
    }

    /**
     * Getter function of response.
     * @return Returns the response, {@code null} if not defined
     */
    public String getResponse() {
        return response;
    }

    /**
     * Setter function of response.
     * @param mbResponse The response to set.
     * @deprecated this setter will be removed in the future when the object is changed to be immutable
     */
    @Deprecated
    public void setResponse(final String mbResponse) {
        this.response = mbResponse;
    }

    /**
     * Getter function of type.
     * @return Returns the type.
     */
    public String getType() {
        return type;
    }

    /**
     * Setter function of type.
     * @param mbType The type to set.
     * @deprecated this setter will be removed in the future when the object is changed to be immutable
     */
    @Deprecated
    public void setType(final String mbType) {
        this.type = mbType;
    }

    /**
     * Generate string for MessageBean.
     * @return string
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuffer buff = new StringBuffer(INT_256);

        buff.append("[").append(id).append("]");
        buff.append("[").append(type).append("] ");
        buff.append(reason);
        if (response != null) {
            buff.append(" ").append(response);
        }

        return buff.toString();
    }

}
