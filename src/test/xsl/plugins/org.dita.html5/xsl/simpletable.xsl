<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:dita-ot="http://dita-ot.sourceforge.net/ns/201007/dita-ot"
                xmlns:table="http://dita-ot.sourceforge.net/ns/201007/dita-ot/table"
                xmlns:simpletable="http://dita-ot.sourceforge.net/ns/201007/dita-ot/simpletable"
                version="2.0"
                exclude-result-prefixes="xs dita-ot table simpletable">
  
  <xsl:import href="../../../../../main/plugins/org.dita.html5/xsl/functions.xsl"/>
  <xsl:import href="../../../../../main/plugins/org.dita.html5/xsl/simpletable.xsl"/>

  <!-- Mocks -->

  <xsl:key name="enumerableByClass"
    match="
      *[contains(@class, ' topic/fig ')][*[contains(@class, ' topic/title ')]] |
      *[contains(@class, ' topic/table ')][*[contains(@class, ' topic/title ')]] |
      *[contains(@class, ' topic/simpletable ')][*[contains(@class, ' topic/title ')]] |
      *[contains(@class, ' topic/fn ') and empty(@callout)]"
    use="tokenize(@class, '\s+')"/>
  
  <xsl:template name="bidi-area" as="xs:boolean">
    <xsl:sequence select="false()"/>
  </xsl:template>
  
  <xsl:template name="spec-title">
    <xsl:if test="@spectitle">
      <div style="margin-top: 1em;">
        <strong>
          <xsl:value-of select="@spectitle"/>
        </strong>
      </div>
    </xsl:if>
  </xsl:template>
  
  <xsl:function name="dita-ot:get-variable" as="node()*">
    <xsl:param name="ctx" as="node()"/>
    <xsl:param name="id" as="xs:string"/>
    <!--xsl:param name="params" as="node()*"/-->
    <xsl:value-of select="$id"/>
  </xsl:function>
  
  <xsl:template match="*" mode="table:common">
    <xsl:copy-of select="@id"/>
  </xsl:template>
  
  <xsl:function name="dita-ot:generate-stable-id" as="xs:string">
    <xsl:param name="element" as="element()"/>
    
    <xsl:value-of>
      <xsl:for-each select="$element/ancestor-or-self::*">
        <xsl:if test="position() ne 1">_</xsl:if>
        <xsl:value-of select="name()"/>
        <xsl:text>-</xsl:text>
        <xsl:number/>
      </xsl:for-each>
    </xsl:value-of>
    <!--
    <xsl:variable name="topic" select="$element/ancestor-or-self::*[contains(@class, ' topic/topic ')][1]" as="element()"/>
    <xsl:variable name="parent-element" select="$element/ancestor-or-self::*[@id][1][not(. is $topic)]" as="element()?"/>
    <xsl:variable name="closest" select="($parent-element, $topic)[1]" as="element()"/>
    <xsl:variable name="index" select="count($closest/descendant::*[local-name() = local-name($element)][. &lt;&lt; $element]) + 1" as="xs:integer"/>
    
    <xsl:sequence select="dita-ot:generate-id($topic/@id, string-join(($parent-element/@id, local-name($element), string($index)), $HTML_ID_SEPARATOR))"/>
    -->
  </xsl:function>
  
</xsl:stylesheet>
