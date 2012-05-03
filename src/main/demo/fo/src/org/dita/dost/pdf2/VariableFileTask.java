/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */
package org.dita.dost.pdf2;

import static javax.xml.XMLConstants.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

import org.dita.dost.util.XMLSerializer;

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
        
        XMLSerializer s = null;
        try {
            s = XMLSerializer.newInstance(new FileOutputStream(file));
            s.writeStartDocument();
            s.writeStartElement("langlist");
            for (final File f: files) {
                s.writeStartElement("var");
                final String n = f.getName();
                final int i = n.indexOf('.');
                s.writeAttribute(XML_NS_URI, "xml:lang", n.substring(0, i).replace('_', '-'));
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
                } catch (IOException e) {
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
