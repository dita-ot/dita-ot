/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
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
    public String getIndexFileName(String outputFileRoot);

    public void setTermList(final List<IndexTerm> termList);
    
}
