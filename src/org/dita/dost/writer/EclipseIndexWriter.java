/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2006 All Rights Reserved.
 */
package org.dita.dost.writer;

import java.io.File;
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
public class EclipseIndexWriter implements AbstractWriter, IDitaTranstypeIndexWriter  {
	
	/** List of indexterms */
	private List termList = null;
	
	private String filepath = null;
	
	private DITAOTJavaLogger javaLogger = null;
	
	/**
	 * Default constructor.
	 */
	public EclipseIndexWriter() {
		javaLogger = new DITAOTJavaLogger();
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
	 * @param outputStream outputStream
	 * @throws UnsupportedEncodingException UnsupportedEncodingException
	 */
	public void write(OutputStream outputStream) throws UnsupportedEncodingException {
		PrintWriter printWriter = null;
		int termNum = termList.size();
		
		try {
			printWriter = new PrintWriter(new OutputStreamWriter(
					outputStream, "UTF-8"));
			
			printWriter.print("<?xml version='1.0' encoding='UTF-8' ?>");
			printWriter.print(System.getProperty("line.separator"));
			printWriter.print("<index>");
			printWriter.print(System.getProperty("line.separator"));

			for (int i = 0; i < termNum; i++) {
				IndexTerm term = (IndexTerm) termList.get(i);
				
				outputIndexTerm(term, printWriter);
			}

			printWriter.print("</index>");
			printWriter.print(System.getProperty("line.separator"));
			
		} finally {
			printWriter.close();
		}
	
	}
	
	/**
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
        String termPrefix = term.getTermPrefix();

        printWriter.print("<entry keyword=\"");
        printWriter.print(term.getTermFullName());
        printWriter.print("\">");
        printWriter.print(System.getProperty("line.separator"));
        
        //Index-see and index-see-also terms should also generate links to its target
        //Otherwise, the term won't be displayed in the index tab.
        if (targets != null && !targets.isEmpty()){
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
	                printWriter.print(System.getProperty("line.separator"));
	        	}
	        	else if (targetName != null){
//	        		if (subTerms != null && subTermNum == 0){ //Eric
	        			printWriter.print("<topic href=\"");
	        			printWriter.print(replaceExtName(targetUri)); //Eric
	                    printWriter.print("\"");
	                    if (targetName.trim().length() > 0){
	                    	printWriter.print(" title=\"");
	                    	printWriter.print(target.getTargetName());
	                    	printWriter.print("\"");
	                    }
	                    printWriter.print("/>");
	                    printWriter.print(System.getProperty("line.separator"));       		
//	        		}
	                
	        	}
	        }
        }

        if (subTerms != null && subTermNum > 0) {

            for (int i = 0; i < subTermNum; i++) {
                IndexTerm subTerm = (IndexTerm) subTerms.get(i);
                outputIndexTerm(subTerm, printWriter);
            }

        }

        printWriter.print("</entry>");
        printWriter.print(System.getProperty("line.separator"));
    }
    
    /**
     * Replace the file extension.
     * @param aFileName file name to be replaced
     * @return repaced file name
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
    
    /**
     * Get index file name.
     * @param outputFileRoot root path
     * @return index file name
     */
	public String getIndexFileName(String outputFileRoot) {
		
		StringBuffer indexFilename;
		
		File indexDir = new File(outputFileRoot).getParentFile();
		// buff.delete(filepath, buff.length());
		setFilePath(indexDir.getAbsolutePath());
		// buff.insert(filepath, "\\index.xml");
		indexFilename = new StringBuffer(new File(indexDir, "index.xml")
				.getAbsolutePath());
		
		// TODO Auto-generated method stub
		return indexFilename.toString();
	}

}
