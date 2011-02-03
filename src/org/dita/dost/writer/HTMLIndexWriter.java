/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2005 All Rights Reserved.
 */
package org.dita.dost.writer;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.index.IndexTerm;
import org.dita.dost.index.IndexTermTarget;
import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.module.Content;
import org.dita.dost.util.Constants;

/**
 * This class extends AbstractWriter, used to output IndexTerm list to CHM index
 * file.
 * 
 * @version 1.0 2005-05-11
 * 
 * @author Anders Svensson (based on modifications of the CHMIndexWriter class by Wu, Zhi Qiang)
 * The class outputs an index for html, including alphabetical headings. (Thanks to pre-existing functionality in DITA OT, 
 * these will be sorted based on locale as long as the xml:lang attribute is used.)
 *
 */
public class HTMLIndexWriter extends AbstractExtendDitaWriter implements AbstractWriter, IDitaTranstypeIndexWriter {
    /** List of indexterms */
    private List<IndexTerm> termList = null;
    private final DITAOTLogger logger = new DITAOTJavaLogger();

    /**
     * Default Constructor.
     */
    public HTMLIndexWriter() {
    }

    /**
     * @see org.dita.dost.writer.AbstractWriter#setContent(org.dita.dost.module.Content)
     */
    public void setContent(Content content) {
        termList = (List<IndexTerm>) content.getCollection();
    }

    /**
     * Write the index term into given OutputStream.
     * 
     * @param outputStream outputStream
     * @throws UnsupportedEncodingException encoding not supported exception
     */
    public void write(OutputStream outputStream) throws UnsupportedEncodingException{
        PrintWriter printWriter = null;
        int termNum = termList.size();

        try {
            printWriter = new PrintWriter(new OutputStreamWriter(outputStream, Constants.UTF8));

            printWriter.println("<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML//EN\">");
            printWriter.println("<html>");
            printWriter.println("<head>");
            printWriter.println("<!-- Sitemap 1.0 -->");			
			printWriter.println("</head>");
            printWriter.println("<body>");
            printWriter.println("<ul>");
			String printLetter = "A"; //Initializing the variable for the alphabetical headings.
            for (int i = 0; i < termNum; i++) {
                IndexTerm term = termList.get(i);
				
				//Add alphabetical headings:
				if (i == 0)
				{
					printLetter = term.getTermFullName().substring(0, 1);
					printWriter.println(printLetter);
				}
				String firstLetter = term.getTermFullName().substring(0, 1);
				if (!firstLetter.equals(printLetter))
				{
					printLetter = firstLetter;
					printWriter.println(printLetter);
				}
				//End alphabetical heading.
                outputIndexTerm(term, printWriter);
            }

            printWriter.println("</ul>");
            printWriter.println("</body>");
            printWriter.println("</html>");
        } finally {
            if (printWriter != null) {
                printWriter.close();
            }
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
			throw new DITAOTException(e);
		} finally {
			if (out != null) {
				try {
	                out.close();
                } catch (IOException e) {
                	logger.logException(e);
                }
			}
		}
	}

	/**
     * Output the given indexterm into the PrintWriter.  
     * 
     * @param term
     * @param printWriter
     */
    private void outputIndexTerm(IndexTerm term, PrintWriter printWriter) {
        List<IndexTermTarget> targets = term.getTargetList();
        List<IndexTerm> subTerms = term.getSubTerms();
        int targetNum = targets.size();
        int subTermNum = subTerms.size();

        printWriter.println("<li>");
        printWriter.println();
        
        //if term doesn't have target to link to, it won't appear in the index tab
        //we need to create links for such terms
        if (targets == null || targets.isEmpty()){
        	findTargets(term);
        	targets = term.getTargetList();
        	targetNum = targets.size();
        }

		if(targetNum > 1) {
		printWriter.print(term.getTermFullName());
		printWriter.println();
		}
		else
		{
            IndexTermTarget target = targets.get(0);
            printWriter.print("<a href=\"");
			printWriter.print(target.getTargetURI());
            printWriter.print("\">");
			printWriter.print(term.getTermFullName());
            printWriter.print("</a>");
            printWriter.println();
		}


        if (subTerms != null && subTermNum > 0) {
            printWriter.println("<ul>");

            for (int i = 0; i < subTermNum; i++) {
                IndexTerm subTerm = subTerms.get(i);
                outputIndexTerm(subTerm, printWriter);
            }

            printWriter.println("</ul>");
        }

        printWriter.println("</li>");
    }

    /**
     * find the targets in its subterms when the current term doesn't have any target
     * 
     * @param term
     * The current IndexTerm instance
     * 
     * @param targets
     * The list of targets to store the result found
     */
	private void findTargets(IndexTerm term) {
		List<IndexTerm> subTerms = term.getSubTerms();
		List<IndexTermTarget> subTargets = null;
		if (subTerms != null && ! subTerms.isEmpty()){
			for (int i = 0; i < subTerms.size(); i++){
				IndexTerm subTerm = subTerms.get(i);
				subTargets = subTerm.getTargetList();
				if (subTargets != null && !subTargets.isEmpty()){
					findTargets(subTerm);
				}
				term.addTargets(subTerm.getTargetList());
			}			
		}	
	}

	/**
	 * Get index file name.
	 * @param outputFileRoot root
	 * @return index file name
	 */
	public String getIndexFileName(String outputFileRoot) {
		StringBuffer indexFilename;
		
		indexFilename = new StringBuffer(outputFileRoot);
		indexFilename.append(".hhk");
		// TODO Auto-generated method stub
		return indexFilename.toString();
	}

}
