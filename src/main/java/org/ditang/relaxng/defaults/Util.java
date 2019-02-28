/*
 * This file is part of the DITA Open Toolkit project.
 *
 * Copyright 2011 George Bina
 *
 * See the accompanying LICENSE file for applicable license.
 */
package org.ditang.relaxng.defaults;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;

/**
 * Corrects the urls.
 */
public class Util {
  /**
   * Windows platform flag.
   */
  private static Boolean windows = null;

  /**
   * Checks for a Windows platform.
   * 
   * @return True if it is a win 32 platform.
   */
  private static boolean isWindows() {
    if (windows == null) {
      windows = System.getProperty("os.name").toUpperCase().startsWith("WIN");
    }
    return windows;
  }

    // which ASCII characters need to be escaped
    private static boolean[] gNeedEscaping = new boolean[128];
    // the first hex character if a character needs to be escaped
    private static char[] gAfterEscaping1 = new char[128];
    // the second hex character if a character needs to be escaped
    private static char[] gAfterEscaping2 = new char[128];
  private static char[] gHexChs = { '0', '1', '2', '3', '4', '5', '6', '7',
      '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
  // initialize the above 3 arrays
  static {
    for (int i = 0; i <= 0x1f; i++) {
      gNeedEscaping[i] = true;
      gAfterEscaping1[i] = gHexChs[i >> 4];
      gAfterEscaping2[i] = gHexChs[i & 0xf];
    }
    gNeedEscaping[0x7f] = true;
    gAfterEscaping1[0x7f] = '7';
    gAfterEscaping2[0x7f] = 'F';
    char[] escChs = { ' ', '<', '>', '#', '%', '"', '{', '}', '?', '|', '\\',
        '^', '~', '[', ']', '`', '\'', '&' };
    int len = escChs.length;
    char ch;
    for (char escCh : escChs) {
      ch = escCh;
      gNeedEscaping[ch] = true;
      gAfterEscaping1[ch] = gHexChs[ch >> 4];
      gAfterEscaping2[ch] = gHexChs[ch & 0xf];
    }
  }

/** 
   * To escape a file path to a URI, by using %HH to represent
   * special ASCII characters: 0x00~0x1F, 0x7F, ' ', '<', '>', '#', '%'
   * and '"' and non-ASCII characters (whose value >= 128).
   * 
   * '\' character will also be escaped.
   * 
   * @param path The path to be escaped.
   * @return The escaped uri.
   */
  private static String filepath2URI(String path) {
    // return null if path is null.
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
  private static String escapeSpecialAsciiAndNonAscii(String path) {
    int len = path.length(), ch;
    StringBuilder buffer = new StringBuilder(len * 3);
    // change C:/blah to /C:/blah
    if (len >= 2 && path.charAt(1) == ':') {
      ch = Character.toUpperCase(path.charAt(0));
      if (ch >= 'A' && ch <= 'Z') {
        buffer.append('/');
      }
    }
    // for each character in the path
    int i = 0;
    for (i = 0; i < len; i++) {
      ch = path.charAt(i);
      // if it's not an ASCII character, break here, and use UTF-8 encoding
      if (ch >= 128) {
        break;
      }
      if (gNeedEscaping[ch]) {
        buffer.append('%');
        buffer.append(gAfterEscaping1[ch]);
        buffer.append(gAfterEscaping2[ch]);
      } else {
        buffer.append((char) ch);
      }
    }
    // we saw some non-ascii character
    if (i < len) {
      // get UTF-8 bytes for the remaining sub-string
      byte[] bytes = null;
      byte b;
      bytes = path.substring(i).getBytes(StandardCharsets.UTF_8);
      len = bytes.length;
      // for each byte
      for (i = 0; i < len; i++) {
        b = bytes[i];
        // for non-ascii character: make it positive, then escape
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
          buffer.append((char) b);
        }
      }
    }
    return buffer.toString();
  }

  /**
   * Corrects an URL.
   * 
   * @param url
   *          The url to be corrected. If null will throw MalformedURLException.
   * @return a corrected URL. Never null.
   * @exception MalformedURLException
   *              when the argument is null.
   */
  public static URL correct(URL url) throws MalformedURLException {
    if (url == null) {
      throw new MalformedURLException("The url is null");
    }
    return new URL(correct(url.toString()));
  }

  /**
   * Method introduced to correct the URLs in the default machine encoding. This
   * was needed by the xsltproc, the catalogs URLs must be encoded in the
   * machine encoding.
   * 
   * @param url
   *          The URL to be corrected. If it contains a % char, it means it
   *          already was corrected, so it will be returned. Take care at
   *          composing URLs from a corrected part and an uncorrected part.
   *          Correcting the result will not work. Try to correct first the
   *          relative part.
   * @return The corrected URL.
   */
  private static String correct(String url) {
    // Fix for bad URLs containing UNC paths
    // If the url is a UNC file url it must be specified like:
    // file:////<PATH>...
    if (url.startsWith("file://")
    // A url like file:///<PATH> refers to a local file so it must not be
    // modified.
        && !url.startsWith("file:///") && isWindows()) {
      url = "file:////" + url.substring("file://".length());
    }

    String userInfo = getUserInfo(url);
    String user = extractUser(userInfo);
    String pass = extractPassword(userInfo);

    String initialUrl = url;
    // See if the url contains user and password. If so we remove them and
    // attach them back after the correction is performed.
    if (user != null || pass != null) {
      URL urlWithoutUserInfo = clearUserInfo(url);
      if (urlWithoutUserInfo != null) {
        url = clearUserInfo(url).toString();
      } else {
        // Possible a malformed URL
      }
    }

    // If there is a % that means the url was already corrected.
    if (url.contains("%")) {
      return initialUrl;
    }

    // Extract the reference (anchor) part from the url. The '#' char
    // identifying the anchor must not be corrected.
    String reference = null;
    int refIndex = url.lastIndexOf('#');
    if (refIndex != -1) {
      reference = filepath2URI(url.substring(refIndex + 1));
      url = url.substring(0, refIndex);
    }

    // Buffer where eventual query string will be processed.
    StringBuilder queryBuffer = null;

    int queryIndex = url.indexOf('?');
    if (queryIndex != -1) {
      // We have a query
      String query = url.substring(queryIndex + 1);
      url = url.substring(0, queryIndex);
      queryBuffer = new StringBuilder(query.length());
      // Tokenize by &
      StringTokenizer st = new StringTokenizer(query, "&");
      while (st.hasMoreElements()) {
        String token = st.nextToken();
        token = filepath2URI(token);
        // Correct token
        queryBuffer.append(token);
        if (st.hasMoreElements()) {
          queryBuffer.append("&");
        }
      }
    }

    String toReturn = filepath2URI(url);

    if (queryBuffer != null) {
      // Append to the end the corrected query.
      toReturn += "?" + queryBuffer.toString();
    }

    if (reference != null) {
      // Append the reference to the end the corrected query.
      toReturn += "#" + reference;
    }

    // Re-attach the user and password.
    if (user != null || pass != null) {
      try {
        if (user == null) {
          user = "";
        }
        if (pass == null) {
          pass = "";
        }
        // Re-attach user info.
        toReturn = attachUserInfo(new URL(toReturn), user, pass.toCharArray())
            .toString();
      } catch (MalformedURLException e) {
        // Shoudn't happen.
      }
    }
    return toReturn;
  }

  /**
   * Extract the user info from an URL.
   * 
   * @param url
   *          The string representing the URL.
   * @return The userinfo or null if cannot extract it.
   */
  private static String getUserInfo(String url) {
    String userInfo = null;
    int startIndex = Integer.MIN_VALUE;
    int nextSlashIndex = Integer.MIN_VALUE;
    int endIndex = Integer.MIN_VALUE;
    try {
      // The user info start index should be the first index of "//".
      startIndex = url.indexOf("//");
      if (startIndex != -1) {
        startIndex += 2;
        // The user info should be found before the next '/' index.
        nextSlashIndex = url.indexOf('/', startIndex);
        if (nextSlashIndex == -1) {
          nextSlashIndex = url.length();
        }
        // The user info ends at the last index of '@' from the previously
        // computed subsequence.
        endIndex = url.substring(startIndex, nextSlashIndex).lastIndexOf('@');
        if (endIndex != -1) {
          userInfo = url.substring(startIndex, startIndex + endIndex);
        }
      }
    } catch (StringIndexOutOfBoundsException ex) {
      System.err.println("String index out of bounds for:|" + url + "|");
      System.err.println("Start index: " + startIndex);
      System.err.println("Next slash index " + nextSlashIndex);
      System.err.println("End index :" + endIndex);
      System.err.println("User info :|" + userInfo + "|");
      ex.printStackTrace();
    }
    return userInfo;
  }

  /**
   * Gets the user from an userInfo string obtained from the starting URL. Used
   * only by the constructor.
   * 
   * @param userInfo
   *          userInfo, taken from the URL.
   * @return The user.
   */
  private static String extractUser(String userInfo) {
    if (userInfo == null) {
      return null;
    }

    int index = userInfo.lastIndexOf(':');
    if (index == -1) {
      return userInfo;
    } else {
      return userInfo.substring(0, index);
    }
  }

  /**
   * Gets the password from an user info string obtained from the starting URL.
   * 
   * @param userInfo
   *          userInfo, taken from the URL. If no user info specified returning
   *          null. If user info not null but no password after ":" then
   *          returning empty, (we have a user so the default pass for him is
   *          empty string). See bug 6086.
   * 
   * @return The password.
   */
  private static String extractPassword(String userInfo) {
    if (userInfo == null) {
      return null;
    }
    String password = "";
    int index = userInfo.lastIndexOf(':');
    if (index != -1 && index < userInfo.length() - 1) {
      // Extract password from the URL.
      password = userInfo.substring(index + 1);
    }
    return password;
  }

  /**
   * Clears the user info from an url.
   * 
   * @param systemID
   *          the url to be cleaned.
   * @return the cleaned url, or null if the argument is not an URL.
   */
  private static URL clearUserInfo(String systemID) {
    try {
      URL url = new URL(systemID);
      // Do not clear user info on "file" urls 'cause on Windows the drive will
      // have no ":"...
      if (!"file".equals(url.getProtocol())) {
        return attachUserInfo(url, null, null);
      }
      return url;
    } catch (MalformedURLException e) {
      return null;
    }
  }

  /**
   * Build the URL from the data obtained from the user.
   * 
   * @param url
   *          The URL to be transformed.
   * @param user
   *          The user name.
   * @param password
   *          The password.
   * @return The URL with userInfo.
   * @exception MalformedURLException
   *              Exception thrown if the URL is malformed.
   */
  private static URL attachUserInfo(URL url, String user, char[] password)
      throws MalformedURLException {
    if (url == null) {
      return null;
    }
    if ((url.getAuthority() == null || "".equals(url.getAuthority()))
        && !"jar".equals(url.getProtocol())) {
      return url;
    }

    StringBuilder buf = new StringBuilder();
    String protocol = url.getProtocol();

    if (protocol.equals("jar")) {
      URL newURL = new URL(url.getPath());
      newURL = attachUserInfo(newURL, user, password);
      buf.append("jar:");
      buf.append(newURL.toString());
    } else {

      password = correctPassword(password);

      user = correctUser(user);

      buf.append(protocol);
      buf.append("://");
      if (!"file".equals(protocol) && user != null && user.trim().length() > 0) {
        buf.append(user);
        if (password != null && password.length > 0) {
          buf.append(":");
          buf.append(password);
        }
        buf.append("@");
      }
      buf.append(url.getHost());
      if (url.getPort() > 0) {
        buf.append(":");
        buf.append(url.getPort());
      }
      buf.append(url.getPath());
      String query = url.getQuery();
      if (query != null && query.trim().length() > 0) {
        buf.append("?").append(query);
      }
      String ref = url.getRef();
      if (ref != null && ref.trim().length() > 0) {
        buf.append("#").append(ref);
      }
    }
    return new URL(buf.toString());
  }

  /**
   * Escape the specified user.
   * 
   * @param user
   *          The user name to correct.
   * @return The escaped user.
   */
  private static String correctUser(String user) {
    if (user != null && user.trim().length() > 0
        && (false || user.indexOf('%') == -1)) {
      String escaped = escapeSpecialAsciiAndNonAscii(user);
      StringBuilder totalEscaped = new StringBuilder();
      for (int i = 0; i < escaped.length(); i++) {
        char ch = escaped.charAt(i);
        if (ch == '@' || ch == '/' || ch == ':') {
          totalEscaped.append('%')
              .append(Integer.toHexString(ch).toUpperCase());
        } else {
          totalEscaped.append(ch);
        }
      }
      user = totalEscaped.toString();
    }
    return user;
  }

  /**
   * Escape the specified password.
   * 
   * @param password
   *          The password to be corrected.
   * 
   * @return The escaped password.
   */
  private static char[] correctPassword(char[] password) {
    if (password != null && new String(password).indexOf('%') == -1) {
      String escaped = escapeSpecialAsciiAndNonAscii(new String(password));
      StringBuilder totalEscaped = new StringBuilder();
      for (int i = 0; i < escaped.length(); i++) {
        char ch = escaped.charAt(i);
        if (ch == '@' || ch == '/' || ch == ':') {
          totalEscaped.append('%')
              .append(Integer.toHexString(ch).toUpperCase());
        } else {
          totalEscaped.append(ch);
        }
      }
      password = totalEscaped.toString().toCharArray();
    }
    return password;
  }
}