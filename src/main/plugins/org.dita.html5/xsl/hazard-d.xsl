<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet version="2.0"
     xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
     xmlns:ditamsg="http://dita-ot.sourceforge.net/ns/200704/ditamsg"
     xmlns:dita-ot="http://dita-ot.sourceforge.net/ns/201007/dita-ot"
     xmlns:xs="http://www.w3.org/2001/XMLSchema"
     exclude-result-prefixes="ditamsg dita-ot xs">
  
  <xsl:param name="default.hazard.image.filename" as="xs:string" select="'ISO_7010_W001.svg'"/>
  <xsl:variable name="default.hazard.image" as="xs:string">
    <xsl:sequence select="concat(/processing-instruction()[name()='path2rootmap-uri'], $default.hazard.image.filename)"/>
  </xsl:variable>
  
  <xsl:template match="*[contains(@class,' hazard-d/messagepanel ')]" mode="get-element-ancestry"><xsl:value-of select="name()"/></xsl:template>
  <xsl:template match="*[contains(@class,' hazard-d/messagepanel ')]/*" mode="get-element-ancestry"><xsl:value-of select="name()"/></xsl:template>
  <xsl:template match="*[contains(@class,' hazard-d/hazardstatement ')]">
    <xsl:variable name="type" select="(@type, 'caution')[1]" as="xs:string"/>
    <xsl:variable name="hazardcolor">
      <xsl:choose>
        <xsl:when test="$type='caution'">#FFD100</xsl:when><!--yellow-->
        <xsl:when test="$type='danger'">#C8102E</xsl:when><!--red-->
        <xsl:when test="$type='warning'">#FF8200</xsl:when><!--orange-->
        <xsl:when test="$type='note' or $type='notice' or $type='attention'">#0072CE</xsl:when><!--blue-->
        <xsl:otherwise>#FFD100</xsl:otherwise><!-- caution yellow -->
      </xsl:choose>
    </xsl:variable>
    <xsl:apply-templates select="*[contains(@class,' ditaot-d/ditaval-startprop ')]"/>
    <table role="presentation" border="1">
      <xsl:call-template name="commonattributes"/>
      <tr>
        <th colspan="2" style="background-color: {$hazardcolor};">
          <xsl:choose>
            <xsl:when test="$type='other'"><xsl:sequence select="@othertype"></xsl:sequence></xsl:when>
            <xsl:otherwise>
              <xsl:call-template name="getVariable">
                <xsl:with-param name="id" select="dita-ot:capitalize($type)"/>
              </xsl:call-template>
            </xsl:otherwise>
          </xsl:choose>
        </th>
      </tr>
      <tr>
        <td>
          <xsl:apply-templates select="*[contains(@class,' hazard-d/hazardsymbol ')]"/>
          <xsl:if test="empty(*[contains(@class,' hazard-d/hazardsymbol ')])">
            <!--<xsl:call-template name="getVariable">
              <xsl:with-param name="id" select="'hazard.image.default'"/>
            </xsl:call-template>-->
            <img src="{$default.hazard.image}" alt="" height="30px" width="30px"/>
          </xsl:if>
        </td>
        <td>
          <xsl:apply-templates select="*[contains(@class,' hazard-d/messagepanel ')]"/>
        </td>
      </tr>
    </table>
    <xsl:apply-templates select="*[contains(@class,' ditaot-d/ditaval-endprop ')]"/>
  </xsl:template>
  <xsl:template match="*[contains(@class,' hazard-d/messagepanel ')]">
    <div>
      <xsl:call-template name="commonattributes"/>
      <xsl:apply-templates/>
    </div>
  </xsl:template>
  <xsl:template match="*[contains(@class,' hazard-d/messagepanel ')]/*">
    <div>
      <xsl:call-template name="commonattributes"/>
      <xsl:apply-templates/>
    </div>
  </xsl:template>
  
</xsl:stylesheet>
