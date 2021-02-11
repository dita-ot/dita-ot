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

import org.apache.tools.ant.launch.AntMain;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static java.lang.System.getProperty;
import static java.lang.Thread.currentThread;
import static java.nio.file.Paths.get;
import static java.util.Arrays.asList;

/**
 * Command line entry point into DITA-OT. This class is entered via the canonical
 * `public static void main` entry point and reads the command line arguments.
 * It then assembles and executes an Ant project.
 */
public class ProxyMain extends org.apache.tools.ant.Main implements AntMain {

    @Override
    public void startAnt(final String[] args, final Properties additionalUserProperties, final ClassLoader coreLoader) {
        try {
            ClassLoader classLoader = getClassLoaderWithExpandPaths(currentThread().getContextClassLoader());
            currentThread().setContextClassLoader(classLoader);
            Class<?> mainClass = classLoader.loadClass("org.dita.dost.invoker.Main");
            Method startAnt = mainClass.getMethod("startAnt", String[].class, Properties.class, ClassLoader.class);
            startAnt.invoke(mainClass.newInstance(), args, additionalUserProperties, classLoader);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    private ClassLoader getClassLoaderWithExpandPaths(ClassLoader ccl) throws IOException {
        List<URL> expandedURLs = new ArrayList<>();
        expandedURLs.addAll(getUrlsFromEnv());
        expandedURLs.addAll(asList(((URLClassLoader) ccl).getURLs()));
        return new URLClassLoader(expandedURLs.toArray(new URL[0]), ccl.getParent());
    }

    private List<URL> getUrlsFromEnv() throws IOException {
        Path env = get(getProperty("dita.dir"), "config", "env.bat");
        return Files.lines(env).map(line -> {
            String filePath = line.substring(line.indexOf("plugins"), line.lastIndexOf("\""));
            return toUrl(get(getProperty("dita.dir"), filePath).toUri());
        }).collect(Collectors.toList());
    }

    private URL toUrl(URI uri) {
        try {
            return uri.toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

}
