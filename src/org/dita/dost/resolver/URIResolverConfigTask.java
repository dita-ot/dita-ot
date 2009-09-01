package org.dita.dost.resolver;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * 
 * @author Alan
 */
public class URIResolverConfigTask extends Task {
	private String basedir = null;
	private String tempdir = null;

	public URIResolverConfigTask() {
		// nop
	}

	@Override
	public void execute() throws BuildException {
		
		String path=tempdir;
		if(!new File(tempdir).isAbsolute()){
			path=new File(basedir,tempdir).getAbsolutePath();
		}
		DitaURIResolverFactory.setPath(path);
		// If you wants to replace the default resolver
		// DitaURIResolverFactory.setURIResolver(/*? extends URIResolver*/
		// resolver);
	}

	public String getBasedir() {
		return basedir;
	}

	public void setBasedir(String basedir) {
		this.basedir = basedir;
	}

	public String getTempdir() {
		return tempdir;
	}

	public void setTempdir(String tempdir) {
		this.tempdir = tempdir;
	}
	
}
