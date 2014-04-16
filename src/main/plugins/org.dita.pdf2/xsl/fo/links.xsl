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

This file is part of the DITA Open Toolkit project. 
See the accompanying license.txt file for applicable licenses.
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:fo="http://www.w3.org/1999/XSL/Format"
    xmlns:opentopic-mapmerge="http://www.idiominc.com/opentopic/mapmerge"
    xmlns:opentopic-func="http://www.idiominc.com/opentopic/exsl/function"
    exclude-result-prefixes="opentopic-mapmerge opentopic-func"
    version="2.0">
  
  <xsl:import href="plugin:org.dita.base:xsl/common/output-message.xsl"/>

  <xsl:param name="figurelink.style" select="'NUMTITLE'"/>
  <xsl:param name="tablelink.style" select="'NUMTITLE'"/>

  <xsl:variable name="msgprefix">DOTX</xsl:variable>
  
    <xsl:key name="key_anchor" match="*[@id][not(contains(@class,' map/topicref '))]" use="@id"/>
<!--[not(contains(@class,' map/topicref '))]-->
    <xsl:template name="insertLinkShortDesc">
    <xsl:param name="destination"/>
    <xsl:param name="element"/>
    <xsl:param name="linkScope"/>
        <xsl:choose>
            <!-- User specified description (from map or topic): use that. -->
            <xsl:when test="*[contains(@class,' topic/desc ')] and
                            processing-instruction()[name()='ditaot'][.='usershortdesc']">
                <fo:block xsl:use-attribute-sets="link__shortdesc">
                    <xsl:apply-templates select="*[contains(@class, ' topic/desc ')]"/>
                </fo:block>
            </xsl:when>
            <!-- External: do not attempt to retrieve. -->
            <xsl:when test="$linkScope='external'">
            </xsl:when>
            <!-- When the target has a short description and no local override, use the target -->
            <xsl:when test="$element/*[contains(@class, ' topic/shortdesc ')]">
                <fo:block xsl:use-attribute-sets="link__shortdesc">
                    <xsl:apply-templates select="$element/*[contains(@class, ' topic/shortdesc ')]"/>
                </fo:block>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="insertLinkDesc">
        <xsl:call-template name="insertVariable">
            <xsl:with-param name="theVariableID" select="'Link description'"/>
            <xsl:with-param name="theParameters">
                <desc>
                    <fo:inline>
                        <xsl:apply-templates select="*[contains(@class,' topic/desc ')]" mode="insert-description"/>
                    </fo:inline>
                </desc>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="*[contains(@class,' topic/xref ') or contains(@class, ' topic/link ')]/*[contains(@class,' topic/desc ')]" priority="1"/>
    <xsl:template match="*[contains(@class,' topic/desc ')]" mode="insert-description">
        <xsl:apply-templates/>
    </xsl:template>


    <!-- The insertReferenceTitle template is called from <xref> and <link> and is
         used to build link contents (using full FO syntax, not just the text). -->
    <!-- Process any cross reference or link with author-specified text. 
         The specified text is used as the link text. -->
    <xsl:template match="*[processing-instruction()[name()='ditaot'][.='usertext']]" mode="insertReferenceTitle">
        <xsl:apply-templates select="*[not(contains(@class,' topic/desc '))]|text()"/>
    </xsl:template>

    <!-- Process any cross reference or link with no content, or with content
         generated by the DITA-OT preprocess. The title will be retrieved from
         the target element, and combined with generated text such as Figure N. -->
    <xsl:template match="*" mode="insertReferenceTitle">
        <xsl:param name="href"/>
        <xsl:param name="titlePrefix"/>
        <xsl:param name="destination"/>
        <xsl:param name="element"/>

        <xsl:variable name="referenceContent">
            <xsl:choose>
                <xsl:when test="not($element) or ($destination = '')">
                    <xsl:text>#none#</xsl:text>
                </xsl:when>
                <xsl:when test="contains($element/@class,' topic/li ') and 
                                contains($element/parent::*/@class,' topic/ol ')">
                    <!-- SF Bug 1839827: This causes preprocessor text to be used for links to OL/LI -->
                    <xsl:text>#none#</xsl:text>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates select="$element" mode="retrieveReferenceTitle"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
                

        <xsl:if test="not($titlePrefix = '')">
            <xsl:call-template name="insertVariable">
                <xsl:with-param name="theVariableID" select="$titlePrefix"/>
            </xsl:call-template>
        </xsl:if>

    <xsl:choose>
            <xsl:when test="not($element) or ($destination = '') or $referenceContent='#none#'">
                <xsl:choose>
                    <xsl:when test="*[not(contains(@class,' topic/desc '))] | text()">
                        <xsl:apply-templates select="*[not(contains(@class,' topic/desc '))] | text()"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="$href"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>

            <xsl:otherwise>
                <xsl:copy-of select="$referenceContent"/>
            </xsl:otherwise>
        </xsl:choose>

    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/fig ')][*[contains(@class, ' topic/title ')]]" mode="retrieveReferenceTitle">
      <xsl:choose>
        <xsl:when test="$figurelink.style='NUMBER'">
          <xsl:call-template name="insertVariable">
            <xsl:with-param name="theVariableID" select="'Figure Number'"/>
            <xsl:with-param name="theParameters">
                <number>
                    <xsl:value-of select="count(preceding::*[contains(@class, ' topic/fig ')][child::*[contains(@class, ' topic/title ')]]) + 1"/>
                </number>
            </xsl:with-param>
          </xsl:call-template>
        </xsl:when>
        <xsl:when test="$figurelink.style='TITLE'">
          <xsl:apply-templates select="*[contains(@class, ' topic/title ')]" mode="insert-text"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="insertVariable">
            <xsl:with-param name="theVariableID" select="'Figure'"/>
            <xsl:with-param name="theParameters">
                <number>
                    <xsl:value-of select="count(preceding::*[contains(@class, ' topic/fig ')][child::*[contains(@class, ' topic/title ')]]) + 1"/>
                </number>
                <title>
                    <xsl:apply-templates select="*[contains(@class, ' topic/title ')]" mode="insert-text"/>
                </title>
            </xsl:with-param>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/section ')][*[contains(@class, ' topic/title ')]]" mode="retrieveReferenceTitle">
        <xsl:variable name="title">
            <xsl:apply-templates select="*[contains(@class, ' topic/title ')]" mode="insert-text"/>
        </xsl:variable>
        <xsl:value-of select="normalize-space($title)"/>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/table ')][*[contains(@class, ' topic/title ')]]" mode="retrieveReferenceTitle">
      <xsl:choose>
        <xsl:when test="$tablelink.style='NUMBER'">
          <xsl:call-template name="insertVariable">
            <xsl:with-param name="theVariableID" select="'Table Number'"/>
            <xsl:with-param name="theParameters">
                <number>
                    <xsl:value-of select="count(preceding::*[contains(@class, ' topic/table ')][child::*[contains(@class, ' topic/title ')]]) + 1"/>
                </number>
            </xsl:with-param>
          </xsl:call-template>
        </xsl:when>
        <xsl:when test="$tablelink.style='TITLE'">
          <xsl:apply-templates select="*[contains(@class, ' topic/title ')]" mode="insert-text"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="insertVariable">
            <xsl:with-param name="theVariableID" select="'Table'"/>
            <xsl:with-param name="theParameters">
                <number>
                    <xsl:value-of select="count(preceding::*[contains(@class, ' topic/table ')][child::*[contains(@class, ' topic/title ')]]) + 1"/>
                </number>
                <title>
                    <xsl:apply-templates select="*[contains(@class, ' topic/title ')]" mode="insert-text"/>
                </title>
            </xsl:with-param>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/li ')]" mode="retrieveReferenceTitle">
        <xsl:call-template name="insertVariable">
            <xsl:with-param name="theVariableID" select="'List item'"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/fn ')]" mode="retrieveReferenceTitle">
    <xsl:call-template name="insertVariable">
        <xsl:with-param name="theVariableID" select="'Foot note'"/>
    </xsl:call-template>
    </xsl:template>

    <!-- Default rule: if element has a title, use that, otherwise return '#none#' -->
    <xsl:template match="*" mode="retrieveReferenceTitle" >
        <xsl:choose>
            <xsl:when test="*[contains(@class,' topic/title ')]">
                <xsl:value-of select="string(*[contains(@class, ' topic/title ')])"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:text>#none#</xsl:text>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="*[contains(@class,' topic/xref ')]" name="topic.xref">
        <fo:inline>
            <xsl:call-template name="commonattributes"/>
        </fo:inline>

    <xsl:variable name="destination" select="opentopic-func:getDestinationId(@href)"/>
    <xsl:variable name="element" select="key('key_anchor',$destination)[1]"/>

    <xsl:variable name="referenceTitle" as="node()*">
      <xsl:apply-templates select="." mode="insertReferenceTitle">
        <xsl:with-param name="href" select="@href"/>
        <xsl:with-param name="titlePrefix" select="''"/>
        <xsl:with-param name="destination" select="$destination"/>
        <xsl:with-param name="element" select="$element"/>
      </xsl:apply-templates>
    </xsl:variable>

    <fo:basic-link xsl:use-attribute-sets="xref">
      <xsl:call-template name="buildBasicLinkDestination">
        <xsl:with-param name="scope" select="@scope"/>
        <xsl:with-param name="format" select="@format"/>
        <xsl:with-param name="href" select="@href"/>
      </xsl:call-template>

      <xsl:choose>
        <xsl:when test="not(@scope = 'external' or not(empty(@format) or  @format = 'dita')) and exists($referenceTitle)">
          <xsl:copy-of select="$referenceTitle"/>
        </xsl:when>
        <xsl:when test="not(@scope = 'external' or not(empty(@format) or  @format = 'dita'))">
          <xsl:call-template name="insertPageNumberCitation">
            <xsl:with-param name="isTitleEmpty" select="'yes'"/>
            <xsl:with-param name="destination" select="$destination"/>
            <xsl:with-param name="element" select="$element"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <xsl:choose>
            <xsl:when test="*[not(contains(@class,' topic/desc '))] | text()">
              <xsl:apply-templates select="*[not(contains(@class,' topic/desc '))] | text()" />
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="@href"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:otherwise>
      </xsl:choose>
    </fo:basic-link>

    <!--
        Disable because of the CQ#8102 bug
        <xsl:if test="*[contains(@class,' topic/desc ')]">
          <xsl:call-template name="insertLinkDesc"/>
        </xsl:if>
    -->

      <xsl:if test="not(@scope = 'external' or not(empty(@format) or  @format = 'dita')) and exists($referenceTitle) and not($element[contains(@class, ' topic/fn ')])">
            <!-- SourceForge bug 1880097: should not include page number when xref includes author specified text -->
            <xsl:if test="not(processing-instruction()[name()='ditaot'][.='usertext'])">
                <xsl:call-template name="insertPageNumberCitation">
                    <xsl:with-param name="destination" select="$destination"/>
                      <xsl:with-param name="element" select="$element"/>
                  </xsl:call-template>
            </xsl:if>
    </xsl:if>

    </xsl:template>

    <!-- xref to footnote makes a callout. -->
    <xsl:template match="*[contains(@class,' topic/xref ')][@type='fn']" priority="2">
        <xsl:variable name="href-fragment" select="substring-after(@href, '#')"/>
        <xsl:variable name="footnote-target" select="//*[contains(@class, ' topic/fn ')][@id = substring-after($href-fragment, '/')][ancestor::*[contains(@class, ' topic/topic ')][1]/@id = substring-before($href-fragment, '/')]"/>
        <xsl:apply-templates select="$footnote-target" mode="footnote-callout"/>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/fn ')]" mode="footnote-callout">
            <fo:inline xsl:use-attribute-sets="fn__callout">

                <xsl:choose>
                    <xsl:when test="@callout">
                        <xsl:value-of select="@callout"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:number level="any" count="*[contains(@class,' topic/fn ') and not(@callout)]"/>
                    </xsl:otherwise>
                </xsl:choose>

            </fo:inline>
    </xsl:template>

    <xsl:template match="*[contains(@class,' topic/related-links ')]">
        <xsl:if test="normalize-space($includeRelatedLinkRoles)">
            <xsl:variable name="topicType">
                <xsl:for-each select="parent::*">
                    <xsl:call-template name="determineTopicType"/>
                </xsl:for-each>
            </xsl:variable>

            <xsl:variable name="collectedLinks">
                <xsl:apply-templates>
                    <xsl:with-param name="topicType" select="$topicType"/>
                </xsl:apply-templates>
            </xsl:variable>

            <xsl:variable name="linkTextContent" select="string($collectedLinks)"/>

            <xsl:if test="normalize-space($linkTextContent)!=''">
                <fo:block xsl:use-attribute-sets="related-links">

            <fo:block xsl:use-attribute-sets="related-links.title">
              <xsl:call-template name="insertVariable">
                <xsl:with-param name="theVariableID" select="'Related Links'"/>
              </xsl:call-template>
            </fo:block>

            <fo:block xsl:use-attribute-sets="related-links__content">
                        <xsl:copy-of select="$collectedLinks"/>
                    </fo:block>
                </fo:block>
            </xsl:if>

        </xsl:if>
    </xsl:template>

    <xsl:template name="getLinkScope">
        <xsl:choose>
            <xsl:when test="@scope">
                <xsl:value-of select="@scope"/>
            </xsl:when>
            <xsl:when test="contains(@class, ' topic/related-links ')">
                <xsl:value-of select="'local'"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:for-each select="..">
                    <xsl:call-template name="getLinkScope"/>
                </xsl:for-each>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="*[contains(@class,' topic/link ')]">
      <xsl:param name="topicType">
          <xsl:for-each select="ancestor::*[contains(@class,' topic/topic ')][1]">
              <xsl:call-template name="determineTopicType"/>
          </xsl:for-each>
      </xsl:param>
      <xsl:choose>
        <xsl:when test="(@role and not(contains($includeRelatedLinkRoles, concat(' ', @role, ' ')))) or
                        (not(@role) and not(contains($includeRelatedLinkRoles, ' #default ')))"/>
        <xsl:when test="@role='child' and $chapterLayout='MINITOC' and
                        ($topicType='topicChapter' or $topicType='topicAppendix' or $topicType='topicPart')">
          <!-- When a minitoc already links to children, do not add them here -->
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="." mode="processLink"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:template>

    <xsl:template match="*[contains(@class,' topic/link ')]" mode="processLink">
    <xsl:variable name="destination" select="opentopic-func:getDestinationId(@href)"/>
    <xsl:variable name="element" select="key('key_anchor',$destination)[1]"/>

    <xsl:variable name="referenceTitle" as="node()*">
            <xsl:apply-templates select="." mode="insertReferenceTitle">
                <xsl:with-param name="href" select="@href"/>
                <xsl:with-param name="titlePrefix" select="''"/>
                <xsl:with-param name="destination" select="$destination"/>
                <xsl:with-param name="element" select="$element"/>
            </xsl:apply-templates>
        </xsl:variable>
        <xsl:variable name="linkScope">
            <xsl:call-template name="getLinkScope"/>
        </xsl:variable>

        <fo:block xsl:use-attribute-sets="link">
            <fo:inline xsl:use-attribute-sets="link__content">
                <fo:basic-link>
                    <xsl:call-template name="buildBasicLinkDestination">
                        <xsl:with-param name="scope" select="$linkScope"/>
                        <xsl:with-param name="href" select="@href"/>
                    </xsl:call-template>
                    <xsl:choose>
                      <xsl:when test="not($linkScope = 'external') and exists($referenceTitle)">
                            <xsl:copy-of select="$referenceTitle"/>
                        </xsl:when>
                        <xsl:when test="not($linkScope = 'external')">
                            <xsl:call-template name="insertPageNumberCitation">
                                <xsl:with-param name="isTitleEmpty" select="'yes'"/>
                <xsl:with-param name="destination" select="$destination"/>
                <xsl:with-param name="element" select="$element"/>
                            </xsl:call-template>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:apply-templates/>
                        </xsl:otherwise>
                    </xsl:choose>
                </fo:basic-link>
            </fo:inline>
          <xsl:if test="not($linkScope = 'external') and exists($referenceTitle)">
                <xsl:call-template name="insertPageNumberCitation">
          <xsl:with-param name="destination" select="$destination"/>
          <xsl:with-param name="element" select="$element"/>
        </xsl:call-template>
            </xsl:if>
                <xsl:call-template name="insertLinkShortDesc">
          <xsl:with-param name="destination" select="$destination"/>
          <xsl:with-param name="element" select="$element"/>
          <xsl:with-param name="linkScope" select="$linkScope"/>
        </xsl:call-template>
        </fo:block>
    </xsl:template>

    <xsl:template name="buildBasicLinkDestination">
        <xsl:param name="scope" select="@scope"/>
      <xsl:param name="format" select="@format"/>
        <xsl:param name="href" select="@href"/>
        <xsl:choose>
            <xsl:when test="(contains($href, '://') and not(starts-with($href, 'file://')))
            or starts-with($href, '/') or $scope = 'external' or not(empty($format) or  $format = 'dita')">
                <xsl:attribute name="external-destination">
                    <xsl:value-of select="concat('url(', $href, ')')"/>
                </xsl:attribute>
            </xsl:when>
          <xsl:when test="$scope = 'peer'">
            <xsl:attribute name="internal-destination">
              <xsl:value-of select="$href"/>
            </xsl:attribute>
          </xsl:when>
          <xsl:when test="contains($href, '#')">
            <xsl:attribute name="internal-destination">
              <xsl:value-of select="opentopic-func:getDestinationId($href)"/>
            </xsl:attribute>
          </xsl:when>         
            <xsl:otherwise>
              <xsl:attribute name="internal-destination">
                <xsl:value-of select="$href"/>
              </xsl:attribute>
              <xsl:call-template name="brokenLinks">
                <xsl:with-param name="href" select="$href"/>
              </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="insertPageNumberCitation">
        <xsl:param name="isTitleEmpty"/>
        <xsl:param name="destination"/>
        <xsl:param name="element"/>

        <xsl:choose>
            <xsl:when test="not($element) or ($destination = '')"/>
            <xsl:when test="$isTitleEmpty">
                <fo:inline>
                    <xsl:call-template name="insertVariable">
                        <xsl:with-param name="theVariableID" select="'Page'"/>
                        <xsl:with-param name="theParameters">
                            <pagenum>
                                <fo:inline>
                                    <fo:page-number-citation ref-id="{$destination}"/>
                                </fo:inline>
                            </pagenum>
                        </xsl:with-param>
                    </xsl:call-template>
                </fo:inline>
            </xsl:when>
            <xsl:otherwise>
                <fo:inline>
                    <xsl:call-template name="insertVariable">
                        <xsl:with-param name="theVariableID" select="'On the page'"/>
                        <xsl:with-param name="theParameters">
                            <pagenum>
                                <fo:inline>
                                    <fo:page-number-citation ref-id="{$destination}"/>
                                </fo:inline>
                            </pagenum>
                        </xsl:with-param>
                    </xsl:call-template>
                </fo:inline>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="*[contains(@class,' topic/linktext ')]">
        <fo:inline xsl:use-attribute-sets="linktext">
            <xsl:apply-templates/>
        </fo:inline>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/linklist ')]">
        <fo:block xsl:use-attribute-sets="linklist">
            <xsl:apply-templates/>
        </fo:block>
    </xsl:template>

    <xsl:template match="*[contains(@class,' topic/linkinfo ')]">
        <fo:block xsl:use-attribute-sets="linkinfo">
            <xsl:apply-templates/>
        </fo:block>
    </xsl:template>

    <xsl:template match="*[contains(@class,' topic/linkpool ')]">
        <xsl:param name="topicType"/>
        <fo:block xsl:use-attribute-sets="linkpool">
            <xsl:apply-templates>
                <xsl:with-param name="topicType" select="$topicType"/>
            </xsl:apply-templates>
        </fo:block>
    </xsl:template>

    <xsl:function name="opentopic-func:getDestinationId">
        <xsl:param name="href"/>
        <xsl:call-template name="getDestinationIdImpl">
            <xsl:with-param name="href" select="$href"/>
        </xsl:call-template>
    </xsl:function>

    <xsl:template name="getDestinationIdImpl">
        <xsl:param name="href"/>
        
        <xsl:variable name="topic-id" select="substring-after($href, '#')"/>

        <xsl:variable name="element-id" select="substring-after($topic-id, '/')"/>

        <xsl:choose>
            <xsl:when test="$element-id = ''">
                <xsl:value-of select="$topic-id"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$element-id"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
        <!--Related links-->

    <xsl:template match="*" mode="buildRelationships">
      <xsl:variable name="parentCollectionType">
        <xsl:call-template name="getCollectionType">
          <xsl:with-param name="nodeType" select="'parent'"/>
        </xsl:call-template>
      </xsl:variable>
      <xsl:variable name="selfCollectionType">
        <xsl:call-template name="getCollectionType">
          <xsl:with-param name="nodeType" select="'self'"/>
        </xsl:call-template>
      </xsl:variable>

      <xsl:variable name="relatedConceptsTitle">
        <xsl:call-template name="insertVariable">
          <xsl:with-param name="theVariableID" select="'Related concepts'"/>
        </xsl:call-template>
      </xsl:variable>
      <xsl:variable name="relatedTasksTitle">
        <xsl:call-template name="insertVariable">
          <xsl:with-param name="theVariableID" select="'Related tasks'"/>
        </xsl:call-template>
      </xsl:variable>
      <xsl:variable name="relatedReferencesTitle">
        <xsl:call-template name="insertVariable">
          <xsl:with-param name="theVariableID" select="'Related references'"/>
        </xsl:call-template>
      </xsl:variable>
      <xsl:variable name="relatedInformationTitle">
        <xsl:call-template name="insertVariable">
          <xsl:with-param name="theVariableID" select="'Related information'"/>
        </xsl:call-template>
      </xsl:variable>


      <xsl:choose>
        <xsl:when test="$selfCollectionType = 'none'">
          <xsl:call-template name="linkToChilds">
            <xsl:with-param name="listType" select="'none'"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:when test="($selfCollectionType = 'unordered') or ($selfCollectionType = 'choice')  or ($selfCollectionType = 'family')">
          <xsl:call-template name="linkToChilds">
            <xsl:with-param name="listType" select="'bulleted'"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:when test="$selfCollectionType = 'sequence'">
          <xsl:call-template name="linkToChilds">
            <xsl:with-param name="listType" select="'numbered'"/>
          </xsl:call-template>
        </xsl:when>
      </xsl:choose>

      <xsl:choose>
        <xsl:when test="($parentCollectionType = 'none') or ($parentCollectionType = 'unordered') or ($parentCollectionType = 'choice')">
          <xsl:call-template name="linkToParent"/>

          <!-- Creating relationships to the concepts -->
          <xsl:call-template name="createRelatedLinks">
            <xsl:with-param name="linkType" select="'concept'"/>
            <xsl:with-param name="title" select="$relatedConceptsTitle"/>
          </xsl:call-template>

          <!-- Creating relationships to the tasks -->
          <xsl:call-template name="createRelatedLinks">
            <xsl:with-param name="linkType" select="'task'"/>
            <xsl:with-param name="title" select="$relatedTasksTitle"/>
          </xsl:call-template>

          <!-- Creating relationships to the references -->
          <xsl:call-template name="createRelatedLinks">
            <xsl:with-param name="linkType" select="'reference'"/>
            <xsl:with-param name="title" select="$relatedReferencesTitle"/>
          </xsl:call-template>

          <!-- Creating relationships to the topics -->
          <xsl:call-template name="createRelatedLinks">
            <xsl:with-param name="linkType" select="'topic'"/>
            <xsl:with-param name="title" select="$relatedInformationTitle"/>
          </xsl:call-template>
        </xsl:when>

        <xsl:when test="$parentCollectionType = 'sequence'">
          <xsl:call-template name="linkToParent"/>
          <xsl:call-template name="linkToPrevious"/>
          <xsl:call-template name="linkToNext"/>

          <!-- Creating relationships to the concepts -->
          <xsl:call-template name="createRelatedLinks">
            <xsl:with-param name="linkType" select="'concept'"/>
            <xsl:with-param name="title" select="$relatedConceptsTitle"/>
          </xsl:call-template>

          <!-- Creating relationships to the tasks -->
          <xsl:call-template name="createRelatedLinks">
            <xsl:with-param name="linkType" select="'task'"/>
            <xsl:with-param name="title" select="$relatedTasksTitle"/>
          </xsl:call-template>

          <!-- Creating relationships to the references -->
          <xsl:call-template name="createRelatedLinks">
            <xsl:with-param name="linkType" select="'reference'"/>
            <xsl:with-param name="title" select="$relatedReferencesTitle"/>
          </xsl:call-template>

          <!-- Creating relationships to the topics -->
          <xsl:call-template name="createRelatedLinks">
            <xsl:with-param name="linkType" select="'topic'"/>
            <xsl:with-param name="title" select="$relatedInformationTitle"/>
          </xsl:call-template>
        </xsl:when>

        <xsl:when test="$parentCollectionType = 'family'">
          <xsl:call-template name="linkToParent"/>

          <!-- Creating relationships to the concepts -->
          <xsl:variable name="siblingConcepts" select="preceding-sibling::*[contains(@class, ' concept/concept ')] | following-sibling::*[contains(@class, ' concept/concept ')]"/>
          <xsl:call-template name="createMapLinks">
            <xsl:with-param name="nodeSet" select="$siblingConcepts"/>
            <xsl:with-param name="title" select="$relatedConceptsTitle"/>
          </xsl:call-template>
          <xsl:choose>
            <xsl:when test="$siblingConcepts">
              <xsl:call-template name="createRelatedLinks">
                <xsl:with-param name="linkType" select="'concept'"/>
              </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
              <xsl:call-template name="createRelatedLinks">
                <xsl:with-param name="linkType" select="'concept'"/>
                <xsl:with-param name="title" select="$relatedConceptsTitle"/>
              </xsl:call-template>
            </xsl:otherwise>
          </xsl:choose>

          <!-- Creating relationships to the tasks -->
          <xsl:variable name="siblingTasks" select="preceding-sibling::*[contains(@class, ' task/task ')] | following-sibling::*[contains(@class, ' task/task ')]"/>
          <xsl:call-template name="createMapLinks">
            <xsl:with-param name="nodeSet" select="$siblingTasks"/>
            <xsl:with-param name="title" select="$relatedTasksTitle"/>
          </xsl:call-template>
          <xsl:choose>
            <xsl:when test="$siblingTasks">
              <xsl:call-template name="createRelatedLinks">
                <xsl:with-param name="linkType" select="'task'"/>
              </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
              <xsl:call-template name="createRelatedLinks">
                <xsl:with-param name="linkType" select="'task'"/>
                <xsl:with-param name="title" select="$relatedTasksTitle"/>
              </xsl:call-template>
            </xsl:otherwise>
          </xsl:choose>

          <!-- Creating relationships to the references -->
          <xsl:variable name="siblingReferences" select="preceding-sibling::*[contains(@class, ' reference/reference ')] | following-sibling::*[contains(@class, ' reference/reference ')]"/>
          <xsl:call-template name="createMapLinks">
            <xsl:with-param name="nodeSet" select="$siblingReferences"/>
            <xsl:with-param name="title" select="$relatedReferencesTitle"/>
          </xsl:call-template>
          <xsl:choose>
            <xsl:when test="$siblingReferences">
              <xsl:call-template name="createRelatedLinks">
                <xsl:with-param name="linkType" select="'reference'"/>
              </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
              <xsl:call-template name="createRelatedLinks">
                <xsl:with-param name="linkType" select="'reference'"/>
                <xsl:with-param name="title" select="$relatedReferencesTitle"/>
              </xsl:call-template>
            </xsl:otherwise>
          </xsl:choose>

          <!-- Creating relationships to the topics -->
          <xsl:variable name="siblingTopics" select="preceding-sibling::*[contains(@class, ' topic/topic ') and not(contains(@class, ' concept/concept ') or contains(@class, ' task/task ') or contains(@class, ' reference/reference '))] | following-sibling::*[contains(@class, ' topic/topic ') and not(contains(@class, ' concept/concept ') or contains(@class, ' task/task ') or contains(@class, ' reference/reference '))]"/>
          <xsl:call-template name="createMapLinks">
            <xsl:with-param name="nodeSet" select="$siblingTopics"/>
            <xsl:with-param name="title" select="$relatedInformationTitle"/>
          </xsl:call-template>
          <xsl:choose>
            <xsl:when test="$siblingTopics">
              <xsl:call-template name="createRelatedLinks">
                <xsl:with-param name="linkType" select="'topic'"/>
              </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
              <xsl:call-template name="createRelatedLinks">
                <xsl:with-param name="linkType" select="'topic'"/>
                <xsl:with-param name="title" select="$relatedInformationTitle"/>
              </xsl:call-template>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>
      </xsl:choose>
  </xsl:template>

  <xsl:template name="getCollectionType">
    <xsl:param name="nodeType"/>
    <xsl:variable name="collectionType">
      <xsl:choose>
        <xsl:when test="$nodeType = 'parent'">
          <xsl:value-of select="parent::*/@collection-type"/>
        </xsl:when>
        <xsl:when test="$nodeType = 'self'">
          <xsl:value-of select="@collection-type"/>
        </xsl:when>
      </xsl:choose>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$collectionType = 'unordered'">
        <xsl:value-of select="'none'"/>
      </xsl:when>
      <xsl:when test="$collectionType">
        <xsl:value-of select="$collectionType"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="'none'"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template name="linkToParent">
    <!-- Creating relationships to the parent -->
    <xsl:variable name="linksTitle">
      <xsl:call-template name="insertVariable">
        <xsl:with-param name="theVariableID" select="'Parent topic'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:call-template name="createMapLinks">
      <xsl:with-param name="nodeSet" select="parent::*[contains(@class, ' topic/topic ')]"/>
      <xsl:with-param name="title" select="$linksTitle"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="linkToNext">
    <!-- Creating relationships to the next sibling -->
    <xsl:variable name="linksTitle">
      <xsl:call-template name="insertVariable">
        <xsl:with-param name="theVariableID" select="'Next topic'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:call-template name="createMapLinks">
      <xsl:with-param name="nodeSet" select="following-sibling::*[contains(@class, ' topic/topic ')][1]"/>
      <xsl:with-param name="title" select="$linksTitle"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="linkToPrevious">
    <!-- Creating relationships to the previous sibling -->
    <xsl:variable name="linksTitle">
      <xsl:call-template name="insertVariable">
        <xsl:with-param name="theVariableID" select="'Previous topic'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:call-template name="createMapLinks">
      <xsl:with-param name="nodeSet" select="preceding-sibling::*[contains(@class, ' topic/topic ')][1]"/>
      <xsl:with-param name="title" select="$linksTitle"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="linkToChilds">
    <!-- Creating relationships to the childs -->
    <xsl:param name="listType"/>
    <xsl:variable name="linksTitle">
      <xsl:call-template name="insertVariable">
        <xsl:with-param name="theVariableID" select="'Child topics'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:call-template name="createMapLinks">
      <xsl:with-param name="nodeSet" select="*[contains(@class, ' topic/topic ')] | *[contains(@class,' topic/dita ')]/*[contains(@class, ' topic/topic ')]"/>
      <xsl:with-param name="title" select="$linksTitle"/>
      <xsl:with-param name="listType" select="$listType"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="createMapLinks">
    <xsl:param name="nodeSet"/>
    <xsl:param name="title"/>
    <xsl:param name="listType" select="'none'"/>

    <xsl:variable name="linkNodes">
      <xsl:if test="$nodeSet">
        <xsl:choose>
          <xsl:when test="$listType = 'bulleted'">
            <xsl:call-template name="createMapLinksUnordered">
              <xsl:with-param name="nodeSet" select="$nodeSet"/>
            </xsl:call-template>
          </xsl:when>
          <xsl:when test="$listType = 'numbered'">
            <xsl:call-template name="createMapLinksOrdered">
              <xsl:with-param name="nodeSet" select="$nodeSet"/>
            </xsl:call-template>
          </xsl:when>
          <xsl:when test="$listType = 'none'">
            <fo:block xsl:use-attribute-sets="related-links">
              <xsl:for-each select="$nodeSet">
                <fo:block xsl:use-attribute-sets="related-links__content">
                  <fo:basic-link internal-destination="{@id}" xsl:use-attribute-sets="xref">
                    <xsl:apply-templates select="child::*[contains(@class, ' topic/title ')]" mode="insert-text"/>
                  </fo:basic-link>
                </fo:block>
              </xsl:for-each>
            </fo:block>
          </xsl:when>
        </xsl:choose>
      </xsl:if>
    </xsl:variable>

    <xsl:if test="($linkNodes//fo:list-block) or ($linkNodes//fo:block)">
      <xsl:if test="$title">
        <fo:block xsl:use-attribute-sets="related-links.title">
          <xsl:value-of select="$title"/>
        </fo:block>
      </xsl:if>
      <xsl:copy-of select="$linkNodes"/>
    </xsl:if>
  </xsl:template>

  <xsl:template name="createMapLinksUnordered">
    <xsl:param name="nodeSet"/>

    <fo:list-block xsl:use-attribute-sets="related-links.ul">
      <xsl:for-each select="$nodeSet">
        <fo:list-item xsl:use-attribute-sets="related-links.ul.li">
          <fo:list-item-label xsl:use-attribute-sets="related-links.ul.li__label">
            <fo:block xsl:use-attribute-sets="related-links.ul.li__label__content">
              <xsl:call-template name="insertVariable">
                <xsl:with-param name="theVariableID" select="'Unordered List bullet'"/>
              </xsl:call-template>
            </fo:block>
          </fo:list-item-label>

          <fo:list-item-body xsl:use-attribute-sets="related-links.ul.li__body">
            <fo:block xsl:use-attribute-sets="related-links.ul.li__content">
              <fo:basic-link internal-destination="{@id}" xsl:use-attribute-sets="xref">
                <xsl:apply-templates select="child::*[contains(@class, ' topic/title ')]" mode="insert-text"/>
              </fo:basic-link>
            </fo:block>
          </fo:list-item-body>
        </fo:list-item>
      </xsl:for-each>
    </fo:list-block>
  </xsl:template>

  <xsl:template name="createMapLinksOrdered">
    <xsl:param name="nodeSet"/>

    <fo:list-block xsl:use-attribute-sets="related-links.ol">
      <xsl:for-each select="$nodeSet">
        <fo:list-item xsl:use-attribute-sets="related-links.ol.li">
          <fo:list-item-label xsl:use-attribute-sets="related-links.ol.li__label">
            <fo:block xsl:use-attribute-sets="related-links.ol.li__label__content">
              <xsl:value-of select="count(preceding-sibling::*[contains(@class,' topic/topic ')])"/>
            </fo:block>
          </fo:list-item-label>

          <fo:list-item-body xsl:use-attribute-sets="related-links.ol.li__body">
            <fo:block xsl:use-attribute-sets="related-links.ol.li__content">
              <fo:basic-link internal-destination="{@id}" xsl:use-attribute-sets="xref">
                <xsl:apply-templates select="child::*[contains(@class, ' topic/title ')]" mode="insert-text"/>
              </fo:basic-link>
            </fo:block>
          </fo:list-item-body>
        </fo:list-item>
      </xsl:for-each>
    </fo:list-block>
  </xsl:template>

  <xsl:template name="createRelatedLinks">
    <xsl:param name="linkType"/>
    <xsl:param name="title"/>
    <xsl:variable name="id" select="@id"/>

    <xsl:if test="$relatedTopicrefs/@id = $id">
      <xsl:variable name="resultLinks">
        <xsl:for-each select="$relatedTopicrefs[@id = $id]">
          <xsl:choose>
            <xsl:when test="ancestor::*[contains(@class, ' map/reltable ')]/*[contains(@class, ' map/relheader ')]">
              <xsl:variable name="topicTypeCellSpec" select="ancestor::*[contains(@class, ' map/reltable ')]/*[contains(@class, ' map/relheader ')]/*[contains(@class, ' map/relcolspec ')][@type = $linkType]"/>
              <xsl:if test="$topicTypeCellSpec">
                <xsl:variable name="currPosition" select="count(ancestor::*[contains(@class, ' map/relcell ')][1]/preceding-sibling::*) + 1"/>
                <xsl:variable name="position">
                  <xsl:for-each select="$topicTypeCellSpec">
                    <xsl:value-of select="count(preceding-sibling::*) + 1"/>
                  </xsl:for-each>
                </xsl:variable>
                <xsl:if test="not($currPosition = $position)">
                  <xsl:for-each select="ancestor::*[contains(@class, ' map/relrow ')]/*[contains(@class, ' map/relcell ')][position() = $position]//*[contains(@class, ' map/topicref ')]">
                    <xsl:variable name="relatedTopic" select="key('key_anchor',@id)[1]"/>
                    <fo:block xsl:use-attribute-sets="related-links__content">
                      <fo:basic-link internal-destination="{@id}" xsl:use-attribute-sets="xref">
                        <xsl:apply-templates select="$relatedTopic/*[contains(@class,' topic/title ')]" mode="insert-text"/>
                      </fo:basic-link>
                    </fo:block>
                  </xsl:for-each>
                </xsl:if>
              </xsl:if>
            </xsl:when>
            <xsl:otherwise>
              <xsl:for-each select="ancestor::*[contains(@class, ' map/relcell ')][1]/preceding-sibling::*//*[contains(@class, ' map/topicref ')] | ancestor::*[contains(@class, ' map/relcell ')][1]/following-sibling::*//*[contains(@class, ' map/topicref ')]">
                <xsl:variable name="relatedTopic" select="key('key_anchor',@id)[1]"/>
                <xsl:choose>
                  <xsl:when test="$linkType = 'topic'">
                    <xsl:if test="contains($relatedTopic/@class, ' topic/topic ') and not(contains($relatedTopic/@class, ' concept/concept ') or contains($relatedTopic/@class, ' task/task ') or contains($relatedTopic/@class, ' reference/reference '))">
                      <fo:block xsl:use-attribute-sets="related-links__content">
                        <fo:basic-link internal-destination="{@id}" xsl:use-attribute-sets="xref">
                          <xsl:apply-templates select="$relatedTopic/*[contains(@class,' topic/title ')]" mode="insert-text"/>
                        </fo:basic-link>
                      </fo:block>
                    </xsl:if>
                  </xsl:when>
                  <xsl:when test="$linkType = 'task'">
                    <xsl:if test="contains($relatedTopic/@class, ' task/task ')">
                      <fo:block xsl:use-attribute-sets="related-links__content">
                        <fo:basic-link internal-destination="{@id}" xsl:use-attribute-sets="xref">
                          <xsl:apply-templates select="$relatedTopic/*[contains(@class,' topic/title ')]" mode="insert-text"/>
                        </fo:basic-link>
                      </fo:block>
                    </xsl:if>
                  </xsl:when>
                  <xsl:when test="$linkType = 'concept'">
                    <xsl:if test="contains($relatedTopic/@class, ' concept/concept ')">
                      <fo:block xsl:use-attribute-sets="related-links__content">
                        <fo:basic-link internal-destination="{@id}" xsl:use-attribute-sets="xref">
                          <xsl:apply-templates select="$relatedTopic/*[contains(@class,' topic/title ')]" mode="insert-text"/>
                        </fo:basic-link>
                      </fo:block>
                    </xsl:if>
                  </xsl:when>
                  <xsl:when test="$linkType = 'reference'">
                    <xsl:if test="contains($relatedTopic/@class, ' reference/reference ')">
                      <fo:block xsl:use-attribute-sets="related-links__content">
                        <fo:basic-link internal-destination="{@id}" xsl:use-attribute-sets="xref">
                          <xsl:apply-templates select="$relatedTopic/*[contains(@class,' topic/title ')]" mode="insert-text"/>
                        </fo:basic-link>
                      </fo:block>
                    </xsl:if>
                  </xsl:when>
                </xsl:choose>
              </xsl:for-each>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:for-each>
      </xsl:variable>
      <xsl:if test="$resultLinks/*">
        <xsl:if test="$title">
          <fo:block xsl:use-attribute-sets="related-links.title">
            <xsl:value-of select="$title"/>
          </fo:block>
        </xsl:if>
        <xsl:copy-of select="$resultLinks"/>
      </xsl:if>
    </xsl:if>
  </xsl:template>

  <xsl:template name="brokenLinks">
    <xsl:param name="href"/>
    <xsl:call-template name="output-message">
      <xsl:with-param name="msgnum">063</xsl:with-param>
      <xsl:with-param name="msgsev">W</xsl:with-param>
      <xsl:with-param name="msgparams">%1=<xsl:value-of select="$href"/></xsl:with-param>
    </xsl:call-template>
  </xsl:template>

</xsl:stylesheet>
