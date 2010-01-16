<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
    xmlns:remodel="http://www.moldflow.com/namespace/2008/dita/remodel" exclude-result-prefixes="remodel">

    <!-- Entry point for collection of pieces to collate. -->
    <xsl:template name="chapterBody">
        <xsl:apply-templates select="." mode="remodel:collect"/>
    </xsl:template>

    <xsl:template match="/dita" mode="remodel:collect">
        <xsl:apply-templates select="*" mode="remodel:collect"/>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/topic ')]" mode="remodel:collect">
        <xsl:variable name="flagrules">
            <xsl:call-template name="getrules"/>
        </xsl:variable>
        <xsl:variable name="conflictexist">
            <xsl:call-template name="conflict-check">
                <xsl:with-param name="flagrules" select="$flagrules"/>
            </xsl:call-template>
        </xsl:variable>
        <body>
            <!-- Already put xml:lang on <html>; do not copy to body with commonattributes -->
            <xsl:call-template name="gen-style">
                <xsl:with-param name="conflictexist" select="$conflictexist"/>
                <xsl:with-param name="flagrules" select="$flagrules"/>
            </xsl:call-template>
            <xsl:call-template name="setidaname"/>
            <xsl:value-of select="$newline"/>
            <xsl:call-template name="start-flagit">
                <xsl:with-param name="flagrules" select="$flagrules"/>
            </xsl:call-template>
            <xsl:call-template name="start-revflag">
                <xsl:with-param name="flagrules" select="$flagrules"/>
            </xsl:call-template>
            <xsl:apply-templates select="." mode="remodel:gen-pieces"/>
            <!-- Include XHTML footer, if specified -->
            <xsl:call-template name="end-revflag">
                <xsl:with-param name="flagrules" select="$flagrules"/>
            </xsl:call-template>
            <xsl:call-template name="end-flagit">
                <xsl:with-param name="flagrules" select="$flagrules"/>
            </xsl:call-template>
        </body>
        <xsl:value-of select="$newline"/>
    </xsl:template>

    <xsl:template match="/*[contains(@class, ' topic/topic ')] | /dita/*[contains(@class, ' topic/topic ')]" mode="remodel:gen-pieces">
        <remodel:breadcrumbs>
            <xsl:call-template name="generateBreadcrumbs"/>
        </remodel:breadcrumbs>
        <remodel:gen-user-header>
            <xsl:call-template name="gen-user-header"/>
        </remodel:gen-user-header>
        <remodel:hdr>
            <xsl:call-template name="processHDR"/>
        </remodel:hdr>
        <remodel:gen-user-sidetoc>
            <xsl:call-template name="gen-user-sidetoc"/>
        </remodel:gen-user-sidetoc>
        <remodel:content>
            <xsl:apply-templates select="node()" mode="remodel:gen-pieces"/>
            <remodel:gen-endnotes>
                <xsl:call-template name="gen-endnotes"/>
            </remodel:gen-endnotes>
        </remodel:content>
        <remodel:gen-user-footer>
            <xsl:call-template name="gen-user-footer"/>
        </remodel:gen-user-footer>
        <remodel:processFTR>
            <xsl:call-template name="processFTR"/>
        </remodel:processFTR>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/topic ')]/*[contains(@class, ' topic/title ')]" mode="remodel:gen-pieces">
        <remodel:title>
            <xsl:apply-templates select="."/>
        </remodel:title>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/topic ')]/*[contains(@class, ' topic/titlealts ')]" mode="remodel:gen-pieces">
        <remodel:titlealts>
            <xsl:apply-templates select="."/>
        </remodel:titlealts>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/topic ')]/*[contains(@class, ' topic/shortdesc')]" mode="remodel:gen-pieces">
        <remodel:shortdesc>
            <xsl:apply-templates select="."/>
        </remodel:shortdesc>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/topic ')]/*[contains(@class, ' topic/abstract ')]" mode="remodel:gen-pieces">
        <remodel:abstract>
            <xsl:apply-templates select="."/>
        </remodel:abstract>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/topic ')]/*[contains(@class, ' topic/prolog ')]" mode="remodel:gen-pieces">
        <remodel:prolog>
            <xsl:apply-templates select="."/>
        </remodel:prolog>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/topic ')]/*[contains(@class, ' topic/body ')]" mode="remodel:gen-pieces">
        <remodel:body>
            <xsl:apply-templates select="."/>
        </remodel:body>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/topic ')]/*[contains(@class, ' topic/related-links ')]"
        mode="remodel:gen-pieces">
        <remodel:related-links>
            <xsl:apply-templates select="."/>
        </remodel:related-links>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/topic ')]/*[contains(@class, ' topic/topic ')]" mode="remodel:gen-pieces">
        <remodel:child-topic>
            <xsl:apply-templates select="."/>
        </remodel:child-topic>
    </xsl:template>

</xsl:stylesheet>
