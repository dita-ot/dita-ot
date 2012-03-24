<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<!-- This file is part of the DITA Open Toolkit project hosted on 
    Sourceforge.net. See the accompanying license.txt file for 
    applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<xsl:import href="../../xsl/map2docbook.xsl"/>

<!-- to do:

bookinfo
-->

<!-- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
   - BOOKMAP
   - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -->
<xsl:template match="*[contains(@class,' bookmap/bookmap ')]">
  <book>
    <xsl:copy-of select="@id"/>
    <!-- doesn't handle reltables or topicgroups -->
    <xsl:if test="@title">
      <title>
        <xsl:value-of select="@title"/>
      </title>
    </xsl:if>
    <!-- doesn't handle reltables or topicgroups -->
    <xsl:apply-templates select="*[contains(@class,' map/topicref ')]"/>
  </book>
</xsl:template>

<!-- doesn't handle bkinfo -->
<xsl:template match="*[contains(@class,' bookmap/bkinfo ')]"/>

<xsl:template match="*[contains(@class,' bookmap/preface ')]">
  <xsl:call-template name="topicref">
    <xsl:with-param name="element" select="'preface'"/>
  </xsl:call-template>
</xsl:template>

<xsl:template match="*[contains(@class,' bookmap/chapter ')]">
  <xsl:call-template name="topicref">
    <xsl:with-param name="element" select="'chapter'"/>
  </xsl:call-template>
</xsl:template>

<xsl:template match="*[contains(@class,' bookmap/part ')]">
  <xsl:call-template name="topicref">
    <xsl:with-param name="element" select="'part'"/>
  </xsl:call-template>
</xsl:template>

<xsl:template match="*[contains(@class,' bookmap/appendix ')]">
  <xsl:call-template name="topicref">
    <xsl:with-param name="element" select="'appendix'"/>
  </xsl:call-template>
</xsl:template>

</xsl:stylesheet>
