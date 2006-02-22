/*
 * (c) Copyright IBM Corp. 2005 All Rights Reserved.
 */
package org.dita.dost.log;

import org.dita.dost.util.Constants;

/**
 * Class description goes here.
 * 
 * @author Wu, Zhi Qiang
 */
public class MessageBean {
	private String id = null;

	private String type = null;

	private String reason = null;

	private String response = null;

	/**
	 * Default constructor.
	 */
	public MessageBean() {
	}

	/**
	 * @param id
	 * @param type
	 * @param reason
	 * @param response
	 */
	public MessageBean(String id, String type, String reason, String response) {
		this.id = id;
		this.type = type;
		this.reason = reason;
		this.response = response;
	}

	/**
	 * @return Returns the id.
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            The id to set.
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return Returns the reason.
	 */
	public String getReason() {
		return reason;
	}

	/**
	 * @param reason
	 *            The reason to set.
	 */
	public void setReason(String reason) {
		this.reason = reason;
	}

	/**
	 * @return Returns the response.
	 */
	public String getResponse() {
		return response;
	}

	/**
	 * @param response
	 *            The response to set.
	 */
	public void setResponse(String response) {
		this.response = response;
	}

	/**
	 * @return Returns the type.
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type
	 *            The type to set.
	 */
	public void setType(String type) {
		this.type = type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer buff = new StringBuffer(Constants.INT_256);
		
		buff.append("[").append(id).append("]");
		buff.append("[").append(type).append("] ");
		buff.append(reason);
		buff.append(" ").append(response);
		
		return buff.toString();
	}

}
