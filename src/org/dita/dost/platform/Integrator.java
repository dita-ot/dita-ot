/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2005, 2006 All Rights Reserved.
 */
package org.dita.dost.platform;

import java.io.File;
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
	/**
	 * Plugin table which contains detected plugins
	 */
	public static Hashtable<String,Features> pluginTable = null;
	private Set<String> templateSet = null;
	private String ditaDir;
	private String basedir;
	private Set<File> descSet;
	private XMLReader reader;
	private DITAOTJavaLogger logger;
	private Set<String> loadedPlugin = null;
	private Hashtable<String,String> featureTable = null;
	
	private void initTemplateSet(){
		templateSet = new HashSet<String>(Constants.INT_16);
		templateSet.add("catalog-dita_template.xml");
		templateSet.add("build_template.xml");
		templateSet.add("build_general_template.xml");
		templateSet.add("build_dita2eclipsehelp_template.xml");
		templateSet.add("build_preprocess_template.xml");
		templateSet.add("xsl/common/allstrings_template.xml");
		templateSet.add("xsl/dita2xhtml_template.xsl");
		templateSet.add("xsl/dita2rtf_template.xsl");
		templateSet.add("xsl/dita2fo-shell_template.xsl");
		templateSet.add("xsl/dita2docbook_template.xsl");
		templateSet.add("xsl/preprocess/maplink_template.xsl");
		templateSet.add("xsl/preprocess/mapref_template.xsl");
		templateSet.add("xsl/preprocess/mappull_template.xsl");
		templateSet.add("xsl/map2plugin_template.xsl");
		templateSet.add("xsl/preprocess/conref_template.xsl");
		templateSet.add("xsl/preprocess/topicpull_template.xsl");
	}

	/**
	 * execute point of Integrator
	 */
	public void execute() {
		File demoDir = null;
		File pluginDir = null;
		File[] demoFiles = null;
		File[] pluginFiles = null;
		if (!new File(ditaDir).isAbsolute()) {
			ditaDir = new File(basedir, ditaDir).getAbsolutePath();
		}
		
		demoDir = new File(ditaDir + File.separatorChar + "demo");
		pluginDir = new File(ditaDir + File.separatorChar + "plugins");
		demoFiles = demoDir.listFiles();
		pluginFiles = pluginDir.listFiles();
		
		for (int i=0; (demoFiles != null) && (i < demoFiles.length); i++){
			File descFile = new File(demoFiles[i],"plugin.xml");
			if (demoFiles[i].isDirectory() && descFile.exists()){
				descSet.add(descFile);
			}
		}
		
		for (int i=0; (pluginFiles != null) && (i < pluginFiles.length); i++){
			File descFile = new File(pluginFiles[i],"plugin.xml");
			if (pluginFiles[i].isDirectory() && descFile.exists()){
				descSet.add(descFile);
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
	}
	
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
			reader.setContentHandler(new DescParser(descFile.getParent()));
			reader.parse(descFile.getAbsolutePath());
		}catch(Exception e){
			logger.logException(e);
		}		
	}

	/**
	 * Default Constructor
	 */
	public Integrator() {
		initTemplateSet();
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
	 * Return the basedir
	 * @return
	 */
	public String getBasedir() {
		return basedir;
	}

	/**
	 * Set the basedir
	 * @param baseDir
	 */
	public void setBasedir(String baseDir) {
		this.basedir = baseDir;
	}
	
	/**
	 * Return the ditaDir
	 * @return
	 */
	public String getDitaDir() {
		return ditaDir;
	}

	/**
	 * Set the ditaDir
	 * @param ditadir
	 */
	public void setDitaDir(String ditadir) {
		this.ditaDir = ditadir;
	}
	
	/**
	 * Test function
	 * @param args
	 */
	public static void main(String[] args) {
		Integrator abc = new Integrator();
		File currentDir = new File(".");
		String currentPath = currentDir.getAbsolutePath();
		abc.setDitaDir(currentPath.substring(0,currentPath.lastIndexOf(Constants.FILE_SEPARATOR)));
		abc.execute();
	}

}
