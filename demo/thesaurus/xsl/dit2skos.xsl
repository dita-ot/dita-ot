<?xml version="1.0" encoding="utf-8"?>
<!--
 | (C) Copyright IBM Corporation 2005 - 2006. All Rights Reserved.
 *-->
<xsl:stylesheet version="1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
                xmlns:skos="http://www.w3.org/2004/02/skos/core#"
                xmlns:foaf="http://xmlns.com/foaf/0.1/">

<xsl:import href="map2skos.xsl"/>
<xsl:import href="skosclassify.xsl"/>
<xsl:import href="skosscheme.xsl"/>

<xsl:param name="SCHEMEBASE">http://www.ibm.com/demo/subject</xsl:param>
<xsl:param name="CONTENTBASE" select="''"/>
<xsl:param name="DITAEXT">.dita</xsl:param>
<xsl:param name="DITAMAPEXT">.ditamap</xsl:param>
<xsl:param name="DEFAULTLANG">en-us</xsl:param>


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

<!--<xsl:template match="*[contains(@class,' scheme/subjectScheme ')]">-->
<xsl:template match="/*[contains(@class,' map/map ')]">
  <rdf:RDF xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
           xmlns:skos="http://www.w3.org/2004/02/skos/core#">
    <xsl:apply-templates/>
  </rdf:RDF>
</xsl:template>

</xsl:stylesheet>
