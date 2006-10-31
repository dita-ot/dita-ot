<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<xsl:template match="*[contains(@class, ' topic/title ')]" mode="table-external">
  <xsl:call-template name="wrap-fm-pi-begin"/>
  <fm_table_title_external>
     <xsl:apply-templates select="@*[name() != 'id']"/>
     <xsl:apply-templates select="@id"/>
     <xsl:apply-templates select="." mode="process-children"/>
  </fm_table_title_external>
  <xsl:call-template name="wrap-fm-pi-end"/>
</xsl:template>

<xsl:template match="*[contains(@class, ' topic/title ')]" mode="table-internal">
  <xsl:call-template name="wrap-fm-pi-begin"/>
  <fm_table_title_internal>
     <xsl:apply-templates select="@*[name() != 'id']"/>
     <xsl:apply-templates select="@id"/>
     <xsl:apply-templates select="." mode="process-children"/>
  </fm_table_title_internal>
  <xsl:call-template name="wrap-fm-pi-end"/>
</xsl:template>

<xsl:template match="*[contains(@class, ' topic/desc ')]" mode="table-external">
  <xsl:call-template name="wrap-fm-pi-begin"/>
  <fm_table_desc_external>
     <xsl:apply-templates select="@*[name() != 'id']"/>
     <xsl:apply-templates select="@id"/>
     <xsl:apply-templates select="." mode="process-children"/>
  </fm_table_desc_external>
  <xsl:call-template name="wrap-fm-pi-end"/>
</xsl:template>

<!-- Tables need a little massaging for title and desc. -->
<xsl:template match="*[contains(@class, ' topic/table ')]" mode="replace-tag">
  <xsl:call-template name="wrap-fm-pi-begin"/>
  <xsl:if test="$config-table-title = 'external'">
    <xsl:apply-templates select="title" mode="table-external"/>
  </xsl:if>
  <xsl:if test="$config-table-title = 'external'">
    <xsl:apply-templates select="desc" mode="table-external"/>
  </xsl:if>
  <fm_div outputclass="fm_table_wrap">
    <table>
      <xsl:apply-templates select="@*[name() != 'id']"/>
      <xsl:attribute name="fm_format">
        <xsl:apply-templates select="@class" mode="simplify-class"/>
        <xsl:if test="@outputclass">
          <xsl:text>.</xsl:text>
          <xsl:value-of select="@outputclass"/>
        </xsl:if>
      </xsl:attribute>
      <xsl:apply-templates select="@id"/>
      <xsl:if test="$config-table-title = 'internal'">
        <xsl:apply-templates select="title" mode="table-internal"/>
      </xsl:if>
      <!-- Can't put desc into table with internal style; FrameMaker has nowhere to put it. -->
      <xsl:apply-templates select="tgroup"/>
    </table>
  </fm_div>
  <xsl:call-template name="wrap-fm-pi-end"/>
</xsl:template>

</xsl:stylesheet>
