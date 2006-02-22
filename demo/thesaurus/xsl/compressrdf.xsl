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

<xsl:param name="DEFAULTLANG">en-us</xsl:param>

<xsl:key name="concepts" match="skos:Concept" use="@rdf:about"/>
<xsl:key name="documents" match="foaf:Document" use="@rdf:about"/>
<xsl:key name="subjects" match="foaf:Document/skos:subject" use="@rdf:resource"/>
<xsl:key name="subjectmerge" match="foaf:Document/skos:subject" use="concat(parent::*/@rdf:about,'\\',@rdf:resource,'\\',@xml:lang)"/>
<xsl:key name="conceptkids" match="skos:Concept/*" use="concat(parent::*/@rdf:about,'\\',name(),'\\',@rdf:about,'\\',@rdf:resource,'\\',@xml:lang,'\\',.)"/>

<!-- Should be referencing dita-utilities.xsl, and picking this up the language there.
     This is a dumbed down version of the getLowerCaseLang function. -->
<xsl:template name="setLanguage">
  <xsl:attribute name="xml:lang">
    <xsl:choose>
      <xsl:when test="ancestor-or-self::*[@xml:lang]">
        <xsl:value-of select="ancestor-or-self::*[@xml:lang][1]/@xml:lang"/>
      </xsl:when>
      <xsl:otherwise><xsl:value-of select="$DEFAULTLANG"/></xsl:otherwise>
    </xsl:choose>
  </xsl:attribute>
</xsl:template>

<xsl:template match="skos:Concept">
  <xsl:if test="generate-id(.)=generate-id(key('concepts',@rdf:about)[1])">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:for-each select="key('concepts',@rdf:about)/*">
        <xsl:if test="generate-id(.)=generate-id(key('conceptkids',concat(parent::*/@rdf:about,'\\',name(),'\\',@rdf:about,'\\',@rdf:resource,'\\',@xml:lang,'\\',.))[1])">
          <xsl:apply-templates select="."/>
        </xsl:if>
      </xsl:for-each>
      <xsl:for-each select="key('subjects',@rdf:about)">
        <xsl:if test="generate-id(.)=generate-id(key('subjectmerge',concat(parent::*/@rdf:about,'\\',@rdf:resource,'\\',@xml:lang))[1])">
          <skos:isSubjectOf rdf:resource="{parent::*/@rdf:about}">
            <xsl:call-template name="setLanguage"/>
          </skos:isSubjectOf>
        </xsl:if>
      </xsl:for-each>
    </xsl:copy>
  </xsl:if>
</xsl:template>

<xsl:template match="*">
  <xsl:copy>
    <xsl:apply-templates select="@*|*|text()"/>
  </xsl:copy>
</xsl:template>

<xsl:template match="@*|text()">
  <xsl:copy/>
</xsl:template>

  
</xsl:stylesheet>
