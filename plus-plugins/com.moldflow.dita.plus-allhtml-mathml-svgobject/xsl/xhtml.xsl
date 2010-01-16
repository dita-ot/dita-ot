<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
  xmlns:mathml-svg="http://www.moldflow.com/namespace/2007/dita/mathml-svgobject"
  xmlns:svgobject="http://www.moldflow.com/namespace/2008/dita/svgobject"
  exclude-result-prefixes="mathml-svg svgobject">

    <xsl:param name="plus-mathml-format"/>

    <xsl:template match="*[contains(@class, ' math-d/mathph ')]">
        <xsl:choose>
          <xsl:when test="$plus-mathml-format = 'svgobject'">
            <span class="mathml-inline">
              <xsl:apply-templates select="." mode="svgobject:generate-reference">
                <xsl:with-param name="doctype-public" select="'-//W3C//DTD MathML 2.0//EN'"/>
                <xsl:with-param name="doctype-system" select="'http://www.w3.org/TR/MathML2/dtd/mathml2.dtd'"/>
                <xsl:with-param name="suffix" select="'.mml'"/>
                <xsl:with-param name="content">
                   <xsl:apply-templates mode="mathml-svg:copy"/>
                </xsl:with-param>
                <xsl:with-param name="baseline-shift" select="'yes'"/>
              </xsl:apply-templates>
            </span>
          </xsl:when>
          <xsl:otherwise>
            <xsl:next-match>
              <xsl:fallback>
                <xsl:message terminate="no">
                  <xsl:text>mathml-svgobject: cannot fall back in XSLT 1.0.</xsl:text>
                </xsl:message>
              </xsl:fallback>
            </xsl:next-match>
          </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' math-d/math ')]">
        <xsl:choose>
          <xsl:when test="$plus-mathml-format = 'svgobject'">
            <div class="mathml-block">
              <xsl:apply-templates select="." mode="svgobject:generate-reference">
                <xsl:with-param name="doctype-public" select="'-//W3C//DTD MathML 2.0//EN'"/>
                <xsl:with-param name="doctype-system" select="'http://www.w3.org/TR/MathML2/dtd/mathml2.dtd'"/>
                <xsl:with-param name="suffix" select="'.mml'"/>
                <xsl:with-param name="content">
                   <xsl:apply-templates mode="mathml-svg:copy"/>
                </xsl:with-param>
              </xsl:apply-templates>
            </div>
          </xsl:when>
          <xsl:otherwise>
            <xsl:next-match>
              <xsl:fallback>
                <xsl:message terminate="no">
                  <xsl:text>mathml-svgobject: cannot fall back in XSLT 1.0.</xsl:text>
                </xsl:message>
              </xsl:fallback>
            </xsl:next-match>
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
