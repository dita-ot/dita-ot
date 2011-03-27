/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for 
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2005 All Rights Reserved.
 */
package org.dita.dost.util;

import static org.dita.dost.util.Constants.*;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.dita.dost.log.DITAOTJavaLogger;


/**
 * Class description goes here.
 * 
 * @author Wu, Zhi Qiang
 */
public final class FileUtils {
	/**
	 * Default Constructor
	 *
	 */
	private FileUtils(){
	}
	
	private static DITAOTJavaLogger logger = new DITAOTJavaLogger();
	
	// Added on 2010-11-09 for bug 3102827: Allow a way to specify recognized image extensions -- start
	/**
	 * Configuration properties.
	 * 
	 * <p>If configuration file is not found e.g. during integration,
	 * the properties object will be an empty.</p> 
	 */
	private final static Properties configuration = new Properties();
	static {
		//BufferedInputStream configurationInputStream = null;
		InputStream configurationInputStream = null;
		try {			
			 final ClassLoader loader = FileUtils.class.getClassLoader();
			 configurationInputStream =loader.getResourceAsStream(CONF_PROPERTIES);
			 if (configurationInputStream != null) {
			 		 configuration.load(configurationInputStream);
			 		//System.out.println("The configuration file path is:" + configurationInputStream.toString());
			 		//System.out.println("Supported image ext:" +configuration.getProperty(CONF_SUPPORTED_IMAGE_EXTENSIONS));
			 } else {
				 //try to find the configuration file from the lib folder from current working dir
				 final File configurationFile = new File("lib"+ File.separator + CONF_PROPERTIES);
				 if(configurationFile.exists()) {
                    configurationInputStream = new BufferedInputStream(new FileInputStream(configurationFile));
                }
				 
				 if (configurationInputStream != null) {
						configuration.load(configurationInputStream); 
				 }
			 }
			 

		} catch (final IOException e) {
			logger.logException(e);
		} finally {
			if (configurationInputStream != null) {
				try {
					configurationInputStream.close();
				} catch (final IOException ex) {
					logger.logException(ex);
				}
			}
		}
	}
	
	/**
	 * Supported image extensions.
	 */
	private final static List<String> supportedImageExtensions = new ArrayList<String>();
	static {
		final String imageExtensions = configuration.getProperty(CONF_SUPPORTED_IMAGE_EXTENSIONS);
		if (imageExtensions != null && imageExtensions.length()>0) {
			for (final String ext: imageExtensions.split(";")) {
				supportedImageExtensions.add(ext);
			}
		} else {
			supportedImageExtensions.add(FILE_EXTENSION_JPG);
			supportedImageExtensions.add(FILE_EXTENSION_GIF);
			supportedImageExtensions.add(FILE_EXTENSION_EPS);
			supportedImageExtensions.add(FILE_EXTENSION_JPEG);
			supportedImageExtensions.add(FILE_EXTENSION_PNG);
			supportedImageExtensions.add(FILE_EXTENSION_SVG);
			supportedImageExtensions.add(FILE_EXTENSION_TIFF);
			supportedImageExtensions.add(FILE_EXTENSION_TIF);			
		}
	}
	
	/**
	 * Supported extensions.
	 */
	private final static List<String> supportedExtensions = new ArrayList<String>();
	static {
		supportedExtensions.addAll(supportedImageExtensions);
		supportedExtensions.add(FILE_EXTENSION_DITA);
		supportedExtensions.add(FILE_EXTENSION_DITAMAP);
		supportedExtensions.add(FILE_EXTENSION_XML);
		supportedExtensions.add(FILE_EXTENSION_HTML);
		supportedExtensions.add(FILE_EXTENSION_PDF);
		supportedExtensions.add(FILE_EXTENSION_SWF);
	}
	// Added on 2010-11-09 for bug 3102827: Allow a way to specify recognized image extensions -- end

	/**
	 * Return if the file is a html file by extension.
	 * @param lcasefn file name
	 * @return true if is html file and false otherwise
	 */
	public static boolean isHTMLFile(final String lcasefn) {
		return (lcasefn.endsWith(FILE_EXTENSION_HTML) || lcasefn
				.endsWith(FILE_EXTENSION_HTM));
	}
	/**
	 * Return if the file is a hhp file by extension.
	 * @param lcasefn file name
	 * @return true if is hhp file and false otherwise
	 */
	public static boolean isHHPFile(final String lcasefn) {
		return (lcasefn.endsWith(FILE_EXTENSION_HHP));
	}
	/**
	 * Return if the file is a hhc file by extension.
	 * @param lcasefn file name
	 * @return true if is hhc file and false otherwise
	 */
	public static boolean isHHCFile(final String lcasefn) {
		return (lcasefn.endsWith(FILE_EXTENSION_HHC));
	}
	/**
	 * Return if the file is a hhk file by extension.
	 * @param lcasefn file name
	 * @return true if is hhk file and false otherwise
	 */
	public static boolean isHHKFile(final String lcasefn) {
		return (lcasefn.endsWith(FILE_EXTENSION_HHK));
	}
	
	
	
	/**
	 * Return if the file is a pdf file by its extension.
	 * @param lcasefn File name in lower case.
	 * @return <code>TRUE</code> if lcasefn contains an extension of "pdf",
	 *         <code>FALSE</code> otherwise.
	 */
	public static boolean isPDFFile(final String lcasefn) {
		return (lcasefn.endsWith(FILE_EXTENSION_PDF));
	}
	
	//Added by William on 2009-10-10 for resources bug:2873560 start
	/**
	 * Return if the file is a swf file by its extension.
	 * @param lcasefn File name in lower case.
	 * @return <code>TRUE</code> if lcasefn contains an extension of "swf",
	 *         <code>FALSE</code> otherwise.
	 */
	public static boolean isSWFile(final String lcasefn) {
		return (lcasefn.endsWith(FILE_EXTENSION_SWF));
	}
	//Added by William on 2009-10-10 for resources bug:2873560 end
	
	/**
	 * Return if the file is a dita file by extension.
	 * @param lcasefn file name
	 * @return ture if is DITA file and false otherwise
	 */
	public static boolean isDITAFile(String lcasefn) {
		if(lcasefn == null) {
            return false;
        }
		if (lcasefn.contains(SHARP)){
			lcasefn = lcasefn.substring(0, lcasefn.indexOf(SHARP));
		}
		
		return isDITATopicFile(lcasefn) || isDITAMapFile(lcasefn);
	}

	/**
	 * Return if the file is a dita topic file by extension.
	 * @param lcasefn file name
	 * @return true if is dita file and false otherwise
	 */
	public static boolean isDITATopicFile(final String lcasefn) {
		return lcasefn.endsWith(FILE_EXTENSION_DITA)
				|| lcasefn.endsWith(FILE_EXTENSION_XML);
	}

	/**
	 * Return if the file is a dita map file by extension.
	 * @param lcasefn file name
	 * @return true if is ditamap file and false otherwise
	 */
	public static boolean isDITAMapFile(final String lcasefn) {
		return lcasefn.endsWith(FILE_EXTENSION_DITAMAP);
	}

	/**
	 * Return if the file is a supported image file by extension.
	 * @param lcasefn filename
	 * @return true if is supported image and false otherwise
	 */
	public static boolean isSupportedImageFile(final String lcasefn) {
		
		// Modified on 2010-11-09 for bug 3102827: Allow a way to specify recognized image extensions -- start	
		/*return lcasefn.endsWith(FILE_EXTENSION_JPG)
				|| lcasefn.endsWith(FILE_EXTENSION_GIF)
				|| lcasefn.endsWith(FILE_EXTENSION_EPS)
				|| lcasefn.endsWith(FILE_EXTENSION_JPEG)
				|| lcasefn.endsWith(FILE_EXTENSION_PNG)
				|| lcasefn.endsWith(FILE_EXTENSION_SVG)
				|| lcasefn.endsWith(FILE_EXTENSION_TIFF)
				|| lcasefn.endsWith(FILE_EXTENSION_TIF);*/
		
		for (final String ext: supportedImageExtensions) {
			if (lcasefn.endsWith(ext)) {
				return true;
			}
		}
		return false;
		// Modified on 2010-11-09 for bug 3102827: Allow a way to specify recognized image extensions -- start
	}

	/**
	 * Return if the file is a topic file by extension.
	 * @param lcasefn filename
	 * @return true if is topic file and false otherwise
	 */
	public static boolean isTopicFile(final String lcasefn) {
		if(StringUtils.isEmptyString(lcasefn)){
			return false;
		}
		return lcasefn.endsWith(FILE_EXTENSION_DITA)
				|| lcasefn.endsWith(FILE_EXTENSION_XML);
	}

	/**
	 * Return if the file is a valid target file by extension.
	 * @param lcasefn filename
	 * @return true is the target is valid and false otherwise
	 */
	public static boolean isValidTarget(final String lcasefn) {
		/*		DITA-OT can support space in file names from 6.5 2008
 * 		if(lcasefn.indexOf(STRING_BLANK)!=-1){
			params.put("%1", lcasefn);
			logger.logWarn(MessageUtils.getMessage("DOTJ027W", params).toString());
		}
*/		
		// Modified on 2010-11-09 for bug 3102827: Allow a way to specify recognized image extensions -- start
		
		/*return lcasefn.endsWith(FILE_EXTENSION_DITA)
				|| lcasefn.endsWith(FILE_EXTENSION_DITAMAP)
				|| lcasefn.endsWith(FILE_EXTENSION_XML)
				|| lcasefn.endsWith(FILE_EXTENSION_JPG)
				|| lcasefn.endsWith(FILE_EXTENSION_GIF)
				|| lcasefn.endsWith(FILE_EXTENSION_EPS)
				|| lcasefn.endsWith(FILE_EXTENSION_HTML)
				|| lcasefn.endsWith(FILE_EXTENSION_JPEG)
				|| lcasefn.endsWith(FILE_EXTENSION_PNG)
				|| lcasefn.endsWith(FILE_EXTENSION_SVG)
				|| lcasefn.endsWith(FILE_EXTENSION_TIFF)
				|| lcasefn.endsWith(FILE_EXTENSION_TIF)
				|| lcasefn.endsWith(FILE_EXTENSION_PDF)
				|| lcasefn.endsWith(FILE_EXTENSION_SWF);*/
		
		for (final String ext: supportedExtensions) {
			if (lcasefn.endsWith(ext)) {
				return true;
			}
		}
		return false;
		// Modified on 2010-11-09 for bug 3102827: Allow a way to specify recognized image extensions -- end
	}
	
	/**
	 * Get the path of topicFile relative to the input map.
	 * In fact this method can be used to calculate any path of topicFile related to the first parameter.
	 * @param mapFilePathName map file with file path
	 * @param topicFilePathName topic file with file path
	 * @return relative path
	 */
	public static String getRelativePathFromMap(final String mapFilePathName,
			final String topicFilePathName) {
		final StringBuffer upPathBuffer = new StringBuffer(INT_128);
		final StringBuffer downPathBuffer = new StringBuffer(INT_128);
		final StringTokenizer mapTokenizer = new StringTokenizer(
				removeRedundantNames(mapFilePathName.replace(WINDOWS_SEPARATOR,UNIX_SEPARATOR),
						UNIX_SEPARATOR),
				UNIX_SEPARATOR);
		final StringTokenizer topicTokenizer = new StringTokenizer(
				removeRedundantNames(topicFilePathName.replace(WINDOWS_SEPARATOR,UNIX_SEPARATOR),
						UNIX_SEPARATOR),
				UNIX_SEPARATOR);

		while (mapTokenizer.countTokens() > 1
				&& topicTokenizer.countTokens() > 1) {
			final String mapToken = mapTokenizer.nextToken();
			final String topicToken = topicTokenizer.nextToken();
			boolean equals = false;
			if (OS_NAME.toLowerCase().indexOf(OS_NAME_WINDOWS) != -1){
				//if OS is Windows, we need to ignore case when comparing path names.
				equals = mapToken.equalsIgnoreCase(topicToken);
			}else{
				equals = mapToken.equals(topicToken);
			}

			if (!equals) {
				if(mapToken.endsWith(COLON) ||
						topicToken.endsWith(COLON)){
					//the two files are in different disks under Windows
					return topicFilePathName;
				}
				upPathBuffer.append("..");
				upPathBuffer.append(UNIX_SEPARATOR);
				downPathBuffer.append(topicToken);
				downPathBuffer.append(UNIX_SEPARATOR);
				break;
			}
		}

		while (mapTokenizer.countTokens() > 1) {
			mapTokenizer.nextToken();

			upPathBuffer.append("..");
			upPathBuffer.append(UNIX_SEPARATOR);
		}

		while (topicTokenizer.hasMoreTokens()) {
			downPathBuffer.append(topicTokenizer.nextToken());
			if (topicTokenizer.hasMoreTokens()) {
				downPathBuffer.append(UNIX_SEPARATOR);
			}
		}

		return upPathBuffer.append(downPathBuffer).toString();
	}
	
	/**
	 * Get path2Project from the relative path of a file.
	 * @param relativePath relative path
	 * @return path relative to project
	 */
	public static String getPathtoProject (final String relativePath){
		final StringTokenizer tokenizer = new StringTokenizer(relativePath, UNIX_SEPARATOR);
		final StringBuffer buffer = new StringBuffer();
		if (tokenizer.countTokens() == 1){
			return null;
		}else{
			while(tokenizer.countTokens() > 1){
				tokenizer.nextToken();
				buffer.append("..");
				buffer.append(UNIX_SEPARATOR);
			}
			return buffer.toString();
		}
	}

	/**
	 * Normalize topic path base on current directory and href value, by 
	 * replacing "\\" and "\" with File.separator, and removing ".", ".."
	 * from the file path, with no change to substring behind "#".  
	 * 
	 * @param rootPath root path
	 * @param relativePath relative path
	 * @return resolved topic file
	 */
	public static String resolveTopic(final String rootPath, final String relativePath) {
		String begin = relativePath;
		String end = "";
	
		if (relativePath.indexOf("#") != -1) {
			begin = relativePath.substring(0, relativePath.indexOf('#'));
			end = relativePath.substring(relativePath.indexOf('#'));
		}
		
		return normalizeDirectory(rootPath, begin) + end;
	}
	
	/**
	 * Normalize topic path base on current directory and href value, by 
	 * replacing "\\" and "\" with File.separator, and removing ".", "..","#"
	 * from the file path.  
	 * 
	 * @param rootPath root path
	 * @param relativePath relative path
	 * @return resolved topic file
	 */
	public static String resolveFile(final String rootPath, final String relativePath) {
		String begin = relativePath;
	
		if (relativePath.indexOf("#") != -1) {
			begin = relativePath.substring(0, relativePath.indexOf('#'));
		}
		
		return normalizeDirectory(rootPath, begin);
	}
	
	/**
	 * Normalize the input file path, by replacing all the '\\', '/' with
	 * File.seperator, and removing '..' from the directory.
	 * 
	 * Note: the substring behind "#" will be removed. 
	 * @param basedir base dir
	 * @param filepath file path
	 * @return normalized path
	 */
	public static String normalizeDirectory(final String basedir, final String filepath) {
		String normilizedPath = null;
		final int index = filepath.indexOf(SHARP);
		final String pathname = (index == -1) ? filepath : filepath.substring(0, index);

		/*
		 * normilize file path using java.io.File
		 */
		normilizedPath = new File(basedir, pathname).getPath();
		
		if (basedir == null || basedir.length() == 0) {
			return normilizedPath;
		}

		return FileUtils.removeRedundantNames(normilizedPath);
	}
	
	/**
	 * Removing redundant names ".." and "." from the given path.
	 * 
	 * @param path input path
	 * @return processed path
	 */
	public static String removeRedundantNames(final String path) {
		return removeRedundantNames(path, File.separator);
    }
	
	
	/**
	 * Removing redundant names ".." and "." from the given path.
	 * 
	 * @param path input path
	 * @param separator file separator
	 * @return processed path
	 */
	public static String removeRedundantNames(final String path, final String separator) {
        StringTokenizer tokenizer = null;
        final StringBuffer buff = new StringBuffer(path.length());
        final List<String> dirs = new LinkedList<String>();
        Iterator<String> iter = null;
        int dirNum = 0;
        int i = 0;
    
        /*
         * remove "." from the directory.
         */
        tokenizer = new StringTokenizer(path, separator);
        while (tokenizer.hasMoreTokens()) {
            final String token = tokenizer.nextToken();
            if (!(".".equals(token))) {
			    dirs.add(token);
            }
        }
    
        /*
         * remove ".." and the dir name before it.
         */
        dirNum = dirs.size();
        while (i < dirNum) {
        	if (i > 0) {
        	  final String lastDir = (String) dirs.get(i - 1);
        	  final String dir = (String) dirs.get(i);
        	  if ("..".equals(dir) && !("..".equals(lastDir))) {
                  dirs.remove(i);
                  dirs.remove(i - 1);
                  dirNum = dirs.size();
                  i = i - 1;
                  continue;
        	  }
        	}
        	
        	i++;
        }
    
        /*
         * restore the directory.
         */
        if (path.startsWith(separator)) {
        	buff.append(separator);
        }
        iter = dirs.iterator();
        while (iter.hasNext()) {
            buff.append(iter.next());
            if (iter.hasNext()) {
                buff.append(separator);
            }
        }
    
        return buff.toString();
    }
	
	
	
	

    /** 
     * Return if the path is absolute.
     * @param path test path
     * @return true if path is absolute and false otherwise.
     */
    public static boolean isAbsolutePath (final String path) {
    	if (path == null || path.trim().length() == 0) {
    		return false;
    	}
    	
        if (FILE_SEPARATOR.equals ("/")) {
            return path.startsWith ("/");
        }
        
        if (FILE_SEPARATOR.equals ("\\") && path.length() > 2) {         
        	return path.matches("[a-zA-Z]:\\\\.*");
        }

        return false;
    }

    /**
     * Copy file from src to target, overwrite if needed.
     * @param src source file
     * @param target target file
     */
    public static void copyFile(final File src, final File target) {
		FileInputStream fis = null;
		FileOutputStream fos = null;
		final byte[] buffer = new byte[INT_1024 * INT_4];
		int len;
		
		try {
			fis = new FileInputStream(src);
			fos = new FileOutputStream(target);
			while ((len = fis.read(buffer)) != -1) {
				fos.write(buffer, 0, len);
			}
			fos.flush();
		} catch (final IOException ex) {
			//removed by Alan on Date:2009-11-02 for Work Item:#1590 start
			/*logger.logWarn("Failed to copy file from '" + src + "' to '"
					+ target + "'");*/
			//removed by Alan on Date:2009-11-02 for Work Item:#1590 end
			logger.logException(ex);
			
		} finally {
			if (fis != null) {
    			try {
    				fis.close();
    			} catch (final Exception e) {
    				logger.logException(e);
    			}
			}
			if (fos != null) {
    			try {
    				fos.close();
    			} catch (final Exception e) {
    				logger.logException(e);
    			}
			}
		}
	}
    
    /**
     * Replace the file extension.
     * @param attValue value to be replaced
     * @param extName value used to replace with
     * @return replaced value
     */
    public static String replaceExtName(final String attValue, final String extName){
    	String fileName;
        int fileExtIndex;
        int index;
    	
    	index = attValue.indexOf(SHARP);
		
    	if (attValue.startsWith(SHARP)){
    		return attValue;
    	} else if (index != -1){
    		fileName = attValue.substring(0,index); 
    		fileExtIndex = fileName.lastIndexOf(DOT);
    		return (fileExtIndex != -1)
    			? fileName.substring(0, fileExtIndex) + extName + attValue.substring(index)
    			: attValue;
    	} else {
    		fileExtIndex = attValue.lastIndexOf(DOT);
    		return (fileExtIndex != -1)
    			? (attValue.substring(0, fileExtIndex) + extName) 
    			: attValue;
    	}
    }
    
    /**
     * Check whether a file exists on the local file systmem.
     * @param filename filename
     * @return boolean  true if the file exists, false otherwise
     */
    public static boolean fileExists (String filename){  //Eric
    	
    	filename = filename.indexOf(SHARP) != -1 
		? filename.substring(0, filename.indexOf(SHARP))
		: filename;
		   
    	
    	if (new File(filename).exists()){
    		return true;
    	}
    	
    	return false;
    }

    
}
