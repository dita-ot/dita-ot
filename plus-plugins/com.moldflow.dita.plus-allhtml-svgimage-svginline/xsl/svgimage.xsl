<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">

    <xsl:param name="plus-svgimage-format"/>

    <!-- Match <image> tag when it points to an SVG document. -->
    <!-- To do: how to handle external images? -->
    <xsl:template match="*[contains(@class, ' topic/image ')][substring(@href, string-length(@href)-3) = '.svg']">
        <xsl:choose>
            <xsl:when test="$plus-svgimage-format = 'svginline'">
                <!-- Insert the SVG source directly into the result. 
                     Assumes that the browser knows how to render inline SVG. -->
                <xsl:choose>
                    <xsl:when test="@placement='break'">
                        <div>
                            <xsl:copy-of select="document(@href, /)"/>
                        </div>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:copy-of select="document(@href, /)"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <xsl:next-match>
                  <xsl:fallback>
                    <xsl:message terminate="no">
                        <xsl:text>plus-allhtml-svgimage-svginline: unable to fall back in XSLT 1.0.</xsl:text>
                    </xsl:message>
                  </xsl:fallback>
                </xsl:next-match>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
