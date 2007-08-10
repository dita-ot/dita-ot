/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2006 All Rights Reserved.
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
import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.module.Content;
import org.dita.dost.util.Constants;

/**
 * This class extends AbstractWriter, used to output index term 
 * into eclipse help index file.
 * 
 *  @author Sirois, Eric
 *  
 *  @version 1.0 2006-10-17
 */
public class EclipseIndexWriter implements AbstractWriter {
	
	/** List of indexterms */
	private List termList = null;
	
	private String filepath = null;
	
	private DITAOTJavaLogger javaLogger = new DITAOTJavaLogger();
	
	/**
	 * Default constructor
	 */
	public EclipseIndexWriter() {
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
	 * Set the file path for output.
     * 
	 * @param filePath The file path to where the plugin are created.
	 */
	public void setFilePath(String filePath) {
		this.filepath = filePath;
	}
	
	/**
	 *  
	 * @return filePath The file path to the plugin.xml file
	 */	
	public String getFilePath(){
		
		return filepath;
	}

	/**
	 * Output the eclipse help index to the output stream.
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
			printWriter.println("<index>");

			for (int i = 0; i < termNum; i++) {
				IndexTerm term = (IndexTerm) termList.get(i);
				
				outputIndexTerm(term, printWriter);
			}

			printWriter.println("</index>");
			
		} finally {
			printWriter.close();
		}
		if (getFilePath() != null){
			addIndexExtension(getFilePath());
		}else{
			javaLogger.logError("The output file path to the file plugin.xml cannot be found. "+
					"Unable to add Eclipse Index extension for this plugin");
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
        int targetNum = targets.size();
        int subTermNum = subTerms.size();

        printWriter.print("<entry keyword=\"");
        printWriter.print(term.getTermFullName());
        printWriter.print("\">");
        printWriter.println();
        
        for (int i = 0; i < targetNum; i++) {
            IndexTermTarget target = (IndexTermTarget) targets.get(i);
        	String targetUri = target.getTargetURI();
        	String targetName = target.getTargetName();
        	if (targetUri == null) {
        		javaLogger.logDebug("Term for " + target.getTargetName() + " does not have a target");
            	printWriter.print("<topic");
                printWriter.print(" title=\"");
                printWriter.print(target.getTargetName());
                printWriter.print("\"/>");
                printWriter.println();
        	}
        	else if (targetName != null){
        		if (subTerms != null && subTermNum == 0){ //Eric
        			printWriter.print("<topic href=\"");
        			printWriter.print(replaceExtName(targetUri)); //Eric
                    printWriter.print("\"");
                    if (targetName.trim().length() > 0){
                    	printWriter.print(" title=\"");
                    	printWriter.print(target.getTargetName());
                    	printWriter.print("\"");
                    }
                    printWriter.print("/>");
                    printWriter.println();        		
        		}
                
        	}
        }

        if (subTerms != null && subTermNum > 0) {

            for (int i = 0; i < subTermNum; i++) {
                IndexTerm subTerm = (IndexTerm) subTerms.get(i);
                outputIndexTerm(subTerm, printWriter);
            }

        }

        printWriter.println("</entry>");
    }
    
    /**
     * Replace the file extension
     * @param aFileName
     * @return
     */
    public String replaceExtName(String aFileName){
    	String fileName;
        int fileExtIndex;
        int index;
    	
    	index = aFileName.indexOf(Constants.SHARP);
		
    	if (aFileName.startsWith(Constants.SHARP)){
    		return aFileName;
    	} else if (index != -1){
    		fileName = aFileName.substring(0,index); 
    		fileExtIndex = fileName.lastIndexOf(Constants.DOT);
    		return (fileExtIndex != -1)
    			? fileName.substring(0, fileExtIndex) + Constants.FILE_EXTENSION_HTML + aFileName.substring(index)
    			: aFileName;
    	} else {
    		fileExtIndex = aFileName.lastIndexOf(Constants.DOT);
    		return (fileExtIndex != -1)
    			? (aFileName.substring(0, fileExtIndex) + Constants.FILE_EXTENSION_HTML) 
    			: aFileName;
    	}
    }
    
    private void addIndexExtension(String filePath){
    	
    	
    }

}
