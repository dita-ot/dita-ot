/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2007 All Rights Reserved.
 */
package org.dita.dost.util;

import static org.dita.dost.util.Constants.*;
import static org.dita.dost.log.MessageBean.*;

/**
 * LogUtils : To anylyse the information from dita & xslt ,supply
 * fatal , error , warn , info to the DITAOTBuildLogger,
 * which to complement the ant's shortage.
 * 
 * Only intended to be used with {@link org.dita.dost.log.DITAOTBuildLogger}.
 * 
 * @author wxzhang
 * @see org.dita.dost.log.DITAOTBuildLogger
 */
public final class LogUtils {
    private static int numOfFatals=0;
    private static int numOfErrors=0;
    private static int numOfWarnings=0;
    private static int numOfInfo=0;

    private LogUtils(){
    }
    /**
     * Increase fatal number by 1.
     */
    public static void increaseNumOfFatals(){
        numOfFatals++;
    }
    /**
     * Increase error number by 1.
     */
    public static void increaseNumOfErrors(){
        numOfErrors++;
    }
    /**
     * Increase warning number by 1.
     */
    public static void increaseNumOfWarnings(){
        numOfWarnings++;
    }
    /**
     * Increase info number by 1.
     */
    public static void increaseNumOfInfo(){
        numOfInfo++;
    }
    /**
     * Get fatals number.
     * @return number of fatals
     */
    public static int getNumOfFatals(){
        return numOfFatals;
    }
    /**
     * Get errors number.
     * @return number of errors
     */
    public static int getNumOfErrors(){
        return numOfErrors;
    }
    /**
     * Get warnings number.
     * @return number of warnings
     */
    public static int getNumOfWarnings(){
        return numOfWarnings;
    }
    /**
     * Get info number.
     * @return number of info
     */
    public static int getNumOfInfo(){
        return numOfInfo;
    }

    /**
     * Initial the class.
     */
    public static void clear(){
        numOfFatals=0;
        numOfErrors=0;
        numOfWarnings=0;
        numOfInfo=0;
    }

    /**
     * Check whether error exists in the whole transforming process.
     * @return boolean
     */
    public  static boolean haveFatalOrError(){
        return numOfFatals > 0 || numOfErrors > 0;
    }

    /**
     * print the statics message.
     */
    public static void print(){
        System.out.println("Number of Fatals : " + numOfFatals );
        System.out.println("Number of Errors : " + numOfErrors );
        System.out.println("Number of Warnings : " + numOfWarnings );
        System.out.println("Number of Info : " + numOfInfo );
    }

    /**
     * Get the statics message.
     * @return String message
     */
    public static String getLogStatisticInfo(){
        String logStaticticInfo;
        logStaticticInfo="Number of Fatals : " + getNumOfFatals() +LINE_SEPARATOR;
        logStaticticInfo=logStaticticInfo+"Number of Errors : " + getNumOfErrors() +LINE_SEPARATOR;
        logStaticticInfo=logStaticticInfo+"Number of Warnings : " + getNumOfWarnings() +LINE_SEPARATOR;
        return logStaticticInfo;
    }

    /**
     * Increase the number of Exceptions by severity level.
     * @param msgType message type:error warn info
     */
    public static void increaseNumOfExceptionByType(final String msgType){

        if (msgType==null){
            increaseNumOfErrors();
            return;
        }

        final String type=msgType.toUpperCase();

        if(FATAL.equals(type)){
            LogUtils.increaseNumOfFatals();
            return;
        }
        if(ERROR.equals(type)){
            increaseNumOfErrors();
            return;
        }
        if(WARN.equals(type)){
            increaseNumOfWarnings();
            return;
        }
        if(INFO.equals(type)){
            increaseNumOfInfo();
            return;
        }
        //TODO
        increaseNumOfErrors();

    }
}
