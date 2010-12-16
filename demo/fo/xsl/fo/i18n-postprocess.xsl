<?xml version="1.0" encoding="UTF-8"?>

<!-- 
Copyright © 2004-2006 by Idiom Technologies, Inc. All rights reserved. 
IDIOM is a registered trademark of Idiom Technologies, Inc. and WORLDSERVER
and WORLDSTART are trademarks of Idiom Technologies, Inc. All other 
trademarks are the property of their respective owners. 

IDIOM TECHNOLOGIES, INC. IS DELIVERING THE SOFTWARE "AS IS," WITH 
ABSOLUTELY NO WARRANTIES WHATSOEVER, WHETHER EXPRESS OR IMPLIED,  AND IDIOM
TECHNOLOGIES, INC. DISCLAIMS ALL WARRANTIES, EXPRESS OR IMPLIED, INCLUDING
BUT NOT LIMITED TO WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
PURPOSE AND WARRANTY OF NON-INFRINGEMENT. IDIOM TECHNOLOGIES, INC. SHALL NOT
BE LIABLE FOR INDIRECT, INCIDENTAL, SPECIAL, COVER, PUNITIVE, EXEMPLARY,
RELIANCE, OR CONSEQUENTIAL DAMAGES (INCLUDING BUT NOT LIMITED TO LOSS OF 
ANTICIPATED PROFIT), ARISING FROM ANY CAUSE UNDER OR RELATED TO  OR ARISING 
OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN IF IDIOM
TECHNOLOGIES, INC. HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES. 

Idiom Technologies, Inc. and its licensors shall not be liable for any
damages suffered by any person as a result of using and/or modifying the
Software or its derivatives. In no event shall Idiom Technologies, Inc.'s
liability for any damages hereunder exceed the amounts received by Idiom
Technologies, Inc. as a result of this transaction.

These terms and conditions supersede the terms and conditions in any
licensing agreement to the extent that such terms and conditions conflict
with those set forth herein.

This file is part of the DITA Open Toolkit project hosted on Sourceforge.net. 
See the accompanying license.txt file for applicable licenses.
-->

<!-- UPDATES: 20100524: SF Bug 2385466, disallow font-family="inherit" due to 
                        lack of support in renderers. -->

<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:opentopic-i18n="http://www.idiominc.com/opentopic/i18n"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
				xmlns:rx="http://www.renderx.com/XSL/Extensions"
                exclude-result-prefixes="opentopic-i18n rx">

    <xsl:variable name="debug-enabled" select="true()"/>
    <xsl:variable name="warn-enabled" select="true()"/>

    <xsl:variable name="font-mappings" select="document('cfg:fo/font-mappings.xml')/font-mappings"/>

	<xsl:template match="fo:bookmark | fo:bookmark-label" priority="+10">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="fo:bookmark//opentopic-i18n:text-fragment" priority="+10">
		<xsl:value-of select="."/>
	</xsl:template>

    <xsl:template match="*[@font-family][not(@font-family='inherit')]" priority="+1">
        <xsl:variable name="currFontFam" select="@font-family"/>
        <xsl:variable name="realFontName">
			<xsl:choose>
				<xsl:when test="$font-mappings/font-table/logical-font[@name=$currFontFam]">
					<xsl:value-of select="$currFontFam"/>
				</xsl:when>
				<xsl:otherwise>
					<!--Try search this name within font aliases-->
					<xsl:variable name="aliasValue" select="$font-mappings/font-table/aliases/alias[@name=$currFontFam]/."/>
					<xsl:if test="$warn-enabled and not($aliasValue)">[WARN] Font definition not found for the logical name or alias '<xsl:value-of select="$currFontFam"/>'!</xsl:if>
					<xsl:value-of select="$aliasValue"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
        <xsl:variable name="phys-font" select="$font-mappings/font-table/logical-font[@name=normalize-space($realFontName)]/physical-font[@char-set='default']"/>
        <xsl:variable name="physical-font-family">
			<xsl:variable
                name="font"
                select="$phys-font/font-face"/>
            <xsl:choose>
                <xsl:when test="$font">
                    <xsl:value-of select="$font"/>
                </xsl:when>
                <xsl:otherwise>Helvetica</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:comment>
            currFontFam = <xsl:value-of select="$currFontFam"/>
            currFontFam = <xsl:copy-of select="$phys-font"/>
        </xsl:comment>
        <xsl:copy>
            <xsl:copy-of select="@*[not(name() = 'font-family')]"/>
            <xsl:attribute name="line-height-shift-adjustment">disregard-shifts</xsl:attribute>
            <xsl:attribute name="font-family"><xsl:value-of select="normalize-space($physical-font-family)"/></xsl:attribute>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="*[opentopic-i18n:text-fragment]">
        <xsl:copy>
            <xsl:apply-templates select="@*"/>
            <xsl:attribute name="line-height-shift-adjustment">disregard-shifts</xsl:attribute>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="opentopic-i18n:text-fragment">
        <xsl:variable name="fontFace" select="ancestor::*[@font-family][not(@font-family = 'inherit')][1]/@font-family"/>
        <xsl:variable name="charSet" select="@char-set"/>

        <xsl:variable name="realFontName">
			<xsl:choose>
				<xsl:when test="$font-mappings/font-table/logical-font[@name=$fontFace]">
					<xsl:value-of select="$fontFace"/>
				</xsl:when>
				<xsl:otherwise>
					<!--Try search this name within font aliases-->
					<xsl:variable name="aliasValue" select="$font-mappings/font-table/aliases/alias[@name=$fontFace]/."/>
					<xsl:if test="$warn-enabled and not($aliasValue)">[WARN] Font definition not found for the logical name or alias '<xsl:value-of select="$fontFace"/>'!</xsl:if>
					<xsl:value-of select="$aliasValue"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>

		<xsl:variable name="phys-font" select="$font-mappings/font-table/logical-font[@name=normalize-space($realFontName)]/physical-font[@char-set=$charSet]"/>

        <xsl:variable name="font-style" select="$phys-font/font-style"/>
        <xsl:variable name="baseline-shift" select="$phys-font/baseline-shift"/>
        <xsl:variable name="override-size" select="$phys-font/override-size"/>

        <xsl:variable name="physical-font-family">
			<xsl:variable
                name="font"
                select="$phys-font/font-face"/>
            <xsl:choose>
                <xsl:when test="$font">
                    <xsl:value-of select="$font"/>
                </xsl:when>
                <xsl:otherwise>Helvetica</xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <fo:inline line-height="100%">
            <xsl:attribute name="font-family"><xsl:value-of select="normalize-space($physical-font-family)"/></xsl:attribute>

            <xsl:if test="$font-style">
                <xsl:attribute name="font-style"><xsl:value-of select="normalize-space($font-style)"/></xsl:attribute>
            </xsl:if>

            <xsl:if test="$baseline-shift">
                <xsl:attribute name="baseline-shift"><xsl:value-of select="normalize-space($baseline-shift)"/></xsl:attribute>
            </xsl:if>

            <xsl:if test="$override-size">
                <xsl:attribute name="font-size"><xsl:value-of select="normalize-space($override-size)"/></xsl:attribute>
            </xsl:if>

            <xsl:apply-templates/>

        </fo:inline>

    </xsl:template>

    <xsl:template match="*" priority="-1">
       <xsl:copy>
          <xsl:apply-templates select="@* | node()" />
       </xsl:copy>
    </xsl:template>

    <xsl:template match="@*|comment()|processing-instruction()" priority="-1">
       <xsl:copy />
    </xsl:template>

    <!-- XEP doesn't like to see font-family="inherit", though it's allowed by the XSLFO spec. -->
    <xsl:template match="@font-family[. = 'inherit']"/>

</xsl:stylesheet>
