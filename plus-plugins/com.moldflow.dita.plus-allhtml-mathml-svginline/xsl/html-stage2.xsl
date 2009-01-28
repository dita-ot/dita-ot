<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">

    <!-- Reunite inline SVG from MathML. -->
    <xsl:template match="processing-instruction('plus-allhtml-mathml-svginline-reunite')">
        <xsl:apply-templates select="document(., /)/*"></xsl:apply-templates>
    </xsl:template>

    <xsl:template match="processing-instruction('plus-allhtml-mathml-svginline-reunite-with-baseline-shift')">
        <span>
          <xsl:if test="document(concat(., '.baseline'), /)">
            <xsl:attribute name="style">
              <xsl:text>vertical-align: </xsl:text>
              <xsl:value-of select="0 - document(concat(., '.baseline'), /)/*"/>
              <xsl:text>px;</xsl:text>
            </xsl:attribute>
          </xsl:if>
          <xsl:apply-templates select="document(., /)/*"></xsl:apply-templates>
        </span>
    </xsl:template>
    
</xsl:stylesheet>
