<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
   
    <!-- Choose non-breaking versions of characters for sensitive elements. -->
    <xsl:template match="*[contains(@class, ' pr-d/codeph ')]//text()">
        <!-- space into &#xA0; non-breaking space.
            hyphen into &#x2011; non-breaking hyphen. -->
        <xsl:value-of select="translate(.,' -','&#xA0;&#x2011;')"/>
    </xsl:template>
    
    <xsl:template match="*[contains(@class, ' pr-d/codeph ')]//text()" mode="is-block">
        <xsl:text>n</xsl:text>
    </xsl:template>
    
</xsl:stylesheet>
