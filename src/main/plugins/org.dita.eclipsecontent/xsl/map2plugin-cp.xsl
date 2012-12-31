<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file for 
     applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2005 All Rights Reserved. -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  
  <xsl:import href="plugin:org.dita.base:xsl/common/output-message.xsl"/>
  
  <xsl:param name="version">1.0</xsl:param>
  <xsl:param name="provider">DITA</xsl:param>
  <xsl:param name="TOCROOT">toc</xsl:param>
  
  <!-- Define the error message prefix identifier -->
  <xsl:variable name="msgprefix">DOTX</xsl:variable>
  
  <xsl:template match="*[contains(@class, ' map/map ')]">
    <xsl:element name="plugin">
      <xsl:attribute name="name">
        <xsl:choose>
          <xsl:when test="*[contains(@class, ' topic/title ')]">
            <xsl:value-of select="*[contains(@class, ' topic/title ')]"/>
          </xsl:when>
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
            <xsl:call-template name="output-message">
              <xsl:with-param name="msgnum">050</xsl:with-param>
              <xsl:with-param name="msgsev">W</xsl:with-param>
             </xsl:call-template>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:attribute>
      <xsl:attribute name="version">
        <xsl:value-of select="$version"/>
      </xsl:attribute>
      <xsl:attribute name="provider-name">
        <xsl:value-of select="$provider"/>
      </xsl:attribute>
	  <xsl:element name="requires">
		<xsl:element name="import">
			<xsl:attribute name="plugin">
				<xsl:text>org.dita.dost.contentProducer</xsl:text>
			</xsl:attribute>
		</xsl:element>
	  </xsl:element>
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
      <xsl:element name="extension">
		<xsl:attribute name="point">
			<xsl:text>org.eclipse.help.contentProducer</xsl:text>
		</xsl:attribute>
		<xsl:element name="contentProducer">
			<xsl:attribute name="producer">
				<xsl:text>org.dita.dost.ContentProducer</xsl:text>
			</xsl:attribute>
		</xsl:element>
	  </xsl:element>
    </xsl:element>
  </xsl:template>
</xsl:stylesheet>
