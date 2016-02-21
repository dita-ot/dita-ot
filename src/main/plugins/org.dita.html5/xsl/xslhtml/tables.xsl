<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of the DITA Open Toolkit project.
     See the accompanying license.txt file for applicable licenses. -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:dita-ot="http://dita-ot.sourceforge.net/ns/201007/dita-ot"
                xmlns:table="http://dita-ot.sourceforge.net/ns/201007/dita-ot/table"
                version="2.0"
                exclude-result-prefixes="xs dita-ot table">

  <xsl:function name="table:is-tbody-entry" as="xs:boolean">
    <xsl:param name="el" as="element()"/>

    <xsl:sequence select="
      contains($el/@class, ' topic/entry ') and contains($el/../../@class, ' topic/tbody ')
    "/>
  </xsl:function>

  <xsl:function name="table:is-thead-entry" as="xs:boolean">
    <xsl:param name="el" as="element()"/>

    <xsl:sequence select="
      contains($el/@class, ' topic/entry ') and contains($el/../../@class, ' topic/thead ')
    "/>
  </xsl:function>

  <xsl:function name="table:get-current-table" as="element()">
    <xsl:param name="node" as="node()"/>

    <xsl:sequence select="
      $node/ancestor-or-self::*[contains(@class, ' topic/table ')][1]
    "/>
  </xsl:function>

  <xsl:function name="table:get-current-tgroup" as="element()">
    <xsl:param name="node" as="node()"/>

    <xsl:sequence select="
      $node/ancestor-or-self::*[contains(@class, ' topic/tgroup ')][1]
    "/>
  </xsl:function>

  <xsl:function name="table:is-row-header" as="xs:boolean">
    <xsl:param name="entry" as="element()"/>

    <xsl:sequence select="
      table:get-current-table($entry)/@rowheader eq 'firstcol'
      and xs:integer($entry/@dita-ot:x) eq 1
    "/>
  </xsl:function>

  <xsl:function name="table:get-entry-colspec" as="element()">
    <xsl:param name="entry" as="element()"/>

    <xsl:sequence select="
      table:get-current-table($entry)/*/*[contains(@class, ' topic/colspec ')]
      [@colname eq $entry/@colname]
    "/>
  </xsl:function>

  <xsl:template match="*" mode="table:common">
    <xsl:call-template name="commonattributes"/>
    <xsl:call-template name="setid"/>
  </xsl:template>

  <xsl:template match="*[contains(@class,' topic/table ')]" name="topic.table">
    <xsl:apply-templates select="*[contains(@class, ' ditaot-d/ditaval-startprop ')]" mode="out-of-line"/>

    <table>
      <xsl:apply-templates select="." mode="table:common"/>
      <xsl:apply-templates select="@frame, @pgwide, @scale" mode="data"/>
      <xsl:apply-templates select="." mode="table:title"/>
      <!-- title and desc are processed elsewhere -->
      <xsl:apply-templates select="*[contains(@class, ' topic/tgroup ')]"/>
    </table>

    <xsl:apply-templates select="*[contains(@class, ' ditaot-d/ditaval-endprop ')]" mode="out-of-line"/>
  </xsl:template>

  <xsl:template match="@*" mode="data">
    <xsl:attribute name="{concat('data-', local-name())}" select="."/>
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
    <xsl:apply-templates select="@valign" mode="data"/>
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="*[contains(@class, ' topic/row ')]" name="topic.row">
    <tr>
      <xsl:apply-templates select="." mode="table:common"/>
      <xsl:apply-templates select="@valign, @rowsep" mode="data"/>
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
    <xsl:apply-templates select="." mode="align"/>
    <xsl:apply-templates select="." mode="colsep"/>
    <xsl:apply-templates select="." mode="rowsep"/>
    <xsl:apply-templates select="." mode="headers"/>
    <xsl:apply-templates select="@morerows, @dita-ot:morecols"/>
    <xsl:apply-templates select="@valign" mode="data"/>
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="*[contains(@class, ' topic/entry ')]" mode="align">
    <xsl:attribute name="data-align" select="
      (@align,
       table:get-current-tgroup(.)/@align,
       table:get-entry-colspec(.)/@align,
       $table.align-default)[1]
    "/>
  </xsl:template>

  <xsl:template match="*[contains(@class, ' topic/entry ')]" mode="colsep">
    <xsl:attribute name="data-colsep" select="
      (@colsep,
       table:get-entry-colspec(.)/@colsep,
       table:get-current-table(.)/@colsep,
       table:get-current-tgroup(.)/@colsep,
       $table.colsep-default)[1]
    "/>
  </xsl:template>

  <xsl:template match="*[contains(@class, ' topic/entry ')]" mode="rowsep">
    <xsl:attribute name="data-rowsep" select="
      (@rowsep,
       table:get-entry-colspec(.)/@rowsep,
       table:get-current-table(.)/@rowsep,
       table:get-current-tgroup(.)/@rowsep,
       $table.rowsep-default)[1]
    "/>
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
      <xsl:apply-templates select="." mode="table:common"/>
      <xsl:apply-templates/>
    </span>
  </xsl:template>

</xsl:stylesheet>