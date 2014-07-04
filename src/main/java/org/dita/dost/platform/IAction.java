/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2005, 2006 All Rights Reserved.
 */
package org.dita.dost.platform;

import java.util.List;
import java.util.Map;

import org.dita.dost.log.DITAOTLogger;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Interface.
 * @author Zhang, Yuan Peng
 */
public interface IAction {
    /**
     * Set the input string.
     * @param input input
     */
    void setInput(List<String> input);
    /**
     * Add input parameter.
     * @param name parameter name
     * @param value parameter value
     */
    void addParam(String name, String value);
    /**
     * Return the result.
     * @return result
     */
    String getResult();
    /**
     * Return the result.
     * @param output output to write results to
     */
    void getResult(ContentHandler output) throws SAXException;
    /**
     * Set the feature table.
     * @param h hastable
     */
    void setFeatures(Map<String, Features> h);
    /**
     * Set logger.
     * @param logger logger instance
     */
    public void setLogger(DITAOTLogger logger);
}
