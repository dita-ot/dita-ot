<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns:svg="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:treediagram2svg="http://www.moldflow.com/namespace/2008/treediagram2svg">
    
    <xsl:variable name="treediagram2svg:css-filename" select="'treediagram.css'"/>

    <xsl:template name="treediagram2svg:gen-user-scripts">
        <xsl:param name="JSPATH">js/treediagram/</xsl:param>
        <svg:script type="text/ecmascript" xlink:href="{$JSPATH}constants.js"/>
        <svg:script type="text/ecmascript" xlink:href="{$JSPATH}text.js"/>
        <svg:script type="text/ecmascript" xlink:href="{$JSPATH}boxed.js"/>
        <svg:script type="text/ecmascript" xlink:href="{$JSPATH}treenode.js"/>
        <svg:script type="text/ecmascript" xlink:href="{$JSPATH}diagram.js"/>
        <svg:script type="text/ecmascript" xlink:href="{$JSPATH}main.js"/>
    </xsl:template>
    
    <xsl:template name="treediagram2svg:gen-user-styles">
        <xsl:param name="CSSPATH"/>
        <xsl:processing-instruction name="xml-stylesheet">
            <xsl:text>type="text/css" href="</xsl:text>
            <xsl:value-of select="$CSSPATH"/>
            <xsl:value-of select="$treediagram2svg:css-filename"/>
            <xsl:text>"</xsl:text>
        </xsl:processing-instruction>
    </xsl:template>

    <xsl:template name="treediagram2svg:create-svg-document">
        <xsl:param name="CSSPATH"></xsl:param>
        <xsl:param name="JSPATH"></xsl:param>
        <xsl:call-template name="treediagram2svg:gen-user-styles">
            <xsl:with-param name="CSSPATH" select="$CSSPATH"/>
        </xsl:call-template>
        <svg class="treediagram" onload="treediagram_onloadSvgRoot(evt)" xmlns="http://www.w3.org/2000/svg">
            <xsl:call-template name="treediagram2svg:gen-user-scripts">
                <xsl:with-param name="JSPATH" select="$JSPATH"/>
            </xsl:call-template>
            <xsl:call-template name="treediagram2svg:root"/>
        </svg>
    </xsl:template>

    <xsl:template name="treediagram2svg:create-svg-element">
       <svg:svg class="treediagram" onload="treediagram_onloadSvgRoot(evt)">
         <xsl:call-template name="treediagram2svg:root"/>
       </svg:svg>
    </xsl:template>

    <xsl:template name="treediagram2svg:root">
        <svg:g class="diagram" treediagram2svg:dispatch="diagram">
            <xsl:apply-templates select="*[not(contains(@class,' topic/title '))][not(contains(@class,' topic/desc '))] |text()|comment()|processing-instruction()"/>
        </svg:g>
    </xsl:template>
    
    <xsl:template match="*[contains(@class, ' tree-d/node ')]">
        <svg:g class="treenode" treediagram2svg:dispatch="treenode">
            <xsl:apply-templates select="*[contains(@class, ' topic/title ')]" mode="treediagram2svg:treenode-title"></xsl:apply-templates>
            <xsl:apply-templates select="*[not(contains(@class,' topic/title '))][not(contains(@class,' topic/desc '))] |text()|comment()|processing-instruction()"/>
        </svg:g>
    </xsl:template>
    
    <xsl:template match="*[contains(@class, ' topic/title ')]" mode="treediagram2svg:treenode-title">
        <svg:g class="boxed" treediagram2svg:dispatch="boxed">
            <xsl:apply-templates select="node()" mode="treediagram2svg:box-contents"></xsl:apply-templates>
        </svg:g>
    </xsl:template>
    
    <xsl:template match="text()" mode="treediagram2svg:box-contents">
        <svg:g class="text" treediagram2svg:dispatch="text">
            <svg:text>
                <xsl:value-of select="."/>
            </svg:text>
        </svg:g>
    </xsl:template>
    
</xsl:stylesheet>
