<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    
    <!-- No-output templates, to be overridden by plugins. -->
    
    <xsl:template match="/">
        <xsl:apply-templates/>
    </xsl:template>
    
    <xsl:template match="*">
        <xsl:apply-templates select="@* | node()"/>
    </xsl:template>
    
    <xsl:template match="@* | text() | comment() | processing-instruction()"/>
    
</xsl:stylesheet>