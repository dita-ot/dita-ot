<?xml version='1.0'?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file for 
     applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<!DOCTYPE xsl:transform [
<!-- entities for use in the generated output (must produce correctly in FO) -->
  <!ENTITY rbl           "&#160;">
]>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:fo="http://www.w3.org/1999/XSL/Format" version="1.0">
  <!-- CALS (OASIS) TABLE -->
  <!-- adapted from sample code in the XSL FO spec; extended considerably! -->
  <!-- table formatting properties -->
  <xsl:attribute-set name="table.data">
    <!-- use for overall table default characteristics -->
    <xsl:attribute name="table-layout">fixed</xsl:attribute>
    <xsl:attribute name="width">100%</xsl:attribute>
    <!--xsl:attribute name="inline-progression-dimension">auto</xsl:attribute-->
    <xsl:attribute name="space-before">10pt</xsl:attribute>
    <xsl:attribute name="space-after">10pt</xsl:attribute>
    <xsl:attribute name="background-color">white</xsl:attribute>
    <!--xsl:attribute name="start-indent">inherit</xsl:attribute-->
  </xsl:attribute-set>
  <xsl:attribute-set name="table.data.caption">
    <xsl:attribute name="start-indent">inherit</xsl:attribute>
    <xsl:attribute name="font-weight">bold</xsl:attribute>
  </xsl:attribute-set>
  <xsl:attribute-set name="table.data.tbody">
    <xsl:attribute name="background-color">white</xsl:attribute>
  </xsl:attribute-set>
  <xsl:attribute-set name="table.data.th">
    <xsl:attribute name="color">black</xsl:attribute>
    <!--xsl:attribute name="background-color">silver</xsl:attribute-->
    <xsl:attribute name="padding">2pt</xsl:attribute>
    <xsl:attribute name="text-align">center</xsl:attribute>
    <xsl:attribute name="font-weight">bold</xsl:attribute>
  </xsl:attribute-set>
  <xsl:attribute-set name="table.data.tf">
    <xsl:attribute name="color">blue</xsl:attribute>
    <xsl:attribute name="padding">2pt</xsl:attribute>
  </xsl:attribute-set>
  <xsl:attribute-set name="table.data.td">
    <!--xsl:attribute name="background-color">grey</xsl:attribute-->
    <xsl:attribute name="padding">2pt</xsl:attribute>
  </xsl:attribute-set>
  <xsl:template
    match="*[contains(@class,' topic/table ')]/*[contains(@class,' topic/xtitle ')]" priority="2"/>
  <xsl:template match="*[contains(@class,' topic/table ')]">
    <fo:block>
      <xsl:if test="@id">
        <xsl:apply-templates select="@id"/>
      </xsl:if>
      <xsl:apply-templates/>
    </fo:block>
  </xsl:template>
  <xsl:template name="default-colwidth">
    <!--fo:table-column column-width="150pt"/-->
    <xsl:for-each select="row[1]/entry">
      <fo:table-column>
        <!-- compute even columns (creates valid processing, but all columns are even width) -->
        <xsl:attribute name="column-width">
          <xsl:value-of select="floor(@width div 72)"/>in</xsl:attribute>
      </fo:table-column>
    </xsl:for-each>
  </xsl:template>
  <xsl:template match="*[contains(@class,' topic/spanspec ')]">
    <!--  <xsl:call-template name="att-align"/> -->
  </xsl:template>
  <xsl:template match="*[contains(@class,' topic/colspec ')]">
    <fo:table-column>
      <xsl:call-template name="att-align"/>
      <xsl:attribute name="column-number">
        <xsl:number count="colspec"/>
      </xsl:attribute>
      <xsl:attribute name="column-width">
        <xsl:call-template name="xcalc.column.width">
          <xsl:with-param name="colwidth">
            <xsl:value-of select="@colwidth"/>
          </xsl:with-param>
        </xsl:call-template>
      </xsl:attribute>
    </fo:table-column>
  </xsl:template>
  <xsl:template match="*[contains(@class,' topic/tgroup ')]">
    <!--
  <xsl:call-template name="att-align"/>
-->
    <xsl:choose>
      <xsl:when test="xtitle">
        <!-- renamed for now to NOT trigger on title; testing the "otherwise" code -->
        <fo:table-and-caption xsl:use-attribute-sets="table.data frameall">
          <fo:table-caption>
            <fo:block xsl:use-attribute-sets="table.data.caption">
              <xsl:value-of select="title"/>
            </fo:block>
          </fo:table-caption>
          <fo:table xsl:use-attribute-sets="table.data frameall" space-before="12pt">
            <xsl:if test="string(@id)">
              <xsl:attribute name="id">
                <xsl:value-of select="@id"/>
              </xsl:attribute>
            </xsl:if>
            <xsl:apply-templates/>
          </fo:table>
        </fo:table-and-caption>
      </xsl:when>
      <xsl:otherwise>
        <fo:table xsl:use-attribute-sets="table.data frameall" space-before="12pt">
          <!--xsl:if test="string(@id)"><xsl:attribute name="id"><xsl:value-of select="@id"/></xsl:attribute></xsl:if-->
          <xsl:call-template name="default-colwidth"/>
          <xsl:apply-templates/>
        </fo:table>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template match="*[contains(@class,' topic/thead ')]">
    <fo:table-header>
      <xsl:call-template name="att-valign"/>
      <xsl:apply-templates/>
    </fo:table-header>
  </xsl:template>
  <xsl:template match="*[contains(@class,' topic/tfoot ')]">
    <fo:table-footer>
      <xsl:call-template name="att-valign"/>
      <xsl:apply-templates/>
    </fo:table-footer>
  </xsl:template>
  <xsl:template match="*[contains(@class,' topic/tbody ')]">
    <fo:table-body>
      <xsl:call-template name="att-valign"/>
      <xsl:apply-templates/>
    </fo:table-body>
  </xsl:template>
  <xsl:template match="*[contains(@class,' topic/tnote ')]">
    <fo:table-row>
      <fo:table-cell start-indent="2pt" background-color="#E0E0F0" xsl:use-attribute-sets="table.data.td">
        <fo:block>
          <fo:inline font-weight="bold">
            <xsl:call-template name="getString">
              <xsl:with-param name="stringName" select="'Note'"/>
            </xsl:call-template>
            <xsl:text>: </xsl:text>
          </fo:inline>
          <xsl:apply-templates/>
        </fo:block>
      </fo:table-cell>
    </fo:table-row>
  </xsl:template>
  <xsl:template match="*[contains(@class,' topic/row ')]">
    <fo:table-row>
      <xsl:call-template name="att-valign"/>
      <xsl:apply-templates/>
    </fo:table-row>
  </xsl:template>
  <xsl:template match="*[contains(@class,' topic/thead ')]/*[contains(@class,' topic/row ')]/*[contains(@class,' topic/entry ')]">
    <xsl:variable name="colnumval">
      <xsl:choose>
        <xsl:when test="ancestor::tgroup/colspec[@colname=current()/@colname]/@colnum">
          <xsl:value-of select="ancestor::tgroup/colspec[@colname=current()/@colname]/@colnum"/>
        </xsl:when>
        <xsl:when test="@colnum">
          <xsl:value-of select="@colnum"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:number count="entry"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <fo:table-cell column-number="{$colnumval}" start-indent="2pt"
      background-color="silver" padding="2pt" text-align="center"
      font-weight="bold" xsl:use-attribute-sets="frameall">
      <!-- xsl:use-attribute-sets="table.data.th"-->
      <xsl:call-template name="entryatts"/>
      <fo:block>
        <xsl:call-template name="fillit"/>
      </fo:block>
    </fo:table-cell>
  </xsl:template>
  <xsl:template match="*[contains(@class,' topic/tfoot ')]/*[contains(@class,' topic/row ')]/*[contains(@class,' topic/entry ')]">
    <xsl:variable name="colnumval">
      <xsl:choose>
        <xsl:when test="ancestor::tgroup/colspec[@colname=current()/@colname]/@colnum">
          <xsl:value-of select="ancestor::tgroup/colspec[@colname=current()/@colname]/@colnum"/>
        </xsl:when>
        <xsl:when test="@colnum">
          <xsl:value-of select="@colnum"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:number count="entry"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <fo:table-cell start-indent="2pt" column-number="{$colnumval}" xsl:use-attribute-sets="table.data.tf frameall">
      <xsl:call-template name="entryatts"/>
      <fo:block>
        <xsl:call-template name="fillit"/>
      </fo:block>
    </fo:table-cell>
  </xsl:template>
  <xsl:template name="get-colnumval">
    <xsl:variable name="colnumval">
      <xsl:choose>
        <xsl:when test="ancestor::tgroup/colspec[@colname=current()/@colname]/@colnum">
          <xsl:value-of select="ancestor::tgroup/colspec[@colname=current()/@colname]/@colnum"/>
        </xsl:when>
        <xsl:when test="@colnum">
          <xsl:value-of select="@colnum"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:number count="entry"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
  </xsl:template>
  <xsl:template match="*[contains(@class,' topic/tbody ')]/*[contains(@class,' topic/row ')]/*[contains(@class,' topic/entry ')]">
    <xsl:variable name="colnumval">
      <xsl:choose>
        <xsl:when test="ancestor::tgroup/colspec[@colname=current()/@colname]/@colnum">
          <xsl:value-of select="ancestor::tgroup/colspec[@colname=current()/@colname]/@colnum"/>
        </xsl:when>
        <xsl:when test="@colnum">
          <xsl:value-of select="@colnum"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:number count="entry"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <fo:table-cell column-number="{$colnumval}" start-indent="2pt"
      background-color="#faf4fa" padding="2pt" xsl:use-attribute-sets="frameall">
      <xsl:call-template name="entryatts"/>
      <fo:block>
        <xsl:call-template name="fillit"/>
      </fo:block>
    </fo:table-cell>
  </xsl:template>
  <!-- named templates -->
  <xsl:template name="fillit">
    <xsl:choose>
      <!-- test to see if the cell contains any sensible text or other element... -->
      <xsl:when test="not(text()[normalize-space(.)] | *)">
        <fo:inline>&rbl;</fo:inline>
      </xsl:when>
      <xsl:otherwise>
        <xsl:apply-templates/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <!-- table "macros" -->
  <xsl:template name="entryatts">
    <xsl:call-template name="att-valign"/>
    <xsl:call-template name="att-align"/>
    <!-- SF 1553905: remove invalid cellpadding attribute -->
    <!--
    <xsl:if test="string(@colsep)">
      <xsl:if test="@colsep='1'">
        <xsl:attribute name="cellpadding">10</xsl:attribute>
      </xsl:if>
    </xsl:if>
    -->
    <!-- IPL start -->
    <xsl:if test="string(@namest)">
      <xsl:variable name="colst" select="substring-after(@namest,'col')"/>
      <xsl:variable name="colend" select="substring-after(@nameend,'col')"/>
      <xsl:attribute name="number-columns-spanned">
        <xsl:value-of select="$colend - $colst + 1"/>
      </xsl:attribute>
    </xsl:if>
    <!-- IPL end -->
	<xsl:if test="@morerows">
	  <xsl:attribute name="number-rows-spanned">
		<xsl:value-of select="@morerows+1"/>
	  </xsl:attribute>
	</xsl:if>
  </xsl:template>
  <xsl:template name="att-valign">
    <xsl:if test="string(@valign)">
      <xsl:choose>
        <xsl:when test="@valign='middle'">
          <xsl:attribute name="display-align">center</xsl:attribute>
        </xsl:when>
        <xsl:when test="@valign='top'">
          <xsl:attribute name="display-align">before</xsl:attribute>
        </xsl:when>
        <xsl:when test="@valign='bottom'">
          <xsl:attribute name="display-align">after</xsl:attribute>
        </xsl:when>
        <xsl:otherwise>
          <xsl:attribute name="display-align">
            <xsl:value-of select="@valign"/>
          </xsl:attribute>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
  </xsl:template>
  <xsl:template name="att-align">
    <xsl:if test="string(@align)">
      <xsl:attribute name="text-align">
        <xsl:value-of select="normalize-space(@align)"/>
      </xsl:attribute>
    </xsl:if>
  </xsl:template>
  <!-- table support based on examples in the XSL spec -->
  <!-- see original support comments in the XSL spec, source of this fragment -->
  <xsl:template name="calc.column.width">
    <xsl:param name="colwidth">1*</xsl:param>
    <!-- Ok, the colwidth could have any one of the following forms: -->
    <!--        1*       = proportional width -->
    <!--     1unit       = 1.0 units wide -->
    <!--         1       = 1pt wide -->
    <!--  1*+1unit       = proportional width + some fixed width -->
    <!--      1*+1       = proportional width + some fixed width -->
    <!-- If it has a proportional width, translate it to XSL -->
    <xsl:if test="contains($colwidth, '*')">
      <!-- modified to handle "*" as input -->
      <xsl:variable name="colfactor">
        <xsl:value-of select="substring-before($colwidth, '*')"/>
      </xsl:variable>
      <xsl:text>proportional-column-width(</xsl:text>
      <xsl:choose>
        <xsl:when test="not($colfactor = '')">
          <xsl:value-of select="$colfactor"/>
        </xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
      <xsl:text>)</xsl:text>
    </xsl:if>
    <!-- Now get the non-proportional part of the specification -->
    <xsl:variable name="width-units">
      <xsl:choose>
        <xsl:when test="contains($colwidth, '*')">
          <xsl:value-of select="normalize-space(substring-after($colwidth, '*'))"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="normalize-space($colwidth)"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <!-- Now the width-units could have any one of the following forms: -->
    <!--                 = <empty string> -->
    <!--     1unit       = 1.0 units wide -->
    <!--         1       = 1pt wide -->
    <!-- with an optional leading sign -->
    <!-- Get the width part by blanking out the units part and discarding -->
    <!-- white space. -->
    <xsl:variable name="width" select="normalize-space(translate($width-units,                           '+-0123456789.abcdefghijklmnopqrstuvwxyz',                           '+-0123456789.'))"/>
    <!-- Get the units part by blanking out the width part and discarding -->
    <!-- white space. -->
    <xsl:variable name="units" select="normalize-space(translate($width-units,                           'abcdefghijklmnopqrstuvwxyz+-0123456789.',                           'abcdefghijklmnopqrstuvwxyz'))"/>
    <!-- Output the width -->
    <xsl:value-of select="$width"/>
    <!-- Output the units, translated appropriately -->
    <xsl:choose>
      <xsl:when test="$units = 'pi'">pc</xsl:when>
      <xsl:when test="$units = '' and $width != ''">pt</xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$units"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <xsl:template name="xcalc.column.width">
    <!-- see original support comments in the XSL spec, source of this fragment -->
    <xsl:param name="colwidth">1*</xsl:param>
    <!-- Ok, the colwidth could have any one of the following forms: -->
    <!--        1*       = proportional width -->
    <!--     1unit       = 1.0 units wide -->
    <!--         1       = 1pt wide -->
    <!--  1*+1unit       = proportional width + some fixed width -->
    <!--      1*+1       = proportional width + some fixed width -->
    <!-- If it has a proportional width, translate it to XSL -->
    <xsl:if test="contains($colwidth, '*')">
      <xsl:variable name="colfactor">
        <xsl:value-of select="substring-before($colwidth, '*')"/>
      </xsl:variable>
      <xsl:text>proportional-column-width(</xsl:text>
      <xsl:choose>
        <xsl:when test="not($colfactor = '')">
          <xsl:value-of select="$colfactor"/>
        </xsl:when>
        <xsl:otherwise>1</xsl:otherwise>
      </xsl:choose>
      <xsl:text>)</xsl:text>
    </xsl:if>
    <!-- Now get the non-proportional part of the specification -->
    <xsl:variable name="width-units">
      <xsl:choose>
        <xsl:when test="contains($colwidth, '*')">
          <xsl:value-of select="normalize-space(substring-after($colwidth, '*'))"/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="normalize-space($colwidth)"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <!-- Now the width-units could have any one of the following forms: -->
    <!--                 = <empty string> -->
    <!--     1unit       = 1.0 units wide -->
    <!--         1       = 1pt wide -->
    <!-- with an optional leading sign -->
    <!-- Get the width part by blanking out the units part and discarding -->
    <!-- whitespace. -->
    <xsl:variable name="width" select="normalize-space(translate($width-units,                                          '+-0123456789.abcdefghijklmnopqrstuvwxyz',                                          '+-0123456789.'))"/>
    <!-- Get the units part by blanking out the width part and discarding -->
    <!-- whitespace. -->
    <xsl:variable name="units" select="normalize-space(translate($width-units,                                          'abcdefghijklmnopqrstuvwxyz+-0123456789.',                                          'abcdefghijklmnopqrstuvwxyz'))"/>
    <!-- Output the width -->
    <xsl:value-of select="$width"/>
    <!-- Output the units, translated appropriately -->
    <xsl:choose>
      <xsl:when test="$units = 'pi'">pc</xsl:when>
      <xsl:when test="$units = '' and $width != ''">pt</xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$units"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <!-- end of table section -->
</xsl:stylesheet>
