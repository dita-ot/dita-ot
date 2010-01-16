<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    
    <xsl:template match="*[contains(@class, ' topic/topic ')]/*[contains(@class, ' topic/prolog ')]/*[contains(@class, ' topic/resourceid ')][@appname = 'WindowsHelpId']">
        <xsl:value-of select="@id"/>
        <xsl:text>=</xsl:text>
        <xsl:value-of select="$CURRENTDIR"/>
        <xsl:text>/</xsl:text>
        <xsl:value-of select="substring-before($CURRENTFILE, '.')"/><xsl:value-of select="$OUTEXT"/>
        
        <!-- To do: fragment within topic. -->
        <xsl:text>
</xsl:text>
    </xsl:template>
    
</xsl:stylesheet>