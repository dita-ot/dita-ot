/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2005 All Rights Reserved.
 */
package org.dita.dost.log;

import static org.dita.dost.log.MessageBean.*;
import static org.dita.dost.util.Constants.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildLogger;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.util.DateUtils;
import org.apache.tools.ant.util.StringUtils;
import org.dita.dost.exception.DITAOTException;

/**
 * Ant build logger for {@link org.dita.dost.invoker.CommandLineInvoker}, not intended to be used as a stand-alone logger.
 * 
 * @author Wu, Zhi Qiang
 * 
 * @see org.dita.dost.invoker.CommandLineInvoker
 */
public final class DITAOTBuildLogger implements BuildLogger {

    /**
     * Size of left-hand column for right-justified task name.
     * 
     * @see #messageLogged(BuildEvent)
     */
    public static final int LEFT_COLUMN_SIZE = 12;

    /** Line separator.*/
    protected static final String LINE_SEP = StringUtils.LINE_SEP;

    private final AtomicInteger numOfFatals = new AtomicInteger(0);
    private final AtomicInteger numOfErrors = new AtomicInteger(0);
    private final AtomicInteger numOfWarnings = new AtomicInteger(0);
    private final AtomicInteger numOfInfo = new AtomicInteger(0);
    
    private final Pattern fatalPattern = Pattern.compile("\\[\\w+F\\]\\[FATAL\\]");
    private final Pattern errorPattern = Pattern.compile("\\[\\w+E\\]\\[ERROR\\]");
    private final Pattern warnPattern = Pattern.compile("\\[\\w+W\\]\\[WARN\\]");
    private final Pattern infoPattern = Pattern.compile("\\[\\w+I\\]\\[INFO\\]");
    private final Pattern debugPattern = Pattern.compile("\\[\\w+D\\]\\[DEBUG\\]");
    
    /**
     * Convenience method to format a specified length of time.
     * 
     * @param millis
     *            Length of time to format, in milliseconds.
     * 
     * @return the time as a formatted string.
     * 
     * @see DateUtils#formatElapsedTime(long)
     */
    protected static String formatTime(final long millis) {
        return DateUtils.formatElapsedTime(millis);
    }

    /**
     * This is an override point: the message that indicates whether a build
     * failed. Subclasses can change/enhance the message.
     * 
     * @return The classic "BUILD FAILED"
     */
    protected static String getBuildFailedMessage() {
        return "BUILD FAILED";
    }

    /**
     * This is an override point: the message that indicates that a build
     * succeeded. Subclasses can change/enhance the message.
     * 
     * @return The classic "BUILD SUCCESSFUL"
     */
    protected static String getBuildSuccessfulMessage() {
        return "BUILD SUCCESSFUL";
    }

    /**
     * Prints a message to a PrintStream.
     * 
     * @param message
     *            The message to print. Should not be <code>null</code>.
     * @param stream
     *            A PrintStream to print the message to. Must not be
     *            <code>null</code>.
     * @param priority
     *            The priority of the message. (Ignored in this implementation.)
     */
    protected void printMessage(final String message,
            final PrintStream stream, final int priority) {
        // fix priority
        int fixedPriority = priority;
        if (fatalPattern.matcher(message).find()) {
            fixedPriority = Project.MSG_ERR;
            numOfFatals.incrementAndGet();
        } else if (errorPattern.matcher(message).find()) {
            fixedPriority = Project.MSG_ERR;
        } else if (warnPattern.matcher(message).find()) {
            fixedPriority = Project.MSG_WARN;
        } else if (infoPattern.matcher(message).find()) {
            fixedPriority = Project.MSG_INFO;
        } else if (debugPattern.matcher(message).find()) {
            fixedPriority = Project.MSG_DEBUG;
        }
        // collect levels
        switch (fixedPriority) {
        case Project.MSG_ERR:
            numOfErrors.incrementAndGet();
            break;
        case Project.MSG_WARN:
            numOfWarnings.incrementAndGet();
            if(!message.contains("[WARN]")) {
            	stream.println("Extra warnings counted");
            	logger.logInfo("Extra warnings counted");
            } else {
            	stream.println("Normal warnings counted");
            	logger.logInfo("Normal warnings counted");
            }
            break;
        case Project.MSG_INFO:
            numOfInfo.incrementAndGet();
            break;
        }
        if (fixedPriority <= Project.MSG_INFO) {
            stream.println(message);
        }
    }

    private PrintStream err;

    final private DITAOTFileLogger logger;

    /** Lowest level of message to write out */
    private int msgOutputLevel = Project.MSG_ERR;

    private PrintStream out;

    /** Time of the start of the build */
    private long startTime = System.currentTimeMillis();
    /** Set which contains already captured exceptions */
    private final Set<Throwable> exceptionsCaptured=new HashSet<Throwable>();
    /**
     * Constructor to init logger.
     * 
     */
    public DITAOTBuildLogger() {
        logger = DITAOTFileLogger.getInstance();
    }

    /**
     * Invoke when build finished. Do the logging.
     * @param event event
     * @see org.apache.tools.ant.BuildListener#buildFinished(org.apache.tools.ant.BuildEvent)
     */
    @Override
    public void buildFinished(final BuildEvent event) {
        final Throwable error = event.getException();
        final StringBuffer message = new StringBuffer();

        message.append("Processing ended.");
        message.append(LINE_SEP);

        if (error == null && numOfFatals.get() == 0 && numOfErrors.get() == 0) {
            message.append(LINE_SEP);
            message.append(getBuildSuccessfulMessage());
        } else {
            message.append(LINE_SEP);
            message.append(getBuildFailedMessage());
            message.append(LINE_SEP);
            // If ant have not errors
            if (error != null) {
                message.append(error.toString());
                message.append(LINE_SEP);
                if (Project.MSG_VERBOSE <= msgOutputLevel) {
                    message.append(StringUtils.getStackTrace(error));
                }
            }
        }
        message.append(LINE_SEP);
        message.append("Number of Fatals : ").append(numOfFatals.get()).append(LINE_SEPARATOR);
        message.append("Number of Errors : ").append(numOfErrors.get()).append(LINE_SEPARATOR);
        message.append("Number of Warnings : ").append(numOfWarnings.get()).append(LINE_SEPARATOR);
        message.append(LINE_SEP);
        message.append("Total time: ");
        message.append(formatTime(System.currentTimeMillis() - startTime));

        final String msg = message.toString();

        if (error == null) {
            printMessage(msg, out, Project.MSG_INFO);
            logger.logInfo(msg);
        } else {
            //fix the block problem which caused by the printMessage to err in java -jar lib/dost.jar ...
            //printMessage(msg, err, Project.MSG_ERR);
            printMessage(msg, out, Project.MSG_ERR);
            logger.logError(msg);
        }

        logger.closeLogger();
    }

    /**
     * Record start time.
     * @param event event
     * @see org.apache.tools.ant.BuildListener#buildStarted(org.apache.tools.ant.BuildEvent)
     */
    @Override
    public void buildStarted(final BuildEvent event) {
        startTime = System.currentTimeMillis();
    }

    /**
     * @param event event
     * @see org.apache.tools.ant.BuildListener#messageLogged(org.apache.tools.ant.BuildEvent)
     */
    @Override
    public void messageLogged(final BuildEvent event) {
        final int priority = event.getPriority();
        // Filter out messages based on priority
        if (priority > msgOutputLevel) {
            return;
        }

        final StringBuffer message = new StringBuffer();
        final Task eventTask = event.getTask();
        if (eventTask != null) {
            // Print out the name of the task if we're in one
            final String label = new StringBuffer().append("  [").append(
                    eventTask.getTaskName()).append("] ").toString();

            BufferedReader r = null;
            try {
                String line;
                boolean first = true;
                r = new BufferedReader(new StringReader(event.getMessage()));
                line = r.readLine();
                while (line != null) {
                    if (!first) {
                        message.append(StringUtils.LINE_SEP);
                    }
                    first = false;
                    message.append(label).append(line);

                    line = r.readLine();
                }
            } catch (final IOException e) {
                // shouldn't be possible
                message.append(label).append(event.getMessage());
            } finally {
                try {
                    r.close();
                } catch (final IOException ioe) {

                }
            }
        } else {
            message.append(event.getMessage());
        }

        final String msg = message.toString();
        //analyse the message to catch the error from HHC
        catchHHCError(msg);
        if (priority != Project.MSG_ERR) {
            boolean flag = false;
            // filter out message came from XSLT in console,
            // except those contains [DOTXxxx]
            if (eventTask != null && "xslt".equals(eventTask.getTaskName())
                    && msg.indexOf("DOTX") == -1) {
                flag = true;
            }

            // filter out fop messages in console
            if (eventTask != null && "fop".equals(eventTask.getTaskName())) {
                flag = true;
            }

            if (!flag) {
                printMessage(msg, out, priority);
            }

            // always log to log file
            logger.logInfo(msg);
        } else {
        	//for error msg return from CHM compiler, such as "[exec] Result: 1", just log it, not count it
            if(eventTask!=null && "exec".equals(eventTask.getTaskName()) && msg.indexOf("ERROR") == -1) {
            	logger.logError(msg);
            } else {
            	printMessage(msg, err, priority);
            	logger.logError(msg);
            }
        }
    }

    /**
     * Ignored.
     * @param mode mode
     * @see org.apache.tools.ant.BuildLogger#setEmacsMode(boolean)
     */
    @Override
    public void setEmacsMode(final boolean mode) {
    }

    /**
     * Setter function for errorPrintStream.
     * @param errorPrintStream errorPrintStream
     * @see org.apache.tools.ant.BuildLogger#setErrorPrintStream(java.io.PrintStream)
     */
    @Override
    public void setErrorPrintStream(final PrintStream errorPrintStream) {
        err = new PrintStream(errorPrintStream, true);
    }

    /**
     * Setter function for messageOutputLevel.
     * @param level outputlevel
     * @see org.apache.tools.ant.BuildLogger#setMessageOutputLevel(int)
     */
    @Override
    public void setMessageOutputLevel(final int level) {
        msgOutputLevel = level;
    }

    /**
     * Setter function for outputPrintStream, and set it to autoflush.
     * @param output output file
     * @see org.apache.tools.ant.BuildLogger#setOutputPrintStream(java.io.PrintStream)
     */
    @Override
    public void setOutputPrintStream(final PrintStream output) {
        out = new PrintStream(output, true);
    }

    /**
     * Ignored.
     * @param event event
     * @see org.apache.tools.ant.BuildListener#targetFinished(org.apache.tools.ant.BuildEvent)
     */
    @Override
    public void targetFinished(final BuildEvent event) {
    }

    /**
     * @param event event
     * @see org.apache.tools.ant.BuildListener#targetStarted(org.apache.tools.ant.BuildEvent)
     */
    @Override
    public void targetStarted(final BuildEvent event) {
        if (Project.MSG_INFO <= msgOutputLevel
                && !"".equals(event.getTarget().getName())) {
            final String desc = event.getTarget().getDescription();
            final String msg = desc + "...";
            if (desc == null || desc.trim().length() == 0) {
                return;
            }
            printMessage(msg, out, Project.MSG_INFO);
            logger.logInfo(msg);
        }
    }

    /**
     * Ignored.
     * @param event event
     * @see org.apache.tools.ant.BuildListener#taskFinished(org.apache.tools.ant.BuildEvent)
     */
    @Override
    public void taskFinished(final BuildEvent event) {
        //captured the error from ant script or others
        //Error or BuildException may be thrown out from ant
        //BuildException wrapped DITAOTException
        if(event.getException()==null) {
            return;
        }
        final Object exception=event.getException();
        //BuildException from ant

        if(exception instanceof BuildException){
            final BuildException buildEx=(BuildException) exception;
            final Object innerEx=buildEx.getException();

            if(innerEx!=null && innerEx instanceof DITAOTException){

                final DITAOTException ex=(DITAOTException)innerEx;

                if(ex.alreadyCaptured()) {
                    return;
                }

                ex.setCaptured(true);
                final MessageBean msgBean=ex.getMessageBean();
                if(msgBean!=null) {
                    increaseNumOfExceptionByType(msgBean.getType());
                } else {
                    increaseNumOfExceptionByType(null);
                }
                return;

            }

            if(!chkThrowableAlreadyCaptured(buildEx)){
                numOfErrors.incrementAndGet();
                return;
            }

        }else{
            //error from ant
            if(!chkThrowableAlreadyCaptured((Throwable)exception)) {
                numOfErrors.incrementAndGet();
            }
        }
    }

    /**
     * Ignored.
     * 
     * @see org.apache.tools.ant.BuildListener#taskStarted(org.apache.tools.ant.BuildEvent)
     */
    @Override
    public void taskStarted(final BuildEvent event) {
    }

    /**
     * To check the exception whether has been captured by the previous task finished.
     * If the exception has not been caught before,the original exception or error in it will be added into exceptionsCaptured.
     * The exception or error with the same original cause will ignored.
     * @param ex the exception or error to analyse
     * @return true if the exception is wrapped with DITAException or it has been captured before
     */
    private boolean chkThrowableAlreadyCaptured(final Throwable ex) {
        boolean captured = false;

        if(ex==null) {
            return true;
        }

        Throwable parent = ex;
        Object unknownEx = parent.getCause();
        while (unknownEx != null) {
            parent = (Throwable) unknownEx;
            if (exceptionsCaptured.contains(unknownEx)
                    || unknownEx instanceof DITAOTException) {
                captured = true;
                return captured;
            } else {
                unknownEx = ((Throwable) unknownEx).getCause();
            }
        }

        if (captured == false) {
            exceptionsCaptured.add(parent);
        }

        return captured;

    }

    private void catchHHCError(final String message){
        //no good method to catch errors/exception from HHC.
        if(message==null) {
            return;
        }
        final String upperMessage=message.toUpperCase();
        if(upperMessage.indexOf("HHC")!=-1 && upperMessage.indexOf("ERROR:")!=-1){
            numOfErrors.incrementAndGet();
        }
    }
        
    private void increaseNumOfExceptionByType(final String msgType) {
        if (msgType == null) {
            numOfInfo.incrementAndGet();
        } else {
            final String type = msgType.toUpperCase();
            if (FATAL.equals(type)) {
                numOfFatals.incrementAndGet();
            } else if (ERROR.equals(type)) {
                numOfErrors.incrementAndGet();
            } else if (WARN.equals(type)) {
                numOfWarnings.incrementAndGet();
                out.println("Extra warnings counted");
                logger.logInfo("Extra warnings counted");

            } else if (INFO.equals(type)) {
                numOfInfo.incrementAndGet();
            }
        }
    }
    
}
