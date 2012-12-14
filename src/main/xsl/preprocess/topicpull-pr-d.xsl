<?xml version="1.0"?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
  Sourceforge.net. See the accompanying license.txt file for 
  applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2007 All Rights Reserved. -->
<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:topicpull="http://dita-ot.sourceforge.net/ns/200704/topicpull"
  xmlns:ditamsg="http://dita-ot.sourceforge.net/ns/200704/ditamsg"
  exclude-result-prefixes="topicpull ditamsg">
  
  <xsl:template match="*[contains(@class, ' pr-d/fragref ')]">
    <xsl:if test="@href=''">
      <xsl:apply-templates select="." mode="ditamsg:empty-href"/>
    </xsl:if>
    <xsl:choose>
      <!-- fragref cannot allow desc, so no need to check for it -->
      <xsl:when test="text()|*">
        <xsl:copy>
          <xsl:apply-templates select="*|@*|comment()|processing-instruction()|text()"/>
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
      <xsl:otherwise>
        <xsl:apply-templates select="." mode="ditamsg:missing-href"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Override the rule for fragref, so that it does not retrieve <desc> -->
  <xsl:template match="*[contains(@class,' pr-d/fragref ')]" mode="topicpull:get-stuff">
    <xsl:param name="localtype">#none#</xsl:param>
    <xsl:param name="scope">#none#</xsl:param>
    <xsl:param name="format">#none#</xsl:param>
    <!--the file name of the target, if any-->
    <xsl:variable name="file"><xsl:apply-templates select="." mode="topicpull:get-stuff_file"/></xsl:variable>

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

  </xsl:template>

</xsl:stylesheet>
