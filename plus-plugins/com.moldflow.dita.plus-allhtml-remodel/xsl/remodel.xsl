<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
    xmlns:remodel="http://www.moldflow.com/namespace/2008/dita/remodel" exclude-result-prefixes="remodel">

    <!-- Collation: turn the intermediate HTML and to-remodel pieces into straight HTML -->
    <xsl:template match="remodel:*">
        <xsl:apply-templates/>
    </xsl:template>

<!-- Example: re-order so that child topics follow the body, then endnotes/footnotes, then related links.
    <xsl:template match="remodel:content">
        <xsl:apply-templates select="node()">
            <xsl:sort
                select="1 * number(boolean(self::remodel:child-topic)) + 3 * number(boolean(self::remodel:related-links)) + 2 * number(boolean(self::remodel:gen-endnotes))"/>
            <xsl:sort select="position()"/>
        </xsl:apply-templates>
    </xsl:template>
-->
    
</xsl:stylesheet>
