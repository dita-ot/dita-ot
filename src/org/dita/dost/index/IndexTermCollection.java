/*
 * (c) Copyright IBM Corp. 2005 All Rights Reserved.
 */
package org.dita.dost.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.module.Content;
import org.dita.dost.module.ContentImpl;
import org.dita.dost.util.Constants;
import org.dita.dost.writer.AbstractWriter;
import org.dita.dost.writer.CHMIndexWriter;
import org.dita.dost.writer.JavaHelpIndexWriter;

/**
 * This class is a collection of index term.
 * 
 * @version 1.0 2005-05-18
 * 
 * @author Wu, Zhi Qiang
 */
public class IndexTermCollection {
	/** The list of all index term */
	private static List termList = new ArrayList(Constants.INT_16);

	/** The type of index term */
	private static String indexType = null;

	/** The output file name of index term without extension */
	private static String outputFileRoot = null;

	/**
	 * Private constructor used to forbid instance.
	 */
	private IndexTermCollection() {
	}

	/**
	 * Get the index type.
	 * 
	 * @return
	 */
	public static String getIndexType() {
		return IndexTermCollection.indexType;
	}

	/**
	 * Set the index type.
	 * 
	 * @param type
	 *            The indexType to set.
	 */
	public static void setIndexType(String type) {
		IndexTermCollection.indexType = type;
	}

	/**
	 * All a new term into the collection.
	 * 
	 * @param term
	 */
	public static void addTerm(IndexTerm term) {
		int i = 0;
		int termNum = termList.size();

		for (; i < termNum; i++) {
			IndexTerm indexTerm = (IndexTerm) termList.get(i);
			if (indexTerm.equals(term)) {
				return;
			}
			
			// Add targets when same term name and same term key
			if (indexTerm.getTermName().equals(term.getTermName())
					&& indexTerm.getTermKey().equals(term.getTermKey())) {
				indexTerm.addTargets(term.getTargetList());
				indexTerm.addSubTerms(term.getSubTerms());
				break;
			}
		}

		if (i == termNum) {
			termList.add(term);
		}
	}

	/**
	 * Get all the term list from the collection.
	 * 
	 * @return
	 */
	public static List getTermList() {
		return termList;
	}

	/**
	 * Sort term list extracted from dita files base on Locale.
	 */
	public static void sort() {
		if (IndexTerm.getTermLocale() == null) {
			IndexTerm.setTermLocale(new Locale(Constants.LANGUAGE_EN,
					Constants.COUNTRY_US));
		}

		/*
		 * Sort all the terms recursively
		 */
		int termListSize = termList.size();
		for (int i = 0; i < termListSize; i++) {
			IndexTerm term = (IndexTerm) termList.get(i);
			term.sortSubTerms();
		}

		Collections.sort(termList);
	}

	/**
	 * Output index terms into index file.
	 * 
	 * @throws DITAOTException
	 */
	public static void outputTerms() throws DITAOTException {
		StringBuffer buff = new StringBuffer(IndexTermCollection.outputFileRoot);
		AbstractWriter indexWriter = null;
		Content content = new ContentImpl();
		
		if (Constants.INDEX_TYPE_HTMLHELP.equalsIgnoreCase(indexType)) {
			indexWriter = new CHMIndexWriter();			
			buff.append(".hhk");
		} else if (Constants.INDEX_TYPE_JAVAHELP.equalsIgnoreCase(indexType)) {
			indexWriter = new JavaHelpIndexWriter();			
			buff.append("_index.xml");			
		}
		
		content.setCollection(IndexTermCollection.getTermList());
		indexWriter.setContent(content);
		indexWriter.write(buff.toString());
	}

	/**
	 * Set the output file
	 * 
	 * @param fileRoot
	 *            The outputFile to set.
	 */
	public static void setOutputFileRoot(String fileRoot) {
		IndexTermCollection.outputFileRoot = fileRoot;
	}

}
