<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
    xmlns:bc="http://www.moldflow.com/namespace/2008/dita/plus-preprocess-breadcrumbs"
    exclude-result-prefixes="bc">

    <xsl:param name="BREADCRUMBS" select="'no'"/>
    
    <!-- Use maplink's generate-unordered-links hook to fire off breadcrumb generation. -->
    <xsl:template match="*[contains(@class, ' map/topicref ')][@href][not(@href='')][not(@linking='none')][not(@linking='sourceonly')]
        [ancestor::*[contains(@class, ' map/topicref ')][@href][not(@href='')]]
        [not(ancestor::*[contains(concat(' ', @chunk, ' '), ' to-content ')])]" mode="generate-unordered-links">
        <xsl:param name="pathBackToMapDirectory"/>
        <!-- If $BREADCRUMBS is yes, call breadcrumb start code. -->
        <xsl:if test="$BREADCRUMBS = 'yes'">
            <xsl:apply-templates select="." mode="bc:start">
                <xsl:with-param name="pathBackToMapDirectory" select="$pathBackToMapDirectory"/>
            </xsl:apply-templates>
        </xsl:if>
        <!-- Daisy-chain to other uses of generate-unordered-links mode. -->
        <xsl:next-match>
            <xsl:with-param name="pathBackToMapDirectory" select="$pathBackToMapDirectory"/>
            <xsl:fallback>
                <xsl:message terminate="no">
                    <xsl:text>plus-preprocess-breadcrumbs: Cannot use xsl:next-match in XSLT 1.0.</xsl:text>
                </xsl:message>
            </xsl:fallback>
        </xsl:next-match>
    </xsl:template>
    
    <xsl:template match="*[contains(@class, ' map/topicref ')]" mode="bc:start">
        <xsl:param name="pathBackToMapDirectory"/>
        <linkpool class="- topic/linkpool " role="ancestor">
            <xsl:apply-templates mode="link" 
                select="ancestor::*[contains(@class, ' map/topicref ')][@href][not(@href='')]">
                <xsl:with-param name="role">ancestor</xsl:with-param>
                <xsl:with-param name="pathBackToMapDirectory" 
                    select="$pathBackToMapDirectory"/>
            </xsl:apply-templates>
        </linkpool>
    </xsl:template>
    
</xsl:stylesheet>
