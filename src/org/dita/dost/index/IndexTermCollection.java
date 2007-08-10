/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2005 All Rights Reserved.
 */
package org.dita.dost.index;

import java.io.File;
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
import org.dita.dost.writer.EclipseIndexWriter;
import org.dita.dost.writer.JavaHelpIndexWriter;

/**
 * This class is a collection of index term.
 * 
 * @version 1.0 2005-05-18
 * 
 * @author Wu, Zhi Qiang
 */
public class IndexTermCollection {
	
	private static IndexTermCollection collection = null;
	/** The list of all index term */
	private List termList = new ArrayList(Constants.INT_16);

	/** The type of index term */
	private String indexType = null;

	/** The output file name of index term without extension */
	private String outputFileRoot = null;

	/**
	 * Private constructor used to forbid instance.
	 */
	private IndexTermCollection() {
	}
	
	/**
	 * The only interface to access IndexTermCollection instance
	 * @return Singleton IndexTermCollection instance
	 * @author Marshall
	 */
	public static IndexTermCollection getInstantce(){
		if(collection == null){
			collection = new IndexTermCollection();
		}
		return collection;
	}

	/**
	 * Get the index type.
	 * 
	 * @return
	 */
	public String getIndexType() {
		return this.indexType;
	}

	/**
	 * Set the index type.
	 * 
	 * @param type
	 *            The indexType to set.
	 */
	public void setIndexType(String type) {
		this.indexType = type;
	}

	/**
	 * All a new term into the collection.
	 * 
	 * @param term
	 */
	public void addTerm(IndexTerm term) {
		int i = 0;
		int termNum = termList.size();

		for (; i < termNum; i++) {
			IndexTerm indexTerm = (IndexTerm) termList.get(i);
			if (indexTerm.equals(term)) {
				return;
			}
			
			// Add targets when same term name and same term key
			if (indexTerm.getTermFullName().equals(term.getTermFullName())
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
	public List getTermList() {
		return termList;
	}

	/**
	 * Sort term list extracted from dita files base on Locale.
	 */
	public void sort() {
		int termListSize = termList.size();
		if (IndexTerm.getTermLocale() == null) {
			IndexTerm.setTermLocale(new Locale(Constants.LANGUAGE_EN,
					Constants.COUNTRY_US));
		}

		/*
		 * Sort all the terms recursively
		 */
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
	public void outputTerms() throws DITAOTException {
		StringBuffer buff = new StringBuffer(this.outputFileRoot);
		AbstractWriter indexWriter = null;
		Content content = new ContentImpl();
		
		if (Constants.INDEX_TYPE_HTMLHELP.equalsIgnoreCase(indexType)) {
			indexWriter = new CHMIndexWriter();			
			buff.append(".hhk");
		} else if (Constants.INDEX_TYPE_JAVAHELP.equalsIgnoreCase(indexType)) {
			indexWriter = new JavaHelpIndexWriter();			
			buff.append("_index.xml");			
		}else if (Constants.INDEX_TYPE_ECLIPSEHELP.equalsIgnoreCase(indexType)) { 
			indexWriter = new EclipseIndexWriter();		
			//We need to get rid of the ditamap or topic name in the URL
		    //so we can create index.xml file for Eclipse plug-ins.
			//int filepath = buff.lastIndexOf("\\");
			File indexDir = new File(buff.toString()).getParentFile();
			//buff.delete(filepath, buff.length());
			((EclipseIndexWriter)indexWriter).setFilePath(indexDir.getAbsolutePath());
			//buff.insert(filepath, "\\index.xml");
			buff = new StringBuffer(new File(indexDir, "index.xml").getAbsolutePath());
		}
		
		//if (!getTermList().isEmpty()){
		//Even if there is no term in the list create an empty index file
		//otherwise the compiler will report error.
			content.setCollection(this.getTermList());
			indexWriter.setContent(content);
			indexWriter.write(buff.toString());
		//}
	}

	/**
	 * Set the output file
	 * 
	 * @param fileRoot
	 *            The outputFile to set.
	 */
	public void setOutputFileRoot(String fileRoot) {
		this.outputFileRoot = fileRoot;
	}

}
