/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.module;

/**
 * The factory to create instance for each module class.
 * 
 * @author Lian, Li
 * 
 */
public class ModuleFactory {
    private static ModuleFactory moduleFactory = null;
    private final String packagePrefix = "org.dita.dost.module.";


	/**
	* Automatically generated constructor: ModuleFactory
	*/
    public ModuleFactory() {

    }

    /**
     * Method to get the only instance of ModuleFactory. ModuleFactory is a singleton
     * class.
     * 
     * @return ModuleFactory
     */
    public static ModuleFactory instance() {
        if (moduleFactory == null) {
            moduleFactory = new ModuleFactory();
        }
        return moduleFactory;
    }

    /**
     * Create the Module class instance according to moduleName.
     * 
     * @param moduleName
     * @return AbstractPipelineModule
     */
    public AbstractPipelineModule createModule(String moduleName) {
        AbstractPipelineModule module = null;

        String moduleClassName = moduleName + "Module";

        try {
            module = (AbstractPipelineModule) Class.forName(
                    packagePrefix + moduleClassName).newInstance();

        } catch (ClassNotFoundException e) {
            System.err.println("PCM not found");
            e.printStackTrace();
            module = null;
        } catch (InstantiationException e) {
            System.err.println("InstantiationException");
            e.printStackTrace();
            module = null;
        } catch (IllegalAccessException e) {
            System.err.println("IllegalAccessException");
            e.printStackTrace();
            module = null;
        }

        return module;
    }
}
