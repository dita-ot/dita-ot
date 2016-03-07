<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of the DITA Open Toolkit project.
     See the accompanying license.txt file for applicable licenses. -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:dita-ot="http://dita-ot.sourceforge.net/ns/201007/dita-ot"
                version="2.0"
                exclude-result-prefixes="xs dita-ot">

  <xsl:function name="dita-ot:css-class" as="xs:string">
    <xsl:param name="block-name" as="xs:string?"/>
    <xsl:param name="attr" as="attribute()"/>

    <xsl:sequence select="
      string-join(($block-name, concat(node-name($attr), '-', $attr)), '--')
    "/>
  </xsl:function>

  <xsl:function name="dita-ot:css-class" as="xs:string">
    <xsl:param name="attr" as="attribute()"/>

    <xsl:sequence select="
      dita-ot:css-class(xs:string(node-name($attr/parent::*)), $attr)
    "/>
  </xsl:function>

  <!-- Don't generate CSS classes for any element or attribute by default. -->
  <xsl:template match="* | @*" mode="css-class"/>

  <!-- Display attributes group -->
  <xsl:template match="@frame | @expanse | @scale" mode="css-class">
    <xsl:sequence select="dita-ot:css-class((), .)"/>
  </xsl:template>

</xsl:stylesheet>
