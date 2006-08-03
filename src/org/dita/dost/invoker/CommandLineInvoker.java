/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.invoker;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.module.Content;
import org.dita.dost.module.ContentImpl;
import org.dita.dost.platform.Integrator;
import org.dita.dost.util.Constants;
import org.dita.dost.writer.PropertiesWriter;

/**
 * Class description goes here.
 * 
 * @version 1.0 2005-5-31
 * @author Zhang, Yuan Peng
 */

public class CommandLineInvoker {
	private static Map paramMap = null;
	private String propertyFile = null;
	private String antBuildFile = null;
	private String ditaDir = null;
	private boolean debugMode = false;

    /**
     * Whether or not this instance has successfully been
     * constructed and is ready to run.
     */
    private boolean readyToRun = false;
    
	DITAOTJavaLogger javaLogger = new DITAOTJavaLogger();
	
	static {
		paramMap = new HashMap();

		paramMap.put("/basedir", "basedir");
		paramMap.put("/ditadir", "dita.dir");
		paramMap.put("/i", "args.input");
		paramMap.put("/if", "dita.input");
		paramMap.put("/id", "dita.input.dirname");
		paramMap.put("/artlbl", "args.artlbl");
		paramMap.put("/draft", "args.draft");
		paramMap.put("/ftr", "args.ftr");
		paramMap.put("/hdr", "args.hdr");
		paramMap.put("/hdf", "args.hdf");
		paramMap.put("/csspath", "args.csspath");
		paramMap.put("/cssroot", "args.cssroot");
		paramMap.put("/css", "args.css");
		paramMap.put("/filter", "dita.input.valfile");
		paramMap.put("/ditaext", "dita.extname");
		paramMap.put("/outdir", "output.dir");
		paramMap.put("/transtype", "transtype");
		paramMap.put("/indexshow", "args.indexshow");
		paramMap.put("/outext", "args.outext");
		paramMap.put("/copycss", "args.copycss");
		paramMap.put("/xsl", "args.xsl");
		paramMap.put("/tempdir", "dita.temp.dir");
		paramMap.put("/cleantemp", "clean.temp");
		paramMap.put("/foimgext", "args.fo.img.ext");
		paramMap.put("/javahelptoc", "args.javahelp.toc");
		paramMap.put("/javahelpmap", "args.javahelp.map");
		paramMap.put("/eclipsehelptoc", "args.eclipsehelp.toc");
		paramMap.put("/eclipsecontenttoc", "args.eclipsecontent.toc");
		paramMap.put("/xhtmltoc", "args.xhtml.toc");
		paramMap.put("/logdir", "args.logdir");
		paramMap.put("/ditalocale", "args.dita.locale");
		paramMap.put("/fooutputrellinks", "args.fo.output.rel.links");
		paramMap.put("/version", "args.eclipse.version");
		paramMap.put("/provider", "args.eclipse.provider");
		paramMap.put("/fouserconfig", "args.fo.userconfig");
		paramMap.put("/htmlhelpincludefile", "args.htmlhelp.includefile");
	}

	/**
	 * Constructor: CommandLineInvoker
	 */
	public CommandLineInvoker() {
	}

	/**
	 * @return Returns the ditaDir.
	 */
	public String getDitaDir() {
		return ditaDir;
	}
	
	public boolean getReadyToRun() {
		return readyToRun;
	}
	
	/**
	 * Process input arguments.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public void processArguments(String[] args) throws DITAOTException {
		Properties prop = new Properties();
		PropertiesWriter propWriter = null;
		Content content = null;
		String baseDir = null;
		String tempDir = null;
		String inputDitaDir = null;
		
		/*
		 * validate dita.dir and init log message file
		 */
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if (arg.startsWith("/ditadir:")) {
				inputDitaDir = arg.substring(arg.indexOf(":") + 1);
			} 
		}
		ditaDir = new File(inputDitaDir, "").getAbsolutePath();
		antBuildFile = new File(ditaDir, "build.xml").getAbsolutePath();
		if (!new File(ditaDir, "build_template.xml").exists()) {
			throw new DITAOTException("Invalid dita-ot home directory '" + ditaDir
					+ "', please specify the correct dita-ot home directory using '/ditadir'.");
		}
		MessageUtils.loadMessages(new File(new File(ditaDir, "resource"),
				"messages.xml").getAbsolutePath());
		
		/*
		 * Process input arguments
		 */
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			String javaArg = null;
			String antArg = null;
			int colonPos = arg.indexOf(Constants.COLON);

			if (arg.equals("-help") || arg.equals("-h")) {
                printUsage();
                return;
            } else if (arg.equals("-version")) {
                printVersion();
                return;
            }
			
			if (arg.equals("/debug") || arg.equals("/d")) {
				debugMode = true;
				continue;
			}
			
			if (colonPos == -1) {
				String msg = null;
				Properties params = new Properties();

				params.put("%1", arg);
				msg = MessageUtils.getMessage("DOTJ001F", params).toString();
				
				throw new DITAOTException(msg);
			}

			javaArg = arg.substring(0, colonPos);
			antArg = (String) paramMap.get(javaArg.toLowerCase());

			if (antArg == null) {
				String msg = null;
				Properties params = new Properties();

				params.put("%1", javaArg);
				msg = MessageUtils.getMessage("DOTJ002F", params).toString();

				throw new DITAOTException(msg);
			}

			String antArgValue = arg.substring(colonPos + 1);

			if (antArgValue.trim().equals(Constants.STRING_EMPTY)) {
				String msg = null;
				Properties params = new Properties();

				params.put("%1", javaArg);
				msg = MessageUtils.getMessage("DOTJ003F", params).toString();

				throw new DITAOTException(msg);
			}

			prop.put(antArg, antArgValue);
		}
		
		/*
		 * Init base directory for transformation
		 */
		baseDir = new File(prop.getProperty("basedir", "")).getAbsolutePath();
		prop.put("basedir", baseDir);
		prop.put("dita.dir", ditaDir);
		
		/*
		 * Init temp directory
		 */
		tempDir = prop.getProperty("dita.temp.dir", Constants.TEMP_DIR_DEFAULT);
		File tempPath = new File(tempDir);
		if (!tempPath.isAbsolute()) {
			tempPath = new File(baseDir, tempDir);
		}
		if (!tempPath.exists() && tempPath.mkdirs() == false) {
			String msg = null;
			Properties params = new Properties();

			params.put("%1", tempPath.getAbsoluteFile());
			msg = MessageUtils.getMessage("DOTJ004F", params).toString();

			throw new DITAOTException(msg);
		}
		
		propertyFile = new File(tempPath, "property.temp").getAbsolutePath();

		/*
		 * Output input params into temp property file
		 */
		propWriter = new PropertiesWriter();
		content = new ContentImpl();
		content.setValue(prop);
		propWriter.setContent(content);
		propWriter.write(propertyFile);
		
		readyToRun = true;
	}

	/**
	 * Start ant process to execute the build process.
	 * 
	 * @throws IOException
	 */
	public void startAnt() throws IOException {
		StringBuffer cmdBuffer = new StringBuffer(Constants.INT_64);
		
		cmdBuffer.append(getCommandRunner());
		cmdBuffer.append(" -f ");
		cmdBuffer.append(antBuildFile);
		cmdBuffer.append(" -logger org.dita.dost.log.DITAOTBuildLogger");
		cmdBuffer.append(" -propertyfile ").append(propertyFile);
		
		if (debugMode) {
			cmdBuffer.append(" -d ");
		}

		startTransformation(cmdBuffer.toString());
	}
	
	private String getCommandRunner() {
		return (Constants.OS_NAME.toLowerCase().indexOf(
				Constants.OS_NAME_WINDOWS) != -1) ? "ant.bat" : "ant";
	}

	private void startTransformation(String cmd) throws IOException {
		BufferedReader reader;
		Process antProcess = Runtime.getRuntime().exec(cmd);

		/*
		 * Get output messages and print to console. 
		 * Note: Since these messages have been logged to the log file, 
		 * there is no need to output them to log file.
		 */
		reader = new BufferedReader(new InputStreamReader(antProcess
				.getInputStream()));
		for (String line = reader.readLine(); line != null; line = reader
				.readLine()) {
			System.out.println(line);
		}
		reader.close();
		
		reader = new BufferedReader(new InputStreamReader(antProcess
				.getErrorStream()));
		for (String line = reader.readLine(); line != null; line = reader
				.readLine()) {
			System.err.println(line);
		}
	}
	
	private static void printVersion() {
		System.out.println("DITA Open Toolkit 1.3");
	}
	
	/**
     * Prints the usage information for this class to <code>System.out</code>.
     */
    private static void printUsage() {
    	String lSep = System.getProperty("line.separator");
        StringBuffer msg = new StringBuffer();
        msg.append("java -jar lib/dost.jar [mandatory parameters] [options]" + lSep);
        msg.append("Mandatory parameters:" + lSep);
        msg.append("  /i:{args.input}        specify the input file" + lSep);
        msg.append("  /transtype:{transtype} specify the transformation type" + lSep);
        msg.append("Options: " + lSep);
        msg.append("  -help, -h              print this message" + lSep);
        msg.append("  -version               print the version information and exit" + lSep);
        msg.append("  /basedir:{basedir}     specify the working directory" + lSep);
        msg.append("  /ditadir:{dita.dir}    specify the toolkit's home directory" + lSep);
        msg.append("  /outdir:{output.dir}   specify the output directory" + lSep);
        msg.append("  /tempdir:{dita.temp.dir} specify the temporary directory" + lSep);
        msg.append("  /logdir:{args.logdir}  specify the log directory" + lSep);
        msg.append("  /ditaext:{dita.extname} specify the dita file extension" + lSep);
        msg.append("  /filter:{dita.input.valfile} specify the filter file" + lSep);
        msg.append("  /draft:{args.draft}    specify whether to output draft info" + lSep);
        msg.append("  /artlbl:{args.artlbl}  specify whether to output artwork filenames" + lSep);
        msg.append("  /ftr:{args.ftr}        specify the file to be placed in the BODY running-footing area" + lSep);
        msg.append("  /hdr:{args.hdr}        specify the file to be placed in the BODY running-heading area" + lSep);
        msg.append("  /hdf:{args.hdf}        specify the file to be placed in the HEAD area" + lSep);
        msg.append("  /csspath:{args.csspath} specify the path for css reference" + lSep);
        msg.append("  /css:{args.css}        specify user css file" + lSep);
        msg.append("  /cssroot:{args.cssroot} specify the root directory for user specified css file" + lSep);
        msg.append("  /copycss:{args.copycss} specify whether to copy user specified css files" + lSep);
        msg.append("  /indexshow:{args.indexshow} specify whether each index entry should display within the body of the text itself" + lSep);
        msg.append("  /outext:{args.outext}  specify the output file extension for generated xhtml files" + lSep);
        msg.append("  /xsl:{args.xsl}  	     specify the xsl file used to replace the default xsl file" + lSep);
        msg.append("  /cleantemp:{clean.temp} specify whether to clean the temp directory before each build" + lSep);
        msg.append("  /foimgext:{args.fo.img.ext} specify the extension of image file in pdf transformation" + lSep);
        msg.append("  /javahelptoc:{args.javahelp.toc} specify the root file name of the output javahelp toc file in javahelp transformation" + lSep);
        msg.append("  /javahelpmap:{args.javahelp.map} specify the root file name of the output javahelp map file in javahelp transformation" + lSep);
        msg.append("  /eclipsehelptoc:{args.eclipsehelp.toc} specify the root file name of the output eclipsehelp toc file in eclipsehelp transformation" + lSep);
        msg.append("  /eclipsecontenttoc:{args.eclipsecontent.toc} specify the root file name of the output Eclipse content provider toc file in eclipsecontent transformation" + lSep);
        msg.append("  /provider:{args.eclipse.provider} specify the provider name of the eclipse help output" + lSep);
        msg.append("  /version:{args.eclipse.version} specify the version number of the eclipse help output" + lSep);
        msg.append("  /xhtmltoc:{args.xhtml.toc} specify the root file name of the output xhtml toc file in xhtml transformation" + lSep);
        msg.append("  /ditalocale:{args.dita.locale} specify the locale used for sorting indexterms." + lSep);
        msg.append("  /fooutputrellinks:{args.fo.output.rel.links} specify whether to output related links in pdf transformation" + lSep);
        msg.append("  /fouserconfig:{args.fo.userconfig} specify the user configuration file for FOP" + lSep);
        msg.append("  /htmlhelpincludefile:{args.htmlhelp.includefile} specify the file that need to be included by the HTMLHelp output" + lSep);
        System.out.println(msg.toString());
    }
    
	/**
	 * Get the input parameter and map them into parameters which can be
	 * accepted by Ant build task. Then start the building process
	 * 
	 * @param args
	 * 
	 */
	public static void main(String[] args) {
		CommandLineInvoker invoker = new CommandLineInvoker();
		Integrator integrator = new Integrator();
		
		try {
			invoker.processArguments(args);
			if (invoker.getReadyToRun()) {
				integrator.setDitaDir(invoker.getDitaDir());
				integrator.execute();
				invoker.startAnt();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
