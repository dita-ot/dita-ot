<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
	Sourceforge.net. See the accompanying license.txt file for 
	applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2005, 2006 All Rights Reserved. -->
<xsl:stylesheet
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:office="urn:oasis:names:tc:opendocument:xmlns:office:1.0"
	xmlns:style="urn:oasis:names:tc:opendocument:xmlns:style:1.0"
	xmlns:text="urn:oasis:names:tc:opendocument:xmlns:text:1.0"
	xmlns:table="urn:oasis:names:tc:opendocument:xmlns:table:1.0"
	xmlns:draw="urn:oasis:names:tc:opendocument:xmlns:drawing:1.0"
	xmlns:fo="urn:oasis:names:tc:opendocument:xmlns:xsl-fo-compatible:1.0"
	xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:dc="http://purl.org/dc/elements/1.1/"
	xmlns:number="urn:oasis:names:tc:opendocument:xmlns:datastyle:1.0"
	xmlns:presentation="urn:oasis:names:tc:opendocument:xmlns:presentation:1.0"
	xmlns:svg="urn:oasis:names:tc:opendocument:xmlns:svg-compatible:1.0"
	xmlns:chart="urn:oasis:names:tc:opendocument:xmlns:chart:1.0"
	xmlns:dr3d="urn:oasis:names:tc:opendocument:xmlns:dr3d:1.0"
	xmlns:math="http://www.w3.org/1998/Math/MathML"
	xmlns:form="urn:oasis:names:tc:opendocument:xmlns:form:1.0"
	xmlns:script="urn:oasis:names:tc:opendocument:xmlns:script:1.0"
	xmlns:dom="http://www.w3.org/2001/xml-events" xmlns:xforms="http://www.w3.org/2002/xforms"
	xmlns:xsd="http://www.w3.org/2001/XMLSchema"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns:anim="urn:oasis:names:tc:opendocument:xmlns:animation:1.0"
	xmlns:smil="urn:oasis:names:tc:opendocument:xmlns:smil-compatible:1.0"
	xmlns:prodtools="http://www.ibm.com/xmlns/prodtools"
	xmlns:dita-ot="http://dita-ot.sourceforge.net/ns/201007/dita-ot"
	xmlns:opentopic-mapmerge="http://www.idiominc.com/opentopic/mapmerge"
	xmlns:exsl="http://exslt.org/common"
	xmlns:exslf="http://exslt.org/functions"
	xmlns:opentopic-func="http://www.idiominc.com/opentopic/exsl/function"
	exclude-result-prefixes="opentopic-mapmerge opentopic-func exslf exsl dita-ot"
	version="1.1">
	
  <xsl:import href="plugin:org.dita.base:xsl/common/output-message.xsl"/>
	<xsl:variable name="msgprefix">DOTX</xsl:variable>
    <!-- 
	<xsl:include href="../../cfg/fo/attrs/links-attr.xsl"/>
    -->
  
  <xsl:key name="link" match="*[contains(@class, ' topic/link ')][not(ancestor::*[contains(@class, ' topic/linklist ')])]" use="concat(ancestor::*[contains(@class, ' topic/related-links ')]/parent::*[contains(@class, ' topic/topic ')]/@id, ' ', @href,@type,@role,@platform,@audience,@importance,@outputclass,@keyref,@scope,@format,@otherrole,@product,@otherprops,@rev,@class,child::*)"/>
	
	<xsl:param name="chapterLayout" select="''"/>
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
            	<xsl:element name="text:p">
            		<xsl:attribute name="text:style-name">Default_20_Text</xsl:attribute>
                    <xsl:apply-templates select="*[contains(@class, ' topic/desc ')]"/>
            	</xsl:element>
            </xsl:when>
            <!-- External: do not attempt to retrieve. -->
            <xsl:when test="$linkScope='external'">
            </xsl:when>
            <!-- When the target has a short description and no local override, use the target -->
            <xsl:when test="$element/*[contains(@class, ' topic/shortdesc ')]">
            	<xsl:element name="text:line-break"/>
            	<xsl:element name="text:tab"/>
            	<xsl:apply-templates select="$element/*[contains(@class, ' topic/shortdesc ')]" mode="dita-ot:text-only"/>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="insertLinkDesc">
        
        <text:line-break>
            <text:tab>
                <xsl:apply-templates select="*[contains(@class,' topic/desc ')]" mode="insert-description"/>
            </text:tab>
        </text:line-break>
            
    </xsl:template>

    <xsl:template match="*[contains(@class,' topic/xref ') or contains(@class, ' topic/link ')]/*[contains(@class,' topic/desc ')]" priority="1"/>
    <xsl:template match="*[contains(@class,' topic/desc ')]" mode="insert-description">
        <xsl:apply-templates/>
    </xsl:template>


    <!-- The insertReferenceTitle template is called from <xref> and <link> and is
         used to build link contents (using full FO syntax, not just the text). -->
    <xsl:template name="insertReferenceTitle">
        <xsl:param name="href"/>
        <xsl:param name="titlePrefix"/>
		<xsl:param name="destination"/>
		<xsl:param name="element"/>
        <xsl:apply-templates select="." mode="insertReferenceTitle">
            <xsl:with-param name="href" select="$href"/>
            <xsl:with-param name="titlePrefix" select="$titlePrefix"/>
            <xsl:with-param name="destination" select="$destination"/>
            <xsl:with-param name="element" select="$element"/>
        </xsl:apply-templates>
    </xsl:template>

    <!-- Process any cross reference or link with author-specified text. 
         The specified text is used as the link text. Added by RDA for
         SourceForge bug 1880097. -->
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
        	<!-- 
            <xsl:call-template name="insertVariable">
                <xsl:with-param name="theVariableID" select="$titlePrefix"/>
            </xsl:call-template>
        	-->
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
        
    	<!-- 
    	<xsl:call-template name="insertVariable">
            <xsl:with-param name="theVariableID" select="'Figure'"/>
            <xsl:with-param name="theParameters">
                <text:span>
                    <xsl:value-of select="count(preceding::*[contains(@class, ' topic/fig ')][child::*[contains(@class, ' topic/title ')]]) + 1"/>
                </text:span>
                <text:h>
                    <xsl:apply-templates select="*[contains(@class, ' topic/title ')]" mode="insert-text"/>
                </text:h>
            </xsl:with-param>
        </xsl:call-template>
    	-->
    	
    	<text:span>
    		<xsl:value-of select="count(preceding::*[contains(@class, ' topic/fig ')][child::*[contains(@class, ' topic/title ')]]) + 1"/>
    	</text:span>
    	<text:h>
    		<xsl:apply-templates select="*[contains(@class, ' topic/title ')]" mode="insert-text"/>
    	</text:h>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/section ')][*[contains(@class, ' topic/title ')]]" mode="retrieveReferenceTitle">
        <xsl:variable name="title">
            <xsl:apply-templates select="*[contains(@class, ' topic/title ')]" mode="insert-text"/>
        </xsl:variable>
        <xsl:value-of select="normalize-space($title)"/>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/table ')][*[contains(@class, ' topic/title ')]]" mode="retrieveReferenceTitle">
        <!-- 
    	<xsl:call-template name="insertVariable">
            <xsl:with-param name="theVariableID" select="'Table'"/>
            <xsl:with-param name="theParameters">
                <text:span>
                    <xsl:value-of select="count(preceding::*[contains(@class, ' topic/table ')][child::*[contains(@class, ' topic/title ')]]) + 1"/>
                </text:span>
                <text:h>
                    <xsl:apply-templates select="*[contains(@class, ' topic/title ')]" mode="insert-text"/>
                </text:h>
            </xsl:with-param>
        </xsl:call-template>
        -->
    	<text:span>
    		<xsl:value-of select="count(preceding::*[contains(@class, ' topic/table ')][child::*[contains(@class, ' topic/title ')]]) + 1"/>
    	</text:span>
    	<text:h>
    		<xsl:apply-templates select="*[contains(@class, ' topic/title ')]" mode="insert-text"/>
    	</text:h>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/li ')]" mode="retrieveReferenceTitle">
        <!-- 
    	<xsl:call-template name="insertVariable">
            <xsl:with-param name="theVariableID" select="'List item'"/>
        </xsl:call-template>
        -->
    	<xsl:element name="text:p">
    		<xsl:attribute name="text:style-name">Default_20_Text</xsl:attribute>
    		<xsl:element name="text:span">
    			<xsl:attribute name="text:style-name">bold</xsl:attribute>
    			<xsl:call-template name="getStringODT">
    				<xsl:with-param name="stringName" select="'List item'"/>
    			</xsl:call-template>
    		</xsl:element>
    	</xsl:element>
    	
    	
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/fn ')]" mode="retrieveReferenceTitle">
		<!-- 
    	<xsl:call-template name="insertVariable">
		    <xsl:with-param name="theVariableID" select="'Foot note'"/>
		</xsl:call-template>
		-->
    	<xsl:element name="text:p">
    		<xsl:attribute name="text:style-name">Default_20_Text</xsl:attribute>
    		<xsl:element name="text:span">
    			<xsl:attribute name="text:style-name">bold</xsl:attribute>
    			<xsl:call-template name="getStringODT">
    				<xsl:with-param name="stringName" select="'Foot note'"/>
    			</xsl:call-template>
    		</xsl:element>
    	</xsl:element>
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

    <xsl:template match="*[contains(@class,' topic/xref ')]">

		<xsl:variable name="destination" select="opentopic-func:getDestinationId(@href)"/>
		<xsl:variable name="element" select="key('key_anchor',$destination)[1]"/>

		<xsl:variable name="referenceTitle">
			<xsl:call-template name="insertReferenceTitle">
				<xsl:with-param name="href" select="@href"/>
				<xsl:with-param name="titlePrefix" select="''"/>
				<xsl:with-param name="destination" select="$destination"/>
				<xsl:with-param name="element" select="$element"/>
			</xsl:call-template>
		</xsl:variable>
    	
    	<xsl:if test="parent::*[contains(@class,' topic/li ')]">
    		<xsl:text disable-output-escaping="yes">&lt;text:p&gt;</xsl:text>
    	</xsl:if>

		<text:a>
			<xsl:call-template name="buildBasicLinkDestination">
				<xsl:with-param name="scope" select="@scope"/>
				<xsl:with-param name="href" select="@href"/>
			</xsl:call-template>

			<xsl:choose>
				<xsl:when test="not(@scope = 'external') and not($referenceTitle = '')">
					<xsl:copy-of select="$referenceTitle"/>
				</xsl:when>
				<xsl:when test="not(@scope = 'external')">
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
		</text:a>

		<!--
				Disable because of the CQ#8102 bug
				<xsl:if test="*[contains(@class,' topic/desc ')]">
					<xsl:call-template name="insertLinkDesc"/>
				</xsl:if>
		-->

		<xsl:if test="not(@scope = 'external') and not($referenceTitle = '') and not($element[contains(@class, ' topic/fn ')])">
            <!-- SourceForge bug 1880097: should not include page number when xref includes author specified text -->
            <xsl:if test="not(processing-instruction()[name()='ditaot'][.='usertext'])">
                <xsl:call-template name="insertPageNumberCitation">
                    <xsl:with-param name="destination" select="$destination"/>
                      <xsl:with-param name="element" select="$element"/>
                  </xsl:call-template>
            </xsl:if>
		</xsl:if>
    	
    	<xsl:if test="parent::*[contains(@class,' topic/li ')]">
    		<xsl:text disable-output-escaping="yes">&lt;/text:p&gt;</xsl:text>
    	</xsl:if>

    </xsl:template>

    <!-- xref to footnote makes a callout. -->
    <xsl:template match="*[contains(@class,' topic/xref ')][@type='fn']" priority="2">
        <xsl:variable name="href-fragment" select="substring-after(@href, '#')"/>
        <xsl:variable name="footnote-target" select="//*[contains(@class, ' topic/fn ')][@id = substring-after($href-fragment, '/')][ancestor::*[contains(@class, ' topic/topic ')][1]/@id = substring-before($href-fragment, '/')]"/>
        <xsl:apply-templates select="$footnote-target" mode="footnote-callout"/>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/fn ')]" mode="footnote-callout">
            <text:tab/>

                <xsl:choose>
                    <xsl:when test="@callout">
                        <xsl:value-of select="@callout"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:number level="any" count="*[contains(@class,' topic/fn ') and not(@callout)]"/>
                    </xsl:otherwise>
                </xsl:choose>

    </xsl:template>

    <xsl:template match="*[contains(@class,' topic/related-links ')]">
      <xsl:if test="normalize-space($includeRelatedLinkRoles)">
            <xsl:variable name="topicType">
                <xsl:for-each select="parent::*">
                    <xsl:call-template name="determineTopicType"/>
                </xsl:for-each>
            </xsl:variable>

            <xsl:variable name="collectedLinks">
                <xsl:apply-templates mode="processLink">
                    <xsl:with-param name="topicType" select="$topicType"/>
                </xsl:apply-templates>
            </xsl:variable>

            <xsl:variable name="linkTextContent">
                <xsl:value-of select="$collectedLinks"/>
            </xsl:variable>

            <xsl:if test="normalize-space($linkTextContent)!=''">
            	<xsl:element name="text:p">
            		<xsl:attribute name="text:style-name">Default_20_Text</xsl:attribute>
                	<!-- 
					<xsl:call-template name="insertVariable">
		    				<xsl:with-param name="theVariableID" select="'Related Links'"/>
			    	</xsl:call-template>
                	-->
                	<xsl:element name="text:span">
                		<xsl:attribute name="text:style-name">bold</xsl:attribute>
                		<xsl:call-template name="getStringODT">
                			<xsl:with-param name="stringName" select="'Related Links'"/>
                		</xsl:call-template>
                	</xsl:element>
            		<!-- 
                	<text:line-break/>
                	<text:tab/>
            		-->
            		<!-- 
					<xsl:copy-of select="$collectedLinks"/>
            		-->
            		<!-- 
            		<xsl:value-of select="$collectedLinks" disable-output-escaping="yes"/>
            		-->
            		<xsl:apply-templates mode="processLink">
            			<xsl:with-param name="topicType" select="$topicType"/>
            		</xsl:apply-templates>
                </xsl:element>
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

    <!-- 20100323: Update to be aware of new includeRelatedLinks parameter.
         Move main processing of link into a mode template; this allows customized
         code to easily match links without the need to copy processing logic. -->
    <xsl:template match="*[contains(@class,' topic/link ')]" mode="processLink">
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
          <xsl:apply-templates select="." mode="processLinks"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:template>

    <xsl:template match="*[contains(@class,' topic/link ')]" mode="processLinks">
		<xsl:variable name="destination" select="opentopic-func:getDestinationId(@href)"/>
		<xsl:variable name="element" select="key('key_anchor',$destination)[1]"/>

		<xsl:variable name="referenceTitle">
            <xsl:call-template name="insertReferenceTitle">
                <xsl:with-param name="href" select="@href"/>
                <xsl:with-param name="titlePrefix" select="''"/>
                <xsl:with-param name="destination" select="$destination"/>
                <xsl:with-param name="element" select="$element"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="linkScope">
            <xsl:call-template name="getLinkScope"/>
        </xsl:variable>

            <!--<xsl:text>&#x2022; </xsl:text>-->
    			<xsl:element name="text:line-break"/>
    			<xsl:element name="text:tab"/>
                <text:a>
                    <xsl:call-template name="buildBasicLinkDestination">
                        <xsl:with-param name="scope" select="$linkScope"/>
                        <xsl:with-param name="href" select="@href"/>
                    </xsl:call-template>
                    <xsl:choose>
                        <xsl:when test="not($linkScope = 'external') and not($referenceTitle = '')">
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
                </text:a>
    
            <xsl:if test="not($linkScope = 'external') and not($referenceTitle = '')">
                <xsl:call-template name="insertPageNumberCitation">
					<xsl:with-param name="destination" select="$destination"/>
					<xsl:with-param name="element" select="$element"/>
				</xsl:call-template>
            </xsl:if>
<!--
            Disable because of the CQ#8102 bug
            <xsl:if test="*[contains(@class, ' topic/desc ')]">
                <xsl:call-template name="insertLinkDesc"/>
            </xsl:if>
-->
            <!-- Previously: skip if linkSope = external. New processing: pass
                 linkscope to the template, let the template decide. -->
            <!--<xsl:if test="not($linkScope = 'external')">-->
                <xsl:call-template name="insertLinkShortDesc">
					<xsl:with-param name="destination" select="$destination"/>
					<xsl:with-param name="element" select="$element"/>
					<xsl:with-param name="linkScope" select="$linkScope"/>
				</xsl:call-template>
            <!--</xsl:if>-->
    </xsl:template>

    <xsl:template name="buildBasicLinkDestination">
        <xsl:param name="scope"/>
        <xsl:param name="href"/>
        <xsl:choose>
            <xsl:when test="(contains(@href, '://') and not(starts-with(@href, 'file://')))
            or starts-with(@href, '/') or $scope = 'external'">
            	<xsl:attribute name="xlink:href">
                    <xsl:value-of select="$href"/>
                </xsl:attribute>
            </xsl:when>
        	<xsl:when test="$scope = 'peer'">
        		<xsl:attribute name="xlink:href">
        			<xsl:value-of select="$href"/>
        		</xsl:attribute>
        	</xsl:when>
        	<xsl:when test="contains($href, '#')">
        		<xsl:attribute name="xlink:href">
        			<xsl:value-of select="$href"/>
        		</xsl:attribute>
        	</xsl:when>       	
            <xsl:otherwise>
            	<xsl:attribute name="xlink:href">
            		<xsl:value-of select="$href"/>
            	</xsl:attribute>
            	<xsl:call-template name="brokenLinks">
            		<xsl:with-param name="href" select="$href"/>
            	</xsl:call-template>
            </xsl:otherwise>
            <!--xsl:otherwise>
                <xsl:attribute name="internal-destination">
                    <xsl:value-of select="opentopic-func:getDestinationId($href)"/>
                </xsl:attribute>
            </xsl:otherwise-->
        </xsl:choose>
    </xsl:template>

    <xsl:template name="insertPageNumberCitation">
        <xsl:param name="isTitleEmpty"/>
        <xsl:param name="destination"/>
        <xsl:param name="element"/>

        <xsl:choose>
            <xsl:when test="not($element) or ($destination = '')"/>
            <xsl:when test="$isTitleEmpty">
            	<!-- 
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
            	-->
            	<text:s/>
            	<xsl:call-template name="getStringODT">
            		<xsl:with-param name="stringName" select="'Page'"/>
            	</xsl:call-template>
            	<text:s/>
            	<xsl:element name="text:bookmark-ref">
            		<xsl:attribute name="text:reference-format">page</xsl:attribute>
            		<xsl:attribute name="text:ref-name"><xsl:value-of select="$destination"/></xsl:attribute>
            	</xsl:element>
            </xsl:when>
            <xsl:otherwise>
            	<!-- 
                    <xsl:call-template name="insertVariable">
                        <xsl:with-param name="theVariableID" select="'On the page'"/>
                        <xsl:with-param name="theParameters">
                        	<xsl:element name="text:bookmark-ref">
                        		<xsl:attribute name="text:reference-format">page</xsl:attribute>
                        		<xsl:attribute name="text:ref-name"><xsl:value-of select="$destination"/></xsl:attribute>
                        	</xsl:element>
                        </xsl:with-param>
                    </xsl:call-template>
            	-->
            	<text:s/>
            	<xsl:call-template name="getStringODT">
            		<xsl:with-param name="stringName" select="'On the page'"/>
            	</xsl:call-template>
            	<text:s/>
            	<xsl:element name="text:bookmark-ref">
            		<xsl:attribute name="text:reference-format">page</xsl:attribute>
            		<xsl:attribute name="text:ref-name"><xsl:value-of select="$destination"/></xsl:attribute>
            	</xsl:element>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="*[contains(@class,' topic/linktext ')]" mode="processLink">
    	<xsl:call-template name="get-ascii">
    		<xsl:with-param name="txt">
    			<xsl:value-of select="*[contains(@class,' topic/linktext ')]"/>
    		</xsl:with-param>
    	</xsl:call-template>
    </xsl:template>

    <xsl:template match="*[contains(@class, ' topic/linklist ')]" mode="processLink">
        <text:line-break/>
        <xsl:apply-templates mode="processLink"/>
    </xsl:template>

	<xsl:template match="*[contains(@class,' topic/linkinfo ')]" mode="processLink">
		<text:line-break/>
            <xsl:apply-templates/>
    </xsl:template>

	<xsl:template match="*[contains(@class,' topic/linkpool ')]" mode="processLink">
        <xsl:param name="topicType"/>
        	<xsl:apply-templates mode="processLink">
                <xsl:with-param name="topicType" select="$topicType"/>
            </xsl:apply-templates>
    </xsl:template>

    <exslf:function name="opentopic-func:getDestinationId">
        <xsl:param name="href"/>
        <xsl:variable name="destination">
            <xsl:call-template name="getDestinationIdImpl">
                <xsl:with-param name="href" select="$href"/>
            </xsl:call-template>
        </xsl:variable>
        <exslf:result select="$destination"/>
    </exslf:function>

    <xsl:function version="2.0" name="opentopic-func:getDestinationId">
        <xsl:param name="href"/>
        <xsl:call-template name="getDestinationIdImpl">
            <xsl:with-param name="href" select="$href"/>
        </xsl:call-template>
    </xsl:function>

    <xsl:template name="getDestinationIdImpl">
        <xsl:param name="href"/>
        
        <xsl:variable name="topic-id">
            <xsl:value-of select="substring-after($href, '#')"/>
        </xsl:variable>

        <xsl:variable name="element-id">
            <xsl:value-of select="substring-after($topic-id, '/')"/>
        </xsl:variable>

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

	<xsl:template name="buildRelationships">
        <xsl:apply-templates select="." mode="buildRelationships"/>
    </xsl:template>
    <xsl:template match="*" mode="buildRelationships">
<!--
		<xsl:param name="context" select="."/>
		<xsl:for-each select=".">

		</xsl:for-each>
-->
    <xsl:if test="normalize-space($includeRelatedLinkRoles)">
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
				<!-- 
				<xsl:call-template name="insertVariable">
					<xsl:with-param name="theVariableID" select="'Related concepts'"/>
				</xsl:call-template>
				-->
				<xsl:element name="text:p">
					<xsl:attribute name="text:style-name">Default_20_Text</xsl:attribute>
					<xsl:element name="text:span">
						<xsl:attribute name="text:style-name">bold</xsl:attribute>
						<xsl:call-template name="getStringODT">
							<xsl:with-param name="stringName" select="'Related concepts'"/>
						</xsl:call-template>
					</xsl:element>
				</xsl:element>
			</xsl:variable>
			<xsl:variable name="relatedTasksTitle">
				<!-- 
				<xsl:call-template name="insertVariable">
					<xsl:with-param name="theVariableID" select="'Related tasks'"/>
				</xsl:call-template>
				-->
				<xsl:element name="text:p">
					<xsl:attribute name="text:style-name">Default_20_Text</xsl:attribute>
					<xsl:element name="text:span">
						<xsl:attribute name="text:style-name">bold</xsl:attribute>
						<xsl:call-template name="getStringODT">
							<xsl:with-param name="stringName" select="'Related tasks'"/>
						</xsl:call-template>
					</xsl:element>
				</xsl:element>
			</xsl:variable>
			<xsl:variable name="relatedReferencesTitle">
				<!-- 
				<xsl:call-template name="insertVariable">
					<xsl:with-param name="theVariableID" select="'Related references'"/>
				</xsl:call-template>
				-->
				<xsl:element name="text:p">
					<xsl:attribute name="text:style-name">Default_20_Text</xsl:attribute>
					<xsl:element name="text:span">
						<xsl:attribute name="text:style-name">bold</xsl:attribute>
						<xsl:call-template name="getStringODT">
							<xsl:with-param name="stringName" select="'Related references'"/>
						</xsl:call-template>
					</xsl:element>
				</xsl:element>
			</xsl:variable>
			<xsl:variable name="relatedInformationTitle">
				<!-- 
				<xsl:call-template name="insertVariable">
					<xsl:with-param name="theVariableID" select="'Related information'"/>
				</xsl:call-template>
				-->
				<xsl:element name="text:p">
					<xsl:attribute name="text:style-name">Default_20_Text</xsl:attribute>
					<xsl:element name="text:span">
						<xsl:attribute name="text:style-name">bold</xsl:attribute>
						<xsl:call-template name="getStringODT">
							<xsl:with-param name="stringName" select="'Related information'"/>
						</xsl:call-template>
					</xsl:element>
				</xsl:element>
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
					<xsl:variable name="siblingConcepts" select="preceding-sibling::*[contains(@class, ' concept/concept ') and not(contains(@class, ' bkinfo/bkinfo '))] | following-sibling::*[contains(@class, ' concept/concept ') and not(contains(@class, ' bkinfo/bkinfo '))]"/>
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
					<xsl:variable name="siblingTasks" select="preceding-sibling::*[contains(@class, ' task/task ') and not(contains(@class, ' bkinfo/bkinfo '))] | following-sibling::*[contains(@class, ' task/task ') and not(contains(@class, ' bkinfo/bkinfo '))]"/>
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
					<xsl:variable name="siblingReferences" select="preceding-sibling::*[contains(@class, ' reference/reference ') and not(contains(@class, ' bkinfo/bkinfo '))] | following-sibling::*[contains(@class, ' reference/reference ') and not(contains(@class, ' bkinfo/bkinfo '))]"/>
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
					<xsl:variable name="siblingTopics" select="preceding-sibling::*[contains(@class, ' topic/topic ') and not(contains(@class, ' bkinfo/bkinfo ') or contains(@class, ' concept/concept ') or contains(@class, ' task/task ') or contains(@class, ' reference/reference '))] | following-sibling::*[contains(@class, ' topic/topic ') and not(contains(@class, ' bkinfo/bkinfo ') or contains(@class, ' concept/concept ') or contains(@class, ' task/task ') or contains(@class, ' reference/reference '))]"/>
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
		</xsl:if>
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
			<!-- 
			<xsl:call-template name="insertVariable">
				<xsl:with-param name="theVariableID" select="'Parent topic'"/>
			</xsl:call-template>
			-->
			<xsl:element name="text:p">
				<xsl:attribute name="text:style-name">Default_20_Text</xsl:attribute>
				<xsl:element name="text:span">
					<xsl:attribute name="text:style-name">bold</xsl:attribute>
					<xsl:call-template name="getStringODT">
						<xsl:with-param name="stringName" select="'Parent topic'"/>
					</xsl:call-template>
				</xsl:element>
			</xsl:element>
		</xsl:variable>
		<xsl:call-template name="createMapLinks">
			<xsl:with-param name="nodeSet" select="parent::*[contains(@class, ' topic/topic ')]"/>
			<xsl:with-param name="title" select="$linksTitle"/>
		</xsl:call-template>
	</xsl:template>

	<xsl:template name="linkToNext">
		<!-- Creating relationships to the next sibling -->
		<xsl:variable name="linksTitle">
			<!-- 
			<xsl:call-template name="insertVariable">
				<xsl:with-param name="theVariableID" select="'Next topic'"/>
			</xsl:call-template>
			-->
			<xsl:element name="text:p">
				<xsl:attribute name="text:style-name">Default_20_Text</xsl:attribute>
				<xsl:element name="text:span">
					<xsl:attribute name="text:style-name">bold</xsl:attribute>
					<xsl:call-template name="getStringODT">
						<xsl:with-param name="stringName" select="'Next topic'"/>
					</xsl:call-template>
				</xsl:element>
			</xsl:element>
		</xsl:variable>
		<xsl:call-template name="createMapLinks">
			<xsl:with-param name="nodeSet" select="following-sibling::*[contains(@class, ' topic/topic ')][1]"/>
			<xsl:with-param name="title" select="$linksTitle"/>
		</xsl:call-template>
	</xsl:template>

	<xsl:template name="linkToPrevious">
		<!-- Creating relationships to the previous sibling -->
		<xsl:variable name="linksTitle">
			<!-- 
			<xsl:call-template name="insertVariable">
				<xsl:with-param name="theVariableID" select="'Previous topic'"/>
			</xsl:call-template>
			-->
			<xsl:element name="text:p">
				<xsl:attribute name="text:style-name">Default_20_Text</xsl:attribute>
				<xsl:element name="text:span">
					<xsl:attribute name="text:style-name">bold</xsl:attribute>
					<xsl:call-template name="getStringODT">
						<xsl:with-param name="stringName" select="'Previous topic'"/>
					</xsl:call-template>
				</xsl:element>
			</xsl:element>
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
			<!-- 
			<xsl:call-template name="insertVariable">
				<xsl:with-param name="theVariableID" select="'Child topics'"/>
			</xsl:call-template>
			-->
			<xsl:element name="text:p">
				<xsl:attribute name="text:style-name">Default_20_Text</xsl:attribute>
				<xsl:element name="text:span">
					<xsl:attribute name="text:style-name">bold</xsl:attribute>
					<xsl:call-template name="getStringODT">
						<xsl:with-param name="stringName" select="'Child topics'"/>
					</xsl:call-template>
				</xsl:element>
			</xsl:element>
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
						<xsl:element name="text:p">
							<xsl:attribute name="text:style-name">Default_20_Text</xsl:attribute>
							<xsl:for-each select="$nodeSet">
									<text:a xlink:href="concat('#', @id)">
										<xsl:apply-templates select="child::*[contains(@class, ' topic/title ')]" mode="insert-text"/>
									</text:a>
								<text:line-break/>
							</xsl:for-each>
						</xsl:element>
					</xsl:when>
				</xsl:choose>
			</xsl:if>
		</xsl:variable>

		<xsl:if test="($linkNodes//text:list) or ($linkNodes//text:p)">
			<xsl:if test="$title">
				<xsl:value-of select="$title"/>
			</xsl:if>
			<xsl:copy-of select="$linkNodes"/>
		</xsl:if>
	</xsl:template>

	<xsl:template name="createMapLinksUnordered">
		<xsl:param name="nodeSet"/>

		<text:list text:style-name="list_style">
			<xsl:for-each select="$nodeSet">
				<text:list-item>
						<!-- 
							<xsl:call-template name="insertVariable">
							<xsl:with-param name="theVariableID" select="'Unordered List bullet'"/>
							</xsl:call-template>
						-->
						<xsl:element name="text:p">
							<xsl:attribute name="text:style-name">Default_20_Text</xsl:attribute>
							<xsl:element name="text:span">
								<xsl:attribute name="text:style-name">bold</xsl:attribute>
								<xsl:call-template name="getStringODT">
									<xsl:with-param name="stringName" select="'Unordered List bullet'"/>
								</xsl:call-template>
							</xsl:element>
						</xsl:element>
						<xsl:element name="text:p">
							<xsl:attribute name="text:style-name">Default_20_Text</xsl:attribute>
							<text:a xlink:href="concat('#' , @id)">
								<xsl:apply-templates select="child::*[contains(@class, ' topic/title ')]" mode="insert-text"/>
							</text:a>
						</xsl:element>
				</text:list-item>
			</xsl:for-each>
		</text:list>
	</xsl:template>

	<xsl:template name="createMapLinksOrdered">
		<xsl:param name="nodeSet"/>

		<text:list text:style-nam="ordered_list_style">
			<xsl:for-each select="$nodeSet">
				<text:list-item>
					
						<xsl:element name="text:p">
							<xsl:attribute name="text:style-name">Default_20_Text</xsl:attribute>
							<xsl:value-of select="count(preceding-sibling::*[contains(@class,' topic/topic ')])"/>
						</xsl:element>
					
						<xsl:element name="text:p">
							<xsl:attribute name="text:style-name">Default_20_Text</xsl:attribute>
							<text:a xlink:href="concat('#', @id)">
								<xsl:apply-templates select="child::*[contains(@class, ' topic/title ')]" mode="insert-text"/>
							</text:a>
						</xsl:element>
					
				</text:list-item>
			</xsl:for-each>
		</text:list>
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
										<xsl:element name="text:p">
											<xsl:attribute name="text:style-name">Default_20_Text</xsl:attribute>
											<text:a xlink:href="concat('#', $id)">
												<xsl:apply-templates select="$relatedTopic/*[contains(@class,' topic/title ')]" mode="insert-text"/>
											</text:a>
										</xsl:element>
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
											<xsl:element name="text:p">
												<xsl:attribute name="text:style-name">Default_20_Text</xsl:attribute>
												<text:a xlink:href="concat('#', $id)">
													<xsl:apply-templates select="$relatedTopic/*[contains(@class,' topic/title ')]" mode="insert-text"/>
												</text:a>
											</xsl:element>
										</xsl:if>
									</xsl:when>
									<xsl:when test="$linkType = 'task'">
										<xsl:if test="contains($relatedTopic/@class, ' task/task ')">
											<xsl:element name="text:p">
												<xsl:attribute name="text:style-name">Default_20_Text</xsl:attribute>
												<text:a xlink:href="concat('#', $id)">
													<xsl:apply-templates select="$relatedTopic/*[contains(@class,' topic/title ')]" mode="insert-text"/>
												</text:a>
											</xsl:element>
										</xsl:if>
									</xsl:when>
									<xsl:when test="$linkType = 'concept'">
										<xsl:if test="contains($relatedTopic/@class, ' concept/concept ')">
											<xsl:element name="text:p">
												<xsl:attribute name="text:style-name">Default_20_Text</xsl:attribute>
												<text:a xlink:href="concat('#', $id)">
													<xsl:apply-templates select="$relatedTopic/*[contains(@class,' topic/title ')]" mode="insert-text"/>
												</text:a>
											</xsl:element>
										</xsl:if>
									</xsl:when>
									<xsl:when test="$linkType = 'reference'">
										<xsl:if test="contains($relatedTopic/@class, ' reference/reference ')">
											<xsl:element name="text:p">
												<xsl:attribute name="text:style-name">Default_20_Text</xsl:attribute>
												<text:a xlink:href="concat('#', $id)">
													<xsl:apply-templates select="$relatedTopic/*[contains(@class,' topic/title ')]" mode="insert-text"/>
												</text:a>
											</xsl:element>
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
					<xsl:element name="text:p">
						<xsl:attribute name="text:style-name">Default_20_Text</xsl:attribute>
						<xsl:value-of select="$title"/>
					</xsl:element>
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
	
	<!--create prerequisite links with all dups eliminated. -->
	<!-- Omit prereq links from unordered related-links (handled by mode="prereqs" template). -->
	<xsl:key name="omit-from-unordered-links" match="*[@importance='required' and (not(@role) or @role='sibling' or @role='friend' or @role='cousin')]" use="1"/>
	<xsl:template match="*[contains(@class,' topic/related-links ')]" mode="prereqs">
		
		<!--if there are any prereqs create a list with dups-->
		<xsl:if test="descendant::*[contains(@class, ' topic/link ')][not(ancestor::*[contains(@class, ' topic/linklist ')])][@importance='required' and (not(@role) or @role='sibling' or @role='friend' or @role='previous' or @role='cousin')]">
			<xsl:element name="text:p">
				<xsl:element name="text:span">
					<xsl:attribute name="text:style-name">default_text_style</xsl:attribute>
					<xsl:call-template name="getStringODT">
						<xsl:with-param name="stringName" select="'Prerequisites'"/>
					</xsl:call-template>
				</xsl:element>
			</xsl:element>
			
			<!--only create link if there is an href, its importance is required, and the role is compatible (don't want a prereq showing up for a "next" or "parent" link, for example) - remove dups-->
			<xsl:apply-templates mode="prereqs" select="descendant::*[generate-id(.)=generate-id(key('link',concat(ancestor::*[contains(@class, ' topic/related-links ')]/parent::*[contains(@class, ' topic/topic ')]/@id, ' ', @href,@type,@role,@platform,@audience,@importance,@outputclass,@keyref,@scope,@format,@otherrole,@product,@otherprops,@rev,@class,child::*))[1])]
				[@href]
				[@importance='required' and (not(@role) or @role='sibling' or @role='friend' or @role='previous' or @role='cousin')]
				[not(ancestor::*[contains(@class, ' topic/linklist ')])]"/>
		</xsl:if>
	</xsl:template>
	
	<xsl:template mode="prereqs" match="*[contains(@class, ' topic/link ')]" priority="2">
		
		<!-- Allow for unknown metadata (future-proofing) -->
		<xsl:apply-templates select="*[contains(@class,' topic/data ') or contains(@class,' topic/foreign ')]"/>
		<xsl:variable name="samefile">
			<xsl:call-template name="check_file_location"/>
		</xsl:variable>
		<xsl:variable name="href-value">
			<xsl:call-template name="format_href_value"/>
		</xsl:variable>
		<xsl:element name="text:p">
			<xsl:attribute name="text:style-name">Default_20_Text</xsl:attribute>
			<xsl:call-template name="create_related_links">
				<xsl:with-param name="samefile" select="$samefile"/>
				<xsl:with-param name="href-value" select="$href-value"/>
			</xsl:call-template>
		</xsl:element>
	</xsl:template>

</xsl:stylesheet>
