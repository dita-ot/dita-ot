<?xml version='1.0'?>

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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:fo="http://www.w3.org/1999/XSL/Format"
    version="2.0">

    <xsl:template match="*[contains(@class,' pr-d/codeph ')]">
        <fo:inline xsl:use-attribute-sets="codeph">
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates/>
        </fo:inline>
    </xsl:template>

    <xsl:template match="*[contains(@class,' pr-d/codeblock ')]">
        <xsl:call-template name="generateAttrLabel"/>
        <fo:block xsl:use-attribute-sets="codeblock">
            <xsl:call-template name="commonattributes"/>
            <xsl:call-template name="setFrame"/>
            <xsl:call-template name="setScale"/>
            <xsl:apply-templates/>
        </fo:block>
    </xsl:template>

    <xsl:template match="*[contains(@class,' pr-d/option ')]">
        <fo:inline xsl:use-attribute-sets="option">
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates/>
        </fo:inline>
    </xsl:template>

    <xsl:template match="*[contains(@class,' pr-d/var ')]">
        <fo:inline xsl:use-attribute-sets="var">
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates/>
        </fo:inline>
    </xsl:template>

    <xsl:template match="*[contains(@class,' pr-d/parmname ')]">
        <fo:inline xsl:use-attribute-sets="parmname">
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates/>
        </fo:inline>
    </xsl:template>

    <xsl:template match="*[contains(@class,' pr-d/synph ')]">
        <fo:inline xsl:use-attribute-sets="synph">
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates/>
        </fo:inline>
    </xsl:template>

    <xsl:template match="*[contains(@class,' pr-d/oper ')]">
        <fo:inline xsl:use-attribute-sets="oper">
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates/>
        </fo:inline>
    </xsl:template>

    <xsl:template match="*[contains(@class,' pr-d/delim ')]">
        <fo:inline xsl:use-attribute-sets="delim">
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates/>
        </fo:inline>
    </xsl:template>

    <xsl:template match="*[contains(@class,' pr-d/sep ')]">
        <fo:inline xsl:use-attribute-sets="sep">
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates/>
        </fo:inline>
    </xsl:template>

    <xsl:template match="*[contains(@class,' pr-d/apiname ')]">
        <fo:inline xsl:use-attribute-sets="apiname">
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates/>
        </fo:inline>
    </xsl:template>

    <xsl:template match="*[contains(@class,' pr-d/parml ')]">
        <xsl:call-template name="generateAttrLabel"/>
        <fo:block xsl:use-attribute-sets="parml">
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates/>
        </fo:block>
    </xsl:template>

    <xsl:template match="*[contains(@class,' pr-d/plentry ')]">
        <fo:block xsl:use-attribute-sets="plentry">
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates/>
        </fo:block>
    </xsl:template>

    <xsl:template match="*[contains(@class,' pr-d/pt ')]">
        <fo:block xsl:use-attribute-sets="pt">
            <xsl:call-template name="commonattributes"/>
            <xsl:choose>
                <xsl:when test="*"> <!-- tagged content - do not default to bold -->
                    <xsl:apply-templates/>
                </xsl:when>
                <xsl:otherwise>
                    <fo:inline xsl:use-attribute-sets="pt__content">
                        <xsl:apply-templates/>
                    </fo:inline> <!-- text only - bold it -->
                </xsl:otherwise>
            </xsl:choose>
        </fo:block>
    </xsl:template>

    <xsl:template match="*[contains(@class,' pr-d/pd ')]">
        <fo:block xsl:use-attribute-sets="pd">
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates/>
        </fo:block>
    </xsl:template>

    <xsl:template match="*[contains(@class,' pr-d/synblk ')]">
        <fo:inline xsl:use-attribute-sets="synblk">
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates/>
        </fo:inline>
    </xsl:template>

    <xsl:template match="*[contains(@class,' pr-d/synnoteref ')]">
        <fo:inline xsl:use-attribute-sets="synnoteref">
        <xsl:call-template name="commonattributes"/>
        [<xsl:value-of select="@refid"/>] <!--TODO: synnoteref-->
        </fo:inline>
    </xsl:template>

    <xsl:template match="*[contains(@class,' pr-d/synnote ')]">
        <fo:inline xsl:use-attribute-sets="synnote"> <!--TODO: synnote-->
            <xsl:call-template name="commonattributes"/>
            <xsl:choose>
                <xsl:when test="not(@id='')"> <!-- case of an explicit id -->
                    <xsl:value-of select="@id"/>
                </xsl:when>
                <xsl:when test="not(@callout='')"> <!-- case of an explicit callout (presume id for now) -->
                    <xsl:value-of select="@callout"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:text>*</xsl:text>
                </xsl:otherwise>
            </xsl:choose>
        </fo:inline>
    </xsl:template>

    <xsl:template match="*[contains(@class,' pr-d/syntaxdiagram ')]">
        <fo:block xsl:use-attribute-sets="syntaxdiagram"> <!--TODO: syntaxdiagram-->
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates/>
        </fo:block>
    </xsl:template>

    <xsl:template match="*[contains(@class,' pr-d/fragment ')]">
        <fo:block xsl:use-attribute-sets="fragment">
            <xsl:call-template name="commonattributes"/>
            <xsl:value-of select="*[contains(@class,' topic/title ')]"/>
            <xsl:text> </xsl:text>
            <xsl:apply-templates/>
        </fo:block>
    </xsl:template>

    <xsl:template match="*[contains(@class,' pr-d/syntaxdiagram ')]/*[contains(@class,' topic/title ')]">
        <fo:block xsl:use-attribute-sets="syntaxdiagram.title">
            <xsl:call-template name="commonattributes"/>
            <xsl:value-of select="."/>
        </fo:block>
    </xsl:template>

    <xsl:template match="*[contains(@class,' pr-d/kwd ')]">
        <fo:inline xsl:use-attribute-sets="kwd">
            <xsl:call-template name="commonattributes"/>
            <xsl:if test="parent::*[contains(@class,' pr-d/groupchoice ')]">
                <xsl:if test="count(preceding-sibling::*)!=0"> | </xsl:if>
            </xsl:if>
            <xsl:if test="@importance='optional'"> [</xsl:if>
            <xsl:choose>
                <xsl:when test="@importance='default'">
                    <fo:inline xsl:use-attribute-sets="kwd__default">
                        <xsl:value-of select="."/>
                    </fo:inline>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="."/>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:if test="@importance='optional'">] </xsl:if>
        </fo:inline>
    </xsl:template>

    <xsl:template match="*[contains(@class,' pr-d/fragref ')]">
        <fo:inline xsl:use-attribute-sets="fragref">     <!--TODO: fragref-->
            <xsl:call-template name="commonattributes"/>
            &lt;<xsl:value-of select="."/>&gt;
        </fo:inline>
    </xsl:template>

    <xsl:template match="*[contains(@class,' pr-d/fragment ')]/*[contains(@class,' topic/title ')]">
        <fo:block xsl:use-attribute-sets="fragment.title">
            <xsl:call-template name="commonattributes"/>
            <xsl:apply-templates/>
        </fo:block>
    </xsl:template>

    <xsl:template match="*[contains(@class,' pr-d/fragment ')]/*[contains(@class,' pr-d/groupcomp ')]|*[contains(@class,' pr-d/fragment ')]/*[contains(@class,' pr-d/groupchoice ')]|*[contains(@class,' pr-d/fragment ')]/*[contains(@class,' pr-d/groupseq ')]">
        <fo:block xsl:use-attribute-sets="fragment.group">
            <xsl:call-template name="commonattributes"/>
            <xsl:call-template name="makeGroup"/>
        </fo:block>
    </xsl:template>

    <xsl:template match="*[contains(@class,' pr-d/syntaxdiagram ')]/*[contains(@class,' pr-d/groupcomp ')]|*[contains(@class,' pr-d/syntaxdiagram ')]/*[contains(@class,' pr-d/groupseq ')]|*[contains(@class,' pr-d/syntaxdiagram ')]/*[contains(@class,' pr-d/groupchoice ')]">
        <fo:block xsl:use-attribute-sets="syntaxdiagram.group">
            <xsl:call-template name="commonattributes"/>
            <xsl:call-template name="makeGroup"/>
        </fo:block>
    </xsl:template>

    <xsl:template match="*[contains(@class,' pr-d/groupcomp ')]/*[contains(@class,' pr-d/groupcomp ')]">
        <fo:inline>
          <xsl:call-template name="commonattributes"/>
        </fo:inline>
        <xsl:call-template name="makeGroup"/>
    </xsl:template>

    <xsl:template match="*[contains(@class,' pr-d/groupchoice ')]/*[contains(@class,' pr-d/groupchoice ')]">
        <fo:inline>
            <xsl:call-template name="commonattributes"/>
        </fo:inline>
        <xsl:call-template name="makeGroup"/>
    </xsl:template>

    <xsl:template match="*[contains(@class,' pr-d/groupseq ')]/*[contains(@class,' pr-d/groupseq ')]">
        <fo:inline>
            <xsl:call-template name="commonattributes"/>
        </fo:inline>
        <xsl:call-template name="makeGroup"/>
    </xsl:template>

    <xsl:template match="*[contains(@class,' pr-d/groupchoice ')]/*[contains(@class,' pr-d/groupcomp ')]">
        <fo:inline>
            <xsl:call-template name="commonattributes"/>
        </fo:inline>
        <xsl:call-template name="makeGroup"/>
    </xsl:template>

    <xsl:template match="*[contains(@class,' pr-d/groupchoice ')]/*[contains(@class,' pr-d/groupseq ')]">
        <fo:inline>
            <xsl:call-template name="commonattributes"/>
        </fo:inline>
        <xsl:call-template name="makeGroup"/>
    </xsl:template>

    <xsl:template match="*[contains(@class,' pr-d/groupcomp ')]/*[contains(@class,' pr-d/groupchoice ')]">
        <fo:inline>
            <xsl:call-template name="commonattributes"/>
        </fo:inline>
        <xsl:call-template name="makeGroup"/>
    </xsl:template>

    <xsl:template match="*[contains(@class,' pr-d/groupcomp ')]/*[contains(@class,' pr-d/groupseq ')]">
        <fo:inline>
            <xsl:call-template name="commonattributes"/>
        </fo:inline>
        <xsl:call-template name="makeGroup"/>
    </xsl:template>

    <xsl:template match="*[contains(@class,' pr-d/groupseq ')]/*[contains(@class,' pr-d/groupchoice ')]">
        <fo:inline>
            <xsl:call-template name="commonattributes"/>
        </fo:inline>
        <xsl:call-template name="makeGroup"/>
    </xsl:template>

    <xsl:template match="*[contains(@class,' pr-d/groupseq ')]/*[contains(@class,' pr-d/groupcomp ')]">
        <fo:inline>
            <xsl:call-template name="commonattributes"/>
        </fo:inline>
        <xsl:call-template name="makeGroup"/>
    </xsl:template>

    <xsl:template name="makeGroup">
        <xsl:if test="parent::*[contains(@class,' pr-d/groupchoice ')]">
            <xsl:if test="count(preceding-sibling::*)!=0"> | </xsl:if>
        </xsl:if>
        <xsl:if test="@importance='optional'">[</xsl:if>
        <xsl:if test="name()='groupchoice'">{</xsl:if>
        <xsl:text> </xsl:text>
            <xsl:apply-templates/>
        <xsl:text> </xsl:text>
        <!-- repid processed here before -->
        <xsl:if test="name()='groupchoice'">}</xsl:if>
        <xsl:if test="@importance='optional'">]</xsl:if>
    </xsl:template>

</xsl:stylesheet>