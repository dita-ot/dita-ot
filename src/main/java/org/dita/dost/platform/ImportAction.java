/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2005, 2006 All Rights Reserved.
 */
package org.dita.dost.platform;

import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dita.dost.log.DITAOTLogger;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * ImportAction implements IAction and import the resource
 * provided by plug-ins into the xsl files and ant scripts.
 * @author Zhang, Yuan Peng
 */
abstract class ImportAction implements IAction {

    /** Action values. */
    protected final Set<String> valueSet;
    /** Action parameters. */
    protected final Hashtable<String,String> paramTable;
    protected DITAOTLogger logger;
    /** Plug-in features. */
    protected Map<String, Features> featureTable = null;

    /**
     * Default Constructor.
     */
    public ImportAction() {
        valueSet = new LinkedHashSet<String>(16);
        paramTable = new Hashtable<String,String>();
    }

    @Override
    public String getResult() {
        throw new UnsupportedOperationException();
    }
    
    /**
     * get result.
     * @return result
     */
    @Override
    public abstract void getResult(ContentHandler output) throws SAXException;

    /**
     * set input.
     * @param input input
     */
    @Override
    public void setInput(final List<String> input) {
        valueSet.addAll(input);
    }

    @Override
    public void addParam(final String name, final String value) {
        paramTable.put(name, value);
    }
    /**
     * Set the feature table.
     * @param h hastable
     */
    @Override
    public void setFeatures(final Map<String, Features> h) {
        featureTable = h;
    }

    @Override
    public void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
    }

}
