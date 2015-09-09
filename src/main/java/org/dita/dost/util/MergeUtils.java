/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.util;

import static org.dita.dost.util.URLUtils.*;

import java.io.File;
import java.net.URI;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.log.DITAOTLogger;

import org.xml.sax.XMLReader;

/**
 * Utility that topic merge utilize. An instance can be reused by calling
 * {@link #reset()} between uses.
 */
public final class MergeUtils {

    private final Hashtable<URI, String> idMap;
    private int index;
    /** Set of visited topic files. */
    private final Set<URI> visitSet;

    /**
     * Default Constructor
     */
    public MergeUtils() {
        super();
        idMap = new Hashtable<>();
        visitSet = Collections.synchronizedSet(new HashSet<URI>(256));
        index = 0;
    }

    /**
     * Resets all internal data structures.
     */
    public void reset() {
        idMap.clear();
        visitSet.clear();
        index = 0;
    }

    /**
     * Find the topic id from idMap.
     * @param id topic id
     * @return true if find and false otherwise
     */
    public boolean findId(final URI id) {
        return id != null && idMap.containsKey(id.normalize());
    }

    /**
     * Add topic id to the idMap.
     * @param id topic id
     * @return updated topic id
     */
    public String addId(final URI id) {
        if (id == null) {
            return null;
        }
        final URI localId = id.normalize();
        index ++;
        final String newId = "unique_" + Integer.toString(index);
        idMap.put(localId, newId);
        return newId;
    }

    /**
     * Add topic id-value pairs to idMap.
     * @param id id
     * @param value value
     */
    public void addId(final URI id, final String value){
        if (id != null && value != null) {
            final URI localId = id.normalize();
            final String localValue = value.trim();
            idMap.put(localId, localValue);
        }
    }

    /**
     * Return the value corresponding to the id.
     * @param id id
     * @return value
     */
    public String getIdValue(final URI id){
        if (id == null) {
            return null;
        }
        final URI localId = id.normalize();
        return idMap.get(localId);
    }

    /**
     * Return if this path has been visited before.
     * @param path topic path, may contain a fragment
     * @return true if has been visited
     */
    public boolean isVisited(final URI path){
        final URI localPath = stripFragment(path).normalize();
        return visitSet.contains(localPath);
    }

    /**
     * Add topic to set of visited topics.
     * 
     * @param path topic path, may contain a fragment
     */
    public void visit(final URI path){
        final URI localPath = stripFragment(path).normalize();
        visitSet.add(localPath);
    }

    /**
     * 
     * Get the first topic id.
     * @param path file path
     * @param dir file dir
     * @param useCatalog whether use catalog file for validation
     * @return topic id
     */
    public static String getFirstTopicId(final URI path, final File dir, final boolean useCatalog){
        if (path == null && dir == null) {
            return null;
        }
        final DITAOTLogger logger = new DITAOTJavaLogger();
        final StringBuilder firstTopicId = new StringBuilder();
        final TopicIdParser parser = new TopicIdParser(firstTopicId);
        try {
            final XMLReader reader = XMLUtils.getXMLReader();
            reader.setContentHandler(parser);
            if (useCatalog) {
                reader.setEntityResolver(CatalogUtils.getCatalogResolver());
            }
            reader.parse(dir.toURI().resolve(path).toString());
        } catch (final Exception e) {
            logger.error(e.getMessage(), e) ;
        }
        return firstTopicId.toString();

    }

}
