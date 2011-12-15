/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2005 All Rights Reserved.
 */
package org.dita.dost.log;

import static org.dita.dost.log.MessageBean.*;

import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Echo;
import org.dita.dost.util.LogUtils;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.log.DITAOTAntLogger;

/**
 * Class description goes here.
 * 
 * @author Wu, Zhi Qiang
 */
public final class DITAOTEchoTask extends Echo {
    private String id = null;

    private Properties prop = null;

    private DITAOTLogger logger;
    
    /**
     * Default Construtor.
     *
     */
    public DITAOTEchoTask(){
    }
    /**
     * Setter function for id.
     * @param identifier The id to set.
     */
    public void setId(final String identifier) {
        this.id = identifier;
    }

    /**
     * Set the parameters.
     * @param params  The prop to set.
     */
    public void setParams(final String params) {
        final StringTokenizer tokenizer = new StringTokenizer(params, ";");
        prop = new Properties();
        while (tokenizer.hasMoreTokens()) {
            final String token = tokenizer.nextToken();
            final int pos = token.indexOf("=");
            this.prop.put(token.substring(0, pos), token.substring(pos + 1));
        }
    }

    /**
     * Task execute point.
     * @throws BuildException exception
     * @see org.apache.tools.ant.taskdefs.Echo#execute()
     */
    @Override
    public void execute() throws BuildException {
        logger = new DITAOTAntLogger(getProject());
        final MessageBean msgBean = MessageUtils.getMessage(id, prop);
        //setMessage(msgBean.toString());
        //super.execute();
        if (msgBean != null) {
            final String type = msgBean.getType();
            if(FATAL.equals(type)){
                logger.logFatal(msgBean.toString());
            } else if(ERROR.equals(type)){
                logger.logError(msgBean.toString());
            } else if(WARN.equals(type)){
                logger.logWarn(msgBean.toString());
            } else if(INFO.equals(type)){
                logger.logInfo(msgBean.toString());
            } else if(DEBUG.equals(type)){
                logger.logDebug(msgBean.toString());
            }
        }
    }

}
