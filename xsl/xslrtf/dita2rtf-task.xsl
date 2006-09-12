<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file for 
     applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2006 All Rights Reserved. -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<xsl:template match="*[contains(@class,' task/choicetable ')]" name="topic.task.choicetable">
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
</xsl:template>
</xsl:stylesheet>