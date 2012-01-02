/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2005 All Rights Reserved.
 */
package org.dita.dost.log;

import java.io.File;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Exit;
import org.dita.dost.exception.DITAOTException;
import org.dita.dost.invoker.ExtensibleAntInvoker.Param;

/**
 * Ant fail task for custom error message.
 *
 * @author Wu, Zhi Qiang
 */
public final class DITAOTFailTask extends Exit {
    private String id = null;

    private final Properties prop = new Properties();
    /** Nested params. */
    private final ArrayList<Param> params = new ArrayList<Param>();

    /**
     * Default Construtor.
     *
     */
    public DITAOTFailTask(){
    }
    
    /**
     * Set the id.
     * @param identifier The id to set.
     * 
     */
    public void setId(final String identifier) {
        this.id = identifier;
    }

    /**
     * Set the parameters.
     * @param params The prop to set.
     * @deprecated use nested {@code param} elements instead with {@link #createParam()}
     */
    @Deprecated
    public void setParams(final String params) {
        final StringTokenizer tokenizer = new StringTokenizer(params, ";");
        while (tokenizer.hasMoreTokens()) {
            final String token = tokenizer.nextToken();
            final int pos = token.indexOf("=");
            this.prop.put(token.substring(0, pos), token.substring(pos + 1));
        }
    }
    
    /**
     * Handle nested parameters. Add the key/value to the pipeline hash, unless
     * the "if" attribute is set and refers to a unset property.
     * @return parameter
     */
    public Param createParam() {
        final Param p = new Param();
        params.add(p);
        return p;
    }

    /**
     * Task execute point.
     * @throws BuildException exception
     * @see org.apache.tools.ant.taskdefs.Exit#execute()
     */
    @Override
    public void execute() throws BuildException {
        if (id == null) {
            throw new BuildException("id attribute must be specified");
        }
        for (final Param p : params) {
            if (!p.isValid()) {
                throw new BuildException("Incomplete parameter");
            }
            final String ifProperty = p.getIf();
            final String unlessProperty = p.getUnless();
            if ((ifProperty == null || getProject().getProperties().containsKey(ifProperty))
                    && (unlessProperty == null || !getProject().getProperties().containsKey(unlessProperty))) {
                prop.put("%" + p.getName(), p.getValue());
            }
        }
        
        initMessageFile();
        final MessageBean msgBean=MessageUtils.getMessage(id, prop);
        setMessage(msgBean.toString());
        try{
            super.execute();
        }catch(final BuildException ex){
            throw new BuildException(msgBean.toString(),new DITAOTException(msgBean,ex,msgBean.toString()));
        }
    }

    private void initMessageFile() {
        String messageFile = getProject().getProperty(
                "args.message.file");

        if(!new File(messageFile).exists()){
            MessageUtils.loadDefaultMessages();
            return;
        }

        if (!new File(messageFile).isAbsolute()) {
            messageFile = new File(getProject().getBaseDir(), messageFile)
            .getAbsolutePath();
        }

        MessageUtils.loadMessages(messageFile);
    }

}
