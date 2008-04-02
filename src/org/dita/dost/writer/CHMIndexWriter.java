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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.index.IndexTerm;
import org.dita.dost.index.IndexTermTarget;
import org.dita.dost.module.Content;
import org.dita.dost.util.Constants;

/**
 * This class extends AbstractWriter, used to output IndexTerm list to CHM index
 * file.
 * 
 * @version 1.0 2005-05-11
 * 
 * @author Wu, Zhi Qiang
 */
public class CHMIndexWriter implements AbstractWriter {
    /** List of indexterms */
    private List termList = null;

    /**
     * Default Constructor
     */
    public CHMIndexWriter() {
    }

    /** (non-Javadoc)
     * @see org.dita.dost.writer.AbstractWriter#setContent(org.dita.dost.module.Content)
     */
    public void setContent(Content content) {
        termList = (List) content.getCollection();
    }

    /**
     * Write the index term into given OutputStream.
     * 
     * @param outputStream
     */
    public void write(OutputStream outputStream) throws UnsupportedEncodingException{
        PrintWriter printWriter = null;
        int termNum = termList.size();

        try {
            printWriter = new PrintWriter(new OutputStreamWriter(outputStream, Constants.UTF8));

            printWriter.println("<!DOCTYPE HTML PUBLIC \"-//IETF//DTD HTML//EN\">");
            printWriter.println("<html>");
            printWriter.println("<head>");
            printWriter
                    .println("<meta name=\"GENERATOR\" content=\"Microsoft&reg; HTML Help Workshop 4.1\">");
            printWriter.println("<!-- Sitemap 1.0 -->");
            printWriter.println("</head>");
            printWriter.println("<body>");
            printWriter.println("<ul>");

            for (int i = 0; i < termNum; i++) {
                IndexTerm term = (IndexTerm) termList.get(i);
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

        printWriter.println("<li> <object type=\"text/sitemap\">");
        printWriter.print("<param name=\"Name\" value=\"");
        printWriter.print(term.getTermFullName());
        printWriter.print("\">");
        printWriter.println();
        
        //if term doesn't has target to link to, it won't appear in the index tab
        //we need to create links for such terms
        if (targets == null || targets.isEmpty()){
        	targets = new ArrayList(Constants.INT_1);
        	findTargets(term, targets);
        	targetNum = targets.size();
        }

        for (int i = 0; i < targetNum; i++) {
            IndexTermTarget target = (IndexTermTarget) targets.get(i);
            printWriter.print("<param name=\"Name\" value=\"");
            printWriter.print(target.getTargetName());
            printWriter.print("\">");
            printWriter.println();
            printWriter.print("<param name=\"Local\" value=\"");
            printWriter.print(target.getTargetURI());
            printWriter.print("\">");
            printWriter.println();
        }

        printWriter.println("</object>");

        if (subTerms != null && subTermNum > 0) {
            printWriter.println("<ul>");

            for (int i = 0; i < subTermNum; i++) {
                IndexTerm subTerm = (IndexTerm) subTerms.get(i);
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
	private void findTargets(IndexTerm term, List targets) {
		List subTerms = term.getSubTerms();
		List subTargets = null;
		if (subTerms != null && ! subTerms.isEmpty()){
			for (int i = 0; i < subTerms.size(); i++){
				IndexTerm subTerm = (IndexTerm) subTerms.get(i);
				subTargets = subTerm.getTargetList();
				if (subTargets != null && !subTargets.isEmpty()){
					targets.addAll(subTargets);
				}
				findTargets(subTerm, targets);
			}			
		}	
	}

}
