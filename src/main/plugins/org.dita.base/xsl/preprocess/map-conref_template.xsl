<?xml version="1.0" encoding="UTF-8"?>
<!--
This file is part of the DITA Open Toolkit project.

Copyright 2017 Jarno Elovirta

See the accompanying LICENSE file for applicable license.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:dita-ot="http://dita-ot.sourceforge.net/ns/201007/dita-ot"
                version="3.0">
  <xsl:import href="plugin:org.dita.base:xsl/preprocess/conrefImpl.xsl"/>
  <dita:extension id="dita.xsl.conref" behavior="org.dita.dost.platform.ImportXSLAction" xmlns:dita="http://dita-ot.sourceforge.net"/>
  <xsl:output method="xml" encoding="utf-8" indent="no" />

  <!-- Ignore missing conref target and retain conref for future processing -->
  <xsl:template match="*" mode="missing-conref-target-file">
    <xsl:call-template name="copy"/>
  </xsl:template>

  <!-- A retained conref that was pulled in from another directory by an earlier conref
       is relative to the directory of that file, so rebase it to the current file. -->
  <xsl:template match="@conref">
    <xsl:param name="current-relative-path" tunnel="yes" as="xs:string" select="''"/>
    <xsl:attribute name="conref"
                   select="if ($current-relative-path = '' or starts-with(., '#') or matches(., '^([a-zA-Z][a-zA-Z0-9+.-]*:|/)'))
                           then .
                           else dita-ot:normalize-uri(concat($current-relative-path, .))"/>
  </xsl:template>
  
</xsl:stylesheet>
