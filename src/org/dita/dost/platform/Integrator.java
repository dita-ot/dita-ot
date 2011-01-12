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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.log.MessageUtils;
import org.dita.dost.util.Constants;
import org.dita.dost.util.FileUtils;
import org.dita.dost.util.StringUtils;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Integrator is the main class to control and excute the 
 * integration of the toolkit and different plug-ins.
 * @author Zhang, Yuan Peng
 */
public class Integrator {
		
	/** Feature name for supported image extensions. */
	public static final String FEAT_IMAGE_EXTENSIONS = "dita.image.extensions";
	public static final String FEAT_VALUE_SEPARATOR = ",";
	
	/**
	 * Plugin table which contains detected plugins.
	 */
	public  Hashtable<String,Features> pluginTable = null;
	private Set<String> templateSet = new HashSet<String>(Constants.INT_16);
	private String ditaDir;
	private String basedir;
	private Set<File> descSet;
	private XMLReader reader;
	private DITAOTJavaLogger logger;
	private Set<String> loadedPlugin = null;
	private Hashtable<String,String> featureTable = null;
    private File propertiesFile = null;
        
    private Properties properties = null;
	
	
	/**
	 * Execute point of Integrator.
	 */
	public void execute() {
		if (!new File(ditaDir).isAbsolute()) {
			ditaDir = new File(basedir, ditaDir).getAbsolutePath();
		}

                // Read the properties file, if it exists.
		        properties = new Properties();
                if (propertiesFile != null) {
                  try {
                    FileInputStream propertiesStream = new FileInputStream(propertiesFile);
                    properties.load(propertiesStream);
                  }
                  catch (Exception e)
                  {
        	     logger.logException(e);
                  }
                }
                else
                {
                  // Set reasonable defaults.
                  properties.setProperty("plugindirs", "plugins;demo");
                  properties.setProperty("plugin.ignores", "");
                }
        
                // Get the list of plugin directories from the properties.
                String[] pluginDirs = properties.getProperty("plugindirs").split(";");
                                
                Set<String> pluginIgnores = new HashSet<String>();
                if (properties.getProperty("plugin.ignores") != null) {
                	pluginIgnores.addAll(Arrays.asList(properties.getProperty("plugin.ignores").split(";")));
                }
                
            	for (final String tmpl: properties.getProperty(Constants.CONF_TEMPLATES, "").split(";")) {
        			final String t = tmpl.trim();
        			if (t.length() != 0) {
        				templateSet.add(t);
        			} 
        		}
                
                for (int j = 0; j < pluginDirs.length; j++)
                {
		  File pluginDir = null;
		  File[] pluginFiles = null;
		  pluginDir = new File(ditaDir + File.separatorChar + pluginDirs[j]);
		  pluginFiles = pluginDir.listFiles();
 
		  for (int i=0; (pluginFiles != null) && (i < pluginFiles.length); i++){
			File f = pluginFiles[i]; 
			File descFile = new File(pluginFiles[i],"plugin.xml");
			if (pluginFiles[i].isDirectory() && !pluginIgnores.contains(f.getName()) && descFile.exists()){
				descSet.add(descFile);
			}
		  }
                }
		
		parsePlugin();
		integrate();
	}

	private void integrate() {
		//Collect information for each feature id and generate a feature table.
		Iterator<String> iter = pluginTable.keySet().iterator();
		Iterator<String> setIter = null;
		File templateFile = null;
		String currentPlugin = null;
		FileGenerator fileGen = new FileGenerator(featureTable);
		while (iter.hasNext()){
			currentPlugin = iter.next();
			loadPlugin (currentPlugin);
		}
		
		//generate the files from template
		setIter = templateSet.iterator();
		while(setIter.hasNext()){
			templateFile = new File(ditaDir,(String) setIter.next());
			fileGen.generate(templateFile.getAbsolutePath());
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
			out = new BufferedOutputStream(new FileOutputStream(
					new File(ditaDir, "lib" + File.separator + Constants.CONF_PROPERTIES)));
			configuration.store(out, "DITA-OT runtime configuration");
		} catch (Exception e) {
			logger.logException(e);
			//throw new RuntimeException("Failed to write configuration properties: " + e.getMessage(), e);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					logger.logException(e);
				}
			}
		}
	}
	// Added on 2010-11-09 for bug 3102827: Allow a way to specify recognized image extensions -- end
	
	//load the plug-ins and aggregate them by feature and fill into feature table
	private boolean loadPlugin (String plugin)
	{
		Set<Map.Entry<String,String>> featureSet = null;
		Iterator<Map.Entry<String,String>> setIter = null;
		Iterator<String> templateIter = null;
		Map.Entry<String,String> currentFeature = null;
		Features pluginFeatures = pluginTable.get(plugin);
		if (checkPlugin(plugin)){
			featureSet = pluginFeatures.getAllFeatures();
			setIter = featureSet.iterator();
			while (setIter.hasNext()){
				currentFeature = setIter.next();
				if(featureTable.containsKey(currentFeature.getKey())){
					String value = (String)featureTable.remove(currentFeature.getKey());
					featureTable.put(currentFeature.getKey(), 
							new StringBuffer(value).append(",").append(currentFeature.getValue()).toString());
				}else{
					featureTable.put(currentFeature.getKey(),currentFeature.getValue());
				}
			}
			
			templateIter = pluginFeatures.getAllTemplates().iterator();
			while (templateIter.hasNext()){
				String templateName = (String) templateIter.next();
				templateSet.add(FileUtils.getRelativePathFromMap(getDitaDir() + File.separator + "dummy", pluginFeatures.getLocation() + File.separator + templateName));
			}
			loadedPlugin.add(plugin);
			return true;
		}else{
			return false;
		}
	}

	//check whether the plugin can be loaded
	private boolean checkPlugin(String currentPlugin) {
		PluginRequirement requirement = null;
		Properties prop = new Properties();		
		Features pluginFeatures = (Features) pluginTable.get(currentPlugin);
		Iterator<PluginRequirement> iter = pluginFeatures.getRequireListIter();
		//check whether dependcy is satisfied
		while (iter.hasNext()){
			boolean anyPluginFound = false;
			requirement = iter.next();
			Iterator<String> requiredPluginIter = requirement.getPlugins();
			while (requiredPluginIter.hasNext()) {
				// Iterate over all alternatives in plugin requirement.
				String requiredPlugin = requiredPluginIter.next();
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
				prop.put("%1",requirement.toString());
				prop.put("%2",currentPlugin);
				logger.logWarn(MessageUtils.getMessage("DOTJ020W",prop).toString());
				return false;
			}
		}		
		return true;
	}

	private void parsePlugin() {
		if(!descSet.isEmpty()){
			Iterator<File> iter = descSet.iterator();
			File descFile = null;
			while(iter.hasNext()){
				descFile = iter.next();
				parseDesc(descFile);
			}
		}
	}

	private void parseDesc(File descFile) {
		try{
			final DescParser parser = new DescParser(descFile.getParent());
			reader.setContentHandler(parser);
			reader.parse(descFile.getAbsolutePath());
			pluginTable.put(parser.getPluginId(), parser.getFeatures());
		}catch(Exception e){
			logger.logException(e);
		}		
	}

	/**
	 * Default Constructor.
	 */
	public Integrator() {
		pluginTable = new Hashtable<String,Features>(Constants.INT_16);
		descSet = new HashSet<File>(Constants.INT_16);
		loadedPlugin = new HashSet<String>(Constants.INT_16);
		featureTable = new Hashtable<String,String>(Constants.INT_16);
		logger = new DITAOTJavaLogger();
		basedir = null;
		ditaDir = null;
		try {
            if (System.getProperty(Constants.SAX_DRIVER_PROPERTY) == null){
                StringUtils.initSaxDriver();
            }
            reader = XMLReaderFactory.createXMLReader();            
        } catch (Exception e) {
        	logger.logException(e);
        }
	}	

	/**
	 * Return the basedir.
	 * @return String
	 */
	public String getBasedir() {
		return basedir;
	}

	/**
	 * Set the basedir.
	 * @param baseDir baseDir
	 */
	public void setBasedir(String baseDir) {
		this.basedir = baseDir;
	}
	
	/**
	 * Return the ditaDir.
	 * @return ditaDir
	 */
	public String getDitaDir() {
		return ditaDir;
	}

	/**
	 * Set the ditaDir.
	 * @param ditadir ditaDir
	 */
	public void setDitaDir(String ditadir) {
		this.ditaDir = ditadir;
	}
	
	/**
	 * Return the properties file.
	 * @return file
	 */
	public File getProperties() {
		return propertiesFile;
	}

	/**
	 * Set the properties file.
	 * @param propertiesfile propertiesfile
	 */
	public void setProperties(File propertiesfile) {
		this.propertiesFile = propertiesfile;
	}
	
	/**
	 * Test function.
	 * @param args args
	 */
	public static void main(String[] args) {
		Integrator abc = new Integrator();
		File currentDir = new File(".");
		String currentPath = currentDir.getAbsolutePath();
		abc.setDitaDir(currentPath.substring(0,currentPath.lastIndexOf(Constants.FILE_SEPARATOR)));
		abc.setProperties(new File("integrator.properties"));
		abc.execute();
	}

}
