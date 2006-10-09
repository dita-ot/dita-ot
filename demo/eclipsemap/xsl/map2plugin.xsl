<?xml version="1.0"?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
  Sourceforge.net. See the accompanying license.txt file for 
  applicable licenses.-->
  <!--
    | (C) Copyright IBM Corporation 2006. All Rights Reserved.
    *-->
<!-- Need to ensure this comes out with the name "plugin.xml" rather than the default.
     So: use saxon to force the plugin name. -->

<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:param name="PLUGINFILE" select="'plugin.xml'"/>
  <xsl:param name="DITAMAPEXT" select="'ditamap'"/>
  <xsl:param name="FULL-DITAMAPEXT">
    <xsl:choose>
      <xsl:when test="starts-with($DITAMAPEXT,'.')"><xsl:value-of select="$DITAMAPEXT"/></xsl:when>
      <xsl:otherwise>.<xsl:value-of select="$DITAMAPEXT"/></xsl:otherwise>
    </xsl:choose>
  </xsl:param>
  <xsl:param name="DITAEXT" select="'dita'"/>
  <xsl:param name="FULL-DITAEXT">
    <xsl:choose>
      <xsl:when test="starts-with($DITAEXT,'.')"><xsl:value-of select="$DITAEXT"/></xsl:when>
      <xsl:otherwise>.<xsl:value-of select="$DITAEXT"/></xsl:otherwise>
    </xsl:choose>
  </xsl:param>

  <xsl:param name="DEFAULTINDEX" select="''"/>

  <xsl:variable name="newline"><xsl:text>
</xsl:text></xsl:variable>

  <xsl:output encoding="utf-8" indent="yes" method="xml"/>

  <!--<xsl:template match="*[contains(@class,' eclipsemap/plugin ')]//*"/>-->

  <xsl:template match="*[contains(@class,' eclipsemap/plugin ')]">
      <xsl:value-of select="$newline"/>
      <plugin>
        <xsl:apply-templates select="@id"/>
        <xsl:apply-templates select="*[contains(@class,' eclipsemap/pluginmeta ')]/*[contains(@class,' eclipsemap/plugininfo ')]/*[contains(@class,' eclipsemap/pluginname ')]" mode="plugin"/>
        <xsl:apply-templates select="*[contains(@class,' eclipsemap/pluginmeta ')]/*[contains(@class,' eclipsemap/providerName ')]" mode="plugin"/>
        <xsl:apply-templates select="*[contains(@class,' eclipsemap/pluginmeta ')]/*[contains(@class,' eclipsemap/plugininfo ')]/*[contains(@class,' topic/vrmlist ')]" mode="plugin"/>
        <xsl:if test="*[contains(@class,' eclipsemap/tocref ')][not(@toc='no')]|*[contains(@class,' eclipsemap/primarytocref ')][not(@toc='no')]">
          <xsl:value-of select="$newline"/>
          <extension point="org.eclipse.help.toc">
            <xsl:apply-templates select="*[contains(@class,' eclipsemap/primarytocref ')][not(@toc='no')]"/>
            <xsl:apply-templates select="*[contains(@class,' eclipsemap/tocref ')][not(@toc='no')]"/>
          </extension>
        </xsl:if>
        <xsl:if test="$DEFAULTINDEX!=''">
          <extension point="org.eclipse.help.index">
            <index file="{$DEFAULTINDEX}"/>
          </extension>
        </xsl:if>
        <xsl:apply-templates select="*[contains(@class,' eclipsemap/indexExtension ')] |
                                     *[contains(@class,' eclipsemap/contextExtension ')] | 
                                     *[contains(@class,' eclipsemap/contentExtension ')] |
                                     *[contains(@class,' eclipsemap/extension ')]"/>
      </plugin>
  </xsl:template>

  <xsl:template match="*[contains(@class,' eclipsemap/plugin ')]/@id">
    <xsl:copy-of select="."/>
  </xsl:template>

  <xsl:template match="*[contains(@class,' eclipsemap/pluginname ')]" mode="plugin">
    <xsl:attribute name="name"><xsl:value-of select="."/></xsl:attribute>
  </xsl:template>
  <xsl:template match="*[contains(@class,' eclipsemap/providerName ')]" mode="plugin">
    <xsl:attribute name="provider-name"><xsl:value-of select="."/></xsl:attribute>
  </xsl:template>
  <xsl:template match="*[contains(@class,' topic/vrmlist ')]" mode="plugin">
    <xsl:apply-templates select="*[contains(@class,' topic/vrm ')][last()]" mode="plugin"/>
  </xsl:template>
  <xsl:template match="*[contains(@class,' topic/vrm ')]" mode="plugin">
    <xsl:attribute name="version"><xsl:value-of select="@version"/></xsl:attribute>
  </xsl:template>

  <xsl:template match="*[contains(@class,' eclipsemap/primarytocref ')][@href]">
    <xsl:variable name="tocname">
      <xsl:choose>
        <xsl:when test="contains(@href,$FULL-DITAMAPEXT)"><xsl:value-of select="substring-before(@href,$FULL-DITAMAPEXT)"/>.xml</xsl:when>
        <xsl:otherwise><xsl:value-of select="@href"/></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:value-of select="$newline"/>
    <toc file="{$tocname}" primary="true">
      <xsl:apply-templates select="*[contains(@class,' eclipsemap/tocrefmeta ')]/*[contains(@class,' eclipsemap/extradir ')]"/>
    </toc>
  </xsl:template>

  <xsl:template match="*[contains(@class,' eclipsemap/tocref ')][@href]">
    <xsl:variable name="tocname">
      <xsl:choose>
        <xsl:when test="contains(@href,$FULL-DITAMAPEXT)"><xsl:value-of select="substring-before(@href,$FULL-DITAMAPEXT)"/>.xml</xsl:when>
        <xsl:otherwise><xsl:value-of select="@href"/></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:value-of select="$newline"/>
    <toc file="{$tocname}">
      <xsl:apply-templates select="*[contains(@class,' eclipsemap/tocrefmeta ')]/*[contains(@class,' eclipsemap/extradir ')]"/>
    </toc>
  </xsl:template>

  <xsl:template match="*[contains(@class,' eclipsemap/extradir ')]">
    <xsl:attribute name="extradir"><xsl:value-of select="@content"/></xsl:attribute>
  </xsl:template>

  <xsl:template match="*[contains(@class,' eclipsemap/indexExtension ')][@href]">
    <xsl:variable name="indexname">
      <xsl:choose>
        <xsl:when test="contains(@href,$FULL-DITAEXT)"><xsl:value-of select="substring-before(@href,$FULL-DITAEXT)"/>.xml</xsl:when>
        <xsl:when test="contains(@href,$FULL-DITAMAPEXT)"><xsl:value-of select="substring-before(@href,$FULL-DITAMAPEXT)"/>.xml</xsl:when>
        <xsl:otherwise><xsl:value-of select="@href"/></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:value-of select="$newline"/>
    <extension point="org.eclipse.help.index">
      <index file="{$indexname}"/>
    </extension>
  </xsl:template>

  <xsl:template match="*[contains(@class,' eclipsemap/contextExtension ')]">
    <xsl:value-of select="$newline"/>
    <extension point="org.eclipse.help.contexts">
      <xsl:apply-templates select="*[contains(@class,' eclipsemap/contextInfo ')]/*[contains(@class,' eclipsemap/extensionName ')]"/>
      <xsl:value-of select="$newline"/>
      <contexts file="{@href}">
        <xsl:apply-templates select="*[contains(@class,' eclipsemap/contextInfo ')]/*[contains(@class,' eclipsemap/contextPlugin ')]"/>
      </contexts>
    </extension>
  </xsl:template>

  <xsl:template match="*[contains(@class,' eclipsemap/extensionName ')]">
    <xsl:attribute name="name"><xsl:value-of select="@content"/></xsl:attribute>
  </xsl:template>

  <xsl:template match="*[contains(@class,' eclipsemap/contextPlugin ')]">
    <xsl:attribute name="plugin"><xsl:value-of select="@content"/></xsl:attribute>
  </xsl:template>

  <xsl:template match="*[contains(@class,' eclipsemap/contentExtension ')]">
    <xsl:value-of select="$newline"/>
    <extension point="org.eclipse.help.contentProducer">
      <xsl:apply-templates select="*"/>
    </extension>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' eclipsemap/contentProducer ')]">
    <xsl:apply-templates select="*[contains(@class,' eclipsemap/extensionName ')]"/>
    <xsl:apply-templates select="*[contains(@class,' eclipsemap/producerClass ')]"/>
  </xsl:template>

  <xsl:template match="*[contains(@class,' eclipsemap/producerClass ')]">
    <xsl:value-of select="$newline"/>
    <contentProducer producer="{@content}">
      <xsl:apply-templates select="following-sibling::*[contains(@class,' eclipsemap/parameter ')]"/>
    </contentProducer>
  </xsl:template>

  <xsl:template match="*[contains(@class,' eclipsemap/parameter ')]">
    <xsl:value-of select="$newline"/>
    <parameter name="{@name}" value="{@content}"/>
  </xsl:template>

  <xsl:template match="*[contains(@class,' eclipsemap/extension ')]">
    <xsl:value-of select="$newline"/>
    <extension>
      <xsl:apply-templates select="*[contains(@class,' eclipsemap/extensionMeta ')]/*[contains(@class,' eclipsemap/extensionPoint ')]"/>
      <xsl:apply-templates select="*[contains(@class,' eclipsemap/extensionMeta ')]/*[contains(@class,' eclipsemap/extensionName ')]"/>

    </extension>
  </xsl:template>

  <xsl:template match="*[contains(@class,' eclipsemap/extensionPoint ')]">
    <xsl:attribute name="point"><xsl:value-of select="@content"/></xsl:attribute>
  </xsl:template>

</xsl:stylesheet>
