<?xml version="1.0"?><!-- This file is part of the DITA Open Toolkit project hosted on      Sourceforge.net. See the accompanying license.txt file in the     main toolkit package for applicable licenses.--><!-- Copyright IBM Corporation 2010. All Rights Reserved. --><!-- Originally created Oct. 2006 by Robert D. Anderson -->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method="html"
            encoding="utf-8"
            indent="no"
            doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"
            doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"/>

<xsl:param name="VIEW" select="'nothing'"/>

  <xsl:template match="/">
    <html>
      <head>
        <title><xsl:value-of select="/*/*[contains(@class,' topic/title ')]"/></title>
        <style>body {font-family: sans-serif, serif;
                     font-size: small}
               .indent {margin-left: .5em}
               li {padding: .1em 0em}
               a {text-decoration: none}
               a:hover {text-decoration: underline}</style>
      </head>
      <body>
        <xsl:apply-templates select="/*/*[contains(@class,' topic/title ')]"/>
        <ul>
          <xsl:apply-templates select="*/*[contains(@class,' map/topicref ')]"/>
        </ul>
      </body>
    </html>
  </xsl:template>

<!--  <xsl:template match="*[contains(@class,' map/topicref ')][not(@otherprops)]" priority="1"/>-->
  <xsl:template match="*[contains(@class,' map/topicref ')]">
    <li>
      <a href="{@href}" target="text">
        <xsl:apply-templates select="*[contains(@class,' map/topicmeta ')]/*[contains(@class,' map/shortdesc ')]"/>
        <xsl:value-of select="@navtitle"/>
      </a>
    </li>
  </xsl:template>

  <xsl:template match="*[contains(@class,' map/shortdesc ')]">
    <xsl:attribute name="title"><xsl:apply-templates/></xsl:attribute>
  </xsl:template>

  <xsl:template match="*[contains(@class,' topic/title ')]">
    <h3 class="indent"><xsl:apply-templates/></h3>
  </xsl:template>

</xsl:stylesheet>
