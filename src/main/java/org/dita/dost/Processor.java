package org.dita.dost;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.LoggerListener;
import org.slf4j.Logger;

import java.io.File;
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
     * Run process
     *
     * @throws DITAOTException if processing failed
     */
    public void run() throws DITAOTException {
        if (!args.containsKey("args.input")) {
            throw new IllegalStateException();
        }
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
            targets.addElement(project.getDefaultTarget());
            project.executeTargets(targets);
        } catch (final BuildException e) {
            throw new DITAOTException(e);
        } finally {
            System.setOut(savedOut);
            System.setErr(savedErr);
        }
    }

}
