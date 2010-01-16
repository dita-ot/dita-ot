<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    
    <xsl:template match="*[contains(@class, ' map/topicref ')][@href]/*[contains(@class, ' map/topicmeta ')]/*[contains(@class, ' topic/resourceid ')][@appname = 'WindowsHelpId']">
        <xsl:value-of select="@id"/>
        <xsl:text>=</xsl:text>
        <xsl:value-of select="substring-before(ancestor::*[contains(@class, ' map/topicref ')]/@href, '.')"/>
        <xsl:text>
</xsl:text>
    </xsl:template>
    
</xsl:stylesheet>