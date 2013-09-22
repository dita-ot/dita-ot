<?xml version='1.0'?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:fo="http://www.w3.org/1999/XSL/Format"
    xmlns:ot-placeholder="http://suite-sol.com/namespaces/ot-placeholder"
    exclude-result-prefixes="ot-placeholder" version="2.0">

    <xsl:template match="ot-placeholder:glossarylist">

        <fo:page-sequence master-reference="glossary-sequence"
            xsl:use-attribute-sets="__force__page__count">
            <xsl:call-template name="insertGlossaryStaticContents"/>
            <fo:flow flow-name="xsl-region-body">
                <fo:marker marker-class-name="current-header">
                    <xsl:call-template name="insertVariable">
                        <xsl:with-param name="theVariableID" select="'Glossary'"/>
                    </xsl:call-template>
                </fo:marker>

                <fo:block xsl:use-attribute-sets="__glossary__label" id="{$id.glossary}">
                    <xsl:call-template name="insertVariable">
                        <xsl:with-param name="theVariableID" select="'Glossary'"/>
                    </xsl:call-template>
                </fo:block>

                <xsl:apply-templates/>

            </fo:flow>
        </fo:page-sequence>

    </xsl:template>

    <!-- Glossary Group Headings -->
    <xsl:template
        match="ot-placeholder:glossarylist//*[contains(@class, ' glossgroup/glossgroup ')]">
        <fo:block xsl:use-attribute-sets="__glossary__group-head">
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates select="*[contains(@class,' topic/title ')]"/>
        </fo:block>
    </xsl:template>
    
    <!-- Glossary entry -->
    <xsl:template
        match="ot-placeholder:glossarylist//*[contains(@class, ' glossentry/glossentry ')]">
        <fo:block>
            <xsl:call-template name="commonattributes"/>
            <fo:block>
                <xsl:attribute name="id">
                    <xsl:call-template name="generate-toc-id"/>
                </xsl:attribute>
                <fo:block xsl:use-attribute-sets="__glossary__term">
                    <xsl:apply-templates
                        select="*[contains(@class, ' glossentry/glossterm ')]/node()"/>
                    <xsl:if
                        test="count(descendant-or-self::*[contains(@class, ' glossentry/glossAcronym ')]) gt 0">
                        <xsl:text> (</xsl:text>
                        <xsl:apply-templates 
                            select="descendant-or-self::*[contains(@class, ' glossentry/glossAcronym ')][1]"/>
                        <xsl:text>)</xsl:text>
                    </xsl:if>
                </fo:block>
                <fo:block xsl:use-attribute-sets="__glossary__def">
                    <xsl:apply-templates
                        select="*[contains(@class, ' glossentry/glossdef ')]/node()"/>
                    <xsl:apply-templates select="*[contains(@class, ' glossentry/glossBody ')]"/>
                    <xsl:apply-templates select="*[contains(@class, ' topic/related-links ')]"/>
                </fo:block>
            </fo:block>
        </fo:block>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' glossentry/glossBody ')]">
        <xsl:variable name="synonyms"
            select="descendant-or-self::*[contains(@class, ' glossentry/glossSynonym ')]/node()"/>
        <xsl:variable name="acronyms"
            select="descendant-or-self::*[contains(@class, ' glossentry/glossAcronym ')]/node()"/>
        <xsl:variable name="abbrevs"
            select="descendant-or-self::*[contains(@class, ' glossentry/glossAbbreviation ')]/node()"/>
        <!-- Synonyms -->
        <xsl:if test="count($synonyms) gt 0">
            <fo:block xsl:use-attribute-sets="__glossary__synonyms">
                <fo:inline xsl:use-attribute-sets="__glossary__synonyms-label">
                    <xsl:call-template name="insertVariable">
                        <xsl:with-param name="theVariableID" select="'Synonyms'"/>
                    </xsl:call-template>
                </fo:inline>
                <xsl:for-each
                    select="descendant-or-self::*[contains(@class, ' glossentry/glossSynonym ')]">
                    <xsl:apply-templates select="."/>
                    <xsl:if test="position() lt last()">
                        <xsl:text>, </xsl:text>
                    </xsl:if>
                </xsl:for-each>
            </fo:block>
        </xsl:if>
        <!-- Acronyms -->
        <xsl:if test="count($acronyms) gt 0">
            <fo:block xsl:use-attribute-sets="__glossary__acronyms">
                <fo:inline xsl:use-attribute-sets="__glossary__acronyms-label">
                    <xsl:call-template name="insertVariable">
                        <xsl:with-param name="theVariableID" select="'Acronyms'"/>
                    </xsl:call-template>
                </fo:inline>
                <xsl:value-of select="string-join($acronyms, ', ')"/>
            </fo:block>
        </xsl:if>
        <!-- Abbrevs -->
        <xsl:if test="count($abbrevs) gt 0">
            <fo:block xsl:use-attribute-sets="__glossary__abbrevs">
                <fo:inline xsl:use-attribute-sets="__glossary__abbrevs-label">
                    <xsl:call-template name="insertVariable">
                        <xsl:with-param name="theVariableID" select="'Abbrevs'"/>
                    </xsl:call-template>
                </fo:inline>
                <xsl:for-each
                    select="descendant-or-self::*[contains(@class, ' glossentry/glossAbbreviation ')]">
                    <xsl:apply-templates select="."/>
                    <xsl:if test="position() lt last()">
                        <xsl:text>, </xsl:text>
                    </xsl:if>
                </xsl:for-each>
            </fo:block>
        </xsl:if>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' glossentry/glossSynonym ')]">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' glossentry/glossAbbreviation ')]">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' glossentry/glossAcronym ')]">
        <xsl:apply-templates/>
    </xsl:template>
</xsl:stylesheet>
