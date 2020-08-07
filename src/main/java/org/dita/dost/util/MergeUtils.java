/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2004, 2005 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.util;

import static org.dita.dost.util.URLUtils.*;

import java.io.File;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.dita.dost.log.DITAOTLogger;

import org.xml.sax.XMLReader;

/**
 * Utility that topic merge utilize. An instance can be reused by calling
 * {@link #reset()} between uses.
 */
public final class MergeUtils {

    private static final String PREFIX = "unique_";
    private final Map<URI, String> idMap;
    private int index;
    /** Set of visited topic files. */
    private final Set<URI> visitSet;
    private DITAOTLogger logger;
    private Job job;

    /**
     * Default Constructor
     */
    public MergeUtils() {
        super();
        idMap = new ConcurrentHashMap<>();
        visitSet = Collections.synchronizedSet(new HashSet<>(256));
        index = 0;
    }

    public void setJob(final Job job) {
        this.job = job;
    }

    public void setLogger(final DITAOTLogger logger) {
        this.logger = logger;
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
        final String newId = PREFIX + Integer.toString(index);
        idMap.put(localId, newId);
        return newId;
    }

    /**
     * Add topic id-value pairs to idMap.
     * @param id id
     * @param value value
     */
    public void addId(final URI id, final String value) {
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
    public String getIdValue(final URI id) {
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
    public boolean isVisited(final URI path) {
        final URI localPath = stripFragment(path).normalize();
        return visitSet.contains(localPath);
    }

    /**
     * Add topic to set of visited topics.
     *
     * @param path topic path, may contain a fragment
     */
    public void visit(final URI path) {
        final URI localPath = stripFragment(path).normalize();
        visitSet.add(localPath);
    }

    /**
     * Get the first topic id.
     *
     * @param file file URI
     * @param useCatalog whether use catalog file for validation
     * @return topic id
     */
    public String getFirstTopicId(final URI file, final boolean useCatalog) {
        assert file.isAbsolute();
        if (!job.getStore().exists(file)) {
            return null;
        }
        final StringBuilder firstTopicId = new StringBuilder();
        final TopicIdParser parser = new TopicIdParser(firstTopicId);
        try {
            job.getStore().transform(file, parser);
        } catch (final Exception e) {
            logger.error(e.getMessage(), e) ;
        }
        return firstTopicId.toString();
    }

}
