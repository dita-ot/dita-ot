<?xml version='1.0'?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:fo="http://www.w3.org/1999/XSL/Format"
    xmlns:rx="http://www.renderx.com/XSL/Extensions"
    version="1.0">

  <xsl:attribute-set name ="__lotf__heading" use-attribute-sets="__toc__header">
  </xsl:attribute-set>

  <xsl:attribute-set name="__lotf__indent" use-attribute-sets="__toc__indent__booklist">
    <xsl:attribute name="margin-left">42pt</xsl:attribute>
  </xsl:attribute-set>
  
  <xsl:attribute-set name ="__lotf__content" use-attribute-sets="__toc__topic__content__booklist">
    <xsl:attribute name ="font-size">12pt</xsl:attribute>
    <xsl:attribute name ="line-height">14pt</xsl:attribute>
    <xsl:attribute name="font-weight">normal</xsl:attribute>
    <xsl:attribute name="space-before">5pt</xsl:attribute>
    <xsl:attribute name="space-after">5pt</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name ="__lotf__leader">
    <xsl:attribute name ="leader-pattern">dots</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="__lotf__number" use-attribute-sets="__lotf__content">
    <xsl:attribute name="start-indent">1in</xsl:attribute>
    <xsl:attribute name="float">left</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="__lotf__title" use-attribute-sets="__lotf__content">
    <xsl:attribute name="margin-left">1.5in</xsl:attribute>
  </xsl:attribute-set>

</xsl:stylesheet>
