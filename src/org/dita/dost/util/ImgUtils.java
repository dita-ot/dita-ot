/**
 * (c) Copyright IBM Corp. 2006 All Rights Reserved.
 */
package org.dita.dost.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import javax.imageio.ImageIO;

import org.dita.dost.log.DITAOTJavaLogger;
import org.dita.dost.log.MessageUtils;

/**
 * Image utility to get the width, height, type and binary data from 
 * specific image file.
 * 
 * @author Zhang, Yuan Peng
 *
 */
public class ImgUtils {
	/**
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
			return img.getHeight();
		}catch (Exception e){
			Properties prop = new Properties();
        	prop.put("%1", dirName+File.separatorChar+fileName);
			logger.logError(MessageUtils.getMessage("DOTJ021E", prop).toString());
			return -1;
		}
	}
	
	/**
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
			return img.getWidth();
		}catch (Exception e){
			Properties prop = new Properties();
        	prop.put("%1", dirName+File.separatorChar+fileName);
			logger.logError(MessageUtils.getMessage("DOTJ023E", prop).toString());
			return -1;
		}
	}
	
	/**
	 * @param dirName -
	 * 				The directory name that will be added to the path 
	 * 				of the image file.
	 * @param fileName -
	 * 				The file name of the image file.
	 * @return java.lang.String -
	 * 				The Hexical binary of image data converted to String.
	 */
	/**
	 * @param dirName
	 * @param fileName
	 * @return
	 */
	public static String getBinData (String dirName, String fileName){
		DITAOTJavaLogger logger = new DITAOTJavaLogger();
		File imgInput = new File(dirName+File.separatorChar+fileName);
		try{
			FileInputStream binInput = new FileInputStream(imgInput);
			StringBuffer ret = new StringBuffer(Constants.INT_16*Constants.INT_1024);
			int bin = binInput.read();
			String binStr = null;
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
			logger.logError(MessageUtils.getMessage("DOTJ023E").toString());
			return null;
		}
	}
	
	/**
	 * @param fileName -
	 * 				The file name of the image file.
	 * @return int -
	 * 				The type of the picture in RTF specification. (JPG or PNG)
	 */
	public static String getType (String fileName){
		String name = fileName.toLowerCase();
		DITAOTJavaLogger logger = new DITAOTJavaLogger();
		if (name.endsWith(".jpg")||name.endsWith(".jpeg")){
			return "jpegblip";
		}else if (name.endsWith(".gif")||name.endsWith(".png")){
			return "pngblip";
		}
		Properties prop = new Properties();
    	prop.put("%1", fileName);
		logger.logError(MessageUtils.getMessage("DOTJ024W", prop).toString());
		return "other";
	}
}
