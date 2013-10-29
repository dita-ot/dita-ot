<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of the DITA Open Toolkit project hosted on
Sourceforge.net. See the accompanying license.txt file for
applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2005, 2006 All Rights Reserved. -->
<xsl:stylesheet
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
version="2.0"
xmlns:rtf="rtf_namespace">
<xsl:import href="plugin:org.dita.base:xsl/common/output-message.xsl"/>
<xsl:import href="plugin:org.dita.base:xsl/common/dita-utilities.xsl"/>
<xsl:import href="dita2rtf-utilities.xsl"/>
<xsl:import href="dita2rtf-img.xsl"/>
<xsl:import href="dita2rtf-parms.xsl"/>
<xsl:import href="dita2rtf-table.xsl"/>
<xsl:import href="dita2rtf-lists.xsl"/>
<xsl:import href="hi-d.xsl"/>
<xsl:import href="pr-d.xsl"/>
<xsl:import href="ui-d.xsl"/>
<xsl:import href="sw-d.xsl"/>
<xsl:import href="dita2rtf-task.xsl"/>
<xsl:import href="dita2rtf-front-matter.xsl"/>
<xsl:include href="inc/rtf_variables.xsl"/>
<xsl:output method="text"/>

<xsl:strip-space elements="*"/>

<xsl:param name="DRAFT" select="'no'"/>
<xsl:param name="OUTPUTDIR" select="''"/>

<xsl:variable name="msgprefix">DOTX</xsl:variable>

<xsl:template match="/">
<xsl:call-template name="root"/>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/topic ')]">
<xsl:call-template name="gen-id"/>
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/topic ')]/*[contains(@class,' topic/title ')]">
<xsl:call-template name="gen-id"/>
<xsl:variable name="depth">
<xsl:value-of select="count(ancestor::*[contains(@class,' topic/topic ')])"/>
</xsl:variable>
<xsl:call-template name="block-title">
<xsl:with-param name="depth" select="$depth"/>
</xsl:call-template>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/section ')]/*[contains(@class,' topic/title ')]">
<xsl:call-template name="gen-id"/>{\pard <xsl:apply-templates/>\par}</xsl:template>

<xsl:template match="synsect">
{\pard Syntax <xsl:apply-templates/>\par}
</xsl:template>

<xsl:template match="*[contains(@class,' topic/section ')]">
<xsl:call-template name="gen-id"/>
{\pard <xsl:apply-templates/>\par}
</xsl:template>

<xsl:template match="*[contains(@class,' topic/example ')]">
<xsl:call-template name="gen-id"/>
{\pard <xsl:apply-templates/>\par}
</xsl:template>

<xsl:template match="*[contains(@class,' topic/example ')]/*[contains(@class,' topic/title ')]">
<xsl:call-template name="gen-id"/>
{pard <xsl:apply-templates/>\par}
</xsl:template>

<!-- =========== block things ============ -->

<xsl:template match="*[contains(@class,' topic/p ')]">
<xsl:call-template name="gen-id"/>
<xsl:call-template name="block-p"/>
</xsl:template>

<!--xsl:template match="*[contains(@class,' topic/lq ')]">
<xsl:call-template name="gen-id"/><xsl:call-template name="block-lq"/>
</xsl:template-->

<!-- phrases -->
<xsl:template match="*[contains(@class,' hi-d/tt ')]">
<xsl:call-template name="inline-em"/>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/q ')]">
<xsl:call-template name="gen-id"/>
<xsl:call-template name="getStringRTF">
<xsl:with-param name="stringName" select="'OpenQuote'"/>
</xsl:call-template>
<xsl:apply-templates/>
<xsl:call-template name="getStringRTF">
<xsl:with-param name="stringName" select="'CloseQuote'"/>
</xsl:call-template>
</xsl:template>

<!-- named template library -->
<xsl:template name="br-replace">
<xsl:param name="word"/>
<!-- </xsl:text> on next line on purpose to get newline -->
<xsl:variable name="cr">
<xsl:text>\line
</xsl:text>
</xsl:variable>
<xsl:choose>
<xsl:when test="contains($word,$cr)">
<xsl:value-of select="substring-before($word,$cr)"/>
<br/>
<xsl:call-template name="br-replace">
<xsl:with-param name="word"
select="substring-after($word,$cr)"/>
</xsl:call-template>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select="$word"/>
</xsl:otherwise>
</xsl:choose>
</xsl:template>

<xsl:template name="root">
<!-- Include RTF prolog from external file inc\rtf_variables.xsl -->
<xsl:value-of select="$rtf:prolog"/>
<xsl:value-of select="$rtf:font_table"/>
<xsl:value-of select="$rtf:color_table"/>
<xsl:value-of select="$rtf:stylesheet"/>
<xsl:value-of select="$rtf:preliminaries"/>
<xsl:call-template name="createFrontMatter"/>
<!-- <xsl:call-template name="gen-list-table"/> -->
<xsl:apply-templates/>}</xsl:template>

<xsl:template name="block-title">
<xsl:param name="depth"/>
<xsl:choose>
<xsl:when test="$depth='1'">
<xsl:call-template name="block-title-h1"/>
</xsl:when>
<xsl:when test="$depth='2'">
<xsl:call-template name="block-title-h2"/>
</xsl:when>
<xsl:when test="$depth='3'">
<xsl:call-template name="block-title-h3"/>
</xsl:when>
<xsl:when test="$depth='4'">
<xsl:call-template name="block-title-h4"/>
</xsl:when>
<xsl:when test="$depth='5'">
<xsl:call-template name="block-title-h5"/>
</xsl:when>
<xsl:otherwise>
<xsl:call-template name="block-title-h6"/>
</xsl:otherwise>
</xsl:choose>
</xsl:template>

<xsl:template name="block-title-h1">{\pard \s1 \f1\fs48\b\sb120\sa240 <xsl:apply-templates/>\par}</xsl:template>
<xsl:template name="block-title-h2">{\pard \s2 \f1\fs36\b\sb90\sa180 <xsl:apply-templates/>\par} </xsl:template>
<xsl:template name="block-title-h3">{\pard \s3 \f1\fs24\b\sa120 <xsl:apply-templates/>\par}</xsl:template>
<xsl:template name="block-title-h4">{\pard \s4 \f1\fs20\b\sa100 <xsl:apply-templates/>\par}</xsl:template>
<xsl:template name="block-title-h5">{\pard \s5 \f1\fs18\b\sa90 <xsl:apply-templates/>\par}</xsl:template>
<xsl:template name="block-title-h6">{\pard \s6 \f1\fs16\b\sa80 <xsl:apply-templates/>\par}</xsl:template>

<xsl:template name="block-p">
<!-- Tagsmiths: Suppress \par \pard when the context is first p in li -->
<xsl:choose>
<xsl:when test="parent::*[contains(@class,' topic/li ')] and position() = 1">
<!-- Tagsmiths: this next line resets the style, font, and size to
the same values used in by p in other contexts. -->
<!-- <xsl:text>\s0\f0\fs24 </xsl:text> -->
</xsl:when>
<xsl:otherwise>
<!-- Tagsmiths: this next line used to appear unconditionally -->
<!-- <xsl:text>\par \pard \s0\f0\fs24</xsl:text> -->
</xsl:otherwise>
</xsl:choose>
<xsl:if test="ancestor::*[contains(@class,' topic/table ') or contains(@class,' topic/simpletable ')]">\intbl</xsl:if>
<!-- Tagsmiths: make the inserted space conditional, suppressing it for first p in li -->
<xsl:if test="parent::*[not(contains(@class,' topic/li '))] or position() != 1">
<xsl:text/>
</xsl:if>
{\pard \sa240 <xsl:apply-templates/>\par}
<!-- Tagsmiths: make the next rtf string conditional, suppressing it for first p in li -->
<xsl:if test="parent::*[not(contains(@class,' topic/li '))] or position() != 1">
<!-- <xsl:text>\par</xsl:text> -->
</xsl:if>
</xsl:template>

<xsl:template name="block-lq">
\par \pard\li720\fi-360\f0\fs24<xsl:if test="ancestor::*[contains(@class,' topic/table ') or contains(@class,' topic/simpletable ')]">\intbl </xsl:if>
<xsl:apply-templates/>
\par
</xsl:template>

<!-- Citation links -->
<xsl:template match="*[contains(@class,' topic/lq ')]" name="topic.lq">

<xsl:variable name="samefile">
<xsl:choose>
<xsl:when test="@href and starts-with(@href,'#')">
<xsl:value-of select="'true'"/>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select="'false'"/>
</xsl:otherwise>
</xsl:choose>
</xsl:variable>

<xsl:variable name="href-value">
<xsl:choose>
<xsl:when test="@href and starts-with(@href,'#')">
<xsl:choose>
<xsl:when test="contains(@href,'/')">
<xsl:value-of select="substring-after(@href,'/')"/>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select="substring-after(@href,'#')"/>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@href and contains(@href,'#')">
<xsl:value-of select="substring-before(@href,'#')"/>
</xsl:when>
<xsl:when test="@href and not(@href='')">
<xsl:value-of select="@href"/>
</xsl:when>
</xsl:choose>
</xsl:variable>
<xsl:apply-templates/>
<xsl:choose>
<xsl:when test="@href and not(@href='')">
<!-- Insert citation as link, use @href as-is -->
<!-- \par\pard\qr\f0\fs24 -->
<!-- <xsl:if test="ancestor::*[contains(@class,' topic/table ') or contains(@class,' topic/simpletable ')]">\intbl </xsl:if> -->
{\field
{\*\fldinst 
{HYPERLINK 
<xsl:if test="$samefile='true'">\\l</xsl:if>
"<xsl:value-of select="$href-value"/>"
}}{\fldrslt {\s8 \f0\fs24\ul\cf2 
<xsl:choose>
<xsl:when
test="@reftitle">
<xsl:call-template name="get-ascii">
<xsl:with-param name="txt">
<xsl:value-of
select="@reftitle"/>
</xsl:with-param>
</xsl:call-template>
</xsl:when>
<xsl:otherwise>
<xsl:call-template
name="get-ascii">
<xsl:with-param name="txt">
<xsl:value-of select="@href"/>
</xsl:with-param>
</xsl:call-template>
</xsl:otherwise>
</xsl:choose>
}}}
<!-- \par\pard\ql\f0\fs24 -->
<!-- <xsl:if test="ancestor::*[contains(@class,' topic/table ') or contains(@class,' topic/simpletable ')]">\intbl </xsl:if> -->
</xsl:when>
<xsl:when test="@reftitle and not(@reftitle='')">
<!-- Insert citation text -->
<!-- \par\pard\qr\f0\fs24 -->
<!-- <xsl:if test="ancestor::*[contains(@class,' topic/table ') or contains(@class,' topic/simpletable ')]">\intbl </xsl:if> -->
<xsl:call-template name="get-ascii">
<xsl:with-param name="txt">
<xsl:value-of select="@reftitle"/>
</xsl:with-param>
</xsl:call-template>
<!-- \par\pard\ql\f0\fs24 -->
<!-- <xsl:if test="ancestor::*[contains(@class,' topic/table ') or contains(@class,' topic/simpletable ')]">\intbl </xsl:if> -->
</xsl:when>
<xsl:otherwise>
<!--nop - do nothing-->
</xsl:otherwise>
</xsl:choose>
</xsl:template>


<!-- font-weight="bold" -->
<xsl:template name="inline-em">{\b <xsl:apply-templates/>}</xsl:template>

<!-- link-like -->
<xsl:template name="inline-link">{\cf2\ul <xsl:apply-templates/>}</xsl:template>

<xsl:template name="gen-id">
<xsl:choose>
<xsl:when test="@id and not(id='')">
<xsl:call-template name="gen-bookmark">
<xsl:with-param name="name" select="@id"/>
</xsl:call-template>
</xsl:when>
</xsl:choose>
</xsl:template>

<!-- Bookmarks (internal links) -->
<xsl:template name="gen-bookmark">
<xsl:param name="name">
<xsl:value-of select="."/>
</xsl:param>
{
{\*\bkmkstart <xsl:value-of select="$name"/>}
{\*\bkmkend <xsl:value-of select="$name"/>}
}
</xsl:template>

<!-- Hyperlinks -->
<xsl:template match="*[contains(@class,' topic/xref ')]|*[contains(@class,' topic/link ')]">

<xsl:variable name="samefile">
<xsl:choose>
<xsl:when test="@href and starts-with(@href,'#')">
<xsl:value-of select="'true'"/>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select="'false'"/>
</xsl:otherwise>
</xsl:choose>
</xsl:variable>

<xsl:variable name="href-value">
<xsl:choose>
<xsl:when test="@href and starts-with(@href,'#')">
<xsl:choose>
<xsl:when test="contains(@href,'/')">
<xsl:value-of select="substring-after(@href,'/')"/>
</xsl:when>
<xsl:otherwise>
<xsl:value-of select="substring-after(@href,'#')"/>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="@href and contains(@href,'#')">
<xsl:value-of select="substring-before(@href,'#')"/>
</xsl:when>
<xsl:when test="@href and not(@href='')">
<xsl:value-of select="@href"/>
</xsl:when>
</xsl:choose>
</xsl:variable>

<xsl:call-template name="gen-id"/>
<xsl:choose>
<xsl:when test="@href and not(@href='')">
<!-- <xsl:if test="not(preceding-sibling::*[contains(@class,' topic/link ')]) and contains(@class,' topic/link ')">\par </xsl:if> -->
{\pard 
{\field{\*\fldinst {HYPERLINK 
<xsl:if test="$samefile='true'">\\l</xsl:if>
"<xsl:value-of select="$href-value"/>"
}}{\fldrslt {\s8 \f0\fs24\ul\cf2 
<xsl:call-template name="gen-linktxt"/>
}}}
\par}
<!-- <xsl:if test="contains(@class,' topic/link ')">
<xsl:apply-templates select="*[contains(@class,' topic/desc ')]"/>\par 
</xsl:if> -->
</xsl:when>
<xsl:otherwise>
<xsl:call-template name="output-message">
<xsl:with-param name="msgnum">028</xsl:with-param>
<xsl:with-param name="msgsev">E</xsl:with-param>
</xsl:call-template>
</xsl:otherwise>
</xsl:choose>

</xsl:template>

<xsl:template match="*[contains(@class,' topic/cite ')]">
{\i <xsl:apply-templates/>}
</xsl:template>

<xsl:template match="*[contains(@class,' topic/desc ')]">
<xsl:if test="../@role='child'">{\pard <xsl:apply-templates/>\par}</xsl:if>
</xsl:template>

<xsl:template name="gen-linktxt">
<xsl:choose>
<xsl:when test="contains(@class,' topic/xref ')">
<xsl:choose>
<xsl:when test="text() or *">
<xsl:apply-templates/>
</xsl:when>
<xsl:otherwise>
<xsl:call-template name="get-ascii">
<xsl:with-param name="txt">
<xsl:value-of select="@href"/>
</xsl:with-param>
</xsl:call-template>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="contains(@class,' topic/link ')">
<xsl:choose>
<xsl:when test="*[contains(@class,' topic/linktext ')]">
<xsl:call-template name="get-ascii">
<xsl:with-param name="txt">
<xsl:value-of select="*[contains(@class,' topic/linktext ')]"/>
</xsl:with-param>
</xsl:call-template>
</xsl:when>
<xsl:when test="text()">
<xsl:call-template name="get-ascii">
<xsl:with-param name="txt">
<xsl:value-of select="text()"/>
</xsl:with-param>
</xsl:call-template>
</xsl:when>
<xsl:otherwise>
<xsl:call-template name="get-ascii">
<xsl:with-param name="txt">
<xsl:value-of select="@href"/>
</xsl:with-param>
</xsl:call-template>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
</xsl:choose>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/prolog ')]"/>
<xsl:template match="*[contains(@class,' topic/titlealts ')]"/>

<!-- Added for DITA 1.1 "Shortdesc proposal" -->
<xsl:template match="*[contains(@class,' topic/abstract ')]">{\pard \sa240 <xsl:apply-templates/>\par}</xsl:template>

<xsl:template match="*[contains(@class,' topic/shortdesc ')]">{\pard \sa240 <xsl:apply-templates/>\par}</xsl:template>

<xsl:template match="*[contains(@class,' topic/note ')]" name="topic.note">
<xsl:choose>
<xsl:when test="@type='note'">
\par \s0\f1\fs24\b <xsl:call-template name="getStringRTF">
<xsl:with-param name="stringName" select="'Note'"/>
</xsl:call-template>
<xsl:call-template name="getStringRTF">
<xsl:with-param name="stringName" select="'ColonSymbol'"/>
</xsl:call-template>
<xsl:text/>\pard \s0\f1\fs24<xsl:if test="ancestor::*[contains(@class,' topic/table ') or contains(@class,' topic/simpletable ')]">\intbl </xsl:if>
<xsl:apply-templates/>
\par \s0\f0\fs24
</xsl:when>
<xsl:when test="@type='tip'">
\par \s0\f1\fs24\b <xsl:call-template name="getStringRTF">
<xsl:with-param name="stringName" select="'Tip'"/>
</xsl:call-template>
<xsl:call-template name="getStringRTF">
<xsl:with-param name="stringName" select="'ColonSymbol'"/>
</xsl:call-template>
<xsl:text/>\pard \s0\f1\fs24<xsl:if test="ancestor::*[contains(@class,' topic/table ') or contains(@class,' topic/simpletable ')]">\intbl </xsl:if>
<xsl:apply-templates/>
\par \s0\f0\fs24
</xsl:when>
<xsl:when test="@type='fastpath'">
\par \s0\f1\fs24\b <xsl:call-template name="getStringRTF">
<xsl:with-param name="stringName" select="'Fastpath'"/>
</xsl:call-template>
<xsl:call-template name="getStringRTF">
<xsl:with-param name="stringName" select="'ColonSymbol'"/>
</xsl:call-template>
<xsl:text/>\pard \s0\f1\fs24<xsl:if test="ancestor::*[contains(@class,' topic/table ') or contains(@class,' topic/simpletable ')]">\intbl </xsl:if>
<xsl:apply-templates/>
\par \s0\f0\fs24
</xsl:when>
<xsl:when test="@type='important'">
\par \s0\f1\fs24\b <xsl:call-template name="getStringRTF">
<xsl:with-param name="stringName" select="'Important'"/>
</xsl:call-template>
<xsl:call-template name="getStringRTF">
<xsl:with-param name="stringName" select="'ColonSymbol'"/>
</xsl:call-template>
<xsl:text/>\pard \s0\f1\fs24<xsl:if test="ancestor::*[contains(@class,' topic/table ') or contains(@class,' topic/simpletable ')]">\intbl </xsl:if>
<xsl:apply-templates/>
\par \s0\f0\fs24
</xsl:when>
<xsl:when test="@type='remember'">
\par \s0\f1\fs24\b <xsl:call-template name="getStringRTF">
<xsl:with-param name="stringName" select="'Remember'"/>
</xsl:call-template>
<xsl:call-template name="getStringRTF">
<xsl:with-param name="stringName" select="'ColonSymbol'"/>
</xsl:call-template>
<xsl:text/>\pard \s0\f1\fs24<xsl:if test="ancestor::*[contains(@class,' topic/table ') or contains(@class,' topic/simpletable ')]">\intbl </xsl:if>
<xsl:apply-templates/>
\par \s0\f0\fs24
</xsl:when>
<xsl:when test="@type='restriction'">
\par \s0\f1\fs24\b <xsl:call-template name="getStringRTF">
<xsl:with-param name="stringName" select="'Restriction'"/>
</xsl:call-template>
<xsl:call-template name="getStringRTF">
<xsl:with-param name="stringName" select="'ColonSymbol'"/>
</xsl:call-template>
<xsl:text/>\pard \s0\f1\fs24<xsl:if test="ancestor::*[contains(@class,' topic/table ') or contains(@class,' topic/simpletable ')]">\intbl </xsl:if>
<xsl:apply-templates/>
\par \s0\f0\fs24
</xsl:when>
<xsl:when test="@type='attention'">
\par \s0\f1\fs24\b <xsl:call-template name="getStringRTF">
<xsl:with-param name="stringName" select="'Attention'"/>
</xsl:call-template>
<xsl:call-template name="getStringRTF">
<xsl:with-param name="stringName" select="'ColonSymbol'"/>
</xsl:call-template>
<xsl:text/>\pard \s0\f1\fs24<xsl:if test="ancestor::*[contains(@class,' topic/table ') or contains(@class,' topic/simpletable ')]">\intbl </xsl:if>
<xsl:apply-templates/>
\par \s0\f0\fs24
</xsl:when>
<xsl:when test="@type='caution'">
\par \s0\f1\fs24\b <xsl:call-template name="getStringRTF">
<xsl:with-param name="stringName" select="'Caution'"/>
</xsl:call-template>
<xsl:call-template name="getStringRTF">
<xsl:with-param name="stringName" select="'ColonSymbol'"/>
</xsl:call-template>
<xsl:text/>\pard \s0\f1\fs24<xsl:if test="ancestor::*[contains(@class,' topic/table ') or contains(@class,' topic/simpletable ')]">\intbl </xsl:if>
<xsl:apply-templates/>
\par \s0\f0\fs24
</xsl:when>
<xsl:when test="@type='danger'">
\par \s0\f1\fs24\b <xsl:call-template name="getStringRTF">
<xsl:with-param name="stringName" select="'Danger'"/>
</xsl:call-template>
<xsl:call-template name="getStringRTF">
<xsl:with-param name="stringName" select="'ColonSymbol'"/>
</xsl:call-template>
<xsl:text/>\pard \s0\f1\fs24<xsl:if test="ancestor::*[contains(@class,' topic/table ') or contains(@class,' topic/simpletable ')]">\intbl </xsl:if>
<xsl:apply-templates/>
\par \s0\f0\fs24
</xsl:when>
<xsl:when test="@type='other'">
\par \s0\f1\fs24\b <xsl:choose>
<xsl:when test="@othertype and
not(@othertype='')">
<xsl:call-template name="get-ascii">
<xsl:with-param name="txt">
<xsl:value-of select="@othertype"/>
</xsl:with-param>
</xsl:call-template>
</xsl:when>
<xsl:otherwise>
<xsl:text>[other]</xsl:text>
</xsl:otherwise>
</xsl:choose>
<xsl:call-template name="getStringRTF">
<xsl:with-param name="stringName" select="'ColonSymbol'"/>
</xsl:call-template>
<xsl:text/>\pard \s0\f1\fs24<xsl:if test="ancestor::*[contains(@class,' topic/table ') or contains(@class,' topic/simpletable ')]">\intbl </xsl:if>
<xsl:apply-templates/>
\par \s0\f0\fs24
</xsl:when>
<xsl:otherwise>
<xsl:apply-templates/>
</xsl:otherwise>
</xsl:choose>
</xsl:template>

<xsl:template match="text()[contains(.,'\') or contains(.,'{') or contains(.,'}')]">
<xsl:variable name="gentext">
<xsl:call-template name="gen-txt1">
<xsl:with-param name="txt" select="."/>
</xsl:call-template>
</xsl:variable>
<xsl:choose>
<xsl:when test="ancestor::*[contains(@class,' topic/pre ')] or ancestor::*[contains(@class,' topic/lines ')]">
<xsl:value-of select="$gentext"/>
</xsl:when>
<xsl:otherwise>
<xsl:if test="starts-with($gentext,' ')">
<xsl:text/>
</xsl:if>
<xsl:value-of select="normalize-space($gentext)"/>
<xsl:if test="substring($gentext,string-length($gentext))=' '">
<xsl:text/>
</xsl:if>
</xsl:otherwise>
</xsl:choose>
</xsl:template>

<xsl:template name="gen-txt1">
<xsl:param name="txt"/>
<xsl:choose>
<xsl:when test="not(contains($txt,'\'))">
<xsl:call-template name="gen-txt2">
<xsl:with-param name="txt" select="$txt"/>
</xsl:call-template>
</xsl:when>
<xsl:otherwise>
<xsl:call-template name="gen-txt2">
<xsl:with-param name="txt" select="substring-before($txt,'\')"/>
</xsl:call-template>
<xsl:text>\\</xsl:text>
<xsl:call-template name="gen-txt1">
<xsl:with-param name="txt" select="substring-after($txt,'\')"/>
</xsl:call-template>
</xsl:otherwise>
</xsl:choose>
</xsl:template>

<xsl:template name="gen-txt2">
<xsl:param name="txt"/>
<xsl:choose>
<xsl:when test="not(contains($txt,'{'))">
<xsl:call-template name="gen-txt3">
<xsl:with-param name="txt" select="$txt"/>
</xsl:call-template>
</xsl:when>
<xsl:otherwise>
<xsl:call-template name="gen-txt3">
<xsl:with-param name="txt" select="substring-before($txt,'{')"/>
</xsl:call-template>
<xsl:text>\{</xsl:text>
<xsl:call-template name="gen-txt2">
<xsl:with-param name="txt" select="substring-after($txt,'{')"/>
</xsl:call-template>
</xsl:otherwise>
</xsl:choose>
</xsl:template>

<xsl:template name="gen-txt3">
<xsl:param name="txt"/>
<xsl:choose>
<xsl:when test="not(contains($txt,'}'))">
<xsl:call-template name="gen-txt">
<xsl:with-param name="txt" select="$txt"/>
</xsl:call-template>
</xsl:when>
<xsl:otherwise>
<xsl:call-template name="gen-txt">
<xsl:with-param name="txt" select="substring-before($txt,'}')"/>
</xsl:call-template>
<xsl:text>\}</xsl:text>
<xsl:call-template name="gen-txt3">
<xsl:with-param name="txt" select="substring-after($txt,'}')"/>
</xsl:call-template>
</xsl:otherwise>
</xsl:choose>
</xsl:template>

<xsl:template name="gen-txt">
<xsl:param name="txt"/>
<xsl:variable name="newline">
<xsl:text>
</xsl:text>
</xsl:variable>
<xsl:choose>
<xsl:when test="ancestor::*[contains(@class,' topic/pre ')] or ancestor::*[contains(@class,' topic/lines ')]">
<xsl:choose>
<xsl:when test="contains($txt,$newline)">
<xsl:value-of select="substring-before($txt,$newline)"/>
<xsl:text>\par </xsl:text>
<xsl:call-template name="gen-txt">
<xsl:with-param name="txt" select="substring-after($txt,$newline)"/>
</xsl:call-template>
</xsl:when>
<xsl:otherwise>
<xsl:call-template name="get-ascii">
<xsl:with-param name="txt" select="$txt"/>
</xsl:call-template>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:otherwise>
<xsl:call-template name="get-ascii">
<xsl:with-param name="txt" select="$txt"/>
</xsl:call-template>
</xsl:otherwise>
</xsl:choose>
</xsl:template>

<xsl:template match="text()">
<xsl:choose>
<xsl:when test="ancestor::*[contains(@class,' topic/pre ')] or ancestor::*[contains(@class,' topic/lines ')]">
<xsl:call-template name="gen-txt">
<xsl:with-param name="txt" select="."/>
</xsl:call-template>
</xsl:when>
<xsl:otherwise>
<xsl:if test="normalize-space(substring(., 1, 1))='' and not(normalize-space(.)='')">
<xsl:text/>
</xsl:if>
<xsl:call-template name="get-ascii">
<xsl:with-param name="txt">
<xsl:value-of select="normalize-space(.)"/>
</xsl:with-param>
</xsl:call-template>
<xsl:if test="normalize-space(substring(.,
string-length(.), 1))='' and not(normalize-space(.)='')">
<xsl:text/>
</xsl:if>
</xsl:otherwise>
</xsl:choose>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/draft-comment ')]">
<xsl:if test="$DRAFT='yes'">
<xsl:text>\par \s0\f1\fs24\cb4\b </xsl:text>
<xsl:call-template name="getStringRTF">
<xsl:with-param name="stringName" select="'Draft comment'"/>
</xsl:call-template>
<xsl:call-template name="getStringRTF">
<xsl:with-param name="stringName" select="'ColonSymbol'"/>
</xsl:call-template>
<xsl:text/>
<xsl:text>\pard \s0\f1\fs24\cb4</xsl:text>
<xsl:if test="ancestor::*[contains(@class,' topic/table ') or contains(@class,' topic/simpletable ')]">\intbl</xsl:if>
<xsl:text/>
<xsl:apply-templates/>
<xsl:text>\par \s0\f0\fs24</xsl:text>
</xsl:if>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/boolean ')]">
<xsl:text>{\s0\f1\fs24\cf5 boolean: </xsl:text>
<xsl:value-of select="@state"/>
<xsl:text>}</xsl:text>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/state ')]">
<xsl:text>{\s0\f1\fs24\cf4 </xsl:text>
<xsl:value-of select="name()"/>
<xsl:text>: </xsl:text>
<xsl:value-of select="@name"/>
<xsl:text>=</xsl:text>
<xsl:value-of select="@value"/>
<xsl:text>}</xsl:text>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/tm ')]">
<xsl:text>{\s0\f1\fs24 </xsl:text>
<xsl:apply-templates/>
<xsl:choose>
<xsl:when test="@tmtype='tm'">
<xsl:text>{\super (TM)}</xsl:text>
</xsl:when>
<xsl:when test="@tmtype='service'">
<xsl:text>{\super (SM)}</xsl:text>
</xsl:when>
<xsl:when test="@tmtype='reg'">
<xsl:text>{\super (R)}</xsl:text>
</xsl:when>
</xsl:choose>
<xsl:text>}</xsl:text>
</xsl:template>

<xsl:template
match="*[contains(@class,' topic/required-cleanup ')]">
<xsl:if test="$DRAFT='yes'">
<xsl:text>\par \s0\f1\fs24\cb4\b</xsl:text>
<xsl:call-template name="getStringRTF">
<xsl:with-param name="stringName" select="'Required cleanup'" />
</xsl:call-template>
<xsl:call-template name="getStringRTF">
<xsl:with-param name="stringName" select="'ColonSymbol'" />
</xsl:call-template>
<xsl:text/>
<xsl:text>\pard \s0\f1\fs24\cb4</xsl:text>
<xsl:if
test="ancestor::*[contains(@class,' topic/table ') or contains(@class,' topic/simpletable ')]">
\intbl
</xsl:if>
<xsl:apply-templates />
<xsl:text>\par \s0\f0\fs24</xsl:text>
</xsl:if>
</xsl:template>

<!-- Add for "New <data> element (#9)" in DITA 1.1 -->
<xsl:template match="*[contains(@class,' topic/data ')]" />

<!-- Add for "Support foreign content vocabularies such as
MathML and SVG with <unknown> (#35) " in DITA 1.1 -->
<xsl:template match="*[contains(@class,' topic/foreign ') or contains(@class,' topic/unknown ')]" />

</xsl:stylesheet>
