/*
 * (c) Copyright IBM Corp. 2005 All Rights Reserved.
 */
package org.dita.dost.writer;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;

import org.dita.dost.index.IndexTerm;
import org.dita.dost.index.IndexTermTarget;
import org.dita.dost.module.Content;

/**
 * This class extends AbstractWriter, used to output index term 
 * into java help index file.
 * 
 * @version 1.0 2005-05-20
 * 
 * @author Wu, Zhi Qiang
 */
public class JavaHelpIndexWriter extends AbstractWriter {
	/** List of indexterms */
	private List termList = null;
	
	/**
	 * Default constructor
	 */
	public JavaHelpIndexWriter() {
	}
	
	/**
	 * Set the content for output.
     * 
	 * @param content The content to output
	 */
	public void setContent(Content content) {
		termList = (List) content.getCollection();
	}

	/**
	 * Output the java help index to the output stream.
     * 
	 * @param outputStream
	 */
	public void write(OutputStream outputStream) {
		PrintWriter printWriter = null;
		int termNum = termList.size();
		
		try {
			printWriter = new PrintWriter(new OutputStreamWriter(
					outputStream));
			
			printWriter.println("<?xml version='1.0' encoding='ISO-8859-1' ?>");
			printWriter.println("<!DOCTYPE index PUBLIC ");
			printWriter.println("\"-//Sun Microsystems Inc.//DTD JavaHelp Index Version 1.0//EN\" ");
			printWriter.println("\"http://java.sun.com/products/javahelp/index_1_0.dtd\">");
			
			printWriter.println("<index version=\"1.0\">");

			for (int i = 0; i < termNum; i++) {
				IndexTerm term = (IndexTerm) termList.get(i);
				
				outputIndexTerm(term, printWriter);
			}

			printWriter.println("</index>");
			
		} finally {
			printWriter.close();
		}
	}
	
	/** (non-Javadoc)
	 * @see org.dita.dost.writer.AbstractWriter#write(java.lang.String)
	 */
	public void write(String filename) {				
	}
	
	/**
     * Output the given indexterm into the PrintWriter.  
     * 
	 * @param term
	 * @param printWriter
	 */
	private void outputIndexTerm(IndexTerm term, PrintWriter printWriter) {
		List targets = term.getTargetList();
		List subTerms = term.getSubTerms();		
		int targetNum = targets.size();
		int subTermNum = subTerms.size();
		
		printWriter.print("<indexitem text=\"");		
		printWriter.print(term.getTermName());
		printWriter.print("\"");		
		
		for ( int i = 0; i<targetNum; i++) {
			IndexTermTarget target = (IndexTermTarget) targets.get(i);			
			String targetName = target.getTargetName();
			
			/*
			 * Remove file extension from targetName, and replace all the 
			 * file seperator with '_'.
			 */
			targetName = targetName.substring(0, targetName.lastIndexOf("."));
			targetName = targetName.replace('\\', '_');
			targetName = targetName.replace('/', '_');
			targetName = targetName.replace('.', '_');
			
			printWriter.print(" target=\"");
			printWriter.print(targetName);
			printWriter.print("\"");
		}
		
		printWriter.println(">");		
		
		if (subTerms != null && subTermNum > 0) {						
			for (int i = 0; i < subTermNum; i++) {
				IndexTerm subTerm = (IndexTerm) subTerms.get(i);
				outputIndexTerm(subTerm, printWriter);
			}			
			
		}		
		
		printWriter.println("</indexitem>");
	}

}
