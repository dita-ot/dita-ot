<?xml version="1.0"?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
  Sourceforge.net. See the accompanying license.txt file for 
  applicable licenses.-->
  <!--
    | (C) Copyright IBM Corporation 2006. All Rights Reserved.
    *-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:template match="/*[contains(@class,' eclipsemap/plugin ')][*[(contains(@class,'
    eclipsemap/primarytocref ') or contains(@class,' eclipsemap/tocref ')) and @format='ditamap']]">
    <mapcollection>
      <xsl:for-each select="*[contains(@class,' eclipsemap/primarytocref ') or contains(@class,' eclipsemap/tocref ')]">
        <xsl:if test="@format='ditamap' and not(@linking='none')">
          <xsl:apply-templates select="document(@href,/)/*[contains(@class, ' map/map ')]/*">
            <xsl:with-param name="pathFromMaplist">
              <xsl:call-template name="getRelativePath"><xsl:with-param name="filename" select="@href"/></xsl:call-template>
            </xsl:with-param>
          </xsl:apply-templates>
        </xsl:if>
      </xsl:for-each>
    </mapcollection>
  </xsl:template>

</xsl:stylesheet>
