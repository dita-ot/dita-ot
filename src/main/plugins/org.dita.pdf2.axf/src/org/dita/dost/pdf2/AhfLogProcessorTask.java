/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */
package org.dita.dost.pdf2;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;

import java.io.*;

/**
 * Read AHF log file and log events to Ant logger.
 *
 * @since 2.0
 * @author Jarno Elovirta
 */
public final class AhfLogProcessorTask extends Task {

    private static String PREFIX = "AHFCmd :";
    private static String PREFIX_INFO = "INFO: ";
    private static String PREFIX_WARN = "WARNING: ";
    private static String PREFIX_ERROR = "RECOVERABLE: ";
    private static String PREFIX_FATAL = "FATAL: ";

    private File file;

    @Override
    public void execute() throws BuildException {
        if (file == null) {
            throw new IllegalArgumentException("File argument not set");
        }
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(file));
            String line;
            // command line
            line = in.readLine();
            log(line, Project.MSG_VERBOSE);
            // product line
            line = in.readLine();
            if (line.startsWith(PREFIX)) {
                line = line.substring(PREFIX.length());
            }
            log(line.trim(), Project.MSG_VERBOSE);
            // copyright line
            line = in.readLine().trim();
            log(line, Project.MSG_DEBUG);
            // separator row
            in.readLine();
            // messages
            while((line = in.readLine()) != null) {
                // TODO: outputs as <path>:<row>:<col>: [<code>][<level>] <msg>
                if (line.startsWith(PREFIX)) {
                    line = line.substring(PREFIX.length());
                }
                int level = Project.MSG_INFO;
                if (line.startsWith(PREFIX_INFO)) {
                    line = line.substring(PREFIX_INFO.length());
                    level = Project.MSG_INFO;
                } else if (line.startsWith(PREFIX_WARN)) {
                    line = line.substring(PREFIX_WARN.length());
                    level = Project.MSG_WARN;
                } else if (line.startsWith(PREFIX_ERROR)) {
                    line = line.substring(PREFIX_ERROR.length());
                    level = Project.MSG_ERR;
                } else if (line.startsWith(PREFIX_FATAL)) {
                    line = line.substring(PREFIX_FATAL.length());
                    level = Project.MSG_ERR;
                }
                log(line, level);
            }
        } catch (final FileNotFoundException e) {
            new BuildException("Failed to find log file: " + e.getMessage(), e);
        } catch (IOException e) {
            new BuildException("Failed to read log file: " + e.getMessage(), e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (final IOException e) {
                }
            }
        }
    }

    public void setFile(final File file) {
        this.file = file;
    }
    
}
