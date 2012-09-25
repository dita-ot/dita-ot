<?xml version="1.0" encoding="utf-8" ?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
  Sourceforge.net. See the accompanying license.txt file for 
  applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="*[contains(@class,' sw-d/msgblock ')]">
  <programlisting>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'msgblck'"/>
    </xsl:call-template>
    <xsl:apply-templates/>
  </programlisting>
</xsl:template>

<xsl:template match="*[contains(@class,' sw-d/msgnum ')]">
  <errorcode>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'msgnm'"/>
    </xsl:call-template>
    <xsl:apply-templates/>
  </errorcode>
</xsl:template>

<xsl:template match="*[contains(@class,' sw-d/msgph ')]">
  <errortext>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'msgph'"/>
    </xsl:call-template>
    <xsl:apply-templates/>
  </errortext>
</xsl:template>

<xsl:template match="*[contains(@class,' sw-d/cmdname ')]">
  <command>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'cmdnm'"/>
    </xsl:call-template>
    <xsl:apply-templates/>
  </command>
</xsl:template>

<xsl:template match="*[contains(@class,' sw-d/varname ')]">
  <varname>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'vrnm'"/>
    </xsl:call-template>
    <xsl:apply-templates/>
  </varname>
</xsl:template>

<xsl:template match="*[contains(@class,' sw-d/filepath ')]">
  <filename>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'flpth'"/>
    </xsl:call-template>
    <xsl:apply-templates/>
  </filename>
</xsl:template>

<xsl:template match="*[contains(@class,' sw-d/userinput ')]">
  <userinput>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'usrinpt'"/>
    </xsl:call-template>
    <xsl:apply-templates/>
  </userinput>
</xsl:template>

<xsl:template match="*[contains(@class,' sw-d/systemoutput ')]">
  <computeroutput>
    <xsl:call-template name="setStandardAttr">
      <xsl:with-param name="IDPrefix" select="'stmotpt'"/>
    </xsl:call-template>
    <xsl:apply-templates/>
  </computeroutput>
</xsl:template>

</xsl:stylesheet>
