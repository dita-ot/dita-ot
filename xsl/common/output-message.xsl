<?xml version="1.0" encoding="UTF-8" ?>
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<!--
        Standard error message template for DITA processing in XSL. This
        file should be included by any XSL program that uses the standard
        message template. To include this file, you will need the following
        two commands in your XSL:

<xsl:include href="output-message.xsl"/>           - Place with other included files

<xsl:variable name="msgprefix">IDXS</xsl:variable> - Place with other variables


        The template takes in the following parameters:
        - msg    = the message to print in the log; default=***
        - msgnum = the message number (3 digits); default=000
        - msgsev = the severity (I, W, E, or F); default=I (Informational)
-->


<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template name="output-message">
  <xsl:param name="msg" select="***"/>
  <xsl:param name="msgnum" select="000"/>
  <xsl:param name="msgsev" select="I"/>
  <xsl:variable name="localclass"><xsl:value-of select="@class"/></xsl:variable>
  <xsl:variable name="debugloc">
   <!-- Information on how to find the error; file name, followed by element counter: -->
   <!-- (File = filename.dita, Element = searchtitle:1) -->
   <xsl:if test="@xtrf|@xtrc">
     <xsl:text>(</xsl:text>
     <xsl:if test="@xtrf">
       <xsl:text>File = </xsl:text><xsl:value-of select="@xtrf"/>
       <xsl:if test="@xtrc"><xsl:text>, </xsl:text></xsl:if>
     </xsl:if>
     <xsl:if test="@xtrc"><xsl:text>Element = </xsl:text><xsl:value-of select="@xtrc"/></xsl:if>
     <xsl:text>)</xsl:text>
   </xsl:if>
  </xsl:variable>
  <xsl:message><xsl:text>------------------------------------------------------------------
</xsl:text>
    <xsl:value-of select="$msgprefix"/><xsl:value-of select="$msgnum"/>
    <xsl:choose>
      <xsl:when test="$msgsev='I'">I Information: </xsl:when>
      <xsl:when test="$msgsev='W'">W Warning: </xsl:when>
      <xsl:when test="$msgsev='E'">E Error: </xsl:when>
      <xsl:when test="$msgsev='F'">F Fatal: </xsl:when>
      <xsl:otherwise>I Information: </xsl:otherwise>
    </xsl:choose>
    <xsl:value-of select="$debugloc"/>     <!-- Debug location, followed by a newline -->
    <xsl:text>
</xsl:text>
    <xsl:value-of select="$msg"/>          <!-- Error message, followed by a newline -->
    <xsl:text>
</xsl:text>
  </xsl:message>
</xsl:template>

</xsl:stylesheet>
