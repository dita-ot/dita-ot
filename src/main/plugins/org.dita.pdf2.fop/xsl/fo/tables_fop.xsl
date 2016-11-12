<?xml version="1.0" encoding="UTF-8"?>
<!--
This file is part of the DITA Open Toolkit project.

Copyright 2016 IBM Corporation

See the accompanying LICENSE file for applicable license.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:fo="http://www.w3.org/1999/XSL/Format"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  version="2.0"
  exclude-result-prefixes="xs">

    <xsl:template match="*[contains(@class, ' topic/dt ')]">
        <fo:block xsl:use-attribute-sets="dlentry.dt__content">
            <xsl:call-template name="commonattributes"/>
            <xsl:if test="not(preceding-sibling::*[contains(@class,' topic/dt ')])">
              <xsl:apply-templates select="../@id" mode="dlentry-id-for-fop"/>
              <xsl:apply-templates select="../*[contains(@class,' ditaot-d/ditaval-startprop ')]" mode="outofline"/>
            </xsl:if>
            <xsl:apply-templates/>
        </fo:block>
    </xsl:template>

    <xsl:template match="*[contains(@class,' topic/dlentry ')]/@id">
      <!-- FOP does not support @id on table rows; drop from the row and add to an <fo:inline> in the first term. -->
    </xsl:template>
    <xsl:template match="@id" mode="dlentry-id-for-fop">
        <fo:inline id="{.}"/>
    </xsl:template>

</xsl:stylesheet>
