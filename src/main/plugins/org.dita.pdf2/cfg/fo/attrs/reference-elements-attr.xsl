<?xml version="1.0" encoding="UTF-8"?>

<!--
    This file copyright by Suite Solutions, released under the same licenses as
    the rest of the DITA Open Toolkit project hosted on Sourceforge.net.
    See the accompanying license.txt file for applicable licenses.

    This file is a collection of basic settings for the FO plugin.  There are many
    more settings available in other files in the toolkit.  Please see the file
    README.txt in the main plugin directory for more information.
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:fo="http://www.w3.org/1999/XSL/Format"
  xmlns:rx="http://www.renderx.com/XSL/Extensions"
  version="2.0">

  <xsl:attribute-set name="properties" use-attribute-sets="base-font">
    <xsl:attribute name="width">100%</xsl:attribute>
    <xsl:attribute name="space-before">8pt</xsl:attribute>
    <xsl:attribute name="space-after">10pt</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="properties__body">
  </xsl:attribute-set>

  <xsl:attribute-set name="property">
  </xsl:attribute-set>

  <xsl:attribute-set name="property.entry">
  </xsl:attribute-set>

  <xsl:attribute-set name="property.entry__keycol-content" use-attribute-sets="common.table.body.entry common.table.head.entry">
      <xsl:attribute name="background-color">antiquewhite</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="property.entry__content" use-attribute-sets="common.table.body.entry">
  </xsl:attribute-set>

  <xsl:attribute-set name="prophead">
  </xsl:attribute-set>

  <xsl:attribute-set name="prophead__row">
  </xsl:attribute-set>

  <xsl:attribute-set name="prophead.entry">
  </xsl:attribute-set>

  <xsl:attribute-set name="prophead.entry__keycol-content" use-attribute-sets="common.table.body.entry common.table.head.entry">
      <xsl:attribute name="background-color">antiquewhite</xsl:attribute>
  </xsl:attribute-set>

  <xsl:attribute-set name="prophead.entry__content" use-attribute-sets="common.table.body.entry common.table.head.entry">
  </xsl:attribute-set>

  <xsl:attribute-set name="reference">
  </xsl:attribute-set>

  <xsl:attribute-set name="refbody" use-attribute-sets="body">
  </xsl:attribute-set>

  <xsl:attribute-set name="refsyn">
  </xsl:attribute-set>

</xsl:stylesheet>
