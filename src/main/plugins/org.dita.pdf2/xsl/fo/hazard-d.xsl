<?xml version="1.0" encoding="UTF-8"?>
<!--
This file is part of the DITA Open Toolkit project. 
See the accompanying license.txt file for applicable licenses.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:dita-ot="http://dita-ot.sourceforge.net/ns/201007/dita-ot"
                version="2.0"
                exclude-result-prefixes="xs dita-ot">

  <xsl:template match="*[contains(@class, ' hazard-d/hazardstatement ')]">
    <xsl:variable name="number-cells" as="xs:integer" select="2"/>
    <xsl:apply-templates select="*[contains(@class,' ditaot-d/ditaval-startprop ')]" mode="outofline"/>
    <fo:table xsl:use-attribute-sets="hazardstatement">
      <xsl:call-template name="commonattributes"/>
      <xsl:call-template name="globalAtts"/>
      <xsl:call-template name="displayAtts"/>
      <fo:table-column xsl:use-attribute-sets="hazardstatement.image.column"/>
      <fo:table-column xsl:use-attribute-sets="hazardstatement.content.column"/>
      <fo:table-body>
        <fo:table-row>
          <fo:table-cell xsl:use-attribute-sets="hazardstatement.title">
            <fo:block>
              <xsl:call-template name="getVariable">
                <xsl:with-param name="id" select="if (exists(@type)) then dita-ot:capitalize(@type) else 'Caution'"/>
              </xsl:call-template>
            </fo:block>
          </fo:table-cell>
        </fo:table-row>
        <fo:table-row>
          <fo:table-cell xsl:use-attribute-sets="hazardstatement.image">
            <fo:block>
              <xsl:choose>
                <xsl:when test="exists(*[contains(@class, ' hazard-d/hazardsymbol ')])">
                  <xsl:apply-templates select="*[contains(@class, ' hazard-d/hazardsymbol ')]"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:variable name="image" as="xs:string">
                    <xsl:call-template name="getVariable">
                      <xsl:with-param name="id" select="'hazard.image.default'"/>
                    </xsl:call-template>
                  </xsl:variable>
                  <fo:external-graphic src="url('{concat($artworkPrefix, $image)}')"
                    xsl:use-attribute-sets="hazardsymbol"/>
                </xsl:otherwise>
              </xsl:choose>
            </fo:block>
          </fo:table-cell>
          <fo:table-cell  xsl:use-attribute-sets="hazardstatement.content">
            <xsl:apply-templates select="*[contains(@class, ' hazard-d/messagepanel ')]/*"/>
          </fo:table-cell>
        </fo:table-row>
      </fo:table-body>
    </fo:table>
    <xsl:apply-templates select="*[contains(@class,' ditaot-d/ditaval-endprop ')]" mode="outofline"/>
  </xsl:template>
    
  <xsl:template match="*[contains(@class, ' hazard-d/messagepanel ')]">
    <xsl:apply-templates/>
  </xsl:template>
  
  <xsl:template match="*[contains(@class, ' hazard-d/typeofhazard ')]">
    <xsl:call-template name="p"/>
  </xsl:template>

  <xsl:template match="*[contains(@class, ' hazard-d/consequence ')]">
    <xsl:call-template name="p"/>
  </xsl:template>
  
  <xsl:template match="*[contains(@class, ' hazard-d/howtoavoid ')]">
    <xsl:call-template name="p"/>
  </xsl:template>
    
</xsl:stylesheet>