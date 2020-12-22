/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2013 Jarno Elovirta
 *
 * See the accompanying LICENSE file for applicable license.
 */
/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.dita.dost.invoker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.tools.ant.launch.AntMain;

/**
 * Command line entry point into DITA-OT. This class is entered via the canonical
 * `public static void main` entry point and reads the command line arguments.
 * It then assembles and executes an Ant project.
 */
public class ProxyMain extends org.apache.tools.ant.Main implements AntMain {

    /**
     * Start Ant
     *
     * @param args command line args
     * @param additionalUserProperties properties to set beyond those that may
     *            be specified on the args list
     * @param coreLoader - not used
     *
     * @since Ant 1.6
     */
    @Override
    public void startAnt(final String[] args, final Properties additionalUserProperties, final ClassLoader coreLoader) {
    	ClassLoader cl = getClassLoaderWithExpandPaths(Thread.currentThread().getContextClassLoader());
    	    Thread.currentThread().setContextClassLoader(cl);
    	    try {
    	      Class<?> mainClass = cl.loadClass("org.dita.dost.invoker.Main");
    	      Method method = mainClass.getMethod("startAnt", String[].class, Properties.class, ClassLoader.class);
    	      method.invoke(mainClass.newInstance(), args, null, null);
    	    } catch (Exception ex) {
    	      throw new IllegalStateException(ex);
    	    }
    }
    
    private ClassLoader getClassLoaderWithExpandPaths(ClassLoader ccl) {
    	List<URL> expandedURLs = new ArrayList<>();
    	File envFile = new File(System.getProperty("dita.dir")+File.separator+"config", "env.bat");
    	try {
    		BufferedReader reader = new BufferedReader(new FileReader(envFile));
    		String relJars = null;
    		
    		while ((relJars = reader.readLine()) != null) {
    			relJars = relJars.substring(relJars.indexOf("plugins"),relJars.lastIndexOf("\""));
    			File libFile = new File(System.getProperty("dita.dir")+File.separator+relJars);
    			expandedURLs.add(libFile.toURI().toURL());
    		}
    		
    		reader.close();
    	} catch (IOException e1) {
    		throw new IllegalStateException(e1);
    	}
    	
    	URL[] urls = ((URLClassLoader) ccl).getURLs();
    	for (URL url : urls) {
    		expandedURLs.add(url);
    	}
    	
    	ClassLoader cl = new URLClassLoader(expandedURLs.toArray(new URL[0]), ccl.getParent());
  
    	return cl;
    }
    
}
