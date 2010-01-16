<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
  xmlns:xhtml="http://www.w3.org/1999/xhtml">

    <xsl:param name="ENCODING" select="'UTF-8'"/>

    <xsl:template match="/">
        <xsl:choose>
            <xsl:when test="number(system-property('xsl:version')) &gt; 1.0">
                <xsl:result-document method="xhtml" encoding="{$ENCODING}" doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN" doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd" omit-xml-declaration="yes">
                    <xsl:next-match/>
                </xsl:result-document>           
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates/>
            </xsl:otherwise>
        </xsl:choose>
   </xsl:template>

</xsl:stylesheet>
