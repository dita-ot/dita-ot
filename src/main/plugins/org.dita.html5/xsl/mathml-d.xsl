<?xml version="1.0" encoding="UTF-8" ?>
<!--
This file is part of the DITA Open Toolkit project.

Copyright 2025 Jason Coleman

See the accompanying LICENSE file for applicable license.
-->
<xsl:stylesheet version="3.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:dita-ot="http://dita-ot.sourceforge.net/ns/201007/dita-ot"
                xmlns:m="http://www.w3.org/1998/Math/MathML"
                exclude-result-prefixes="dita-ot m">
  
  <xsl:template match="*[contains(@class, ' mathml-d/mathml ')]" name="topic.mathml-d.mathml">
    <xsl:call-template name="setaname"/>
    <xsl:apply-templates/>
  </xsl:template>
  
  <xsl:template match="m:math">
    <xsl:apply-templates select="." mode="dita-ot:mathml-prefix"/>
  </xsl:template>
  
  <xsl:template match="m:*" mode="dita-ot:mathml-prefix" priority="10">
    <xsl:element name="{local-name()}" namespace="http://www.w3.org/1998/Math/MathML">
      <xsl:call-template name="mathml-displaystyle"/>
      <xsl:apply-templates select="* | @* | text()" mode="#current"/>
    </xsl:element>
  </xsl:template>
  
  <xsl:template match="@* | node()" mode="dita-ot:mathml-prefix">
    <xsl:copy>
      <xsl:apply-templates select="@* | node()" mode="#current"/>
    </xsl:copy>
  </xsl:template>
  
  <!-- TODO: this may need to be moved to 'topicpull' so that common mathml processing can also be done here -->
  <xsl:template match="*[contains(@class, ' mathml-d/mathmlref ')]" name="topic.mathml-d.mathmlref">
    <xsl:apply-templates select="*[contains(@class, ' ditaot-d/ditaval-startprop ')]" mode="out-of-line"/>
    <span>
      <xsl:call-template name="commonattributes"/>
      <xsl:call-template name="setid"/>
      <xsl:apply-templates select="@href"/>
    </span>
    <xsl:apply-templates select="*[contains(@class, ' ditaot-d/ditaval-endprop ')]" mode="out-of-line"/>
  </xsl:template>
  
  <!-- Copy the mathref contents into the resulting HTML5 document, as HTML5 
    can't reference these as external files like an image src -->
  <xsl:template match="*[contains(@class, ' mathml-d/mathmlref ')]/@href">
    <xsl:choose>
      <xsl:when test="contains(.,'#')">
        <xsl:variable name="filepath" select="substring-before(.,'#')"/>
        <xsl:variable name="id" select="substring-after(.,'#')"/>
        <xsl:copy-of select="document($filepath,.)//*[@id=$id]"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:copy-of select="document(.)/*"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  
  <!-- In DITA it is assumed that the equation-inline or equation-block will
       dictate display so ignore this attribute. So ignore @display and assume that 
       MathML within an equation-block should be displayed as 'normal' (i.e., not
       'compact'. -->
  <xsl:template match="@display" mode="dita-ot:mathml-prefix"/>
  
  <xsl:template name="mathml-displaystyle">
    <xsl:if test="local-name()='math' and not(@displaystyle) and ancestor::equation-block">
      <xsl:attribute name="displaystyle" select="'true'"/>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
