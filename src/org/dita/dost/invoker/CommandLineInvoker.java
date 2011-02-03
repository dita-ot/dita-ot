/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.invoker;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.module.Content;
import org.dita.dost.module.ContentImpl;
import org.dita.dost.platform.Integrator;
import org.dita.dost.util.Constants;
import org.dita.dost.util.Version;
import org.dita.dost.writer.PropertiesWriter;

/**
 * Class description goes here.
 * 
 * @version 1.0 2005-5-31
 * @author Zhang, Yuan Peng
 */

public class CommandLineInvoker {
	/** Map to store input parameters.*/
	private static Map<String, String> paramMap = null;
    /**logger.*/
	private static DITAOTJavaLogger javaLogger = new DITAOTJavaLogger();
	
	static {
		paramMap = new HashMap<String,String>();

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
		//Added by William on 2010-06-21 for bug:3012392 start
		paramMap.put("/xslpdf", "args.xsl.pdf");
		//Added by William on 2010-06-21 for bug:3012392 end 
		paramMap.put("/tempdir", "dita.temp.dir");
		paramMap.put("/cleantemp", "clean.temp");
		paramMap.put("/foimgext", "args.fo.img.ext");
		paramMap.put("/javahelptoc", "args.javahelp.toc");
		paramMap.put("/javahelpmap", "args.javahelp.map");
		paramMap.put("/eclipsehelptoc", "args.eclipsehelp.toc");
		paramMap.put("/eclipsecontenttoc", "args.eclipsecontent.toc");
		paramMap.put("/xhtmltoc", "args.xhtml.toc");
		paramMap.put("/xhtmlclass", "args.xhtml.classattr");
		paramMap.put("/usetasklabels", "args.gen.task.lbl");
		paramMap.put("/logdir", "args.logdir");
		paramMap.put("/ditalocale", "args.dita.locale");
		paramMap.put("/fooutputrellinks", "args.fo.output.rel.links");
		paramMap.put("/foincluderellinks", "args.fo.include.rellinks");
		paramMap.put("/odtincluderellinks", "args.odt.include.rellinks");
		paramMap.put("/retaintopicfo", "retain.topic.fo");
		paramMap.put("/version", "args.eclipse.version");
		paramMap.put("/provider", "args.eclipse.provider");
		paramMap.put("/fouserconfig", "args.fo.userconfig");
		paramMap.put("/htmlhelpincludefile", "args.htmlhelp.includefile");
		paramMap.put("/validate", "validate");
		paramMap.put("/outercontrol", "outer.control");
		paramMap.put("/generateouter", "generate.copy.outer");
		paramMap.put("/onlytopicinmap", "onlytopic.in.map");
		paramMap.put("/debug", "args.debug");
		//added on 20100824 to disable grammar pool caching start
		paramMap.put("/grammarcache", "args.grammar.cache");
		//added on 20100824 to disable grammar pool caching end
		paramMap.put("/odtimgembed", "args.odt.img.embed");
		
		
	}
	/**propertyFile store input params.*/
	private String propertyFile = null;
	/**antBuildFile run the ant.*/
	private String antBuildFile = null;
	/**ditaDir.*/
	private String ditaDir = null;
	/**debugMode.*/
	private boolean debugMode = false;

	/**
	 * Whether or not this instance has successfully been
	 * constructed and is ready to run.
	 */
	private boolean readyToRun = false;

	/**
	 * Constructor: CommandLineInvoker.
	 */
	public CommandLineInvoker() {
	}

	/**
	 * Getter function of ditaDir.
	 * @return Returns the ditaDir.
	 */
	public String getDitaDir() {
		return ditaDir;
	}
	/**
	 * Getter function for readytorun.
	 * @return if ready to run
	 */
	public boolean getReadyToRun() {
		return readyToRun;
	}
	
	/**
	 * Process input arguments.
	 * 
	 * @param args args
	 * @throws DITAOTException Exception
	 */
	public void processArguments(String[] args) throws DITAOTException {
		Properties prop = new Properties();
		PropertiesWriter propWriter = null;
		Content content = null;
		String baseDir = null;
		String tempDir = null;
		String inputDitaDir = null;
		File tempPath = null;
		
		if(args.length == 0){
			printUsage();
			readyToRun = false;
			return;
		}
		
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
			String antArgValue = null;
			int colonPos = arg.indexOf(Constants.COLON);

			if ("help".equals(arg) || "-h".equals(arg)) {
                printUsage();
                return;
            } else if ("-version".equals(arg)) {
                printVersion();
                return;
            }
			
			if ("/debug".equals(arg) || "/d".equals(arg)) {
				debugMode = true;
				continue;
			}
			
			if (colonPos == -1) {
				String msg = null;
				Properties params = new Properties();
				
				printUsage();

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
				
				printUsage();

				throw new DITAOTException(msg);
			}

			antArgValue = arg.substring(colonPos + 1);

			if (Constants.STRING_EMPTY.equals(antArgValue.trim())) {
				String msg = null;
				Properties params = new Properties();

				params.put("%1", javaArg);
				msg = MessageUtils.getMessage("DOTJ003F", params).toString();
				
				printUsage();

				throw new DITAOTException(msg);
			}
			
			//Added by William on 2009-11-09 for bug:2893493 start
			if(antArg.equals("clean.temp")&&
				!("yes".equalsIgnoreCase(antArgValue)||
				"no".equalsIgnoreCase(antArgValue))){
				antArgValue = "yes";
				
			}
			//Added by William on 2009-11-09 for bug:2893493 end

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
		 * 
		 */
		if (prop.containsKey("dita.temp.dir")) {
			tempDir = prop.getProperty("dita.temp.dir");
		} else {
			java.text.DateFormat format = new java.text.SimpleDateFormat("yyyyMMddHHmmssSSS");
			String timestamp = format.format(new java.util.Date());
			prop.setProperty("dita.temp.dir", Constants.TEMP_DIR_DEFAULT + Constants.FILE_SEPARATOR
					+ "temp" + timestamp);
			tempDir = prop.getProperty("dita.temp.dir");
		}
		//tempDir = prop.getProperty("dita.temp.dir", Constants.TEMP_DIR_DEFAULT);
		tempPath = new File(tempDir);
		if (!tempPath.isAbsolute()) {
			tempPath = new File(baseDir, tempDir);
		}
		if (!(tempPath.exists() || tempPath.mkdirs())) {
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
	 * @throws IOException IOException
	 */
	public void startAnt() throws IOException {
		List<String> cmd = new ArrayList<String>(Constants.INT_8);
		String[] cmds;
		
		cmd.add(getCommandRunner());
		cmd.add("-f");
		cmd.add(antBuildFile);
		cmd.add("-logger");
		cmd.add("org.dita.dost.log.DITAOTBuildLogger");
		cmd.add("-propertyfile");
		cmd.add(propertyFile);

		if (debugMode){
			cmd.add("-d");
		}
		
		cmds = new String[cmd.size()];
		cmd.toArray(cmds);
		
		startTransformation(cmds);
	}
	/**
	 * 
	 * @return String
	 */
	private static String getCommandRunner() {
		return (Constants.OS_NAME.toLowerCase().indexOf(Constants.OS_NAME_WINDOWS) != -1)
				? "ant.bat" 
				: "ant";
	}
	/**
	 * begin transformation.
	 * @param cmd cmd
	 * @throws IOException exception
	 */
	private static void startTransformation(String[] cmd) throws IOException {
		BufferedReader reader = null;
		Process antProcess = Runtime.getRuntime().exec(cmd);
		try{
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
		}finally{
			reader.close();
		}
		
		reader = null;
		try {
    		reader = new BufferedReader(new InputStreamReader(antProcess
    				.getErrorStream()));
    		for (String line = reader.readLine(); line != null; line = reader
    				.readLine()) {
    			System.err.println(line);
    		}
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					javaLogger.logException(e);
				}
			}
		}
	}
	/**
	 * print dita version.
	 */
	private static void printVersion() {
		//System.out.println("DITA Open Toolkit 1.5");
		System.out.println(Version.getVersion());
	}
	
	/**
     * Prints the usage information for this class to <code>System.out</code>.
     */
    private static void printUsage() {
    	String lSep = System.getProperty("line.separator");
        StringBuffer msg = new StringBuffer();
        msg.append("java -jar lib/dost.jar [mandatory parameters] [options]" + lSep);
        msg.append("Mandatory parameters:" + lSep);
        msg.append("  /i:                    specify path and name of the input file" + lSep);
        msg.append("  /transtype:            specify the transformation type" + lSep);
        msg.append("Options: " + lSep);
        msg.append("  -help, -h              print this message" + lSep);
        msg.append("  -version               print the version information and exit" + lSep);
        msg.append("  /basedir:              specify the working directory" + lSep);
        msg.append("  /ditadir:              specify the toolkit's home directory. Default is \"temp\"" + lSep);
        msg.append("  /outdir:               specify the output directory" + lSep);
        msg.append("  /tempdir:              specify the temporary directory" + lSep);
        msg.append("  /logdir:               specify the log directory" + lSep);
        msg.append("  /ditaext:              specify the file extension name to be used in the temp directory. Default is \".xml\"" + lSep);
        msg.append("  /filter:               specify the name of the file that contains the filter/flaggin/revision information" + lSep);
        msg.append("  /draft:                specify whether to output draft info. Valid values are \"no\" and \"yes\". Default is \"no\" (hide them)." + lSep);
        msg.append("  /artlbl:               specify whether to output artwork filenames. Valid values are \"no\" and \"yes\"" + lSep);
        msg.append("  /ftr:                  specify the file to be placed in the BODY running-footing area" + lSep);
        msg.append("  /hdr:                  specify the file to be placed in the BODY running-heading area" + lSep);
        msg.append("  /hdf:                  specify the file to be placed in the HEAD area" + lSep);
        msg.append("  /csspath:              specify the path for css reference" + lSep);
        msg.append("  /css:                  specify user css file" + lSep);
        msg.append("  /cssroot:              specify the root directory for user specified css file" + lSep);
        msg.append("  /copycss:              specify whether to copy user specified css files. Valid values are \"no\" and \"yes\"" + lSep);
        msg.append("  /indexshow:            specify whether each index entry should display within the body of the text itself. Valid values are \"no\" and \"yes\"" + lSep);
        msg.append("  /outext:               specify the output file extension for generated xhtml files. Default is \".html\"" + lSep);
        msg.append("  /xsl:            	     specify the xsl file used to replace the default xsl file" + lSep);
        msg.append("  /xslpdf:            	 specify the xsl file used to replace the default xsl file when transforming pdf" + lSep);
        msg.append("  /cleantemp:            specify whether to clean the temp directory before each build. Valid values are \"no\" and \"yes\". Default is \"yes\"" + lSep);
        msg.append("  /foimgext:             specify the extension of image file in legacy pdf transformation. Default is \".jpg\"" + lSep);
        msg.append("  /fooutputrellinks      For legacy PDF transform: determine if links are included in the PDF. Values are \"no\" and \"yes\". Default is \"no\"." + lSep);
        msg.append("  /foincluderellinks     For default PDF transform: determine which links are included in the PDF. Values are \"none\", \"all\", and \"nofamily\". Default is \"none\"." + lSep);
        msg.append("  /odtincluderellinks    For default ODT transform: determine which links are included in the ODT. Values are \"none\", \"all\", and \"nofamily\". Default is \"none\"." + lSep);
        msg.append("  /retaintopicfo         specify that topic.fo file should be preserved in the output directory. Specify any value, such as \"yes\", to preserve the file." + lSep);
        msg.append("  /javahelptoc:          specify the root file name of the output javahelp toc file in javahelp transformation. Default is the name of the input ditamap file" + lSep);
        msg.append("  /javahelpmap:          specify the root file name of the output javahelp map file in javahelp transformation. Default is the name of the input ditamap file" + lSep);
        msg.append("  /eclipsehelptoc:       specify the root file name of the output eclipsehelp toc file in eclipsehelp transformation. Default is the name of the input ditamap file" + lSep);
        msg.append("  /eclipsecontenttoc:    specify the root file name of the output Eclipse content provider toc file in eclipsecontent transformation. Default is the name of the input ditamap file" + lSep);
        msg.append("  /xhtmltoc:             specify the root file name of the output xhtml toc file in xhtml transformation" + lSep);
        msg.append("  /xhtmlclass:           specify whether DITA element names and ancestry are included in XHTML class attributes. Only \"yes\" and \"no\" are valid values. The default is yes. " + lSep);
        msg.append("  /usetasklabels:        specify whether DITA Task sections should get headings. Only \"YES\" and \"NO\" are valid values. The default is NO. " + lSep);
        msg.append("  /validate:             specify whether the ditamap/dita/xml files to be validated" + lSep);
        msg.append("  /outercontrol:         specify how to respond to the overflowing dita/topic files. Only \"fail\", \"warn\" and \"quiet\" are valid values. The default is warn. " + lSep);
        msg.append("  /generateouter:        specify how to deal with the overflowing dita/topic files. Only \"1\", \"2\" and \"3\" are valid values. The default is 1. Option 1: Only generate/copy files that fit within the designated output directory. Option 2: Generate/copy all files, even those that will end up outside of the output directory. Option 3: the old solution,adjust the input.dir according to the referenced files" +
        									 "(It is the most secure way to avoid broken links). (not default option any more but keep this as the option of backward compatibility)." + lSep);
		msg.append("  /onlytopicinmap:       specify whether make dita processor only resolve dita/topic files which are referenced by primary ditamap files Only \"true\" and \"false\" are valid values. The default is false. " + lSep);
		msg.append("  /debug:                specify whether extra debug information should be included in the log. Only \"yes\" and \"no\" are valid values. The default is no. " + lSep);
		msg.append("  /grammarcache:            specify whether grammar pool caching is used when parsing dita files. Only \"yes\" and \"no\" are valid values. The default is yes. " + lSep);
		msg.append("  /odtimgembed:            specify whether embedding images as binary data in odt transform. Only \"yes\" and \"no\" are valid values. The default is yes. " + lSep);
        System.out.println(msg.toString());
    }
    
	/**
	 * Get the input parameter and map them into parameters which can be
	 * accepted by Ant build task. Then start the building process
	 * 
	 * @param args args
	 * 
	 */
	public static void main(String[] args) {
		CommandLineInvoker invoker = new CommandLineInvoker();
		Integrator integrator = new Integrator();
		
		try {
			invoker.processArguments(args);
			if (invoker.getReadyToRun()) {
				integrator.setDitaDir(invoker.getDitaDir());
				integrator.setProperties(new File("integrator.properties"));
				integrator.execute();
				invoker.startAnt();
			}
		} catch (Exception e) {
			javaLogger.logException(e);
		}
	}

}
