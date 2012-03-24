<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"  version="1.0">

<xsl:template match="object">
<xsl:if test="@otherprops">
<xsl:element name="a">
   <xsl:attribute name="href">../../../../video/html/segmentvideos.html?segmentid=<xsl:value-of select="@otherprops"/></xsl:attribute>
   <!-- frame target in frameset -->
	<xsl:attribute name="target">
		<xsl:text>viewvideoframe</xsl:text>
	</xsl:attribute>
   <!-- hook to CSS in HTML -->
	<xsl:attribute name="class">
		<xsl:text>link2video</xsl:text>
	</xsl:attribute>
	<xsl:value-of select="@outputclass"/>
</xsl:element> 
</xsl:if>
</xsl:template>

<!-- </ph> tag provides may also be used as a containing tag for video segment references without requiring deeply embedded </object> structures. -->
<!--
<xsl:template match="ph">
<xsl:if test="@otherprops">
<xsl:element name="a">
   <xsl:attribute name="href">../../../../vidocs/pages/segmentvideos.html?segmentid=<xsl:value-of select="@otherprops"/></xsl:attribute>
	<xsl:attribute name="target">
		<xsl:text>viewvideoframe</xsl:text>
	</xsl:attribute>
	<xsl:attribute name="class">
		<xsl:text>link2video</xsl:text>
	</xsl:attribute>
	<xsl:value-of select="@outputclass"/>
</xsl:element>   
</xsl:if>
</xsl:template>
-->
   
</xsl:stylesheet>    