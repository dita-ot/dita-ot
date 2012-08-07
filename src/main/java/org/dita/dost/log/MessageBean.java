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

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.xml.sax.Locator;

import org.xml.sax.Attributes;

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
    private String srcFile;
    private int srcLine = -1;
    private int srcColumn = -1;
    

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
     * Set error location in source document.
     * @param locator current location during parsing
     * @return message bean with location set
     */
    public MessageBean setLocation(final Locator locator) {
        if (locator == null) {
            return this;
        }
        final MessageBean ret = new MessageBean(this);
        if (locator.getSystemId() != null) {
            URI s;
            try {
                s = new URI(locator.getSystemId());
            } catch (final URISyntaxException e) {
                throw new RuntimeException("Failed to parse URI '" + locator.getSystemId() + "': " + e.getMessage(), e);
            }
            ret.srcFile = new File(s).getAbsolutePath();
        }
        ret.srcLine = locator.getLineNumber(); 
        ret.srcColumn = locator.getColumnNumber();
        return ret;
    }
    
    /**
     * Set error location in source document.
     * @param atts source element attributes
     * @return message bean with location set
     */
    public MessageBean setLocation(final Attributes atts) {
        final MessageBean ret = new MessageBean(this);
        final String xtrf = atts.getValue(ATTRIBUTE_NAME_XTRF);
        if (xtrf != null) {
            ret.srcFile = xtrf;
        }
        final String xtrc = atts.getValue(ATTRIBUTE_NAME_XTRC);
        if (xtrc != null) {
            final int sep = xtrc.indexOf(';');
            if (sep != -1) {
                final int delim = xtrc.indexOf(COLON, sep + 1);
                if (delim != -1) {
                    ret.srcLine = Integer.parseInt(xtrc.substring(sep + 1, delim));
                    ret.srcColumn = Integer.parseInt(xtrc.substring(delim + 1));
                }
            }
        }
        return ret;
    }

    /**
     * Generate string for MessageBean.
     * @return string
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuffer buff = new StringBuffer(INT_256);

        if (srcFile != null) {
            buff.append(srcFile);
            if (srcLine != -1 && srcColumn != -1) {
                buff.append(':')
                    .append(Integer.valueOf(srcLine))
                    .append(':')
                    .append(Integer.valueOf(srcColumn));
            }
            buff.append(": ");
        }
        buff.append("[").append(id).append("]");
        buff.append("[").append(type).append("] ");
        buff.append(reason);
        if (response != null) {
            buff.append(" ").append(response);
        }

        return buff.toString();
    }

}
