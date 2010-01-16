<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns:related-links="http://dita-ot.sourceforge.net/ns/200709/related-links"
    exclude-result-prefixes="related-links">

    <!-- Concepts have their own group. -->
    <xsl:template match="*[contains(@class, ' topic/link ')][@type='concept']" mode="related-links:get-group" name="related-links:group.concept">
        <xsl:text>concept</xsl:text>
    </xsl:template>
    
    <!-- Priority of concept group. -->
    <xsl:template match="*[contains(@class, ' topic/link ')][@type='concept']" mode="related-links:get-group-priority" name="related-links:group-priority.concept">
        <xsl:value-of select="3"/>
    </xsl:template>
    
    <!-- Wrapper for concept group: "Related concepts" in a <div>. -->
    <xsl:template match="*[contains(@class, ' topic/link ')][@type='concept']" mode="related-links:result-group" name="related-links:result.concept">
        <xsl:param name="links"/>
        <div class="relinfo relconcepts">
            <strong>
                <xsl:call-template name="getString">
                    <xsl:with-param name="stringName" select="'Related concepts'"/>
                </xsl:call-template>
            </strong><br/><xsl:value-of select="$newline"/>
            <xsl:copy-of select="$links"/>
        </div><xsl:value-of select="$newline"/>
    </xsl:template>
    
</xsl:stylesheet>
