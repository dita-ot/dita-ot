<?xml version="1.0" encoding="UTF-8" ?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file for 
     applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<xsl:stylesheet version="1.0"
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     xmlns:ditamsg="http://dita-ot.sourceforge.net/ns/200704/ditamsg"
     exclude-result-prefixes="ditamsg">

<!-- KEYREF-FILE is defined in dita2htmlImpl.xsl: -->
<!--<xsl:param name="KEYREF-FILE" select="concat($WORKDIR,$PATH2PROJ,'keydef.xml')"/>-->

<xsl:template match="*[contains(@class,' abbrev-d/abbreviated-form ')]" name="topic.abbreviated-form">
  <xsl:variable name="keys" select="@keyref"/>
  <xsl:variable name="target" select="$keydefs//*[@keys = $keys and normalize-space(@href)]/@href"/>
  <xsl:if test="$keys and $target">
    <xsl:variable name="entry-file" select="concat($WORKDIR, $PATH2PROJ, $target)"/>
    <xsl:variable name="entry-file-contents" select="document($entry-file, /)"/>
    <xsl:choose>
      <xsl:when test="$entry-file-contents//*[contains(@class,' glossentry/glossentry ')]">
        <!-- Fall back to process with normal term rules -->
        <xsl:call-template name="topic.term"/>
      </xsl:when>
      <xsl:otherwise>
        <!-- TODO: Throw a warning for incorrect usage of <abbreviated-form> -->
        <xsl:apply-templates select="." mode="ditamsg:no-glossentry-for-abbreviated-form">
          <xsl:with-param name="keys" select="$keys"/>
        </xsl:apply-templates>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:if>
</xsl:template>

<xsl:template match="*" mode="ditamsg:no-glossentry-for-abbreviated-form">
  <xsl:param name="keys"/>
  <xsl:call-template name="output-message">
    <xsl:with-param name="msgnum">060</xsl:with-param>
    <xsl:with-param name="msgsev">W</xsl:with-param>
    <xsl:with-param name="msgparams">%1=<xsl:value-of select="$keys"/></xsl:with-param>
  </xsl:call-template>
</xsl:template>

</xsl:stylesheet>
