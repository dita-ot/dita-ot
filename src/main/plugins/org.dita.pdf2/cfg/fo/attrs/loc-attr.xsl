<?xml version='1.0'?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:rx="http://www.renderx.com/XSL/Extensions"
                version="2.0">

    <xsl:attribute-set name ="__loc__heading" use-attribute-sets="__toc__header">
    </xsl:attribute-set>

    <xsl:attribute-set name="__loc__indent" use-attribute-sets="__toc__indent__booklist">
    </xsl:attribute-set>

    <xsl:attribute-set name ="__loc__content" use-attribute-sets="base-font __toc__topic__content__booklist">
        <xsl:attribute name="font-weight">normal</xsl:attribute>
        <xsl:attribute name="space-before">5pt</xsl:attribute>
        <xsl:attribute name="space-after">5pt</xsl:attribute>
    </xsl:attribute-set>

    <xsl:attribute-set name="__loc__title" use-attribute-sets="__loc__content">
    </xsl:attribute-set>

    <xsl:attribute-set name="__loc__page-number">
        <xsl:attribute name="keep-together.within-line">always</xsl:attribute>
    </xsl:attribute-set>

</xsl:stylesheet>
