<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
    xmlns:svg="http://www.w3.org/2000/svg" xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:f = "http://www.moldflow.com/namespace/2008/svg/transform"
    xmlns:math="http://exslt.org/math"
    xmlns:syntaxdiagram2svg="http://www.moldflow.com/namespace/2008/syntaxdiagram2svg"
    exclude-result-prefixes="svg xlink"
    extension-element-prefixes="math f xs syntaxdiagram2svg">
    
    <!-- Fragment references.  Link to the defining fragment. -->
    <xsl:template match="svg:a[@syntaxdiagram2svg:element='fragref']">
        <xsl:param name="current-transform" as="xs:float+" tunnel="yes"/>
        <xsl:param name="current-href" as="xs:string" tunnel="yes" select="''"/>
        <area href="{$current-href}" shape="rect">
            <xsl:attribute name="coords" separator=",">
                <xsl:call-template name="transform-point">
                    <xsl:with-param name="T" as="xs:float+" select="$current-transform"/>
                    <xsl:with-param name="V" as="xs:float+" select="(0, 0 - number(@syntaxdiagram2svg:heightAbove))"/>
                </xsl:call-template>
                <xsl:call-template name="transform-point">
                    <xsl:with-param name="T" as="xs:float+" select="$current-transform"/>
                    <xsl:with-param name="V" as="xs:float+" select="(number(@syntaxdiagram2svg:width), number(@syntaxdiagram2svg:heightBelow))"/>
                </xsl:call-template>
            </xsl:attribute>
        </area>
    </xsl:template>

    <!-- Note callouts.  Link to the matching footnote. -->
    <xsl:template match="svg:a[@syntaxdiagram2svg:dispatch='note']">
        <xsl:param name="current-transform" as="xs:float+" tunnel="yes"/>
        <xsl:param name="current-href" as="xs:string" tunnel="yes" select="''"/>
        <area href="{$current-href}" shape="rect">
            <xsl:attribute name="coords" separator=",">
                <xsl:call-template name="transform-point">
                    <xsl:with-param name="T" as="xs:float+" select="$current-transform"/>
                    <xsl:with-param name="V" as="xs:float+" select="(0, 0 - number(@syntaxdiagram2svg:heightAbove))"/>
                </xsl:call-template>
                <xsl:call-template name="transform-point">
                    <xsl:with-param name="T" as="xs:float+" select="$current-transform"/>
                    <xsl:with-param name="V" as="xs:float+" select="(number(@syntaxdiagram2svg:width), number(@syntaxdiagram2svg:heightBelow))"/>
                </xsl:call-template>
            </xsl:attribute>
        </area>
    </xsl:template>
    
<!--    <xsl:template match="svg:rect" >
        <xsl:param name="current-transform" as="xs:float+" tunnel="yes"/>
        <xsl:param name="current-href" as="xs:string" tunnel="yes" select="''"/>
        <xsl:if test="$current-href">
            <area href="{$current-href}" shape="poly">
                <xsl:attribute name="coords" separator=",">
                    <xsl:call-template name="transform-point">
                        <xsl:with-param name="T" as="xs:float+" select="$current-transform"/>
                        <xsl:with-param name="V" as="xs:float+" select="(number(@x), number(@y))"/>
                    </xsl:call-template>
                    <xsl:call-template name="transform-point">
                        <xsl:with-param name="T" as="xs:float+" select="$current-transform"/>
                        <xsl:with-param name="V" as="xs:float+" select="(number(@x)+number(@width), number(@y))"/>
                    </xsl:call-template>
                    <xsl:call-template name="transform-point">
                        <xsl:with-param name="T" as="xs:float+" select="$current-transform"/>
                        <xsl:with-param name="V" as="xs:float+" select="(number(@x)+number(@width), number(@y)+number(@height))"/>
                    </xsl:call-template>
                    <xsl:call-template name="transform-point">
                        <xsl:with-param name="T" as="xs:float+" select="$current-transform"/>
                        <xsl:with-param name="V" as="xs:float+" select="(number(@x), number(@y)+number(@height))"/>
                    </xsl:call-template>
                    <xsl:call-template name="transform-point">
                        <xsl:with-param name="T" as="xs:float+" select="$current-transform"/>
                        <xsl:with-param name="V" as="xs:float+" select="(number(@x), number(@y))"/>
                    </xsl:call-template>
                </xsl:attribute>
            </area>
        </xsl:if>
    </xsl:template>
-->    

    
</xsl:stylesheet>
