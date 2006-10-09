<?xml version='1.0'?>
<!-- This file is part of the DITA Open Toolkit project hosted on 
     Sourceforge.net. See the accompanying license.txt file for 
     applicable licenses.-->
<!-- (c) Copyright IBM Corp. 2004, 2005 All Rights Reserved. -->

<!DOCTYPE xsl:transform [
<!-- entities for use in the generated output (must produce correctly in FO) -->
  <!ENTITY rbl           "&#160;">
  <!ENTITY bullet        "&#x2022;"><!--check these two for better assignments -->
]>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                version='1.0'>
 

<!-- ============ Lists (ul, ol, sl, dl) ============ -->
<!-- all dingbat lists start the same way (sl has a null dingbat, in effect) -->
 
<xsl:template match="*[contains(@class,' topic/ul ')]|*[contains(@class,' topic/ol ')]|*[contains(@class,' topic/sl ')]">
<xsl:variable name="list-type" select="name()" /> 
<xsl:variable name="list-level" select="count(ancestor-or-self::*[name()=$list-type])" /> 
<!--for FOP, delete the provisional attributes below -->
  <fo:list-block><!-- provisional-distance-between-starts="2pc"
                 provisional-label-separation="2pc"-->
    <xsl:apply-templates/>
  </fo:list-block>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/ul ')]/*[contains(@class,' topic/li ')]">
  <xsl:variable name="list-level" 
    select="count(ancestor-or-self::*[contains(@class,' topic/ul ')] | 
                  ancestor-or-self::*[contains(@class,' topic/dl ')] |
                  ancestor-or-self::*[contains(@class,' topic/sl ')] |
                  ancestor-or-self::*[contains(@class,' topic/ol ')] )" />
  <xsl:variable name="extra-list-indent"><xsl:value-of select="number($list-level)*16"/>pt</xsl:variable>
  <fo:list-item>
    <fo:list-item-label end-indent="label-end()" text-align="end"> 
      <fo:block>
        <fo:inline>&#x2022;</fo:inline>
      </fo:block>
    </fo:list-item-label>
    <fo:list-item-body start-indent="body-start()"> 
      <!--xsl:attribute name="start-indent"><xsl:value-of select="$basic-start-indent"/> + <xsl:value-of select="$extra-list-indent"/></xsl:attribute-->
      <fo:block> 
        <xsl:apply-templates /> 
      </fo:block> 
    </fo:list-item-body> 
  </fo:list-item>
</xsl:template>


<xsl:template match="*[contains(@class,' topic/ol ')]/*[contains(@class,' topic/li ')]">
  <xsl:variable name="list-level" 
    select="count(ancestor-or-self::*[contains(@class,' topic/ul ')] | 
                  ancestor-or-self::*[contains(@class,' topic/dl ')] |
                  ancestor-or-self::*[contains(@class,' topic/sl ')] |
                  ancestor-or-self::*[contains(@class,' topic/ol ')] )" />
  <xsl:variable name="extra-list-indent"><xsl:value-of select="number($list-level)*16"/>pt</xsl:variable>
  <fo:list-item>
    <fo:list-item-label end-indent="label-end()" text-align="end">
      <fo:block><!-- linefeed-treatment="ignore"-->
        <xsl:choose> 
          <xsl:when test="($list-level mod 2) = 1"> 
            <!--          arabic         --> 
            <!--xsl:number format="1." /--> 
            <xsl:value-of select="position()"/>. 
          </xsl:when> 
          <xsl:otherwise> 
            <!--              alphabetic             --> 
            <xsl:number format="a." /> 
          </xsl:otherwise> 
        </xsl:choose> 
      </fo:block>
    </fo:list-item-label>
    <fo:list-item-body start-indent="body-start()"> 
      <!--xsl:attribute name="start-indent"><xsl:value-of select="$basic-start-indent"/> + <xsl:value-of select="$extra-list-indent"/></xsl:attribute-->
      <fo:block> 
         <xsl:apply-templates /> 
      </fo:block> 
    </fo:list-item-body> 
  </fo:list-item>
</xsl:template>
 
<xsl:template match="*[contains(@class,' topic/sl ')]/*[contains(@class,' topic/sli ')]">
  <xsl:variable name="list-level" 
    select="count(ancestor-or-self::*[contains(@class,' topic/ul ')] | 
                  ancestor-or-self::*[contains(@class,' topic/dl ')] |
                  ancestor-or-self::*[contains(@class,' topic/sl ')] |
                  ancestor-or-self::*[contains(@class,' topic/ol ')] )" />
  <xsl:variable name="extra-list-indent"><xsl:value-of select="number($list-level)*16"/>pt</xsl:variable>
  <fo:list-item>
    <fo:list-item-label end-indent="label-end()" text-align="end">
      <fo:block linefeed-treatment="ignore">
        <fo:inline><xsl:text> </xsl:text></fo:inline>
      </fo:block>
    </fo:list-item-label>
    <fo:list-item-body start-indent="body-start()"> 
      <!--xsl:attribute name="start-indent"><xsl:value-of select="$basic-start-indent"/> + <xsl:value-of select="$extra-list-indent"/></xsl:attribute-->
      <fo:block> 
         <xsl:apply-templates /> 
      </fo:block> 
    </fo:list-item-body> 
  </fo:list-item>
</xsl:template>


<xsl:template name="li.name">
  <xsl:if test="string(@id)"><fo:inline id="{@id}"> </fo:inline></xsl:if>
</xsl:template>

<xsl:template match="*[contains(@class,' topic/itemgroup ')]">
  <fo:block xsl:use-attribute-sets="p">
    <!-- setclass -->
    <xsl:apply-templates select="@compact"/>
    <!-- set id -->
    <xsl:apply-templates />
  </fo:block>
</xsl:template>


<!-- ========= definition list group ========== -->
<!-- 
wish list: 
    multiple styles (table based, list based with term-based indent, term overlap, etc)
-->

<xsl:template match="*[contains(@class,' topic/dl ')]">
  <fo:block>
    <xsl:apply-templates/>
  </fo:block>
</xsl:template>


<xsl:template match="*[contains(@class, ' topic/dlhead ')]">
  <fo:block font-weight="bold" text-decoration="underline">
    <xsl:apply-templates />
  </fo:block>
</xsl:template>


<xsl:template match="*[contains(@class,' topic/dthd ')]">          
  <fo:block xsl:use-attribute-sets="dt" font-weight="bold">
    <!-- setclass -->
    <!-- set id -->
    <xsl:call-template name="apply-for-phrases"/>
  </fo:block>
</xsl:template>


<xsl:template match="*[contains(@class,' topic/ddhd ')]">
  <fo:block xsl:use-attribute-sets="dd" font-weight="bold">
    <!-- setclass -->
    <!-- set id -->
    <xsl:call-template name="apply-for-phrases"/>
  </fo:block>
</xsl:template>


<xsl:template match="*[contains(@class,' topic/dlentry ')]">
  <fo:block>
    <xsl:apply-templates/>
  </fo:block>
</xsl:template>



<xsl:template match="*[contains(@class,' topic/dt ')]">
  <fo:block xsl:use-attribute-sets="dt">
    <!-- setclass -->
    <!-- set id -->
    <xsl:choose>
      <xsl:when test="*"> <!-- tagged content - do not default to bold -->
        <xsl:apply-templates/>
      </xsl:when>
      <xsl:otherwise>
        <fo:inline font-weight="bold"><xsl:call-template name="apply-for-phrases"/></fo:inline> <!-- text only - bold it -->
      </xsl:otherwise>
    </xsl:choose>
  </fo:block>
</xsl:template>


<xsl:template match="*[contains(@class,' topic/dd ')]">
  <xsl:variable name="list-level" 
    select="count(ancestor-or-self::*[contains(@class,' topic/ul ')] | 
                  ancestor-or-self::*[contains(@class,' topic/dl ')] |
                  ancestor-or-self::*[contains(@class,' topic/sl ')] |
                  ancestor-or-self::*[contains(@class,' topic/ol ')] )" />
  <xsl:variable name="extra-list-indent"><xsl:value-of select="number($list-level)*16"/>pt</xsl:variable>
  <fo:block>
    <xsl:attribute name="start-indent"><xsl:value-of select="$basic-start-indent"/> + <xsl:value-of select="$extra-list-indent"/></xsl:attribute>
<!--
 start-indent="{$basic-start-indent} + {
                   count(ancestor-or-self::*[contains(@class,' topic/ul ')]) +
                   count(ancestor-or-self::*[contains(@class,' topic/ol ')]) +
                   count(ancestor-or-self::*[contains(@class,' topic/dl ')])}em"
-->
    <!-- setclass -->
    <!-- set id -->
    <xsl:apply-templates />
  </fo:block>
</xsl:template>

<!-- case of dl within a table cell -->
<xsl:template match="*[contains(@class,' topic/entry ')]//*[contains(@class,' topic/dd ')]">
  <fo:block xsl:use-attribute-sets="dd.cell">
    <!-- setclass -->
    <!-- set id -->
    <xsl:apply-templates />
  </fo:block>
</xsl:template>

<!-- case of dl within a simpletable cell -->
<xsl:template match="*[contains(@class,' topic/stentry ')]//*[contains(@class,' topic/dd ')]" priority="2">
  <fo:block xsl:use-attribute-sets="dd.cell">
    <!-- setclass -->
    <!-- set id -->
    <xsl:apply-templates />
  </fo:block>
</xsl:template>

<!-- =================== end of element rules ====================== -->


</xsl:stylesheet>
