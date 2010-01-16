<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">

    <xsl:param name="ENCODING" select="'UTF-8'"/>

    <xsl:template match="/">
        <xsl:choose>
            <xsl:when test="number(system-property('xsl:version')) &gt; 1.0">
                <xsl:result-document method="html" encoding="{$ENCODING}" doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN" doctype-system="http://www.w3.org/TR/html4/loose.dtd">
                    <xsl:next-match/>
                </xsl:result-document>           
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates/>
            </xsl:otherwise>
        </xsl:choose>
   </xsl:template>

</xsl:stylesheet>
