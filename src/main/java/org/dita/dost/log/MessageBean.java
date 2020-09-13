/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2005 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.log;

import net.sf.saxon.s9api.XdmNode;
import org.apache.tools.ant.Location;
import org.dita.dost.exception.DITAOTException;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;

import java.net.URI;
import java.net.URISyntaxException;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.util.URLUtils.toURI;

/**
 * Class description goes here.
 *
 * @author Wu, Zhi Qiang
 */
public final class MessageBean {

    public enum Type {
        FATAL, ERROR, WARN, INFO, DEBUG
    }

    public static final String FATAL = Type.FATAL.name();
    public static final String ERROR = Type.ERROR.name();
    public static final String WARN = Type.WARN.name();
    public static final String INFO = Type.INFO.name();
    public static final String DEBUG = Type.DEBUG.name();

    public final String id;
    public final Type type;
    public final String reason;
    private final String response;
    private URI srcFile;
    private int srcLine = -1;
    private int srcColumn = -1;

    /**
     * Constructor with params to init.
     * @param mbId id
     * @param mbType type
     * @param mbReason reason
     * @param mbResponse response
     * @deprecated since 3.0
     */
    @Deprecated
    public MessageBean(final String mbId, final String mbType, final String mbReason, final String mbResponse) {
        id = mbId;
        type = Type.valueOf(mbType);
        reason = mbReason;
        response = mbResponse;
    }

    /**
     * Constructor with params to init.
     * @param mbId id
     * @param mbType type
     * @param mbReason reason
     * @param mbResponse response
     */
    public MessageBean(final String mbId, final Type mbType, final String mbReason, final String mbResponse) {
        id = mbId;
        type = mbType;
        reason = mbReason;
        response = mbResponse;
    }

    /**
     * Copy constructor.
     *
     * @param message message
     */
    public MessageBean(final MessageBean message) {
        this(message.id, message.type, message.reason, message.response);
    }

    /**
     * Getter function of id.
     * @return Returns the id.
     * @deprecated since 3.0
     */
    @Deprecated
    public String getId() {
        return id;
    }

    /**
     * Getter function of reason.
     * @return Returns the reason
     * @deprecated since 3.0
     */
    @Deprecated
    public String getReason() {
        return reason;
    }

    /**
     * Getter function of response.
     * @return Returns the response, {@code null} if not defined
     * @deprecated since 3.0
     */
    @Deprecated
    public String getResponse() {
        return response;
    }

    /**
     * Getter function of type.
     * @return Returns the type.
     * @deprecated since 3.0
     */
    @Deprecated
    public String getType() {
        return type != null ? type.name() : null;
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
            try {
                ret.srcFile = new URI(locator.getSystemId());
            } catch (final URISyntaxException e) {
                throw new RuntimeException("Failed to parse URI '" + locator.getSystemId() + "': " + e.getMessage(), e);
            }
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
        final URI xtrf = toURI(atts.getValue(ATTRIBUTE_NAME_XTRF));
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
     * Set error location in source document.
     * @param elem source element.
     * @return message bean with location set
     */
    public MessageBean setLocation(final Element elem) {
        final MessageBean ret = new MessageBean(this);
        final String xtrf = elem.getAttribute(ATTRIBUTE_NAME_XTRF);
        if (!xtrf.isEmpty()) {
            ret.srcFile = toURI(xtrf);
        }
        final String xtrc = elem.getAttribute(ATTRIBUTE_NAME_XTRC);
        if (!xtrc.isEmpty()) {
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
     * Set error location in source document.
     * @param elem source element.
     * @return message bean with location set
     */
    public MessageBean setLocation(final XdmNode elem) {
        final MessageBean ret = new MessageBean(this);
        final String xtrf = elem.attribute(ATTRIBUTE_NAME_XTRF);
        if (xtrf != null && !xtrf.isEmpty()) {
            ret.srcFile = toURI(xtrf);
        }
        final String xtrc = elem.attribute(ATTRIBUTE_NAME_XTRC);
        if (xtrc != null && !xtrc.isEmpty()) {
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

    public MessageBean setLocation(final Location location) {
        final MessageBean ret = new MessageBean(this);
        ret.srcFile = toURI(location.getFileName());
        ret.srcLine = location.getLineNumber();
        ret.srcColumn = location.getColumnNumber();
        return ret;
    }

    /**
     * Generate string for MessageBean.
     * @return string
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        final StringBuilder buff = new StringBuilder(256);

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

    /**
     * Create exception from message data.
     */
    public DITAOTException toException() {
        return new DITAOTException(toString());
    }

}
