<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<!-- Get relative width of "dt" (first) column of "dl"-as-table. -->
<xsl:template match="*[contains(@class, ' topic/dl ')]" mode="get-column-width-dt">
  <xsl:for-each select="*[contains(@class, ' topic/dlhead ') or contains(@class, ' topic/dlentry ')]/*[contains(@class, ' topic/dthd ') or contains(@class, ' topic/dt ')]">
    <xsl:sort select="string-length(.)" data-type="number" order="descending"/>
    <xsl:if test="position()=1">
      <xsl:value-of select="string-length(.)"/>
    </xsl:if>
  </xsl:for-each>
  <xsl:text>*</xsl:text>
</xsl:template>

<!-- Get relative width of "dd" (second) column of "dl"-as-table. -->
<xsl:template match="*[contains(@class, ' topic/dl ')]" mode="get-column-width-dd">
  <xsl:for-each select="*[contains(@class, ' topic/dlhead ') or contains(@class, ' topic/dlentry ')]/*[contains(@class, ' topic/ddhd ') or contains(@class, ' topic/dd ')]">
    <xsl:sort select="string-length(.)" data-type="number" order="descending"/>
    <xsl:if test="position()=1">
      <xsl:value-of select="string-length(.)"/>
    </xsl:if>
  </xsl:for-each>
  <xsl:text>*</xsl:text>
</xsl:template>

<!-- Generate "colspec" elements for "dl"-as-table. -->
<xsl:template match="*[contains(@class, ' topic/dl ')]" mode="create-colspec">
  <colspec colnum="1">
    <xsl:attribute name="colwidth">
      <xsl:apply-templates select="." mode="get-column-width-dt"/>
    </xsl:attribute>
  </colspec>
  <colspec colnum="2">
    <xsl:attribute name="colwidth">
      <xsl:apply-templates select="." mode="get-column-width-dd"/>
    </xsl:attribute>
  </colspec>
</xsl:template>

<!-- Convert "dthd" to "entry" for "dl"-as-table. -->
<xsl:template match="dthd" mode="dl-as-table">
  <xsl:call-template name="wrap-fm-pi-begin"/>
  <entry>
    <xsl:apply-templates select="@class | @outputclass"/>
    <xsl:apply-templates select="@id"/>
    <xsl:apply-templates select="." mode="process-children"/>
  </entry>
  <xsl:call-template name="wrap-fm-pi-end"/>
</xsl:template>

<!-- Convert "ddhd" to "entry" for "dl"-as-table. -->
<xsl:template match="ddhd" mode="dl-as-table">
  <!-- Add dummy entry if preceding "dthd" is missing. -->
  <xsl:if test="count(preceding-sibling::dthd) = 0">
    <entry/>
  </xsl:if>
  <xsl:call-template name="wrap-fm-pi-begin"/>
  <entry>
    <xsl:apply-templates select="@class | @outputclass"/>
    <xsl:apply-templates select="@id"/>
    <xsl:apply-templates select="." mode="process-children"/>
  </entry>
  <xsl:call-template name="wrap-fm-pi-end"/>
</xsl:template>

<!-- Convert "dlhead" to "row" for "dl"-as-table. -->
<xsl:template match="dlhead" mode="dl-as-table">
  <xsl:call-template name="wrap-fm-pi-begin"/>
  <row>
    <xsl:apply-templates select="@class | @outputclass"/>
    <xsl:apply-templates select="@id"/>
    <xsl:apply-templates select="*" mode="dl-as-table"/>
  </row>
  <xsl:call-template name="wrap-fm-pi-end"/>
</xsl:template>

<!-- Convert "dt" to either "entry" or "p"-inside-"entry" for "dl"-as-table. -->
<xsl:template match="dt" mode="dl-as-table">
  <xsl:apply-templates select="@class | @outputclass"/>
  <xsl:apply-templates select="@id"/>
  <xsl:apply-templates select="." mode="process-children"/>
</xsl:template>

<!-- Convert "dd" to either "entry" or "p"-inside-"entry" for "dl"-as-table. -->
<xsl:template match="dd" mode="dl-as-table">
  <xsl:apply-templates select="@class | @outputclass"/>
  <xsl:apply-templates select="@id"/>
  <xsl:apply-templates select="." mode="process-children"/>
</xsl:template>

<!-- Convert "dlentry" to "row" for "dl"-as-table. -->
<xsl:template match="dlentry" mode="dl-as-table">
  <xsl:call-template name="wrap-fm-pi-begin"/>
  <row>
    <xsl:apply-templates select="@class | @outputclass"/>
    <xsl:apply-templates select="@id"/>
    <!-- "dt" can be repeated once or more.  If more than one, wrap each in a paragraph. -->
    <xsl:choose>
      <xsl:when test="count(*[contains(@class, ' topic/dt ')]) = 1">
        <entry>
          <xsl:apply-templates select="*[contains(@class, ' topic/dt ')]" mode="dl-as-table"/>
        </entry>
      </xsl:when>
      <xsl:otherwise>
        <entry>
          <xsl:for-each select="*[contains(@class, ' topic/dt ')]">
            <p>
              <xsl:apply-templates select="." mode="dl-as-table"/>
            </p>
          </xsl:for-each>
        </entry>
      </xsl:otherwise>
    </xsl:choose>
    <!-- Do same for "dd". -->
    <xsl:choose>
      <xsl:when test="count(*[contains(@class, ' topic/dd ')]) = 1">
        <entry>
          <xsl:apply-templates select="*[contains(@class, ' topic/dd ')]" mode="dl-as-table"/>
        </entry>
      </xsl:when>
      <xsl:otherwise>
        <entry>
          <xsl:for-each select="*[contains(@class, ' topic/dd ')]">
            <p>
              <xsl:apply-templates select="." mode="dl-as-table"/>
            </p>
          </xsl:for-each>
        </entry>
      </xsl:otherwise>
    </xsl:choose>
  </row>
  <xsl:call-template name="wrap-fm-pi-end"/>
</xsl:template>

<!-- "dl" as table, when outputclass="table". -->
<xsl:template match="*[contains(@class, ' topic/dl ')][contains(@outputclass, 'table')]" mode="replace-tag">
  <xsl:call-template name="wrap-fm-pi-begin"/>
  <fm_div outputclass="fm_table_wrap">
    <table rowheader="firstCol">
      <xsl:apply-templates select="@class|@outputclass"/>
      <xsl:attribute name="fm_format">
        <xsl:apply-templates select="@class" mode="simplify-class"/>
        <xsl:if test="@outputclass">
          <xsl:text>.</xsl:text>
          <xsl:value-of select="@outputclass"/>
        </xsl:if>
      </xsl:attribute>
      <xsl:apply-templates select="@id"/>
      <tgroup cols="2">
        <xsl:apply-templates select="." mode="create-colspec"/>
        <xsl:if test="*[contains(@class, ' topic/dlhead ')]">
          <thead>
            <xsl:apply-templates select="*[contains(@class, ' topic/dlhead ')]" mode="dl-as-table"/>
          </thead>
        </xsl:if>
        <tbody>
          <xsl:apply-templates select="*[contains(@class, ' topic/dlentry ')]" mode="dl-as-table"/>
        </tbody>
      </tgroup>
    </table>
  </fm_div>
  <xsl:call-template name="wrap-fm-pi-end"/>
</xsl:template>

</xsl:stylesheet>
<!-- Stylus Studio meta-information - (c) 2004-2005. Progress Software Corporation. All rights reserved.
<metaInformation>
<scenarios/><MapperMetaTag><MapperInfo srcSchemaPathIsRelative="yes" srcSchemaInterpretAsXML="no" destSchemaPath="" destSchemaRoot="" destSchemaPathIsRelative="yes" destSchemaInterpretAsXML="no"/><MapperBlockPosition></MapperBlockPosition><TemplateContext></TemplateContext><MapperFilter side="source"></MapperFilter></MapperMetaTag>
</metaInformation>
-->