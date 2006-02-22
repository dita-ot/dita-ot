/*
 * (c) Copyright IBM Corp. 2005, 2006 All Rights Reserved.
 */
package org.dita.dost.platform;

/**
 *
 * @author Zhang, Yuan Peng
 */
public interface IAction {
	public void setInput(String input);
	public void setParam(String param);
	public String getResult();
}
