<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of the DITA Open Toolkit project hosted on
     Sourceforge.net. See the accompanying license.txt file for
     applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2006 All Rights Reserved. -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">

  <!-- Get rid of whitespace only nodes -->
  <xsl:strip-space elements="*"/>

  <xsl:template name="createFrontMatter">
    <!-- Set the title -->
    <xsl:variable name="maptitle">
      <xsl:choose>
        <xsl:when test="//*[contains(@class, ' map/map ')]/@title">
          <xsl:value-of select="//*[contains(@class, ' map/map ')]/@title"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="/descendant::*[contains(@class, ' topic/topic ')][1]/*[contains(@class, ' topic/title ')]"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>

    <!-- Page header  -->
    <xsl:text>{\header \pard\qc\plain\f1\fs20 </xsl:text>
    <xsl:value-of select="$maptitle"/>
    <xsl:text>\par}</xsl:text>

    <!-- Page footer -->
    <xsl:text>{\footer \pard\qc\plain\f1\fs20 Page \chpgn \par}</xsl:text>

    <!-- Front page -->
    <xsl:text>{\pard \pvmrg\phmrg\posxc\posyc \qc \widctlpar \f1\fs72</xsl:text>
    <xsl:value-of select="$maptitle"/>
    <xsl:text>\par}\page </xsl:text>
  </xsl:template>

</xsl:stylesheet>