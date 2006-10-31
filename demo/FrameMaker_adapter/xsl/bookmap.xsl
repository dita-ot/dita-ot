<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <xsl:template match="*[contains(@class, ' bookmap/bookmap ')]" mode="replace-tag">
        <map>
        <xsl:choose>
            <xsl:when test="$config-book = 'yes' and $config-chapter-grouping = 'one'">
                <xsl:processing-instruction name="Fm">
                    <xsl:text> document "body.fm" </xsl:text>
                </xsl:processing-instruction>
                <fm_topicgroup>
                    <xsl:apply-templates select="*[contains(@class, ' topic/title ')]"/>
                    <xsl:apply-templates select="*[contains(@class, ' bookmap/bookmeta ')]"/>
                    <xsl:apply-templates select="." mode="process-children"/>
                </fm_topicgroup>
            </xsl:when>
            <xsl:when test="$config-book = 'yes' and $config-chapter-grouping = 'type'">
                <xsl:processing-instruction name="Fm">
                    <xsl:text> document "title-page.fm" </xsl:text>
                </xsl:processing-instruction>
                <fm_topicgroup>
                    <xsl:apply-templates select="*[contains(@class, ' topic/title ')]"/>
                    <xsl:apply-templates select="*[contains(@class, ' bookmap/bookmeta ')]"/>
                </fm_topicgroup>
                <xsl:for-each select="*[contains(@class, ' bookmap/frontmatter ')]">
                    <xsl:call-template name="group-bookmap-sections-by-type">
                        <xsl:with-param name="class" select="'notices'"/>
                    </xsl:call-template>
                    <xsl:call-template name="group-bookmap-sections-by-type">
                        <xsl:with-param name="class" select="'dedication'"/>
                    </xsl:call-template>
                    <xsl:call-template name="group-bookmap-sections-by-type">
                        <xsl:with-param name="class" select="'colophon'"/>
                    </xsl:call-template>
                    <xsl:call-template name="group-bookmap-sections-by-type">
                        <xsl:with-param name="class" select="'bookabstract'"/>
                    </xsl:call-template>
                    <xsl:call-template name="group-bookmap-sections-by-type">
                        <xsl:with-param name="class" select="'draftintro'"/>
                    </xsl:call-template>
                    <xsl:call-template name="group-bookmap-sections-by-type">
                        <xsl:with-param name="class" select="'preface'"/>
                    </xsl:call-template>
                </xsl:for-each>
                <xsl:call-template name="group-bookmap-sections-by-type">
                    <xsl:with-param name="class" select="'chapter'"/>
                </xsl:call-template>
                <!-- To do: parts -->
                <xsl:call-template name="group-bookmap-sections-by-type">
                    <xsl:with-param name="class" select="'appendix'"/>
                </xsl:call-template>
                <xsl:for-each select="*[contains(@class, ' bookmap/backmatter ')]">
                    <xsl:call-template name="group-bookmap-sections-by-type">
                        <xsl:with-param name="class" select="'dedication'"/>
                    </xsl:call-template>
                    <xsl:call-template name="group-bookmap-sections-by-type">
                        <xsl:with-param name="class" select="'colophon'"/>
                    </xsl:call-template>
                    <xsl:call-template name="group-bookmap-sections-by-type">
                        <xsl:with-param name="class" select="'amendments'"/>
                    </xsl:call-template>
                </xsl:for-each>
            </xsl:when>
            <xsl:when test="$config-book = 'yes' and $config-chapter-grouping = 'each'">
                <fm_topicgroup>
                    <xsl:apply-templates select="*[contains(@class, ' topic/title ')]"/>
                    <xsl:apply-templates select="*[contains(@class, ' bookmap/bookmeta ')]"/>
                </fm_topicgroup>
                <xsl:apply-templates select="." mode="process-children"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates select="." mode="process-children"/>
            </xsl:otherwise>
        </xsl:choose>
        </map>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' bookmap/bookmap ')]" mode="process-children">
        <xsl:apply-templates
            select="*[contains(@class, ' topic/topic ') or contains(@class, ' map/topicref ')]"/>
    </xsl:template>

    <xsl:template name="group-bookmap-sections-by-type">
        <xsl:param name="class"/>
        <xsl:if
            test="*[contains(@class, ' topic/topic ')][contains(@refclass, concat(' bookmap/', $class, ' '))]">
            <xsl:processing-instruction name="Fm">
                <xsl:text>document "</xsl:text>
                <xsl:value-of select="$class"/>
                <xsl:text>.fm" </xsl:text>
            </xsl:processing-instruction>
            <fm_topicgroup>
                <xsl:for-each
                    select="*[contains(@class, ' topic/topic ')][contains(@refclass, concat(' bookmap/', $class, ' '))]">
                    <xsl:apply-templates select="."/>
                </xsl:for-each>
            </fm_topicgroup>
        </xsl:if>
    </xsl:template>

    <xsl:template
        match="*[contains(@class, ' topic/topic ')][
        contains(@refclass, ' bookmap/')]"
        mode="replace-tag" priority="0.5">
        <xsl:call-template name="topic-content"/>
    </xsl:template>

    <xsl:template
        match="*[contains(@class, ' bookmap/bookmap ')]/*[contains(@class, ' topic/title ')]"
        mode="replace-tag">
        <xsl:if test="$config-book = 'yes' and $config-chapter-grouping = 'each'">
            <xsl:processing-instruction name="Fm">
                <xsl:text> document "title-page.fm" </xsl:text>
            </xsl:processing-instruction>
        </xsl:if>
        <booktitle>
            <xsl:choose>
                <xsl:when test="*[contains(@class, ' bookmap/mainbooktitle ')]">
                    <xsl:apply-templates select="*[contains(@class, ' bookmap/booklibrary ')]"/>
                    <xsl:apply-templates select="*[contains(@class, ' bookmap/mainbooktitle ')]"/>
                    <xsl:apply-templates select="*[contains(@class, ' bookmap/booktitlealt ')]"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates select="." mode="process-children"/>
                </xsl:otherwise>
            </xsl:choose>
        </booktitle>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' bookmap/')]" mode="replace-tag" priority="0">
        <xsl:element name="{substring-before(substring-after(@class, ' bookmap/'), ' ')}">
            <xsl:apply-templates select="." mode="process-children"/>
        </xsl:element>
    </xsl:template>

</xsl:stylesheet>
