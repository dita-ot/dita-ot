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
    exclude-result-prefixes="opentopic"
    version="2.0">

    <xsl:variable name="map" select="//opentopic:map"/>

    <xsl:template name="createFrontMatter">
        <xsl:choose>
            <xsl:when test="$ditaVersion &gt;= 1.1">
                <xsl:call-template name="createFrontMatter_1.0"/>
            </xsl:when>
            <xsl:otherwise>
                <fo:page-sequence master-reference="front-matter" xsl:use-attribute-sets="__force__page__count">
                    <xsl:call-template name="insertFrontMatterStaticContents"/>
                    <fo:flow flow-name="xsl-region-body">
                        <fo:block xsl:use-attribute-sets="__frontmatter">
                            <!-- set the title -->
                            <fo:block xsl:use-attribute-sets="__frontmatter__title">
                                <xsl:choose>
                                    <xsl:when test="//*[contains(@class,' bkinfo/bkinfo ')][1]">
                                        <xsl:apply-templates select="//*[contains(@class,' bkinfo/bkinfo ')][1]/*[contains(@class,' topic/title ')]/node()"/>
                                    </xsl:when>
                                    <xsl:when test="//*[contains(@class, ' map/map ')]/@title">
                                        <xsl:value-of select="//*[contains(@class, ' map/map ')]/@title"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="/descendant::*[contains(@class, ' topic/topic ')][1]/*[contains(@class, ' topic/title ')]"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </fo:block>

                            <!-- set the subtitle -->
                            <xsl:apply-templates select="//*[contains(@class,' bkinfo/bkinfo ')][1]/*[contains(@class,' bkinfo/bktitlealts ')]/*[contains(@class,' bkinfo/bksubtitle ')]"/>

                            <fo:block xsl:use-attribute-sets="__frontmatter__owner">
                                <xsl:choose>
                                    <xsl:when test="//*[contains(@class,' bkinfo/bkowner ')]">
                                        <xsl:apply-templates select="//*[contains(@class,' bkinfo/bkowner ')]"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:apply-templates select="$map/*[contains(@class, ' map/topicmeta ')]"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </fo:block>

                        </fo:block>

                        <!--<xsl:call-template name="createPreface"/>-->

                    </fo:flow>
                </fo:page-sequence>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="createFrontMatter_1.0">
        <fo:page-sequence master-reference="front-matter" xsl:use-attribute-sets="__force__page__count">
            <xsl:call-template name="insertFrontMatterStaticContents"/>
            <fo:flow flow-name="xsl-region-body">
                <fo:block xsl:use-attribute-sets="__frontmatter">
                    <!-- set the title -->
                    <fo:block xsl:use-attribute-sets="__frontmatter__title">
                        <xsl:choose>
                            <xsl:when test="$map/*[contains(@class,' topic/title ')][1]">
                                <xsl:apply-templates select="$map/*[contains(@class,' topic/title ')][1]"/>
                            </xsl:when>
                            <xsl:when test="$map//*[contains(@class,' bookmap/mainbooktitle ')][1]">
                                <xsl:apply-templates select="$map//*[contains(@class,' bookmap/mainbooktitle ')][1]"/>
                            </xsl:when>
                            <xsl:when test="//*[contains(@class, ' map/map ')]/@title">
                                <xsl:value-of select="//*[contains(@class, ' map/map ')]/@title"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="/descendant::*[contains(@class, ' topic/topic ')][1]/*[contains(@class, ' topic/title ')]"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </fo:block>

                    <!-- set the subtitle -->
                    <xsl:apply-templates select="$map//*[contains(@class,' bookmap/booktitlealt ')]"/>

                    <fo:block xsl:use-attribute-sets="__frontmatter__owner">
                        <xsl:apply-templates select="$map//*[contains(@class,' bookmap/bookmeta ')]"/>
                    </fo:block>

                </fo:block>

                <!--<xsl:call-template name="createPreface"/>-->

            </fo:flow>
        </fo:page-sequence>
        <xsl:if test="not($retain-bookmap-order)">
          <xsl:call-template name="createNotices"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' bookmap/bookmeta ')]">
        <fo:block-container xsl:use-attribute-sets="__frontmatter__owner__container">
            <fo:block >
				<xsl:apply-templates/>
            </fo:block>
        </fo:block-container>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' bookmap/booktitlealt ')]" priority="+2">
        <fo:block xsl:use-attribute-sets="__frontmatter__subtitle">
            <xsl:apply-templates/>
        </fo:block>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' bookmap/booktitle ')]" priority="+2">
        <fo:block xsl:use-attribute-sets="__frontmatter__booklibrary">
            <xsl:apply-templates select="*[contains(@class, ' bookmap/booklibrary ')]"/>
        </fo:block>
        <fo:block xsl:use-attribute-sets="__frontmatter__mainbooktitle">
            <xsl:apply-templates select="*[contains(@class,' bookmap/mainbooktitle ')]"/>
        </fo:block>
    </xsl:template>

	<xsl:template match="*[contains(@class, ' xnal-d/namedetails ')]">
		<fo:block>
			<xsl:apply-templates/>
		</fo:block>
	</xsl:template>

	<xsl:template match="*[contains(@class, ' xnal-d/addressdetails ')]">
		<fo:block>
			<xsl:apply-templates/>
		</fo:block>
	</xsl:template>

	<xsl:template match="*[contains(@class, ' xnal-d/contactnumbers ')]">
		<fo:block>
			<xsl:apply-templates/>
		</fo:block>
	</xsl:template>

	<xsl:template match="*[contains(@class, ' bookmap/bookowner ')]">
		<fo:block xsl:use-attribute-sets="author">
			<xsl:apply-templates/>
		</fo:block>
	</xsl:template>

	<xsl:template match="*[contains(@class, ' bookmap/summary ')]">
		<fo:block xsl:use-attribute-sets="bookmap.summary">
			<xsl:apply-templates/>
		</fo:block>
	</xsl:template>
	
	<xsl:template name="createNotices">
	   <xsl:apply-templates select="/bookmap/*[contains(@class,' topic/topic ')]" mode="process-notices"/>
	</xsl:template>
	
	<xsl:template match="*[contains(@class, ' topic/topic ')]" mode="process-notices">
        <xsl:param name="include" select="'true'"/>
        <xsl:variable name="topicType">
            <xsl:call-template name="determineTopicType"/>
        </xsl:variable>

        <xsl:if test="$topicType = 'topicNotices'">
            <xsl:call-template name="processTopicNotices"/>
        </xsl:if>
    </xsl:template>
    
</xsl:stylesheet>