package org.dita.dost;

import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.LoggerListener;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * DITA-OT processer. Not thread-safe, but can be reused.
 */
public final class Processor {

    private File ditaDir;
    private Map<String, String> args;
    private Logger logger;
    private boolean cleanOnFailure = true;

    Processor(final File ditaDir, final String transtype, final Map<String, String> args) {
        this.ditaDir = ditaDir;
        this.args = new HashMap<>(args);
        this.args.put("dita.dir", ditaDir.getAbsolutePath());
        this.args.put("transtype", transtype);
    }

    /**
     * Set input document.
     *
     * @param input input document file
     * @return this Process object
     */
    public Processor setInput(final File input) {
        if (!input.isAbsolute()) {
            throw new IllegalArgumentException("Input file path must be absolute");
        }
        setInput(input.toURI());
        return this;
    }

    /**
     * Set input document.
     *
     * @param input absolute input document URI
     * @return this Process object
     */
    public Processor setInput(final URI input) {
        if (!input.isAbsolute()) {
            throw new IllegalArgumentException("Input file URI must be absolute");
        }
        args.put("args.input", input.toString());
        return this;
    }

    /**
     * Set output directory.
     *
     * @param output absolute output directory
     * @return this Process object
     */
    public Processor setOutput(final File output) {
        if (!output.isAbsolute()) {
            throw new IllegalArgumentException("Output directory path must be absolute");
        }
        args.put("output.dir", output.getAbsolutePath());
        return this;
    }

    /**
     * Set output directory.
     *
     * @param output absolute output directory URI
     * @return this Process object
     */
    public Processor setOutput(final URI output) {
        if (!output.isAbsolute()) {
            throw new IllegalArgumentException("Output directory URI must be absolute");
        }
        if (!output.getScheme().equals("file")) {
            throw new IllegalArgumentException("Only file scheme allowed as output directory URI");
        }
        args.put("output.dir", output.toString());
        return this;
    }

    /**
     * Set property. Existing property mapping will be overridden.
     *
     * @param name property name
     * @param value property value
     * @return this Process object
     */
    public Processor setProperty(final String name, final String value) {
        args.put(name, value);
        return this;
    }

    /**
     * Set properties. Existing property mapping will be overridden.
     *
     * @param value property mappings
     * @return this Process object
     */
    public Processor setProperties(final Map<String, String> value) {
        args.putAll(value);
        return this;
    }

    /**
     * Set process logger
     *
     * @param logger process logger
     * @return this Process object
     */
    public Processor setLogger(final Logger logger) {
        this.logger = logger;
        return this;
    }

    /**
     * Clean temporary directory when process fails. By default temporary directory is always cleaned.
     *
     * @param cleanOnFailure clean on failure
     * @return this Process object
     */
    public Processor cleanOnFailure(final boolean cleanOnFailure) {
        this.cleanOnFailure = cleanOnFailure;
        return this;
    }

    /**
     * Run process
     *
     * @throws DITAOTException if processing failed
     */
    public void run() throws DITAOTException {
        if (!args.containsKey("args.input")) {
            throw new IllegalStateException();
        }
        final File tempDir = getTempDir();
        args.put("dita.temp.dir", tempDir.getAbsolutePath());

        final PrintStream savedErr = System.err;
        final PrintStream savedOut = System.out;
        try {
            final File buildFile = new File(ditaDir, "build.xml");
            final Project project = new Project();
            project.setCoreLoader(this.getClass().getClassLoader());
            if (logger != null) {
                project.addBuildListener(new LoggerListener(logger));
            }
            project.fireBuildStarted();
            project.init();
            project.setBaseDir(ditaDir);
            project.setKeepGoingMode(false);
            for (final Map.Entry<String, String> arg : args.entrySet()) {
                project.setUserProperty(arg.getKey(), arg.getValue());
            }
            ProjectHelper.configureProject(project, buildFile);
            final Vector<String> targets = new Vector<>();
//            targets.addElement(project.getDefaultTarget());
            targets.addElement("dita2" + args.get("transtype"));
            project.executeTargets(targets);
            try {
                FileUtils.forceDelete(tempDir);
            } catch (final IOException ex) {
                logger.error("Failed to delete temporary directory " + tempDir);
            }
        } catch (final BuildException e) {
            if (cleanOnFailure) {
                try {
                    FileUtils.forceDelete(tempDir);
                } catch (final IOException ex) {
                    logger.error("Failed to delete temporary directory " + tempDir);
                }
            }
            throw new DITAOTException(e);
        } finally {
            System.setOut(savedOut);
            System.setErr(savedErr);
        }
    }

    private File getTempDir() {
        final File baseTempDir = new File(args.get("base.temp.dir"));
        File tempDir;
        for (int i = 0; i < 10; i++) {
            tempDir = new File(baseTempDir, Long.toString(System.currentTimeMillis()));
            if (!tempDir.exists()) {
                return tempDir;
            }
        }
        throw new RuntimeException("Unable to create temporary directory");
    }

}
