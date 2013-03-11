/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/**
 * (c) Copyright IBM Corp. 2006 All Rights Reserved.
 */
package org.dita.dost.util;

import static org.dita.dost.util.Constants.INT_1024;
import static org.dita.dost.util.Constants.INT_16;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.log.MessageUtils;

/**
 * Image utility to get the width, height, type and binary data from
 * specific image file.
 * 
 * @author Zhang, Yuan Peng
 *
 */
public final class ImgUtils {
	
    /**
     * Private default constructor to make class uninstantiable.
     */
    private ImgUtils(){
    }
    
    private static String getImageOutPutPath(final String fileName) {
    	final DITAOTJavaLogger logger = new DITAOTJavaLogger();
		String uplevelPath = null;
		final File outDir = OutputUtils.getOutputDir();
		final String filename = FileUtils.separatorsToUnix(fileName);

		File imgoutDir = outDir;
		if (OutputUtils.getGeneratecopyouter() != OutputUtils.Generate.OLDSOLUTION) {
			Properties propterties = null;
			try {
				propterties = ListUtils.getDitaList();
				uplevelPath = propterties.getProperty("uplevels");
				if (uplevelPath != null&&uplevelPath.length()>0){
					imgoutDir = new File(outDir, uplevelPath);
				}

			} catch (final IOException e) {
				throw new RuntimeException("Reading list file failed: "
						+ e.getMessage(), e);
			}
		}
		String imagePath = null;
		try {
			imagePath =new File(imgoutDir, filename).getCanonicalPath();
		} catch (final IOException e) {
			logger.logError(e.getMessage(), e) ;
		}			
		return imagePath;
	}
    
    private static boolean checkDirName(final String dirName) {
		final File outDir = OutputUtils.getOutputDir();
		if (outDir != null) {
			final String o = FileUtils.separatorsToUnix(outDir.getAbsolutePath());

			if (FileUtils.separatorsToUnix(new File(dirName).getPath()).equalsIgnoreCase(o)) {
				return true;
			}
		}
		return false;
	}
    /**
     * Get the image width.
     * @param dirName -
     * 				The directory name that will be added to the path
     * 				of the image file.
     * @param fileName -
     * 				The file name of the image file.
     * @return int -
     * 				The width of the picture in pixels.
     */
    public static int getWidth (final String dirName, final String fileName){
        final DITAOTJavaLogger logger = new DITAOTJavaLogger();
		File imgInput = new File(dirName + File.separatorChar + fileName);		
		if (checkDirName(dirName)) {
            imgInput = new File(getImageOutPutPath(fileName));
        }    
        try {
            final BufferedImage img = ImageIO.read(imgInput);
            return img.getWidth();
        }catch (final Exception e){
            logger.logError(MessageUtils.getInstance().getMessage("DOTJ023E", dirName+File.separatorChar+fileName).toString(), e);
            return -1;
        }
    }

    /**
     * Get the image height.
     * @param dirName -
     * 				The directory name that will be added to the path
     * 				of the image file.
     * @param fileName -
     * 				The file name of the image file.
     * @return int -
     * 				The height of the picture in pixels.
     */
    public static int getHeight (final String dirName, final String fileName){
        final DITAOTJavaLogger logger = new DITAOTJavaLogger();
        File imgInput = new File(dirName+File.separatorChar+fileName);
        if (checkDirName(dirName)) {
            imgInput = new File(getImageOutPutPath(fileName));
        }      
        try {
            final BufferedImage img = ImageIO.read(imgInput);
            return img.getHeight();
        }catch (final Exception e){
            logger.logError(MessageUtils.getInstance().getMessage("DOTJ023E", dirName+File.separatorChar+fileName).toString(), e);
            return -1;
        }
    }

    /**
     * Get the image binary data, with hexical output. For RTF transformation
     * @param dirName -
     * 				The directory name that will be added to the path
     * 				of the image file.
     * @param fileName -
     * 				The file name of the image file.
     * @return java.lang.String -
     * 				The Hexical binary of image data converted to String.
     */
    public static String getBinData (final String dirName, final String fileName){
        final DITAOTJavaLogger logger = new DITAOTJavaLogger();
        File imgInput = new File(dirName+File.separatorChar+fileName);
        if (checkDirName(dirName)) {
            imgInput = new File(getImageOutPutPath(fileName));
        }      
        FileInputStream binInput = null;
        int bin;
        try{
            String binStr = null;
            final StringBuffer ret = new StringBuffer(INT_16*INT_1024);
            binInput = new FileInputStream(imgInput);
            bin = binInput.read();
            while (bin != -1){
                binStr = Integer.toHexString(bin);
                if(binStr.length() < 2){
                    ret.append("0");
                }
                ret.append(binStr);
                bin = binInput.read();
            }
            return ret.toString();
        }catch (final Exception e){
            logger.logError(MessageUtils.getInstance().getMessage("DOTJ023E").toString());
            logger.logError(e.getMessage(), e) ;
            return null;
        }finally{
            try{
                binInput.close();
            }catch(final IOException ioe){
                logger.logError(ioe.getMessage(), ioe) ;
            }
        }
    }
    /**
     * Get Base64 encoding content. For ODT transformation
     * @param dirName -
     * 				The directory name that will be added to the path
     * 				of the image file.
     * @param fileName -
     * 				The file name of the image file.
     * @return base64 encoded binary data.
     */
    public static String getBASE64(final String dirName, final String fileName) {
        final DITAOTJavaLogger logger = new DITAOTJavaLogger();
        File imgInput = new File(dirName+File.separatorChar+fileName);
        if (checkDirName(dirName)) {
            imgInput = new File(getImageOutPutPath(fileName));
        }      
        //BASE64Encoder encoder = new BASE64Encoder();
        final Base64 encoder = new Base64();
        final byte   buff[]=new   byte[(int)imgInput.length()];
        FileInputStream file = null;
        try {
            file = new FileInputStream(imgInput);
            file.read(buff);
            //String ret = encoder.encode(buff);
            final String ret = encoder.encodeToString(buff);
            return ret;
        } catch (final FileNotFoundException e) {
            logger.logError(MessageUtils.getInstance().getMessage("DOTJ023E").toString());
            logger.logError(e.getMessage(), e) ;
            return null;
        } catch (final IOException e) {
            logger.logError(MessageUtils.getInstance().getMessage("DOTJ023E").toString());
            logger.logError(e.getMessage(), e) ;
            return null;
        }finally{
            try{
                file.close();
            }catch(final IOException ioe){
                logger.logError(ioe.getMessage(), ioe) ;
            }
        }

    }

}
