<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file for 
     applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2006 All Rights Reserved. -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">

  <!-- single-part lists -->
  <!-- copied from dita2rtf-lists.xsl -->

  <xsl:template match="*[contains(@class,' topic/ul ')]">
    <xsl:call-template name="gen-id"/>
    <xsl:apply-templates/>
    <xsl:if test="not(ancestor::*[contains(@class,' topic/li ')])">\par\pard\li360\fi-180</xsl:if>
  </xsl:template>

  <xsl:template match="*[contains(@class,' topic/li ')]">
    <xsl:call-template name="gen-id"/>
    <xsl:call-template name="block-li"/>
  </xsl:template>

  <xsl:template match="*[contains(@class,' topic/ol ')]">
    <xsl:call-template name="gen-id"/>
    <xsl:apply-templates/>
    <xsl:if test="not(ancestor::*[contains(@class,' topic/li ')])">\par\pard\li360\fi-180</xsl:if>    
  </xsl:template>


  <xsl:template match="steps">
    <p />
    <xsl:for-each select="step">
      <xsl:number count="step" level="any" format="1" />) <xsl:apply-templates/><br />
    </xsl:for-each>
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

  <!-- Here starts the original code -->

  <!-- <xsl:template match="*[contains(@class,' task/choicetable ')]" name="topic.task.choicetable">
  <xsl:variable name="thiswidth-twips">
      <xsl:value-of select="$table-row-width div 2"/>
    </xsl:variable>
<xsl:call-template name="gen-id"/><xsl:text>\par </xsl:text>
  <xsl:choose>
    <xsl:when test="not(./*[contains(@class,' task/chhead ')])">
     <xsl:text>\trowd \trgaph108\trleft-108\trbrdrt\brdrs\brdrw10 \trhdr </xsl:text>
<xsl:text>\trbrdrl\brdrs\brdrw10 \trbrdrb\brdrs\brdrw10 \trbrdrr\brdrs\brdrw10 
\trbrdrh\brdrs\brdrw10 \trbrdrv\brdrs\brdrw10 
\trftsWidth1\trautofit1\trpaddl108\trpaddr108\trpaddfl3\trpaddfr3</xsl:text>
<xsl:text>\clvertalt\clbrdrt\brdrs\brdrw10 \clbrdrl\brdrs\brdrw10 \clbrdrb\brdrs\brdrw10 \clbrdrr\brdrs\brdrw10 \clftsWidth3\clwWidth</xsl:text><xsl:value-of select="round($thiswidth-twips)"/>
    <xsl:text>\cellx </xsl:text><xsl:text>\clvertalt\clbrdrt\brdrs\brdrw10 \clbrdrl\brdrs\brdrw10 \clbrdrb\brdrs\brdrw10 \clbrdrr\brdrs\brdrw10 \clftsWidth3\clwWidth</xsl:text><xsl:value-of select="round($thiswidth-twips)"/>
    <xsl:text>\cellx </xsl:text>
<xsl:text>\plain \s7\f4\fs24\b \qc</xsl:text>
\li0\fi0\ri0\nowidctlpar\intbl\aspalpha\aspnum\faauto\adjustright\rin0\lin0
<xsl:text>{</xsl:text><xsl:call-template name="getStringRTF">
      <xsl:with-param name="stringName" select="'Option'"/>
    </xsl:call-template><xsl:text>\cell </xsl:text><xsl:call-template name="getStringRTF">
      <xsl:with-param name="stringName" select="'Description'"/>
    </xsl:call-template><xsl:text>\cell}\row</xsl:text>
    </xsl:when>
  </xsl:choose>
  <xsl:apply-templates/>
  <xsl:text>\pard \qj \li0\ri0\nowidctlpar\aspalpha\aspnum\faauto\adjustright\rin0\lin0\itap0 {
\par }</xsl:text>
</xsl:template> -->

</xsl:stylesheet>