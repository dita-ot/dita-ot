/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2004, 2005 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.module;

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

    /**
     * Automatically generated constructor: ModuleFactory.
     */
    private ModuleFactory() {

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
     * Create the ModuleElem class instance according to moduleName.
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
            final MessageBean msgBean = MessageUtils.getMessage("DOTJ005F", moduleClass.getName());
            final String msg = msgBean.toString();

            throw new DITAOTException(msgBean, e,msg);
        }
    }
}
