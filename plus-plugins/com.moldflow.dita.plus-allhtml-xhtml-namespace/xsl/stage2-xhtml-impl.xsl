<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <xsl:param name="XHTML-NAMESPACE" select="'http://www.w3.org/1999/xhtml'"/>

    <!-- Elements without a namespace should be renamespaced to the XHTML namespace. -->
    <xsl:template match="*[namespace-uri() = ''] | *[namespace-uri() = 'http://www.w3.org/1999/xhtml']">
        <xsl:element name="{local-name()}" namespace="{$XHTML-NAMESPACE}">
            <xsl:apply-templates select="node() | @*"/>
        </xsl:element>
    </xsl:template>

</xsl:stylesheet>
