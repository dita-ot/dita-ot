/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2005, 2006 All Rights Reserved.
 */
package org.dita.dost.platform;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.log.DITAOTLogger;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.util.Constants;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.StringUtils;
import org.xml.sax.XMLReader;

/**
 * Integrator is the main class to control and excute the 
 * integration of the toolkit and different plug-ins.
 * @author Zhang, Yuan Peng
 */
public final class Integrator {
		
	/** Feature name for supported image extensions. */
	public static final String FEAT_IMAGE_EXTENSIONS = "dita.image.extensions";
	public static final String FEAT_VALUE_SEPARATOR = ",";
	public static final String PARAM_VALUE_SEPARATOR = ";";
	public static final String PARAM_NAME_SEPARATOR = "=";
	
	/**
	 * Plugin table which contains detected plugins.
	 */
	private final Map<String,Features> pluginTable;
	private final Set<String> templateSet = new HashSet<String>(Constants.INT_16);
	private File ditaDir;
	private File basedir;
	/** Plugin configuration file. */
	private final Set<File> descSet;
	private final XMLReader reader;
	private DITAOTLogger logger;
	private final Set<String> loadedPlugin;
	private final Hashtable<String,String> featureTable;
    private File propertiesFile;
        
    private Properties properties;
	
	
	/**
	 * Execute point of Integrator.
	 */
	public void execute() {
	    if (logger == null) {
	        logger = new DITAOTJavaLogger();
	    }
		if (!ditaDir.isAbsolute()) {
			ditaDir = new File(basedir, ditaDir.getPath());
		}

                // Read the properties file, if it exists.
		        properties = new Properties();
                if (propertiesFile != null) {
                  FileInputStream propertiesStream = null;
                  try {
                    propertiesStream = new FileInputStream(propertiesFile);
                    properties.load(propertiesStream);
                  }
                  catch (final Exception e)
                  {
        	     logger.logException(e);
                  } finally {
                	  if (propertiesStream != null) {
                		  try {
                			  propertiesStream.close();
                		  } catch (IOException e) {
                			  logger.logException(e);
                		  }
                	  }
                  }
                }
                else
                {
                  // Set reasonable defaults.
                  properties.setProperty("plugindirs", "plugins;demo");
                  properties.setProperty("plugin.ignores", "");
                }
        
                // Get the list of plugin directories from the properties.
                final String[] pluginDirs = properties.getProperty("plugindirs").split(PARAM_VALUE_SEPARATOR);
                                
                final Set<String> pluginIgnores = new HashSet<String>();
                if (properties.getProperty("plugin.ignores") != null) {
                	pluginIgnores.addAll(Arrays.asList(properties.getProperty("plugin.ignores").split(PARAM_VALUE_SEPARATOR)));
                }
                
            	for (final String tmpl: properties.getProperty(Constants.CONF_TEMPLATES, "").split(PARAM_VALUE_SEPARATOR)) {
        			final String t = tmpl.trim();
        			if (t.length() != 0) {
        				templateSet.add(t);
        			} 
        		}
                
                for (final String pluginDir2 : pluginDirs) {
		  final File pluginDir = new File(ditaDir, pluginDir2);
		  final File[] pluginFiles = pluginDir.listFiles();
 
		  for (int i=0; (pluginFiles != null) && (i < pluginFiles.length); i++){
			final File f = pluginFiles[i]; 
			final File descFile = new File(pluginFiles[i],"plugin.xml");
			if (pluginFiles[i].isDirectory() && !pluginIgnores.contains(f.getName()) && descFile.exists()){
				descSet.add(descFile);
			}
		  }
                }
		
		parsePlugin();
		integrate();
	}

	/**
	 * Generate and process plugin files.
	 */
	private void integrate() {
		//Collect information for each feature id and generate a feature table.
		final FileGenerator fileGen = new FileGenerator(featureTable, pluginTable);
		fileGen.setLogger(logger);
		for (final String currentPlugin: pluginTable.keySet()) {
			loadPlugin (currentPlugin);
		}
		
		//generate the files from template
		for (final String template: templateSet) {
			final File templateFile = new File(ditaDir, template);
			logger.logDebug("Process template " + templateFile.getPath());
			fileGen.generate(templateFile);
		}
		
		// Added on 2010-11-09 for bug 3102827: Allow a way to specify recognized image extensions -- start
		// generate configuration properties
		final Properties configuration = new Properties();
		//image extensions
		final Set<String> imgExts = new HashSet<String>();
		
		for (final String ext: properties.getProperty(Constants.CONF_SUPPORTED_IMAGE_EXTENSIONS, "").split(Constants.CONF_LIST_SEPARATOR)) {
			final String e = ext.trim();
			if (e.length() != 0) {
				imgExts.add(e);
			} 
		}
		if (featureTable.containsKey(FEAT_IMAGE_EXTENSIONS)) {
			for (final String ext: featureTable.get(FEAT_IMAGE_EXTENSIONS).split(FEAT_VALUE_SEPARATOR)) {
				final String e = ext.trim();
				if (e.length() != 0) {
					imgExts.add(e);
				}
			}
		}
		configuration.put(Constants.CONF_SUPPORTED_IMAGE_EXTENSIONS, StringUtils.assembleString(imgExts, Constants.CONF_LIST_SEPARATOR));
		OutputStream out = null;
		try {
		    final File outFile = new File(ditaDir, "lib" + File.separator + Constants.CONF_PROPERTIES);
		    logger.logDebug("Generate configuration properties " + outFile.getPath());
			out = new BufferedOutputStream(new FileOutputStream(outFile));
			configuration.store(out, "DITA-OT runtime configuration");
		} catch (final Exception e) {
			logger.logException(e);
			//throw new RuntimeException("Failed to write configuration properties: " + e.getMessage(), e);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (final IOException e) {
					logger.logException(e);
				}
			}
		}
	}
	// Added on 2010-11-09 for bug 3102827: Allow a way to specify recognized image extensions -- end
	
	/**
	 * Load the plug-ins and aggregate them by feature and fill into feature table.
	 * 
	 * @param plugin plugin ID
	 * @return <code>true</code> if plugin was loaded, otherwise <code>false</code>
	 */
	private boolean loadPlugin (final String plugin)
	{
		if (checkPlugin(plugin)){
			final Features pluginFeatures = pluginTable.get(plugin);
			final Set<Map.Entry<String,String>> featureSet = pluginFeatures.getAllFeatures();
			for (final Map.Entry<String,String> currentFeature: featureSet) {
				if(featureTable.containsKey(currentFeature.getKey())){
					final String value = featureTable.remove(currentFeature.getKey());
					featureTable.put(currentFeature.getKey(), 
							new StringBuffer(value).append(FEAT_VALUE_SEPARATOR).append(currentFeature.getValue()).toString());
				}else{
					featureTable.put(currentFeature.getKey(),currentFeature.getValue());
				}
			}
			
			for (final String templateName: pluginFeatures.getAllTemplates()) {
				templateSet.add(FileUtils.getRelativePathFromMap(getDitaDir() + File.separator + "dummy", pluginFeatures.getLocation() + File.separator + templateName));
			}
			loadedPlugin.add(plugin);
			return true;
		}else{
			return false;
		}
	}

	/**
	 * Check whether the plugin can be loaded.
	 * 
	 * @param currentPlugin plugin ID
	 * @return <code>true</code> if plugin can be loaded, otherwise <code>false</code>
	 */
	private boolean checkPlugin(final String currentPlugin) {
		final Features pluginFeatures = pluginTable.get(currentPlugin);
		final Iterator<PluginRequirement> iter = pluginFeatures.getRequireListIter();
		//check whether dependcy is satisfied
		while (iter.hasNext()){
			boolean anyPluginFound = false;
			final PluginRequirement requirement = iter.next();
			final Iterator<String> requiredPluginIter = requirement.getPlugins();
			while (requiredPluginIter.hasNext()) {
				// Iterate over all alternatives in plugin requirement.
				final String requiredPlugin = requiredPluginIter.next();
				if(pluginTable.containsKey(requiredPlugin)){
					if (!loadedPlugin.contains(requiredPlugin)){
						//required plug-in is not loaded
						loadPlugin(requiredPlugin);
					}
					// As soon as any plugin is found, it's OK.
					anyPluginFound = true;
				}
			}
			if (!anyPluginFound && requirement.getRequired()) {
				//not contain any plugin required by current plugin
			    final Properties prop = new Properties();
				prop.put("%1",requirement.toString());
				prop.put("%2",currentPlugin);
				logger.logWarn(MessageUtils.getMessage("DOTJ020W",prop).toString());
				return false;
			}
		}		
		return true;
	}

	/**
	 * Parse plugin configuration files.
	 */
	private void parsePlugin() {
		if(!descSet.isEmpty()){
			for (final File descFile: descSet) {
			    logger.logDebug("Read plugin configuration " + descFile.getPath());
				parseDesc(descFile);
			}
		}
	}

	/**
	 * Parse plugin configuration file
	 * @param descFile plugin configuration
	 */
	private void parseDesc(final File descFile) {
		try{
			final DescParser parser = new DescParser(descFile.getParentFile(), ditaDir);
			reader.setContentHandler(parser);
			reader.parse(descFile.getAbsolutePath());
			pluginTable.put(parser.getPluginId(), parser.getFeatures());
		}catch(final Exception e){
			logger.logException(e);
		}		
	}

	/**
	 * Default Constructor.
	 */
	public Integrator() {
		pluginTable = new HashMap<String,Features>(Constants.INT_16);
		descSet = new HashSet<File>(Constants.INT_16);
		loadedPlugin = new HashSet<String>(Constants.INT_16);
		featureTable = new Hashtable<String,String>(Constants.INT_16);
		try {
            reader = StringUtils.getXMLReader();
        } catch (final Exception e) {
        	throw new RuntimeException("Failed to initialize XML parser: " + e.getMessage(), e);
        }
	}	

	/**
	 * Return the basedir.
	 * @return base directory
	 */
	public File getBasedir() {
		return basedir;
	}

	/**
	 * Set the basedir.
	 * @param baseDir base directory
	 */
	public void setBasedir(final File baseDir) {
		this.basedir = baseDir;
	}
	
	/**
	 * Return the ditaDir.
	 * @return dita directory
	 */
	public File getDitaDir() {
		return ditaDir;
	}

	/**
	 * Set the ditaDir.
	 * @param ditadir dita directory
	 */
	public void setDitaDir(final File ditadir) {
		this.ditaDir = ditadir;
	}
	
	/**
	 * Return the properties file.
	 * @return properties file
	 */
	public File getProperties() {
		return propertiesFile;
	}

	/**
	 * Set the properties file.
	 * @param propertiesfile properties file
	 */
	public void setProperties(final File propertiesfile) {
		this.propertiesFile = propertiesfile;
	}
	
	/**
	 * Set logger.
	 * @param logger logger instance
	 */
	public void setLogger(final DITAOTLogger logger) {
	    this.logger = logger;
	}
	
	/**
	 * Get all and combine extension values
	 * 
	 * @param featureTable plugin features
	 * @param extension extension ID
	 * @return combined extension value, {@code null} if no value available
	 */
	static final String getValue(final Map<String, Features> featureTable, final String extension) {
        final List<String> buf = new ArrayList<String>();
        for (final Features f: featureTable.values()) {
            final String v = f.getFeature(extension);
            if (v != null) {
                buf.add(v);
            }
        }
        if (buf.isEmpty()) {
            return null;
        } else {
            return StringUtils.assembleString(buf, ",");
        }
	}
	
	/**
	 * Command line interface for testing.
	 * @param args arguments
	 */
	public static void main(final String[] args) {
		final Integrator abc = new Integrator();
		final File currentDir = new File(".");
		abc.setDitaDir(currentDir);
		abc.setProperties(new File("integrator.properties"));
		abc.execute();
	}

}
