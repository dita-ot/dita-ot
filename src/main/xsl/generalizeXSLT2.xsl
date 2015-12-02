<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of the DITA Open Toolkit project.
     See the accompanying license.txt file for applicable licenses.-->
<!-- (c) Copyright Syncro Soft SRL, 2015. All Rights Reserved. -->
<!-- @author George Bina -->

<!--  generalizeXSLT2.xsl 
     | Convert specialied DITA topics into revertable, "generalized" form.
     | Started with generalize.xsl then 
     |   - fix errors
     |   - remove the parameter that allowed to set a folder
     |   - use XSLT 2.0 xsl:result-document instead of a Java class to generate foreign content
     *-->

<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
     xmlns:f="org.dita-ot/ns/functions"
     exclude-result-prefixes="f">
     
     <!-- get the generalized element name from the class attribute value -->
     <xsl:function name="f:generalizedName">
          <xsl:param name="class"/>
          <xsl:value-of select="substring-before(substring-after($class,'/'),' ')"/>
     </xsl:function>
     
     <!-- default recursive copy template -->
     <xsl:template match="node() | @*" mode="#all">
          <xsl:copy>
               <xsl:apply-templates select="node() | @*"/>
          </xsl:copy>
     </xsl:template>
     
     <!-- if we have an element with a class attribute, replace it with the generalized name -->
     <xsl:template match="*[@class]">
          <xsl:element name="{f:generalizedName(@class)}">
               <xsl:copy-of select="@*"/>
               <xsl:apply-templates/>
          </xsl:element>
     </xsl:template>
     
     <!-- we have a special handling for foreign (and unknown?) elements as described at 
          http://docs.oasis-open.org/dita/dita/v1.3/cos01/part3-all-inclusive/non-normative/foreigngeneralization.html#foreigngeneralization -->
     <xsl:template match="*[contains(@class,' topic/unknown ') or contains(@class,' topic/foreign ')]" priority="10">
          <xsl:element name="{f:generalizedName(@class)}">
               <xsl:apply-templates select="node() | @*" mode="insideForeign"/>
          </xsl:element>
     </xsl:template>
     
     <!-- DITA elements inside foreign appear generalized -->     
     <xsl:template match="*[contains(@class,' topic/')]" mode="insideForeign">
          <xsl:apply-templates select="."/>
     </xsl:template>
     <!-- non-DITA elements will be placed into a separate file -->
     <xsl:template match="*" mode="insideForeign">
          <!-- find out the result file name that need to be generated. -->
          <xsl:variable name="filename">
               <xsl:text>dita-generalized-</xsl:text>
               <xsl:value-of select="substring-before(tokenize(base-uri(), '/')[last()], '.')"/>
               <xsl:text>-</xsl:text>
               <xsl:value-of select="ancestor::*[contains(@class,' topic/topic ')][1]/@id"/>
               <xsl:text>-</xsl:text>
               <xsl:value-of select="normalize-space((tokenize(../@class, '/'))[last()])"/>
               <xsl:text>-</xsl:text>
               <xsl:value-of select="@id"/>
               <xsl:if test="not(@id)">
                    <xsl:value-of select="generate-id(.)"/>
               </xsl:if>
               <xsl:text>.xml</xsl:text>
          </xsl:variable>
          <!-- replace the non-DITA element with an object pointing to a separate file -->
          <object data="{$filename}" type="DITA-foreign"/>
          <!-- emit the separate file containing the foreign element and the non-DITA content -->
          <xsl:result-document href="{$filename}">
               <xsl:element name="{f:generalizedName(../@class)}">
                    <xsl:copy-of select="../@class"/>
                    <xsl:apply-templates select="." mode="foreignContent"/>
               </xsl:element>
          </xsl:result-document>
     </xsl:template>
     
     <!-- process foreign content -->     
     <xsl:template match="*" mode="foreignContent">
          <xsl:element name="{name(.)}">
               <xsl:apply-templates select="node()|@*" mode="foreignContent"/>
          </xsl:element>
     </xsl:template>     
</xsl:stylesheet>
