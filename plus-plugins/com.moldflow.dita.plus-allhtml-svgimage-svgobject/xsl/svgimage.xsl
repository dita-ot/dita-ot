<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
  xmlns:svgobject="http://www.moldflow.com/namespace/2008/dita/svgobject"
  exclude-result-prefixes="svgobject">

    <xsl:param name="plus-svgimage-format"/>

    <!-- Match <image> tag when it points to an SVG document. -->
    <!-- To do: how to handle external images? -->
    <xsl:template match="*[contains(@class, ' topic/image ')][substring(@href, string-length(@href)-3) = '.svg']">
        <xsl:choose>
            <xsl:when test="$plus-svgimage-format = 'svgobject'">
                <xsl:if test="@placement='break'">
                    <xsl:element name="br">
                        <xsl:attribute name="clear">none</xsl:attribute>
                    </xsl:element>
                </xsl:if>

                <xsl:apply-templates select="." mode="svgobject:generate-reference">
                  <xsl:with-param name="content" select="document(@href, /)"/>
                  <xsl:with-param name="alt" select="*[contains(@class, ' topic/alt ')]"/>
                </xsl:apply-templates>

                <xsl:if test="@placement='break'">
                    <xsl:element name="br">
                        <xsl:attribute name="clear">none</xsl:attribute>
                    </xsl:element>
                </xsl:if>
            </xsl:when>
            <xsl:otherwise>
                <xsl:next-match>
                  <xsl:fallback>
                    <xsl:message terminate="no">
                        <xsl:text>plus-allhtml-svgimage: unable to fall back in XSLT 1.0.</xsl:text>
                    </xsl:message>
                  </xsl:fallback>
                </xsl:next-match>
                <xsl:apply-imports/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
