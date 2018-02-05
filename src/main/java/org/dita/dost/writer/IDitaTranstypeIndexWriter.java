/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2010 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.writer;

import java.util.List;

import org.dita.dost.index.IndexTerm;

/**
 * Interface IDitaTranstypeIndexWriter.
 *
 */
public interface IDitaTranstypeIndexWriter {

    /**
     * Get index file name.
     * @param outputFileRoot root
     * @return index file name
     */
    String getIndexFileName(String outputFileRoot);

    void setTermList(final List<IndexTerm> termList);

}
