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
	private boolean debugMode = false;
	DITAOTJavaLogger javaLogger = new DITAOTJavaLogger();
	
	static {
		paramMap = new HashMap();

		paramMap.put("/i", "args.input");
		paramMap.put("/if", "dita.input");
		paramMap.put("/id", "dita.input.dirname");
		paramMap.put("/artlbl", "args.artlbl");
		paramMap.put("/draft", "args.draft");
		paramMap.put("/ftr", "args.ftr");
		paramMap.put("/hdr", "args.hdr");
		paramMap.put("/hdf", "args.hdf");
		paramMap.put("/csspath", "args.csspath");
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
	}

	/**
	 * Constructor: CommandLineInvoker
	 */
	public CommandLineInvoker() {
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
		String tempDir = null;

		/*
		 * Process input arguments
		 */
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			String javaArg = null;
			String antArg = null;
			int colonPos = arg.indexOf(Constants.COLON);

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
		 * Create temp property file
		 */
		tempDir = prop.getProperty("dita.temp.dir", Constants.TEMP_DIR_DEFAULT);
		File tempPath = new File(tempDir);
		if (!tempPath.exists() && tempPath.mkdirs() == false) {
			String msg = null;
			Properties params = new Properties();

			params.put("%1", tempDir);
			msg = MessageUtils.getMessage("DOTJ004F", params).toString();

			throw new DITAOTException(msg);
		}

		propertyFile = new StringBuffer(tempDir).append("/property.temp")
				.toString();

		/*
		 * Output input params into temp property file
		 */
		propWriter = new PropertiesWriter();
		content = new ContentImpl();
		content.setValue(prop);
		propWriter.setContent(content);
		propWriter.write(propertyFile);
	}

	/**
	 * Start ant process to execute the build process.
	 * 
	 * @throws IOException
	 */
	public void startAnt() throws IOException {
		String antScript = "ant.bat"; // default for windows
		StringBuffer cmdBuffer = new StringBuffer(Constants.INT_64);
		BufferedReader reader = null;
		Process antProcess = null;		

		if (Constants.OS_NAME.toLowerCase().indexOf(Constants.OS_NAME_WINDOWS) == -1) {
			antScript = "ant";
		}

		cmdBuffer.append(antScript);
		cmdBuffer.append(" -f conductor.xml");
		cmdBuffer.append(" -logger org.dita.dost.log.DITAOTBuildLogger");
		cmdBuffer.append(" -propertyfile ").append(propertyFile);
		if (debugMode) {
			cmdBuffer.append(" -d");
		}

		antProcess = Runtime.getRuntime().exec(cmdBuffer.toString());

		/*
		 * Get output message and print to console. 
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

	/**
	 * Get the input parameter and map them into parameters which can be
	 * accepted by Ant build task. Then start the building process
	 * 
	 * @param args
	 * 
	 */
	public static void main(String[] args) {
		CommandLineInvoker invoker = new CommandLineInvoker();

		try {
			File currentDir = new File(".");
			String ditaDir = currentDir.getAbsolutePath();
			Integrator integrator = new Integrator();
			integrator.setDitaDir(ditaDir.substring(0,ditaDir.lastIndexOf(Constants.FILE_SEPARATOR)));
			integrator.execute();
			invoker.processArguments(args);
			invoker.startAnt();
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}

}
