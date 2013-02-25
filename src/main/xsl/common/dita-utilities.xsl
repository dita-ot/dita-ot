<?xml version="1.0" encoding="utf-8"?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file for 
     applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<!-- Common utilities that can be used by DITA transforms -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:param name="DEFAULTLANG">en-us</xsl:param>
  <!-- Function to convert a string to lower case -->
  
  <xsl:variable name="pixels-per-inch" select="number(96)"/>
  
  <xsl:template name="convert-to-lower">
    <xsl:param name="inputval"/>
    <xsl:value-of
      select="translate($inputval,                                     '._-abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ+=!@#$%^&amp;*()[]{};:\/&lt;&gt;,~?',                                     '._-abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz+=!@#$%^&amp;*()[]{};:\/&lt;&gt;,~?')"
    />
  </xsl:template>
  <!-- Function to determine the current language, and return it in lower case -->
  <xsl:template name="getLowerCaseLang">
    <xsl:variable name="ancestorlangUpper">
      <!-- the current xml:lang value (en-us if none found) -->
      <xsl:choose>
        <xsl:when test="ancestor-or-self::*/@xml:lang">
          <xsl:value-of select="ancestor-or-self::*[@xml:lang][1]/@xml:lang"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$DEFAULTLANG"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:call-template name="convert-to-lower">
      <!-- ensure lowercase for comparisons -->
      <xsl:with-param name="inputval" select="$ancestorlangUpper"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="*" mode="get-first-topic-lang">
    <xsl:variable name="first-topic-lang">
      <xsl:choose>
        <xsl:when test="/*[@xml:lang]"><xsl:value-of select="/*/@xml:lang"/></xsl:when>
        <xsl:when test="/dita/*[@xml:lang]"><xsl:value-of select="/dita/*[@xml:lang][1]/@xml:lang"/></xsl:when>
        <xsl:otherwise><xsl:value-of select="$DEFAULTLANG"/></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:call-template name="convert-to-lower">
      <xsl:with-param name="inputval" select="$first-topic-lang"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template match="*" mode="get-render-direction">
    <xsl:param name="lang">
      <xsl:apply-templates select="/*" mode="get-first-topic-lang"/>
    </xsl:param>
    <xsl:choose>
      <xsl:when test="$lang='ar-eg' or $lang='ar'">rtl</xsl:when>
      <xsl:when test="$lang='he-il' or $lang='he'">rtl</xsl:when>
      <xsl:when test="$lang='ur-pk' or $lang='ur'">rtl</xsl:when>
      <xsl:otherwise>ltr</xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Function to get translated text for a common string.
     * Each language is stored in a unique file. The association between a language and
     its translations is stored in $stringFileList.
     * Default file associations are in strings.xml.
     * Once the file for a language is found, look for the translation in that file.
     * If the correct file or translation are not found, use the default language.

     If adding translations for a specialization, create a new version of strings.xml,
     to indicate which languages are supported, and the name of each language file.
     When calling this template, pass in the new association file as $stringFileList.

     To reset the default language, import this template, and then set the DEFAULTLANG
     parameter in the importing topic. Or, just pass it in on the command line.
      -->
  <xsl:template name="getString">
    <xsl:param name="stringName"/>
    <xsl:param name="stringFileList" select="document('allstrings.xml')/allstrings/stringfile"/>
    <xsl:param name="stringFile">#none#</xsl:param>
    <xsl:param name="ancestorlang">
      <!-- Get the current language -->
      <xsl:call-template name="getLowerCaseLang"/>
    </xsl:param>
    <xsl:choose>
      <xsl:when test="$stringFile != '#none#'">
        <!-- Use the old getString template interface -->
        <!-- Get the translated string -->
        <xsl:variable name="str"
          select="$stringFile/strings/str[@name=$stringName][lang($ancestorlang)]"/>
        <xsl:choose>
          <!-- If the string was found, use it. Cannot test $str, because value could be empty. -->
          <xsl:when test="$stringFile/strings/str[@name=$stringName][lang($ancestorlang)]">
            <xsl:value-of select="$str"/>
          </xsl:when>
          <!-- If the current language is not the default language, try the default -->
          <xsl:when test="$ancestorlang!=$DEFAULTLANG">
            <!-- Determine which file holds the defaults; then get the default translation. -->
            <xsl:variable name="str-default"
              select="$stringFile/strings/str[@name=$stringName][lang($DEFAULTLANG)]"/>
            <xsl:choose>
              <!-- If a default was found, use it, but warn that fallback was needed.-->
              <xsl:when test="string-length($str-default)>0">
                <xsl:value-of select="$str-default"/>
                <xsl:call-template name="output-message">
                  <xsl:with-param name="msgnum">001</xsl:with-param>
                  <xsl:with-param name="msgsev">W</xsl:with-param>
                  <xsl:with-param name="msgparams">%1=<xsl:value-of select="$stringName"/>;%2=<xsl:value-of select="$ancestorlang"/>;%3=<xsl:value-of select="$DEFAULTLANG"/></xsl:with-param>
                </xsl:call-template>
              </xsl:when>
              <!-- Translation was not even found in the default language. -->
              <xsl:otherwise>
                <xsl:value-of select="$stringName"/>
                <xsl:call-template name="output-message">
                  <xsl:with-param name="msgnum">052</xsl:with-param>
                  <xsl:with-param name="msgsev">W</xsl:with-param>
                  <xsl:with-param name="msgparams">%1=<xsl:value-of select="$stringName"/></xsl:with-param>
                </xsl:call-template>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <!-- The current language is the default; no translation found at all. -->
          <xsl:otherwise>
            <xsl:value-of select="$stringName"/>
            <xsl:call-template name="output-message">
              <xsl:with-param name="msgnum">052</xsl:with-param>
              <xsl:with-param name="msgsev">W</xsl:with-param>
              <xsl:with-param name="msgparams">%1=<xsl:value-of select="$stringName"/></xsl:with-param>
            </xsl:call-template>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <!-- Use the new getString template interface -->
        <!-- Determine which file holds translations for the current language -->
        <xsl:variable name="stringfile"
            select="document($stringFileList)/*/lang[@xml:lang=$ancestorlang]/@filename"/>
        <!-- Get the translated string -->
        <xsl:variable name="str" select="document($stringfile)/strings/str[@name=$stringName]"/>
        <xsl:choose>
          <!-- If the string was found, use it. -->
          <xsl:when test="count($str) &gt; 0">
            <xsl:value-of select="$str[last()]"/>
          </xsl:when>
          <!-- If the current language is not the default language, try the default -->
          <xsl:when test="$ancestorlang!=$DEFAULTLANG">
            <!-- Determine which file holds the defaults; then get the default translation. -->
            <xsl:variable name="backupstringfile"
                select="document($stringFileList)/*/lang[@xml:lang=$DEFAULTLANG]/@filename"/>
            <xsl:variable name="str-default"
              select="document($backupstringfile)/strings/str[@name=$stringName]"/>
            <xsl:choose>
              <!-- If a default was found, use it, but warn that fallback was needed.-->
              <xsl:when test="count($str-default) &gt; 0">
                <xsl:value-of select="$str-default[last()]"/>
                <xsl:call-template name="output-message">
                  <xsl:with-param name="msgnum">001</xsl:with-param>
                  <xsl:with-param name="msgsev">W</xsl:with-param>
                  <xsl:with-param name="msgparams">%1=<xsl:value-of select="$stringName"/>;%2=<xsl:value-of select="$ancestorlang"/>;%3=<xsl:value-of select="$DEFAULTLANG"/></xsl:with-param>
                </xsl:call-template>
              </xsl:when>
              <!-- Translation was not even found in the default language. -->
              <xsl:otherwise>
                <xsl:value-of select="$stringName"/>
                <xsl:call-template name="output-message">
                  <xsl:with-param name="msgnum">052</xsl:with-param>
                  <xsl:with-param name="msgsev">W</xsl:with-param>
                  <xsl:with-param name="msgparams">%1=<xsl:value-of select="$stringName"/></xsl:with-param>
                </xsl:call-template>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:when>
          <!-- The current language is the default; no translation found at all. -->
          <xsl:otherwise>
            <xsl:value-of select="$stringName"/>
            <xsl:call-template name="output-message">
              <xsl:with-param name="msgnum">052</xsl:with-param>
              <xsl:with-param name="msgsev">W</xsl:with-param>
              <xsl:with-param name="msgparams">%1=<xsl:value-of select="$stringName"/></xsl:with-param>
            </xsl:call-template>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
    
  <xsl:template name="length-to-pixels">
    <xsl:param name="dimen"/>
    <!-- We handle units of cm, mm, in, pt, pc, px.  We also accept em,
      but just treat 1em=1pc.  An omitted unit is taken as px. -->
    
    <xsl:variable name="dimenx" select="concat('00',$dimen)"/>
    <xsl:variable name="units" select="substring($dimenx,string-length($dimenx)-1)"/>
    <xsl:variable name="numeric-value" select="number(substring($dimenx,1,string-length($dimenx)-2))"/>
    <xsl:choose>
      <xsl:when test="string(number($units))!='NaN' and string(number($numeric-value))!='NaN'">
        <!-- Since $units is a number, the input was unitless, so we default
          the unit to pixels and just return the input value -->
        <xsl:value-of select="round(number(concat($numeric-value,$units)))"/>
      </xsl:when>
      <xsl:when test="string(number($numeric-value))='NaN'">
        <!-- If the input isn't valid, just return 100% -->
        <xsl:value-of select="'100%'"/>
      </xsl:when>
      <xsl:when test="$units='cm'">
        <xsl:value-of select="number(round($numeric-value * $pixels-per-inch div 2.54))"/>
      </xsl:when>
      <xsl:when test="$units='mm'">
        <xsl:value-of select="number(round($numeric-value * $pixels-per-inch div 25.4))"/>
      </xsl:when>
      <xsl:when test="$units='in'">
        <xsl:value-of select="number(round($numeric-value * $pixels-per-inch))"/>
      </xsl:when>
      <xsl:when test="$units='pt'">
        <xsl:value-of select="number(round($numeric-value * $pixels-per-inch div 72))"/>
      </xsl:when>
      <xsl:when test="$units='pc'">
        <xsl:value-of select="number(round($numeric-value * $pixels-per-inch div 6))"/>
      </xsl:when>
      <xsl:when test="$units='px'">
        <xsl:value-of select="number(round($numeric-value))"/>
      </xsl:when>
      <xsl:when test="$units='em'">
        <xsl:value-of select="number(round($numeric-value * $pixels-per-inch div 6))"/>
      </xsl:when>
      <xsl:otherwise>
        <!-- If the input isn't valid, just return 100% -->
        <xsl:value-of select="'100%'"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!-- replace all the blank in file name or directory with %20 -->
  <xsl:template name="replace-blank">
    <xsl:param name="file-origin"></xsl:param>
    <xsl:choose>
      <xsl:when test="contains($file-origin,' ')">
        <xsl:call-template name="replace-blank">
          <xsl:with-param name="file-origin">
            <xsl:value-of select="substring-before($file-origin,' ')"/>%20<xsl:value-of select="substring-after($file-origin,' ')"/>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$file-origin"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
<!-- Return the portion of an HREF value up to the file's extension. This assumes
     that the file has an extension, and that the topic and/or element ID does not
     contain a period. Written to allow references such as com.example.dita.files/file.dita#topic -->
<!-- Deprecated: use replace-extension instead -->
<xsl:template match="*" mode="parseHrefUptoExtension">
  <xsl:param name="href" select="@href"/>
  <xsl:variable name="uptoDot"><xsl:value-of select="substring-before($href,'.')"/></xsl:variable>
  <xsl:variable name="afterDot"><xsl:value-of select="substring-after($href,'.')"/></xsl:variable>
  <xsl:value-of select="$uptoDot"/>
  <xsl:choose>
    <!-- No more periods, so this is at the extension -->
    <xsl:when test="not(contains($afterDot,'.'))"/>
    <!-- Multiple slashes; at least one must be a directory, so it's before the extension -->
    <xsl:when test="contains(substring-after($afterDot,'/'),'/')">
      <xsl:text>.</xsl:text>
      <xsl:value-of select="substring-before($afterDot,'/')"/>
      <xsl:text>/</xsl:text>
      <xsl:apply-templates select="." mode="parseHrefUptoExtension"><xsl:with-param name="href" select="substring-after($afterDot,'/')"/></xsl:apply-templates>
    </xsl:when>
    <!-- Multiple periods, no slashes, no topic or element ID, so the file name contains more periods -->
    <xsl:when test="not(contains($afterDot,'#'))">
      <xsl:text>.</xsl:text>
      <xsl:apply-templates select="." mode="parseHrefUptoExtension"><xsl:with-param name="href" select="$afterDot"/></xsl:apply-templates>
    </xsl:when>
    <!-- Multiple periods, no slashes, with #. Move to next period. Needs additional work to support
         IDs containing periods. -->
    <xsl:otherwise>
      <xsl:text>.</xsl:text>
      <xsl:apply-templates select="." mode="parseHrefUptoExtension"><xsl:with-param name="href" select="$afterDot"/></xsl:apply-templates>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>
  
  <!-- Template returns "true" if $text parameter string ends with the $with parameter string, and otherwise returns "false". -->
  <xsl:template name="ends-with">
    <xsl:param name="text"/>
    <xsl:param name="with"/>
    <xsl:value-of select="substring($text, string-length($text) - string-length($with) + 1) = $with"/>
  </xsl:template>

  <!-- Get filename base -->
  <xsl:template name="getFileName">
    <xsl:param name="filename"/>
    <xsl:param name="extension"/>
    <xsl:choose>
      <xsl:when test="contains($filename, $extension)">
        <xsl:call-template name="substring-before-last">
          <xsl:with-param name="text" select="$filename"/>
          <xsl:with-param name="delim" select="$extension"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$filename"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!-- Replace file extension in a URI -->
  <xsl:template name="replace-extension">
    <xsl:param name="filename"/>
    <xsl:param name="extension"/>
    <xsl:param name="ignore-fragment" select="false()"/>
    <xsl:variable name="f">
      <xsl:call-template name="substring-before-last">
        <xsl:with-param name="text">
          <xsl:choose>
            <xsl:when test="contains($filename, '#')">
              <xsl:value-of select="substring-before($filename, '#')"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$filename"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:with-param>
        <xsl:with-param name="delim" select="'.'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:if test="string($f)">
      <xsl:value-of select="concat($f, $extension)"/>  
    </xsl:if>
    <xsl:if test="not($ignore-fragment) and contains($filename, '#')">
      <xsl:value-of select="concat('#', substring-after($filename, '#'))"/>
    </xsl:if>
  </xsl:template>
  
  <xsl:template name="substring-before-last">
    <xsl:param name="text"/>
    <xsl:param name="delim"/>
    
    <xsl:if test="string($text) and string($delim)">
      <xsl:value-of select="substring-before($text, $delim)" />
      <xsl:variable name="tail" select="substring-after($text, $delim)" />
      <xsl:if test="contains($tail, $delim)">
        <xsl:value-of select="$delim" />
        <xsl:call-template name="substring-before-last">
          <xsl:with-param name="text" select="$tail" />
          <xsl:with-param name="delim" select="$delim" />
        </xsl:call-template>
      </xsl:if>
    </xsl:if>
  </xsl:template>

  <xsl:template match="processing-instruction('workdir-uri')" mode="get-work-dir">
    <xsl:value-of select="."/>
  </xsl:template>

  <xsl:template match="processing-instruction('path2project-uri')" mode="get-path2project">
    <xsl:choose>
      <!-- Backwards compatibility with path2project that is empty when current directory is the root directory -->
      <xsl:when test=". = './'"/>
      <xsl:otherwise>
        <xsl:value-of select="."/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="processing-instruction('path2project')" mode="get-path2project">
    <xsl:call-template name="get-path2project">
      <xsl:with-param name="s" select="."/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="get-path2project">
    <!-- Deal with being handed a Windows backslashed path by accident. -->
    <!-- This code only changes \ to / and doesn't handle the many other situations
         where a URI differs from a file path.  Hopefully they don't occur in path2proj anyway. -->
    <xsl:param name="s"/>
    <xsl:choose>
      <xsl:when test="contains($s, '\')">
        <xsl:value-of select="substring-before($s, '\')"/>
        <xsl:text>/</xsl:text>
        <xsl:call-template name="get-path2project">
          <xsl:with-param name="s" select="substring-after($s, '\')"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$s"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>

