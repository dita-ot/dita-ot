<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <xsl:template match="*[contains(@class, ' topic/related-links ')]">
        <xsl:choose>
            <xsl:when
                test="count(descendant::*[contains(@class, ' topic/link ')][contains(concat(',', $config-related-links, ','), @role)]) &gt; 0">
                <related-links>
                    <xsl:apply-templates
                        select="descendant::*[contains(@class, ' topic/link ')][contains(concat(',', $config-related-links, ','), @role)]"
                    />
                </related-links>
            </xsl:when>
            <xsl:otherwise> </xsl:otherwise>
        </xsl:choose>

    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/link ')]">
        <link>
            <linktext>
                <xsl:call-template name="create-xref">
                    <xsl:with-param name="linktext"
                        select="*[contains(@class, ' topic/linktext ')]"/>
                </xsl:call-template>
            </linktext>
            <xsl:if test="string-length(*[contains(@class, ' topic/desc ')]) &gt; 0">
                <xsl:apply-templates select="desc" mode="replace-element"/>

            </xsl:if>
        </link>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/linktext ')]" mode="replace-element">
        <xsl:apply-templates select="." mode="process-children"/>
    </xsl:template>
</xsl:stylesheet>
