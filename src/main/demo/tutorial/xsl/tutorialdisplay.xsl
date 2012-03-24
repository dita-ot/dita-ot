<?xml version="1.0" encoding="UTF-8" ?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file in the
     main toolkit package for applicable licenses.-->
<!-- (C) Copyright IBM Corporation 2005, 2009 All Rights Reserved. -->
<!--
 | Specific override stylesheet for tutorials 
 |
 +======================================
 *-->

<xsl:stylesheet version="1.0"
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<!-- XHTML output with XML syntax -->
<xsl:output method="xml"
            encoding="utf-8"
            indent="no"
/>

<!-- == TASK UNIQUE SUBSTRUCTURES == -->

  <!-- section processor - div with no generated title -->
  <xsl:template match="*[contains(@class,' tutorial/timeRequired ')]" name="topic.tutorial.timeRequired">
    <xsl:variable name="flagrules">
      <xsl:call-template name="getrules"/>
    </xsl:variable>
    <div class="tutorialTimeRequired">
      <xsl:call-template name="commonattributes">
        <xsl:with-param name="default-output-class">tutorialTimeRequired</xsl:with-param>
      </xsl:call-template>
      <xsl:call-template name="gen-style">
        <xsl:with-param name="flagrules" select="$flagrules"></xsl:with-param>
      </xsl:call-template>
      <xsl:call-template name="gen-toc-id"/>
      <xsl:call-template name="setidaname"/>
      <xsl:value-of select="$newline"/>
      <xsl:call-template name="start-flagit">
        <xsl:with-param name="flagrules" select="$flagrules"></xsl:with-param>     
      </xsl:call-template>
      <xsl:variable name="revtest">
        <xsl:if test="@rev and not($FILTERFILE='') and ($DRAFT='yes')"> <!-- revision? -->
          <xsl:call-template name="find-active-rev-flag">               <!-- active? (revtest will be 1 when active)-->
            <xsl:with-param name="allrevs" select="@rev"/>
          </xsl:call-template>
        </xsl:if>
      </xsl:variable>
      <xsl:choose>
        <xsl:when test="$revtest=1">   <!-- Rev is active - add the DIV -->
          <div class="{@rev}"><xsl:apply-templates select="."  mode="section-fmt" /></div>
        </xsl:when>
        <xsl:otherwise>  <!-- Rev wasn't active - process normally -->
          <xsl:apply-templates select="."  mode="section-fmt" />
        </xsl:otherwise>
      </xsl:choose>
      <xsl:call-template name="end-flagit">
        <xsl:with-param name="flagrules" select="$flagrules"></xsl:with-param>     
      </xsl:call-template>
    </div><xsl:value-of select="$newline"/>
  </xsl:template>

  <xsl:template match="*[contains(@class,' tutorial/learningObjectives ')]" name="topic.tutorial.learningObjectives">
    <xsl:variable name="flagrules">
      <xsl:call-template name="getrules"/>
    </xsl:variable>
    <div class="tutorialLearningObjectives">
      <xsl:call-template name="commonattributes">
        <xsl:with-param name="default-output-class">tutorialLearningObjectives</xsl:with-param>
      </xsl:call-template>
      <xsl:call-template name="gen-style">
        <xsl:with-param name="flagrules" select="$flagrules"></xsl:with-param>
      </xsl:call-template>
      <xsl:call-template name="gen-toc-id"/>
      <xsl:call-template name="setidaname"/>
      <xsl:value-of select="$newline"/>
      <xsl:call-template name="start-flagit">
        <xsl:with-param name="flagrules" select="$flagrules"></xsl:with-param>     
      </xsl:call-template>
      <xsl:variable name="revtest">
        <xsl:if test="@rev and not($FILTERFILE='') and ($DRAFT='yes')"> <!-- revision? -->
          <xsl:call-template name="find-active-rev-flag">               <!-- active? (revtest will be 1 when active)-->
            <xsl:with-param name="allrevs" select="@rev"/>
          </xsl:call-template>
        </xsl:if>
      </xsl:variable>
      <xsl:choose>
        <xsl:when test="$revtest=1">   <!-- Rev is active - add the DIV -->
          <div class="{@rev}"><xsl:apply-templates select="."  mode="section-fmt" /></div>
        </xsl:when>
        <xsl:otherwise>  <!-- Rev wasn't active - process normally -->
          <xsl:apply-templates select="."  mode="section-fmt" />
        </xsl:otherwise>
      </xsl:choose>
      <xsl:call-template name="end-flagit">
        <xsl:with-param name="flagrules" select="$flagrules"></xsl:with-param>     
      </xsl:call-template>
    </div><xsl:value-of select="$newline"/>
  </xsl:template>

  <!-- Add spaces between authorName elements -->
  <xsl:template match="*[contains(@class,' authorInfo/prefix ') or contains(@class,' authorInfo/givenName ') or contains(@class,' authorInfo/middleName ') or contains(@class,' authorInfo/familyName ') or contains(@class,' authorInfo/suffix ')]" name="topic.tutorial.authorNameContent">
    <xsl:value-of select="." />
    <xsl:text> </xsl:text>
  </xsl:template>

<!-- Disable task headings in tutorial lessons -->
<xsl:template match="*[contains(@class,' task/prereq ')][ancestor::*[contains(@class,' tutorialLesson/tutorialLesson ')]]" mode="generate-task-label" />
<xsl:template match="*[contains(@class,' task/context ')][ancestor::*[contains(@class,' tutorialLesson/tutorialLesson ')]]" mode="generate-task-label" />
<xsl:template match="*[contains(@class,' task/steps ')][ancestor::*[contains(@class,' tutorialLesson/tutorialLesson ')]]" mode="generate-task-label" />
<xsl:template match="*[contains(@class,' task/steps-unordered ')][ancestor::*[contains(@class,' tutorialLesson/tutorialLesson ')]]" mode="generate-task-label" />
<xsl:template match="*[contains(@class,' task/result ')][ancestor::*[contains(@class,' tutorialLesson/tutorialLesson ')]]" mode="generate-task-label" />
<xsl:template match="*[contains(@class,' topic/example ')][ancestor::*[contains(@class,' tutorialLesson/tutorialLesson ')]]" mode="generate-task-label" />
<xsl:template match="*[contains(@class,' task/postreq ')][ancestor::*[contains(@class,' tutorialLesson/tutorialLesson ')]]" mode="generate-task-label" />

</xsl:stylesheet>
