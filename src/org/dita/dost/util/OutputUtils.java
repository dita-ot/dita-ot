/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved.
 */
package org.dita.dost.util;

/**
 * OutputUtils to control the output behavior.
 * @author wxzhang
 *
 */
public final class OutputUtils {

    public enum OutterControl {
        /** Fail behavior. */
        FAIL,
        /** Warn behavior. */
        WARN,
        /** Quiet behavior. */
        QUIET
    }

    public enum Generate {
        /** Not generate outer files. */
        NOT_GENERATEOUTTER(1),
        /** Generate outer files. */
        GENERATEOUTTER(2),
        /** Old solution. */
        OLDSOLUTION(3);

        public final int type;

        Generate(final int type) {
            this.type = type;
        }

        public static Generate get(final int type) {
            for (final Generate g: Generate.values()) {
                if (g.type == type) {
                    return g;
                }
            }
            throw new IllegalArgumentException();
        }
    }

    private static Generate generatecopyouter = Generate.NOT_GENERATEOUTTER;//default:only generate&copy the non-overflowing files
    private boolean onlytopicinmap=false;//default:only the topic files will be resolved in the map
    private OutterControl outercontrol = OutterControl.WARN;
    /**Output Dir.*/
    private static String OutputDir=null;
    /**Input Map Dir.*/
    private String InputMapDir=null;

    /**
     * Retrieve the outercontrol.
     * @return String outercontrol behavior
     *
     */
    public OutterControl getOutterControl(){
        return outercontrol;
    }

    /**
     * Set the outercontrol.
     * @param control control
     */
    public void setOutterControl(final String control){
        outercontrol = OutterControl.valueOf(control.toUpperCase());
    }

    /**
     * Retrieve the flag of onlytopicinmap.
     * @return boolean if only topic in map
     */
    public boolean getOnlyTopicInMap(){
        return onlytopicinmap;
    }

    /**
     * Set the onlytopicinmap.
     * @param flag onlytopicinmap flag
     */
    public void setOnlyTopicInMap(final String flag){
        if("true".equalsIgnoreCase(flag)){
            onlytopicinmap=true;
        }else{
            onlytopicinmap=false;
        }
    }

    /**
     * Retrieve the flag of generatecopyouter.
     * @return int generatecopyouter flag
     */
    public static Generate getGeneratecopyouter(){
        return generatecopyouter;
    }

    /**
     * Set the generatecopyouter.
     * @param flag generatecopyouter flag
     */
    public void setGeneratecopyouter(final String flag){
        generatecopyouter = Generate.get(Integer.parseInt(flag));
    }

    /**
     * Get output dir.
     * @return output dir
     */
    public static String getOutputDir(){
        return OutputDir;
    }
    /**
     * Set output dir.
     * @param outputDir output dir
     */
    public void setOutputDir(final String outputDir){
        OutputDir=outputDir;
    }
    /**
     * Get input map path.
     * @return input map path
     */
    public String getInputMapPathName(){
        return InputMapDir;
    }
    /**
     * Set input map path.
     * @param inputMapDir input map path
     */
    public void setInputMapPathName(final String inputMapDir){
        InputMapDir=inputMapDir;
    }
}
