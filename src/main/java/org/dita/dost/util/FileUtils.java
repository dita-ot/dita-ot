/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2005 All Rights Reserved.
 */
package org.dita.dost.util;

import static org.dita.dost.util.Constants.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.dita.dost.log.DITAOTJavaLogger;


/**
 * Static file utilities.
 * 
 * @author Wu, Zhi Qiang
 */
public final class FileUtils {

    /**
     * Private default constructor to make class uninstantiable.
     */
    private FileUtils(){
    }

    private static DITAOTJavaLogger logger = new DITAOTJavaLogger();

    /**
     * Supported DITA topic extensions. File extensions contain a leading dot.
     */
    private final static List<String> supportedTopicExtensions;
    static {
        final List<String> ste = new ArrayList<String>();
        final String extensions = Configuration.configuration.get(CONF_SUPPORTED_TOPIC_EXTENSIONS);
        if (extensions != null && extensions.length()>0) {
            for (final String ext: extensions.split(CONF_LIST_SEPARATOR)) {
                ste.add(ext);
            }
        } else {
            logger.logError("Failed to read supported DITA topic extensions from configuration, using defaults.");
            ste.add(FILE_EXTENSION_DITA);
            ste.add(FILE_EXTENSION_XML);
        }
        supportedTopicExtensions = Collections.unmodifiableList(ste);
    }
    
    /**
     * Supported DITA map extensions. File extensions contain a leading dot.
     */
    private final static List<String> supportedMapExtensions;
    static {
        final List<String> sme = new ArrayList<String>();
        final String extensions = Configuration.configuration.get(CONF_SUPPORTED_MAP_EXTENSIONS);
        if (extensions != null && extensions.length()>0) {
            for (final String ext: extensions.split(CONF_LIST_SEPARATOR)) {
                sme.add(ext);
            }
        } else {
            logger.logError("Failed to read supported DITA map extensions from configuration, using defaults.");
            sme.add(FILE_EXTENSION_DITAMAP);
        }
        supportedMapExtensions = Collections.unmodifiableList(sme);
    }

    /**
     * Supported image extensions. File extensions contain a leading dot.
     */
    private final static List<String> supportedImageExtensions;
    static {
        final List<String> sie = new ArrayList<String>();
        final String imageExtensions = Configuration.configuration.get(CONF_SUPPORTED_IMAGE_EXTENSIONS);
        if (imageExtensions != null && imageExtensions.length()>0) {
            for (final String ext: imageExtensions.split(CONF_LIST_SEPARATOR)) {
                sie.add(ext);
            }
        } else {
            logger.logError("Failed to read supported image extensions from configuration, using defaults.");
            sie.add(FILE_EXTENSION_JPG);
            sie.add(FILE_EXTENSION_GIF);
            sie.add(FILE_EXTENSION_EPS);
            sie.add(FILE_EXTENSION_JPEG);
            sie.add(FILE_EXTENSION_PNG);
            sie.add(FILE_EXTENSION_SVG);
            sie.add(FILE_EXTENSION_TIFF);
            sie.add(FILE_EXTENSION_TIF);
        }
        supportedImageExtensions = Collections.unmodifiableList(sie);
    }

    /**
     * Supported HTML extensions. File extensions contain a leading dot.
     */
    private final static List<String> supportedHTMLExtensions;
    static {
        final List<String> she = new ArrayList<String>();
        final String extensions = Configuration.configuration.get(CONF_SUPPORTED_HTML_EXTENSIONS);
        if (extensions != null && extensions.length()>0) {
            for (final String ext: extensions.split(CONF_LIST_SEPARATOR)) {
                she.add(ext);
            }
        } else {
            logger.logError("Failed to read supported HTML extensions from configuration, using defaults.");
            she.add(FILE_EXTENSION_HTML);
            she.add(FILE_EXTENSION_HTM);
        }
        supportedHTMLExtensions = Collections.unmodifiableList(she);
    }

    /**
     * Supported resource file extensions. File extensions contain a leading dot.
     */
    private final static List<String> supportedResourceExtensions;
    static {
        final List<String> sre = new ArrayList<String>();
        final String extensions = Configuration.configuration.get(CONF_SUPPORTED_RESOURCE_EXTENSIONS);
        if (extensions != null && extensions.length()>0) {
            for (final String ext: extensions.split(CONF_LIST_SEPARATOR)) {
                sre.add(ext);
            }
        } else {
            logger.logError("Failed to read supported resource file extensions from configuration, using defaults.");
            sre.add(FILE_EXTENSION_SWF);
            sre.add(FILE_EXTENSION_PDF);
        }
        supportedResourceExtensions = Collections.unmodifiableList(sre);
    }

    /**
     * Supported extensions. File extensions contain a leading dot.
     */
    private final static List<String> supportedExtensions;
    static {
        final List<String> se = new ArrayList<String>();
        se.addAll(supportedTopicExtensions);
        se.addAll(supportedMapExtensions);
        se.addAll(supportedImageExtensions);
        se.addAll(supportedHTMLExtensions);
        se.addAll(supportedResourceExtensions);
        supportedExtensions = Collections.unmodifiableList(se);
    }

    /**
     * Return if the file is a html file by extension.
     * @param lcasefn file name
     * @return true if is html file and false otherwise
     */
    public static boolean isHTMLFile(final String lcasefn) {
        for (final String ext: supportedHTMLExtensions) {
            if (lcasefn.endsWith(ext)) {
                return true;
            }
        }
        return false;
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
     * Return if the file is a resource file by its extension.
     * 
     * @param lcasefn file name in lower case.
     * @return {@code true} if file is a resource file, otherwise {@code false}
     */
    public static boolean isResourceFile(final String lcasefn) {
        for (final String ext: supportedResourceExtensions) {
            if (lcasefn.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return if the file is a dita file by extension.
     * @param lcasefn file name
     * @return ture if is DITA file and false otherwise
     */
    public static boolean isDITAFile(String lcasefn) {
        if(lcasefn == null) {
            return false;
        }
        lcasefn = stripFragment(lcasefn);

        return isDITATopicFile(lcasefn) || isDITAMapFile(lcasefn);
    }

    /**
     * Return if the file is a dita topic file by extension.
     * @param lcasefn file name
     * @return true if is dita file and false otherwise
     */
    public static boolean isDITATopicFile(final String lcasefn) {
        for (final String ext: supportedTopicExtensions) {
            if (lcasefn.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return if the file is a dita map file by extension.
     * @param lcasefn file name
     * @return true if is ditamap file and false otherwise
     */
    public static boolean isDITAMapFile(final String lcasefn) {
        for (final String ext: supportedMapExtensions) {
            if (lcasefn.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return if the file is a supported image file by extension.
     * @param lcasefn filename
     * @return true if is supported image and false otherwise
     */
    public static boolean isSupportedImageFile(final String lcasefn) {
        for (final String ext: supportedImageExtensions) {
            if (lcasefn.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return if the file is a valid target file by extension.
     * @param lcasefn filename
     * @return true is the target is valid and false otherwise
     */
    public static boolean isValidTarget(final String lcasefn) {
        for (final String ext: supportedExtensions) {
            if (lcasefn.endsWith(ext)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Resolves a path reference against a base path.
     * 
     * @param basePath base path
     * @param refPath reference path
     * @return relative path using {@link Constants#UNIX_SEPARATOR} path separator
     */
    public static String getRelativePath(final String basePath, final String refPath) {
        final StringBuffer upPathBuffer = new StringBuffer(INT_128);
        final StringBuffer downPathBuffer = new StringBuffer(INT_128);
        final StringTokenizer mapTokenizer = new StringTokenizer(
                normalize(FileUtils.separatorsToUnix(basePath),
                        UNIX_SEPARATOR),
                        UNIX_SEPARATOR);
        final StringTokenizer topicTokenizer = new StringTokenizer(
                normalize(FileUtils.separatorsToUnix(refPath),
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
                    return refPath;
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
     * Get relative path to base path.
     * 
     * <p>For {@code foo/bar/baz.txt} return {@code ../../}</p>
     * 
     * @param relativePath relative UNIX path
     * @return relative UNIX path to base path, {@code null} if reference path was a single file
     */
    public static String getRelativePath(final String relativePath) {
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
     * replacing "\\" and "\" with {@link File#separator}, and removing ".", ".."
     * from the file path, with no change to substring behind "#".
     * 
     * @param rootPath root path
     * @param relativePath relative path
     * @return resolved topic file
     */
    public static String resolveTopic(final String rootPath, final String relativePath) {
        String begin = relativePath;
        String end = "";

        if (relativePath.indexOf(SHARP) != -1) {
            begin = relativePath.substring(0, relativePath.indexOf('#'));
            end = relativePath.substring(relativePath.indexOf('#'));
        }

        return normalizeDirectory(rootPath, begin) + end;
    }

    /**
     * Normalize topic path base on current directory and href value, by
     * replacing "\\" and "\" with {@link File#separator}, and removing ".", "..", and "#"
     * from the file path.
     * 
     * @param rootPath root path
     * @param relativePath relative path
     * @return resolved topic file
     */
    public static String resolveFile(final String rootPath, final String relativePath) {
        final String begin = stripFragment(relativePath);
        return normalizeDirectory(rootPath, begin);
    }

    /**
     * Normalize the input file path, by replacing all the '\\', '/' with
     * File.seperator, and removing '..' from the directory.
     * 
     * <p>Note: the substring behind "#" will be removed.</p>
     * 
     * @param basedir base dir
     * @param filepath file path
     * @return normalized path
     */
    public static String normalizeDirectory(final String basedir, final String filepath) {
        final String pathname = stripFragment(filepath);
        final String normilizedPath = new File(basedir, pathname).getPath();
        if (basedir == null || basedir.length() == 0) {
            return normilizedPath;
        }
        return FileUtils.normalize(normilizedPath);
    }

    /**
     * Remove redundant names ".." and "." from the given path.
     * Use platform directory separator.
     * 
     * @param path input path
     * @return processed path
     */
    public static String normalize(final String path) {
        return normalize(path, File.separator);
    }


    /**
     * Remove redundant names ".." and "." from the given path.
     * 
     * @param path input path
     * @param separator directory separator
     * @return processed path
     */
    public static String normalize(final String path, final String separator) {
        // remove "." from the directory.
        final List<String> dirs = new LinkedList<String>();
        final StringTokenizer tokenizer = new StringTokenizer(path, separator);
        while (tokenizer.hasMoreTokens()) {
            final String token = tokenizer.nextToken();
            if (!(".".equals(token))) {
                dirs.add(token);
            }
        }

        // remove ".." and the dir name before it.
        int dirNum = dirs.size();
        int i = 0;
        while (i < dirNum) {
            if (i > 0) {
                final String lastDir = dirs.get(i - 1);
                final String dir = dirs.get(i);
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

        // restore the directory.
        final StringBuffer buff = new StringBuffer(path.length());
        if (path.startsWith(separator + separator)) {
            buff.append(separator).append(separator);
        } else if (path.startsWith(separator)) {
            buff.append(separator);
        }
        final Iterator<String> iter = dirs.iterator();
        while (iter.hasNext()) {
            buff.append(iter.next());
            if (iter.hasNext()) {
                buff.append(separator);
            }
        }
        if (path.endsWith(separator)) {
            buff.append(separator);
        }

        return buff.toString();
    }

    /**
     * Translate path separators from Windows to Unix.
     * @param path path to translate
     * @return UNIX path
     */
    public static String separatorsToUnix(final String path) {
        return path.replace(WINDOWS_SEPARATOR, UNIX_SEPARATOR);
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

        if (File.separator.equals(UNIX_SEPARATOR)) {
            return path.startsWith(UNIX_SEPARATOR);
        } else 

        if (File.separator.equals(WINDOWS_SEPARATOR) && path.length() > 2) {
            return path.matches("([a-zA-Z]:|\\\\)\\\\.*");
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
        try {
            fis = new FileInputStream(src);
            fos = new FileOutputStream(target);
            copy(fis, fos);
        } catch (final IOException ex) {
            logger.logError(ex.getMessage(), ex) ;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (final Exception e) {
                    logger.logError(e.getMessage(), e) ;
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (final Exception e) {
                    logger.logError(e.getMessage(), e) ;
                }
            }
        }
    }
    
    /**
     * Copy input stream to output stream
     * @param fis input stream
     * @param fos output stream
     */
    public static void copy(final InputStream fis, final OutputStream fos) throws IOException {
        final byte[] buffer = new byte[1024 * 4];
        int len;
        while ((len = fis.read(buffer)) != -1) {
            fos.write(buffer, 0, len);
        }
        fos.flush();
    }

    /**
     * Replace the file extension.
     * @param attValue value to be replaced
     * @param extName value used to replace with
     * @return replaced value
     */
    public static String replaceExtension(final String attValue, final String extName){
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
     * Get file extension
     * 
     * @param file filename, may contain a URL fragment
     * @return file extensions, {@code null} if no extension was found
     */
    public static String getExtension(final String file){
        final int index = file.indexOf(SHARP);

        if (file.startsWith(SHARP)) {
            return null;
        } else if (index != -1) {
            final String fileName = file.substring(0, index);
            final int fileExtIndex = fileName.lastIndexOf(DOT);
            return (fileExtIndex != -1) ? fileName.substring(fileExtIndex + 1,
                    fileName.length()) : null;
        } else {
            final int fileExtIndex = file.lastIndexOf(DOT);
            return (fileExtIndex != -1) ? file.substring(fileExtIndex + 1,
                    file.length()) : null;
        }
    }

    /**
     * Check whether a file exists on the local file systmem.
     * @param filename platform path, may contain a hash fragment
     * @return boolean  true if the file exists, false otherwise
     */
    public static boolean fileExists (String filename){  //Eric
        // FIXME don't modify argument, use a separate variable for results2
        filename = filename.indexOf(SHARP) != -1
                ? filename.substring(0, filename.indexOf(SHARP))
                        : filename;


                if (new File(filename).exists()){
                    return true;
                }

                return false;
    }

    /**
     * Get filename from a path.
     * 
     * @param aURLString Windows, UNIX, or URI path, may contain hash fragment
     * @return filename without path or hash fragment
     */
    public static String getName(final String aURLString) {
        int pathnameEndIndex;
        if (isWindows()) {
            if (aURLString.contains(SHARP)) {
                pathnameEndIndex = aURLString.lastIndexOf(SHARP);
            } else {
                pathnameEndIndex = aURLString.lastIndexOf(WINDOWS_SEPARATOR);
                if (pathnameEndIndex == -1) {
                    pathnameEndIndex = aURLString.lastIndexOf(UNIX_SEPARATOR);
                }
            }
        } else {
            if (aURLString.contains(SHARP)) {
                pathnameEndIndex = aURLString.lastIndexOf(SHARP);
            }
            pathnameEndIndex = aURLString.lastIndexOf(UNIX_SEPARATOR);
        }

        String schemaLocation = null;
        if (aURLString.contains(SHARP)) {
            schemaLocation = aURLString.substring(0, pathnameEndIndex);
        } else {
            schemaLocation = aURLString.substring(pathnameEndIndex + 1);
        }

        return schemaLocation;
    }

    /**
     * Test if current platform is Windows
     * 
     * @return {@code true} if platform is Windows, otherwise {@code fasel}
     */
    public static boolean isWindows() {
        final String osName = System.getProperty("os.name");
        if (osName.startsWith("Win")) {
            return true;
        }
        return false;

    }

    /**
     * Get base path from a path.
     * 
     * @param aURLString UNIX or URI path
     * @return base path
     */
    public static String getFullPathNoEndSeparator(final String aURLString) {
        final int pathnameStartIndex = aURLString.indexOf(UNIX_SEPARATOR);
        final int pathnameEndIndex = aURLString.lastIndexOf(UNIX_SEPARATOR);
        String aPath = aURLString.substring(pathnameStartIndex, pathnameEndIndex);
        aPath = aURLString.substring(0, pathnameEndIndex);
        return aPath;
    }
    
    /**
     * Strip fragment part from path.
     * 
     * @param path path
     * @return path without path
     */
    public static String stripFragment(final String path) {
        final int i = path.indexOf(SHARP);
        if (i != -1) {
           return path.substring(0, i);
        } else {
           return path;
        }
    }
    
    /**
     * Get fragment part from path.
     * 
     * @param path path
     * @return fragment without {@link Constants#SHARP}, {@code null} if no fragment exists
     */
    public static String getFragment(final String path) {
        final int i = path.indexOf(SHARP);
        if (i != -1) {
           return path.substring(i + 1);
        } else {
           return null;
        }
    }

}
