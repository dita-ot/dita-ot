/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2005 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.exception;

import net.sf.saxon.trans.UncheckedXPathException;
import net.sf.saxon.trans.XPathException;
import org.dita.dost.log.MessageBean;

/**
 * Exception class for DITAOT, used to handle exceptions in Java modules.
 *
 * @author Wu, Zhi Qiang
 */
public final class DITAOTException extends Exception {

    /** serial version. */
    private static final long serialVersionUID = -7505646495801170017L;
    /** message bean. */
    private MessageBean messageBean = null;
    /** capture flag. */
    private boolean captured = false;

    /**
     * Constructs a new DITAOTException with {@code null}
     * as its detail message.
     */
    public DITAOTException() {
        this(null, (Throwable) null);
    }

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the detail message
     */
    public DITAOTException(final String message) {
        this(message, (Throwable) null);
    }

    /**
     * Constructs a new exception with the specified detail cause.
     *
     * @param cause the cause
     */
    public DITAOTException(final Throwable cause) {
        this(null, cause);
    }

    /**
     * Constructs a new exception with the specified detail message and
     * cause.
     *
     * @param message the detail message.
     * @param cause the cause
     */
    public DITAOTException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new exception with cause, using location information from a root {@link XPathException}
     *
     * @param cause the cause
     */
    public DITAOTException(final UncheckedXPathException cause) {
        super(cause.getXPathException().getMessageAndLocation(), cause.getXPathException());
    }

    /**
     * Constructs a new exception with the specified detail message and
     * cause, using location information from a root {@link XPathException}
     *
     * @param message the detail message.
     * @param cause the cause
     */
    public DITAOTException(final String message, final UncheckedXPathException cause) {
        super(message + ": " + cause.getXPathException().getMessageAndLocation(), cause.getXPathException());
    }

    /**
     * Constructs a new exception with the detailed messagebean and
     * cause.
     *
     * @param msgBean the bean contains detailed information for log statistic.
     * @param cause the cause
     * @param message the detail message.
     */
    public DITAOTException(final MessageBean msgBean, final Throwable cause, final String message) {
        super(message, cause);
        messageBean = msgBean;
    }

}
