package com.idiominc.ws.opentopic.fo.index2.configuration;

/**
 * User: Ivan Luzyanin
 * Date: 25.06.2005
 * Time: 11:00:20
 */
public class ParseException
		extends Exception {
	public ParseException() {
	}


	public ParseException(String message) {
		super(message);
	}


	public ParseException(Throwable cause) {
		super(cause);
	}


	public ParseException(String message, Throwable cause) {
		super(message, cause);
	}
}
