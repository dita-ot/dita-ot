/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.module;

import java.util.Properties;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.log.MessageBean;

/**
 * The factory to create instance for each module class.
 * 
 * @author Lian, Li
 * 
 */
public final class ModuleFactory {
    private static ModuleFactory moduleFactory = null;

    private static final String packagePrefix = "org.dita.dost.module.";

    /**
     * Automatically generated constructor: ModuleFactory.
     */
    public ModuleFactory() {

    }

    /**
     * Method to get the only instance of ModuleFactory. ModuleFactory is a
     * singleton class.
     * 
     * @return ModuleFactory
     */
    public static synchronized ModuleFactory instance() {
        if (moduleFactory == null) {
            moduleFactory = new ModuleFactory();
        }
        return moduleFactory;
    }

    /**
     * Create the Module class instance according to moduleName.
     * 
     * @param moduleName moduleName
     * @return AbstractPipelineModule
     * @throws DITAOTException DITAOTException
     */
    public AbstractPipelineModule createModule(final String moduleName)
            throws DITAOTException {
        final String module = packagePrefix + moduleName + "Module";

        try {
            return (AbstractPipelineModule) Class.forName(
                    module).newInstance();
        } catch (final Exception e) {
            final MessageBean msgBean=MessageUtils.getInstance().getMessage("DOTJ005F", module);
            final String msg = msgBean.toString();

            throw new DITAOTException(msgBean,e,msg);
        }
    }
    
    /**
     * Create the Module class instance according to moduleName.
     * 
     * @param moduleClass module class
     * @return AbstractPipelineModule
     * @throws DITAOTException DITAOTException
     * @since 1.6
     */
    public AbstractPipelineModule createModule(final Class<? extends AbstractPipelineModule> moduleClass)
            throws DITAOTException {
        try {
            return moduleClass.newInstance();
        } catch (final Exception e) {
            final MessageBean msgBean=MessageUtils.getInstance().getMessage("DOTJ005F", moduleClass.getName());
            final String msg = msgBean.toString();

            throw new DITAOTException(msgBean,e,msg);
        }
    }
}
