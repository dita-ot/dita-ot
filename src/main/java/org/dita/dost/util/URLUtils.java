/*
 * This file is part of the DITA Open Toolkit project.
 * See the accompanying license.txt file for applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.util;

import static org.dita.dost.util.Constants.*;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.StringTokenizer;

/**
 * Corrects the URLs.
 */
public final class URLUtils {
    
    /**
     * Private default constructor to make class uninstantiable.
     */
    private URLUtils() {
    }
    
    /**
     * Corrects the file to URL.
     * 
     * @param file
     *            The file to be corrected. If null will throw
     *            MalformedURLException.
     * @return a corrected URL. Never null.
     * @exception MalformedURLException
     *                when the argument is null.
     */
    public static URL correct(final File file) throws MalformedURLException {
        if (file == null) {
            throw new MalformedURLException("The url is null");
        }
        return new URL(correct(file.toURI().toString(), true));
    }

    /**
     * Corrects an URL.
     * 
     * @param url
     *            The URL to be corrected. If null will throw
     *            MalformedURLException.
     * @return a corrected URL. Never null.
     * @exception MalformedURLException
     *                when the argument is null.
     */
    public static URL correct(final URL url) throws MalformedURLException {
        if (url == null) {
            throw new MalformedURLException("The url is null");
        }
        return new URL(correct(url.toString(), false));
    }

    /**
     * Decodes a application/x-www-form-urlencoded string using UTF-8 encoding scheme.
     * 
     * Convenience method for {@link java.net.URLDecoder#decode(String,String) URLDecoder}:
     * use UTF-8 and do not throw {@link java.io.UnsupportedEncodingException UnsupportedEncodingException}.
     * 
     * @param s the string to decode
     * @return the newly decoded string
     */
    public static String decode(final String s) {
    	try {
    		return URLDecoder.decode(s, UTF8);
		} catch (final UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
    }
    
    /**
     * Decode UTF8/URL encoded strings.
     * 
     * @param s
     *            the string to be decoded
     * @return the decoded string
     */
    private static String uncorrect(final String s) {
        if (s == null) {
            return null;
        }
        if (s.indexOf('%') == -1) {
            // Optimization, nothing to uncorrect here
            return s;
        }
        final StringBuilder sbuf = new StringBuilder();
        final int l = s.length();
        int ch = -1;
        int b = 0, sumb = 0;
        boolean applyUTF8dec = false;

        for (int i = 0, more = -1; i < l; i++) {
            // Get next byte b from URL segment s
            final char current = s.charAt(i);
            ch = current;
            switch (ch) {
            case '%':
                if (i + 2 < s.length()) {
                    // Avoid java.lang.StringIndexOutOfBoundsException...
                    ch = s.charAt(++i);
                    final int hb = (Character.isDigit((char) ch) ? ch - '0'
                            : 10 + Character.toLowerCase((char) ch) - 'a') & 0xF;
                    ch = s.charAt(++i);
                    final int lb = (Character.isDigit((char) ch) ? ch - '0'
                            : 10 + Character.toLowerCase((char) ch) - 'a') & 0xF;
                    b = (hb << 4) | lb;
                    applyUTF8dec = true;
                }
                break;
            default:
                b = ch;
                applyUTF8dec = false;
            }
            // Decode byte b as UTF-8, sumb collects incomplete chars
            if (applyUTF8dec) {
                if ((b & 0xc0) == 0x80) {
                    // 10xxxxxx (continuation byte)
                    sumb = (sumb << 6) | (b & 0x3f); // Add 6 bits to sumb
                    if (--more == 0) {
                        sbuf.append((char) sumb); // Add char to sbuf
                    }
                } else if ((b & 0x80) == 0x00) {
                    // 0xxxxxxx (yields 7 bits)
                    sbuf.append((char) b); // Store in sbuf
                } else if ((b & 0xe0) == 0xc0) {
                    // 110xxxxx (yields 5 bits)
                    sumb = b & 0x1f;
                    more = 1; // Expect 1 more byte
                } else if ((b & 0xf0) == 0xe0) {
                    // 1110xxxx (yields 4 bits)
                    sumb = b & 0x0f;
                    more = 2; // Expect 2 more bytes
                } else if ((b & 0xf8) == 0xf0) {
                    // 11110xxx (yields 3 bits)
                    sumb = b & 0x07;
                    more = 3; // Expect 3 more bytes
                } else if ((b & 0xfc) == 0xf8) {
                    // 111110xx (yields 2 bits)
                    sumb = b & 0x03;
                    more = 4; // Expect 4 more bytes
                } else {
                    sumb = b & 0x01;
                    more = 5; // Expect 5 more bytes
                }
            } else {
                sbuf.append(current);
                // Do not expect other continuation.
                more = -1;
            }
            // We don't test if the UTF-8 encoding is well-formed
        }
        return sbuf.toString();
    }

    /**
     * On Windows names of files from network neighborhood must be corrected
     * before open.
     * 
     * @param url
     *            The file URL.
     * @return The canonical or absolute file, or null if the protocol is not
     *         file.
     */
    public static File getCanonicalFileFromFileUrl(final URL url) {
        File file = null;
        if (url == null) {
            throw new NullPointerException("The URL cannot be null.");
        }
        if ("file".equals(url.getProtocol())) {
            final String fileName = url.getFile();
            final String path = URLUtils.uncorrect(fileName);
            file = new File(path);

            try {
                file = file.getCanonicalFile();
            } catch (final IOException e) {
                // Does not exist.
                file = file.getAbsoluteFile();
            }

        }
        return file;
    }

    /**
     * Method introduced to correct the URLs in the default machine encoding.
     * @param url
     *            The URL to be corrected. If it contains a % char, it means it
     *            already was corrected, so it will be returned. Take care at
     *            composing URLs from a corrected part and an uncorrected part.
     *            Correcting the result will not work. Try to correct first the
     *            relative part.
     * @param forceCorrection
     *            True if the correction must be executed any way (for files
     *            containing % for example - the % will be also corrected). Also
     *            if <code>true</code> '#' and '?' will be corrected otherwise
     *            will consider that is an URL that contains an anchor or a
     *            query.
     * @return The corrected URL.
     */
    private static String correct(String url, final boolean forceCorrection) {
        if (url == null) {
            return null;
        }

        final String initialUrl = url;

        // If there is a % that means the URL was already corrected.
        if (!forceCorrection && url.contains("%")) {
            return initialUrl;
        }

        // Extract the reference (anchor) part from the URL. The '#' char identifying the anchor
        // must not be corrected.
        String reference = null;
        if (!forceCorrection) {
            final int refIndex = url.lastIndexOf('#');
            if (refIndex != -1) {
                reference = FilePathToURI.filepath2URI(url.substring(refIndex + 1));
                url = url.substring(0, refIndex);
            }
        }

        // Buffer where eventual query string will be processed.
        StringBuilder queryBuffer = null;
        if (!forceCorrection) {
            final int queryIndex = url.indexOf('?');
            if (queryIndex != -1) {
                // We have a query
                final String query = url.substring(queryIndex + 1);
                url = url.substring(0, queryIndex);
                queryBuffer = new StringBuilder(query.length());
                // Tokenize by &
                final StringTokenizer st = new StringTokenizer(query, "&");
                while (st.hasMoreElements()) {
                    String token = st.nextToken();
                    token = FilePathToURI.filepath2URI(token);
                    // Correct token
                    queryBuffer.append(token);
                    if (st.hasMoreElements()) {
                        queryBuffer.append("&");
                    }
                }
            }
        }
        String toReturn = FilePathToURI.filepath2URI(url);

        if (queryBuffer != null) {
            // Append to the end the corrected query.
            toReturn += "?" + queryBuffer.toString();
        }

        if (reference != null) {
            // Append the reference to the end the corrected query.
            toReturn += "#" + reference;
        }
        return toReturn;
    }

    /**
     * Convert a file name to url.
     * @param fileName -
     * 				The file name string.
     * @return string -
     * 				URL
     */
    public static String getURL(final String fileName){

        if(fileName.startsWith("file:/")){
            return fileName;
        }else{
            final File file = new File(fileName);
            return file.toURI().toString();
        }

    }
    
    // Which ASCII characters need to be escaped
    private static final boolean gNeedEscaping[] = new boolean[128];
    // The first hex character if a character needs to be escaped
    private static final char gAfterEscaping1[] = new char[128];
    // The second hex character if a character needs to be escaped
    private static final char gAfterEscaping2[] = new char[128];
    private static final char[] gHexChs = {'0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    // Initialize the above 3 arrays
    static {
        for (int i = 0; i <= 0x1f; i++) {
            gNeedEscaping[i] = true;
            gAfterEscaping1[i] = gHexChs[i >> 4];
            gAfterEscaping2[i] = gHexChs[i & 0xf];
        }
        gNeedEscaping[0x7f] = true;
        gAfterEscaping1[0x7f] = '7';
        gAfterEscaping2[0x7f] = 'F';
        final char[] escChs = {' ', '<', '>',
                //'#',
                //'%',
                '"', '{', '}',
                //'?',
                '|', '\\', '^', '~', '[', ']', '`', '\'',
                //'&'
                };
        final int len = escChs.length;
        char ch;
        for (char escCh : escChs) {
            ch = escCh;
            gNeedEscaping[ch] = true;
            gAfterEscaping1[ch] = gHexChs[ch >> 4];
            gAfterEscaping2[ch] = gHexChs[ch & 0xf];
        }
    }
    
    /**
     * Try to clean an invalid URI.
     * 
     * @param path URI to be escaped.
     * @return cleaned URI
     */
    public static String clean(final String path) {
        return clean(path.replace(WINDOWS_SEPARATOR, UNIX_SEPARATOR), true);
    }
    
    /**
     * Try to clean an invalid URI.
     * 
     * @param path URI to be escaped.
     * @param ascii encode non-ASCII characters to ASCII
     * @return cleaned URI
     */
    public static String clean(final String path, final boolean ascii) {
        int len = path.length(), ch;
        final StringBuilder buffer = new StringBuilder(len*3);
        // Change C:/something to /C:/something
        if (len >= 2 && path.charAt(1) == ':') {
            ch = Character.toUpperCase(path.charAt(0));
            if (ch >= 'A' && ch <= 'Z') {
                buffer.append('/');
            }
        }

        // For each character in the path
        int i = 0;
        for (; i < len; i++) {
            ch = path.charAt(i);
            // If it's not an ASCII character, break here, and use UTF-8 encoding
            if (ch >= 128 && ascii) {
                break;
            }
            if (ch <  gNeedEscaping.length && gNeedEscaping[ch]) {
                buffer.append('%');
                buffer.append(gAfterEscaping1[ch]);
                buffer.append(gAfterEscaping2[ch]);
                // Record the fact that it's escaped
            }
            else {
                buffer.append((char)ch);
            }
        }

        // We saw some non-ASCII character
        if (i < len && ascii) {
            // Get UTF-8 bytes for the remaining sub-string
            byte[] bytes;
            byte b;
            try {
                bytes = path.substring(i).getBytes("UTF-8");
            } catch (final java.io.UnsupportedEncodingException e) {
            	throw new RuntimeException(e);
            }
            len = bytes.length;

            // For each byte
            for (i = 0; i < len; i++) {
                b = bytes[i];
                // For non-ASCII character: make it positive, then escape
                if (b < 0) {
                    ch = b + 256;
                    buffer.append('%');
                    buffer.append(gHexChs[ch >> 4]);
                    buffer.append(gHexChs[ch & 0xf]);
                }
                else if (gNeedEscaping[b]) {
                    buffer.append('%');
                    buffer.append(gAfterEscaping1[b]);
                    buffer.append(gAfterEscaping2[b]);
                }
                else {
                    buffer.append((char)b);
                }
            }
        }
        return buffer.toString();
    }
    
    /**
     * Test if URI path is absolute.
     */
    public static boolean isAbsolute(final URI uri) {
        final String p = uri.getPath();
        return p != null && p.startsWith(URI_SEPARATOR);
    }
    
    /**
     * Convert URI reference to system file path.
     * 
     * @filename URI to convert to system file path, may be relative or absolute
     * @return file path, {@code null} if input was {@code null}
     */
    public static File toFile(final URI filename) {
        if (filename == null) {
            return null;
        }
        final URI f = stripFragment(filename);
        if ("file".equals(f.getScheme()) && f.getPath() != null && f.isAbsolute()) {
            return new File(f);
        } else {
            return toFile(f.toString());
        }
    }
    
    /**
     * Convert URI or chimera references to file paths.
     * 
     * @param filename file reference
     * @return file path, {@code null} if input was {@code null}
     */
    public static File toFile(final String filename) {
        if (filename == null) {
            return null;
        }
        String f = filename;
        try {
            f = URLDecoder.decode(filename, UTF8);
        } catch (final UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        f = f.replace(WINDOWS_SEPARATOR, File.separator).replace(UNIX_SEPARATOR, File.separator);
        return new File(f);
    }

    /**
     * Covert file reference to URI. The difference between this method and
     * {@link java.net.URI(java.io.File)} constructor is that this
     * method doesn't make the URI absolute.
     * 
     * @param file system path to convert to a URI, may be {@code null}
     * @return file URI, {@code null} if input was {@code null}
     */
    public static URI toURI(final File file) {
        if (file == null) {
            return null;
        }
        if (file.isAbsolute()) {
            return file.toURI();
        } else {
            try {
                return new URI(clean(file.getPath().replace(WINDOWS_SEPARATOR, URI_SEPARATOR), false));
            } catch (final URISyntaxException e) {
                throw new IllegalArgumentException(e.getMessage(), e);
            }
        }
    }
    
    /**
     * Covert file reference to URI. Fixes directory separators and escapes characters.
     * 
     * @param file The string to be parsed into a URI, may be {@code null}
     * @return URI from parsing the given string, {@code null} if input was {@code null}
     */
    public static URI toURI(final String file) {
        if (file == null) {
            return null;
        }
        if (File.separatorChar == '\\' && file.indexOf('\\') != -1) {
            return toURI(new File(file));
        }
        try {
            return new URI(file);
        } catch (final URISyntaxException e) {
            try {
                return new URI(clean(file.replace(WINDOWS_SEPARATOR, URI_SEPARATOR), false));
            } catch (final URISyntaxException ex) {
                throw new IllegalArgumentException(ex.getMessage(), ex);
            }
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
    public static boolean directoryContains(final URI directory, final URI child) {
        final String d = directory.normalize().toString();
        final String c = child.normalize().toString();
        if (d.equals(c)) {
            return false;
        } else {
            return c.startsWith(d);
        }
    }
 
    /**
     * Strip fragment part from path.
     * 
     * @param path path
     * @return path without path
     */
    public static URI stripFragment(final URI path) {
        return setFragment(path, null);
    }
    
    /**
     * Create new URI with a given fragment.
     * 
     * @param path URI to set fragment on
     * @param fragment new fragment, {@code null} for no fragment
     * @return new URI instance with given fragment 
     */
    public static URI setFragment(final URI path, final String fragment) {
        try {
            if (path.getPath() != null) {
                return new URI(path.getScheme(), path.getUserInfo(), path.getHost(), path.getPort(), path.getPath(), path.getQuery(), fragment);
            } else {
                return new URI(path.getScheme(), path.getSchemeSpecificPart(), fragment);
            }
        } catch (final URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    /**
     * Create new URI with a given path.
     * 
     * @param orig URI to set path on
     * @param path new path, {@code null} for no path
     * @return new URI instance with given path
     */
    public static URI setPath(final URI orig, final String path) {
        try {
            return new URI(orig.getScheme(), orig.getUserInfo(), orig.getHost(), orig.getPort(), path, orig.getQuery(), orig.getFragment());
        } catch (final URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Create new URI with a given scheme.
     *
     * @param orig URI to set scheme on
     * @param scheme new scheme, {@code null} for no scheme
     * @return new URI instance with given path
     */
    public static URI setScheme(final URI orig, final String scheme) {
        try {
            return new URI(scheme, orig.getUserInfo(), orig.getHost(), orig.getPort(), orig.getPath(), orig.getQuery(), orig.getFragment());
        } catch (final URISyntaxException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Resolves absolute URI against another absolute URI.
     * 
     * @param base absolute base file URI
     * @param ref absolute reference file URI
     * @return relative URI if possible, otherwise original reference file URI argument
     */
    public static URI getRelativePath(final URI base, final URI ref) {
        final String baseScheme = base.getScheme();
        final String refScheme = ref.getScheme();
        final String baseAuth = base.getAuthority();
        final String refAuth = ref.getAuthority();
        if (!(((baseScheme == null && refScheme == null) || (baseScheme != null && refScheme != null && baseScheme.equals(refScheme))) &&
                ((baseAuth == null && refAuth == null) || (baseAuth != null && refAuth != null && baseAuth.equals(refAuth))))) {
            return ref;
        }
        
        URI rel;
        if (base.getPath().equals(ref.getPath()) && ref.getFragment() != null) {
            rel = toURI("");
        } else {
            final StringBuilder upPathBuffer = new StringBuilder(128);
            final StringBuilder downPathBuffer = new StringBuilder(128);
            String basePath = base.normalize().getPath();
            if (basePath.endsWith("/")) {
                basePath = basePath + "dummy";
            }
            String refPath = ref.normalize().getPath();
            final StringTokenizer baseTokenizer = new StringTokenizer(basePath, URI_SEPARATOR);
            final StringTokenizer refTokenizer = new StringTokenizer(refPath, URI_SEPARATOR);
    
            while (baseTokenizer.countTokens() > 1 && refTokenizer.countTokens() > 1) {
                final String baseToken = baseTokenizer.nextToken();
                final String refToken = refTokenizer.nextToken();
                //if OS is Windows, we need to ignore case when comparing path names.
                final boolean equals = OS_NAME.toLowerCase().contains(OS_NAME_WINDOWS)
                                       ? baseToken.equalsIgnoreCase(refToken)
                                       : baseToken.equals(refToken);
                if (!equals) {
                    if (baseToken.endsWith(COLON) || refToken.endsWith(COLON)) {
                        //the two files are in different disks under Windows
                        return ref;
                    }
                    upPathBuffer.append("..");
                    upPathBuffer.append(URI_SEPARATOR);
                    downPathBuffer.append(refToken);
                    downPathBuffer.append(URI_SEPARATOR);
                    break;
                }
            }
    
            while (baseTokenizer.countTokens() > 1) {
                baseTokenizer.nextToken();
                upPathBuffer.append("..");
                upPathBuffer.append(URI_SEPARATOR);
            }
    
            while (refTokenizer.hasMoreTokens()) {
                downPathBuffer.append(refTokenizer.nextToken());
                if (refTokenizer.hasMoreTokens()) {
                    downPathBuffer.append(URI_SEPARATOR);
                }
            }
            upPathBuffer.append(downPathBuffer);
            
            try {
                rel = new URI(null, null, upPathBuffer.toString(), null, null);
            } catch (final URISyntaxException e) {
                throw new IllegalArgumentException(e);
            }
        }
        
        return setFragment(rel, ref.getFragment());
    }
 
    /**
     * Get relative path to base path.
     * 
     * <p>For {@code foo/bar/baz.txt} return {@code ../../}</p>
     * 
     * @param relativePath relative URI
     * @return relative URI to base path, {@code null} if reference path was a single file
     */
    public static URI getRelativePath(final URI relativePath) {
        final StringTokenizer tokenizer = new StringTokenizer(relativePath.toString(), URI_SEPARATOR);
        final StringBuilder buffer = new StringBuilder();
        if (tokenizer.countTokens() == 1){
            return null;
        }else{
            while(tokenizer.countTokens() > 1){
                tokenizer.nextToken();
                buffer.append("..");
                buffer.append(URI_SEPARATOR);
            }
            return toURI(buffer.toString());
        }
    }

    public static boolean exists(final URI file) {
        if (file.getScheme() == null) {
            return new File(file.getPath()).exists();
        } else if ("file".equals(file.getScheme())) {
            return new File(file).exists();
        } else {
            // Assume non-file URIs always exists and force fetching them
            return true;
        }
    }
    
    public static URI addSuffix(final URI orig, final String suffix) {
        final String fragment = orig.getFragment();
        final String u = stripFragment(orig).toString();
        final int idx = u.lastIndexOf(".");
        final URI res;
        if (idx != -1) {
            res = toURI(u.toString().substring(0, idx) + suffix + u.toString().substring(idx));
        } else {
            res = toURI(u.toString() + suffix);
        }
        return setFragment(res, fragment);
    }
}