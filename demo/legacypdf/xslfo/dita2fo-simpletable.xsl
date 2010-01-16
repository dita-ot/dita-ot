<?xml version='1.0'?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file for 
     applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                version='1.0'>
 

<!-- simpletable support -->

<xsl:template match="*[contains(@class,' topic/simpletable ')]">
<fo:block space-before="12pt">
  <fo:table xsl:use-attribute-sets="table.data frameall">
    <xsl:call-template name="semtbl-colwidth"/>
    <fo:table-body>
      <xsl:call-template name="gen-dflt-data-hdr"/>
      <xsl:apply-templates/>
    </fo:table-body>
  </fo:table>
</fo:block>
</xsl:template>

<!-- if cells have default spectitle values, copy these up to a first new hdr row -->
<xsl:template name="gen-dflt-data-hdr">
  <xsl:if test="*/*[contains(@class,' topic/stentry ')]/@specentry">
    <fo:table-row>
       <xsl:for-each select="*[contains(@class,' topic/strow ')][1]/*">
          <fo:table-cell start-indent="2pt" background-color="silver" padding="2pt" text-align=
            "center" font-weight="bold" xsl:use-attribute-sets="frameall">
            <xsl:attribute name="column-number"><xsl:number count="*"/></xsl:attribute>
            <fo:block>
            <xsl:value-of select="@specentry"/>
            </fo:block>
          </fo:table-cell>
        </xsl:for-each>
    </fo:table-row>
  </xsl:if>
</xsl:template>



<xsl:template name="semtbl-colwidth">
  <!-- Find the total number of relative units for the table. If @relcolwidth="1* 2* 2*",
       the variable is set to 5. -->
  <xsl:variable name="totalwidth">
    <xsl:if test="@relcolwidth and not(@relcolwidth='')">
      <xsl:call-template name="find-total-table-width"/>
    </xsl:if>
  </xsl:variable>
  <!-- Find how much of the table each relative unit represents. If @relcolwidth is 1* 2* 2*,
       there are 5 units. So, each unit takes up 100/5, or 20% of the table. Default to 0,
       which the entries will ignore. -->
  <!-- bad relcolwidth data will still generate "NaN" errors, however -->
  <xsl:variable name="width-multiplier">
    <xsl:choose>
      <xsl:when test="@relcolwidth and not(@relcolwidth='')">
        <xsl:value-of select="100 div $totalwidth"/>
      </xsl:when>
      <xsl:otherwise>0</xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <!-- specialize strow, so choicetable will work -->
  <xsl:for-each select="*[contains(@class,' topic/strow ')][1]/*"><!-- use cells as counters; avoid recursion! -->
    <!-- Determine which column this entry is in. -->
    <xsl:variable name="thiscolnum"><xsl:value-of select="position()"/></xsl:variable>
<!--xsl:comment>Relwidth should be (<xsl:call-template name="get-proportional-width">
            <xsl:with-param name="entry-num"><xsl:value-of select="$thiscolnum"/></xsl:with-param>
            <xsl:with-param name="all-widths"><xsl:value-of select="../../@relcolwidth"/></xsl:with-param>
          </xsl:call-template>)</xsl:comment-->    
    <fo:table-column>
      <xsl:if test="../../@relcolwidth and not(../../@relcolwidth='')">
        <!-- use relative width from called template, instead of old value with inches -->
        <!--<xsl:attribute name="column-width">
          <xsl:variable name="thispct">
            <xsl:call-template name="get-current-entry-percentage">
              <xsl:with-param name="multiplier"><xsl:value-of select="$width-multiplier"/></xsl:with-param>
              <xsl:with-param name="entry-num"><xsl:value-of select="$thiscolnum"/></xsl:with-param>
            </xsl:call-template>
          </xsl:variable> 
          <xsl:value-of select="number(198 * $thispct div 100 div 36)"/>in<xsl:text/>
        </xsl:attribute>-->
        <xsl:attribute name="column-width">proportional-column-width(<xsl:call-template name="get-proportional-width">
            <xsl:with-param name="entry-num"><xsl:value-of select="$thiscolnum"/></xsl:with-param>
            <xsl:with-param name="all-widths"><xsl:value-of select="../../@relcolwidth"/></xsl:with-param>
          </xsl:call-template>)</xsl:attribute>
      </xsl:if>
    </fo:table-column>
  </xsl:for-each>
</xsl:template>

<!-- used to get relative column width for simpletables. If in the first entry, take
          the first width. Otherwise, chop off a width, and proceed to the next value, until
          getting to the proper one. -->
<xsl:template name="get-proportional-width">
  <xsl:param name="entry-num">0</xsl:param>
  <xsl:param name="all-widths"><xsl:value-of select="../@relcolwidth"/></xsl:param>
  <xsl:choose>
    <xsl:when test="$entry-num &lt;= 0"/>
    <xsl:when test="$entry-num=1">
      <xsl:value-of select="substring-before($all-widths,'*')"/>
    </xsl:when> 
    <xsl:when test="not(contains($all-widths,' '))"/>
    <xsl:otherwise>
      <xsl:call-template name="get-proportional-width">
        <xsl:with-param name="entry-num"><xsl:value-of select="$entry-num - 1"/></xsl:with-param>
        <xsl:with-param name="all-widths"><xsl:value-of select="substring-after($all-widths,' ')"/></xsl:with-param>
      </xsl:call-template>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>






<!-- Use @relcolwidth to find the total width of the table. That is, if the attribute is set
     to 1* 2* 2* 1*, then the table is 6 units wide. -->
<xsl:template name="find-total-table-width">
  <!-- Start with relcolwidth, and each recursive call will remove the first value -->
  <xsl:param name="relcolwidth"><xsl:value-of select="@relcolwidth"/></xsl:param>
  <!-- Determine the first value, which is the value before the first asterisk -->
  <xsl:variable name="firstval">
    <xsl:if test="contains($relcolwidth,'*')">
      <xsl:value-of select="substring-before($relcolwidth,'*')"/>
    </xsl:if>
  </xsl:variable>
  <!-- Begin processing if we were able to find a first value -->
  <xsl:if test="string-length($firstval)>0">
    <!-- Chop off the first value, and set morevals to the remainder -->
    <xsl:variable name="morevals"><xsl:value-of select="substring-after($relcolwidth,' ')"/></xsl:variable>
    <xsl:choose>
      <!-- If there are additional values, call this template on the remainder.
           Add the result of that call to the first value. -->
      <xsl:when test="string-length($morevals)>0">
        <xsl:variable name="nextval">   <!-- The total of the remaining values -->
          <xsl:call-template name="find-total-table-width">
            <xsl:with-param name="relcolwidth"><xsl:value-of select="$morevals"/></xsl:with-param>
          </xsl:call-template>
        </xsl:variable>
        <xsl:value-of select="number($firstval)+number($nextval)"/>
      </xsl:when>
      <!-- If there are no more values, return the first (and only) value -->
      <xsl:otherwise><xsl:value-of select="$firstval"/></xsl:otherwise>
    </xsl:choose>
  </xsl:if>
</xsl:template>

<!-- Find the width of the current cell. Multiplier is how much each unit of width is multiplied to total 100.
     Entry-num is the current entry. Current-col is what column we are at when scanning @relcolwidth.
     Relcolvalues is the unscanned part of @relcolwidth. -->
<xsl:template name="get-current-entry-percentage">
  <xsl:param name="multiplier">1</xsl:param>  <!-- Each relative unit is worth this many percentage points -->
  <xsl:param name="entry-num"/>               <!-- The entry number of the cell we are evaluating now -->
  <xsl:param name="current-col">1</xsl:param> <!-- Position within the recursive call to evaluate @relcolwidth -->
  <!-- relcolvalues begins with @relcolwidth. Each call to the template removes the first value. -->
  <xsl:param name="relcolvalues"><xsl:value-of select="parent::*/parent::*/@relcolwidth"/></xsl:param>

  <xsl:choose>
    <!-- If the recursion has moved up to the proper cell, multiply $multiplier by the number of
         relative units for this column. -->
    <xsl:when test="$entry-num = $current-col">
      <xsl:variable name="relcol"><xsl:value-of select="substring-before($relcolvalues,'*')"/></xsl:variable>
      <xsl:value-of select="$relcol * $multiplier"/>
    </xsl:when>
    <!-- Otherwise, call this template again, removing the first value form @relcolwidth. Also add one
         to $current-col. -->
    <xsl:otherwise>
      <xsl:call-template name="get-current-entry-percentage">
        <xsl:with-param name="multiplier"><xsl:value-of select="$multiplier"/></xsl:with-param>
        <xsl:with-param name="entry-num"><xsl:value-of select="$entry-num"/></xsl:with-param>
        <xsl:with-param name="current-col"><xsl:value-of select="$current-col + 1"/></xsl:with-param>
        <xsl:with-param name="relcolvalues"><xsl:value-of select="substring-after($relcolvalues,' ')"/></xsl:with-param>
      </xsl:call-template>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>


<xsl:template match="*[contains(@class,' topic/sthead ')]">
  <fo:table-row>
    <xsl:apply-templates/>
  </fo:table-row>
</xsl:template>


<xsl:template match="*[contains(@class,' topic/strow ')]">
  <fo:table-row>
    <xsl:apply-templates/>
  </fo:table-row>
</xsl:template>


<!-- in the following two rules, the xsl:number needs to give the
  sequence number of either the current semdh or sementry in the current
  semrow OR semhdr. -->

<xsl:template match="*[contains(@class,' topic/sthead ')]/*[contains(@class,' topic/stentry ')]" priority="2">
  <fo:table-cell start-indent="2pt" background-color="silver" padding="2pt" text-align=
"center" font-weight="bold" xsl:use-attribute-sets="frameall">
    <xsl:attribute name="column-number"><xsl:number count="*"/></xsl:attribute>
    <fo:block>
  <xsl:attribute name="font-size">10pt</xsl:attribute>
    <xsl:call-template name="get-title"/>
    </fo:block>
  </fo:table-cell>
</xsl:template>


<xsl:template match="*[contains(@class,' topic/stentry ')]">
  <xsl:variable name="localkeycol">
    <xsl:choose>
      <xsl:when test="ancestor::*[contains(@class,' topic/simpletable ')]/@keycol">
        <xsl:value-of select="ancestor::*[contains(@class,' topic/simpletable ')]/@keycol"/>
      </xsl:when>
      <xsl:otherwise>0</xsl:otherwise>
    </xsl:choose>
  </xsl:variable>
  <xsl:variable name="thisnum"><xsl:number/></xsl:variable>
  <xsl:variable name="thisrow"><xsl:number level="single" count="strow"/></xsl:variable>
  <!--xsl:message>Keycol: <xsl:value-of select="$localkeycol"/>  Number: <xsl:value-of select="$thisnum"/>  Row: <xsl:value-of select="$thisrow"/></xsl:message-->
  <xsl:choose>
    <xsl:when test="$thisnum=$localkeycol">
      <fo:table-cell start-indent="2pt" background-color="#fafafa" padding="2pt" xsl:use-attribute-sets="frameall">
        <xsl:attribute name="column-number"><xsl:number count="*"/></xsl:attribute>
        <fo:block font-weight="bold">
  <xsl:attribute name="font-size">9pt</xsl:attribute>
          <xsl:apply-templates/> <!-- don't use "apply-for-phrases" for editor -->
        </fo:block>
      </fo:table-cell>
    </xsl:when>
    <xsl:otherwise>
      <fo:table-cell start-indent="2pt" background-color="#fafafa" padding="2pt" xsl:use-attribute-sets="frameall">
        <xsl:attribute name="column-number"><xsl:number count="*"/></xsl:attribute>
        <fo:block>
  <xsl:attribute name="font-size">9pt</xsl:attribute>
          <xsl:apply-templates/>
        </fo:block>
      </fo:table-cell>
    </xsl:otherwise>
  </xsl:choose>
</xsl:template>


<!-- end of simpletable section -->


</xsl:stylesheet>
