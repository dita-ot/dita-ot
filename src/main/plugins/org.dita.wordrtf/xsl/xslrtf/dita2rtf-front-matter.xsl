<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of the DITA Open Toolkit project hosted on
     Sourceforge.net. See the accompanying license.txt file for
     applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2006 All Rights Reserved. -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">

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
{\header \pard\qc\plain\f1\fs20
[ <xsl:value-of select="$maptitle"/> ]
\par}

<!-- Page footer -->
{\footer \pard\qc\plain\f1\fs20
Page \chpgn
\par}

<!-- Front page -->
{\pard \pvmrg\phmrg\posxc\posyc \qc
\f1\fs72 <xsl:value-of select="$maptitle"/>\par}
{\pard \page \par}
  </xsl:template>

</xsl:stylesheet>