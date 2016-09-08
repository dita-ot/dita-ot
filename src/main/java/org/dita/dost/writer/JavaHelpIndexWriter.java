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
 * This class extends AbstractWriter, used to output index term
 * into java help index file.
 * 
 * @version 1.0 2005-05-20
 * 
 * @author Wu, Zhi Qiang
 */
public final class JavaHelpIndexWriter extends AbstractExtendDitaWriter {

    @Override
    public void write(final File filename) throws DITAOTException {
        OutputStream out = null;
        try {
            out = new FileOutputStream(filename);
            final XMLSerializer serializer = XMLSerializer.newInstance(out);
            final Transformer transformer = serializer.getTransformerHandler().getTransformer();
            transformer.setOutputProperty(DOCTYPE_PUBLIC , "-//Sun Microsystems Inc.//DTD JavaHelp Index Version 1.0//EN");
            transformer.setOutputProperty(DOCTYPE_SYSTEM, "http://java.sun.com/products/javahelp/index_1_0.dtd");
            serializer.writeStartDocument();
            serializer.writeStartElement("index");
            serializer.writeAttribute("version", "1.0");
            for (final IndexTerm term : termList) {
                outputIndexTerm(term, serializer);
            }
            serializer.writeEndElement(); // index
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
     * @param term term to output
     * @param serializer XML output to write to
     * @throws SAXException if serializing failed
     */
    private void outputIndexTerm(final IndexTerm term, final XMLSerializer serializer) throws SAXException {
        final List<IndexTermTarget> targets = term.getTargetList();
        final List<IndexTerm> subTerms = term.getSubTerms();
        final int targetNum = (targets == null) ? 0: targets.size();
        final int subTermNum = (subTerms == null) ? 0 : subTerms.size();

        // Don't set 'target' attribute for group purpose index item.
        if (subTermNum > 0) {
            serializer.writeStartElement("indexitem");
            serializer.writeAttribute("text", term.getTermFullName());
            for (int i = 0; i < subTermNum; i++) {
                final IndexTerm subTerm = subTerms.get(i);
                outputIndexTerm(subTerm, serializer);
            }
            serializer.writeEndElement(); // indexitem
        } else {
            for (int i = 0; i < targetNum; i++) {
                final IndexTermTarget target = targets.get(i);
                String targetURL = target.getTargetURI();
                targetURL = targetURL.substring(0, targetURL.lastIndexOf(".")).replace('\\', '_').replace('/', '_').replace('.', '_');

                serializer.writeStartElement("indexitem");
                serializer.writeAttribute("text", term.getTermFullName());
                serializer.writeAttribute("target", targetURL);
                serializer.writeEndElement();
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
        return outputFileRoot + "_index.xml";
    }

}
