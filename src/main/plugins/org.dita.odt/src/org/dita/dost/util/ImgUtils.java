/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/**
 * (c) Copyright IBM Corp. 2006 All Rights Reserved.
 */
package org.dita.dost.util;

import static org.dita.dost.util.URLUtils.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.log.DITAOTLogger;
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
    @Deprecated
    public static int getWidth (final String dirName, final String fileName){
        final DITAOTLogger logger = new DITAOTJavaLogger();
		final File imgInput = new File(dirName, toFile(fileName).getPath());		
        try {
            final BufferedImage img = ImageIO.read(imgInput);
            return img.getWidth();
        }catch (final Exception e){
            logger.error(MessageUtils.getInstance().getMessage("DOTJ023E", dirName+File.separatorChar+fileName).toString(), e);
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
    @Deprecated
    public static int getHeight (final String dirName, final String fileName){
        final DITAOTLogger logger = new DITAOTJavaLogger();
        final File imgInput = new File(dirName, toFile(fileName).getPath());
        try {
            final BufferedImage img = ImageIO.read(imgInput);
            return img.getHeight();
        }catch (final Exception e){
            logger.error(MessageUtils.getInstance().getMessage("DOTJ023E", dirName+File.separatorChar+fileName).toString(), e);
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
        final DITAOTLogger logger = new DITAOTJavaLogger();
        final File imgInput = new File(dirName, toFile(fileName).getPath());
        FileInputStream binInput = null;
        try{
            String binStr = null;
            final StringBuilder ret = new StringBuilder(16*1024);
            binInput = new FileInputStream(imgInput);
            int bin = binInput.read();
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
            logger.error(MessageUtils.getInstance().getMessage("DOTJ023E").toString());
            logger.error(e.getMessage(), e) ;
            return null;
        }finally{
            if (binInput != null) {
                try{
                    binInput.close();
                }catch(final IOException ioe){
                    logger.error(ioe.getMessage(), ioe) ;
                }
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
        final DITAOTLogger logger = new DITAOTJavaLogger();
        final URI imgInputURI = toURI(fileName);
        final File imgInput = imgInputURI.isAbsolute() ? new File(imgInputURI) : new File(dirName, toFile(imgInputURI).getPath());
        //BASE64Encoder encoder = new BASE64Encoder();
        final Base64 encoder = new Base64();
        final byte   buff[]=new   byte[(int)imgInput.length()];
        FileInputStream file = null;
        try {
            file = new FileInputStream(imgInput);
            file.read(buff);
            //String ret = encoder.encode(buff);
            return encoder.encodeToString(buff);
        } catch (final FileNotFoundException e) {
            logger.error(MessageUtils.getInstance().getMessage("DOTJ023E").toString());
            logger.error(e.getMessage(), e) ;
            return null;
        } catch (final IOException e) {
            logger.error(MessageUtils.getInstance().getMessage("DOTJ023E").toString());
            logger.error(e.getMessage(), e) ;
            return null;
        }finally{
            if (file != null) {
                try{
                    file.close();
                }catch(final IOException ioe){
                    logger.error(ioe.getMessage(), ioe) ;
                }
            }
        }

    }

}
