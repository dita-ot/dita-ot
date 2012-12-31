/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */
package org.dita.dost.pdf2;

import static javax.xml.XMLConstants.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

/**
 * Generate list of variable files.
 * 
 * @since 1.6
 * @author Jarno Elovirta
 */
public final class VariableFileTask extends Task {

    private List<FileSet> filesets = new ArrayList<FileSet>();
    private File file;
    
    @Override
    public void execute() throws BuildException {
        final List<File> files = new ArrayList<File>();
        for (final FileSet fs: filesets) {
            final DirectoryScanner ds = fs.getDirectoryScanner(getProject());
            for (final String f: ds.getIncludedFiles()) {
                files.add(new File(ds.getBasedir(), f));
            }
        }
        
        OutputStream out = null;
        XMLStreamWriter s = null;
        try {
        	out = new FileOutputStream(file);
            s = XMLOutputFactory.newInstance().createXMLStreamWriter(out);
            s.writeStartDocument();
            s.writeStartElement("langlist");
            for (final File f: files) {
                s.writeStartElement("var");
                final String n = f.getName();
                final int i = n.indexOf('.');
                s.writeAttribute("xml", XML_NS_URI, "lang", n.substring(0, i).replace('_', '-'));
                s.writeAttribute("filename", f.toURI().toString());
                s.writeEndElement();
            }
            s.writeEndDocument();
        } catch (final Exception e) {
            throw new BuildException("Failed to write output file: " + e.getMessage(), e);
        } finally {
            if (s != null) {
                try {
                    s.close();
                } catch (final XMLStreamException e) {
                    getProject().log("Failed to close output writer", Project.MSG_ERR);
				}
            }
            if (out != null) {
                try {
                    out.close();
                } catch (final IOException e) {
                    getProject().log("Failed to close output writer", Project.MSG_ERR);
                }
            }
        }
    }
    
    public void addFileset(FileSet fileset) {
        filesets.add(fileset);
    }
        
    public void setFile(final File file) {
        this.file = file;
    }
    
}
