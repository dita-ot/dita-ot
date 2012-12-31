<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file for 
     applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2005, 2006 All Rights Reserved. -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" xmlns:java="org.dita.dost.util.ImgUtils" exclude-result-prefixes="java">

<xsl:template match="*[contains(@class,' topic/fig ')]">
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/fig ')]/*[contains(@class,' topic/title ')]">
<xsl:variable name="ancestorlang">
<xsl:call-template name="getLowerCaseLang"/>
</xsl:variable>
<xsl:variable name="fig-count-actual" select="count(preceding::*[contains(@class,' topic/fig ')]/*[contains(@class,' topic/title ')])+1"><!-- Number of fig/title's including this one --></xsl:variable>
<xsl:call-template name="gen-id"/>\pard \plain\s9 \qc\f4\fs24\b <xsl:if test="ancestor::*[contains(@class,' topic/table ') or contains(@class,' topic/simpletable ')]">\intbl </xsl:if><xsl:choose><!-- Hungarian: "1. Figure " --><xsl:when test="( (string-length($ancestorlang)=5 and contains($ancestorlang,'hu-hu')) or (string-length($ancestorlang)=2 and contains($ancestorlang,'hu')) )"><xsl:value-of select="$fig-count-actual"/><xsl:text>. </xsl:text><xsl:call-template name="getStringRTF"><xsl:with-param name="stringName" select="'Figure'"/></xsl:call-template><xsl:text> </xsl:text></xsl:when><xsl:otherwise><xsl:call-template name="getStringRTF"><xsl:with-param name="stringName" select="'Figure'"/></xsl:call-template><xsl:text> </xsl:text><xsl:value-of select="$fig-count-actual"/><xsl:text>. </xsl:text></xsl:otherwise></xsl:choose><xsl:value-of select="."/>\par \plain\s0 \qj\f2\fs24
</xsl:template>

<xsl:template match="*[contains(@class,' topic/fig ')]/*[contains(@class,' topic/desc ')]">
<xsl:call-template name="gen-id"/>\pard \plain\s0 \f2\fs24<xsl:if test="ancestor::*[contains(@class,' topic/table ') or contains(@class,' topic/simpletable ')]">\intbl </xsl:if><xsl:apply-templates/>\par \plain\s0 \f2\fs24
</xsl:template>

<xsl:template match="*[contains(@class,' topic/image ')]">
<xsl:if test="@href and not(@href='')">
  <xsl:variable name="type">
    <xsl:choose>
      <xsl:when test="not(contains(@href,'://'))">
        <xsl:call-template name="getType">
         <xsl:with-param name="file" select="string(@href)"/>
       </xsl:call-template>
      </xsl:when>
      <xsl:otherwise/>
    </xsl:choose>
  </xsl:variable>
  <xsl:variable name="height">
    <xsl:choose>
      <xsl:when test="not(contains(@href,'://'))">
        <xsl:value-of select="java:getHeight($OUTPUTDIR, string(@href))"/>
      </xsl:when>
      <xsl:otherwise/>
    </xsl:choose>
  </xsl:variable>
  <xsl:variable name="width">
    <xsl:choose>
      <xsl:when test="not(contains(@href,'://'))">
        <xsl:value-of select="java:getWidth($OUTPUTDIR, string(@href))"/>
      </xsl:when>
      <xsl:otherwise/>
    </xsl:choose>
  </xsl:variable>

<xsl:call-template name="gen-id"/>
<xsl:choose>
  <xsl:when test="not(contains(@href,'://')) and $type and not($type='other') and ($height &gt; 0) and ($width &gt; 0)">
    <xsl:text>{\*\shppict {\pict \picw</xsl:text><xsl:value-of select="$width"/>\pich<xsl:value-of
    select="$height"/>\<xsl:value-of select="$type"/><xsl:text> </xsl:text><xsl:value-of
      select="java:getBinData($OUTPUTDIR, string(@href))"/><xsl:text>}}</xsl:text>
  </xsl:when>
  <xsl:otherwise>{\field{\*\fldinst {\s8 \f2\fs24\ul\cf1 HYPERLINK "<xsl:value-of select="@href"/>"}}{\fldrslt {\s8 \f2\fs24\ul\cf1 <xsl:call-template name="gen-img-txt"/>\s8 \f2\fs24\ul\cf1}}}</xsl:otherwise>
</xsl:choose>
</xsl:if>
</xsl:template>

<xsl:template name="gen-img-txt">
<xsl:choose>
<xsl:when test="*[contains(@class,' topic/alt ')]">
<xsl:text>[PIC]</xsl:text><xsl:value-of select="*[contains(@class,' topic/alt ')]"/>
</xsl:when>
<xsl:when test="@alt and not(@alt='')"><xsl:text>[PIC]</xsl:text><xsl:value-of select="@alt"/></xsl:when>
<xsl:when test="text() or *"><xsl:text>[PIC]</xsl:text><xsl:apply-templates/></xsl:when>
<xsl:otherwise><xsl:text>[PIC]</xsl:text><xsl:value-of select="@href"/></xsl:otherwise>
</xsl:choose>
</xsl:template>

  <xsl:template name="getType">
    <xsl:param name="file"/>
    <xsl:variable name="f">
      <xsl:call-template name="convert-to-lower">
        <xsl:with-param name="inputval" select="$file"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="substring($f, string-length($f) - 3) = '.jpg' or
                      substring($f, string-length($f) - 4) = '.jpg'">
        <xsl:text>jpegblip</xsl:text>
      </xsl:when>
      <xsl:when test="substring($f, string-length($f) - 3) = '.gif' or
                      substring($f, string-length($f) - 3) = '.png'">
        <xsl:text>pngblip</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>other</xsl:text>
        <xsl:call-template name="output-message">
          <xsl:with-param name="msgcat" select="'DOTJ'"/>
          <xsl:with-param name="msgnum" select="'024'"/>
          <xsl:with-param name="msgsev" select="'W'"/>
          <xsl:with-param name="msgparams" select="concat('%1=', $file)"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>