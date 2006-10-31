<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<!-- Number of columns in "simpletable" is greatest number of "stentry" in any given row. -->
<xsl:template match="*[contains(@class, ' topic/simpletable ')]" mode="count-columns">
  <xsl:choose>
    <!-- If table has a "cols" attribute, use that. (Not standard DITA.) -->
    <xsl:when test="@cols">
      <xsl:value-of select="@cols" />
    </xsl:when>
    <xsl:otherwise>
      <xsl:for-each select="*[contains(@class, ' topic/sthead ') or contains(@class, ' topic/strow ')]">
        <xsl:sort select="count(*[contains(@class, ' topic/stentry ')])" data-type="number" order="descending"/>
        <xsl:if test="position()=1">
          <xsl:value-of select="count(*[contains(@class, ' topic/stentry ')])"/>
        </xsl:if>
      </xsl:for-each>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- Extract a column width from the "relcolwidth" attribute. -->
<xsl:template name="extract-relcolwidth">
  <xsl:param name="colnum"/>
  <xsl:param name="relcolwidth"/>
  <xsl:choose>
    <xsl:when test="$colnum = 1">
      <!-- Width is string up to first space. -->
      <xsl:choose>
        <xsl:when test="contains($relcolwidth, ' ')">
          <xsl:value-of select="substring-before($relcolwidth, ' ')"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="$relcolwidth"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:when>
    <xsl:otherwise>
      <!-- Recurse to next column. -->
      <xsl:call-template name="extract-relcolwidth">
        <xsl:with-param name="colnum" select="$colnum - 1"/>
        <xsl:with-param name="relcolwidth" select="substring-after($relcolwidth, ' ')"/>
      </xsl:call-template>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>

<!-- Extract or guess width of column number $colnum in a "simpletable". -->
<xsl:template match="*[contains(@class, ' topic/simpletable ')]" mode="get-column-width">
  <xsl:param name="colnum"/>
  <xsl:choose>
    <xsl:when test="@relcolwidth">
      <xsl:call-template name="extract-relcolwidth">
        <xsl:with-param name="colnum" select="$colnum"/>
        <xsl:with-param name="relcolwidth" select="normalize-space(@relcolwidth)"/>
      </xsl:call-template>
    </xsl:when>
    <xsl:otherwise>
      <!-- Guess relative column width as the number of characters in the widest cell in this column. -->
      <xsl:for-each select="*[contains(@class, ' topic/sthead ') or contains(@class, ' topic/strow ')]">
        <xsl:sort select="string-length(*[contains(@class, ' topic/stentry ')][$colnum])" data-type="number" order="descending"/>
        <xsl:if test="position()=1">
          <xsl:value-of select="string-length(*[contains(@class, ' topic/stentry ')][$colnum])"/>
        </xsl:if>
      </xsl:for-each>
      <xsl:text>*</xsl:text>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>


<!-- Generate "colspec" elements for "simpletable"-as-table. -->
<xsl:template match="*[contains(@class, ' topic/simpletable ')]" mode="create-colspec">
  <xsl:param name="cols"/>
  <xsl:param name="colnum" select="1"/>
  <xsl:if test="$colnum &lt;= $cols">
    <colspec>
      <xsl:attribute name="colnum">
        <xsl:value-of select="$colnum"/>
      </xsl:attribute>
      <xsl:attribute name="colwidth">
        <xsl:apply-templates select="." mode="get-column-width">
	  <xsl:with-param name="colnum" select="$colnum"/>
        </xsl:apply-templates>
      </xsl:attribute>
    </colspec>
    <xsl:apply-templates select="." mode="create-colspec">
      <xsl:with-param name="cols" select="$cols"/>
      <xsl:with-param name="colnum" select="$colnum + 1"/>
    </xsl:apply-templates>
  </xsl:if>
</xsl:template>

<!-- Convert "stentry" to "entry". -->
<xsl:template match="*[contains(@class, ' topic/stentry ')]">
  <xsl:call-template name="wrap-fm-pi-begin"/>
  <entry>
    <xsl:apply-templates select="@class | @outputclass"/>
    <xsl:apply-templates select="@id"/>
    <xsl:apply-templates select="." mode="process-children"/>
  </entry>
  <xsl:call-template name="wrap-fm-pi-end"/>
</xsl:template>

<!-- Convert "sthead" to "row". -->
<xsl:template match="*[contains(@class, ' topic/sthead ')]">
  <xsl:call-template name="wrap-fm-pi-begin"/>
  <row>
    <xsl:apply-templates select="@class | @outputclass"/>
    <xsl:apply-templates select="@id"/>
    <xsl:apply-templates select="*"/>
  </row>
  <xsl:call-template name="wrap-fm-pi-end"/>
</xsl:template>

<!-- Convert "strow" to "row". -->
<xsl:template match="*[contains(@class, ' topic/strow ')]">
  <xsl:call-template name="wrap-fm-pi-begin"/>
  <row>
    <xsl:apply-templates select="@class | @outputclass"/>
    <xsl:apply-templates select="@id"/>
    <xsl:apply-templates select="*"/>
  </row>
  <xsl:call-template name="wrap-fm-pi-end"/>
</xsl:template>

<!-- Convert "simpletable" to "table". -->
<xsl:template match="*[contains(@class, ' topic/simpletable ')]" mode="replace-tag">
  <xsl:variable name="cols">
    <xsl:apply-templates select="." mode="count-columns"/>
  </xsl:variable>
  <xsl:call-template name="wrap-fm-pi-begin"/>
  <fm_div outputclass="fm_table_wrap">
    <table>
      <xsl:if test="@keycol = 1">
        <xsl:attribute name="rowheader">firstcol</xsl:attribute>
      </xsl:if>
      <xsl:apply-templates select="@class | @outputclass"/>
      <xsl:attribute name="fm_format">
        <xsl:apply-templates select="@class" mode="simplify-class"/>
        <xsl:if test="@outputclass">
          <xsl:text>.</xsl:text>
          <xsl:value-of select="@outputclass"/>
        </xsl:if>
      </xsl:attribute>
      <xsl:apply-templates select="@id"/>
      <!-- Create intermediate "tgroup" element. -->
      <tgroup>
        <xsl:attribute name="cols">
          <xsl:value-of select="$cols"/>
        </xsl:attribute>
        <!-- Generate colspec -->
        <xsl:apply-templates select="." mode="create-colspec">
          <xsl:with-param name="cols" select="$cols"/>
        </xsl:apply-templates>
        <!-- Optional table head. -->
        <xsl:if test="*[contains(@class, ' topic/sthead ')]">
          <!-- Create intermediate "thead" element. -->
          <thead>
            <xsl:apply-templates select="*[contains(@class, ' topic/sthead ')]"/>
          </thead>
        </xsl:if>
        <!-- Create intermediate "tbody" element. -->
        <tbody>
          <xsl:apply-templates select="*[contains(@class, ' topic/strow ')]"/>
        </tbody>
      </tgroup>
    </table>
  </fm_div>
  <xsl:call-template name="wrap-fm-pi-end"/>
</xsl:template>

</xsl:stylesheet>
