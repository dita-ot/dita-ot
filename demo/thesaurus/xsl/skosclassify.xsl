<?xml version="1.0"?>
<!--
 | (C) Copyright IBM Corporation 2005 - 2006. All Rights Reserved.
 *-->
<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:skos="http://www.w3.org/2004/02/skos/core#"
                xmlns:foaf="http://xmlns.com/foaf/0.1/">

<xsl:output method="xml"/>
  
<xsl:template match="*[contains(@class,' classify-d/subjectref ')]">
  <skos:subject rdf:resource="{$SCHEMEBASE}#{substring-before(@href,$DITAEXT)}"><!-- xsl:call-template name="setLanguage"/ --></skos:subject>
</xsl:template>

<xsl:template match="*[contains(@class,' classify-d/topicsubject ')]">
  <xsl:if test="@href">
    <skos:primarySubject rdf:resource="{$SCHEMEBASE}#{substring-before(@href,$DITAEXT)}"><!-- xsl:call-template name="setLanguage"/ --></skos:primarySubject>
  </xsl:if>
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="*[contains(@class,' classify-d/topicSubjectTable ')]">
  <xsl:apply-templates select="*[contains(@class,' map/relrow ')]/*[1]/*"/>
</xsl:template>

</xsl:stylesheet>
