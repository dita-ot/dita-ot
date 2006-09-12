/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.module;

import java.util.Properties;

import org.dita.dost.exception.DITAOTException;
import org.dita.dost.log.MessageUtils;

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
	 * Method to get the only instance of ModuleFactory. ModuleFactory is a
	 * singleton class.
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
	 * @throws DITAOTException
	 */
	public AbstractPipelineModule createModule(String moduleName)
			throws DITAOTException {
		String module = packagePrefix + moduleName + "Module";
		
		try {
			return (AbstractPipelineModule) Class.forName(
					module).newInstance();
		} catch (Exception e) {
			String msg = null;
			Properties params = new Properties();

			params.put("%1", module);
			msg = MessageUtils.getMessage("DOTJ005F", params).toString();

			throw new DITAOTException(msg, e);
		}
	}
}
