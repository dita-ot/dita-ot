<?xml version='1.0'?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:dita2xslfo="http://dita-ot.sourceforge.net/ns/200910/dita2xslfo"
                xmlns:opentopic="http://www.idiominc.com/opentopic"
                xmlns:opentopic-index="http://www.idiominc.com/opentopic/index"
                xmlns:ot-placeholder="http://suite-sol.com/namespaces/ot-placeholder"
                exclude-result-prefixes="opentopic opentopic-index dita2xslfo ot-placeholder"
                version="2.0">

  <xsl:variable name="changeset">
    <xsl:for-each select="//*[contains (@class, ' relmgmt-d/change-item ')]">
      <xsl:value-of select="."/>
      <!--
      <xsl:copy>
        <xsl:copy-of select="@*"/>
      </xsl:copy>
      -->
    </xsl:for-each>
  </xsl:variable>


  <!--   LOC   -->

  <xsl:template match="ot-placeholder:changelist" name="createChangeList">
    <xsl:if test="//*[contains(@class, ' relmgmt-d/change-historylist ')]">
      <!--exists tables with titles-->
      <fo:page-sequence master-reference="toc-sequence" xsl:use-attribute-sets="page-sequence.loc">
        <xsl:call-template name="insertTocStaticContents"/>
        <fo:flow flow-name="xsl-region-body">
          <fo:block start-indent="0in">

            <xsl:call-template name="createLOCHeader"/>


            <xsl:text>topic/title of concepts that have a change-item:</xsl:text>
            <!-- Select topic/title of all topic that have a change-item -->
            <xsl:apply-templates select="//*[contains (@class, ' topic/topic ')][.//*[contains (@class, ' relmgmt-d/change-item ')]]/*[contains (@class, ' topic/title ')]"/>

            <xsl:text>topic/title of all change-items:</xsl:text>
            <xsl:apply-templates select="//*[contains (@class, ' topic/topic ')]/*[contains (@class, ' topic/title ')]"/>

            <xsl:text>...:</xsl:text>
            <xsl:apply-templates select="//*[contains (@class, ' relmgmt-d/change-item ')][child::*[contains(@class, ' relmgmt-d/change-completed ' )]]" mode="list.of.changes"/>
          </fo:block>
        </fo:flow>
      </fo:page-sequence>
    </xsl:if>

    <xsl:text>critdates</xsl:text>
    <xsl:if test="//*[contains(@class, ' topic/critdates ')]">
      <xsl:call-template name="createBookRelease"/>
    </xsl:if>
  </xsl:template>

  <xsl:template name="createLOCHeader">
    <fo:block>
      <xsl:text>HELLO WORLD X</xsl:text>
    </fo:block>
    <fo:block xsl:use-attribute-sets="__lotf__heading" id="{$id.lot}">
      <fo:marker marker-class-name="current-header">
        <xsl:call-template name="getVariable">
          <xsl:with-param name="id" select="'List of Changes'"/>
        </xsl:call-template>
      </fo:marker>
      <xsl:call-template name="getVariable">
        <xsl:with-param name="id" select="'List of Changes'"/>
      </xsl:call-template>
    </fo:block>
  </xsl:template>
  
  <xsl:template name="createBookRelease">
    <xsl:for-each select="*[contains (@class, ' topic/created ')] or *[contains (@class, ' topic/revised ')]">
      <xsl:choose>
        <xsl:when test="*[contains (@class, ' topic/created ')]">
          <xsl:text>created date: </xsl:text><xsl:value-of select="@date"/>
        </xsl:when>
        <xsl:when test="*[contains (@class, ' topic/revised ')]">
          <xsl:text>revised modified: </xsl:text><xsl:value-of select="@modified"/>
        </xsl:when>
      </xsl:choose>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="*[contains (@class, ' bookmap/changelist ')]" mode="list.of.changes">
    <fo:block><xsl:text>HELLO WORLD 1</xsl:text></fo:block>
  </xsl:template>

</xsl:stylesheet>
