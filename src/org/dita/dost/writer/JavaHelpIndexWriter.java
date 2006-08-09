/*
 * (c) Copyright IBM Corp. 2005 All Rights Reserved.
 */
package org.dita.dost.writer;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.dita.dost.exception.DITAOTException;
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
public class JavaHelpIndexWriter implements AbstractWriter {
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
	 * @throws UnsupportedEncodingException 
	 */
	public void write(OutputStream outputStream) throws UnsupportedEncodingException {
		PrintWriter printWriter = null;
		int termNum = termList.size();
		
		try {
			printWriter = new PrintWriter(new OutputStreamWriter(
					outputStream, "UTF-8"));
			
			printWriter.println("<?xml version='1.0' encoding='UTF-8' ?>");
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
	public void write(String filename) throws DITAOTException {		
		try {
			write(new FileOutputStream(filename));
		} catch (Exception e) {
			throw new DITAOTException(e);
		}
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
		int targetNum = (targets == null) ? 0: targets.size();
		int subTermNum = (subTerms == null) ? 0 : subTerms.size();
		
		/*
		 * Don't set 'target' attribute for group purpose index item.
		 */
		if (subTermNum > 0) { 
			printWriter.print("<indexitem text=\"");		
			printWriter.print(term.getTermName());
			printWriter.print("\">");	
			
			for (int i = 0; i < subTermNum; i++) {
				IndexTerm subTerm = (IndexTerm) subTerms.get(i);
				outputIndexTerm(subTerm, printWriter);
			}
			
			printWriter.println("</indexitem>");
		} else {
			for (int i = 0; i < targetNum; i++) {
				IndexTermTarget target = (IndexTermTarget) targets.get(i);
				String targetURL = target.getTargetURI();

				/*
				 * Remove file extension from targetName, and replace all the
				 * file seperator with '_'.
				 */
				targetURL = targetURL.substring(0, targetURL
						.lastIndexOf("."));
				targetURL = targetURL.replace('\\', '_');
				targetURL = targetURL.replace('/', '_');
				targetURL = targetURL.replace('.', '_');

				printWriter.print("<indexitem text=\"");
				printWriter.print(term.getTermName());
				printWriter.print("\"");
				printWriter.print(" target=\"");
				printWriter.print(targetURL);
				printWriter.println("\"/>");
			}
		}		
		
	}

}
