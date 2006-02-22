/*
 * (c) Copyright IBM Corp. 2005 All Rights Reserved.
 */
package org.dita.dost.exception;

/**
 * Exception class for DITAOT, used to handle exceptions in Java modules.
 *
 * @author Wu, Zhi Qiang
 */
public class DITAOTException extends Exception {	
	private static final long serialVersionUID = -7505646495801170017L;

	/**
	 * Constructs a new DITAOTException with <code>null</code> as its detail message.
	 */
	public DITAOTException() {
		super();
	}

	/**
	 * Constructs a new exception with the specified detail message.
	 * 
	 * @param message the detail message.
	 */
	public DITAOTException(String message) {
		super(message);
	}

	/**
	 * Constructs a new exception with the specified detail cause. 
     * 
	 * @param cause the cause 
	 */
	public DITAOTException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a new exception with the specified detail message and
     * cause. 
     * 
	 * @param message the detail message.
	 * @param cause the cause 
	 */
	public DITAOTException(String message, Throwable cause) {
		super(message, cause);
	}

}
