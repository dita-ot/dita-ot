<?xml version="1.0" encoding="utf-8"?>
<!--
 | (C) Copyright IBM Corporation 2005 - 2006. All Rights Reserved.
 *-->

<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:skos="http://www.w3.org/2004/02/skos/core#"
                xmlns:foaf="http://xmlns.com/foaf/0.1/">
  
<xsl:output method="xml"/>

<!-- First: use the definition in the topic. 
     Otherwise: use the shortdesc in the topic.
     Otherwise: Warn if topic exists with no definition, use local shortdesc.
     Otherwise: use local shortdesc. -->
<xsl:template name="getSubjectDefinition">
  <xsl:param name="SUBJECT_EXISTS"/>
  <xsl:choose>
    <xsl:when test="$SUBJECT_EXISTS='yes' and 
                    document(@href,/)/*[contains(@class,' topic/topic ')]/
                                      *[contains(@class,' topic/body ')]/
                                      *[contains(@class,' subject-d/subjectDetail ')]/
                                      *[contains(@class,' subject-d/subjectDefinition ')]">
      <xsl:apply-templates select="document(@href,/)/*[contains(@class,' topic/topic ')]/
                                                              *[contains(@class,' topic/body ')]/
                                                              *[contains(@class,' subject-d/subjectDetail ')]/
                                                              *[contains(@class,' subject-d/subjectDefinition ')]"/>
    </xsl:when>
    <xsl:when test="$SUBJECT_EXISTS='yes' and 
                    document(@href,/)/*[contains(@class,' topic/topic ')]/
                                      *[contains(@class,' topic/shortdesc ')]">
      <xsl:apply-templates select="document(@href,/)/*[contains(@class,' topic/topic ')]/
                                                              *[contains(@class,' topic/shortdesc ')]" mode="fallbackDefinition"/>
    </xsl:when>
    <xsl:otherwise>
      <xsl:if test="$SUBJECT_EXISTS='yes'">
        <!-- Message about a missing definition -->
      </xsl:if>
      <xsl:apply-templates select="*[contains(@class,' map/topicmeta ')]/*[contains(@class,' map/shortdesc ')]" mode="fallbackDefinition"/>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- For preferred label, use in order of preference:
     Local navtitle
     prefLabel from the topic
     title of the topic
     navtitle on any other subjectdef for this topic -->
<xsl:template name="getPrefLabel">
  <xsl:param name="SUBJECT_EXISTS"/>
  <xsl:choose>
    <xsl:when test="*[contains(@class,' scheme/subjectmeta ')]/*[contains(@class,' scheme/subjPrefLabel ')]">
      <skos:prefLabel><xsl:call-template name="setLanguage"/><xsl:value-of select="*[contains(@class,' scheme/subjectmeta ')]/*[contains(@class,' scheme/subjPrefLabel ')]/@content"/></skos:prefLabel>
    </xsl:when>
    <xsl:when test="@locktitle='yes' and @navtitle">
      <skos:prefLabel><xsl:call-template name="setLanguage"/><xsl:value-of select="@navtitle"/></skos:prefLabel>
    </xsl:when>
    <xsl:when test="$SUBJECT_EXISTS='yes' and 
                    document(@href,/)/*[contains(@class,' topic/topic ')]/
                                      *[contains(@class,' topic/body ')]/
                                      *[contains(@class,' subject-d/subjectDetail ')]/
                                      *[contains(@class,' subject-d/subjectLabels ')]/
                                      *[contains(@class,' subject-d/prefLabel ')]">
      <skos:prefLabel><xsl:call-template name="setLanguage"/><xsl:value-of select="document(@href,/)/*[contains(@class,' topic/topic ')]/
                                                              *[contains(@class,' topic/body ')]/
                                                              *[contains(@class,' subject-d/subjectDetail ')]/
                                                              *[contains(@class,' subject-d/subjectLabels ')]/
                                                              *[contains(@class,' subject-d/prefLabel ')]"/></skos:prefLabel>
    </xsl:when>
    <xsl:when test="$SUBJECT_EXISTS='yes' and 
                    document(@href,/)/*/*[contains(@class,' topic/title ')]">
      <skos:prefLabel><xsl:call-template name="setLanguage"/><xsl:value-of select="document(@href,/)/*/*[contains(@class,' topic/title ')]"/></skos:prefLabel>
    </xsl:when>
    <xsl:when test="@navtitle">
      <skos:prefLabel><xsl:call-template name="setLanguage"/><xsl:value-of select="@navtitle"/></skos:prefLabel>
    </xsl:when>
    <xsl:when test="key('subjects',@href)[@navtitle]">
      <skos:prefLabel><xsl:call-template name="setLanguage"/><xsl:value-of select="key('subjects',@href)[@navtitle][1]/@navtitle"/></skos:prefLabel>
    </xsl:when>
  </xsl:choose>
</xsl:template>

<xsl:template name="getPrefSymbol">
  <xsl:param name="SUBJECT_EXISTS"/>
  <xsl:choose>
    <xsl:when test="*[contains(@class,' scheme/subjectmeta ')]/*[contains(@class,' scheme/subjPrefSymbol ')]">
      <skos:prefSymbol rdf:resource="{*[contains(@class,' scheme/subjectmeta ')]/*[contains(@class,' scheme/subjPrefSymbol ')]/@content}">
        <xsl:call-template name="setLanguage"/>
      </skos:prefSymbol>
    </xsl:when>
    <xsl:when test="$SUBJECT_EXISTS='yes' and 
                    document(@href,/)/*[contains(@class,' topic/topic ')]/
                                      *[contains(@class,' topic/body ')]/
                                      *[contains(@class,' subject-d/subjectDetail ')]/
                                      *[contains(@class,' subject-d/prefSymbol ')]">
      <xsl:apply-templates select="document(@href,/)/*[contains(@class,' topic/topic ')]/
                                                              *[contains(@class,' topic/body ')]/
                                                              *[contains(@class,' subject-d/subjectDetail ')]/
                                                              *[contains(@class,' subject-d/prefSymbol ')]" mode="prefSymbol"/>
    </xsl:when>
    <xsl:when test="key('subjects',@href)/*[contains(@class,' scheme/subjectmeta ')]/*[contains(@class,' scheme/subjPrefSymbol ')]">
      <skos:prefSymbol rdf:resource="{key('subjects',@href)/*[contains(@class,' scheme/subjPrefSymbol ')][1]/@content}">
        <xsl:call-template name="setLanguage"/>
      </skos:prefSymbol>
    </xsl:when>
  </xsl:choose>
</xsl:template>

<xsl:template match="*[contains(@class,' subject-d/prefSymbol ')]" mode="prefSymbol">
  <skos:prefSymbol rdf:resource="{@href}"><xsl:call-template name="setLanguage"/></skos:prefSymbol>
</xsl:template>

<!-- Container for all SKOS information in a subjectdef. Do not process items that should only
     show up once, they were pulled previously. -->
<xsl:template match="*[contains(@class,' scheme/subjectmeta ')]">
  <xsl:apply-templates select="*[not(contains(@class,' scheme/subjPrefLabel ') or contains(@class,' scheme/subjPrefSymbol ') or
                                     contains(@class,' map/shortdesc '))]"/>
</xsl:template>

<!-- Items that are not yet turned in to anything -->
<xsl:template match="*[contains(@class,' scheme/subjectmeta ')]/*[contains(@class,' topic/author ')]"/>
<xsl:template match="*[contains(@class,' scheme/subjectmeta ')]/*[contains(@class,' topic/category ')]"/>
<xsl:template match="*[contains(@class,' scheme/subjectmeta ')]/*[contains(@class,' topic/copyright ')]"/>
<xsl:template match="*[contains(@class,' scheme/subjectmeta ')]/*[contains(@class,' topic/critdates ')]"/>
<xsl:template match="*[contains(@class,' scheme/subjectmeta ')]/*[contains(@class,' topic/permissions ')]"/>
<xsl:template match="*[contains(@class,' scheme/subjectmeta ')]/*[contains(@class,' topic/publisher ')]"/>
<xsl:template match="*[contains(@class,' scheme/subjectmeta ')]/*[contains(@class,' topic/resourceid ')]"/>
<xsl:template match="*[contains(@class,' scheme/subjectmeta ')]/*[contains(@class,' topic/source ')]"/>

<!-- Othermeta needs to be ignored, unless it is specialized to something we recognize -->
<xsl:template match="*[contains(@class,' scheme/subjectmeta ')]/*[contains(@class,' topic/othermeta ')]">
  <xsl:choose>
    <xsl:when test="contains(@class,' scheme/subjAltLabel ') or
                    contains(@class,' scheme/subjHiddenLabel ') or
                    contains(@class,' scheme/subjAltSymbol ')">
      <xsl:apply-templates select="." mode="process"/>
    </xsl:when>
    <xsl:otherwise>
      <!-- Ignore for now -->
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- Container for all SKOS information in a topic. Do not process the preferred symbol here - 
     it is processed from the subjectdef. -->
<xsl:template match="*[contains(@class,' subject-d/subjectDetail ')]">
  <xsl:apply-templates select="*[not(contains(@class,' subject-d/prefSymbol ') or contains(@class,' subject-d/subjectDefinition '))]"/>
</xsl:template> 

<!-- container for all labels. Do not process the preferred label - it is only
     processed if needed (determined inside the subjectdef). -->
<xsl:template match="*[contains(@class,' subject-d/subjectLabels ')]">
  <xsl:apply-templates select="*[not(contains(@class,' subject-d/prefLabel '))]"/>
</xsl:template> 
<xsl:template match="*[contains(@class,' subject-d/subjectLabels ')]/text()"/>

<!-- Process an alternate label, found in the topic or inside the map -->
<xsl:template match="*[contains(@class,' subject-d/altLabel ')]">
  <xsl:variable name="storeLabel"><xsl:apply-templates/></xsl:variable>
  <skos:altLabel><xsl:call-template name="setLanguage"/><xsl:value-of select="normalize-space($storeLabel)"/></skos:altLabel>
</xsl:template> 
<xsl:template match="*[contains(@class,' scheme/subjAltLabel ')]" mode="process">
  <skos:altLabel><xsl:call-template name="setLanguage"/><xsl:value-of select="@content"/></skos:altLabel>
</xsl:template> 

<!-- Process a hidden label, found in the topic or inside the map -->
<xsl:template match="*[contains(@class,' subject-d/hiddenLabel ')]">
  <xsl:variable name="storeLabel"><xsl:apply-templates/></xsl:variable>
  <skos:hiddenLabel><xsl:call-template name="setLanguage"/><xsl:value-of select="normalize-space($storeLabel)"/></skos:hiddenLabel>
</xsl:template> 
<xsl:template match="*[contains(@class,' scheme/subjHiddenLabel ')]" mode="process">
  <skos:hiddenLabel><xsl:call-template name="setLanguage"/><xsl:value-of select="@content"/></skos:hiddenLabel>
</xsl:template> 

<!-- Process an alternate symbol, found in the topic or inside the map -->
<xsl:template match="*[contains(@class,' subject-d/altSymbol ')]">
  <skos:altSymbol rdf:resource="{@href}"><xsl:call-template name="setLanguage"/></skos:altSymbol>
</xsl:template> 
<xsl:template match="*[contains(@class,' scheme/subjAltSymbol ')]" mode="process">
  <skos:altSymbol rdf:resource="{@content}"><xsl:call-template name="setLanguage"/></skos:altSymbol>
</xsl:template>

<xsl:template match="*[contains(@class,' subject-d/subjectDefinition ')]">
  <xsl:variable name="storeDef"><xsl:apply-templates/></xsl:variable>
  <skos:definition><xsl:call-template name="setLanguage"/><xsl:value-of select="normalize-space($storeDef)"/></skos:definition>
</xsl:template> 
<xsl:template match="*[contains(@class,' topic/shortdesc ')]" mode="fallbackDefinition">
  <xsl:variable name="storeDef"><xsl:apply-templates/></xsl:variable>
  <skos:definition><xsl:call-template name="setLanguage"/><xsl:value-of select="normalize-space($storeDef)"/></skos:definition>
</xsl:template>
<xsl:template match="*[contains(@class,' map/shortdesc ')]" mode="fallbackDefinition">
  <xsl:variable name="storeDef"><xsl:apply-templates/></xsl:variable>
  <skos:definition><xsl:call-template name="setLanguage"/><xsl:value-of select="normalize-space($storeDef)"/></skos:definition>
</xsl:template>

<xsl:template match="*[contains(@class,' subject-d/scopeNote ')]">
  <xsl:variable name="storeNote"><xsl:apply-templates/></xsl:variable>
  <skos:scopeNote><xsl:call-template name="setLanguage"/><xsl:value-of select="normalize-space($storeNote)"/></skos:scopeNote>
</xsl:template> 

<xsl:template match="*[contains(@class,' subject-d/historyNote ')]">
  <xsl:variable name="storeNote"><xsl:apply-templates/></xsl:variable>
  <skos:historyNote><xsl:call-template name="setLanguage"/><xsl:value-of select="normalize-space($storeNote)"/></skos:historyNote>
</xsl:template> 

<xsl:template match="*[contains(@class,' subject-d/editorialNote ')]">
  <xsl:variable name="storeNote"><xsl:apply-templates/></xsl:variable>
  <skos:editorialNote><xsl:call-template name="setLanguage"/><xsl:value-of select="normalize-space($storeNote)"/></skos:editorialNote>
</xsl:template> 

<xsl:template match="*[contains(@class,' subject-d/changeNote ')]">
  <xsl:variable name="storeNote"><xsl:apply-templates/></xsl:variable>
  <skos:changeNote><xsl:call-template name="setLanguage"/><xsl:value-of select="normalize-space($storeNote)"/></skos:changeNote>
</xsl:template> 

<xsl:template match="*[contains(@class,' subject-d/publicNote ')]">
  <xsl:variable name="storeNote"><xsl:apply-templates/></xsl:variable>
  <skos:publicNote><xsl:call-template name="setLanguage"/><xsl:value-of select="normalize-space($storeNote)"/></skos:publicNote>
</xsl:template> 

<xsl:template match="*[contains(@class,' subject-d/privateNote ')]">
  <xsl:variable name="storeNote"><xsl:apply-templates/></xsl:variable>
  <skos:privateNote><xsl:call-template name="setLanguage"/><xsl:value-of select="normalize-space($storeNote)"/></skos:privateNote>
</xsl:template> 

</xsl:stylesheet>
