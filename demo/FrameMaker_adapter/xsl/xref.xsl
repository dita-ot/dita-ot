<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
	xmlns:xlink="http://www.w3.org/1999/xlink">

    <xsl:template match="*[contains(@class, ' topic/xref ')]" mode="replace-content">
    	<xsl:call-template name="create-xref">
    		<xsl:with-param name="linktext" select="."></xsl:with-param>
    	</xsl:call-template>
    </xsl:template>
	
	<xsl:template name="create-xref">
		<xsl:param name="linktext"></xsl:param>
		<xsl:choose>
			<!-- external URL with link anchor text -->
			<xsl:when test="@scope='external' and string-length(normalize-space($linktext))">
				<fm_external_link_with_anchor_text><xsl:copy-of select="@*"/>
					<fm_external_linktext><xsl:copy-of select="@*"/><xsl:call-template name="fm_url_marker"/><xsl:apply-templates select="$linktext" mode="process-children"/></fm_external_linktext>
					<fm_external_url_after_content><xsl:copy-of select="@*"/><xsl:call-template name="fm_url_marker"/><xsl:value-of select="@href"/></fm_external_url_after_content>
				</fm_external_link_with_anchor_text>
			</xsl:when>
			
			<!-- external URL with no link anchor text -->
			<xsl:when test="@scope='external'">
				<fm_external_link_no_anchor_text><xsl:copy-of select="@*"/>
					<fm_external_url><xsl:copy-of select="@*"/><xsl:call-template name="fm_url_marker"/><xsl:value-of select="@href"/></fm_external_url>
				</fm_external_link_no_anchor_text>
			</xsl:when>
			
			<!-- internal cross reference with link anchor text -->
			<xsl:when test="string-length(normalize-space(.))">
				<fm_internal_link_with_anchor_text><xsl:copy-of select="@*"/>
					<xsl:if test="$config-drop-xref-content = 'no'">
						<fm_internal_linktext><xsl:copy-of select="@*"/><xsl:call-template name="fm_goto_marker"/><xsl:apply-templates select="$linktext" mode="process-children"/></fm_internal_linktext>
					</xsl:if>
					<fm_internal_xref_after_content><xsl:attribute name="idref"><xsl:value-of select="substring-after(@href,'#')"/></xsl:attribute><xsl:copy-of select="@*"/></fm_internal_xref_after_content>
				</fm_internal_link_with_anchor_text>
			</xsl:when>
			
			<!-- internal cross reference without link anchor text -->
			<xsl:otherwise>
				<fm_internal_link_no_anchor_text><xsl:copy-of select="@*"/>
					<!--			<fm_internal_xref idref="{substring-after(@href,'#')}"><xsl:copy-of select="@*"/></fm_internal_xref> -->
					<fm_internal_xref><xsl:attribute name="idref"><xsl:value-of select="substring-after(@href,'#')"/></xsl:attribute><xsl:copy-of select="@*"/></fm_internal_xref>
				</fm_internal_link_no_anchor_text>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

    <xsl:template name="fm_url_marker">
			<xsl:processing-instruction name="Fm"> 
				<xsl:text>MARKER [Hypertext] message URL </xsl:text>
				<xsl:value-of select="@href"/>
			</xsl:processing-instruction>
    </xsl:template>
    
    <xsl:template name="fm_goto_marker">
			<xsl:processing-instruction name="Fm"> 
				<xsl:text>MARKER [Hypertext] gotolink </xsl:text>
				<xsl:value-of select="substring-after(@href,'#')"/>
			</xsl:processing-instruction>
    </xsl:template>

	<!-- desc inside an xref should be suppressed. -->
	<xsl:template match="*[contains(@class, ' topic/xref ')]/*[contains(@class, ' topic/desc ')]" mode="replace-tag"></xsl:template>
	
</xsl:stylesheet>
<!-- Stylus Studio meta-information - (c) 2004-2006. Progress Software Corporation. All rights reserved.
<metaInformation>
<scenarios/><MapperMetaTag><MapperInfo srcSchemaPathIsRelative="yes" srcSchemaInterpretAsXML="no" destSchemaPath="" destSchemaRoot="" destSchemaPathIsRelative="yes" destSchemaInterpretAsXML="no"/><MapperBlockPosition></MapperBlockPosition><TemplateContext></TemplateContext><MapperFilter side="source"></MapperFilter></MapperMetaTag>
</metaInformation>
-->