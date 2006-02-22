<?xml version="1.0" encoding="utf-8" ?>
<!--
 | (C) Copyright IBM Corporation 2005 - 2006. All Rights Reserved.
 *-->
<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:skos="http://www.w3.org/2004/02/skos/core#"
                xmlns:foaf="http://xmlns.com/foaf/0.1/">
  
<xsl:import href="skosmeta.xsl"/>

<xsl:output method="xml"/>

<xsl:key name="subjects" match="*[contains(@class,' scheme/subjectdef ')]" use="@href"/>

<xsl:template match="*[contains(@class,' scheme/subjectScheme ')]">
  <skos:ConceptScheme>
    <xsl:attribute name="rdf:about">
      <xsl:apply-templates select="." mode="schemeID"/>
    </xsl:attribute>
    <!-- xsl:call-template name="setLanguage"/ -->
    <xsl:apply-templates select="*[contains(@class,' scheme/subjectdef ')][@href]" mode="topConcepts"/>
  </skos:ConceptScheme>
  <xsl:apply-templates/>
</xsl:template>

<xsl:template match="*" mode="schemeID">
  <xsl:value-of select="$SCHEMEBASE"/>/scheme#<xsl:text/>
  <xsl:choose>
    <xsl:when test="@id"><xsl:value-of select="@id"/></xsl:when>
    <xsl:when test="@xtrf"><xsl:value-of select="substring-before(@xtrf,$DITAMAPEXT)"/></xsl:when>
    <xsl:otherwise><!--ERROR-->scheme</xsl:otherwise>
  </xsl:choose>
</xsl:template>

<xsl:template match="*" mode="topConcepts">
  <skos:hasTopConcept rdf:resource="{$SCHEMEBASE}#{substring-before(@href,$DITAEXT)}">
    <!-- xsl:call-template name="setLanguage"/ -->
  </skos:hasTopConcept>
</xsl:template>

<xsl:template
    match="*[contains(@class,' scheme/subjectdef ') and not(@href)]"/>

<xsl:template match="*[contains(@class,' scheme/subjectdef ') and @href]">
    <xsl:variable name="SUBJECT_IDENTIFIER">
      <xsl:choose>
        <xsl:when test="contains(@href,$DITAEXT)"><xsl:value-of select="substring-before(@href,$DITAEXT)"/></xsl:when>
        <xsl:otherwise><xsl:value-of select="@href"/></xsl:otherwise> <!-- could do better here...? -->
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="SUBJECT_EXISTS">
      <xsl:if test="document(@href,/)">yes</xsl:if>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$SUBJECT_EXISTS!='yes'">
        <!-- emit message -->
      </xsl:when>
      <xsl:when test="document(@href,/)/*/*/*[contains(@class,' subject-d/subjectDetail ')]"/>
      <xsl:otherwise>
        <!-- emit message: subject has no detail -->
      </xsl:otherwise>
    </xsl:choose>
    <skos:Concept rdf:about="{$SCHEMEBASE}#{$SUBJECT_IDENTIFIER}">
      <!-- xsl:call-template name="setLanguage"/ -->
      <!-- If this is the first time the subject appears, look for a preferred label and symbol -->
      <xsl:if test="generate-id(.)=generate-id(key('subjects',@href)[1])">
        <xsl:call-template name="getPrefLabel">
          <xsl:with-param name="SUBJECT_EXISTS" select="$SUBJECT_EXISTS"/>
        </xsl:call-template>
        <xsl:call-template name="getPrefSymbol">
          <xsl:with-param name="SUBJECT_EXISTS" select="$SUBJECT_EXISTS"/>
        </xsl:call-template>
        <xsl:call-template name="getSubjectDefinition">
          <xsl:with-param name="SUBJECT_EXISTS" select="$SUBJECT_EXISTS"/>
        </xsl:call-template>
        <xsl:if test="$SUBJECT_EXISTS='yes' and
                      document(@href,/)/*[contains(@class,' topic/topic ')]/
                                        *[contains(@class,' topic/body ')]/
                                        *[contains(@class,' subject-d/subjectDetail ')]">
          <xsl:apply-templates select="document(@href,/)/*[contains(@class,' topic/topic ')]/
                                                         *[contains(@class,' topic/body ')]/
                                                         *[contains(@class,' subject-d/subjectDetail ')]"/>
        </xsl:if>
 
      </xsl:if>

      <skos:inScheme>
        <!-- xsl:call-template name="setLanguage"/ -->
        <xsl:attribute name="rdf:resource">
          <xsl:apply-templates select="ancestor::*[contains(@class,' scheme/subjectScheme ')][1]" mode="schemeID"/>
        </xsl:attribute>
      </skos:inScheme>

      <!-- Not sure what to do about othermeta name=prompt? -->
      <xsl:apply-templates select="*[contains(@class,' map/topicmeta ')]"/>

      <!-- Generate relationships to narrower topics, defined as children of the current -->
      <xsl:apply-templates mode="getNarrowerSubject"/>
      <!-- Generate relationships to broader topics, defined as parents of the current -->
      <xsl:apply-templates select="parent::*" mode="getBroaderSubject"/>

      <!-- Add general related relationships -->
      <xsl:if test="parent::*[contains(@class,' scheme/relatedSubjects ')]">
        <xsl:apply-templates select="following-sibling::*[contains(@class,' scheme/subjectdef ') and @href] |
                                     preceding-sibling::*[contains(@class,' scheme/subjectdef ') and @href]" mode="relatedSubject"/>
      </xsl:if>


      <!-- same thing for reltable....how to interpret? traditional - subjectdef in one 'column' is related
           to subjects in another 'column'? If so then how to use relatedFrom and relatedTo? What about relatedrole? -->
      <!--  -->
      <xsl:if test="parent::*[contains(@class,' map/relcell ')] and not(ancestor-or-self::*[@linking][1][@linking='targetonly' or @linking='none'])">
        <xsl:apply-templates select="parent::*/preceding-sibling::* | parent::*/following-sibling::*" mode="relatedSubject"/>
      </xsl:if>
      
  
    </skos:Concept>
    <xsl:text>

</xsl:text>
  <xsl:apply-templates select="*[not(contains(@class,' scheme/subjectmeta '))]"/>
</xsl:template>


<xsl:template match="*" mode="getNarrowerSubject"/>
<xsl:template match="*[contains(@class,' scheme/hasNarrower ') or contains(@class,' scheme/hasPart ') or 
                       contains(@class,' scheme/hasInstance ') or contains(@class,' scheme/hasKind ')]" mode="getNarrowerSubject">
  <xsl:apply-templates mode="getNarrowerSubject"/>
</xsl:template>
<xsl:template match="*[contains(@class,' scheme/subjectdef ')][@href]" mode="getNarrowerSubject">
  <xsl:variable name="relType">
    <xsl:choose>
      <xsl:when test="ancestor::*[contains(@class,' scheme/hasNarrower ') or contains(@class,' scheme/hasPart ') or 
                                  contains(@class,' scheme/hasInstance ') or contains(@class,' scheme/hasKind ')][1][contains(@class,' scheme/hasNarrower ')]">
        <xsl:text>skos:narrower</xsl:text>
      </xsl:when>
      <xsl:when test="ancestor::*[contains(@class,' scheme/hasPart ') or 
                                  contains(@class,' scheme/hasInstance ') or contains(@class,' scheme/hasKind ')][1][contains(@class,' scheme/hasPart ')]">
        <xsl:text>skos:narrowerPartative</xsl:text>
      </xsl:when>
      <xsl:when test="ancestor::*[contains(@class,' scheme/hasInstance ') or contains(@class,' scheme/hasKind ')][1][contains(@class,' scheme/hasInstance ')]">
        <xsl:text>skos:narrowerInstantive</xsl:text>
      </xsl:when>
      <xsl:when test="ancestor::*[contains(@class,' scheme/hasKind ')]">
        <xsl:text>skos:narrower</xsl:text> <!-- Better term to use? -->
      </xsl:when>
      <xsl:otherwise>skos:narrower</xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:element name="{$relType}">
    <!-- xsl:call-template name="setLanguage"/ -->
    <xsl:attribute name="rdf:resource"><xsl:value-of select="$SCHEMEBASE"/>#<xsl:value-of select="substring-before(@href,$DITAEXT)"/></xsl:attribute>
  </xsl:element>
</xsl:template>

<!-- For broader relationships, we're processing the parent. If we moved up through a relationship type,
     that type must be saved; otherwise, look to ancestors to determine the current relationship type. -->
<xsl:template match="*" mode="getBroaderSubject"/>
<xsl:template match="*[contains(@class,' scheme/hasNarrower ') or contains(@class,' scheme/hasPart ') or 
                       contains(@class,' scheme/hasInstance ') or contains(@class,' scheme/hasKind ')]" mode="getBroaderSubject">
  <xsl:apply-templates select="parent::*" mode="getBroaderSubject">
    <xsl:with-param name="usingtype" select="@class"/>
  </xsl:apply-templates>
</xsl:template>
<xsl:template match="*[contains(@class,' scheme/subjectdef ')][@href]" mode="getBroaderSubject">
  <xsl:param name="usingtype"/>
  <xsl:variable name="relType">
    <xsl:choose>
      <xsl:when test="contains($usingtype,' scheme/hasNarrower ')">skos:broader</xsl:when>
      <xsl:when test="contains($usingtype,' scheme/hasPart ')">skos:broaderPartitive</xsl:when>
      <xsl:when test="contains($usingtype,' scheme/hasInstance ')">skos:broaderInstantive</xsl:when>
      <xsl:when test="contains($usingtype,' scheme/hasKind ')">skos:broader</xsl:when>
      <xsl:when test="ancestor::*[contains(@class,' scheme/hasNarrower ') or contains(@class,' scheme/hasPart ') or 
                                  contains(@class,' scheme/hasInstance ') or contains(@class,' scheme/hasKind ')][1][contains(@class,' scheme/hasNarrower ')]">
        <xsl:text>skos:broader</xsl:text>
      </xsl:when>
      <xsl:when test="ancestor::*[contains(@class,' scheme/hasPart ') or 
                                  contains(@class,' scheme/hasInstance ') or contains(@class,' scheme/hasKind ')][1][contains(@class,' scheme/hasPart ')]">
        <xsl:text>skos:broaderPartitive</xsl:text>
      </xsl:when>
      <xsl:when test="ancestor::*[contains(@class,' scheme/hasInstance ') or contains(@class,' scheme/hasKind ')][1][contains(@class,' scheme/hasInstance ')]">
        <xsl:text>skos:broaderInstantive</xsl:text>
      </xsl:when>
      <xsl:when test="ancestor::*[contains(@class,' scheme/hasKind ')]">
        <xsl:text>skos:broader</xsl:text> <!-- Better term to use? -->
      </xsl:when>
      <xsl:otherwise>skos:broader</xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:element name="{$relType}">
    <!-- xsl:call-template name="setLanguage"/ -->
    <xsl:attribute name="rdf:resource"><xsl:value-of select="$SCHEMEBASE"/>#<xsl:value-of select="substring-before(@href,$DITAEXT)"/></xsl:attribute>
  </xsl:element>
</xsl:template>

<xsl:template match="*[contains(@class,' map/relcell ')]" mode="relatedSubject">
  <xsl:apply-templates mode="relatedSubject"/>
</xsl:template>


<xsl:template match="*[contains(@class,' scheme/subjectdef ')][@href]" mode="relatedSubject">
  <xsl:if test="not(ancestor-or-self::*[@linking][1][@linking='sourceonly' or @linking='none'])">
    <skos:related rdf:resource="{$SCHEMEBASE}#{substring-before(@href,$DITAEXT)}">
      <!-- xsl:call-template name="setLanguage"/ -->
    </skos:related>
  </xsl:if>
</xsl:template>

</xsl:stylesheet>
