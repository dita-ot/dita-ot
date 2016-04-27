<?xml version="1.0" encoding="UTF-8"?>
<!-- This file is part of the DITA Open Toolkit project.
     See the accompanying license.txt file for applicable licenses. -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:dita2html="http://dita-ot.sourceforge.net/ns/200801/dita2html"
                xmlns:dita-ot="http://dita-ot.sourceforge.net/ns/201007/dita-ot"
                xmlns:table="http://dita-ot.sourceforge.net/ns/201007/dita-ot/table"
                xmlns:simpletable="http://dita-ot.sourceforge.net/ns/201007/dita-ot/simpletable"
                version="2.0"
                exclude-result-prefixes="xs dita2html dita-ot table simpletable">

  <xsl:function name="simpletable:generate-headers" as="xs:string">
    <xsl:param name="el" as="element()"/>
    <xsl:param name="suffix" as="xs:string"/>
    <xsl:sequence select="string-join((generate-id($el), $suffix), '-')"/>
  </xsl:function>

  <xsl:template match="*[contains(@class, ' topic/simpletable ')]" name="topic.simpletable">
    <xsl:apply-templates select="*[contains(@class, ' ditaot-d/ditaval-startprop ')]" mode="out-of-line"/>

    <table>
      <xsl:apply-templates select="." mode="table:common"/>
      <xsl:call-template name="dita2html:simpletable-cols"/>

      <xsl:apply-templates select="*[contains(@class, ' topic/sthead ')]"/>
      <xsl:apply-templates select="." mode="generate-table-header"/>

      <tbody>
        <xsl:apply-templates select="*[contains(@class, ' topic/strow ')]"/>
      </tbody>
    </table>

    <xsl:apply-templates select="*[contains(@class, ' ditaot-d/ditaval-endprop ')]" mode="out-of-line"/>
  </xsl:template>

  <xsl:template match="*[contains(@class, ' topic/simpletable ')]" mode="css-class">
    <xsl:apply-templates select="@frame, @expanse, @scale" mode="#current"/>
  </xsl:template>

  <xsl:template match="*[contains(@class, ' topic/strow ')]">
    <tr>
      <xsl:apply-templates select="." mode="table:common"/>
      <xsl:apply-templates/>
    </tr>
  </xsl:template>

  <xsl:template match="*[contains(@class, ' topic/sthead ')]">
    <thead>
      <tr>
        <xsl:apply-templates select="." mode="table:common"/>
        <xsl:apply-templates/>
      </tr>
    </thead>
  </xsl:template>

  <xsl:template match="*[simpletable:is-head-entry(.)]">
    <th>
      <xsl:apply-templates select="." mode="simpletable:entry"/>
    </th>
  </xsl:template>

  <xsl:template match="*[simpletable:is-body-entry(.)][simpletable:is-keycol-entry(.)]">
    <th scope="row">
      <xsl:apply-templates select="." mode="simpletable:entry"/>
    </th>
  </xsl:template>

  <xsl:template match="*[simpletable:is-body-entry(.)][not(simpletable:is-keycol-entry(.))]">
    <td>
      <xsl:apply-templates select="." mode="simpletable:entry"/>
    </td>
  </xsl:template>

  <xsl:template match="*[contains(@class, ' topic/stentry ')]" mode="simpletable:entry">
    <xsl:apply-templates select="." mode="table:common"/>
    <xsl:apply-templates select="." mode="headers"/>
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="*[simpletable:is-head-entry(.)]" mode="headers">
    <xsl:attribute name="id" select="dita-ot:generate-html-id(.)"/>
  </xsl:template>

  <xsl:template match="*[simpletable:is-body-entry(.)]" mode="headers">
    <xsl:call-template name="set.stentry.headers"/>
  </xsl:template>

</xsl:stylesheet>
