<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of the DITA Open Toolkit project.
     See the accompanying license.txt file for applicable licenses.-->
<!-- (c) Copyright  Syncro Soft SRL, All Rights Reserved. -->
<!-- @author George Bina -->

<!--  generalizeSaxon6.xsl 
 | Convert specialied DITA topics into revertable, "generalized" form.
 | Started with generalize.xsl then 
 |   - fix errors
 |   - remove the parameter that allowed to set a folder
 |   - use Saxon 6.5 saxon:output instead of a Java class to generate foreign content
 *-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     extension-element-prefixes="saxon"
     xmlns:saxon="http://icl.com/saxon" exclude-result-prefixes="saxon">
     <xsl:output method="xml" indent="no"/>

     <xsl:template match="*[@class]">
          <xsl:variable name="generalize" select="substring-before(substring-after(@class,'/'),' ')" />
                <xsl:element name="{$generalize}">
                     <xsl:copy-of select="@*"/>
                    <xsl:apply-templates/>
                </xsl:element>
     </xsl:template>

     <xsl:template match="node() | @*">
          <xsl:copy>
               <xsl:apply-templates select="node() | @*"/>
          </xsl:copy>
     </xsl:template>
     
     <xsl:template match="*[contains(@class,' topic/unknown ') or contains(@class,' topic/foreign ')]" priority="10">
          <xsl:variable name="spec-type">
               <xsl:call-template name="get-spec-type"/>
          </xsl:variable>
          <xsl:variable name="generalize" select="substring-before(substring-after(@class,'/'),' ')"/>
          <xsl:element name="{$generalize}">
               <xsl:apply-templates select="node() | @*" mode="generalize-foreign-unknown">
                    <xsl:with-param name="spec-type" select="$spec-type"/>
               </xsl:apply-templates>
          </xsl:element>
     </xsl:template>
     
     <xsl:template match="*[contains(@class,' topic/')]|@*|text()|comment()|processing-instruction()" mode="generalize-foreign-unknown">
          <xsl:copy>
               <xsl:apply-templates select="node() | @*"/>
          </xsl:copy>
     </xsl:template>
     
     <xsl:template match="*[not(contains(@class,' topic/'))]" mode="generalize-foreign-unknown">
          <xsl:param name="spec-type"/>          
          <!-- find out the result file name that need to be generated. -->
          <xsl:variable name="base">
               <xsl:call-template name="get-base">
                    <xsl:with-param name="path" select="saxon:systemId()"/>
               </xsl:call-template>     
          </xsl:variable>
          <xsl:variable name="currentFile" select="substring-before(substring-after(saxon:systemId(), $base), '.')"/>
          
          <xsl:variable name="filename">
               <xsl:text>dita-generalized-</xsl:text>
               <xsl:value-of select="$currentFile"/>
               <xsl:text>-</xsl:text>
               <xsl:value-of select="ancestor::*[contains(@class,' topic/topic ')][1]/@id"/>
               <xsl:text>-</xsl:text>
               <xsl:value-of select="$spec-type"/>
               <xsl:text>-</xsl:text>
               <xsl:value-of select="@id"/>
               <xsl:if test="not(@id)">
                    <xsl:value-of select="generate-id(.)"/>
               </xsl:if>
               <xsl:text>.xml</xsl:text>
          </xsl:variable>
          <xsl:element name="object">
               <xsl:attribute name="data">
                    <xsl:value-of select="$filename"/>
               </xsl:attribute>
               <xsl:attribute name="type">
                    <xsl:text>DITA-foreign</xsl:text>
               </xsl:attribute>
          </xsl:element>
          <xsl:variable name="generalize" select="substring-before(substring-after(../@class,'/'),' ')"/>
          
          <saxon:output href="{substring-after(concat($base,$filename), 'file:')}" xmlns:ditaarch="http://dita.oasis-open.org/architecture/2005/" xsl:exclude-result-prefixes="ditaarch">
               <xsl:element name="{$generalize}">
                    <xsl:attribute name="class"><xsl:value-of select="../@class"/></xsl:attribute>
                    <xsl:apply-templates select="." mode="generalize-subsidiary"/>
               </xsl:element>
          </saxon:output>
     </xsl:template>
     
     
     <xsl:template match="*" mode="generalize-subsidiary">
          <xsl:element name="{name(.)}">
               <xsl:apply-templates select="node()|@*" mode="generalize-subsidiary"/>
          </xsl:element>
     </xsl:template>
     <xsl:template match="@*|comment()|processing-instruction()|text()" mode="generalize-subsidiary">
          <xsl:copy/>
     </xsl:template>
     
     
     <xsl:template name="get-spec-type">
          <xsl:param name="class" select="substring-after(@class,' ')"/>
          <xsl:choose>
               <xsl:when test="contains(substring-after($class,'/'),'/')">
                    <xsl:call-template name="get-spec-type">
                         <xsl:with-param name="class" select="substring-after($class,' ')"/>
                    </xsl:call-template>
               </xsl:when>
               <xsl:otherwise>
                    <xsl:value-of select="substring-before($class,'/')"/>
               </xsl:otherwise>
          </xsl:choose>
     </xsl:template>
     
     <xsl:template name="get-base">
          <xsl:param name="path"/>
          <xsl:choose>
               <xsl:when test="contains($path,'/')">
                    <xsl:value-of select="substring-before($path, '/')"/>
                    <xsl:text>/</xsl:text>
                    <xsl:call-template name="get-base">
                         <xsl:with-param name="path" select="substring-after($path,'/')"/>
                    </xsl:call-template>
               </xsl:when>
          </xsl:choose>
     </xsl:template>     
</xsl:stylesheet>
