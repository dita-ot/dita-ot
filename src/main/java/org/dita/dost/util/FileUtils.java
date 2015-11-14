/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2005 All Rights Reserved.
 */
package org.dita.dost.util;

import static org.apache.commons.io.FilenameUtils.*;
import static org.dita.dost.util.Constants.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;

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

    private static final DITAOTJavaLogger logger = new DITAOTJavaLogger();

    /**
     * Supported image extensions. File extensions contain a leading dot.
     */
    @Deprecated
    private final static List<String> supportedImageExtensions;
    static {
        final List<String> sie = new ArrayList<>();
        final String imageExtensions = Configuration.configuration.get(CONF_SUPPORTED_IMAGE_EXTENSIONS);
        if (imageExtensions != null && imageExtensions.length()>0) {
            Collections.addAll(sie, imageExtensions.split(CONF_LIST_SEPARATOR));
        } else {
            logger.error("Failed to read supported image extensions from configuration, using defaults.");
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
    @Deprecated
    private final static List<String> supportedHTMLExtensions;
    static {
        final List<String> she = new ArrayList<>();
        final String extensions = Configuration.configuration.get(CONF_SUPPORTED_HTML_EXTENSIONS);
        if (extensions != null && extensions.length()>0) {
            Collections.addAll(she, extensions.split(CONF_LIST_SEPARATOR));
        } else {
            logger.error("Failed to read supported HTML extensions from configuration, using defaults.");
            she.add(FILE_EXTENSION_HTML);
            she.add(FILE_EXTENSION_HTM);
        }
        supportedHTMLExtensions = Collections.unmodifiableList(she);
    }

    /**
     * Supported resource file extensions. File extensions contain a leading dot.
     */
    @Deprecated
    private final static List<String> supportedResourceExtensions;
    static {
        final List<String> sre = new ArrayList<>();
        final String extensions = Configuration.configuration.get(CONF_SUPPORTED_RESOURCE_EXTENSIONS);
        if (extensions != null && extensions.length()>0) {
            Collections.addAll(sre, extensions.split(CONF_LIST_SEPARATOR));
        } else {
            logger.error("Failed to read supported resource file extensions from configuration, using defaults.");
            sre.add(FILE_EXTENSION_SWF);
            sre.add(FILE_EXTENSION_PDF);
        }
        supportedResourceExtensions = Collections.unmodifiableList(sre);
    }

    /**
     * Return if the file is a html file by extension.
     * @param lcasefn file name
     * @return true if is html file and false otherwise
     */
    @Deprecated
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
    @Deprecated
    public static boolean isResourceFile(final String lcasefn) {
        for (final String ext: supportedResourceExtensions) {
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
    @Deprecated
    public static boolean isSupportedImageFile(final String lcasefn) {
        for (final String ext: supportedImageExtensions) {
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
    @Deprecated
    public static String getRelativeUnixPath(final File basePath, final String refPath) {
        return getRelativePath(basePath.getPath(), refPath, UNIX_SEPARATOR);
    }
    
    /**
     * Resolves a path reference against a base path.
     * 
     * @param basePath base path
     * @param refPath reference path
     * @return relative path
     */
    public static File getRelativePath(final File basePath, final File refPath) {
        return new File(getRelativePath(basePath.getPath(), refPath.getPath(), File.separator));
    }
    
    /**
     * Resolves a path reference against a base path.
     * 
     * @param basePath base path
     * @param refPath reference path
     * @return relative path using {@link Constants#UNIX_SEPARATOR} path separator
     */
    @Deprecated
    public static String getRelativeUnixPath(final String basePath, final String refPath) {
        return getRelativePath(basePath, refPath, UNIX_SEPARATOR);
    }
    
    /**
     * Resolves a path reference against a base path.
     * 
     * @param basePath base path
     * @param refPath reference path
     * @param sep path separator
     * @return relative path using {@link Constants#UNIX_SEPARATOR} path separator
     */
    private static String getRelativePath(final String basePath, final String refPath, final String sep) {
        final StringBuilder upPathBuffer = new StringBuilder(128);
        final StringBuilder downPathBuffer = new StringBuilder(128);
        final StringTokenizer mapTokenizer = new StringTokenizer(normalizePath(basePath, File.separator), File.separator);
        final StringTokenizer topicTokenizer = new StringTokenizer(normalizePath(refPath, File.separator), File.separator);

        while (mapTokenizer.countTokens() > 1
                && topicTokenizer.countTokens() > 1) {
            final String mapToken = mapTokenizer.nextToken();
            final String topicToken = topicTokenizer.nextToken();
            boolean equals = false;
            if (OS_NAME.toLowerCase().contains(OS_NAME_WINDOWS)){
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
                upPathBuffer.append(sep);
                downPathBuffer.append(topicToken);
                downPathBuffer.append(sep);
                break;
            }
        }

        while (mapTokenizer.countTokens() > 1) {
            mapTokenizer.nextToken();

            upPathBuffer.append("..");
            upPathBuffer.append(sep);
        }

        while (topicTokenizer.hasMoreTokens()) {
            downPathBuffer.append(topicTokenizer.nextToken());
            if (topicTokenizer.hasMoreTokens()) {
                downPathBuffer.append(sep);
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
    @Deprecated
    public static String getRelativeUnixPath(final String relativePath) {
        return getRelativePathForPath(relativePath, UNIX_SEPARATOR);
    }
    
    /**
     * Get relative path to base path.
     * 
     * <p>For {@code foo/bar/baz.txt} return {@code ../../}</p>
     * 
     * @param relativePath relative path
     * @return relative path to base path, {@code null} if reference path was a single file
     */
    public static File getRelativePath(final File relativePath) {
        final String p = getRelativePathForPath(relativePath.getPath(), File.separator);
        return p != null ? new File(p) : null;
    }
    
    /**
     * Get relative path to base path.
     * 
     * <p>For {@code foo/bar/baz.txt} return {@code ../../}</p>
     * 
     * @param relativePath relative path
     * @param sep path separator
     * @return relative path to base path, {@code null} if reference path was a single file
     */
    private static String getRelativePathForPath(final String relativePath, final String sep) {
        final StringTokenizer tokenizer = new StringTokenizer(relativePath.replace(WINDOWS_SEPARATOR, UNIX_SEPARATOR), UNIX_SEPARATOR);
        final StringBuilder buffer = new StringBuilder();
        if (tokenizer.countTokens() == 1){
            return null;
        }else{
            while(tokenizer.countTokens() > 1){
                tokenizer.nextToken();
                buffer.append("..");
                buffer.append(sep);
            }
            return buffer.toString();
        }
    }

    /**
     * Normalize topic path base on current directory and href value, by
     * replacing "\\" and "\" with {@link File#separator}, and removing ".", ".."
     * from the file path, with no change to substring behind "#".
     * 
     * @param rootPath root directory path
     * @param relativePath relative path
     * @return resolved topic file
     */
    @Deprecated
    public static String resolveTopic(final File rootPath, final String relativePath) {
        return setFragment(resolve(rootPath, stripFragment(relativePath)).getPath(), getFragment(relativePath));
    }

    /**
     * Normalize topic path base on current directory and href value, by
     * replacing "\\" and "\" with {@link File#separator}, and removing ".", ".."
     * from the file path, with no change to substring behind "#".
     * 
     * @param rootPath root directory path
     * @param relativePath relative path
     * @return resolved topic file
     */
    @Deprecated
    public static String resolveTopic(final String rootPath, final String relativePath) {
        return setFragment(resolve(rootPath, stripFragment(relativePath)).getPath(), getFragment(relativePath));
    }
    
    @Deprecated
    public static URI resolve(final File rootPath, final URI relativePath) {
        return URLUtils.toURI(resolve(rootPath != null ? rootPath.getPath() : null, relativePath.getPath()));
    }

    /**
     * Resolve file path against a base directory path. Normalize the input file path, by replacing all the '\\', '/' with
     * File.seperator, and removing '..' from the directory.
     * 
     * <p>Note: the substring behind "#" will be removed.</p>
     *  
     * @param basedir base dir, may be {@code null}
     * @param filepath file path
     * @return normalized path
     */
    @Deprecated
    public static File resolve(final String basedir, final String filepath) {
        final File pathname = new File(normalizePath(stripFragment(filepath), File.separator));
        if (basedir == null || basedir.length() == 0) {
            return pathname;
        }
        final String normilizedPath = new File(basedir, pathname.getPath()).getPath();
        return new File(normalizePath(normilizedPath, File.separator));
    }
    
    @Deprecated
    public static File resolve(final File basedir, final String filepath) {
        return resolve(basedir != null ? basedir.getPath() : null, filepath);
    }

    public static File resolve(final File basedir, final File filepath) {
        return resolve(basedir != null ? basedir.getPath() : null, filepath.getPath());
    }

    /**
     * Remove redundant names ".." and "." from the given path and replace directory separators.
     * 
     * @param path input path
     * @param separator directory separator
     * @return processed path
     */
    private static String normalizePath(final String path, final String separator) {
        final String p = path.replace(WINDOWS_SEPARATOR, separator).replace(UNIX_SEPARATOR, separator);
        // remove "." from the directory.
        final List<String> dirs = new LinkedList<>();
        final StringTokenizer tokenizer = new StringTokenizer(p, separator);
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
        final StringBuilder buff = new StringBuilder(p.length());
        if (p.startsWith(separator + separator)) {
            buff.append(separator).append(separator);
        } else if (p.startsWith(separator)) {
            buff.append(separator);
        }
        final Iterator<String> iter = dirs.iterator();
        while (iter.hasNext()) {
            buff.append(iter.next());
            if (iter.hasNext()) {
                buff.append(separator);
            }
        }
        if (p.endsWith(separator)) {
            buff.append(separator);
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

        if (File.separator.equals(UNIX_SEPARATOR)) {
            return path.startsWith(UNIX_SEPARATOR);
        } else 

        if (File.separator.equals(WINDOWS_SEPARATOR) && path.length() > 2) {
            return path.matches("([a-zA-Z]:|\\\\)\\\\.*");
        }

        return false;
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
    private static boolean isWindows() {
        final String osName = System.getProperty("os.name");
        return osName.startsWith("Win");

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
    @Deprecated
    public static String stripFragment(final String path) {
        final int i = path.indexOf(SHARP);
        if (i != -1) {
           return path.substring(0, i);
        } else {
           return path;
        }
    }
    
    /**
     * Set fragment
     * @param path path
     * @param fragment new fragment, may be {@code null}
     * @return path with new fragment
     */
    @Deprecated
    public static String setFragment(final String path, final String fragment) {
        final int i = path.indexOf(SHARP);
        final String p = i != -1 ? path.substring(0, i) : path;
        return p + (fragment != null ? (SHARP + fragment) : ""); 
    }
    
    /**
     * Get fragment part from path.
     * 
     * @param path path
     * @return fragment without {@link Constants#SHARP}, {@code null} if no fragment exists
     */
    @Deprecated
    public static String getFragment(final String path) {
        return getFragment(path, null);
    }

    /**
     * Retrieve the topic ID from the path
     * 
     * @param relativePath
     * @return topic ID, may be {@code null}
     */
    @Deprecated
    public static String getTopicID(final String relativePath) {
        final String fragment = getFragment(relativePath);
        if (fragment != null) {
            final String id = fragment.lastIndexOf(SLASH) != -1
                              ? fragment.substring(0, fragment.lastIndexOf(SLASH))
                              : fragment;
            return id.isEmpty() ? null : id;
        }
        return null;
    }
    
    /**
     * Retrieve the element ID from the path
     * 
     * @param relativePath
     * @return element ID, may be {@code null}
     */
    @Deprecated
    public static String getElementID(final String relativePath) {
        final String fragment = getFragment(relativePath);
        if (fragment != null) {
            if (fragment.lastIndexOf(SLASH) != -1) {
                final String id = fragment.substring(fragment.lastIndexOf(SLASH) + 1);
                return id.isEmpty() ? null : id;
            }
        }
        return null;
    }
    
    /**
     * Set the element ID from the path
     * 
     * @param relativePath
     * @param id element ID
     * @return element ID, may be {@code null}
     */
    @Deprecated
    public static String setElementID(final String relativePath, final String id) {
        String topic = getTopicID(relativePath);
        if (topic != null) {
            return setFragment(relativePath, topic + (id != null ? SLASH + id : ""));
        } else if (id == null) {
            return stripFragment(relativePath);
        } else {
            throw new IllegalArgumentException(relativePath);
        }
    }
    
    /**
     * Get fragment part from path or return default fragment.
     * 
     * @param path path
     * @param defaultValue default fragment value
     * @return fragment without {@link Constants#SHARP}, default value if no fragment exists
     */
    @Deprecated
    public static String getFragment(final String path, final String defaultValue) {
        final int i = path.indexOf(SHARP);
        if (i != -1) {
           return path.substring(i + 1);
        } else {
           return defaultValue;
        }
    }

    /**
     * Determines whether the parent directory contains the child element (a file or directory)
     * 
     * @param directory the file to consider as the parent
     * @param child the file to consider as the child
     * @return true is the candidate leaf is under by the specified composite, otherwise false
     * @throws IOException
     */
    public static boolean directoryContains(final File directory, final File child) {
        final File d = new File(normalize(directory.getAbsolutePath()));
        final File c = new File(normalize(child.getAbsolutePath()));
        if (d.equals(c)) {
            return false;
        } else {
            return c.getPath().startsWith(d.getPath());
        }
    }

}
