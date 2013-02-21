<?xml version="1.0"?>
<!-- 
   Stylesheet to produce normalized, resolved DITA for use
   within dynamic DITA renderers such as Eclipse. The output meets the
   following requirements:
   * Debug information is removed (@xtrf/@xtrc)
   * DITA-OT processing instructions removed (workdir and path2proj)
   * Draft elements are removed unless $DRAFT=yes
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  
  <xsl:import href="plugin:org.dita.base:xsl/common/output-message.xsl"/>
  <xsl:import href="plugin:org.dita.base:xsl/common/dita-utilities.xsl"/>

  <xsl:output method="xml"/>

  <xsl:param name="DRAFT" select="'no'"/>

  <xsl:variable name="msgprefix">DOTX</xsl:variable>

  <xsl:template match="*|@*|processing-instruction()|comment()|text()">
    <xsl:copy>
      <xsl:apply-templates select="@*|*|processing-instruction()|comment()|text()"/>
    </xsl:copy>
  </xsl:template>

  <!-- xtrf and xtrc debug attributes are removed from the output -->
  <xsl:template match="@xtrf|@xtrc"/>

  <!-- Remove DITA-OT processing PI's -->
  <xsl:template match="processing-instruction()[name()='workdir' or name()='workdir-uri' or name()='path2project' or name()='path2project-uri']"/>

  <!-- Remove draft elements unless instructed to leave them in -->
  <xsl:template match="*[contains(@class,' topic/draft-comment ') or
                         contains(@class,' topic/required-cleanup ')]">
    <xsl:if test="$DRAFT='yes'">
      <xsl:copy>
        <xsl:apply-templates select="@*|*|processing-instruction()|comment()|text()"/>
      </xsl:copy>
    </xsl:if>
  </xsl:template>

</xsl:stylesheet>
