<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file for 
     applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<!--  generalize.xsl 
 | Convert specialied DITA topics into revertable, "generalized" form
 *-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     xmlns:java="org.dita.dost.util.GenUtils" exclude-result-prefixes="java">
     <xsl:param name="outdir.subsidiary"/>
     <xsl:variable name="file-prefix">
          <xsl:choose>
               <xsl:when test="string-length($outdir.subsidiary) &gt; 0 and not(substring($outdir.subsidiary,string-length($outdir.subsidiary))='/')and not(substring($outdir.subsidiary,string-length($outdir.subsidiary))='\')">
                    <xsl:value-of select="translate($outdir.subsidiary,
                         '\/=+|?[]{}()!#$%^&amp;*__~`;:.,-abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ',
                         '//=+|?[]{}()!#$%^&amp;*__~`;:.,-abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ')"/><xsl:text>/</xsl:text>
               </xsl:when>
               <xsl:otherwise>
                    <xsl:value-of select="translate($outdir.subsidiary,
                         '\/=+|?[]{}()!#$%^&amp;*__~`;:.,-abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ',
                         '//=+|?[]{}()!#$%^&amp;*__~`;:.,-abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ')"/>
               </xsl:otherwise>
               
          </xsl:choose>
     </xsl:variable>
     <xsl:output method="xml" indent="no"/>

     <xsl:template match="*[@class]">
          <xsl:variable name="generalize" select="substring-before(substring-after(@class,'/'),' ')" />
                <xsl:element name="{$generalize}">
                     <xsl:copy-of select="@*"/>
                    <xsl:apply-templates/>
                </xsl:element>
     </xsl:template>

     <xsl:template match="*|@*|comment()|processing-instruction()|text()">
       <xsl:copy>
         <xsl:apply-templates select="*|@*|comment()|processing-instruction()|text()"/>
       </xsl:copy>
     </xsl:template>
     
     
     <xsl:template match="*[contains(@class,' topic/unknown ') or contains(@class,' topic/foreign ')]" priority="10">
          <xsl:variable name="spec-type">
               <xsl:call-template name="get-spec-type"/>
          </xsl:variable>
          <xsl:variable name="generalize" select="substring-before(substring-after(@class,'/'),' ')"/>
          <xsl:element name="{$generalize}">
               <xsl:apply-templates select="*|@*|text()|comment()|processing-instruction()" mode="generalize-foreign-unknown">
                    <xsl:with-param name="spec-type" select="$spec-type"/>
               </xsl:apply-templates>
          </xsl:element>
     </xsl:template>
     
     <xsl:template match="*[contains(@class,' topic/object ')]|@*|text()|comment()|processing-instruction()" mode="generalize-foreign-unknown">
          <xsl:copy>
               <xsl:apply-templates select="*|@*|comment()|processing-instruction()|text()"/>
          </xsl:copy>
     </xsl:template>
     
     <xsl:template match="*[not(contains(@class,' topic/object '))]" mode="generalize-foreign-unknown">
          <xsl:param name="spec-type"/>
          <xsl:value-of select="preceding-sibling::*[not(contains(@class,' topic/topic '))]"/>
          
          <!-- find out the subsidiary result file name that need to be generated. -->
          <xsl:variable name="filename">
               <xsl:text>dita-generalized-</xsl:text>
               <xsl:value-of select="ancestor::*[contains(@class,' topic/topic ')][1]/@id"/>
               <xsl:text>-</xsl:text>
               <xsl:value-of select="concat($spec-type,generate-id(.))"/>
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
          <xsl:variable name="generalize" select="substring-before(substring-after(../@class,'/'),' ')"></xsl:variable>
          <xsl:value-of select="java:clear()"/>
          <xsl:value-of select="java:setOutput(concat($file-prefix,$filename))"/>
          <xsl:value-of select="java:startElement($generalize)"/>
          <xsl:value-of select="java:addAttr('class',string(../@class))"/>
          <xsl:apply-templates select="." mode="generalize-subsidiary"/>
          <xsl:value-of select="java:endElement($generalize)"/>
          <xsl:value-of select="java:flush()"/>
     </xsl:template>
     
     <xsl:template match="*" mode="generalize-subsidiary">
          <xsl:variable name="name" select="name()"/>
          <xsl:value-of select="java:startElement($name)"/>
          <xsl:apply-templates select="*|@*|text()|comment()" mode="generalize-subsidiary"/>
          <xsl:value-of select="java:endElement($name)"/>
     </xsl:template>
     
     <xsl:template match="@*" mode="generalize-subsidiary">
          <xsl:variable name="name" select="name()"/>
          <xsl:variable name="value"><xsl:value-of select="."/></xsl:variable>
          <xsl:value-of select="java:addAttr($name,$value)"/>
     </xsl:template>
     
     <xsl:template match="text()|comment()" mode="generalize-subsidiary">
          <xsl:variable name="text"><xsl:value-of select="."/></xsl:variable>
          <xsl:value-of select="java:addText($text)"/>
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
</xsl:stylesheet>
