<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<!-- Todo: fancier logic to decide what DPI to use for any given image. -->
<xsl:template match="*[contains(@class, ' topic/image ')]" mode="dpi">
  <xsl:text>150</xsl:text>
</xsl:template>

<!-- put figure title under image if config = yes -->
<xsl:template match="*[contains(@class, ' topic/fig ')]">
	<xsl:element name="fig">
		<xsl:copy-of select="@id"/>
		<xsl:if test="$config-title-after-image='no'">
			<xsl:copy-of select="title"/>
		</xsl:if>
		<xsl:apply-templates select="image"/>
		<xsl:if test="$config-title-after-image='yes'">
			<xsl:copy-of select="title"/>
		</xsl:if>
	</xsl:element>
</xsl:template>

<xsl:template match="*[contains(@class, ' topic/image ')]" mode="attributes">
  <xsl:variable name="dpi">
    <xsl:apply-templates select="." mode="dpi"/>
  </xsl:variable>
  <xsl:apply-templates select="@class|@outputclass"/>
  <xsl:attribute name="fm_file"><xsl:value-of select="@href"/></xsl:attribute>
  <xsl:attribute name="fm_dpi"><xsl:value-of select="$dpi"/></xsl:attribute>
  <xsl:attribute name="fm_position">
  	<xsl:choose>
		<xsl:when test="@placement='inline'">
  			<xsl:value-of select="@placement"/>
		</xsl:when>
		<xsl:otherwise>below</xsl:otherwise>
	</xsl:choose>
  </xsl:attribute>

<!-- Best not to touch these unless we learn to dig through image files to
     learn their sizes. 
  <xsl:attribute name="fm_height">

  </xsl:attribute>
  <xsl:attribute name="fm_width">

  </xsl:attribute>
-->
  <xsl:attribute name="fm_baseline_offset">
    <xsl:choose>
      <!-- Tweak baseline offset on images that I've said are icons. -->
      <xsl:when test="@outputclass='icon'">
        <xsl:text>-4</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>0</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:attribute>
  <xsl:attribute name="fm_xoffset">
    <xsl:text>0</xsl:text>
  </xsl:attribute>
  <xsl:attribute name="fm_yoffset">
    <xsl:text>0</xsl:text>
  </xsl:attribute>
<!--
  <xsl:attribute name="fm_alignment"><xsl:value-of select="@align"/></xsl:attribute>
-->
</xsl:template>


<xsl:template match="*[contains(@class, ' topic/image')]" mode="replace-tag">
	<xsl:call-template name="wrap-fm-pi-begin"/>
			<xsl:choose>
				<!-- Block-level images are to be wrapped in "fm_div" to force paragraph treatment. -->
				<xsl:when test="@placement='break'">
					<fm_div outputclass="fm_image_wrap">
						<xsl:attribute name="fm_image_align">
							<xsl:choose>
								<xsl:when test="@align">
									<xsl:value-of select="@align"/>
								</xsl:when>
								<xsl:otherwise>center</xsl:otherwise>
							</xsl:choose>

						</xsl:attribute>
						<xsl:apply-templates select="@id"/>
						<image>
							<xsl:apply-templates select="." mode="attributes"/>
						</image>
					</fm_div>

				</xsl:when>
		<!-- Inline images are left as-is. -->
		<xsl:otherwise>
				<!--	<xsl:apply-templates select="@id"/>  -->
					<image>
						<xsl:apply-templates select="." mode="attributes"/>
					</image>
		</xsl:otherwise>
	</xsl:choose>
	<xsl:call-template name="wrap-fm-pi-end"/>
</xsl:template>


</xsl:stylesheet>
<!-- Stylus Studio meta-information - (c) 2004-2005. Progress Software Corporation. All rights reserved.
<metaInformation>
<scenarios/><MapperMetaTag><MapperInfo srcSchemaPathIsRelative="yes" srcSchemaInterpretAsXML="no" destSchemaPath="" destSchemaRoot="" destSchemaPathIsRelative="yes" destSchemaInterpretAsXML="no"/><MapperBlockPosition></MapperBlockPosition><TemplateContext></TemplateContext><MapperFilter side="source"></MapperFilter></MapperMetaTag>
</metaInformation>
-->