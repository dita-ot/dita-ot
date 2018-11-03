/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2005 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.dita.dost.ant;

import static org.dita.dost.ant.ExtensibleAntInvoker.isValid;
import static org.dita.dost.log.MessageBean.*;

import java.util.ArrayList;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Echo;
import org.dita.dost.ant.ExtensibleAntInvoker.ParamElem;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.log.DITAOTAntLogger;
import org.dita.dost.log.MessageBean;
import org.dita.dost.log.MessageUtils;

/**
 * Ant echo task for custom error message.
 *
 * @author Wu, Zhi Qiang
 */
public final class DITAOTEchoTask extends Echo {
    private String id = null;

    /** Nested params. */
    private final ArrayList<ParamElem> params = new ArrayList<>();
    private DITAOTLogger logger;

    /**
     * Default Construtor.
     *
     */
    public DITAOTEchoTask() {
    }
    /**
     * Setter function for id.
     * @param identifier The id to set.
     */
    public void setId(final String identifier) {
        id = identifier;
    }

    /**
     * Handle nested parameters. Add the key/value to the pipeline hash, unless
     * the "if" attribute is set and refers to a unset property.
     * @return parameter
     */
    public ParamElem createParam() {
        final ParamElem p = new ParamElem();
        params.add(p);
        return p;
    }

    /**
     * Task execute point.
     * @throws BuildException exception
     * @see org.apache.tools.ant.taskdefs.Echo#execute()
     */
    @Override
    public void execute() throws BuildException {
        logger = new DITAOTAntLogger(getProject());
        final MessageBean msgBean = MessageUtils.getMessage(id, readParamValues());
        if (msgBean != null) {
            final String type = msgBean.getType();
            if (ERROR.equals(type)) {
                logger.error(msgBean.toString());
            } else if (WARN.equals(type)) {
                logger.warn(msgBean.toString());
            } else if (INFO.equals(type)) {
                logger.info(msgBean.toString());
            } else if (DEBUG.equals(type)) {
                logger.debug(msgBean.toString());
            }
        }
    }

    /**
     * Read parameter values to an array.
     *
     * @return parameter values where array index corresponds to parameter name
     */
    private String[] readParamValues() throws BuildException {
        final ArrayList<String> prop = new ArrayList<>();
        for (final ParamElem p : params) {
            if (!p.isValid()) {
                throw new BuildException("Incomplete parameter");
            }
            if (isValid(getProject(), getLocation(), p.getIf(), p.getUnless())) {
                final int idx = Integer.parseInt(p.getName()) - 1;
                if (idx >= prop.size()) {
                    prop.ensureCapacity(idx + 1);
                    while (prop.size() < idx + 1) {
                        prop.add(null);
                    }
                }
                prop.set(idx, p.getValue());
            }
        }
        return prop.toArray(new String[0]);
    }

}
