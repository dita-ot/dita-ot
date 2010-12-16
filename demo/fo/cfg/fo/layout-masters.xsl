<?xml version="1.0"?>

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
    version="1.0">
    
    <xsl:template name="createLayoutMasters">
        <xsl:choose>
            <!-- Check whether the old style layout-masters.xml is in use, and if so, use it. -->
            <xsl:when test="document($layout-masters)/fo:layout-master-set
                and not(document($layout-masters)/processing-instruction()[name()='opentopic' and string()='do not use'])">
                <xsl:apply-templates select="document($layout-masters)/*" mode="layout-masters-processing"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="createDefaultLayoutMasters"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="createDefaultLayoutMasters">
        <fo:layout-master-set>

            <fo:simple-page-master master-name="front-matter-first"
                page-width="{$page-width}"
                page-height="{$page-height}">
                <fo:region-body margin-top="{$page-margin-top}"
                    margin-bottom="{$page-margin-bottom}"
                    margin-left="{$page-margin-left}"
                    margin-right="{$page-margin-right}"/>
            </fo:simple-page-master>

            <fo:simple-page-master master-name="front-matter-last"
                page-width="{$page-width}"
                page-height="{$page-height}">
                <fo:region-body margin-top="{$page-margin-top}"
                    margin-bottom="{$page-margin-bottom}"
                    margin-left="{$page-margin-left}"
                    margin-right="{$page-margin-right}"/>
                <fo:region-before extent="{$page-margin-top}"
                    display-align="before"
                    region-name="last-frontmatter-header"/>
                <fo:region-after extent="{$page-margin-bottom}"
                    display-align="after"
                    region-name="last-frontmatter-footer"/>
            </fo:simple-page-master>

            <fo:simple-page-master master-name="front-matter-even"
                page-width="{$page-width}"
                page-height="{$page-height}">
                <fo:region-body margin-top="{$page-margin-top}"
                    margin-bottom="{$page-margin-bottom}"
                    margin-left="{$page-margin-left}"
                    margin-right="{$page-margin-right}"/>
                <fo:region-before extent="{$page-margin-top}"
                    display-align="before"
                    region-name="even-frontmatter-header"/>
                <fo:region-after extent="{$page-margin-bottom}"
                    display-align="after"
                    region-name="even-frontmatter-footer"/>
            </fo:simple-page-master>

            <fo:simple-page-master master-name="front-matter-odd"
                page-width="{$page-width}"
                page-height="{$page-height}">
                <fo:region-body margin-top="{$page-margin-top}"
                    margin-bottom="{$page-margin-bottom}"
                    margin-left="{$page-margin-left}"
                    margin-right="{$page-margin-right}"/>
                <fo:region-before extent="{$page-margin-top}"
                    display-align="before"
                    region-name="odd-frontmatter-header"/>
                <fo:region-after extent="{$page-margin-bottom}"
                    display-align="after"
                    region-name="odd-frontmatter-footer"/>
            </fo:simple-page-master>

            <!--TOC simple masters-->
            <fo:simple-page-master
                master-name="toc-even"
                page-width="{$page-width}"
                page-height="{$page-height}">
                <fo:region-body
                    margin-top="{$page-margin-top}"
                    margin-bottom="{$page-margin-bottom}"
                    margin-left="{$page-margin-left}"
                    margin-right="{$page-margin-right}"/>
                <fo:region-before extent="{$page-margin-top}"
                    display-align="before"
                    region-name="even-toc-header"/>
                <fo:region-after extent="{$page-margin-bottom}"
                    display-align="after"
                    region-name="even-toc-footer"/>
            </fo:simple-page-master>

            <fo:simple-page-master
                master-name="toc-odd"
                page-width="{$page-width}"
                page-height="{$page-height}">
                <fo:region-body
                    margin-top="{$page-margin-top}"
                    margin-bottom="{$page-margin-bottom}"
                    margin-left="{$page-margin-left}"
                    margin-right="{$page-margin-right}"/>
                <fo:region-before extent="{$page-margin-top}"
                    display-align="before"
                    region-name="odd-toc-header"/>
                <fo:region-after extent="{$page-margin-bottom}"
                    display-align="after"
                    region-name="odd-toc-footer"/>
            </fo:simple-page-master>

            <fo:simple-page-master
                master-name="toc-last"
                page-width="{$page-width}"
                page-height="{$page-height}">
                <fo:region-body
                    margin-top="{$page-margin-top}"
                    margin-bottom="{$page-margin-bottom}"
                    margin-left="{$page-margin-left}"
                    margin-right="{$page-margin-right}"/>
                <fo:region-before extent="{$page-margin-top}"
                    display-align="before"
                    region-name="even-toc-header"/>
                <fo:region-after extent="{$page-margin-bottom}"
                    display-align="after"
                    region-name="even-toc-footer"/>
            </fo:simple-page-master>

            <fo:simple-page-master
                master-name="toc-first"
                page-width="{$page-width}"
                page-height="{$page-height}">
                <fo:region-body
                    margin-top="{$page-margin-top}"
                    margin-bottom="{$page-margin-bottom}"
                    margin-left="{$page-margin-left}"
                    margin-right="{$page-margin-right}"/>
                <fo:region-before extent="{$page-margin-top}"
                    display-align="before"
                    region-name="odd-toc-header"/>
                <fo:region-after extent="{$page-margin-bottom}"
                    display-align="after"
                    region-name="odd-toc-footer"/>
            </fo:simple-page-master>


            <!--BODY simple masters-->
            <fo:simple-page-master
                master-name="body-first"
                page-width="{$page-width}"
                page-height="{$page-height}">
                <fo:region-body
                    margin-top="{$page-margin-top}"
                    margin-bottom="{$page-margin-bottom}"
                    margin-left="{$page-margin-left}"
                    margin-right="{$page-margin-right}"/>
                <fo:region-before extent="{$page-margin-top}"
                    display-align="before"
                    region-name="first-body-header"/>
                <fo:region-after extent="{$page-margin-bottom}"
                    display-align="after"
                    region-name="first-body-footer"/>
            </fo:simple-page-master>

            <fo:simple-page-master
                master-name="body-even"
                page-width="{$page-width}"
                page-height="{$page-height}">
                <fo:region-body
                    margin-top="{$page-margin-top}"
                    margin-bottom="{$page-margin-bottom}"
                    margin-left="{$page-margin-left}"
                    margin-right="{$page-margin-right}"/>
                <fo:region-before extent="{$page-margin-top}"
                    display-align="before"
                    region-name="even-body-header"/>
                <fo:region-after extent="{$page-margin-bottom}"
                    display-align="after"
                    region-name="even-body-footer"/>
            </fo:simple-page-master>

            <fo:simple-page-master
                master-name="body-odd"
                page-width="{$page-width}"
                page-height="{$page-height}">
                <fo:region-body
                    margin-top="{$page-margin-top}"
                    margin-bottom="{$page-margin-bottom}"
                    margin-left="{$page-margin-left}"
                    margin-right="{$page-margin-right}"/>
                <fo:region-before extent="{$page-margin-top}"
                    display-align="before"
                    region-name="odd-body-header"/>
                <fo:region-after extent="{$page-margin-bottom}"
                    display-align="after"
                    region-name="odd-body-footer"/>
            </fo:simple-page-master>

            <fo:simple-page-master
                master-name="body-last"
                page-width="{$page-width}"
                page-height="{$page-height}">
                <fo:region-body
                    margin-top="{$page-margin-top}"
                    margin-bottom="{$page-margin-bottom}"
                    margin-left="{$page-margin-left}"
                    margin-right="{$page-margin-right}"/>
                <fo:region-before extent="{$page-margin-top}"
                    display-align="before"
                    region-name="last-body-header"/>
                <fo:region-after extent="{$page-margin-bottom}"
                    display-align="after"
                    region-name="last-body-footer"/>
            </fo:simple-page-master>

            <!--INDEX simple masters-->

            <fo:simple-page-master
                master-name="index-first"
                page-width="{$page-width}"
                page-height="{$page-height}">
                <fo:region-body
                    margin-top="{$page-margin-top}"
                    margin-bottom="{$page-margin-bottom}"
                    margin-left="{$page-margin-left}"
                    margin-right="{$page-margin-right}">
                      <xsl:if test="$pdfFormatter != 'xep'">
                        <xsl:attribute name="column-count">2</xsl:attribute>
                      </xsl:if>
                </fo:region-body>
                <fo:region-before extent="{$page-margin-top}"
                    display-align="before"
                    region-name="odd-index-header"/>
                <fo:region-after extent="{$page-margin-bottom}"
                    display-align="after"
                    region-name="odd-index-footer"/>
            </fo:simple-page-master>

            <fo:simple-page-master
                master-name="index-even"
                page-width="{$page-width}"
                page-height="{$page-height}">
                <fo:region-body
                    margin-top="{$page-margin-top}"
                    margin-bottom="{$page-margin-bottom}"
                    margin-left="{$page-margin-left}"
                    margin-right="{$page-margin-right}">
                      <xsl:if test="$pdfFormatter != 'xep'">
                        <xsl:attribute name="column-count">2</xsl:attribute>
                      </xsl:if>
                </fo:region-body>
                <fo:region-before extent="{$page-margin-top}"
                    display-align="before"
                    region-name="even-index-header"/>
                <fo:region-after extent="{$page-margin-bottom}"
                    display-align="after"
                    region-name="even-index-footer"/>
            </fo:simple-page-master>

            <fo:simple-page-master
                master-name="index-odd"
                page-width="{$page-width}"
                page-height="{$page-height}">
                <fo:region-body
                    margin-top="{$page-margin-top}"
                    margin-bottom="{$page-margin-bottom}"
                    margin-left="{$page-margin-left}"
                    margin-right="{$page-margin-right}">
                      <xsl:if test="$pdfFormatter != 'xep'">
                        <xsl:attribute name="column-count">2</xsl:attribute>
                      </xsl:if>
                </fo:region-body>
                <fo:region-before extent="{$page-margin-top}"
                    display-align="before"
                    region-name="odd-index-header"/>
                <fo:region-after extent="{$page-margin-bottom}"
                    display-align="after"
                    region-name="odd-index-footer"/>
            </fo:simple-page-master>

            <!--GLOSSARY simple masters-->

            <fo:simple-page-master
                master-name="glossary-first"
                page-width="{$page-width}"
                page-height="{$page-height}">
                <fo:region-body
                    margin-top="{$page-margin-top}"
                    margin-bottom="{$page-margin-bottom}"
                    margin-left="{$page-margin-left}"
                    margin-right="{$page-margin-right}">
                </fo:region-body>
                <fo:region-before extent="{$page-margin-top}"
                    display-align="before"
                    region-name="odd-glossary-header"/>
                <fo:region-after extent="{$page-margin-bottom}"
                    display-align="after"
                    region-name="odd-glossary-footer"/>
            </fo:simple-page-master>

            <fo:simple-page-master
                master-name="glossary-even"
                page-width="{$page-width}"
                page-height="{$page-height}">
                <fo:region-body
                    margin-top="{$page-margin-top}"
                    margin-bottom="{$page-margin-bottom}"
                    margin-left="{$page-margin-left}"
                    margin-right="{$page-margin-right}">
                </fo:region-body>
                <fo:region-before extent="{$page-margin-top}"
                    display-align="before"
                    region-name="even-glossary-header"/>
                <fo:region-after extent="{$page-margin-bottom}"
                    display-align="after"
                    region-name="even-glossary-footer"/>
            </fo:simple-page-master>

            <fo:simple-page-master
                master-name="glossary-odd"
                page-width="{$page-width}"
                page-height="{$page-height}">
                <fo:region-body
                    margin-top="{$page-margin-top}"
                    margin-bottom="{$page-margin-bottom}"
                    margin-left="{$page-margin-left}"
                    margin-right="{$page-margin-right}">
                </fo:region-body>
                <fo:region-before extent="{$page-margin-top}"
                    display-align="before"
                    region-name="odd-glossary-header"/>
                <fo:region-after extent="{$page-margin-bottom}"
                    display-align="after"
                    region-name="odd-glossary-footer"/>
            </fo:simple-page-master>


            <!--Sequences-->
            <fo:page-sequence-master master-name="toc-sequence">
                <fo:repeatable-page-master-alternatives>
                    <fo:conditional-page-master-reference master-reference="toc-first" odd-or-even="odd" page-position="first"/>
                    <fo:conditional-page-master-reference master-reference="toc-last" odd-or-even="even" page-position="last" blank-or-not-blank="blank"/>
                    <fo:conditional-page-master-reference master-reference="toc-odd" odd-or-even="odd"/>
                    <fo:conditional-page-master-reference master-reference="toc-even" odd-or-even="even"/>
                </fo:repeatable-page-master-alternatives>
            </fo:page-sequence-master>

            <fo:page-sequence-master master-name="body-sequence">
                <fo:repeatable-page-master-alternatives>
                    <fo:conditional-page-master-reference page-position="first" master-reference="body-first" odd-or-even="odd"/>
                    <fo:conditional-page-master-reference master-reference="body-last" odd-or-even="even" page-position="last" blank-or-not-blank="blank"/>
                    <fo:conditional-page-master-reference master-reference="body-odd" odd-or-even="odd"/>
                    <fo:conditional-page-master-reference master-reference="body-even" odd-or-even="even"/>
                </fo:repeatable-page-master-alternatives>
            </fo:page-sequence-master>

            <fo:page-sequence-master master-name="ditamap-body-sequence">
                <fo:repeatable-page-master-alternatives>
                    <fo:conditional-page-master-reference master-reference="body-odd" odd-or-even="odd"/>
                    <fo:conditional-page-master-reference master-reference="body-even" odd-or-even="even"/>
                </fo:repeatable-page-master-alternatives>
            </fo:page-sequence-master>

            <fo:page-sequence-master master-name="index-sequence">
                <fo:repeatable-page-master-alternatives>
                    <fo:conditional-page-master-reference page-position="first" master-reference="index-first" odd-or-even="odd"/>
                    <fo:conditional-page-master-reference master-reference="index-odd" odd-or-even="odd" />
                    <fo:conditional-page-master-reference master-reference="index-even" odd-or-even="even"/>
                </fo:repeatable-page-master-alternatives>
            </fo:page-sequence-master>

            <fo:page-sequence-master master-name="front-matter">
                <fo:repeatable-page-master-alternatives>
                    <fo:conditional-page-master-reference page-position="first" master-reference="front-matter-first" odd-or-even="odd"/>
                    <fo:conditional-page-master-reference master-reference="front-matter-last" page-position="last" odd-or-even="even" blank-or-not-blank="blank"/>
                    <fo:conditional-page-master-reference master-reference="front-matter-even" odd-or-even="even"/>
                    <fo:conditional-page-master-reference master-reference="front-matter-odd" odd-or-even="odd"/>
                </fo:repeatable-page-master-alternatives>
            </fo:page-sequence-master>

            <fo:page-sequence-master master-name="glossary-sequence">
                <fo:repeatable-page-master-alternatives>
                    <fo:conditional-page-master-reference page-position="first" master-reference="glossary-first" odd-or-even="odd"/>
                    <fo:conditional-page-master-reference master-reference="glossary-odd" odd-or-even="odd" />
                    <fo:conditional-page-master-reference master-reference="glossary-even" odd-or-even="even"/>
                </fo:repeatable-page-master-alternatives>
            </fo:page-sequence-master>

        </fo:layout-master-set>
    </xsl:template>
</xsl:stylesheet>