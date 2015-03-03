/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.invoker;

import static org.dita.dost.util.Constants.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.platform.Integrator;
import org.dita.dost.util.Configuration;

/**
 * Command line tool for running DITA OT.
 * 
 * @version 1.0 2005-5-31
 * @author Zhang, Yuan Peng
 */

public final class CommandLineInvoker {

    /**logger.*/
    private static final DITAOTLogger logger = new DITAOTJavaLogger();
    /** Map to store input parameters.*/
    private static final Map<String, String> paramMap;
    static {
        final Map<String, String> pm = new HashMap<String,String>();
        pm.put("/basedir", "basedir");
        pm.put("/ditadir", "dita.dir");
        pm.put("/i", "args.input");
        pm.put("/if", "dita.input");
        pm.put("/id", "dita.input.dirname");
        pm.put("/artlbl", "args.artlbl");
        pm.put("/draft", "args.draft");
        pm.put("/ftr", "args.ftr");
        pm.put("/hdr", "args.hdr");
        pm.put("/hdf", "args.hdf");
        pm.put("/csspath", "args.csspath");
        pm.put("/cssroot", "args.cssroot");
        pm.put("/css", "args.css");
        pm.put("/filter", "args.filter");
        pm.put("/outdir", "output.dir");
        pm.put("/transtype", "transtype");
        pm.put("/indexshow", "args.indexshow");
        pm.put("/outext", "args.outext");
        pm.put("/copycss", "args.copycss");
        pm.put("/xsl", "args.xsl");
        pm.put("/xslpdf", "args.xsl.pdf");
        pm.put("/tempdir", "dita.temp.dir");
        pm.put("/cleantemp", "clean.temp");
        pm.put("/foimgext", "args.fo.img.ext");
        pm.put("/javahelptoc", "args.javahelp.toc");
        pm.put("/javahelpmap", "args.javahelp.map");
        pm.put("/eclipsehelptoc", "args.eclipsehelp.toc");
        pm.put("/eclipsecontenttoc", "args.eclipsecontent.toc");
        pm.put("/xhtmltoc", "args.xhtml.toc");
        pm.put("/xhtmlclass", "args.xhtml.classattr");
        pm.put("/usetasklabels", "args.gen.task.lbl");
        pm.put("/logdir", "args.logdir");
        pm.put("/ditalocale", "args.dita.locale");
        pm.put("/fooutputrellinks", "args.fo.output.rel.links");
        pm.put("/foincluderellinks", "args.fo.include.rellinks");
        pm.put("/odtincluderellinks", "args.odt.include.rellinks");
        pm.put("/retaintopicfo", "retain.topic.fo");
        pm.put("/version", "args.eclipse.version");
        pm.put("/provider", "args.eclipse.provider");
        pm.put("/fouserconfig", "args.fo.userconfig");
        pm.put("/htmlhelpincludefile", "args.htmlhelp.includefile");
        pm.put("/validate", "validate");
        pm.put("/outercontrol", "outer.control");
        pm.put("/generateouter", "generate.copy.outer");
        pm.put("/onlytopicinmap", "onlytopic.in.map");
        pm.put("/debug", "args.debug");
        //added on 20100824 to disable grammar pool caching start
        pm.put("/grammarcache", "args.grammar.cache");
        //added on 20100824 to disable grammar pool caching end
        pm.put("/odtimgembed", "args.odt.img.embed");
        paramMap = Collections.unmodifiableMap(pm);
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
     * Process input arguments.
     * 
     * @param args args
     * @throws DITAOTException Exception
     */
    public void processArguments(final String[] args) throws DITAOTException {
        final Properties prop = new Properties();

        if(args.length == 0){
            printUsage();
            readyToRun = false;
            return;
        }

        /*
         * validate dita.dir and init log message file
         */
        String inputDitaDir = null;
        for (final String arg : args) {
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

        /*
         * Process input arguments
         */
        for (final String arg : args) {
            if ("-help".equals(arg) || "-h".equals(arg) || "help".equals(arg)) { // plain "help" is a bug, keep for backwards compatibility
                printUsage();
                return;
            } else if ("-version".equals(arg)) {
                printVersion();
                return;
            } else if ("/debug".equals(arg) || "/d".equals(arg)) {
                debugMode = true;
                continue;
            }

            final int colonPos = arg.indexOf(COLON);
            if (colonPos == -1) {
                printUsage();
                throw new DITAOTException(MessageUtils.getInstance().getMessage("DOTJ001F", arg).toString());
            }

            final String javaArg = arg.substring(0, colonPos);
            final String antArg = paramMap.get(javaArg.toLowerCase());

            if (antArg == null) {
                printUsage();
                throw new DITAOTException(MessageUtils.getInstance().getMessage("DOTJ002F", javaArg).toString());
            }

            String antArgValue = arg.substring(colonPos + 1);
            if (antArgValue.trim().length() == 0) {
                printUsage();
                throw new DITAOTException(MessageUtils.getInstance().getMessage("DOTJ003F", javaArg).toString());
            }

            if (antArg.equals("clean.temp")
                    && !("yes".equalsIgnoreCase(antArgValue)
                            || "no".equalsIgnoreCase(antArgValue))) {
                antArgValue = "yes";

            }

            prop.put(antArg, antArgValue);
        }

        /*
         * Init base directory for transformation
         */
        final String baseDir = new File(prop.getProperty("basedir", "")).getAbsolutePath();
        prop.put("basedir", baseDir);
        prop.put("dita.dir", ditaDir);

        /*
         * Init temp directory
         * 
         */
        String tempDir;
        if (prop.containsKey("dita.temp.dir")) {
            tempDir = prop.getProperty("dita.temp.dir");
        } else {
            final java.text.DateFormat format = new java.text.SimpleDateFormat("yyyyMMddHHmmssSSS");
            final String timestamp = format.format(new java.util.Date());
            prop.setProperty("dita.temp.dir", TEMP_DIR_DEFAULT + File.separator
                    + "temp" + timestamp);
            tempDir = prop.getProperty("dita.temp.dir");
        }
        //tempDir = prop.getProperty("dita.temp.dir", TEMP_DIR_DEFAULT);
        File tempPath = new File(tempDir);
        if (!tempPath.isAbsolute()) {
            tempPath = new File(baseDir, tempDir);
        }
        if (!(tempPath.exists() || tempPath.mkdirs())) {
            String msg = null;
            msg = MessageUtils.getInstance().getMessage("DOTJ004F", tempPath.getAbsolutePath()).toString();

            throw new DITAOTException(msg);
        }

        propertyFile = new File(tempPath, "property.temp").getAbsolutePath();

        /*
         * Output input params into temp property file
         */
        FileOutputStream fileOutputStream = null;
		try {
		    fileOutputStream = new FileOutputStream(propertyFile);
		    prop.store(fileOutputStream, null);
		    fileOutputStream.flush();
		} catch (final Exception e) {
		    throw new DITAOTException(e);
		} finally {
		    if (fileOutputStream != null) {
		        try {
		            fileOutputStream.close();
		        } catch (final Exception e) {
		            throw new DITAOTException(e);
		        }
		    }
		}

        readyToRun = true;
    }

    /**
     * Start ant process to execute the build process.
     * 
     * @throws IOException IOException
     */
    public void startAnt() throws IOException {
        final List<String> cmd = new ArrayList<String>(8);
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
        // targets
        cmd.add("init");

        final String[] cmds = new String[cmd.size()];
        cmd.toArray(cmds);

        startTransformation(cmds);
    }
    /**
     * Get Ant executable.
     * @return Ant executable file name
     */
    private String getCommandRunner() {
        return (OS_NAME.toLowerCase().contains(OS_NAME_WINDOWS))
                ? "ant.bat"
                        : "ant";
    }
    /**
     * begin transformation.
     * @param cmd cmd
     * @throws IOException exception
     */
    private void startTransformation(final String[] cmd) throws IOException {
        BufferedReader outReader = null;
        final Process antProcess = Runtime.getRuntime().exec(cmd);
        try {
            /*
             * Get output messages and print to console.
             * Note: Since these messages have been logged to the log file,
             * there is no need to output them to log file.
             */
            outReader = new BufferedReader(new InputStreamReader(antProcess.getInputStream()));
            for (String line = outReader.readLine(); line != null; line = outReader.readLine()) {
                System.out.println(line);
            }
        } finally {
            if (outReader != null) {
                try {
                    outReader.close();
                } catch (final IOException e) {
                    logger.error(e.getMessage(), e) ;
                }
            }
        }

        BufferedReader errReader = null;
        try {
            errReader = new BufferedReader(new InputStreamReader(antProcess.getErrorStream()));
            for (String line = errReader.readLine(); line != null; line = errReader.readLine()) {
                System.err.println(line);
            }
        } finally {
            if (errReader != null) {
                try {
                    errReader.close();
                } catch (final IOException e) {
                    logger.error(e.getMessage(), e) ;
                }
            }
        }
    }
    /**
     * print dita version.
     */
    private void printVersion() {
        System.out.println(Configuration.configuration.get("otversion"));
    }

    /**
     * Prints the usage information for this class to <code>System.out</code>.
     */
    private void printUsage() {
        System.out.println("java -jar lib/dost.jar [mandatory parameters] [options]");
        System.out.println("Mandatory parameters:");
        System.out.println("  /i:                    specify path and name of the input file");
        System.out.println("  /transtype:            specify the transformation type");
        System.out.println("Options: ");
        System.out.println("  -help, -h              print this message");
        System.out.println("  -version               print the version information and exit");
        System.out.println("  /basedir:              specify the working directory");
        System.out.println("  /ditadir:              specify the toolkit's home directory. Default is \"temp\"");
        System.out.println("  /outdir:               specify the output directory");
        System.out.println("  /tempdir:              specify the temporary directory");
        System.out.println("  /logdir:               specify the log directory");
        System.out.println("  /filter:               specify the name of the file that contains the filter/flaggin/revision information");
        System.out.println("  /draft:                specify whether to output draft info. Valid values are \"no\" and \"yes\". Default is \"no\" (hide them).");
        System.out.println("  /artlbl:               specify whether to output artwork filenames. Valid values are \"no\" and \"yes\"");
        System.out.println("  /ftr:                  specify the file to be placed in the BODY running-footing area");
        System.out.println("  /hdr:                  specify the file to be placed in the BODY running-heading area");
        System.out.println("  /hdf:                  specify the file to be placed in the HEAD area");
        System.out.println("  /csspath:              specify the path for css reference");
        System.out.println("  /css:                  specify user css file");
        System.out.println("  /cssroot:              specify the root directory for user specified css file");
        System.out.println("  /copycss:              specify whether to copy user specified css files. Valid values are \"no\" and \"yes\"");
        System.out.println("  /indexshow:            specify whether each index entry should display within the body of the text itself. Valid values are \"no\" and \"yes\"");
        System.out.println("  /outext:               specify the output file extension for generated xhtml files. Default is \".html\"");
        System.out.println("  /xsl:                  specify the xsl file used to replace the default xsl file");
        System.out.println("  /xslpdf:            	 specify the xsl file used to replace the default xsl file when transforming pdf");
        System.out.println("  /cleantemp:            specify whether to clean the temp directory before each build. Valid values are \"no\" and \"yes\". Default is \"yes\"");
        System.out.println("  /foimgext:             specify the extension of image file in legacy pdf transformation. Default is \".jpg\"");
        System.out.println("  /fooutputrellinks      For legacy PDF transform: determine if links are included in the PDF. Values are \"no\" and \"yes\". Default is \"no\".");
        System.out.println("  /foincluderellinks     For default PDF transform: determine which links are included in the PDF. Values are \"none\", \"all\", and \"nofamily\". Default is \"none\".");
        System.out.println("  /odtincluderellinks    For default ODT transform: determine which links are included in the ODT. Values are \"none\", \"all\", and \"nofamily\". Default is \"none\".");
        System.out.println("  /retaintopicfo         specify that topic.fo file should be preserved in the output directory. Specify any value, such as \"yes\", to preserve the file.");
        System.out.println("  /javahelptoc:          specify the root file name of the output javahelp toc file in javahelp transformation. Default is the name of the input ditamap file");
        System.out.println("  /javahelpmap:          specify the root file name of the output javahelp map file in javahelp transformation. Default is the name of the input ditamap file");
        System.out.println("  /eclipsehelptoc:       specify the root file name of the output eclipsehelp toc file in eclipsehelp transformation. Default is the name of the input ditamap file");
        System.out.println("  /eclipsecontenttoc:    specify the root file name of the output Eclipse content provider toc file in eclipsecontent transformation. Default is the name of the input ditamap file");
        System.out.println("  /xhtmltoc:             specify the root file name of the output xhtml toc file in xhtml transformation");
        System.out.println("  /xhtmlclass:           specify whether DITA element names and ancestry are included in XHTML class attributes. Only \"yes\" and \"no\" are valid values. The default is yes. ");
        System.out.println("  /usetasklabels:        specify whether DITA Task sections should get headings. Only \"YES\" and \"NO\" are valid values. The default is NO. ");
        System.out.println("  /validate:             specify whether the ditamap/dita/xml files to be validated");
        System.out.println("  /outercontrol:         specify how to respond to the overflowing dita/topic files. Only \"fail\", \"warn\" and \"quiet\" are valid values. The default is warn. ");
        System.out.println("  /generateouter:        specify how to deal with the overflowing dita/topic files. Only \"1\", \"2\" and \"3\" are valid values. The default is 1. Option 1: Only generate/copy files that fit within the designated output directory. Option 2: Generate/copy all files, even those that will end up outside of the output directory. Option 3: the old solution,adjust the input.dir according to the referenced files" +
                "(It is the most secure way to avoid broken links). (not default option any more but keep this as the option of backward compatibility).");
        System.out.println("  /onlytopicinmap:       specify whether make dita processor only resolve dita/topic files which are referenced by primary ditamap files Only \"true\" and \"false\" are valid values. The default is false. ");
        System.out.println("  /debug:                specify whether extra debug information should be included in the log. Only \"yes\" and \"no\" are valid values. The default is no. ");
        System.out.println("  /grammarcache:         specify whether grammar pool caching is used when parsing dita files. Only \"yes\" and \"no\" are valid values. The default is yes. ");
        System.out.println("  /odtimgembed:          specify whether embedding images as binary data in odt transform. Only \"yes\" and \"no\" are valid values. The default is yes. ");
    }

    /**
     * Get the input parameter and map them into parameters which can be
     * accepted by Ant build task. Then start the building process
     * 
     * @param args args
     * 
     */
    public static void main(final String[] args) {
        final CommandLineInvoker invoker = new CommandLineInvoker();
        final Integrator integrator = new Integrator();

        try {
            invoker.processArguments(args);
            if (invoker.readyToRun) {
                integrator.setDitaDir(new File(invoker.getDitaDir()));
                integrator.setProperties(new File("integrator.properties"));
                integrator.execute();
                invoker.startAnt();
            }
        } catch (final Exception e) {
            logger.error(e.getMessage(), e) ;
        }
    }

}
