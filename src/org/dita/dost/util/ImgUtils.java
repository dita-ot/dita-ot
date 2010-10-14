/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/**
 * (c) Copyright IBM Corp. 2006 All Rights Reserved.
 */
package org.dita.dost.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import javax.imageio.ImageIO;

import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.log.MessageUtils;

import sun.misc.BASE64Encoder;

/**
 * Image utility to get the width, height, type and binary data from 
 * specific image file.
 * 
 * @author Zhang, Yuan Peng
 *
 */
public class ImgUtils {
	/**
	 * Default Constructor
	 *
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
	public static int getWidth (String dirName, String fileName){
		DITAOTJavaLogger logger = new DITAOTJavaLogger();
		File imgInput = new File(dirName+File.separatorChar+fileName);
		try {
			BufferedImage img = ImageIO.read(imgInput);
			return img.getWidth();
		}catch (Exception e){
			Properties prop = new Properties();
        	prop.put("%1", dirName+File.separatorChar+fileName);
			logger.logError(MessageUtils.getMessage("DOTJ021E", prop).toString());
			logger.logException(e);
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
	public static int getHeight (String dirName, String fileName){
		DITAOTJavaLogger logger = new DITAOTJavaLogger();
		File imgInput = new File(dirName+File.separatorChar+fileName);
		try {
			BufferedImage img = ImageIO.read(imgInput);
			return img.getHeight();
		}catch (Exception e){
			Properties prop = new Properties();
        	prop.put("%1", dirName+File.separatorChar+fileName);
			logger.logError(MessageUtils.getMessage("DOTJ023E", prop).toString());
			logger.logException(e);
			return -1;
		}
	}
	
	/**
	 * Get the image width(ODT Transform).
	 * @param dirName -
	 * 				The directory name that will be added to the path 
	 * 				of the image file.
	 * @param fileName -
	 * 				The file name of the image file.
	 * @return int -
	 * 				The width of the picture in pixels.
	 */
	public static int getWidthODT (String dirName, String fileName){
		DITAOTJavaLogger logger = new DITAOTJavaLogger();
		File imgInput = new File(dirName+File.separatorChar+fileName);
		try {
			BufferedImage img = ImageIO.read(imgInput);
			return img.getWidth();
		}catch (Exception e){
			Properties prop = new Properties();
			prop.put("%1", dirName+File.separatorChar+fileName);
			logger.logError(MessageUtils.getMessage("DOTJ021E", prop).toString());
			logger.logException(e);
			return -1;
		}
	}
	
	/**
	 * Get the image height(ODT Transform).
	 * @param dirName -
	 * 				The directory name that will be added to the path 
	 * 				of the image file.
	 * @param fileName -
	 * 				The file name of the image file.
	 * @return int -
	 * 				The height of the picture in pixels.
	 */
	public static int getHeightODT (String dirName, String fileName){
		DITAOTJavaLogger logger = new DITAOTJavaLogger();
		File imgInput = new File(dirName+File.separatorChar+fileName);
		try {
			BufferedImage img = ImageIO.read(imgInput);
			return img.getHeight();
		}catch (Exception e){
			Properties prop = new Properties();
			prop.put("%1", dirName+File.separatorChar+fileName);
			logger.logError(MessageUtils.getMessage("DOTJ021E", prop).toString());
			logger.logException(e);
			return -1;
		}
	}
	
	/**
	 * Get the image binary data, with hexical output.
	 * @param dirName -
	 * 				The directory name that will be added to the path 
	 * 				of the image file.
	 * @param fileName -
	 * 				The file name of the image file.
	 * @return java.lang.String -
	 * 				The Hexical binary of image data converted to String.
	 */
	public static String getBinData (String dirName, String fileName){
		DITAOTJavaLogger logger = new DITAOTJavaLogger();
		File imgInput = new File(dirName+File.separatorChar+fileName);
		FileInputStream binInput = null;
		int bin;
		try{
			String binStr = null;
			StringBuffer ret = new StringBuffer(Constants.INT_16*Constants.INT_1024);
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
		}catch (Exception e){
			logger.logError(MessageUtils.getMessage("DOTJ021E").toString());
			logger.logException(e);
			return null;
		}finally{
			try{
				binInput.close();
			}catch(IOException ioe){
				logger.logException(ioe);
			}
		}
	}
	/**
	 * Get Base64 encoding content.
	 * @param dirName -
	 * 				The directory name that will be added to the path 
	 * 				of the image file.
	 * @param fileName -
	 * 				The file name of the image file.
	 * @return base64 encoded binary data.
	 */
	public static String getBASE64(String dirName, String fileName) {
		DITAOTJavaLogger logger = new DITAOTJavaLogger();
		File imgInput = new File(dirName+File.separatorChar+fileName);
		BASE64Encoder encoder = new BASE64Encoder();
		byte   buff[]=new   byte[(int)imgInput.length()];
		FileInputStream file = null;
		try {
			file = new FileInputStream(imgInput);
			file.read(buff);
			String ret = encoder.encode(buff);
			return ret;
		} catch (FileNotFoundException e) {
			logger.logError(MessageUtils.getMessage("DOTJ023E").toString());
			logger.logException(e);
			return null;
		} catch (IOException e) {
			logger.logError(MessageUtils.getMessage("DOTJ023E").toString());
			logger.logException(e);
			return null;
		}finally{
			try{
				file.close();
			}catch(IOException ioe){
				logger.logException(ioe);
			}
		}
		
	}
	
	/**
	 * Get the type of image file by extension.
	 * @param fileName -
	 * 				The file name of the image file.
	 * @return int -
	 * 				The type of the picture in RTF specification. (JPG or PNG)
	 */
	public static String getType (String fileName){
		String name = fileName.toLowerCase();
		DITAOTJavaLogger logger = new DITAOTJavaLogger();
		Properties prop = new Properties();
		if (name.endsWith(".jpg")||name.endsWith(".jpeg")){
			return "jpegblip";
		}else if (name.endsWith(".gif")||name.endsWith(".png")){
			return "pngblip";
		}
    	prop.put("%1", fileName);
		logger.logError(MessageUtils.getMessage("DOTJ024W", prop).toString());
		return "other";
	}
}
