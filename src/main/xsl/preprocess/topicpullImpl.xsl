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
          
<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:exsl="http://exslt.org/common"
                xmlns:dita-ot="http://dita-ot.sourceforge.net/ns/201007/dita-ot"
                xmlns:topicpull="http://dita-ot.sourceforge.net/ns/200704/topicpull"
                xmlns:ditamsg="http://dita-ot.sourceforge.net/ns/200704/ditamsg"
                exclude-result-prefixes="dita-ot topicpull ditamsg exsl">
  <xsl:import href="../common/dita-utilities.xsl"/>
  <xsl:import href="../common/output-message.xsl"/>
  <xsl:import href="../common/dita-textonly.xsl"/>
  <!-- Define the error message prefix identifier -->
  <xsl:variable name="msgprefix">DOTX</xsl:variable>

  <!-- Deprecated -->
  <xsl:param name="FILEREF">file://</xsl:param>
  <!-- The directory where the topic resides, starting with root -->
  <xsl:param name="WORKDIR" select="'./'"/>
  <xsl:param name="DITAEXT" select="'.xml'"/>  
  <xsl:param name="DBG" select="'no'"/>

  <!-- Set the format for generated text for links to tables and figures.   -->
  <!-- Recognized values are 'NUMBER' (Table 5) and 'TITLE' (Table Caption) -->
  <xsl:param name="TABLELINK">NUMBER</xsl:param>
  <xsl:param name="FIGURELINK">NUMBER</xsl:param>
  
  <!-- Check whether the onlytopicinmap is turned on -->
  <xsl:param name="ONLYTOPICINMAP" select="'false'"/>
  
  <!-- Establish keys for the counting of figures, tables, and anything else -->
  <!-- To remove something from the figure count, create the same key in an override.
       Match all items to be excluded. Set the use attribute to 'exclude'. -->
  <xsl:key name="count.topic.fig"
           match="*[contains(@class, ' topic/fig ')][*[contains(@class, ' topic/title ')]]"
           use="'include'"/>
  <xsl:key name="count.topic.table"
           match="*[contains(@class, ' topic/table ')][*[contains(@class, ' topic/title ')]]"
           use="'include'"/>
  
  <xsl:key name="id" match="*[@id]" use="@id"/>
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
      <!--copy inheritable attributes that aren't already explicitly defined-->
      <!--@type|@format|@scope|@importance|@role-->

      <!--need to create type, format, scope variables regardless of whether they exist, for passing as a parameter to getstuff template-->
      <xsl:variable name="type">
        <xsl:call-template name="topicpull:inherit"><xsl:with-param name="attrib">type</xsl:with-param></xsl:call-template>
      </xsl:variable>
      <xsl:variable name="format">
        <xsl:call-template name="topicpull:inherit"><xsl:with-param name="attrib">format</xsl:with-param></xsl:call-template>
      </xsl:variable>
      <xsl:variable name="scope">
        <xsl:call-template name="topicpull:inherit"><xsl:with-param name="attrib">scope</xsl:with-param></xsl:call-template>
      </xsl:variable>

      <xsl:if test="not(@type) and $type!='#none#'">
        <xsl:attribute name="type"><xsl:value-of select="$type"/></xsl:attribute>
      </xsl:if>
      <xsl:if test="not(@format) and $format!='#none#'">
        <xsl:attribute name="format"><xsl:value-of select="$format"/></xsl:attribute>
      </xsl:if>
      <xsl:if test="not(@scope) and $scope!='#none#'">
        <xsl:attribute name="scope"><xsl:value-of select="$scope"/></xsl:attribute>
      </xsl:if>

      <xsl:if test="not(@importance)">
        <xsl:apply-templates select="." mode="topicpull:inherit-and-set-attribute"><xsl:with-param name="attrib">importance</xsl:with-param></xsl:apply-templates>
      </xsl:if>
      <xsl:if test="not(@role)">
        <xsl:apply-templates select="." mode="topicpull:inherit-and-set-attribute"><xsl:with-param name="attrib">role</xsl:with-param></xsl:apply-templates>
      </xsl:if>

      <xsl:choose>
        <xsl:when test="@type and *[contains(@class, ' topic/linktext ')] and *[contains(@class, ' topic/desc ')]">
          <xsl:apply-templates select="." mode="topicpull:add-usertext-PI"/>
          <xsl:apply-templates/>
        </xsl:when>
        <xsl:otherwise>
          <!--grab type, text and metadata, as long there's an href to grab from, otherwise error-->
          <xsl:choose>
            <xsl:when test="@href=''"/>
            <xsl:when test="@href">
              <xsl:apply-templates select="." mode="topicpull:get-stuff">
                <xsl:with-param name="localtype" select="$type"/>
                <xsl:with-param name="scope" select="$scope"/>
                <xsl:with-param name="format" select="$format"/>
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
    <xsl:param name="attrib"/>
    <xsl:apply-templates select="." mode="topicpull:inherit-from-self-then-ancestor">
      <xsl:with-param name="attrib" select="$attrib"/>
    </xsl:apply-templates>
  </xsl:template>

  <!-- Match the attribute which we are trying to inherit -->
  <xsl:template match="@*" mode="topicpull:inherit-attribute">
    <xsl:value-of select="."/>
  </xsl:template>

  <!-- If an attribute is specified locally, set it. Otherwise, try to inherit from ancestors. -->
  <xsl:template match="*" mode="topicpull:inherit-from-self-then-ancestor">
    <xsl:param name="attrib"/>
    <xsl:variable name="attrib-here">
      <xsl:if test="@*[local-name()=$attrib]"><xsl:value-of select="@*[local-name()=$attrib]"/></xsl:if>
    </xsl:variable>
    <xsl:choose>
      <!-- Any time the attribute is specified on this element, use it -->
      <xsl:when test="$attrib-here!=''"><xsl:value-of select="$attrib-here"/></xsl:when>
      <!-- Otherwise, use normal inheritance fallback -->
      <xsl:otherwise>
        <xsl:apply-templates select="." mode="topicpull:inherit-attribute">
          <xsl:with-param name="attrib" select="$attrib"/>
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
      <!-- Any time the attribute is specified on this element, use it -->
      <xsl:when test="$attrib-here!=''"><xsl:value-of select="$attrib-here"/></xsl:when>
      <!-- No ancestors left to check, so the value is not available. -->
      <xsl:when test="contains(@class,' topic/related-links ')">#none#</xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates select="parent::*" mode="topicpull:inherit-attribute">
          <xsl:with-param name="attrib" select="$attrib"/>
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
        <xsl:with-param name="attrib" select="$attrib"/>
      </xsl:apply-templates>
    </xsl:variable>
    <xsl:if test="$inherited-value!='#none#'">
      <xsl:attribute name="{$attrib}"><xsl:value-of select="$inherited-value"/></xsl:attribute>
    </xsl:if>
  </xsl:template>
  
  <!-- Process an in-line cross reference. Retrieve link text, type, and
       description if possible (and not already specified locally). -->
  <xsl:template match="*[contains(@class, ' topic/xref ')]">
    <!--<xsl:call-template name="verify-href-attribute"/>-->
    <xsl:choose>
      <xsl:when test="normalize-space(@href)='' or not(@href)">
        <xsl:if test="not(@keyref)">
          <!-- If keyref is specified, keyref code can generate message about unresolved key -->
          <xsl:apply-templates select="." mode="ditamsg:empty-href"/>
        </xsl:if>
        <xsl:copy>
          <xsl:apply-templates select="@*"/>
          <xsl:apply-templates select="." mode="topicpull:add-usertext-PI"/>
          <xsl:apply-templates select="*|comment()|processing-instruction()|text()"/>
        </xsl:copy>
      </xsl:when>
      <!-- replace "*|text()" with "normalize-space()" to handle xref without 
        valid link content, in this situation, the xref linktext should be 
        grabbed from href target. -->
      <!-- replace normalize-space() with test for actual valid content. If there is link text
           and a <desc> for hover help, do not try to retrieve anything. -->
      <xsl:when test="(text()|*[not(contains(@class,' topic/desc '))]) and *[contains(@class,' topic/desc ')]">
        <xsl:copy>
          <xsl:apply-templates select="@*"/>
          <xsl:apply-templates select="." mode="topicpull:add-usertext-PI"/>
          <xsl:apply-templates select="*|comment()|processing-instruction()|text()"/>
        </xsl:copy>
      </xsl:when>
      <xsl:when test="@href and not(@href='')">
        <xsl:copy>
          <xsl:apply-templates select="@*"/>
          <!--create variables for attributes that will be passed by parameter to the getstuff template (which is shared with link, which needs the attributes in variables to save doing inheritance checks for each one)-->
          <xsl:variable name="type">
            <xsl:choose>
              <xsl:when test="@type">
                <xsl:value-of select="@type"/>
              </xsl:when>
              <xsl:otherwise>#none#</xsl:otherwise>
            </xsl:choose>
          </xsl:variable>
          <xsl:variable name="format">
            <xsl:choose>
              <xsl:when test="@format">
                <xsl:value-of select="@format"/>
              </xsl:when>
              <xsl:otherwise>#none#</xsl:otherwise>
            </xsl:choose>
          </xsl:variable>
          <xsl:variable name="scope">
            <xsl:choose>
              <xsl:when test="@scope">
                <xsl:value-of select="@scope"/>
              </xsl:when>
              <xsl:otherwise>#none#</xsl:otherwise>
            </xsl:choose>
          </xsl:variable>
          <!--grab type, text and metadata, as long there's an href to grab from, otherwise error-->
          <xsl:apply-templates select="." mode="topicpull:get-stuff">
            <xsl:with-param name="localtype" select="$type"/>
            <xsl:with-param name="scope" select="$scope"/>
            <xsl:with-param name="format" select="$format"/>
          </xsl:apply-templates>
        </xsl:copy>
      </xsl:when>
      <!-- Ignore <xref></xref>, <xref href=""></xref> -->
      <xsl:otherwise>
        <xsl:apply-templates select="." mode="ditamsg:missing-href"/>
        <xsl:copy>
          <xsl:apply-templates select="@*"/>
          <xsl:apply-templates select="." mode="topicpull:add-usertext-PI"/>
          <xsl:apply-templates select="*|comment()|processing-instruction()|text()"/>
        </xsl:copy>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- verify the href attribute, to check whether href target can be retrieved. -->
  <xsl:template name="verify-href-attribute">
    <xsl:variable name="format">
      <xsl:choose>
        <xsl:when test="@format">
          <xsl:value-of select="@format"/>
        </xsl:when>
        <xsl:otherwise>#none#</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="scope">
      <xsl:choose>
        <xsl:when test="@scope">
          <xsl:value-of select="@scope"/>
        </xsl:when>
        <xsl:otherwise>#none#</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <!--the file name of the target, if any-->
    <xsl:variable name="file-origin">
      <xsl:apply-templates select="." mode="topicpull:get-stuff_file"/>
    </xsl:variable>
    <xsl:variable name="file">
      <xsl:call-template name="replace-blank">
        <xsl:with-param name="file-origin">
          <xsl:value-of select="translate($file-origin,'\','/')"/>
        </xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    <!--the position of the target topic relative to the current one: in the same file, referenced by id in another file, or referenced as the first topic in another file-->
    <xsl:variable name="topicpos">
      <xsl:apply-templates select="." mode="topicpull:get-stuff_topicpos"/>
    </xsl:variable>
    <!--the id of the target topic-->
    <xsl:variable name="topicid">
      <xsl:apply-templates select="." mode="topicpull:get-stuff_topicid"/>
    </xsl:variable>
    <!--the id of the target element, if any-->
    <xsl:variable name="elemid">
      <xsl:apply-templates select="." mode="topicpull:get-stuff_elemid"/>
    </xsl:variable>
    <xsl:variable name="doc" select="document($file,/)"/>
    <xsl:choose>
      <xsl:when test="not($scope='external') and not($scope='peer') and $topicpos='samefile'">
        <xsl:choose>
          <xsl:when test="$topicid='' or not(key('topic', $topicid)) or $topicid='#none#' ">
            <xsl:call-template name="output-message">
              <xsl:with-param name="msgnum">057</xsl:with-param>
              <xsl:with-param name="msgsev">W</xsl:with-param>
              <xsl:with-param name="msgparams">%1=<xsl:value-of select="@href"/>
              </xsl:with-param>
            </xsl:call-template>
          </xsl:when>
          <xsl:when test="not($elemid='') and not($elemid='#none#') and not(key('topic', $topicid)//*[@id=$elemid])">
            <xsl:call-template name="output-message">
              <xsl:with-param name="msgnum">057</xsl:with-param>
              <xsl:with-param name="msgsev">W</xsl:with-param>
              <xsl:with-param name="msgparams">%1=<xsl:value-of select="@href"/>
              </xsl:with-param>
            </xsl:call-template>
          </xsl:when>
        </xsl:choose>
      </xsl:when>
      <xsl:when test="($format='dita' or $format='DITA' or $format='#none#') and not($scope='external') and not($scope='peer') and $topicpos='otherfile' and not(contains(@href,'://'))">
        <xsl:choose>
          <xsl:when test="not($doc) or not($doc/*/*)">
            <xsl:call-template name="output-message">
              <xsl:with-param name="msgnum">057</xsl:with-param>
              <xsl:with-param name="msgsev">W</xsl:with-param>
              <xsl:with-param name="msgparams">%1=<xsl:value-of select="@href"/>
              </xsl:with-param>
            </xsl:call-template>
          </xsl:when>
          <xsl:when test="not($doc//*[contains(@class,' topic/topic ')][@id=$topicid])">
            <xsl:call-template name="output-message">
              <xsl:with-param name="msgnum">057</xsl:with-param>
              <xsl:with-param name="msgsev">W</xsl:with-param>
              <xsl:with-param name="msgparams">%1=<xsl:value-of select="@href"/>
              </xsl:with-param>
            </xsl:call-template>
          </xsl:when>
          <xsl:when test="not($elemid='') and not($elemid='#none#') and not(key('topic', $topicid)//*[@id=$elemid])">
            <xsl:call-template name="output-message">
              <xsl:with-param name="msgnum">057</xsl:with-param>
              <xsl:with-param name="msgsev">W</xsl:with-param>
              <xsl:with-param name="msgparams">%1=<xsl:value-of select="@href"/>
              </xsl:with-param>
            </xsl:call-template>
          </xsl:when>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <xsl:if test="($format='dita' or $format='DITA' or $format='#none#') and not($scope='external') and not($scope='peer') and not($doc) and not(contains(@href,'://'))">
          <xsl:call-template name="output-message">
            <xsl:with-param name="msgnum">057</xsl:with-param>
            <xsl:with-param name="msgsev">W</xsl:with-param>
            <xsl:with-param name="msgparams">%1=<xsl:value-of select="@href"/>
            </xsl:with-param>
          </xsl:call-template>
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  
  <!-- Verify that a locally specified type attribute matches the determined target type.
       If it does not, generate a message. -->
  <xsl:template match="*" mode="topicpull:verify-type-attribute">
    <xsl:param name="type"/>            <!-- Type value specified on the link -->
    <xsl:param name="actual-class"/>    <!-- Class value of the target element -->
    <xsl:param name="actual-name"/>     <!-- Name of the target element -->
    <xsl:param name="targetting"/>      <!-- Targetting a "topic" or "element" -->
    <xsl:choose>
      <!-- The type is correct; concept typed as concept, newtype defined as newtype -->
      <xsl:when test="$type=$actual-name"/>
      <!-- If the actual class contains the specified type; reference can be called topic,
         specializedReference can be called reference -->
      <xsl:when test="($targetting='topic' and contains($actual-class,concat(' ',$type,'/',$type,' '))) or
                      ($targetting='element' and contains($actual-class,concat('/',$type,' ')))">
        <xsl:apply-templates select="." mode="ditamsg:type-attribute-not-specific">
          <xsl:with-param name="targetting" select="$targetting"/>
          <xsl:with-param name="type" select="$type"/>
          <xsl:with-param name="actual-name" select="$actual-name"/>
        </xsl:apply-templates>
      </xsl:when>
      <!-- Otherwise: incorrect type is specified -->
      <xsl:otherwise>
        <xsl:apply-templates select="." mode="ditamsg:type-attribute-incorrect">
          <xsl:with-param name="targetting" select="$targetting"/>
          <xsl:with-param name="type" select="$type"/>
          <xsl:with-param name="actual-name" select="$actual-name"/>
        </xsl:apply-templates>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Get link text, type, and short description for a link or cross reference.
       If specified locally, use the local value, otherwise retrieve from target. -->
  <xsl:template match="*" mode="topicpull:get-stuff">
    <xsl:param name="localtype">#none#</xsl:param>
    <xsl:param name="scope">#none#</xsl:param>
    <xsl:param name="format">#none#</xsl:param>
    <!--the file name of the target, if any-->
    <xsl:variable name="file-origin"><xsl:apply-templates select="." mode="topicpull:get-stuff_file"/></xsl:variable>
    <xsl:variable name="file">
      <xsl:call-template name="replace-blank">
        <xsl:with-param name="file-origin">
          <xsl:value-of select="translate($file-origin,'\','/')"/>
        </xsl:with-param>
      </xsl:call-template>
    </xsl:variable>
    
    <!--the position of the target topic relative to the current one: in the same file, referenced by id in another file, or referenced as the first topic in another file-->
    <xsl:variable name="topicpos"><xsl:apply-templates select="." mode="topicpull:get-stuff_topicpos"/></xsl:variable>

    <xsl:apply-templates select="." mode="topicpull:get-stuff_verify-target-present">
      <xsl:with-param name="topicpos" select="$topicpos"/>
      <xsl:with-param name="scope" select="$scope"/>
      <xsl:with-param name="format" select="$format"/>
      <xsl:with-param name="file" select="$file"/>
    </xsl:apply-templates>

    <!--the id of the target topic-->
    <xsl:variable name="topicid">
      <xsl:apply-templates select="." mode="topicpull:get-stuff_topicid"/>
    </xsl:variable>
    <!--the id of the target element, if any-->
    <xsl:variable name="elemid">
      <xsl:apply-templates select="." mode="topicpull:get-stuff_elemid"/>
    </xsl:variable>
    <!--type - grab type from target, if not defined locally -->
    <xsl:variable name="type">
      <xsl:apply-templates select="." mode="topicpull:get-stuff_get-type">
        <xsl:with-param name="localtype" select="$localtype"/>
        <xsl:with-param name="scope" select="$scope"/>
        <xsl:with-param name="format" select="$format"/>
        <xsl:with-param name="topicpos" select="$topicpos"/>
        <xsl:with-param name="file" select="$file"/>
        <xsl:with-param name="topicid" select="$topicid"/>
        <xsl:with-param name="elemid" select="$elemid"/>
      </xsl:apply-templates>
    </xsl:variable>

    <!--now, create the type attribute, if the type attribute didn't exist locally but was retrieved successfully-->
    <xsl:if test="$localtype='#none#' and not($type='#none#')">
      <xsl:attribute name="type"><xsl:value-of select="$type"/></xsl:attribute>
    </xsl:if>

    <!-- Verify that the type was correct, if specified locally, and DITA target is available -->
    <xsl:apply-templates select="." mode="topicpull:get-stuff_verify-type">
      <xsl:with-param name="localtype" select="$localtype"/>
      <xsl:with-param name="scope" select="$scope"/>
      <xsl:with-param name="format" select="$format"/>
      <xsl:with-param name="topicpos" select="$topicpos"/>
      <xsl:with-param name="file" select="$file"/>
      <xsl:with-param name="topicid" select="$topicid"/>
      <xsl:with-param name="elemid" select="$elemid"/>
    </xsl:apply-templates>

    <!--create class value string implied by the link's type, used for comparison with class strings in the target topic for validation-->
    <xsl:variable name="classval">
      <xsl:apply-templates select="." mode="topicpull:get-stuff_classval"><xsl:with-param name="type" select="$type"/></xsl:apply-templates>
    </xsl:variable>

    <!--linktext-->
    <xsl:apply-templates select="." mode="topicpull:get-stuff_get-linktext">
      <xsl:with-param name="type" select="$type"/>
      <xsl:with-param name="scope" select="$scope"/>
      <xsl:with-param name="format" select="$format"/>
      <xsl:with-param name="topicpos" select="$topicpos"/>
      <xsl:with-param name="file" select="$file"/>
      <xsl:with-param name="topicid" select="$topicid"/>
      <xsl:with-param name="elemid" select="$elemid"/>
      <xsl:with-param name="classval" select="$classval"/>
    </xsl:apply-templates>

    <!-- shortdesc -->
    <xsl:apply-templates select="." mode="topicpull:get-stuff_get-shortdesc">
      <xsl:with-param name="type" select="$type"/>
      <xsl:with-param name="scope" select="$scope"/>
      <xsl:with-param name="format" select="$format"/>
      <xsl:with-param name="topicpos" select="$topicpos"/>
      <xsl:with-param name="file" select="$file"/>
      <xsl:with-param name="topicid" select="$topicid"/>
      <xsl:with-param name="elemid" select="$elemid"/>
      <xsl:with-param name="classval" select="$classval"/>
    </xsl:apply-templates>
  </xsl:template>

  <!-- ******************************************************************************
       Individual portions of get-stuff processing, broken apart for easy overriding.
       If a template overrides all of get-stuff, most of these templates can still be
       used so as to avoid duplicating processing code in the override.
       ************************************************************************** -->

  <!-- Get the file name for a reference that goes out of the file -->
  <xsl:template match="*" mode="topicpull:get-stuff_file">
    <!--xsl:param name="WORKDIR">
      <xsl:apply-templates select="/processing-instruction()" mode="get-work-dir"/>
    </xsl:param-->
    <xsl:param name="WORKDIR">
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
        <xsl:value-of select="substring-before(@href,'#')"/>
      </xsl:when>
      <xsl:when test="contains(@href,'://')">
        <xsl:value-of select="@href"/>
      </xsl:when>
      <xsl:when test="contains(@href,'#')">
        <xsl:value-of select="$WORKDIR"/>
        <xsl:value-of select="substring-before(@href,'#')"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$WORKDIR"/>
        <xsl:value-of select="@href"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Determine where the target of a reference exists -->
  <xsl:template match="*" mode="topicpull:get-stuff_topicpos">
    <xsl:choose>
      <xsl:when test="starts-with(@href,'#')">samefile</xsl:when>
      <xsl:when test="contains(@href,'#')">otherfile</xsl:when>
      <xsl:otherwise>firstinfile</xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- If target is in another file, warn when that file cannot be found -->
  <xsl:template match="*" mode="topicpull:get-stuff_verify-target-present">
    <xsl:param name="topicpos"/>
    <xsl:param name="scope"/>
    <xsl:param name="format"/>
    <xsl:param name="file"/>
    <xsl:if test="$topicpos!='samefile' and
                 ($scope!='external' and $scope!='peer') and
                 ($format='dita' or $format='DITA' or $format='#none#')">
      <xsl:variable name="doc" select="document($file,/)"/>
      <xsl:if test="not($doc) or not($doc/*/*)">
        <xsl:apply-templates select="." mode="ditamsg:missing-href-target">
          <xsl:with-param name="file" select="$file"/>
        </xsl:apply-templates>
      </xsl:if>
    </xsl:if>
  </xsl:template>

  <!-- Pick out the topic ID from an href value -->
  <xsl:template match="*" mode="topicpull:get-stuff_topicid">
    <xsl:choose>
      <xsl:when test="contains(@href,'#') and contains(substring-after(@href,'#'),'/')">
        <xsl:value-of select="substring-before(substring-after(@href,'#'),'/')"/>
      </xsl:when>
      <xsl:when test="contains(@href,'#')">
        <xsl:value-of select="substring-after(@href,'#')"/>
      </xsl:when>
      <xsl:otherwise>#none#</xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Pick out the element ID from an href value -->
  <xsl:template match="*" mode="topicpull:get-stuff_elemid">
    <xsl:choose>
      <xsl:when test="contains(@href,'#') and contains(substring-after(@href,'#'),'/')">
        <xsl:value-of select="substring-after(substring-after(@href,'#'),'/')"/>
      </xsl:when>
      <xsl:otherwise>#none#</xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Get the type from target, if not defined locally -->
  <xsl:template match="*" mode="topicpull:get-stuff_get-type">
    <xsl:param name="localtype"/>
    <xsl:param name="scope"/>
    <xsl:param name="format"/>
    <xsl:param name="topicpos"/>
    <xsl:param name="file"/>
    <xsl:param name="topicid"/>
    <xsl:param name="elemid"/>
    <xsl:choose>
      <!--just use localtype if it's not "none"-->
      <xsl:when test="not($localtype='#none#')"><xsl:value-of select="$localtype"/></xsl:when>
      <!--check whether it's worth trying to retrieve-->
      <xsl:when test="$scope='external' or $scope='peer' or not($format='#none#' or $format='dita' or $format='DITA')"><xsl:text>#none#</xsl:text><!--type is unavailable--></xsl:when>
      <!-- If this is an empty href, ignore it; we already put out a message -->
      <xsl:when test="@href=''"/>
      <!--check whether file extension is correct, for targets in other files-->
      <!--xsl:when test="not($topicpos='samefile') and not(contains($file,$DITAEXT))">
        <xsl:text>#none#</xsl:text>
        <xsl:apply-templates select="." mode="ditamsg:unknown-extension"/>
      </xsl:when-->

      <!--grab from target topic-->
      <xsl:when test="$elemid='#none#'">
        <xsl:apply-templates select="." mode="topicpull:get-stuff_get-type-without-elemid">
          <xsl:with-param name="topicpos" select="$topicpos"/>
          <xsl:with-param name="file" select="$file"/>
          <xsl:with-param name="topicid" select="$topicid"/>
        </xsl:apply-templates>
      </xsl:when>

      <!--grab type from target element-->
      <xsl:when test="$localtype='#none#'">
        <xsl:apply-templates select="." mode="topicpull:get-stuff_get-type-with-elemid">
          <xsl:with-param name="topicpos" select="$topicpos"/>
          <xsl:with-param name="file" select="$file"/>
          <xsl:with-param name="topicid" select="$topicid"/>
          <xsl:with-param name="elemid" select="$elemid"/>
        </xsl:apply-templates>
      </xsl:when>

      <xsl:otherwise>
        <!--tested both conditions for localtype (exists or not), so no otherwise-->
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Get the type when pointing to a topic level element -->
  <xsl:template match="*" mode="topicpull:get-stuff_get-type-without-elemid">
    <xsl:param name="topicpos"/>
    <xsl:param name="file"/>
    <xsl:param name="topicid"/>
    <xsl:variable name="doc" select="document($file,/)"/>
    <xsl:choose>
      <xsl:when test="$topicpos='samefile'">
        <xsl:choose>
          <xsl:when test="key('topic', $topicid)">
            <xsl:value-of select="local-name(key('topic', $topicid))"/>
          </xsl:when>
          <!--type could not be retrieved-->
          <xsl:otherwise>#none#</xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:when test="$topicpos='otherfile'">
        <xsl:choose>
          <xsl:when test="$doc//*[contains(@class, ' topic/topic ')][@id=$topicid]">
            <xsl:value-of select="local-name($doc//*[contains(@class, ' topic/topic ')][@id=$topicid])"/>
          </xsl:when>
          <!--type could not be retrieved-->
          <xsl:otherwise>#none#</xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:when test="$topicpos='firstinfile'">
        <xsl:choose>
          <xsl:when test="$doc//*[contains(@class, ' topic/topic ')][1]">
            <xsl:value-of select="local-name(($doc//*[contains(@class, ' topic/topic ')])[1])"/>
          </xsl:when>
          <!--type could not be retrieved-->
          <xsl:otherwise>#none#</xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <!--never happens - all three values for topicpos are tested-->
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Get the type from an element within topic -->
  <xsl:template match="*" mode="topicpull:get-stuff_get-type-with-elemid">
    <xsl:param name="topicpos"/>
    <xsl:param name="file"/>
    <xsl:param name="topicid"/>
    <xsl:param name="elemid"/>
    <xsl:variable name="doc" select="document($file,/)"/>
    <xsl:choose>
      <xsl:when test="$topicpos='samefile'">
        <xsl:choose>
          <xsl:when test="key('topic', $topicid)/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')]//*[@id=$elemid]">
            <xsl:value-of select="local-name(key('topic', $topicid)/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')]//*[@id=$elemid])"/>
          </xsl:when>
          <!--type could not be retrieved-->
          <xsl:otherwise>#none#</xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:when test="$topicpos='otherfile'">
        <xsl:choose>
          <xsl:when test="$doc//*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')]//*[@id=$elemid]">
            <xsl:value-of select="local-name($doc//*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')]//*[@id=$elemid])"/>
          </xsl:when>
          <!--type could not be retrieved-->
          <xsl:otherwise>#none#</xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <!--never happens - must be either same file or other file, firstinfile not possible if there's an element id present-->
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Find the class attribute of the reference topic. Determine if the specified type
       exists in that class attribute. -->
  <xsl:template match="*" mode="topicpull:get-stuff_verify-type">
    <xsl:param name="localtype"/>
    <xsl:param name="scope"/>
    <xsl:param name="format"/>
    <xsl:param name="topicpos"/>
    <xsl:param name="file"/>
    <xsl:param name="topicid"/>
    <xsl:param name="elemid"/>
    <xsl:if test="$localtype!='#none#' and 
                  not(@scope='external' or @scope='peer') and 
                  ((not(@format) or @format='dita' or @format='DITA') or starts-with(@href,'#'))">
      <xsl:variable name="doc" select="document($file,/)"/>
      <xsl:choose>
        <!-- If this is an xref, there can't be any elements or text inside -->
        <xsl:when test="contains(@class,' topic/xref ') and not(*[not(contains(@class,' topic/desc '))]|text())">
          <xsl:choose>
            <!-- targetting an element in the same file (not a topic) -->
            <xsl:when test="$topicpos='samefile' and $elemid!='#none#' and key('topic', $topicid)/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')]//*[@id=$elemid]">
              <xsl:apply-templates select="." mode="topicpull:verify-type-attribute">
                <xsl:with-param name="type" select="$localtype"/>
                <xsl:with-param name="actual-class" select="key('topic', $topicid)/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')]//*[@id=$elemid][1]/@class"/>
                <xsl:with-param name="actual-name" select="local-name(key('topic', $topicid)/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')]//*[@id=$elemid][1])"/>
                <xsl:with-param name="targetting">element</xsl:with-param>
              </xsl:apply-templates>
            </xsl:when>
            <!-- targetting a topic in the same file -->
            <xsl:when test="$topicpos='samefile' and $elemid='#none#' and key('topic', $topicid)">
              <xsl:apply-templates select="." mode="topicpull:verify-type-attribute">
                <xsl:with-param name="type" select="$localtype"/>
                <xsl:with-param name="actual-class" select="key('topic', $topicid)[1]/@class"/>
                <xsl:with-param name="actual-name" select="local-name(key('topic', $topicid)[1])"/>
                <xsl:with-param name="targetting">topic</xsl:with-param>
              </xsl:apply-templates>
            </xsl:when>
            <!-- targetting an element in another  file (not a topic) -->
            <xsl:when test="$topicpos='otherfile' and $elemid!='#none#' and $doc//*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')]//*[@id=$elemid]">
              <xsl:apply-templates select="." mode="topicpull:verify-type-attribute">
                <xsl:with-param name="type" select="$localtype"/>
                <xsl:with-param name="actual-class" select="$doc//*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')]//*[@id=$elemid][1]/@class"/>
                <xsl:with-param name="actual-name" select="local-name($doc//*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')]//*[@id=$elemid][1])"/>
                <xsl:with-param name="targetting">element</xsl:with-param>
              </xsl:apply-templates>
            </xsl:when>
            <!-- targetting a topic in another file -->
            <xsl:when test="$topicpos='otherfile' and $elemid='#none#' and $doc//*[contains(@class, ' topic/topic ')][@id=$topicid]">
              <xsl:apply-templates select="." mode="topicpull:verify-type-attribute">
                <xsl:with-param name="type" select="$localtype"/>
                <xsl:with-param name="actual-class" select="$doc//*[contains(@class, ' topic/topic ')][@id=$topicid][1]/@class"/>
                <xsl:with-param name="actual-name" select="local-name($doc//*[contains(@class, ' topic/topic ')][@id=$topicid][1])"/>
                <xsl:with-param name="targetting">topic</xsl:with-param>
              </xsl:apply-templates>
            </xsl:when>
            <!-- targetting a topic in another file -->
            <xsl:when test="$topicpos='firstinfile' and $doc//*[contains(@class, ' topic/topic ')]">
              <xsl:apply-templates select="." mode="topicpull:verify-type-attribute">
                <xsl:with-param name="type" select="$localtype"/>
                <xsl:with-param name="actual-class" select="$doc//*[contains(@class, ' topic/topic ')][1]/@class"/>
                <xsl:with-param name="actual-name" select="local-name($doc//*[contains(@class, ' topic/topic ')][1])"/>
                <xsl:with-param name="targetting">topic</xsl:with-param>
              </xsl:apply-templates>
            </xsl:when>
          </xsl:choose>
        </xsl:when>
        <!-- If this is a link, linktext, linkdesc, or @type must be missing.
           There should not be any links with element IDs, but put in the check just to be sure. -->
        <!-- If linktext, desc, and @type are all specified, we won't be here, so assume something is not specified. -->
        <xsl:when test="contains(@class,' topic/link ')">
          <xsl:choose>
            <!-- If there is a link to an element (error condition, so skip) -->
            <xsl:when test="$elemid!='#none#'"/>
            <!-- If we know this link came from a map, it has already been checked -->
            <xsl:when test="contains(@xtrf,'.ditamap')"/>
            <xsl:when test="ancestor::*[contains(@class, ' topic/linkpool ')]/@mapkeyref"/>
            <xsl:otherwise>
              <xsl:choose>
                <!-- targetting a topic in this file -->
                <xsl:when test="$topicpos='samefile' and key('topic', $topicid)">
                  <xsl:apply-templates select="." mode="topicpull:verify-type-attribute">
                    <xsl:with-param name="type" select="$localtype"/>
                    <xsl:with-param name="actual-class" select="key('topic', $topicid)[1]/@class"/>
                    <xsl:with-param name="actual-name" select="local-name(key('topic', $topicid)[1])"/>
                    <xsl:with-param name="targetting">topic</xsl:with-param>
                  </xsl:apply-templates>
                </xsl:when>
                <!-- targetting a topic in another file -->
                <xsl:when test="$topicpos='otherfile' and $doc//*[contains(@class, ' topic/topic ')][@id=$topicid]">
                  <xsl:apply-templates select="." mode="topicpull:verify-type-attribute">
                    <xsl:with-param name="type" select="$localtype"/>
                    <xsl:with-param name="actual-class" select="$doc//*[contains(@class, ' topic/topic ')][@id=$topicid][1]/@class"/>
                    <xsl:with-param name="actual-name" select="local-name($doc//*[contains(@class, ' topic/topic ')][@id=$topicid][1])"/>
                    <xsl:with-param name="targetting">topic</xsl:with-param>
                  </xsl:apply-templates>
                </xsl:when>
                <!-- targetting the first topic in another file -->
                <xsl:when test="$topicpos='firstinfile' and $doc//*[contains(@class, ' topic/topic ')]">
                  <xsl:apply-templates select="." mode="topicpull:verify-type-attribute">
                    <xsl:with-param name="type" select="$localtype"/>
                    <xsl:with-param name="actual-class" select="$doc//*[contains(@class, ' topic/topic ')][1]/@class"/>
                    <xsl:with-param name="actual-name" select="local-name($doc//*[contains(@class, ' topic/topic ')][1])"/>
                    <xsl:with-param name="targetting">topic</xsl:with-param>
                  </xsl:apply-templates>
                </xsl:when>
              </xsl:choose>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:when>
      </xsl:choose>
    </xsl:if>
  </xsl:template>

  <!--template used to construct the class value used to validate link targets against source types. -->
  <xsl:template match="*" mode="topicpull:get-stuff_classval">
    <xsl:param name="type">#none#</xsl:param>
    <xsl:choose>
      <!--if type doesn't exist, assume target is a topic of some kind-->
      <xsl:when test="$type='#none#'"><xsl:text> topic/topic </xsl:text></xsl:when>
      <!--if there is an element id, construct a partial classvalue and just use that-->
      <xsl:when test="contains(@href,'#') and contains(substring-after(@href,'#'),'/')">
        <xsl:text>/</xsl:text><xsl:value-of select="$type"/><xsl:text> </xsl:text>
      </xsl:when>
      <!-- otherwise there's a type but no element id, so construct a root element classvalue, eg task/task or concept/concept-->
      <xsl:otherwise>
        <xsl:text> </xsl:text>
        <xsl:value-of select="$type"/>/<xsl:value-of select="$type"/>
        <xsl:text> </xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Get the short description for a link or xref -->
  <xsl:template match="*" mode="topicpull:get-stuff_get-shortdesc">
    <xsl:param name="type"/>
    <xsl:param name="scope"/>
    <xsl:param name="format"/>
    <xsl:param name="topicpos"/>
    <xsl:param name="file"/>
    <xsl:param name="topicid"/>
    <xsl:param name="elemid"/>
    <xsl:param name="classval"/>
    <xsl:choose>
      <!--if there's already a desc, copy it-->
      <xsl:when test="*[contains(@class, ' topic/desc ')]">
        <xsl:apply-templates select="." mode="topicpull:add-usershortdesc-PI"/>
        <xsl:apply-templates select="*[contains(@class, ' topic/desc ')]"/>
      </xsl:when>
      <!--if the target is inaccessible, don't do anything - shortdesc is optional -->
      <xsl:when test="$scope='external' or $scope='peer' or $type='external' or not($format='#none#' or $format='dita' or $format='DITA')"/>
      <!--otherwise try pulling shortdesc from target-->
      <xsl:otherwise>
        <xsl:variable name="shortdesc">
          <xsl:apply-templates select="." mode="topicpull:getshortdesc">
            <xsl:with-param name="file" select="$file"/>
            <xsl:with-param name="topicpos" select="$topicpos"/>
            <xsl:with-param name="classval" select="$classval"/>
            <xsl:with-param name="topicid" select="$topicid"/>
            <xsl:with-param name="elemid" select="$elemid"/>
          </xsl:apply-templates>
        </xsl:variable>
        <xsl:if test="not($shortdesc='#none#')">
          <xsl:apply-templates select="." mode="topicpull:add-genshortdesc-PI"/>
          <desc class="- topic/desc ">
            <xsl:choose>
              <xsl:when test="number(system-property('xsl:version')) >= 2.0">
                <xsl:apply-templates select="$shortdesc"/>
              </xsl:when>
              <xsl:otherwise>
                <xsl:apply-templates select="exsl:node-set($shortdesc)"/>
              </xsl:otherwise>
            </xsl:choose>
          </desc>
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Get the link text for a link or cross reference. If specified locally, use that. Otherwise,
       work with the target to get the text. -->
  <xsl:template match="*" mode="topicpull:get-stuff_get-linktext">
    <xsl:param name="type"/>
    <xsl:param name="scope"/>
    <xsl:param name="format"/>
    <xsl:param name="topicpos"/>
    <xsl:param name="file"/>
    <xsl:param name="topicid"/>
    <xsl:param name="elemid"/>
    <xsl:param name="classval"/>
    <xsl:choose>
      <xsl:when test="contains(@class,' topic/link ') and *[not(contains(@class, ' topic/desc '))]">
        <xsl:apply-templates select="." mode="topicpull:add-usertext-PI"/>
        <xsl:apply-templates select="*[not(contains(@class, ' topic/desc '))]|comment()|processing-instruction()"/>
      </xsl:when>
      <xsl:when test="contains(@class,' topic/xref ') and (normalize-space(text())!='' or *[not(contains(@class, ' topic/desc '))])">
        <xsl:apply-templates select="." mode="topicpull:add-usertext-PI"/>
        <xsl:apply-templates select="text()|*[not(contains(@class, ' topic/desc '))]|comment()|processing-instruction()"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:variable name="linktext">
          <xsl:choose>
            <!--when type is external, or format is defaulted to not-DITA 
                (because scope is external), or format is explicitly something 
                non-DITA, use the href value with no error message-->
            <xsl:when test="$type='external' or 
                            ($scope='external' and $format='#none#') or 
                            not($format='#none#' or $format='dita' or $format='DITA')">
              <xsl:value-of select="@href"/>
            </xsl:when>
            <!--when scope is external or peer and format is DITA, don't use
              the href - defer to the final output process - and leave it 
              to the final output process to emit an error msg-->
            <xsl:when test="$scope='peer' or $scope='external'">#none#</xsl:when>
            <xsl:when test="@href=''">#none#</xsl:when>

            <!--when format is DITA, it's a different file, and file extension 
              is wrong, use the href and generate an error -->
            <!--xsl:when test="not($topicpos='samefile') and not(contains($file,$DITAEXT))">
              <xsl:value-of select="@href"/>
              <xsl:apply-templates select="." mode="ditamsg:unknown-extension"/>
            </xsl:when-->
            <!-- otherwise pull text from the target -->
            <xsl:otherwise>
              <xsl:apply-templates select="." mode="topicpull:getlinktext">
                <xsl:with-param name="file" select="$file"/>
                <xsl:with-param name="topicpos" select="$topicpos"/>
                <xsl:with-param name="classval" select="$classval"/>
                <xsl:with-param name="topicid" select="$topicid"/>
                <xsl:with-param name="elemid" select="$elemid"/>
              </xsl:apply-templates>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:if test="not($linktext='#none#') and contains(@class, ' topic/xref ')">
          <!-- need to avoid flattening complex markup here-->
          <xsl:apply-templates select="." mode="topicpull:add-gentext-PI"/>
          <xsl:value-of select="$linktext"/>
        </xsl:if>
        <xsl:if test="not($linktext='#none#') and contains(@class, ' topic/link ')">
          <xsl:apply-templates select="." mode="topicpull:add-gentext-PI"/>
          <linktext class="- topic/linktext ">
            <xsl:value-of select="$linktext"/>
          </linktext>
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Called when retrieving text for a link or xref. Determine if the reference
       points to a topic, or to an element, and process accordingly. -->
  <xsl:template match="*" mode="topicpull:getlinktext">
    <xsl:param name="file">#none#</xsl:param>
    <xsl:param name="topicpos">#none#</xsl:param>
    <xsl:param name="classval">#none#</xsl:param>
    <xsl:param name="topicid">#none#</xsl:param>
    <xsl:param name="elemid">#none#</xsl:param>
    <xsl:choose>
      <xsl:when test="contains(@href,'#') and contains(substring-after(@href,'#'),'/')">
        <!-- Points to an element within a topic -->
        <xsl:apply-templates select="." mode="topicpull:getlinktext_within-topic">
          <xsl:with-param name="file" select="$file"/>
          <xsl:with-param name="topicpos" select="$topicpos"/>
          <xsl:with-param name="classval" select="$classval"/>
          <xsl:with-param name="topicid" select="$topicid"/>
          <xsl:with-param name="elemid" select="$elemid"/>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:otherwise>
        <!-- Points to a topic, not an element within a topic -->
        <xsl:apply-templates select="." mode="topicpull:getlinktext_topic">
          <xsl:with-param name="file" select="$file"/>
          <xsl:with-param name="topicpos" select="$topicpos"/>
          <xsl:with-param name="classval" select="$classval"/>
          <xsl:with-param name="topicid" select="$topicid"/>
        </xsl:apply-templates>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>


  <!-- Get the link text for a reference to a topic. Already verified that
       topic should be available (scope!=external, etc). -->
  <xsl:template match="*" mode="topicpull:getlinktext_topic">
    <xsl:param name="file">#none#</xsl:param>
    <xsl:param name="topicpos">#none#</xsl:param>
    <xsl:param name="classval">#none#</xsl:param>
    <xsl:param name="topicid">#none#</xsl:param>
    <xsl:choose>
      <!--targetting a topic in the same file-->
      <xsl:when test="$topicpos='samefile'">
        <xsl:apply-templates select="." mode="topicpull:getlinktext-samefile">
          <xsl:with-param name="classval" select="$classval"/>
          <xsl:with-param name="topicid" select="$topicid"/>
        </xsl:apply-templates>
      </xsl:when>
      <!--targetting the first topic in a target file-->
      <xsl:when test="$topicpos='firstinfile'">
        <xsl:apply-templates select="." mode="topicpull:getlinktext-firstinfile">
          <xsl:with-param name="file" select="$file"/>
          <xsl:with-param name="classval" select="$classval"/>
        </xsl:apply-templates>
      </xsl:when>
      <!--targetting a particular topic in another file-->
      <xsl:when test="$topicpos='otherfile'">
        <xsl:apply-templates select="." mode="topicpull:getlinktext-otherfile">
          <xsl:with-param name="file" select="$file"/>
          <xsl:with-param name="classval" select="$classval"/>
          <xsl:with-param name="topicid" select="$topicid"/>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:otherwise>
        <!--tested all three values for topicpos, for both topics and elements - no otherwise-->
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Get the link text from a topic within the same file as the link. -->
  <xsl:template match="*" mode="topicpull:getlinktext-samefile">
    <xsl:param name="classval"/>
    <xsl:param name="topicid"/>
    <xsl:choose>
      <xsl:when test="key('id', $topicid)[contains(@class, $classval)]/*[contains(@class, ' topic/title ')]">
        <xsl:variable name="target-text">
          <xsl:apply-templates
            select="(key('id', $topicid)[contains(@class, $classval)])[1]/*[contains(@class, ' topic/title ')]" mode="text-only"/>
        </xsl:variable>
        <xsl:value-of select="normalize-space($target-text)"/>
      </xsl:when>
      <xsl:when test="key('topic', $topicid)">
        <xsl:variable name="target-text">
          <xsl:apply-templates
            select="key('topic', $topicid)[1]/*[contains(@class, ' topic/title ')]" mode="text-only"/>
        </xsl:variable>
        <xsl:value-of select="normalize-space($target-text)"/>
      </xsl:when>
      <!--if can't retrieve, output href as linktext, emit message. since href doesn't include file name, no issues with file extension-->
      <xsl:otherwise>
        <xsl:value-of select="@href"/>
        <xsl:apply-templates select="." mode="ditamsg:cannot-retrieve-linktext"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Get the link text from a topic when the target is just a file. -->
  <xsl:template match="*" mode="topicpull:getlinktext-firstinfile">
    <xsl:param name="file"/>
    <xsl:param name="classval"/>
    <xsl:variable name="doc" select="document($file,/)"/>
    <xsl:choose>
      <xsl:when test="$doc//*[contains(@class, $classval)][1]/*[contains(@class, ' topic/title ')]">
        <xsl:variable name="target-text">
          <xsl:apply-templates
            select="($doc//*[contains(@class, $classval)])[1]/*[contains(@class, ' topic/title ')]" mode="text-only"/>
        </xsl:variable>
        <xsl:value-of select="normalize-space($target-text)"/>
      </xsl:when>
      <xsl:when test="$doc//*[contains(@class, ' topic/topic ')]/*[contains(@class, ' topic/title ')]">
        <xsl:variable name="target-text">
          <xsl:apply-templates
            select="$doc//*[contains(@class, ' topic/topic ')][1]/*[contains(@class, ' topic/title ')]" mode="text-only"/>
        </xsl:variable>
        <xsl:value-of select="normalize-space($target-text)"/>
      </xsl:when>
      <!-- if can't retrieve, don't create the linktext - defer to the final output process, which will massage the file name-->
      <xsl:otherwise>#none#</xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Get the link text from a specific topic within another file. -->
  <xsl:template match="*" mode="topicpull:getlinktext-otherfile">
    <xsl:param name="file"/>
    <xsl:param name="classval"/>
    <xsl:param name="topicid"/>
    <xsl:variable name="doc" select="document($file,/)"/>
    <xsl:for-each select="$doc">
    <xsl:choose>
      <xsl:when test="key('id', $topicid)[contains(@class, $classval)]/*[contains(@class, ' topic/title ')]">
        <xsl:variable name="target-text">
          <xsl:apply-templates
            select="(key('id', $topicid)[contains(@class, $classval)])[1]/*[contains(@class, ' topic/title ')]" mode="text-only"/>
        </xsl:variable>
        <xsl:value-of select="normalize-space($target-text)"/>
      </xsl:when>
      <xsl:when test="key('topic', $topicid)/*[contains(@class, ' topic/title ')]">
        <xsl:variable name="target-text">
          <xsl:apply-templates
            select="key('topic', $topicid)[1]/*[contains(@class, ' topic/title ')]" mode="text-only"/>
        </xsl:variable>
        <xsl:value-of select="normalize-space($target-text)"/>
      </xsl:when>
      <!-- if can't retrieve, don't create the linktext - defer to the final output process, which will massage the file name-->
      <xsl:otherwise>#none#</xsl:otherwise>
    </xsl:choose>
    </xsl:for-each>
  </xsl:template>

  <!--get linktext for xref to any body elements. Unsure how to make this extensible,
      if a plugin needs to add support for type=step or type=newElement. May need
      to wait for XSLT 2.0 support, with fancier modes? -->
  <xsl:template match="*" mode="topicpull:getlinktext_within-topic">
    <xsl:param name="file">#none#</xsl:param>
    <xsl:param name="topicpos">#none#</xsl:param>
    <xsl:param name="classval">#none#</xsl:param>
    <xsl:param name="topicid">#none#</xsl:param>
    <xsl:param name="elemid">#none#</xsl:param>
    <xsl:variable name="doc" select="document($file,/)"/>
    <xsl:variable name="useclassval">
      <xsl:choose>
        <!--if it's a known type we can handle, use type as-is-->
        <xsl:when test="      $classval='/li '    or $classval='/fn '    or $classval='/dlentry '    or $classval='/section '   or $classval='/example '   or $classval='/fig '   or $classval='/figgroup '">
          <!--can be handled as-is-->
          <xsl:value-of select="$classval"/>
        </xsl:when>
        <!--otherwise figure out what it's topic-level equivalent is by looking it up in the target element's class value-->
        <xsl:when test="$topicpos='samefile'">
          <xsl:apply-templates select="key('topic', $topicid)/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')]//*[@id=$elemid][contains(@class, $classval)]" mode="topicpull:determine_firstclass"/>
        </xsl:when>
        <xsl:when test="$topicpos='otherfile'">
          <xsl:apply-templates select="$doc//*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')]//*[@id=$elemid][contains(@class, $classval)]" mode="topicpull:determine_firstclass"/>
        </xsl:when>
        <xsl:otherwise>
          <!--don't generate error msg, since will also be attempting retrieval of linktext, and don't want to double-up on error msgs-->
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:choose>
      <!--processing as a list item - this call only happens when the type is not defined locally or was unknown, but was retrieved from the target; when a known type is defined locally, the appropriate template is applied directly-->
      <xsl:when test="@type='li' or $useclassval='/li '">
        <xsl:apply-templates select="." mode="topicpull:getlinktext_within-topic_li">
          <xsl:with-param name="file" select="$file"/>
          <xsl:with-param name="topicpos" select="$topicpos"/>
          <xsl:with-param name="classval" select="$useclassval"/>
          <xsl:with-param name="topicid" select="$topicid"/>
          <xsl:with-param name="elemid" select="$elemid"/>
        </xsl:apply-templates>
      </xsl:when>
      <!--processing as a footnote - this call only happens when the type is not defined locally or was unknown, but was retrieved from the target; when a known type is defined locally, the appropriate template is applied directly-->
      <xsl:when test="@type='fn' or $useclassval='/fn '">
        <xsl:apply-templates select="." mode="topicpull:getlinktext_within-topic_fn">
          <xsl:with-param name="file" select="$file"/>
          <xsl:with-param name="topicpos" select="$topicpos"/>
          <xsl:with-param name="classval" select="$useclassval"/>
          <xsl:with-param name="topicid" select="$topicid"/>
          <xsl:with-param name="elemid" select="$elemid"/>
        </xsl:apply-templates>
      </xsl:when>
      <!--processing as a dlentry - this call only happens when the type is not defined locally or was unknown, but was retrieved from the target; when a known type is defined locally, the appropriate template is applied directly-->
      <xsl:when test="@type='dlentry' or $useclassval='/dlentry '">
        <xsl:apply-templates select="." mode="topicpull:getlinktext_within-topic_dlentry">
          <xsl:with-param name="file" select="$file"/>
          <xsl:with-param name="topicpos" select="$topicpos"/>
          <xsl:with-param name="classval" select="$useclassval"/>
          <xsl:with-param name="topicid" select="$topicid"/>
          <xsl:with-param name="elemid" select="$elemid"/>
        </xsl:apply-templates>
      </xsl:when>
      <!--processing as a table - this call only happens when the type is not defined locally or was unknown, but was retrieved from the target; when a known type is defined locally, the appropriate template is applied directly-->
      <xsl:when test="@type='table' or $useclassval='/table '">
        <xsl:apply-templates select="." mode="topicpull:getlinktext_within-topic_table">
          <xsl:with-param name="file" select="$file"/>
          <xsl:with-param name="topicpos" select="$topicpos"/>
          <xsl:with-param name="classval" select="$useclassval"/>
          <xsl:with-param name="topicid" select="$topicid"/>
          <xsl:with-param name="elemid" select="$elemid"/>
        </xsl:apply-templates>
      </xsl:when>
      <!--processing as a figure - this call only happens when the type is not defined locally or was unknown, but was retrieved from the target; when a known type is defined locally, the appropriate template is applied directly-->
      <xsl:when test="@type='fig' or $useclassval='/fig '">
        <xsl:apply-templates select="." mode="topicpull:getlinktext_within-topic_fig">
          <xsl:with-param name="file" select="$file"/>
          <xsl:with-param name="topicpos" select="$topicpos"/>
          <xsl:with-param name="classval" select="$useclassval"/>
          <xsl:with-param name="topicid" select="$topicid"/>
          <xsl:with-param name="elemid" select="$elemid"/>
        </xsl:apply-templates>
      </xsl:when>
      <!--if it's none of the above types, then apply generic processing - for table, fig, etc. - looking for a child title element-->
      <xsl:otherwise>
        <xsl:apply-templates select="." mode="topicpull:getlinktext_within-topic_otherblock">
          <xsl:with-param name="file" select="$file"/>
          <xsl:with-param name="topicpos" select="$topicpos"/>
          <xsl:with-param name="classval" select="$useclassval"/>
          <xsl:with-param name="topicid" select="$topicid"/>
          <xsl:with-param name="elemid" select="$elemid"/>
        </xsl:apply-templates>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--used to retrieve a topic-level type from the target element - for example, 
    if user specifies "step" as the type, this will look up its topic-level 
    equivalent - /li - and use that -->
  <xsl:template match="*" mode="topicpull:determine_firstclass">
    <xsl:text>/</xsl:text>
    <xsl:value-of select="substring-before(substring-after(@class,' topic/'),' ')"/>
    <xsl:text> </xsl:text>
  </xsl:template>

  <!-- Get link text for arbitrary block elements inside a topic. Assumes that the
       target element has a title element. -->
  <xsl:template match="*" mode="topicpull:getlinktext_within-topic_otherblock">
    <xsl:param name="file">#none#</xsl:param>
    <xsl:param name="topicpos">#none#</xsl:param>
    <xsl:param name="classval">#none#</xsl:param>
    <xsl:param name="topicid">#none#</xsl:param>
    <xsl:param name="elemid">#none#</xsl:param>
    <xsl:variable name="doc" select="document($file,/)"/>
    <xsl:choose>
      <!--look for the target in the same file, and create the linktext if accessible-->
      <xsl:when test="$topicpos='samefile' and key('topic', $topicid)/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')  or contains(@class,' topic/related-links ') ]//*[@id=$elemid][contains(@class, $classval)]/*[contains(@class,' topic/title ')][1]">
        <xsl:variable name="target-text">
          <xsl:apply-templates
            select="key('topic', $topicid)[1]/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')  or contains(@class,' topic/related-links ') ]//*[@id=$elemid][contains(@class, $classval)]/*[contains(@class,' topic/title ')][1]" mode="text-only"/>
        </xsl:variable>
        <xsl:value-of select="normalize-space($target-text)"/>
      </xsl:when>
      <!--look for the target in another file, and create the linktext if accessible-->
      <xsl:when test="$topicpos='otherfile' and $doc//*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')  or contains(@class,' topic/related-links ') ]//*[@id=$elemid][contains(@class, $classval)]/*[contains(@class,' topic/title ')][1]">
        <xsl:variable name="target-text">
          <xsl:apply-templates
            select="($doc//*[contains(@class, ' topic/topic ')][@id=$topicid])[1]/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')  or contains(@class,' topic/related-links ') ]//*[@id=$elemid][contains(@class, $classval)]/*[contains(@class,' topic/title ')][1]" mode="text-only"/>
        </xsl:variable>
        <xsl:value-of select="normalize-space($target-text)"/>
      </xsl:when>
      <!--If there isn't a title ,then process with spectitle -->
      <xsl:when test="$topicpos='samefile' and key('topic', $topicid)/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ') or contains(@class,' topic/related-links ') ] //*[@id=$elemid][contains(@class, $classval)][1][@spectitle]">
        <xsl:variable name="target-text">
          <xsl:value-of select="key('topic', $topicid)/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')  or contains(@class,' topic/related-links ') ]//*[@id=$elemid][contains(@class, $classval)][1]/@spectitle"/>
        </xsl:variable>
        <xsl:value-of select="normalize-space($target-text)"/>
      </xsl:when>
      
      <xsl:when test="$topicpos='otherfile' and $doc//*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')]//*[contains(@class, $classval)  or contains(@class,' topic/related-links ') ][@id=$elemid][1][@spectitle]">
        <xsl:variable name="target-text">
          <xsl:value-of select="$doc//*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')  or contains(@class,' topic/related-links ') ]//*[@id=$elemid][contains(@class, $classval)][1]/@spectitle"/>
        </xsl:variable>
        <xsl:value-of select="normalize-space($target-text)"/>
      </xsl:when>

      <!-- No title or spectitle; check to see if the element provides generated text -->
      <xsl:when test="$topicpos='samefile' and key('topic', $topicid)/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ') or contains(@class,' topic/related-links ') ] //*[@id=$elemid][contains(@class, $classval)][1]">
        <xsl:variable name="target-text">
          <xsl:apply-templates select="key('topic', $topicid)/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')  or contains(@class,' topic/related-links ') ]//*[@id=$elemid][contains(@class, $classval)][1]" mode="topicpull:get_generated_text"/>
        </xsl:variable>
        <xsl:choose>
          <xsl:when test="$target-text!='#none#'"><xsl:value-of select="normalize-space($target-text)"/></xsl:when>
          <xsl:otherwise><xsl:apply-templates select="." mode="topicpull:otherblock-linktext-fallback"/></xsl:otherwise>
        </xsl:choose>
      </xsl:when>

      <xsl:when test="$topicpos='otherfile' and $doc//*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')]//*[contains(@class, $classval)  or contains(@class,' topic/related-links ') ][@id=$elemid][1]">
        <xsl:variable name="target-text">
          <xsl:apply-templates select="$doc//*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')  or contains(@class,' topic/related-links ') ]//*[@id=$elemid][contains(@class, $classval)][1]" mode="topicpull:get_generated_text"/>
        </xsl:variable>
        <xsl:choose>
          <xsl:when test="$target-text!='#none#'"><xsl:value-of select="normalize-space($target-text)"/></xsl:when>
          <xsl:otherwise><xsl:apply-templates select="." mode="topicpull:otherblock-linktext-fallback"/></xsl:otherwise>
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
    <xsl:choose>
      <xsl:when test="starts-with(@href,'#')">
        <xsl:value-of select="@href"/>
      </xsl:when>
      <xsl:when test="not(@format) or @format = 'dita'">#none#</xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="@href"/>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates select="." mode="ditamsg:cannot-retrieve-linktext"/>
  </xsl:template>

  <!-- Pull link text for a figure. Uses mode="topicpull:figure-linktext" to output the text.  -->
  <xsl:template match="*" mode="topicpull:getlinktext_within-topic_fig">
    <xsl:param name="file">#none#</xsl:param>
    <xsl:param name="topicpos">#none#</xsl:param>
    <xsl:param name="classval">#none#</xsl:param>
    <xsl:param name="topicid">#none#</xsl:param>
    <xsl:param name="elemid">#none#</xsl:param>
    <xsl:variable name="doc" select="document($file,/)"/>
    <xsl:choose>
      <!--look for the target in the same file, and create the linktext if accessible-->
      <!-- and look for the target in another file, and create the linktext if accessible-->
      <!-- April 2007: replace manual language test with lang() function -->
      <xsl:when test="($topicpos='samefile' and key('topic', $topicid)/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')]//*[@id=$elemid][contains(@class, $classval)]/*[contains(@class,' topic/title ')][1])
                   or ($topicpos='otherfile' and $doc//*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')]//*[@id=$elemid][contains(@class, $classval)]/*[contains(@class,' topic/title ')][1])">
        <xsl:variable name="fig-count-actual">
          <xsl:choose>
            <xsl:when test="$topicpos='samefile'">
              <xsl:apply-templates select="key('topic', $topicid)[1]/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')]//*[@id=$elemid][contains(@class, $classval)]/*[contains(@class,' topic/title ')][1]" mode="topicpull:fignumber"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:apply-templates select="($doc//*[contains(@class, ' topic/topic ')][@id=$topicid])[1]/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')]//*[@id=$elemid][contains(@class, $classval)]/*[contains(@class,' topic/title ')][1]" mode="topicpull:fignumber"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:apply-templates select="." mode="topicpull:figure-linktext">
          <xsl:with-param name="figtext"><xsl:call-template name="getString"><xsl:with-param name="stringName" select="'Figure'"/></xsl:call-template></xsl:with-param>
          <xsl:with-param name="figcount" select="$fig-count-actual"/>
          <xsl:with-param name="figtitle">
            <xsl:choose>
              <xsl:when test="$topicpos='samefile'"><xsl:copy-of select="key('topic', $topicid)[1]/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')]//*[@id=$elemid][contains(@class, $classval)]/*[contains(@class,' topic/title ')][1]"/></xsl:when>
              <xsl:otherwise><xsl:copy-of select="($doc//*[contains(@class, ' topic/topic ')][@id=$topicid])[1]/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')]//*[@id=$elemid][contains(@class, $classval)]/*[contains(@class,' topic/title ')][1]"/></xsl:otherwise>
            </xsl:choose>
          </xsl:with-param>
        </xsl:apply-templates>
      </xsl:when>
      <!--If there isn't a title ,then process with spectitle -->
      <xsl:when test="($topicpos='samefile' and key('topic', $topicid)/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')]//*[@id=$elemid][contains(@class, $classval)][1][@spectitle])
        or ($topicpos='otherfile' and $doc//*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')]//*[@id=$elemid][contains(@class, $classval)][1][@spectitle])">
        <xsl:variable name="fig-count-actual">
          <xsl:choose>
            <xsl:when test="$topicpos='samefile'">
              <xsl:apply-templates select="key('topic', $topicid)[1]/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')]//*[@id=$elemid][contains(@class, $classval)][1][@spectitle]" mode="topicpull:fignumber"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:apply-templates select="($doc//*[contains(@class, ' topic/topic ')][@id=$topicid])[1]/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')]//*[@id=$elemid][contains(@class, $classval)][1][@spectitle]" mode="topicpull:fignumber"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:apply-templates select="." mode="topicpull:figure-linktext">
          <xsl:with-param name="figtext"><xsl:call-template name="getString"><xsl:with-param name="stringName" select="'Figure'"/></xsl:call-template></xsl:with-param>
          <xsl:with-param name="figcount" select="$fig-count-actual"/>
          <xsl:with-param name="figtitle">
            <xsl:choose>
              <xsl:when test="$topicpos='samefile'"><xsl:value-of select="key('topic', $topicid)[1]/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')]//*[@id=$elemid][contains(@class, $classval)][1]/@spectitle"/></xsl:when>
              <xsl:otherwise><xsl:value-of select="($doc//*[contains(@class, ' topic/topic ')][@id=$topicid])[1]/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')]//*[@id=$elemid][contains(@class, $classval)][1]/@spectitle"/></xsl:otherwise>
            </xsl:choose>
          </xsl:with-param>
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
        <xsl:choose>
          <xsl:when test="number(system-property('xsl:version')) >= 2.0">
            <xsl:apply-templates select="$figtitle" mode="text-only"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:apply-templates select="exsl:node-set($figtitle)" mode="text-only"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise> <!-- Default: FIGURELINK='NUMBER' -->
        <xsl:value-of select="$figtext"/>
        <xsl:call-template name="getString">
          <xsl:with-param name="stringName" select="'figure-number-separator'"/>
        </xsl:call-template>
        <xsl:value-of select="$figcount"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="*[lang('hu')]" mode="topicpull:figure-linktext">
    <!-- Hungarian: "1. Figure " -->
    <xsl:param name="figtext"/>
    <xsl:param name="figcount"/>
    <xsl:param name="figtitle"/> <!-- Currently unused, but may be picked up by an override -->
    <xsl:choose>
      <xsl:when test="$FIGURELINK='TITLE'">
        <xsl:choose>
          <xsl:when test="number(system-property('xsl:version')) >= 2.0">
            <xsl:apply-templates select="$figtitle" mode="text-only"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:apply-templates select="exsl:node-set($figtitle)" mode="text-only"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise> <!-- Default: FIGURELINK='NUMBER' -->
        <xsl:value-of select="$figcount"/>
        <xsl:call-template name="getString">
          <xsl:with-param name="stringName" select="'figure-number-separator'"/>
        </xsl:call-template>
        <xsl:value-of select="$figtext"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <!-- If the figure is unavailable or we're not sure what to do with it, generate fallback text -->
  <xsl:template match="*" mode="topicpull:figure-linktext-fallback">
    <xsl:choose>
      <xsl:when test="starts-with(@href,'#')">
        <xsl:value-of select="@href"/>
      </xsl:when>
      <xsl:when test="not(@format) or @format = 'dita'">#none#</xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="@href"/>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates select="." mode="ditamsg:cannot-retrieve-linktext"/>
  </xsl:template>

  <!-- Determine the number of the figure being linked to -->
  <xsl:template match="*[contains(@class,' topic/fig ')]/*[contains(@class,' topic/title ')] | *[contains(@class,' topic/fig ')][@spectitle]" mode="topicpull:fignumber">
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
    <xsl:param name="file">#none#</xsl:param>
    <xsl:param name="topicpos">#none#</xsl:param>
    <xsl:param name="classval">#none#</xsl:param>
    <xsl:param name="topicid">#none#</xsl:param>
    <xsl:param name="elemid">#none#</xsl:param>
    <xsl:variable name="doc" select="document($file,/)"/>
    <xsl:choose>
      <!--look for the target in the same file, and create the linktext if accessible-->
      <!-- and look for the target in another file, and create the linktext if accessible-->
      <xsl:when test="($topicpos='samefile' and key('topic', $topicid)/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')]//*[@id=$elemid][contains(@class, $classval)]/*[contains(@class,' topic/title ')][1])                        
        or ($topicpos='otherfile' and $doc//*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')]//*[@id=$elemid][contains(@class, $classval)]/*[contains(@class,' topic/title ')][1])">
        <xsl:variable name="tbl-count-actual">
          <xsl:choose>
            <xsl:when test="$topicpos='samefile'">
              <xsl:apply-templates select="key('topic', $topicid)[1]/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')]//*[@id=$elemid][contains(@class, $classval)]/*[contains(@class,' topic/title ')][1]" mode="topicpull:tblnumber"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:apply-templates select="($doc//*[contains(@class, ' topic/topic ')][@id=$topicid])[1]/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')]//*[@id=$elemid][contains(@class, $classval)]/*[contains(@class,' topic/title ')][1]" mode="topicpull:tblnumber"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:apply-templates select="." mode="topicpull:table-linktext">
          <xsl:with-param name="tbltext"><xsl:call-template name="getString"><xsl:with-param name="stringName" select="'Table'"/></xsl:call-template></xsl:with-param>
          <xsl:with-param name="tblcount" select="$tbl-count-actual"/>
          <xsl:with-param name="tbltitle">
            <xsl:choose>
              <xsl:when test="$topicpos='samefile'"><xsl:copy-of select="key('topic', $topicid)[1]/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')]//*[@id=$elemid][contains(@class, $classval)]/*[contains(@class,' topic/title ')][1]"/></xsl:when>
              <xsl:otherwise><xsl:copy-of select="($doc//*[contains(@class, ' topic/topic ')][@id=$topicid])[1]/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')]//*[@id=$elemid][contains(@class, $classval)]/*[contains(@class,' topic/title ')][1]"/></xsl:otherwise>
            </xsl:choose>
          </xsl:with-param>
        </xsl:apply-templates>
      </xsl:when>
      <!--If there isn't a title ,then process with spectitle -->
      <xsl:when test="($topicpos='samefile' and key('topic', $topicid)/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')]//*[@id=$elemid][contains(@class, $classval)][1][@spectitle])                        
        or ($topicpos='otherfile' and $doc//*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')]//*[@id=$elemid][contains(@class, $classval)][1][@spectitle])">
        <xsl:variable name="tbl-count-actual">
          <xsl:choose>
            <xsl:when test="$topicpos='samefile'">
              <xsl:apply-templates select="key('topic', $topicid)[1]/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')]//*[@id=$elemid][contains(@class, $classval)][1][@spectitle]" mode="topicpull:tblnumber"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:apply-templates select="($doc//*[contains(@class, ' topic/topic ')][@id=$topicid])[1]/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')]//*[@id=$elemid][contains(@class, $classval)][1][@spectitle]" mode="topicpull:tblnumber"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:apply-templates select="." mode="topicpull:table-linktext">
          <xsl:with-param name="tbltext"><xsl:call-template name="getString"><xsl:with-param name="stringName" select="'Table'"/></xsl:call-template></xsl:with-param>
          <xsl:with-param name="tblcount" select="$tbl-count-actual"/>
          <xsl:with-param name="tbltitle">
            <xsl:choose>
              <xsl:when test="$topicpos='samefile'"><xsl:value-of select="key('topic', $topicid)[1]/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')]//*[@id=$elemid][contains(@class, $classval)][1]/@spectitle"/></xsl:when>
              <xsl:otherwise><xsl:value-of select="($doc//*[contains(@class, ' topic/topic ')][@id=$topicid])[1]/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')]//*[@id=$elemid][contains(@class, $classval)][1]/@spectitle"/></xsl:otherwise>
            </xsl:choose>
          </xsl:with-param>
        </xsl:apply-templates>
      </xsl:when>
      
      <!--otherwise use the href, unless it contains .dita, in which case defer to the final output pass to decide what to do with the file extension-->
      <xsl:otherwise>
        <xsl:apply-templates select="." mode="topicpull:table-linktext-fallback"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Determine the text for a link to a table. Currently uses table title. -->
  <xsl:template match="*" mode="topicpull:table-linktext">
    <xsl:param name="tbltext"/>
    <xsl:param name="tblcount"/>
    <xsl:param name="tbltitle"/> <!-- Currently unused, but may be picked up by an override -->
    <xsl:choose>
      <xsl:when test="$TABLELINK='TITLE'">
        <xsl:choose>
          <xsl:when test="number(system-property('xsl:version')) >= 2.0">
            <xsl:apply-templates select="$tbltitle" mode="text-only"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:apply-templates select="exsl:node-set($tbltitle)" mode="text-only"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise> <!-- Default: TABLELINK='NUMBER' -->
        <xsl:value-of select="$tbltext"/>
        <xsl:call-template name="getString">
          <xsl:with-param name="stringName" select="'figure-number-separator'"/>
        </xsl:call-template>
        <xsl:value-of select="$tblcount"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="*[lang('hu')]" mode="topicpull:table-linktext">
    <!-- Hungarian: "1. Table" -->
    <xsl:param name="tbltext"/>
    <xsl:param name="tblcount"/>
    <xsl:param name="tbltitle"/> <!-- Currently unused, but may be picked up by an override -->
    <xsl:choose>
      <xsl:when test="$TABLELINK='TITLE'">
        <xsl:choose>
          <xsl:when test="number(system-property('xsl:version')) >= 2.0">
            <xsl:apply-templates select="$tbltitle" mode="text-only"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:apply-templates select="exsl:node-set($tbltitle)" mode="text-only"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise> <!-- Default: TABLELINK='NUMBER' -->
        <xsl:value-of select="$tblcount"/>
        <xsl:call-template name="getString">
          <xsl:with-param name="stringName" select="'figure-number-separator'"/>
        </xsl:call-template>
        <xsl:value-of select="$tbltext"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <!-- Fallback text if a table target cannot be found, or there is some other problem -->
  <xsl:template match="*" mode="topicpull:table-linktext-fallback">
    <xsl:choose>
      <xsl:when test="starts-with(@href,'#')">
        <xsl:value-of select="@href"/>
      </xsl:when>
      <xsl:when test="not(@format) or @format = 'dita'">#none#</xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="@href"/>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:apply-templates select="." mode="ditamsg:cannot-retrieve-linktext"/>
  </xsl:template>

  <!-- Determine the number of the table being linked to -->
  <xsl:template match="*[contains(@class,' topic/table ')]/*[contains(@class,' topic/title ')]  | *[contains(@class,' topic/table ')][@spectitle]" mode="topicpull:tblnumber">
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
    <xsl:param name="file">#none#</xsl:param>
    <xsl:param name="topicpos">#none#</xsl:param>
    <xsl:param name="classval">#none#</xsl:param>
    <xsl:param name="topicid">#none#</xsl:param>
    <xsl:param name="elemid">#none#</xsl:param>
    <xsl:variable name="doc" select="document($file,/)"/>
    <xsl:choose>
      <!-- If the list item exists, and is in an OL, process it -->
      <xsl:when test="$topicpos='samefile' and key('topic', $topicid)/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')]//*[contains(@class,' topic/ol ')]/*[@id=$elemid][contains(@class, $classval)]">
        <xsl:apply-templates mode="topicpull:li-linktext" select="key('topic', $topicid)/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')]//*[contains(@class,' topic/ol ')]/*[@id=$elemid][contains(@class, $classval)]"/>
      </xsl:when>
      <xsl:when test="$topicpos='otherfile' and $doc//*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')]//*[contains(@class,' topic/ol ')]/*[@id=$elemid][contains(@class, $classval)]">
        <xsl:apply-templates mode="topicpull:li-linktext" select="$doc//*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')]//*[contains(@class,' topic/ol ')]/*[@id=$elemid][contains(@class, $classval)]"/>
      </xsl:when>
      <!-- If the list item exists, but is in some other kind of list, issue a message -->
      <xsl:when test="$topicpos='samefile' and key('topic', $topicid)/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')]//*[@id=$elemid][contains(@class, $classval)]">
        <xsl:call-template name="topicpull:referenced-invalid-list-item"/>
      </xsl:when>
      <xsl:when test="$topicpos='otherfile' and $doc//*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')]//*[@id=$elemid][contains(@class, $classval)]">
        <xsl:call-template name="topicpull:referenced-invalid-list-item"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="starts-with(@href,'#')">
            <xsl:value-of select="@href"/>
          </xsl:when>
          <xsl:when test="not(@format) or @format = 'dita'">#none#</xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="@href"/>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:apply-templates select="." mode="ditamsg:cannot-retrieve-list-number"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <!-- Matching the list item, determine the count for this item -->
  <xsl:template match="*[contains(@class,' topic/ol ')]/*[contains(@class,' topic/li ')]" mode="topicpull:li-linktext">
    <xsl:number level="multiple"
      count="*[contains(@class,' topic/ol ')]/*[contains(@class,' topic/li ')]" format="1.a.i.1.a.i.1.a.i"/>
  </xsl:template>
  <!-- Instead of matching an unordered list item, we will call this template; that way
     the error points to the XREF, not to the list item. -->
  <xsl:template name="topicpull:referenced-invalid-list-item">
    <xsl:apply-templates select="." mode="ditamsg:crossref-unordered-listitem"/>
  </xsl:template>

  <!-- Generate link text for a footnote reference -->
  <xsl:template match="*" mode="topicpull:getlinktext_within-topic_fn">
    <xsl:param name="file">#none#</xsl:param>
    <xsl:param name="topicpos">#none#</xsl:param>
    <xsl:param name="classval">#none#</xsl:param>
    <xsl:param name="topicid">#none#</xsl:param>
    <xsl:param name="elemid">#none#</xsl:param>
    <xsl:variable name="doc" select="document($file,/)"/>
    <xsl:choose>
      <xsl:when test="$topicpos='samefile' and key('topic', $topicid)/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')]//*[@id=$elemid][contains(@class, $classval)]">
        <xsl:apply-templates mode="topicpull:fn-linktext" select="key('topic', $topicid)/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')]//*[@id=$elemid][contains(@class, $classval)]"/>
      </xsl:when>
      <xsl:when test="$topicpos='otherfile' and $doc//*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')]//*[@id=$elemid][contains(@class, $classval)]">
        <xsl:apply-templates mode="topicpull:fn-linktext" select="$doc//*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')]//*[@id=$elemid][contains(@class, $classval)]"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="starts-with(@href,'#')">
            <xsl:value-of select="@href"/>
          </xsl:when>
          <xsl:when test="not(@format) or @format = 'dita'">#none#</xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="@href"/>
          </xsl:otherwise>
        </xsl:choose>
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
    <xsl:param name="file">#none#</xsl:param>
    <xsl:param name="topicpos">#none#</xsl:param>
    <xsl:param name="classval">#none#</xsl:param>
    <xsl:param name="topicid">#none#</xsl:param>
    <xsl:param name="elemid">#none#</xsl:param>
    <xsl:variable name="doc" select="document($file,/)"/>
    <xsl:choose>
      <xsl:when test="$topicpos='samefile' and key('topic', $topicid)/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')]//*[@id=$elemid][contains(@class, $classval)]/*[contains(@class,' topic/dt ')][1]">
        <xsl:variable name="target-text">
          <xsl:apply-templates
            select="key('topic', $topicid)[1]/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')]//*[@id=$elemid][contains(@class, $classval)]/*[contains(@class,' topic/dt ')][1]" mode="text-only"/>
        </xsl:variable>
        <xsl:value-of select="normalize-space($target-text)"/>
      </xsl:when>
      <xsl:when test="$topicpos='otherfile' and $doc//*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')]//*[@id=$elemid][contains(@class, $classval)]/*[contains(@class,' topic/dt ')][1]">
        <xsl:variable name="target-text">
          <xsl:apply-templates
            select="($doc//*[contains(@class, ' topic/topic ')][@id=$topicid])[1]/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')]//*[@id=$elemid][contains(@class, $classval)]/*[contains(@class,' topic/dt ')][1]" mode="text-only"/>
        </xsl:variable>
        <xsl:value-of select="normalize-space($target-text)"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="starts-with(@href,'#')">
            <xsl:value-of select="@href"/>
          </xsl:when>
          <xsl:when test="not(@format) or @format = 'dita'">#none#</xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="@href"/>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:apply-templates select="." mode="ditamsg:cannot-find-dlentry-target"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--getting the shortdesc for a link; called from main mode template for link/xref, 
      only after conditions such as scope and format have been tested and a text pull
      has been determined to be appropriate-->
  <xsl:template match="*" mode="topicpull:getshortdesc">
    <xsl:param name="file">#none#</xsl:param>
    <xsl:param name="topicpos">#none#</xsl:param>
    <xsl:param name="classval">#none#</xsl:param>
    <xsl:param name="topicid">#none#</xsl:param>
    <xsl:param name="elemid">#none#</xsl:param>
    <xsl:choose>
      <xsl:when test="$elemid!='#none#'">
        <xsl:apply-templates select="." mode="topicpull:getshortdesc_element">
          <xsl:with-param name="file" select="$file"/>
          <xsl:with-param name="topicpos" select="$topicpos"/>
          <xsl:with-param name="classval" select="$classval"/>
          <xsl:with-param name="topicid" select="$topicid"/>
          <xsl:with-param name="elemid" select="$elemid"/>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:when test="$topicpos='samefile'">
        <xsl:apply-templates select="." mode="topicpull:getshortdesc_samefile">
          <xsl:with-param name="classval" select="$classval"/>
          <xsl:with-param name="topicid" select="$topicid"/>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:when test="$topicpos='firstinfile'">
        <xsl:apply-templates select="." mode="topicpull:getshortdesc_firstinfile">
          <xsl:with-param name="file" select="$file"/>
          <xsl:with-param name="classval" select="$classval"/>
        </xsl:apply-templates>
      </xsl:when>
      <xsl:when test="$topicpos='otherfile'">
        <xsl:apply-templates select="." mode="topicpull:getshortdesc_otherfile">
          <xsl:with-param name="file" select="$file"/>
          <xsl:with-param name="classval" select="$classval"/>
          <xsl:with-param name="topicid" select="$topicid"/>
        </xsl:apply-templates>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <!-- Get the short description for a non-topic element. Search for an element with the matching
       correct class value and matching ID, inside the topic with the correct ID. If found, use
       the local <desc> element. If not found, do not create a short description. -->
  <xsl:template match="*" mode="topicpull:getshortdesc_element">
    <xsl:param name="file">#none#</xsl:param>
    <xsl:param name="topicpos">#none#</xsl:param>
    <xsl:param name="classval">#none#</xsl:param>
    <xsl:param name="topicid">#none#</xsl:param>
    <xsl:param name="elemid">#none#</xsl:param>
    <xsl:variable name="doc" select="document($file,/)"/>
    <xsl:choose>
      <xsl:when test="$topicpos='samefile' and key('topic', $topicid)/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')]//*[@id=$elemid][contains(@class, $classval)]/*[contains(@class, ' topic/desc ')]">
          <xsl:apply-templates select="key('topic', $topicid)/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')]//*[@id=$elemid][contains(@class, $classval)]/*[contains(@class, ' topic/desc ')]" mode="copy-desc-contents"/>
      </xsl:when>
      <xsl:when test="$topicpos='otherfile' and $doc//*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')]//*[@id=$elemid][contains(@class, $classval)]/*[contains(@class, ' topic/desc ')]">
        <xsl:apply-templates select="$doc//*[contains(@class, ' topic/topic ')][@id=$topicid]/*[contains(@class,' topic/body ') or contains(@class,' topic/abstract ')]//*[@id=$elemid][contains(@class, $classval)]/*[contains(@class, ' topic/desc ')]" mode="copy-desc-contents"/>
      </xsl:when>
      <xsl:otherwise>#none#</xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Get the shortdesc for a topic that is in the same file -->
  <xsl:template match="*" mode="topicpull:getshortdesc_samefile">
    <xsl:param name="classval">#none#</xsl:param>
    <xsl:param name="topicid">#none#</xsl:param>
    <xsl:choose>
      <!-- Check for topic with matching class and matching ID -->
      <xsl:when test="key('id', $topicid)[contains(@class, $classval)]/*[contains(@class, ' topic/shortdesc ')] |
                      key('id', $topicid)[contains(@class, $classval)]/*[contains(@class, ' topic/abstract ')]/*[contains(@class, ' topic/shortdesc ')]">
        <xsl:apply-templates select="key('id', $topicid)[contains(@class, $classval)]/*[contains(@class, ' topic/shortdesc ')] |
                                     key('id', $topicid)[contains(@class, $classval)]/*[contains(@class, ' topic/abstract ')]/*[contains(@class, ' topic/shortdesc ')]" mode="copy-shortdesc" />
      </xsl:when>
      <!-- Check for topic with matching ID -->
      <xsl:when test="key('topic', $topicid)/*[contains(@class, ' topic/shortdesc ')] |
                      key('topic', $topicid)/*[contains(@class, ' topic/abstract ')]/*[contains(@class, ' topic/shortdesc ')]">
        <xsl:apply-templates select="key('topic', $topicid)/*[contains(@class, ' topic/shortdesc ')] | 
                                     key('topic', $topicid)/*[contains(@class, ' topic/abstract ')]/*[contains(@class, ' topic/shortdesc ')]" mode="copy-shortdesc"/>
      </xsl:when>
      <!-- Remove previously existing general tests. If a specific topic is the target, shortdesc should not be pulled from another. -->
      <xsl:otherwise>
        <xsl:text>#none#</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Get the shortdesc from a topic, when the topic is a file name -->
  <xsl:template match="*" mode="topicpull:getshortdesc_firstinfile">
    <xsl:param name="file">#none#</xsl:param>
    <xsl:param name="classval">#none#</xsl:param>
   <xsl:variable name="doc" select="document($file,/)"/>
    <xsl:choose>
      <xsl:when test="$doc//*[contains(@class, $classval)][1]/*[contains(@class, ' topic/shortdesc ')] |
                      $doc//*[contains(@class, $classval)][1]/*[contains(@class, ' topic/abstract ')]/*[contains(@class, ' topic/shortdesc ')]">
        <xsl:apply-templates select="($doc//*[contains(@class, $classval)])[1]/*[contains(@class, ' topic/shortdesc ')] |
                                     ($doc//*[contains(@class, $classval)])[1]/*[contains(@class, ' topic/abstract ')]/*[contains(@class, ' topic/shortdesc ')]" mode="copy-shortdesc"/>
      </xsl:when>
      <xsl:when test="$doc//*[contains(@class, ' topic/topic ')][1]/*[contains(@class, ' topic/shortdesc ')] |
                      $doc//*[contains(@class, ' topic/topic ')][1]/*[contains(@class, ' topic/abstract ')]/*[contains(@class, ' topic/shortdesc ')]">
        <xsl:apply-templates select="($doc//*[contains(@class, ' topic/topic ')])[1]/*[contains(@class, ' topic/shortdesc ')] |
                                     ($doc//*[contains(@class, ' topic/topic ')])[1]/*[contains(@class, ' topic/abstract ')]/*[contains(@class, ' topic/shortdesc ')]" mode="copy-shortdesc"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>#none#</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Get the shortdesc from a specific topic in another file -->
  <xsl:template match="*" mode="topicpull:getshortdesc_otherfile">
    <xsl:param name="file">#none#</xsl:param>
    <xsl:param name="classval">#none#</xsl:param>
    <xsl:param name="topicid">#none#</xsl:param>
    <xsl:variable name="doc" select="document($file,/)"/>
    <xsl:for-each select="$doc">
    <xsl:choose>
      <xsl:when test="key('id', $topicid)[contains(@class, $classval)]/*[contains(@class, ' topic/shortdesc ')] |
                      key('id', $topicid)[contains(@class, $classval)]/*[contains(@class, ' topic/abstract ')]/*[contains(@class, ' topic/shortdesc ')]">
        <xsl:apply-templates select="(key('id', $topicid)[contains(@class, $classval)])[1]/*[contains(@class, ' topic/shortdesc ')] | 
                                     (key('id', $topicid)[contains(@class, $classval)])[1]/*[contains(@class, ' topic/abstract ')]/*[contains(@class, ' topic/shortdesc ')]" mode="copy-shortdesc"/>
      </xsl:when>
      <xsl:when test="key('topic', $topicid)/*[contains(@class, ' topic/shortdesc ')] |
                      key('topic', $topicid)/*[contains(@class, ' topic/abstract ')]/*[contains(@class, ' topic/shortdesc ')]">
        <xsl:apply-templates select="key('topic', $topicid)[1]/*[contains(@class, ' topic/shortdesc ')] |
                                     key('topic', $topicid)[1]/*[contains(@class, ' topic/abstract ')]/*[contains(@class, ' topic/shortdesc ')]" mode="copy-shortdesc"/>
      </xsl:when>
      <xsl:when test="key('id', $topicid)[contains(@class, $classval)]//*[contains(@class, ' topic/shortdesc ')]">
        <xsl:apply-templates select="(key('id', $topicid)[contains(@class, $classval)])[1]//*[contains(@class, ' topic/shortdesc ')]" mode="copy-shortdesc" />
      </xsl:when>
      <xsl:when
        test="key('topic', $topicid)//*[contains(@class, ' topic/shortdesc ')]">
        <xsl:apply-templates select="key('topic', $topicid)[1]//*[contains(@class, ' topic/shortdesc ')]" mode="copy-shortdesc" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>#none#</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="*|text()|processing-instruction()" mode="text-only">
    <!-- Redirect to common dita-ot module -->
    <xsl:apply-templates select="." mode="dita-ot:text-only"/>
  </xsl:template>
  <xsl:template match="*|@*|comment()|processing-instruction()|text()">
    <xsl:copy>
      <xsl:apply-templates select="*|@*|comment()|processing-instruction()|text()"/>
    </xsl:copy>
  </xsl:template>
  
  
  <xsl:template match="*[contains(@class,' topic/xref ')]" mode="copy-shortdesc">
    <xsl:choose>
      <xsl:when test="not(@href) or @scope='peer' or @scope='external'">
        <xsl:copy>
          <xsl:apply-templates select="@*|text()|*" mode="copy-shortdesc" />
        </xsl:copy>
      </xsl:when>
      <xsl:when test="@format and not(@format='dita' or @format='DITA')">
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
        <xsl:apply-templates select="*[not(contains(@class,' topic/desc '))]|text()|comment()|processing-instruction()" mode="copy-shortdesc" />
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
    <xsl:apply-templates select="*|text()|comment()|processing-instruction()" mode="copy-shortdesc"/>
  </xsl:template>

  <xsl:template match="*[contains(@class,' topic/shortdesc ')]" mode="copy-shortdesc">
    <xsl:if test="preceding-sibling::*[contains(@class,' topic/shortdesc ')]">
      <!-- In an abstract, and this is not the first -->
      <xsl:text> </xsl:text>
    </xsl:if>
    <xsl:apply-templates select="*|text()|comment()|processing-instruction()" mode="copy-shortdesc" />
  </xsl:template>
  
  <xsl:template match="@id" mode="copy-shortdesc" />
  
  <xsl:template match="*[contains(@class,' topic/indexterm ')]" mode="copy-shortdesc" />
  <xsl:template match="*[contains(@class,' topic/draft-comment ') or contains(@class,' topic/required-cleanup ')]" mode="copy-shortdesc"/>
  
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
      <xsl:with-param name="msgnum">006</xsl:with-param>
      <xsl:with-param name="msgsev">E</xsl:with-param>
      <xsl:with-param name="msgparams">%1=<xsl:value-of select="@href"/></xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  <xsl:template match="*" mode="ditamsg:empty-href">
    <xsl:call-template name="output-message">
      <xsl:with-param name="msgnum">017</xsl:with-param>
      <xsl:with-param name="msgsev">E</xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  <xsl:template match="*" mode="ditamsg:missing-href">
    <xsl:call-template name="output-message">
      <xsl:with-param name="msgnum">028</xsl:with-param>
      <xsl:with-param name="msgsev">E</xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  <xsl:template match="*" mode="ditamsg:type-attribute-not-specific">
    <xsl:param name="elem-name" select="name()"/>
    <xsl:param name="targetting"/>
    <xsl:param name="type"/>
    <xsl:param name="actual-name"/>
    <xsl:call-template name="output-message">
      <xsl:with-param name="msgnum">029</xsl:with-param>
      <xsl:with-param name="msgsev">I</xsl:with-param>
      <xsl:with-param name="msgparams">%1=<xsl:value-of select="$elem-name"/>;%2=<xsl:value-of select="$targetting"/>;%3=<xsl:value-of select="$type"/>;%4=<xsl:value-of select="$actual-name"/></xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  <xsl:template match="*" mode="ditamsg:type-attribute-incorrect">
    <xsl:param name="elem-name" select="name()"/>
    <xsl:param name="targetting"/>
    <xsl:param name="type"/>
    <xsl:param name="actual-name"/>
    <xsl:call-template name="output-message">
      <xsl:with-param name="msgnum">030</xsl:with-param>
      <xsl:with-param name="msgsev">W</xsl:with-param>
      <xsl:with-param name="msgparams">%1=<xsl:value-of select="$elem-name"/>;%2=<xsl:value-of select="$targetting"/>;%3=<xsl:value-of select="$type"/>;%4=<xsl:value-of select="$actual-name"/></xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  <xsl:template match="*" mode="ditamsg:missing-href-target">
    <xsl:param name="file"/>
    <xsl:choose>
       <xsl:when test="$ONLYTOPICINMAP='true'">
          <xsl:call-template name="output-message">
             <xsl:with-param name="msgnum">056</xsl:with-param>
             <xsl:with-param name="msgsev">W</xsl:with-param>
             <xsl:with-param name="msgparams">%1=<xsl:value-of select="$file"/></xsl:with-param>
           </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
         <xsl:call-template name="output-message">
            <xsl:with-param name="msgnum">031</xsl:with-param>
            <xsl:with-param name="msgsev">E</xsl:with-param>
            <xsl:with-param name="msgparams">%1=<xsl:value-of select="$file"/></xsl:with-param>
            </xsl:call-template>
         </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="*" mode="ditamsg:cannot-retrieve-linktext">
    <xsl:call-template name="output-message">
      <xsl:with-param name="msgnum">032</xsl:with-param>
      <xsl:with-param name="msgsev">E</xsl:with-param>
      <xsl:with-param name="msgparams">%1=<xsl:value-of select="@href"/></xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  <xsl:template match="*" mode="ditamsg:cannot-retrieve-list-number">
    <xsl:call-template name="output-message">
      <xsl:with-param name="msgnum">033</xsl:with-param>
      <xsl:with-param name="msgsev">E</xsl:with-param>
      <xsl:with-param name="msgparams">%1=<xsl:value-of select="@href"/></xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  <xsl:template match="*" mode="ditamsg:crossref-unordered-listitem">
    <xsl:call-template name="output-message">
      <xsl:with-param name="msgnum">034</xsl:with-param>
      <xsl:with-param name="msgsev">E</xsl:with-param>
      <xsl:with-param name="msgparams">%1=<xsl:value-of select="@href"/></xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  <xsl:template match="*" mode="ditamsg:cannot-retrieve-footnote-number">
    <xsl:call-template name="output-message">
      <xsl:with-param name="msgnum">035</xsl:with-param>
      <xsl:with-param name="msgsev">E</xsl:with-param>
      <xsl:with-param name="msgparams">%1=<xsl:value-of select="@href"/></xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  <xsl:template match="*" mode="ditamsg:cannot-find-dlentry-target">
    <xsl:call-template name="output-message">
      <xsl:with-param name="msgnum">036</xsl:with-param>
      <xsl:with-param name="msgsev">E</xsl:with-param>
      <xsl:with-param name="msgparams">%1=<xsl:value-of select="@href"/></xsl:with-param>
    </xsl:call-template>
  </xsl:template>
  
  <!-- re-specialize the unknown and foreign elements -->
  <xsl:template match="*[contains(@class,' topic/object ')][@data and not(@data='')][@type='DITA-foreign']" priority="10">
    <xsl:apply-templates select="document(@data,/)/*/*" mode="specialize-foreign-unknown"/> 
  </xsl:template>
  
  <xsl:template match="*|@*|text()|comment()|processing-instruction()" mode="specialize-foreign-unknown">
    <xsl:copy>
      <xsl:apply-templates select="*|@*|comment()|processing-instruction()|text()" mode="specialize-foreign-unknown"/>
    </xsl:copy>
  </xsl:template>

  <!-- Added for RFE 1367897. Ensure that if a value was passed in from the map,
       we respect that value, otherwise, use the value determined by this program. -->
  <xsl:template match="*" mode="topicpull:add-gentext-PI">
    <xsl:choose>
      <xsl:when test="processing-instruction()[name()='ditaot'][.='usertext' or .='gentext']">
        <xsl:copy-of select="processing-instruction()[name()='ditaot'][.='usertext' or .='gentext']"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:processing-instruction name="ditaot">gentext</xsl:processing-instruction>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="*" mode="topicpull:add-usertext-PI">
    <xsl:choose>
      <xsl:when test="processing-instruction()[name()='ditaot'][.='usertext' or .='gentext']">
        <xsl:copy-of select="processing-instruction()[name()='ditaot'][.='usertext' or .='gentext']"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:processing-instruction name="ditaot">usertext</xsl:processing-instruction>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!-- Added for RFE 3001750. -->
  <xsl:template match="*" mode="topicpull:add-genshortdesc-PI">
    <xsl:choose>
      <xsl:when test="processing-instruction()[name()='ditaot'][.='usershortdesc' or .='genshortdesc']">
        <xsl:copy-of select="processing-instruction()[name()='ditaot'][.='usershortdesc' or .='genshortdesc']"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:processing-instruction name="ditaot">genshortdesc</xsl:processing-instruction>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="*" mode="topicpull:add-usershortdesc-PI">
    <xsl:choose>
      <xsl:when test="processing-instruction()[name()='ditaot'][.='usershortdesc' or .='genshortdesc']">
        <xsl:copy-of select="processing-instruction()[name()='ditaot'][.='usershortdesc' or .='genshortdesc']"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:processing-instruction name="ditaot">usershortdesc</xsl:processing-instruction>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
