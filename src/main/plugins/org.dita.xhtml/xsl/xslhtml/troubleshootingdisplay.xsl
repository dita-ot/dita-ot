<?xml version="1.0" encoding="UTF-8" ?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file for 
     applicable licenses.-->
<!-- 20170503 SCH: Add support for troubleshooting elements. -->

<xsl:stylesheet version="2.0"
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     xmlns:related-links="http://dita-ot.sourceforge.net/ns/200709/related-links"
     xmlns:dita2html="http://dita-ot.sourceforge.net/ns/200801/dita2html"
     xmlns:ditamsg="http://dita-ot.sourceforge.net/ns/200704/ditamsg"
     xmlns:xs="http://www.w3.org/2001/XMLSchema"
     exclude-result-prefixes="related-links dita2html ditamsg xs">

<!-- Determines whether to generate titles for task sections. Values are YES and NO. -->
<xsl:param name="GENERATE-TASK-LABELS" select="'NO'"/>

<!-- == TASK UNIQUE SUBSTRUCTURES == -->

  <xsl:template match="*[contains(@class,' troubleshooting/troublebody ')]" name="topic.troubleshooting">
<div>
  <xsl:call-template name="commonattributes"/>
  <xsl:call-template name="setidaname"/>
  <xsl:apply-templates select="*[contains(@class,' ditaot-d/ditaval-startprop ')]" mode="out-of-line"/>
  <!-- here, you can generate a toc based on what's a child of body -->
  <!--xsl:call-template name="gen-sect-ptoc"/--><!-- Works; not always wanted, though; could add a param to enable it.-->

  <!-- Added for DITA 1.1 "Shortdesc proposal" -->
  <!-- get the abstract para -->
  <xsl:apply-templates select="preceding-sibling::*[contains(@class,' topic/abstract ')]" mode="outofline"/>

  <!-- get the short descr para -->
  <xsl:apply-templates select="preceding-sibling::*[contains(@class,' topic/shortdesc ')]" mode="outofline"/>

  <xsl:apply-templates/>
  <xsl:apply-templates select="*[contains(@class,' ditaot-d/ditaval-endprop ')]" mode="out-of-line"/>
</div><xsl:value-of select="$newline"/>
</xsl:template>

<xsl:template match="*[contains(@class,' task/steptroubleshooting ')]" name="topic.task.steptroubleshooting">
  <xsl:call-template name="generateItemGroupTaskElement"/>
</xsl:template>

  <xsl:template match="*[contains(@class,' troubleshooting/troubleSolution ')]" name="topic.troubleshooting.troubleSolution">
  <xsl:call-template name="generateItemGroupTaskElement"/>
</xsl:template>

<xsl:template name="generateItemGroupTaskElement">
  <div>
    <xsl:call-template name="commonattributes"/>
    <xsl:call-template name="setidaname"/>
    <xsl:apply-templates/>
  </div>
</xsl:template>

  <xsl:template match="*[contains(@class,' troubleshooting/condition ')]" mode="dita2html:section-heading">
  <xsl:apply-templates select="." mode="generate-task-label">
    <xsl:with-param name="use-label">
      <xsl:call-template name="getVariable">
        <xsl:with-param name="id" select="'trouble_condition'"/>
      </xsl:call-template>
    </xsl:with-param>
  </xsl:apply-templates>
</xsl:template>

  <xsl:template match="*[contains(@class,' troubleshooting/cause ')]" mode="dita2html:section-heading">
  <xsl:apply-templates select="." mode="generate-task-label">
    <xsl:with-param name="use-label">
      <xsl:call-template name="getVariable">
        <xsl:with-param name="id" select="'trouble_cause'"/>
      </xsl:call-template>
    </xsl:with-param>
  </xsl:apply-templates>
</xsl:template>
  
  <xsl:template match="*[contains(@class,' troubleshooting/remedy ')]" mode="dita2html:section-heading">
    <xsl:apply-templates select="." mode="generate-task-label">
      <xsl:with-param name="use-label">
        <xsl:call-template name="getVariable">
          <xsl:with-param name="id" select="'trouble_remedy'"/>
        </xsl:call-template>
      </xsl:with-param>
    </xsl:apply-templates>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' troubleshooting/responsibleParty ')]" mode="dita2html:section-heading">
    <xsl:apply-templates select="." mode="generate-task-label">
      <xsl:with-param name="use-label">
        <xsl:call-template name="getVariable">
          <xsl:with-param name="id" select="'trouble_responsibleParty'"/>
        </xsl:call-template>
      </xsl:with-param>
    </xsl:apply-templates>
  </xsl:template>
  
  <xsl:template match="*[contains(@class,' task/tasktroubleshooting ')]" mode="dita2html:section-heading">
    <xsl:apply-templates select="." mode="generate-task-label">
      <xsl:with-param name="use-label">
        <xsl:call-template name="getVariable">
          <xsl:with-param name="id" select="'trouble_task'"/>
        </xsl:call-template>
      </xsl:with-param>
    </xsl:apply-templates>
  </xsl:template>

<!-- 
     To override the task label for a specific element, match that element with this mode. 
     For example, you can turn off labels for <context> with this rule:
     <xsl:template match="*[contains(@class,' task/context ')]" mode="generate-task-label"/>
-->
<xsl:template match="*" mode="generate-task-label">
  <xsl:param name="use-label"/>
  <xsl:if test="$GENERATE-TASK-LABELS='YES'">
    <xsl:variable name="headLevel">
      <xsl:variable name="headCount">
        <xsl:value-of select="count(ancestor::*[contains(@class,' topic/topic ')])+1"/>
      </xsl:variable>
      <xsl:choose>
        <xsl:when test="$headCount > 6">h6</xsl:when>
        <xsl:otherwise>h<xsl:value-of select="$headCount"/></xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <div class="troubleshootinglabel">
      <xsl:element name="{$headLevel}">
        <xsl:attribute name="class">sectiontitle troubleshootinglabel</xsl:attribute>
        <xsl:value-of select="$use-label"/>
      </xsl:element>
    </div>
  </xsl:if>
</xsl:template>

  <!-- Tasks have their own group. -->
  <xsl:template match="*[contains(@class, ' topic/link ')][@type='task']" mode="related-links:get-group"
                name="related-links:group.task"
                as="xs:string">
    <xsl:text>task</xsl:text>
  </xsl:template>
  
  <!-- Priority of task group. -->
  <xsl:template match="*[contains(@class, ' topic/link ')][@type='task']" mode="related-links:get-group-priority"
                name="related-links:group-priority.task"
                as="xs:integer">
    <xsl:sequence select="2"/>
  </xsl:template>
  
  <!-- Task wrapper for HTML: "Related tasks" in <div>. -->
  <xsl:template match="*[contains(@class, ' topic/link ')][@type='task']" mode="related-links:result-group"
                name="related-links:result.task" as="element()">
    <xsl:param name="links" as="node()*"/>
    <xsl:if test="normalize-space(string-join($links, ''))">
      <linklist class="- topic/linklist " outputclass="relinfo reltasks">
        <title class="- topic/title ">
          <xsl:call-template name="getVariable">
            <xsl:with-param name="id" select="'Related tasks'"/>
          </xsl:call-template>
        </title>
        <xsl:copy-of select="$links"/>
      </linklist>
    </xsl:if>
  </xsl:template>
  
</xsl:stylesheet>
