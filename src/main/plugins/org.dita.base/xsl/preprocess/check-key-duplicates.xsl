<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:math="http://www.w3.org/2005/xpath-functions/math"
    xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl"
    xmlns:dita-ot="http://dita-ot.sourceforge.net/ns/201007/dita-ot"
    exclude-result-prefixes="#all"
    version="3.0">
    
    <xd:doc>
        <xd:desc>This stylesheet processes the expanded input ditamap as generated in the temp folder (with submap elements) and checks for duplicate keys in the same submap.</xd:desc>
    </xd:doc>    
    
    <!-- include error message template -->
    <xsl:import href="../common/dita-utilities.xsl"/>
    <xsl:include href="../common/output-message.xsl"/>
    
    <xsl:template match="/">
        <!-- construct keydef elements with attributes needed to determine duplicates -->
        <xsl:variable name="keydefs">
            <root>
                <xsl:for-each select="//*[contains(@class, ' mapgroup-d/keydef ')]">
                    <keydef keys="{@keys}" map="{ancestor::*[contains(@class, ' ditaot-d/submap ')][1]/generate-id(.)}"/>
                </xsl:for-each>
            </root>
        </xsl:variable>
        
        <xsl:for-each select="$keydefs/*/*">
            <xsl:variable name="keydef" select="."/>
            <xsl:for-each select="tokenize(@keys)">
                <!-- a key is deemed a duplicate if it is defined more than once in the same submap (beware: NOT in a nested submap, though - this is the same behavior as in gen-list target) -->
                <xsl:if test="$keydef/preceding-sibling::*[current() = tokenize(@keys) and @map eq $keydef/@map]">
                    <!-- same message is generated as in gen-list target -->
                    <xsl:call-template name="output-message">
                        <xsl:with-param name="ctx" select="$keydef" tunnel="yes"/>
                        <xsl:with-param name="id" select="'DOTJ045I'"/>
                        <xsl:with-param name="msgparams">%1=<xsl:value-of select="."/></xsl:with-param>
                    </xsl:call-template>
                </xsl:if>
            </xsl:for-each>
        </xsl:for-each>
        <xsl:copy>
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
    </xsl:template>
	 
    <xsl:template match="@* | node()">
        <xsl:copy>
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
    </xsl:template>
	 
</xsl:stylesheet>