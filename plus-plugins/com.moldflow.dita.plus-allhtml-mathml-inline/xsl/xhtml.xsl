<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
  xmlns:mathml-inline="http://www.moldflow.com/namespace/2007/dita/mathml-inline"
  exclude-result-prefixes="mathml-inline">

    <xsl:param name="plus-mathml-format" select="'inline'"/>

    <xsl:template match="*[contains(@class, ' math-d/mathph ')]">
        <xsl:choose>
          <xsl:when test="$plus-mathml-format = 'inline'">
            <xsl:apply-templates mode="mathml-inline:copy"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:next-match>
              <xsl:fallback>
                <xsl:message terminate="no">
                  <xsl:text>mathml-inline: cannot fall back in XSLT 1.0.</xsl:text>
                </xsl:message>
              </xsl:fallback>
            </xsl:next-match>
          </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' math-d/math ')]">
        <xsl:choose>
          <xsl:when test="$plus-mathml-format = 'inline'">
            <div class="mathml-block">
                <xsl:apply-templates mode="mathml-inline:copy"/>
            </div>
          </xsl:when>
          <xsl:otherwise>
            <xsl:next-match>
              <xsl:fallback>
                <xsl:message terminate="no">
                  <xsl:text>mathml-inline: cannot fall back in XSLT 1.0.</xsl:text>
                </xsl:message>
              </xsl:fallback>
            </xsl:next-match>
          </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- Copy MathML namespace elements and attributes directly. -->
    <xsl:template match="mml:*" xmlns:mml="http://www.w3.org/1998/Math/MathML" mode="mathml-inline:copy">
        <xsl:copy>
            <xsl:apply-templates select="node() | @*" mode="mathml-inline:copy"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="mml:*/@*" xmlns:mml="http://www.w3.org/1998/Math/MathML" mode="mathml-inline:copy">
        <xsl:copy/>
    </xsl:template>

    <xsl:template match="mml:*/text()" xmlns:mml="http://www.w3.org/1998/Math/MathML" mode="mathml-inline:copy">
        <!-- Normalize space to work around a JEuclid 2.9 bug. -->
        <xsl:value-of select="normalize-space()"/>
    </xsl:template>

</xsl:stylesheet>
