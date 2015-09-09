/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2008 All Rights Reserved.
 */
package org.dita.dost.platform;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.util.StringUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * InsertDependsAction implements IAction.
 * Parses an attribute value containing comma-separated identifiers.
 * Identifiers inside braces are replaced with the plugin features for the corresponding extension point.
 * @author Deborah Pickett
 */
final class InsertDependsAction implements IAction {

    /** Action parameters. */
    private final Hashtable<String,String> paramTable;
    /** Action value. */
    private List<String> value;
    /** Plug-in features. */
    private Map<String, Features> featureTable = null;
    /**
     * Constructor.
     */
    public InsertDependsAction() {
        paramTable = new Hashtable<>();
    }
    
    @Override
    public void getResult(final ContentHandler buf) throws SAXException {
        throw new UnsupportedOperationException();        
    }
    
    /**
     * Get result.
     * @return result
     */
    @Override
    public String getResult() {
        final List<String> result = new ArrayList<>();
        for (final String t: value) {
            final String token = t.trim();
            // Pieces which are surrounded with braces are extension points.
            if (token.startsWith("{") && token.endsWith("}")) {
                final String extension = token.substring(1, token.length() - 1);
                final String extensionInputs = Integrator.getValue(featureTable, extension);
                if (extensionInputs != null) {
                    result.add(extensionInputs);
                }
            } else {
                result.add(token);
            }
        }
        if (!result.isEmpty()){
            return StringUtils.join(result, ",");
        } else {
            return "";
        }
    }
    /**
     * Set input.
     * @param input input
     */
    @Override
    public void setInput(final List<String> input) {
        value = input;
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
    }

}
