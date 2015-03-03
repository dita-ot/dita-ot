<?xml version="1.0" encoding="UTF-8" ?>
<!-- This file is part of the DITA Open Toolkit project.
     See the accompanying license.txt file for applicable licenses.-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="2.0">

  <xsl:template match="*[contains(@class, ' xml-d/xmlelement ')]">
    <code>
      <xsl:call-template name="commonattributes"/>
      <xsl:text>&lt;</xsl:text>
      <xsl:apply-templates/>
      <xsl:text>&gt;</xsl:text>
    </code>
  </xsl:template>
  
  <xsl:template match="*[contains(@class, ' xml-d/xmlatt ')]">
    <code>
      <xsl:call-template name="commonattributes"/>
      <xsl:text>@</xsl:text>
      <xsl:apply-templates/>
    </code>
  </xsl:template>
  
  <xsl:template match="*[contains(@class, ' xml-d/textentity ')]">
    <code>
      <xsl:call-template name="commonattributes"/>
      <xsl:text>&amp;</xsl:text>
      <xsl:apply-templates/>
      <xsl:text>;</xsl:text>
    </code>
  </xsl:template>
  
  <xsl:template match="*[contains(@class, ' xml-d/parameterentity ')]">
    <code>
      <xsl:call-template name="commonattributes"/>
      <xsl:text>%</xsl:text>
      <xsl:apply-templates/>
      <xsl:text>;</xsl:text>
    </code>
  </xsl:template>
  
  <xsl:template match="*[contains(@class, ' xml-d/numcharref ')]">
    <code>
      <xsl:call-template name="commonattributes"/>
      <xsl:text>&amp;#</xsl:text>
      <xsl:apply-templates/>
      <xsl:text>;</xsl:text>
    </code>
  </xsl:template>
  
  <xsl:template match="*[contains(@class, ' xml-d/xmlnsname ')]">
    <code>
      <xsl:call-template name="commonattributes"/>
      <xsl:apply-templates/>
    </code>
  </xsl:template>
  
  <xsl:template match="*[contains(@class, ' xml-d/xmlpi ')]">
    <code>
      <xsl:call-template name="commonattributes"/>
      <xsl:apply-templates/>
    </code>
  </xsl:template>
    
</xsl:stylesheet>
