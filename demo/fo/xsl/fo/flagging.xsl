<?xml version='1.0' encoding="UTF-8"?>

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

<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:exsl="http://exslt.org/common"
    xmlns:exslf="http://exslt.org/functions"
    xmlns:opentopic-func="http://www.idiominc.com/opentopic/exsl/function"
    xmlns:fo="http://www.w3.org/1999/XSL/Format"
    extension-element-prefixes="exsl"
    exclude-result-prefixes="exsl opentopic-func exslf">

    <xsl:template match="*[@audience or @product or @platform or @rev or @otherprops]//text()[1]">
        <xsl:variable name="ancestor" select="ancestor::*[@audience or @product or @platform or @rev or @otherprops][1]"/>
        <xsl:for-each select="$ancestor/@*">
            <xsl:if test="name() = 'audience' or name() = 'product' or name() = 'platform' or name() = 'rev' or name() = 'otherprops'">
                <xsl:call-template name="applyFlags">
                    <xsl:with-param name="name" select="name()"/>
                    <xsl:with-param name="value" select="normalize-space(.)"/>
                </xsl:call-template>
            </xsl:if>
        </xsl:for-each>
        <xsl:copy-of select="."/>
    </xsl:template>

    <xsl:template name="applyFlags">
        <xsl:param name="name"/>
        <xsl:param name="value"/>
        <xsl:choose>
            <xsl:when test="$value = ''"/>
            <xsl:when test="contains($value,' ')">
                <xsl:variable name="firstValue" select="substring-before($value,' ')"/>
                <xsl:if test="opentopic-func:checkIfFlaged($name,$firstValue)">
                    <xsl:apply-templates select="$flagsParams//prop[@att = $name][@val = $firstValue]" mode="process-flag"/>
                </xsl:if>
                <xsl:call-template name="applyFlags">
                    <xsl:with-param name="name" select="$name"/>
                    <xsl:with-param name="value" select="substring-after($value,' ')"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:if test="opentopic-func:checkIfFlaged($name,$value)">
                    <xsl:apply-templates select="$flagsParams//prop[@att = $name][@val = $value]" mode="process-flag"/>
                </xsl:if>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="prop" mode="process-flag">
        <fo:inline>
            <xsl:value-of select="@img"/> <xsl:value-of select="@alt"/>
        </fo:inline>
    </xsl:template>

    <exslf:function name="opentopic-func:checkIfFlaged">
        <xsl:param name="name"/>
        <xsl:param name="value"/>
        <xsl:choose>
            <xsl:when test="$flagsParams//prop[@att = $name][@val = $value]">
                <exslf:result select="generate-id($flagsParams//prop[@att = $name][@val = $value])"/>
            </xsl:when>
            <xsl:otherwise>
                <exslf:result select="''"/>
            </xsl:otherwise>
        </xsl:choose>
    </exslf:function>
    
    <xsl:function version="2.0" name="opentopic-func:checkIfFlaged">
        <xsl:param name="name"/>
        <xsl:param name="value"/>
        <xsl:choose>
            <xsl:when test="$flagsParams//prop[@att = $name][@val = $value]">
                <xsl:value-of select="generate-id($flagsParams//prop[@att = $name][@val = $value])"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="''"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>



</xsl:stylesheet>