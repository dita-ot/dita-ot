<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    
    <!-- Inline v. block: These items are inline iff all of their child elements are inline. -->
    <xsl:template match="*[contains(@class, ' task/info ')
        or contains(@class, ' task/stepresult ')
        or contains(@class, ' task/stepxmp ')
        or contains(@class, ' task/tutorialinfo ')]" mode="is-block">
        <xsl:variable name="childblock">
            <xsl:apply-templates select="*" mode="is-block"/>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="contains($childblock, 'y')">
                <xsl:text>y</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:text>n</xsl:text>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    
    <!-- "choicetable" always has two columns. -->
    <xsl:template match="*[contains(@class, ' task/choicetable ')]" mode="count-columns">
        2
    </xsl:template>
    
</xsl:stylesheet>
