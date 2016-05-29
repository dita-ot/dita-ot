<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of the DITA Open Toolkit project.
     See the accompanying license.txt file for applicable licenses. -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:dita-ot="http://dita-ot.sourceforge.net/ns/201007/dita-ot"
                xmlns:table="http://dita-ot.sourceforge.net/ns/201007/dita-ot/table"
                version="2.0"
                exclude-result-prefixes="xs dita-ot table">

  <xsl:template match="*" mode="table:common">
    <xsl:call-template name="commonattributes"/>
    <xsl:call-template name="setid"/>
    <xsl:apply-templates select="." mode="css-class"/>
  </xsl:template>

  <xsl:template match="*[contains(@class,' topic/table ')]" name="topic.table">
    <xsl:apply-templates select="*[contains(@class, ' ditaot-d/ditaval-startprop ')]" mode="out-of-line"/>

    <table>
      <xsl:apply-templates select="." mode="table:common"/>
      <xsl:apply-templates select="." mode="table:title"/>
      <!-- title and desc are processed elsewhere -->
      <xsl:apply-templates select="*[contains(@class, ' topic/tgroup ')]"/>
    </table>

    <xsl:apply-templates select="*[contains(@class, ' ditaot-d/ditaval-endprop ')]" mode="out-of-line"/>
  </xsl:template>

  <xsl:template match="*[contains(@class, ' topic/thead ')]" name="topic.thead">
    <thead>
      <xsl:apply-templates select="." mode="table:section"/>
    </thead>
  </xsl:template>

  <xsl:template match="*[contains(@class, ' topic/tbody ')]" name="topic.tbody">
    <tbody>
      <xsl:apply-templates select="." mode="table:section"/>
    </tbody>
  </xsl:template>

  <xsl:template match="*[contains(@class, ' topic/tgroup ')]/*" mode="table:section">
    <xsl:apply-templates select="../*[contains(@class, ' ditaot-d/ditaval-startprop ')]/@outputclass" mode="add-ditaval-style"/>
    <xsl:apply-templates select="." mode="table:common"/>
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="*[contains(@class, ' topic/row ')]" name="topic.row">
    <tr>
      <xsl:apply-templates select="." mode="table:common"/>
      <xsl:apply-templates/>
    </tr>
  </xsl:template>

  <xsl:template match="*[table:is-thead-entry(.)]">
    <th>
      <xsl:apply-templates select="." mode="table:entry"/>
    </th>
  </xsl:template>

  <xsl:template match="*[table:is-tbody-entry(.)][table:is-row-header(.)]">
    <th scope="row">
      <xsl:apply-templates select="." mode="table:entry"/>
    </th>
  </xsl:template>

  <xsl:template match="*[table:is-tbody-entry(.)][not(table:is-row-header(.))]" name="topic.entry">
    <td>
      <xsl:apply-templates select="." mode="table:entry"/>
    </td>
  </xsl:template>

  <xsl:template match="*[contains(@class, ' topic/entry ')]" mode="table:entry">
    <xsl:apply-templates select="." mode="table:common"/>
    <xsl:apply-templates select="." mode="headers"/>
    <xsl:apply-templates select="@morerows, @dita-ot:morecols"/>
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="@pgwide" mode="css-class">
    <xsl:sequence select="dita-ot:css-class(.)"/>
  </xsl:template>

  <xsl:template match="*[contains(@class, ' topic/table ')]" mode="css-class">
    <xsl:apply-templates select="@frame, @pgwide, @scale" mode="#current"/>
  </xsl:template>

  <xsl:template match="@align | @valign | @colsep | @rowsep" mode="css-class">
    <xsl:sequence select="dita-ot:css-class((), .)"/>
  </xsl:template>

  <xsl:template match="*[contains(@class, ' topic/tgroup ')]/*" mode="css-class">
    <xsl:apply-templates select="@valign" mode="#current"/>
  </xsl:template>

  <xsl:template match="*[contains(@class, ' topic/row ')]" mode="css-class">
    <xsl:apply-templates select="@rowsep, @valign" mode="#current"/>
  </xsl:template>

  <xsl:template match="*[contains(@class, ' topic/entry ')]" mode="css-class">
    <xsl:variable name="colsep" as="attribute(colsep)?" select="table:get-entry-colsep(.)"/>
    <xsl:variable name="rowsep" as="attribute(rowsep)?" select="table:get-entry-rowsep(.)"/>

    <xsl:apply-templates select="." mode="legacy-css-class">
      <xsl:with-param name="colsep" as="xs:integer"
        select="xs:integer(($colsep, $table.colsep-default)[1])"/>
      <xsl:with-param name="rowsep" as="xs:integer"
        select="xs:integer(($rowsep, $table.rowsep-default)[1])"/>
    </xsl:apply-templates>

    <xsl:apply-templates mode="#current" select="
      table:get-entry-align(.), $colsep, $rowsep, @valign
    "/>
  </xsl:template>

  <!-- deprecated since 2.3 -->
  <xsl:template match="*[contains(@class, ' topic/entry ')]" mode="legacy-css-class" as="xs:string*">
    <xsl:param name="colsep" as="xs:integer"/>
    <xsl:param name="rowsep" as="xs:integer"/>

    <xsl:if test="table:is-row-header(.)">firstcol</xsl:if>

    <xsl:choose>
      <xsl:when test="not($rowsep) and not($colsep)">nocellnorowborder</xsl:when>
      <xsl:when test="$rowsep = 1 and not($colsep)">row-nocellborder</xsl:when>
      <xsl:when test="not($rowsep) and $colsep = 1">cell-norowborder</xsl:when>
      <xsl:when test="$rowsep = 1 and $colsep = 1">cellrowborder</xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="*[table:is-thead-entry(.)]" mode="headers">
    <xsl:attribute name="id" select="dita-ot:generate-html-id(.)"/>
  </xsl:template>

  <xsl:template match="*[table:is-tbody-entry(.)]" mode="headers">
    <xsl:call-template name="add-headers-attribute"/>
  </xsl:template>

  <xsl:template match="@morerows">
    <xsl:attribute name="rowspan" select="xs:integer(.) + 1"/>
  </xsl:template>

  <xsl:template match="@dita-ot:morecols">
    <xsl:attribute name="colspan" select="xs:integer(.) + 1"/>
  </xsl:template>

  <xsl:template match="*[contains(@class, ' topic/table ')]" mode="table:title">
    <caption>
      <xsl:apply-templates select="*[contains(@class, ' topic/title ')]" mode="label"/>
      <xsl:apply-templates select="
        *[contains(@class, ' topic/title ')] | *[contains(@class, ' topic/desc ')]
      "/>
    </caption>
  </xsl:template>

  <xsl:template match="*[contains(@class, ' topic/table ')]/*[contains(@class, ' topic/title ')]" mode="label">
    <span class="table--title-label">
      <xsl:apply-templates select="." mode="title-number">
        <xsl:with-param name="number" as="xs:integer"
          select="count(key('enumerableByClass', 'topic/table')[. &lt;&lt; current()])"/>
      </xsl:apply-templates>
    </span>
  </xsl:template>

  <xsl:template match="*[contains(@class, ' topic/table ')]/*[contains(@class, ' topic/title ')]" mode="title-number">
    <xsl:param name="number" as="xs:integer"/>
    <xsl:sequence select="concat(dita-ot:get-variable(., 'Table'), ' ', $number, '. ')"/>
  </xsl:template>

  <xsl:template mode="title-number" priority="1" match="
    *[contains(@class, ' topic/table ')]
     [dita-ot:get-current-language(.) = ('hu', 'hu-hu')]
   /*[contains(@class, ' topic/title ')]
  ">
    <xsl:param name="number" as="xs:integer"/>
    <xsl:sequence select="concat($number, '. ', dita-ot:get-variable(., 'Table'), ' ')"/>
  </xsl:template>

  <xsl:template match="
    *[contains(@class, ' topic/table ')]/*[contains(@class, ' topic/title ')]
  | *[contains(@class, ' topic/table ')]/*[contains(@class, ' topic/desc ')]
  ">
    <span>
      <xsl:call-template name="setid"/>
      <xsl:call-template name="commonattributes"/>
      <xsl:apply-templates/>
    </span>
  </xsl:template>

</xsl:stylesheet>