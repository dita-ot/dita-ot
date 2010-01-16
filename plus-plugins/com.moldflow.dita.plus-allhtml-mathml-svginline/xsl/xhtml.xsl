<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
  xmlns:mathml-svg="http://www.moldflow.com/namespace/2007/dita/mathml-svginline"
  exclude-result-prefixes="mathml-svg">

    <xsl:param name="plus-mathml-format"/>

    <xsl:template match="*[contains(@class, ' math-d/mathph ')]">
        <xsl:choose>
          <xsl:when test="$plus-mathml-format = 'svginline'">
            <xsl:call-template name="mathml-svg:copy-mathml">
              <xsl:with-param name="baseline-shift" select="'yes'"/>
            </xsl:call-template>
          </xsl:when>
          <xsl:otherwise>
            <xsl:next-match>
              <xsl:fallback>
                <xsl:message terminate="no">
                  <xsl:text>mathml-svginline: cannot fall back in XSLT 1.0.</xsl:text>
                </xsl:message>
              </xsl:fallback>
            </xsl:next-match>
          </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' math-d/math ')]">
        <xsl:choose>
          <xsl:when test="$plus-mathml-format = 'svginline'">
            <div class="mathml-block">
              <xsl:call-template name="mathml-svg:copy-mathml">
                <xsl:with-param name="baseline-shift" select="'no'"/>
              </xsl:call-template>
            </div>
          </xsl:when>
          <xsl:otherwise>
            <xsl:next-match>
              <xsl:fallback>
                <xsl:message terminate="no">
                  <xsl:text>mathml-svginline: cannot fall back in XSLT 1.0.</xsl:text>
                </xsl:message>
              </xsl:fallback>
            </xsl:next-match>
          </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="mathml-svg:copy-mathml">
        <xsl:param name="baseline-shift" select="'no'"/>
        <xsl:if test="number(system-property('xsl:version')) &lt; 2">
            <xsl:message terminate="yes">
                <xsl:text>External MathML files require an XSLT 2.0 processor.</xsl:text>
            </xsl:message>
        </xsl:if>

        <xsl:variable name="external-svg-name" select="replace(document-uri(/),'^(.*/)?([^/]+?)(\.[^\.]+)?$','$2')"/>

        <xsl:result-document href="{$external-svg-name}_{generate-id(.)}_mathml.mml"
            doctype-public="-//W3C//DTD MathML 2.0//EN" doctype-system="http://www.w3.org/TR/MathML2/dtd/mathml2.dtd">
          <xsl:apply-templates mode="mathml-svg:copy"/>
        </xsl:result-document>

        <xsl:choose>
          <xsl:when test="$baseline-shift = 'yes'">
            <xsl:processing-instruction name="plus-allhtml-mathml-svginline-reunite-with-baseline-shift">
               <xsl:value-of select="concat($external-svg-name, '_', generate-id(.), '_mathml.svg')"/>
            </xsl:processing-instruction>
          </xsl:when>
          <xsl:otherwise>
            <xsl:processing-instruction name="plus-allhtml-mathml-svginline-reunite">
               <xsl:value-of select="concat($external-svg-name, '_', generate-id(.), '_mathml.svg')"/>
            </xsl:processing-instruction>
          </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- Copy MathML namespace elements and attributes directly. -->
    <xsl:template match="mml:*" xmlns:mml="http://www.w3.org/1998/Math/MathML" mode="mathml-svg:copy">
        <xsl:copy>
            <xsl:apply-templates select="node() | @*" mode="mathml-svg:copy"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="mml:*/@*" xmlns:mml="http://www.w3.org/1998/Math/MathML" mode="mathml-svg:copy">
        <xsl:copy/>
    </xsl:template>

    <xsl:template match="mml:*/text()" xmlns:mml="http://www.w3.org/1998/Math/MathML" mode="mathml-svg:copy">
        <xsl:value-of select="normalize-space()"/>
    </xsl:template>

    <!-- Invisible glyphs are not so invisible in some fonts.  See if this can come out some day. -->
    <xsl:template match="mml:mo[. = '&#x2061;']" xmlns:mml="http://www.w3.org/1998/Math/MathML" mode="mathml-svg:copy"/>
    <xsl:template match="mml:mo[. = '&#x2062;']" xmlns:mml="http://www.w3.org/1998/Math/MathML" mode="mathml-svg:copy"/>
    <xsl:template match="mml:mo[. = '&#x2063;']" xmlns:mml="http://www.w3.org/1998/Math/MathML" mode="mathml-svg:copy"/>

</xsl:stylesheet>
