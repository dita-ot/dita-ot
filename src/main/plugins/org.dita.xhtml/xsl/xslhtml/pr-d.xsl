<?xml version="1.0" encoding="UTF-8" ?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file for 
     applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<xsl:stylesheet version="1.0"
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:import href="syntax-braces.xsl"/>

<!-- programming-domain.ent domain: codeph | var | kwd | synph | oper | delim | sep | repsep |
                                    option | parmname | apiname-->

<xsl:template match="*[contains(@class,' pr-d/codeph ')]" name="topic.pr-d.codeph">
 <samp class="codeph">
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:apply-templates/>
  </samp>
</xsl:template>

<xsl:template match="*[contains(@class,' pr-d/kwd ')]" name="topic.pr-d.kwd">
 <span class="kwd">
  <xsl:if test="(@importance='default')">
   <xsl:attribute name="class">defkwd</xsl:attribute>
  </xsl:if>
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:apply-templates/>
 </span>
</xsl:template>

<xsl:template match="*[contains(@class,' pr-d/var ')]" name="topic.pr-d.var">
 <span class="var">
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:apply-templates/>
 </span>
</xsl:template>

<xsl:template match="*[contains(@class,' pr-d/synph ')]" name="topic.pr-d.synph">
 <span class="synph">
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:apply-templates/>
 </span>
</xsl:template>

<xsl:template match="*[contains(@class,' pr-d/oper ')]" name="topic.pr-d.oper">
 <span class="oper">
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:apply-templates/>
 </span>
</xsl:template>

<xsl:template match="*[contains(@class,' pr-d/delim ')]" name="topic.pr-d.delim">
 <span class="delim">
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:apply-templates/>
 </span>
</xsl:template>

<xsl:template match="*[contains(@class,' pr-d/sep ')]" name="topic.pr-d.sep">
 <span class="sep">
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:apply-templates/>
 </span>
</xsl:template>

<xsl:template match="*[contains(@class,' pr-d/repsep ')]" name="topic.pr-d.repsep">
 <span class="repsep">
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:apply-templates/>
 </span>
</xsl:template>

<xsl:template match="*[contains(@class,' pr-d/option ')]" name="topic.pr-d.option">
 <span class="option">
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:apply-templates/>
 </span>
</xsl:template>

<xsl:template match="*[contains(@class,' pr-d/parmname ')]" name="topic.pr-d.parmname">
 <span class="parmname">
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:apply-templates/>
 </span>
</xsl:template>

<xsl:template match="*[contains(@class,' pr-d/apiname ')]" name="topic.pr-d.apiname">
 <span class="apiname">
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:apply-templates/>
 </span>
</xsl:template>

</xsl:stylesheet>
