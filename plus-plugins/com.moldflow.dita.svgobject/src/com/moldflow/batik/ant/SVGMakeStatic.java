package com.moldflow.batik.ant;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.DirectoryScanner;
import java.util.Vector;
import java.util.Iterator;
import java.io.File;
import java.net.URI;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.FileReader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.tools.ant.types.Mapper;
import org.apache.tools.ant.util.FileNameMapper;
import org.apache.tools.ant.util.IdentityMapper;

import com.moldflow.batik.transcoder.SVGScriptExecutor;

public class SVGMakeStatic extends Task {
	private String toDir = null;
	private Vector filesets = new Vector(); 
	private Mapper mapper = null;
	
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
		
		for(Iterator fsi = filesets.iterator(); fsi.hasNext(); )
		{
			FileSet fs = (FileSet) fsi.next();
			DirectoryScanner ds = fs.getDirectoryScanner(getProject());
			String[] includedFiles = ds.getIncludedFiles();
			File filesetBaseDir = ds.getBasedir();
			for (int i = 0; i < includedFiles.length; i++)
			{
				String filename = includedFiles[i];
				File absoluteFile = new File(filesetBaseDir, filename);
				URI absoluteURI = absoluteFile.toURI();
				
				/*Reader in;
				try {
					in = new FileReader(absoluteFile);
				} catch (FileNotFoundException e) {
					throw new BuildException("File not found: " + absoluteFile.getName());
				}*/
				TranscoderInput transcoderInput = new TranscoderInput(absoluteURI.toString());
				
				File destinationFile = new File(toDir, m.mapFileName(filename)[0]);
				OutputStream ostream;
				try {
					ostream = new FileOutputStream(destinationFile);
					Writer out = new OutputStreamWriter(ostream, "UTF-8");
					TranscoderOutput transcoderOutput = new TranscoderOutput(out);

					SVGScriptExecutor t = new SVGScriptExecutor();
					t.transcode(transcoderInput, transcoderOutput);

					out.flush();
					out.close();
					log("Processed file " + absoluteFile.getName(), Project.MSG_INFO);
				} catch (FileNotFoundException e) {
					throw new BuildException("File not found: " + absoluteFile.getName());
				} catch (UnsupportedEncodingException e) {
					throw new BuildException("Unsupported encoding");
				} catch (TranscoderException e) {
					throw new BuildException("Error while transcoding: " + e.toString());
				} catch (IOException e) {
					throw new BuildException("Failed to close file " + absoluteFile.getName());
				}
			}
		}
	}
}
