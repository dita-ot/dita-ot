/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2005 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.writer;

import static javax.xml.transform.OutputKeys.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.xml.transform.Transformer;

import org.xml.sax.SAXException;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.index.IndexTerm;
import org.dita.dost.index.IndexTermTarget;
import org.dita.dost.util.XMLSerializer;

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
@SuppressWarnings("ForLoopReplaceableByForEach")
public final class HTMLIndexWriter extends AbstractExtendDitaWriter {

    @Override
    public void write(final File filename) throws DITAOTException {
        OutputStream out = null;
        try {
            out = new FileOutputStream(filename);
            final XMLSerializer serializer = XMLSerializer.newInstance(out);
            final Transformer transformer = serializer.getTransformerHandler().getTransformer();
            transformer.setOutputProperty(DOCTYPE_PUBLIC, "-//IETF//DTD HTML//EN");
            transformer.setOutputProperty(METHOD, "html");
            transformer.setOutputProperty(ENCODING, "UTF-8");

            serializer.writeStartDocument();
            serializer.writeStartElement("html");
            serializer.writeStartElement("head");
            serializer.writeComment("Sitemap 1.0");
            serializer.writeEndElement(); // head
            serializer.writeStartElement("body");
            serializer.writeStartElement("ul");
            // Initializing the variable for the alphabetical headings.
            String printLetter = "A";
            final int termNum = termList.size();
            for (int i = 0; i < termNum; i++) {
                final IndexTerm term = termList.get(i);
                //Add alphabetical headings:
                if (i == 0) {
                    printLetter = term.getTermFullName().substring(0, 1);
                    serializer.writeCharacters(printLetter);
                }
                final String firstLetter = term.getTermFullName().substring(0, 1);
                if (!firstLetter.equals(printLetter)) {
                    printLetter = firstLetter;
                    serializer.writeCharacters(printLetter);
                }
                //End alphabetical heading.
                outputIndexTerm(term, serializer);
            }
            serializer.writeEndElement(); // ul
            serializer.writeEndElement(); // body
            serializer.writeEndElement(); // html
            serializer.writeEndDocument();
        } catch (final Exception e) {
            throw new DITAOTException(e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (final IOException e) {
                    logger.error(e.getMessage(), e) ;
                }
            }
        }
    }

    /**
     * Output the given indexterm into the XML writer.
     *
     * @param term term to serializer
     * @param serializer XML output to write to
     */
    private void outputIndexTerm(final IndexTerm term, final XMLSerializer serializer) throws SAXException {
        List<IndexTermTarget> targets = term.getTargetList();
        final List<IndexTerm> subTerms = term.getSubTerms();

        serializer.writeStartElement("li");

        //if term doesn't have target to link to, it won't appear in the index tab
        //we need to create links for such terms
        if (targets.isEmpty()) {
            findTargets(term);
            targets = term.getTargetList();
        }

        if (targets.isEmpty()) {
            serializer.writeCharacters(term.getTermFullName());
        } else {
            final IndexTermTarget target = targets.get(0);
            serializer.writeStartElement("a");
            serializer.writeAttribute("href", target.getTargetURI());
            serializer.writeCharacters(term.getTermFullName());
            serializer.writeEndElement(); // a
        }
        if (subTerms.size() > 0) {
            serializer.writeStartElement("ul");
            for (final IndexTerm subTerm : subTerms) {
                outputIndexTerm(subTerm, serializer);
            }
            serializer.writeEndElement(); // ul
        }
        serializer.writeEndElement(); // li
    }

    /**
     * Find the targets in its subterms when the current term doesn't have any target
     *
     * @param term current IndexTerm instance
     */
    private void findTargets(final IndexTerm term) {
        final List<IndexTerm> subTerms = term.getSubTerms();
        List<IndexTermTarget> subTargets;
        if (subTerms != null && ! subTerms.isEmpty()) {
            for (final IndexTerm subTerm : subTerms) {
                subTargets = subTerm.getTargetList();
                if (subTargets != null && !subTargets.isEmpty()) {
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
    @Override
    public String getIndexFileName(final String outputFileRoot) {
        return outputFileRoot + ".hhk";
    }

}
