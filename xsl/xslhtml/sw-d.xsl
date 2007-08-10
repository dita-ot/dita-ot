<?xml version="1.0" encoding="UTF-8" ?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file for 
     applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<xsl:stylesheet version="1.0"
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method="xml"
            encoding="utf-8"
            indent="no"
/>

<!-- software-domain.ent domain: filepath | msgph | userinput | systemoutput | cmdname | msgnum | varname -->

<xsl:template match="*[contains(@class,' sw-d/filepath ')]" name="topic.sw-d.filepath">
 <xsl:variable name="flagrules">
  <xsl:call-template name="getrules"/>
 </xsl:variable>
 <span class="filepath">
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:call-template name="flagcheck"/>
  <xsl:call-template name="revtext">
   <xsl:with-param name="flagrules" select="$flagrules"/>
  </xsl:call-template>
  </span>
</xsl:template>

<xsl:template match="*[contains(@class,' sw-d/msgph ')]" name="topic.sw-d.msgph">
 <xsl:variable name="flagrules">
  <xsl:call-template name="getrules"/>
 </xsl:variable>
 <tt class="msgph">
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:call-template name="flagcheck"/>
  <xsl:call-template name="revtext">
   <xsl:with-param name="flagrules" select="$flagrules"/>
  </xsl:call-template>
  </tt>
</xsl:template>

<xsl:template match="*[contains(@class,' sw-d/userinput ')]" name="topic.sw-d.userinput">
 <xsl:variable name="flagrules">
  <xsl:call-template name="getrules"/>
 </xsl:variable>
 <kbd class="userinput">
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:call-template name="flagcheck"/>
  <xsl:call-template name="revtext">
   <xsl:with-param name="flagrules" select="$flagrules"/>
  </xsl:call-template>
  </kbd>
</xsl:template>

<xsl:template match="*[contains(@class,' sw-d/systemoutput ')]" name="topic.sw-d.systemoutput">
 <xsl:variable name="flagrules">
  <xsl:call-template name="getrules"/>
 </xsl:variable>
 <tt class="sysout">
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:call-template name="flagcheck"/>
  <xsl:call-template name="revtext">
   <xsl:with-param name="flagrules" select="$flagrules"/>
  </xsl:call-template>
  </tt>
</xsl:template>

<xsl:template match="*[contains(@class,' sw-d/cmdname ')]" name="topic.sw-d.cmdname">
 <xsl:variable name="flagrules">
  <xsl:call-template name="getrules"/>
 </xsl:variable>
 <span class="cmdname">
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:call-template name="flagcheck"/>
  <xsl:call-template name="revtext">
   <xsl:with-param name="flagrules" select="$flagrules"/>
  </xsl:call-template>
  </span>
</xsl:template>

<xsl:template match="*[contains(@class,' sw-d/msgnum ')]" name="topic.sw-d.msgnum">
 <xsl:variable name="flagrules">
  <xsl:call-template name="getrules"/>
 </xsl:variable>
 <span class="msgnum">
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:call-template name="flagcheck"/>
  <xsl:call-template name="revtext">
   <xsl:with-param name="flagrules" select="$flagrules"/>
  </xsl:call-template>
  </span>
</xsl:template>

<xsl:template match="*[contains(@class,' sw-d/varname ')]" name="topic.sw-d.varname">
 <xsl:variable name="flagrules">
  <xsl:call-template name="getrules"/>
 </xsl:variable>
 <var class="varname">
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:call-template name="flagcheck"/>
  <xsl:call-template name="revtext">
   <xsl:with-param name="flagrules" select="$flagrules"/>
  </xsl:call-template>
  </var>
</xsl:template>

</xsl:stylesheet>
