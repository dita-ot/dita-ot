/*
 * This file is part of the DITA Open Toolkit project hosted on
 * Sourceforge.net. See the accompanying license.txt file for
 * applicable licenses.
 */

/*
 * (c) Copyright IBM Corp. 2010 All Rights Reserved.
 */
package org.dita.dost.util;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
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
        return new URL(correct(file.toURL().toString(), true));
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
     * Decode UTF8/URL encoded strings.
     * 
     * @param s
     *            the string to be decoded
     * @return the decoded string
     */
    public static String uncorrect(final String s) {
        if (s == null) {
            return null;
        }
        if (s.indexOf('%') == -1) {
            // Optimization, nothing to uncorrect here
            return s;
        }
        final StringBuffer sbuf = new StringBuffer();
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
    public static String correct(String url, final boolean forceCorrection) {
        if (url == null) {
            return null;
        }

        final String initialUrl = url;

        // If there is a % that means the URL was already corrected.
        if (!forceCorrection && url.indexOf("%") != -1) {
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
        StringBuffer queryBuffer = null;
        if (!forceCorrection) {
            final int queryIndex = url.indexOf('?');
            if (queryIndex != -1) {
                // We have a query
                final String query = url.substring(queryIndex + 1);
                url = url.substring(0, queryIndex);
                queryBuffer = new StringBuffer(query.length());
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
     * @author Zhang Di Hua
     */
    public static String getURL(final String fileName){

        if(fileName.startsWith("file:/")){
            return fileName;
        }else{
            final File file = new File(fileName);
            try {
                return file.toURI().toURL().toString();
            } catch (final MalformedURLException e) {
                // TODO Auto-generated catch block
                //e.printStackTrace();
                return "";
            }
        }

    }

}