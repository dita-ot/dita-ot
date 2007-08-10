<?xml version="1.0" encoding="UTF-8" ?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file for 
     applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<xsl:stylesheet version="1.0"
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:import href="syntax-braces.xsl"/>

<xsl:output method="xml"
            encoding="utf-8"
            indent="no"
/>

<!-- programming-domain.ent domain: codeph | var | kwd | synph | oper | delim | sep | repsep |
                                    option | parmname | apiname-->

<xsl:template match="*[contains(@class,' pr-d/codeph ')]" name="topic.pr-d.codeph">
 <xsl:variable name="flagrules">
  <xsl:call-template name="getrules"/>
 </xsl:variable>
 <samp class="codeph">
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:call-template name="flagcheck"/>
  <xsl:call-template name="revtext">
   <xsl:with-param name="flagrules" select="$flagrules"/>
  </xsl:call-template>
  </samp>
</xsl:template>

<xsl:template match="*[contains(@class,' pr-d/kwd ')]" name="topic.pr-d.kwd">
 <xsl:variable name="flagrules">
  <xsl:call-template name="getrules"/>
 </xsl:variable>
 <span class="kwd">
  <xsl:if test="(@importance='default')">
   <xsl:attribute name="class">defkwd</xsl:attribute>
  </xsl:if>
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:call-template name="flagcheck"/>
  <xsl:call-template name="revtext">
   <xsl:with-param name="flagrules" select="$flagrules"/>
  </xsl:call-template>
  </span>
</xsl:template>

<xsl:template match="*[contains(@class,' pr-d/var ')]" name="topic.pr-d.var">
 <xsl:variable name="flagrules">
  <xsl:call-template name="getrules"/>
 </xsl:variable>
 <span class="var">
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:call-template name="flagcheck"/>
  <xsl:call-template name="revtext">
   <xsl:with-param name="flagrules" select="$flagrules"/>
  </xsl:call-template>
  </span>
</xsl:template>

<xsl:template match="*[contains(@class,' pr-d/synph ')]" name="topic.pr-d.synph">
 <xsl:variable name="flagrules">
  <xsl:call-template name="getrules"/>
 </xsl:variable>
 <span class="synph">
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:call-template name="flagcheck"/>
  <xsl:call-template name="revtext">
   <xsl:with-param name="flagrules" select="$flagrules"/>
  </xsl:call-template>
  </span>
</xsl:template>

<xsl:template match="*[contains(@class,' pr-d/oper ')]" name="topic.pr-d.oper">
 <xsl:variable name="flagrules">
  <xsl:call-template name="getrules"/>
 </xsl:variable>
 <span class="oper">
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:call-template name="flagcheck"/>
  <xsl:call-template name="revtext">
   <xsl:with-param name="flagrules" select="$flagrules"/>
  </xsl:call-template>
  </span>
</xsl:template>

<xsl:template match="*[contains(@class,' pr-d/delim ')]" name="topic.pr-d.delim">
 <xsl:variable name="flagrules">
  <xsl:call-template name="getrules"/>
 </xsl:variable>
 <span class="delim">
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:call-template name="flagcheck"/>
  <xsl:call-template name="revtext">
   <xsl:with-param name="flagrules" select="$flagrules"/>
  </xsl:call-template>
  </span>
</xsl:template>

<xsl:template match="*[contains(@class,' pr-d/sep ')]" name="topic.pr-d.sep">
 <xsl:variable name="flagrules">
  <xsl:call-template name="getrules"/>
 </xsl:variable>
 <span class="sep">
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:call-template name="flagcheck"/>
  <xsl:call-template name="revtext">
   <xsl:with-param name="flagrules" select="$flagrules"/>
  </xsl:call-template>
  </span>
</xsl:template>

<xsl:template match="*[contains(@class,' pr-d/repsep ')]" name="topic.pr-d.repsep">
 <xsl:variable name="flagrules">
  <xsl:call-template name="getrules"/>
 </xsl:variable>
 <span class="repsep">
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:call-template name="flagcheck"/>
  <xsl:call-template name="revtext">
   <xsl:with-param name="flagrules" select="$flagrules"/>
  </xsl:call-template>
  </span>
</xsl:template>

<xsl:template match="*[contains(@class,' pr-d/option ')]" name="topic.pr-d.option">
 <xsl:variable name="flagrules">
  <xsl:call-template name="getrules"/>
 </xsl:variable>
 <span class="option">
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:call-template name="flagcheck"/>
  <xsl:call-template name="revtext">
   <xsl:with-param name="flagrules" select="$flagrules"/>
  </xsl:call-template>
  </span>
</xsl:template>

<xsl:template match="*[contains(@class,' pr-d/parmname ')]" name="topic.pr-d.parmname">
 <xsl:variable name="flagrules">
  <xsl:call-template name="getrules"/>
 </xsl:variable>
 <span class="parmname">
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:call-template name="flagcheck"/>
  <xsl:call-template name="revtext">
   <xsl:with-param name="flagrules" select="$flagrules"/>
  </xsl:call-template>
  </span>
</xsl:template>

<xsl:template match="*[contains(@class,' pr-d/apiname ')]" name="topic.pr-d.apiname">
 <xsl:variable name="flagrules">
  <xsl:call-template name="getrules"/>
 </xsl:variable>
 <span class="apiname">
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:call-template name="flagcheck"/>
  <xsl:call-template name="revtext">
   <xsl:with-param name="flagrules" select="$flagrules"/>
  </xsl:call-template>
  </span>
</xsl:template>

</xsl:stylesheet>
