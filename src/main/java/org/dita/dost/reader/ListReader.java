/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.reader;

import static org.dita.dost.util.Constants.*;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.module.Content;
import org.dita.dost.module.ContentImpl;
import org.dita.dost.util.ListUtils;
import org.dita.dost.util.StringUtils;

/**
 * ListReader reads "dita.list" file in temp directory.
 * "dita.list" file contains information of the files that should be processed.
 * 
 * @author Zhang, Yuan Peng
 * @deprecated use {@link org.dita.dost.util.Job} instead
 */
@Deprecated
public final class ListReader implements AbstractReader {

    private final LinkedList<String> refList;
    private final Content content;
    private Map<String, String> copytoMap = new HashMap<String, String>();
    private final Set<String> schemeSet = new HashSet<String>();
    private String inputMap;
    /**
     * Default constructor of ListReader class.
     */
    public ListReader() {
        super();
        refList = new LinkedList<String>();
        content = new ContentImpl();
    }

    public void read(final String filename) {
        Properties propterties = null;
        try {
            propterties=ListUtils.getDitaList();
        } catch (final IOException e) {
            throw new RuntimeException("Reading list file failed: " + e.getMessage(), e);
        }

        setList(propterties);
        schemeSet.addAll(StringUtils.restoreSet(propterties.getProperty(SUBJEC_SCHEME_LIST, "")));
        inputMap = propterties.getProperty(INPUT_DITAMAP);
    }

    private void setList(final Properties property){
        content.setValue(property.getProperty("user.input.dir"));
        
        // Parse copy-to target to source map list, and restore the copy-to map
        final String copytoMapEntries = property.getProperty(COPYTO_TARGET_TO_SOURCE_MAP_LIST);
        copytoMap = StringUtils.restoreMap(copytoMapEntries);

        final String liststr = property.getProperty(FULL_DITAMAP_TOPIC_LIST)
                + COMMA
                + property.getProperty(CONREF_TARGET_LIST)
                + COMMA
                + property.getProperty(COPYTO_SOURCE_LIST);

        final StringTokenizer tokenizer = new StringTokenizer(liststr,COMMA);
        while (tokenizer.hasMoreTokens()) {
            refList.addFirst(tokenizer.nextToken());
        }
        content.setCollection(Collections.unmodifiableList(refList));
    }

    /**
     * @return content value {@code String}; collection unmodifiable {@code List<String>}
     */
    public Content getContent() {
        return content;
    }

    public void setLogger(final DITAOTLogger logger) {
    }

    /**
     * Return the copy-to map.
     * @return copy-to map
     */
    public Map<String, String> getCopytoMap() {
        return copytoMap;
    }

    /**
     * @return the schemeSet
     */
    public Set<String> getSchemeSet() {
        return schemeSet;
    }

    /**
     * @return the inputMap
     */
    public String getInputMap() {
        return inputMap;
    }
    
}
