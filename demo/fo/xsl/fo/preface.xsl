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
    xmlns:opentopic="http://www.idiominc.com/opentopic"
    xmlns:exsl="http://exslt.org/common"
    extension-element-prefixes="exsl"
    exclude-result-prefixes="opentopic exsl"
    version="2.0">

     <xsl:template name="processTopicPreface">
         <fo:page-sequence master-reference="body-sequence" format="i" xsl:use-attribute-sets="__force__page__count">
             <xsl:call-template name="insertPrefaceStaticContents"/>
             <fo:flow flow-name="xsl-region-body">
                 <fo:block xsl:use-attribute-sets="topic">
                     <xsl:if test="not(ancestor::*[contains(@class, ' topic/topic ')])">
                         <fo:marker marker-class-name="current-topic-number">
                             <xsl:number format="1"/>
                         </fo:marker>
                         <fo:marker marker-class-name="current-header">
                             <xsl:for-each select="child::*[contains(@class,' topic/title ')]">
                                 <xsl:call-template name="getTitle"/>
                             </xsl:for-each>
                         </fo:marker>
                     </xsl:if>

                     <xsl:apply-templates select="*[contains(@class,' topic/prolog ')]"/>

                     <xsl:call-template name="insertChapterFirstpageStaticContent">
                         <xsl:with-param name="type" select="'preface'"/>
                     </xsl:call-template>

                     <fo:block xsl:use-attribute-sets="topic.title">
                         <xsl:for-each select="child::*[contains(@class,' topic/title ')]">
                             <xsl:call-template name="getTitle"/>
                         </xsl:for-each>
                     </fo:block>

                     <!--<xsl:call-template name="createMiniToc"/>-->

                     <xsl:apply-templates select="*[not(contains(@class,' topic/title '))]"/>
                 </fo:block>
             </fo:flow>
         </fo:page-sequence>


<!--        <fo:page-sequence master-reference="body-sequence" xsl:use-attribute-sets="__force__page__count">-->
<!--            <xsl:call-template name="insertBodyStaticContents"/>-->
<!--            <fo:flow flow-name="xsl-region-body">-->
<!--
                <fo:block xsl:use-attribute-sets="topic" page-break-before="always">
                    <xsl:if test="not(ancestor::*[contains(@class, ' topic/topic ')])">
                        <fo:marker marker-class-name="current-topic-number">
                            <xsl:number format="1"/>
                        </fo:marker>
                        <fo:marker marker-class-name="current-header">
                            <xsl:for-each select="child::*[contains(@class,' topic/title ')]">
                                <xsl:call-template name="getTitle"/>
                            </xsl:for-each>
                        </fo:marker>
                    </xsl:if>
                    <fo:inline id="{@id}"/>
                    <fo:inline>
                        <xsl:attribute name="id"">
                            <xsl:call-template name="generate-toc-id"/>
                        </xsl:attribute>
                    </fo:inline>
                    <fo:block>
                        <xsl:attribute name="border-bottom">3pt solid black</xsl:attribute>
                        <xsl:attribute name="margin-bottom">16.8pt</xsl:attribute>
                    </fo:block>
                    <fo:block xsl:use-attribute-sets="body__toplevel">
                        <xsl:apply-templates select="*[not(contains(@class, ' topic/title '))]"/>
                    </fo:block>
                </fo:block>
-->
<!--            </fo:flow>-->
<!--        </fo:page-sequence>-->
    </xsl:template>

<!--
    <xsl:template match="*[contains(@class, ' topic/topic ')]" mode="process-preface">
        <xsl:param name="include" select="'true'"/>
        <xsl:variable name="topicType">
            <xsl:call-template name="determineTopicType"/>
        </xsl:variable>

        <xsl:if test="$topicType = 'topicPreface'">
            <xsl:call-template name="processTopicPreface"/>
        </xsl:if>
    </xsl:template>
-->


</xsl:stylesheet>