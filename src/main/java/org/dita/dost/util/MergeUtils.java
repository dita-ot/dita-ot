/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.util;

import static org.dita.dost.util.Constants.*;

import java.io.File;
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

    private final Hashtable<String, String> idMap;
    private int index;
    /** Set of visited topic files. */
    private final Set<String> visitSet;

    /**
     * Default Constructor
     */
    public MergeUtils() {
        super();
        idMap = new Hashtable<String, String>();
        visitSet = Collections.synchronizedSet(new HashSet<String>(INT_256));
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
    public boolean findId(final String id) {
        return id != null && idMap.containsKey(FileUtils.normalize(FileUtils.separatorsToUnix(id.trim()), UNIX_SEPARATOR));
    }

    /**
     * Add topic id to the idMap.
     * @param id topic id
     * @return updated topic id
     */
    public String addId(final String id) {
        if (id == null) {
            return null;
        }
        final String localId = FileUtils.separatorsToUnix(id.trim());
        index ++;
        final String newId = "unique_" + Integer.toString(index);
        idMap.put(FileUtils.normalize(localId, UNIX_SEPARATOR), newId);
        return newId;
    }

    /**
     * Add topic id-value pairs to idMap.
     * @param id id
     * @param value value
     */
    public void addId(final String id, final String value){
        if (id != null && value != null) {
            final String localId = FileUtils.separatorsToUnix(id.trim());
            final String localValue = value.trim();
            idMap.put(FileUtils.normalize(localId, UNIX_SEPARATOR), localValue);
        }
    }

    /**
     * Return the value corresponding to the id.
     * @param id id
     * @return value
     */
    public String getIdValue(final String id){
        if (id == null) {
            return null;
        }
        final String localId = FileUtils.separatorsToUnix(id.trim());
        return idMap.get(FileUtils.normalize(localId, UNIX_SEPARATOR));
    }

    /**
     * Return if this path has been visited before.
     * @param path topic path, may contain a fragment
     * @return true if has been visited
     */
    public boolean isVisited(final String path){
        String localPath = path;
        final int idx = path.indexOf(SHARP);
        if (idx != -1) {
            localPath=localPath.substring(0, idx);
        }
        return visitSet.contains(FileUtils.normalize(FileUtils.separatorsToUnix(localPath.trim()), UNIX_SEPARATOR));
    }

    /**
     * Add topic to set of visited topics.
     * 
     * @param path topic path, may contain a fragment
     */
    public void visit(final String path){
        String localPath = path;
        final int idx = path.indexOf(SHARP);
        if (idx != -1) {
            localPath=localPath.substring(0, idx);
        }
        visitSet.add(FileUtils.normalize(FileUtils.separatorsToUnix(localPath.trim()), UNIX_SEPARATOR));
    }

    /**
     * 
     * Get the first topic id.
     * @param path file path
     * @param dir file dir
     * @param useCatalog whether use catalog file for validation
     * @return topic id
     */
    public static String getFirstTopicId(final String path, final String dir, final boolean useCatalog){
        final DITAOTLogger logger = new DITAOTJavaLogger();
        String localPath = path;
        String localDir = dir;
        final StringBuffer firstTopicId = new StringBuffer();

        if (path != null && dir != null) {
            localPath = localPath.trim();
            localDir = localDir.trim();
        } else {
            return null;
        }
        final TopicIdParser parser = new TopicIdParser(firstTopicId);
        try {
            final XMLReader reader = StringUtils.getXMLReader();
            reader.setContentHandler(parser);

            if (useCatalog) {
                try {
                    Class.forName(RESOLVER_CLASS);
                    reader.setEntityResolver(CatalogUtils.getCatalogResolver());
                } catch (final ClassNotFoundException e) {
                    logger.logError(e.getMessage(), e) ;
                }
            }
            reader.parse(new File(localDir, localPath).toURI().toString());
        } catch (final Exception e) {
            logger.logError(e.getMessage(), e) ;
        }
        return firstTopicId.toString();

    }

}
