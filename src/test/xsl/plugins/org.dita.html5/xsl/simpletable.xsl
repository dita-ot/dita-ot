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
  
</xsl:stylesheet>
