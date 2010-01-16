/* Version 0.1 Deborah Pickett 20060531 */
/* Version 0.2 Deborah Pickett 20070308 uses Jeuclid 2.9.4 */
/* Version 0.3 Deborah Pickett 20070806 uses JEuclid 2.9.8 and Batik 1.7 */

package com.moldflow.jeuclid.ant;

import java.awt.Dimension;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import net.sourceforge.jeuclid.MathBase;
import net.sourceforge.jeuclid.MathMLParserSupport;
import net.sourceforge.jeuclid.ParameterKey;

import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Mapper;
import org.apache.tools.ant.util.FileNameMapper;
import org.apache.tools.ant.util.IdentityMapper;
import org.w3c.dom.DOMImplementation;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class MathML2SVG extends Task {

	private String toDir = null;
	private Vector<FileSet> filesets = new Vector<FileSet>(); 
	private Mapper mapper = null;
        private String baselineExtension = null;

	public void setToDir(String s)
	{
		toDir = s;
	}

	public void addFileSet(FileSet fs)
	{
		filesets.add(fs);
	}

	public void add(FileNameMapper m)
	{
		if (mapper != null)
		{
			throw new BuildException(new String("maximum of one mapper element allowed"));
		}
		mapper = new Mapper(getProject());
		mapper.add(m);
	}

        public void setBaselineExtension(String s)
        {
          baselineExtension = s;
        }

	public void execute() throws BuildException
	{
		if (toDir == null)
		{
			throw new BuildException(new String("required attribute toDir not specified"));
		}
		if (filesets == null)
		{
			throw new BuildException(new String("required child element fileset not specified"));
		}

		FileNameMapper m;
		if (mapper == null)
		{
			m = new IdentityMapper();
		}
		else
		{
			m = mapper.getImplementation();
		}

		for(Iterator<FileSet> fsi = filesets.iterator(); fsi.hasNext(); )
		{
			FileSet fs = fsi.next();
			DirectoryScanner ds = fs.getDirectoryScanner(getProject());
			String[] includedFiles = ds.getIncludedFiles();
			File filesetBaseDir = ds.getBasedir();
			for (int i = 0; i < includedFiles.length; i++)
			{
				String filename = includedFiles[i];
				File absoluteFile = new File(filesetBaseDir, filename);
				File destinationFile = new File(toDir, m.mapFileName(filename)[0]);
				MathBase base = null;
                                float baseline = 0;
				SVGGraphics2D canvas = null;

				Document inputDocument;
				try {
					inputDocument = MathMLParserSupport.parseFile(absoluteFile);
					Map<ParameterKey, String> params = MathBase.getDefaultParameters();
					params.put(ParameterKey.FontSize, ((Integer) m_size).toString());
					params.put(ParameterKey.OutFileType, "image/svg+xml");
					// Font f = new Font(m_font, Font.PLAIN, m_size);
					base = MathMLParserSupport.createMathBaseFromDocument(inputDocument, params);

					DOMImplementation domImpl = SVGDOMImplementation.getDOMImplementation();
					Document outputDocument = domImpl.createDocument(null, "svg", null);
					canvas = new SVGGraphics2D(outputDocument);

                                        baseline = base.getDescender(canvas);
					float width = base.getWidth(canvas);
					float height = base.getHeight(canvas);
					Dimension d = new Dimension();
					d.setSize(width, height);
					canvas.setSVGCanvasSize(d);

					base.paint(canvas);

				} catch (SAXException e) {
					log("Error reading file " + absoluteFile.getName(), Project.MSG_WARN);
				} catch (IOException e) {
					log("Error reading file " + absoluteFile.getName(), Project.MSG_WARN);
				}

				// Write out the result.
				try {
					Writer out = new OutputStreamWriter(new FileOutputStream(destinationFile), "UTF-8");
					canvas.stream(out, false);   // false = don't use CSS.
					out.flush();	
					out.close();
					log("Processed file " + absoluteFile.getName(), Project.MSG_INFO);
				} catch (IOException se) {
					log("Cannot write to output file: " + se.getMessage());
					throw new BuildException();
				}
                                if (baselineExtension != null)
                                {
				  File baselineFile = new File(toDir, m.mapFileName(filename)[0] + baselineExtension);
				  try {
				  	Writer out = new OutputStreamWriter(new FileOutputStream(baselineFile), "UTF-8");
                                        out.write("<baseline>");
                                        out.write(Float.toString(baseline));
                                        out.write("</baseline>\n");
					out.flush();	
					out.close();
				  } catch (IOException se) {
					log("Cannot write to output file: " + se.getMessage());
					throw new BuildException();
				  }
                                }
			}
		}
	}

//	private String m_font = "Arial Unicode MS";
private int m_size = 12;

	/*
public void setFont(String font)
{
	m_font = font;
}
	 */

public void setSize(int size)
{
	m_size = size;
}

};
