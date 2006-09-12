<?xml version="1.0"?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
  Sourceforge.net. See the accompanying license.txt file for 
  applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2004, 2006 All Rights Reserved. -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:output method="xml"/>

<xsl:template match="*[contains(@class, ' ut-d/imagemap ')]">
  <!-- Only use the image -->
  <block>
    <xsl:choose>
      <xsl:when test="*[contains(@class, ' ut-d/imagemap ')]/*[contains(@class,' topic/image ')]/*[contains(@class,' topic/alt ')]">
        <xsl:apply-templates select="*[contains(@class, ' ut-d/imagemap ')]/*[contains(@class,' topic/image ')]/*[contains(@class,' topic/alt ')]"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates select="*[contains(@class, ' ut-d/imagemap ')]/*[contains(@class,' topic/image ')]/@alt"/>
      </xsl:otherwise>
    </xsl:choose>
  </block>
</xsl:template>

<xsl:template match="*[contains(@class, ' ut-d/area ')]"/>
<xsl:template match="*[contains(@class, ' ut-d/coords ')]"/>
<xsl:template match="*[contains(@class, ' ut-d/shape ')]"/>

</xsl:stylesheet>
