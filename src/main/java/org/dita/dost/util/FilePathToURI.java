/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2010 IBM Corporation
 *
 * See the accompanying LICENSE file for applicable license.

 */
package org.dita.dost.util;

import java.nio.charset.StandardCharsets;

/**
 * Used to escape a file path to a URI.
 */
final class FilePathToURI {

    /**
     * Private default constructor to make class uninstantiable.
     */
    private FilePathToURI() {
    }

    // Which ASCII characters need to be escaped
    private static final boolean[] gNeedEscaping = new boolean[128];
    // The first hex character if a character needs to be escaped
    private static final char[] gAfterEscaping1 = new char[128];
    // The second hex character if a character needs to be escaped
    private static final char[] gAfterEscaping2 = new char[128];
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
        final char[] escChs = {' ', '<', '>', '#', '%', '"', '{', '}', '?',
                '|', '\\', '^', '~', '[', ']', '`', '\'', '&'};
        char ch;
        for (char escCh : escChs) {
            ch = escCh;
            gNeedEscaping[ch] = true;
            gAfterEscaping1[ch] = gHexChs[ch >> 4];
            gAfterEscaping2[ch] = gHexChs[ch & 0xf];
        }
    }

    /** To escape a file path to a URI, by using %HH to represent special ASCII characters:
     * 0x00~0x1F, 0x7F, ' ', '<', '>', '#', '%' and '"' and non-ASCII characters
     * (whose value >= 128).
     *
     * '\' character will also be escaped.
     *
     * @param path The path to be escaped.
     * @return The escaped URI.
     */
    public static String filepath2URI(String path) {
        // Return null if path is null.
        if (path == null) {
            return null;
        }
        path = escapeSpecialAsciiAndNonAscii(path);

        return path;
    }

    /**
     * To escape a file path to a URI, by using %HH to represent
     * special ASCII characters: 0x00~0x1F, 0x7F, ' ', '<', '>', '#', '%'
     * and '"' and non-ASCII characters (whose value >= 128).
     *
     * @param path The path to be escaped.
     * @return The escaped path.
     */
    private static String escapeSpecialAsciiAndNonAscii(final String path) {
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
            if (ch >= 128) {
                break;
            }
            if (gNeedEscaping[ch]) {
                buffer.append('%');
                buffer.append(gAfterEscaping1[ch]);
                buffer.append(gAfterEscaping2[ch]);
                // Record the fact that it's escaped
            } else {
                buffer.append((char)ch);
            }
        }

        // We saw some non-ASCII character
        if (i < len) {
            // Get UTF-8 bytes for the remaining sub-string
            byte[] bytes;
            byte b;
            bytes = path.substring(i).getBytes(StandardCharsets.UTF_8);
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
                } else if (gNeedEscaping[b]) {
                    buffer.append('%');
                    buffer.append(gAfterEscaping1[b]);
                    buffer.append(gAfterEscaping2[b]);
                } else {
                    buffer.append((char)b);
                }
            }
        }
        return buffer.toString();
    }
}