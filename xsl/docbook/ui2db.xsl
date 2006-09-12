<?xml version="1.0" encoding="utf-8" ?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
  Sourceforge.net. See the accompanying license.txt file for 
  applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!-- to do: attributes -->
<xsl:template match="*[contains(@class,' ui-d/screen ')]">
  <screen>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'scrn'"/>
    </xsl:call-template>
    <xsl:apply-templates/>
  </screen>
</xsl:template>

<xsl:template match="*[contains(@class,' ui-d/menucascade ')]">
  <menuchoice>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'mncscd'"/>
    </xsl:call-template>
    <xsl:apply-templates select="*[contains(@class,' ui-d/uicontrol ')] /
        *[contains(@class,' ui-d/shortcut ')]"/>
    <xsl:apply-templates select="*[contains(@class,' ui-d/uicontrol ')]"/>
  </menuchoice>
</xsl:template>

<xsl:template match="*[contains(@class,' ui-d/menucascade ')] /
      *[contains(@class,' ui-d/uicontrol ')]
	  [position()=1]">
  <xsl:call-template name="menuitem">
    <xsl:with-param name="elementName" select="'guimenu'"/>
  </xsl:call-template>
</xsl:template>

<xsl:template match="*[contains(@class,' ui-d/menucascade ')] /
      *[contains(@class,' ui-d/uicontrol ')]
	  [position()!=1 and position()!=last()]">
  <xsl:call-template name="menuitem">
    <xsl:with-param name="elementName" select="'guisubmenu'"/>
  </xsl:call-template>
</xsl:template>

<xsl:template match="*[contains(@class,' ui-d/menucascade ')] /
      *[contains(@class,' ui-d/uicontrol ')]
	  [position()=last()]">
  <xsl:call-template name="menuitem">
    <xsl:with-param name="elementName" select="'guimenuitem'"/>
  </xsl:call-template>
</xsl:template>

<xsl:template name="menuitem">
  <xsl:param name="elementName"/>
  <xsl:element name="{$elementName}">
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'mnuictrl'"/>
    </xsl:call-template>
    <xsl:apply-templates/>
  </xsl:element>
</xsl:template>

<xsl:template match="*[not(contains(@class,' ui-d/menucascade '))] /
      *[contains(@class,' ui-d/uicontrol ')]">
  <guilabel>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'uictrl'"/>
    </xsl:call-template>
    <xsl:apply-templates/>
  </guilabel>
</xsl:template>

<xsl:template match="*[contains(@class,' ui-d/wintitle ')]">
  <guilabel>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'wnttl'"/>
    </xsl:call-template>
    <xsl:apply-templates/>
  </guilabel>
</xsl:template>

<xsl:template match="*[contains(@class,' ui-d/shortcut ')]">
  <shortcut>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'shrtct'"/>
    </xsl:call-template>
    <xsl:apply-templates/>
  </shortcut>
</xsl:template>

</xsl:stylesheet>
