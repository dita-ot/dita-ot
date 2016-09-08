<?xml version="1.0" encoding="UTF-8"?>
<!--
This file is part of the DITA Open Toolkit project.

Copyright 2016 Eero Helenius

See the accompanying LICENSE file for applicable license.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:dita-ot="http://dita-ot.sourceforge.net/ns/201007/dita-ot"
                xmlns:table="http://dita-ot.sourceforge.net/ns/201007/dita-ot/table"
                xmlns:simpletable="http://dita-ot.sourceforge.net/ns/201007/dita-ot/simpletable"
                version="2.0"
                exclude-result-prefixes="xs dita-ot table simpletable">

  <xsl:template mode="generate-table-header" match="
    *[contains(@class,' task/choicetable ')]
     [empty(*[contains(@class,' task/chhead ')])]
  ">
    <chhead class="- topic/sthead task/chhead ">
      <choptionhd class="- topic/stentry task/choptionhd ">
        <xsl:attribute name="id" select="simpletable:generate-headers(., 'option')"/>
        <xsl:sequence select="dita-ot:get-variable(., 'Option')"/>
      </choptionhd>

      <chdeschd class="- topic/stentry task/chdeschd ">
        <xsl:attribute name="id" select="simpletable:generate-headers(., 'desc')"/>
        <xsl:sequence select="dita-ot:get-variable(., 'Description')"/>
      </chdeschd>
    </chhead>
  </xsl:template>

  <xsl:template mode="headers" match="
    *[contains(@class,' task/choption ')]
     [empty(simpletable:get-current-table(.)/*[contains(@class,' task/chhead ')])]
  ">
    <xsl:attribute name="headers" select="
      simpletable:generate-headers(simpletable:get-current-table(.), 'option')
    "/>
  </xsl:template>

  <xsl:template mode="headers" match="
    *[contains(@class,' task/chdesc ')]
     [empty(simpletable:get-current-table(.)/*[contains(@class,' task/chhead ')])]
  ">
    <xsl:attribute name="headers" select="
      simpletable:generate-headers(simpletable:get-current-table(.), 'desc')
    "/>
  </xsl:template>

</xsl:stylesheet>
