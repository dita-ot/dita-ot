<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
  xmlns:svgobject="http://www.moldflow.com/namespace/2008/dita/svgobject"
  exclude-result-prefixes="svgobject">

    <xsl:template match="@svgobject:target"/>

    <!-- Fix up width and height of external object references. -->
    <xsl:template match="object/@svgobject:width | xhtml:object/@svgobject:width" xmlns:xhtml="http://www.w3.org/1999/xhtml">
      <xsl:attribute name="width">
        <xsl:value-of select="document(../@data, /)/svg:svg[1]/@width" xmlns:svg="http://www.w3.org/2000/svg"/>
      </xsl:attribute>
    </xsl:template>

    <xsl:template match="object/@svgobject:height | xhtml:object/@svgobject:height" xmlns:xhtml="http://www.w3.org/1999/xhtml">
      <xsl:attribute name="height">
        <xsl:value-of select="document(../@data, /)/svg:svg[1]/@height" xmlns:svg="http://www.w3.org/2000/svg"/>
      </xsl:attribute>
    </xsl:template>

    <xsl:template match="@svgobject:baseline-shift">
      <xsl:if test="document(string(.), /)">
        <xsl:attribute name="style">
          <xsl:text>vertical-align: </xsl:text>
          <xsl:value-of select="0 - document(string(.), /)"/>
          <xsl:text>px;</xsl:text>
        </xsl:attribute>
      </xsl:if>
    </xsl:template>

    <xsl:template match="img/@svgobject:usemap | xhtml:img/@svgobject:usemap" xmlns:xhtml="http://www.w3.org/1999/xhtml">
      <xsl:if test="document(string(.), /)/map/*">
        <xsl:attribute name="usemap">
          <xsl:value-of select="concat('#', generate-id(document(string(.), /)/map))"/>
        </xsl:attribute>
        <xsl:attribute name="border">0</xsl:attribute>
      </xsl:if>
    </xsl:template>

    <xsl:template match="processing-instruction('plus-svgobject-raster-imagemap')">
      <xsl:if test="document(string(.), /)/map/*">
        <map name="{generate-id(document(string(.), /)/map)}">
          <xsl:apply-templates select="document(string(.), /)/map/*" mode="copy-map"/>
        </map>
      </xsl:if>
    </xsl:template>

    <xsl:template match="*" mode="copy-map">
      <xsl:copy>
        <xsl:apply-templates select="@* | node()"/>
      </xsl:copy>
    </xsl:template>

    <xsl:template match="*[namespace-uri() = ''] | *[namespace-uri() = 'http://www.w3.org/1999/xhtml']" mode="copy-map">
      <xsl:element name="{local-name()}">
        <xsl:apply-templates select="@* | node()"/>
      </xsl:element>
    </xsl:template>

    <xsl:template match="text() | processing-instruction() | @*" mode="copy-map">
      <xsl:copy/>
    </xsl:template>

</xsl:stylesheet>
