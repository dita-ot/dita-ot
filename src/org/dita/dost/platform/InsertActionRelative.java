/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */
package org.dita.dost.platform;

import java.util.Iterator;

/**
 * InsertActionRelative is an abstract base class for InsertAction classes that need
 * to know the location of each file being inserted, in order to correctly rewrite relative
 * paths.
 * 
 * @author Deborah Pickett
 *
 */
public abstract class InsertActionRelative extends InsertAction implements IAction {

	/** Current processing file. */
	protected String currentFile;
	/**
	 * Constructor.
	 */
	public InsertActionRelative() {
		super();
	}
	/**
	 * Return the result.
	 * @return result
	 */
	@Override
	public String getResult() {
		final Iterator<String> iter = fileNameSet.iterator();
		try{
			while(iter.hasNext()){
				currentFile = iter.next();
				reader.parse(currentFile);
			}
		} catch (final Exception e) {
	       	logger.logException(e);
		}
		return retBuf.toString();
	}
}
