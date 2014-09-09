<?xml version="1.0" encoding="utf-8"?>
<!-- This file is part of the DITA Open Toolkit project.
     See the accompanying license.txt file for 
     applicable licenses.-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="2.0">

  <xsl:template match="/">
    <xsl:variable name="content" as="node()*">
      <xsl:apply-imports/>
    </xsl:variable>
    <xsl:apply-templates select="$content" mode="add-xhtml-ns"/>
  </xsl:template>

  <xsl:template match="*[namespace-uri() eq '']" mode="add-xhtml-ns" priority="10">
    <xsl:element name="{name()}" namespace="http://www.w3.org/1999/xhtml">
      <xsl:apply-templates select="@* | node()" mode="add-xhtml-ns"/>
    </xsl:element>
  </xsl:template>
  
  <xsl:template match="nav" mode="add-xhtml-ns" priority="20">
    <xsl:element name="div" namespace="http://www.w3.org/1999/xhtml">
      <xsl:apply-templates select="@* | node()" mode="add-xhtml-ns"/>
    </xsl:element>
  </xsl:template>
  
  <xsl:template match="@*[starts-with(name(), 'data-')]" mode="add-xhtml-ns" priority="10"/>
  
  <xsl:template match="@* | node()" mode="add-xhtml-ns">
    <xsl:copy>
      <xsl:apply-templates select="@* | node()" mode="add-xhtml-ns"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
