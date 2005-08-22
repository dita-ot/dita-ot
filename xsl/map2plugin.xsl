<?xml version="1.0" encoding="UTF-8"?>
<!-- (c) Copyright IBM Corp. 2005 All Rights Reserved. -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  
  <xsl:param name="version">1.0</xsl:param>
  <xsl:param name="provider">DITA</xsl:param>
  <xsl:param name="TOCROOT">toc</xsl:param>
  
  <xsl:template match="*[contains(@class, ' map/map ')]">
    <xsl:element name="plugin">
      <xsl:attribute name="name">
        <xsl:choose>
          <xsl:when test="@title">
            <xsl:value-of select="@title"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>Sample Title</xsl:text>
          </xsl:otherwise>
        </xsl:choose>        
      </xsl:attribute>
      <xsl:attribute name="id">
        <xsl:choose>
          <xsl:when test="@id">
            <xsl:value-of select="@id"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>org.sample.help.doc</xsl:text>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
      <xsl:attribute name="version">
        <xsl:value-of select="$version"/>
      </xsl:attribute>
      <xsl:attribute name="provider-name">
        <xsl:value-of select="$provider"/>
      </xsl:attribute>
      <xsl:element name="extension">
        <xsl:attribute name="point">
          <xsl:text>org.eclipse.help.toc</xsl:text>
        </xsl:attribute>
        <xsl:element name="toc">
          <xsl:attribute name="file">
            <xsl:value-of select="$TOCROOT"/>
            <xsl:text>.xml</xsl:text>
          </xsl:attribute>
          <xsl:attribute name="primary">
            <xsl:text>true</xsl:text>
          </xsl:attribute>
        </xsl:element>
      </xsl:element>
      
    </xsl:element>
  </xsl:template>
</xsl:stylesheet>
