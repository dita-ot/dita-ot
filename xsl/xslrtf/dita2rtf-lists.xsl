<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file for 
     applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2005, 2006 All Rights Reserved. -->
<xsl:stylesheet
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0" xmlns:random="org.dita.dost.util.RandomUtils" exclude-result-prefixes="random">
	
<!-- single-part lists -->

<xsl:template match="*[contains(@class,' topic/ul ')]">
<xsl:call-template name="gen-id"/><xsl:apply-templates/><xsl:if test="not(ancestor::*[contains(@class,' topic/li ')])">\par\pard\li360\fi-180</xsl:if>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/li ')]">
<xsl:call-template name="gen-id"/><xsl:call-template name="block-li"/>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/ol ')]">
<xsl:call-template name="gen-id"/><xsl:apply-templates/><xsl:if test="not(ancestor::*[contains(@class,' topic/li ')])">\par\pard\li360\fi-180</xsl:if>    
</xsl:template>


<!-- definition list -->

<xsl:template match="*[contains(@class,' topic/dl ')]">
<xsl:call-template name="gen-id"/><xsl:call-template name="block-lq"/>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/dt ')]">
<xsl:call-template name="gen-id"/><xsl:call-template name="inline-em"/>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/dd ')]">
<xsl:call-template name="gen-id"/><xsl:call-template name="block-p"/>
</xsl:template>

<!-- parameter list -->

<xsl:template match="parml"> <!-- not found -->
  <xsl:call-template name="block-lq"/>
</xsl:template>

<xsl:template match="plentry/synph">  <!-- plentry not found -->
  <xsl:call-template name="inline-em"/>
</xsl:template>

<xsl:template match="plentry/li">  <!-- plentry not found -->
  <xsl:call-template name="block-lq"/>
</xsl:template>
	
<!-- block-list -->
<xsl:template name="block-list">
<xsl:param name="depth">0</xsl:param>
<xsl:variable name="li-num" select="720 + ($depth * 360)"/>
\par \pard\li<xsl:value-of select="$li-num"/>\fi-360{\*\pn\pnlvlblt\pnf1\pnindent180{\pntxtb\'b7}}\plain\f2\fs24
<xsl:apply-templates/>
\pard\li360\fi-180 \par
</xsl:template>

<xsl:template name="block-ol">
<xsl:param name="depth">0</xsl:param>
<xsl:variable name="li-num" select="720 + ($depth * 360)"/>
\par \pard\li<xsl:value-of select="$li-num"/>\fi-360{\*\pn\pnlvlbody\pndec\pnstart1\pnf1\pnindent180}\plain\f2\fs24
<xsl:apply-templates/>
\pard\li360\fi-180 \par 
</xsl:template>

<xsl:template name="block-li">
<xsl:variable name="depth"><xsl:value-of select="count(ancestor::*[contains(@class,' topic/li ')])"/></xsl:variable>
<xsl:variable name="li-num" select="420 + ($depth * 420)"/>
<xsl:variable name="listnum" select="count(preceding::*[contains(@class,' topic/ol ') or contains(@class,' topic/ul ')][not(ancestor::*[contains(@class,' topic/li ')])]) + 1"/>
\par\pard\plain \qj \fi-420\li<xsl:value-of select="$li-num"/><xsl:if test="ancestor::*[contains(@class,' topic/table ') or contains(@class,' topic/simpletable ')]">\intbl</xsl:if>\jclisttab\tx<xsl:value-of select="$li-num"/>\ls<xsl:value-of select="$listnum"/><xsl:if test="$depth &gt; 0">\ilvl<xsl:value-of select="$depth"/></xsl:if><xsl:text> </xsl:text><xsl:if test="@importance='optional'"><xsl:call-template name="getStringRTF"><xsl:with-param name="stringName" select="'Optional'"/></xsl:call-template><xsl:call-template name="getStringRTF"><xsl:with-param name="stringName" select="'ColonSymbol'"/></xsl:call-template><xsl:text> </xsl:text></xsl:if><xsl:if test="@importance='required'"><xsl:call-template name="getStringRTF"><xsl:with-param name="stringName" select="'Required'"/></xsl:call-template><xsl:call-template name="getStringRTF"><xsl:with-param name="stringName" select="'ColonSymbol'"/></xsl:call-template><xsl:text> </xsl:text></xsl:if><xsl:apply-templates/></xsl:template>

<xsl:template name="gen-list-table">
{\*\listtables <xsl:apply-templates select="descendant::*[contains(@class,' topic/ol ') or contains(@class,' topic/ul ')][1]" mode="gen-list-table"/>}
</xsl:template>
	
<xsl:template match="*[contains(@class,' topic/ol ')]" mode="gen-list-table">
<xsl:variable name="templateid" select="random:getRandomNum()"/>
<xsl:variable name="listid" select="random:getRandomNum()"/>
<xsl:variable name="listnum" select="count(preceding::*[contains(@class,' topic/ol ') or contains(@class,' topic/ul ')][not(ancestor::*[contains(@class,' topic/li ')])]) + 1"/>
{\list\listtemplateid<xsl:value-of select="$templateid"/>\listhybrid{\listlevel\levelnfc0\levelnfcn0\leveljc0\leveljcn0\levelfollow0\levelstartat1\levelspace0
\levelindent0{\leveltext\'02\'00.;}{\levelnumbers\'01;}\fi-420\li420\jclisttab\tx420\lin420 }{\listlevel\levelnfc4\levelnfcn4\leveljc0\leveljcn0\levelfollow0\levelstartat1\levelspace0\levelindent0{\leveltext\'02\'01);}{\levelnumbers\'01;}\fi-420\li840\jclisttab\tx840\lin840 }{\listlevel\levelnfc2\levelnfcn2\leveljc2\leveljcn2\levelfollow0\levelstartat1\levelspace0\levelindent0{\leveltext\'02\'02.;}{\levelnumbers\'01;}\fi-420\li1260
\jclisttab\tx1260\lin1260 }{\listlevel\levelnfc0\levelnfcn0\leveljc0\leveljcn0\levelfollow0\levelstartat1\levelspace0\levelindent0{\leveltext\'02\'03.;}{\levelnumbers\'01;}\fi-420\li1680\jclisttab\tx1680\lin1680 }{\listlevel
\levelnfc4\levelnfcn4\leveljc0\leveljcn0\levelfollow0\levelstartat1\levelspace0\levelindent0{\leveltext\'02\'04);}{\levelnumbers\'01;}\fi-420\li2100\jclisttab\tx2100\lin2100 }{\listlevel\levelnfc2\levelnfcn2\leveljc2\leveljcn2
\levelfollow0\levelstartat1\levelspace0\levelindent0{\leveltext\'02\'05.;}{\levelnumbers\'01;}\fi-420\li2520\jclisttab\tx2520\lin2520 }{\listlevel\levelnfc0\levelnfcn0\leveljc0\leveljcn0\levelfollow0\levelstartat1\levelspace0
\levelindent0{\leveltext\'02\'06.;}{\levelnumbers\'01;}\fi-420\li2940\jclisttab\tx2940\lin2940 }{\listlevel\levelnfc4\levelnfcn4\leveljc0\leveljcn0\levelfollow0\levelstartat1\levelspace0\levelindent0{\leveltext\'02\'07);}{\levelnumbers\'01;}\fi-420\li3360\jclisttab\tx3360\lin3360 }{\listlevel\levelnfc2\levelnfcn2\leveljc2\leveljcn2\levelfollow0\levelstartat1\levelspace0\levelindent0{\leveltext\'02\'08.;}{\levelnumbers\'01;}\fi-420\li3780\jclisttab\tx3780\lin3780 }{\listname ;}\listid<xsl:value-of select="$listid"/>}
<xsl:choose>
<!--xsl:when test="descendant::*[contains(@class,' topic/ol ')]">
<xsl:apply-templates select="descendant::*[contains(@class,' topic/ol ')][1]" mode="gen-list-table"/>
</xsl:when-->
<xsl:when test="following::*[contains(@class,' topic/ol ') or contains(@class,' topic/ul ')]">
<xsl:apply-templates select="following::*[contains(@class,' topic/ol ') or contains(@class,' topic/ul ')][1]" mode="gen-list-table"/>
</xsl:when>
<xsl:otherwise>}{\*\listoverridetable</xsl:otherwise>
</xsl:choose>
{\listoverride\listid<xsl:value-of select="$listid"/>\listoverridecount0\ls<xsl:value-of select="$listnum"/>}
</xsl:template>
	
<xsl:template match="*[contains(@class,' topic/ul ')]" mode="gen-list-table">
<xsl:variable name="templateid" select="random:getRandomNum()"/>
<xsl:variable name="listid" select="random:getRandomNum()"/>
<xsl:variable name="listnum" select="count(preceding::*[contains(@class,' topic/ol ') or contains(@class,' topic/ul ')][not(ancestor::*[contains(@class,' topic/li ')])]) + 1"/>
{\list\listtemplateid<xsl:value-of select="$templateid"/>\listhybrid{\listlevel\levelnfc23\levelnfcn23\leveljc0\leveljcn0\levelfollow0
\levelstartat1\levelspace0\levelindent0{\leveltext\'01{\uc1\u-3988 ?};}{\levelnumbers;}\f10\fbias0 \fi-420\li420\jclisttab\tx420\lin420 }{\listlevel\levelnfc23\levelnfcn23\leveljc0\leveljcn0\levelfollow0\levelstartat1\levelspace0
\levelindent0{\leveltext\'01{\uc1\u-3986 ?};}{\levelnumbers;}\f10\fbias0 \fi-420\li840\jclisttab\tx840\lin840 }{\listlevel\levelnfc23\levelnfcn23\leveljc0\leveljcn0\levelfollow0\levelstartat1\levelspace0\levelindent0{\leveltext\'01{\uc1\u-3979 ?};}{\levelnumbers;}\f10\fbias0 \fi-420\li1260\jclisttab\tx1260\lin1260 }{\listlevel\levelnfc23\levelnfcn23\leveljc0\leveljcn0\levelfollow0\levelstartat1\levelspace0\levelindent0{\leveltext\'01{\uc1\u-3988 ?};}{\levelnumbers;}\f10\fbias0 \fi-420\li1680\jclisttab\tx1680\lin1680 }{\listlevel\levelnfc23\levelnfcn23\leveljc0\leveljcn0\levelfollow0\levelstartat1\levelspace0\levelindent0{\leveltext\'01{\uc1\u-3986 ?};}{\levelnumbers;}\f10\fbias0 \fi-420\li2100\jclisttab\tx2100\lin2100 }{\listlevel\levelnfc23\levelnfcn23\leveljc0\leveljcn0\levelfollow0\levelstartat1\levelspace0\levelindent0{\leveltext\'01{\uc1\u-3979 ?};}{\levelnumbers;}\f10\fbias0 \fi-420\li2520\jclisttab\tx2520\lin2520 }{\listlevel\levelnfc23\levelnfcn23\leveljc0\leveljcn0\levelfollow0\levelstartat1\levelspace0\levelindent0{\leveltext\'01{\uc1\u-3988 ?};}{\levelnumbers;}\f10\fbias0 \fi-420\li2940\jclisttab\tx2940\lin2940 }{\listlevel\levelnfc23\levelnfcn23\leveljc0\leveljcn0\levelfollow0\levelstartat1\levelspace0\levelindent0{\leveltext\'01{\uc1\u-3986 ?};}{\levelnumbers;}\f10\fbias0 \fi-420\li3360\jclisttab\tx3360\lin3360 }{\listlevel\levelnfc23\levelnfcn23\leveljc0\leveljcn0\levelfollow0\levelstartat1\levelspace0\levelindent0{\leveltext\'01{\uc1\u-3979 ?};}{\levelnumbers;}\f10\fbias0 \fi-420\li3780\jclisttab\tx3780\lin3780 }{\listname ;}\listid<xsl:value-of select="$listid"/>}
<xsl:choose>
<xsl:when test="following::*[contains(@class,' topic/ol ') or contains(@class,' topic/ul ')]">
<xsl:apply-templates select="following::*[contains(@class,' topic/ol ') or contains(@class,' topic/ul ')][1]" mode="gen-list-table"/>
</xsl:when>
<xsl:otherwise>}{\*\listoverridetable</xsl:otherwise></xsl:choose>
{\listoverride\listid<xsl:value-of select="$listid"/>\listoverridecount0\ls<xsl:value-of select="$listnum"/>}
</xsl:template>
	
</xsl:stylesheet>