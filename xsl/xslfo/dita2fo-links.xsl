<?xml version='1.0'?>
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<!DOCTYPE xsl:transform [
<!-- entities for use in the generated output (must produce correctly in FO) -->
  <!ENTITY bullet        "&#x2022;"><!--check these two for better assignments -->
]>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:fo="http://www.w3.org/1999/XSL/Format" version="1.0">  
  
  <!--==== related-links content subset ====-->
  <xsl:template match="*[contains(@class,' topic/related-links ')]">
    <xsl:if test="$output-related-links">
      <fo:block>
        <fo:block font-weight="bold">
          <xsl:text>Related Links</xsl:text>
        </fo:block>
        <fo:block>
          <xsl:attribute name="start-indent">
            <xsl:value-of select="$basic-start-indent"/>
          </xsl:attribute>
          <xsl:apply-templates/>
        </fo:block>
      </fo:block>
    </xsl:if>
  </xsl:template>
  <xsl:template match="*[contains(@class,' topic/linklist ')]/*[contains(@class,' topic/title ')]">
    <fo:block font-weight="bold">
      <xsl:apply-templates/>
    </fo:block>
  </xsl:template>
  <xsl:template match="*[contains(@class,' topic/linklist ')]">
    <fo:block>
      <xsl:apply-templates/>
    </fo:block>
  </xsl:template>
  <xsl:template match="*[contains(@class,' topic/linkpool ')]">
    <fo:block>
      <xsl:apply-templates/>
    </fo:block>
  </xsl:template>
  <xsl:template match="*[contains(@class,' topic/linktext ')]">
    <fo:inline>
      <xsl:apply-templates/>
    </fo:inline>
  </xsl:template>
  <!-- rule for when the link is still not pointing at anything -->
  <xsl:template match="*[contains(@class,' topic/link ')][@href = '']">
    <fo:block color="red" start-indent="{$basic-start-indent} + {                    count(ancestor-or-self::*[contains(@class,' topic/linklist ')]) +                    count(ancestor-or-self::*[contains(@class,' topic/linkpool ')])}em">
      <xsl:text>&bullet; </xsl:text>
      <xsl:apply-templates/>
    </fo:block>
  </xsl:template>
  <!-- if there is an href, make it look like a link and remove prompt -->
  <xsl:template match="*[contains(@class,' topic/link ')][not(@href = '')]">
    <fo:block color="blue" text-decoration="underline" start-indent="{$basic-start-indent} + {                    count(ancestor-or-self::*[contains(@class,' topic/linklist ')]) +                    count(ancestor-or-self::*[contains(@class,' topic/linkpool ')])}em">
      <xsl:text>&bullet; </xsl:text>
      <xsl:apply-templates/>
    </fo:block>
  </xsl:template>
  <!-- ===============  end of related links markup  ================= -->
  <!-- ===============  xref section (these occur in body) ================= -->
  <!-- rule for when the xref is still not pointing at anything -->
  <xsl:template match="*[contains(@class,' topic/xref ')][@href = '']">
    <fo:inline color="red">
      <fo:inline font-weight="bold">[xref to: <xsl:value-of select="@href"/>]</fo:inline>
      <xsl:apply-templates/>
    </fo:inline>
  </xsl:template>
  <!-- if there is an href, make it look like a link and remove prompt -->
  <xsl:template match="*[contains(@class,' topic/xref ')][not(@href = '')]">
    <fo:inline color="blue" text-decoration="underline">
      <xsl:apply-templates/> (<xsl:value-of select="@href"/>) </fo:inline>
  </xsl:template>
  <!-- =================== end of related links and xrefs ====================== -->
</xsl:stylesheet>
