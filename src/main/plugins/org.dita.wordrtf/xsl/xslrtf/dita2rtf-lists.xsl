<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of the DITA Open Toolkit project hosted on
Sourceforge.net. See the accompanying license.txt file for
applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2005, 2006 All Rights Reserved. -->
<xsl:stylesheet
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
version="2.0" xmlns:random="org.dita.dost.util.RandomUtils" exclude-result-prefixes="random">
<xsl:strip-space elements="*"/>

<!-- single-part lists -->

<xsl:template match="*[contains(@class,' topic/ul ')]">
<xsl:call-template name="gen-id"/>
<xsl:apply-templates/>
<!-- <xsl:if test="not(ancestor::*[contains(@class,' topic/li ')])">\par\pard\li360\fi-180</xsl:if> -->
</xsl:template>

<xsl:template match="*[contains(@class,' topic/li ')]">
<xsl:call-template name="gen-id"/>
<xsl:call-template name="block-li"/>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/ol ')]">
<xsl:call-template name="gen-id"/>
<xsl:apply-templates/>
<!-- <xsl:if test="not(ancestor::*[contains(@class,' topic/li ')])">\par\pard\li360\fi-180</xsl:if>     -->
</xsl:template>


<!-- definition list -->

<xsl:template match="*[contains(@class,' topic/dl ')]">
<xsl:call-template name="gen-id"/>
<xsl:call-template name="block-lq"/>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/dt ')]">
<xsl:call-template name="gen-id"/>
<xsl:call-template name="inline-em"/>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/dd ')]">
<xsl:call-template name="gen-id"/>
<xsl:call-template name="block-p"/>
</xsl:template>

<!-- parameter list -->

<xsl:template match="parml">
<!-- not found -->
<xsl:call-template name="block-lq"/>
</xsl:template>

<xsl:template match="plentry/synph">
<!-- plentry not found -->
<xsl:call-template name="inline-em"/>
</xsl:template>

<xsl:template match="plentry/li">
<!-- plentry not found -->
<xsl:call-template name="block-lq"/>
</xsl:template>

<!-- block-list -->
<xsl:template name="block-list">
<xsl:param name="depth">0</xsl:param>
<xsl:variable name="li-num" select="720 + ($depth * 360)"/>
<!-- \par \pard\li<xsl:value-of select="$li-num"/> -->
<!-- \fi-360{\*\pn\pnlvlblt\pnf1\pnindent180{\pntxtb\'b7}}\plain\f2\fs24 -->
<xsl:apply-templates/>
<!-- \pard\li360\fi-180 \par -->
</xsl:template>

<xsl:template name="block-ol">
<xsl:param name="depth">0</xsl:param>
<xsl:variable name="li-num" select="720 + ($depth * 360)"/>
<!-- \par \pard\li<xsl:value-of select="$li-num"/> -->
<!-- \fi-360{\*\pn\pnlvlbody\pndec\pnstart1\pnf1\pnindent180}\plain\f2\fs24 -->
<xsl:apply-templates/>
<!-- \pard\li360\fi-180 \par  -->
</xsl:template>

<xsl:template name="block-li">
<xsl:variable name="depth">
<xsl:value-of select="count(ancestor::*[contains(@class,' topic/li ')])"/>
</xsl:variable>
<xsl:variable name="li-num" select="420 + ($depth * 420)"/>
<xsl:variable name="listnum" select="count(preceding::*[contains(@class,' topic/ol ') or contains(@class,' topic/ul ')][not(ancestor::*[contains(@class,' topic/li ')])]) + 1"/>
<!-- \par\pard\plain \qj \fi-420\li<xsl:value-of select="$li-num"/> -->
<xsl:if test="ancestor::*[contains(@class,' topic/table ') or contains(@class,' topic/simpletable ')]">\intbl
</xsl:if>
<!-- \jclisttab\tx<xsl:value-of select="$li-num"/> -->
{\pard \ls<xsl:value-of select="$listnum"/> \'b7\'20
<xsl:if test="$depth &gt; 0">
<!-- \ilvl<xsl:value-of select="$depth"/> -->
</xsl:if>
<xsl:text/>
<xsl:if test="@importance='optional'">
<xsl:call-template name="getStringRTF">
<xsl:with-param name="stringName" select="'Optional'"/>
</xsl:call-template>
<xsl:call-template name="getStringRTF">
<xsl:with-param name="stringName" select="'ColonSymbol'"/>
</xsl:call-template>
<xsl:text/>
</xsl:if>
<xsl:if test="@importance='required'">
<xsl:call-template name="getStringRTF">
<xsl:with-param name="stringName" select="'Required'"/>
</xsl:call-template>
<xsl:call-template name="getStringRTF">
<xsl:with-param name="stringName" select="'ColonSymbol'"/>
</xsl:call-template>
<xsl:text/>
</xsl:if>
<xsl:apply-templates/>\par}</xsl:template>

<!-- listtables code deactivated; 
template seems to be called by another stylesheet 
-->
<xsl:template name="gen-list-table">
</xsl:template>

</xsl:stylesheet>