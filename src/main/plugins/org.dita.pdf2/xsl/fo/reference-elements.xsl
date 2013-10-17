<?xml version="1.0" encoding="UTF-8"?>
<!--
This file is part of the DITA Open Toolkit project.
See the accompanying license.txt file for applicable licenses.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:fo="http://www.w3.org/1999/XSL/Format"
  exclude-result-prefixes="xs"
  version="2.0">

  <xsl:template match="*[contains(@class, ' reference/refbody ')]" priority="1">
    <xsl:variable name="level" as="xs:integer">
      <xsl:apply-templates select="." mode="get-topic-level"/>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="not(node())"/>
      <xsl:when test="$level = 1">
        <fo:block xsl:use-attribute-sets="body__toplevel refbody">
          <xsl:call-template name="commonattributes"/>
          <xsl:apply-templates/>
        </fo:block>
      </xsl:when>
      <xsl:when test="$level = 2">
        <fo:block xsl:use-attribute-sets="body__secondLevel refbody">
          <xsl:call-template name="commonattributes"/>
          <xsl:apply-templates/>
        </fo:block>
      </xsl:when>
      <xsl:otherwise>
        <fo:block xsl:use-attribute-sets="refbody">
          <xsl:call-template name="commonattributes"/>
          <xsl:apply-templates/>
        </fo:block>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="*[contains(@class, ' reference/refsyn ')]">
    <fo:block xsl:use-attribute-sets="refsyn">
      <xsl:call-template name="commonattributes"/>
      <xsl:apply-templates/>
    </fo:block>
  </xsl:template>

  <xsl:template match="*[contains(@class, ' reference/properties ')]">
    <fo:table xsl:use-attribute-sets="properties">
      <xsl:call-template name="commonattributes"/>
      <xsl:call-template name="univAttrs"/>
      <xsl:call-template name="globalAtts"/>
      <xsl:call-template name="displayAtts">
        <xsl:with-param name="element" select="."/>
      </xsl:call-template>

      <xsl:if test="@relcolwidth">
        <xsl:variable name="fix-relcolwidth">
          <xsl:apply-templates select="." mode="fix-relcolwidth"/>
        </xsl:variable>
        <xsl:call-template name="createSimpleTableColumns">
          <xsl:with-param name="theColumnWidthes" select="$fix-relcolwidth"/>
        </xsl:call-template>
      </xsl:if>

      <xsl:if test="*[contains(@class, ' reference/prophead ')]">
        <xsl:apply-templates select="*[contains(@class, ' reference/prophead ')]"/>
      </xsl:if>

      <fo:table-body xsl:use-attribute-sets="properties__body">
        <xsl:apply-templates select="*[contains(@class, ' reference/property ')]"/>
      </fo:table-body>
    </fo:table>
  </xsl:template>

  <!-- If there is no "type" column, value is in column 1 -->
  <xsl:template match="*" mode="get-propvalue-position">
    <xsl:choose>
      <xsl:when test="../*[contains(@class, ' reference/property ')]/*[contains(@class, ' reference/proptype ')] |
                        ../*[contains(@class, ' reference/prophead ')]/*[contains(@class, ' reference/propheadhd ')]">
        <xsl:text>2</xsl:text>
      </xsl:when>
      <xsl:otherwise>1</xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- If there is a type and value column, desc is 3;
         Otherwise, if there is type or value, desc is 2;
         Otherwise, desc is the only column. -->
  <xsl:template match="*" mode="get-propdesc-position">
    <xsl:choose>
      <xsl:when test="../*/*[contains(@class, ' reference/proptype ') or contains(@class, ' reference/proptypehd ')] and
                        ../*/*[contains(@class, ' reference/propvalue ') or contains(@class, ' reference/propvaluehd ')]">
        <xsl:text>3</xsl:text>
      </xsl:when>
      <xsl:when test="../*/*[contains(@class, ' reference/proptype ') or contains(@class, ' reference/proptypehd ')] |
                        ../*/*[contains(@class, ' reference/propvalue ') or contains(@class, ' reference/propvaluehd ')]">
        <xsl:text>2</xsl:text>
      </xsl:when>
      <xsl:otherwise>1</xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="*[contains(@class, ' reference/property ')]">
    <fo:table-row xsl:use-attribute-sets="property">
      <xsl:call-template name="commonattributes"/>
      <xsl:variable name="valuePos">
        <xsl:apply-templates select="." mode="get-propvalue-position"/>
      </xsl:variable>
      <xsl:variable name="descPos">
        <xsl:apply-templates select="." mode="get-propdesc-position"/>
      </xsl:variable>
      <xsl:variable name="frame">
        <xsl:variable name="f" select="ancestor::*[contains(@class, ' reference/properties ')][1]/@frame"/>
        <xsl:choose>
          <xsl:when test="$f">
            <xsl:value-of select="$f"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$table.frame-default"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <xsl:variable name="keyCol" select="number(ancestor::*[contains(@class, ' reference/properties ')][1]/@keycol)"/>
      <xsl:variable name="hasHorisontalBorder">
        <xsl:choose>
          <xsl:when test="following-sibling::*[contains(@class, ' reference/property ')]">
            <xsl:value-of select="'yes'"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="'no'"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <xsl:choose>
        <xsl:when test="*[contains(@class, ' reference/proptype ')]">
          <xsl:apply-templates select="*[contains(@class, ' reference/proptype ')]">
            <xsl:with-param name="entryCol" select="1"/>
          </xsl:apply-templates>
        </xsl:when>
        <xsl:when test="../*/*[contains(@class, ' reference/proptype ') or contains(@class, ' reference/proptypehd ')]">
          <xsl:call-template name="createEmptyPropertyEntry">
            <xsl:with-param name="entryCol" select="1"/>
            <xsl:with-param name="keyCol" select="$keyCol"/>
            <xsl:with-param name="hasHorisontalBorder" select="$hasHorisontalBorder"/>
            <xsl:with-param name="hasVerticalBorder" select="'yes'"/>
            <xsl:with-param name="frame" select="$frame"/>
          </xsl:call-template>
        </xsl:when>
      </xsl:choose>
      <xsl:choose>
        <xsl:when test="*[contains(@class, ' reference/propvalue ')]">
          <xsl:apply-templates select="*[contains(@class, ' reference/propvalue ')]">
            <xsl:with-param name="entryCol" select="$valuePos"/>
          </xsl:apply-templates>
        </xsl:when>
        <xsl:when test="../*/*[contains(@class, ' reference/propvalue ') or contains(@class, ' reference/propvaluehd ')]">
          <xsl:call-template name="createEmptyPropertyEntry">
            <xsl:with-param name="entryCol" select="$valuePos"/>
            <xsl:with-param name="keyCol" select="$keyCol"/>
            <xsl:with-param name="hasHorisontalBorder" select="$hasHorisontalBorder"/>
            <xsl:with-param name="hasVerticalBorder" select="'yes'"/>
            <xsl:with-param name="frame" select="$frame"/>
          </xsl:call-template>
        </xsl:when>
      </xsl:choose>
      <xsl:choose>
        <xsl:when test="*[contains(@class, ' reference/propdesc ')]">
          <xsl:apply-templates select="*[contains(@class, ' reference/propdesc ')]">
            <xsl:with-param name="entryCol" select="$descPos"/>
          </xsl:apply-templates>
        </xsl:when>
        <xsl:when test="../*/*[contains(@class, ' reference/propdesc ') or contains(@class, ' reference/propdeschd ')]">
          <xsl:call-template name="createEmptyPropertyEntry">
            <xsl:with-param name="entryCol" select="$descPos"/>
            <xsl:with-param name="keyCol" select="$keyCol"/>
            <xsl:with-param name="hasHorisontalBorder" select="$hasHorisontalBorder"/>
            <xsl:with-param name="hasVerticalBorder" select="'no'"/>
            <xsl:with-param name="frame" select="$frame"/>
          </xsl:call-template>
        </xsl:when>
      </xsl:choose>
    </fo:table-row>
  </xsl:template>

  <xsl:template name="createEmptyPropertyEntry">
    <xsl:param name="entryCol"/>
    <xsl:param name="keyCol"/>
    <xsl:param name="hasHorisontalBorder"/>
    <xsl:param name="hasVerticalBorder"/>
    <xsl:param name="frame"/>

    <fo:table-cell xsl:use-attribute-sets="property.entry">
      <xsl:if test="$hasHorisontalBorder = 'yes'">
        <xsl:call-template name="generateSimpleTableHorizontalBorders">
          <xsl:with-param name="frame" select="$frame"/>
        </xsl:call-template>
      </xsl:if>
      <xsl:if test="$hasVerticalBorder = 'yes'">
        <xsl:call-template name="generateSimpleTableVerticalBorders">
          <xsl:with-param name="frame" select="$frame"/>
        </xsl:call-template>
      </xsl:if>

      <xsl:choose>
        <xsl:when test="$keyCol = $entryCol">
          <fo:block xsl:use-attribute-sets="property.entry__keycol-content"></fo:block>
        </xsl:when>
        <xsl:otherwise>
          <fo:block xsl:use-attribute-sets="property.entry__content"></fo:block>
        </xsl:otherwise>
      </xsl:choose>
    </fo:table-cell>
  </xsl:template>

  <xsl:template match="*[contains(@class, ' reference/proptype ') or contains(@class, ' reference/propvalue ') or contains(@class, ' reference/propdesc ')]">
    <xsl:param name="entryCol"/>
    <fo:table-cell xsl:use-attribute-sets="property.entry">
      <xsl:call-template name="commonattributes"/>
      <xsl:variable name="frame">
        <xsl:variable name="f" select="ancestor::*[contains(@class, ' reference/properties ')][1]/@frame"/>
        <xsl:choose>
          <xsl:when test="$f">
            <xsl:value-of select="$f"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$table.frame-default"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <xsl:if test="../following-sibling::*[contains(@class, ' reference/property ')]">
        <xsl:call-template name="generateSimpleTableHorizontalBorders">
          <xsl:with-param name="frame" select="$frame"/>
        </xsl:call-template>
      </xsl:if>
      <xsl:if test="following-sibling::*[contains(@class, ' reference/proptype ') or contains(@class, ' reference/propvalue ') or contains(@class, ' reference/propdesc ')]">
        <xsl:call-template name="generateSimpleTableVerticalBorders">
          <xsl:with-param name="frame" select="$frame"/>
        </xsl:call-template>
      </xsl:if>

      <xsl:choose>
        <xsl:when test="number(ancestor::*[contains(@class, ' reference/properties ')][1]/@keycol) = $entryCol">
          <fo:block xsl:use-attribute-sets="property.entry__keycol-content">
            <xsl:apply-templates/>
          </fo:block>
        </xsl:when>
        <xsl:otherwise>
          <fo:block xsl:use-attribute-sets="property.entry__content">
            <xsl:apply-templates/>
          </fo:block>
        </xsl:otherwise>
      </xsl:choose>
    </fo:table-cell>
  </xsl:template>

  <xsl:template match="*[contains(@class, ' reference/prophead ')]">
    <fo:table-header xsl:use-attribute-sets="prophead">
      <xsl:call-template name="commonattributes"/>
      <xsl:variable name="frame">
        <xsl:variable name="f" select="ancestor::*[contains(@class, ' reference/properties ')][1]/@frame"/>
        <xsl:choose>
          <xsl:when test="$f">
            <xsl:value-of select="$f"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$table.frame-default"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>
      <xsl:variable name="keyCol" select="number(ancestor::*[contains(@class, ' reference/properties ')][1]/@keycol)"/>

      <fo:table-row xsl:use-attribute-sets="prophead__row">
        <xsl:choose>
          <xsl:when test="*[contains(@class, ' reference/proptypehd ')]">
            <xsl:apply-templates select="*[contains(@class, ' reference/proptypehd ')]">
              <xsl:with-param name="entryCol" select="1"/>
            </xsl:apply-templates>
          </xsl:when>
          <xsl:when test="../*[contains(@class,' reference/property ')]/*[contains(@class,' reference/proptype ')]">
            <xsl:call-template name="createEmptyPropertyHeadEntry">
              <xsl:with-param name="entryCol" select="1"/>
              <xsl:with-param name="keyCol" select="$keyCol"/>
              <xsl:with-param name="hasVerticalBorder" select="'yes'"/>
              <xsl:with-param name="frame" select="$frame"/>
            </xsl:call-template>
          </xsl:when>
        </xsl:choose>
        <xsl:choose>
          <xsl:when test="*[contains(@class, ' reference/propvaluehd ')]">
            <xsl:apply-templates select="*[contains(@class, ' reference/propvaluehd ')]">
              <xsl:with-param name="entryCol" select="2"/>
            </xsl:apply-templates>
          </xsl:when>
          <xsl:when test="../*[contains(@class,' reference/property ')]/*[contains(@class,' reference/propvalue ')]">
            <xsl:call-template name="createEmptyPropertyHeadEntry">
              <xsl:with-param name="entryCol" select="2"/>
              <xsl:with-param name="keyCol" select="$keyCol"/>
              <xsl:with-param name="hasVerticalBorder" select="'yes'"/>
              <xsl:with-param name="frame" select="$frame"/>
            </xsl:call-template>
          </xsl:when>
        </xsl:choose>
        <xsl:choose>
          <xsl:when test="*[contains(@class, ' reference/propdeschd ')]">
            <xsl:apply-templates select="*[contains(@class, ' reference/propdeschd ')]">
              <xsl:with-param name="entryCol" select="3"/>
            </xsl:apply-templates>
          </xsl:when>
          <xsl:when test="../*[contains(@class,' reference/property ')]/*[contains(@class,' reference/propdesc ')]">
            <xsl:call-template name="createEmptyPropertyHeadEntry">
              <xsl:with-param name="entryCol" select="3"/>
              <xsl:with-param name="keyCol" select="$keyCol"/>
              <xsl:with-param name="hasVerticalBorder" select="'no'"/>
              <xsl:with-param name="frame" select="$frame"/>
            </xsl:call-template>
          </xsl:when>
        </xsl:choose>
      </fo:table-row>
    </fo:table-header>
  </xsl:template>

  <xsl:template name="createEmptyPropertyHeadEntry">
    <xsl:param name="entryCol"/>
    <xsl:param name="keyCol"/>
    <xsl:param name="hasVerticalBorder"/>
    <xsl:param name="frame"/>

    <fo:table-cell xsl:use-attribute-sets="prophead.entry">
      <xsl:call-template name="generateSimpleTableHorizontalBorders">
        <xsl:with-param name="frame" select="$frame"/>
      </xsl:call-template>
      <xsl:if test="$frame = 'all' or $frame = 'topbot' or $frame = 'top' or not($frame)">
        <xsl:call-template name="processAttrSetReflection">
          <xsl:with-param name="attrSet" select="'__tableframe__top'"/>
          <xsl:with-param name="path" select="$tableAttrs"/>
        </xsl:call-template>
      </xsl:if>
      <xsl:if test="$hasVerticalBorder = 'yes'">
        <xsl:call-template name="generateSimpleTableVerticalBorders">
          <xsl:with-param name="frame" select="$frame"/>
        </xsl:call-template>
      </xsl:if>

      <xsl:choose>
        <xsl:when test="$keyCol = $entryCol">
          <fo:block xsl:use-attribute-sets="prophead.entry__keycol-content"></fo:block>
        </xsl:when>
        <xsl:otherwise>
          <fo:block xsl:use-attribute-sets="prophead.entry__content"></fo:block>
        </xsl:otherwise>
      </xsl:choose>
    </fo:table-cell>
  </xsl:template>

  <xsl:template match="*[contains(@class, ' reference/proptypehd ') or contains(@class, ' reference/propvaluehd ') or contains(@class, ' reference/propdeschd ')]">
    <xsl:param name="entryCol"/>
    <fo:table-cell xsl:use-attribute-sets="prophead.entry">
      <xsl:call-template name="commonattributes"/>
      <xsl:variable name="frame">
        <xsl:variable name="f" select="ancestor::*[contains(@class, ' reference/properties ')][1]/@frame"/>
        <xsl:choose>
          <xsl:when test="$f">
            <xsl:value-of select="$f"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="$table.frame-default"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <xsl:call-template name="generateSimpleTableHorizontalBorders">
        <xsl:with-param name="frame" select="$frame"/>
      </xsl:call-template>
      <xsl:if test="$frame = 'all' or $frame = 'topbot' or $frame = 'top' or not($frame)">
        <xsl:call-template name="processAttrSetReflection">
          <xsl:with-param name="attrSet" select="'__tableframe__top'"/>
          <xsl:with-param name="path" select="$tableAttrs"/>
        </xsl:call-template>
      </xsl:if>
      <xsl:if test="following-sibling::*[contains(@class, ' reference/proptypehd ') or contains(@class, ' reference/propvaluehd ') or contains(@class, ' reference/propdeschd ')]">
        <xsl:call-template name="generateSimpleTableVerticalBorders">
          <xsl:with-param name="frame" select="$frame"/>
        </xsl:call-template>
      </xsl:if>

      <xsl:choose>
        <xsl:when test="number(ancestor::*[contains(@class, ' reference/properties ')][1]/@keycol) = $entryCol">
          <fo:block xsl:use-attribute-sets="prophead.entry__keycol-content">
            <xsl:apply-templates/>
          </fo:block>
        </xsl:when>
        <xsl:otherwise>
          <fo:block xsl:use-attribute-sets="prophead.entry__content">
            <xsl:apply-templates/>
          </fo:block>
        </xsl:otherwise>
      </xsl:choose>
    </fo:table-cell>
  </xsl:template>

  <!-- RFE 2882109: Combine this common code with topic/topic rule. -->
  <!--
  <xsl:template match="*[contains(@class, ' reference/reference ')]">
    <xsl:variable name="topicType">
      <xsl:call-template name="determineTopicType"/>
    </xsl:variable>

    <xsl:choose>
      <xsl:when test="$topicType = 'topicChapter'">
        <xsl:call-template name="processTopicChapter"/>
      </xsl:when>
      <xsl:when test="$topicType = 'topicAppendix'">
        <xsl:call-template name="processTopicAppendix"/>
      </xsl:when>
      <xsl:when test="$topicType = 'topicPart'">
        <xsl:call-template name="processTopicPart"/>
      </xsl:when>
      <xsl:when test="$topicType = 'topicPreface'">
        <xsl:call-template name="processTopicPreface"/>
      </xsl:when>
      <xsl:when test="$topicType = 'topicSimple'">
        <xsl:variable name="page-sequence-reference">
          <xsl:choose>
            <xsl:when test="$mapType = 'bookmap'">
              <xsl:value-of select="'body-sequence'"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="'ditamap-body-sequence'"/>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:variable>
        <xsl:choose>
          <xsl:when test="not(ancestor::*[contains(@class,' topic/topic ')])">
            <fo:page-sequence master-reference="{$page-sequence-reference}" xsl:use-attribute-sets="__force__page__count">
              <xsl:call-template name="insertBodyStaticContents"/>
              <fo:flow flow-name="xsl-region-body">
                <xsl:call-template name="processReference"/>
              </fo:flow>
            </fo:page-sequence>
          </xsl:when>
          <xsl:otherwise>
            <xsl:call-template name="processReference"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="processUnknowTopic">
          <xsl:with-param name="topicType" select="$topicType"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  -->

</xsl:stylesheet>