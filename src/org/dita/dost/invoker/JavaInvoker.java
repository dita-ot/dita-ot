/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.invoker;

import java.io.File;

import org.dita.dost.log.DITAOTJavaLogger;


/**
 * Invoke the process in Java and use java code to control building process.
 * 
 * @author Lian, Li
 * 
 */
@Deprecated
public final class JavaInvoker {
    private static DITAOTJavaLogger javaLogger = new DITAOTJavaLogger();

    /**
     * Remove all files in certain directory
     * @param dir
     * @author Marshall
     */
    public static void removeFiles(final String dir){
        final File file = new File(dir);
        final int size = file.listFiles().length;
        if(!(file.exists() && file.isDirectory())){
            return;
        }
        for(int i=0; i< size; i++){
            final File f = file.listFiles()[i];
            f.deleteOnExit();
        }
    }

    /**
     * Automatically generated constructor for utility class
     */
    private JavaInvoker() {
    }

}