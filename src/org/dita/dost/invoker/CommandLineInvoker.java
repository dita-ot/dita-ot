/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.invoker;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
/**
 * Class description goes here.
 * 
 * @version 1.0 2005-5-31
 * @author Zhang, Yuan Peng
 */

public class CommandLineInvoker extends Thread {
    
    public void run(){
        
    }

    public static void main(String[] args){
        String key;
        String value;
        String cmd;
        File propertyFile = new File("temp/property.temp");
        if (!propertyFile.getParentFile().exists())
        {
            propertyFile.getParentFile().mkdirs();
        }
        try{
            FileOutputStream fileOutput = new FileOutputStream(propertyFile);
            OutputStreamWriter output = new OutputStreamWriter(fileOutput, "UTF-8");
            
            HashMap map = new HashMap();
            initMap(map);
            for(int i=0; i < args.length; i++)
            {
                key = args[i].substring(0, args[i].indexOf(':'));
                value = (String)map.get(key.toLowerCase());
                output.write(value + "=" + 
                        args[i].substring(args[i].indexOf(':')+1));
                output.write(System.getProperty("line.separator"));
            }
            output.flush();
            output.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        
        
        Runtime current = Runtime.getRuntime();
        try{
            //specify whether the OS is linux or windows.
            if(System.getProperty("os.name").toLowerCase().indexOf("windows")==-1)
            {//not windows
                cmd = "ant -f conductor.xml -propertyfile temp/property.temp";
            }else{//windows
                cmd = "ant.bat -f conductor.xml -propertyfile temp/property.temp";
            }
            
            Process buildProcess = current.exec(cmd);
            BufferedReader subReader = new BufferedReader(
                    new InputStreamReader(buildProcess.getInputStream()));
            String inLine;
            inLine=subReader.readLine();
            while (inLine != null){
                System.out.println(inLine);
                inLine=subReader.readLine();
            }
            
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    
    private static void initMap(HashMap map)
    {
        map.put("/i", "args.input");
        map.put("/if", "dita.input");
        map.put("/id", "dita.input.dirname");
        map.put("/artlbl", "args.artlbl");
        map.put("/draft", "args.draft");
        map.put("/ftr", "args.ftr");
        map.put("/hdr", "args.hdr");
        map.put("/hdf", "args.hdf");
        map.put("/csspath", "args.csspath");
        map.put("/css", "args.css");
        map.put("/filter", "dita.input.valfile");
        map.put("/ditaext", "dita.extname");
        map.put("/outdir", "output.dir");
        map.put("/transtype", "transtype");
    }
}



