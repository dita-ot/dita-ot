<?xml version="1.0" encoding="UTF-8" ?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
  Sourceforge.net. See the accompanying license.txt file for 
  applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->
<!--
  Fixes: add "-" to start of class attribute on generated elements (<linktext>, <desc>)
         links to elements inside abstract do not retrieve text
         Get link text for <link> elements that include <desc> but no link text
         Get link text for <xref> elements that include <desc> but no link text
         Get short description for <xref> elements that contain link text but no <desc>
         Fig and table numbering is now specialization aware
         XREF to dlentry looked for <dlterm>, should be <dt>
         No space between "Figure" and number in "Figure 1" reference
         No space between "Table" and number in "Table 1" reference
         Reference to table without @type used a title instead of Table N; now uses Table N to
             be consistent with typed reference and with figures
         Function to determine class had collapsed <xsl:text> </xsl:text> into <xsl:text/>,
             this caused the previous bug with tables
         Wrapping an </xsl:otherwise> to a newline added many spaces to '#none#' in some cases;
             resulted in type attributes getting set to "#none#&#xA;          &#xA;        "
         Hungarian references to figure and table should use Hungarian rules
         draft-comment and required-cleanup were pulled in to link text and hover help
         Shortdesc fixes:
         - If an element within a topic is the target, only look at that element for a desc (not the topic)
         - If a file has many topics, do not pull every shortdesc when targeting a topic
         - If a target topic uses abstract, add a space between shortdesc's in the abstract
         - If the target topic does not have a shortdesc, do not fall back to shortdesc from another topic
-->
<!-- Refactoring completed March and April 2007. The code now contains 
     numerous hooks that can be overridden using modes. Most noteworthy:
mode="topicpull:inherit-attribute"
     Can be used to selectively modify how attributes are inherited on a specific element
mode="topicpull:get-stuff_get-linktext" and mode="topicpull:get-stuff_get-shortdesc"
     Can be used to determine how link text or shortdesc are retrieved for some types of references
mode="topicpull:getlinktext" and more specific modes "topicpull:getlinktext_*"
     Can be used to modify retrieved link text for specific types of elements
mode="topicpull:figure-linktext" and mode="topicpull:table-linktext"
     Can be used to modify link style for table and figure references; can
     also be used to add support for languages that use different orders.
     Note also the TABLELINK and FIGURELINK parameters.
     -->
<!-- 20090903 RDA: added <?ditaot gentext?> and <?ditaot linktext?> PIs for RFE 1367897.
                   Allows downstream processes to identify original text vs. generated link text. -->
<!-- 20160421 WEK: Refactored to do topic and element lookup once, use XSLT 2 techniques, generally
                   simplify and streamline the code to ensure correct behavior for all cases.
                   Use key lookup to resolve topic and non-topic element references.
                   Fixes issue when topic and referenced subelement have the same ID.
                   Added new runtime parameter orderedListNumberFormat
  --> 
<xsl:stylesheet version="2.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:dita-ot="http://dita-ot.sourceforge.net/ns/201007/dita-ot"
                xmlns:topicpull="http://dita-ot.sourceforge.net/ns/200704/topicpull"
                xmlns:ditamsg="http://dita-ot.sourceforge.net/ns/200704/ditamsg"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                exclude-result-prefixes="dita-ot topicpull ditamsg xs">
  <xsl:import href="../common/dita-utilities.xsl"/>
  <xsl:import href="../common/output-message.xsl"/>
  <xsl:import href="../common/dita-textonly.xsl"/>
  <!-- Define the error message prefix identifier -->
  <!-- Deprecated since 2.3 -->
  <xsl:variable name="msgprefix">DOTX</xsl:variable>

  <xsl:param name="DBG" select="'no'"/>

  <!-- Set the format for generated text for links to tables and figures.   -->
  <!-- Recognized values are 'NUMBER' (Table 5) and 'TITLE' (Table Caption) -->
  <xsl:param name="TABLELINK">NUMBER</xsl:param>
  <xsl:param name="FIGURELINK">NUMBER</xsl:param>
  
  <!-- Check whether the onlytopicinmap is turned on -->
  <xsl:param name="ONLYTOPICINMAP" select="'false'"/>
  
  <!-- Enable specifying number list format (used to generate number
       for referenced list items during link text construction.
       
       NOTE: This needs to be coordinated with final-form transforms
       so that the generated numbers and the link text match.
    -->
  <xsl:param name="orderedListNumberFormat" as="xs:string"
    select="'1.a.i.1.a.i.1.a.i'"
  />
  
  <!-- Make internal variable from parameter (enables checking and massaging
       specified value as needed).
    -->
  <xsl:variable name="dita-ot:ordered-list-number-format" 
    as="xs:string" 
    select="$orderedListNumberFormat"
  />
  
  <!-- Establish keys for the counting of figures, tables, and anything else -->
  <!-- To remove something from the figure count, create the same key in an override.
       Match all items to be excluded. Set the use attribute to 'exclude'. -->
  <xsl:key name="count.topic.fig"
           match="*[contains(@class, ' topic/fig ')][*[contains(@class, ' topic/title ')]]"
           use="'include'"/>
  <xsl:key name="count.topic.table"
           match="*[contains(@class, ' topic/table ')][*[contains(@class, ' topic/title ')]]"
           use="'include'"/>
  
  <!-- All elements by ID -->
  <xsl:key name="id" match="*[@id]" use="@id"/>
  <!-- Topic elements by ID. -->
  <xsl:key name="topic" match="*[@id][contains(@class, ' topic/topic ')]" use="@id"/>
  
  <!-- Process a link in the related-links section. Retrieve link text, type, and
       description if possible (and not already specified locally). -->
  <xsl:template match="*[contains(@class, ' topic/link ')]">    
    <xsl:if test="@href=''">
      <xsl:apply-templates select="." mode="ditamsg:empty-href"/>
    </xsl:if>

    <xsl:copy>
      <!--copy existing explicit attributes-->
      <xsl:apply-templates select="@*"/>
      <xsl:variable name="type" as="xs:string">
        <xsl:call-template name="topicpull:inherit">
          <xsl:with-param name="attrib" select="'type'" as="xs:string"/>
        </xsl:call-template>
      </xsl:variable>
      <xsl:variable name="format" as="xs:string">
        <xsl:call-template name="topicpull:inherit">
          <xsl:with-param name="attrib" select="'format'" as="xs:string"/>
        </xsl:call-template>
      </xsl:variable>
      <xsl:variable name="scope" as="xs:string">
        <xsl:call-template name="topicpull:inherit">
          <xsl:with-param name="attrib" select="'scope'" as="xs:string"/>
        </xsl:call-template>
      </xsl:variable>

      <xsl:if test="not(@type) and $type!='#none#'">
        <xsl:attribute name="type" select="$type"/>
      </xsl:if>
      <xsl:if test="not(@format) and $format!='#none#'">
        <xsl:attribute name="format" select="$format"/>
      </xsl:if>
      <xsl:if test="not(@scope) and $scope!='#none#'">
        <xsl:attribute name="scope" select="$scope"/>
      </xsl:if>

      <xsl:if test="not(@importance)">
        <xsl:apply-templates select="." mode="topicpull:inherit-and-set-attribute">
          <xsl:with-param name="attrib"  select="'importance'" as="xs:string"/>
        </xsl:apply-templates>
      </xsl:if>
      <xsl:if test="not(@role)">
        <xsl:apply-templates select="." mode="topicpull:inherit-and-set-attribute">
          <xsl:with-param name="attrib" select="'role'" as="xs:string"/>
        </xsl:apply-templates>
      </xsl:if>

      <xsl:choose>
        <xsl:when test="@type and *[contains(@class, ' topic/linktext ')] and *[contains(@class, ' topic/desc ')]">
          <xsl:apply-templates select="." mode="topicpull:add-usertext-PI"/>
          <xsl:apply-templates/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:choose>
            <xsl:when test="@href=''"/>
            <xsl:when test="@href">
              <xsl:apply-templates select="." mode="topicpull:get-stuff">
                <xsl:with-param name="localtype" select="$type" as="xs:string"/>
                <xsl:with-param name="scope" select="$scope" as="xs:string"/>
                <xsl:with-param name="format" select="$format" as="xs:string"/>
              </xsl:apply-templates>
            </xsl:when>
            <xsl:otherwise>
              <xsl:apply-templates select="." mode="ditamsg:missing-href"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:copy>
  </xsl:template>

  <!-- 2007.03.13: Update inheritance to check specific elements and attributes.
       Similar to the inheritance template in mappull, except that it stops at related links. -->
  <xsl:template name="topicpull:inherit">
    <xsl:param name="attrib" as="xs:string"/>
    <xsl:apply-templates select="." mode="topicpull:inherit-from-self-then-ancestor">
      <xsl:with-param name="attrib" select="$attrib" as="xs:string"/>
    </xsl:apply-templates>
  </xsl:template>

  <!-- Match the attribute which we are trying to inherit -->
  <xsl:template match="@*" mode="topicpull:inherit-attribute">
    <xsl:value-of select="."/>
  </xsl:template>

  <!-- If an attribute is specified locally, set it. Otherwise, try to inherit from ancestors. -->
  <xsl:template match="*" mode="topicpull:inherit-from-self-then-ancestor">
    <xsl:param name="attrib" as="xs:string"/>
    <xsl:choose>
      <xsl:when test="@*[local-name()=$attrib]">
        <xsl:value-of select="@*[local-name()=$attrib]"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates select="." mode="topicpull:inherit-attribute">
          <xsl:with-param name="attrib" select="$attrib" as="xs:string"/>
        </xsl:apply-templates>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!-- Match an element when trying to inherit an attribute. Put the value of the attribute in $attrib-here.
         * If the attribute is present and should be used ($attrib=here!=''), then use it
         * If we are at the related-links element, attribute can't be inherited, so return #none#
         * Anything else, move on to parent
         -->
  <xsl:template match="*" mode="topicpull:inherit-attribute">
    <xsl:param name="attrib"/>
    <xsl:variable name="attrib-here">
      <xsl:apply-templates select="@*[local-name()=$attrib]" mode="topicpull:inherit-attribute"/>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$attrib-here!=''">
        <xsl:value-of select="$attrib-here"/>
      </xsl:when>
      <xsl:when test="contains(@class,' topic/related-links ')">
        <xsl:text>#none#</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates select="parent::*" mode="topicpull:inherit-attribute">
          <xsl:with-param name="attrib" select="$attrib" as="xs:string"/>
        </xsl:apply-templates>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!-- Similar to the template above, but saves duplicated processing by setting the
       attribute when the inherited value != #none# -->
  <xsl:template match="*" mode="topicpull:inherit-and-set-attribute">
    <xsl:param name="attrib"/>
    <xsl:variable name="inherited-value">
      <xsl:apply-templates select="." mode="topicpull:inherit-from-self-then-ancestor">
        <xsl:with-param name="attrib" select="$attrib" as="xs:string"/>
      </xsl:apply-templates>
    </xsl:variable>
    <xsl:if test="$inherited-value!='#none#'">
      <xsl:attribute name="{$attrib}" select="$inherited-value"/>
    </xsl:if>
  </xsl:template>
  
  <!-- Process an in-line cross reference. Retrieve link text, type, and
       description if possible (and not already specified locally). -->
  <xsl:template match="*[dita-ot:is-link(.)]">
    
    <xsl:choose>
      <xsl:when test="normalize-space(@href)='' or not(@href)">
        <xsl:if test="not(@keyref)">
          <xsl:apply-templates select="." mode="ditamsg:empty-href"/>
        </xsl:if>
        <xsl:copy>
          <xsl:apply-templates select="@*"/>
          <xsl:apply-templates select="." mode="topicpull:add-usertext-PI"/>
          <xsl:apply-templates select="node()"/>
        </xsl:copy>
      </xsl:when>
      <xsl:when test="(text()|*[not(contains(@class,' topic/desc '))]) and *[contains(@class,' topic/desc ')]">
        <xsl:copy>
          <xsl:apply-templates select="@*"/>
          <xsl:apply-templates select="." mode="topicpull:add-usertext-PI"/>
          <xsl:apply-templates select="node()"/>
        </xsl:copy>
      </xsl:when>
      <xsl:when test="@href and not(@href='')">
        <xsl:copy>
          <xsl:apply-templates select="@*"/>
          <xsl:variable name="type" as="xs:string"
            select="if (@type) then @type else '#none#'"
          />
          <xsl:variable name="format" as="xs:string"
            select="if (@format) then @format else '#none#'"
          />
          <xsl:variable name="scope" as="xs:string"
            select="if (@scope) then @scope else '#none#'"
          />
          <xsl:apply-templates select="." mode="topicpull:get-stuff">
            <xsl:with-param name="localtype" select="$type" as="xs:string"/>
            <xsl:with-param name="scope" select="$scope" as="xs:string"/>
            <xsl:with-param name="format" select="$format" as="xs:string"/>
          </xsl:apply-templates>
        </xsl:copy>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates select="." mode="ditamsg:missing-href"/>
        <xsl:copy>
          <xsl:apply-templates select="@*"/>
          <xsl:apply-templates select="." mode="topicpull:add-usertext-PI"/>
          <xsl:apply-templates select="node()"/>
        </xsl:copy>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Verify that a locally specified type attribute matches the determined target type.
       If it does not, generate a message. -->
  <xsl:template match="*" mode="topicpull:verify-type-attribute">
    <xsl:param name="type" as="xs:string?"/>            <!-- Type value specified on the link -->
    <xsl:param name="actual-class" as="xs:string"/>    <!-- Class value of the target element -->
    <xsl:param name="actual-name" as="xs:string"/>     <!-- Name of the target element -->
    <xsl:param name="targetting" as="xs:string"/>      <!-- Targetting a "topic" or "element" -->
    <xsl:choose>
      <!-- The type is correct; concept typed as concept, newtype defined as newtype -->
      <xsl:when test="$type=$actual-name"/>
      <xsl:when test="($targetting='topic' and contains($actual-class,concat(' ',$type,'/',$type,' '))) or
                      ($targetting='element' and contains($actual-class,concat('/',$type,' ')))">
        <xsl:apply-templates select="." mode="ditamsg:type-attribute-not-specific">
          <xsl:with-param name="targetting" select="$targetting"/>
          <xsl:with-param name="type" select="$type" as="xs:string?"/>
          <xsl:with-param name="actual-name" select="$actual-name"/>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates select="." mode="ditamsg:type-attribute-incorrect">
          <xsl:with-param name="targetting" select="$targetting"/>
          <xsl:with-param name="type" select="$type" as="xs:string?"/>
          <xsl:with-param name="actual-name" select="$actual-name"/>
        </xsl:apply-templates>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Get link text, type, and short description for a link or cross reference.
       If specified locally, use the local value, otherwise retrieve from target. -->
  <xsl:template match="*" mode="topicpull:get-stuff">
    <xsl:param name="localtype" as="xs:string"/>
    <xsl:param name="scope" as="xs:string"/>
    <xsl:param name="format" as="xs:string"/>
    
    <xsl:variable name="baseDocUrl" as="xs:string">
      <xsl:apply-templates select="." mode="topicpull:get-stuff_docurl"/>
    </xsl:variable>
    <xsl:variable name="docUrl" as="xs:string"
      select="replace($baseDocUrl, ' ', '%20')"
    />    
    
    <!-- $doc will be empty sequence if doc is not resolved. 
    
         The get-stuff_verify-target-present template reports inability to resolve the file
         reference.
    -->
    <xsl:variable name="doc" as="document-node()?">
      <xsl:apply-templates select="." mode="topicpull:get-target-document">
        <xsl:with-param name="scope" select="$scope" as="xs:string"/>
        <xsl:with-param name="format" select="$format" as="xs:string"/>
        <xsl:with-param name="docUrl" select="$docUrl" as="xs:string?"/>
      </xsl:apply-templates>
    </xsl:variable>
    
    <xsl:variable name="targetTopicid" as="xs:string">
      <xsl:apply-templates select="." mode="topicpull:get-stuff_topicid"/>
    </xsl:variable>
    <!-- Topic will be () if there's no doc or the ID can't be resolved. -->
    <xsl:variable name="targetTopic" as="element()?"
      select="if ($doc) 
                 then dita-ot:get-topic-for-doc-and-id($doc, $targetTopicid) else ()"
    />
    <xsl:variable name="elemid" as="xs:string">
      <xsl:apply-templates select="." mode="topicpull:get-stuff_elemid"/>
    </xsl:variable>
    <xsl:variable name="targetElement" as="element()?"
      select="dita-ot:get-element-in-topic-by-id($targetTopic, $elemid)"
    />
    
    <xsl:variable name="type" as="xs:string?">
      <xsl:apply-templates select="." mode="topicpull:get-stuff_get-type">
        <xsl:with-param name="localtype" select="$localtype"/>
        <xsl:with-param name="scope" select="$scope"/>
        <xsl:with-param name="format" select="$format"/>
        <xsl:with-param name="targetTopic" as="element()?" select="$targetTopic"/>
        <xsl:with-param name="targetElement" as="element()?" select="$targetElement"/>
      </xsl:apply-templates>
    </xsl:variable>
    
    <xsl:if test="$localtype='#none#' and not($type='#none#')">
      <xsl:attribute name="type" select="$type"/>
    </xsl:if>

    <xsl:apply-templates select="." mode="topicpull:get-stuff_verify-type">
      <xsl:with-param name="localtype" select="$localtype"/>
      <xsl:with-param name="scope" select="$scope"/>
      <xsl:with-param name="format" select="$format"/>
      <xsl:with-param name="targetTopic" as="element()?" select="$targetTopic"/>
      <xsl:with-param name="targetElement" as="element()?" select="$targetElement"/>
    </xsl:apply-templates>

    <xsl:variable name="classval">
      <xsl:apply-templates select="." mode="topicpull:get-stuff_classval">
        <xsl:with-param name="type" select="$type" as="xs:string?"/>
      </xsl:apply-templates>
    </xsl:variable>
    <xsl:apply-templates select="." mode="topicpull:get-stuff_get-linktext">
      <xsl:with-param name="type" select="$type" as="xs:string?"/>
      <xsl:with-param name="scope" select="$scope"/>
      <xsl:with-param name="format" select="$format"/>
      <xsl:with-param name="targetTopic" as="element()?" select="$targetTopic"/>
      <xsl:with-param name="targetElement" as="element()?" select="$targetElement"/>
      <xsl:with-param name="classval" select="$classval"/>
    </xsl:apply-templates>

    <xsl:apply-templates select="." mode="topicpull:get-stuff_get-shortdesc">
      <xsl:with-param name="type" select="$type" as="xs:string?"/>
      <xsl:with-param name="scope" select="$scope"/>
      <xsl:with-param name="format" select="$format"/>
      <xsl:with-param name="targetTopic" as="element()?" select="$targetTopic"/>
      <xsl:with-param name="targetElement" as="element()?" select="$targetElement"/>
      <xsl:with-param name="classval" select="$classval"/>
    </xsl:apply-templates>
  </xsl:template>

  <!-- ******************************************************************************
       Individual portions of get-stuff processing, broken apart for easy overriding.
       If a template overrides all of get-stuff, most of these templates can still be
       used so as to avoid duplicating processing code in the override.
       ************************************************************************** -->

  <!-- Get the file name for a reference that goes out of the file -->
  <xsl:template match="*" mode="topicpull:get-stuff_file topicpull:get-stuff_docurl" as="xs:string">
    <xsl:param name="WORKDIR" as="xs:string?">
      <xsl:choose>
        <xsl:when test="contains(@class, ' topic/link ')">
          <xsl:choose>
            <xsl:when test="./preceding::processing-instruction('workdir-uri')[1]">
              <xsl:apply-templates select="./preceding::processing-instruction('workdir-uri')[1]" mode="get-work-dir"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:apply-templates select="/processing-instruction('workdir-uri')[1]" mode="get-work-dir"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates select="/processing-instruction('workdir-uri')[1]" mode="get-work-dir"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:param>
    <xsl:choose>
      <xsl:when test="contains(@href,'://') and contains(@href,'#')">
        <xsl:sequence select="substring-before(@href,'#')"/>
      </xsl:when>
      <xsl:when test="contains(@href,'://')">
        <xsl:sequence select="@href"/>
      </xsl:when>
      <xsl:when test="starts-with(@href, '#')">
        <xsl:sequence select="'#none#'"/>
      </xsl:when>
      <xsl:when test="contains(@href,'#')">
        <xsl:sequence select="concat($WORKDIR, substring-before(@href,'#'))"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="concat($WORKDIR, @href)"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Get the document that contains the target of the link.
    
       @return The document as indicated by the specified topicpos and
               file or () if not a sametopic reference and file cannot
               be resolved.
  -->
  <xsl:template match="*" mode="topicpull:get-target-document" as="document-node()?">
    <xsl:param name="scope" as="xs:string"/>
    <xsl:param name="format" as="xs:string"/>
    <xsl:param name="docUrl" as="xs:string?"/>
    
    <xsl:variable name="isSameFile" as="xs:boolean" 
      select="starts-with(@href,'#')"
    />
    
    <xsl:choose>
      <xsl:when test="$isSameFile"                 
      >        
        <xsl:sequence select="root(.)"/>
      </xsl:when>
      <xsl:when test="not($format) or $format = ('dita', '#none#')">
        <xsl:variable name="doc" as="document-node()?"
          select="if ($docUrl = ('#none#')) 
                     then () 
                     else document($docUrl,.)"
        />
        <xsl:if test="not($doc) or not($doc/*/*)">
          <xsl:apply-templates select="." mode="ditamsg:missing-href-target">
            <xsl:with-param name="file" select="$docUrl" as="xs:string"/>
          </xsl:apply-templates>
        </xsl:if>
        <xsl:sequence select="$doc"/>
      </xsl:when>
      <xsl:otherwise>        
        <xsl:sequence select="()"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Pick out the topic ID from an href value -->
  <xsl:template match="*" mode="topicpull:get-stuff_topicid" as="xs:string">
    <xsl:variable name="targetTopicId" select="dita-ot:get-topic-id(@href)" as="xs:string?"/>
    <xsl:choose>
      <xsl:when test="exists(targetTopicId)">
        <xsl:value-of select="$targetTopicId"/>
      </xsl:when>
      <xsl:otherwise>#none#</xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Pick out the element ID from an href value -->
  <xsl:template match="*" mode="topicpull:get-stuff_elemid" as="xs:string">
    <xsl:variable name="targetElementId" select="dita-ot:get-element-id(@href)" as="xs:string?"/>
    <xsl:sequence 
      select="if (exists($targetElementId)) 
                 then $targetElementId
                 else '#none#'"
    />
  </xsl:template>

  <!-- Get the type from target, if not defined locally -->
  <xsl:template match="*" mode="topicpull:get-stuff_get-type" as="xs:string?">
    <xsl:param name="localType" as="xs:string?"/>
    <xsl:param name="scope" as="xs:string?"/>
    <xsl:param name="format" as="xs:string?"/>
    <xsl:param name="targetTopic" as="element()?"/>
    <xsl:param name="targetElement" as="element()?"/>
    <xsl:choose>
      <xsl:when test="$localType and not($localType='#none#')">
        <xsl:sequence select="$localType"/>
      </xsl:when>
      <xsl:when test="$targetElement">
        <xsl:sequence select="local-name($targetElement)"/>
      </xsl:when>
      <xsl:when test="$targetTopic">
        <xsl:sequence select="local-name($targetTopic)"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:sequence select="'#none#'"/>        
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Find the class attribute of the referenced topic. Determine if the specified type
       exists in that class attribute. -->
  <xsl:template match="*" mode="topicpull:get-stuff_verify-type">
    <xsl:param name="localtype" as="xs:string"/>
    <xsl:param name="scope" as="xs:string"/>
    <xsl:param name="format" as="xs:string"/>
    <xsl:param name="targetTopic" as="element()?" />
    <xsl:param name="targetElement" as="element()?"/>
    
    
    <xsl:if test="$localtype!='#none#' and 
                  not(@scope=('external', 'peer')) and 
                  ((not(@format) or 
                   @format='dita') or 
                   starts-with(@href,'#'))">
      
      <xsl:choose>
        <xsl:when test="dita-ot:is-link(.) and not(*[not(contains(@class,' topic/desc '))]|text())">
          <xsl:apply-templates select="." mode="topicpull:verify-type-attribute">
            <xsl:with-param name="type" select="$localtype" as="xs:string?"/>
            <xsl:with-param name="actual-class" select="$targetTopic/@class" as="xs:string"/>
            <xsl:with-param name="actual-name" select="local-name($targetTopic)" as="xs:string"/>
            <xsl:with-param name="targetting" select="if ($targetElement) then 'element' else 'topic'" as="xs:string"/>
          </xsl:apply-templates>
        </xsl:when>
        <xsl:when test="contains(@class,' topic/link ')">
          <xsl:choose>
            <xsl:when test="$targetElement"/>
            <xsl:when test="contains(@xtrf,'.ditamap')"/>
            <xsl:when test="ancestor::*[contains(@class, ' topic/linkpool ')]/@mapkeyref"/>
            <xsl:otherwise>
              <xsl:apply-templates select="." mode="topicpull:verify-type-attribute">
                <xsl:with-param name="type" select="$localtype" as="xs:string?"/>
                <xsl:with-param name="actual-class" select="$targetTopic/@class" as="xs:string"/>
                <xsl:with-param name="actual-name" select="local-name($targetTopic)" as="xs:string"/>
                <xsl:with-param name="targetting" select="'topic'" as="xs:string"/>
              </xsl:apply-templates>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>
      </xsl:choose>
    </xsl:if>
  </xsl:template>

  <!--template used to construct the class value used to validate link targets against source types. -->
  <xsl:template match="*" mode="topicpull:get-stuff_classval" as="xs:string">
    <xsl:param name="type" select="'#none#'" as="xs:string?"/>
    <xsl:sequence 
      select="if ($type = '#none#')
                 then ' topic/topic '
              else if (dita-ot:has-element-id(@href))
                   then concat('/', $type, ' ')
              else concat(' ', $type, '/', $type, ' ')
      "/>
  </xsl:template>

  <!-- Get the short description for a link or xref -->
  <xsl:template match="*" mode="topicpull:get-stuff_get-shortdesc">
    <xsl:param name="type" as="xs:string?"/>
    <xsl:param name="scope" as="xs:string"/>
    <xsl:param name="format" as="xs:string"/>
    <xsl:param name="classval" as="xs:string"/>
    <xsl:param name="targetTopic" as="element()?" />
    <xsl:param name="targetElement" as="element()?"/>

    <xsl:choose>
      <xsl:when test="*[contains(@class, ' topic/desc ')]">
        <xsl:apply-templates select="." mode="topicpull:add-usershortdesc-PI"/>
        <xsl:apply-templates select="*[contains(@class, ' topic/desc ')]"/>
      </xsl:when>
      <xsl:when test="$scope='external' or $scope='peer' or $type='external' or not($format='#none#' or $format='dita')"/>
      <xsl:otherwise>
        <xsl:variable name="shortdesc" as="node()*">
          <xsl:apply-templates select="." mode="topicpull:getshortdesc">
            <xsl:with-param name="classval" select="$classval" as="xs:string"/>
            <xsl:with-param name="targetTopic" as="element()?" select="$targetTopic"/>
            <xsl:with-param name="targetElement" as="element()?" select="$targetElement"/>
          </xsl:apply-templates>
        </xsl:variable>
        <xsl:if test="not($shortdesc='#none#')">
          <xsl:apply-templates select="." mode="topicpull:add-genshortdesc-PI"/>
          <desc class="- topic/desc ">
            <xsl:apply-templates select="$shortdesc"/>
          </desc>
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Ignore desc in elements that do not support it. -->
  <xsl:template match="*[contains(@class,' topic/cite ') or
                         contains(@class,' topic/dt ') or
                         contains(@class,' topic/keyword ') or
                         contains(@class,' topic/term ') or
                         contains(@class,' topic/ph ') or
                         contains(@class,' topic/indexterm ') or
                         contains(@class,' topic/index-base ') or
                         contains(@class,' topic/indextermref ')]"
                mode="topicpull:get-stuff_get-shortdesc" priority="10"/>

  <!-- Get the link text for a link or cross reference. If specified locally, use that. Otherwise,
       work with the target to get the text. -->
  <xsl:template match="*" mode="topicpull:get-stuff_get-linktext">
    <xsl:param name="type" as="xs:string?"/>
    <xsl:param name="scope" as="xs:string"/>
    <xsl:param name="format" as="xs:string"/>
    <xsl:param name="classval" as="xs:string"/>
    <xsl:param name="targetTopic" as="element()?"/>
    <xsl:param name="targetElement" as="element()?"/>
    
    
    <xsl:choose>
      <xsl:when test="contains(@class,' topic/link ') and *[not(contains(@class, ' topic/desc '))]">
        <xsl:apply-templates select="." mode="topicpull:add-usertext-PI"/>
        <xsl:apply-templates select="*[not(contains(@class, ' topic/desc '))]|comment()|processing-instruction()"/>
      </xsl:when>
      <xsl:when test="dita-ot:is-link(.) and (not(matches(., '^\s*$')) or *[not(contains(@class, ' topic/desc '))])">
        <xsl:apply-templates select="." mode="topicpull:add-usertext-PI"/>
        <xsl:apply-templates select="text()|*[not(contains(@class, ' topic/desc '))]|comment()|processing-instruction()"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:variable name="linktext" as="node()*">
          <xsl:choose>
            <xsl:when test="dita-ot:is-non-local-non-dita-target($type, $scope, $format)">
              <xsl:value-of select="@href"/>
            </xsl:when>
            <xsl:when test="$scope=('peer', 'external') or @href = ''">
              <xsl:text>#none#</xsl:text>
            </xsl:when>
            <xsl:otherwise>              
              <xsl:apply-templates select="." mode="topicpull:getlinktext">
                <xsl:with-param name="targetTopic" as="element()?" select="$targetTopic"/>
                <xsl:with-param name="targetElement" as="element()?" select="$targetElement"/>
                <xsl:with-param name="classval" select="$classval" as="xs:string"/>
              </xsl:apply-templates>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>

        <xsl:if test="not($linktext='#none#') and dita-ot:is-link(.)">
          <!-- need to avoid flattening complex markup here-->
          <xsl:apply-templates select="." mode="topicpull:add-gentext-PI"/>
          <!-- FIXME: Link text should retain its element structure. -->
          <xsl:value-of select="$linktext"/>
        </xsl:if>
        <xsl:if test="not($linktext='#none#') and contains(@class, ' topic/link ')">
          <xsl:apply-templates select="." mode="topicpull:add-gentext-PI"/>
          <linktext class="- topic/linktext ">
            <!-- FIXME: Link text should retain its element structure. -->
            <xsl:value-of select="$linktext"/>
          </linktext>
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:variable name="link-classes" as="xs:string*"
                select="(' topic/cite ',
                         ' topic/dt ',
                         ' topic/keyword ',
                         ' topic/term ',
                         ' topic/ph ',
                         ' topic/indexterm ',
                         ' topic/index-base ',
                         ' topic/indextermref ')"/>
  
  <xsl:function name="dita-ot:is-link" as="xs:boolean">
    <xsl:param name="targetElement" as="element()"/>
    <xsl:sequence 
      select="contains($targetElement/@class,' topic/xref ') or
              (($targetElement/@href and not(contains($targetElement/@class,' delay-d/anchorkey ')) and 
               (some $c in $link-classes satisfies contains($targetElement/@class, $c))))"/>
  </xsl:function>

  <!-- Called when retrieving text for a link or xref. Determine if the reference
       points to a topic, or to an element, and process accordingly. -->
  <xsl:template match="*" mode="topicpull:getlinktext">
    <xsl:param name="targetTopic" as="element()?"/>
    <xsl:param name="targetElement" as="element()?"/>
    <xsl:param name="classval" select="'#none#'" as="xs:string"/>
        
    <xsl:choose>
      <xsl:when test="$targetElement">
        <xsl:apply-templates select="." mode="topicpull:getlinktext_within-topic">
          <xsl:with-param name="targetTopic" as="element()?" select="$targetTopic"/>
          <xsl:with-param name="targetElement" as="element()?" select="$targetElement"/>
          <xsl:with-param name="classval" select="$classval" as="xs:string"/>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates select="." mode="topicpull:getlinktext_topic">
          <xsl:with-param name="targetTopic" as="element()?" select="$targetTopic"/>
          <xsl:with-param name="classval" select="$classval" as="xs:string"/>
        </xsl:apply-templates>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!-- Get the link text for a reference to a topic. Already verified that
       topic should be available (scope!=external, etc). -->
  <xsl:template match="*" mode="topicpull:getlinktext_topic">
    <xsl:param name="targetTopic" as="element()?"/>
    <xsl:param name="classval" select="'#none#'" as="xs:string"/>

    <xsl:apply-templates select="." mode="topicpull:getlinktext-for-target-topic">
      <xsl:with-param name="targetTopic" as="element()?" select="$targetTopic"/>
      <xsl:with-param name="classval" select="$classval" as="xs:string"/>
    </xsl:apply-templates>
  </xsl:template>

  <!-- Get the link text from a specific topic within another file.
  
  -->
  <xsl:template match="*" mode="topicpull:getlinktext-for-target-topic">
    <xsl:param name="targetTopic" as="element()?"/>
    <xsl:param name="classval"/>
    
    <xsl:choose>
      <xsl:when test="$targetTopic/*[contains(@class, ' topic/title ')]">
        <xsl:variable name="target-text">
          <xsl:apply-templates
            select="$targetTopic/*[contains(@class, ' topic/title ')]" mode="dita-ot:text-only"/>
        </xsl:variable>
        <xsl:value-of select="normalize-space($target-text)"/>
      </xsl:when>
      <xsl:otherwise>
        <!-- defer to the final output process, which will massage the file name-->
        <xsl:text>#none#</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--get linktext for xref to any body elements. Unsure how to make this extensible,
      if a plugin needs to add support for type=step or type=newElement. May need
      to wait for XSLT 2.0 support, with fancier modes? -->
  <xsl:template match="*" mode="topicpull:getlinktext_within-topic">
    <xsl:param name="classval" select="'#none#'" as="xs:string"/>
    <xsl:param name="targetTopic" as="element()?"/>
    <xsl:param name="targetElement" as="element()?"/>
        
    <xsl:variable name="useclassval" as="xs:string?">
      <xsl:choose>
        <xsl:when test="$classval = ('/li ','/fn ','/dlentry ','/section ', '/example ', '/fig ','/figgroup ')">
          <xsl:sequence select="$classval"/>
        </xsl:when>
        <xsl:when test="$targetTopic">
          <xsl:sequence select="concat('/', tokenize(tokenize(@class, ' ')[2], '/')[2], ' ')"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:sequence select="()"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
        
    <!-- WEK: I think this would be better done through a dispatch mode that takes the @type and $useclass
              as parameters and then does the dispatch done here so that extensions can add or override
              the base link resolution processing.
      -->
    <xsl:choose>
      <xsl:when test="@type='li' or $useclassval='/li '">
        <xsl:apply-templates select="." mode="topicpull:getlinktext_within-topic_li">
          <xsl:with-param name="classval" select="$useclassval"/>
          <xsl:with-param name="targetTopic" as="element()?" select="$targetTopic"/>
          <xsl:with-param name="targetElement" as="element()?" select="$targetElement"/>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:when test="@type='fn' or $useclassval='/fn '">
        <xsl:apply-templates select="." mode="topicpull:getlinktext_within-topic_fn">
          <xsl:with-param name="classval" select="$useclassval"/>
          <xsl:with-param name="targetTopic" as="element()?" select="$targetTopic"/>
          <xsl:with-param name="targetElement" as="element()?" select="$targetElement"/>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:when test="@type='dlentry' or $useclassval='/dlentry '">
        <xsl:apply-templates select="." mode="topicpull:getlinktext_within-topic_dlentry">
          <xsl:with-param name="classval" select="$useclassval"/>
          <xsl:with-param name="targetTopic" as="element()?" select="$targetTopic"/>
          <xsl:with-param name="targetElement" as="element()?" select="$targetElement"/>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:when test="@type='table' or $useclassval='/table '">
        <xsl:apply-templates select="." mode="topicpull:getlinktext_within-topic_table">
          <xsl:with-param name="classval" select="$useclassval"/>
          <xsl:with-param name="targetTopic" as="element()?" select="$targetTopic"/>
          <xsl:with-param name="targetElement" as="element()?" select="$targetElement"/>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:when test="@type='fig' or $useclassval='/fig '">
        <xsl:apply-templates select="." mode="topicpull:getlinktext_within-topic_fig">
          <xsl:with-param name="classval" select="$useclassval"/>
          <xsl:with-param name="targetTopic" as="element()?" select="$targetTopic"/>
          <xsl:with-param name="targetElement" as="element()?" select="$targetElement"/>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates select="." mode="topicpull:getlinktext_within-topic_otherblock">
          <xsl:with-param name="classval" select="$useclassval"/>
          <xsl:with-param name="targetTopic" as="element()?" select="$targetTopic"/>
          <xsl:with-param name="targetElement" as="element()?" select="$targetElement"/>
        </xsl:apply-templates>
      </xsl:otherwise>
    </xsl:choose>
    
  </xsl:template>

  <!-- Get link text for arbitrary block elements inside a topic. 
         
  -->
  <xsl:template match="*" mode="topicpull:getlinktext_within-topic_otherblock">
    <xsl:param name="classval" select="'#none#'" as="xs:string"/>
    <xsl:param name="targetTopic" as="element()?"/>
    <xsl:param name="targetElement" as="element()?"/>
          
    <xsl:choose>
      <xsl:when test="$targetElement/*[contains(@class,' topic/title ')][1]">
        <xsl:variable name="target-text">
          <xsl:apply-templates  mode="dita-ot:text-only"
            select="$targetElement/*[contains(@class,' topic/title ')][1]"
          />
        </xsl:variable>
        <xsl:value-of select="normalize-space($target-text)"/>
      </xsl:when>
      <xsl:when test="$targetElement/@spectitle">
        <xsl:value-of select="normalize-space($targetElement/@spectitle)"/>
      </xsl:when>            
      <xsl:when test="$targetElement[contains(@class, ' topic/title ')]">
        <xsl:variable name="target-text">
          <xsl:apply-templates  mode="dita-ot:text-only" 
            select="($targetTopic/*[contains(@class, ' topic/title ')])[1]"
          />
        </xsl:variable>
        <xsl:value-of select="normalize-space($target-text)"/>
      </xsl:when>
      <xsl:when test="$targetElement">
        <xsl:variable name="target-text">
          <xsl:apply-templates mode="topicpull:get_generated_text" 
            select="$targetElement" />
        </xsl:variable>
        <xsl:choose>
          <xsl:when test="$target-text!='#none#'">
            <xsl:value-of select="normalize-space($target-text)"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:apply-templates select="." mode="topicpull:otherblock-linktext-fallback"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates select="." mode="topicpull:otherblock-linktext-fallback"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Provide a hook for specializations to give default generated text to new elements.
       By default, elements with no generated text return #none#. -->
  <xsl:template match="*" mode="topicpull:get_generated_text">
    <xsl:text>#none#</xsl:text>
  </xsl:template>

  <!--No link text found; use the href, unless it contains .dita, in which case defer to the final output pass to decide what to do with the file extension-->
  <xsl:template match="*" mode="topicpull:otherblock-linktext-fallback">
    <xsl:call-template name="dita-ot:use-href-as-link-text"/>
    <xsl:apply-templates select="." mode="ditamsg:cannot-retrieve-linktext"/>
  </xsl:template>

  <!-- Pull link text for a figure. Uses mode="topicpull:figure-linktext" to output the text.  -->
  <xsl:template match="*" mode="topicpull:getlinktext_within-topic_fig">
    <xsl:param name="classval" select="'#none#'"/>
    <xsl:param name="targetTopic" as="element()?"/>
    <xsl:param name="targetElement" as="element()?"/>
    
    <xsl:choose>
      <xsl:when test="$targetElement/*[contains(@class,' topic/title ')]">
        <xsl:variable name="fig-count-actual">
          <xsl:apply-templates select="$targetElement/*[contains(@class,' topic/title ')][1]" mode="topicpull:fignumber"/>
        </xsl:variable>
        <xsl:apply-templates select="." mode="topicpull:figure-linktext">
          <xsl:with-param name="figtext"><xsl:call-template name="getVariable"><xsl:with-param name="id" select="'Figure'"/></xsl:call-template></xsl:with-param>
          <xsl:with-param name="figcount" select="$fig-count-actual"/>
          <xsl:with-param name="figtitle">
            <xsl:sequence select="$targetElement/*[contains(@class,' topic/title ')][1]"/>
          </xsl:with-param>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:when test="$targetElement/@spectitle">
        <xsl:variable name="fig-count-actual">
          <xsl:apply-templates select="$targetElement" mode="topicpull:fignumber"/>
        </xsl:variable>
        <xsl:apply-templates select="." mode="topicpull:figure-linktext">
          <xsl:with-param name="figtext"><xsl:call-template name="getVariable"><xsl:with-param name="id" select="'Figure'"/></xsl:call-template></xsl:with-param>
          <xsl:with-param name="figcount" select="$fig-count-actual"/>
          <xsl:with-param name="figtitle" select="string($targetElement/@spectitle)"/>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates select="." mode="topicpull:figure-linktext-fallback"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Determine the text for a link to a figure. Currently uses "Figure N". A node set
       containing the figure's <title> element is also passed in, an override may choose
       to use this in the figure's reference text. -->
  <xsl:template match="*" mode="topicpull:figure-linktext">
    <xsl:param name="figtext"/>
    <xsl:param name="figcount"/>
    <xsl:param name="figtitle"/>
    <xsl:choose>
      <xsl:when test="$FIGURELINK='TITLE'">
        <xsl:apply-templates select="$figtitle" mode="dita-ot:text-only"/>
      </xsl:when>
      <xsl:otherwise> <!-- Default: FIGURELINK='NUMBER' -->
        <xsl:value-of select="$figtext"/>
        <xsl:call-template name="getVariable">
          <xsl:with-param name="id" select="'figure-number-separator'"/>
        </xsl:call-template>
        <xsl:value-of select="$figcount"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!-- XXX: Remove I18N processing from here and move to transtype specific code -->
  <!-- Generate Hungarian appropriate figure reference, which is "1. Table" -->
  <xsl:template match="*[lang('hu')]" mode="topicpull:figure-linktext">
    <xsl:param name="figtext"/>
    <xsl:param name="figcount"/>
    <xsl:param name="figtitle"/> 
    <xsl:choose>
      <xsl:when test="$FIGURELINK='TITLE'">
        <xsl:apply-templates select="$figtitle" mode="dita-ot:text-only"/>
      </xsl:when>
      <xsl:otherwise> 
        <xsl:value-of select="$figcount"/>
        <xsl:call-template name="getVariable">
          <xsl:with-param name="id" select="'figure-number-separator'"/>
        </xsl:call-template>
        <xsl:value-of select="$figtext"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!-- If the figure is unavailable or we're not sure what to do with it, generate fallback text -->
  <xsl:template match="*" mode="topicpull:figure-linktext-fallback">
    <xsl:call-template name="dita-ot:use-href-as-link-text"/>
    <xsl:apply-templates select="." mode="ditamsg:cannot-retrieve-linktext"/>
  </xsl:template>

  <!-- Determine the number of the figure being linked to -->
  <xsl:template match="*[contains(@class,' topic/fig ')]/*[contains(@class,' topic/title ')] | 
                       *[contains(@class,' topic/fig ')][@spectitle]" mode="topicpull:fignumber">
    <xsl:call-template name="compute-number">
      <xsl:with-param name="all">
        <xsl:number from="/*" count="key('count.topic.fig','include')" level="any"/>
      </xsl:with-param>
      <xsl:with-param name="except">
        <xsl:number from="/*" count="key('count.topic.fig','exclude')" level="any"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- Get text for a link to a table. Actual text is generated in template with mode="topicpull:table-linktext" -->
  <xsl:template match="*" mode="topicpull:getlinktext_within-topic_table">
    <xsl:param name="file" select="'#none#'" as="xs:string"/>
    <xsl:param name="topicpos" as="xs:string"/>
    <xsl:param name="classval" select="'#none#'" as="xs:string"/>
    <xsl:param name="targetTopic" as="element()?"/>
    <xsl:param name="targetElement" as="element()?"/>
    
    <xsl:choose>
      <xsl:when test="$targetElement/*[contains(@class,' topic/title ')][1]">
        <xsl:apply-templates select="." mode="topicpull:table-linktext">
          <xsl:with-param name="tbltext">
            <xsl:call-template name="getVariable">
              <xsl:with-param name="id" select="'Table'"/>
            </xsl:call-template>
          </xsl:with-param>
          <xsl:with-param name="tblcount">
            <xsl:apply-templates 
              mode="topicpull:tblnumber"
              select="$targetElement/*[contains(@class,' topic/title ')][1]" 
            />
          </xsl:with-param>
          <xsl:with-param name="tbltitle">
            <xsl:sequence select="$targetElement/*[contains(@class,' topic/title ')][1]"/>
          </xsl:with-param>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:when test="$targetElement/@spectitle">
        <xsl:apply-templates select="." mode="topicpull:table-linktext">
          <xsl:with-param name="tbltext" as="node()*">
            <xsl:call-template name="getVariable">
              <xsl:with-param name="id" select="'Table'"/>
            </xsl:call-template>
          </xsl:with-param>
          <xsl:with-param name="tblcount" as="node()*">
            <xsl:apply-templates select="$targetElement" mode="topicpull:tblnumber"/>
          </xsl:with-param>
          <xsl:with-param name="tbltitle" as="node()*">
            <xsl:value-of select="$targetElement/@spectitle"/>
          </xsl:with-param>
        </xsl:apply-templates>
      </xsl:when>      
      <xsl:otherwise>
        <xsl:apply-templates select="." mode="topicpull:table-linktext-fallback"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Determine the text for a link to a table. Currently uses table title. -->
  <xsl:template match="*" mode="topicpull:table-linktext">
    <xsl:param name="tbltext" as="node()*"/>
    <xsl:param name="tblcount" as="node()*"/>
    <xsl:param name="tbltitle" as="node()*"/> 
    <xsl:choose>
      <xsl:when test="$TABLELINK='TITLE'">
        <xsl:apply-templates select="$tbltitle" mode="dita-ot:text-only"/>
      </xsl:when>
      <xsl:otherwise> 
        <xsl:value-of select="$tbltext"/>
        <xsl:call-template name="getVariable">
          <xsl:with-param name="id" select="'figure-number-separator'"/>
        </xsl:call-template>
        <xsl:value-of select="$tblcount"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!-- Generate Hungarian appropriate table reference, which is "1. Table" -->
  <xsl:template match="*[lang('hu')]" mode="topicpull:table-linktext">
    <xsl:param name="tbltext" as="node()*"/>
    <xsl:param name="tblcount" as="node()*"/>
    <xsl:param name="tbltitle" as="node()*"/>
    <xsl:choose>
      <xsl:when test="$TABLELINK='TITLE'">
        <xsl:apply-templates select="$tbltitle" mode="dita-ot:text-only"/>
      </xsl:when>
      <xsl:otherwise> 
        <xsl:value-of select="$tblcount"/>
        <xsl:call-template name="getVariable">
          <xsl:with-param name="id" select="'figure-number-separator'"/>
        </xsl:call-template>
        <xsl:value-of select="$tbltext"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!-- Fallback text if a table target cannot be found, or there is some other problem -->
  <xsl:template match="*" mode="topicpull:table-linktext-fallback">
    <xsl:call-template name="dita-ot:use-href-as-link-text"/>
    <xsl:apply-templates select="." mode="ditamsg:cannot-retrieve-linktext"/>
  </xsl:template>

  <!-- Determine the number of the table being linked to -->
  <xsl:template mode="topicpull:tblnumber" 
    match="*[contains(@class,' topic/table ')]/*[contains(@class,' topic/title ')]  | 
           *[contains(@class,' topic/table ')][@spectitle]" >
    <xsl:call-template name="compute-number">
      <xsl:with-param name="all">
        <xsl:number from="/*" count="key('count.topic.table','include')" level="any"/>
      </xsl:with-param>
      <xsl:with-param name="except">
        <xsl:number from="/*" count="key('count.topic.table','exclude')" level="any"/>
      </xsl:with-param>
    </xsl:call-template>
  </xsl:template>

  <!-- Set link text when linking to a list item -->
  <xsl:template match="*" mode="topicpull:getlinktext_within-topic_li">
    <xsl:param name="classval" select="'#none#'" as="xs:string"/>
    <xsl:param name="targetTopic" as="element()?"/>
    <xsl:param name="targetElement" as="element()?"/>

    <xsl:choose>
      <!-- If the list item exists, and is in an OL, process it -->
      <xsl:when test="$targetElement[contains(@class, ' topic/li ')]/parent::*[contains(@class, ' topic/ol ')]">
        <xsl:apply-templates mode="topicpull:li-linktext" 
          select=" $targetElement"
        />
      </xsl:when>
      <!-- If the list item exists, but is in some other kind of list, issue a message -->
      <xsl:when test="$targetElement[contains(@class, ' topic/li ')]">
        <xsl:call-template name="topicpull:referenced-invalid-list-item"/>
      </xsl:when>
      <xsl:otherwise>
        <!-- Should only get here if $targetElement is not set (which is possible) -->
        <xsl:call-template name="dita-ot:use-href-as-link-text"/>
        <xsl:apply-templates select="." mode="ditamsg:cannot-retrieve-list-number"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!-- Matching the list item, determine the count for this item -->
  <xsl:template match="*[contains(@class,' topic/ol ')]/*[contains(@class,' topic/li ')]" mode="topicpull:li-linktext">
    <xsl:number level="multiple"
      count="*[contains(@class,' topic/ol ')]/*[contains(@class,' topic/li ')]" 
      format="{$dita-ot:ordered-list-number-format}"
    />
  </xsl:template>
  
  <!-- Instead of matching an unordered list item, we will call this template; that way
     the error points to the XREF, not to the list item. -->
  <xsl:template name="topicpull:referenced-invalid-list-item">
    <xsl:apply-templates select="." mode="ditamsg:crossref-unordered-listitem"/>
  </xsl:template>

  <!-- Generate link text for a footnote reference -->
  <xsl:template match="*" mode="topicpull:getlinktext_within-topic_fn">
    <xsl:param name="classval">#none#</xsl:param>
    <xsl:param name="targetTopic" as="element()?"/>
    <xsl:param name="targetElement" as="element()?"/>

    <xsl:choose>
      <xsl:when test="$targetElement">
        <xsl:apply-templates mode="topicpull:fn-linktext" 
          select="$targetElement"
        />
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="dita-ot:use-href-as-link-text"/>
        <xsl:apply-templates select="." mode="ditamsg:cannot-retrieve-footnote-number"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' topic/fn ')]" mode="topicpull:fn-linktext">
    <xsl:variable name="fnid">
      <xsl:number from="/" level="any"/>
    </xsl:variable>
    <xsl:variable name="callout">
      <xsl:value-of select="@callout"/>
    </xsl:variable>
    <xsl:variable name="convergedcallout">
      <xsl:choose>
        <xsl:when test="string-length($callout)&gt;0">
          <xsl:value-of select="$callout"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$fnid"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <a name="fnsrc_{$fnid}" href="#fntarg_{$fnid}">
      <sup>
        <xsl:value-of select="$convergedcallout"/>
      </sup>
    </a>
  </xsl:template>

  <!-- Getting text from a dlentry target: use the contents of the term -->
  <xsl:template match="*" mode="topicpull:getlinktext_within-topic_dlentry">
    <xsl:param name="classval">#none#</xsl:param>
    <xsl:param name="targetTopic" as="element()?"/>
    <xsl:param name="targetElement" as="element()?"/>

    <xsl:choose>
      <xsl:when test="$targetElement">
        <xsl:variable name="target-text">
          <xsl:apply-templates
            select="$targetElement"
          />
        </xsl:variable>
        <xsl:value-of select="normalize-space($target-text)"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="dita-ot:use-href-as-link-text"/>
        <xsl:apply-templates select="." mode="ditamsg:cannot-find-dlentry-target"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--getting the shortdesc for a link; called from main mode template for link/xref, 
      only after conditions such as scope and format have been tested and a text pull
      has been determined to be appropriate-->
  <xsl:template match="*" mode="topicpull:getshortdesc">
    <xsl:param name="classval">#none#</xsl:param>
    <xsl:param name="targetTopic" as="element()?"/>
    <xsl:param name="targetElement" as="element()?"/>
    
    <xsl:choose>
      <xsl:when test="$targetElement">
        <xsl:apply-templates select="." mode="topicpull:getshortdesc_element">
          <xsl:with-param name="classval" select="$classval"/>
          <xsl:with-param name="targetTopic" as="element()?" select="$targetTopic"/>
          <xsl:with-param name="targetElement" as="element()?" select="$targetElement"/>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:when test="$targetTopic">
        <xsl:apply-templates select="." mode="topicpull:getshortdesc_for_topic">
          <xsl:with-param name="classval" select="$classval"/>
          <xsl:with-param name="targetTopic" as="element()?" select="$targetTopic"/>
        </xsl:apply-templates>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <!-- Get the short description for a non-topic element. Search for an element with the matching
       correct class value and matching ID, inside the topic with the correct ID. If found, use
       the local <desc> element. If not found, do not create a short description. -->
  <xsl:template match="*" mode="topicpull:getshortdesc_element">
    <xsl:param name="classval">#none#</xsl:param>
    <xsl:param name="targetTopic" as="element()?"/>
    <xsl:param name="targetElement" as="element()?"/>

    <xsl:choose>
      <xsl:when test="$targetElement/*[contains(@class, ' topic/desc ')]">
          <xsl:apply-templates 
            mode="copy-desc-contents"
            select="$targetElement/*[contains(@class, ' topic/desc ')]" 
          />
      </xsl:when>
      <xsl:otherwise>#none#</xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Get the shortdesc for a topic  -->
  <xsl:template match="*" mode="topicpull:getshortdesc_for_topic">
    <xsl:param name="classval">#none#</xsl:param>
    <xsl:param name="targetTopic" as="element()?"/>
    <xsl:param name="targetElement" as="element()?"/>

    <xsl:choose>
      <xsl:when test="$targetTopic/*[contains(@class, ' topic/shortdesc ')] |
                      $targetTopic/*[contains(@class, ' topic/abstract ')]/*[contains(@class, ' topic/shortdesc ')]">
        <xsl:apply-templates mode="copy-shortdesc" 
          select="$targetTopic/*[contains(@class, ' topic/shortdesc ')] |
                  $targetTopic/*[contains(@class, ' topic/abstract ')]/*[contains(@class, ' topic/shortdesc ')]" 
                                     
        />
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>#none#</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="*">
    <xsl:copy>
      <xsl:apply-templates select="@*,node()"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="@*|comment()|processing-instruction()|text()">
    <xsl:sequence select="."/>
  </xsl:template>  
  
  <xsl:template match="*[contains(@class,' topic/xref ')]" mode="copy-shortdesc">
    <xsl:choose>
      <xsl:when test="not(@href) or (@scope= ('peer', 'external'))">
        <xsl:copy>
          <xsl:apply-templates select="@*|text()|*" mode="copy-shortdesc" />
        </xsl:copy>
      </xsl:when>
      <xsl:when test="@format and not(@format='dita')">
        <xsl:copy>
          <xsl:apply-templates select="@*|text()|*" mode="copy-shortdesc" />
        </xsl:copy>
      </xsl:when>
      <xsl:when test="not(contains(@href,'/'))"><!-- May be DITA, but in the same directory -->
        <xsl:copy>
          <xsl:apply-templates select="@*|text()|*" mode="copy-shortdesc" />
        </xsl:copy>
      </xsl:when>
      <xsl:when test="text()|*[not(contains(@class,' topic/desc '))]">
        <xsl:apply-templates 
          mode="copy-shortdesc" 
          select="*[not(contains(@class,' topic/desc '))]|text()|comment()|processing-instruction()" 
        />
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>***</xsl:text><!-- go get the target text -->
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="text()" mode="copy-shortdesc">
    <xsl:value-of select="translate(.,'&#xA;',' ')" />
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' topic/desc ')]" mode="copy-desc-contents">
    <!-- For desc: match a desc, then switch to matching shortdesc rules -->
    <xsl:apply-templates select="node()" mode="copy-shortdesc"/>
  </xsl:template>

  <xsl:template match="*[contains(@class,' topic/shortdesc ')]" mode="copy-shortdesc">
    <xsl:if test="preceding-sibling::*[contains(@class,' topic/shortdesc ')]">
      <xsl:text>&#x20;</xsl:text>
    </xsl:if>
    <xsl:apply-templates select="node()" mode="copy-shortdesc" />
  </xsl:template>
  
  <xsl:template match="@id" mode="copy-shortdesc" />
  
  <xsl:template match="*[contains(@class,' topic/indexterm ')]" mode="copy-shortdesc" />
  <xsl:template match="*[contains(@class,' topic/draft-comment ') or 
                        contains(@class,' topic/required-cleanup ')]" 
                mode="copy-shortdesc"/>
  
  <xsl:template match="*|@*|processing-instruction()" mode="copy-shortdesc">
    <xsl:copy>
      <xsl:apply-templates select="@*|text()|*|processing-instruction()" mode="copy-shortdesc" />
    </xsl:copy>
  </xsl:template>

  <!-- Used to determine the number of figures and tables; could be used for other functions as well. -->
  <xsl:template name="compute-number">
    <xsl:param name="except"/>
    <xsl:param name="all"/>
    <xsl:choose>
      <xsl:when test="$except != ''">
        <xsl:value-of select="number($all) - number($except)"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$all"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!-- Make it easy to override messages. If a product wishes to change or hide
       specific messages, it can override these templates. Longer term, it would
       be good to move messages from each XSL file into a common location. -->
  <!-- Deprecated -->
  <xsl:template match="*" mode="ditamsg:unknown-extension">
    <xsl:call-template name="output-message">
      <xsl:with-param name="id" select="'DOTX006E'"/>
      <xsl:with-param name="msgparams">%1=<xsl:value-of select="@href"/></xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  <xsl:template match="*" mode="ditamsg:empty-href">
    <xsl:call-template name="output-message">
      <xsl:with-param name="id" select="'DOTX017E'"/>
    </xsl:call-template>
  </xsl:template>
  <xsl:template match="*" mode="ditamsg:missing-href">
    <xsl:call-template name="output-message">
      <xsl:with-param name="id" select="'DOTX028E'"/>
    </xsl:call-template>
  </xsl:template>
  <xsl:template match="*" mode="ditamsg:type-attribute-not-specific">
    <xsl:param name="elem-name" select="name()"/>
    <xsl:param name="targetting"/>
    <xsl:param name="type" as="xs:string?"/>
    <xsl:param name="actual-name"/>
    <xsl:call-template name="output-message">
      <xsl:with-param name="id" select="'DOTX029I'"/>
      <xsl:with-param name="msgparams">%1=<xsl:value-of select="$elem-name"/>;%2=<xsl:value-of select="$targetting"/>;%3=<xsl:value-of select="$type"/>;%4=<xsl:value-of select="$actual-name"/></xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  <xsl:template match="*" mode="ditamsg:type-attribute-incorrect">
    <xsl:param name="elem-name" select="name()"/>
    <xsl:param name="targetting"/>
    <xsl:param name="type" as="xs:string?"/>
    <xsl:param name="actual-name"/>
    <xsl:call-template name="output-message">
      <xsl:with-param name="id" select="'DOTX030W'"/>
      <xsl:with-param name="msgparams">%1=<xsl:value-of select="$elem-name"/>;%2=<xsl:value-of select="$targetting"/>;%3=<xsl:value-of select="$type"/>;%4=<xsl:value-of select="$actual-name"/></xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  <xsl:template match="*" mode="ditamsg:missing-href-target">
    <xsl:param name="file"/>
    <xsl:choose>
       <xsl:when test="$ONLYTOPICINMAP='true'">
          <xsl:call-template name="output-message">
             <xsl:with-param name="id" select="'DOTX056W'"/>
             <xsl:with-param name="msgparams">%1=<xsl:value-of select="$file"/></xsl:with-param>
           </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
         <xsl:call-template name="output-message">
            <xsl:with-param name="id" select="'DOTX031E'"/>
            <xsl:with-param name="msgparams">%1=<xsl:value-of select="$file"/></xsl:with-param>
            </xsl:call-template>
         </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="*" mode="ditamsg:cannot-retrieve-linktext">
    <xsl:call-template name="output-message">
      <xsl:with-param name="id" select="'DOTX032E'"/>
      <xsl:with-param name="msgparams">%1=<xsl:value-of select="@href"/></xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  <xsl:template match="*" mode="ditamsg:cannot-retrieve-list-number">
    <xsl:call-template name="output-message">
      <xsl:with-param name="id" select="'DOTX033E'"/>
      <xsl:with-param name="msgparams">%1=<xsl:value-of select="@href"/></xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  <xsl:template match="*" mode="ditamsg:crossref-unordered-listitem">
    <xsl:call-template name="output-message">
      <xsl:with-param name="id" select="'DOTX034E'"/>
      <xsl:with-param name="msgparams">%1=<xsl:value-of select="@href"/></xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  <xsl:template match="*" mode="ditamsg:cannot-retrieve-footnote-number">
    <xsl:call-template name="output-message">
      <xsl:with-param name="id" select="'DOTX035E'"/>
      <xsl:with-param name="msgparams">%1=<xsl:value-of select="@href"/></xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  <xsl:template match="*" mode="ditamsg:cannot-find-dlentry-target">
    <xsl:call-template name="output-message">
      <xsl:with-param name="id" select="'DOTX036E'"/>
      <xsl:with-param name="msgparams">%1=<xsl:value-of select="@href"/></xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  
  <xsl:template match="*|@*|text()|comment()|processing-instruction()" mode="specialize-foreign-unknown">
    <xsl:copy>
      <xsl:apply-templates select="@*, node()" mode="specialize-foreign-unknown"/>
    </xsl:copy>
  </xsl:template>

  <!-- Added for RFE 1367897. Ensure that if a value was passed in from the map,
       we respect that value, otherwise, use the value determined by this program. -->
  <xsl:template match="*" mode="topicpull:add-gentext-PI">
    <xsl:choose>
      <xsl:when test="processing-instruction()[name()='ditaot'][.=('usertext', 'gentext')]">
        <xsl:sequence select="processing-instruction()[name()='ditaot'][.=('usertext', 'gentext')]"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:processing-instruction name="ditaot">gentext</xsl:processing-instruction>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="*" mode="topicpull:add-usertext-PI">
    <xsl:choose>
      <xsl:when test="processing-instruction()[name()='ditaot'][.=('usertext', 'gentext')]">
        <xsl:sequence select="processing-instruction()[name()='ditaot'][.=('usertext', 'gentext')]"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:processing-instruction name="ditaot">usertext</xsl:processing-instruction>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!-- Added for RFE 3001750. -->
  <xsl:template match="*" mode="topicpull:add-genshortdesc-PI">
    <xsl:choose>
      <xsl:when test="processing-instruction()[name()='ditaot'][.=('usershortdesc', 'genshortdesc')]">
        <xsl:sequence select="processing-instruction()[name()='ditaot'][.=('usershortdesc','genshortdesc')]"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:processing-instruction name="ditaot">genshortdesc</xsl:processing-instruction>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="*" mode="topicpull:add-usershortdesc-PI">
    <xsl:choose>
      <xsl:when test="processing-instruction()[name()='ditaot'][.=('usershortdesc', 'genshortdesc')]">
        <xsl:sequence select="processing-instruction()[name()='ditaot'][.=('usershortdesc', 'genshortdesc')]"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:processing-instruction name="ditaot">usershortdesc</xsl:processing-instruction>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!-- Use the context element's @href value as the link text
    -->
  <xsl:template name="dita-ot:use-href-as-link-text">
    
    <xsl:choose>
      <xsl:when test="starts-with(@href,'#')">
        <xsl:value-of select="@href"/>
      </xsl:when>
      <xsl:when test="not(@format) or @format = 'dita'">
        <xsl:text>#none#</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="@href"/>
      </xsl:otherwise>
    </xsl:choose>
    
  </xsl:template>

  <xsl:function name="dita-ot:get-element-in-topic-by-id" as="element()?">
    <xsl:param name="targetTopic" as="element()?"/><!-- Topic that may contain the target element -->
    <xsl:param name="elemid" as="xs:string"/><!-- Target element ID -->
    <!-- Return the first result for element with target ID within any of the
         possible topics within the target topic.
      -->
    <xsl:variable name="result" as="element()?"
      select="(for $top in ($targetTopic/*[contains(@class, ' topic/title ')],
                            $targetTopic/*[contains(@class, ' topic/abstract ')],
                            $targetTopic/*[contains(@class, ' topic/shortdesc ')],
                            $targetTopic/*[contains(@class, ' topic/body ')]) 
                   return key('id', $elemid, $top))[1]
      "
    />
    <xsl:sequence select="$result"/>
  </xsl:function>
  
  <!-- Get the topic with the specified ID from the specified document. -->
  <xsl:function name="dita-ot:get-topic-for-doc-and-id" as="element()?">
    <xsl:param name="targetDoc" as="document-node()?"/>
    <xsl:param name="targetTopicId" as="xs:string"/>
    
    <!-- Topic IDs should be unique within a given XML document but user error could
         result in duplicated topic IDs in some cases so we return the first
         instance found just in case.
      -->
    <xsl:variable name="targetTopic" as="element()?"
      select="if ($targetTopicId and not($targetTopicId = ('#none#'))) 
                 then key('topic', $targetTopicId, $targetDoc)[1]
                 else ($targetDoc/*[contains(@class, ' topic/topic ')],
                       $targetDoc/*/*[contains(@class, ' topic/topic ')])[1]
      "
    />
    <xsl:sequence select="$targetTopic"/>
  </xsl:function>
  
  <!-- Return true if the target is not local and is not a DITA topic. -->
  <xsl:function name="dita-ot:is-non-local-non-dita-target" as="xs:boolean">
    <xsl:param name="type" as="xs:string?"/>
    <xsl:param name="scope" as="xs:string?"/>
    <xsl:param name="format" as="xs:string?"/>
    <xsl:variable name="result" as="xs:boolean"
      select="
      $type='external' or 
      ($scope='external') and 
      ($format != '' and 
       not($format=('#none#', 'dita')))
      "
    />
    <xsl:sequence select="$result"/>    
  </xsl:function>
  
</xsl:stylesheet>
