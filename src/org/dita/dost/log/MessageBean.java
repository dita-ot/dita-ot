/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

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
		this(null, null, null, null);
	}

	/**
	 * Constructor with params to init.
	 * @param mbId id
	 * @param mbType type
	 * @param mbReason reason
	 * @param mbResponse response
	 */
	public MessageBean(String mbId, String mbType, String mbReason, String mbResponse) {
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
	public MessageBean(MessageBean message) {
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
	 */
	public void setId(String mbId) {
		this.id = mbId;
	}

	/**
	 * Getter function of reason.
	 * @return Returns the reason.
	 */
	public String getReason() {
		return reason;
	}

	/**
	 * Setter function of reason.
	 * @param mbReason The reason to set.           
	 */
	public void setReason(String mbReason) {
		this.reason = mbReason;
	}

	/**
	 * Getter function of response.
	 * @return Returns the response.
	 */
	public String getResponse() {
		return response;
	}

	/**
	 * Setter function of response.
	 * @param mbResponse The response to set.
	 */
	public void setResponse(String mbResponse) {
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
	 */
	public void setType(String mbType) {
		this.type = mbType;
	}

	/**
	 * Generate string for MessageBean.
	 * @return string
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
