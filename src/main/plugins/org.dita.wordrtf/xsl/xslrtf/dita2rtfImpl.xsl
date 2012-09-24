<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file for 
     applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2005, 2006 All Rights Reserved. -->
<xsl:stylesheet
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0">
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
<xsl:call-template name="gen-id"/>\par \plain\f4\fs36\b <xsl:apply-templates/>
\par \plain\f2\fs24
</xsl:template>

<xsl:template match="synsect">
\par \plain\f4\fs36\b Syntax 
\par \plain\f2\fs24 
<xsl:apply-templates/>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/section ')]">
<xsl:call-template name="gen-id"/>\par \pard \s0\f2\fs24 <xsl:apply-templates/>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/example ')]">
<xsl:call-template name="gen-id"/>\par \pard \s0\f2\fs24 <xsl:apply-templates/>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/example ')]/*[contains(@class,' topic/title ')]">
<xsl:call-template name="gen-id"/>\plain\f4\fs24\b <xsl:apply-templates/>\par \plain\f2\fs24 
</xsl:template>

<!-- =========== block things ============ -->

<xsl:template match="*[contains(@class,' topic/p ')]">
<xsl:call-template name="gen-id"/><xsl:call-template name="block-p"/>
</xsl:template>

<!--xsl:template match="*[contains(@class,' topic/lq ')]">
<xsl:call-template name="gen-id"/><xsl:call-template name="block-lq"/>
</xsl:template-->


<!-- phrases -->

<xsl:template match="*[contains(@class,' hi-d/tt ')]">
  <xsl:call-template name="inline-em"/>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/q ')]"><xsl:call-template name="gen-id"/><xsl:call-template name="getStringRTF"><xsl:with-param name="stringName" select="'OpenQuote'"/></xsl:call-template><xsl:apply-templates/><xsl:call-template name="getStringRTF"><xsl:with-param name="stringName" select="'CloseQuote'"/></xsl:call-template></xsl:template>



<!-- named template library -->
 
   <xsl:template name="br-replace">
     <xsl:param name="word"/>
<!-- </xsl:text> on next line on purpose to get newline -->
<xsl:variable name="cr"><xsl:text>\n
</xsl:text></xsl:variable>
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

<!-- standard RTF library mapped to formatting objects -->

  <xsl:template name="root">{\rtf1\ansi\ansicpg<xsl:value-of select="$code-page"/>\deff0\deftab720\deflang1033\deflangfe2052{\fonttbl{\f0\fswiss MS Sans Serif;}
{\f1\froman\fcharset2 Symbol;}
{\f2\froman Times New Roman;}
{\f3\froman Times New Roman;}
{\f4\fswiss Arial;}
{\f5\fmono Courier New;}
{\f10\fnil\fcharset2{\*\panose 05000000000000000000}Wingdings;}
{\f13\fnil\fcharset134 \'cb\'ce\'cc\'e5{\*\falt SimSun};}}
{\colortbl\red0\green0\blue0;\red0\green0\blue255;\red128\green128\blue128;\red255\green0\blue0;\red0\green255\blue0;}
{\stylesheet{\s0 \f2\fs24 Normal;}
{\s1 \f4\fs48\b heading 1;}
{\s2 \f4\fs36\b heading 2;}
{\s3 \f4\fs24\b heading 3;}
{\s4 \f4\fs20\b heading 4;}
{\s5 \f4\fs18\b heading 5;}
{\s6 \f4\fs16\b heading 6;}
{\s7 \f4\fs24\b table header;}
{\s8 \f2\fs24 link;}
{\s9 \f4\fs24\b table title;}}<xsl:call-template name="gen-list-table"/>
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

<!-- font-size="24pt"
     font-weight="bold"
     space-before.optimum="16pt"
     space-after.optimum="12pt" -->
<xsl:template name="block-title-h1">
\par \pard\plain\s1\f4\fs48\b <xsl:apply-templates/>
\par \plain\f2\fs24
</xsl:template>

<!-- font-size="16pt"
     font-weight="bold"
     space-before.optimum="14pt"
     space-after.optimum="14pt" \pard\li720\fi-360-->
<xsl:template name="block-title-h2">
\par \plain\s2\f4\fs36\b <xsl:apply-templates/> 
\par \plain\f2\fs24 
</xsl:template>

<xsl:template name="block-title-h3">
\par \plain\s3\f4\fs24\b <xsl:apply-templates/> 
\par \plain\f2\fs24 
</xsl:template>

<xsl:template name="block-title-h4">
\par \plain\s4\f4\fs20\b <xsl:apply-templates/> 
\par \plain\f2\fs24 
</xsl:template>

<xsl:template name="block-title-h5">
\par \plain\s5\f4\fs18\b <xsl:apply-templates/> 
\par \plain\f2\fs24 
</xsl:template>

<xsl:template name="block-title-h6">
\par \plain\s6\f4\fs16\b <xsl:apply-templates/> 
\par \plain\f2\fs24 
</xsl:template>


<!-- space-before.optimum="8pt"
     space-after.optimum="8pt" -->
<xsl:template name="block-p">
  <!-- Tagsmiths: Suppress \par \pard when the context is first p in li -->
  <xsl:choose>
    <xsl:when test="parent::*[contains(@class,' topic/li ')] and position() = 1">
      <!-- Tagsmiths: this next line resets the style, font, and size to 
        the same values used in by p in other contexts. -->
      <xsl:text>\s0\f2\fs24 </xsl:text>
    </xsl:when>
    <xsl:otherwise>
      <!-- Tagsmiths: this next line used to appear unconditionally -->
      <xsl:text>\par \pard \s0\f2\fs24</xsl:text>
    </xsl:otherwise>
  </xsl:choose>
  <xsl:if test="ancestor::*[contains(@class,' topic/table ') or contains(@class,' topic/simpletable ')]">\intbl</xsl:if>
  <!-- Tagsmiths: make the inserted space conditional, suppressing it for first p in li -->
  <xsl:if test="parent::*[not(contains(@class,' topic/li '))] or position() != 1">
    <xsl:text> </xsl:text>
  </xsl:if><xsl:apply-templates/>
  <!-- Tagsmiths: make the next rtf string conditional, suppressing it for first p in li -->
  <xsl:if test="parent::*[not(contains(@class,' topic/li '))] or position() != 1">
    <xsl:text>\par</xsl:text>
  </xsl:if>
</xsl:template>


<xsl:template name="block-lq">
\par \pard\li720\fi-360\plain\f2\fs24<xsl:if test="ancestor::*[contains(@class,' topic/table ') or contains(@class,' topic/simpletable ')]">\intbl </xsl:if>
<xsl:apply-templates/>
\par
</xsl:template>

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
   <xsl:when test="@href and not(@href='')"> <!-- Insert citation as link, use @href as-is -->
\par\pard\plain\qr\f2\fs24<xsl:if test="ancestor::*[contains(@class,' topic/table ') or
contains(@class,' topic/simpletable ')]">\intbl </xsl:if>{\field{\*\fldinst {\s8 \f2\fs24\ul\cf1
HYPERLINK <xsl:if test="$samefile='true'">\\l</xsl:if> "<xsl:value-of
select="$href-value"/>"}}{\fldrslt {\s8 \f2\fs24\ul\cf1 <xsl:choose><xsl:when
  test="@reftitle"><xsl:call-template name="get-ascii"><xsl:with-param name="txt"><xsl:value-of
  select="@reftitle"/></xsl:with-param></xsl:call-template></xsl:when><xsl:otherwise><xsl:call-template
    name="get-ascii"><xsl:with-param name="txt"><xsl:value-of select="@href"/></xsl:with-param></xsl:call-template></xsl:otherwise></xsl:choose>\s8 \f2\fs24\ul\cf1}}}\par\pard\ql\f2\fs24<xsl:if test="ancestor::*[contains(@class,' topic/table ') or contains(@class,' topic/simpletable ')]">\intbl </xsl:if>
   </xsl:when>
   <xsl:when test="@reftitle and not(@reftitle='')"> <!-- Insert citation text -->
\par\pard\plain\qr\f2\fs24<xsl:if test="ancestor::*[contains(@class,' topic/table ') or
contains(@class,' topic/simpletable ')]">\intbl </xsl:if><xsl:call-template
  name="get-ascii"><xsl:with-param name="txt"><xsl:value-of select="@reftitle"/></xsl:with-param></xsl:call-template>\par\pard\ql\f2\fs24<xsl:if test="ancestor::*[contains(@class,' topic/table ') or contains(@class,' topic/simpletable ')]">\intbl </xsl:if></xsl:when>
   <xsl:otherwise><!--nop - do nothing--></xsl:otherwise>
  </xsl:choose>
</xsl:template>


<!-- font-weight="bold" -->
<xsl:template name="inline-em">{\b <xsl:apply-templates/>}</xsl:template>

<!-- link-like -->
<xsl:template name="inline-link">\plain\f2\fs24\cf1\ul <xsl:apply-templates/>\plain\f2\fs24 </xsl:template>

<xsl:template name="gen-id">
  <xsl:choose>
    <xsl:when test="@id and not(id='')">
      <xsl:call-template name="gen-bookmark">
        <xsl:with-param name="name" select="@id"/>
      </xsl:call-template>
    </xsl:when>
  </xsl:choose>  
</xsl:template>

<xsl:template name="gen-bookmark">
  <xsl:param name="name"><xsl:value-of select="."/></xsl:param>
{\bkmkstart <xsl:value-of select="$name"/>}{\bkmkend <xsl:value-of select="$name"/>}
</xsl:template>

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
<xsl:when test="@href and not(@href='')"><xsl:if test="not(preceding-sibling::*[contains(@class,' topic/link ')]) and contains(@class,' topic/link ')">\par </xsl:if>
{\field{\*\fldinst {\s8 \f2\fs24\ul\cf1 HYPERLINK <xsl:if test="$samefile='true'">\\l</xsl:if>
"<xsl:value-of select="$href-value"/>"}}{\fldrslt {\s8 \f2\fs24\ul\cf1 <xsl:call-template
name="gen-linktxt"/>\s8 \f2\fs24\ul\cf1}}}\s8
<xsl:if test="contains(@class,' topic/link ')"><xsl:apply-templates select="*[contains(@class,' topic/desc ')]"/>\par </xsl:if>
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
  <xsl:text>{\i </xsl:text><xsl:apply-templates/><xsl:text>}</xsl:text>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/desc ')]">
<xsl:if test="../@role='child'">\par \plain\s0\f4\fs24 <xsl:apply-templates/> 
\plain\s0\f2\fs24 </xsl:if>
</xsl:template>

<xsl:template name="gen-linktxt">
<xsl:choose>
<xsl:when test="contains(@class,' topic/xref ')">
<xsl:choose>
<xsl:when test="text() or *">
<xsl:apply-templates/>
</xsl:when>
<xsl:otherwise>
<xsl:call-template name="get-ascii"><xsl:with-param name="txt"><xsl:value-of select="@href"/></xsl:with-param></xsl:call-template>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
<xsl:when test="contains(@class,' topic/link ')">
<xsl:choose>
<xsl:when test="*[contains(@class,' topic/linktext ')]">
<xsl:call-template name="get-ascii"><xsl:with-param name="txt"><xsl:value-of select="*[contains(@class,' topic/linktext ')]"/></xsl:with-param></xsl:call-template>
</xsl:when>
<xsl:when test="text()">
<xsl:call-template name="get-ascii"><xsl:with-param name="txt"><xsl:value-of select="text()"/></xsl:with-param></xsl:call-template>
</xsl:when>
<xsl:otherwise>
<xsl:call-template name="get-ascii"><xsl:with-param name="txt"><xsl:value-of select="@href"/></xsl:with-param></xsl:call-template>
</xsl:otherwise>
</xsl:choose>
</xsl:when>
</xsl:choose>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/prolog ')]"/>
<xsl:template match="*[contains(@class,' topic/titlealts ')]"/>

<!-- Added for DITA 1.1 "Shortdesc proposal" -->
<xsl:template match="*[contains(@class,' topic/abstract ')]">
<xsl:apply-templates/>\par
</xsl:template>

<xsl:template match="*[contains(@class,' topic/shortdesc ')]">
<xsl:apply-templates/>\par
</xsl:template>

<xsl:template match="*[contains(@class,' topic/note ')]" name="topic.note">
  <xsl:choose>
    <xsl:when test="@type='note'">
\par \plain\s0\f4\fs24\b <xsl:call-template name="getStringRTF"><xsl:with-param name="stringName" select="'Note'"/></xsl:call-template><xsl:call-template name="getStringRTF"><xsl:with-param name="stringName" select="'ColonSymbol'"/></xsl:call-template><xsl:text> </xsl:text>\pard \plain\s0\f4\fs24<xsl:if test="ancestor::*[contains(@class,' topic/table ') or contains(@class,' topic/simpletable ')]">\intbl </xsl:if><xsl:apply-templates/>
\par \plain\s0\f2\fs24
    </xsl:when>
    <xsl:when test="@type='tip'">
\par \plain\s0\f4\fs24\b <xsl:call-template name="getStringRTF"><xsl:with-param name="stringName" select="'Tip'"/></xsl:call-template><xsl:call-template name="getStringRTF"><xsl:with-param name="stringName" select="'ColonSymbol'"/></xsl:call-template><xsl:text> </xsl:text>\pard \plain\s0\f4\fs24<xsl:if test="ancestor::*[contains(@class,' topic/table ') or contains(@class,' topic/simpletable ')]">\intbl </xsl:if><xsl:apply-templates/>
\par \plain\s0\f2\fs24
    </xsl:when>
    <xsl:when test="@type='fastpath'">
\par \plain\s0\f4\fs24\b <xsl:call-template name="getStringRTF"><xsl:with-param name="stringName" select="'Fastpath'"/></xsl:call-template><xsl:call-template name="getStringRTF"><xsl:with-param name="stringName" select="'ColonSymbol'"/></xsl:call-template><xsl:text> </xsl:text>\pard \plain\s0\f4\fs24<xsl:if test="ancestor::*[contains(@class,' topic/table ') or contains(@class,' topic/simpletable ')]">\intbl </xsl:if><xsl:apply-templates/>
\par \plain\s0\f2\fs24
    </xsl:when>
    <xsl:when test="@type='important'">
\par \plain\s0\f4\fs24\b <xsl:call-template name="getStringRTF"><xsl:with-param name="stringName" select="'Important'"/></xsl:call-template><xsl:call-template name="getStringRTF"><xsl:with-param name="stringName" select="'ColonSymbol'"/></xsl:call-template><xsl:text> </xsl:text>\pard \plain\s0\f4\fs24<xsl:if test="ancestor::*[contains(@class,' topic/table ') or contains(@class,' topic/simpletable ')]">\intbl </xsl:if><xsl:apply-templates/>
\par \plain\s0\f2\fs24
    </xsl:when>
    <xsl:when test="@type='remember'">
\par \plain\s0\f4\fs24\b <xsl:call-template name="getStringRTF"><xsl:with-param name="stringName" select="'Remember'"/></xsl:call-template><xsl:call-template name="getStringRTF"><xsl:with-param name="stringName" select="'ColonSymbol'"/></xsl:call-template><xsl:text> </xsl:text>\pard \plain\s0\f4\fs24<xsl:if test="ancestor::*[contains(@class,' topic/table ') or contains(@class,' topic/simpletable ')]">\intbl </xsl:if><xsl:apply-templates/>
\par \plain\s0\f2\fs24
    </xsl:when>
    <xsl:when test="@type='restriction'">
\par \plain\s0\f4\fs24\b <xsl:call-template name="getStringRTF"><xsl:with-param name="stringName" select="'Restriction'"/></xsl:call-template><xsl:call-template name="getStringRTF"><xsl:with-param name="stringName" select="'ColonSymbol'"/></xsl:call-template><xsl:text> </xsl:text>\pard \plain\s0\f4\fs24<xsl:if test="ancestor::*[contains(@class,' topic/table ') or contains(@class,' topic/simpletable ')]">\intbl </xsl:if><xsl:apply-templates/>
\par \plain\s0\f2\fs24
    </xsl:when>
    <xsl:when test="@type='attention'">
\par \plain\s0\f4\fs24\b <xsl:call-template name="getStringRTF"><xsl:with-param name="stringName" select="'Attention'"/></xsl:call-template><xsl:call-template name="getStringRTF"><xsl:with-param name="stringName" select="'ColonSymbol'"/></xsl:call-template><xsl:text> </xsl:text>\pard \plain\s0\f4\fs24<xsl:if test="ancestor::*[contains(@class,' topic/table ') or contains(@class,' topic/simpletable ')]">\intbl </xsl:if><xsl:apply-templates/>
\par \plain\s0\f2\fs24
    </xsl:when>
    <xsl:when test="@type='caution'">
\par \plain\s0\f4\fs24\b <xsl:call-template name="getStringRTF"><xsl:with-param name="stringName" select="'Caution'"/></xsl:call-template><xsl:call-template name="getStringRTF"><xsl:with-param name="stringName" select="'ColonSymbol'"/></xsl:call-template><xsl:text> </xsl:text>\pard \plain\s0\f4\fs24<xsl:if test="ancestor::*[contains(@class,' topic/table ') or contains(@class,' topic/simpletable ')]">\intbl </xsl:if><xsl:apply-templates/>
\par \plain\s0\f2\fs24
    </xsl:when>
    <xsl:when test="@type='danger'">
\par \plain\s0\f4\fs24\b <xsl:call-template name="getStringRTF"><xsl:with-param name="stringName" select="'Danger'"/></xsl:call-template><xsl:call-template name="getStringRTF"><xsl:with-param name="stringName" select="'ColonSymbol'"/></xsl:call-template><xsl:text> </xsl:text>\pard \plain\s0\f4\fs24<xsl:if test="ancestor::*[contains(@class,' topic/table ') or contains(@class,' topic/simpletable ')]">\intbl </xsl:if><xsl:apply-templates/>
\par \plain\s0\f2\fs24
    </xsl:when>
    <xsl:when test="@type='other'">
\par \plain\s0\f4\fs24\b <xsl:choose><xsl:when test="@othertype and
  not(@othertype='')"><xsl:call-template name="get-ascii"><xsl:with-param name="txt"><xsl:value-of select="@othertype"/></xsl:with-param></xsl:call-template></xsl:when><xsl:otherwise><xsl:text>[other]</xsl:text></xsl:otherwise></xsl:choose><xsl:call-template name="getStringRTF"><xsl:with-param name="stringName" select="'ColonSymbol'"/></xsl:call-template><xsl:text> </xsl:text>\pard \plain\s0\f4\fs24<xsl:if test="ancestor::*[contains(@class,' topic/table ') or contains(@class,' topic/simpletable ')]">\intbl </xsl:if><xsl:apply-templates/>
\par \plain\s0\f2\fs24
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
<xsl:if test="starts-with($gentext,' ')"><xsl:text> </xsl:text></xsl:if><xsl:value-of select="normalize-space($gentext)"/><xsl:if test="substring($gentext,string-length($gentext))=' '"><xsl:text> </xsl:text></xsl:if>
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
<xsl:variable name="newline"><xsl:text>
</xsl:text></xsl:variable>    
    <xsl:choose>
      <xsl:when test="ancestor::*[contains(@class,' topic/pre ')] or ancestor::*[contains(@class,' topic/lines ')]">
        <xsl:choose>
          <xsl:when test="contains($txt,$newline)">
            <xsl:value-of select="substring-before($txt,$newline)"/><xsl:text>\par </xsl:text><xsl:call-template name="gen-txt"><xsl:with-param name="txt" select="substring-after($txt,$newline)"/></xsl:call-template>
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
<xsl:call-template name="gen-txt"><xsl:with-param name="txt" select="."/></xsl:call-template>
</xsl:when>
<xsl:otherwise>
  <xsl:if test="normalize-space(substring(., 1, 1))='' and not(normalize-space(.)='')"><xsl:text> </xsl:text></xsl:if><xsl:call-template name="get-ascii">
    <xsl:with-param name="txt"><xsl:value-of select="normalize-space(.)"/></xsl:with-param>
  </xsl:call-template><xsl:if test="normalize-space(substring(.,
    string-length(.), 1))='' and not(normalize-space(.)='')"><xsl:text> </xsl:text></xsl:if>
</xsl:otherwise>
</xsl:choose>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/draft-comment ')]">
<xsl:if test="$DRAFT='yes'">
<xsl:text>\par \plain\s0\f4\fs24\cb3\b </xsl:text><xsl:call-template name="getStringRTF"><xsl:with-param name="stringName" select="'Draft comment'"/></xsl:call-template><xsl:call-template name="getStringRTF"><xsl:with-param name="stringName" select="'ColonSymbol'"/></xsl:call-template><xsl:text> </xsl:text><xsl:text>\pard \plain\s0\f4\fs24\cb3</xsl:text><xsl:if test="ancestor::*[contains(@class,' topic/table ') or contains(@class,' topic/simpletable ')]">\intbl</xsl:if><xsl:text> </xsl:text><xsl:apply-templates/><xsl:text>\par \plain\s0\f2\fs24</xsl:text>
</xsl:if>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/boolean ')]">
<xsl:text>{\plain\s0\f4\fs24\cf4 boolean: </xsl:text><xsl:value-of select="@state"/><xsl:text>}</xsl:text>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/state ')]">
  <xsl:text>{\plain\s0\f4\fs24\cf3 </xsl:text>
  <xsl:value-of select="name()"/><xsl:text>: </xsl:text><xsl:value-of select="@name"/><xsl:text>=</xsl:text><xsl:value-of select="@value"/>
  <xsl:text>}</xsl:text>  
</xsl:template>

  <xsl:template match="*[contains(@class,' topic/tm ')]">
    <xsl:text>{\plain\s0\f4\fs24 </xsl:text><xsl:apply-templates/>
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
      <xsl:text>\par \plain\s0\f4\fs24\cb3\b</xsl:text>
      <xsl:call-template name="getStringRTF">
        <xsl:with-param name="stringName" select="'Required cleanup'" />
      </xsl:call-template>
      <xsl:call-template name="getStringRTF">
        <xsl:with-param name="stringName" select="'ColonSymbol'" />
      </xsl:call-template>
      <xsl:text></xsl:text>
      <xsl:text>\pard \plain\s0\f4\fs24\cb3</xsl:text>
      <xsl:if
        test="ancestor::*[contains(@class,' topic/table ') or contains(@class,' topic/simpletable ')]">
        \intbl
      </xsl:if>
      <xsl:apply-templates />
      <xsl:text>\par \plain\s0\f2\fs24</xsl:text>
    </xsl:if>
  </xsl:template>

  <!-- Add for "New <data> element (#9)" in DITA 1.1 -->
  <xsl:template match="*[contains(@class,' topic/data ')]" />

  <!-- Add for "Support foreign content vocabularies such as 
    MathML and SVG with <unknown> (#35) " in DITA 1.1 -->
  <xsl:template match="*[contains(@class,' topic/foreign ') or contains(@class,' topic/unknown ')]" />

</xsl:stylesheet>
