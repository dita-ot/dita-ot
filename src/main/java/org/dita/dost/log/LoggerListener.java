package org.dita.dost.log;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;
import org.slf4j.Logger;

import java.util.regex.Pattern;

/**
 * Logger adapter from Ant logger to SLF4J logger.
 */
public final class LoggerListener implements BuildListener {

    private static final Pattern FATAL_PATTERN = Pattern.compile("\\[\\w+F]\\[FATAL]");
    private static final Pattern ERROR_PATTERN = Pattern.compile("\\[\\w+E]\\[ERROR]");
    private static final Pattern WARN_PATTERN = Pattern.compile("\\[\\w+W]\\[WARN]");
    private static final Pattern INFO_PATTERN = Pattern.compile("\\[\\w+I]\\[INFO]");
    private static final Pattern DEBUG_PATTERN = Pattern.compile("\\[\\w+D]\\[DEBUG]");

    private final Logger logger;

    public LoggerListener(final Logger logger) {
        this.logger = logger;
    }

    @Override
    public void buildStarted(BuildEvent event) {
        //System.out.println("build started: " + event.getMessage());
    }

    @Override
    public void buildFinished(BuildEvent event) {
        //System.out.println("build finished: " + event.getMessage());
    }

    @Override
    public void targetStarted(BuildEvent event) {
        //System.out.println(event.getTarget().getName() + ":");
    }

    @Override
    public void targetFinished(BuildEvent event) {
        //System.out.println("target finished: " + event.getTarget().getName());
    }

    @Override
    public void taskStarted(BuildEvent event) {
        //System.out.println("task started: " + event.getTask().getTaskName());
    }

    @Override
    public void taskFinished(BuildEvent event) {
        //System.out.println("task finished: " + event.getTask().getTaskName());
    }

    @Override
    public void messageLogged(BuildEvent event) {
        final String message = event.getMessage();
        int level;
        if (FATAL_PATTERN.matcher(message).find()) {
            level = Project.MSG_ERR;
        } else if (ERROR_PATTERN.matcher(message).find()) {
            level = Project.MSG_ERR;
        } else if (WARN_PATTERN.matcher(message).find()) {
            level = Project.MSG_WARN;
        } else if (INFO_PATTERN.matcher(message).find()) {
            level = Project.MSG_INFO;
        } else if (DEBUG_PATTERN.matcher(message).find()) {
            level = Project.MSG_DEBUG;
        } else {
            level = event.getPriority();
        }
        switch (level) {
            case Project.MSG_DEBUG:
                logger.trace(message);
                break;
            case Project.MSG_VERBOSE:
                logger.debug(message);
                break;
            case Project.MSG_INFO:
                logger.info(message);
                break;
            case Project.MSG_WARN:
                logger.warn(message);
                break;
            default:
                logger.error(message);
        }
    }
}
