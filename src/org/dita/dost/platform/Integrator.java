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
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 *
 * @author Zhang, Yuan Peng
 */
public class Integrator {
	public static Hashtable pluginTable = null;
	private static HashSet templateSet = null;
	private String ditaDir;
	private HashSet descSet;
	private XMLReader reader;
	private DITAOTJavaLogger logger;
	
	static{
		templateSet = new HashSet(8);
		templateSet.add("catalog-dita_template.xml");
		templateSet.add("xsl/dita2xhtml_template.xsl");
		templateSet.add("conductor_template.xml");
		templateSet.add("xsl/dita2rtf_template.xsl");
		templateSet.add("xsl/dita2fo-shell_template.xsl");
		templateSet.add("xsl/dita2docbook_template.xsl");
	}

	public void execute() {
		// TODO Auto-generated method stub
		File demoDir = new File(ditaDir + "/demo");
		File pluginDir = new File(ditaDir + "/plugins");
		File[] demoFiles = demoDir.listFiles();
		File[] pluginFiles = pluginDir.listFiles();
		
		for (int i=0; i < demoFiles.length; i++){
			File descFile = new File(demoFiles[i],"plugin.xml");
			if (demoFiles[i].isDirectory() && descFile.exists()){
				descSet.add(descFile);
			}
		}
		for (int i=0; i < pluginFiles.length; i++){
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
		Hashtable featureTable = new Hashtable(16);
		Iterator iter = pluginTable.entrySet().iterator();
		Iterator setIter = null;
		Map.Entry currentFeature = null;
		Set featureSet = null;
		File templateFile = null;
		Features pluginFeatures = null;
		Map.Entry currentPlugin = null;
		FileGenerator fileGen = new FileGenerator(featureTable);
		while (iter.hasNext()){
			currentPlugin = (Map.Entry)iter.next();
			pluginFeatures = (Features)currentPlugin.getValue();
			if (checkPlugin((String)currentPlugin.getKey(), pluginFeatures)){
				featureSet = pluginFeatures.getAllFeatures();
				setIter = featureSet.iterator();
				while (setIter.hasNext()){
					currentFeature = (Map.Entry)setIter.next();
					if(featureTable.containsKey(currentFeature.getKey())){
						String value = (String)featureTable.remove(currentFeature.getKey());
						featureTable.put(currentFeature.getKey(), value+"," + currentFeature.getValue());
					}else{
						featureTable.put(currentFeature.getKey(),currentFeature.getValue());
					}
				}
			}
		}
		
		//generate the files from template
		setIter = templateSet.iterator();
		while(setIter.hasNext()){
			templateFile = new File(ditaDir,(String) setIter.next());
			fileGen.generate(templateFile.getAbsolutePath());
		}
	}

	//check whether the plugin can be loaded
	private boolean checkPlugin(String currentPlugin, Features pluginFeatures) {
		String requiredPlugin = null;
		Properties prop = new Properties();
		Iterator iter = pluginFeatures.getRequireListIter();
		//check whether dependcy is satisfied
		while (iter.hasNext()){
			requiredPlugin = (String)iter.next();
			if(!(pluginTable.containsKey(requiredPlugin))){
				//not contain the plugin required by current plugin
				prop.put("%1",requiredPlugin);
				prop.put("%2",currentPlugin);
				logger.logWarn(MessageUtils.getMessage("DOTJ020W",prop).toString());
				return false;
			}
		}		
		return true;
	}

	private void parsePlugin() {
		if(!descSet.isEmpty()){
			Iterator iter = descSet.iterator();
			File descFile = null;
			while(iter.hasNext()){
				descFile = (File) iter.next();
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

	public Integrator() {
		pluginTable = new Hashtable(16);
		descSet = new HashSet(16);
		logger = new DITAOTJavaLogger();
		try {
            if (System.getProperty(Constants.SAX_DRIVER_PROPERTY) == null){
                //The default sax driver is set to xerces's sax driver
            	if(System.getProperty("java.vendor").toLowerCase().indexOf("sun")==-1){
                System.setProperty(Constants.SAX_DRIVER_PROPERTY,Constants.SAX_DRIVER_DEFAULT_CLASS);
            	}else{
            		if(System.getProperty("java.version").startsWith("1.5")){
            			System.setProperty(Constants.SAX_DRIVER_PROPERTY,"com.sun.org.apache.xerces.internal.parsers.SAXParser");
            		}else{
            			System.setProperty(Constants.SAX_DRIVER_PROPERTY,"org.apache.crimson.parser.XMLReaderImpl");
            		}
            	}
            }
            reader = XMLReaderFactory.createXMLReader();            
        } catch (Exception e) {
        	logger.logException(e);
        }
	}

	public String getDitaDir() {
		return ditaDir;
	}

	public void setDitaDir(String ditaDir) {
		this.ditaDir = ditaDir;
	}
	
	public static void main(String[] args) {
		Integrator abc = new Integrator();
		File currentDir = new File(".");
		String currentPath = currentDir.getAbsolutePath();
		abc.setDitaDir(currentPath.substring(0,currentPath.lastIndexOf(Constants.FILE_SEPARATOR)));
		abc.execute();
	}

}
