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
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.index.IndexTerm;
import org.dita.dost.index.IndexTermTarget;
import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.log.MessageUtils;
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
public class EclipseIndexWriter extends AbstractExtendDitaWriter implements AbstractWriter, IDitaTranstypeIndexWriter {
	
	/** List of indexterms */
	private List<IndexTerm> termList = null;
	
	private String filepath = null;
	
	private DITAOTJavaLogger javaLogger = null;
	
	private String targetExt = Constants.FILE_EXTENSION_HTML;
	
	/** 
     * Boolean to indicate when we are processing indexsee and child elements
	 */
	private boolean inIndexsee = false;
	
	/** List of index terms used to search for see references. */ 
	private List<IndexTerm> termCloneList = null;
	
	
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
		termList = (List<IndexTerm>) content.getCollection();
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
		
		//boolean for processing indexsee the new markup (Eclipse 3.6 feature).
		boolean indexsee = false;
		
		//RFE 2987769 Eclipse index-see
		if (this.getPipelineHashIO() != null){
			
        	indexsee = Boolean.valueOf(this.getPipelineHashIO().getAttribute("eclipse.indexsee"));
        	targetExt = this.getPipelineHashIO().getAttribute(Constants.ANT_INVOKER_EXT_PARAM_TARGETEXT);
        	
        }
		
		try {
			printWriter = new PrintWriter(new OutputStreamWriter(
					outputStream, "UTF-8"));
			
			printWriter.print("<?xml version='1.0' encoding='UTF-8' ?>");
			printWriter.print(System.getProperty("line.separator"));
			printWriter.print("<index>");
			printWriter.print(System.getProperty("line.separator"));
			
			//Clone the list of indexterms so we can look for see references
			termCloneList = cloneIndextermList(termList);

			for (int i = 0; i < termNum; i++) {
				IndexTerm term = (IndexTerm) termList.get(i);
				outputIndexTerm(term, printWriter, indexsee);
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
		OutputStream out = null;
		try {
			out = new FileOutputStream(filename);
			write(out);
		} catch (Exception e) {			
			javaLogger.logError(e.getMessage());
			e.printStackTrace(); 
			throw new DITAOTException(e);
		} finally {
			if (out != null) {
				try {
	                out.close();
                } catch (IOException e) {
                	javaLogger.logException(e);
                }
			}
		}
	}
	
	/**
     * Output the given indexterm into the PrintWriter.  
     * 
	 * @param term
	 * @param printWriter
	 * @param indexsee
	 * 
	 * RFE 2987769 - Added indexsee parameter to keep track of the processing pipeline.
	 */
    private void outputIndexTerm(IndexTerm term, PrintWriter printWriter, boolean indexsee) {
        
    	List<IndexTerm> subTerms = term.getSubTerms();
        int subTermNum = subTerms.size();
        
        outputIndexTermStartElement (term, printWriter, indexsee);
        
        if (subTerms != null && subTermNum > 0) {

            for (int i = 0; i < subTermNum; i++) {
                IndexTerm subTerm = (IndexTerm) subTerms.get(i);
                
                outputIndexTerm(subTerm, printWriter, indexsee);
                
            }

        }
        
        outputIndexTermEndElement (term, printWriter, indexsee);
        
    }
    
    /**
     * Replace the file extension.
     * @param aFileName file name to be replaced
     * @return replaced file name
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
    			? fileName.substring(0, fileExtIndex) + targetExt + aFileName.substring(index)
    			: aFileName;
    	} else {
    		fileExtIndex = aFileName.lastIndexOf(Constants.DOT);
    		return (fileExtIndex != -1)
    			? (aFileName.substring(0, fileExtIndex) + targetExt) 
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
	
	/*
	 * Method for see references in Eclipse. This version does not have a 
	 * dependency on a specific Eclipse version.
	 * 
	 * @param term  The indexterm to be processed.
	 * @param printWriter The Writer used for writing content to disk.
	 */
	private void outputIndexEntry(IndexTerm term, PrintWriter printWriter) {

		List<IndexTermTarget> targets = term.getTargetList();
		int targetNum = targets.size();
		
		boolean foundIndexTerm = false;
		boolean foundIndexsee = false;
		
		String indexSeeRefTerm = null;

		/*
		 * Use the cloned List to find the index-see reference in the list. If
		 * found use that target URI for the href value, otherwise return a
		 * warning to the build. RFE 2987769 Eclipse index-see
		 */

		int termCloneNum = termCloneList.size();

		// Index-see and index-see-also terms should also generate links to its
		// target
		// Otherwise, the term won't be displayed in the index tab.
		if (targets != null && !targets.isEmpty()) {
			for (int i = 0; i < targetNum; i++) {
				IndexTermTarget target = (IndexTermTarget) targets.get(i);
				String targetUri = target.getTargetURI();
				String targetName = target.getTargetName();
				if (targetUri == null) {
					printWriter.print("<topic");
					printWriter.print(" title=\"");
					printWriter.print(target.getTargetName());
					printWriter.print("\"/>");
					printWriter.print(System.getProperty("line.separator"));
				} else if (targetName != null && targetName.trim().length() > 0) {

					/*
					 * Check to see if the target Indexterm is a "see"
					 * reference.Added inIndexsee so we know that we are still
					 * processing contentfrom a referenced indexterm.
					 */
					if (term.getTermPrefix() != null || inIndexsee) {
						indexSeeRefTerm = term.getTermName();
						inIndexsee = true;
						foundIndexsee = true;						

						// Find the term with an href.

						for (int j = 0; j < termCloneNum; j++) {
							IndexTerm termClone = (IndexTerm) termCloneList
									.get(j);

							if (term.getTermName().equals(
									termClone.getTermName())) {
								foundIndexTerm = true;

								if (termClone.getTargetList().size() > 0) {
									printWriter.print("<topic href=\"");
									printWriter
											.print(replaceExtName(((IndexTermTarget) termClone
													.getTargetList().get(0))
													.getTargetURI())); // Eric
									printWriter.print("\"");
									if (targetName.trim().length() > 0) {
										printWriter.print(" title=\"");
										printWriter
												.print(((IndexTermTarget) termClone
														.getTargetList().get(0))
														.getTargetName());
										printWriter.print("\"");
									}
									printWriter.print("/>");
									printWriter.print(System
											.getProperty("line.separator"));
								}
								/*
								 * We found the term we are looking for, but it
								 * does not have a target name (title). We need
								 * to take a look at the subterms for the
								 * redirect and
								 */
								termCloneList = termClone.getSubTerms();
								break;

							}

						}// end for
						// If there are no subterms, then we are done.
						if (term.getSubTerms().size() == 0)
							inIndexsee = false;

					} else {
						printWriter.print("<topic href=\"");
						printWriter.print(replaceExtName(targetUri)); // Eric
						printWriter.print("\"");
						if (targetName.trim().length() > 0) {
							printWriter.print(" title=\"");
							printWriter.print(target.getTargetName());
							printWriter.print("\"");
						}
						printWriter.print("/>");
						printWriter.print(System.getProperty("line.separator"));
					}

				}
			}//end for
			
			if (!foundIndexTerm && foundIndexsee && indexSeeRefTerm != null && !indexSeeRefTerm.equals("***")){
				Properties prop=new Properties();
				prop.put("%1", indexSeeRefTerm.trim());
				javaLogger.logWarn(MessageUtils.getMessage("DOTJ050W", prop).toString());
				
			}
		}

	}

	/*
	 * Specific method for new markup for see references in Eclipse. Depends on
	 * Eclipse 3.6.
	 * 
	 * @param term The indexterm to be processed.
	 * @param printWriter The Writer used for writing content to disk.
	 */

	private void outputIndexEntryEclipseIndexsee(IndexTerm term,
			PrintWriter printWriter) {
		List<IndexTermTarget> targets = term.getTargetList();
		int targetNum = targets.size();

		// Index-see and index-see-also terms should also generate links to its
		// target
		// Otherwise, the term won't be displayed in the index tab.
		if (targets != null && !targets.isEmpty()) {
			for (int i = 0; i < targetNum; i++) {
				IndexTermTarget target = targets.get(i);
				String targetUri = target.getTargetURI();
				String targetName = target.getTargetName();
				if (targetUri == null) {

					printWriter.print("<topic");
					printWriter.print(" title=\"");
					printWriter.print(target.getTargetName());
					printWriter.print("\"/>");
					printWriter.print(System.getProperty("line.separator"));
				}
//				
				else {
					printWriter.print("<topic href=\"");
					printWriter.print(replaceExtName(targetUri)); // Eric
					printWriter.print("\"");
					if (targetName.trim().length() > 0) {
						printWriter.print(" title=\"");
						printWriter.print(target.getTargetName());
						printWriter.print("\"");
					}
					printWriter.print("/>");
					printWriter.print(System.getProperty("line.separator"));
				}
			}
		}// end for

	}
	
	/*
	 * Clone a list used for comparison against the original list.
	 * 
	 * @param  List A list to be deep cloned 
	 * @return List The deep cloned list 
	 */
	
	private List<IndexTerm> cloneIndextermList (List<IndexTerm> termList){
		 List<IndexTerm> termListClone = new ArrayList<IndexTerm>(termList.size());
	        
	        
	     if (termList != null && !termList.isEmpty()){
		    for (int i = 0; i < termList.size(); i++) {
		     	termListClone.add(termList.get(i));
	         }
	     }
	    return termListClone;
	}
	
	/*
	 * Logic for adding various start index entry elements for Eclipse help.
	 * 
	 * @param term  The indexterm to be processed.
	 * @param printWriter The Writer used for writing content to disk.
	 * @param indexsee Boolean value for using the new markup for see references.
	 */
	private void outputIndexTermStartElement (IndexTerm term, PrintWriter printWriter, boolean indexsee){
		
				
		//RFE 2987769 Eclipse index-see
        if (indexsee){
        	if (term.getTermPrefix() != null){
        		inIndexsee = true;
	        	printWriter.print("<see keyword=\"");
	
	            printWriter.print(term.getTermName());
	            printWriter.print("\"");	
	           
	            if (term.getSubTerms() == null || term.getSubTerms().size() == 0){
	            	printWriter.print("/");
	            }
	            printWriter.print(">");
	            
	            printWriter.print(System.getProperty("line.separator"));
	            
	            
        	}
        	//subterm of an indexsee.
        	else if (term.getTermPrefix() == null && inIndexsee){
        		printWriter.print("<subpath keyword=\"");
        		
	            printWriter.print(term.getTermName());
                printWriter.print("\"/>");
	
        	}
        	else {
        		printWriter.print("<entry keyword=\"");
        		
	            printWriter.print(term.getTermName());
	            printWriter.print("\">");
	            printWriter.print(System.getProperty("line.separator"));
	            outputIndexEntryEclipseIndexsee(term, printWriter);

        	}
        	
        	
        }
        
        else {
        	printWriter.print("<entry keyword=\"");

            printWriter.print(term.getTermFullName());
            printWriter.print("\">");
            printWriter.print(System.getProperty("line.separator"));
        	outputIndexEntry(term, printWriter);
        	
        	
        }
	}
	
	/*
	 * Logic for adding various end index entry elements for Eclipse help.
	 * 
	 * @param term  The indexterm to be processed.
	 * @param printWriter The Writer used for writing content to disk.
	 * @param indexsee Boolean value for using the new markup for see references.
	 */
	private void outputIndexTermEndElement (IndexTerm term, PrintWriter printWriter, boolean indexsee){
		
		
        if (indexsee){
			if (term.getTermPrefix() != null){
	        	if (term.getSubTerms() != null || term.getSubTerms().size() > 0){
	        		printWriter.print("</see>");
	        		printWriter.print(System.getProperty("line.separator"));
	        	}
	            inIndexsee = false;
	        }
	        else if (term.getTermPrefix() == null && inIndexsee){
	        	printWriter.print(System.getProperty("line.separator"));
	        }
	        else {
	        	printWriter.print("</entry>");
	            printWriter.print(System.getProperty("line.separator"));
	        }
	    }
        else {
        	printWriter.print("</entry>");
            printWriter.print(System.getProperty("line.separator"));
        }
	}

}
