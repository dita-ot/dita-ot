<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<!-- treat codeblock as table -->
<xsl:template match="*[contains(@class, ' pr-d/codeblock ')]">
	<xsl:choose>
	<xsl:when test="$config-codeblock-as-table = 'yes'">
		<fm_div>
			<table fm_format="pr-d.codeblock">
				<tgroup cols="1">
					<xsl:element name="colspec">
						<xsl:attribute name="colnum">1</xsl:attribute>
						<xsl:attribute name="colwidth">
							<xsl:value-of select="$config-text-frame-width"/>
						</xsl:attribute>
					</xsl:element>
					<tbody>
						<row>
							<entry>
								<pre class="pr-d.codeblock">
									<xsl:apply-templates/>
								</pre>
							</entry>
						</row>
					</tbody>
				</tgroup>
			</table>
		</fm_div>
	</xsl:when>
	<xsl:otherwise>
		<pre class="pr-d.codeblock">
			<xsl:apply-templates/>
		</pre>
	</xsl:otherwise>
	</xsl:choose>
</xsl:template>


<!-- put title under figure
<xsl:template match="*[contains(@class, ' topic/fig')]">
	<fig>
		<xsl:apply-templates select="image" mode="replace-tag"/>
		<xsl:apply-templates select="title" />
	</fig>
</xsl:template>
-->

</xsl:stylesheet>
<!-- Stylus Studio meta-information - (c) 2004-2005. Progress Software Corporation. All rights reserved.
<metaInformation>
<scenarios/><MapperMetaTag><MapperInfo srcSchemaPathIsRelative="yes" srcSchemaInterpretAsXML="no" destSchemaPath="" destSchemaRoot="" destSchemaPathIsRelative="yes" destSchemaInterpretAsXML="no"/><MapperBlockPosition></MapperBlockPosition><TemplateContext></TemplateContext><MapperFilter side="source"></MapperFilter></MapperMetaTag>
</metaInformation>
-->