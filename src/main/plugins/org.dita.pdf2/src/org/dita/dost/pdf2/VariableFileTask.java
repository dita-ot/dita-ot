/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */
package org.dita.dost.pdf2;

import static javax.xml.XMLConstants.*;

import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.*;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.dita.dost.util.URLUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

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
        if (files.isEmpty()) {
            log("No variable files found", Project.MSG_DEBUG);
            return;
        }


        final File strings = getBaseVariableFile().getAbsoluteFile();

        OutputStream out = null;
        try {
            final Document d = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(strings);
            final Element root = d.getDocumentElement();

            final NodeList nl = root.getElementsByTagName("lang");
            for (int i = 0; i < nl.getLength(); i++) {
                final Element lang = (Element) nl.item(i);
                final Attr filename = lang.getAttributeNode("filename");
                final URI f = URLUtils.toURI(filename.getValue());
                if (!f.isAbsolute()) {
                    filename.setValue(strings.toURI().resolve(f).toString());
                }
            }

            for (final File f: files) {
                final Element lang = d.createElement("lang");
                final String n = f.getName();
                final int i = n.indexOf('.');
                lang.setAttributeNS(XML_NS_URI, "xml:lang", n.substring(0, i).replace('_', '-'));
                lang.setAttribute("filename", f.toURI().toString());
                root.appendChild(lang);
            }

            TransformerFactory.newInstance().newTransformer().transform(new DOMSource(d), new StreamResult(file));
        } catch (final Exception e) {
            throw new BuildException("Failed to write output file: " + e.getMessage(), e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (final IOException e) {
                    getProject().log("Failed to close output writer", Project.MSG_ERR);
                }
            }
        }
    }

    private File getBaseVariableFile() {
        final String f = getProject().getProperty("dita.plugin.org.dita.base.dir");
        if (f != null) {
            return new File(f, "xsl" + File.separator + "common" + File.separator + "strings.xml");
        }
        return null;
    }

    public void addFileset(FileSet fileset) {
        filesets.add(fileset);
    }
        
    public void setFile(final File file) {
        this.file = file;
    }
    
}
